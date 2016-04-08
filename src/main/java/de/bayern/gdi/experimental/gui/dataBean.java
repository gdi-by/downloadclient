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

package de.bayern.gdi.experimental.gui;

import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class dataBean extends Observable {

    private Stage primaryStage = null;
    private Map<String , String> namePwMap = null;

    public dataBean(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.namePwMap = new HashMap<>();
    }

    public Map<String, String> getNamePwMap() {
        return namePwMap;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

}
