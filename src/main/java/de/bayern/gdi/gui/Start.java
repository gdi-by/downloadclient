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

import de.bayern.gdi.utils.DocumentResponseHandler;
import de.bayern.gdi.utils.FileResponseHandler;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Unauthorized;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class Start extends Application {

    private static final CountDownLatch LATCH = new CountDownLatch(1);
    private static Start start;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final String LOGONAME = "icon_118x118_300dpi.jpg";


    /**
     * waits for the javafx application to startup.
     * @return the application
     */
    public static Start waitForStart() {
        try {
            LATCH.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return start;
    }

    /**
     * Constructor.
     */
    public Start() {
        start = this;
        LATCH.countDown();
    }

    /**
     * print a String, if this was invoked.
     */
    public void printInvoking() {
        System.out.println("You called a method on the application");
    }

    /**
     * starts the application.
     * @param primaryStage the stage
     */
    @Override
    public void start(Stage primaryStage) {

        try {
            ClassLoader classLoader = Start.class.getClassLoader();
            URL url = classLoader.getResource("download-client.fxml");
            //System.out.println(url);
            FXMLLoader fxmlLoader = new FXMLLoader(url, I18n.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, WIDTH, HEIGHT);
            DataBean dataBean = null;
            try {
                dataBean = new DataBean();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
            Controller controller = fxmlLoader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setDataBean(dataBean);
            Unauthorized unauthorized = new WarningPopup();
            FileResponseHandler.setUnauthorized(unauthorized);
            DocumentResponseHandler.setUnauthorized(unauthorized);
            primaryStage.setTitle(I18n.getMsg("GDI-BY Download-Client"));
            URL imageURL = classLoader.getResource(LOGONAME);
            Image image = new Image(imageURL.toExternalForm());
            primaryStage.getIcons().add(image);
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent e) {
                    Platform.exit();
                    System.exit(0);
                }
            });
        } catch (IOException ioe) {
            System.out.println("Could not find UI description file.");
            System.out.println(ioe.getMessage());
            System.out.println(ioe.getCause());
            for (StackTraceElement element : ioe.getStackTrace()) {
                System.out.println(element.toString());
            }
        }

        // session scope /application scope Beans initialisieren!
        // muss von Controller zu Controller weitergegeben werden
        // Ersten Controller aufrufen
//        Controller c = new Controller(dataBean);
//        c.show();
    }
}
