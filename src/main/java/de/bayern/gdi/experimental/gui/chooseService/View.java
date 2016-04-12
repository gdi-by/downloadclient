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

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Screen;
import javafx.stage.Stage;


/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class View {

    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final int SEVEN = 7;
    private static final int EIGHT = 8;
    private static final int NINE = 9;
    private static final int ZERO = 0;

    private static final int GRID_HGAP = 10;
    private static final int GRID_VGAP = 10;

    private static final int BUTTONBOX_SIZE = 10;

    private static final int PADDING = 25;

    private static final int FULLSIZE = 100;
    private static final int COLUMN_COUNT = THREE;

    private static final double EIGHTY_PERCENT_OF = 0.8;
    private static final int MIN_WINDOWHEIGHT = 480;
    private static final int MIN_WINDOWWIDTH = 640;

    private static final int FIRST_COLUMN = ONE;
    private static final int SECOND_COLUMN = TWO;
    private static final int THIRD_COLUMN = THREE;
    private static final int FOURTH_COLUMN = FOUR;
    private static final int FIFTH_COLUMN = FIVE;
    private static final int SIXTH_COLUMN = SIX;
    private static final int SEVENTH_COLUMN = SEVEN;
    private static final int EIGTH_COLUMN = EIGHT;
    private static final int NINETH_COLUMN = NINE;
    private static final int MAX_COLUMN = NINETH_COLUMN;


    private static final int FIRST_ROW = ONE;
    private static final int SECOND_ROW = TWO;
    private static final int THIRD_ROW = THREE;
    private static final int FOURTH_ROW = FOUR;
    private static final int FIFTH_ROW = FIVE;
    private static final int SIXTH_ROW = SIX;
    private static final int SEVENTH_ROW = SEVEN;
    private static final int EIGHT_ROW = EIGHT;
    private static final int NINETH_ROW = NINE;
    private static final int MAX_ROW = NINETH_ROW;

    private int sceneHeight = MIN_WINDOWHEIGHT;
    private int sceneWidth = MIN_WINDOWWIDTH;

    private int columnWidth;

    private Scene scene;

    private GridPane grid;

    private HBox hbBtn;

    private ListView<String> serviceList;

    private TextField serviceSearch;

    private Label urlLabel;

    private TextField urlField;

    private Button chooseService;

    private CheckBox useAuthentication;

    private HBox authenticationLabels;

    private HBox authenticationFields;

    private Label userLabel;

    private TextField user;

    private Label pwLabel;

    private TextField pw;

    /**
     * Constructor.
     */
    public View() {

        //Calculate Screen Bounds
        Rectangle2D primaryScreenBounds =
                Screen.getPrimary().getVisualBounds();
        if (primaryScreenBounds.getWidth() * EIGHTY_PERCENT_OF > sceneWidth) {
            sceneWidth =
                    (int) (primaryScreenBounds.getWidth() * EIGHTY_PERCENT_OF);
        }
        if (primaryScreenBounds.getHeight() * EIGHTY_PERCENT_OF > sceneHeight) {
            sceneHeight =
                    (int) (primaryScreenBounds.getHeight() * EIGHTY_PERCENT_OF);
        }
        columnWidth = sceneWidth / MAX_COLUMN;
        // Layout
        this.grid = new GridPane();
        for (int i = 0; i < MAX_COLUMN; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth((MAX_COLUMN / FULLSIZE) / FULLSIZE);
            grid.getColumnConstraints().add(column);
        }

        for (int i = 0; i < MAX_ROW; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight((MAX_ROW / FULLSIZE) / FULLSIZE);
            grid.getRowConstraints().add(row);
        }

        this.grid.setAlignment(Pos.BASELINE_LEFT);
        this.grid.setHgap(GRID_HGAP);
        this.grid.setVgap(GRID_VGAP);
        this.grid.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));
        /*
        this.grid.setStyle("-fx-background-color: white;
                            -fx-grid-lines-visible: true");
        */
        //Select List for Services
        this.serviceList = new ListView<String>();

        //Auto Completion Text Box for Services
        this.serviceSearch = new TextField();

        //URL Label
        this.urlLabel = new Label("URL:");

        //URL Field
        this.urlField = new TextField();
        this.urlLabel.setLabelFor(this.urlField);

        //Checkbox for authentication
        this.useAuthentication = new CheckBox("Use Authentication");

        //Basic Auth Stuff
        this.authenticationLabels = new HBox(0);
        this.authenticationLabels.setAlignment(Pos.BASELINE_LEFT);
        this.authenticationLabels.setSpacing(columnWidth * TWO);
        this.authenticationFields = new HBox(0);
        this.authenticationFields.setAlignment(Pos.BASELINE_LEFT);
        this.authenticationFields.setSpacing(columnWidth / TWO);
        this.userLabel = new Label("User:");
        this.user = new TextField();
        this.pwLabel = new Label("Password:");
        this.pw = new TextField();
        this.authenticationLabels.getChildren()
                .addAll(
                    this.userLabel, this.pwLabel
        );
        this.authenticationFields.getChildren().addAll(this.user, this.pw);

        //Choose Serive Button
        this.chooseService = new Button();
        this.chooseService.setText("Choose Service");
        this.hbBtn = new HBox(BUTTONBOX_SIZE);
        this.hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        this.hbBtn.getChildren().add(this.chooseService);


        //Adding to the Layout
        this.grid.add(this.serviceSearch, FIRST_COLUMN, FIRST_ROW);
        this.grid.add(this.serviceList, FIRST_COLUMN, SECOND_ROW);
        this.grid.add(this.urlLabel, FIRST_ROW, THIRD_ROW);
        this.grid.add(this.urlField, FIRST_COLUMN, FOURTH_ROW);
        this.grid.add(this.hbBtn, FIRST_COLUMN, FIFTH_ROW);
        this.grid.add(this.useAuthentication, FIRST_COLUMN, SIXTH_ROW);
        this.grid.add(this.authenticationLabels, FIRST_COLUMN, SEVENTH_ROW);
        this.grid.add(this.authenticationFields, FIRST_COLUMN, EIGHT_ROW);


        this.scene = new Scene(this.grid, sceneHeight, sceneWidth);

    }

    /**
     * @param stage the stage to show
     * shows the current stage.
     */
    public void show(Stage stage) {
        stage.setTitle("GDI-BY Downloadclient");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * sets the content of the select list for the services.
     * @param items the items
     */
    public void setServiceList(ObservableList<String> items) {
        this.serviceList.setItems(items);
    }

    /**
     * returns the select list for services.
     * @return the select list for the services
     */
    public ListView<String> getServiceList() {
        return this.serviceList;
    }

    /**
     * returns the service Search Field.
     * @return the service Search field
     */
    public TextField getServiceSearch() {
        return serviceSearch;
    }

    /**
     * sets the service search field.
     * @param serviceSearch the service search field
     */
    public void setServiceSearch(TextField serviceSearch) {
        this.serviceSearch = serviceSearch;
    }

    /**
     * gets the url field.
     * @return the url field
     */
    public TextField getUrlField() {
        return urlField;
    }

    /**
     * returns the url field.
     * @param urlField the url field
     */
    public void setUrlField(TextField urlField) {
        this.urlField = urlField;
    }

    /**
     * gets the Choose Service button.
     * @return the Choose Service Button
     */
    public Button getChooseService() {
        return chooseService;
    }

    /**
     * sets the Choose Service button.
     * @param chooseService the Choose Service Button
     */
    public void setChooseService(Button chooseService) {
        this.chooseService = chooseService;
    }

    /**
     * gets the url Label.
     * @return the url Label
     */
    public Label getUrlLabel() {
        return urlLabel;
    }

    /**
     * gets the Password field.
     * @return the password field
     */
    public TextField getPw() {
        return pw;
    }

    /**
     * sets the Password field.
     * @param pw the password field
     */
    public void setPw(TextField pw) {
        this.pw = pw;
    }

    /**
     * gets the user Field.
     * @return the user Field
     */
    public TextField getUser() {
        return user;
    }

    /**
     * sets the user Field.
     * @param user the user Field
     */
    public void setUser(TextField user) {
        this.user = user;
    }

    /**
     * gets the use authentication checkbox.
     * @return the use authentication checkbox
     */
    public CheckBox getUseAuthentication() {
        return useAuthentication;
    }

    /**
     * sets the use authentication checkbox.
     * @param useAuthentication the use authentication checkbox
     */
    public void setUseAuthentication(CheckBox useAuthentication) {
        this.useAuthentication = useAuthentication;
    }

}
