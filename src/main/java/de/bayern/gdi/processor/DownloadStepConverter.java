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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.model.Parameter;
import de.bayern.gdi.utils.StringUtils;

/** Make DownloadStep configurations suitable for the download processor. */
public class DownloadStepConverter {

    private static final Log log =
        LogFactory.getLog(DownloadStepConverter.class);


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

    private static final SimpleDateFormat DF_FORMAT
        = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final int MAX_TRIES = 1000;

    private static final String PREFIX = "gdibydl-";

    // XXX: This is pontentially racy!!!
    private static File createWorkingDir(File parent) {

        Date now = new Date();

        String dir = PREFIX + DF_FORMAT.format(now);
        File path = new File(parent, dir);
        int count = 0;
        while (count < MAX_TRIES && path.exists()) {
            ++count;
            dir = PREFIX + DF_FORMAT.format(now) + "-" + count;
            path = new File(parent, dir);
        }

        return count < MAX_TRIES && path.mkdirs() ? path : null;
    }


    /**
     * Converts a DownloadStep into a sequence of jobs for the processor.
     * @param dls DownloadStep the configuration to be converted.
     * @return A job list for the download processor.
     * @throws ConverterException If the conversion went wrong.
     */
    public static JobList convert(DownloadStep dls) throws ConverterException {
        JobList jl = new JobList();

        String url = wfsURL(dls);

        log.info("url: " + url);

        //XXX: Alternative ways of getting the credentials?
        String user = dls.findParameter("user");
        String password = dls.findParameter("password");

        File path = new File(dls.getPath());
        File wd = createWorkingDir(
            path.isDirectory() ? path : path.getParentFile());

        File gml = new File(wd, "download.gml");
        log.info("Download to file \"" + gml + "\"");
        FileDownloadJob fdj = new FileDownloadJob(url, gml, user, password);
        jl.addJob(fdj);

        jl.addJob(new GMLCheckJob(gml));
        // TODO: Add transformation job.
        return jl;
    }
}
