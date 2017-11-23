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

import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.utils.DownloadConfiguration;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;

/**
 * @author thomas
 */
public class IntegrationTest extends TestBase {

    /**
     * Number of steps.
     */
    private static final int TWO_ELEMENTS = 2;

    /**
     * Total number of services.
     */
    private static final int TOTAL_NUMBER_OF_SERVICES = 13;


    /**
     * Biergarten text.
     */
    private static final String BIERGARTEN = "Biergarten";
    /**
     * Biergarten URL.
     */
    private static final String BIERGARTEN_URL =
        "https://geoportal.bayern.de/gdiadmin/ausgabe/ATOM_SERVICE/"
            + "a90c75a0-f1b5-46e7-9e45-c0385fd0c200";

    /**
     * Verwaltungsgrenzen.
     */
    private static final String VERWALTUNGSGRENZEN = "Verwaltungsgrenzen";

    /**
     * Verwaltungsgrenzen_URL.
     */
    private static final String VERWALTUNGSGRENZEN_URL =
        "http://geoserv.weichand.de:8080/geoserver/wfs?service="
            + "wfs&acceptversions=2.0.0"
            + "&request=GetCapabilities";
    /**
     * Downloadconfigurator.
     */
    private static final DownloadConfiguration DOWNLOAD_CONFIGURATION =
        new DownloadConfiguration();

    /**
     * Before wait for Ready state.
     *
     * @throws Exception just in case
     */
    @Before
    public void applicationReady() throws Exception {
        waitUntilReady();
    }

    /**
     * Tests the initial state of the application.
     *
     * @throws Exception if something went wrong.
     */
    @Test
    public void initialState() throws Exception {
        assertTrue(titlePaneShows(READY_STATUS));
        assertTrue(isEmpty(SEARCH));
        assertTrue(isEmpty(URL));
        assertTrue(isEmpty(USERNAME));
        assertTrue(isEmpty(PASSWORD));
        assertTrue(isEmpty(SERVICE_LIST));
        clickOn(DOWNLOAD_BUTTON);
        clickOn(SAVE_BUTTON);
    }

    /**
     * When no URL is chosen, the user should get feedback.
     *
     * @throws Exception if something went wrong.
     */
    @Test
    public void noURLChosen() throws Exception {
        clickOn(SERVICE_SELECTION);
        assertTrue(titlePaneShows(NO_URL));
    }

    /**
     * Select additional steps.
     *
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
        clickOn(ACTIVATE_FURTHER_PROCESSING);
    }

    /**
     * Selects steps and adds one.
     *
     * @throws Exception if something went wrong.
     */
    @Test
    public void addOneProcessingStep() throws Exception {
        addOneStep();
        clickOn(ADD_PROCESSING_STEP);
        assertFalse(isEmpty(PROCESSINGSTEPS));
        assertTrue(titlePaneShows(NO_FORMAT_CHOSEN));
    }

    /**
     * Select Biergarten.
     *
     * @throws Exception in case something breaks
     */
    @Test
    public void searchBiergarten() throws Exception {
        selectBiergarten();
        assertTrue(titlePaneShows(CALLING_SERVICE));
    }

    /**
     * Enters "Biergarten" in the search area.
     */
    private void selectBiergarten() {
        clickOn(SEARCH).write(BIERGARTEN);
        waitForPopulatedServiceList();
    }

    /**
     * Chooses "Biergarten"-Service.
     * This is an example for an Atom Feed
     *
     * @throws Exception in case something breaks
     */
    @Test
    public void chooseBiergarten() throws Exception {
        selectBiergarten();
        setServiceUrl(BIERGARTEN_URL);
        clickOn(SERVICE_SELECTION);
        waitUntilReady();
        assertFalse(isEmpty(SERVICE_TYPE_CHOOSER));
    }

    /**
     * Chooses "Verwaltungsgrenzen".
     * <p>
     * This is an example for an WFS2 Feed
     *
     * @throws Exception in case something breaks
     */
    @Test
    public void chooseVerwaltungsgrenzen() throws Exception {
        clickOn(SEARCH).write(VERWALTUNGSGRENZEN);
        waitForPopulatedServiceList();
        assertFalse(isEmpty(SERVICE_LIST));
        setServiceUrl(VERWALTUNGSGRENZEN_URL);
        clickOn(SERVICE_SELECTION);
        waitUntilReady();
        assertFalse(isEmpty(SERVICE_TYPE_CHOOSER));
        assertTrue(size(SERVICE_TYPE_CHOOSER, TOTAL_NUMBER_OF_SERVICES));
        selectDataFormatByNumber(0);
        clickOn(ACTIVATE_FURTHER_PROCESSING);
        clickOn(ADD_PROCESSING_STEP);
        assertFalse(isEmpty(PROCESS_SELECTION));
        IntStream.range(1, TOTAL_NUMBER_OF_SERVICES - 1).forEach(i -> {
                selectDataFormatByNumber(i);
                clickOn(SERVICE_TYPE_CHOOSER);
            }
        );
    }

    /**
     * Start biergarten download.
     *
     * @throws Exception just in case
     */
    @Test
    public void testDownloadBiergarten() throws Exception {
        List<DownloadStep> steps = prepareStep(
            DOWNLOAD_CONFIGURATION::getBiergartenConfiguration
        );
        String username = "";
        String password = "";
        int result = Headless.runHeadless(username, password, steps);
        assertTrue(result == 0);
    }

    /**
     * Start Verwaltungsgebiete download.
     *
     * @throws Exception just in case
     */
    @Test
    public void testDownloadNuremburg() throws Exception {
        List<DownloadStep> steps = prepareStep(
            DOWNLOAD_CONFIGURATION::getNuremburgConfig
        );
        String username = "";
        String password = "";
        int result = Headless.runHeadless(username, password, steps);
        assertTrue(result == 0);
    }

    /**
     * Generic DownloadStep preparation.
     * @param getConfig Lambda to retrieve configuration
     * @return Downloadsteps
     * @throws IOException just in case
     */
    private List<DownloadStep> prepareStep(
        Function<String, String> getConfig) throws IOException {
        String dirname = "gdiBY";
        Path tempPath = Files.createTempDirectory(dirname);
        String config = getConfig.apply(tempPath.toString());
        List<DownloadStep> steps = new ArrayList<>();
        steps.add(DownloadStep.read(config));
        return steps;
    }

}
