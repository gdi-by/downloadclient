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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;


/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class View {

        private Scene scene;

        private GridPane grid;
        private Text scenetitle;

        private Label vornameLB;
        private TextField vornameTF;

        private Label nachnameLB;
        private TextField nachnameTF;

        private Text meldungT;

        private Button okBtn;

        private HBox hbBtn;

        public View() {
            // Layout
            grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(25, 25, 25, 25));

            // Ueberschrift
            scenetitle = new Text("Hallo");
            scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
            grid.add(scenetitle, 0, 0, 2, 1);

            okBtn = new Button("OK");

            // Buttongruppe
            hbBtn = new HBox(10);
            hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
            hbBtn.getChildren().add(okBtn);
            grid.add(hbBtn, 1, 4);

            // Meldung
            meldungT = new Text();
            grid.add(meldungT, 1, 6);

            scene = new Scene(grid, 300, 300);
        }

        public void show(Stage stage) {
            stage.setTitle("Test");
            stage.setScene(scene);
            stage.show();
        }

        public Button getOkBtn() {
            return okBtn;
        }

}
