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

import de.bayern.gdi.utils.FileTracker;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Log;
import de.bayern.gdi.utils.XML;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Checks if a given set of GML files contains
 * data for further processing.
 */
public class GmlEmptyCheckJob implements Job {

    private static final String XPATH_NUMBER_FEATURES
        = "count(//*[local-name()='FeatureCollection']/*)";

    private Log logger;

    private final FileTracker fileTracker;

    public GmlEmptyCheckJob(Log logger, FileTracker fileTracker) {
        this.logger = logger;
        this.fileTracker = fileTracker;
    }

    private void log(String msg) {
        if (this.logger != null) {
            this.logger.log(msg);
        }
    }

    private void checkIfEmpty(File file)
    throws JobExecutionException, SAXException,
            ParserConfigurationException, IOException {
        Document doc = XML.getDocument(file);

        String numberReturnedString = (String)XML.xpath(
            doc, XPATH_NUMBER_FEATURES,
            XPathConstants.STRING);
        if (asInt(numberReturnedString) == 0) {
            String msg = I18n.format("gml.empty.check.isempty");
            log(msg);
            throw new JobExecutionException(msg);
        }
    }

    @Override
    public void run(Processor p) throws JobExecutionException {
        fileTracker.scan();
        List<File> files = fileTracker.globalGlob("*.{gml}");
        if (files.size() == 1) {
            File file = files.get(0);
            p.broadcastMessage(I18n.format("gml.empty.check.start", file));
            try {
                checkIfEmpty(file);
            } catch (SAXException
                    | ParserConfigurationException
                    | IOException e) {
                throw new JobExecutionException(e.getMessage());
            }
            String msg = I18n.getMsg("gml.empty.check.passed");
            log(msg);
            p.broadcastMessage(msg);
        }
    }

    private int asInt(String numberReturnedString) {
        if (numberReturnedString != null && !numberReturnedString.isEmpty()) {
            try {
                return Integer.parseInt(numberReturnedString);
            } catch (NumberFormatException nfe) {
            }
        }
        return -1;
    }

}
