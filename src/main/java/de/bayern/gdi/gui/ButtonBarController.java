package de.bayern.gdi.gui;

import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.processor.ConverterException;
import de.bayern.gdi.processor.DownloadStepConverter;
import de.bayern.gdi.processor.JobList;
import de.bayern.gdi.processor.Processor;
import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Misc;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import static de.bayern.gdi.gui.Controller.USER_DIR;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ButtonBarController {

    private static final Logger log
        = LoggerFactory.getLogger( ButtonBarController.class.getName() );

    @Inject
    private Controller controller;

    @Inject
    private MenuBarController menuBarController;

    @Inject
    private StatusLogController statusLogController;

    @FXML
    private Button buttonDownload;

    @FXML
    private Button buttonSaveConfig;

    @FXML
    private Button buttonClose;

    /**
     * Start the download.
     *
     * @param event
     *     The event
     */
    @FXML
    protected void handleDownload( ActionEvent event ) {
        controller.extractStoredQuery();
        controller.extractBoundingBox();
        controller.extractCql();
        if ( controller.validateInput() && controller.validateCqlInput() ) {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle( I18n.getMsg( "gui.save-dir" ) );
            if ( controller.downloadConfig != null
                 && controller.downloadConfig.getDownloadPath() != null ) {
                try {
                    File dir = new File( controller.downloadConfig.getDownloadPath() );
                    if ( dir.exists() ) {
                        dirChooser.setInitialDirectory( dir );
                    }
                } catch ( Exception e ) {
                    log.warn( e.getLocalizedMessage() );
                }
            }
            File selectedDir = dirChooser.showDialog( controller.getPrimaryStage() );
            if ( selectedDir == null ) {
                return;
            }
            statusLogController.setStatusTextUI(
                I18n.format( "status.download.started" ) );

            controller.dataBean.setProcessingSteps( controller.extractProcessingSteps() );
            String savePath = selectedDir.getPath();
            Runnable convertTask = () -> {
                DownloadStep ds = controller.dataBean.convertToDownloadStep( savePath );
                try {
                    this.buttonDownload.setDisable( true );
                    DownloadStepConverter dsc = new DownloadStepConverter(
                        controller.dataBean.getSelectedService().getUsername(),
                        controller.dataBean.getSelectedService().getPassword() );
                    JobList jl = dsc.convert( ds );
                    Processor p = Processor.getInstance();
                    p.addJob( jl );
                } catch ( ConverterException ce ) {
                    statusLogController.setStatusTextUI( ce.getMessage() );
                    Controller.logToAppLog( ce.getMessage() );
                } finally {
                    this.buttonDownload.setDisable( false );
                }
            };
            new Thread( convertTask ).start();
        }
    }

    /**
     * Handle config saving.
     *
     * @param event
     *     The event.
     */
    @FXML
    protected void handleSaveConfig( ActionEvent event ) {
        controller.extractStoredQuery();
        controller.extractBoundingBox();
        controller.extractCql();
        if ( controller.validateInput() && controller.validateCqlInput() ) {
            FileChooser fileChooser = new FileChooser();
            DirectoryChooser dirChooser = new DirectoryChooser();
            File downloadDir;
            File initDir;

            dirChooser.setTitle( I18n.getMsg( "gui.save-dir" ) );

            if ( controller.downloadConfig == null ) {
                downloadDir = dirChooser.showDialog( controller.getPrimaryStage() );
                String basedir = Config.getInstance().getServices()
                                       .getBaseDirectory();
                initDir = new File(
                    basedir.isEmpty()
                    ? System.getProperty( USER_DIR )
                    : basedir );
                File uniqueName = Misc.uniqueFile( downloadDir, "config", "xml",
                                                   null );
                fileChooser.setInitialFileName( uniqueName.getName() );
            } else {
                File downloadInitDir
                    = new File( controller.downloadConfig.getDownloadPath() );
                if ( !downloadInitDir.exists() ) {
                    downloadInitDir = new File( System.getProperty( USER_DIR ) );
                }
                dirChooser.setInitialDirectory( downloadInitDir );
                downloadDir = dirChooser.showDialog( controller.getPrimaryStage() );

                String path = controller.downloadConfig.getFile().getAbsolutePath();
                path = path.substring( 0, path.lastIndexOf( File.separator ) );
                initDir = new File( path );
                fileChooser.setInitialFileName( controller.downloadConfig.getFile()
                                                                         .getName() );
            }
            fileChooser.setInitialDirectory( initDir );
            FileChooser.ExtensionFilter xmlFilter =
                new FileChooser.ExtensionFilter( "xml files (*.xml)",
                                                 "*.xml" );
            fileChooser.getExtensionFilters().add( xmlFilter );
            fileChooser.setSelectedExtensionFilter( xmlFilter );
            fileChooser.setTitle( I18n.getMsg( "gui.save-conf" ) );
            File configFile = fileChooser.showSaveDialog( controller.getPrimaryStage() );
            if ( configFile == null ) {
                return;
            }

            if ( !configFile.toString().endsWith( ".xml" ) ) {
                configFile = new File( configFile.toString() + ".xml" );
            }

            controller.dataBean.setProcessingSteps( controller.extractProcessingSteps() );

            String savePath = downloadDir.getPath();
            DownloadStep ds = controller.dataBean.convertToDownloadStep( savePath );
            try {
                ds.write( configFile );
            } catch ( IOException ex ) {
                log.warn( ex.getMessage(), ex );
            }
        }
    }

    @FXML
    protected void handleCloseApp( ActionEvent event ) {
        Stage stage = (Stage) buttonClose.getScene().getWindow();
        menuBarController.closeApp( stage );
    }

}
