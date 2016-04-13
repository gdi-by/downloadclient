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

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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

    private HBox serviceChooseBox;

    private ListView<String> serviceList;

    private ObservableList<String> serviceListEntries;

    private TextField serviceSearch;

    private Label serviceURLLabel;

    private TextField serviceURLfield;

    private Button serviceChooseButton;

    private CheckBox serviceUseAuthenticationCBX;

    private HBox serviceAuthenticationLabelsBox;

    private HBox serviceAuthenticationFieldsBox;

    private Label serviceUserLabel;

    private TextField serviceUser;

    private Label servicePWLabel;

    private TextField servicePW;

    private VBox statusBar;

    private MenuBar menubar;

    private BorderPane prgrmLayout;

    private MenuItem resetMenuItem;

    private MenuItem quitMenuItem;

    private Menu optionsMenu;

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
        this.prgrmLayout = new BorderPane();

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
        this.serviceURLLabel = new Label("URL:");

        //URL Field
        this.serviceURLfield = new TextField();
        this.serviceURLLabel.setLabelFor(this.serviceURLfield);

        //Checkbox for authentication
        this.serviceUseAuthenticationCBX = new CheckBox("Use Authentication");

        //Basic Auth Stuff
        this.serviceAuthenticationLabelsBox = new HBox(0);
        this.serviceAuthenticationLabelsBox.setAlignment(Pos.BASELINE_LEFT);
        this.serviceAuthenticationLabelsBox.setSpacing(columnWidth * TWO);
        this.serviceAuthenticationFieldsBox = new HBox(0);
        this.serviceAuthenticationFieldsBox.setAlignment(Pos.BASELINE_LEFT);
        this.serviceAuthenticationFieldsBox.setSpacing(columnWidth / TWO);
        this.serviceUserLabel = new Label("User:");
        this.serviceUser = new TextField();
        this.serviceUserLabel.setLabelFor(this.serviceUser);
        this.servicePWLabel = new Label("Password:");
        this.servicePW = new TextField();
        this.servicePWLabel.setLabelFor(this.servicePW);
        this.serviceAuthenticationLabelsBox.getChildren()
                .addAll(
                    this.serviceUserLabel, this.servicePWLabel
        );
        this.serviceAuthenticationFieldsBox.
                getChildren().
                addAll(this.serviceUser, this.servicePW);

        //Choose Serive Button
        this.serviceChooseButton = new Button();
        this.serviceChooseButton.setText("Choose Service");
        this.serviceChooseBox = new HBox(BUTTONBOX_SIZE);
        this.serviceChooseBox.setAlignment(Pos.BOTTOM_RIGHT);
        this.serviceChooseBox.getChildren().add(this.serviceChooseButton);


        //Adding to the Layout
        this.grid.add(this.serviceSearch,
                FIRST_COLUMN,
                FIRST_ROW);
        this.grid.add(this.serviceList,
                FIRST_COLUMN,
                SECOND_ROW);
        this.grid.add(this.serviceURLLabel,
                FIRST_ROW,
                THIRD_ROW);
        this.grid.add(this.serviceURLfield,
                FIRST_COLUMN,
                FOURTH_ROW);
        this.grid.add(this.serviceChooseBox,
                FIRST_COLUMN,
                FIFTH_ROW);
        this.grid.add(this.serviceUseAuthenticationCBX,
                FIRST_COLUMN,
                SIXTH_ROW);
        this.grid.add(this.serviceAuthenticationLabelsBox,
                FIRST_COLUMN,
                SEVENTH_ROW);
        this.grid.add(this.serviceAuthenticationFieldsBox,
                FIRST_COLUMN,
                EIGHT_ROW);


        //Menubar
        this.menubar = new MenuBar();
        this.optionsMenu = new Menu("Options");
        this.resetMenuItem = new MenuItem("Reset");
        this.quitMenuItem = new MenuItem("Quit");
        this.optionsMenu.getItems().add(this.resetMenuItem);
        this.optionsMenu.getItems().add(this.quitMenuItem);
        this.menubar.getMenus().add(this.optionsMenu);

        //Statusbar
        this.statusBar = new VBox();
        statusBar.setStyle("-fx-background-color: gainsboro");
        final Text statusText = new Text("I'm important");
        statusBar.getChildren().add(statusText);

        //Setting everything
        prgrmLayout.setTop(this.menubar);
        prgrmLayout.setCenter(this.grid);
        prgrmLayout.setBottom(this.statusBar);
        this.scene = new Scene(prgrmLayout, sceneHeight, sceneWidth);

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
     * gets the service List entries.
     * @return service list entries
     */
    public ObservableList<String> getServiceListEntries() {
        return serviceListEntries;
    }

    /**
     * sets the service List entries.
     * @param serviceListEntries the service List entires
     */
    public void setServiceListEntries(
            ObservableList<String> serviceListEntries) {
        this.serviceListEntries = serviceListEntries;
        this.serviceList.setItems(this.serviceListEntries);
    }

    /**
     * sets the text in the statusbar.
     * @param statusBarText the text it should be set to
     */
    public void setStatusBarText(String statusBarText) {
        final Text statusText = new Text(statusBarText);
        this.statusBar.getChildren().remove(0);
        this.statusBar.getChildren().add(statusText);
    }

    /**
     * sets the content of the select list for the services.
     * @param items the items
     */
    public void setServiceList(ObservableList<String> items) {
        this.setServiceListEntries(items);
    }

    /**
     * sets the service List.
     * @param serviceList the service list
     */
    public void setServiceList(ListView<String> serviceList) {
        this.serviceList = serviceList;
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
    public TextField getServiceURLfield() {
        return serviceURLfield;
    }

    /**
     * returns the url field.
     * @param serviceURLfield the url field
     */
    public void setServiceURLfield(TextField serviceURLfield) {
        this.serviceURLfield = serviceURLfield;
    }

    /**
     * gets the Choose Service button.
     * @return the Choose Service Button
     */
    public Button getServiceChooseButton() {
        return serviceChooseButton;
    }

    /**
     * sets the Choose Service button.
     * @param serviceChooseButton the Choose Service Button
     */
    public void setServiceChooseButton(Button serviceChooseButton) {
        this.serviceChooseButton = serviceChooseButton;
    }

    /**
     * gets the url Label.
     * @return the url Label
     */
    public Label getServiceURLLabel() {
        return serviceURLLabel;
    }

    /**
     * gets the Password field.
     * @return the password field
     */
    public TextField getServicePW() {
        return servicePW;
    }

    /**
     * sets the Password field.
     * @param servicePW the password field
     */
    public void setServicePW(TextField servicePW) {
        this.servicePW = servicePW;
    }

    /**
     * gets the serviceUser Field.
     * @return the serviceUser Field
     */
    public TextField getServiceUser() {
        return serviceUser;
    }

    /**
     * sets the serviceUser Field.
     * @param serviceUser the serviceUser Field
     */
    public void setServiceUser(TextField serviceUser) {
        this.serviceUser = serviceUser;
    }

    /**
     * gets the use authentication checkbox.
     * @return the use authentication checkbox
     */
    public CheckBox getServiceUseAuthenticationCBX() {
        return serviceUseAuthenticationCBX;
    }

    /**
     * sets the use authentication checkbox.
     * @param serviceUseAuthenticationCBX the use authentication checkbox
     */
    public void setServiceUseAuthenticationCBX(
            CheckBox serviceUseAuthenticationCBX) {
        this.serviceUseAuthenticationCBX = serviceUseAuthenticationCBX;
    }

    /**
     * gets the used scene.
     * @return the scene
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * sets the scene.
     * @param scene the scene
     */
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * gets the box around the choose service button.
     * @return box around choose service button
     */
    public HBox getServiceChooseBox() {
        return serviceChooseBox;
    }

    /**
     * set the box around the choose service button.
     * @param serviceChooseBox box around choose service button
     */
    public void setServiceChooseBox(HBox serviceChooseBox) {
        this.serviceChooseBox = serviceChooseBox;
    }

    /**
     * gets the initial heigth of the scene.
     * @return initial height of the scene
     */
    public int getSceneHeight() {
        return sceneHeight;
    }

    /**
     * gets teh initial width of the scene.
     * @return initial width of the scene
     */
    public int getSceneWidth() {
        return sceneWidth;
    }

    /**
     * gets the width of a column on the scene.
     * @return the width of a column on the scene
     */
    public int getColumnWidth() {
        return columnWidth;
    }

    /**
     * gets the count of columns on the grid.
     * @return count of columnns on the grid
     */
    public int getMaxColumn() {
        return MAX_COLUMN;
    }

    /**
     * gets the number ob rows on the grid.
     * @return number of rows on the grid
     */
    public int getMaxRow() {
        return MAX_ROW;
    }

    /**
     * gets the main grid of.
     * @return the main grid
     */
    public GridPane getGrid() {
        return grid;
    }

    /**
     * sets the main Grid.
     * @param grid the main Grid
     */
    public void setGrid(GridPane grid) {
        this.grid = grid;
    }

    /**
     * gets the box for service authentication labels.
     * @return box for service authentication labels
     */
    public HBox getServiceAuthenticationLabelsBox() {
        return serviceAuthenticationLabelsBox;
    }

    /**
     * sets the box for service authentication labels.
     * @param serviceAuthenticationLabelsBox box for service authentication
     *                                       labels
     */
    public void setServiceAuthenticationLabelsBox(
            HBox serviceAuthenticationLabelsBox) {
        this.serviceAuthenticationLabelsBox = serviceAuthenticationLabelsBox;
    }

    /**
     * gets the box for service authentication fields.
     * @return the box for service authentication fileds
     */
    public HBox getServiceAuthenticationFieldsBox() {
        return serviceAuthenticationFieldsBox;
    }

    /**
     * sets the box for service authentication fields.
     * @param serviceAuthenticationFieldsBox the box for service authentication
     *                                       fields
     */
    public void setServiceAuthenticationFieldsBox(
            HBox serviceAuthenticationFieldsBox) {
        this.serviceAuthenticationFieldsBox = serviceAuthenticationFieldsBox;
    }

    /**
     * label of the service url field.
     * @param serviceURLLabel label of the service url field
     */
    public void setServiceURLLabel(Label serviceURLLabel) {
        this.serviceURLLabel = serviceURLLabel;
    }

    /**
     * gets the service user label.
     * @return service user label
     */
    public Label getServiceUserLabel() {
        return serviceUserLabel;
    }

    /**
     * sets the service user label.
     * @param serviceUserLabel service user label
     */
    public void setServiceUserLabel(Label serviceUserLabel) {
        this.serviceUserLabel = serviceUserLabel;
    }

    /**
     * gets the service password label.
     * @return service password label
     */
    public Label getServicePWLabel() {
        return servicePWLabel;
    }

    /**
     * sets the service password label.
     * @param servicePWLabel service password label
     */
    public void setServicePWLabel(Label servicePWLabel) {
        this.servicePWLabel = servicePWLabel;
    }

    /**
     * gets the statusbar.
     * @return the statusbar
     */
    public VBox getStatusBar() {
        return statusBar;
    }

    /**
     * sets the statusbar.
     * @param statusBar the statusbar
     */
    public void setStatusBar(VBox statusBar) {
        this.statusBar = statusBar;
    }

    /**
     * gets the menu bar.
     * @return the menu bar
     */
    public MenuBar getMenubar() {
        return menubar;
    }

    /**
     * sets the menu bar.
     * @param menubar the menu bar
     */
    public void setMenubar(MenuBar menubar) {
        this.menubar = menubar;
    }

    /**
     * sets gets the programs border pane layout.
     * @return border pane layout
     */
    public BorderPane getPrgrmLayout() {
        return prgrmLayout;
    }

    /**
     * sets the programms border pane layout.
     * @param prgrmLayout border pane layout
     */
    public void setPrgrmLayout(BorderPane prgrmLayout) {
        this.prgrmLayout = prgrmLayout;
    }

    /**
     * gets the reset programm menu item.
     * @return reset programm menu item
     */
    public MenuItem getResetMenuItem() {
        return resetMenuItem;
    }

    /**
     * sets the reset programm menu item.
     * @param resetMenuItem reset program menu item
     */
    public void setResetMenuItem(MenuItem resetMenuItem) {
        this.resetMenuItem = resetMenuItem;
    }

    /**
     * gets the quit program menu item.
     * @return quit program menu item
     */
    public MenuItem getQuitMenuItem() {
        return quitMenuItem;
    }

    /**
     * sets the quit program menu item.
     * @param quitMenuItem quit program menu item
     */
    public void setQuitMenuItem(MenuItem quitMenuItem) {
        this.quitMenuItem = quitMenuItem;
    }

    /**
     * gets the options menu point.
     * @return the options menu point
     */
    public Menu getOptionsMenu() {
        return optionsMenu;
    }

    /**
     * sets the options menu point.
     * @param optionsMenu the options menu point
     */
    public void setOptionsMenu(Menu optionsMenu) {
        this.optionsMenu = optionsMenu;
    }

}
