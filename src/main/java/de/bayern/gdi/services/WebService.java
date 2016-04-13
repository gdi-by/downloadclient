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

package de.bayern.gdi.services;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public abstract class WebService implements ServiceSchema {

    public enum Type {
        WFSOne,
        WFSTwo,
        Atom
    }
    private String serviceURL;

    private String serviceType;

    public WebService() {
        this.serviceURL = null;
    }
    /**
     * Constructor.
     * @param serviceURL URL to the Service
     */
    public WebService(String serviceURL) {
        this.serviceURL = serviceURL;
    }

}
