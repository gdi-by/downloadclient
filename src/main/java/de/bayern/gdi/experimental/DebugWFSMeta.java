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

import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.services.WFSMetaExtractor;

/** Experimental help for extracting meta data from WFS. */
public class DebugWFSMeta {

    private DebugWFSMeta() {
    }

    /**
     * @param args the command line arguments.
     * @throws Exception If something went wrong.
     */
    public static void main(String[] args) throws Exception {

        for (String arg: args) {
            WFSMeta meta = WFSMetaExtractor.parse(arg);
            System.out.println(meta);
        }
    }
}
