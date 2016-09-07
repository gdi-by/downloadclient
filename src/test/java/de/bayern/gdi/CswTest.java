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

import de.bayern.gdi.gui.ServiceModel;
import de.bayern.gdi.services.CatalogService;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import junit.framework.TestCase;
import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadler;

import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Juergen Weichand
 */
public class CswTest extends TestCase {

    public static final int HTTP_OKAY = 200;

    public static final String
            PATH_GETCAPABILITIES = "/csw/csw-getcapabilities";

    public CswTest(String testName) {
        super(testName);
    }

    @Before
    @Override
    public void setUp() {
        initJadler();
    }

    @After
    @Override
    public void tearDown() {
        closeJadler();
    }

    @Test
    public void testCswClient() throws IOException {
        System.out.println("... Testing virtuell search");
        run("/csw/atom-feeds", "/csw202/atom-feeds.xml");
    }


    private void run(String queryPath, String queryResource)
                throws IOException {

        String body = IOUtils.toString(
                CswTest.class.getResourceAsStream(queryResource), "UTF-8"
        );

        prepareGetCapabilities(queryPath);
        prepareResource(queryPath, body);

        CatalogService catalogService =
                new CatalogService(buildGetCapabilitiesUrl(port()));

        List<ServiceModel> services = catalogService.
                getServicesByFilter("not-required-because-mocked");


        System.out.println("Anzahl der ermittelten Dienste: " + services.size());

        assertTrue(services.size() == 4);
    }



    /*
        prepare Resources
    */
    private void prepareResource(String queryPath, String body)
                throws IOException {

        onRequest()
                // .havingMethodEqualTo("GET")
                .havingPathEqualTo(queryPath)
                // .havingBody(isEmptyOrNullString())
                // .havingHeaderEqualTo("Accept", "application/xml")
                .respond()
                 // .withDelay(1, SECONDS)
                .withStatus(HTTP_OKAY)
                .withBody(body)
                .withEncoding(Charset.forName("UTF-8"))
                .withContentType("application/xml; charset=UTF-8");
    }


    private void prepareGetCapabilities(String queryPath) throws IOException {

        String queryResource = "/csw202/csw-capabilities.xml";

        String body = IOUtils.toString(
                WfsTest.class.getResourceAsStream(queryResource), "UTF-8"
        );

        body = body.replace("{GETRECORDS_URL}",
                buildGetRecordsUrl(queryPath, port()));

        prepareResource(PATH_GETCAPABILITIES, body);
    }

    /*
        URL-Helper
    */
    private String buildGetCapabilitiesUrl(int port) {

        StringBuilder sb = new StringBuilder();
        sb.append("http://localhost:");
        sb.append(port);
        sb.append(PATH_GETCAPABILITIES);
        System.out.println("GetCapabilities-URL: " + sb.toString());
        return sb.toString();
    }


    private String buildGetRecordsUrl(String queryPath, int port) {

        StringBuilder sb = new StringBuilder();
        sb.append("http://localhost:");
        sb.append(port);
        sb.append(queryPath);
        System.out.println("GetRecords-URL: " + sb.toString());
        return sb.toString();
    }


}
