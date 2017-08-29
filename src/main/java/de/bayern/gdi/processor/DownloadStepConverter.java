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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPathConstants;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
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

/** Make DownloadStep configurations suitable for the download processor. */
public class DownloadStepConverter {

    private static final Logger log
        = Logger.getLogger(FileDownloadJob.class.getName());

    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;

    private static final String[][] SERVICE2TYPE = {
        {"ATOM", "STOREDQUERY_ID"}, // XXX: NOT CORRECT!
        {"WFS2_BASIC", "DATASET"},
        {"WFS2_SIMPLE", "STOREDQUERY_ID"},
        {"WFS1", "DATASET"},
        {"WFS", "DATASET"}
    };

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
            // TODO: i18n
            throw new ConverterException(
                "Cannot create unique download filename");
        }

        this.logger = new Log(dlLog);

        OpenLogJob olj = new OpenLogJob(this.logger);
        CloseLogJob clj = new CloseLogJob(this.logger);

        LogMetaJob lmj = new LogMetaJob(this.logger, dls);

        FileTracker fileTracker = new FileTracker(path);
        if (!fileTracker.scan()) {
            // TODO: i18n
            throw new ConverterException(
                "Inital scan of download file failed.");
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


    private static String findQueryType(String type) {
        String t = type.toUpperCase();
        for (String []pair: SERVICE2TYPE) {
            if (t.equals(pair[0])) {
                return pair[1];
            }
        }
        return type;
    }

    private static String encodeParameters(
        ArrayList<Parameter> parameters,
        Set<String>          usedVars
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
    ) throws ConverterException {

        String url = dls.getServiceURL();
        String base = baseURL(url);
        String vendor = vendorSpecific(url);
        String getFeaturesURL = meta.findOperation("GetFeature").get;
        if (getFeaturesURL.startsWith("/")) {
            base += getFeaturesURL;
        } else {
            base = getFeaturesURL;
        }

        String version = StringUtils.urlEncode(
            meta.highestVersion(WFSMeta.WFS2_0_0).toString());

        String dataset = dls.getDataset();
        String queryType = findQueryType(dls.getServiceType());

        StringBuilder sb = new StringBuilder();
        sb.append(base);
        if (!base.endsWith("?")) {
            sb.append("?");
        }
        sb.append("service=wfs&")
          .append("request=GetFeature&")
          .append("version=").append(version);

        if (queryType.equals("STOREDQUERY_ID")) {
            sb.append("&STOREDQUERY_ID=")
              .append(StringUtils.urlEncode(dataset));
        } else {
            sb.append("&typeNames=")
              .append(StringUtils.urlEncode(dataset));
        }

        int idx = dataset.indexOf(':');
        if (idx >= 0) {
            String prefix = dataset.substring(0, idx);
            String ns = meta.namespaces.getNamespaceURI(prefix);
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
    private static String createWFSPostParams(
        DownloadStep dls,
        Set<String> usedVars,
        WFSMeta meta) {
        return createWFSPostParams(dls, usedVars, meta, false, -1, -1, false);
    }
    private static String createWFSPostParams(
        DownloadStep dls,
        Set<String> usedVars,
        WFSMeta meta,
        int ofs,
        int count,
        boolean wfs2) {
        return createWFSPostParams(dls, usedVars, meta,
                false, ofs, count, wfs2);
    }

    private static String createWFSPostParams(
        DownloadStep dls,
        Set<String> usedVars,
        WFSMeta meta,
        boolean hits) {
        return createWFSPostParams(dls, usedVars, meta, hits, -1, -1, false);
    }

    private static String createWFSPostParams(
        DownloadStep dls,
        Set<String>  usedVars,
        WFSMeta      meta,
        boolean      hits,
        int          ofs,
        int          count,
        boolean      wfs2) {

        String url = dls.getServiceURL();
        String base = baseURL(url);
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

        String version = meta.highestVersion(WFSMeta.WFS2_0_0).toString();
        String dataset = dls.getDataset();
        String queryType = findQueryType(dls.getServiceType());
        String outputFormat = "";
        String srsName = "";
        String typenames = "";
        String namespaces = "";
        String storedQueryId = "";
        String bbox = "";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

        boolean storedQuery = false;
        if (queryType.equals("STOREDQUERY_ID")) {
            storedQueryId = dataset;
            storedQuery = true;
        } else {
            storedQuery = false;
            typenames = dataset;
        }

        int idx = dataset.indexOf(':');
        if (idx >= 0) {
            String prefix = dataset.substring(0, idx);
            String ns = meta.namespaces.getNamespaceURI(prefix);
            namespaces = ns;
        }

        if (dls.getParameters().size() > 0) {
            for (Parameter p: dls.getParameters()) {
                if (p.getValue().isEmpty()
                || usedVars.contains(p.getKey())) {
                    continue;
                }
                switch (p.getKey()) {
                    case "outputformat":
                        outputFormat = p.getValue();
                        break;
                    case "srsName":
                        srsName = p.getValue();
                        break;
                    case "bbox":
                        bbox = p.getValue();
                        break;
                    default:
                        params.add(new BasicNameValuePair(
                                p.getKey(),
                                p.getValue()));
                        break;
                }
            }
        }

        String xmlStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        String xmlEnd = "";

        xmlStart += "<wfs:GetFeature "
                + " service=\"WFS\" "
                + " version=\"" + version + "\" "
                + " outputFormat=\"" + outputFormat + "\" ";
        if (hits) {
            xmlStart += "resultType=\"hits\" ";
        }
        if (ofs != -1) {
            xmlStart += "startIndex=\"" + String.valueOf(ofs) + "\" ";
            if (wfs2) {
                xmlStart += "count=\"" + String.valueOf(count) + "\" ";
            } else {
                xmlStart += "maxFeatures=\"" + String.valueOf(count) + "\" ";
            }
        }
        xmlStart += "xmlns:wfs=\"http://www.opengis.net/wfs/2.0\" "
                + "xmlns:gml=\"http://www.opengis.net/gml/3.2\" "
                + "xmlns:fes=\"http://www.opengis.net/fes/2.0\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:schemaLocation=\"http://www.opengis.net/wfs/2.0 "
                + "http://schemas.opengis.net/wfs/2.0/wfs.xsd "
                + "http://www.opengis.net/gml/3.2 "
                + "http://schemas.opengis.net/gml/3.2.1/gml.xsd\" "
                + ">";
        xmlEnd = "</wfs:GetFeature>" + xmlEnd;

        if (storedQuery) {
            xmlStart += "<wfs:StoredQuery id =\"" + storedQueryId + "\">";
            xmlEnd = "</wfs:StoredQuery>" + xmlEnd;
            for (NameValuePair p: params) {
                xmlStart += "<wfs:Parameter name=\"" + p.getName() + "\">"
                         + p.getValue()
                         + "</wfs:Parameter>";
            }
        } else {
            xmlStart += "<wfs:Query typeNames=\"" + typenames
                     + "\" xmlns:bvv=\"" + namespaces
                     + "\" srsName=\"" + srsName + "\">";
            xmlEnd = "</wfs:Query>" + xmlEnd;
            String[] bboxArr = bbox.split(",");
            if (bboxArr.length == FIVE) {
                xmlStart += " <fes:Filter> <fes:BBOX> "
                        + "<gml:Envelope srsName=\"" + bboxArr[FOUR] + "\">"
                        + "<gml:lowerCorner>" + bboxArr[0] + " "
                        + bboxArr[1] + "</gml:lowerCorner>"
                        + "<gml:upperCorner>" + bboxArr[2] + " "
                        + bboxArr[THREE] + "</gml:upperCorner>"
                        + "</gml:Envelope> </fes:BBOX> </fes:Filter>";
            }
        }
        xmlString = xmlStart + xmlEnd;
        return xmlString;
    }

    private void unpagedWFSDownload(
        JobList      jl,
        File         workingDir,
        Set<String>  usedVars,
        WFSMeta      meta
    ) throws ConverterException {

        boolean usePost = false;
        WFSMeta.Operation getFeature = meta.findOperation("GetFeature");
        if (getFeature.post != null) {
            usePost = true;
        }

        String url = usePost ? getFeature.post : wfsURL(dls, usedVars, meta);
        String params = null;
        if (usePost) {
            params = createWFSPostParams(dls, usedVars, meta);
        }
        log.log(Level.INFO, "url: " + url);

        String ext = extension();

        File gml = new File(workingDir, "download." + ext);
        log.info("Download to file \"" + gml + "\"");

        FileDownloadJob fdj = null;
        if (usePost) {
            try {
                HttpEntity ent =
                new StringEntity(params);
                fdj = new FileDownloadJob(
                    url, gml,
                    this.user, this.password,
                    ent,
                    this.logger);
            } catch (Exception e) {
                logger.log(e.getMessage());
            }
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

    private static final String XPATH_NUMBER_MATCHED
        = "/wfs:FeatureCollection/@numberMatched";

    private int numFeatures(String wfsURL,
            String postparams) throws ConverterException {
        URL url = null;
        HttpEntity ent = null;
        if (postparams == null) {
            url = newURL(hitsURL(wfsURL));
        } else {
            try {
                url = newURL(wfsURL);
                ent = new StringEntity(postparams);
            } catch (Exception e) {
                log.log(Level.INFO, e.getMessage());
            }
        }
        Document hitsDoc = null;
        try {
            hitsDoc = XML.getDocument(url, user, password, ent);
        } catch (URISyntaxException | IOException e) {
            throw new ConverterException(e.getMessage());
        }
        if (hitsDoc == null) {
            // TODO: I18n
            throw new ConverterException("cannot load hits document");
        }

        checkServiceException(hitsDoc);
        String numberMatchedString = (String)XML.xpath(
            hitsDoc, XPATH_NUMBER_MATCHED,
            XPathConstants.STRING,
            WFSMetaExtractor.NAMESPACES);

        if (numberMatchedString == null || numberMatchedString.isEmpty()) {
            // TODO: I18n
            throw new ConverterException("numberMatched not found");
        }
        try {
            return Integer.parseInt(numberMatchedString);
        } catch (NumberFormatException nfe) {
            // TODO: I18n
            throw new ConverterException(nfe.getMessage(), nfe);
        }
    }

    private URL pagedFeatureURL(String wfsURL, int ofs, int count, boolean wfs2)
    throws ConverterException {
        StringBuilder sb = new StringBuilder(wfsURL)
            .append("&startIndex=").append(ofs)
            .append(wfs2 ? "&count=" : "&maxFeatures=").append(count);
        return newURL(sb.toString());
    }

    private ArrayList<BasicNameValuePair> pagedFeaturePostParams(
            int ofs, int count, boolean wfs2) {
        ArrayList<BasicNameValuePair> params =
                new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("startIndex", String.valueOf(ofs)));
        if (wfs2) {
            params.add(new BasicNameValuePair("count", String.valueOf(count)));
        } else {
            params.add(new BasicNameValuePair("maxFeatures",
                    String.valueOf(count)));
        }
        return params;
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
            // TODO: I18n
            throw new ConverterException("Cannot load meta data", ioe);
        }

        WFSMeta.Operation getFeatureOp = meta.findOperation("GetFeature");
        if (getFeatureOp == null) {
            // TODO: I18n
            throw new ConverterException("'GetFeature' not supported.");
        }

        Integer fpp = getFeatureOp.featuresPerPage();
        if (fpp == null) { // Fall back to global default.
            fpp = meta.featuresPerPage();
        }

        if (fpp == null) {
            unpagedWFSDownload(jl, workingDir, usedVars, meta);
            return;
        }

        boolean usePost = false;
        String wfsURL;
        String params = null;
        HttpEntity ent = null;
        int numFeatures;
        if (getFeatureOp.post != null) {
            usePost = true;
            wfsURL = getFeatureOp.post;
            params = createWFSPostParams(dls, usedVars, meta, true);
            numFeatures = numFeatures(wfsURL, params);
        } else {
            wfsURL = wfsURL(dls, usedVars, meta);
            numFeatures = numFeatures(wfsURL(dls, usedVars, meta), null);

        }

        // Page size greater than number features -> Normal download.
        if (numFeatures < fpp) {
            try {
                ent = new StringEntity(params);
            } catch (Exception e) {
                logger.log(e.getMessage());
            }
            unpagedWFSDownload(jl, workingDir, usedVars, meta);
            return;
        }

        log.info("total number of features: " + numFeatures);

        FilesDownloadJob fdj = new FilesDownloadJob(
            this.user, this.password, this.logger);
        GMLCheckJob gcj = new GMLCheckJob(logger);

        String ext = extension();

        boolean isGML = ext.equals("gml");

        int numFiles = Math.max(1, numFeatures / fpp);
        String format = "%0" + StringUtils.places(numFiles) + "d-%d." + ext;

        boolean wfs2 =
            meta.highestVersion(WFSMeta.WFS2_0_0)
                .compareTo(WFSMeta.WFS2_0_0) >= 0;

        for (int ofs = 0, i = 0; ofs < numFeatures; ofs += fpp, i++) {
            String filename = String.format(format, i, ofs);
            File file = new File(workingDir, filename);
            log.info("download to file: " + file);
            if (!usePost) {
                fdj.add(file, pagedFeatureURL(wfsURL, ofs, fpp, wfs2));
            } else {
                try {
                    URL wfs = new URL(wfsURL);
                    String pagedParams = createWFSPostParams(
                            dls, usedVars, meta, ofs, fpp, wfs2);
                    HttpEntity pagedEnt =
                            new StringEntity(pagedParams, "UTF-8");
                    fdj.add(file, wfs, pagedEnt);

                } catch (Exception e) {
                    log.log(Level.SEVERE, e.getMessage());
                }
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
}
