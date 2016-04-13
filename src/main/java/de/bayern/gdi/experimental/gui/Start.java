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

package de.bayern.gdi.experimental.gui;


import javafx.application.Application;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;
/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class Start extends Application {

    private static final CountDownLatch LATCH = new CountDownLatch(1);
    private static Start start = null;

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

        // session scope /application scope Beans initialisieren!
        // muss von Controller zu Controller weitergegeben werden
        DataBean dataBean = new DataBean(primaryStage);
        // Ersten Controller aufrufen
        Controller c = new Controller(dataBean);
        c.show();
    }


}
