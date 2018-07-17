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

import de.bayern.gdi.WFS20ResourceTestBase;
import de.bayern.gdi.processor.DownloadStepConverter;
import de.bayern.gdi.processor.FileDownloadJob;
import de.bayern.gdi.processor.Job;
import de.bayern.gdi.processor.JobList;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.nio.file.Files.createTempDirectory;
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
public class DownloadStepConverterTest extends WFS20ResourceTestBase {

    private String testName;

    private DownloadStep downloadStep;

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
        String queryPath = "/wfs/caps";
        prepareCapabilities("/wfs20/geoserver/geoserver-capabilities.xml",
            queryPath, port);
        prepareGetFeature();
        prepareDescribeStoredQueries();

        String serviceURL = buildGetCapabilitiesUrl(queryPath, port);
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

}
