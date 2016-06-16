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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.geometry.Envelope2D;

import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.model.Option;
import de.bayern.gdi.model.Parameter;
import de.bayern.gdi.model.ProcessingStep;
import de.bayern.gdi.model.ProcessingStepConfiguration;
import de.bayern.gdi.processor.ConverterException;
import de.bayern.gdi.processor.DownloadStepConverter;
import de.bayern.gdi.processor.JobList;
import de.bayern.gdi.processor.Processor;
import de.bayern.gdi.processor.ProcessorEvent;
import de.bayern.gdi.processor.ProcessorListener;
import de.bayern.gdi.services.Atom;
import de.bayern.gdi.services.Field;
import de.bayern.gdi.services.ServiceType;
import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.services.WFSMetaExtractor;
import de.bayern.gdi.services.WebService;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.ServiceChecker;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class Controller {

    private static final int MAP_WIDTH = 350;
    private static final int MAP_HEIGHT = 250;
    private static final int BGCOLOR = 244;

    // DataBean
    private DataBean dataBean;

    private Stage primaryStage;

    private UIFactory factory;

    @FXML private Button buttonClose;
    @FXML private MenuBar mainMenu;
    @FXML private ListView serviceList;
    @FXML private TextField searchField;
    @FXML private TextField serviceURL;
    @FXML private CheckBox serviceAuthenticationCbx;
    @FXML private CheckBox chkChain;
    @FXML private TextField serviceUser;
    @FXML private TextField servicePW;
    @FXML private Label statusBarText;
    @FXML private ComboBox<ItemModel> serviceTypeChooser;
    @FXML private ComboBox atomVariationChooser;
    @FXML private ComboBox dataFormatChooser;
    @FXML private ComboBox referenceSystemChooser;
    @FXML private VBox simpleWFSContainer;
    @FXML private VBox basicWFSContainer;
    @FXML private VBox atomContainer;
    @FXML private VBox chainContainer;
    @FXML private Group mapNodeWFS;
    @FXML private Group mapNodeAtom;
    @FXML private TextField basicX1;
    @FXML private TextField basicY1;
    @FXML private TextField basicX2;
    @FXML private TextField basicY2;
    @FXML private TextField atomX1;
    @FXML private TextField atomY1;
    @FXML private TextField atomX2;
    @FXML private TextField atomY2;
    @FXML private Label labelURL;
    @FXML private Label labelUser;
    @FXML private Label labelPassword;
    @FXML private Label labelSelectType;
    @FXML private Label labelPostProcess;
    @FXML private WebView valueAtomDescr;
    @FXML private Label valueAtomFormat;
    @FXML private Label valueAtomRefsys;
    @FXML private Button serviceSelection;
    @FXML private Button buttonDownload;
    @FXML private Button buttonSaveConfig;
    @FXML private Button addChainItem;
    @FXML private ProgressIndicator progressSearch;
    private WMSMapSwing mapAtom;
    private WMSMapSwing mapWFS;

    /**
     * Handler to close the application.
     *
     * @param event The event.
     */
    @FXML protected void handleCloseApp(ActionEvent event) {
        Alert closeDialog = new Alert(Alert.AlertType.CONFIRMATION);
        closeDialog.setTitle(I18n.getMsg("gui.confirm-exit"));
        closeDialog.setContentText(I18n.getMsg("gui.want-to-quit"));
        ButtonType confirm = new ButtonType(I18n.getMsg("gui.exit"));
        ButtonType cancel = new ButtonType(I18n.getMsg("gui.cancel"),
            ButtonData.CANCEL_CLOSE);
        closeDialog.getButtonTypes().setAll(confirm, cancel);
        Optional<ButtonType> res = closeDialog.showAndWait();
        if (res.get() == confirm) {
            Stage stage = (Stage) buttonClose.getScene().getWindow();
            stage.fireEvent(new WindowEvent(
                stage,
                WindowEvent.WINDOW_CLOSE_REQUEST
            ));
        }
    }

    /**
     * Handle the service selection.
     *
     * @param event The mouse click event.
     */
    @FXML protected void handleServiceSelectButton(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            chooseService();
        }
    }

    /**
     * Handle search and filter the service list.
     *
     * @param event the event
     */
    @FXML protected void handleSearch(KeyEvent event) {
        String currentText = this.searchField.getText();
        this.serviceList.getItems().clear();
        this.dataBean.reset();
        if ("".equals(currentText) || currentText == null) {
            this.serviceList.setItems(this.dataBean.getServicesAsList());
        }
        String searchValue = currentText.toUpperCase();
        ObservableList<ServiceModel> subentries
                = FXCollections.observableArrayList();
        ObservableList<ServiceModel> all = this.dataBean.getServicesAsList();
        for (ServiceModel entry : all) {
            boolean match = true;
            if (!entry.getName().toUpperCase().contains(searchValue)) {
                match = false;
            }
            if (match) {
                subentries.add(entry);
            }
        }
        if (currentText.length() > 2) {
            Task task = new Task() {
                @Override
                protected Integer call() throws Exception {
                    Platform.runLater(() -> {
                        progressSearch.setVisible(true);
                    });
                    List<ServiceModel> catalog = dataBean.getCatalogService()
                            .getServicesByFilter(currentText);
            //System.out.println(catalog.size());
                    for (ServiceModel entry: catalog) {
                        dataBean.addCatalogServiceToList(entry);
                        Platform.runLater(() -> {
                            subentries.add(entry);
                        });
                    }
                    Platform.runLater(() -> {
                        progressSearch.setVisible(false);
                    });
                    return 0;
                }
            };
            Thread th = new Thread(task);
            statusBarText.setText(I18n.getMsg("status.calling-service"));
            th.setDaemon(true);
            th.start();
        }

        this.serviceList.setItems(subentries);
    }

    /**
     * Handle the service selection.
     *
     * @param event The mouse click event.
     */
    @FXML protected void handleServiceSelect(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)
            && event.getClickCount() > 1
        ) {
            chooseService();
        } else if (event.getButton().equals(MouseButton.PRIMARY)
            && event.getClickCount() == 1
        ) {
            if (this.serviceList.getSelectionModel().getSelectedItems().get(0)
                    != null
            ) {
                ServiceModel service =
                    (ServiceModel)this.serviceList.getSelectionModel()
                        .getSelectedItems().get(0);
                String url = service.getUrl();
                this.serviceURL.setText(url);
                if (service.isRestricted()) {
                    this.serviceAuthenticationCbx.setSelected(true);
                    this.serviceUser.setDisable(false);
                    this.servicePW.setDisable(false);
                } else {
                    this.serviceAuthenticationCbx.setSelected(false);
                    this.serviceUser.setDisable(true);
                    this.servicePW.setDisable(true);
                }
            }
        }
    }

    /**
     * Handle authentication required selection.
     * @param event the event
     */
    @FXML protected void handleAuthenticationRequired(ActionEvent event) {
        if (this.serviceAuthenticationCbx.isSelected()) {
            this.serviceUser.setDisable(false);
            this.servicePW.setDisable(false);
        } else {
            this.serviceUser.setDisable(true);
            this.servicePW.setDisable(true);
        }
    }

    /**
     * Handle the service type selection.
     *
     * @param event The event
     */
    @FXML protected void handleServiceTypeSelect(ActionEvent event) {
        ItemModel item =
            this.serviceTypeChooser.
                getSelectionModel().getSelectedItem();
        if (item != null) {
            dataBean.setDataType(item);
            dataBean.setAttributes(new HashMap<String, String>());
            chooseType(item);
        }
    }

    /**
     * Handle the dataformat selection.
     *
     * @param event The event
     */
    @FXML protected void handleDataformatSelect(ActionEvent event) {
        this.dataBean.addAttribute("format",
            this.dataFormatChooser.getValue() != null
                ? this.dataFormatChooser.getValue().toString()
                : "");
    }

    /**
     * Handle the dataformat selection.
     *
     * @param event The event
     */
    @FXML protected void handleAddChainItem(ActionEvent event) {
        factory.addChainAttribute(this.dataBean, chainContainer);
    }

    /**
     * Handle the reference system selection.
     *
     * @param event The event
     */
    @FXML protected void handleReferenceSystemSelect(ActionEvent event) {
        this.dataBean.addAttribute("srsName",
            referenceSystemChooser.getValue() != null
                ? referenceSystemChooser.getValue().toString()
                : "EPSG:38468");
    }

    /**
     * Handle the variation selection.
     *
     * @param event The event
     */
    @FXML protected void handleVariationSelect(ActionEvent event) {
        this.dataBean.addAttribute("VARIATION",
            this.atomVariationChooser.getValue() != null
                ? this.atomVariationChooser.getValue().toString()
                : "");
    }

    private ArrayList<ProcessingStep> extractProcessingSteps() {

        ArrayList<ProcessingStep> steps = new ArrayList<>();
        if (!this.chkChain.isSelected()) {
            return steps;
        }

        Set<Node> parameter =
            this.chainContainer.lookupAll("#process_parameter");

        for (Node n: parameter) {
            Set<Node> vars = n.lookupAll("#process_var");
            Node nameNode = n.lookup("#process_name");
            ComboBox namebox = (ComboBox)nameNode;
            String name =
                ((ProcessingStepConfiguration)namebox.getValue())
                    .getName();
            //System.out.println(name);

            ProcessingStep step = new ProcessingStep();
            steps.add(step);
            step.setName(name);
            ArrayList<Parameter> parameters = new ArrayList<>();
            step.setParameters(parameters);

            for (Node v: vars) {
                String varName = null;
                String varValue = null;
                if (v instanceof TextField) {
                    TextField input = (TextField)v;
                    varName = input.getUserData().toString();
                    varValue = input.getText();
                } else if (v instanceof ComboBox) {
                    ComboBox input = (ComboBox)v;
                    varName = input.getUserData().toString();
                    varValue = input.getValue() != null
                        ? ((Option)input.getValue()).getValue()
                        : null;
                }
                //System.out.println(varName + ": " + varValue);
                if (varName != null && varValue != null) {
                    Parameter p = new Parameter(varName, varValue);
                    parameters.add(p);
                }
            }
        }
        return steps;
    }

    private void extractStoredQuery() {
        ItemModel data = this.dataBean.getDatatype();
        if (data instanceof StoredQueryModel) {
            this.dataBean.setAttributes(new HashMap<String, String>());
            Set<Node> textfields =
                this.simpleWFSContainer.lookupAll("#parameter");
            for (Node n: textfields) {
                TextField f = (TextField)n;
                this.dataBean.addAttribute(
                    f.getUserData().toString(),
                    f.getText());
            }
        }
    }

    private void extractBoundingBox() {
        String bbox = "";
        Envelope2D envelope;
        if (this.dataBean.getServiceType().equals(ServiceType.Atom)) {
            envelope = this.mapAtom.getBounds();
        } else {
            envelope = this.mapWFS.getBounds();
        }
        if (envelope != null) {
            bbox += envelope.getX() + ",";
            bbox += envelope.getY() + ",";
            bbox += (envelope.getX() + envelope.getWidth()) + ",";
            bbox += (envelope.getY() + envelope.getHeight());
            this.dataBean.addAttribute("bbox", bbox);
        } else {
            // Raise an error?
        }
    }

    /**
     * Start the download.
     *
     * @param event The event
     */
    @FXML protected void handleDownload(ActionEvent event) {

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle(I18n.getMsg("gui.save-dir"));
        //fileChooser.getExtensionFilters().addAll();
        File selectedDir = dirChooser.showDialog(getPrimaryStage());
        if (selectedDir == null) {
            return;
        }

        extractStoredQuery();
        extractBoundingBox();
        this.dataBean.setProcessingSteps(extractProcessingSteps());

        Task task = new Task() {
            @Override
            protected Integer call() {
                String savePath = selectedDir.getPath();
                DownloadStep ds = dataBean.convertToDownloadStep(savePath);
                try {
                    DownloadStepConverter dsc = new DownloadStepConverter(
                        dataBean.getUserName(),
                        dataBean.getPassword());
                    JobList jl = dsc.convert(ds);
                    Processor p = Processor.getInstance();
                    p.addJob(jl);
                } catch (final ConverterException ce) {
                    Platform.runLater(() -> {
                        statusBarText.setText(ce.getMessage());
                    });
                }
                return 0;
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    /**
     * Handle config saving.
     * @param event The event.
     */
    @FXML
    protected void handleSaveConfig(ActionEvent event) {

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle(I18n.getMsg("gui.save-dir"));
        File downloadDir = dirChooser.showDialog(getPrimaryStage());
        if (downloadDir == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.getMsg("gui.save-conf"));
        File configFile = fileChooser.showSaveDialog(getPrimaryStage());
        if (configFile == null) {
            return;
        }

        extractStoredQuery();
        extractBoundingBox();
        this.dataBean.setProcessingSteps(extractProcessingSteps());

        String savePath = downloadDir.getPath();
        DownloadStep ds = dataBean.convertToDownloadStep(savePath);
        try {
            ds.write(configFile);
        } catch (IOException ex) {
            log.log(Level.WARNING, ex.getMessage() , ex);
        }
    }

    /**
     * Use selection to request the service data and fill th UI.
     */
    private void chooseService() {
        Task task = new Task() {
            @Override
            protected Integer call() throws Exception {
                serviceURL.getScene().setCursor(Cursor.WAIT);
                String url = null;
                String username = null;
                String password = null;
                url = serviceURL.getText();
                if (serviceAuthenticationCbx.isSelected()) {
                    username = serviceUser.getText();
                    dataBean.setUsername(username);
                    password = servicePW.getText();
                    dataBean.setPassword(password);
                }
                if (url != null && !"".equals(url)) {
                    //view.setStatusBarText("Check for Servicetype");
                    ServiceType st = ServiceChecker.checkService(
                        url,
                        dataBean.getUserName(),
                        dataBean.getPassword());
                    WebService ws = null;
                    //Check for null, since switch breaks on a null value
                    if (st == null) {
                        log.log(Level.WARNING, "Could not determine "
                                + "Service Type" , st);
                        Platform.runLater(() -> {
                            statusBarText.setText(
                                I18n.getMsg("status.no-service-type"));
                        });
                    } else {
                        switch (st) {
                            case Atom:
                                Platform.runLater(() -> {
                                    statusBarText.setText(
                                        I18n.getMsg("status.type.atom"));
                                });
                                Atom atom = new Atom(url);
                                dataBean.setAtomService(atom);
                                break;
                            case WFSOne:
                                Platform.runLater(() -> {
                                    statusBarText.setText(
                                        I18n.getMsg("status.type.wfsone"));
                                });
                                WFSMetaExtractor wfsOne =
                                    new WFSMetaExtractor(url,
                                        dataBean.getUserName(),
                                        dataBean.getPassword());
                                WFSMeta metaOne = wfsOne.parse();
                                dataBean.setWFSService(metaOne);
                                break;
                            case WFSTwo:
                                Platform.runLater(() -> {
                                    statusBarText.setText(
                                        I18n.getMsg("status.type.wfstwo"));
                                });
                                WFSMetaExtractor extractor =
                                    new WFSMetaExtractor(url,
                                        dataBean.getUserName(),
                                        dataBean.getPassword());
                                WFSMeta meta = extractor.parse();
                                dataBean.setWFSService(meta);
                                break;
                            default:
                                log.log(Level.WARNING,
                                    "Could not determine URL" , st);
                                Platform.runLater(() -> {
                                    statusBarText.setText(
                                            I18n.getMsg("status.no-url"));
                                });
                                break;
                        }
                    }
//                    dataBean.setWebService(ws);
                    Platform.runLater(() -> {
                        setServiceTypes();
                        serviceTypeChooser.
                                getSelectionModel().select(0);
                        /*ChooseTypeEventHandler chooseType
                                = new ChooseTypeEventHandler();
                        chooseType.handle(e);*/
                        statusBarText.setText(I18n.getMsg("status.ready"));
                    });
                } else {
                    Platform.runLater(() -> {
                        statusBarText.setText(I18n.getMsg("status.no-url"));
                    });
                }
                serviceURL.getScene().setCursor(Cursor.DEFAULT);
                return 0;
            }
        };
        Thread th = new Thread(task);
        statusBarText.setText(I18n.getMsg("status.calling-service"));
        th.setDaemon(true);
        th.start();
    }

    /**
     * Sets the Service Types.
     */
    public void setServiceTypes() {
        if (dataBean.isWebServiceSet()) {
            switch (dataBean.getServiceType()) {
                case WFSOne:
//                    dataBean.setServiceTypes(
//                            dataBean.getWFSService());
//                    break;
                case WFSTwo:
                    List<WFSMeta.Feature> features =
                        dataBean.getWFSService().features;
                    List<WFSMeta.StoredQuery> queries =
                        dataBean.getWFSService().storedQueries;
                    ObservableList<ItemModel> types =
                        FXCollections.observableArrayList();
                    for (WFSMeta.Feature f : features) {
                        types.add(new FeatureModel(f));
                    }
                    for (WFSMeta.StoredQuery s : queries) {
                        types.add(new StoredQueryModel(s));
                    }
                    serviceTypeChooser.getItems().retainAll();
                    serviceTypeChooser.setItems(types);
                    serviceTypeChooser.setValue(types.get(0));
                    chooseType(serviceTypeChooser.getValue());
                    break;
                case Atom:
                    List<Atom.Item> items =
                        dataBean.getAtomService().getItems();
                    ObservableList<ItemModel> opts =
                        FXCollections.observableArrayList();
                    for (Atom.Item i : items) {
                        opts.add(new AtomItemModel(i));
                    }
                    serviceTypeChooser.getItems().retainAll();
                    serviceTypeChooser.setItems(opts);
                    serviceTypeChooser.setValue(opts.get(0));
                    chooseType(serviceTypeChooser.getValue());
                default:
            }
        }
    }

    private void chooseType(ItemModel data) {
        ServiceType type = this.dataBean.getServiceType();
        if (type == ServiceType.Atom) {
            Atom.Item item = (Atom.Item)data.getItem();
            item.load();
            List<Field> fields = item.fields;
            ObservableList<String> list =
                FXCollections.observableArrayList();
            for (Field f : fields) {
                list.add(f.type);
            }
            this.atomVariationChooser.setItems(list);
            WebEngine engine = this.valueAtomDescr.getEngine();
            java.lang.reflect.Field f;
            try {
                f = engine.getClass().getDeclaredField("page");
                f.setAccessible(true);
                com.sun.webkit.WebPage page =
                    (com.sun.webkit.WebPage) f.get(engine);
                page.setBackgroundColor(
                    (new java.awt.Color(BGCOLOR, BGCOLOR, BGCOLOR)).getRGB());
            } catch (NoSuchFieldException
                | SecurityException
                | IllegalArgumentException
                | IllegalAccessException e) {
                // Displays the webview with white background...
            }
            engine.loadContent(item.description);
            this.valueAtomFormat.setText(item.format);
            this.valueAtomRefsys.setText(item.defaultCRS);
            this.simpleWFSContainer.setVisible(false);
            this.basicWFSContainer.setVisible(false);
            this.atomContainer.setVisible(true);
        } else if (type == ServiceType.WFSTwo) {
            if (data instanceof FeatureModel) {
                this.simpleWFSContainer.setVisible(false);
                this.basicWFSContainer.setVisible(true);
                this.atomContainer.setVisible(false);
                WFSMeta.Feature feature = (WFSMeta.Feature)data.getItem();
                ObservableList<String> list =
                    FXCollections.observableArrayList();
                list.add(feature.defaultCRS);
                list.addAll(feature.otherCRSs);
                this.referenceSystemChooser.setItems(list);
                this.referenceSystemChooser.setValue(feature.defaultCRS);
                List<String> outputFormats = feature.outputFormats;
                if (outputFormats.isEmpty()) {
                    outputFormats =
                        this.dataBean.getWFSService()
                            .findOperation("GetFeature").outputFormats;
                    if (outputFormats.isEmpty()) {
                        outputFormats =
                            this.dataBean.getWFSService().outputFormats;
                    }
                }
                ObservableList<String> formats =
                    FXCollections.observableArrayList(outputFormats);
                this.dataFormatChooser.setItems(formats);
            } else if (data instanceof StoredQueryModel) {
                factory.fillSimpleWFS(
                    dataBean,
                    this.simpleWFSContainer,
                    (WFSMeta.StoredQuery)data.getItem());
                this.simpleWFSContainer.setVisible(true);
                this.basicWFSContainer.setVisible(false);
                this.atomContainer.setVisible(false);
            }
        }
    }

    /**
     * Set the DataBean and fill the UI with initial data objects.
     *
     * @param dataBean  The DataBean object.
     */
    public void setDataBean(DataBean dataBean) {
        this.dataBean = dataBean;
        this.serviceList.setItems(this.dataBean.getServicesAsList());
        URL url = null;
        try {
            url = new URL(this.dataBean.getWmsUrl());
        } catch (MalformedURLException e) {
        }
        mapWFS = new WMSMapSwing(url, MAP_WIDTH, MAP_HEIGHT);
        mapWFS.setCoordinateDisplay(basicX1, basicY1, basicX2, basicY2);
        mapAtom = new WMSMapSwing(url, MAP_WIDTH, MAP_HEIGHT);
        mapAtom.setCoordinateDisplay(atomX1, atomY1, atomX2, atomY2);

        this.mapNodeWFS.getChildren().add(mapWFS);
        this.mapNodeAtom.getChildren().add(mapAtom);

        this.simpleWFSContainer.setVisible(false);
        this.basicWFSContainer.setVisible(false);
        this.atomContainer.setVisible(false);
        this.progressSearch.setVisible(false);
        this.serviceUser.setDisable(true);
        this.servicePW.setDisable(true);
    }

    private static final Logger log
            = Logger.getLogger(Controller.class.getName());

    /**
     * @return the primaryStage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * @param primaryStage the primaryStage to set
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Creates the Controller.
     */
    public Controller() {
        this.factory = new UIFactory();
/*
        //this.dataBean = dataBean;
        //this.view = new View();
        //this.view.setServiceListEntries(this.dataBean.getServicesAsList());
        //this.view.setCatalogueServiceNameLabelText(
        //        I18n.getMsg("gui.catalogue") + ": "
        //        + this.dataBean.getCatalogService().getProviderName());

        // Register Event Handler
        /*view.getQuitMenuItem().
                setOnAction(new QuitMenuItemEventHandler());
        view.getResetMenuItem().
                setOnAction(new ResetMenuItemEventHandler());
        view.getServiceChooseButton().
                setOnAction(new ServiceChooseButtonEventHandler());
        view.getTypeComboBox().
                setOnAction(new ChooseTypeEventHandler());
        view.getAttributesFilledButton().
                setOnAction(new AttributesFilledEventHandler());
        //FIXME - Not only on Click, but everytime
        view.getServiceList().
                setOnMouseClicked(new MouseClickedOnServiceList());
        view.getDownloadButton().
                setOnAction(new DownloadButtonEventHandler());
        view.getSaveMenuItem().
                setOnAction(new SaveMenuItemEventHandler());
        //TODO - Implement Loading Function
        /*
        view.getLoadMenuItem().
                setOnAction(new LoadMenuItemEventHandler());

        // Register Listener
        view.getServiceSearch().textProperty().
                addListener(new SearchServiceListChangeListener());
*/
        Processor.getInstance().addListener(new DownloadListener());

        // stage overrides
//        getPrimaryStage().
//                setOnCloseRequest(new ConfirmCloseEventHandler());

    }

    /**
     * shows the view.
     *
    public void show() {
    //    view.show(getPrimaryStage());
    }

    /**
     * sets the Service Types.
     *
    public void setServiceTypes() {
        if (dataBean.isWebServiceSet()) {
            switch (dataBean.getWebService().getServiceType()) {
                case WFSOne:
                    dataBean.setServiceTypes(
                            dataBean.getWebService().getTypes());
                    break;
                case WFSTwo:
                    WFSTwo wfstwo = (WFSTwo) dataBean.getWebService();
                    ArrayList<String> wfstwoServices = new ArrayList<>();
                    ArrayList<String> storedQuieres = dataBean
                        .getWebService().getStoredQueries();
                    for (String str: storedQuieres) {
                        str = wfstwo.getSimplePrefix() + " " + str;
                        wfstwoServices.add(str);
                    }
                    ArrayList<String> types = dataBean
                            .getWebService().getTypes();
                    for (String str: types) {
                        str = wfstwo.getBasicPrefix() + " " + str;
                        wfstwoServices.add(str);
                    }
                    dataBean.setServiceTypes(wfstwoServices);
                    break;
                case Atom:
                default:
            }
//            view.setTypes(dataBean.getServiceTypes());
        }
    }

    /**
     * sets the Service Types Attributes.
     * @param map the Map of Attributes
     *
    public void setServiceAttributes(Map<String, String> map) {
        if (dataBean.isWebServiceSet()) {
  //          view.setAttributes(map);
            setWMSMap(this.dataBean.getWmsUrl(), this.dataBean.getWmsName());
        }
    }

    private void setWMSMap(String wmsUrl, String wmsName) {
  //      view.setWMSMap(wmsUrl, wmsName);
    }

    //+++++++++++++++++++++++++++++++++++++++++++++
    // Listener
    //+++++++++++++++++++++++++++++++++++++++++++++

    /**
     * listener for changes in search field, so the list can be searched.
     *
    @FXML
    protected void searchServiceList(String oldVal, String newVal) {
        if (oldVal != null && (newVal.length() < oldVal.length())) {
//            view.getServiceList().setItems(view.getServiceListEntries());
        }
        String value = newVal.toUpperCase();
        ObservableList<String> subentries
                = FXCollections.observableArrayList();
        Map<String, String> catalog = dataBean.getCatalogService()
                .getServicesByFilter(newVal);
        for (Map.Entry<String, String> entry: catalog.entrySet()) {
//            view.addServiceToList(entry.getKey());
            dataBean.addServiceToList(entry.getKey(), entry.getValue());
        }
//        for (Object entry : view.getServiceList().getItems()) {
            boolean match = true;
            String entryText = (String) entry;
            if (!entryText.toUpperCase().contains(value)) {
                match = false;
                break;
            }
            if (match) {
                subentries.add(entryText);
            }
        }
//        view.getServiceList().setItems(subentries);
    }

    //+++++++++++++++++++++++++++++++++++++++++++++
    // Events
    //+++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Event Handler for choosing a type.
     *
    private class ChooseTypeEventHandler
        implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            Map<String, String> map = new HashMap<String, String>();
//            if (view.getTypeComboBox().getSelectionModel().getSelectedItem()
//                    != null) {
//                String choosenType =
//                        view.getTypeComboBox().getSelectionModel()
//                        .getSelectedItem()
//                        .toString();
                ArrayList <AttributeType> attributes = null;
                switch (dataBean.getWebService().getServiceType()) {
                    case WFSOne:
                        map = dataBean.getWebService()
                                        .getAttributes(choosenType);
                        break;
                    case WFSTwo:
                        if (choosenType.startsWith(WFSTwo.getSimplePrefix())) {
                            choosenType = choosenType.substring(WFSTwo
                                    .getSimplePrefix().length() + 1);
                            map.putAll(dataBean.getWebService()
                                    .getParameters(choosenType));
                        } else {
                            choosenType = choosenType.substring(WFSTwo
                                    .getBasicPrefix().length() + 1);
                            map.putAll(dataBean.getWebService()
                                    .getAttributes(choosenType));
                        }
                        break;
                    case Atom:
                    default:
                }
                dataBean.setAttributes(map);
                setServiceAttributes(dataBean.getAttributes());
            }
        }
    }

    /**
     * Class for handling stuff if Attributes are filled.
     *
    private class AttributesFilledEventHandler
            implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            Map map = dataBean.getAttributes();
            for (Node n:view.getAttributeGridPane().getChildren()) {
                System.out.println(n.toString());
            }
        }
    }

    /**
     * Event handler for clicking "Save".
     *
    private class LoadMenuItemEventHandler
            implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            /* TODO: Implent this function in a way so the configuration File
             * fills the Frontend with the Informtaion. So all fields in the
             * frontend should be filled with the Information from the config
             * file, so you can check and verify your settings before clicking
             * "download"
             *
            FileChooser configFileChooser = new FileChooser();
            configFileChooser.setTitle(I18n.getMsg("gui.load-conf"));

            File configFile = configFileChooser.showOpenDialog(
                    getPrimaryStage());
            if (configFile == null) {
                return;
            }
            try {
                DownloadStep ds = DownloadStep.read(configFile);
                FileChooser downloadFileChooser = new FileChooser();
                downloadFileChooser.setTitle(I18n.getMsg("gui.save-conf"));
                downloadFileChooser.setInitialDirectory(new File(ds.getPath()));
                File downloadFile = downloadFileChooser.showSaveDialog(
                        getPrimaryStage());
                if (downloadFile == null) {
                    return;
                }
                ds.setPath(downloadFile.toString());
                JobList jl = DownloadStepConverter.convert(ds);
                Processor p = Processor.getInstance();
                p.addJob(jl);
            } catch (IOException | ConverterException ex) {
                log.log(Level.WARNING, ex.getMessage() , ex);
            }
        }
    }

    /**
     * Event handler for clicking "Save".
     *
    private class SaveMenuItemEventHandler
            implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            FileChooser downloadFileChooser = new FileChooser();
            downloadFileChooser.setTitle(I18n.getMsg("gui.save-file"));
            File downloadFile = downloadFileChooser.showSaveDialog(
                    getPrimaryStage());
            if (downloadFile == null) {
                return;
            }
            FileChooser configFileChooser = new FileChooser();
            configFileChooser.setTitle(I18n.getMsg("gui.save-conf"));
            File configFile = configFileChooser.showSaveDialog(
                    getPrimaryStage());
            if (configFile == null) {
                return;
            }
            String savePath = downloadFile.getPath();
            DownloadStepFactory dsf = new DownloadstepFactory();
            DownloadStep ds = dsf.getStep(view, dataBean, savePath);
            try {
                ds.write(configFile);

            } catch (IOException ex) {
                log.log(Level.WARNING, ex.getMessage() , ex);
            }
        }
    }

    /**
     * Event Handler for the Quit Programm Menu Entry.
     *
    private class QuitMenuItemEventHandler
            implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            getPrimaryStage().fireEvent(
                            new WindowEvent(
                                    getPrimaryStage(),
                                    WindowEvent.WINDOW_CLOSE_REQUEST
                            )
            );
        }
    }

    /**
     * Event Handler for resetting the programm.
     *
    private class ResetMenuItemEventHandler
            implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            view.reset();
        }
    }

    /**
     * Event Handler for Downloading.
     *
    private class DownloadButtonEventHandler
            implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle(I18n.getMsg("gui.save-dir"));
            //fileChooser.getExtensionFilters().addAll();
            File selectedDir = dirChooser.showDialog(
                    getPrimaryStage());
            if (selectedDir == null) {
                return;
            }
            Task task = new Task() {
                @Override
                protected Integer call() throws Exception {
                    String savePath = selectedDir.getPath();
                    DownloadStepFactory dsf = DownloadStepFactory.getInstance();
                    DownloadStep ds = dsf.getStep(view, dataBean, savePath);
                    JobList jl = DownloadStepConverter.convert(ds);
                    Processor p = Processor.getInstance();
                    p.addJob(jl);
                    return 0;
                }
            };
            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
        }
    }

    /**
     * Event Handler for the choose Service Button.
     *
    private class ServiceChooseButtonEventHandler implements
            EventHandler<ActionEvent> {

        public void handle(MouseEvent e) {
            ActionEvent ev = new ActionEvent();
            handle(ev);
        }

        @Override
        public void handle(ActionEvent e) {
            Task task = new Task() {
                @Override
                protected Integer call() throws Exception {
                    view.getScene().setCursor(Cursor.WAIT);
                    String serviceURL = null;
                    String username = null;
                    String password = null;
                    if (view.getServiceUseAuthenticationCBX().
                            isSelected()) {
                        username =
                                view.getServiceUser().
                                        textProperty().getValue();
                        dataBean.setUsername(username);
                        password =
                                view.getServicePW().
                                        textProperty().getValue();
                        dataBean.setPassword(password);
                    }
                    if (view.getServiceList().
                            getSelectionModel().getSelectedItems().get(0)
                            != null) {
                        String serviceName =
                                view.getServiceList().
                                        getSelectionModel().
                                        getSelectedItems().get(0);
                        serviceURL = dataBean.getServiceURL(serviceName);
                    } else {
                        serviceURL =
                                view.getServiceURLfield().
                                        textProperty().getValue();
                    }
                    if (view.getServiceUseAuthenticationCBX().
                            isSelected()) {
                        username = view.getServiceUser().
                                        textProperty().getValue();
                        dataBean.setUsername(username);
                        password = view.getServicePW().
                                        textProperty().getValue();
                        dataBean.setPassword(password);
                    }
                    if (serviceURL != null) {
                        //view.setStatusBarText("Check for Servicetype");
                        ServiceType st =
                                ServiceChecker.checkService(serviceURL,
                                        dataBean.getBase64EncAuth());
                        WebService ws = null;
                        //Check for null, since switch breaks on a null value
                        if (st == null) {
                            log.log(Level.WARNING, "Could not determine "
                                    + "Service Type" , st);
                            Platform.runLater(() -> {
                                view.setStatusBarText(
                                    I18n.getMsg("status.no-service-type"));
                            });
                        } else {
                            switch (st) {
                                case Atom:
                                    Platform.runLater(() -> {
                                        view.setStatusBarText(
                                            I18n.getMsg("status.type.atom"));
                                    });
                                    ws = new Atom(serviceURL);
                                    break;
                                case WFSOne:
                                    Platform.runLater(() -> {
                                        view.setStatusBarText(
                                            I18n.getMsg("status.type.wfsone"));
                                    });
                                    ws = new WFSOne(serviceURL, dataBean
                                            .getUserName(), dataBean
                                            .getPassword());
                                    break;
                                case WFSTwo:
                                    Platform.runLater(() -> {
                                        view.setStatusBarText(
                                            I18n.getMsg("status.type.wfstwo"));
                                    });
                                    ws = new WFSTwo(serviceURL, dataBean
                                            .getUserName(), dataBean
                                            .getPassword());
                                    break;
                                default:
                                    log.log(Level.WARNING,
                                        "Could not determine URL" , st);
                                    Platform.runLater(() -> {
                                        view.setStatusBarText(
                                                I18n.getMsg("status.no-url"));
                                    });
                                    break;
                            }
                        }
                        dataBean.setWebService(ws);
                        Platform.runLater(() -> {
                            setServiceTypes();
                            view.getTypeComboBox().
                                    getSelectionModel().select(0);
                            ChooseTypeEventHandler chooseType
                                    = new ChooseTypeEventHandler();
                            chooseType.handle(e);
                            view.setStatusBarText(I18n.getMsg("status.ready"));
                        });
                    } else {
                        Platform.runLater(() -> {
                            view.setStatusBarText(I18n.getMsg("status.no-url"));
                        });
                    }
                    view.getScene().setCursor(Cursor.DEFAULT);
                    return 0;
                }
            };
            Thread th = new Thread(task);
            view.setStatusBarText(I18n.getMsg("status.calling-service"));
            th.setDaemon(true);
            th.start();
        }
    }

    /**
     * Event Handler for closing the Application.
     *
    private class ConfirmCloseEventHandler implements
            EventHandler<WindowEvent> {
        @Override
        public void handle(WindowEvent e) {
            Alert closeConfirmation = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    I18n.getMsg("gui.want-to-quit")
            );
            Button exitButton
                    = (Button) closeConfirmation.getDialogPane().lookupButton(
                        ButtonType.OK
                    );
            exitButton.setText(I18n.getMsg("gui.exit"));
            closeConfirmation.setHeaderText(I18n.getMsg("gui.confirm-exit"));
            closeConfirmation.initModality(Modality.APPLICATION_MODAL);
            closeConfirmation.initOwner(getPrimaryStage());

            Optional<ButtonType> closeResponse =
                    closeConfirmation.showAndWait();
            if (!ButtonType.OK.equals(closeResponse.get())) {
                e.consume();
            }
        }
    }

    /**
     *  Eventhandler for mouse events on map.
     *
    private class MouseClickedOnServiceList
            implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                if (e.getClickCount() > 1) {
                    ServiceChooseButtonEventHandler se =
                            new ServiceChooseButtonEventHandler();
                    se.handle(e);
                }
                if (e.getClickCount() == 1) {
                    if (view.getServiceList().
                            getSelectionModel().getSelectedItems().get(0)
                            != null) {
                        String serviceName =
                                view.getServiceList().
                                        getSelectionModel().
                                        getSelectedItems().get(0);
                        String serviceURL = dataBean.getServiceURL(serviceName);
                        view.setServiceURLText(serviceURL);
                    }
                }
            }
        }
    }
*/
    /** Keeps track of download progression and errors. */
    private class DownloadListener implements ProcessorListener, Runnable {

        private String message;

        private synchronized void setMessage(String message) {
            this.message = message;
        }

        private synchronized String getMessage() {
            return this.message;
        }

        @Override
        public void run() {
            statusBarText.setText(getMessage());
        }

        @Override
        public void receivedException(ProcessorEvent pe) {
            setMessage(
                I18n.format(
                "status.error",
                pe.getException().getMessage()));
            Platform.runLater(this);
        }

        @Override
        public void receivedMessage(ProcessorEvent pe) {
            setMessage(pe.getMessage());
            Platform.runLater(this);
        }
    }
}
