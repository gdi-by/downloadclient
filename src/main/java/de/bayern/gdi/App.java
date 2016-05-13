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

import de.bayern.gdi.experimental.WFSTwoServiceHandler;
import de.bayern.gdi.gui.Start;
import de.bayern.gdi.services.WebService;
import de.bayern.gdi.utils.ServiceChecker;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;

/**
 * @author Sascha L. Teichmann (sascha.teichmann@intevation.de)
 */
public class App {

    private static final Logger log = Logger.getLogger(App.class.getName());

    private static final String DEMO_URL =
        "http://geoserv.weichand.de:8080/geoserver/wfs?"
        + "service=WFS&acceptversions=2.0.0&request=GetCapabilities";
    //TODO: Remove DEMO_URL and read Infos from args or external file

    private App() {
    }

    private static boolean runHeadless(String[] args) {
        for (String arg: args) {
            if (arg.equals("-headless")) {
                return true;
            }
        }
        return false;
    }

    private static String getBase64EncAuth(String username, String password) {
        if (username == null || password == null) {
            return null;
        } else {
            String authString = username + ":" + password;
            System.out.println("auth string: " + authString);
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            return authStringEnc;
        }
    }
    private static WebService.Type serviceType(String url, String userName,
                                               String password) {
        return ServiceChecker.checkService(url, getBase64EncAuth(userName,
                password));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (runHeadless(args)) {
            log.info("Running in headless mode");
            //There could be a Service Handler for each Service that deals
            //with command line arguments and/or a stored XML File
            String userName = null;
            String password = null;
            WebService.Type st = ServiceChecker.checkService(DEMO_URL,
                    getBase64EncAuth(userName,password));
            switch (st) {
                case Atom:
                    log.info("Atom Service Found");
                    break;
                case WFSOne:
                    log.info("WFSOne Service Found");
                    break;
                case WFSTwo:
                    log.info("WFSTwo Service Found");
                    WFSTwoServiceHandler wfstwo =
                            new WFSTwoServiceHandler(DEMO_URL, userName,
                                    password);
                    break;
                default:
                    log.log(Level.SEVERE, "Could not determine Service Type");
                    break;
            }
        } else {
            // Its kind of complicated to start a javafx application from
            // another class. Thank god for StackExchange:
            // http://stackoverflow.com/a/25909862
            new Thread() {
                @Override
                public void run() {
                    javafx.application.Application.launch(Start.class);
                }
            }.start();
            Start start = Start.waitForStart();
            //start.printInvoking();
        }
    }
}
