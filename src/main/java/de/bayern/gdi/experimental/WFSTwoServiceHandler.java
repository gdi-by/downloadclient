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

import de.bayern.gdi.services.WFSTwo;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public class WFSTwoServiceHandler {

    private String serviceURL;
    private WFSTwo ws;
    private String userName;
    private String password;
    private String typeName;

    private static final com.vividsolutions.jts.geom.Envelope BBOX
            = new com.vividsolutions.jts.geom.Envelope(
            5234456.559480272, 5609330.972506456,
            4268854.282683062, 4644619.626498722);

    /**
     * Handles the WFSTwoService, when not called from Frontend.
     * @param serviceURL URL of the Service
     */
    public WFSTwoServiceHandler(String serviceURL, String userName, String
            password) {
        this.serviceURL = serviceURL;
        this.userName = userName;
        this.password = password;
        this.ws = new WFSTwo(this.serviceURL, this.userName, this.password);
        typeName = this.ws.getTypes().get(0);
    }
}
