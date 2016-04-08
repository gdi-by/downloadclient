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

package de.bayern.gdi.experimental.gui.serviceATOM;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class Controller {

    // DataBean
    private DataBean dataBean;

    // View
    private View view;


    /**
     * @brief Creates the Conroller
     * @param dataBean the model
     */
    public Controller(DataBean dataBean) {
        this.dataBean = dataBean;
        this.view = new View();

        // Eventhandler registrieren
        //view.getOkBtn().setOnAction(new OkBtnEventHandler());
    }

    /**
     * @brief shows the view
     */
    public void show() {
        view.show(dataBean.getPrimaryStage());
    }

    //+++++++++++++++++++++++++++++++++++++++++++++
    // Events
    //+++++++++++++++++++++++++++++++++++++++++++++


    /**
     * @brief Event Handler for the OK Button
     */
    class OkBtnEventHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent e) {
            //Nade
        }

    }
}
