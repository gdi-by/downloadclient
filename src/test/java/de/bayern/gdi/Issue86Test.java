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

import java.io.IOException;

import java.nio.charset.Charset;

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
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import org.testfx.framework.junit.ApplicationTest;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import static net.jadler.Jadler.initJadler;
import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;

import static org.hamcrest.Matchers.isOneOf;

import org.apache.commons.io.IOUtils;

/**
 * Unit tests, using TestFX to test controller functions
 * @author Alexander Woestmann (awoestmann@intevation.de)
 */
public class Issue86Test extends ApplicationTest {

    private static Logger log = Logger.getLogger(
            Issue86Test.class.getName());

    private static final CountDownLatch LATCH = new CountDownLatch(1);
    private static Start start;
    private static Scene scene;
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    private static final String LOGONAME = "icon_118x118_300dpi.jpg";

    private static final int TIMEOUT_SECONDS = 15;
    private static final int POLL_MILLISECONDS = 250;

    public static final int HTTP_OKAY = 200;

    private static final String QUERY_RESOURCE =
        "/issues/issue86.xml";
    private static final String QUERY_PATH =
        "/issues/issue86";

    private static Controller controller;

    @BeforeClass
    public static void initTests() {
        System.err.println("init tests ....");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
    }

    @Before
    public void startJadler() {
        System.err.println("Start jadler ...");
        initJadler();
    }

    @After
    public void stopJadler() {
        System.err.println("Stop jadler ...");
        closeJadler();
    }

    /**
     * starts the application.
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

    private static void prepareResource(String queryPath, String body)
        throws IOException {

        onRequest()
            .havingMethod(isOneOf("GET", "HEAD", "POST"))
            .havingPathEqualTo(queryPath)
        .respond()
            .withStatus(HTTP_OKAY)
            .withBody(body)
            .withEncoding(Charset.forName("UTF-8"))
            .withContentType("application/xml; charset=UTF-8");
    }

    private static String buildGetCapabilitiesUrl(String queryPath, int port) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://localhost:");
        sb.append(port);
        sb.append(queryPath);
        System.out.println("GetCapabilities-URL: " + sb.toString());
        return sb.toString();
    }

    @Test
    public void processingChainValidationTest() throws Exception {

        String body = IOUtils.toString(
            Issue86Test.class.getResourceAsStream(QUERY_RESOURCE), "UTF-8");
        prepareResource(QUERY_PATH, body);

        TitledPane n = (TitledPane) scene.lookup("#logHistoryParent");
        //wait for the App to load
        await()
            .atMost(TIMEOUT_SECONDS, SECONDS)
            .pollInterval(POLL_MILLISECONDS, MILLISECONDS)
            .until(() -> n.getText().equals(I18n.getMsg("status.ready")));
        TextField serviceURL = (TextField) scene.lookup("#serviceURL");

        String urlText = buildGetCapabilitiesUrl(QUERY_PATH, port());

        serviceURL.setText(urlText);
        clickOn("#serviceSelection");

        //Wait for the service to load
        await()
            .atMost(TIMEOUT_SECONDS, SECONDS)
            .pollInterval(POLL_MILLISECONDS, MILLISECONDS)
            .until(() -> n.getText().equals(I18n.getMsg("status.ready")));

        clickOn("#chkChain");
        clickOn("#addChainItem");
        Assert.assertTrue(n.getText().equals(
                I18n.getMsg("gui.process.no.format")));
    }
}
