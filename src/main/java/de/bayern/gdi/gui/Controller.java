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
import de.bayern.gdi.services.ServiceType;
import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.services.WFSMetaExtractor;
import de.bayern.gdi.services.WebService;
import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.ServiceChecker;
import de.bayern.gdi.utils.ServiceSetting;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
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
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
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
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class Controller {

    private static final int MAP_WIDTH = 350;
    private static final int MAP_HEIGHT = 250;
    private static final int BGCOLOR = 244;
    private static final String INITIAL_CRS_DISPLAY = "EPSG:4326";
    private static final String ATOM_CRS_STRING = "EPSG:4326";
    private CoordinateReferenceSystem atomCRS;
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
    @FXML private ComboBox<CRSModel> referenceSystemChooser;
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
                try {
                    URL servUrl = new URL(url);
                    service.setRestricted(ServiceChecker.isRestricted(servUrl));
                } catch (MalformedURLException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
                this.serviceURL.setText(url);
                if (service.isRestricted()) {
                    statusBarText.setText(
                            I18n.format("status.service-needs-auth")
                    );
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
            dataBean.setAttributes(new ArrayList<DataBean.Attribute>());
            chooseType(item);
        }
    }

    /**
     * Handle the dataformat selection.
     *
     * @param event The event
     */
    @FXML protected void handleDataformatSelect(ActionEvent event) {
        this.dataBean.addAttribute("outputformat",
            this.dataFormatChooser.getValue() != null
                ? this.dataFormatChooser.getValue().toString()
                : "",
                "");
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
                ? referenceSystemChooser.
                    getValue().getOldName()
                : "EPSG:4326",
                "");
        if (referenceSystemChooser.getValue() != null) {
            this.mapWFS.setDisplayCRS(
                    referenceSystemChooser.getValue().getCRS());
        } else {
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
    @FXML protected void handleVariationSelect(ActionEvent event) {
        this.dataBean.addAttribute("VARIATION",
            this.atomVariationChooser.getValue() != null
                ? this.atomVariationChooser.getValue().toString()
                : "",
                "");
        ItemModel im = (ItemModel) serviceTypeChooser.getSelectionModel()
                .getSelectedItem();
        Atom.Item item = (Atom.Item) im.getItem();
        List <Atom.Field> fields = item.fields;
        for (Atom.Field field: fields) {
            if (field.type.equals(this.atomVariationChooser.getValue())) {
                this.valueAtomFormat.setText(field.format);
                this.valueAtomRefsys.setText(field.crs);
                this.dataBean.addAttribute("outputformat", field.format, "");
                break;
            }
        }
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
            statusBarText.setText(I18n.getMsg("gui.process.no.format"));
            return steps;
        }

        MIMETypes mtypes = Config.getInstance().getMimeTypes();
        MIMEType mtype = mtypes.findByName(format);
        if (mtype == null) {
            statusBarText.setText(I18n.getMsg("gui.process.format.not.found"));
            return steps;
        }

        for (Node n: parameter) {
            Set<Node> vars = n.lookupAll("#process_var");
            Node nameNode = n.lookup("#process_name");
            ComboBox namebox = (ComboBox)nameNode;
            ProcessingStepConfiguration psc =
                (ProcessingStepConfiguration)namebox.getValue();

            String name = psc.getName();

            if (!psc.isCompatibleWithFormat(mtype.getType())) {
                statusBarText.setText(
                        I18n.format("gui.process.not.compatible", name));
                continue;
            }

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
            for (Node n: children) {
                if (n.getClass() == HBox.class) {
                    HBox hbox = (HBox) n;
                    ObservableList<Node> hboxChildren = hbox.getChildren();
                    String value = "";
                    String name = "";
                    String type = "";
                    Label l1 = null;
                    Label l2 = null;
                    TextField tf = null;
                    for (Node hn: hboxChildren) {
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
                    }
                    name = tf.getUserData().toString();
                    value = tf.getText();
                    if (l1.getText().equals(name)) {
                        type = l2.getText();
                    } else {
                        type = l1.getText();
                    }
                    this.dataBean.addAttribute(
                            name,
                            value,
                            type);
                }
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
        for (DataBean.Attribute attribute: attributes) {
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
        if (!failed.equals("")) {
            statusBarText.setText(
                    I18n.format("status.validation-fail", failed)
            );
            return false;
        }
        return true;
    }

    /**
     * Start the download.
     *
     * @param event The event
     */
    @FXML protected void handleDownload(ActionEvent event) {
        extractStoredQuery();
        extractBoundingBox();
        if (validateInput()) {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle(I18n.getMsg("gui.save-dir"));
            File selectedDir = dirChooser.showDialog(getPrimaryStage());
            if (selectedDir == null) {
                return;
            }
            this.dataBean.setProcessingSteps(extractProcessingSteps());

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
                statusBarText.setText(ce.getMessage());
            }
        }
    }

    /**
     * Handle config saving.
     * @param event The event.
     */
    @FXML
    protected void handleSaveConfig(ActionEvent event) {
        extractStoredQuery();
        extractBoundingBox();
        if (validateInput()) {
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
     * Use selection to request the service data and fill th UI.
     */
    private void chooseService() {
        Task task = new Task() {
            private void setAuth() {
                Platform.runLater(() -> {
                    statusBarText.setText(
                            I18n.format("status.service-needs-auth")
                    );
                });
                serviceURL.getScene().setCursor(Cursor.DEFAULT);
                serviceAuthenticationCbx.setSelected(true);
                serviceUser.setDisable(false);
                servicePW.setDisable(false);
            }
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
                if ((username == null && password == null)
                        || (username.equals("") && password.equals(""))) {
                    if (ServiceChecker.isRestricted(new URL(url))) {
                        String pw = dataBean.getPassword();
                        String un = dataBean.getUserName();

                        if ((pw == null && un == null)
                                || (pw.isEmpty() && un.isEmpty())) {
                            setAuth();
                            return 0;
                        }
                    } else {
                        serviceAuthenticationCbx.setSelected(false);
                        serviceUser.setDisable(true);
                        servicePW.setDisable(true);
                    }
                }
                if (url != null && !"".equals(url)) {
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
                                Atom atom = new Atom(url,
                                        dataBean.getUserName(),
                                        dataBean.getPassword());
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
                    Platform.runLater(() -> {
                        setServiceTypes();
                        serviceTypeChooser.
                                getSelectionModel().select(0);
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
                case WFSTwo:
                    ReferencedEnvelope extendWFS = null;
                    List<WFSMeta.Feature> features =
                        dataBean.getWFSService().features;
                    List<WFSMeta.StoredQuery> queries =
                        dataBean.getWFSService().storedQueries;
                    ObservableList<ItemModel> types =
                        FXCollections.observableArrayList();
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
                            if (i.polygon != null) {
                                if (all == null) {
                                    all = i.polygon;
                                } else {
                                    all = all.union(i.polygon);
                                }
                            }
                        }
                        if (all != null) {
                            extendATOM = new ReferencedEnvelope(
                                    all.getEnvelopeInternal(), atomCRS);
                            mapAtom.setExtend(extendATOM);
                        }
                        mapAtom.drawPolygons(polygonList);
                    } catch (FactoryException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                    }
                    serviceTypeChooser.getItems().retainAll();
                    serviceTypeChooser.setItems(opts);
                    if (!opts.isEmpty()) {
                        serviceTypeChooser.setValue(opts.get(0));
                        chooseType(serviceTypeChooser.getValue());
                    }
                default:
            }
        }
    }

    private void chooseType(ItemModel data) {
        ServiceType type = this.dataBean.getServiceType();
        if (type == ServiceType.Atom) {
            statusBarText.setText(I18n.format("status.ready"));
            Atom.Item item = (Atom.Item)data.getItem();
            item.load();
            mapAtom.highlightSelectedPolygon(item.id);
            List<Atom.Field> fields = item.fields;
            ObservableList<String> list =
                FXCollections.observableArrayList();
            for (Atom.Field f : fields) {
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
            this.simpleWFSContainer.setVisible(false);
            this.basicWFSContainer.setVisible(false);
            this.atomContainer.setVisible(true);
        } else if (type == ServiceType.WFSTwo) {
            if (data instanceof FeatureModel) {
                this.simpleWFSContainer.setVisible(false);
                this.basicWFSContainer.setVisible(true);
                this.atomContainer.setVisible(false);
                WFSMeta.Feature feature = (WFSMeta.Feature)data.getItem();
                ArrayList<String> list = new ArrayList<String>();
                list.add(feature.defaultCRS);
                list.addAll(feature.otherCRSs);
                ObservableList<CRSModel> crsList =
                        FXCollections.observableArrayList();
                for (String crsStr: list) {
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

        ServiceSetting serviceSetting = Config.getInstance().getServices();

        URL url = null;
        try {
            url = new URL(serviceSetting.getWMSUrl());
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        mapWFS = new WMSMapSwing(url, MAP_WIDTH, MAP_HEIGHT,
                serviceSetting.getWMSLayer(), serviceSetting.getWMSSource());
        mapWFS.setCoordinateDisplay(basicX1, basicY1, basicX2, basicY2);
        mapAtom = new WMSMapSwing(url, MAP_WIDTH, MAP_HEIGHT,
                serviceSetting.getWMSLayer(), serviceSetting.getWMSSource());
        mapAtom.addEventHandler(PolygonClickedEvent.ANY,
                new SelectedAtomPolygon());
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

    /**
     * Handels the Action, when a polygon is selected.
     */
    public class SelectedAtomPolygon implements
            EventHandler<Event> {
        @Override
        public void handle(Event event) {
            String polygonName = mapAtom.getClickedPolygonName();
            String polygonID = mapAtom.getClickedPolygonID();

            if (polygonName != null && polygonID != null) {
                if (polygonName.equals("#@#")) {
                    statusBarText.setText(I18n.format(
                            "status.polygon-intersect",
                            polygonID));
                    return;
                }

                ObservableList<ItemModel> items = serviceTypeChooser.getItems();
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
        Processor.getInstance().addListener(new DownloadListener());

    }

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
