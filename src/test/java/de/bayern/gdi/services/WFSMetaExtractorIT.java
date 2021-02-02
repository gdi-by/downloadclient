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
package de.bayern.gdi.services;

import de.bayern.gdi.WFS20ResourceTestBase;
import de.bayern.gdi.services.WFSMeta.Feature;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static net.jadler.Jadler.port;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Juergen Weichand
 */
public class WFSMetaExtractorIT extends WFS20ResourceTestBase {

    private static final int FEATURES_PER_PAGE = 1037;

    private static final int NUMBER_OF_FEATURE_TYPES_GEOSERVER = 5;

    private static final int NUMBER_OF_FEATURE_TYPES_XTRASERVER = 123;

    /**
     * Test virtuell GeoServer.
     *
     * @throws IOException        Something went wrong
     * @throws URISyntaxException if URL is wrong
     */
    @Test
    public void testGeoServer() throws IOException, URISyntaxException {
        LOG.debug("... Testing virtuell GeoServer");
        run("/geoserver/wfs",
            "/wfs20/geoserver/geoserver-capabilities.xml",
            NUMBER_OF_FEATURE_TYPES_GEOSERVER);
    }

    /**
     * Test virtuell XtraServer.
     *
     * @throws IOException        Something went wrong
     * @throws URISyntaxException if URL is wrong
     */
    @Test
    public void testXtraServer() throws IOException, URISyntaxException {
        LOG.debug("... Testing virtuell XtraServer");
        run("/xtraserver/wfs",
            "/wfs20/xtraserver/xtraserver-capabilities.xml",
            NUMBER_OF_FEATURE_TYPES_XTRASERVER);
    }

    private void run(String queryPath, String queryResource,
                     int numberOfFeatureTypes)
        throws IOException, URISyntaxException {
        int port = port();

        prepareCapabilities(queryResource, queryPath, port);
        prepareDescribeStoredQueries();

        WFSMeta wfsMeta = new WFSMetaExtractor(
            buildGetCapabilitiesUrl(queryPath, port)).parse();

        checkFeatureTypes(wfsMeta, numberOfFeatureTypes);
        checkFeaturesPerPage(wfsMeta);
    }

    /*
        Checks
    */
    private void checkFeatureTypes(WFSMeta wfsMeta, int numberOfFeatureTypes) {
        List<Feature> features = wfsMeta.getFeatures();
        for (Feature feature : features) {
            String name = feature.getName();
            assertThat(name, is(notNullValue()));
            assertThat(name.isEmpty(), is(false));
        }
        assertThat(features.size(), is(numberOfFeatureTypes));
    }


    private void checkFeaturesPerPage(WFSMeta wfsMeta) {
        Integer fpp = wfsMeta.findOperation("GetFeature").featuresPerPage();
        if (fpp == null) { // Fall back to global default.
            fpp = wfsMeta.featuresPerPage();
        }

        if (fpp == null || !fpp.equals(FEATURES_PER_PAGE)) {
            StringBuilder sb = new StringBuilder();
            sb.append(wfsMeta.getTitle());
            sb.append(" - ");
            sb.append("FeaturesPerPage parsing failed.");
            fail(sb.toString());
        }
    }

}
