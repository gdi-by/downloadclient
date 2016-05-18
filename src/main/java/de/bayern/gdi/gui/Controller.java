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

import de.bayern.gdi.services.Atom;
import de.bayern.gdi.services.WFSOne;
import de.bayern.gdi.services.WFSTwo;
import de.bayern.gdi.services.WebService;

import de.bayern.gdi.utils.ServiceChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import java.util.logging.Level;
import java.util.logging.Logger;

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

import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import org.opengis.feature.type.AttributeType;


/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class Controller {

    // DataBean
    private DataBean dataBean;

    // View
    private View view;

    private static final Logger log
            = Logger.getLogger(WMSMap.class.getName());
    /**
     * Creates the Conroller.
     * @param dataBean the model
     */
    public Controller(DataBean dataBean) {
        this.dataBean = dataBean;
        this.view = new View();
        this.view.setServiceListEntries(this.dataBean.getServicesAsList());

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
        view.getServiceList().
                setOnMouseClicked(new MouseClickedOnServiceList());

        // Register Listener
        view.getServiceSearch().textProperty().
                addListener(new SearchServiceListChangeListener());

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
                case WFSTwo:
                    dataBean.setServiceTypes(
                            dataBean.getWebService().getStoredQueries());
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
            Map map = new HashMap<String, String>();
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
                    case WFSTwo:
                        map = dataBean.getWebService()
                                .getParameters(choosenType);
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
                        WebService.Type st =
                                ServiceChecker.checkService(serviceURL,
                                        dataBean.getBase64EncAuth());
                        WebService ws = null;
                        //Check for null, since switch breaks on a null value
                        if (st == null) {
                            log.log(Level.WARNING, "Could not determine "
                                    + "Service Type" , st);
                            Platform.runLater(() -> {
                                view.setStatusBarText("Could not determine "
                                        + "Service Type");
                            });
                        } else {
                            switch (st) {
                                case Atom:
                                    Platform.runLater(() -> {
                                        view.setStatusBarText("Found Atom "
                                                + "Service");
                                    });
                                    ws = new Atom(serviceURL);
                                    break;
                                case WFSOne:
                                    Platform.runLater(() -> {
                                        view.setStatusBarText("Found WFSOne "
                                                + "Service");
                                    });
                                    ws = new WFSOne(serviceURL, dataBean
                                            .getUserName(), dataBean
                                            .getPassword());
                                    break;
                                case WFSTwo:
                                    Platform.runLater(() -> {
                                        view.setStatusBarText("Found WFSTwo "
                                                + "Service");
                                    });
                                    ws = new WFSTwo(serviceURL, dataBean
                                            .getUserName(), dataBean
                                            .getPassword());
                                    break;
                                default:
                                    log.log(Level.WARNING,
                                        "Could not determine URL" , st);
                                    Platform.runLater(() -> {
                                        view.setStatusBarText("Could not "
                                                + "determine URL");
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
                            view.setStatusBarText("Ready");
                        });
                    } else {
                        Platform.runLater(() -> {
                            view.setStatusBarText("Could not determine URL");
                        });
                    }
                    view.getScene().setCursor(Cursor.DEFAULT);
                    return 0;
                }
            };
                Thread th = new Thread(task);
                view.setStatusBarText("Calling Service to get Infos");
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
                    "Are you sure you want to exit?"
            );
            Button exitButton
                    = (Button) closeConfirmation.getDialogPane().lookupButton(
                        ButtonType.OK
                    );
            exitButton.setText("Exit");
            closeConfirmation.setHeaderText("Confirm Exit");
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
}
