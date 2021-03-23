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

import de.bayern.gdi.gui.ProgressDialog;
import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.processor.ConverterException;
import de.bayern.gdi.processor.DownloadStepConverter;
import de.bayern.gdi.processor.Processor;
import de.bayern.gdi.processor.job.JobList;
import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Misc;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static de.bayern.gdi.gui.GuiConstants.USER_DIR;

/**
 * Button bar controller.
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Named
@Singleton
public class ButtonBarController {

    private static final Logger LOG = LoggerFactory.getLogger(ButtonBarController.class.getName());

    @Inject
    private Controller controller;

    @Inject
    private MenuBarController menuBarController;

    @Inject
    private StatusLogController statusLogController;

    @Inject
    private FXMLLoader fxmlLoader;

    @Inject
    private DownloadListener downloadListener;

    @FXML
    private Button buttonDownload;

    @FXML
    private Button buttonClose;

    private Future<?> jobExecution;

    /**
     * Start the download.
     *
     * @param event
     *     The event
     */
    @FXML
    protected void handleDownload(ActionEvent event) {
        controller.extractStoredQuery();
        controller.extractBoundingBox();
        controller.extractCql();
        if (controller.validateInput() && controller.validateCqlInput()) {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle(I18n.getMsg("gui.save-dir"));
            if (controller.downloadConfig != null
                 && controller.downloadConfig.getDownloadPath() != null) {
                try {
                    File dir = new File(controller.downloadConfig.getDownloadPath());
                    if (dir.exists()) {
                        dirChooser.setInitialDirectory(dir);
                    }
                } catch (Exception e) {
                    LOG.warn(e.getLocalizedMessage());
                }
            }
            File selectedDir = dirChooser.showDialog(controller.getPrimaryStage());
            if (selectedDir == null) {
                return;
            }
            statusLogController.setStatusTextUI(
                I18n.format("status.download.started"));

            controller.dataBean.setProcessingSteps(controller.extractProcessingSteps());
            String savePath = selectedDir.getPath();
            try {
                this.buttonDownload.setDisable(true);
                DownloadStep ds = controller.dataBean.convertToDownloadStep(savePath);
                DownloadStepConverter dsc = new DownloadStepConverter(
                    controller.dataBean.getSelectedService().getUsername(),
                    controller.dataBean.getSelectedService().getPassword());
                ProgressDialog progressDialog = new ProgressDialog(controller);
                dsc.addListener(progressDialog);
                JobList jl = dsc.convert(ds);
                Processor p = new Processor(jl);
                p.addListeners(downloadListener, progressDialog);
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                openProgressDialog(progressDialog, selectedDir);
                try {
                    this.jobExecution = executorService.submit(p);
                } finally {
                    executorService.shutdown();
                }
            } catch (ConverterException ce) {
                statusLogController.setStatusTextUI(ce.getMessage());
                Controller.logToAppLog(ce.getMessage());
            } finally {
                this.buttonDownload.setDisable(false);
            }
        }
    }

    /**
     * Handle config saving.
     *
     * @param event
     *     The event.
     */
    @FXML
    protected void handleSaveConfig(ActionEvent event) {
        controller.extractStoredQuery();
        controller.extractBoundingBox();
        controller.extractCql();
        if (controller.validateInput() && controller.validateCqlInput()) {
            FileChooser fileChooser = new FileChooser();
            DirectoryChooser dirChooser = new DirectoryChooser();
            File downloadDir;
            File initDir;

            dirChooser.setTitle(I18n.getMsg("gui.save-dir"));

            if (controller.downloadConfig == null) {
                downloadDir = dirChooser.showDialog(controller.getPrimaryStage());
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
                    = new File(controller.downloadConfig.getDownloadPath());
                if (!downloadInitDir.exists()) {
                    downloadInitDir = new File(System.getProperty(USER_DIR));
                }
                dirChooser.setInitialDirectory(downloadInitDir);
                downloadDir = dirChooser.showDialog(controller.getPrimaryStage());

                String path = controller.downloadConfig.getFile().getAbsolutePath();
                path = path.substring(0, path.lastIndexOf(File.separator));
                initDir = new File(path);
                fileChooser.setInitialFileName(controller.downloadConfig.getFile()
                                                                         .getName());
            }
            fileChooser.setInitialDirectory(initDir);
            FileChooser.ExtensionFilter xmlFilter =
                new FileChooser.ExtensionFilter("xml files (*.xml)",
                                                 "*.xml");
            fileChooser.getExtensionFilters().add(xmlFilter);
            fileChooser.setSelectedExtensionFilter(xmlFilter);
            fileChooser.setTitle(I18n.getMsg("gui.save-conf"));
            File configFile = fileChooser.showSaveDialog(controller.getPrimaryStage());
            if (configFile == null) {
                return;
            }

            if (!configFile.toString().endsWith(".xml")) {
                configFile = new File(configFile.toString() + ".xml");
            }

            controller.dataBean.setProcessingSteps(controller.extractProcessingSteps());

            String savePath = downloadDir.getPath();
            DownloadStep ds = controller.dataBean.convertToDownloadStep(savePath);
            try {
                ds.write(configFile);
            } catch (IOException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        }
    }

    /**
     * Handles the closing event.
     * @param event the action event
     */
    @FXML
    protected void handleCloseApp(ActionEvent event) {
        Stage stage = (Stage) buttonClose.getScene().getWindow();
        menuBarController.closeApp(stage);
    }

    private void cancelJobExecution() {
        if (jobExecution != null && !jobExecution.isDone()) {
            // TODO: Job is not cancelled
            jobExecution.cancel(true);
        }
    }

    private void openProgressDialog(ProgressDialog dialog, File selectedDir) {
        Platform.runLater(
            () -> {
                Optional<ButtonType> buttonType = dialog.showAndWait();
                buttonType.ifPresent(bt -> {
                    if (buttonType.get() == ButtonType.CANCEL) {
                        this.cancelJobExecution();
                    } else if (buttonType.get() == ProgressDialog.OPEN_FILES) {
                        openFileSystemBrowser(selectedDir);
                    }
                });
            }
        );
    }

    private void openFileSystemBrowser(File selectedDir) {
        if (Desktop.isDesktopSupported()) {
            new Thread(() -> {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(selectedDir.toURI());
                } catch (IOException e) {
                    LOG.error("File Browser with the passed uri " + selectedDir + " could not be opened.", e);
                }
            }).start();
        }
    }

}
