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

package de.bayern.gdi.experimental.gui.chooseService;

import de.bayern.gdi.experimental.ServiceSetting;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;


/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class DataBean extends Observable {

    private Stage primaryStage = null;
    private Map<String , String> namePwMap = null;
    private ServiceSetting serviceSetting = null;
    private Map<String, String> services;
    private Map<String, String> catalogues;

    /**
     * @brief Constructor
     */
    public DataBean(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.namePwMap = new HashMap<>();
        this.serviceSetting = new ServiceSetting();
        this.services = this.serviceSetting.getServices();
        printStringMap(this.services);
        this.catalogues = this.serviceSetting.getCatalogues();
        printStringMap(this.catalogues);
        /*
        //FIXME: THIS IS JUST FOR TESTING!
        Iterator it = this.services.entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            try {
                WFStwo wfs = new WFStwo((String) pair.getValue());
                wfs.getAttributes(wfs.getTypes().elementAt(0));
            } catch (Exception e) {
                System.err.println(e.getStackTrace());
            }
        }
        */
    }

    /**
     * @brief returns the Name Map
     * @return the Name Map
     */
    public Map<String, String> getNamePwMap() {
        return namePwMap;
    }

    /**
     * @brief returns the current stage
     * @return the stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void printStringMap(Map<String, String> map) {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }

}
