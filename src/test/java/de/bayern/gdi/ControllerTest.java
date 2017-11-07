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

import java.net.URL;
import java.util.concurrent.CountDownLatch;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.testfx.framework.junit.ApplicationTest;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Unit tests, using TestFX to test controller functions
 * @author Alexander Woestmann (awoestmann@intevation.de)
 */
public class ControllerTest extends ApplicationTest {

    private static final CountDownLatch LATCH = new CountDownLatch(1);
    private static Start start;
    private static Scene scene;
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    private static final String LOGONAME = "icon_118x118_300dpi.jpg";

    private static final int TEN = 10;
    private static final int WAIT_TIMEOUT = 250;

    private static Logger log = Logger.getLogger(
            ControllerTest.class.getName());

    private static Controller controller;

    @BeforeClass
    public static void initTests() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
    }

    /**
     * starts the application.
     * @param primaryStage the stage
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        log.log(Level.INFO, "Preparing app for controller tests");
        Config.initialize(null);
        ClassLoader classLoader = Start.class.getClassLoader();
        URL url = classLoader.getResource("download-client.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(url, I18n.getBundle());
        Parent root = fxmlLoader.load();
        scene = new Scene(root, WIDTH, HEIGHT);
        controller = fxmlLoader.getController();
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


    @Test
    public void processingChainValidationTest() throws Exception {
        TitledPane n = (TitledPane) scene.lookup("#logHistoryParent");
        //wait for the App to load
        await()
            .atMost(TEN, SECONDS)
            .pollInterval(WAIT_TIMEOUT, MILLISECONDS)
            .until(() -> n.getText().equals(I18n.getMsg("status.ready")));

        TextField serviceURL = (TextField) scene.lookup("#serviceURL");
        serviceURL.setText(
                "https://gdiserv.bayern.de/srv66381/services/"
                + "benachteiligtegebiete-wfs?service=wfs&acceptversions=2.0.0"
                + "&request=GetCapabilities");
        clickOn("#serviceSelection");

        //Wait for the service to load
        await()
            .atMost(TEN, SECONDS)
            .pollInterval(WAIT_TIMEOUT, MILLISECONDS)
            .until(() -> n.getText().equals(I18n.getMsg("status.ready")));

        clickOn("#chkChain");
        clickOn("#addChainItem");
        Assert.assertTrue(n.getText().equals(
                I18n.getMsg("gui.process.no.format")));
    }
}
