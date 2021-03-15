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

package de.bayern.gdi.gui.controller;

import de.bayern.gdi.gui.DataBean;
import de.bayern.gdi.gui.FeatureModel;
import de.bayern.gdi.gui.ItemModel;
import de.bayern.gdi.gui.OutputFormatModel;
import de.bayern.gdi.gui.OverallFeatureTypeModel;
import de.bayern.gdi.gui.ServiceModel;
import de.bayern.gdi.gui.StoredQueryModel;
import de.bayern.gdi.gui.UIFactory;
import de.bayern.gdi.gui.Validator;
import de.bayern.gdi.model.MIMEType;
import de.bayern.gdi.model.MIMETypes;
import de.bayern.gdi.model.Option;
import de.bayern.gdi.model.Parameter;
import de.bayern.gdi.model.ProcessingStep;
import de.bayern.gdi.model.ProcessingStepConfiguration;
import de.bayern.gdi.processor.ConverterException;
import de.bayern.gdi.processor.Processor;
import de.bayern.gdi.processor.ProcessorEvent;
import de.bayern.gdi.processor.ProcessorListener;
import de.bayern.gdi.services.FilterEncoder;
import de.bayern.gdi.services.ServiceType;
import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.DownloadConfig;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.ServiceChecker;
import de.bayern.gdi.utils.ServiceSettings;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static de.bayern.gdi.gui.GuiConstants.FX_BORDER_COLOR_NULL;
import static de.bayern.gdi.gui.GuiConstants.FX_BORDER_COLOR_RED;
import static de.bayern.gdi.gui.GuiConstants.GUI_PROCESS_FORMAT_NOT_FOUND;
import static de.bayern.gdi.gui.GuiConstants.GUI_PROCESS_NOT_COMPATIBLE;
import static de.bayern.gdi.gui.GuiConstants.GUI_PROCESS_NO_FORMAT;
import static de.bayern.gdi.gui.GuiConstants.OUTPUTFORMAT;
import static de.bayern.gdi.gui.FeatureModel.FilterType.BBOX;
import static de.bayern.gdi.gui.FeatureModel.FilterType.FILTER;
import static de.bayern.gdi.services.ServiceType.WFS_TWO;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
@Named
@Singleton
public class Controller {

    private static final Logger LOG = LoggerFactory.getLogger(Controller.class.getName());
    /** Application log. */
    private static final Logger APP_LOG = LoggerFactory.getLogger("Application_Log");

    /** DataBean. */
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
    private ServiceTypeSelectionController serviceTypeSelectionController;

    @FXML
    private FilterWfsSimpleController filterWfsSimpleController;

    @FXML
    private FilterWfsBasicController filterWfsBasicController;

    @FXML
    private FilterAtomController filterAtomController;

    @FXML
    private ProcessingChainController processingChainController;


    /**
     * Creates the Controller.
     */
    public Controller() {
        this.factory = new UIFactory();
        Processor.getInstance().addListener(new DownloadListener());
    }

    /**
     * Logs a message to the application log.
     *
     * @param msg Message to log
     */
    public static void logToAppLog(String msg) {
            APP_LOG.info(msg);
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
        processingChainController.validateChainContainerItems();
    }

    /**
     * Extracts the processing steps.
     *
     * @return all processing steps, may be <code>empty</code> but never <code>null</code>
     */
    public List<ProcessingStep> extractProcessingSteps() {
        List<ProcessingStep> steps = new ArrayList<>();
        Set<Node> parameter =
            processingChainController.getProcessingChainParameter();
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

    void extractStoredQuery() {
        ItemModel data = this.dataBean.getDatatype();
        if (data instanceof StoredQueryModel) {
            filterWfsSimpleController.setStoredQueryAttributes();
        }
    }


    void extractBoundingBox() {
        switch (this.dataBean.getServiceType()) {
            case ATOM:
                //in Atom the bboxes are given by the extend of every dataset
                break;
            case WFS_ONE:
            case WFS_TWO:
                String boundingBox = filterWfsBasicController.getBoundingBox();
                this.dataBean.addAttribute("bbox", boundingBox, "");
                break;
            default:
                break;
        }
    }

    void extractCql() {
        if (dataBean.isFilterType()) {
            String sqlInput = filterWfsBasicController.getCqlText();
            if (sqlInput != null && !sqlInput.isEmpty()) {
                this.dataBean.addAttribute("CQL", sqlInput, "");
            }
        }
    }

    boolean validateInput() {
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
            serviceTypeSelectionController.validate(fail);
            filterAtomController.validate(fail);
            filterWfsBasicController.validate(fail);
            filterWfsSimpleController.validate(fail);
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

    boolean validateCqlInput() {
        if (dataBean.isFilterType()) {
            String sqlInput = filterWfsBasicController.getCqlText();
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
        this.dataBean.reset();
        serviceTypeSelectionController.resetGui();
        filterWfsBasicController.resetGui();
        filterAtomController.resetGui();
        filterAtomController.setVisible(false);
        filterWfsSimpleController.setVisible(false);
        processingChainController.resetProcessingChainContainer();
    }

    /**
     * Collects the available service types.
     *
     * @param isWfs2
     *     <code>true</code> if the service is a WFS 2, <code>false</code> otherwise
     * @return the available service types. may be <code>empty</code> but never <code>null</code>
     */
    public ObservableList<ItemModel> collectServiceTypes(boolean isWfs2) {
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
            filterWfsBasicController.setExtent(extendWFS);
        }
        return types;
    }

    /**
     * Adds new stored queries to the data bean.
     *
     * @param types
     *     the stored queries to add, never <code>null</code>
     */
    public void addStoredQueries(ObservableList<ItemModel> types) {
        List<WFSMeta.StoredQuery> queries =
            dataBean.getWFSService().getStoredQueries();
        for (WFSMeta.StoredQuery s : queries) {
            types.add(new StoredQueryModel(s));
        }
    }

    /**
     * Select the service type.
     * @param data never <code>null</code>
     * @param type the type of the service
     * @param datasetAvailable <code>true</code> if a dataset is available, <code>false</code> otherwise
     */
    public void chooseServiceType(ItemModel data, ServiceType type, boolean datasetAvailable) {
        if (type == ServiceType.ATOM) {
            filterAtomController.initGui(data, datasetAvailable);
            enableAtomType();
        } else if (type == WFS_TWO) {
            if (data instanceof FeatureModel
                || data instanceof  OverallFeatureTypeModel
                || (!datasetAvailable
                    && downloadConfig.getServiceType() == "WFS2_BASIC")) {
                filterWfsBasicController.initGui(data);
                enableWfsBasic();
            } else if (data instanceof StoredQueryModel
                       || (!datasetAvailable
                           && downloadConfig.getServiceType().equals("WFS2_SIMPLE"))) {
                filterWfsSimpleController.initGui(data, datasetAvailable);
                enableWfsSimple();
            }
        }
    }

    private void enableWfsSimple() {
        filterAtomController.setVisible(false);
        filterWfsSimpleController.setVisible(true);
        filterWfsBasicController.setVisible(false);
    }

    private void enableWfsBasic() {
        filterWfsSimpleController.setVisible(false);
        filterAtomController.setVisible(false);
        filterWfsBasicController.setVisible(true);
    }

    private void enableAtomType() {
        filterWfsSimpleController.setVisible(false);
        filterWfsBasicController.setVisible(false);
        filterAtomController.setVisible(true);
    }

    /**
     * Set the DataBean and fill the UI with initial data objects.
     *
     * @param dataBean The DataBean object.
     */
    public void setDataBean(DataBean dataBean) {
        this.dataBean = dataBean;
        ObservableList<ServiceModel> servicesAsList = this.dataBean.getServicesAsList();
        serviceSelectionController.setServices(servicesAsList);

        ServiceSettings serviceSetting = Config.getInstance().getServices();
        catalogReachable = dataBean.getCatalogService() != null
                && ServiceChecker.isReachable(
                dataBean.getCatalogService().getUrl());
        URL url = null;
        try {
            url = new URL(serviceSetting.getWMSUrl());
        } catch (MalformedURLException e) {
            LOG.error(e.getMessage(), e);
        }
        if (url != null && filterWfsBasicController.isReachable(url)) {
            filterWfsBasicController.initMapHandler(serviceSetting);
            filterAtomController.initMapHandler(serviceSetting);
        } else {
            statusLogController.setStatusTextUI(I18n.format("status.wms-not-available"));
        }
        filterAtomController.setVisible(false);
        filterWfsSimpleController.setVisible(false);
        filterWfsBasicController.setVisible(false);
        processingChainController.setVisible(false);
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
     * Validates the process steps.
     */
    public void validateChainContainerItems() {
        processingChainController.validateChainContainerItems();
    }

    /**
     * Sets the processing steps.
     * @param steps to set
     */
    public void setProcessingSteps(List<DownloadConfig.ProcessingStep> steps) {
        processingChainController.setProcessingSteps(steps);
    }

    /**
     * Selects the service type.
     *
     * @param polygonID
     *     id of the selected polygon
     */
    public void selectServiceType(String polygonID) {
        serviceTypeSelectionController.selectServiceType(polygonID);
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
