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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Integration test for running download jobs.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@RunWith(Parameterized.class)
public class DownloadStepConverterIT {

    private static final String BASEURL =
        "http://geoserv.weichand.de:8080/geoserver/wfs";
    private String testName;
    private DownloadStep downloadStep;

    /**
     * Initialise Config.
     *
     * @throws IOException if config initialization failed
     */
    @Before
    public void setUp() throws IOException {
        Config.initialize(null);
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
    public DownloadStepConverterIT(String testName,
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

        DownloadStepConverter downloadStepConverter =
            new DownloadStepConverter();
        JobList jobList = downloadStepConverter.convert(downloadStep);

        FileDownloadJob fileDownloadJob = findFileDownloadJob(jobList);
        assertThat(fileDownloadJob, is(notNullValue()));

        fileDownloadJob.download();

        String downloadPath = downloadStep.getPath();
        Path path = Paths.get(downloadPath);

        DirectoryStream<Path> paths = Files.newDirectoryStream(path);
        Iterator<Path> iterator = paths.iterator();
        assertThat(iterator.hasNext(), is(true));
    }

    private static DownloadStep createDownloadStep(String dataset, String cql)
        throws IOException {
        ArrayList<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter("srsName",
            "urn:ogc:def:crs:EPSG::31468"));
        parameters.add(new Parameter("outputformat",
            "text/xml; subtype=gml/3.2"));
        parameters.add(new Parameter("CQL", cql));
        String path = Files.createTempDirectory("DownloadStepConverterIT")
            .toString();
        return new DownloadStep(dataset, parameters, "WFS2_SQL",
            BASEURL, path, new ArrayList<>());
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
