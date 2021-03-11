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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;
import static org.xmlunit.matchers.ValidationMatcher.valid;

/**
 * Test class to verify CQL to filter expressions encoder.
 *
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
        List<FilterEncoder.QueryToFeatureType> filters =
            filterEncoder.initializeQueries("\"bvv:sch\" LIKE '09774%'");

        assertThat(filters.size(), is(1));

        Document filter = filters.get(0).getFilter();
        assertThat(filter, valid(fes()));
        assertThat(filter, hasXPath("/fes:Filter")
            .withNamespaceContext(fesContext()));
    }


    /**
     * Test creation of valid CQL.
     *
     * @throws Exception e
     */
    @Test
    public void testFilterEqual() throws Exception {
        FilterEncoder filterEncoder = new FilterEncoder();
        List<FilterEncoder.QueryToFeatureType> filters =
            filterEncoder.initializeQueries("\"bvv:sch\" = '09774'");

        assertThat(filters.size(), is(1));

        Document filter = filters.get(0).getFilter();
        assertThat(filter, valid(fes()));
        assertThat(filter, hasXPath("/fes:Filter")
            .withNamespaceContext(fesContext()));
    }


    /**
     * Test creation of valid CQL.
     *
     * @throws Exception e
     */
    @Test
    public void testFilterEqualAndLessThan() throws Exception {
        FilterEncoder filterEncoder = new FilterEncoder();
        List<FilterEncoder.QueryToFeatureType> filters =
            filterEncoder.initializeQueries(
                "\"bvv:sch\" = '09774' AND \"bvv:abc\" <= 9");

        assertThat(filters.size(), is(1));

        Document filter = filters.get(0).getFilter();
        assertThat(filter, valid(fes()));
        assertThat(filter, hasXPath("/fes:Filter")
            .withNamespaceContext(fesContext()));
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
        List<FilterEncoder.QueryToFeatureType> filters =
            filterEncoder.initializeQueries(userInput);

        assertThat(filters.size(), is(2));

        Document firstFilter = filters.get(0).getFilter();
        assertThat(firstFilter, valid(fes()));
        assertThat(firstFilter, hasXPath("/fes:Filter")
            .withNamespaceContext(fesContext()));

        Document secondFilter = filters.get(1).getFilter();
        assertThat(secondFilter, valid(fes()));
        assertThat(secondFilter, hasXPath("/fes:Filter")
            .withNamespaceContext(fesContext()));
    }

    /**
     * Test creation of valid CQL.
     *
     * @throws Exception e
     */
    @Test
    public void testFilterMultipleLinesWhere() throws Exception {
        FilterEncoder filterEncoder = new FilterEncoder();
        String userInput = "\"bvv:lkr_ex\" where \"bvv:sch\" LIKE '09774'\n"
            + "\"bvv:gmd_ex\" WHeRE \"bvv:sch\" LIKE '09161000'";
        List<FilterEncoder.QueryToFeatureType> filters =
            filterEncoder.initializeQueries(userInput);

        assertThat(filters.size(), is(2));

        Document firstFilter = filters.get(0).getFilter();
        assertThat(firstFilter, valid(fes()));
        assertThat(firstFilter, hasXPath("/fes:Filter")
            .withNamespaceContext(fesContext()));

        Document secondFilter = filters.get(1).getFilter();
        assertThat(secondFilter, valid(fes()));
        assertThat(secondFilter, hasXPath("/fes:Filter")
            .withNamespaceContext(fesContext()));
    }

    private Map<String, String> fesContext() {
        Map<String, String> namespaceContext = new HashMap<>();
        namespaceContext.put("fes", "http://www.opengis.net/fes/2.0");
        return namespaceContext;
    }

    private URL fes() throws MalformedURLException {
        return new URL(
            "http://schemas.opengis.net/filter/2.0/filter.xsd");
    }

}
