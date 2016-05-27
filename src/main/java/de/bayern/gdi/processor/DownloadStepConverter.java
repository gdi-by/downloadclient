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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.utils.StringUtils;

/** Make DownloadStep configurations suitable for the download processor. */
public class DownloadStepConverter {

    private static final Log log =
        LogFactory.getLog(DownloadStepConverter.class);


    private DownloadStepConverter() {
    }

    private static final String[][] WFS_TABLE = {
        {"WFS2_SIMPLE", "2.0"},
        {"WFS2", "2.0"},
        {"WFS", "1.0"}
    };

    private static String findWFSVersion(String wfs) {
        wfs = wfs.toUpperCase();
        for (String []pair: WFS_TABLE) {
            if (wfs.equals(pair[0])) {
                return pair[1];
            }
        }
        return "1.0";
    }

    private static String wfsURL(
        String url, String typeName, String version,
        Integer count, Integer maxFeatures,
        String bbox
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(url)
            .append('?')
            .append("service=wfs&")
            .append("request=GetFeature&")
            .append("typeNames=")
                .append(StringUtils.urlEncode(typeName)).append('&')
            .append("version=")
                .append(StringUtils.urlEncode(findWFSVersion(version)));

        if (count != null) {
            sb.append("&count=").append(count);
        }
        if (maxFeatures != null) {
            sb.append("&maxFeatures=").append(maxFeatures);
        }
        if (bbox != null) {
            sb.append("&bbox=").append(StringUtils.urlEncode(bbox));
        }
        return sb.toString();
    }

    private static Integer toInteger(String value)
    throws ConverterException {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException nfe) {
            throw new ConverterException(
                "\"" + value + "\" is not an integer.", nfe);
        }
    }

    /**
     * Converts a DownloadStep into a sequence of jobs for the processor.
     * @param dls DownloadStep the configuration to be converted.
     * @return A job list for the download processor.
     * @throws ConverterException If the conversion went wrong.
     */
    public static JobList convert(DownloadStep dls) throws ConverterException {
        JobList jl = new JobList();

        String url = wfsURL(
            dls.getServiceURL(),
            dls.getDataset(),
            dls.getServiceType(),
            toInteger(dls.findParameter("count")),
            toInteger(dls.findParameter("maxFeatures")),
            dls.findParameter("bbox"));

        log.info("url: " + url);

        String user = null; // TODO: From parameters.
        String password = null; // TODO: From parameters.

        File path = new File(dls.getPath());
        if (path.isDirectory()) {
            // TODO: Make file unique.
            path = new File(path, "download.gml");
        }

        FileDownloadJob fdj = new FileDownloadJob(url, path, user, password);
        jl.addJob(fdj);

        jl.addJob(new GMLCheckJob(path));
        // TODO: Add transformation job.
        return jl;
    }
}
