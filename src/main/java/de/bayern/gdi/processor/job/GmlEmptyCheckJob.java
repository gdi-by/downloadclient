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
package de.bayern.gdi.processor.job;

import de.bayern.gdi.processor.JobExecutionException;
import de.bayern.gdi.processor.Processor;
import de.bayern.gdi.utils.FileTracker;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Log;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Checks if a given set of GML files contains
 * data for further processing.
 */
public class GmlEmptyCheckJob implements Job {

    private Log logger;

    private final FileTracker fileTracker;

    public GmlEmptyCheckJob(Log logger, FileTracker fileTracker) {
        this.logger = logger;
        this.fileTracker = fileTracker;
    }

    @Override
    public void run(Processor p) throws JobExecutionException {
        List<File> files = retrieveGmlFiles(p);
        if (files.size() == 1) {
            File file = files.get(0);
            p.broadcastMessage(I18n.format("gml.empty.check.start", file));
            try {
                checkIfEmpty(file);
            } catch (IOException | XMLStreamException e) {
                throw new JobExecutionException(e.getMessage());
            }
            String msg = I18n.getMsg("gml.empty.check.passed");
            log(msg);
            p.broadcastMessage(msg);
        }
    }

    private void checkIfEmpty(File file)
        throws JobExecutionException, IOException, XMLStreamException {
        try (FileInputStream fis = new FileInputStream(file)) {
            XMLStreamReader xmlStreamReader = XMLInputFactory.newFactory().createXMLStreamReader(fis);
            boolean isFeatureCollection = false;
            while (xmlStreamReader.hasNext()) {
                xmlStreamReader.next();
                if (xmlStreamReader.isStartElement()) {
                    String localName = xmlStreamReader.getLocalName();
                    if ("FeatureCollection".equals(localName)) {
                        isFeatureCollection = true;
                    }
                    if (isFeatureCollection
                        && ("member".equals(localName) || "featureMember".equals(localName))) {
                        return;
                    }
                }
            }
            String msg = I18n.format("gml.empty.check.isempty");
            log(msg);
            throw new JobExecutionException(msg);
        }
    }

    private List<File> retrieveGmlFiles(Processor p)
        throws JobExecutionException {
        List<File> files = fileTracker.retrieveFilesWithoutScan("*.{gml}");
        if (files == null) {
            String msg = I18n.format(
                "external.process.scan.dir.failed",
                this.fileTracker.getDirectory());
            JobExecutionException jee = new JobExecutionException(msg);
            log(jee.getMessage());
            throw jee;
        }
        return files;
    }

    private void log(String msg) {
        if (this.logger != null) {
            this.logger.log(msg);
        }
    }

}
