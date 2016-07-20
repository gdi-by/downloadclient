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

import java.io.IOException;

import de.bayern.gdi.utils.Log;

/**
 * A job to open a log file.
 */
public class OpenLogJob implements Job {

    private Log logger;

    public OpenLogJob(Log logger) {
        this.logger = logger;
    }

    @Override
    public void run(Processor p) throws JobExecutionException {
        try {
            logger.open();
        } catch (IOException ioe) {
            // TODO: i18n
            throw new JobExecutionException("Cannot open log file", ioe);
        }
    }
}

