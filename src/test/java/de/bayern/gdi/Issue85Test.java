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

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.model.Parameter;
import de.bayern.gdi.model.ProcessingStep;

import de.bayern.gdi.processor.ConverterException;
import de.bayern.gdi.processor.job.ExternalProcessJob;
import de.bayern.gdi.processor.job.Job;
import de.bayern.gdi.processor.ProcessingStepConverter;

import de.bayern.gdi.config.Config;
import de.bayern.gdi.utils.StringUtils;

/**
 * @author Sascha L. Teichmann (sascha.teichmann@intevation.de)
 * Unit test for http://github.com/gdi-by/downloadclient/issues/85.
 */
public class Issue85Test extends TestCase {

    /**
     * Prepare setup for the test case.
     *
     */
    @Before
    @Override
    public void setUp() throws IOException {
        Config.initialize(null);
    }

    /**
     * The test.
     *
     *  @throws ConverterException If something went wrong.
     *
     */
    @Test
    public void testVarReplacement() throws ConverterException {

        ArrayList<ProcessingStep> pss = new ArrayList<>();
        ArrayList<Parameter> params = new ArrayList<>();

        ProcessingStep ps = new ProcessingStep();
        ps.setName("toShape");
        ps.setParameters(new ArrayList<Parameter>());
        pss.add(ps);

        DownloadStep dls = new DownloadStep(
            "", params,
            "", "", "",
            pss);

        ProcessingStepConverter psc = new ProcessingStepConverter();

        psc.convert(dls, null, null);

        for (Job job: psc.getJobs()) {
            if (job instanceof ExternalProcessJob) {
                ExternalProcessJob epj = (ExternalProcessJob)job;
                List<String> cmds = epj.commandList();
                String cmd = StringUtils.join(cmds, " ");
                assertEquals(
                    cmd, "ogr2ogr -f ESRI Shapefile shp *.{gml,kml,shp}");
                return;
            }
        }

        fail("No external processing job found");
    }
}
