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

/**
 * An instance of this class manages the application and service settings.
 * The settings are read from an XML config file settings.xml. A default configuration
 * is provided with the application. This default settings.xml is read only.
 * An external settings.xml file can be specified and in this case changes
 * of the application settings are written to this file.
 *
 * @author Alexander Woestmann (awoestmann@intevation)
 */

public class Settings {

    private static final Logger LOG = LoggerFactory.getLogger(Settings.class.getName());

    /** Name of the config file. */
    public static final String SETTINGS_FILE = "settings.xml";

    /** Name of the settings type. */
    private static final String NAME = "Settings";

    /** The settings.xml file, can be null. */
    private File file;

    private ServiceSettings serviceSettings;

    private ApplicationSettings applicationSettings;

    /**
     * Return class name.
     * @return The name
     */
    public static String getName() {
        return NAME;
    }

    /**
     * Default constructor with r/o settings.xml from class path.
     *
     * @throws SAXException if the XML is invalid
     * @throws ParserConfigurationException if the XML parser config is invalid
     * @throws IOException if the file is not accessible
     */
    public Settings()
            throws SAXException, ParserConfigurationException, IOException {
        this(XML.getDocument(Misc.getResource(SETTINGS_FILE)));
    }

    /**
     * Creates an instance of Settings which are read from the file system and the settings.xml is writable.
     *
     * @param file the settings.xml file, if not null the settings.xml is writable
     * @throws SAXException if the XML is invalid
     * @throws ParserConfigurationException if the XML parser config is invalid
     * @throws IOException if the file is not accessible
     */
    public Settings(File file)
        throws SAXException, ParserConfigurationException, IOException {
        this(XML.getDocument(file));
        this.file = file;
    }

    /**
     * Constructor reads service and application settings from given document.
     *
     * @param doc the DOM representation of settings.xml
     * @throws IOException if an error occurs getting the sections from the document.
     */
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

    /**
     * Persist the passed document as settings.xml in case the file is writable.
     *
     * @param doc to persist, should not be <code>null</code>
     * @see #file
     */
    public void persistSettingsFile(Document doc) {
        if (this.file == null) {
            LOG.warn("Not saving settings.xml. File is not defined!");
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
