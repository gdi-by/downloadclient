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
import static de.bayern.gdi.utils.SceneConstants.ACTIVATE_FURTHER_PROCESSING;
import static de.bayern.gdi.utils.SceneConstants.ADD_PROCESSING_STEP;
import static de.bayern.gdi.utils.SceneConstants.AUTH;
import static de.bayern.gdi.utils.SceneConstants.CALLING_SERVICE;
import static de.bayern.gdi.utils.SceneConstants.CQL_INPUT;
import static de.bayern.gdi.utils.SceneConstants.DOWNLOAD_BUTTON;
import static de.bayern.gdi.utils.SceneConstants.NO_FORMAT_CHOSEN;
import static de.bayern.gdi.utils.SceneConstants.NO_URL;
import static de.bayern.gdi.utils.SceneConstants.PASSWORD;
import static de.bayern.gdi.utils.SceneConstants.PROCESSINGSTEPS;
import static de.bayern.gdi.utils.SceneConstants.PROCESS_SELECTION;
import static de.bayern.gdi.utils.SceneConstants.PROTECTED_STATE;
import static de.bayern.gdi.utils.SceneConstants.READY_STATUS;
import static de.bayern.gdi.utils.SceneConstants.SAVE_BUTTON;
import static de.bayern.gdi.utils.SceneConstants.SEARCH;
import static de.bayern.gdi.utils.SceneConstants.SERVICE_LIST;
import static de.bayern.gdi.utils.SceneConstants.SERVICE_SELECTION;
import static de.bayern.gdi.utils.SceneConstants.SERVICE_TYPE_CHOOSER;
import static de.bayern.gdi.utils.SceneConstants.URL;
import static de.bayern.gdi.utils.SceneConstants.USERNAME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

/**
 * Basic integrative tests for the UI component.
 * Tests are not exhaustive although helpful for
 * covering the basic testing needs.
 *
 * @author thomas
 */
public class IntegrationTest extends TestBase {

    // CONSTANTS

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
     * Number of steps.
     */
    private static final int TWO_ELEMENTS = 2;

    /**
     * Total number of FeatureTypes provided by service
     * VERWALTUNGSGRENZEN_URL.
     */
    private static final int VERWALTUNGSGRENZEN_NUMBER_OF_FEATURETYPES = 5;

    /**
     * Total number of StoredQueries provided by service
     * VERWALTUNGSGRENZEN_URL.
     */
    private static final int VERWALTUNGSGRENZEN_NUMBER_OF_STOREDQUERIES = 10;

    /**
     * Number of services overall FeatureTypes.
     */
    private static final int  VERWALTUNGSGRENZEN_NUMBER_OF_OVERALL = 1;

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
     * WFS.
     */
    private static final String WFS = "WFS";

    /**
     * Position of a protected WFS service in the selection list. This value
     * is very likely to change over time!
     */
    private static final int POS_OF_PROTECTED_SERVICE = 3;

    // DATA MEMBERS

    /**
     * Downloadconfigurator.
     */
    private static final DownloadConfiguration DOWNLOAD_CONFIGURATION =
        new DownloadConfiguration();

    // METHODS

    /**
     * Before waitFor for Ready state.
     *
     * @throws Exception just in case
     */
    @Before
    public void applicationReady() throws Exception {
        waitUntilReady();
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
        assertTrue(hasSize(PROCESSINGSTEPS, TWO_ELEMENTS));
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
        int numberOfExpectedServices =
            VERWALTUNGSGRENZEN_NUMBER_OF_FEATURETYPES
                + VERWALTUNGSGRENZEN_NUMBER_OF_FEATURETYPES
                + VERWALTUNGSGRENZEN_NUMBER_OF_STOREDQUERIES
                + VERWALTUNGSGRENZEN_NUMBER_OF_OVERALL;
        assertTrue(hasSize(SERVICE_TYPE_CHOOSER, numberOfExpectedServices));
        selectDataFormatByNumber(0);
        clickOn(ACTIVATE_FURTHER_PROCESSING);
        clickOn(ADD_PROCESSING_STEP);
        assertFalse(isEmpty(PROCESS_SELECTION));
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
     * Generic DownloadStep preparation.
     *
     * @param getConfig Lambda to retrieve configuration
     * @return Downloadsteps
     * @throws IOException just in case
     */
    private List<DownloadStep> prepareStep(Function<String, String>
                                               getConfig) throws IOException {
        String dirname = "gdiBY";
        Path tempPath = Files.createTempDirectory(dirname);
        String config = getConfig.apply(tempPath.toString());
        List<DownloadStep> steps = new ArrayList<>();
        steps.add(DownloadStep.read(config));
        return steps;
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
     * Test a protected service.
     *
     *                   <p>
     *                   TODO selectNthService is a brittle solution
     *                   which circumvents a problem of selecting a
     *                   service by name. Since there are Umlauts in
     *                   the services name, tests broke.
     *                   To improve testability, I added an additional
     *                   assertion, that at least the size has the
     *                   appropriate length.
     *                   <p>
     *                   Works for **now**. tj
     */
    @Test
    public void testProtected() {
        clickOn(SEARCH).write(WFS);
        waitForPopulatedServiceList();
        assertFalse(isEmpty(SERVICE_LIST));
        assertTrue(size(SERVICE_LIST, x -> x > 0));
        // FIXME This magic number shall select a protected service,
        //  but this is selection is instable!
        selectNthService(POS_OF_PROTECTED_SERVICE); // TODO brittle solution
        waitFor(PROTECTED_STATE);
        assertTrue(isChecked(AUTH));
    }

    /**
     * Test the input of CQL.
     */
    @Test
    public void testCqlInputAndDownload() {
        clickOn(SEARCH).write(VERWALTUNGSGRENZEN);
        waitForPopulatedServiceList();
        assertFalse(isEmpty(SERVICE_LIST));
        setServiceUrl(VERWALTUNGSGRENZEN_URL);
        clickOn(SERVICE_SELECTION);
        waitUntilReady();
        assertFalse(isEmpty(SERVICE_TYPE_CHOOSER));
        assertTrue(isEmpty(CQL_INPUT));
        setCqlInput("\"bvv:sch\" LIKE '09162*'");
        clickOn(DOWNLOAD_BUTTON);
        waitUntilReady();
    }

    /**
     * Start AGZ download.
     * <p>
     * Even though the server answers Bad Request. The exitcode is 0 (?)
     *
     * @throws Exception just in case
     */
    @Test
    public void testDownloadAGZ() throws Exception {
        List<DownloadStep> steps = prepareStep(
            DOWNLOAD_CONFIGURATION::getAGZConfiguration
        );
        String username = "";
        String password = "";
        int result = Headless.runHeadless(username, password, steps);
        assertTrue(result == 0);
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
     * Start CQL download.
     *
     * @throws Exception just in case
     */
    @Test
    public void testDownloadCql() throws Exception {
        List<DownloadStep> steps = prepareStep(
            DOWNLOAD_CONFIGURATION::getCqlConfig
        );
        String username = "";
        String password = "";
        int result = Headless.runHeadless(username, password, steps);
        assertTrue(result == 0);
    }

}
