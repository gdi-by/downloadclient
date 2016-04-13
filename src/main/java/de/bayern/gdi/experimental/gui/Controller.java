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


import de.bayern.gdi.experimental.services.WFStwo;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.Optional;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class Controller {

    // DataBean
    private DataBean dataBean;

    // View
    private View view;

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
            //Nada
        }
    }

    /**
     * Event Handler for the choose Service Button.
     */
    private class ServiceChooseButtonEventHandler implements
            EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            //Nada
            String serviceURL = null;
            String username = null;
            String password = null;
            if (view.getServiceList().getSelectionModel().getSelectedItems()
                    != null) {
                String serviceName =
                        view.getServiceList().
                                getSelectionModel().getSelectedItems().get(0);
                serviceURL = dataBean.getServiceURL(serviceName);
            } else if (view.getServiceURLfield().textProperty().getValue()
                    != null) {
                serviceURL =
                        view.getServiceURLfield().textProperty().getValue();
                if (view.getServiceUseAuthenticationCBX().isSelected()) {
                    username = view.getServiceUser().textProperty().getValue();
                    password = view.getServicePW().textProperty().getValue();
                }
                if (username != null && password != null) {
                }
            }
            if (serviceURL != null) {
                view.setStatusBarText("Check for Servicetype");
                //
                dataBean.setWebService(new WFStwo(serviceURL));
            } else {
                view.setStatusBarText("Could not determine URL");
            }
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

            closeConfirmation.setX(dataBean.getPrimaryStage().getX());
            closeConfirmation.setY(dataBean.getPrimaryStage().getY()
                    + dataBean.getPrimaryStage().getHeight());

            Optional<ButtonType> closeResponse =
                    closeConfirmation.showAndWait();
            if (!ButtonType.OK.equals(closeResponse.get())) {
                e.consume();
            }
        }
    }
}
