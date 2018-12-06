/*
 * DownloadClient Geodateninfrastruktur Bayern
 *
 * (c) 2016 GSt. GDI-BY (gdi.bayern.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bayern.gdi.processor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;

import org.apache.http.HttpEntity;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.EntityTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.model.Parameter;
import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.services.WFSMetaExtractor;
import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.FileTracker;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Log;
import de.bayern.gdi.utils.Misc;
import de.bayern.gdi.utils.StringUtils;
import de.bayern.gdi.utils.XML;

import static de.bayern.gdi.processor.DownloadStepConverter.QueryType.DATASET;
import static de.bayern.gdi.processor.DownloadStepConverter.
    QueryType.SQLQUERY;
import static de.bayern.gdi.processor.DownloadStepConverter.
    QueryType.STOREDQUERY;

/** Make DownloadStep configurations suitable for the download processor. */
public class DownloadStepConverter {

    private static final Logger LOG
        = LoggerFactory.getLogger(DownloadStepConverter.class.getName());

    /**
     * Type of a Query.
     */
    public enum QueryType {
        /**
         * StoredQuery.
         */
        STOREDQUERY,
        /**
         * Dataset.
         */
        DATASET,
        /**
         * SQL Query.
         */
        SQLQUERY }

    private static final String GETFEATURE = "GetFeature";

    private String user;
    private String password;

    private DownloadStep dls;

    private Log logger;

    public DownloadStepConverter() {
    }

    /**
     * Creates a converter with given user name and password.
     * @param user The user name.
     * @param password The password.
     */
    public DownloadStepConverter(String user, String password) {
        this.user = user;
        this.password = password;
    }

    /**
     * Converts a DownloadStep into a sequence of jobs for the processor.
     * @param downloadSteps DownloadStep the configuration to be converted.
     * @return A job list for the download processor.
     * @throws ConverterException If the conversion went wrong.
     */
    public JobList convert(DownloadStep downloadSteps)
        throws ConverterException {

        this.dls = downloadSteps;

        ProcessingStepConverter psc = new ProcessingStepConverter();

        File path = new File(dls.getPath());

        if (!path.exists()) {
            if (!path.mkdirs()) {
                throw new ConverterException(
                    I18n.format("dls.converter.cant.create.dir", path));
            }
        } else if (!path.isDirectory()) {
            throw new ConverterException(
                I18n.format("dls.converter.not.dir", path));
        }

        File dlLog = Misc.uniqueFile(path, "download-", "log", null);
        if (dlLog == null) {
            throw new ConverterException(
                I18n.getMsg("dls.converter.no.unique.filename"));
        }

        this.logger = new Log(dlLog);

        OpenLogJob olj = new OpenLogJob(this.logger);
        CloseLogJob clj = new CloseLogJob(this.logger);

        LogMetaJob lmj = new LogMetaJob(this.logger, dls);

        FileTracker fileTracker = new FileTracker(path);
        if (!fileTracker.scan()) {
            throw new ConverterException(
                I18n.getMsg("dls.converter.init.scan.failed"));
        }

        psc.convert(dls, fileTracker, logger);

        JobList jl = new JobList();

        jl.addJob(olj);
        jl.addJob(lmj);

        if (dls.getServiceType().equals("ATOM")) {
            createAtomDownload(jl, path);
        } else {
            createWFSDownload(jl, path, psc.getUsedVars());
        }

        jl.addJobs(psc.getJobs());

        jl.addJob(clj);

        return jl;
    }


    /**
     * Finds WFS service type for given dataset.
     * @param type The dataset type.
     * @return The query type.
     */
    protected static QueryType findQueryType(String type) {

        switch (type) {
            case "ATOM":
            case "WFS2_SIMPLE":
                return STOREDQUERY;
            case "WFS2_SQL":
                return SQLQUERY;
            default:
                return DATASET;
        }
    }

    private static String encodeParameters(
        List<Parameter> parameters,
        Set<String>     usedVars
    ) {
        StringBuilder sb = new StringBuilder();
        for (Parameter p: parameters) {
            if (p.getValue().isEmpty()
            || usedVars.contains(p.getKey())) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(StringUtils.urlEncode(p.getKey()))
              .append('=')
              .append(StringUtils.urlEncode(p.getValue()));
        }
        return sb.toString();
    }

    private static final String[] IGNORE_WFS = {
        "service=",
        "request=",
        "acceptversions="
    };

    private static String vendorSpecific(String url) {
        int idx = url.lastIndexOf('?');
        if (idx < 0) {
            return "";
        }
        return StringUtils.ignorePartsWithPrefix(
            url.substring(idx + 1),
            "&",
            IGNORE_WFS);
    }

    private static String baseURL(String url) {
        int idx = url.indexOf('?');
        return idx >= 0 ? url.substring(0, idx) : url;

    }

    private static String capURL(String base) {
        return base
            + "?service=WFS&request=GetCapabilities&acceptversions=2.0.0";
    }

    private static String wfsURL(
        DownloadStep dls,
        Set<String>  usedVars,
        WFSMeta      meta
    ) {
        String url = dls.getServiceURL();
        String base = baseURL(url);
        String vendor = vendorSpecific(url);
        String getFeaturesURL = meta.findOperation(GETFEATURE).getGET();
        if (getFeaturesURL.startsWith("/")) {
            base += getFeaturesURL;
        } else {
            base = getFeaturesURL;
        }

        String version = StringUtils.urlEncode(
            meta.highestVersion(WFSMeta.WFS2_0_0).toString());

        String dataset = dls.getDataset();
        QueryType queryType = findQueryType(dls.getServiceType());

        StringBuilder sb = new StringBuilder();
        sb.append(base);
        if (!base.endsWith("?")) {
            sb.append("?");
        }
        sb.append("service=wfs&")
          .append("request=GetFeature&")
          .append("version=").append(version);

        if (queryType.equals(STOREDQUERY)) {
            sb.append("&STOREDQUERY_ID=")
              .append(StringUtils.urlEncode(dataset));
        } else {
            sb.append("&typeNames=")
              .append(StringUtils.urlEncode(dataset));
        }

        int idx = dataset.indexOf(':');
        if (idx >= 0) {
            String prefix = dataset.substring(0, idx);
            String ns = meta.getNamespaces().getNamespaceURI(prefix);
            sb.append("&namespaces=xmlns(")
                .append(StringUtils.urlEncode(prefix)).append(',')
                .append(StringUtils.urlEncode(ns)).append(')');
        }

        String parameters = encodeParameters(dls.getParameters(), usedVars);
        if (parameters.length() > 0) {
            sb.append('&').append(parameters);
        }

        if (!vendor.isEmpty()) {
            sb.append('&').append(vendor);
        }

        return sb.toString();
    }

    private void unpagedWFSDownload(
        JobList      jl,
        File         workingDir,
        Set<String>  usedVars,
        WFSMeta      meta
    ) throws ConverterException {
        WFSMeta.Operation getFeature = meta.findOperation(GETFEATURE);

        boolean usePost = getFeature.getPOST() != null;

        String url = usePost
            ? getFeature.getPOST()
            : wfsURL(dls, usedVars, meta);

        Document params = usePost
            ? WFSPostParamsBuilder.create(dls, usedVars, meta)
            : null;

        LOG.info("url: {}", url);

        String ext = extension();

        File gml = new File(workingDir, "download." + ext);
        LOG.info("Download to file '{}'", gml);

        FileDownloadJob fdj = null;
        if (usePost) {
            logGetFeatureRequest(params);
            HttpEntity ent = new EntityTemplate(
                XML.toContentProducer(params));

            fdj = new FileDownloadJob(
                url, gml,
                this.user, this.password,
                ent,
                this.logger);
        } else {
            fdj = new FileDownloadJob(
                url, gml,
                this.user, this.password,
                this.logger);
        }

        jl.addJob(fdj);
        if (ext.equals("gml")) {
            jl.addJob(new GMLCheckJob(gml, logger));
            jl.addJob(new BroadcastJob(I18n.getMsg("file.download.success")));
        }
    }

    private static URL newURL(String url) throws ConverterException {
        try {
            return new URL(url);
        } catch (MalformedURLException mfe) {
            throw new ConverterException(mfe.getMessage(), mfe);
        }
    }

    private static final String XPATH_SERVICE_EXCEPTION
        = "//ows:ExceptionReport/ows:Exception/ows:ExceptionText";

    private static void checkServiceException(Document doc)
        throws ConverterException {

        String exceptionText = (String)XML.xpath(
            doc, XPATH_SERVICE_EXCEPTION,
            XPathConstants.STRING,
            WFSMetaExtractor.NAMESPACES);

        if (exceptionText != null && !exceptionText.isEmpty()) {
            throw new ConverterException(
                I18n.format(
                    "dls.converter.wfs.exception",
                    exceptionText));
        }
    }

    private static final String[] OUTPUTFORMAT = new String[] {
        "outputformat="
    };

    private static String hitsURL(String wfsURL) {
        // outputformat parameters irritates wfs servers
        // when doing hits requests.
        int idx = wfsURL.lastIndexOf('?');
        if (idx >= 0) {
            String prefix = wfsURL.substring(0, idx + 1);
            String rest = wfsURL.substring(idx + 1);
            wfsURL = prefix + StringUtils.ignorePartsWithPrefix(
                rest, "&", OUTPUTFORMAT);
        }
        return wfsURL + "&resultType=hits";
    }

    // This is an XPATH expression and not an URI.
    @java.lang.SuppressWarnings("squid:S1075")
    private static final String XPATH_NUMBER_MATCHED
        = "/wfs:FeatureCollection/@numberMatched";

    private int numFeatures(String wfsURL, Document postparams)
        throws ConverterException {

        URL url = postparams == null
            ? newURL(hitsURL(wfsURL))
            : newURL(wfsURL);

        Document hitsDoc = null;
        try {
            HttpEntity ent = postparams == null
                ? null
                : new EntityTemplate(XML.toContentProducer(postparams));

            hitsDoc = XML.getDocument(url, user, password, ent);
        } catch (SocketTimeoutException | ConnectTimeoutException te) {
            throw new ConverterException(
                I18n.format(
                    "file.download.failed_reason",
                    I18n.getMsg("file.download.failed.timeout")),
                te);
        } catch (URISyntaxException | IOException e) {
            throw new ConverterException(e.getMessage());
        }

        if (hitsDoc == null) {
            throw new ConverterException(
                I18n.getMsg("dls.converter.no.hits.doc"));
        }

        checkServiceException(hitsDoc);
        String numberMatchedString = (String)XML.xpath(
            hitsDoc, XPATH_NUMBER_MATCHED,
            XPathConstants.STRING,
            WFSMetaExtractor.NAMESPACES);

        if (numberMatchedString == null || numberMatchedString.isEmpty()) {
            throw new ConverterException(
                I18n.getMsg("file.download.no.number"));
        }
        try {
            return Integer.parseInt(numberMatchedString);
        } catch (NumberFormatException nfe) {
            throw new ConverterException(nfe.getLocalizedMessage(), nfe);
        }
    }

    private URL pagedFeatureURL(String wfsURL, int ofs, int count, boolean wfs2)
    throws ConverterException {
        StringBuilder sb = new StringBuilder(wfsURL)
            .append("&startIndex=").append(ofs)
            .append(wfs2 ? "&count=" : "&maxFeatures=").append(count);
        return newURL(sb.toString());
    }

    private String extension() {
        String mimeType = dls.findParameter("outputformat");
        return  Config.getInstance().getMimeTypes()
            .findExtension(mimeType, "gml");
    }

    private void createWFSDownload(
        JobList      jl,
        File         workingDir,
        Set<String>  usedVars
    ) throws ConverterException {

        String url = dls.getServiceURL();
        String base = baseURL(url);
        String cap = capURL(base);
        String vendor = vendorSpecific(url);

        if (!vendor.isEmpty()) {
            cap += "&" + vendor;
        }

        WFSMetaExtractor extractor =
            new WFSMetaExtractor(cap, this.user, this.password);

        WFSMeta meta;
        try {
            meta = extractor.parse();
        } catch (URISyntaxException | IOException ioe) {
            throw new ConverterException(
                I18n.getMsg("dls.converter.no.meta.data"), ioe);
        }

        WFSMeta.Operation getFeatureOp = meta.findOperation(GETFEATURE);
        if (getFeatureOp == null) {
            throw new ConverterException(
                I18n.getMsg("dls.converter.getfeature.unsupported"));
        }

        Integer fpp = getFeatureOp.featuresPerPage();
        if (fpp == null) { // Fall back to global default.
            fpp = meta.featuresPerPage();
        }

        if (fpp == null) {
            unpagedWFSDownload(jl, workingDir, usedVars, meta);
            return;
        }

        boolean usePost = getFeatureOp.getPOST() != null;
        String wfsURL;
        Document params = null;

        int numFeatures;

        if (usePost) {
            wfsURL = getFeatureOp.getPOST();
            params = WFSPostParamsBuilder.create(dls, usedVars, meta, true);
            logGetFeatureRequest(params);
            numFeatures = numFeatures(wfsURL, params);
        } else {
            wfsURL = wfsURL(dls, usedVars, meta);
            numFeatures = numFeatures(wfsURL(dls, usedVars, meta), null);
        }

        // Page size greater than number features -> Normal download.
        if (numFeatures < fpp) {
            unpagedWFSDownload(jl, workingDir, usedVars, meta);
            return;
        }

        LOG.info("total number of features: {}", numFeatures);

        FilesDownloadJob fdj = new FilesDownloadJob(this.user, this.password);
        GMLCheckJob gcj = new GMLCheckJob(logger);

        String ext = extension();

        boolean isGML = ext.equals("gml");

        int numFiles = Math.max(1, numFeatures / fpp);
        // 1000 file -> 000, 000, ..., 999
        int places = StringUtils.places(Math.max(0, numFiles - 1));
        String format = "%0" + places + "d-%d." + ext;

        boolean wfs2 =
            meta.highestVersion(WFSMeta.WFS2_0_0)
                .compareTo(WFSMeta.WFS2_0_0) >= 0;

        for (int ofs = 0, i = 0; ofs < numFeatures; ofs += fpp, i++) {
            String filename = String.format(format, i, ofs);
            File file = new File(workingDir, filename);
            LOG.info("download to file: {}", file);
            if (!usePost) {
                fdj.add(file, pagedFeatureURL(wfsURL, ofs, fpp, wfs2));
            } else {
                URL wfs = newURL(wfsURL);
                Document pagedParams = WFSPostParamsBuilder.create(
                    dls, usedVars, meta, ofs, fpp, wfs2);
                logGetFeatureRequest(params);
                fdj.add(file, wfs,
                    new EntityTemplate(XML.toContentProducer(pagedParams)));
            }
            if (isGML) {
                gcj.add(file);
            }
        }

        jl.addJob(fdj);
        jl.addJob(gcj);
        jl.addJob(new BroadcastJob(I18n.getMsg("file.download.success")));
    }


    private static void check(String s, String name)
    throws ConverterException {
        if (s == null || s.isEmpty()) {
            throw new ConverterException(
                I18n.format("dls.converter.missing", name));
        }
    }

    private void createAtomDownload(
        JobList jl,
        File workingDir
    ) throws ConverterException {

        String dataset = dls.getDataset();
        String url = dls.getServiceURL();
        String variation = dls.findParameter("VARIATION");

        check(url, "service url");
        check(dataset, "dataset");
        check(variation, "VARIATION");

        AtomDownloadJob job = new AtomDownloadJob(
            url,
            dataset,
            variation,
            workingDir,
            this.user, this.password,
            this.logger);
        jl.addJob(job);
    }

    private void logGetFeatureRequest(Document params) {
        try {
            String xmlAsString = XML.documentToString(params);
            LOG.info("WFS GetFeature Request: {}", xmlAsString);
        } catch (TransformerException e) {
            // nothing to do
        }
        LOG.info("WFS GetFeature Request cannot be logged");
    }
}
