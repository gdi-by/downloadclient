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

import de.bayern.gdi.gui.UIFactory;
import de.bayern.gdi.model.MIMEType;
import de.bayern.gdi.model.MIMETypes;
import de.bayern.gdi.model.ProcessingStepConfiguration;
import de.bayern.gdi.config.Config;
import de.bayern.gdi.config.DownloadConfig;
import de.bayern.gdi.utils.I18n;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static de.bayern.gdi.gui.GuiConstants.FX_BORDER_COLOR_NULL;
import static de.bayern.gdi.gui.GuiConstants.FX_BORDER_COLOR_RED;
import static de.bayern.gdi.gui.GuiConstants.GUI_PROCESS_FORMAT_NOT_FOUND;
import static de.bayern.gdi.gui.GuiConstants.GUI_PROCESS_NOT_COMPATIBLE;
import static de.bayern.gdi.gui.GuiConstants.GUI_PROCESS_NO_FORMAT;
import static de.bayern.gdi.gui.GuiConstants.OUTPUTFORMAT;
import static de.bayern.gdi.gui.GuiConstants.STATUS_READY;

/**
 * Processing chain controller.
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Named
@Singleton
public class ProcessingChainController {

    @Inject
    private Controller controller;

    @Inject
    private StatusLogController statusLogController;

    @FXML
    private VBox chainContainer;

    @FXML
    private CheckBox chkChain;

    @FXML
    private HBox processStepContainter;

    private UIFactory factory = new UIFactory();

    /**
     * Handle events on the process Chain Checkbox.
     *
     * @param event
     *     the event
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
     * Handle the dataformat selection.
     *
     * @param event
     *     The event
     */
    @FXML
    protected void handleAddChainItem(ActionEvent event) {
        factory.addChainAttribute(chainContainer,
                                   this::validateChainContainerItems);
        validateChainContainerItems();
    }

    /**
     * Sets visibility.
     * @param isVisible shows container when true otherwise is hidden
     */
    public void setVisible(boolean isVisible) {
        this.processStepContainter.setVisible(isVisible);
    }

    /**
     * Sets processing steps.
     * @param steps list of steps
     */
    public void setProcessingSteps(List<DownloadConfig.ProcessingStep> steps) {
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

    /**
     * Returns processing chain parameter.
     * @return set of parameter
     */
    public Set<Node> getProcessingChainParameter() {
        if (!this.chkChain.isSelected()) {
            Collections.emptyList();
        }
        return this.chainContainer.lookupAll("#process_parameter");
    }

    /**
     * Resets all marks at the processing chain container, items are kept.
     */
    public void resetProcessingChainContainer() {
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
     * Validates all items in processing chain container.
     */
    public void validateChainContainerItems() {

        boolean allValid = true;
        for (Node o : chainContainer.getChildren()) {
            if (o instanceof VBox) {
                VBox v = (VBox) o;
                HBox hbox = (HBox) v.getChildren().get(0);
                Node cBox = hbox.getChildren().get(0);
                if (cBox instanceof ComboBox
                     && !validateChainContainer((ComboBox) cBox)) {
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
     * Validates the chain items of a ComboBox
     * and marks the box according to the chosen item.
     *
     * @param box
     *     Item to validate
     * @return True if chosen item is valid, else false
     */
    private boolean validateChainContainer(ComboBox box) {
        String format = controller.dataBean.getAttributeValue(OUTPUTFORMAT);
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
}
