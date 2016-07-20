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
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Document;

import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Log;
import de.bayern.gdi.utils.XML;

/**
 * Checks if a given set of GML files contain
 * indicators for an WFS exception.
 */
public class GMLCheckJob implements Job {

    private static final long SCREENING_THESHOLD = 4096;

    private static final String ERROR_MESSAGE
        = "//*[local-name()='Exception']"
        + "/*[local-name()='ExceptionText']/text()";

    private List<File> files;

    private Log logger;

    private static final String[] EXCEPTION_INDICATORS = {
        "ExceptionReport"
    };

    public GMLCheckJob(Log logger) {
        this.logger = logger;
        this.files = new ArrayList<>();
    }

    public GMLCheckJob(File file, Log logger) {
        this.logger = logger;
        this.files = new ArrayList<>();
        this.files.add(file);
    }

    public GMLCheckJob(List<File> files, Log logger) {
        this.logger = logger;
        this.files = files;
    }

    /** Adds a GML file to check.
     * @param file The file to check.
     */
    public void add(File file) {
        this.files.add(file);
    }

    private static void checkForProblems(File file)
    throws JobExecutionException {
        Document doc = XML.getDocument(file);
        if (doc == null) {
            throw new JobExecutionException(
                I18n.format("gml.check.parsing.failed", file));
        }
        String message = XML.xpathString(doc, ERROR_MESSAGE, null);
        if (message != null && !message.isEmpty()) {
            throw new JobExecutionException(
                I18n.format("gml.check.wfs.problem", message));
        }
    }

    @Override
    public void run(Processor p) throws JobExecutionException {

        for (File file: this.files) {
            p.broadcastMessage(I18n.format("gml.check.start", file));

            if (!file.isFile() || !file.canRead()) {
                throw new JobExecutionException(
                    I18n.format("gml.check.not.accessible", file));
            }

            // If the document is large screen for
            // indicators of ExceptionReport first to avoid
            // building a large in memory DOM.
            if (file.length() > SCREENING_THESHOLD) {
                try {
                    if (XML.containsTags(file, EXCEPTION_INDICATORS) == null) {
                        // No indicators no cry ...
                        p.broadcastMessage(I18n.getMsg("gml.check.passed"));
                        return;
                    }
                } catch (XMLStreamException | IOException e) {
                    throw new JobExecutionException(
                        I18n.format("gml.check.processing.failed", file));
                }
            }
            checkForProblems(file);
            p.broadcastMessage(I18n.getMsg("gml.check.passed"));
        }
    }
}
