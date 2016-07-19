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

package de.bayern.gdi.experimental;

import de.bayern.gdi.utils.ServiceChecker;
import java.net.URL;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class DebugServiceRestriction {
    public static void main(String[] args) {
        try {
            URL url = new URL("http://www.geodaten.bayern.de/ogc/ogc_dtk25.cgi?" +
                    "service=WMS&version=1.1.1&request=GetMap&width=1935&" +
                    "height=1828&srs=EPSG:31468&" +
                    "bbox=4367801.572000001,5606637.276000001," +
                    "4370220.99,5608922.61&" +
                    "layers=by_dtk25&styles=&format=image/tiff&");
            ServiceChecker.isRestricted(url);
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
