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

import de.bayern.gdi.experimental.SimpleLoader;
import de.bayern.gdi.experimental.gui.Start;
/**
 * @author Sascha L. Teichmann (sascha.teichmann@intevation.de)
 */
public class App {

    private static final String DEMO_URL =
        "http://geoserv.weichand.de:8080/geoserver/wfs?"
        + "service=WFS&acceptversions=2.0.0&request=GetCapabilities";

    private App() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean headless = false;
        //check the arguments
        for (String arg: args) {
            System.out.println(arg);
            if(arg.equals("-headless")) {
                headless = true;
            }
        }
        if(headless) {
            SimpleLoader sl = new SimpleLoader(DEMO_URL);
            try {
                sl.download();
            } catch (Exception e) {
                // TODO: Add logging.
                System.err.println(e);
            }
        } else {
            System.out.println("Loading Gui");
            Start startGui = new Start();
            startGui.start(null);
        }
    }
}
