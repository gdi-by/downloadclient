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

package de.bayern.gdi.config;

import de.bayern.gdi.utils.Misc;
import de.bayern.gdi.utils.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class managing all settings.
 * @author Alexander Woestmann (awoestmann@intevation)
 */

public class Settings {

    private static final Logger LOG = LoggerFactory.getLogger(Settings.class.getName());

    /** Name of the config file. */
    public static final String SETTINGS_FILE =
            "settings.xml";

    private static final String NAME =
            "Settings";

    private File file;

    private ServiceSettings serviceSettings;

    private ApplicationSettings applicationSettings;

    public Settings()
            throws SAXException, ParserConfigurationException, IOException {
        this(SETTINGS_FILE);
    }

    /**
     * Constructor.
     * @param filePath Path to the settings xml document
     */
    public Settings(String filePath)
        throws SAXException, ParserConfigurationException, IOException {
        this(new File(filePath));
    }

    public Settings(File file)
        throws SAXException, ParserConfigurationException, IOException {
        this(XML.getDocument(file));
        this.file = file;
    }

    private Settings(Document doc) throws IOException {
        this.serviceSettings = new ServiceSettings(doc);
        this.applicationSettings = new ApplicationSettings(doc, this);
    }

    /**
     * Returns service settings manager class.
     * @return Service settings
     */
    public ServiceSettings getServiceSettings() {
        return this.serviceSettings;
    }

    /**
     * Returns application settings manager class.
     * @return Application settings
     */
    public ApplicationSettings getApplicationSettings() {
        return this.applicationSettings;
    }

    private static InputStream getFileStream(String fileName) {
        return Misc.getResource(fileName);
    }

    /**
     * Return class name.
     * @return The name
     */
    public static String getName() {
        return NAME;
    }

    /**
     * Persist the passed document as settings.xml.
     *
     * @param doc to persist, should not be <code>null</code>
     */
    public void persistSettingsFile(Document doc) {
        if (this.file == null) {
            LOG.error("Failed to save settings.xml. File is not defined!");
            return;
        }
        try (FileOutputStream outputStream = new FileOutputStream(this.file)) {
            XML.printDocument(doc, outputStream);
        } catch (IOException e) {
            LOG.error("Error while saving settings.xml: ", e);
        }
        LOG.info("Successfully saved settings.xml");
    }
}
