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

package de.bayern.gdi.experimental.gui.serviceWFStwo;

import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class View extends de.bayern.gdi.experimental.gui.View {

    private de.bayern.gdi.experimental.gui.View mainView;
    private Scene scene;

    /**
     * Constructor.
     */
    public View(de.bayern.gdi.experimental.gui.View mainView) {
        this.mainView = mainView;
        this.scene = this.mainView.getScene();
    }

    /**
     * @param stage the stage to show
     * shows the current stage.
     */
    public void show(Stage stage) {
        stage.setTitle("Test");
        stage.setScene(scene);
        stage.show();
    }


}
