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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author thomas
 */
public class IntegrationTest extends TestBase {

    /**
     * Number of steps.
     */
    private static final int TWO_ELEMENTS = 2;

    /**
     * Tests the initial state of the application.
     *
     * @throws Exception if something went wrong.
     */
    @Test
    public void initialState() throws Exception {
        waitUntilReady();
        assertTrue(titlePaneShows(READY_STATUS));
        assertTrue(isEmpty(SEARCH));
        assertTrue(isEmpty(URL));
        assertTrue(isEmpty(USERNAME));
        assertTrue(isEmpty(PASSWORD));
        assertTrue(isEmpty(LIST_OF_SERVICES));
    }

    /**
     * When no URL is chosen, the user should get feedback.
     * @throws Exception if something went wrong.
     */
    @Test
    public void noURLChosen() throws Exception {
        waitUntilReady();
        clickOn(SERVICE_SELECTION);
        assertTrue(titlePaneShows(NO_URL));
    }

    /**
     * Select additional steps.
     * @throws Exception if something went wrong.
     */
    @Test
    public void activateProcessingSteps() throws Exception {
        addOneStep();
        assertFalse(isEmpty(PROCESSINGSTEPS));
        assertTrue(size(PROCESSINGSTEPS, TWO_ELEMENTS));
    }

    /**
     * Adds one processing step.
     */
    private void addOneStep() {
        waitUntilReady();
        clickOn(ACTIVATE_FURTHER_PROCESSING);
    }

    /**
     * Selects steps and adds one.
     * @throws Exception if something went wrong.
     */
    @Test
    public void addOneProcessingStep() throws Exception {
        addOneStep();
        clickOn(ADD_PROCESSING_STEP);
        assertFalse(isEmpty(PROCESSINGSTEPS));
        assertTrue(titlePaneShows(NO_FORMAT_CHOSEN));
    }

}
