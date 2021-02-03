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

import de.bayern.gdi.gui.AtomItemModel;
import de.bayern.gdi.gui.CellTypes;
import de.bayern.gdi.gui.DataBean;
import de.bayern.gdi.gui.FeatureModel;
import de.bayern.gdi.gui.ItemModel;
import de.bayern.gdi.gui.MiscItemModel;
import de.bayern.gdi.services.Atom;
import de.bayern.gdi.services.ServiceType;
import de.bayern.gdi.utils.DownloadConfig;
import de.bayern.gdi.utils.I18n;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static de.bayern.gdi.gui.FeatureModel.FilterType.FILTER;
import static de.bayern.gdi.gui.GuiConstants.FX_BORDER_COLOR_NULL;
import static de.bayern.gdi.gui.GuiConstants.FX_BORDER_COLOR_RED;
import static de.bayern.gdi.gui.GuiConstants.STATUS_READY;
import static de.bayern.gdi.services.ServiceType.WFS_TWO;

/**
 * Service type selection controller.
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Named
@Singleton
public class ServiceTypeSelectionController {

    @Inject
    private Controller controller;

    @Inject
    private StatusLogController statusLogController;

    @Inject
    private FilterAtomController filterAtomController;

    @Inject
    private FilterWfsBasicController filterWfsBasicController;

    @Inject
    private FilterWfsSimpleController filterWfsSimpleController;

    @FXML
    private ComboBox<ItemModel> serviceTypeChooser;

    /**
     * Handle the service type selection.
     *
     * @param event
     *     The event
     */
    @FXML
    protected void handleServiceTypeSelect(ActionEvent event) {
        ItemModel item =
            this.serviceTypeChooser.
                                       getSelectionModel().getSelectedItem();
        if (item != null) {
            controller.dataBean.setDataType(item);
            controller.dataBean.setAttributes(new ArrayList<DataBean.Attribute>());
            chooseType(item);
        }
    }

    /**
     * Validates the user input.
     *
     * @param fail
     *     never <code>null</code>>
     */
    public void validate(Consumer<String> fail) {
        if (serviceTypeChooser.isVisible()
             && serviceTypeChooser.getValue() instanceof MiscItemModel) {
            fail.accept(I18n.format("gui.dataset"));
        }
    }

    /**
     * Resets GUI.
     */
    public void resetGui() {
        Platform.runLater(() ->
                               this.serviceTypeChooser.getItems().retainAll()
       );
        this.serviceTypeChooser.setStyle(FX_BORDER_COLOR_NULL);
    }

    /**
     * Load the passed {@link DownloadConfig}.
     *
     * @param downloadConf
     *     never <code>null</code>
     */
    public void loadDownloadConfig(DownloadConfig downloadConf) {
        serviceTypeChooser.getSelectionModel().select(0);
        if (downloadConf != null) {
            String dataset = downloadConf.getDataset();
            if (dataset != null) {
                boolean datasetAvailable = false;
                List<ItemModel> datasets = serviceTypeChooser.
                                                                 getItems();
                for (ItemModel iItem : datasets) {
                    if (isFeatureTypeToSelect(iItem, downloadConf)) {
                        serviceTypeChooser.
                                              getSelectionModel().select(iItem);
                        datasetAvailable = true;
                    }
                }
                if (!datasetAvailable) {
                    MiscItemModel errorItem = new MiscItemModel();
                    errorItem.setDataset(dataset);
                    errorItem.setItem(downloadConf.getDataset());
                    statusLogController.setStatusTextUI(I18n.format("gui.dataset-not-available"));
                    serviceTypeChooser.getItems().add(errorItem);
                    serviceTypeChooser.
                                          getSelectionModel().select(errorItem);
                }
            }
            setCellFactories();
            filterAtomController.setCellFactories();
            filterWfsBasicController.setCellFactories();
            loadGUIComponents();
        }
    }

    /**
     * Sets the service types.
     */
    public void setServiceTypes() {
        if (controller.dataBean.isWebServiceSet()) {
            switch (controller.dataBean.getServiceType()) {
            case WFS_ONE:
            case WFS_TWO:
                boolean isWfs2 = WFS_TWO.equals(controller.dataBean.getServiceType());
                ObservableList<ItemModel> types =
                    controller.collectServiceTypes(isWfs2);
                controller.addStoredQueries(types);
                serviceTypeChooser.getItems().retainAll();
                serviceTypeChooser.setItems(types);
                serviceTypeChooser.setValue(types.get(0));
                chooseType(serviceTypeChooser.getValue());
                break;
            case ATOM:
                List<Atom.Item> items =
                    controller.dataBean.getAtomService().getItems();
                ObservableList<ItemModel> opts = filterAtomController.chooseType(items);
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

    /**
     * Select the service type with the passed id.
     *
     * @param id
     *     the id of the service to select
     */
    public void selectServiceType(String id) {
        ObservableList<ItemModel> items =
            serviceTypeChooser.getItems();
        int i = 0;
        for (i = 0; i < items.size(); i++) {
            AtomItemModel item = (AtomItemModel) items.get(i);
            Atom.Item aitem = (Atom.Item) item.getItem();
            if (aitem.getID().equals(id)) {
                break;
            }
        }
        Atom.Item oldItem = (Atom.Item) serviceTypeChooser
            .getSelectionModel()
            .getSelectedItem().getItem();
        if (i < items.size()
            && !oldItem.getID().equals(id)) {
            serviceTypeChooser.setValue(items.get(i));
            chooseType(serviceTypeChooser.getValue());
        }
    }

    private void setCellFactories() {
        serviceTypeChooser.setCellFactory(list -> new CellTypes.ItemCell());
    }

    private void chooseType(ItemModel data) {
        ServiceType type = controller.dataBean.getServiceType();
        boolean datasetAvailable = false;
        if (data instanceof MiscItemModel) {
            serviceTypeChooser.setStyle(FX_BORDER_COLOR_RED);
            statusLogController.setStatusTextUI(I18n.format("gui.dataset-not-available"));
        } else {
            serviceTypeChooser.setStyle(FX_BORDER_COLOR_NULL);
            datasetAvailable = true;
            statusLogController.setStatusTextUI(I18n.format(STATUS_READY));
        }
        controller.chooseServiceType(data, type, datasetAvailable);
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
        switch (controller.downloadConfig.getServiceType()) {
        case "ATOM":
            filterAtomController.initialiseGuiComponents();
            break;
        case "WFS2_BASIC":
            filterWfsBasicController.initialiseCrsChooser();
            filterWfsBasicController.initializeBoundingBox();
            filterWfsBasicController.initializeDataFormatChooser();
            break;
        case "WFS2_SIMPLE":
            filterWfsSimpleController.initialiseGuiComponents();
            break;
        case "WFS2_SQL":
            filterWfsBasicController.initialiseCrsChooser();
            filterWfsBasicController.initializeDataFormatChooser();
            filterWfsBasicController.initializeCqlTextArea();
            break;
        default:
            statusLogController.setStatusTextUI(I18n.format("status.config.invalid-xml"));
            break;
        }
        List<DownloadConfig.ProcessingStep> steps =
            controller.downloadConfig.getProcessingSteps();
        controller.setProcessingSteps(steps);
    }

}
