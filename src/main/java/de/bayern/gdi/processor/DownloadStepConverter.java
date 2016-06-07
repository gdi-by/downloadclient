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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.model.Parameter;
import de.bayern.gdi.model.ProcessingConfiguration;
import de.bayern.gdi.model.ProcessingStep;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.StringUtils;

/** Make DownloadStep configurations suitable for the download processor. */
public class DownloadStepConverter {

    private static final Logger log
        = Logger.getLogger(FileDownloadJob.class.getName());

    private static ProcessingConfiguration processingConfig;

    private DownloadStepConverter() {
    }

    private static final String[][] SERVICE2VERSION = {
        {"ATOM", "2.0.0"},
        {"WFS2_BASIC", "2.0.0"},
        {"WFS2_SIMPLE", "2.0.0"},
        {"WFS", "1.1.0"}
    };

    private static final String[][] SERVICE2TYPE = {
        {"ATOM", "STOREDQUERY_ID"}, // XXX: NOT CORRECT!
        {"WFS2_BASIC", "DATASET"},
        {"WFS2_SIMPLE", "STOREDQUERY_ID"},
        {"WFS", "DATASET"}
    };

    private static String findWFSVersion(String type) {
        type = type.toUpperCase();
        for (String []pair: SERVICE2VERSION) {
            if (type.equals(pair[0])) {
                return pair[1];
            }
        }
        return "2.0.0";
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

    private static String encodeParameters(ArrayList<Parameter> parameters) {
        StringBuilder sb = new StringBuilder();
        for (Parameter p: parameters) {
            if (p.getKey().equals("user") || p.getKey().equals("password")) {
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

    private static String wfsURL(DownloadStep dls) {
        String url = dls.getServiceURL();
        String dataset = dls.getDataset();
        String queryType = findQueryType(dls.getServiceType());
        String version = findWFSVersion(dls.getServiceType());

        StringBuilder sb = new StringBuilder();
        sb.append(url)
          .append('?')
          .append("service=wfs&")
          .append("request=GetFeature&")
          .append("version=")
              .append(StringUtils.urlEncode(version));

        // TODO: handle namespaces?!

        if (queryType.equals("STOREDQUERY_ID")) {
            sb.append("&STOREDQUERY_ID=")
              .append(StringUtils.urlEncode(dataset));
        } else {
            sb.append("&typeNames=")
              .append(StringUtils.urlEncode(dataset));
        }

        String parameters = encodeParameters(dls.getParameters());
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


    private static final String OGR2OGR
        = System.getProperty("ogr2ogr", "ogr2ogr");

    private static void createProcessings(
        DownloadStep dls,
        JobList jl,
        File wd) throws ConverterException {

        ArrayList<ProcessingStep> steps = dls.getProcessingSteps();

        if (steps == null) {
            return;
        }

        for (ProcessingStep ps: steps) {
            if (ps.getName().equals("toShape")) {
                ArrayList<String> params = new ArrayList<String>();

                params.add("-f");
                params.add("ESRI Shapefile");
                params.add("download.shp");
                params.add("download.gml");

                for (Parameter p: ps.getParameters()) {
                    if (p.getKey().equals("EPSG")) {
                        params.add("-t_srs");
                        params.add(p.getValue());
                    }
                    // TODO: Handle more parameters.
                }

                ExternalProcessJob epj = new ExternalProcessJob(
                    OGR2OGR,
                    wd,
                    params.toArray(new String[params.size()]));

                jl.addJob(epj);
            }
            // TODO: Implement more steps.
        }
    }

    private static void createWfsDownload(
        JobList jl,
        File workingDir,
        String user,
        String password,
        DownloadStep dls
    ) throws ConverterException {
        String url = wfsURL(dls);
        log.log(Level.INFO, "url: " + url);

        File gml = new File(workingDir, "download.gml");
        log.log(Level.INFO, "Download to file \"" + gml + "\"");

        FileDownloadJob fdj = new FileDownloadJob(url, gml, user, password);
        jl.addJob(fdj);
        jl.addJob(new GMLCheckJob(gml));
    }

    private static void createAtomDownload(
        JobList jl,
        File workingDir,
        String user,
        String password,
        DownloadStep dls
    ) throws ConverterException {
        String dataset = dls.getDataset();
        String url = dls.getServiceURL();
        String variation = dls.findParameter("VARIATION");
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
        JobList jl = new JobList();

        //XXX: Alternative ways of getting the credentials?
        String user = dls.findParameter("user");
        String password = dls.findParameter("password");

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

        if (dls.getServiceType().equals("ATOM")) {
            createAtomDownload(jl, path, user, password, dls);
        } else {
            createWfsDownload(jl, path, user, password, dls);
        }

        createProcessings(dls, jl, path);

        return jl;
    }

    private static
    ProcessingConfiguration loadProcessingConfiguration() {
        InputStream in = null;
        try {
            in = DownloadStepConverter.class.getResourceAsStream(
                "Verarbeitungsschritte.xml");
            if (in == null) {
                log.log(Level.SEVERE, "Verarbeitungsschritte.xml not found");
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
            processingConfig = loadProcessingConfiguration();
        }
        return processingConfig;
    }
}
