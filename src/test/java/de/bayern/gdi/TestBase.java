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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.junit.BeforeClass;
import org.testfx.framework.junit.ApplicationTest;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

/**
 * Base Class for JavaFXTests.
 *
 * @author thomas
 */
public class TestBase extends ApplicationTest {

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
     * Titlepane
     */
    private static final String HISTORY_PARENT =
            "#logHistoryParent";
    /**
     * ServiceURL element
     */
    private static final String SERVICE_URL =
            "#serviceURL";
    /**
     * Ready state
     */
    private static final String READY_STATUS =
            "status.ready";
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
    private void waitForStatus(TitledPane titlePane, String status) {
        await()
                .atMost(TIMEOUT_SECONDS, SECONDS)
                .pollInterval(POLL_MILLISECONDS, MILLISECONDS)
                .until(
                        () -> titlePane.getText()
                                .equals(I18n.getMsg(status))
                );
    }

    /**
     * Waits until UI has settled down
     */
    void waitUntilReady() {
        TitledPane titledPane = getPane();
        waitForStatus(titledPane, READY_STATUS);
    }

    /**
     * Retrieves the title pane
     *
     * @return the selected pane
     */
    private TitledPane getPane() {
        return getElementById(HISTORY_PARENT, TitledPane.class);
    }

    /**
     * Get state from titlepane
     */
    boolean titlePaneShows(String msg) {
        TitledPane titledPane = getPane();
        return titledPane.getText().equals(
                I18n.getMsg(msg));
    }

    /**
     * Sets the serviceURL
     */
    void setServiceUrl(String url) {
        TextField serviceURL = getServiceURL();
        serviceURL.setText(url);
    }

    /**
     * Retrieve ServiceURLTextfield
     *
     * @return Textfield
     */
    private TextField getServiceURL() {
        return getElementById(SERVICE_URL, TextField.class);
    }


    /**
     * Gets Element from the scene.
     *
     * @param element Selector for the element to look
     * @param type    Class of the Element
     * @param <T>     Generic returntype
     * @return Returns Element
     */
    private <T extends javafx.scene.control.Control> T getElementById(
            String element, Class<T> type) {
        return type.cast(scene.lookup(element));
    }

}
