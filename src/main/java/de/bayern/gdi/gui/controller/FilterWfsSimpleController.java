package de.bayern.gdi.gui.controller;

import de.bayern.gdi.gui.CellTypes;
import de.bayern.gdi.gui.DataBean;
import de.bayern.gdi.gui.ItemModel;
import de.bayern.gdi.gui.OutputFormatModel;
import de.bayern.gdi.gui.UIFactory;
import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.utils.I18n;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static de.bayern.gdi.gui.GuiConstants.FX_BORDER_COLOR_NULL;
import static de.bayern.gdi.gui.GuiConstants.FX_BORDER_COLOR_RED;
import static de.bayern.gdi.gui.GuiConstants.GUI_FORMAT_NOT_SELECTED;
import static de.bayern.gdi.gui.GuiConstants.OUTPUTFORMAT;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Named
@Singleton
public class FilterWfsSimpleController {

    @Inject
    private Controller controller;

    @Inject
    private StatusLogController statusLogController;

    @FXML
    private VBox simpleWFSContainer;

    private UIFactory factory = new UIFactory();

    public void setVisible( boolean isVisible ) {
        this.simpleWFSContainer.setVisible( isVisible );
    }


    public void initGui( ItemModel data, boolean datasetAvailable ) {
        List<String> outputFormats = controller.dataBean.getWFSService()
                                                  .findOperation("GetFeature").getOutputFormats();
        if (outputFormats.isEmpty()) {
            outputFormats =
                controller.dataBean.getWFSService().getOutputFormats();
        }
        List<OutputFormatModel> formatModels =
            new ArrayList<>();
        for (String i : outputFormats) {
            OutputFormatModel m = new OutputFormatModel();
            m.setItem(i);
            m.setAvailable(true);
            formatModels.add(m);
        }
        WFSMeta.StoredQuery storedQuery;
        if (datasetAvailable) {
            storedQuery = (WFSMeta.StoredQuery) data.getItem();
        } else {
            storedQuery = new WFSMeta.StoredQuery();
        }
        factory.fillSimpleWFS(
            this.simpleWFSContainer,
            storedQuery,
            formatModels);

        // XXX: This is a bit ugly. We need real MVC.
        Node df = this.simpleWFSContainer
            .lookup( "#" + UIFactory.getDataFormatID());
        if (df instanceof ComboBox ) {
            ((ComboBox)df).setOnAction(evt -> {
                ComboBox<OutputFormatModel> cb =
                    (ComboBox<OutputFormatModel>)evt.getSource();
                controller.handleDataformatSelect(cb);
            });
        }
    }

    public void setStoredQueryAttributes() {
        controller.dataBean.setAttributes( new ArrayList<DataBean.Attribute>() );

        ObservableList<Node> children
            = this.simpleWFSContainer.getChildren();
        for ( Node n : children ) {
            if ( n.getClass() == HBox.class ) {
                HBox hbox = (HBox) n;
                ObservableList<Node> hboxChildren = hbox.getChildren();
                String value = "";
                String name = "";
                String type = "";
                Label l1 = null;
                Label l2 = null;
                TextField tf = null;
                ComboBox cb = null;
                for ( Node hn : hboxChildren ) {
                    if ( hn.getClass() == ComboBox.class ) {
                        cb = (ComboBox) hn;
                    }
                    if ( hn.getClass() == TextField.class ) {
                        tf = (TextField) hn;
                    }
                    if ( hn.getClass() == Label.class ) {
                        if ( l1 == null ) {
                            l1 = (Label) hn;
                        }
                        if ( l1 != (Label) hn ) {
                            l2 = (Label) hn;
                        }
                    }
                    if ( tf != null && ( l1 != null || l2 != null ) ) {
                        name = tf.getUserData().toString();
                        value = tf.getText();
                        if ( l2 != null && l1.getText().equals( name ) ) {
                            type = l2.getText();
                        } else {
                            type = l1.getText();
                        }
                    }
                    if ( cb != null && ( l1 != null || l2 != null )
                         && cb.getId().equals( UIFactory.getDataFormatID() ) ) {
                        name = OUTPUTFORMAT;
                        if ( cb.getSelectionModel() != null
                             && cb.getSelectionModel().getSelectedItem()
                                != null ) {
                            value = cb.getSelectionModel()
                                      .getSelectedItem().toString();
                            type = "";
                        } else {
                            Platform.runLater( () -> statusLogController.setStatusTextUI(
                                I18n.getMsg( GUI_FORMAT_NOT_SELECTED ) ) );
                        }
                    }
                    if ( !name.isEmpty() && !value.isEmpty() ) {
                        controller.dataBean.addAttribute(
                            name,
                            value,
                            type );
                    }
                }
            }
        }
    }

    public void loadWfsSimple() {
        ObservableList<Node> children
            = simpleWFSContainer.getChildren();
        Map<String, String> parameters = controller.downloadConfig.getParams();
        for ( Node node : children ) {
            if ( node instanceof HBox ) {
                HBox hb = (HBox) node;
                Node n1 = hb.getChildren().get( 0 );
                Node n2 = hb.getChildren().get( 1 );
                if ( n1 instanceof Label && n2 instanceof TextField ) {
                    Label paramLabel = (Label) n1;
                    TextField paramBox = (TextField) n2;
                    String targetValue = parameters.get( paramLabel
                                                             .getText() );
                    if ( targetValue != null ) {
                        paramBox.setText( targetValue );
                    }
                }
                if ( n2 instanceof ComboBox ) {
                    ComboBox<OutputFormatModel> cb
                        = (ComboBox<OutputFormatModel>) n2;
                    cb.setCellFactory(
                        new Callback<ListView<OutputFormatModel>,
                            ListCell<OutputFormatModel>>() {
                            @Override
                            public ListCell<OutputFormatModel>
                            call( ListView<OutputFormatModel> list ) {
                                return new CellTypes.StringCell();
                            }
                        } );
                    cb.setOnAction( event -> {
                        if ( cb.getValue().isAvailable() ) {
                            cb.setStyle( FX_BORDER_COLOR_NULL );
                        } else {
                            cb.setStyle( FX_BORDER_COLOR_RED );
                        }
                    } );
                    boolean formatAvailable = false;
                    for ( OutputFormatModel i : cb.getItems() ) {
                        if ( i.getItem().equals( controller.downloadConfig
                                                     .getOutputFormat() ) ) {
                            cb.getSelectionModel().select( i );
                            formatAvailable = true;
                        }
                    }
                    if ( !formatAvailable ) {
                        String format = controller.downloadConfig
                            .getOutputFormat();
                        OutputFormatModel m = new OutputFormatModel();
                        m.setItem( format );
                        m.setAvailable( false );
                        cb.getItems().add( m );
                        cb.getSelectionModel().select( m );
                    }
                    if ( cb.getValue().isAvailable() ) {
                        cb.setStyle( FX_BORDER_COLOR_NULL );
                    } else {
                        cb.setStyle( FX_BORDER_COLOR_RED );
                    }
                }
            }
        }
    }

    public void validate( Consumer<String> fail ) {
        if ( simpleWFSContainer.isVisible() ) {
            ObservableList<Node> children
                = simpleWFSContainer.getChildren();
            for ( Node node : children ) {
                if ( node instanceof HBox ) {
                    HBox hb = (HBox) node;
                    Node n2 = hb.getChildren().get( 1 );
                    if ( n2 instanceof ComboBox ) {
                        ComboBox<OutputFormatModel> cb
                            = (ComboBox<OutputFormatModel>) n2;
                        if ( !cb.getValue().isAvailable() ) {
                            fail.accept( I18n.format( "gui.data-format" ) );
                            break;
                        }
                    }
                }
            }
        }
    }
}
