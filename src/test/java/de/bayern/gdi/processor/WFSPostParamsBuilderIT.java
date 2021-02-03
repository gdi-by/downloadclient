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
package de.bayern.gdi.processor;

import de.bayern.gdi.WFS20ResourceTestBase;
import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.model.Parameter;
import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.services.WFSMetaExtractor;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xmlunit.matchers.HasXPathMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static net.jadler.Jadler.port;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

/**
 * Test class to verify WFS GetFeature request builder.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class WFSPostParamsBuilderIT extends WFS20ResourceTestBase {

    private static final String GEOSERVER =
        "/wfs20/geoserver/geoserver-capabilities.xml";

    private static final String XTRASERVER =
        "/wfs20/xtraserver/xtraserver-capabilities.xml";


    /**
     * Test Geoserver.
     *
     * @throws Exception e
     */
    @Test
    public void testCreateSimpleExampleGeoserver() throws Exception {
        DownloadStep downloadStep = createDownloadStep("bvv:gmd_ex");

        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta(GEOSERVER);
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);

        // Validation fails when xmlunit is used
        //assertThat(wfsRequest, valid(wfs()));

        assertThat(wfsRequest, hasXPath("/wfs:GetFeature/wfs:Query/@srsName",
            is(getValue(downloadStep, "srsName")))
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames",
            is(downloadStep.getDataset())).withNamespaceContext(wfsContext()));
    }

    /**
     * Test Xtraserver.
     *
     * @throws Exception e
     */
    @Test
    public void testCreateSimpleExampleXtraserver() throws Exception {
        DownloadStep downloadStep = createDownloadStep("adv:AP_LTO");

        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta(XTRASERVER);
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);

        // Validation fails when xmlunit is used
        //assertThat(wfsRequest, valid(wfs()));

        assertThat(wfsRequest, hasXPath("/wfs:GetFeature/wfs:Query/@srsName",
            is(getValue(downloadStep, "srsName")))
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames",
            is(downloadStep.getDataset())).withNamespaceContext(wfsContext()));
    }

    /**
     * Test Example 1.
     *
     * @throws Exception e
     */
    @Test
    public void testCreateExample1() throws Exception {
        String cql = resourceAsString("/cql/example1.cql");
        DownloadStep downloadStep = createDownloadStep("bvv:gmd_ex", cql);

        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta(GEOSERVER);
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);

        // Validation fails when xmlunit is used
        //assertThat(wfsRequest, valid(wfs()));

        assertThat(wfsRequest, hasXPath("/wfs:GetFeature/wfs:Query/@srsName",
            is(getValue(downloadStep, "srsName")))
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames",
            is(downloadStep.getDataset())).withNamespaceContext(wfsContext()));
    }

    /**
     * Test Example 2.
     *
     * @throws Exception e
     */
    @Test
    public void testCreateExample2() throws Exception {
        String cql = resourceAsString("/cql/example2.cql");
        DownloadStep downloadStep = createDownloadStep("bvv:gmd_ex", cql);

        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta(GEOSERVER);
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);

        // Validation fails when xmlunit is used
        //assertThat(wfsRequest, valid(wfs()));

        assertThat(wfsRequest, HasXPathMatcher.hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:lkr_ex']")
            .withNamespaceContext(wfsContext()));
        assertThat(wfsRequest, HasXPathMatcher.hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:gmd_ex']")
            .withNamespaceContext(wfsContext()));
    }

    /**
     * Test Example 3.
     *
     * @throws Exception e
     */
    @Test
    public void testCreateExample3() throws Exception {
        String cql = resourceAsString("/cql/example3.cql");
        DownloadStep downloadStep = createDownloadStep(
            "Typübergreifende Abfrage (Filter)", cql);

        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta(GEOSERVER);
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);

        // Validation fails when xmlunit is used
        //assertThat(wfsRequest, valid(wfs()));

        assertThat(wfsRequest, hasXPath(
            "count(/wfs:GetFeature/wfs:Query)", is("2"))
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, HasXPathMatcher.hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:lkr_ex']")
            .withNamespaceContext(wfsContext()));
        assertThat(wfsRequest, HasXPathMatcher.hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:gmd_ex']")
            .withNamespaceContext(wfsContext()));
    }

    /**
     * Test Example 4.
     *
     * @throws Exception e
     */
    @Test
    public void testCreateExample4() throws Exception {
        String cql = resourceAsString("/cql/example4.cql");
        DownloadStep downloadStep = createDownloadStep(
            "Typübergreifende Abfrage (Filter)", cql);

        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta(GEOSERVER);
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);

        // Validation fails when xmlunit is used
        //assertThat(wfsRequest, valid(wfs()));

        assertThat(wfsRequest, hasXPath(
            "count(/wfs:GetFeature/wfs:Query)", is("2"))
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, HasXPathMatcher.hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:lkr_ex']")
            .withNamespaceContext(wfsContext()));
        assertThat(wfsRequest, HasXPathMatcher.hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:gmd_ex']")
            .withNamespaceContext(wfsContext()));
    }

    /**
     * Test with spatial operator EQUALS.
     *
     * @throws Exception e
     */
    @Test
    public void testCreateEquals() throws Exception {
        String cql = resourceAsString("/cql/cql_equals.cql");
        DownloadStep downloadStep = createDownloadStep("bvv:gmd_ex", cql);

        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta(GEOSERVER);
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);

        // Validation fails when xmlunit is used
        //assertThat(wfsRequest, valid(wfsAndGml()));

        assertThat(wfsRequest, hasXPath(
            "count(/wfs:GetFeature/wfs:Query)", is("1"))
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, HasXPathMatcher.hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:gmd_ex']")
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, hasXPath("/wfs:GetFeature/wfs:Query/"
            + "fes:Filter/fes:Equals/fes:ValueReference", is("bvv:geom"))
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, HasXPathMatcher.hasXPath(
            "/wfs:GetFeature/wfs:Query/fes:Filter/fes:Equals/gml:Polygon")
            .withNamespaceContext(wfsContext()));

    }

    /**
     * Test with spatial operator WITHIN.
     *
     * @throws Exception e
     */
    @Test
    public void testCreateWithin() throws Exception {
        String cql = resourceAsString("/cql/cql_within.cql");
        DownloadStep downloadStep = createDownloadStep("bvv:gmd_ex", cql);

        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta(GEOSERVER);
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);

        // Validation fails when xmlunit is used
        //assertThat(wfsRequest, valid(wfsAndGml()));

        assertThat(wfsRequest, hasXPath(
            "count(/wfs:GetFeature/wfs:Query)", is("1"))
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, HasXPathMatcher.hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:gmd_ex']")
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, hasXPath("/wfs:GetFeature/wfs:Query/"
            + "fes:Filter/fes:Within/fes:ValueReference", is("bvv:geom"))
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, HasXPathMatcher.hasXPath(
            "/wfs:GetFeature/wfs:Query/fes:Filter/fes:Within/gml:Polygon")
            .withNamespaceContext(wfsContext()));
    }

    /**
     * Test with spatial operator INTERSECTS.
     *
     * @throws Exception e
     */
    @Test
    public void testCreateIntersects() throws Exception {
        String cql = resourceAsString("/cql/cql_intersects.cql");
        DownloadStep downloadStep = createDownloadStep("bvv:gmd_ex", cql);

        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta(GEOSERVER);
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);

        // Validation fails when xmlunit is used
        //assertThat(wfsRequest, valid(wfsAndGml()));

        assertThat(wfsRequest, hasXPath(
            "count(/wfs:GetFeature/wfs:Query)", is("1"))
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, HasXPathMatcher.hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:gmd_ex']")
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, hasXPath("/wfs:GetFeature/wfs:Query/"
                + "fes:Filter/fes:Intersects/fes:ValueReference",
            is("bvv:geom"))
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, HasXPathMatcher.hasXPath(
            "/wfs:GetFeature/wfs:Query/fes:Filter/fes:Intersects/gml:Point")
            .withNamespaceContext(wfsContext()));
    }


    /**
     * Test with spatial operator DISJOINT.
     *
     * @throws Exception e
     */
    @Test
    public void testCreateDisjoint() throws Exception {
        String cql = resourceAsString("/cql/cql_disjoint.cql");
        DownloadStep downloadStep = createDownloadStep("bvv:gmd_ex", cql);

        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta(GEOSERVER);
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);

        // Validation fails when xmlunit is used
        //assertThat(wfsRequest, valid(wfsAndGml()));

        assertThat(wfsRequest, hasXPath(
            "count(/wfs:GetFeature/wfs:Query)", is("1"))
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, HasXPathMatcher.hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:gmd_ex']")
            .withNamespaceContext(wfsContext()));

        assertThat(wfsRequest, hasXPath("/wfs:GetFeature/wfs:Query/"
            + "fes:Filter/fes:Disjoint/fes:ValueReference", is("bvv:geom"))
            .withNamespaceContext(wfsContext()
        ));

        assertThat(wfsRequest, HasXPathMatcher.hasXPath(
            "/wfs:GetFeature/wfs:Query/fes:Filter/fes:Disjoint/gml:Polygon")
            .withNamespaceContext(wfsContext()));
    }

    private WFSMeta parseMeta(String queryResource)
        throws IOException, URISyntaxException {
        int port = port();
        String queryPath = "/wfs";
        prepareCapabilities(queryResource, queryPath, port);
        prepareDescribeStoredQueries();

        return new WFSMetaExtractor(
            buildGetCapabilitiesUrl(queryPath, port)).parse();
    }

    private static DownloadStep createDownloadStep(String dataset) {
        return createDownloadStep(dataset, null);
    }

    private static DownloadStep createDownloadStep(String dataset,
                                                   String cql) {
        ArrayList<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter("srsName",
            "urn:ogc:def:crs:EPSG::31468"));
        parameters.add(new Parameter("outputformat",
            "text/xml; subtype=gml/3.2"));
        String typeName = "WFS2_BASIC";
        if (cql != null) {
            typeName = "WFS2_SQL";
            parameters.add(new Parameter("CQL", cql));
        }
        return new DownloadStep(dataset, parameters, typeName,
            "DUMMYURL", "DUMMYPATH", Collections.emptyList());
    }

    private String getValue(DownloadStep dls, String key) {
        for (Parameter param : dls.getParameters()) {
            if (param.getKey().equals(key)) {
                return param.getValue();
            }
        }
        return null;
    }

    private URL wfs() throws MalformedURLException {
        return new URL("http://schemas.opengis.net/wfs/2.0/wfs.xsd");
    }

    private URL wfsAndGml() {
        return getClass().getResource("/xsd/wfs20gml32.xsd");
    }

    private Map<String, String> wfsContext() {
        Map<String, String> namespaceContext = new HashMap<>();
        namespaceContext.put("wfs", "http://www.opengis.net/wfs/2.0");
        namespaceContext.put("fes", "http://www.opengis.net/fes/2.0");
        namespaceContext.put("gml", "http://www.opengis.net/gml/3.2");
        return namespaceContext;
    }

    private String resourceAsString(String resourceName) throws IOException {
        InputStream resource = getClass().getResourceAsStream(resourceName);
        String s = IOUtils.toString(resource).trim();
        resource.close();
        return s;
    }
}
