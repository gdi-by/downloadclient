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
package de.bayern.gdi.model;

import de.bayern.gdi.processor.DownloadStepConverter;
import de.bayern.gdi.processor.FileDownloadJob;
import de.bayern.gdi.processor.Job;
import de.bayern.gdi.processor.JobList;
import de.bayern.gdi.utils.Config;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.nio.file.Files.createTempDirectory;
import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadler;
import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.XmlMatchers.conformsTo;
import static org.xmlmatchers.transform.XmlConverters.the;
import static org.xmlmatchers.validation.SchemaFactory.w3cXmlSchemaFrom;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@RunWith(Parameterized.class)
public class DownloadStepConverterTest {

    private static final int HTTP_OKAY = 200;
    private String testName;
    private DownloadStep downloadStep;

    /**
     * Setup Jadler.
     *
     * @throws IOException if config initialization failed
     */
    @Before
    public void setUp() throws IOException {
        initJadler();
        Config.initialize(null);
    }

    /**
     * Close Jadler.
     */
    @After
    public void tearDown() {
        closeJadler();
    }

    /**
     * @return DownloadSteps to tests
     * @throws IOException if creation of a DownloadStep failed
     */
    @Parameters
    public static Collection<Object[]> downloadSteps() throws IOException {
        return Arrays.asList(new Object[][] {
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
     * Tests a single DownloadStep.
     *
     * @param testName     name of the test
     * @param downloadStep DownloadStep to test
     */
    public DownloadStepConverterTest(String testName,
                                     DownloadStep downloadStep) {
        this.testName = testName;
        this.downloadStep = downloadStep;
    }

    /**
     * Test to execute.
     *
     * @throws Exception if an exception occured
     */
    @Test
    public void testConvert() throws Exception {
        System.out.println("Start test " + testName + "...");
        int port = port();

        prepareCapabilities(port);
        prepareGetFeature();
        prepareDescribeStoredQueries();

        String serviceURL = buildServiceUrl(port);
        downloadStep.setServiceURL(serviceURL);

        DownloadStepConverter downloadStepConverter =
            new DownloadStepConverter();
        JobList jobList = downloadStepConverter.convert(downloadStep);

        FileDownloadJob fileDownloadJob = findFileDownloadJob(jobList);
        assertThat(fileDownloadJob, is(notNullValue()));
        HttpEntity postParams = fileDownloadJob.getPostParams();
        assertThat(postParams, is(notNullValue()));
        String postBody = IOUtils.toString(postParams.getContent());

        URL wfsSchema = new URL("http://schemas.opengis.net/wfs/2.0/wfs.xsd");
        assertThat(the(postBody), conformsTo(w3cXmlSchemaFrom(wfsSchema)));
    }

    private static DownloadStep createDownloadStep(String dataset, String cql)
        throws IOException {
        ArrayList<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter("srsName",
            "urn:ogc:def:crs:EPSG::31468"));
        parameters.add(new Parameter("outputformat",
            "text/xml; subtype=gml/3.2"));
        parameters.add(new Parameter("CQL", cql));
        String path = createTempDirectory("DownloadStepConverterTest")
            .toString();
        return new DownloadStep(dataset, parameters, "WFS2_SQL",
            "DUMMY", path, new ArrayList<>());
    }

    private FileDownloadJob findFileDownloadJob(JobList jobList) {
        List<Job> jobs = jobList.getJobList();
        for (Job job : jobs) {
            if (job instanceof FileDownloadJob) {
                return (FileDownloadJob) job;
            }
        }
        return null;
    }

    private void prepareResource(String method, String queryPath, String body) {
        onRequest()
            .havingMethodEqualTo(method)
            .havingPathEqualTo(queryPath)
            .respond()
            .withStatus(HTTP_OKAY)
            .withBody(body)
            .withEncoding(Charset.forName("UTF-8"))
            .withContentType("application/xml; charset=UTF-8");
    }


    private void prepareCapabilities(int port) throws IOException {
        String capabilities = "/wfs20/geoserver/geoserver-capabilities.xml";
        String body = readResourceAsString(capabilities);
        body = body.replace("{GETFEATURE_URL}",
            buildGetFeatureUrl(port));
        body = body.replace("{DESCRIBESTOREDQUERIES_URL}",
            buildDescribeStoredQueriesUrl(port));
        prepareResource("GET", "/wfs/caps", body);
    }

    private void prepareGetFeature() throws IOException {
        String body = readResourceAsString("/wfs20/getfeature-hits.xml");
        prepareResource("POST", "/wfs/gf", body);
    }

    private void prepareDescribeStoredQueries() throws IOException {
        String body = readResourceAsString("/wfs20/desc-storedqueries.xml");
        prepareResource("GET", "/wfs/wfs", body);
    }

    private String readResourceAsString(String resource) throws IOException {
        InputStream resourceIs = getClass().getResourceAsStream(resource);
        return IOUtils.toString(resourceIs, "UTF-8");
    }

    private String buildServiceUrl(int port) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://localhost:");
        sb.append(port);
        sb.append("/wfs/caps");
        System.out.println("Service-URL: " + sb.toString());
        return sb.toString();
    }

    private CharSequence buildGetFeatureUrl(int port) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://localhost:");
        sb.append(port);
        sb.append("/wfs/gf");
        System.out.println("GetFeature-URL: " + sb.toString());
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
