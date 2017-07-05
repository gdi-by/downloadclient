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

import com.vividsolutions.jts.geom.Geometry;
import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.model.MIMEType;
import de.bayern.gdi.model.MIMETypes;
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
import de.bayern.gdi.services.Service;
import de.bayern.gdi.services.ServiceType;
import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.services.WFSMetaExtractor;
import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.DownloadConfig;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Misc;
import de.bayern.gdi.utils.ServiceChecker;
import de.bayern.gdi.utils.ServiceSettings;

import java.io.File;
import java.io.IOException;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.xml.sax.SAXException;

import org.apache.commons.io.IOUtils;


/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class Controller {

    private static final int MAP_WIDTH = 350;
    private static final int MAP_HEIGHT = 250;
    private static final int BGCOLOR = 244;
    private static final String INITIAL_CRS_DISPLAY = "EPSG:4326";
    private static final String ATOM_CRS_STRING = "EPSG:4326";
    private static final int BBOX_X1_INDEX = 0;
    private static final int BBOX_Y1_INDEX = 1;
    private static final int BBOX_X2_INDEX = 2;
    private static final int BBOX_Y2_INDEX = 3;
    private static final int MAX_APP_LOG_BYTES = 8096;
    private static final Logger log
            = Logger.getLogger(Controller.class.getName());
    //Application log
    private static final Logger APP_LOG = Logger.getLogger("Application Log");
    private static boolean appLogInit = false;
    private static FileHandler appLogFileHandler;
    private static AppLogFormatter appLogFormatter;

    private static final String UNAVAILABLE_PREFIX = "N/A:";
    private CoordinateReferenceSystem atomCRS;
    // DataBean
    private DataBean dataBean;
    private Stage primaryStage;
    private UIFactory factory;
    private boolean catalogReachable;
    private WMSMapSwing mapAtom;
    private WMSMapSwing mapWFS;
    private DownloadConfig downloadConfig;

    @FXML
    private Button buttonClose;
    @FXML
    private MenuBar mainMenu;
    @FXML
    private ListView serviceList;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private TextField serviceURL;
    @FXML
    private CheckBox serviceAuthenticationCbx;
    @FXML
    private CheckBox chkChain;
    @FXML
    private TextField serviceUser;
    @FXML
    private TextField servicePW;
    @FXML
    private Label logHistory;
    @FXML
    private TitledPane logHistoryParent;
    @FXML
    private ScrollPane logHistoryPanel;
    @FXML
    private ComboBox<ItemModel> serviceTypeChooser;
    @FXML
    private ComboBox<ItemModel> atomVariationChooser;
    @FXML
    private ComboBox<OutputFormatModel> dataFormatChooser;
    @FXML
    private ComboBox<CRSModel> referenceSystemChooser;
    @FXML
    private SplitPane mapSplitPane;
    @FXML
    private VBox simpleWFSContainer;
    @FXML
    private VBox basicWFSContainer;
    @FXML
    private VBox atomContainer;
    @FXML
    private VBox chainContainer;
    @FXML
    private Group mapNodeWFS;
    @FXML
    private Group mapNodeAtom;
    @FXML
    private TextField basicX1;
    @FXML
    private TextField basicY1;
    @FXML
    private TextField basicX2;
    @FXML
    private TextField basicY2;
    @FXML
    private Label lablbasicx1;
    @FXML
    private Label lablbasicy1;
    @FXML
    private Label lablbasicx2;
    @FXML
    private Label lablbasicy2;
    @FXML
    private TextField atomX1;
    @FXML
    private TextField atomY1;
    @FXML
    private TextField atomX2;
    @FXML
    private TextField atomY2;
    @FXML
    private Label lablatomx1;
    @FXML
    private Label lablatomy1;
    @FXML
    private Label lablatomx2;
    @FXML
    private Label lablatomy2;
    @FXML
    private Label labelURL;
    @FXML
    private Label labelUser;
    @FXML
    private Label labelPassword;
    @FXML
    private Label labelSelectType;
    @FXML
    private Label labelPostProcess;
    @FXML
    private WebView valueAtomDescr;
    @FXML
    private Label valueAtomFormat;
    @FXML
    private Label valueAtomRefsys;
    @FXML
    private Button serviceSelection;
    @FXML
    private Button buttonDownload;
    @FXML
    private Button buttonSaveConfig;
    @FXML
    private Button addChainItem;
    @FXML
    private ProgressIndicator progressSearch;
    @FXML
    private HBox processStepContainter;
    @FXML
    private VBox basicWFSX1Y1;
    @FXML
    private VBox basicWFSX2Y2;
    @FXML
    private Label referenceSystemChooserLabel;

    @FXML
    private MenuItem menuHelp;
    @FXML
    private MenuItem menuAbout;
    @FXML
    private MenuBar menuBar;

    /**
     * Creates the Controller.
     */
    public Controller() {
        this.factory = new UIFactory();
        Processor.getInstance().addListener(new DownloadListener());
    }

    private static void initAppLog() {
        try {
            APP_LOG.setUseParentHandlers(false);
            //Open file in append mode
            File logPath = new File(System.getProperty("java.io.tmpdir")
                    + "/downloadclient");
            logPath.mkdirs();

            appLogFileHandler = new FileHandler(logPath.getAbsolutePath()
                    + "/downloadclient_app_log.txt",
                    MAX_APP_LOG_BYTES, 1, true);
            appLogFormatter = new AppLogFormatter();
            appLogFileHandler.setFormatter(appLogFormatter);
            APP_LOG.addHandler(appLogFileHandler);
            appLogInit = true;
        } catch (IOException ioex) {
            log.log(Level.SEVERE, "Could not open application log file",
                    ioex);
        }
    }

    /**
     * Initializes gui elements.
     */
    @FXML
    protected void initialize() {
        logHistoryParent.expandedProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal,
                    Object newVal) {
                boolean val = (boolean) newVal;
                if (val) {
                    logHistoryParent.getTooltip().setText(
                            I18n.format("tooltip.log_history_expanded"));
                } else {
                    logHistoryParent.getTooltip().setText(
                            I18n.format("tooltip.log_history_hidden"));
                }
            }
        });
    }

    /**
     * Logs a message to the application log.
     *
     * @param msg Message to log
     */
    public static void logToAppLog(String msg) {
        try {
            Platform.runLater(() -> {
                if (!appLogInit) {
                    initAppLog();
                }
                APP_LOG.info(msg);
            });
        } catch (Exception e) {
            if (!appLogInit) {
                initAppLog();
            }
            APP_LOG.info(msg);
        }
    }

    /**
     * Handle action related to "About" menu item.
     *
     * @param event Event on "About" menu item.
     */
    @FXML
    private void handleAboutAction(final ActionEvent event) {
        try {
            displayHTMLFileAsPopup(I18n.getMsg("menu.about"),
                    "about/about_" + I18n.getLocale().toLanguageTag()
                            + ".html");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void displayHTMLFileAsPopup(String popuptitle, String pathToFile)
            throws
            IOException {
        WebView web = new WebView();
        InputStream htmlPage = Misc.getResource(pathToFile);
        String content = IOUtils.toString(htmlPage, "UTF-8");
        web.getEngine().loadContent(content);
        WebViewWindow wvw = new WebViewWindow(web, popuptitle);
        wvw.popup();
    }
    /**
     * Handle action related to "Help" menu item.
     *
     * @param event Event on "Help" menu item.
     */

    @FXML
    private void handleHelpAction(final ActionEvent event) {
        String pathToFile = "help/help_" + I18n.getLocale().toLanguageTag()
                + ".txt";
        try {
            openLinkFromFile(pathToFile);
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void openLinkFromFile(String pathToFile) throws IOException {
        InputStream is = Misc.getResource(pathToFile);
        String contents = IOUtils.toString(is, "UTF-8");
        if (contents == null
        || contents.isEmpty()
        || contents.equals("null")) {
            throw new MalformedURLException("URL is Empty");
        }
        URL helpURL = new URL(contents);
        Misc.startExternalBrowser(helpURL.toString());
    }

    /**
     * Handler to close the application.
     *
     * @param event The event.
     */
    @FXML
    protected void handleCloseApp(ActionEvent event) {
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
    * Handle click at load config menu items.
    * Opens a file chooser dialog and loads a download config from a XML file.
    *
    * @param event The Event.
    */
    @FXML
    protected void handleLoadConfig(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File initialDir = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(initialDir);
        fileChooser.setTitle(I18n.getMsg("menu.load_config"));
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("XML Files", "*.xml"),
            new FileChooser.ExtensionFilter("All Files", "*.*"));
        File configFile = fileChooser.showOpenDialog(primaryStage);

        if (configFile != null) {
            resetGui();
            try {
                this.downloadConfig = new DownloadConfig(configFile);
                serviceURL.setText(this.downloadConfig.getServiceURL());
                doSelectService(downloadConfig);
            } catch (IOException
                        | ParserConfigurationException
                        | SAXException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
                setStatusTextUI(
                        I18n.format("status.config.invalid-xml"));
                return;
            } catch (DownloadConfig.NoServiceURLException urlEx) {
                setStatusTextUI(
                        I18n.format("status.config.no-url-provided"));
            }
        }
    }

    private void loadDownloadConfig(DownloadConfig conf) {
        String dataset = conf.getDataset();
        if (dataset != null) {
            boolean datasetAvailable = false;
            List<ItemModel> datasets = serviceTypeChooser.
                    getItems();
            for (ItemModel iItem : datasets) {
                if (iItem.getDataset().equals(dataset)) {
                    serviceTypeChooser.
                            getSelectionModel().select(iItem);
                    datasetAvailable = true;
                }
            }
            if (!datasetAvailable) {
                MiscItemModel errorItem = new MiscItemModel();
                errorItem.setDataset(dataset);
                errorItem.setItem(conf.getDataset());
                setStatusTextUI(
                        I18n.format("gui.dataset-not-available"));
                serviceTypeChooser.getItems().add(errorItem);
                serviceTypeChooser.
                        getSelectionModel().select(errorItem);
            }
        }
        serviceTypeChooser.setCellFactory(
                new Callback<ListView<ItemModel>,
                ListCell<ItemModel>>() {
            @Override
            public ListCell<ItemModel> call(ListView<ItemModel> list) {
                return new CellTypes.ItemCell();
            }
        });
        atomVariationChooser.setCellFactory(
                new Callback<ListView<ItemModel>,
                ListCell<ItemModel>>() {
            @Override
            public ListCell<ItemModel> call(ListView<ItemModel> list) {
                return new CellTypes.ItemCell();
            }
        });
        referenceSystemChooser.setCellFactory(
                new Callback<ListView<CRSModel>,
                ListCell<CRSModel>>() {
            @Override
            public ListCell<CRSModel> call(ListView<CRSModel> list) {
                return new CellTypes.CRSCell() {
                };
            }
        });
        dataFormatChooser.setCellFactory(
                new Callback<ListView<OutputFormatModel>,
                ListCell<OutputFormatModel>>() {
            @Override
            public ListCell<OutputFormatModel>
                    call(ListView<OutputFormatModel> list) {
                return new CellTypes.StringCell();
            }
        });
        loadGUIComponents();
    }

    private void loadGUIComponents() {
        switch (downloadConfig.getServiceType()) {
            case "ATOM":
                boolean variantAvailable = false;
                for (ItemModel i: atomVariationChooser.getItems()) {
                    Atom.Field field = (Atom.Field) i.getItem();
                    if (field.type.equals(downloadConfig.getAtomVariation())) {
                        variantAvailable = true;
                        atomVariationChooser.getSelectionModel().select(i);
                    }
                }
                if (!variantAvailable) {
                    MiscItemModel errorVariant = new MiscItemModel();
                    errorVariant.setItem(downloadConfig.getAtomVariation());
                    atomVariationChooser.getItems().add(errorVariant);
                    atomVariationChooser.getSelectionModel()
                            .select(errorVariant);
                }
                break;
            case "WFS2_BASIC":
                try {
                    CoordinateReferenceSystem targetCRS =
                            CRS.decode(downloadConfig.getSRSName());
                    boolean crsAvailable = false;
                    for (CRSModel crsModel: referenceSystemChooser.getItems()) {
                        if (CRS.equalsIgnoreMetadata(targetCRS,
                                    crsModel.getCRS())) {
                            crsAvailable = true;
                            referenceSystemChooser.getSelectionModel()
                                    .select(crsModel);
                        }
                    }
                    if (!crsAvailable) {
                        CRSModel crsErrorModel = new CRSModel(targetCRS);
                        crsErrorModel.setAvailable(false);
                        referenceSystemChooser.getItems().add(crsErrorModel);
                        referenceSystemChooser.getSelectionModel()
                                .select(crsErrorModel);
                    }
                } catch (NoSuchAuthorityCodeException nsace) {
                    setStatusTextUI(I18n.format("status.config.invalid-epsg"));
                } catch (Exception e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
                if (downloadConfig.getBoundingBox() != null) {
                    String[] bBox = downloadConfig.getBoundingBox().split(",");
                    basicX1.setText(bBox[BBOX_X1_INDEX]);
                    basicY1.setText(bBox[BBOX_Y1_INDEX]);
                    basicX2.setText(bBox[BBOX_X2_INDEX]);
                    basicY2.setText(bBox[BBOX_Y2_INDEX]);
                }
                boolean outputFormatAvailable = false;
                for (OutputFormatModel i: dataFormatChooser.getItems()) {
                    if (i.getItem().equals(downloadConfig.getOutputFormat())) {
                        dataFormatChooser.getSelectionModel().select(i);
                        outputFormatAvailable = true;
                    }
                }
                if (!outputFormatAvailable) {
                    OutputFormatModel output = new OutputFormatModel();
                    output.setAvailable(false);
                    output.setItem(downloadConfig.getOutputFormat());
                    dataFormatChooser.getItems().add(output);
                    dataFormatChooser.getSelectionModel().select(output);
                }
                break;
            case "WFS2_SIMPLE":
                ObservableList<Node> children
                         = simpleWFSContainer.getChildren();
                HashMap<String, String> parameters = downloadConfig.getParams();
                for (Node node: children) {
                    if (node instanceof HBox) {
                        HBox hb = (HBox) node;
                        Node n1 = hb.getChildren().get(0);
                        Node n2 = hb.getChildren().get(1);
                        if (n1 instanceof Label
                            && n2 instanceof TextField) {
                            Label paramLabel = (Label) n1;
                            TextField paramBox = (TextField) n2;
                            String targetValue = parameters.get(paramLabel
                                    .getText());
                            if (targetValue != null) {
                                paramBox.setText(targetValue);
                            }
                        }
                        if (n2 instanceof ComboBox) {
                            ComboBox<OutputFormatModel> cb
                                    = (ComboBox<OutputFormatModel>) n2;
                            cb.setCellFactory(
                                    new Callback<ListView<OutputFormatModel>,
                                    ListCell<OutputFormatModel>>() {
                                @Override
                                public ListCell<OutputFormatModel>
                                        call(ListView<OutputFormatModel> list) {
                                    return new CellTypes.StringCell();
                                }
                            });
                            cb.setOnAction((event) -> {
                                if (cb.getValue().isAvailable()) {
                                    cb.setStyle("-fx-border-color: null;");
                                } else {
                                    cb.setStyle("-fx-border-color: red;");
                                }
                            });
                            boolean formatAvailable = false;
                            for (OutputFormatModel i: cb.getItems()) {
                                if (i.getItem().equals(downloadConfig
                                        .getOutputFormat())) {
                                    cb.getSelectionModel().select(i);
                                    formatAvailable = true;
                                }
                            }
                            if (!formatAvailable) {
                                String format = downloadConfig
                                        .getOutputFormat();
                                OutputFormatModel m = new OutputFormatModel();
                                m.setItem(format);
                                m.setAvailable(false);
                                cb.getItems().add(m);
                                cb.getSelectionModel().select(m);
                            }
                            if (cb.getValue().isAvailable()) {
                                cb.setStyle("-fx-border-color: null;");
                            } else {
                                cb.setStyle("-fx-border-color: red;");
                            }
                        }
                    }
                }
                break;
            default:
                setStatusTextUI(I18n.format("status.config.invalid-xml"));
                break;
        }
        ArrayList<DownloadConfig.ProcessingStep> steps =
                downloadConfig.getProcessingSteps();
        factory.removeAllChainAttributes(this.dataBean, chainContainer);
        if (steps != null) {
            chkChain.setSelected(true);
            handleChainCheckbox(new ActionEvent());

            for (DownloadConfig.ProcessingStep iStep : steps) {
                factory.addChainAttribute(this.dataBean, chainContainer,
                        iStep.name, iStep.params);
            }
        } else {
            chkChain.setSelected(false);
            handleChainCheckbox(new ActionEvent());
        }
    }

    /**
     * Handle the service selection button event.
     *
     * @param event The mouse click event.
     */
    @FXML
    protected void handleServiceSelectButton(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            this.downloadConfig = null;
            doSelectService();
        }
    }

   /**
    * Select a service according to service url textfield.
    */
    protected void doSelectService() {
        doSelectService(null);
    }

   /**
    * Select a service according to service url textfield.
    *
    * @param downloadConf Loaded download config, null if a service is chosen
    *    from an URL or the service List
    */
    protected void doSelectService(DownloadConfig downloadConf) {
        dataBean.resetSelectedService();
        serviceSelection.setDisable(true);
        serviceURL.getScene().setCursor(Cursor.WAIT);
        serviceURL.setDisable(true);
        resetGui();
        new Thread(new Runnable() {
            public void run() {
                try {
                    ServiceModel serviceModel =
                            (ServiceModel) serviceList.getSelectionModel()
                                    .getSelectedItems().get(0);
                    Service service = null;
                    if (serviceModel != null
                        && serviceModel.getUrl().toString().equals(
                            serviceURL.getText())
                            ) {
                        if (ServiceChecker.isReachable(serviceModel
                                    .getItem().getServiceURL())) {
                            service = serviceModel.getItem();
                            service.setPassword(servicePW.getText());
                            service.setUsername(serviceUser.getText());
                        }
                    } else {
                        URL sURL = new URL(serviceURL.getText());
                        if (ServiceChecker.isReachable(sURL)) {
                            service = new Service(
                                    sURL,
                                    "",
                                    true,
                                    serviceUser.getText(),
                                    servicePW.getText());
                        }
                    }
                    if (service == null) {
                        setStatusTextUI(
                            I18n.format("status.service-timeout"));
                        dataBean.setSelectedService(null);
                        serviceSelection.setDisable(false);
                        serviceURL.setDisable(false);
                        serviceURL.getScene().setCursor(Cursor.DEFAULT);
                        return;
                    }
                    serviceSelection.setDisable(true);
                    serviceURL.getScene().setCursor(Cursor.WAIT);
                    setStatusTextUI(
                            I18n.format("status.checking-auth"));
                    serviceURL.setDisable(true);
                    Service finalService = service;
                    Task task = new Task() {
                        protected Integer call() {
                            try {
                                boolean serviceSelected = selectService(
                                        finalService);
                                if (serviceSelected) {
                                    chooseSelectedService(downloadConf);
                                }
                                return 0;
                            } finally {
                                serviceSelection.setDisable(false);
                                serviceURL.getScene()
                                        .setCursor(Cursor.DEFAULT);
                                serviceURL.setDisable(false);
                                validateChainContainerItems();
                            }
                        }
                    };
                    Thread th = new Thread(task);
                    th.setDaemon(true);
                    th.start();
                } catch (MalformedURLException e) {
                    setStatusTextUI(
                            I18n.format("status.no-url"));
                    log.log(Level.SEVERE, e.getMessage(), e);
                    serviceSelection.setDisable(false);
                    serviceURL.getScene()
                            .setCursor(Cursor.DEFAULT);
                    serviceURL.setDisable(false);
                }
            }
        }).start();
    }

    /**
     * Handle search button clicks.
     * Hide search button and start search
     *
     * @param event the event
     */
    @FXML
    protected void handleSearchButtonClick(MouseEvent event) {
        handleSearch(null);
    }

    /**
     * Handle search and filter the service list.
     *
     * @param event the event
     */
    @FXML
    protected void handleSearch(KeyEvent event) {
        if (!catalogReachable) {
            setStatusTextUI(I18n.getMsg("status.catalog-not-available"));
        }
        String currentText = this.searchField.getText();
        this.serviceList.getItems().clear();
        dataBean.resetCatalogLists();
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
                        searchButton.setVisible(false);
                        searchButton.setManaged(false);
                        progressSearch.setVisible(true);
                        progressSearch.setManaged(true);
                    });
                    if (catalogReachable) {
                        List<Service> catalog =
                                dataBean.getCatalogService()
                                        .getServicesByFilter(currentText);
                        for (Service entry : catalog) {
                            dataBean.addCatalogServiceToList(entry);
                        }
                        Platform.runLater(() -> {
                            for (Service entry : catalog) {
                                subentries.add(new ServiceModel(entry));
                            }
                        });
                    }
                    Platform.runLater(() -> {
                        progressSearch.setVisible(false);
                        progressSearch.setManaged(false);
                        searchButton.setManaged(true);
                        searchButton.setVisible(true);
                    });
                    return 0;
                }
            };
            Thread th = new Thread(task);
            if (catalogReachable) {
                setStatusTextUI(I18n.getMsg("status.calling-service"));
            }
            th.setDaemon(true);
            th.start();
        }
        this.serviceList.setItems(subentries);
    }

    private void clearUserNamePassword() {
        this.serviceUser.setText("");
        this.servicePW.setText("");
    }

    private boolean selectService(Service service) {
        if (ServiceChecker.isReachable(service.getServiceURL())) {
            try {
                service.load();
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
                Platform.runLater(() -> {
                    setStatusTextUI(
                            I18n.format("status.service.broken"));
                });
                return false;
            }
        } else {
            Platform.runLater(() -> {
                setStatusTextUI(
                        I18n.format("status.service-not-available"));
            });
            return false;
        }
        if (dataBean.getSelectedService() != null) {
            if (dataBean.getSelectedService().equals(service)) {
                Platform.runLater(() -> {
                    setStatusTextUI(
                            I18n.format("status.ready"));
                });
                return true;
            }
        }
        dataBean.setSelectedService(service);
        Platform.runLater(() -> {
            resetGui();
            this.serviceURL.setText(
                    dataBean.getSelectedService().getServiceURL().toString()
            );
        });
        //Check if Username and Password are given
        if (((dataBean.getSelectedService().getUsername() != null
                && dataBean.getSelectedService().getPassword() != null)
                || (dataBean.getSelectedService().getUsername().isEmpty()
                && dataBean.getSelectedService().getPassword().isEmpty()))
                && dataBean.getSelectedService().isRestricted()) {
            Platform.runLater(() -> {
                setStatusTextUI(
                        I18n.format("status.service-needs-auth"));
                this.serviceAuthenticationCbx.setSelected(true);
                this.serviceUser.setDisable(false);
                this.servicePW.setDisable(false);
            });
            return false;
        } else {
            Platform.runLater(() -> {
                this.serviceAuthenticationCbx.setSelected(false);
                this.serviceUser.setDisable(true);
                this.servicePW.setDisable(true);
                clearUserNamePassword();
            });
        }
        //Check if this thing could be loaded
        if (dataBean.getSelectedService().getServiceType() == null) {
            Platform.runLater(() -> {
                setStatusTextUI(
                        I18n.format("status.service.broken"));
            });
            return false;
        }

        Platform.runLater(() -> {
            setStatusTextUI(
                    I18n.format("status.ready"));
        });
        return true;
    }

    /**
     * Handle the service selection.
     *
     * @param event The mouse click event.
     */
    @FXML
    protected void handleServiceSelect(MouseEvent event) {
        if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
            if (event.getClickCount() == 1) {
                clearUserNamePassword();
                ServiceModel serviceModel =
                        (ServiceModel) this.serviceList.getSelectionModel()
                                .getSelectedItems().get(0);
                if (serviceModel != null) {
                    serviceSelection.setDisable(true);
                    serviceURL.getScene().setCursor(Cursor.WAIT);
                    setStatusTextUI(
                            I18n.format("status.checking-auth"));
                    Task task = new Task() {
                        protected Integer call() {
                            try {
                                selectService(serviceModel.getItem());
                                return 0;
                            } finally {
                                serviceSelection.setDisable(false);
                                serviceURL.getScene().setCursor(Cursor.DEFAULT);
                            }
                        }
                    };
                    Thread th = new Thread(task);
                    th.setDaemon(true);
                    th.start();
                }
            } else if (event.getClickCount() > 1) {
                clearUserNamePassword();
                resetGui();
            }
        }
    }

    /**
     * Handle authentication required selection.
     *
     * @param event the event
     */
    @FXML
    protected void handleAuthenticationRequired(ActionEvent event) {
        boolean flag = !this.serviceAuthenticationCbx.isSelected();
        this.serviceUser.setDisable(flag);
        this.servicePW.setDisable(flag);
    }

    /**
     * Handle the service type selection.
     *
     * @param event The event
     */
    @FXML
    protected void handleServiceTypeSelect(ActionEvent event) {
        ItemModel item =
                this.serviceTypeChooser.
                        getSelectionModel().getSelectedItem();
        if (item != null) {
            dataBean.setDataType(item);
            dataBean.setAttributes(new ArrayList<DataBean.Attribute>());
            chooseType(item);
        }
    }

    /**
     * Handle the dataformat selection.
     *
     * @param event The event
     */
    @FXML
    protected void handleDataformatSelect(ActionEvent event) {
        if (dataFormatChooser.getValue() != null) {
            if (dataFormatChooser.getValue().isAvailable()) {
                dataFormatChooser.setStyle("-fx-border-color: null;");
            } else {
                dataFormatChooser.setStyle("-fx-border-color: red;");
            }
        }
        ComboBox source = (ComboBox) event.getSource();
        dataBean.addAttribute("outputformat",
                source.getValue() != null
                        ? source.getValue().toString()
                        : "",
                "");
        validateChainContainerItems();
    }

    /**
     * Handle the dataformat selection.
     *
     * @param event The event
     */
    @FXML
    protected void handleAddChainItem(ActionEvent event) {
        factory.addChainAttribute(this.dataBean, chainContainer,
                ()->validateChainContainerItems());
        validateChainContainerItems();
    }

    /**
     * Handle the reference system selection.
     *
     * @param event The event
     */
    @FXML
    protected void handleReferenceSystemSelect(ActionEvent event) {
        if (referenceSystemChooser.getValue() != null) {
            if (referenceSystemChooser.getValue().isAvailable()) {
                referenceSystemChooser.setStyle("-fx-border-color: null;");
            } else {
                referenceSystemChooser.setStyle("-fx-border-color: red;");
            }
        }
        this.dataBean.addAttribute("srsName",
                referenceSystemChooser.getValue() != null
                        ? referenceSystemChooser.
                        getValue().getOldName()
                        : "EPSG:4326",
                "");
        if (mapWFS != null && referenceSystemChooser.getValue() != null) {
            this.mapWFS.setDisplayCRS(
                    referenceSystemChooser.getValue().getCRS());
        } else if (mapWFS != null) {
            try {
                this.mapWFS.setDisplayCRS(
                        this.dataBean.getAttributeValue("srsName"));
            } catch (FactoryException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /**
     * Handle the variation selection.
     *
     * @param event The event
     */
    @FXML
    protected void handleVariationSelect(ActionEvent event) {
        ItemModel selim = (ItemModel) this.atomVariationChooser.getValue();
        boolean variationAvailable = false;
        if (selim instanceof MiscItemModel) {
            atomVariationChooser.setStyle("-fx-border-color: red;");
        } else {
            atomVariationChooser.setStyle("-fx-border-color: null;");
            variationAvailable = true;
        }
        if (selim != null) {
            Atom.Field selaf;
            if (variationAvailable) {
                selaf = (Atom.Field) selim.getItem();
            } else {
                selaf = new Atom.Field();
                selaf.format = "";
                selaf.crs = "";
            }
            this.dataBean.addAttribute("VARIATION", selaf.type, "");
            if (selaf.format.isEmpty()) {
                this.valueAtomFormat.setVisible(false);
            } else {
                this.valueAtomFormat.setText(selaf.format);
                this.valueAtomFormat.setVisible(true);
            }
            if (selaf.crs.isEmpty()) {
                this.valueAtomRefsys.setVisible(false);
            } else {
                this.valueAtomRefsys.setVisible(true);
                this.valueAtomRefsys.setText(selaf.crs);
            }
            this.dataBean.addAttribute("outputformat", selaf.format, "");
        } else {
            this.dataBean.addAttribute("VARIATION", "", "");
            this.dataBean.addAttribute("outputformat", "", "");
        }
        validateChainContainerItems();
    }

    private ArrayList<ProcessingStep> extractProcessingSteps() {

        ArrayList<ProcessingStep> steps = new ArrayList<>();
        if (!this.chkChain.isSelected()) {
            return steps;
        }

        Set<Node> parameter =
                this.chainContainer.lookupAll("#process_parameter");

        if (parameter.isEmpty()) {
            return steps;
        }

        String format = this.dataBean.getAttributeValue("outputformat");
        if (format == null || format.isEmpty()) {
            setStatusTextUI(I18n.getMsg("gui.process.no.format"));
            logToAppLog(I18n.getMsg("gui.process.no.format"));
            return steps;
        }

        MIMETypes mtypes = Config.getInstance().getMimeTypes();
        MIMEType mtype = mtypes.findByName(format);
        if (mtype == null) {
            setStatusTextUI(I18n.getMsg("gui.process.format.not.found"));
            logToAppLog(I18n.getMsg("gui.process.format.not.found"));
            return steps;
        }

        for (Node n : parameter) {
            Set<Node> vars = n.lookupAll("#process_var");
            Node nameNode = n.lookup("#process_name");
            ComboBox namebox = (ComboBox) nameNode;
            ProcessingStepConfiguration psc =
                    (ProcessingStepConfiguration) namebox.getValue();

            String name = psc.getName();

            if (!psc.isCompatibleWithFormat(mtype.getType())) {
                setStatusTextUI(
                        I18n.format("gui.process.not.compatible", name));
                        logToAppLog(I18n.format("gui.process.not.compatible",
                                name));
                continue;
            }

            ProcessingStep step = new ProcessingStep();
            steps.add(step);
            step.setName(name);
            ArrayList<Parameter> parameters = new ArrayList<>();
            step.setParameters(parameters);

            for (Node v : vars) {
                String varName = null;
                String varValue = null;
                if (v instanceof TextField) {
                    TextField input = (TextField) v;
                    varName = input.getUserData().toString();
                    varValue = input.getText();
                } else if (v instanceof ComboBox) {
                    ComboBox input = (ComboBox) v;
                    varName = input.getUserData().toString();
                    varValue = input.getValue() != null
                            ? ((Option) input.getValue()).getValue()
                            : null;
                }
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
            this.dataBean.setAttributes(new ArrayList<DataBean.Attribute>());

            ObservableList<Node> children
                    = this.simpleWFSContainer.getChildren();
            for (Node n : children) {
                if (n.getClass() == HBox.class) {
                    HBox hbox = (HBox) n;
                    ObservableList<Node> hboxChildren = hbox.getChildren();
                    String value = "";
                    String name = "";
                    String type = "";
                    Label l1 = null;
                    Label l2 = null;
                    TextField tf = null;
                    ComboBox cb = null;
                    for (Node hn : hboxChildren) {
                        if (hn.getClass() == ComboBox.class) {
                            cb = (ComboBox) hn;
                        }
                        if (hn.getClass() == TextField.class) {
                            tf = (TextField) hn;
                        }
                        if (hn.getClass() == Label.class) {
                            if (l1 == null) {
                                l1 = (Label) hn;
                            }
                            if (l1 != (Label) hn) {
                                l2 = (Label) hn;
                            }
                        }
                        if (tf != null
                                && (l1 != null
                                || l2 != null)) {
                            name = tf.getUserData().toString();
                            value = tf.getText();
                            if (l2 != null && l1.getText().equals(name)) {
                                type = l2.getText();
                            } else {
                                type = l1.getText();
                            }
                        }
                        if (cb != null
                                && (l1 != null
                                || l2 != null)) {
                            if (cb.getId().equals(
                                    UIFactory.getDataFormatID())
                                    ) {
                                name = "outputformat";
                                value = cb.getSelectionModel()
                                        .getSelectedItem().toString();
                                type = "";
                            }
                        }
                        if (!name.isEmpty() && !value.isEmpty()) {
                            this.dataBean.addAttribute(
                                    name,
                                    value,
                                    type);
                        }
                    }

                }
            }
        }
    }

    private void extractBoundingBox() {
        String bbox = "";
        Envelope2D envelope = null;
        switch (this.dataBean.getServiceType()) {
            case Atom:
                //in Atom the bboxes are given by the extend of every dataset
                break;
            case WFSOne:
            case WFSTwo:
                if (mapWFS != null) {
                    envelope = this.mapWFS.getBounds(
                            referenceSystemChooser.
                                    getSelectionModel().
                                    getSelectedItem().
                                    getCRS());
                } else {
                    envelope = mapWFS.calculateBBox(basicX1,
                            basicX2,
                            basicY1,
                            basicY2,
                            referenceSystemChooser.
                                    getSelectionModel().
                                    getSelectedItem().
                                    getCRS());
                }
                break;
            default:
                break;

        }
        if (envelope != null) {
            bbox += envelope.getX() + ",";
            bbox += envelope.getY() + ",";
            bbox += (envelope.getX() + envelope.getWidth()) + ",";
            bbox += (envelope.getY() + envelope.getHeight());

            CRSModel model = referenceSystemChooser.getValue();
            if (model != null) {
                bbox += "," + model.getOldName();
            }
            this.dataBean.addAttribute("bbox", bbox, "");
        } else {
            // Raise an error?
        }
    }

    private boolean validateInput() {
        String failed = "";
        ArrayList<DataBean.Attribute> attributes
                = this.dataBean.getAttributes();

        Validator validator = Validator.getInstance();
        for (DataBean.Attribute attribute : attributes) {
            if (!attribute.type.equals("")) {
                if (!validator.isValid(attribute.type, attribute.value)) {
                    if (failed.equals("")) {
                        failed = attribute.name;
                    } else {
                        failed = failed + ", " + attribute.name;
                    }
                }
            }
        }

        if (serviceTypeChooser.isVisible()
                && downloadConfig != null
                && serviceTypeChooser.getValue() instanceof MiscItemModel) {
            failed += I18n.format("gui.dataset") + ", ";
        }

        if (atomContainer.isVisible()
                && downloadConfig != null
                && atomVariationChooser.getValue() instanceof MiscItemModel) {
            failed += I18n.format("gui.variants") + ", ";
        }

        if (referenceSystemChooser.isVisible()
                && downloadConfig != null
                && !referenceSystemChooser.getValue().isAvailable()) {
            failed += I18n.format("gui.reference-system") + ", ";
        }

        if (basicWFSContainer.isVisible()
                &&  dataFormatChooser.isVisible()
                && downloadConfig != null
                && !dataFormatChooser.getValue().isAvailable()) {
            failed += I18n.format("gui.data-format") + ", ";
        }

        if (simpleWFSContainer.isVisible()
                && downloadConfig != null) {
            ObservableList<Node> children
                    = simpleWFSContainer.getChildren();
            for (Node node: children) {
                if (node instanceof HBox) {
                    HBox hb = (HBox) node;
                    Node n2 = hb.getChildren().get(1);
                    if (n2 instanceof ComboBox) {
                        ComboBox<OutputFormatModel> cb
                                = (ComboBox<OutputFormatModel>) n2;
                        if (!cb.getValue().isAvailable()) {
                            failed += I18n.format("gui.data-format");
                        }
                    }
                }
            }
        }

        if (failed.endsWith(", ")) {
            failed = failed.substring(0, failed.length() - 2);
        }

        if (!failed.equals("")) {
            setStatusTextUI(
                    I18n.format("status.validation-fail", failed));
            return false;
        }
        return true;
    }

    /**
     * Start the download.
     *
     * @param event The event
     */
    @FXML
    protected void handleDownload(ActionEvent event) {
        extractStoredQuery();
        extractBoundingBox();
        if (validateInput()) {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle(I18n.getMsg("gui.save-dir"));
            if (downloadConfig != null
                        && downloadConfig.getDownloadPath() != null) {
                try {
                    File dir =  new File(downloadConfig.getDownloadPath());
                    if (dir.exists()) {
                        dirChooser.setInitialDirectory(dir);
                    }
                } catch (Exception e) {

                }
            }
            File selectedDir = dirChooser.showDialog(getPrimaryStage());
            if (selectedDir == null) {
                return;
            }
            this.dataBean.setProcessingSteps(extractProcessingSteps());

            String savePath = selectedDir.getPath();
            DownloadStep ds = dataBean.convertToDownloadStep(savePath);
            try {
                DownloadStepConverter dsc = new DownloadStepConverter(
                        dataBean.getSelectedService().getUsername(),
                        dataBean.getSelectedService().getPassword());
                JobList jl = dsc.convert(ds);
                Processor p = Processor.getInstance();
                p.addJob(jl);
            } catch (ConverterException ce) {
                setStatusTextUI(ce.getMessage());
            }
        }
    }

    private void resetGui() {
        Platform.runLater(() -> {
            this.serviceTypeChooser.getItems().retainAll();
        });
        this.serviceTypeChooser.setStyle("-fx-border-color: null;");
        this.dataBean.reset();
        this.mapAtom.reset();
        this.mapWFS.reset();
        this.simpleWFSContainer.setVisible(false);
        this.basicWFSContainer.setVisible(false);
        this.mapNodeWFS.setVisible(false);
        this.atomContainer.setVisible(false);
        this.basicWFSX1Y1.setVisible(false);
        this.basicWFSX2Y2.setVisible(false);
        this.referenceSystemChooser.setVisible(false);
        this.referenceSystemChooserLabel.setVisible(false);
        resetProcessingChainContainer();
    }

    /**
     * Handle events on the process Chain Checkbox.
     *
     * @param event the event
     */
    @FXML
    protected void handleChainCheckbox(ActionEvent event) {
        if (chkChain.isSelected()) {
            processStepContainter.setVisible(true);
        } else {
            factory.removeAllChainAttributes(this.dataBean, chainContainer);
            processStepContainter.setVisible(false);
        }
    }

    /**
     * Handle config saving.
     *
     * @param event The event.
     */
    @FXML
    protected void handleSaveConfig(ActionEvent event) {
        extractStoredQuery();
        extractBoundingBox();
        if (validateInput()) {
            FileChooser fileChooser = new FileChooser();
            DirectoryChooser dirChooser = new DirectoryChooser();
            File downloadDir;
            File initDir;

            dirChooser.setTitle(I18n.getMsg("gui.save-dir"));

            if (downloadConfig == null) {
                downloadDir = dirChooser.showDialog(getPrimaryStage());
                initDir = new File(System.getProperty("user.dir"));
                File uniqueName = Misc.uniqueFile(downloadDir, "config", "xml",
                        null);
                fileChooser.setInitialFileName(uniqueName.getName());
            } else {
                File downloadInitDir
                        = new File(downloadConfig.getDownloadPath());
                if (!downloadInitDir.exists()) {
                    downloadInitDir = new File(System.getProperty("user.dir"));
                }
                dirChooser.setInitialDirectory(downloadInitDir);
                downloadDir = dirChooser.showDialog(getPrimaryStage());

                String path = downloadConfig.getFile().getAbsolutePath();
                path = path.substring(0, path.lastIndexOf(File.separator));
                initDir = new File(path);
                fileChooser.setInitialFileName(downloadConfig.getFile()
                        .getName());
            }

            fileChooser.setInitialDirectory(initDir);
            FileChooser.ExtensionFilter xmlFilter =
                    new FileChooser.ExtensionFilter("xml files (*.xml)",
                            "*.xml");
                        fileChooser.getExtensionFilters().add(xmlFilter);
            fileChooser.setSelectedExtensionFilter(xmlFilter);
            fileChooser.setTitle(I18n.getMsg("gui.save-conf"));
            File configFile = fileChooser.showSaveDialog(getPrimaryStage());
            if (configFile == null) {
                return;
            }

            if (!configFile.toString().endsWith(".xml")) {
                configFile = new File(configFile.toString() + ".xml");
            }

            this.dataBean.setProcessingSteps(extractProcessingSteps());

            String savePath = downloadDir.getPath();
            DownloadStep ds = dataBean.convertToDownloadStep(savePath);
            try {
                ds.write(configFile);
            } catch (IOException ex) {
                log.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    /**
     * Use selection to request the service data and fill the UI.
     *
     * @param downloadConf Loaded download config, null if service
     *      was chosen from an URL or the service list
     */
    private void chooseSelectedService(DownloadConfig downloadConf) {
        switch (dataBean.getSelectedService().getServiceType()) {
            case Atom:
                Platform.runLater(() -> {
                    setStatusTextUI(
                            I18n.getMsg("status.type.atom"));
                });
                Atom atom = null;
                try {
                    atom = new Atom(
                            dataBean.getSelectedService()
                                    .getServiceURL().toString(),
                            dataBean.getSelectedService().getUsername(),
                            dataBean.getSelectedService().getPassword());
                } catch (IllegalArgumentException
                        | URISyntaxException
                        | SAXException
                        | ParserConfigurationException
                        | IOException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                    Platform.runLater(() -> {
                        setStatusTextUI(
                                I18n.getMsg("status.service.broken")
                        );
                    });
                    resetGui();
                    return;
                } finally {
                    dataBean.setAtomService(atom);
                }
                break;
            case WFSOne:
                Platform.runLater(() -> {
                    setStatusTextUI(
                            I18n.getMsg("status.type.wfsone"));
                });
                WFSMetaExtractor wfsOne =
                        new WFSMetaExtractor(
                                dataBean.getSelectedService()
                                        .getServiceURL().toString(),
                                dataBean.getSelectedService().getUsername(),
                                dataBean.getSelectedService().getPassword());
                WFSMeta metaOne = null;
                try {
                    metaOne = wfsOne.parse();
                } catch (IOException
                        | URISyntaxException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                    Platform.runLater(() -> {
                        setStatusTextUI(
                                I18n.getMsg("status.service.broken")
                        );
                    });
                } finally {
                    dataBean.setWFSService(metaOne);
                }
                break;
            case WFSTwo:
                Platform.runLater(() -> {
                    setStatusTextUI(
                            I18n.getMsg("status.type.wfstwo"));
                });
                WFSMetaExtractor extractor =
                        new WFSMetaExtractor(
                                dataBean.getSelectedService()
                                        .getServiceURL().toString(),
                                dataBean.getSelectedService().getUsername(),
                                dataBean.getSelectedService().getPassword());
                WFSMeta meta = null;
                try {
                    meta = extractor.parse();
                } catch (IOException
                        | URISyntaxException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                    Platform.runLater(() -> {
                        setStatusTextUI(
                                I18n.getMsg("status.service.broken"));
                    });

                } finally {
                    dataBean.setWFSService(meta);
                }
                break;
            default:
                log.log(Level.WARNING,
                        "Could not determine URL",
                        dataBean.getSelectedService());
                Platform.runLater(() -> {
                    setStatusTextUI(I18n.getMsg("status.no-url"));
                });
                break;
        }
        if (dataBean.isWebServiceSet()) {
            Platform.runLater(() -> {
                setServiceTypes();
            });
        } else {
            return;
        }
        Platform.runLater(() -> {
            serviceTypeChooser.
                    getSelectionModel().select(0);
            if (downloadConf != null) {
                loadDownloadConfig(downloadConf);
            }
            setStatusTextUI(I18n.getMsg("status.ready"));
        });
        return;

    }

    /**
     * Sets the Service Types.
     */
    public void setServiceTypes() {
        if (dataBean.isWebServiceSet()) {
            switch (dataBean.getServiceType()) {
                case WFSOne:
                case WFSTwo:
                    ReferencedEnvelope extendWFS = null;
                    List<WFSMeta.Feature> features =
                            dataBean.getWFSService().features;
                    List<WFSMeta.StoredQuery> queries =
                            dataBean.getWFSService().storedQueries;
                    ObservableList<ItemModel> types =
                            FXCollections.observableArrayList();
                    if (!dataBean.getWFSService().isSimple()) {
                        for (WFSMeta.Feature f : features) {
                            types.add(new FeatureModel(f));
                            if (f.bbox != null) {
                                if (extendWFS == null) {
                                    extendWFS = f.bbox;
                                } else {
                                    extendWFS.expandToInclude(f.bbox);
                                }
                            }
                        }
                    }
                    if (extendWFS != null) {
                        mapWFS.setExtend(extendWFS);
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
                    List<WMSMapSwing.FeaturePolygon> polygonList = new
                            ArrayList<WMSMapSwing.FeaturePolygon>();
                    //Polygons are always epsg:4326
                    // (http://www.georss.org/gml.html)
                    try {
                        ReferencedEnvelope extendATOM = null;
                        atomCRS = CRS.decode(ATOM_CRS_STRING);
                        Geometry all = null;
                        for (Atom.Item i : items) {
                            opts.add(new AtomItemModel(i));
                            WMSMapSwing.FeaturePolygon polygon =
                                    new WMSMapSwing.FeaturePolygon(
                                            i.polygon,
                                            i.title,
                                            i.id,
                                            this.atomCRS);
                            polygonList.add(polygon);
                            all = all == null
                                    ? i.polygon : all.union(i.polygon);
                        }
                        if (mapAtom != null) {
                            if (all != null) {
                                extendATOM = new ReferencedEnvelope(
                                        all.getEnvelopeInternal(), atomCRS);
                                mapAtom.setExtend(extendATOM);
                            }
                            mapAtom.drawPolygons(polygonList);
                        }
                    } catch (FactoryException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                    }
                    serviceTypeChooser.getItems().retainAll();
                    serviceTypeChooser.setItems(opts);
                    if (!opts.isEmpty()) {
                        serviceTypeChooser.setValue(opts.get(0));
                        chooseType(serviceTypeChooser.getValue());
                    }
                    Platform.runLater(() -> {
                        mapAtom.repaint();
                    });
                default:
            }
        }
    }

    private void chooseAtomType(ItemModel data, boolean datasetAvailable) {
        Atom.Item item;
        if (datasetAvailable) {
            item = (Atom.Item) data.getItem();
            try {
                item.load();
            } catch (URISyntaxException
                    | SAXException
                    | ParserConfigurationException
                    | IOException e) {
                log.log(Level.SEVERE, "Could not Load Item\n"
                        + e.getMessage(), item);
                return;
            }
        } else {
            try {
                item = new Atom.Item(new URL(
                        this.downloadConfig.getServiceURL()));
                item.description = "";
            } catch (Exception e) {
                return;
            }
        }
        if (mapAtom != null) {
            mapAtom.highlightSelectedPolygon(item.id);
            Platform.runLater(() -> {
                mapAtom.repaint();
            });
        }
        List<Atom.Field> fields = item.fields;
        ObservableList<ItemModel> list =
                FXCollections.observableArrayList();
        for (Atom.Field f : fields) {
            AtomFieldModel afm = new AtomFieldModel(f);
            list.add(afm);
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
                    (new java.awt.Color(BGCOLOR, BGCOLOR, BGCOLOR)).getRGB()
            );
        } catch (NoSuchFieldException
                | SecurityException
                | IllegalArgumentException
                | IllegalAccessException e) {
            // Displays the webview with white background...
        }
        engine.loadContent("<head> <style>"
                + ".description-content" + "{"
                + "font-family: Sans-Serif" + "}"
                + "</style> </head>"
                + "<div class=\"description-content\">"
                + item.description + "</div>");
        this.simpleWFSContainer.setVisible(false);
        this.basicWFSContainer.setVisible(false);
        this.atomContainer.setVisible(true);
    }

    private void chooseWFSType(ItemModel data, boolean datasetAvailable) {
        if (data instanceof FeatureModel
            || (!datasetAvailable
            && downloadConfig.getServiceType() == "WFS2_BASIC")) {
            this.simpleWFSContainer.setVisible(false);
            this.basicWFSContainer.setVisible(true);
            this.mapNodeWFS.setVisible(true);
            this.atomContainer.setVisible(false);
            this.basicWFSX1Y1.setVisible(true);
            this.basicWFSX2Y2.setVisible(true);
            this.referenceSystemChooser.setVisible(true);
            this.referenceSystemChooserLabel.setVisible(true);
            WFSMeta.Feature feature;
            if (datasetAvailable) {
                feature = (WFSMeta.Feature) data.getItem();
            } else {
                feature = new WFSMeta.Feature();
            }
            mapWFS.setExtend(feature.bbox);
            ArrayList<String> list = new ArrayList<String>();
            list.add(feature.defaultCRS);
            list.addAll(feature.otherCRSs);
            ObservableList<CRSModel> crsList =
                    FXCollections.observableArrayList();
            for (String crsStr : list) {
                try {
                    String newcrsStr = crsStr;
                    String seperator = null;
                    if (newcrsStr.contains("::")) {
                        seperator = "::";
                    } else if (newcrsStr.contains("/")) {
                        seperator = "/";
                    }
                    if (seperator != null) {
                        newcrsStr = "EPSG:"
                                + newcrsStr.substring(
                                newcrsStr.lastIndexOf(seperator)
                                        + seperator.length(),
                                newcrsStr.length());
                    }
                    CoordinateReferenceSystem crs = CRS.decode(newcrsStr);
                    CRSModel crsm = new CRSModel(crs);
                    crsm.setOldName(crsStr);
                    crsList.add(crsm);
                } catch (FactoryException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (!crsList.isEmpty()) {
                this.referenceSystemChooser.setItems(crsList);
                CRSModel crsm = crsList.get(0);
                try {
                    CoordinateReferenceSystem initCRS = CRS.decode(
                            INITIAL_CRS_DISPLAY);
                    CRSModel initCRSM = new CRSModel(initCRS);
                    for (int i = 0; i < crsList.size(); i++) {
                        if (crsList.get(i).equals(initCRSM)) {
                            crsm = crsList.get(i);
                            break;
                        }
                    }
                } catch (FactoryException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
                this.referenceSystemChooser.setValue(crsm);
            }
            List<String> outputFormats = feature.outputFormats;

            outputFormats = this.dataBean.getWFSService()
                    .findOperation("GetFeature").outputFormats;
            if (outputFormats.isEmpty()) {
                outputFormats =
                        this.dataBean.getWFSService().outputFormats;
            }
            List<OutputFormatModel> formatModels
                    = new ArrayList<OutputFormatModel>();
            for (String s : outputFormats) {
                OutputFormatModel m = new OutputFormatModel();
                m.setItem(s);
                m.setAvailable(true);
                formatModels.add(m);
            }
            ObservableList<OutputFormatModel> formats =
                    FXCollections.observableArrayList(formatModels);
            this.dataFormatChooser.setItems(formats);
        } else if (data instanceof StoredQueryModel
                || (!datasetAvailable
                && downloadConfig.getServiceType().equals("WFS2_SIMPLE"))) {
            List<String> outputFormats = this.dataBean.getWFSService()
                    .findOperation("GetFeature").outputFormats;
            if (outputFormats.isEmpty()) {
                outputFormats =
                        this.dataBean.getWFSService().outputFormats;
            }
            List<OutputFormatModel> formatModels =
                new ArrayList<OutputFormatModel>();
            for (String i : outputFormats) {
                OutputFormatModel m = new OutputFormatModel();
                m.setItem(i);
                m.setAvailable(true);
                formatModels.add(m);
            }
            WFSMeta.StoredQuery storedQuery;
            if (datasetAvailable) {
                storedQuery = (WFSMeta.StoredQuery) data.getItem();
            } else {
                storedQuery = new WFSMeta.StoredQuery();
            }
            factory.fillSimpleWFS(
                    dataBean,
                    this.simpleWFSContainer,
                    storedQuery,
                    formatModels);
            this.atomContainer.setVisible(false);
            this.simpleWFSContainer.setVisible(true);
            this.basicWFSContainer.setVisible(false);
        }
    }

    private void chooseType(ItemModel data) {
        ServiceType type = this.dataBean.getServiceType();
        boolean datasetAvailable = false;
        if (data instanceof MiscItemModel) {
            serviceTypeChooser.setStyle("-fx-border-color: red;");
            setStatusTextUI(I18n.format("gui.dataset-not-available"));
        } else {
            serviceTypeChooser.setStyle("-fx-border-color: null;");
            datasetAvailable = true;
            setStatusTextUI(I18n.format("status.ready"));
        }
        if (type == ServiceType.Atom) {
            chooseAtomType(data, datasetAvailable);
        } else if (type == ServiceType.WFSTwo) {
            chooseWFSType(data, datasetAvailable);
        }
    }

    /**
     * Set the DataBean and fill the UI with initial data objects.
     *
     * @param dataBean The DataBean object.
     */
    public void setDataBean(DataBean dataBean) {
        this.dataBean = dataBean;
        this.serviceList.setItems(this.dataBean.getServicesAsList());

        ServiceSettings serviceSetting = Config.getInstance().getServices();
        catalogReachable = dataBean.getCatalogService() != null
                && ServiceChecker.isReachable(
                dataBean.getCatalogService().getUrl());
        URL url = null;
        try {
            url = new URL(serviceSetting.getWMSUrl());
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        if (url != null
                && ServiceChecker.isReachable(
                WMSMapSwing.getCapabiltiesURL(url))
                ) {
            mapWFS = new WMSMapSwing(url,
                    MAP_WIDTH,
                    MAP_HEIGHT,
                    serviceSetting.getWMSLayer(),
                    serviceSetting.getWMSSource());
            mapWFS.setCoordinateDisplay(basicX1,
                    basicY1,
                    basicX2,
                    basicY2);
            mapWFS.setCoordinateLabel(lablbasicx1,
                    lablbasicx2,
                    lablbasicy1,
                    lablbasicy2);
            this.mapNodeWFS.getChildren().add(mapWFS);
            this.mapNodeWFS.setAutoSizeChildren(false);

            mapAtom = new WMSMapSwing(url,
                    MAP_WIDTH,
                    MAP_HEIGHT,
                    serviceSetting.getWMSLayer(),
                    serviceSetting.getWMSSource());
            mapAtom.addEventHandler(PolygonClickedEvent.ANY,
                    new SelectedAtomPolygon());
            mapAtom.setCoordinateDisplay(atomX1,
                    atomY1,
                    atomX2,
                    atomY2);
            this.mapNodeAtom.getChildren().add(mapAtom);
            this.mapNodeAtom.setAutoSizeChildren(false);

            this.mapSplitPane.widthProperty().addListener(
                    (obs, oldVal, newVal) -> {
                mapWFS.resizeSwingContent(newVal.doubleValue());
                mapAtom.resizeSwingContent(newVal.doubleValue());
            });
        } else {
            setStatusTextUI(I18n.format("status.wms-not-available"));
        }
        this.atomContainer.setVisible(false);
        this.progressSearch.setVisible(false);
        this.simpleWFSContainer.setVisible(false);
        this.basicWFSContainer.setVisible(false);
        this.serviceUser.setDisable(true);
        this.servicePW.setDisable(true);
        this.processStepContainter.setVisible(false);
    }

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
     * Set the text of the status bar in UI thread.
     * Adds current message to log history.
     *
     * @param msg the text to set.
     */
    public void setStatusTextUI(String msg) {
        String logText;
        String regexAtom = I18n.format("atom.bytes.downloaded",
                 "[\\d|\\.|\\,]+");
        String regexWfs = I18n.format("file.download.bytes", "[\\d|\\.|\\,]+");
        //Filter atom/wfs download messages
        if (!logHistoryParent.getText().matches(regexAtom)
           && !logHistoryParent.getText().matches(regexWfs)) {
            logText = logHistoryParent.getText() + "\n"
                    + logHistory.getText();
        } else {
            logText = logHistory.getText();
        }

        Platform.runLater(() -> {
            logHistoryParent.setText(msg);
            logHistory.setText(logText);
        });
    }

    /**
     * Resets all marks at the processing chain container, items are kept.
     */
    private void resetProcessingChainContainer() {
        for (Node o : chainContainer.getChildren()) {
            if (o instanceof VBox) {
                VBox v = (VBox) o;
                HBox hbox = (HBox) v.getChildren().get(0);
                Node cBox = hbox.getChildren().get(0);
                if (cBox instanceof ComboBox) {
                    cBox.setStyle("-fx-border-color: null");
                    ComboBox box = (ComboBox) cBox;
                    ObservableList<ProcessingStepConfiguration> confs =
                        (ObservableList<ProcessingStepConfiguration>)
                        box.getItems();
                    for (ProcessingStepConfiguration cfgI : confs) {
                        cfgI.setCompatible(true);
                        confs.set(confs.indexOf(cfgI), cfgI);
                    }
                }
            }
        }
    }

    /**
     * Validates the chain items of a ComboBox
     * and marks the box according to the chosen item.
     *
     * @param box Item to validate
     * @return True if chosen item is valid, else false
     */
    private boolean validateChainContainer(ComboBox box) {
        String format = this.dataBean.getAttributeValue("outputformat");
        MIMETypes mtypes = Config.getInstance().getMimeTypes();
        MIMEType mtype = mtypes.findByName(format);

        ProcessingStepConfiguration cfg =
                (ProcessingStepConfiguration) box.getValue();
        ObservableList<ProcessingStepConfiguration> items =
                (ObservableList<ProcessingStepConfiguration>) box.getItems();

        if (mtype == null) {
            box.setStyle("-fx-border-color: red;");
            for (ProcessingStepConfiguration cfgI : items) {
                cfgI.setCompatible(false);
                //Workaround to force cell update
                items.set(items.indexOf(cfgI), cfgI);
            }
            setStatusTextUI(I18n.format("gui.process.format.not.found"));
            return false;
        }

        //Mark items that are incompatible
        for (ProcessingStepConfiguration cfgI : items) {
            cfgI.setCompatible(cfgI.isCompatibleWithFormat(mtype.getType()));
            items.set(items.indexOf(cfgI), cfgI);
        }

        if (cfg == null) {
            box.setStyle("-fx-border-color: null;");
            return true;
        }

        if (cfg.isCompatible()) {
            box.setStyle("-fx-border-color: null;");
        } else {
            box.setStyle("-fx-border-color: red;");
            setStatusTextUI(I18n.format("gui.process.not.compatible",
                    box.getValue()));
        }
        return cfg.isCompatible();
    }

    /**
     * Validates all items in processing chain container.
     */
    private void validateChainContainerItems() {
        //If there's no outputformat selected, return
        if (this.dataBean.getAttributeValue("outputformat")
                == null) {
            resetProcessingChainContainer();
            return;
        }

        boolean allValid = true;
        for (Node o : chainContainer.getChildren()) {
            if (o instanceof VBox) {
                VBox v = (VBox) o;
                HBox hbox = (HBox) v.getChildren().get(0);
                Node cBox = hbox.getChildren().get(0);
                if (cBox instanceof ComboBox) {
                    if (!validateChainContainer((ComboBox) cBox)) {
                        allValid = false;
                    }
                }
            }
        }
        //If all chain items were ready, set status to ready
        if (allValid) {
            setStatusTextUI(I18n.format("status.ready"));
        }
    }

    /**
     * Handels the Action, when a polygon is selected.
     */
    public class SelectedAtomPolygon implements
            EventHandler<Event> {
        @Override
        public void handle(Event event) {
            if (mapAtom != null) {
                if (event instanceof PolygonClickedEvent) {
                    PolygonClickedEvent pce = (PolygonClickedEvent) event;
                    WMSMapSwing.PolygonInfos polygonInfos =
                            pce.getPolygonInfos();
                    String polygonName = polygonInfos.getName();
                    String polygonID = polygonInfos.getID();

                    if (polygonName != null && polygonID != null) {
                        if (polygonName.equals("#@#")) {
                            setStatusTextUI(I18n.format(
                                    "status.polygon-intersect",
                                    polygonID));
                            return;
                        }

                        ObservableList<ItemModel> items =
                                serviceTypeChooser.getItems();
                        int i = 0;
                        for (i = 0; i < items.size(); i++) {
                            AtomItemModel item = (AtomItemModel) items.get(i);
                            Atom.Item aitem = (Atom.Item) item.getItem();
                            if (aitem.id.equals(polygonID)) {
                                break;
                            }
                        }
                        Atom.Item oldItem = (Atom.Item) serviceTypeChooser
                                .getSelectionModel()
                                .getSelectedItem().getItem();
                        if (i < items.size() && !oldItem.id.equals(polygonID)) {
                            serviceTypeChooser.setValue(items.get(i));
                            chooseType(serviceTypeChooser.getValue());
                        }
                    }
                }
            }
        }
    }

    /**
     * Keeps track of download progression and errors.
     */
    private class DownloadListener implements ProcessorListener, Runnable {

        private String message;

        private synchronized String getMessage() {
            return this.message;
        }

        private synchronized void setMessage(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            setStatusTextUI(getMessage());
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

    /**
     * Application log formatting.
     */
    private static class AppLogFormatter extends Formatter {
        /**
         * Formats log record.
         *
         * @return Formatted log entry
         */
        @Override
        public String format(LogRecord record) {
            LocalDateTime time = Instant.ofEpochMilli(record.getMillis())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            return time.format(DateTimeFormatter
                            .ofPattern("E, dd.MM.yyyy - kk:mm:ss"))
                            + " "
                            + record.getMessage()
                            + System.lineSeparator();
        }
    }
}
