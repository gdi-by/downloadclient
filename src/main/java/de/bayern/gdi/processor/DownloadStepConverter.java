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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.model.Parameter;
import de.bayern.gdi.model.ProcessingConfiguration;
import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.services.WFSMetaExtractor;
import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.FeatureGuesser;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.NumberMatched;
import de.bayern.gdi.utils.StringUtils;

/** Make DownloadStep configurations suitable for the download processor. */
public class DownloadStepConverter {

    private static final Logger log
        = Logger.getLogger(FileDownloadJob.class.getName());

    private static ProcessingConfiguration processingConfig;

    private DownloadStepConverter() {
    }

    private static final String[][] SERVICE2TYPE = {
        {"ATOM", "STOREDQUERY_ID"}, // XXX: NOT CORRECT!
        {"WFS2_BASIC", "DATASET"},
        {"WFS2_SIMPLE", "STOREDQUERY_ID"},
        {"WFS1", "DATASET"},
        {"WFS", "DATASET"}
    };

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
            if (usedVars.contains(p.getKey())
            || p.getKey().equals("user")
            || p.getKey().equals("password")) {
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
        return base + "?service=WFS&request=GetCapabilities&version=2.0.0";
    }

    private static String wfsURL(
        DownloadStep dls,
        Set<String>  usedVars,
        WFSMeta      meta
    ) throws ConverterException {

        String url = dls.getServiceURL();
        String base = baseURL(url);

        String version = StringUtils.urlEncode(meta.highestVersion("2.0.0"));

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

        /*
        String bbox = dls.findParameter("bbox");
        if (bbox != null) {
            sb.append("&bbox=").append(StringUtils.urlEncode(bbox));
        }

        String srsName = dls.findParameter("srsName");
        if (srsName != null) {
            sb.append("&srsName=").append(StringUtils.urlEncode(srsName));
        }
        */
        return sb.toString();
    }

    private static void unpagedWFSDownload(
        JobList      jl,
        File         workingDir,
        Set<String>  usedVars,
        DownloadStep dls,
        WFSMeta      meta
    ) throws ConverterException {

        String url = wfsURL(dls, usedVars, meta);
        log.log(Level.INFO, "url: " + url);

        File gml = new File(workingDir, "download.gml");
        log.info("Download to file \"" + gml + "\"");

        String user     = dls.findParameter("user");
        String password = dls.findParameter("password");

        FileDownloadJob fdj = new FileDownloadJob(url, gml, user, password);
        jl.addJob(fdj);
        jl.addJob(new GMLCheckJob(gml));
    }

    private static URL newURL(String url) throws ConverterException {
        try {
            return new URL(url);
        } catch (MalformedURLException mfe) {
            throw new ConverterException(mfe.getMessage(), mfe);
        }
    }

    private static void createWFSDownload(
        JobList      jl,
        File         workingDir,
        Set<String>  usedVars,
        DownloadStep dls
    ) throws ConverterException {

        String user     = dls.findParameter("user");
        String password = dls.findParameter("password");

        String url = dls.getServiceURL();
        String base = baseURL(url);
        String cap = capURL(base);

        WFSMetaExtractor extractor = new WFSMetaExtractor(cap, user, password);

        WFSMeta meta;
        try {
            meta = extractor.parse();
        } catch (IOException ioe) {
            // TODO: I18n
            throw new ConverterException("Cannot load meta data", ioe);
        }

        Integer fpp = meta.findOperation("GetFeature").featuresPerPage();

        if (fpp == null) {
            unpagedWFSDownload(jl, workingDir, usedVars, dls, meta);
            return;
        }

        String wfsURL = wfsURL(dls, usedVars, meta);
        NumberMatched nm = new NumberMatched(wfsURL, user, password);
        int numFeatures;
        try {
            numFeatures = nm.numFeatures(0, fpp);
        } catch (Exception e) {
            throw new ConverterException(e.getMessage(), e);
        }
        // Page size greater than number features -> Normal download.
        if (numFeatures < fpp) {
            unpagedWFSDownload(jl, workingDir, usedVars, dls, meta);
            return;
        }
        // Real paging. Figure out the real number of features.
        FeatureGuesser guesser = new FeatureGuesser(fpp);
        try {
            numFeatures = guesser.totalNumFeatures(nm);
        } catch (Exception e) {
            throw new ConverterException(e.getMessage(), e);
        }
        log.info("total number of features: " + numFeatures);

        FilesDownloadJob fdj = new FilesDownloadJob(user, password);
        GMLCheckJob gcj = new GMLCheckJob();

        int numFiles = Math.max(1, numFeatures / fpp);
        String format = "%0" + StringUtils.places(numFiles) + "d-%d.gml";

        for (int ofs = 0, i = 0; ofs < numFeatures; ofs += fpp, i++) {
            String filename = String.format(format, i, ofs);
            File file = new File(workingDir, filename);
            URL featureURL = newURL(nm.getFeatureURL(ofs, fpp));
            fdj.add(file, featureURL);
            gcj.add(file);
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

    private static void createAtomDownload(
        JobList jl,
        File workingDir,
        DownloadStep dls
    ) throws ConverterException {

        String dataset = dls.getDataset();
        String url = dls.getServiceURL();
        String variation = dls.findParameter("VARIATION");

        check(url, "service url");
        check(dataset, "dataset");
        check(variation, "VARIATION");

        String user = dls.findParameter("user");
        String password = dls.findParameter("password");

        AtomDownloadJob job = new AtomDownloadJob(
            url,
            dataset,
            variation,
            workingDir,
            user, password);
        jl.addJob(job);
    }

    /**
     * Converts a DownloadStep into a sequence of jobs for the processor.
     * @param dls DownloadStep the configuration to be converted.
     * @return A job list for the download processor.
     * @throws ConverterException If the conversion went wrong.
     */
    public static JobList convert(DownloadStep dls) throws ConverterException {

        ProcessingStepConverter psc =
            new ProcessingStepConverter(getProcessingConfiguration());

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

        psc.convert(dls, path);

        JobList jl = new JobList();


        if (dls.getServiceType().equals("ATOM")) {
            createAtomDownload(jl, path, dls);
        } else {
            createWFSDownload(jl, path, psc.getUsedVars(), dls);
        }

        jl.addJobs(psc.getJobs());

        return jl;
    }

    private static
    ProcessingConfiguration loadProcessingConfiguration() {
        InputStream in = null;
        try {
            in = DownloadStepConverter.class.getResourceAsStream(
                ProcessingConfiguration.PROCESSING_CONFIG_FILE);
            if (in == null) {
                log.log(Level.SEVERE,
                    ProcessingConfiguration.PROCESSING_CONFIG_FILE
                    + " not found");
                return new ProcessingConfiguration();
            }
            return ProcessingConfiguration.read(in);
        } catch (IOException ioe) {
            log.log(Level.SEVERE, "Failed to load configuration", ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }
        return new ProcessingConfiguration();
    }

    /**
     * Returns the processing step configuration.
     * @return the processing step configuration.
     */
    public static synchronized
    ProcessingConfiguration getProcessingConfiguration() {
        if (processingConfig == null) {
            processingConfig = Config.getInstance().getProcessingConfig();
            if (processingConfig == null) {
                processingConfig = loadProcessingConfiguration();
            }
        }
        return processingConfig;
    }
}
