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

import de.bayern.gdi.model.DownloadStep;

/** Make DownloadStep configurations suitable for the download processor. */
public class DownloadStepConverter {

    private DownloadStepConverter() {
    }

    /**
     * Converts a DownloadStep into a sequence of jobs for the processor.
     * @param dls DownloadStep the configuration to be converted.
     * @return A job list for the download processor.
     */
    public static JobList convert(DownloadStep dls) {
        // TODO: Implement me!
        return new JobList();
    }
}
