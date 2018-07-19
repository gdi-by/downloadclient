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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static net.jadler.Jadler.port;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.XmlMatchers.conformsTo;
import static org.xmlmatchers.XmlMatchers.hasXPath;
import static org.xmlmatchers.transform.XmlConverters.the;
import static org.xmlmatchers.validation.SchemaFactory.w3cXmlSchemaFrom;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@RunWith(Parameterized.class)
public class WFSPostParamsBuilderTest extends WFS20ResourceTestBase {

    private String testName;

    private DownloadStep downloadStep;

    /**
     * @return DownloadSteps to tests
     * @throws IOException if creation of a DownloadStep failed
     */
    @Parameterized.Parameters
    public static Collection<Object[]> downloadSteps() throws IOException {
        return Arrays.asList(new Object[][] {
            {"SimpleExample", createDownloadStep("bvv:gmd_ex")},
            {"Example1", createDownloadStep("bvv:gmd_ex (Filter)",
                "\"bvv:sch\" = '09774135'")},
            {"Example2", createDownloadStep("bvv:gmd_ex (Filter)",
                "\"bvv:sch\" LIKE '09774%'")},
            {"Example3", createDownloadStep(
                "Typübergreifende Abfrage (Filter)",
                "\"bvv:lkr_ex\" WHERE \"bvv:sch\" = '09774'\n"
                    + "\"bvv:gmd_ex\" WHERE \"bvv:sch\" LIKE '09774%'")},
            {"Example4", createDownloadStep(
                "Typübergreifende Abfrage (Filter)",
                "\"bvv:lkr_ex\" WHERE \"bvv:sch\" = '09774'\n"
                    + "\"bvv:gmd_ex\" WHERE \"bvv:sch\" IN "
                    + "('09161000', '09161000')")}
        });
    }

    /**
     * Tests a single Build.
     *
     * @param testName     name of the test
     * @param downloadStep DownloadStep to test
     */
    public WFSPostParamsBuilderTest(String testName,
                                    DownloadStep downloadStep) {
        this.testName = testName;
        this.downloadStep = downloadStep;
    }

    /**
     * Test.
     *
     * @throws Exception e
     */
    @Test
    public void testCreate() throws Exception {
        System.out.println("Start test " + testName + "...");
        Set<String> usedVars = new HashSet<>();
        WFSMeta meta = parseMeta();
        Document wfsRequest = WFSPostParamsBuilder.create(downloadStep,
            usedVars, meta);
        printDocument(wfsRequest);

        URL wfsSchema = new URL(
            "http://schemas.opengis.net/wfs/2.0/wfs.xsd");
        assertThat(the(wfsRequest), conformsTo(w3cXmlSchemaFrom(wfsSchema)));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@srsName",
            namespaceContext(),
            is(getValue(downloadStep, "srsName"))));

        assertThat(the(wfsRequest), hasXPath(
            "/wfs:GetFeature/wfs:Query/@typeNames",
            namespaceContext(),
            is(downloadStep.getDataset())));
    }

    private WFSMeta parseMeta()
        throws IOException, URISyntaxException {
        int port = port();
        String queryPath = "/geoserver/wfs";
        String queryResource = "/wfs20/geoserver/geoserver-capabilities.xml";
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
        if (cql != null) {
            parameters.add(new Parameter("CQL", cql));
        }
        return new DownloadStep(dataset, parameters, "WFS2_SQL",
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

    private static void printDocument(Node doc)
        throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(
            "{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc),
            new StreamResult(new OutputStreamWriter(System.out, "UTF-8")));
    }

    private NamespaceContext namespaceContext() {
        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.bind("wfs", "http://www.opengis.net/wfs/2.0");
        return namespaceContext;
    }

}
