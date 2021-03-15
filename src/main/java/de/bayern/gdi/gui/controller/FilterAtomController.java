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

import com.sothawo.mapjfx.MapView;
import de.bayern.gdi.gui.AtomFieldModel;
import de.bayern.gdi.gui.AtomItemModel;
import de.bayern.gdi.gui.CellTypes;
import de.bayern.gdi.gui.ItemModel;
import de.bayern.gdi.gui.MiscItemModel;
import de.bayern.gdi.gui.map.FeaturePolygon;
import de.bayern.gdi.gui.map.MapHandler;
import de.bayern.gdi.gui.map.MapHandlerBuilder;
import de.bayern.gdi.gui.map.PolygonClickedEvent;
import de.bayern.gdi.gui.map.PolygonInfos;
import de.bayern.gdi.services.Atom;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.ServiceSettings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static de.bayern.gdi.gui.GuiConstants.ATOM_CRS_STRING;
import static de.bayern.gdi.gui.GuiConstants.BGCOLOR;
import static de.bayern.gdi.gui.GuiConstants.FX_BORDER_COLOR_NULL;
import static de.bayern.gdi.gui.GuiConstants.FX_BORDER_COLOR_RED;
import static de.bayern.gdi.gui.GuiConstants.OUTPUTFORMAT;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Named
@Singleton
public class FilterAtomController {

    private static final Logger LOG = LoggerFactory.getLogger(FilterAtomController.class.getName());

    @Inject
    private Controller controller;

    @Inject
    private StatusLogController statusLogController;

    @FXML
    private ComboBox<ItemModel> atomVariationChooser;

    @FXML
    private VBox atomContainer;

    @FXML
    private VBox mapNodeAtom;

    @FXML
    private WebView valueAtomDescr;

    @FXML
    private Label valueAtomFormat;

    @FXML
    private Label valueAtomRefsys;

    @FXML
    private MapView atomMapView;

    @FXML
    private Label atomMapWmsSource;

    @FXML
    private ToggleButton atomMapSelectButton;

    @FXML
    private ToggleButton atomMapInfoButton;

    @FXML
    private Button atomMapResizeButton;

    private MapHandler wmsAtomMapHandler;

    /**
     * Handle the variation selection.
     *
     * @param event
     *     The event
     */
    @FXML
    protected void handleVariationSelect(ActionEvent event) {
        ItemModel selim = (ItemModel) this.atomVariationChooser.getValue();
        boolean variationAvailable = false;
        if (selim instanceof MiscItemModel) {
            atomVariationChooser.setStyle(FX_BORDER_COLOR_RED);
        } else {
            atomVariationChooser.setStyle(FX_BORDER_COLOR_NULL);
            variationAvailable = true;
        }
        if (selim != null) {
            Atom.Field selaf;
            if (variationAvailable) {
                selaf = (Atom.Field) selim.getItem();
            } else {
                selaf = new Atom.Field("", "");
            }
            controller.dataBean.addAttribute("VARIATION", selaf.getType(), "");
            if (selaf.getFormat().isEmpty()) {
                this.valueAtomFormat.setVisible(false);
            } else {
                this.valueAtomFormat.setText(selaf.getFormat());
                this.valueAtomFormat.setVisible(true);
            }
            if (selaf.getCRS().isEmpty()) {
                this.valueAtomRefsys.setVisible(false);
            } else {
                this.valueAtomRefsys.setVisible(true);
                this.valueAtomRefsys.setText(selaf.getCRS());
            }
            controller.dataBean.addAttribute(OUTPUTFORMAT, selaf.getFormat(), "");
        } else {
            controller.dataBean.addAttribute("VARIATION", "", "");
            controller.dataBean.addAttribute(OUTPUTFORMAT, "", "");
        }
        controller.validateChainContainerItems();
    }

    /**
     * Initialise GUI components.
     */
    public void initialiseGuiComponents() {
        boolean variantAvailable = false;
        for (ItemModel i : atomVariationChooser.getItems()) {
            Atom.Field field = (Atom.Field) i.getItem();
            if (field.getType()
                      .equals(controller.downloadConfig.getAtomVariation())) {
                variantAvailable = true;
                atomVariationChooser.getSelectionModel().select(i);
            }
        }
        if (!variantAvailable) {
            MiscItemModel errorVariant = new MiscItemModel();
            errorVariant.setItem(controller.downloadConfig.getAtomVariation());
            atomVariationChooser.getItems().add(errorVariant);
            atomVariationChooser.getSelectionModel()
                                .select(errorVariant);
        }
    }

    /**
     * Sets the visibility.
     * @param isVisible is shown when true otherwise hidden
     */
    public void setVisible(boolean isVisible) {
        this.atomContainer.setVisible(isVisible);
    }

    /**
     * Resets GUI.
     */
    public void resetGui() {
        if (wmsAtomMapHandler != null) {
            this.wmsAtomMapHandler.reset();
        }
    }

    /**
     * Validates the user input.
     *
     * @param fail
     *     never <code>null</code>>
     */
    public void validate(Consumer<String> fail) {
        if (atomContainer.isVisible()
             && atomVariationChooser.getValue() instanceof MiscItemModel) {
            fail.accept(I18n.format("gui.variants"));
        }
    }

    /**
     * Initialise the map handler.
     * @param serviceSetting never <code>null</code>
     */
    public void initMapHandler(ServiceSettings serviceSetting) {
        this.wmsAtomMapHandler = MapHandlerBuilder
            .newBuilder(serviceSetting)
            .withEventTarget(mapNodeAtom)
            .withMapView(atomMapView)
            .withWmsSourceLabel(atomMapWmsSource)
            .withSelectButton(atomMapSelectButton)
            .withInfoButton(atomMapInfoButton)
            .withResizeButtton(atomMapResizeButton)
            .build();
        mapNodeAtom.addEventHandler(PolygonClickedEvent.ANY,
                                     new SelectedAtomPolygon());
    }

    /**
     * Highlight the polygon identified by the passed item.
     * @param item never <code>null</code>
     */
    public void highlightPolygon(Atom.Item item) {
        if (wmsAtomMapHandler != null) {
            wmsAtomMapHandler.highlightSelectedPolygon(item.getID());
        }
    }

    /**
     * Sets the cell factories of this controller.
     */
    public void setCellFactories() {
        atomVariationChooser.setCellFactory(
            new Callback<ListView<ItemModel>,
                ListCell<ItemModel>>() {
                @Override
                public ListCell<ItemModel> call(ListView<ItemModel> list) {
                    return new CellTypes.ItemCell();
                }
            });
    }

    /**
     * Creates a list with the items.
     *
     * @param items
     *     never <code>null</code>
     * @return the list, may be <code>empty</code> but never <code>null</code>
     */
    public ObservableList<ItemModel> chooseType(List<Atom.Item> items) {
        ObservableList<ItemModel> opts =
            FXCollections.observableArrayList();
        List<FeaturePolygon> polygonList
            = new ArrayList<>();
        //Polygons are always epsg:4326
        // (http://www.georss.org/gml.html)
        try {
            ReferencedEnvelope extendATOM = null;
            CoordinateReferenceSystem
                atomCRS = CRS.decode(ATOM_CRS_STRING);
            Geometry all = null;
            for (Atom.Item i : items) {
                opts.add(new AtomItemModel(i));
                FeaturePolygon polygon =
                    new FeaturePolygon(
                        i.getPolygon(),
                        i.getTitle(),
                        i.getID(),
                        atomCRS);
                polygonList.add(polygon);
                all = all == null
                      ? i.getPolygon()
                      : all.union(i.getPolygon());
            }
            if (wmsAtomMapHandler != null) {
                if (all != null) {
                    extendATOM = new ReferencedEnvelope(
                        all.getEnvelopeInternal(), atomCRS);
                    wmsAtomMapHandler.setExtend(extendATOM);
                }
                wmsAtomMapHandler.drawPolygons(polygonList);
            }
        } catch (FactoryException e) {
            LOG.error(e.getMessage(), e);
        }
        return opts;
    }

    /**
     * Initialise the GUI with the passed data.
     *
     * @param data
     *     never <code>null</code>
     * @param datasetAvailable
     *     <code> true</code> if a dataset is available
     */
    public void initGui(ItemModel data, boolean datasetAvailable) {
        Atom.Item item;
        if (datasetAvailable) {
            item = (Atom.Item) data.getItem();
            try {
                item.load();
            } catch (URISyntaxException
                | SAXException
                | ParserConfigurationException
                | IOException e) {
                LOG.error("Could not Load Item\n"
                           + e.getMessage(), item);
                return;
            }
        } else {
            try {
                item = new Atom.Item(
                    new URL(controller.downloadConfig.getServiceURL()),
                    "");
            } catch (Exception e) {
                return;
            }
        }
        highlightPolygon(item);
        List<Atom.Field> fields = item.getFields();
        ObservableList<ItemModel> list =
            FXCollections.observableArrayList();
        for (Atom.Field f : fields) {
            AtomFieldModel afm = new AtomFieldModel(f);
            list.add(afm);
        }
        this.atomVariationChooser.setItems(list);
        this.atomVariationChooser.getSelectionModel().selectFirst();
        WebEngine engine = this.valueAtomDescr.getEngine();
        java.lang.reflect.Field f;
        try {
            f = engine.getClass().getDeclaredField("page");
            f.setAccessible(true);
            com.sun.webkit.WebPage page =
                (com.sun.webkit.WebPage) f.get(engine);
            page.setBackgroundColor(
                (new java.awt.Color(BGCOLOR, BGCOLOR, BGCOLOR)).getRGB()
           );
        } catch (NoSuchFieldException
            | SecurityException
            | IllegalArgumentException
            | IllegalAccessException e) {
            // Displays the webview with white background...
        }
        engine.loadContent("<head> <style>"
                            + ".description-content" + "{"
                            + "font-family: Sans-Serif" + "}"
                            + "</style> </head>"
                            + "<div class=\"description-content\">"
                            + item.getDescription() + "</div>");
    }

    public class SelectedAtomPolygon implements
                                     EventHandler<Event> {

        @Override
        public void handle(Event event) {
            if (wmsAtomMapHandler != null
                 && event instanceof PolygonClickedEvent) {

                PolygonClickedEvent pce = (PolygonClickedEvent) event;
                PolygonInfos polygonInfos =
                    pce.getPolygonInfos();
                String polygonName = polygonInfos.getName();
                String polygonID = polygonInfos.getID();

                if (polygonName != null && polygonID != null) {
                    if (polygonName.equals("#@#")) {
                        statusLogController.setStatusTextUI(I18n.format(
                            "status.polygon-intersect",
                            polygonID));
                        return;
                    }
                    controller.selectServiceType(polygonID);
                }
            }
        }
    }

}
