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
package de.bayern.gdi.utils;

import junit.framework.TestCase;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test for DownloadConfig.
 */
public class DownloadConfigTest extends TestCase {


    /** Test the getters of the DownloadConfig class based on Biergarten.
     * @throws ParserConfigurationException Exception just in case
     * @throws SAXException Exception just in case
     * @throws DownloadConfig.NoServiceURLException Exception just in case
     * @throws IOException Exception just in case
     */
    @Test
    public void testDownloadConfigGetters() throws ParserConfigurationException,
        SAXException, DownloadConfig.NoServiceURLException, IOException {


        DownloadConfiguration dc = new DownloadConfiguration();
        String dirname = "gdiBY";
        Path tempPath = Files.createTempDirectory(dirname);
        File file = new File(tempPath + File.separator + "test.xml");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(dc.getBiergartenConfiguration(tempPath.toString()));
        fileWriter.flush();
        fileWriter.close();
        DownloadConfig downloadConfig = new DownloadConfig(file);

        assertEquals(
            "http://www.geodaten.bayern.de/ba-data/Themen/kml/biergarten.kml", downloadConfig.getAtomVariation());

        assertEquals("2496b2ed-8a64-465a-95dd-170799788982", downloadConfig.getDataset());

        assertEquals(tempPath.toString(), downloadConfig.getDownloadPath());

        assertEquals(file, downloadConfig.getFile());

        assertEquals("application/vnd.google-earth.kml+xml", downloadConfig.getOutputFormat());

//        assertEquals("{VARIATION="
//                + downloadConfig.getAtomVariation().toString()
//                + ", outputformat="
//                + downloadConfig.getOutputFormat().toString() + "}"
//            , downloadConfig.getParams());

        assertNull(downloadConfig.getProcessingSteps());

        assertEquals("ATOM", downloadConfig.getServiceType());

        assertEquals("https://geoportal.bayern.de/gdiadmin/ausgabe/"
            + "ATOM_SERVICE/a90c75a0-f1b5-46e7-9e45-c0385fd0c200", downloadConfig.getServiceURL());

    }
}
