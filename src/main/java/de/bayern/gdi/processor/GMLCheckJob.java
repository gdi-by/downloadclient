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
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import de.bayern.gdi.utils.XML;

/**
 * Checks is a given GML file contains indicators for an exception.
 */
public class GMLCheckJob implements Job {

    private static final Logger log
        = Logger.getLogger(GMLCheckJob.class.getName());

    private File file;

    private static final String[] EXCEPTION_INDICATORS = {
        "ExceptionReport"
    };

    public GMLCheckJob() {
    }

    public GMLCheckJob(File file) {
        this.file = file;
    }

    @Override
    public void run() throws JobExecutionException {
        log.info("Checking: \"" + this.file + "\"");
        if (!file.exists() || !file.canRead()) {
            throw new JobExecutionException(
                "file \"" + file + "\" is not accessible.");
        }

        try {
            String tag = XML.containsTags(this.file, EXCEPTION_INDICATORS);
            if (tag != null) {
                throw new JobExecutionException(
                    "Exception indicator found: \"" + tag + "\"");
            }
        } catch (XMLStreamException | IOException e) {
            throw new JobExecutionException(
                "processing file \"" + this.file + "\" failed.", e);
        }
    }
}
