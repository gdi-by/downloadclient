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

import org.geotools.filter.text.cql2.CQLException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

import javax.xml.namespace.NamespaceContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlmatchers.XmlMatchers.conformsTo;
import static org.xmlmatchers.XmlMatchers.hasXPath;
import static org.xmlmatchers.transform.XmlConverters.the;
import static org.xmlmatchers.validation.SchemaFactory.w3cXmlSchemaFrom;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FilterEncoderTest {

    /**
     * Test validation of valid CQL.
     *
     * @throws Exception e
     */
    @Test
    public void testValidateValidCql() throws Exception {
        FilterEncoder filterEncoder = new FilterEncoder();
        filterEncoder.validateCql("\"bvv:sch\" LIKE '09774%'");
    }

    /**
     * Test validation of invalid CQL.
     *
     * @throws Exception e
     */
    @Test(expected = CQLException.class)
    public void testValidateInvalidCql() throws Exception {
        FilterEncoder filterEncoder = new FilterEncoder();
        filterEncoder.validateCql("\"bvv:sch\" IS LIKE '09774%'");
    }

    /**
     * Test creation of valid CQL.
     *
     * @throws Exception e
     */
    @Test
    public void testFilter() throws Exception {
        FilterEncoder filterEncoder = new FilterEncoder();
        List<Document> filters = filterEncoder.initializeQueries(
            "\"bvv:sch\" LIKE '09774%'");

        assertThat(filters.size(), is(1));

        Document filter = filters.get(0);
        assertThat(the(filter), conformsTo(w3cXmlSchemaFrom(fes())));
        assertThat(the(filter), hasXPath("/fes:Filter", namespaceContext()));
    }

    /**
     * Test creation of valid CQL.
     *
     * @throws Exception e
     */
    @Test
    public void testFilterMultipleLines() throws Exception {
        FilterEncoder filterEncoder = new FilterEncoder();
        String userInput = "\"bvv:lkr_ex\" WHERE \"bvv:sch\" LIKE '09774'\n"
            + "\"bvv:gmd_ex\" WHERE \"bvv:sch\" LIKE '09161000'";
        List<Document> filters = filterEncoder.initializeQueries(userInput);

        assertThat(filters.size(), is(2));

        Document firstFilter = filters.get(0);
        assertThat(the(firstFilter), conformsTo(w3cXmlSchemaFrom(fes())));
        assertThat(the(firstFilter), hasXPath("/fes:Filter",
            namespaceContext()));

        Document secondFilter = filters.get(1);
        assertThat(the(secondFilter), conformsTo(w3cXmlSchemaFrom(fes())));
        assertThat(the(secondFilter), hasXPath("/fes:Filter",
            namespaceContext()));
    }

    private NamespaceContext namespaceContext() {
        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.bind("fes", "http://www.opengis.net/fes/2.0");
        return namespaceContext;
    }

    private URL fes() throws MalformedURLException {
        return new URL(
            "http://schemas.opengis.net/filter/2.0/filter.xsd");
    }

}
