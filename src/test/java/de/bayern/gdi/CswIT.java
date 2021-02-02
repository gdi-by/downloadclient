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
import de.bayern.gdi.utils.Config;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import de.bayern.gdi.services.Service;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadler;
import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;

import org.apache.commons.io.IOUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juergen Weichand
 */
public class CswIT extends TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(CswIT.class);

    private static final int THREE = 3;

    /**
     * Constant for HTTP_OKAY.
     */
    public static final int HTTP_OKAY = 200;

    /**
     * Constant for PATH_GETCAPABILITIES.
     */
    public static final String
            PATH_GETCAPABILITIES = "/csw/csw-getcapabilities";

    public CswIT(String testName) {
        super(testName);
    }

    @Override
    @Before
    public void setUp() throws IOException {
        LOG.debug("Start jadler ...");
        Config.initialize(null);
        initJadler();
    }

    @Override
    @After
    public void tearDown() {
        LOG.debug("Stop jadler ...");
        closeJadler();
    }

    /**
     * Test for CSW client 1.
     * @throws IOException Something went wrong
     * @throws URISyntaxException if URL is wrong
     */
    @Test
    public void testCswClient1() throws IOException, URISyntaxException {
        LOG.debug("... Testing virtuell search");
        run("/csw/atom-feeds",
            "/csw202/atom-feeds.xml",
            "/csw202/csw-capabilities-1.xml");
    }

    /**
     * Test for CSW client 2.
     * @throws IOException Something went wrong
     * @throws URISyntaxException if URL is wrong
     */
    @Test
    public void testCswClient2() throws IOException, URISyntaxException {
        LOG.debug("... Testing virtuell search");
        run("/csw/atom-feeds",
            "/csw202/atom-feeds.xml",
            "/csw202/csw-capabilities-2.xml");
    }


    private void run(String queryPath, String queryResource, String capResource)
                throws IOException, URISyntaxException {

        String body = IOUtils.toString(
                CswIT.class.getResourceAsStream(queryResource), "UTF-8"
        );

        prepareGetCapabilities(queryPath, capResource);
        prepareResource(queryPath, body);

        CatalogService catalogService =
                new CatalogService(buildGetCapabilitiesUrl(port()));

        List<Service> services = catalogService.
                getServicesByFilter("not-required-because-mocked");


        LOG.debug("Anzahl der ermittelten Dienste: "
                + services.size());

        assertTrue(services.size() == THREE);
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


    private void prepareGetCapabilities(String queryPath, String capResource)
            throws IOException {

        String body = IOUtils.toString(
                CswIT.class.getResourceAsStream(capResource), "UTF-8"
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
        LOG.debug("GetCapabilities-URL: " + sb.toString());
        return sb.toString();
    }


    private String buildGetRecordsUrl(String queryPath, int port) {

        StringBuilder sb = new StringBuilder();
        sb.append("http://localhost:");
        sb.append(port);
        sb.append(queryPath);
        LOG.debug("GetRecords-URL: " + sb.toString());
        return sb.toString();
    }


}
