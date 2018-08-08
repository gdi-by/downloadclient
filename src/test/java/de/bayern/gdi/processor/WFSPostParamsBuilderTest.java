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
import org.xmlmatchers.namespace.SimpleNamespaceContext;

import javax.xml.namespace.NamespaceContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static net.jadler.Jadler.port;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlmatchers.XmlMatchers.conformsTo;
import static org.xmlmatchers.XmlMatchers.hasXPath;
import static org.xmlmatchers.transform.XmlConverters.the;
import static org.xmlmatchers.validation.SchemaFactory.w3cXmlSchemaFrom;
import static org.xmlmatchers.xpath.XpathReturnType.returningABoolean;
import static org.xmlmatchers.xpath.XpathReturnType.returningANumber;
import static org.xmlmatchers.xpath.XpathReturnType.returningAString;

/**
 * Test class to verify WFS GetFeature request builder.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class WFSPostParamsBuilderTest extends WFS20ResourceTestBase {

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

        assertThat(the(wfsRequest), conformsTo(w3cXmlSchemaFrom(wfs())));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@srsName",
            namespaceContext(),
            is(getValue(downloadStep, "srsName"))));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames",
            namespaceContext(),
            is(downloadStep.getDataset())));
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

        assertThat(the(wfsRequest), conformsTo(w3cXmlSchemaFrom(wfs())));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@srsName",
            namespaceContext(),
            is(getValue(downloadStep, "srsName"))));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames",
            namespaceContext(),
            is(downloadStep.getDataset())));
    }

    /**
     * Test Example 1.
     *
     * @throws Exception e
     */
    @Test
    public void testCreateExample1() throws Exception {
        DownloadStep downloadStep = createDownloadStep("bvv:gmd_ex",
            "\"bvv:sch\" = '09774135'");

        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta(GEOSERVER);
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);

        // Fails cause of ANY (instead of Any)
        // assertThat(the(wfsRequest), conformsTo(w3cXmlSchemaFrom(wfs())));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@srsName",
            namespaceContext(),
            is(getValue(downloadStep, "srsName"))));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames",
            namespaceContext(),
            is(downloadStep.getDataset())));
    }

    /**
     * Test Example 2.
     *
     * @throws Exception e
     */
    @Test
    public void testCreateExample2() throws Exception {
        DownloadStep downloadStep = createDownloadStep("bvv:gmd_ex",
            "\"bvv:sch\" LIKE '09774%'");

        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta(GEOSERVER);
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);

        assertThat(the(wfsRequest), conformsTo(w3cXmlSchemaFrom(wfs())));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:lkr_ex']",
            namespaceContext(),
            returningABoolean(),
            is(true)));
        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:gmd_ex']",
            namespaceContext(),
            returningABoolean(),
            is(true)));
    }

    /**
     * Test Example 3.
     *
     * @throws Exception e
     */
    @Test
    public void testCreateExample3() throws Exception {
        DownloadStep downloadStep = createDownloadStep(
            "Typübergreifende Abfrage (Filter)",
            "\"bvv:lkr_ex\" WHERE \"bvv:sch\" = '09774'\n"
                + "\"bvv:gmd_ex\" WHERE \"bvv:sch\" LIKE '09774%'");

        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta(GEOSERVER);
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);

        // Fails cause of ANY (instead of Any)
        // assertThat(the(wfsRequest), conformsTo(w3cXmlSchemaFrom(wfs())));

        assertThat(the(wfsRequest), hasXPath(
            "count(/wfs:GetFeature/wfs:Query)",
            namespaceContext(),
            returningANumber(),
            is(2d)));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:lkr_ex']",
            namespaceContext(),
            returningABoolean(),
            is(true)));
        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:gmd_ex']",
            namespaceContext(),
            returningABoolean(),
            is(true)));
    }

    /**
     * Test Example 4.
     *
     * @throws Exception e
     */
    @Test
    public void testCreateExample4() throws Exception {
        DownloadStep downloadStep = createDownloadStep(
            "Typübergreifende Abfrage (Filter)",
            "\"bvv:lkr_ex\" WHERE \"bvv:sch\" = '09774'\n"
                + "\"bvv:gmd_ex\" WHERE \"bvv:sch\" IN "
                + "('09161000', '09161000')");

        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta(GEOSERVER);
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);

        // Fails cause of ANY (instead of Any)
        // assertThat(the(wfsRequest), conformsTo(w3cXmlSchemaFrom(wfs())));

        assertThat(the(wfsRequest), hasXPath(
            "count(/wfs:GetFeature/wfs:Query)",
            namespaceContext(),
            returningANumber(),
            is(2d)));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:lkr_ex']",
            namespaceContext(),
            returningABoolean(),
            is(true)));
        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:gmd_ex']",
            namespaceContext(),
            returningABoolean(),
            is(true)));
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

        assertThat(the(wfsRequest), conformsTo(w3cXmlSchemaFrom(wfsAndGml())));

        assertThat(the(wfsRequest), hasXPath(
            "count(/wfs:GetFeature/wfs:Query)",
            namespaceContext(),
            returningANumber(),
            is(1d)));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:gmd_ex']",
            namespaceContext(),
            returningABoolean(),
            is(true)));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/fes:Filter/"
                + "fes:Equals/fes:ValueReference",
            namespaceContext(),
            returningAString(),
            is("bvv:geom")));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/fes:Filter/fes:Equals/gml:Polygon",
            namespaceContext(),
            returningABoolean(),
            is(true)));

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

        assertThat(the(wfsRequest), conformsTo(w3cXmlSchemaFrom(wfsAndGml())));

        assertThat(the(wfsRequest), hasXPath(
            "count(/wfs:GetFeature/wfs:Query)",
            namespaceContext(),
            returningANumber(),
            is(1d)));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:gmd_ex']",
            namespaceContext(),
            returningABoolean(),
            is(true)));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/fes:Filter/"
                + "fes:Within/fes:ValueReference",
            namespaceContext(),
            returningAString(),
            is("bvv:geom")));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/fes:Filter/fes:Within/gml:Polygon",
            namespaceContext(),
            returningABoolean(),
            is(true)));
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

        assertThat(the(wfsRequest), conformsTo(w3cXmlSchemaFrom(wfsAndGml())));

        assertThat(the(wfsRequest), hasXPath(
            "count(/wfs:GetFeature/wfs:Query)",
            namespaceContext(),
            returningANumber(),
            is(1d)));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:gmd_ex']",
            namespaceContext(),
            returningABoolean(),
            is(true)));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/fes:Filter/"
                + "fes:Intersects/fes:ValueReference",
            namespaceContext(),
            returningAString(),
            is("bvv:geom")));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/fes:Filter/fes:Intersects/gml:Point",
            namespaceContext(),
            returningABoolean(),
            is(true)));
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

        assertThat(the(wfsRequest), conformsTo(w3cXmlSchemaFrom(wfsAndGml())));

        assertThat(the(wfsRequest), hasXPath(
            "count(/wfs:GetFeature/wfs:Query)",
            namespaceContext(),
            returningANumber(),
            is(1d)));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames['bvv:gmd_ex']",
            namespaceContext(),
            returningABoolean(),
            is(true)));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/fes:Filter/"
                + "fes:Disjoint/fes:ValueReference",
            namespaceContext(),
            returningAString(),
            is("bvv:geom")));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/fes:Filter/fes:Disjoint/gml:Polygon",
            namespaceContext(),
            returningABoolean(),
            is(true)));
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

    private NamespaceContext namespaceContext() {
        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.bind("wfs", "http://www.opengis.net/wfs/2.0");
        namespaceContext.bind("fes", "http://www.opengis.net/fes/2.0");
        namespaceContext.bind("gml", "http://www.opengis.net/gml/3.2");
        return namespaceContext;
    }

    private String resourceAsString(String resourceName) throws IOException {
        InputStream resource = getClass().getResourceAsStream(resourceName);
        String s = IOUtils.toString(resource).trim();
        resource.close();
        return s;
    }
}
