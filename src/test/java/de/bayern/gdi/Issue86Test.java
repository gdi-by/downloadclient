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

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadler;
import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import static org.hamcrest.Matchers.isOneOf;

/**
 * Unit tests, using TestFX to test controller functions.
 *
 * @author Alexander Woestmann (awoestmann@intevation.de)
 */
public class Issue86Test extends TestBase {

    /**
     * Constant for HTTP_OKAY.
     */
    private static final int HTTP_OKAY = 200;
    private static final String QUERY_RESOURCE =
            "/issues/issue86.xml";
    private static final String QUERY_PATH =
            "/issues/issue86";

    private void prepareServer(String queryPath, String body)
            throws IOException {

        onRequest()
                .havingMethod(isOneOf("GET", "HEAD", "POST"))
                .havingPathEqualTo(queryPath)
                .respond()
                .withStatus(HTTP_OKAY)
                .withBody(body)
                .withEncoding(Charset.forName("UTF-8"))
                .withContentType("application/xml; charset=UTF-8");
    }

    private String getCapabilitiesUrl(String queryPath, int port) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://localhost:");
        sb.append(port);
        sb.append(queryPath);
        System.out.println("GetCapabilities-URL: " + sb.toString());
        return sb.toString();
    }

    /**
     * Start jadler webserver.
     */
    @Before
    public void startJadler() {
        System.err.println("Start jadler ...");
        initJadler();
    }

    /**
     * Stop jadler webserver.
     */
    @After
    public void stopJadler() {
        System.err.println("Stop jadler ...");
        closeJadler();
    }

    /**
     * The processingChainValidationTest.
     *
     * @throws Exception if something went wrong.
     */
    @Test
    public void processingChainValidationTest() throws Exception {
        prepareServer(QUERY_PATH, getResponseBody());
        waitUntilReady();
        setServiceUrl(getCapabilitiesUrl(QUERY_PATH, port()));
        clickOn(SERVICE_SELECTION);
        waitUntilReady();
        clickOn(ACTIVATE_FURTHER_PROCESSING);
        clickOn(ADD_PROCESSING_STEP);
        Assert.assertTrue(titlePaneShows(NO_FORMAT_CHOSEN));
    }


    /**
     * Mock response for jadler.
     *
     * @return Prepared ResponseBody
     * @throws IOException Exception thrown by IOUtils
     */
    private String getResponseBody() throws IOException {
        return IOUtils.toString(
                Issue86Test.class.getResourceAsStream(QUERY_RESOURCE),
                "UTF-8");
    }
}
