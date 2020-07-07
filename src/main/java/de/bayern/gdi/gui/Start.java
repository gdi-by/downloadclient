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

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import java.util.concurrent.CountDownLatch;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class Start extends Application {

    private static final Logger LOG
        = LoggerFactory.getLogger(Start.class.getName());

    private static final CountDownLatch LATCH = new CountDownLatch(1);

    private static Start start;

    private SeContainer container;

    /**
     * waits for the javafx application to startup.
     *
     * @return the application
     */
    public static Start waitForStart() {
        try {
            LATCH.await();
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return getInstance();
    }

    /**
     * Constructor.
     */
    public Start() {
        setInstance(this);
        LATCH.countDown();
    }

    private static synchronized void setInstance(Start s) {
        Start.start = s;
    }

    private static synchronized Start getInstance() {
        return Start.start;
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void start(Stage primaryStage) {
        SeContainerInitializer initializer = SeContainerInitializer.newInstance();
        container = initializer.disableDiscovery().addBeanClasses(FxMain.class, FXMLLoaderProducer.class, StatusLogController.class, Controller.class, MenuBarController.class).initialize();
        container.select(FxMain.class).get().start(primaryStage, getParameters());
    }

    @Override
    public void stop() throws Exception {
        container.close();
    }

}
