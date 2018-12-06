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

import de.bayern.gdi.utils.Config;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadler;
import static net.jadler.Jadler.onRequest;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * Base class which provides methods to prepare WFS 2.0 requests.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public abstract class WFS20ResourceTestBase {

    /**
     * Logger instance to be used.
     */
    protected static final Logger log
        = LoggerFactory.getLogger(WFS20ResourceTestBase.class);

    /**
     * Init Jadler and Config.
     *
     * @throws IOException if an error occured
     */
    @Before
    public void setUp() throws IOException {
        try {
            initJadler();
        } catch (IllegalStateException ise) {
            log.debug("Jadler is already initialized");
        }
        Config.initialize(null);
    }

    /**
     * The tearDown phase just closes all resources.
     */
    @After
    public void tearDown() {
        closeJadler();
    }


    /**
     * Prepare Capabilities.
     *
     * @param capabilitiesResource the resource to test
     * @param queryPath            to use
     * @param port                 to append
     * @throws IOException if the resource could not be prepared
     */
    protected void prepareCapabilities(String capabilitiesResource,
                                       String queryPath,
                                       int port)
        throws IOException {
        String body = readResourceAsString(capabilitiesResource);
        body = body.replace("{GETFEATURE_URL}",
            buildGetFeatureUrl(port));
        body = body.replace("{DESCRIBESTOREDQUERIES_URL}",
            buildDescribeStoredQueriesUrl(port));
        prepareResource("GET", queryPath, body);
    }

    /**
     * Prepare GetFeature.
     *
     * @throws IOException if the resource could not be prepared
     */
    protected void prepareGetFeature() throws IOException {
        String body = readResourceAsString("/wfs20/getfeature-hits.xml");
        prepareResource("POST", "/wfs/gf", body);
    }

    /**
     * Prepare DescribeStoredQueries.
     *
     * @throws IOException if the resource could not be prepared
     */
    protected void prepareDescribeStoredQueries() throws IOException {
        String body = readResourceAsString("/wfs20/desc-storedqueries.xml");
        prepareResource("GET", "/wfs/wfs", body);
    }

    /**
     * Build GetCapabilities Url.
     *
     * @param queryPath to use
     * @param port      to append
     * @return the GetCapabilities Url
     */
    protected String buildGetCapabilitiesUrl(String queryPath, int port) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://localhost:");
        sb.append(port);
        sb.append(queryPath);
        log.debug("Service-URL: " + sb.toString());
        return sb.toString();
    }

    /**
     * Build GetFeature Url.
     *
     * @param port to append
     * @return the GetFeature Url
     */
    protected String buildGetFeatureUrl(int port) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://localhost:");
        sb.append(port);
        sb.append("/wfs/gf");
        log.debug("GetFeature-URL: " + sb.toString());
        return sb.toString();
    }

    /**
     * Build DescribeStoredQueries Url.
     *
     * @param port to append
     * @return the DescribeStoredQueries Url
     */
    protected String buildDescribeStoredQueriesUrl(int port) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://localhost:");
        sb.append(port);
        sb.append("/wfs/wfs");
        log.debug("DescribeStoredQueries-URL: " + sb.toString());
        return sb.toString();
    }

    private void prepareResource(String method,
                                 String queryPath,
                                 String body) {
        log.debug("Method: " + method + " QueryPath: " + queryPath);
        onRequest()
            .havingMethodEqualTo(method)
            .havingPathEqualTo(queryPath)
            .respond()
            .withStatus(SC_OK)
            .withBody(body)
            .withEncoding(Charset.forName("UTF-8"))
            .withContentType("application/xml; charset=UTF-8");
    }

    private String readResourceAsString(String resource) throws IOException {
        InputStream resourceIs = getClass().getResourceAsStream(resource);
        return IOUtils.toString(resourceIs, "UTF-8");
    }

}
