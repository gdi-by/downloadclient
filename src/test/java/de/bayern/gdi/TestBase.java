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

import de.bayern.gdi.gui.DataBean;
import de.bayern.gdi.gui.WarningPopup;
import de.bayern.gdi.gui.controller.ButtonBarController;
import de.bayern.gdi.gui.controller.Controller;
import de.bayern.gdi.gui.controller.DownloadListener;
import de.bayern.gdi.gui.controller.FXMLLoaderProducer;
import de.bayern.gdi.gui.controller.FilterAtomController;
import de.bayern.gdi.gui.controller.FilterWfsBasicController;
import de.bayern.gdi.gui.controller.FilterWfsSimpleController;
import de.bayern.gdi.gui.controller.MenuBarController;
import de.bayern.gdi.gui.controller.ProcessingChainController;
import de.bayern.gdi.gui.controller.ServiceSelectionController;
import de.bayern.gdi.gui.controller.ServiceTypeSelectionController;
import de.bayern.gdi.gui.controller.StatusLogController;
import de.bayern.gdi.config.Config;
import de.bayern.gdi.utils.DocumentResponseHandler;
import de.bayern.gdi.utils.FileResponseHandler;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Misc;
import de.bayern.gdi.utils.SceneConstants;
import de.bayern.gdi.utils.Unauthorized;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.framework.junit.ApplicationTest;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import static de.bayern.gdi.utils.SceneConstants.CQL_INPUT;
import static de.bayern.gdi.utils.SceneConstants.DATAFORMATCHOOSER;
import static de.bayern.gdi.utils.SceneConstants.HEIGHT;
import static de.bayern.gdi.utils.SceneConstants.HISTORY_PARENT;
import static de.bayern.gdi.utils.SceneConstants.PROCESSINGSTEPS;
import static de.bayern.gdi.utils.SceneConstants.PROCESS_SELECTION;
import static de.bayern.gdi.utils.SceneConstants.READY_STATUS;
import static de.bayern.gdi.utils.SceneConstants.SERVICE_LIST;
import static de.bayern.gdi.utils.SceneConstants.SERVICE_TYPE_CHOOSER;
import static de.bayern.gdi.utils.SceneConstants.WIDTH;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

/**
 * Base Class for JavaFXTests.
 *
 * @author thomas
 */
public abstract class TestBase extends ApplicationTest {

    // Constants

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
     * The Scene.
     */
    private Scene scene;

    /**
     * The Controller.
     */
    protected Controller controller;

    /**
     * The logger.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(TestBase.class);

    /**
     * The WeldInitiator.
     */
    @Rule
    public WeldInitiator weld = WeldInitiator.from(FXMLLoaderProducer.class, Controller.class,
                                                    MenuBarController.class, StatusLogController.class,
                                                    ServiceSelectionController.class, ProcessingChainController.class,
                                                    ServiceTypeSelectionController.class, FilterAtomController.class,
                                                    FilterWfsBasicController.class, FilterWfsSimpleController.class,
                                                    ButtonBarController.class, DownloadListener.class)
                                             .inject(this).build();;

    @Inject
    private FXMLLoader fxmlLoader;

    // Overrides and Annotated Methods
    /**
     * Initial phase.
     */
    @BeforeClass
    public static void initTests() {
        LOG.debug("init tests ....");
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
        LOG.debug("start stage ...");
        Config.initialize(null);
        LOG.debug("Preparing app for controller tests");
        Parent root = fxmlLoader.load();
        scene = new Scene(root, WIDTH, HEIGHT);
        controller = fxmlLoader.getController();
        controller.setDataBean(getDataBean());
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
     * @return the DataBean to pass to the Controller.
     * @throws IOException if an exception occurred
     */
    protected DataBean getDataBean() throws IOException {
        return new DataBean();
    }

    // Methods

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

    /**
     * Retrieves the title pane.
     *
     * @return the selected pane
     */
    private TitledPane getPane() {
        return getElementById(HISTORY_PARENT, TitledPane.class);
    }

    /**
     * Tests, if a given field has n elements.
     */
    boolean hasSize(String element, int number) {
        return size(element, x -> x == number);
    }

    /**
     * Checks whether checkox is checked.
     *
     * @return true, if so
     */
    boolean isChecked(String checkbox) {
        CheckBox b = getElementById(checkbox, CheckBox.class);
        return b.isSelected();
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
            case CQL_INPUT:
                TextArea ta = getElementById(element, TextArea.class);
                result = ta.getText().isEmpty();
                break;
            default:
                TextField t = getElementById(element, TextField.class);
                result = t.getText().isEmpty();
        }
        return result;
    }

    /**
     * Select an option of a combobox.
     *
     * @param index    index of option
     * @param combobox id of combobox
     */
    private void selectComboBoxOption(int index, String combobox) {
        ComboBox box = getElementById(combobox, ComboBox.class);
        box.getSelectionModel().select(index);
    }

    /**
     * Selects the Service at position n.
     *
     * @param n number. Zerobased index
     */
    void selectNthService(int n) {
        ListView lw = getElementById(SERVICE_LIST, ListView.class);
        clickOn(lw.getItems().get(n).toString());
    }

    /**
     * Selects dataformat by zerobased index.
     *
     * @param index of service
     */
    void selectDataFormatByNumber(int index) {
        selectComboBoxOption(index, DATAFORMATCHOOSER);
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
     * Sets the serviceURL.
     */
    void setServiceUrl(String url) {
        setTextField(SceneConstants.URL, url);
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
     * Sets CQL to TextArea.
     *
     * @param text    content
     */
    void setCqlInput(String text) {
        TextArea field = getElementById(CQL_INPUT, TextArea.class);
        field.setText(text);
    }

    /**
     * More generic size method.
     *
     * @param element    element to which size is compared
     * @param comparison lambda
     * @return true, if so
     */
    boolean size(String element,
                 Predicate<Integer> comparison) {
        boolean result;
        switch (element) {
            case PROCESS_SELECTION:
            case SERVICE_TYPE_CHOOSER:
                ComboBox cb = getElementById(element, ComboBox.class);
                LOG.debug("Element '{}' has {} entries: {}", element, cb.getItems().size(), cb.getItems());
                result = comparison.test(cb.getItems().size());
                break;
            case SERVICE_LIST:
                ListView lv = getElementById(element, ListView.class);
                LOG.debug("Element '{}' has {} entries: {}", element, lv.getItems().size(), lv.getItems());
                result = comparison.test(lv.getItems().size());
                break;
            case PROCESSINGSTEPS:
                HBox b = getElementById(element, HBox.class);
                LOG.debug("Element '{}' has {} entries: {}", element, b.getChildren().size(), b.getChildren());
                result = comparison.test(b.getChildren().size());
                break;
            default:
                TextField t = getElementById(element, TextField.class);
                LOG.debug("Element '{}' has {} entries: {}", element, t.getText().length(), t.getText());
                result = comparison.test(t.getText().length());
        }
        return result;
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
     * Waits untitl UI has reached state.
     */
    void waitFor(String state) {
        TitledPane titledPane = getPane();
        waitForTitlebarStatus(titledPane, state);
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
     * Waits until UI has settled down.
     */
    protected void waitUntilReady() {
        waitFor(READY_STATUS);
    }
}
