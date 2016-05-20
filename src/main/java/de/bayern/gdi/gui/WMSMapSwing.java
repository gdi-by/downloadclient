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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.map.MapContent;
import org.geotools.map.WMSLayer;
import org.geotools.ows.ServiceException;
import org.geotools.swing.JMapFrame;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public class WMSMapSwing extends Parent {

    private static final Logger log
            = Logger.getLogger(WMSMapFX.class.getName());

    private WebMapServer wms;
    private VBox vBox;
    private MapContent mapContent;
    private String title;
    //private ObservableList<String> layerList;
    //private ListView wmsLayers;
    private ComboBox wmsLayers;
    private Label layerLabel;

    /**
     * adds a node to this map.
     * @param n the node
     */
    public void add(Node n) {
        this.vBox.getChildren().remove(n);
        this.vBox.getChildren().add(n);
    }

    /**
     * gets the children of this node.
     * @return the children of the node
     */
    @Override
    public ObservableList getChildren() {
        return super.getChildren();
    }

    /**
     * Constructor.
     */
    public WMSMapSwing() {

    }

    /**
     * Constructor.
     * @param mapURL The URL of the WMS Service
     * @throws MalformedURLException
     */
    public WMSMapSwing(String mapURL) throws MalformedURLException {
        this(new URL(mapURL));
    }

    /**
     * Constructor.
     * @param mapURL The URL of the WMS Service
     */
    public WMSMapSwing(URL mapURL) {
        try {
            this.vBox = new VBox();
            this.wms = new WebMapServer(mapURL);
            List<Layer> layers = this.wms.getCapabilities().getLayerList();
            ObservableList<String> layerList = FXCollections
                    .observableArrayList();
            for (Layer layer: layers) {
                layerList.add(layer.getName());
            }
            if (layers == null) {
                throw new ServiceException("could not connect to url");
            }
            this.mapContent = new MapContent();
            this.title = this.wms.getCapabilities().getService().getTitle();
            this.mapContent.setTitle(this.title);
            this.wmsLayers = new ComboBox(layerList);
            this.layerLabel = new Label();
            this.layerLabel.setLabelFor(this.wmsLayers);
            this.layerLabel.setText("Layer: ");
            this.add(this.layerLabel);
            this.add(this.wmsLayers);
            this.getChildren().add(vBox);
            this.wmsLayers.setOnAction(new SelectLayer());

            //Actually select the second entry, because the first is null
            this.wmsLayers.getSelectionModel().select(0);
            this.wmsLayers.getSelectionModel().selectNext();
            //Maually fire the Event, because we "selected" something
            ActionEvent layerSelected = new ActionEvent();
            SelectLayer selLay = new SelectLayer();
            selLay.handle(layerSelected);
        } catch (IOException | ServiceException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void displayMap(Layer wmsLayer) {
        System.out.println("Selected Layer: " + wmsLayer.getName());
        WMSLayer displayLayer = new WMSLayer(this.wms, wmsLayer);
        this.mapContent.addLayer(displayLayer);
        JMapFrame.showMap(this.mapContent);
    }

    /**
     * Event Handler for the Layer Combobox.
     */
    private class SelectLayer
            implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            if (wmsLayers.getSelectionModel().getSelectedItem() != null) {
                String layerName = (String) wmsLayers.getSelectionModel()
                        .getSelectedItem();
                List<Layer> layers = wms.getCapabilities().getLayerList();
                Layer layer = null;
                for (Layer lay: layers) {
                    if (layerName == lay.getName()) {
                        layer = lay;
                        break;
                    }
                }
                displayMap(layer);
            }
        }
    }
}
