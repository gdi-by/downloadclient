package de.bayern.gdi.gui.controller;

import com.sothawo.mapjfx.MapView;
import de.bayern.gdi.gui.CRSModel;
import de.bayern.gdi.gui.CellTypes;
import de.bayern.gdi.gui.FeatureModel;
import de.bayern.gdi.gui.ItemModel;
import de.bayern.gdi.gui.OutputFormatModel;
import de.bayern.gdi.gui.OverallFeatureTypeModel;
import de.bayern.gdi.gui.map.MapHandler;
import de.bayern.gdi.gui.map.MapHandlerBuilder;
import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.ServiceChecker;
import de.bayern.gdi.utils.ServiceSettings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static de.bayern.gdi.gui.GuiConstants.BBOX_X1_INDEX;
import static de.bayern.gdi.gui.GuiConstants.BBOX_X2_INDEX;
import static de.bayern.gdi.gui.GuiConstants.BBOX_Y1_INDEX;
import static de.bayern.gdi.gui.GuiConstants.BBOX_Y2_INDEX;
import static de.bayern.gdi.gui.GuiConstants.EPSG4326;
import static de.bayern.gdi.gui.GuiConstants.FX_BORDER_COLOR_NULL;
import static de.bayern.gdi.gui.GuiConstants.FX_BORDER_COLOR_RED;
import static de.bayern.gdi.gui.GuiConstants.INITIAL_CRS_DISPLAY;
import static de.bayern.gdi.gui.FeatureModel.FilterType.FILTER;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Named
@Singleton
public class FilterWfsBasicController {

    private static final Logger log
        = LoggerFactory.getLogger( FilterWfsBasicController.class.getName() );

    @Inject
    private Controller controller;

    @Inject
    private StatusLogController statusLogController;

    @FXML
    private VBox basicWFSContainer;

    @FXML
    private VBox mapNodeWFS;

    @FXML
    private ToggleButton wfsMapBboxButton;

    @FXML
    private ToggleButton wfsMapInfoButton;

    @FXML
    private Button wfsMapResizeButton;

    @FXML
    private MapView wfsMapView;

    @FXML
    private Label wfsMapWmsSource;

    @FXML
    private TextField basicX1;

    @FXML
    private TextField basicY1;

    @FXML
    private TextField basicX2;

    @FXML
    private TextField basicY2;

    @FXML
    private Label lablbasicx1;

    @FXML
    private Label lablbasicy1;

    @FXML
    private Label lablbasicx2;

    @FXML
    private Label lablbasicy2;

    @FXML
    private Button basicApplyBbox;

    @FXML
    private VBox sqlWFSArea;

    @FXML
    private TextArea sqlTextarea;

    @FXML
    private Label referenceSystemChooserLabel;

    @FXML
    private ComboBox<CRSModel> referenceSystemChooser;

    @FXML
    private ComboBox<OutputFormatModel> dataFormatChooser;

    private MapHandler wmsWfsMapHandler;

    /**
     * Handle the reference system selection.
     *
     * @param event
     *     The event
     */
    @FXML
    protected void handleReferenceSystemSelect( ActionEvent event ) {
        if ( referenceSystemChooser.getValue() != null ) {
            if ( referenceSystemChooser.getValue().isAvailable() ) {
                referenceSystemChooser.setStyle( FX_BORDER_COLOR_NULL );
            } else {
                referenceSystemChooser.setStyle( FX_BORDER_COLOR_RED );
            }
        }
        controller.dataBean.addAttribute( "srsName",
                                          referenceSystemChooser.getValue() != null
                                          ? referenceSystemChooser.
                                                                      getValue().getOldName()
                                          : EPSG4326,
                                          "" );
        if ( wmsWfsMapHandler != null
             && referenceSystemChooser.getValue() != null ) {
            this.wmsWfsMapHandler.setDisplayCRS(
                referenceSystemChooser.getValue().getCRS() );
        } else if ( wmsWfsMapHandler != null ) {
            try {
                this.wmsWfsMapHandler.setDisplayCRS(
                    controller.dataBean.getAttributeValue( "srsName" ) );
            } catch ( FactoryException e ) {
                log.error( e.getMessage(), e );
            }
        }
    }

    /**
     * Handle the dataformat selection.
     *
     * @param event
     *     The event
     */
    @FXML
    protected void handleDataformatSelect( ActionEvent event ) {
        ComboBox<OutputFormatModel> cb =
            (ComboBox<OutputFormatModel>) event.getSource();
        controller.handleDataformatSelect( cb );
    }

    public void initGui( ItemModel data ) {
        boolean isSqlFilterType = false;
        if ( data instanceof OverallFeatureTypeModel ) {
            isSqlFilterType = true;
        }
        if ( data instanceof FeatureModel ) {
            FeatureModel.FilterType filterType =
                ( (FeatureModel) data ).getFilterType();
            isSqlFilterType = FILTER.equals( filterType );
        }
        this.referenceSystemChooser.setVisible( true );
        this.referenceSystemChooserLabel.setVisible( true );
        this.basicWFSContainer.setVisible( true );
        if ( isSqlFilterType ) {
            this.sqlWFSArea.setVisible( true );
            this.sqlWFSArea.setManaged( true );
            this.mapNodeWFS.setVisible( false );
            this.mapNodeWFS.setManaged( false );
        } else {
            this.sqlWFSArea.setVisible( false );
            this.sqlWFSArea.setManaged( false );
            this.mapNodeWFS.setVisible( true );
            this.mapNodeWFS.setManaged( true );
        }

        if ( data.getItem() instanceof WFSMeta.Feature ) {
            setCrsAndExtent( (WFSMeta.Feature) data.getItem() );
        } else if ( data.getItem() instanceof List
                    && !( (List) data.getItem() ).isEmpty() ) {
            List items = (List) data.getItem();
            setCrsAndExtent( (WFSMeta.Feature)
                                 items.get( items.size() - 1 ) );
        }
        List<String> outputFormats = controller
            .dataBean.getWFSService()
                     .findOperation( "GetFeature" ).getOutputFormats();

        if ( outputFormats.isEmpty() ) {
            outputFormats =
                controller.dataBean.getWFSService().getOutputFormats();
        }
        List<OutputFormatModel> formatModels = new ArrayList<>();
        for ( String s : outputFormats ) {
            OutputFormatModel m = new OutputFormatModel();
            m.setItem( s );
            m.setAvailable( true );
            formatModels.add( m );
        }
        ObservableList<OutputFormatModel> formats =
            FXCollections.observableArrayList( formatModels );
        this.dataFormatChooser.setItems( formats );
        this.dataFormatChooser.getSelectionModel().selectFirst();
    }

    public void resetGui() {
        if ( wmsWfsMapHandler != null ) {
            this.wmsWfsMapHandler.reset();
        }
        this.basicWFSContainer.setVisible( false );
        this.mapNodeWFS.setVisible( false );
        this.sqlWFSArea.setVisible( false );
        this.referenceSystemChooser.setVisible( false );
        this.referenceSystemChooserLabel.setVisible( false );
    }

    public void validate( Consumer<String> fail ) {
        if ( referenceSystemChooser.isVisible()
             && !referenceSystemChooser.getValue().isAvailable() ) {
            fail.accept( I18n.format( "gui.reference-system" ) );
        }
        if ( basicWFSContainer.isVisible()
             && dataFormatChooser.isVisible()
             && !dataFormatChooser.getValue().isAvailable() ) {
            fail.accept( I18n.format( "gui.data-format" ) );
        }
    }

    public void initMapHandler( ServiceSettings serviceSetting ) {
        this.wmsWfsMapHandler = MapHandlerBuilder
            .newBuilder( serviceSetting )
            .withEventTarget( mapNodeWFS )
            .withMapView( wfsMapView )
            .withWmsSourceLabel( wfsMapWmsSource )
            .withBboxButton( wfsMapBboxButton )
            .withInfoButton( wfsMapInfoButton )
            .withResizeButtton( wfsMapResizeButton )
            .withCoordinateDisplay(
                basicX1,
                basicX2,
                basicY1,
                basicY2 )
            .withCoordinateLabel(
                lablbasicx1,
                lablbasicx2,
                lablbasicy1,
                lablbasicy2 )
            .withApplyCoordsToMapButton(
                basicApplyBbox )
            .build();
    }

    public void setVisible( boolean isVisible ) {
        this.basicWFSContainer.setVisible( isVisible );
    }

    public boolean isReachable( URL url ) {
        return ServiceChecker
            .isReachable( wmsWfsMapHandler.getCapabiltiesURL( url ) );
    }

    public void setCellFactories() {
        referenceSystemChooser.setCellFactory(
            new Callback<ListView<CRSModel>,
                ListCell<CRSModel>>() {
                @Override
                public ListCell<CRSModel> call( ListView<CRSModel> list ) {
                    return new CellTypes.CRSCell() {
                    };
                }
            } );
        dataFormatChooser.setCellFactory(
            new Callback<ListView<OutputFormatModel>,
                ListCell<OutputFormatModel>>() {
                @Override
                public ListCell<OutputFormatModel>
                call( ListView<OutputFormatModel> list ) {
                    return new CellTypes.StringCell();
                }
            } );
    }

    public void setExtent( ReferencedEnvelope extendWFS ) {
        wmsWfsMapHandler.setExtend( extendWFS );
    }

    public String getSqlText() {
        return sqlTextarea.getText();
    }

    public String getBoundingBox() {
        Envelope2D envelope = this.wmsWfsMapHandler.getBounds(
            referenceSystemChooser.
                                      getSelectionModel().
                                      getSelectedItem().
                                      getCRS() );
        if ( envelope == null ) {
            // Raise an error?
            return null;
        }
        StringBuilder bbox = new StringBuilder();
        bbox.append( envelope.getX() ).append( ',' )
            .append( envelope.getY() ).append( ',' )
            .append( envelope.getX() + envelope.getWidth() ).append( ',' )
            .append( envelope.getY() + envelope.getHeight() );

        CRSModel model = referenceSystemChooser.getValue();
        if ( model != null ) {
            bbox.append( ',' ).append( model.getOldName() );
        }
        return bbox.toString();
    }

    public void initializeBoundingBox() {
        if ( controller.downloadConfig.getBoundingBox() != null ) {
            String[] bBox = controller.downloadConfig.getBoundingBox().split( "," );
            basicX1.setText( bBox[BBOX_X1_INDEX] );
            basicY1.setText( bBox[BBOX_Y1_INDEX] );
            basicX2.setText( bBox[BBOX_X2_INDEX] );
            basicY2.setText( bBox[BBOX_Y2_INDEX] );
        }
    }

    public void initializeCqlTextArea() {
        if ( controller.downloadConfig != null ) {
            String cql = controller.downloadConfig.getCql();
            sqlTextarea.setText( cql );
        }
    }

    public void initializeDataFormatChooser() {
        boolean outputFormatAvailable = false;
        for ( OutputFormatModel i : dataFormatChooser.getItems() ) {
            if ( i.getItem().equals( controller.downloadConfig.getOutputFormat() ) ) {
                dataFormatChooser.getSelectionModel().select( i );
                outputFormatAvailable = true;
            }
        }
        if ( !outputFormatAvailable ) {
            OutputFormatModel output = new OutputFormatModel();
            output.setAvailable( false );
            output.setItem( controller.downloadConfig.getOutputFormat() );
            dataFormatChooser.getItems().add( output );
            dataFormatChooser.getSelectionModel().select( output );
        }
    }

    public void initialiseCrsChooser() {
        try {
            CoordinateReferenceSystem targetCRS =
                CRS.decode( controller.downloadConfig.getSRSName() );
            boolean crsAvailable = false;
            for ( CRSModel crsModel : referenceSystemChooser.getItems() ) {
                if ( CRS.equalsIgnoreMetadata( targetCRS,
                                               crsModel.getCRS() ) ) {
                    crsAvailable = true;
                    referenceSystemChooser.getSelectionModel()
                                          .select( crsModel );
                }
            }
            if ( !crsAvailable ) {
                CRSModel crsErrorModel = new CRSModel( targetCRS );
                crsErrorModel.setAvailable( false );
                referenceSystemChooser.getItems().add( crsErrorModel );
                referenceSystemChooser.getSelectionModel()
                                      .select( crsErrorModel );
            }
        } catch ( NoSuchAuthorityCodeException nsace ) {
            statusLogController.setStatusTextUI( I18n.format( "status.config.invalid-epsg" ) );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
    }

    public void setCrsAndExtent( WFSMeta.Feature feature ) {
        wmsWfsMapHandler.setExtend( feature.getBBox() );
        ArrayList<String> list = new ArrayList<>();
        list.add( feature.getDefaultCRS() );
        list.addAll( feature.getOtherCRSs() );
        ObservableList<CRSModel> crsList =
            FXCollections.observableArrayList();
        for ( String crsStr : list ) {
            try {
                String newcrsStr = crsStr;
                String seperator = null;
                if ( newcrsStr.contains( "::" ) ) {
                    seperator = "::";
                } else if ( newcrsStr.contains( "/" ) ) {
                    seperator = "/";
                }
                if ( seperator != null ) {
                    newcrsStr = "EPSG:"
                                + newcrsStr.substring(
                        newcrsStr.lastIndexOf( seperator )
                        + seperator.length(),
                        newcrsStr.length() );
                }
                CoordinateReferenceSystem crs = CRS.decode( newcrsStr );
                CRSModel crsm = new CRSModel( crs );
                crsm.setOldName( crsStr );
                crsList.add( crsm );
            } catch ( FactoryException e ) {
                log.error( e.getMessage(), e );
            }
        }
        if ( !crsList.isEmpty() ) {
            this.referenceSystemChooser.setItems( crsList );
            CRSModel crsm = crsList.get( 0 );
            try {
                CoordinateReferenceSystem initCRS = CRS.decode(
                    INITIAL_CRS_DISPLAY );
                CRSModel initCRSM = new CRSModel( initCRS );
                for ( int i = 0; i < crsList.size(); i++ ) {
                    if ( crsList.get( i ).equals( initCRSM ) ) {
                        crsm = crsList.get( i );
                        break;
                    }
                }
            } catch ( FactoryException e ) {
                log.error( e.getMessage(), e );
            }
            this.referenceSystemChooser.setValue( crsm );
        }
    }
}
