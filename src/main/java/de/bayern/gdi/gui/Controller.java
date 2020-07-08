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

import com.sothawo.mapjfx.MapView;
import de.bayern.gdi.gui.map.FeaturePolygon;
import de.bayern.gdi.gui.map.MapHandler;
import de.bayern.gdi.gui.map.MapHandlerBuilder;
import de.bayern.gdi.gui.map.PolygonClickedEvent;
import de.bayern.gdi.gui.map.PolygonInfos;
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
import de.bayern.gdi.services.FilterEncoder;
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
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static de.bayern.gdi.gui.FeatureModel.FilterType.BBOX;
import static de.bayern.gdi.gui.FeatureModel.FilterType.FILTER;
import static de.bayern.gdi.services.ServiceType.WFS_TWO;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
@Named
@Singleton
public class Controller {

    public static final String USER_DIR = "user.dir";
    private static final String STATUS_READY = "status.ready";
    private static final String OUTPUTFORMAT = "outputformat";
    private static final String FX_BORDER_COLOR_NULL
            = "-fx-border-color: null;";
    private static final String FX_BORDER_COLOR_RED = "-fx-border-color: red;";
    private static final String GUI_PROCESS_NO_FORMAT
            = "gui.process.no.format";
    private static final String GUI_PROCESS_FORMAT_NOT_FOUND
            = "gui.process.format.not.found";
    private static final String GUI_PROCESS_NOT_COMPATIBLE
            = "gui.process.not.compatible";
    private static final String GUI_FORMAT_NOT_SELECTED
            = "gui.no-format-selected";
    private static final int BGCOLOR = 244;
    private static final String EPSG4326 = "EPSG:4326";
    private static final String INITIAL_CRS_DISPLAY = EPSG4326;
    private static final String ATOM_CRS_STRING = EPSG4326;
    private static final int BBOX_X1_INDEX = 0;
    private static final int BBOX_Y1_INDEX = 1;
    private static final int BBOX_X2_INDEX = 2;
    private static final int BBOX_Y2_INDEX = 3;
    private static final int MAX_APP_LOG_BYTES = 8096;
    private static final Logger log
            = LoggerFactory.getLogger(Controller.class.getName());
    //Application log
    private static final Logger APP_LOG
            = LoggerFactory.getLogger("Application_Log");

    // DataBean
    DataBean dataBean;
    private Stage primaryStage;
    private UIFactory factory;
    boolean catalogReachable;
    DownloadConfig downloadConfig;

    @FXML
    private StatusLogController statusLogController;

    @FXML
    private ServiceSelectionController serviceSelectionController;

    @FXML
    private Button buttonClose;
    @FXML
    private MenuBar mainMenu;
    @FXML
    private CheckBox chkChain;
    @FXML
    ComboBox<ItemModel> serviceTypeChooser;
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
    private VBox mapNodeWFS;
    @FXML
    private VBox mapNodeAtom;
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
    private Button basicApplyBbox;
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
    private Button buttonDownload;
    @FXML
    private Button buttonSaveConfig;
    @FXML
    private Button addChainItem;
    @FXML
    private HBox processStepContainter;
    @FXML
    private VBox basicWFSX1Y1;
    @FXML
    private VBox basicWFSX2Y2;
    @FXML
    private Label referenceSystemChooserLabel;
    @FXML
    private TextArea sqlTextarea;
    @FXML
    private VBox sqlWFSArea;
    @FXML
    private HBox basicWFSFirstRows;
    @FXML
    private Label labelAtomVariation;
    @FXML
    private VBox containerChain;
    @FXML
    private Tab tabMap;
    @FXML
    private Tab tabFilter;
    @FXML
    private TabPane tabPane;
    @FXML
    private MapView wfsMapView;
    @FXML
    private Label wfsMapWmsSource;
    @FXML
    private ToggleButton wfsMapBboxButton;
    @FXML
    private ToggleButton wfsMapInfoButton;
    @FXML
    private Button wfsMapResizeButton;
    @FXML
    private MapView atomMapView;
    @FXML
    private Label atomMapWmsSource;
    @FXML
    private ToggleButton atomMapSelectButton;
    @FXML
    private ToggleButton atomMapInfoButton;
    @FXML
    private Button atomMapResizeButton;

    private MapHandler wmsWfsMapHandler;

    private MapHandler wmsAtomMapHandler;

    /**
     * Creates the Controller.
     */
    public Controller() {
        this.factory = new UIFactory();
        Processor.getInstance().addListener(new DownloadListener());
    }

    @FXML
    protected void handleCloseApp(ActionEvent event) {

    }

    /**
     * Logs a message to the application log.
     *
     * @param msg Message to log
     */
    public static void logToAppLog(String msg) {
            APP_LOG.info(msg);
    }

    void loadDownloadConfig( DownloadConfig conf ) {
        String dataset = conf.getDataset();
        if (dataset != null) {
            boolean datasetAvailable = false;
            List<ItemModel> datasets = serviceTypeChooser.
                    getItems();
            for (ItemModel iItem : datasets) {
                if (isFeatureTypeToSelect(iItem, conf)) {
                    serviceTypeChooser.
                            getSelectionModel().select(iItem);
                    datasetAvailable = true;
                }
            }
            if (!datasetAvailable) {
                MiscItemModel errorItem = new MiscItemModel();
                errorItem.setDataset(dataset);
                errorItem.setItem(conf.getDataset());
                statusLogController.setStatusTextUI(
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

    private boolean isFeatureTypeToSelect(ItemModel iItem,
                                          DownloadConfig config) {
        boolean isSameFeatureTypeName =
            iItem.getDataset().equals(config.getDataset());
        if (!isSameFeatureTypeName) {
            return false;
        }
        if (iItem instanceof FeatureModel && config.getCql() != null) {
            return FILTER.equals(((FeatureModel) iItem).getFilterType());
        }
        return true;
    }

    private void loadGUIComponents() {
        switch (downloadConfig.getServiceType()) {
            case "ATOM":
                loadAtom();
                break;
            case "WFS2_BASIC":
                initialiseCrsChooser();
                initializeBoundingBox();
                initializeDataFormatChooser();
                break;
            case "WFS2_SIMPLE":
                loadWfsSimple();
                break;
            case "WFS2_SQL":
                initialiseCrsChooser();
                initializeDataFormatChooser();
                initializeCqlTextArea();
                break;
            default:
                statusLogController.setStatusTextUI(I18n.format("status.config.invalid-xml"));
                break;
        }
        List<DownloadConfig.ProcessingStep> steps =
                downloadConfig.getProcessingSteps();
        factory.removeAllChainAttributes(chainContainer);
        if (steps != null) {
            chkChain.setSelected(true);
            handleChainCheckbox(new ActionEvent());

            for (DownloadConfig.ProcessingStep iStep : steps) {
                factory.addChainAttribute(chainContainer,
                        iStep.getName(), iStep.getParams());
            }
        } else {
            chkChain.setSelected(false);
            handleChainCheckbox(new ActionEvent());
        }
    }

    private void initializeBoundingBox() {
        if (downloadConfig.getBoundingBox() != null) {
            String[] bBox = downloadConfig.getBoundingBox().split(",");
            basicX1.setText(bBox[BBOX_X1_INDEX]);
            basicY1.setText(bBox[BBOX_Y1_INDEX]);
            basicX2.setText(bBox[BBOX_X2_INDEX]);
            basicY2.setText(bBox[BBOX_Y2_INDEX]);
        }
    }

    private void initializeDataFormatChooser() {
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
    }

    private void initialiseCrsChooser() {
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
            statusLogController.setStatusTextUI(I18n.format("status.config.invalid-epsg"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void initializeCqlTextArea() {
        if (downloadConfig != null) {
            String cql = downloadConfig.getCql();
            sqlTextarea.setText(cql);
        }
    }

    private void loadAtom() {
        boolean variantAvailable = false;
        for (ItemModel i: atomVariationChooser.getItems()) {
            Atom.Field field = (Atom.Field) i.getItem();
            if (field.getType()
                    .equals(downloadConfig.getAtomVariation())) {
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
    }

    private void loadWfsSimple() {
        ObservableList<Node> children
            = simpleWFSContainer.getChildren();
        Map<String, String> parameters = downloadConfig.getParams();
        for (Node node: children) {
            if (node instanceof HBox) {
                HBox hb = (HBox) node;
                Node n1 = hb.getChildren().get(0);
                Node n2 = hb.getChildren().get(1);
                if (n1 instanceof Label && n2 instanceof TextField) {
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
                    cb.setOnAction(event -> {
                        if (cb.getValue().isAvailable()) {
                            cb.setStyle(FX_BORDER_COLOR_NULL);
                        } else {
                            cb.setStyle(FX_BORDER_COLOR_RED);
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
                        cb.setStyle(FX_BORDER_COLOR_NULL);
                    } else {
                        cb.setStyle(FX_BORDER_COLOR_RED);
                    }
                }
            }
        }
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
        ComboBox<OutputFormatModel> cb =
            (ComboBox<OutputFormatModel>)event.getSource();
        handleDataformatSelect(cb);
    }

    /**
     * Handle the dataformat selection.
     *
     * @param cb The ComboBox
     */
    protected void handleDataformatSelect(ComboBox<OutputFormatModel> cb) {
        if (cb.getValue() != null) {
            cb.setStyle(cb.getValue().isAvailable()
                ? FX_BORDER_COLOR_NULL
                : FX_BORDER_COLOR_RED);
        }
        dataBean.addAttribute(OUTPUTFORMAT,
            cb.getValue() != null
                ? cb.getValue().toString()
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
        factory.addChainAttribute(chainContainer,
                this::validateChainContainerItems);
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
                referenceSystemChooser.setStyle(FX_BORDER_COLOR_NULL);
            } else {
                referenceSystemChooser.setStyle(FX_BORDER_COLOR_RED);
            }
        }
        this.dataBean.addAttribute("srsName",
                referenceSystemChooser.getValue() != null
                        ? referenceSystemChooser.
                        getValue().getOldName()
                        : EPSG4326,
                "");
        if (wmsWfsMapHandler != null
            && referenceSystemChooser.getValue() != null) {
            this.wmsWfsMapHandler.setDisplayCRS(
                    referenceSystemChooser.getValue().getCRS());
        } else if (wmsWfsMapHandler != null) {
            try {
                this.wmsWfsMapHandler.setDisplayCRS(
                        this.dataBean.getAttributeValue("srsName"));
            } catch (FactoryException e) {
                log.error(e.getMessage(), e);
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
            atomVariationChooser.setStyle(FX_BORDER_COLOR_RED);
        } else {
            atomVariationChooser.setStyle(FX_BORDER_COLOR_NULL);
            variationAvailable = true;
        }
        if (selim != null) {
            Atom.Field selaf;
            if (variationAvailable) {
                selaf = (Atom.Field) selim.getItem();
            } else {
                selaf = new Atom.Field("", "");
            }
            this.dataBean.addAttribute("VARIATION", selaf.getType(), "");
            if (selaf.getFormat().isEmpty()) {
                this.valueAtomFormat.setVisible(false);
            } else {
                this.valueAtomFormat.setText(selaf.getFormat());
                this.valueAtomFormat.setVisible(true);
            }
            if (selaf.getCRS().isEmpty()) {
                this.valueAtomRefsys.setVisible(false);
            } else {
                this.valueAtomRefsys.setVisible(true);
                this.valueAtomRefsys.setText(selaf.getCRS());
            }
            this.dataBean.addAttribute(OUTPUTFORMAT, selaf.getFormat(), "");
        } else {
            this.dataBean.addAttribute("VARIATION", "", "");
            this.dataBean.addAttribute(OUTPUTFORMAT, "", "");
        }
        validateChainContainerItems();
    }

    private List<ProcessingStep> extractProcessingSteps() {

        List<ProcessingStep> steps = new ArrayList<>();
        if (!this.chkChain.isSelected()) {
            return steps;
        }

        Set<Node> parameter =
                this.chainContainer.lookupAll("#process_parameter");

        if (parameter.isEmpty()) {
            return steps;
        }

        String format = this.dataBean.getAttributeValue(OUTPUTFORMAT);
        if (format == null || format.isEmpty()) {
            statusLogController.setStatusTextUI(I18n.getMsg(GUI_PROCESS_NO_FORMAT));
            logToAppLog(I18n.getMsg(GUI_PROCESS_NO_FORMAT));
            return steps;
        }

        MIMETypes mtypes = Config.getInstance().getMimeTypes();
        MIMEType mtype = mtypes.findByName(format);
        if (mtype == null) {
            statusLogController.setStatusTextUI(I18n.getMsg(GUI_PROCESS_FORMAT_NOT_FOUND));
            logToAppLog(I18n.getMsg(GUI_PROCESS_FORMAT_NOT_FOUND));
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
                statusLogController.setStatusTextUI(
                        I18n.format(GUI_PROCESS_NOT_COMPATIBLE, name));
                        logToAppLog(I18n.format(GUI_PROCESS_NOT_COMPATIBLE,
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
                        if (tf != null && (l1 != null || l2 != null)) {
                            name = tf.getUserData().toString();
                            value = tf.getText();
                            if (l2 != null && l1.getText().equals(name)) {
                                type = l2.getText();
                            } else {
                                type = l1.getText();
                            }
                        }
                        if (cb != null && (l1 != null || l2 != null)
                        && cb.getId().equals(UIFactory.getDataFormatID())) {
                            name = OUTPUTFORMAT;
                            if (cb.getSelectionModel() != null
                                && cb.getSelectionModel().getSelectedItem()
                                != null) {
                                value = cb.getSelectionModel()
                                    .getSelectedItem().toString();
                                type = "";
                            } else {
                                Platform.runLater(() -> statusLogController.setStatusTextUI(
                                    I18n.getMsg(GUI_FORMAT_NOT_SELECTED)));
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
        Envelope2D envelope = null;
        switch (this.dataBean.getServiceType()) {
            case ATOM:
                //in Atom the bboxes are given by the extend of every dataset
                break;
            case WFS_ONE:
            case WFS_TWO:
                if (wmsWfsMapHandler != null) {
                    envelope = this.wmsWfsMapHandler.getBounds(
                            referenceSystemChooser.
                                    getSelectionModel().
                                    getSelectedItem().
                                    getCRS());
                }
                break;
            default:
                break;
        }
        if (envelope == null) {
            // Raise an error?
            return;
        }

        StringBuilder bbox = new StringBuilder();
        bbox.append(envelope.getX()).append(',')
            .append(envelope.getY()).append(',')
            .append(envelope.getX() + envelope.getWidth()).append(',')
            .append(envelope.getY() + envelope.getHeight());

        CRSModel model = referenceSystemChooser.getValue();
        if (model != null) {
            bbox.append(',').append(model.getOldName());
        }
        this.dataBean.addAttribute("bbox", bbox.toString(), "");
    }

    private void extractCql() {
        String sqlInput = sqlTextarea.getText();
        if (dataBean.isFilterType()
            && sqlInput != null
            && !sqlInput.isEmpty()) {
            this.dataBean.addAttribute("CQL", sqlInput, "");
        }
    }

    private boolean validateInput() {
        final StringBuilder failed = new StringBuilder();

        Consumer<String> fail = s -> {
            if (failed.length() != 0) {
                failed.append(", ");
            }
            failed.append(s);
        };

        for (DataBean.Attribute attr: this.dataBean.getAttributes()) {
            if (!attr.getType().isEmpty()
            && !Validator.isValid(attr.getType(), attr.getValue())) {
                fail.accept(attr.getName());
            }
        }

        if (downloadConfig != null) {
            if (serviceTypeChooser.isVisible()
            && serviceTypeChooser.getValue() instanceof MiscItemModel) {
                fail.accept(I18n.format("gui.dataset"));
            }

            if (atomContainer.isVisible()
            && atomVariationChooser.getValue() instanceof MiscItemModel) {
                fail.accept(I18n.format("gui.variants"));
            }

            if (referenceSystemChooser.isVisible()
            && !referenceSystemChooser.getValue().isAvailable()) {
                fail.accept(I18n.format("gui.reference-system"));
            }

            if (basicWFSContainer.isVisible()
            &&  dataFormatChooser.isVisible()
            && !dataFormatChooser.getValue().isAvailable()) {
                fail.accept(I18n.format("gui.data-format"));
            }

            if (simpleWFSContainer.isVisible()) {
                ObservableList<Node> children
                    = simpleWFSContainer.getChildren();
                for (Node node: children) {
                    if (node instanceof HBox) {
                        HBox hb = (HBox)node;
                        Node n2 = hb.getChildren().get(1);
                        if (n2 instanceof ComboBox) {
                            ComboBox<OutputFormatModel> cb
                                    = (ComboBox<OutputFormatModel>) n2;
                            if (!cb.getValue().isAvailable()) {
                                fail.accept(I18n.format("gui.data-format"));
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (failed.length() == 0) {
            return true;
        }
        statusLogController.setStatusTextUI(
            I18n.format("status.validation-fail", failed.toString()));
        return false;
    }

    /**
     * Validates the input in the SQL-Textbox for ECQL pattern.
     *
     * @param userInput      the ECQL to validate
     * @param overallQueries if the query is over all feature types
     * @return <code>true</code> if the ECQL is valid,
     * <code>false</code> otherwise
     */
    public boolean validateEcqlUserInput(String userInput,
                                         boolean overallQueries) {
        if (overallQueries) {
            String[] userInputLines = userInput.split("\n");
            for (String userInputLine : userInputLines) {
                boolean lineContainsNoWhere = !userInputLine
                    .toLowerCase().contains("where");
                if (lineContainsNoWhere) {
                    statusLogController.setStatusTextUI(I18n.format(
                        "status.sql.validation.error.overall.where"));
                    return false;
                }
                boolean lineContainsSupportedFeatureType =
                    featureTypeWithNameExists(userInputLine);
                if (!lineContainsSupportedFeatureType) {
                    statusLogController.setStatusTextUI(I18n.format(
                        "status.sql.validation.error.overall.featureTypes"));
                    return false;
                }
            }
        } else {
            boolean filterContainsWhere = userInput
                .toLowerCase().contains("where");
            if (filterContainsWhere) {
                statusLogController.setStatusTextUI(I18n.format(
                    "status.sql.validation.error.simple.where"));
                return false;
            }
            boolean filterContiansLineBreak = userInput
                .toLowerCase().contains("\n");
            if (filterContiansLineBreak) {
                statusLogController.setStatusTextUI(I18n.format(
                    "status.sql.validation.error.simple.lineBreak"));
                return false;
            }
        }
        return true;
    }

    private boolean featureTypeWithNameExists(String userInputLine) {
        List<WFSMeta.Feature> featureTypes = dataBean.getWFSService()
            .getFeatures();
        for (WFSMeta.Feature feature : featureTypes) {
            if (userInputLine.contains(feature.getName())) {
                return true;
            }
        }
        return false;
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
        extractCql();
        if (validateInput() && validateCqlInput()) {
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
                    log.warn(e.getLocalizedMessage());
                }
            }
            File selectedDir = dirChooser.showDialog(getPrimaryStage());
            if (selectedDir == null) {
                return;
            }
            statusLogController.setStatusTextUI(
                I18n.format("status.download.started"));

            this.dataBean.setProcessingSteps(extractProcessingSteps());
            String savePath = selectedDir.getPath();
            Runnable convertTask = () -> {
                DownloadStep ds = dataBean.convertToDownloadStep(savePath);
                try {
                    this.buttonDownload.setDisable(true);
                    DownloadStepConverter dsc = new DownloadStepConverter(
                            dataBean.getSelectedService().getUsername(),
                            dataBean.getSelectedService().getPassword());
                    JobList jl = dsc.convert(ds);
                    Processor p = Processor.getInstance();
                    p.addJob(jl);
                } catch (ConverterException ce) {
                    statusLogController.setStatusTextUI(ce.getMessage());
                    logToAppLog(ce.getMessage());
                } finally {
                    this.buttonDownload.setDisable(false);
                }
            };
            new Thread(convertTask).start();
        }
    }

    private boolean validateCqlInput() {
        if (dataBean.isFilterType()) {
            String sqlInput = sqlTextarea.getText();
            statusLogController.setLogHistoryStyle(null);
            if (sqlInput == null || sqlInput.isEmpty()) {
                statusLogController.setLogHistoryStyle("-fx-text-fill: #FF0000");
                statusLogController.setStatusTextUI(I18n
                    .format("status.sql.input.empty"));
                return false;
            }
            boolean multipleQuery = dataBean.isMultipleQuery();
            boolean isValidCql = validateEcqlUserInput(sqlInput,
                multipleQuery);
            if (!isValidCql) {
                return false;
            }
            try {
                FilterEncoder filterEncoder = new FilterEncoder();
                filterEncoder.validateCql(sqlInput);
            } catch (CQLException e) {
                statusLogController.setLogHistoryStyle("-fx-text-fill: #FF0000");
                statusLogController.setStatusTextUI(e.getSyntaxError());
                return false;
            } catch (ConverterException e) {
                statusLogController.setLogHistoryStyle("-fx-text-fill: #FF0000");
                statusLogController.setStatusTextUI(I18n
                    .format("status.sql.validation.failed", e.getMessage()));
                return false;
            }
        }
        statusLogController.setStatusTextUI(I18n.format("status.ready"));
        return true;
    }

    void resetGui() {
        Platform.runLater(() ->
            this.serviceTypeChooser.getItems().retainAll()
        );
        this.serviceTypeChooser.setStyle(FX_BORDER_COLOR_NULL);
        this.dataBean.reset();
        if (wmsWfsMapHandler != null) {
            this.wmsWfsMapHandler.reset();
        }
        if (wmsAtomMapHandler != null) {
            this.wmsAtomMapHandler.reset();
        }
        this.simpleWFSContainer.setVisible(false);
        this.basicWFSContainer.setVisible(false);
        this.mapNodeWFS.setVisible(false);
        this.atomContainer.setVisible(false);
        this.sqlWFSArea.setVisible(false);
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
            factory.removeAllChainAttributes(chainContainer);
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
        extractCql();
        if (validateInput() && validateCqlInput()) {
            FileChooser fileChooser = new FileChooser();
            DirectoryChooser dirChooser = new DirectoryChooser();
            File downloadDir;
            File initDir;

            dirChooser.setTitle(I18n.getMsg("gui.save-dir"));

            if (downloadConfig == null) {
                downloadDir = dirChooser.showDialog(getPrimaryStage());
                String basedir = Config.getInstance().getServices()
                    .getBaseDirectory();
                initDir = new File(
                    basedir.isEmpty()
                    ? System.getProperty(USER_DIR)
                    : basedir);
                File uniqueName = Misc.uniqueFile(downloadDir, "config", "xml",
                        null);
                fileChooser.setInitialFileName(uniqueName.getName());
            } else {
                File downloadInitDir
                        = new File(downloadConfig.getDownloadPath());
                if (!downloadInitDir.exists()) {
                    downloadInitDir = new File(System.getProperty(USER_DIR));
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
                log.warn(ex.getMessage(), ex);
            }
        }
    }

    /**
     * Sets the Service Types.
     */
    public void setServiceTypes() {
        if (dataBean.isWebServiceSet()) {
            switch (dataBean.getServiceType()) {
                case WFS_ONE:
                case WFS_TWO:
                    boolean isWfs2 = WFS_TWO.equals(dataBean.getServiceType());
                    ObservableList<ItemModel> types =
                        collectServiceTypes(isWfs2);
                    addStoredQueries(types);
                    serviceTypeChooser.getItems().retainAll();
                    serviceTypeChooser.setItems(types);
                    serviceTypeChooser.setValue(types.get(0));
                    chooseType(serviceTypeChooser.getValue());
                    break;
                case ATOM:
                    List<Atom.Item> items =
                            dataBean.getAtomService().getItems();
                    ObservableList<ItemModel> opts =
                            FXCollections.observableArrayList();
                    List<FeaturePolygon> polygonList
                        = new ArrayList<>();
                    //Polygons are always epsg:4326
                    // (http://www.georss.org/gml.html)
                    try {
                        ReferencedEnvelope extendATOM = null;
                        CoordinateReferenceSystem
                            atomCRS = CRS.decode(ATOM_CRS_STRING);
                        Geometry all = null;
                        for (Atom.Item i : items) {
                            opts.add(new AtomItemModel(i));
                            FeaturePolygon polygon =
                                    new FeaturePolygon(
                                            i.getPolygon(),
                                            i.getTitle(),
                                            i.getID(),
                                            atomCRS);
                            polygonList.add(polygon);
                            all = all == null
                                ? i.getPolygon()
                                : all.union(i.getPolygon());
                        }
                        if (wmsAtomMapHandler != null) {
                            if (all != null) {
                                extendATOM = new ReferencedEnvelope(
                                        all.getEnvelopeInternal(), atomCRS);
                                wmsAtomMapHandler.setExtend(extendATOM);
                            }
                            wmsAtomMapHandler.drawPolygons(polygonList);
                        }
                    } catch (FactoryException e) {
                        log.error(e.getMessage(), e);
                    }
                    serviceTypeChooser.getItems().retainAll();
                    serviceTypeChooser.setItems(opts);
                    if (!opts.isEmpty()) {
                        serviceTypeChooser.setValue(opts.get(0));
                        chooseType(serviceTypeChooser.getValue());
                    }
                    break;
                default:
            }
        }
    }

    private ObservableList<ItemModel> collectServiceTypes(boolean isWfs2) {
        ReferencedEnvelope extendWFS = null;
        List<WFSMeta.Feature> features =
                dataBean.getWFSService().getFeatures();
        ObservableList<ItemModel> types =
                FXCollections.observableArrayList();
        if (!dataBean.getWFSService().isSimple()) {
            for (WFSMeta.Feature f : features) {
                if (isWfs2) {
                    types.add(new FeatureModel(f, FILTER));
                    types.add(new FeatureModel(f, BBOX));
                } else {
                    types.add(new FeatureModel(f));
                }
                if (f.getBBox() != null) {
                    if (extendWFS == null) {
                        extendWFS = f.getBBox();
                    } else {
                        extendWFS.expandToInclude(f.getBBox());
                    }
                }
            }
            types.add(new OverallFeatureTypeModel(features));
        }
        if (extendWFS != null) {
            wmsWfsMapHandler.setExtend(extendWFS);
        }
        return types;
    }

    private void addStoredQueries(ObservableList<ItemModel> types) {
        List<WFSMeta.StoredQuery> queries =
            dataBean.getWFSService().getStoredQueries();
        for (WFSMeta.StoredQuery s : queries) {
            types.add(new StoredQueryModel(s));
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
                log.error("Could not Load Item\n"
                        + e.getMessage(), item);
                return;
            }
        } else {
            try {
                item = new Atom.Item(
                    new URL(this.downloadConfig.getServiceURL()),
                    "");
            } catch (Exception e) {
                return;
            }
        }
        if (wmsAtomMapHandler != null) {
            wmsAtomMapHandler.highlightSelectedPolygon(item.getID());
        }
        List<Atom.Field> fields = item.getFields();
        ObservableList<ItemModel> list =
                FXCollections.observableArrayList();
        for (Atom.Field f : fields) {
            AtomFieldModel afm = new AtomFieldModel(f);
            list.add(afm);
        }
        this.atomVariationChooser.setItems(list);
        this.atomVariationChooser.getSelectionModel().selectFirst();
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
                + item.getDescription() + "</div>");
        this.simpleWFSContainer.setVisible(false);
        this.basicWFSContainer.setVisible(false);
        this.atomContainer.setVisible(true);
    }

    private void chooseWFSType(ItemModel data, boolean datasetAvailable) {
        if (data instanceof FeatureModel
            || data instanceof  OverallFeatureTypeModel
            || (!datasetAvailable
            && downloadConfig.getServiceType() == "WFS2_BASIC")) {
            boolean isSqlFilterType = false;
            if (data instanceof OverallFeatureTypeModel) {
                isSqlFilterType = true;
            }
            if (data instanceof FeatureModel) {
                FeatureModel.FilterType filterType =
                    ((FeatureModel) data).getFilterType();
                isSqlFilterType = FILTER.equals(filterType);
            }

            this.simpleWFSContainer.setVisible(false);
            this.atomContainer.setVisible(false);
            this.referenceSystemChooser.setVisible(true);
            this.referenceSystemChooserLabel.setVisible(true);
            this.basicWFSContainer.setVisible(true);
            if (isSqlFilterType) {
                this.sqlWFSArea.setVisible(true);
                this.sqlWFSArea.setManaged(true);
                this.mapNodeWFS.setVisible(false);
                this.mapNodeWFS.setManaged(false);
            } else {
                this.sqlWFSArea.setVisible(false);
                this.sqlWFSArea.setManaged(false);
                this.mapNodeWFS.setVisible(true);
                this.mapNodeWFS.setManaged(true);
            }

            if (data.getItem() instanceof WFSMeta.Feature) {
                setCrsAndExtent((WFSMeta.Feature) data.getItem());
            } else if (data.getItem() instanceof List
                && !((List) data.getItem()).isEmpty()) {
                List items = (List) data.getItem();
                setCrsAndExtent((WFSMeta.Feature)
                    items.get(items.size() - 1));
            }
            List<String> outputFormats = this
                .dataBean.getWFSService()
                .findOperation("GetFeature").getOutputFormats();

            if (outputFormats.isEmpty()) {
                outputFormats =
                        this.dataBean.getWFSService().getOutputFormats();
            }
            List<OutputFormatModel> formatModels = new ArrayList<>();
            for (String s : outputFormats) {
                OutputFormatModel m = new OutputFormatModel();
                m.setItem(s);
                m.setAvailable(true);
                formatModels.add(m);
            }
            ObservableList<OutputFormatModel> formats =
                    FXCollections.observableArrayList(formatModels);
            this.dataFormatChooser.setItems(formats);
            this.dataFormatChooser.getSelectionModel().selectFirst();
        } else if (data instanceof StoredQueryModel
                || (!datasetAvailable
                && downloadConfig.getServiceType().equals("WFS2_SIMPLE"))) {
            List<String> outputFormats = this.dataBean.getWFSService()
                    .findOperation("GetFeature").getOutputFormats();
            if (outputFormats.isEmpty()) {
                outputFormats =
                        this.dataBean.getWFSService().getOutputFormats();
            }
            List<OutputFormatModel> formatModels =
                new ArrayList<>();
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
                    this.simpleWFSContainer,
                    storedQuery,
                    formatModels);

            // XXX: This is a bit ugly. We need real MVC.
            Node df = this.simpleWFSContainer
                .lookup("#" + UIFactory.getDataFormatID());
            if (df instanceof ComboBox) {
                ((ComboBox)df).setOnAction(evt -> {
                    ComboBox<OutputFormatModel> cb =
                        (ComboBox<OutputFormatModel>)evt.getSource();
                    handleDataformatSelect(cb);
                });
            }

            this.atomContainer.setVisible(false);
            this.simpleWFSContainer.setVisible(true);
            this.basicWFSContainer.setVisible(false);
        }
    }

    private void setCrsAndExtent(WFSMeta.Feature feature) {
        wmsWfsMapHandler.setExtend(feature.getBBox());
        ArrayList<String> list = new ArrayList<>();
        list.add(feature.getDefaultCRS());
        list.addAll(feature.getOtherCRSs());
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
                log.error(e.getMessage(), e);
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
                log.error(e.getMessage(), e);
            }
            this.referenceSystemChooser.setValue(crsm);
        }
    }

    private void chooseType(ItemModel data) {
        ServiceType type = this.dataBean.getServiceType();
        boolean datasetAvailable = false;
        if (data instanceof MiscItemModel) {
            serviceTypeChooser.setStyle(FX_BORDER_COLOR_RED);
            statusLogController.setStatusTextUI(I18n.format("gui.dataset-not-available"));
        } else {
            serviceTypeChooser.setStyle(FX_BORDER_COLOR_NULL);
            datasetAvailable = true;
            statusLogController.setStatusTextUI(I18n.format(STATUS_READY));
        }
        if (type == ServiceType.ATOM) {
            chooseAtomType(data, datasetAvailable);
        } else if (type == WFS_TWO) {
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
        ObservableList<ServiceModel> servicesAsList = this.dataBean.getServicesAsList();
        serviceSelectionController.setServices( servicesAsList );

        ServiceSettings serviceSetting = Config.getInstance().getServices();
        catalogReachable = dataBean.getCatalogService() != null
                && ServiceChecker.isReachable(
                dataBean.getCatalogService().getUrl());
        URL url = null;
        try {
            url = new URL(serviceSetting.getWMSUrl());
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        }
        if (url != null && ServiceChecker
            .isReachable(wmsWfsMapHandler.getCapabiltiesURL(url))) {
            this.wmsWfsMapHandler = MapHandlerBuilder
                .newBuilder(serviceSetting)
                .withEventTarget(mapNodeWFS)
                .withMapView(wfsMapView)
                .withWmsSourceLabel(wfsMapWmsSource)
                .withBboxButton(wfsMapBboxButton)
                .withInfoButton(wfsMapInfoButton)
                .withResizeButtton(wfsMapResizeButton)
                .withCoordinateDisplay(
                    basicX1,
                    basicX2,
                    basicY1,
                    basicY2)
                .withCoordinateLabel(
                    lablbasicx1,
                    lablbasicx2,
                    lablbasicy1,
                    lablbasicy2)
                .withApplyCoordsToMapButton(
                    basicApplyBbox)
                .build();
            this.wmsAtomMapHandler = MapHandlerBuilder
                .newBuilder(serviceSetting)
                .withEventTarget(mapNodeAtom)
                .withMapView(atomMapView)
                .withWmsSourceLabel(atomMapWmsSource)
                .withSelectButton(atomMapSelectButton)
                .withInfoButton(atomMapInfoButton)
                .withResizeButtton(atomMapResizeButton)
                .build();
            mapNodeAtom.addEventHandler(PolygonClickedEvent.ANY,
                new SelectedAtomPolygon());
        } else {
            statusLogController.setStatusTextUI(I18n.format("status.wms-not-available"));
        }
        this.atomContainer.setVisible(false);
        this.simpleWFSContainer.setVisible(false);
        this.basicWFSContainer.setVisible(false);
        this.processStepContainter.setVisible(false);
        serviceSelectionController.resetGui();
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
     * Resets all marks at the processing chain container, items are kept.
     */
    private void resetProcessingChainContainer() {
        for (Node o : chainContainer.getChildren()) {
            if (o instanceof VBox) {
                VBox v = (VBox) o;
                HBox hbox = (HBox) v.getChildren().get(0);
                Node cBox = hbox.getChildren().get(0);
                if (cBox instanceof ComboBox) {
                    cBox.setStyle(FX_BORDER_COLOR_NULL);
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
        String format = this.dataBean.getAttributeValue(OUTPUTFORMAT);
        if (format == null) {
            box.setStyle(FX_BORDER_COLOR_RED);
            statusLogController.setStatusTextUI(I18n.format(GUI_PROCESS_NO_FORMAT));
        }
        MIMETypes mtypes = Config.getInstance().getMimeTypes();
        MIMEType mtype = mtypes.findByName(format);

        ProcessingStepConfiguration cfg =
                (ProcessingStepConfiguration) box.getValue();
        ObservableList<ProcessingStepConfiguration> items =
                (ObservableList<ProcessingStepConfiguration>) box.getItems();

        if (format != null && mtype == null) {
            box.setStyle(FX_BORDER_COLOR_RED);
            for (ProcessingStepConfiguration cfgI : items) {
                cfgI.setCompatible(false);
                //Workaround to force cell update
                items.set(items.indexOf(cfgI), cfgI);
            }
            statusLogController.setStatusTextUI(I18n.format(GUI_PROCESS_FORMAT_NOT_FOUND));
            return false;
        }

        //Mark items that are incompatible
        for (ProcessingStepConfiguration cfgI : items) {
            if (format != null) {
                cfgI.setCompatible(
                        cfgI.isCompatibleWithFormat(mtype.getType()));
            } else {
                cfgI.setCompatible(false);
            }
            items.set(items.indexOf(cfgI), cfgI);
        }

        if (format == null) {
            return false;
        }

        if (cfg == null) {
            box.setStyle(FX_BORDER_COLOR_NULL);
            return true;
        }

        if (cfg.isCompatible()) {
            box.setStyle(FX_BORDER_COLOR_NULL);
        } else {
            box.setStyle(FX_BORDER_COLOR_RED);
            statusLogController.setStatusTextUI(I18n.format(GUI_PROCESS_NOT_COMPATIBLE,
                    box.getValue()));
        }
        return cfg.isCompatible();
    }

    /**
     * Validates all items in processing chain container.
     */
    void validateChainContainerItems() {

        boolean allValid = true;
        for (Node o : chainContainer.getChildren()) {
            if (o instanceof VBox) {
                VBox v = (VBox)o;
                HBox hbox = (HBox)v.getChildren().get(0);
                Node cBox = hbox.getChildren().get(0);
                if (cBox instanceof ComboBox
                && !validateChainContainer((ComboBox)cBox)) {
                    allValid = false;
                }
            }
        }
        //If all chain items were ready, set status to ready
        if (allValid) {
            statusLogController.setStatusTextUI(I18n.format(STATUS_READY));
        }
    }


    /**
     * Handels the Action, when a polygon is selected.
     */
    public class SelectedAtomPolygon implements
            EventHandler<Event> {
        @Override
        public void handle(Event event) {
            if (wmsAtomMapHandler != null
                && event instanceof PolygonClickedEvent) {

                PolygonClickedEvent pce = (PolygonClickedEvent) event;
                PolygonInfos polygonInfos =
                        pce.getPolygonInfos();
                String polygonName = polygonInfos.getName();
                String polygonID = polygonInfos.getID();

                if (polygonName != null && polygonID != null) {
                    if (polygonName.equals("#@#")) {
                        statusLogController.setStatusTextUI(I18n.format(
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
                        if (aitem.getID().equals(polygonID)) {
                            break;
                        }
                    }
                    Atom.Item oldItem = (Atom.Item) serviceTypeChooser
                            .getSelectionModel()
                            .getSelectedItem().getItem();
                    if (i < items.size()
                    && !oldItem.getID().equals(polygonID)) {
                        serviceTypeChooser.setValue(items.get(i));
                        chooseType(serviceTypeChooser.getValue());
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
            statusLogController.setStatusTextUI(getMessage());
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
