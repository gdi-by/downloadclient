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

import de.bayern.gdi.services.Service;
import de.bayern.gdi.utils.I18n;
import java.net.URL;

/**
 * UI model for services.
 */
public class ServiceModel implements ItemModel {

    private Service service;

    public ServiceModel(Service service) {
        this.service = service;
    }

    public String getDataset() {
        return getName();
    }

    public Service getItem() {
        return this.service;
    }
    /**
     * Get the service name.
     * @return the name
     */
    public String getName() {
        return this.service.getName();
    }


    /**
     * Get the service url.
     * @return the url
     */
    public URL getUrl() {
        return this.service.getServiceURL();
    }


    /**
     * Get the service version.
     * @return the version
     */
    public String getVersion() {
        return this.service.getServiceType().toString();
    }

    /**
     * Is authentication required?
     * @return the restricted
     */
    public boolean isRestricted() {
        return this.service.isRestricted();
    }


    @Override
    public String toString() {
        if (this.service.isRestricted()) {
            return  I18n.format("gui.restricted", this.service.getName());
        }
        return this.service.getName();
    }
}
