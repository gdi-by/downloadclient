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

import de.bayern.gdi.config.ApplicationSettings;
import de.bayern.gdi.config.Config;
import de.bayern.gdi.config.Credentials;
import de.bayern.gdi.gui.ServiceModel;
import de.bayern.gdi.services.Atom;
import de.bayern.gdi.services.Service;
import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.services.WFSMetaExtractor;
import de.bayern.gdi.config.DownloadConfig;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.ServiceChecker;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Named
@Singleton
public class ServiceSelectionController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceSelectionController.class.getName());

    private static final String STATUS_SERVICE_BROKEN = "status.service.broken";

    private static final String STATUS_READY = "status.ready";

    @Inject
    private Controller controller;

    @Inject
    private StatusLogController statusLogController;

    @Inject
    private ServiceTypeSelectionController serviceTypeSelectionController;

    @Inject
    private ProcessingChainController processingChainController;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private ProgressIndicator progressSearch;

    @FXML
    private ListView serviceList;

    @FXML
    private TextField serviceURL;

    @FXML
    private Button serviceSelectionBt;

    @FXML
    private CheckBox serviceAuthenticationCbx;

    @FXML
    private TextField serviceUser;

    @FXML
    private TextField servicePW;

    /**
     * Handle the service selection button event.
     *
     * @param event
     *     The mouse click event.
     */
    @FXML
    protected void handleServiceSelectButton(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            controller.downloadConfig = null;
            doSelectService();
        }
    }

    /**
     * Handle the service selection.
     *
     * @param event
     *     The mouse click event.
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
                    serviceSelectionBt.setDisable(true);
                    serviceURL.getScene().setCursor(Cursor.WAIT);
                    statusLogController.setStatusTextUI(
                        I18n.format("status.checking-auth"));
                    Task task = new Task() {
                        protected Integer call() {
                            try {
                                selectService(serviceModel.getItem());
                                return 0;
                            } finally {
                                serviceSelectionBt.setDisable(false);
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
                controller.resetGui();
            }
        }
    }

    /**
     * Handle authentication required selection.
     *
     * @param event
     *     the event
     */
    @FXML
    protected void handleAuthenticationRequired(ActionEvent event) {
        boolean flag = !this.serviceAuthenticationCbx.isSelected();
        this.serviceUser.setDisable(flag);
        this.servicePW.setDisable(flag);
    }

    /**
     * Handle search button clicks.
     * Hide search button and start search
     *
     * @param event
     *     the event
     */
    @FXML
    protected void handleSearchButtonClick(MouseEvent event) {
        handleSearch(null);
    }

    /**
     * Handle search and filter the service list.
     *
     * @param event
     *     the event
     */
    @FXML
    protected void handleSearch(KeyEvent event) {
        if (!controller.catalogReachable) {
            statusLogController.setStatusTextUI(I18n.getMsg("status.catalog-not-available"));
        }

        String currentText = this.searchField.getText();
        this.serviceList.getItems().clear();
        controller.dataBean.resetCatalogLists();
        if (currentText == null || currentText.isEmpty()) {
            setServices(controller.dataBean.getServicesAsList());
        }

        String searchValue = currentText == null
                             ? ""
                             : currentText.toUpperCase();

        ObservableList<ServiceModel> subentries
            = FXCollections.observableArrayList();
        ObservableList<ServiceModel> all = controller.dataBean.getServicesAsList();
        for (ServiceModel entry : all) {
            boolean match
                = entry.getName().toUpperCase().contains(searchValue);
            if (match) {
                subentries.add(entry);
            }
        }
        if (currentText != null && currentText.length() > 2) {
            Task task = new Task() {
                @Override
                protected Integer call()
                    throws Exception {
                    Platform.runLater(() -> {
                        searchButton.setVisible(false);
                        searchButton.setManaged(false);
                        progressSearch.setVisible(true);
                        progressSearch.setManaged(true);
                    });
                    if (controller.catalogReachable) {
                        List<Service> catalog =
                            controller.dataBean.getCatalogService()
                                               .getServicesByFilter(currentText);
                        for (Service entry : catalog) {
                            controller.dataBean.addCatalogServiceToList(entry);
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
            if (controller.catalogReachable) {
                statusLogController.setStatusTextUI(I18n.getMsg("status.calling-service"));
            }
            th.setDaemon(true);
            th.start();
        }
        setServices(subentries);
    }

    /**
     * Initialise the GUI with the passed url and config.
     *
     * @param url
     *     never <code>null</code>
     * @param downloadConfig
     *     never <code>null</code>
     */
    public void loadDownloadConfig(String url, DownloadConfig downloadConfig) {
        this.serviceURL.setText(url);
        doSelectService(downloadConfig);
    }

    /**
     * Sets the services.
     *
     * @param servicesAsList
     *     may be <code>empty</code>> but never <code>null</code>
     */
    public void setServices(ObservableList<ServiceModel> servicesAsList) {
        this.serviceList.setItems(servicesAsList);
    }

    /**
     * Resets GUI.
     */
    public void resetGui() {
        this.progressSearch.setVisible(false);
        this.serviceUser.setDisable(true);
        this.servicePW.setDisable(true);
    }

    private void clearUserNamePassword() {
        this.serviceUser.setText("");
        this.servicePW.setText("");
    }

    private void setUserNamePasswordFromServiceOrConfig(Service selectedService) {
        ApplicationSettings settings = Config
            .getInstance()
            .getApplicationSettings();
        Credentials credentials = settings.getCredentials();
        if (selectedService.getUsername() != null && !selectedService.getUsername().isEmpty()) {
            this.serviceUser.setText(selectedService.getUsername());
            this.servicePW.setText(selectedService.getPassword());
        } else if (credentials != null) {
            this.serviceUser.setText(credentials.getUsername());
            this.servicePW.setText(credentials.getPassword());
        }
    }

    /**
     * Select a service according to service url textfield.
     */
    private void doSelectService() {
        doSelectService(null);
    }

    /**
     * Select a service according to service url textfield.
     *
     * @param downloadConf
     *     Loaded download config, null if a service is chosen
     *     from an URL or the service List
     */
    private void doSelectService(DownloadConfig downloadConf) {
        LOG.info("Using download config: " + downloadConf);
        controller.dataBean.resetSelectedService();
        serviceSelectionBt.setDisable(true);
        serviceURL.getScene().setCursor(Cursor.WAIT);
        serviceURL.setDisable(true);
        controller.resetGui();
        new Thread(() -> {
            try {
                ObservableList selectedItems = serviceList.
                                                              getSelectionModel().getSelectedItems();
                ServiceModel serviceModel = selectedItems.isEmpty() ? null
                                                                    : (ServiceModel) selectedItems.get(0);
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
                    LOG.info("Connecting " + sURL + "...");
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
                    statusLogController.setStatusTextUI(
                        I18n.format("status.service-timeout"));
                    controller.dataBean.setSelectedService(null);
                    serviceSelectionBt.setDisable(false);
                    serviceURL.setDisable(false);
                    serviceURL.getScene().setCursor(Cursor.DEFAULT);
                    return;
                }
                serviceSelectionBt.setDisable(true);
                serviceURL.getScene().setCursor(Cursor.WAIT);
                statusLogController.setStatusTextUI(
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
                            persistUsernameAndPasswordInSettingsXml(finalService);
                            return 0;
                        } finally {
                            serviceSelectionBt.setDisable(false);
                            serviceURL.getScene()
                                      .setCursor(Cursor.DEFAULT);
                            serviceURL.setDisable(false);
                            processingChainController.validateChainContainerItems();
                        }
                    }
                };
                Thread th = new Thread(task);
                th.setDaemon(true);
                th.start();
            } catch (MalformedURLException e) {
                statusLogController.setStatusTextUI(
                    I18n.format("status.no-url"));
                LOG.error(e.getMessage(), e);
                serviceSelectionBt.setDisable(false);
                serviceURL.getScene()
                          .setCursor(Cursor.DEFAULT);
                serviceURL.setDisable(false);
            }
        }).start();
    }

    private boolean selectService(Service service) {
        LOG.info("User selected: " + service.toString());
        if (ServiceChecker.isReachable(service.getServiceURL())) {
            try {
                service.load();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                Platform.runLater(() ->
                                       statusLogController.setStatusTextUI(
                                           I18n.format(STATUS_SERVICE_BROKEN))
               );
                return false;
            }
        } else {
            Platform.runLater(() ->
                                   statusLogController.setStatusTextUI(
                                       I18n.format("status.service-not-available"))
           );
            return false;
        }
        if (controller.dataBean.getSelectedService() != null
             && controller.dataBean.getSelectedService().equals(service)) {
            Platform.runLater(() ->
                                   statusLogController.setStatusTextUI(
                                       I18n.format(STATUS_READY))
           );
            return true;
        }
        controller.dataBean.setSelectedService(service);
        Platform.runLater(() -> {
            controller.resetGui();
            this.serviceURL.setText(
                controller.dataBean.getSelectedService().getServiceURL().toString()
           );
        });
        //Check if Username and Password are given
        if (((controller.dataBean.getSelectedService().getUsername() != null
                 && controller.dataBean.getSelectedService().getPassword() != null)
               || (controller.dataBean.getSelectedService().getUsername().isEmpty()
                    && controller.dataBean.getSelectedService().getPassword().isEmpty()))
             && controller.dataBean.getSelectedService().isRestricted()) {
            Platform.runLater(() -> {
                statusLogController.setStatusTextUI(
                    I18n.format("status.service-needs-auth"));
                this.serviceAuthenticationCbx.setSelected(true);
                this.serviceUser.setDisable(false);
                this.servicePW.setDisable(false);
                setUserNamePasswordFromServiceOrConfig(controller.dataBean.getSelectedService());
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
        if (controller.dataBean.getSelectedService().getServiceType() == null) {
            Platform.runLater(() ->
                                   statusLogController.setStatusTextUI(
                                       I18n.format(STATUS_SERVICE_BROKEN))
           );
            return false;
        }

        Platform.runLater(() ->
                               statusLogController.setStatusTextUI(
                                   I18n.format(STATUS_READY))
       );
        return true;
    }

    /**
     * Use selection to request the service data and fill the UI.
     *
     * @param downloadConf
     *     Loaded download config, null if service
     *     was chosen from an URL or the service list
     */
    private void chooseSelectedService(DownloadConfig downloadConf) {
        switch (controller.dataBean.getSelectedService().getServiceType()) {
        case ATOM:
            Platform.runLater(() ->
                                   statusLogController.setStatusTextUI(
                                       I18n.getMsg("status.type.atom"))
           );
            Atom atom = null;
            try {
                atom = new Atom(
                    controller.dataBean.getSelectedService()
                                       .getServiceURL().toString(),
                    controller.dataBean.getSelectedService().getUsername(),
                    controller.dataBean.getSelectedService().getPassword());
            } catch (IllegalArgumentException
                | URISyntaxException
                | ParserConfigurationException
                | IOException e) {
                LOG.error(e.getMessage(), e);
                Platform.runLater(() ->
                                       statusLogController.setStatusTextUI(
                                           I18n.getMsg(STATUS_SERVICE_BROKEN)
                                      )
               );
                controller.resetGui();
                return;
            } finally {
                controller.dataBean.setAtomService(atom);
            }
            break;
        case WFS_ONE:
            Platform.runLater(() ->
                                   statusLogController.setStatusTextUI(
                                       I18n.getMsg("status.type.wfsone"))
           );
            WFSMetaExtractor wfsOne =
                new WFSMetaExtractor(
                    controller.dataBean.getSelectedService()
                                       .getServiceURL().toString(),
                    controller.dataBean.getSelectedService().getUsername(),
                    controller.dataBean.getSelectedService().getPassword());
            WFSMeta metaOne = null;
            try {
                metaOne = wfsOne.parse();
            } catch (IOException
                | URISyntaxException e) {
                LOG.error(e.getMessage(), e);
                Platform.runLater(() ->
                                       statusLogController.setStatusTextUI(
                                           I18n.getMsg(STATUS_SERVICE_BROKEN)
                                      )
               );
            } finally {
                controller.dataBean.setWFSService(metaOne);
            }
            break;
        case WFS_TWO:
            Platform.runLater(() ->
                                   statusLogController.setStatusTextUI(
                                       I18n.getMsg("status.type.wfstwo"))
           );
            WFSMetaExtractor extractor =
                new WFSMetaExtractor(
                    controller.dataBean.getSelectedService()
                                       .getServiceURL().toString(),
                    controller.dataBean.getSelectedService().getUsername(),
                    controller.dataBean.getSelectedService().getPassword());
            WFSMeta meta = null;
            try {
                meta = extractor.parse();
            } catch (IOException
                | URISyntaxException e) {
                LOG.error(e.getMessage(), e);
                Platform.runLater(() ->
                                       statusLogController.setStatusTextUI(
                                           I18n.getMsg(STATUS_SERVICE_BROKEN))
               );

            } finally {
                controller.dataBean.setWFSService(meta);
            }
            break;
        default:
            LOG.warn(
                "Could not determine URL",
                controller.dataBean.getSelectedService());
            Platform.runLater(() ->
                                   statusLogController.setStatusTextUI(I18n.getMsg("status.no-url"))
           );
            break;
        }
        if (controller.dataBean.isWebServiceSet()) {
            Platform.runLater(serviceTypeSelectionController::setServiceTypes);
        } else {
            return;
        }
        Platform.runLater(() -> {
            serviceTypeSelectionController.loadDownloadConfig(downloadConf);
            statusLogController.setStatusTextUI(I18n.getMsg(STATUS_READY));
        });
        return;
    }

    private void persistUsernameAndPasswordInSettingsXml(Service service) {
        ApplicationSettings settings = Config
            .getInstance()
            .getApplicationSettings();
        if ((service.getUsername() != null
            && !service.getUsername().isEmpty())) {
            Credentials credentials = new Credentials(service.getUsername(), service.getPassword());
            settings.persistCredentials(credentials);
        }
    }

}
