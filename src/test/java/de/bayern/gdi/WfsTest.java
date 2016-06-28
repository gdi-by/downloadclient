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

import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.services.WFSMeta.Feature;
import de.bayern.gdi.services.WFSMetaExtractor;
import java.io.IOException;
import java.nio.charset.Charset;
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
public class WfsTest extends TestCase {

    public static final int HTTP_OKAY = 200;
    public static final int FEATURES_PER_PAGE = 1037;

    public WfsTest(String testName) {
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
    public void testGeoServer() throws IOException {
        System.out.println("... Testing virtuell GeoServer");
        run("/geoserver/wfs", "/wfs20/geoserver/geoserver-capabilities.xml");
    }

    @Test
    public void testXtraServer() throws IOException {
        System.out.println("... Testing virtuell XtraServer");
        run("/xtraserver/wfs", "/wfs20/xtraserver/xtraserver-capabilities.xml");
    }



    private void run(String queryPath, String queryResource)
                throws IOException {

        String body = IOUtils.toString(
                WfsTest.class.getResourceAsStream(queryResource), "UTF-8"
        );

        body = body.replace("{DESCRIBESTOREDQUERIES_URL}",
                buildDescribeStoredQueriesUrl(port()));

        prepareResource(queryPath, body); // prepare GetCapabilities-Response
        prepareDescribeStoredQueries();


        WFSMeta wfsMeta = new WFSMetaExtractor(
                buildGetCapabilitiesUrl(queryPath, port())).parse();

        checkFeatureTypes(wfsMeta);
        checkFeaturesPerPage(wfsMeta);
    }

    /*
        Checks
    */
    private void checkFeatureTypes(WFSMeta wfsMeta) {

        StringBuilder sb = new StringBuilder();
        sb.append(wfsMeta.title);
        sb.append(" - ");
        sb.append("FeatureType parsing failed.");

        for (Feature feature : wfsMeta.features) {
            boolean condition =
                    feature.name == null || feature.name.isEmpty();
            assertFalse(sb.toString(), condition);
        }
    }


    private void checkFeaturesPerPage(WFSMeta wfsMeta) {

        StringBuilder sb = new StringBuilder();
        sb.append(wfsMeta.title);
        sb.append(" - ");
        sb.append("FeaturesPerPage parsing failed.");


        WFSMeta.Operation getFeature = wfsMeta.findOperation("GetFeature");
        if (getFeature.featuresPerPage() == null ||
                getFeature.featuresPerPage() != FEATURES_PER_PAGE) {
            assertFalse(sb.toString(), true);
        }
    }


    /*
        prepare Resources
    */
    private void prepareResource(String queryPath, String body)
                throws IOException {

        onRequest()
                .havingMethodEqualTo("GET")
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


    private void prepareDescribeStoredQueries() throws IOException {

        String queryResource = "/wfs20/desc-storedqueries.xml";

        String body = IOUtils.toString(
                WfsTest.class.getResourceAsStream(queryResource), "UTF-8"
        );

        prepareResource("/wfs/wfs", body);
    }


    /*
        URL-Helper
    */
    private String buildGetCapabilitiesUrl(String queryPath, int port) {

        StringBuilder sb = new StringBuilder();
        sb.append("http://localhost:");
        sb.append(port);
        sb.append(queryPath);
        System.out.println("GetCapabilities-URL: " + sb.toString());
        return sb.toString();
    }


    private String buildDescribeStoredQueriesUrl(int port) {

        StringBuilder sb = new StringBuilder();
        sb.append("http://localhost:");
        sb.append(port);
        sb.append("/wfs/wfs");
        System.out.println("DescribeStoredQueries-URL: " + sb.toString());
        return sb.toString();
    }

}
