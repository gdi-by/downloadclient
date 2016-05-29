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

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Document;

import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.XML;

/**
 * Checks is a given GML file contains indicators for an exception.
 */
public class GMLCheckJob implements Job {

    private static final long SCREENING_THESHOLD = 4096;

    private static final String ERROR_MESSAGE
        = "//*[local-name()='Exception']"
        + "/*[local-name()='ExceptionText']/text()";

    private File file;

    private static final String[] EXCEPTION_INDICATORS = {
        "ExceptionReport"
    };

    public GMLCheckJob() {
    }

    public GMLCheckJob(File file) {
        this.file = file;
    }

    private void checkForProblems() throws JobExecutionException {
        Document doc = XML.getDocument(this.file);
        if (doc == null) {
            throw new JobExecutionException(
                I18n.format("gml.check.parsing.failed", this.file));
        }
        String message = XML.xpathString(doc, ERROR_MESSAGE, null);
        if (message != null && !message.isEmpty()) {
            throw new JobExecutionException(
                I18n.format("gml.check.wfs.problem", message));
        }
    }

    @Override
    public void run(Processor p) throws JobExecutionException {

        p.broadcastMessage(I18n.format("gml.check.start", this.file));

        if (!this.file.isFile() || !this.file.canRead()) {
            throw new JobExecutionException(
                I18n.format("gml.check.not.accessible", this.file));
        }

        // If the document is large screen for
        // indicators of ExceptionReport first to avoid
        // building a large in memory DOM.
        if (file.length() > SCREENING_THESHOLD) {
            try {
                if (XML.containsTags(this.file, EXCEPTION_INDICATORS) == null) {
                    // No indicators no cry ...
                    p.broadcastMessage(I18n.getMsg("gml.check.passed"));
                    return;
                }
            } catch (XMLStreamException | IOException e) {
                throw new JobExecutionException(
                    I18n.format("gml.check.processing.failed", this.file));
            }
        }
        checkForProblems();
        p.broadcastMessage(I18n.getMsg("gml.check.passed"));
    }
}
