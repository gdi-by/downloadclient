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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ControllerTest {

    /**
     * Test.
     */
    @Test
    public void testInputValidatorSimple() {
        String userInput = "bvv:objid='DEBYBDLMjK0001Ia'";
        Controller controller = new Controller();
        boolean result = controller.validateUserInput(userInput, false);
        assertThat(result, is(true));

    }

    /**
     * TODO: Check test - was not used before.
     */
    @Test
    public void testInputValidatorOverall() {
        String userInput = "\"bvv:gmd_ex\" WHERE bvv:objid='DEBYBDLMjK0001Ia'\n"
            + "\"bvv:lkr_ex\" WHERE bvv:bez_krs='Ingolstadt'";
        Controller controller = new Controller();

        boolean result = controller.validateUserInput(userInput, false);
        assertThat(result, is(true));
    }

}
