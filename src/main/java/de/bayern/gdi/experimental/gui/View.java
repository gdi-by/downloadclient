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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;


/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class View {

    private Scene scene;

    private GridPane grid;
    private Text scenetitle;

    private Button okBtn;

    private HBox hbBtn;

    private static final int PADDING = 25;

    private static final int FONT_SIZE = 20;

    private static final int SCENE_HEIGHT = 300;
    private static final int SCENE_WIDTH = 300;

    private static final int BUTTONGROUP_X_GRID = 1;
    private static final int BUTTONGROUP_Y_GRID = 4;

    private static final int HEADER_COLUMN_INDEX = 0;
    private static final int HEADER_ROW_INDEX = 0;
    private static final int HEADER_COLSPAN = 2;
    private static final int HEADER_ROWSPAN = 1;

    private static final int GRID_HGAP = 10;
    private static final int GRID_VGAP = 10;

    private static final int BUTTONBOX_SIZE = 10;


    /**
     * @brief Constructor
     */
    public View() {
        // Layout
        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(GRID_HGAP);
        grid.setVgap(GRID_VGAP);
        grid.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));

        // HEADLINE
        scenetitle = new Text("Hallo");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, FONT_SIZE));
        grid.add(scenetitle, HEADER_COLUMN_INDEX, HEADER_ROW_INDEX,
                HEADER_COLSPAN, HEADER_ROWSPAN);

        okBtn = new Button();
        okBtn.setText("Ok");

        // BUTTONGRP
        hbBtn = new HBox(BUTTONBOX_SIZE);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(okBtn);
        grid.add(hbBtn, BUTTONGROUP_X_GRID, BUTTONGROUP_Y_GRID);

        scene = new Scene(grid, SCENE_HEIGHT, SCENE_WIDTH);
    }

    /**
     * @param stage the stage to show
     * @brief shows the current stage
     */
    public void show(Stage stage) {
        stage.setTitle("Test");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @return the OK Button
     * @brief gets the OK Buttons
     */
    public Button getOkBtn() {
        return okBtn;
    }

}
