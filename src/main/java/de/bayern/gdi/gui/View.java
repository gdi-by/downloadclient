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

package de.bayern.gdi.gui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
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

    private static final double TWO_FIFTH = TWO / FIVE;
    private static final double THREE_FIFTH = THREE / FIVE;

    private static final int GRID_HGAP = 10;
    private static final int GRID_VGAP = 10;

    private static final int BUTTONBOX_SIZE = 10;

    private static final int PADDING = 25;

    private static final int FULLSIZE = 100;

    private static final double EIGHTY_PERCENT_OF = 0.8;
    private static final double THIRTHY_PERCENT = 33.33D;
    private static final double THIRTHE_PERCENT_OF = 0.33D;
    private static final int MIN_WINDOWHEIGHT = 480;
    private static final int MIN_WINDOWWIDTH = 640;
    private static final int MIN_FIELDWIDTH = 150;

    private static final int FIRST_COLUMN = ZERO;
    private static final int SECOND_COLUMN = ONE;
    private static final int THIRD_COLUMN = TWO;
    private static final int MAX_COLUMN = THIRD_COLUMN;


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

    private static final Logger log
            = Logger.getLogger(View.class.getName());

    //private static final String INITALBBOX
    //        = "-131.13151509433965,46.60532747661736,"+
    //        "-117.61620566037737,56.34191403281659";

    //private static final String INITALBBOX
    //        = "5234456.559480272,5609330.972506456,"
    //        + "4268854.282683062,4644619.626498722";

    private static final String INITALBBOX
            = "4253019,5760588,"
            + "4278454,5777977";

    //private static final String INITALBBOX
    //        = "939258.2035682457,6731350.458905762,"
    //        + "1095801.2374962866,6887893.4928338";

    //private static final String INITALBBOX
    //        = "4443138,5356232,"
    //        + "4505393,5298301";
    //private static final String INITALBBOX
    //        = "9.000,50.500,"
    //        + "14.000,47.300";

    private int sceneHeight = MIN_WINDOWHEIGHT;

    private int sceneWidth = MIN_WINDOWWIDTH;

    private int columnWidth;

    private Scene scene;

    private GridPane grid;

    private Label catalagueServiceNameLabel;

    private HBox serviceChooseBox;

    private ListView<String> serviceList;

    private ObservableList<String> serviceListEntries;

    private TextField serviceSearch;

    private Label serviceURLLabel;

    private TextField serviceURLfield;

    private Button serviceChooseButton;

    private Button downloadButton;

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


    private MenuItem saveMenuItem;

    private Menu optionsMenu;

    private ComboBox typeComboBox;

    private ScrollPane attributeScrollPane;

    private HBox attributesFilledBox;

    private Button attributesFilledButton;

    private GridPane attributeGridPane;

    private WMSMapFX wmsMapFX;

    private WMSMapSwing wmsMapSwing;

    private Group mapGroup;

    private VBox serviceListVBox;

    private ResourceBundle myResources;

    /**
     * Constructor.
     */
    public View() {
        myResources = ResourceBundle.getBundle("messages", new Locale("de"));


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
        this.columnWidth = sceneWidth / MAX_COLUMN;
        // Layout
        this.prgrmLayout = new BorderPane();

        this.grid = new GridPane();

        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(THIRTHY_PERCENT);
        grid.getColumnConstraints().add(column);

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
        this.serviceUser.setMinWidth(MIN_FIELDWIDTH);
        this.serviceUserLabel.setLabelFor(this.serviceUser);
        this.servicePWLabel = new Label("Password:");
        this.servicePW = new TextField();
        this.servicePW.setMinWidth(MIN_FIELDWIDTH);
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
        this.grid.addColumn(FIRST_COLUMN, this.serviceSearch, this
                .serviceList, this.serviceURLLabel, this.serviceURLfield,
                this.serviceChooseBox, this.serviceUseAuthenticationCBX, this
                        .serviceAuthenticationFieldsBox);

        //Menubar
        this.menubar = new MenuBar();
        this.optionsMenu = new Menu(myResources.getString("menu.options"));
        this.resetMenuItem = new MenuItem(myResources.getString("menu.reset"));
        this.saveMenuItem = new MenuItem(myResources.getString("menu.save"));
        this.quitMenuItem = new MenuItem(myResources.getString("menu.quit"));
        this.optionsMenu.getItems().add(this.resetMenuItem);
        this.optionsMenu.getItems().add(this.saveMenuItem);
        this.optionsMenu.getItems().add(this.quitMenuItem);
        this.menubar.getMenus().add(this.optionsMenu);

        //Statusbar
        this.statusBar = new VBox();
        statusBar.setStyle("-fx-background-color: gainsboro");
        final Text statusText = new Text("Ready");
        statusBar.getChildren().add(statusText);

        //Setting everything
        prgrmLayout.setTop(this.menubar);
        prgrmLayout.setCenter(this.grid);
        prgrmLayout.setBottom(this.statusBar);
        this.scene = new Scene(prgrmLayout, sceneHeight, sceneWidth);

        //Initializing later used sutff
        this.attributesFilledBox = new HBox(BUTTONBOX_SIZE);
        this.attributesFilledButton = new Button();
        this.typeComboBox = new ComboBox();
        this.attributeScrollPane = new ScrollPane();
        this.attributeGridPane = new GridPane();
        //this.wmsMapFX = new WMSMapFX();
        this.wmsMapSwing = new WMSMapSwing();
        this.mapGroup = new Group();
        this.downloadButton = new Button();
        this.catalagueServiceNameLabel = new Label();
    }

    /**
     * Resets the View to the initial Position.
     */
    public void reset() {
        this.grid.getChildren().removeAll(this.attributesFilledBox,
                this.attributesFilledButton,
                this.typeComboBox,
                this.attributeScrollPane,
                this.attributeGridPane,
                //this.wmsMapFX,
                this.wmsMapSwing,
                this.mapGroup,
                this.downloadButton);
    }

    /**
     * adds a service to the list.
     * @param serviceName the Name of the Service
     */
    public void addServiceToList(String serviceName) {
        ObservableList<String> items = this.serviceList.getItems();
        items.add(serviceName);
        this.serviceList.setItems(items);
    }

    /**
     * set the CatalogueServiceName Label Text.
     * @param text text of the label
     */
    public void setCatalagueServiceNameLabelText(String text) {
        this.catalagueServiceNameLabel.setText(text);
        this.grid.getChildren().remove(this.catalagueServiceNameLabel);
        this.grid.add(this.catalagueServiceNameLabel, FIRST_COLUMN,
                SEVENTH_ROW);
    }

    /**
     * @param stage the stage to show
     * shows the current stage.
     */
    public void show(Stage stage) {
        stage.setTitle("GDI-BY Downloadclient");
        stage.setScene(scene);
        Rectangle2D primaryScreenBounds =
                Screen.getPrimary().getVisualBounds();
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
        stage.show();
    }

    /**
     * sets the Types of a Service.
     * @param types types
     */
    public void setTypes(ArrayList<String> types) {
        ObservableList<String> options =
                FXCollections.observableArrayList(types);
        this.typeComboBox.getItems().removeAll(this.typeComboBox.getItems());
        this.typeComboBox.setItems(options);
        this.grid.getChildren().remove(this.typeComboBox);
        this.grid.add(this.typeComboBox,
                SECOND_COLUMN,
                FIRST_ROW);
    }

    /**
     * sets the Attributes.
     * @param attributes map of Attributes
     */
    public void setAttributes(Map<String, String> attributes) {
        //Grid in Grid - Gridception... (I'll show myself the way out)

        this.attributeGridPane.getChildren().clear();
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setPercentWidth(this.columnWidth * TWO_FIFTH);
        this.attributeGridPane.getColumnConstraints().add(labelColumn);

        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setPercentWidth(this.columnWidth * THREE_FIFTH);
        this.attributeGridPane.getColumnConstraints().add(fieldColumn);


        for (int i = 0; i < attributes.size(); i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight((MAX_ROW / FULLSIZE) / FULLSIZE);
            this.attributeGridPane.getRowConstraints().add(row);
        }

        Iterator it = attributes.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            TextField tf = new TextField(pair.getValue().toString());
            this.attributeGridPane.add(tf, ONE, i);
            Label l = new Label((String) pair.getKey());
            this.attributeGridPane.add(l, ZERO, i);
            i++;
        }

        this.attributeScrollPane.setContent(this.attributeGridPane);
        this.grid.getChildren().remove(this.attributeScrollPane);

        attributeScrollPane.setFitToWidth(true);
        this.attributesFilledButton.setText("All Attributes Filled");
        this.attributesFilledButton.setAlignment(Pos.BOTTOM_RIGHT);
        this.attributesFilledBox.getChildren().removeAll(
                this.attributesFilledBox.getChildren()
        );
        this.grid.getChildren().remove(this.attributesFilledBox);
        this.grid.getChildren().remove(this.typeComboBox);
        this.grid.addColumn(SECOND_COLUMN, this.typeComboBox);
        this.grid.add(this.attributeScrollPane,
                SECOND_COLUMN,
                FIRST_ROW);
        this.grid.add(this.attributesFilledBox,
                SECOND_COLUMN,
                SECOND_ROW);
    }

    /**.
     * sets the WMS Map on the Grid
     * @param wmsUrl the WMS Url
     * @param wmsName the WMS Name
     */
    public void setWMSMap(String wmsUrl, String wmsName) {
        URL url = null;
        try {
            url = new URL(wmsUrl);
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        this.wmsMapSwing = new WMSMapSwing(url,
                (int) this.serviceList.getWidth(),
                (int) this.serviceList.getHeight());
        this.mapGroup.getChildren().clear();
        this.mapGroup.getChildren().add(this.wmsMapSwing);
        /*
        this.wmsMapFX = new WMSMapFX(wmsUrl,
                wmsName,
                INITALBBOX,
                (int) serviceList.getWidth(),
                (int) serviceList.getWidth());
        this.mapGroup.getChildren().clear();
        this.mapGroup.getChildren().add(this.wmsMapFX);
        */
        this.grid.getChildren().remove(this.mapGroup);
        this.grid.addColumn(THIRD_COLUMN, new Label());
        this.grid.add(this.mapGroup,
                THIRD_COLUMN,
                FIRST_ROW);
        this.downloadButton.setText("Download");
        this.grid.add(this.downloadButton,
                THIRD_COLUMN,
                SECOND_ROW);
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
     * sets the Service URL Text.
     * @param urlText the URL Text
     */
    public void setServiceURLText(String urlText) {
        TextField tf = getServiceURLfield();
        tf.clear();
        tf.setText(urlText);
        setServiceURLfield(tf);
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
     * gets the Password field.
     * @return the password field
     */
    public TextField getServicePW() {
        return servicePW;
    }

    /**
     * gets the serviceUser Field.
     * @return the serviceUser Field
     */
    public TextField getServiceUser() {
        return serviceUser;
    }

    /**
     * gets the use authentication checkbox.
     * @return the use authentication checkbox
     */
    public CheckBox getServiceUseAuthenticationCBX() {
        return serviceUseAuthenticationCBX;
    }

    /**
     * gets the used scene.
     * @return the scene
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * gets the reset programm menu item.
     * @return reset programm menu item
     */
    public MenuItem getResetMenuItem() {
        return resetMenuItem;
    }

    /**
     * gets the quit program menu item.
     * @return quit program menu item
     */
    public MenuItem getQuitMenuItem() {
        return quitMenuItem;
    }

    /**
     * Gets the Combo Box for Types.
     * @return Combobox of Types
     */
    public ComboBox getTypeComboBox() {
        return typeComboBox;
    }

    /**
     * gets the Attributes Filled Button.
     * @return the Attributes Filled Button
     */
    public Button getAttributesFilledButton() {
        return attributesFilledButton;
    }

    /**
     * gets the group with the Attributes in.
     * @return the group with the attributes in
     */
    public GridPane getAttributeGridPane() {
        return attributeGridPane;
    }

    /**
     * gets the Downloadbutton.
     * @return the Downloadbutton
     */
    public Button getDownloadButton() {
        return downloadButton;
    }


    /**
     * gets the save menu item.
     * @return save menu item
     */
    public MenuItem getSaveMenuItem() {
        return saveMenuItem;
    }
}
