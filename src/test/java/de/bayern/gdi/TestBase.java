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

import de.bayern.gdi.gui.Controller;
import de.bayern.gdi.gui.DataBean;
import de.bayern.gdi.gui.Start;
import de.bayern.gdi.gui.WarningPopup;
import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.DocumentResponseHandler;
import de.bayern.gdi.utils.FileResponseHandler;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Misc;
import de.bayern.gdi.utils.Unauthorized;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import static org.awaitility.Awaitility.await;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.testfx.framework.junit.ApplicationTest;

/**
 * Base Class for JavaFXTests.
 *
 * @author thomas
 */
public class TestBase extends ApplicationTest {

    /**
     * ServiceURL element.
     */
    protected static final String URL =
        "#serviceURL";
    /**
     * Calling the service msg.
     */
    static final String CALLING_SERVICE =
        "status.calling-service";
    /**
     * Processing steps Containerelement.
     */
    static final String PROCESSINGSTEPS =
        "#processStepContainter";
    /**
     * Ready state.
     */
    static final String READY_STATUS =
        "status.ready";
    /**
     * Search field.
     */
    static final String SEARCH =
        "#searchField";
    /**
     * Username field.
     */
    static final String USERNAME =
        "#serviceUser";
    /**
     * Password field.
     */
    static final String PASSWORD =
        "#servicePW";
    /**
     * Selection field.
     */
    static final String SERVICE_SELECTION =
        "#serviceSelection";
    /**
     * ACTIVATE_FURTHER_PROCESSING.
     */
    static final String ACTIVATE_FURTHER_PROCESSING =
        "#chkChain";
    /**
     * ADD_PROCESSING_STEP.
     */
    static final String ADD_PROCESSING_STEP =
        "#addChainItem";
    /**
     * No format chosen.
     */
    static final String NO_FORMAT_CHOSEN =
        "gui.process.no.format";
    /**
     * No URL.
     */
    static final String NO_URL =
        "status.no-url";
    /**
     * Selection of service kinds.
     */
    static final String SERVICE_TYPE_CHOOSER =
        "#serviceTypeChooser";
    /**
     * The list of services.
     */
    protected static final String SERVICE_LIST =
        "#serviceList";
    /**
     * Width of the scene.
     */
    private static final int WIDTH = 1024;
    /**
     * Height of the scene.
     */
    private static final int HEIGHT = 768;
    /**
     * Timeout.
     */
    private static final int TIMEOUT_SECONDS = 15;
    /**
     * Polling time.
     */
    private static final int POLL_MILLISECONDS = 250;
    /**
     * Name of the Logo.
     */
    private static final String LOGONAME = "icon_118x118_300dpi.jpg";
    /**
     * Titlepane.
     */
    private static final String HISTORY_PARENT =
        "#logHistoryParent";
    /**
     * ProcessStep Combobox.
     */
    static final String PROCESS_SELECTION =
        "#process_name";

    /**
     * The Scene.
     */
    private Scene scene;

    /**
     * The logger.
     */
    private Logger log = Logger.getLogger(
        this.getClass().getName());

    /**
     * DataFormatChooser Combobox.
     */
    private static final String DATAFORMATCHOOSER =
        "#dataFormatChooser";

    /**
     * Initial phase.
     */
    @BeforeClass
    public static void initTests() {
        System.err.println("init tests ....");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
    }

    /**
     * starts the application.
     *
     * @param primaryStage the stage
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        System.err.println("start stage ...");
        Config.initialize(null);

        log.log(Level.INFO, "Preparing app for controller tests");
        ClassLoader classLoader = Start.class.getClassLoader();
        URL url = classLoader.getResource("download-client.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(url, I18n.getBundle());
        Parent root = fxmlLoader.load();
        scene = new Scene(root, WIDTH, HEIGHT);
        Controller controller = fxmlLoader.getController();
        controller.setDataBean(new DataBean());
        controller.setPrimaryStage(primaryStage);
        Unauthorized unauthorized = new WarningPopup();
        FileResponseHandler.setUnauthorized(unauthorized);
        DocumentResponseHandler.setUnauthorized(unauthorized);
        Image image = new Image(Misc.getResource("img/" + LOGONAME));
        primaryStage.getIcons().add(image);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    /**
     * Waits for Display of Status in TitlePane.
     *
     * @param titlePane Pane to watch
     * @param status    Status to watch for
     */
    private void waitForTitlebarStatus(TitledPane titlePane, String status) {
        await()
            .atMost(TIMEOUT_SECONDS, SECONDS)
            .pollInterval(POLL_MILLISECONDS, MILLISECONDS)
            .until(
                titlePaneShowsMessage(titlePane, status)
            );
    }

    /**
     * Waits for the servicelist to populate.
     */
    void waitForPopulatedServiceList() {
        ListView serviceList = getElementById(SERVICE_LIST, ListView.class);
        await()
            .atMost(TIMEOUT_SECONDS, SECONDS)
            .pollInterval(POLL_MILLISECONDS, MILLISECONDS)
            .until(serviceListIsPopulated(serviceList));
    }

    /**
     * Lambda for Titlepane.
     *
     * @param titlePane The titlepane to look at
     * @param status    the status to look fot
     * @return whether the status is set according expectations
     */
    private Callable<Boolean> titlePaneShowsMessage(TitledPane titlePane,
                                                    String status) {
        return () -> titlePane.getText().equals(I18n.getMsg(status));
    }

    /**
     * Lambda for service list.
     *
     * @param serviceList The servicelist to watch
     * @return true, if there is at least one element
     */
    private Callable<Boolean> serviceListIsPopulated(ListView serviceList) {
        return () -> serviceList.getItems().size() > 0;
    }

    /**
     * Waits until UI has settled down.
     */
    void waitUntilReady() {
        TitledPane titledPane = getPane();
        waitForTitlebarStatus(titledPane, READY_STATUS);
    }

    /**
     * Retrieves the title pane.
     *
     * @return the selected pane
     */
    private TitledPane getPane() {
        return getElementById(HISTORY_PARENT, TitledPane.class);
    }

    /**
     * Get state from titlepane.
     */
    boolean titlePaneShows(String msg) {
        TitledPane titledPane = getPane();
        return titledPane.getText().equals(
            I18n.getMsg(msg));
    }

    /**
     * Sets the serviceURL.
     */
    void setServiceUrl(String url) {
        setTextField(URL, url);
    }

    /**
     * Sets content of TextField.
     *
     * @param fieldId Id of field
     * @param text    content
     */
    private void setTextField(String fieldId, String text) {
        TextField field = getElementById(fieldId, TextField.class);
        field.setText(text);
    }

    /**
     * Checks whether a given textfield fieldContentContains a given text.
     *
     * @param fieldId Id of field
     * @param text    text to compare to
     * @return result of comparison
     */
    boolean fieldContentContains(String fieldId, String text) {
        TextField field = getElementById(fieldId, TextField.class);
        return field.getText().contains(text);
    }

    /**
     * Checks whether a given textfield equals a given text.
     *
     * @param fieldId Id of field
     * @param text    text to compare to
     * @return result of comparison
     */
    boolean fieldContentEquals(String fieldId, String text) {
        TextField field = getElementById(fieldId, TextField.class);
        return field.getText().contains(text);
    }

    /**
     * Selects service by zerobased index.
     * @param index of service
     */
    void selectServiceByNumber(int index) {
       ListView services =  getElementById(SERVICE_LIST, ListView.class);
       services.getSelectionModel().select(index);
    }

    /**
     * Selects dataformat by zerobased index.
     * @param index of service
     */
    void selectDataFormatByNumber(int index) {
        selectComboBoxOption(index, DATAFORMATCHOOSER);
    }

    /**
     * Selects serviceType by zerobased index.
     * @param index of service
     */
    void selectServiceTypeByNumber(int index) {
        selectComboBoxOption(index, SERVICE_TYPE_CHOOSER);
    }

    /**
     * Select an option of a combobox
     * @param index index of option
     * @param combobox id of combobox
     */
    private void selectComboBoxOption(int index, String combobox) {
        ComboBox box = getElementById(combobox, ComboBox.class);
        box.getSelectionModel().select(index);
    }

    /**
     * Tests, whether a given field has no content.
     */
    boolean isEmpty(String element) {
        boolean result;
        switch (element) {
            case PROCESS_SELECTION:
            case SERVICE_TYPE_CHOOSER:
                ComboBox cb = getElementById(element, ComboBox.class);
                result = cb.getItems().isEmpty();
                break;
            case SERVICE_LIST:
                ListView lv = getElementById(element, ListView.class);
                result = lv.getItems().isEmpty();
                break;
            case PROCESSINGSTEPS:
                HBox b = getElementById(element, HBox.class);
                result = b.getChildren().isEmpty();
                break;
            default:
                TextField t = getElementById(element, TextField.class);
                result = t.getText().isEmpty();
        }
        return result;
    }

    /**
     * Tests, if a given field has n elements.
     */
    boolean size(String element, int number) {
        boolean result;
        switch (element) {
            case PROCESS_SELECTION:
            case SERVICE_TYPE_CHOOSER:
                ComboBox cb = getElementById(element, ComboBox.class);
                result = cb.getItems().size() == number;
                break;
            case SERVICE_LIST:
                ListView lv = getElementById(element, ListView.class);
                result = lv.getItems().size() == number;
                break;
            case PROCESSINGSTEPS:
                HBox b = getElementById(element, HBox.class);
                result = b.getChildren().size() == number;
                break;
            default:
                TextField t = getElementById(element, TextField.class);
                result = t.getText().length() == number;
        }
        return result;
    }

    /**
     * Gets Element from the scene.
     *
     * @param el  Selector for the element to look
     * @param t   Class of the Element
     * @param <T> Generic returntype
     * @return Returns Element
     */
    private <T extends javafx.scene.layout.Region> T getElementById(String el,
                                                                    Class<T> t
    ) {
        return t.cast(scene.lookup(el));
    }

}
