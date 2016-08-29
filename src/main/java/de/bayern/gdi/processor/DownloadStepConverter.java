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
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPathConstants;

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

        FileTracker fileTracker = new FileTracker(path);
        if (!fileTracker.scan()) {
            // TODO: i18n
            throw new ConverterException(
                "Inital scan of download file failed.");
        }

        psc.convert(dls, fileTracker, logger);

        JobList jl = new JobList();

        jl.addJob(olj);

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

        String version = StringUtils.urlEncode(
            meta.highestVersion(WFSMeta.WFS2_0_0).toString());

        String dataset = dls.getDataset();
        String queryType = findQueryType(dls.getServiceType());

        StringBuilder sb = new StringBuilder();
        sb.append(base)
          .append('?')
          .append("service=wfs&")
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

        return sb.toString();
    }

    private void unpagedWFSDownload(
        JobList      jl,
        File         workingDir,
        Set<String>  usedVars,
        WFSMeta      meta
    ) throws ConverterException {

        String url = wfsURL(dls, usedVars, meta);
        log.log(Level.INFO, "url: " + url);

        String ext = extension();

        File gml = new File(workingDir, "download." + ext);
        log.info("Download to file \"" + gml + "\"");

        FileDownloadJob fdj = new FileDownloadJob(
            url, gml,
            this.user, this.password,
            this.logger);

        jl.addJob(fdj);
        if (ext.equals("gml")) {
            jl.addJob(new GMLCheckJob(gml, logger));
        }
    }

    private static URL newURL(String url) throws ConverterException {
        try {
            return new URL(url);
        } catch (MalformedURLException mfe) {
            throw new ConverterException(mfe.getMessage(), mfe);
        }
    }

    private static final String XPATH_NUMBER_MATCHED
        = "/wfs:FeatureCollection/@numberMatched";

    private int numFeatures(String wfsURL) throws ConverterException {
        URL url = newURL(wfsURL + "&resultType=hits");
        Document hitsDoc = XML.getDocument(url, user, password);
        if (hitsDoc == null) {
            // TODO: I18n
            throw new ConverterException("cannot load hits document");
        }
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

        WFSMetaExtractor extractor =
            new WFSMetaExtractor(cap, this.user, this.password);

        WFSMeta meta;
        try {
            meta = extractor.parse();
        } catch (IOException ioe) {
            // TODO: I18n
            throw new ConverterException("Cannot load meta data", ioe);
        }

        Integer fpp = meta.findOperation("GetFeature").featuresPerPage();
        if (fpp == null) { // Fall back to global default.
            fpp = meta.featuresPerPage();
        }

        if (fpp == null) {
            unpagedWFSDownload(jl, workingDir, usedVars, meta);
            return;
        }

        String wfsURL = wfsURL(dls, usedVars, meta);
        int numFeatures = numFeatures(wfsURL);

        // Page size greater than number features -> Normal download.
        if (numFeatures < fpp) {
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
            fdj.add(file, pagedFeatureURL(wfsURL, ofs, fpp, wfs2));
            if (isGML) {
                gcj.add(file);
            }
        }

        jl.addJob(fdj);
        jl.addJob(gcj);
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
