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
package de.bayern.gdi.model;

import de.bayern.gdi.utils.ServiceSettings;
import de.bayern.gdi.utils.XML;

import junit.framework.TestCase;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Test for ServiceSettings.
 */
public class ServiceSettingsTest extends TestCase {

    public ServiceSettingsTest(String testName) {
        super(testName);
    }

    private static final String HEADER
        = "<settings>"
        + "<catalogues>"
        + "<catalog>"
        + "<name>CSW</name>"
        + "<url>http://example.com</url>"
        + "</catalog>"
        + "</catalogues>"
        + "<wms>"
        + "<service>"
        + "<name>WMS</name>"
        + "<url>http://example.com</url>"
        + "<layer>The layer</layer>"
        + "<source>The source</source>"
        + "</service>"
        + "</wms>";

    private static final String FOOTER
        = "</settings>";

    private static final String CORRECT_CHECK_GET
        = HEADER
        + "<check-restriction>"
        + "<use-get-url>(https|http)://example\\.com(/.*)?</use-get-url>"
        + "<use-get-url>http://example\\.org(/.*)?</use-get-url>"
        + "</check-restriction>"
        + FOOTER;

    private static final String WRONG_CHECK_GET
        = HEADER
        + "<check-restriction>"
        + "<use-get-url>http://example\\.org(/.*)?</use-get-url>"
        + "<use-get-url>(</use-get-url>"
        + "</check-restriction>"
        + FOOTER;

    private static final String EMPTY_CHECK_GET
        = HEADER
        + FOOTER;

    private static final String BASEDIR
        = "<basedir>/var/tmp</basedir>";

    private static final String EMPTY_BASEDIR
        = "<basedir></basedir>";

    private static final String WITHOUT_BASEDIR
        = HEADER
        + FOOTER;

    private static final String WITH_EMPTY_BASEDIR
        = HEADER
        + EMPTY_BASEDIR
        + FOOTER;

    private static final String WITH_BASEDIR
        = HEADER
        + BASEDIR
        + FOOTER;

    /**
     * test extractCheckWithGET.
     * @throws Exception something went wrong.
     */
    @Test
    public void testExtractCheckWithGET() throws Exception {

        ServiceSettings settings = new ServiceSettings(
            XML.getDocument(
                new ByteArrayInputStream(
                    EMPTY_CHECK_GET.getBytes("UTF-8"))));

        assertFalse(
            settings.checkRestrictionWithGET("http://example.net"));
    }

    /**
     * test checkRestrictionWithGET.
     * @throws Exception something went wrong.
     */
    @Test
    public void testCheckRestrictionWithGET() throws Exception {

        ServiceSettings settings = new ServiceSettings(
            XML.getDocument(
                new ByteArrayInputStream(
                    CORRECT_CHECK_GET.getBytes("UTF-8"))));

        assertTrue(
            settings.checkRestrictionWithGET("http://example.com"));
        assertTrue(
            settings.checkRestrictionWithGET("http://example.org"));
        assertTrue(
            settings.checkRestrictionWithGET("https://example.com"));
        assertTrue(
            settings.checkRestrictionWithGET("https://example.com/example"));
        assertFalse(
            settings.checkRestrictionWithGET("http://example.net"));
    }

    /**
     * test wrong check.
     * @throws Exception something went wrong.
     */
    @Test
    public void testWrongCheckWithGET() throws Exception {
        try {
            new ServiceSettings(
                XML.getDocument(
                    new ByteArrayInputStream(
                        WRONG_CHECK_GET.getBytes("UTF-8"))));

            fail("should not be reached");
        } catch (IOException ioe) {
            // Test passed.
        }
    }

    /**
     * test parsing basedirectory from settings file.
     * @throws IOException not expected
     * @throws SAXException not expected
     * @throws ParserConfigurationException not expected
     */
    @Test
    public void testThatBaseDirectoryCanBeParsedFromFile()
        throws IOException, SAXException, ParserConfigurationException {
        ServiceSettings settings = new ServiceSettings(
            XML.getDocument(
                ServiceSettings.class.getResourceAsStream("/settings.xml")));
        assertEquals("", settings.getBaseDirectory());
    }

    /**
     * test parsing basedirectory from existing element.
     * @throws IOException not expected
     * @throws SAXException not expected
     * @throws ParserConfigurationException not expected
     */
    @Test
    public void testThatBaseDirectoryCanBeExtracted()
        throws IOException, SAXException, ParserConfigurationException {
        ServiceSettings settings = new ServiceSettings(
            XML.getDocument(
                new ByteArrayInputStream(
                    WITH_BASEDIR.getBytes("UTF-8"))));
        assertEquals("/var/tmp", settings.getBaseDirectory());
    }

    /**
     * test parsing basedirectory from empty element.
     * @throws IOException not expected
     * @throws SAXException not expected
     * @throws ParserConfigurationException not expected
     */
    @Test
    public void testThatBaseDirectoryCanBeExtractedWhenEmpty()
        throws IOException, SAXException, ParserConfigurationException {
        ServiceSettings settings = new ServiceSettings(
            XML.getDocument(
                new ByteArrayInputStream(
                    WITH_EMPTY_BASEDIR.getBytes("UTF-8"))));
        assertEquals("", settings.getBaseDirectory());
    }

    /**
     * test parsing basedirectory from missing element.
     * @throws IOException not expected
     * @throws SAXException not expected
     * @throws ParserConfigurationException not expected
     */
    @Test
    public void testThatBaseDirectoryCanBeMissing()
        throws IOException, SAXException, ParserConfigurationException {
        ServiceSettings settings = new ServiceSettings(
            XML.getDocument(
                new ByteArrayInputStream(
                    WITHOUT_BASEDIR.getBytes("UTF-8"))));
        assertEquals("", settings.getBaseDirectory());
    }
}
