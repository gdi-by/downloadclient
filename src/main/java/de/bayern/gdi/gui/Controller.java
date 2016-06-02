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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengis.feature.type.AttributeType;

import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.processor.ConverterException;
import de.bayern.gdi.processor.DownloadStepConverter;
import de.bayern.gdi.processor.DownloadStepFactory;
import de.bayern.gdi.processor.JobList;
import de.bayern.gdi.processor.Processor;
import de.bayern.gdi.processor.ProcessorEvent;
import de.bayern.gdi.processor.ProcessorListener;
import de.bayern.gdi.services.Atom;
import de.bayern.gdi.services.ServiceType;
import de.bayern.gdi.services.WFSOne;
import de.bayern.gdi.services.WFSTwo;
import de.bayern.gdi.services.WebService;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.ServiceChecker;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;


/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class Controller {

    // DataBean
    private DataBean dataBean;

    // View
    private View view;

    private static final Logger log
            = Logger.getLogger(Controller.class.getName());
    /**
     * Creates the Controller.
     * @param dataBean the model
     */
    public Controller(DataBean dataBean) {
        this.dataBean = dataBean;
        this.view = new View();
        this.view.setServiceListEntries(this.dataBean.getServicesAsList());
        this.view.setCatalogueServiceNameLabelText(
                I18n.getMsg("gui.catalogue") + ": "
                + this.dataBean.getCatalogService().getProviderName());

        // Register Event Handler
        view.getQuitMenuItem().
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
        */

        // Register Listener
        view.getServiceSearch().textProperty().
                addListener(new SearchServiceListChangeListener());

        Processor.getInstance().addListener(new DownloadListener());

        // stage overrides
        this.dataBean.getPrimaryStage().
                setOnCloseRequest(new ConfirmCloseEventHandler());

    }

    /**
     * shows the view.
     */
    public void show() {
        view.show(dataBean.getPrimaryStage());
    }

    /**
     * sets the Service Types.
     */
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
            view.setTypes(dataBean.getServiceTypes());
        }
    }

    /**
     * sets the Service Types Attributes.
     * @param map the Map of Attributes
     */
    public void setServiceAttributes(Map<String, String> map) {
        if (dataBean.isWebServiceSet()) {
            view.setAttributes(map);
            setWMSMap(this.dataBean.getWmsUrl(), this.dataBean.getWmsName());
        }
    }

    private void setWMSMap(String wmsUrl, String wmsName) {
        view.setWMSMap(wmsUrl, wmsName);
    }

    //+++++++++++++++++++++++++++++++++++++++++++++
    // Listener
    //+++++++++++++++++++++++++++++++++++++++++++++

    /**
     * listener for changes in search field, so the list can be searched.
     */
    private class SearchServiceListChangeListener
            implements ChangeListener {
        @Override
        public void changed(ObservableValue observable, Object oldVal,
                            Object newVal) {
            searchServiceList((String) oldVal, (String) newVal);
        }
        public void searchServiceList(String oldVal, String newVal) {
            if (oldVal != null && (newVal.length() < oldVal.length())) {
                view.getServiceList().setItems(view.getServiceListEntries());
            }
            String value = newVal.toUpperCase();
            ObservableList<String> subentries
                    = FXCollections.observableArrayList();
            Map<String, String> catalog = dataBean.getCatalogService()
                    .getServicesByFilter(newVal);
            for (Map.Entry<String, String> entry: catalog.entrySet()) {
                view.addServiceToList(entry.getKey());
                dataBean.addServiceToList(entry.getKey(), entry.getValue());
            }
            for (Object entry : view.getServiceList().getItems()) {
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
            view.getServiceList().setItems(subentries);
        }
    }

    //+++++++++++++++++++++++++++++++++++++++++++++
    // Events
    //+++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Event Handler for choosing a type.
     */
    private class ChooseTypeEventHandler
        implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            Map<String, String> map = new HashMap<String, String>();
            if (view.getTypeComboBox().getSelectionModel().getSelectedItem()
                    != null) {
                String choosenType =
                        view.getTypeComboBox().getSelectionModel()
                        .getSelectedItem()
                        .toString();
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
     */
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
     */
    private class LoadMenuItemEventHandler
            implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            /* TODO: Implent this function in a way so the configuration File
             * fills the Frontend with the Informtaion. So all fields in the
             * frontend should be filled with the Information from the config
             * file, so you can check and verify your settings before clicking
             * "download"
             */
            FileChooser configFileChooser = new FileChooser();
            configFileChooser.setTitle(I18n.getMsg("gui.load-conf"));

            File configFile = configFileChooser.showOpenDialog(
                    dataBean.getPrimaryStage());
            if (configFile == null) {
                return;
            }
            try {
                DownloadStep ds = DownloadStep.read(configFile);
                FileChooser downloadFileChooser = new FileChooser();
                downloadFileChooser.setTitle(I18n.getMsg("gui.save-conf"));
                downloadFileChooser.setInitialDirectory(new File(ds.getPath()));
                File downloadFile = downloadFileChooser.showSaveDialog(
                        dataBean.getPrimaryStage());
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
     */
    private class SaveMenuItemEventHandler
            implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            FileChooser downloadFileChooser = new FileChooser();
            downloadFileChooser.setTitle(I18n.getMsg("gui.save-file"));
            File downloadFile = downloadFileChooser.showSaveDialog(
                    dataBean.getPrimaryStage());
            if (downloadFile == null) {
                return;
            }
            FileChooser configFileChooser = new FileChooser();
            configFileChooser.setTitle(I18n.getMsg("gui.save-conf"));
            File configFile = configFileChooser.showSaveDialog(
                    dataBean.getPrimaryStage());
            if (configFile == null) {
                return;
            }
            String savePath = downloadFile.getPath();
            DownloadStepFactory dsf = DownloadStepFactory.getInstance();
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
     */
    private class QuitMenuItemEventHandler
            implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            dataBean.getPrimaryStage().fireEvent(
                            new WindowEvent(
                                    dataBean.getPrimaryStage(),
                                    WindowEvent.WINDOW_CLOSE_REQUEST
                            )
            );
        }
    }

    /**
     * Event Handler for resetting the programm.
     */
    private class ResetMenuItemEventHandler
            implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            view.reset();
        }
    }

    /**
     * Event Handler for Downloading.
     */
    private class DownloadButtonEventHandler
            implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle(I18n.getMsg("gui.save-dir"));
            //fileChooser.getExtensionFilters().addAll();
            File selectedDir = dirChooser.showDialog(
                    dataBean.getPrimaryStage());
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
     */
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
     */
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
            closeConfirmation.initOwner(dataBean.getPrimaryStage());

            Optional<ButtonType> closeResponse =
                    closeConfirmation.showAndWait();
            if (!ButtonType.OK.equals(closeResponse.get())) {
                e.consume();
            }
        }
    }

    /**
     *  Eventhandler for mouse events on map.
     */
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
            view.setStatusBarText(getMessage());
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
