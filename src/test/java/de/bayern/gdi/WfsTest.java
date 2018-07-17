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
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static net.jadler.Jadler.port;
import static org.junit.Assert.assertFalse;

/**
 * @author Juergen Weichand
 */
public class WfsTest extends WFS20ResourceTestBase {

    /**
     * Constant for FEATURES_PER_PAGE.
     */
    public static final int FEATURES_PER_PAGE = 1037;


    /**
     * Test virtuell GeoServer.
     *
     * @throws IOException        Something went wrong
     * @throws URISyntaxException if URL is wrong
     */
    @Test
    public void testGeoServer() throws IOException, URISyntaxException {
        System.out.println("... Testing virtuell GeoServer");
        run("/geoserver/wfs", "/wfs20/geoserver/geoserver-capabilities.xml");
    }

    /**
     * Test virtuell XtraServer.
     *
     * @throws IOException        Something went wrong
     * @throws URISyntaxException if URL is wrong
     */
    @Test
    public void testXtraServer() throws IOException, URISyntaxException {
        System.out.println("... Testing virtuell XtraServer");
        run("/xtraserver/wfs", "/wfs20/xtraserver/xtraserver-capabilities.xml");
    }

    private void run(String queryPath, String queryResource)
        throws IOException, URISyntaxException {
        int port = port();

        prepareCapabilities(queryResource, queryPath, port);
        prepareDescribeStoredQueries();

        WFSMeta wfsMeta = new WFSMetaExtractor(
            buildGetCapabilitiesUrl(queryPath, port)).parse();

        checkFeatureTypes(wfsMeta);
        checkFeaturesPerPage(wfsMeta);
    }

    /*
        Checks
    */
    private void checkFeatureTypes(WFSMeta wfsMeta) {

        StringBuilder sb = new StringBuilder();
        sb.append(wfsMeta.getTitle());
        sb.append(" - ");
        sb.append("FeatureType parsing failed.");

        for (Feature feature : wfsMeta.getFeatures()) {
            String name = feature.getName();
            boolean condition = name == null || name.isEmpty();
            assertFalse(sb.toString(), condition);
        }
    }


    private void checkFeaturesPerPage(WFSMeta wfsMeta) {

        StringBuilder sb = new StringBuilder();
        sb.append(wfsMeta.getTitle());
        sb.append(" - ");
        sb.append("FeaturesPerPage parsing failed.");


        Integer fpp = wfsMeta.findOperation("GetFeature").featuresPerPage();
        if (fpp == null) { // Fall back to global default.
            fpp = wfsMeta.featuresPerPage();
        }

        if (fpp == null || !fpp.equals(FEATURES_PER_PAGE)) {
            assertFalse(sb.toString(), true);
        }
    }

}
