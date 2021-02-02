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
package de.bayern.gdi.gui.controller;

import de.bayern.gdi.utils.I18n;
import javafx.fxml.FXMLLoader;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class FXMLLoaderProducer {

    /** controller factory instance. */
    @Inject
    Instance<Object> instance;

    /**
     * Creates FXML loader instance.
     * @return the FXML loader
     */
    @Produces
    public FXMLLoader createLoader() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setResources(I18n.getBundle());
        fxmlLoader.setLocation(getClass().getResource("/"));
        fxmlLoader.setControllerFactory(param -> instance.select(param).get());
        return fxmlLoader;
    }

}
