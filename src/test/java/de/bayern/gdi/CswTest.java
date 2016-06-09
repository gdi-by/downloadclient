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
package de.bayern.gdi;

import de.bayern.gdi.services.CatalogService;
import java.net.MalformedURLException;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Jürgen Weichand (LDBV Bayern)
 */
public class CswTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CswTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(CswTest.class);
    }

    /**
     * Check if the correct service url is determined from the metadata
     *
     * @throws java.net.MalformedURLException
     */
    public void testCswBy() throws MalformedURLException {

        String[] notValidExtensions = {".pdf", ".htm"};

        String cswUrl = "http://geoportal.bayern.de/csw/gdi?"
                + "service=CSW&version=2.0.2&request=GetCapabilities";
        CatalogService catalogService = new CatalogService(cswUrl);

        Map<String, String> results =
                catalogService.getServicesByFilter("umwelt");
        for (String key : results.keySet()) {
            String url = results.get(key);
            for (String notValidExtension : notValidExtensions) {
                if (url.toLowerCase().endsWith(notValidExtension)) {
                    assertFalse("Wrong URL parsed from metadata", true);
                }
            }
        }
    }

}
