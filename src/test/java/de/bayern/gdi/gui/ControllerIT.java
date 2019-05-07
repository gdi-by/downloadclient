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
package de.bayern.gdi.gui;

import de.bayern.gdi.TestBase;
import de.bayern.gdi.services.WFSMeta;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test class for validating user input with CQL expressions.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ControllerIT extends TestBase {

    @Override
    protected DataBean getDataBean() throws IOException {
        DataBean dataBean = new DataBean();
        WFSMeta wfsService = new WFSMeta();
        wfsService.addFeature(createFeature("bvv:gmd_ex"));
        wfsService.addFeature(createFeature("bvv:lkr_ex"));
        dataBean.setWFSService(wfsService);
        return dataBean;
    }

    /**
     * Test simple valid input.
     */
    @Test
    public void testInputValidatorSimple() {
        String userInput = "bvv:objid='DEBYBDLMjK0001Ia'";

        boolean result = controller.validateEcqlUserInput(userInput, false);
        assertThat(result, is(true));

    }

    /**
     * Test valid input with two feature types.
     */
    @Test
    public void testInputValidatorOverall() {
        String userInput =
            "\"bvv:gmd_ex\" WHERE bvv:objid='DEBYBDLMjK0001Ia'\n"
            + "\"bvv:lkr_ex\" WHERE bvv:bez_krs='Ingolstadt'";

        boolean result = controller.validateEcqlUserInput(userInput, false);
        assertThat(result, is(false));
    }


    /**
     * Test simple invalid input with WHERE.
     */
    @Test
    public void testInputValidatorSimpleWithWhere() {
        String userInput =
            "\"bvv:gmd_ex\" WHERE bvv:objid='DEBYBDLMjK0001Ia'";

        boolean result = controller.validateEcqlUserInput(userInput, false);
        assertThat(result, is(false));

    }


    /**
     * Test simple invalid input with line break.
     */
    @Test
    public void testInputValidatorSimpleWithLineBreak() {
        String userInput =
            "\"bvv:gmd_ex\" WHERE \nbvv:objid='DEBYBDLMjK0001Ia'";

        boolean result = controller.validateEcqlUserInput(userInput, false);
        assertThat(result, is(false));

    }

    /**
     * Test invalid input with unknown feature types.
     */
    @Test
    public void testInputValidatorOverallWithUnknownFeatureType() {
        String userInput =
            "\"bvv:unknown\" WHERE bvv:objid='DEBYBDLMjK0001Ia'\n"
            + "\"bvv:lkr_ex\" WHERE bvv:bez_krs='Ingolstadt'";

        boolean result = controller.validateEcqlUserInput(userInput, false);
        assertThat(result, is(false));
    }

    /**
     * Test invalid input without WHERE.
     */
    @Test
    public void testInputValidatorOverallWithoutWhere() {
        String userInput =
            "\"bvv:unknown\" WHERE bvv:objid='DEBYBDLMjK0001Ia'\n"
            + "\"bvv:lkr_ex\" WHERE bvv:bez_krs='Ingolstadt'";

        boolean result = controller.validateEcqlUserInput(userInput, false);
        assertThat(result, is(false));
    }

    private WFSMeta.Feature createFeature(String name) {
        WFSMeta.Feature feature1 = new WFSMeta.Feature();
        feature1.setName(name);
        return feature1;
    }

}
