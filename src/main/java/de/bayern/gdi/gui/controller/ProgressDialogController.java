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

import javafx.fxml.FXML;
import javafx.scene.text.Text;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ProgressDialogController {

    @FXML
    private Text downloadUrl;

    @FXML
    private Text progressBytes;

    private Controller controller;

    /**
     * Initialize ProgressDialogController with the Controller.
     *
     * @param mainController never <code>null</code>
     */
    public void init(Controller mainController) {
        this.controller = mainController;
        downloadUrl.setText(mainController.dataBean.getSelectedService().getServiceURL().toString());

    }


}
