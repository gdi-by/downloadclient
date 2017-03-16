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


import com.vividsolutions.jts.geom.Polygon;
import de.bayern.gdi.utils.I18n;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.geotools.data.DataUtilities;
import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.HTTPClient;
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.map.WMSLayer;
import org.geotools.ows.ServiceException;
import org.geotools.referencing.CRS;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.swing.JMapPane;
import org.geotools.swing.MapPane;
import org.geotools.swing.action.InfoAction;
import org.geotools.swing.action.NoToolAction;
import org.geotools.swing.action.PanAction;
import org.geotools.swing.action.ResetAction;
import org.geotools.swing.action.ZoomInAction;
import org.geotools.swing.action.ZoomOutAction;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.locale.LocaleUtils;
import org.geotools.swing.tool.InfoToolHelper;
import org.geotools.swing.tool.InfoToolResult;
import org.geotools.swing.tool.ZoomInTool;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public class WMSMapSwing extends Parent {

    private static final Logger log
            = Logger.getLogger(WMSMapSwing.class.getName());

    private WebMapServer wms;
    private VBox vBox;
    private MapContent mapContent;
    private String title;
    private int mapWidth;
    private int mapHeight;
    private SwingNode mapNode;
    private TextField coordinateX1TextField;
    private TextField coordinateY1TextField;
    private TextField coordinateX2TextField;
    private TextField coordinateY2TextField;
    private Label coordinateX1Label;
    private Label coordinateX2Label;
    private Label coordinateY1Label;
    private Label coordinateY2Label;
    private ExtJMapPane mapPane;
    private Layer baseLayer;
    private StyleBuilder sb;
    private StyleFactory sf;
    private FilterFactory2 ff;
    DefaultFeatureCollection polygonFeatureCollection;
    private CoordinateReferenceSystem displayCRS;
    private CoordinateReferenceSystem oldDisplayCRS;
    private CoordinateReferenceSystem mapCRS;

    private static final double TEN_PERCENT = 0.1D;
    private static final String POLYGON_LAYER_TITLE = "PolygonLayer";
    private static final String TOOLBAR_INFO_BUTTON_NAME = "ToolbarInfoButton";
    private static final String TOOLBAR_PAN_BUTTON_NAME
            = "ToolbarPanButton";
    private static final String TOOLBAR_POINTER_BUTTON_NAME
            = "ToolbarPointerButton";
    private static final String TOOLBAR_RESET_BUTTON_NAME
            = "ToolbarResetButton";
    private static final String TOOLBAR_ZOOMIN_BUTTON_NAME
            = "ToolbarZoomInButton";
    private static final String TOOLBAR_ZOOMOUT_BUTTON_NAME
            = "ToolbarZoomOutButton";
    private static final double INITIAL_EXTEND_X1 = 850028;
    private static final double INITIAL_EXTEND_Y1 = 6560409;
    private static final double INITIAL_EXTEND_X2 = 1681693;
    private static final double INITIAL_EXTEND_Y2 = 5977713;
    private static final String INITIAL_CRS = "EPSG:3857";

    private static final Double HOUNDREDTHOUSAND = 100000.0D;

    private static final int MAP_NODE_MARGIN = 40;
    private static final int SOURCE_LABEL_HEIGHT = 70;

    private static final Color OUTLINE_COLOR = Color.BLACK;
    private static final Color SELECTED_COLOUR = Color.YELLOW;
    private static final Color FILL_COLOR = Color.CYAN;
    private static final Float OUTLINE_WIDTH = 0.3f;
    private static final Float FILL_TRANSPARACY = 0.4f;
    private static final Float STROKY_TRANSPARACY = 0.8f;
    private String selectedPolygonName;
    private String selectedPolygonID;
    private GeometryDescriptor geomDesc;
    private String geometryAttributeName;
    private String source;
    private CursorAction bboxAction;


    /**
     * Represents all Infos needed for drawing a Polyon.
     **/
    public static class FeaturePolygon {
        /**
         * the polygon.
         **/
        public Polygon polygon;
        /**
         * name of the polygon.
         **/
        public String name;
        /**
         * id of the polygon.
         **/
        public String id;

        /**
         * crs of the polygon.
         */
        public CoordinateReferenceSystem crs;

        /**
         * Constructor.
         **/
        public FeaturePolygon(Polygon polygon,
                              String name,
                              String id,
                              CoordinateReferenceSystem crs) {
            this.polygon = polygon;
            this.name = name;
            this.id = id;
            this.crs = crs;
        }
    }

    /**
     * Initializes geotools localisation system with our default I18n locale.
     * TODO: Buggy with org/geotools/gt-swing/14.3 (and probably also w 15.0.)
     */
    private static void initGeotoolsLocale() {
        LocaleUtils.setLocale(I18n.getLocale());
        //the next line for testing needs an additional import java.util.Locale;
        //LocaleUtils.setLocale(Locale.ITALIAN);
    }

    /**
     * adds a node to this map.
     *
     * @param n the node
     */
    public void add(Node n) {
        this.vBox.getChildren().remove(n);
        this.vBox.getChildren().add(n);
    }

    /**
     * gets the children of this node.
     *
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
        initGeotoolsLocale();
    }

    /**
     * Constructor.
     * @param mapURL mapURL
     * @param width width
     * @param heigth heigth
     * @param layer layer
     * @param mapURL The URL of the WMS Service
     * @throws MalformedURLException
     */
    public WMSMapSwing(String mapURL, int width, int heigth, String layer)
            throws
            MalformedURLException {
        this(new URL(mapURL), width, heigth, layer);
    }

    /**
     * Constructor.
     * @param mapURL mapURL
     * @param width width
     * @param heigth heigth
     * @param layer layer
     */
    public WMSMapSwing(URL mapURL, int width, int heigth, String layer) {
        this(mapURL, width, heigth, layer, null, null);
    }

    /**
     * Constructor.
     * @param mapURL mapURL
     * @param width width
     * @param heigth heigth
     * @param layer layer
     * @param source source
     */
    public WMSMapSwing(URL mapURL, int width, int heigth, String layer,
                       String source) {
        this(mapURL, width, heigth, layer, null, source);
    }

    /**
     * gets the getCapabilities URL.
     * @param mapURL the URL of the Map
     * @return getCapabilties URL
     */
    public static URL getCapabiltiesURL(URL mapURL) {
        URL url = mapURL;
        try {
            WebMapServer wms = new WebMapServer(mapURL);
            HTTPClient httpClient = wms.getHTTPClient();
            URL get = wms.
                    getCapabilities().
                    getRequest().
                    getGetCapabilities().
                    getGet();
            if (get != null) {
                url = new URL(get.toString() + "request=GetCapabilities");
            }
            httpClient.getConnectTimeout();
        } catch (IOException | ServiceException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return url;
    }

    /**
     * Constructor.
     * @param mapURL mapURL
     * @param width width
     * @param heigth heigth
     * @param layer layer
     * @param source source
     * @param displayCRS crs of display
     */
    public WMSMapSwing(URL mapURL, int width, int heigth, String layer,
                       CoordinateReferenceSystem displayCRS, String source) {
        initGeotoolsLocale();
        try {
            if (displayCRS == null) {
                setDisplayCRS(INITIAL_CRS);
            } else {
                setDisplayCRS(displayCRS);
            }
            this.source = source;
            this.sb = new StyleBuilder();
            this.sf = CommonFactoryFinder.getStyleFactory(null);
            this.ff = CommonFactoryFinder.getFilterFactory2(null);
            this.mapHeight = heigth;
            this.mapWidth = width;
            this.vBox = new VBox();
            this.wms = new WebMapServer(mapURL);
            List<Layer> layers = this.wms.getCapabilities().getLayerList();
            baseLayer = null;
            boolean layerFound = false;
            for (Layer outerLayer : layers) {
                if (outerLayer.getName() != null) {
                    if (outerLayer.getName().toLowerCase().equals(layer
                            .toLowerCase())) {
                        baseLayer = outerLayer;
                        // we actually need to set both by hand, else the
                        // request will fail
                        baseLayer.setTitle(layer);
                        baseLayer.setName(layer);
                        layerFound = true;
                    }
                }
                for (Layer wmsLayer : outerLayer.getChildren()) {
                    if (wmsLayer.getName().toLowerCase().equals(
                            layer.toLowerCase())) {
                        baseLayer = wmsLayer.getParent();
                        baseLayer.setTitle(layer);
                        baseLayer.setName(layer);
                        layerFound = true;
                        break;
                    }
                }
                if (layerFound) {
                    break;
                }
            }
            this.mapContent = new MapContent();
            this.mapContent.setTitle(this.title);
            this.mapNode = new SwingNode();
            this.mapNode.setManaged(false);
            //this.add(this.layerLabel);
            //this.add(this.wmsLayers);
            this.add(this.mapNode);
            this.getChildren().add(vBox);
            displayMap(baseLayer);

        } catch (IOException | ServiceException | FactoryException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * sets the CRS the coords under the map should be displayed in.
     * @param crs Coordinate Reference System
     * @throws FactoryException when the CRS can't be found
     */
    public void setDisplayCRS(String crs) throws FactoryException {
        CoordinateReferenceSystem coordinateReferenceSystem = null;
        coordinateReferenceSystem = CRS.decode(crs);
        setDisplayCRS(coordinateReferenceSystem);
    }


    private void setMapCRS(CoordinateReferenceSystem crs) {
        this.mapCRS = crs;
    }

    /**
     * Sets the CRS to Display the coordinates under the map.
     * @param crs CoordinateReferenceSystem
     */
    public void setDisplayCRS(CoordinateReferenceSystem crs) {
        if (this.displayCRS == null) {
            this.oldDisplayCRS = crs;
        }
        this.oldDisplayCRS = this.displayCRS;
        this.displayCRS = crs;
        changeLabels(crs);
        if (this.coordinateX1TextField != null
                && this.coordinateY1TextField != null
                && this.coordinateX2TextField != null
                && this.coordinateY2TextField != null) {
            if (!this.coordinateX1TextField.getText()
                    .toString().equals("")
                    && !this.coordinateY1TextField.getText()
                    .toString().equals("")
                    && !this.coordinateX2TextField.getText()
                    .toString().equals("")
                    && !this.coordinateY2TextField.getText().
                    toString().equals("")) {
                Double x1Coordinate = Double.parseDouble(
                        this.coordinateX1TextField.getText().toString());
                Double x2Coordinate = Double.parseDouble(
                        this.coordinateX2TextField.getText().toString());
                Double y1Coordinate = Double.parseDouble(
                        this.coordinateY1TextField.getText().toString());
                Double y2Coordinate = Double.parseDouble(
                        this.coordinateY2TextField.getText().toString());
                if (x1Coordinate != null
                        && x2Coordinate != null
                        && y1Coordinate != null
                        && y2Coordinate != null) {
                    try {
                        convertAndDisplayBoundingBox(x1Coordinate,
                                x2Coordinate,
                                y1Coordinate,
                                y2Coordinate,
                                this.oldDisplayCRS,
                                this.displayCRS);
                    } catch (FactoryException | TransformException e) {
                        clearCoordinateDisplay();
                        log.log(Level.SEVERE, e.getMessage(), e);
                    }
                } else {
                    clearCoordinateDisplay();
                }
            } else {
                clearCoordinateDisplay();
            }
        }
    }

    private void displayMap(Layer wmsLayer) {
        CRSEnvelope targetEnv = null;
        for (CRSEnvelope env  : wmsLayer.getLayerBoundingBoxes()) {
            if (env.getEPSGCode().equals(INITIAL_CRS)) {
                targetEnv = env;
            }
        }
        wmsLayer.setBoundingBoxes(targetEnv);

        WMSLayer displayLayer = new WMSLayer(this.wms, wmsLayer);
        this.mapContent.addLayer(displayLayer);
        setMapCRS(this
                .mapContent
                .getViewport()
                .getCoordinateReferenceSystem());
        createSwingContent(this.mapNode);
    }

    /**
     * sets text fields for coordinates.
     * @param x1 x1
     * @param y1 y1
     * @param x2 x2
     * @param y2 y2
     */
    public void setCoordinateDisplay(
            TextField x1,
            TextField y1,
            TextField x2,
            TextField y2) {
        this.coordinateX1TextField = x1;
        this.coordinateY1TextField = y1;
        this.coordinateX2TextField = x2;
        this.coordinateY2TextField = y2;
    }

    /**
     * Sets the Labels.
     * @param labelx1 label x1
     * @param labelx2 label x2
     * @param labely1 label y1
     * @param labely2 label y2
     */
    public void setCoordinateLabel(
            Label labelx1,
            Label labelx2,
            Label labely1,
            Label labely2
    ) {
        this.coordinateX1Label = labelx1;
        this.coordinateX2Label = labelx2;
        this.coordinateY1Label = labely1;
        this.coordinateY2Label = labely2;
    }

    private void setDisplayCoordinates(
            Double x1,
            Double y1,
            Double x2,
            Double y2
    ) {
        try {
            convertAndDisplayBoundingBox(x1,
                    x2,
                    y1,
                    y2,
                    this.mapCRS,
                    this.displayCRS);
        } catch (FactoryException | TransformException e) {
            clearCoordinateDisplay();
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void convertAndDisplayBoundingBox(
            Double x1,
            Double x2,
            Double y1,
            Double y2,
            CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS
    ) throws TransformException, FactoryException {
        com.vividsolutions.jts.geom.Point p1 = convertDoublesToPoint(
                x1,
                y1,
                sourceCRS,
                targetCRS);
        com.vividsolutions.jts.geom.Point p2 = convertDoublesToPoint(
                x2,
                y2,
                sourceCRS,
                targetCRS);
        ReferencedEnvelope re = new ReferencedEnvelope(targetCRS);
        re.include(p1.getX(), p1.getY());
        re.include(p2.getX(), p2.getY());
        DirectPosition lowerCorner = re.getLowerCorner();
        DirectPosition upperCorner = re.getUpperCorner();
        if (lowerCorner != null && upperCorner != null) {
            double valX1 = lowerCorner.getCoordinate()[0];
            double valY1 = lowerCorner.getCoordinate()[1];
            double valX2 = upperCorner.getCoordinate()[0];
            double valY2 = upperCorner.getCoordinate()[1];
            if (CRS.getProjectedCRS(targetCRS) == null) {
                this.coordinateX1TextField.setText(String.valueOf(
                        Math.round(valX1 * HOUNDREDTHOUSAND) / HOUNDREDTHOUSAND
                ));
                this.coordinateY1TextField.setText(String.valueOf(
                        Math.round(valY1 * HOUNDREDTHOUSAND) / HOUNDREDTHOUSAND
                ));
                this.coordinateX2TextField.setText(String.valueOf(
                        Math.round(valX2 * HOUNDREDTHOUSAND) / HOUNDREDTHOUSAND
                ));
                this.coordinateY2TextField.setText(String.valueOf(
                        Math.round(valY2 * HOUNDREDTHOUSAND) / HOUNDREDTHOUSAND
                ));
            } else {
                this.coordinateX1TextField.setText(String.valueOf(
                        Math.round((float) valX1)
                ));
                this.coordinateY1TextField.setText(String.valueOf(
                        Math.round((float) valY1)
                ));
                this.coordinateX2TextField.setText(String.valueOf(
                        Math.round((float) valX2)
                ));
                this.coordinateY2TextField.setText(String.valueOf(
                        Math.round((float) valY2)
                ));
            }
        }
    }

    private void changeLabels(CoordinateReferenceSystem targetCRS) {
        if (coordinateY1Label != null
                && coordinateX1Label != null
                && coordinateY2Label != null
                && coordinateX2Label != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    String axis1 = targetCRS
                            .getCoordinateSystem().getAxis(1).getName()
                            .getCode();
                    String axis0 = targetCRS
                            .getCoordinateSystem().getAxis(0).getName()
                            .getCode();
                    axis0 = axis0.replace(" ", "");
                    axis0 = "gui." + axis0.toLowerCase();
                    axis1 = axis1.replace(" ", "");
                    axis1 = "gui." + axis1.toLowerCase();
                    axis0 = I18n.getMsg(axis0);
                    axis1 = I18n.getMsg(axis1);
                    coordinateY1Label.setText(axis1);
                    coordinateY2Label.setText(axis1);
                    coordinateX1Label.setText(axis0);
                    coordinateX2Label.setText(axis0);
                }
            });
        }
    }

    private com.vividsolutions.jts.geom.Point convertDoublesToPoint(
            Double x,
            Double y,
            CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS)
    throws TransformException, FactoryException {
        com.vividsolutions.jts.geom.GeometryFactory gf = new
                com.vividsolutions.jts.geom.GeometryFactory();
        com.vividsolutions.jts.geom.Coordinate coo = new com
                .vividsolutions.jts.geom.Coordinate(x, y);
        com.vividsolutions.jts.geom.Point p = gf.createPoint(coo);
        MathTransform transform = CRS.findMathTransform(
                sourceCRS, targetCRS);
        return (com.vividsolutions.jts.geom.Point) JTS.transform(p, transform);
    }

    private void clearCoordinateDisplay() {
        if (this.coordinateX1TextField != null) {
            this.coordinateX1TextField.setText("");
        }
        if (this.coordinateY1TextField != null) {
            this.coordinateY1TextField.setText("");
        }
        if (this.coordinateX2TextField != null) {
            this.coordinateX2TextField.setText("");
        }
        if (this.coordinateY2TextField != null) {
            this.coordinateY2TextField.setText("");
        }
    }

    /**
     * represents the actions for the cursor.
     **/
    private class CursorAction extends NoToolAction {

        private Double x1Coordinate;
        private Double x2Coordinate;
        private Double y1Coordinate;
        private Double y2Coordinate;
        private int clickCount = 0;

        /**
         * resets the coordinate Infos.
         */
        public void resetCoordinates() {
            x1Coordinate = null;
            x2Coordinate = null;
            y1Coordinate = null;
            y2Coordinate = null;
            clickCount = 0;
        }

        public CursorAction(MapPane mapPane) {
            super(mapPane);
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent ev) {
            ZoomInTool tool = new ZoomInTool() {
                private Point start;
                private Point end;
                private DirectPosition2D mapStartPos;
                private DirectPosition2D mapEndPos;
                private WeakHashMap<Layer, InfoToolHelper> helperTable;


                @Override
                public void onMouseClicked(MapMouseEvent ev) {
                    List<org.geotools.map.Layer> layers =
                            mapPane.getMapContent().layers();
                    if (layers.size() == 1) {
                        //"normal" bounding Box selection
                        if (clickCount == 0) {
                            end = null;
                            mapEndPos = null;
                            start = ev.getPoint();
                            mapStartPos = ev.getWorldPos();
                            clearCoordinateDisplay();
                            x1Coordinate = mapStartPos.getX();
                            y1Coordinate = mapStartPos.getY();
                            x2Coordinate = null;
                            y2Coordinate = null;
                            clickCount++;
                        } else if (clickCount == 1) {
                            end = ev.getPoint();
                            mapEndPos = ev.getWorldPos();
                            x2Coordinate = mapEndPos.getX();
                            y2Coordinate = mapEndPos.getY();
                            setDisplayCoordinates(x1Coordinate, y1Coordinate,
                                    x2Coordinate, y2Coordinate);
                            if (start != null && end != null) {
                                Rectangle rect = new Rectangle();
                                rect.setFrameFromDiagonal(start, end);
                                mapPane.setDrawRect(rect);
                            }
                            clickCount = 0;
                        } else {
                            clickCount = 0;
                        }
                        repaint();
                    } else {
                        DirectPosition2D pos = ev.getWorldPos();
                        MapContent content = mapPane.getMapContent();
                        final int nlayers = content.layers().size();
                        helperTable = new WeakHashMap<Layer,
                                InfoToolHelper>();
                        for (org.geotools.map.Layer layer : content.layers()) {
                            if (layer.isSelected()) {
                                InfoToolHelper helper = null;

                                String layerName = layer.getTitle();
                                if (layerName == null
                                        || layerName.length() == 0) {
                                    layerName = layer.
                                            getFeatureSource().
                                            getName().
                                            getLocalPart();
                                }
                                if (layerName == null
                                        || layerName.length() == 0) {
                                    layerName = layer.
                                            getFeatureSource().
                                            getSchema().
                                            getName().
                                            getLocalPart();
                                }
                                if (helper == null) {
                                    helper = InfoToolHelperLookup.
                                            getHelper(layer);

                                    if (helper == null) {
                                        return;
                                    }

                                    helper.setMapContent(content);
                                    helper.setLayer(layer);
                                }

                                try {
                                    if (layerName.
                                            equals(POLYGON_LAYER_TITLE)) {
                                        InfoToolResult result =
                                                helper.getInfo(pos);
                                        int numFeatures =
                                                result.getNumFeatures();
                                        if (numFeatures == 1) {
                                            Map<String, Object> featureData
                                                    = result.
                                                    getFeatureData(0);
                                            String name = (String)
                                                    featureData.get(
                                                            "name");
                                            String id = (String)
                                                    featureData.get("id");
                                            //highlightSelectedPolygon(id);
                                            setNameAndId(name, id);
                                        } else if (numFeatures > 1) {
                                            //Yup, this is dirty.
                                            setNameAndId("#@#", Integer
                                                    .toString(numFeatures));
                                        }

                                    }

                                } catch (Exception e) {
                                    log.log(Level.SEVERE, e.getMessage(),
                                            e);
                                }

                            }
                        }
                    }
                }

                @Override
                public void onMousePressed(MapMouseEvent ev) {
                }

                @Override
                public void onMouseDragged(MapMouseEvent ev) {
                }
            };
            mapPane.setCursorTool(tool);
        }
    }

    /**
     * Information about the Polygon.
     */
    public static class PolygonInfos {
        private String name;
        private String id;

        /**
         * Constructor.
         * @param name the name
         * @param id the id
         */
        public PolygonInfos(String name, String id) {
            this.name = name;
            this.id = id;
        }

        /**
         * returns the name.
         * @return the name
         */
        public String getName() {
            return this.name;
        }

        /**
         * returns the ID.
         * @return the ID
         */
        public String getID() {
            return this.id;
        }
    }

    /**
     * sets name and id of the selected polygon.
     * @param name name
     * @param id id
     */
    private void setNameAndId(String name, String id) {
        PolygonInfos polyInf = new PolygonInfos(name, id);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                fireEvent(new PolygonClickedEvent(polyInf));
            }
        });
    }

    /**
     * hightlight the selected Polygon.
     * @param polygonID the selected Polygon
     */
    public void highlightSelectedPolygon(String polygonID) {
        for (SimpleFeature simpleFeature : polygonFeatureCollection) {
            String featureID = (String) simpleFeature.getAttribute("id");
            if (featureID.equals(polygonID)) {
                Style style;

                style = createSelectedStyle(simpleFeature.getIdentifier());
                org.geotools.map.Layer layer = null;
                for (org.geotools.map.Layer layers : mapPane.getMapContent()
                        .layers()) {
                    if (layers.getTitle() != null) {
                        if (layers.getTitle().equals(POLYGON_LAYER_TITLE)) {
                            layer = layers;
                        }
                    }
                }
                ((FeatureLayer) layer).setStyle(style);
            }
        }
    }
    private Style createSelectedStyle(FeatureId ids) {
        Rule selectedRule = createRule(SELECTED_COLOUR, SELECTED_COLOUR);
        selectedRule.setFilter(ff.id(ids));

        Rule otherRule = createRule(OUTLINE_COLOR, FILL_COLOR);
        otherRule.setElseFilter(true);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(selectedRule);
        fts.rules().add(otherRule);

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }

    private Rule createRule(Color outlineColor, Color fillColor) {
        Symbolizer symbolizer = null;
        Fill fill = null;
        Stroke stroke = sf.createStroke(
                ff.literal(outlineColor),
                ff.literal(OUTLINE_WIDTH));

        fill = sf.createFill(ff.literal(fillColor),
                ff.literal(FILL_TRANSPARACY));
        symbolizer = sf.createPolygonSymbolizer(stroke, fill,
                geometryAttributeName);


        Rule rule = sf.createRule();
        rule.symbolizers().add(symbolizer);
        return rule;
    }

    /**
      * Resizes swing content and centers map.
      * @param width The new content width.
      */
    public void resizeSwingContent(double width) {
        try {
            if (width >= mapWidth) {
                double oldWidth = mapPane.getWidth();

                this.mapNode.resize(width - MAP_NODE_MARGIN, mapHeight);
                double scale = mapPane.getWorldToScreenTransform().getScaleX();
                ReferencedEnvelope bounds = mapPane.getDisplayArea();

                double dXScreenCoord = (width - MAP_NODE_MARGIN - oldWidth) / 2;
                double dXWorldCoord = dXScreenCoord / scale;

                bounds.translate(-1 * dXWorldCoord , 0);
                mapPane.setDisplayArea(bounds);
                mapPane.deleteGraphics();
                clearCoordinateDisplay();
            }
        } catch (NullPointerException e) { }
    }

    private void createSwingContent(final SwingNode swingNode) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("[]");
                stringBuilder.append("[min!]");
                JPanel panel = new JPanel(new MigLayout(
                        "wrap 1, insets 0",
                        "[grow]",
                        stringBuilder.toString()));

                mapPane = new ExtJMapPane(mapContent);
                mapPane.setMinimumSize(new Dimension(mapWidth,
                        mapHeight));
                mapPane.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        mapPane.setBorder(
                                BorderFactory.createLineBorder(Color.BLACK));
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        mapPane.setBorder(
                                BorderFactory.createLineBorder(
                                        Color.LIGHT_GRAY));
                    }
                });
                mapPane.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        mapPane.requestFocusInWindow();
                    }
                });
                JToolBar toolBar = new JToolBar();
                toolBar.setOrientation(JToolBar.HORIZONTAL);
                toolBar.setFloatable(false);
                JButton btn;
                JToggleButton tbtn;
                ButtonGroup cursorToolGrp = new ButtonGroup();
                ActionListener deleteGraphics = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mapPane.deleteGraphics();
                    }
                };
                bboxAction = new CursorAction(mapPane);
                tbtn = new JToggleButton(bboxAction);
                tbtn.setName(TOOLBAR_POINTER_BUTTON_NAME);
                tbtn.addActionListener(deleteGraphics);
                toolBar.add(tbtn);
                cursorToolGrp.add(tbtn);
                tbtn = new JToggleButton(new ZoomInAction(mapPane));
                tbtn.addActionListener(deleteGraphics);
                tbtn.setName(TOOLBAR_ZOOMIN_BUTTON_NAME);
                toolBar.add(tbtn);
                cursorToolGrp.add(tbtn);
                tbtn = new JToggleButton(new ZoomOutAction(mapPane));
                tbtn.addActionListener(deleteGraphics);
                tbtn.setName(TOOLBAR_ZOOMOUT_BUTTON_NAME);
                toolBar.add(tbtn);
                cursorToolGrp.add(tbtn);
                toolBar.addSeparator();
                tbtn = new JToggleButton(new PanAction(mapPane));
                tbtn.addActionListener(deleteGraphics);
                tbtn.setName(TOOLBAR_PAN_BUTTON_NAME);
                toolBar.add(tbtn);
                cursorToolGrp.add(tbtn);
                toolBar.addSeparator();
                tbtn = new JToggleButton(new InfoAction(mapPane));
                tbtn.addActionListener(deleteGraphics);
                tbtn.setName(TOOLBAR_INFO_BUTTON_NAME);
                toolBar.add(tbtn);
                cursorToolGrp.add(tbtn);
                toolBar.addSeparator();
                btn = new JButton(new ResetAction(mapPane));
                btn.addActionListener(deleteGraphics);
                btn.setName(TOOLBAR_RESET_BUTTON_NAME);
                toolBar.add(btn);
                panel.add(toolBar, "grow");
                panel.add(mapPane, "grow");
                if (source != null) {
                    JLabel sourceLabel = new JLabel(source);
                    mapHeight += SOURCE_LABEL_HEIGHT;
                    panel.add(sourceLabel, "grow");
                }
                swingNode.setContent(panel);
                setExtend(INITIAL_EXTEND_X1, INITIAL_EXTEND_X2,
                        INITIAL_EXTEND_Y1, INITIAL_EXTEND_Y2, INITIAL_CRS);
            }
        });
    }

    /**
     * repaints the map.
     */
    public void repaint() {
        Task task = new Task() {
            protected Integer call() {
                mapPane.repaint();
                return 0;
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    /**
     * Draws Polygons on the maps.
     *
     * @param featurePolygons List of drawable Polygons
     */
    public void drawPolygons(List<FeaturePolygon> featurePolygons) {
        try {

            SimpleFeatureType polygonFeatureType;

            String epsgCode = this
                    .mapCRS
                    .getIdentifiers()
                    .toArray()[0]
                    .toString();
            epsgCode = epsgCode.substring(epsgCode.lastIndexOf(":") + 1,
                    epsgCode.length());
            polygonFeatureType = DataUtilities.createType(
                    "Dataset",
                    "geometry:Geometry:srid="
                            + epsgCode
                            + ","
                            + "name:String,"
                            + "id:String"
            );
            polygonFeatureCollection =
                    new DefaultFeatureCollection("internal",
                            polygonFeatureType);
            geomDesc = polygonFeatureCollection.getSchema()
                    .getGeometryDescriptor();
            geometryAttributeName = geomDesc.getLocalName();

            for (FeaturePolygon fp : featurePolygons) {
                SimpleFeatureBuilder featureBuilder =
                        new SimpleFeatureBuilder(polygonFeatureType);
                try {
                    MathTransform transform = CRS.findMathTransform(
                            fp.crs, this.mapCRS);
                    featureBuilder.add((Polygon) JTS.transform(fp.polygon,
                            transform));
                    featureBuilder.add(fp.name);
                    featureBuilder.add(fp.id);
                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    polygonFeatureCollection.add(feature);
                } catch (FactoryException | TransformException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            org.geotools.map.Layer polygonLayer = new FeatureLayer(
                    polygonFeatureCollection, createPolygonStyle());
            polygonLayer.setTitle(POLYGON_LAYER_TITLE);
            List<org.geotools.map.Layer> layers = mapContent.layers();
            for (org.geotools.map.Layer layer : layers) {
                if (layer.getTitle() != null) {
                    if (layer.getTitle().equals(POLYGON_LAYER_TITLE)) {
                        mapContent.removeLayer(layer);
                    }
                }
            }
            mapContent.addLayer(polygonLayer);
        } catch (SchemaException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private Style createPolygonStyle() {
        Fill fill = sf.createFill(ff.literal(FILL_COLOR),
                ff.literal(FILL_TRANSPARACY));
        Stroke stroke = sf.createStroke(ff.literal(OUTLINE_COLOR),
                ff.literal(OUTLINE_WIDTH),
                ff.literal(STROKY_TRANSPARACY));
        PolygonSymbolizer polygonSymbolizer =
                sf.createPolygonSymbolizer(stroke, fill, null);
        return this.sb.createStyle(polygonSymbolizer);
    }

    /**
     * sets the viewport of the map to the given extend.
     *
     * @param envelope the extend
     */
    public void setExtend(ReferencedEnvelope envelope) {
        try {
            envelope = envelope.transform(this.mapContent.getViewport()
                    .getCoordinateReferenceSystem(), true);
            double xLength = envelope.getSpan(0);
            xLength = xLength * TEN_PERCENT;
            double yLength = envelope.getSpan(1);
            yLength = yLength * TEN_PERCENT;
            envelope.expandBy(xLength, yLength);
            bboxAction.resetCoordinates();
            mapPane.deleteGraphics();
            mapPane.setDisplayArea(envelope);
        } catch (FactoryException | TransformException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void setExtend(Double x1, Double x2, Double y1, Double y2, String
            crs) {
        CoordinateReferenceSystem coordinateReferenceSystem = null;
        try {
            coordinateReferenceSystem = CRS.decode(crs);
            ReferencedEnvelope initExtend =
                    new ReferencedEnvelope(x1,
                            x2,
                            y1,
                            y2, coordinateReferenceSystem);
            setExtend(initExtend);
        } catch (FactoryException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    /**
     * return the Bounds of the Map.
     *
     * @return the Bounds of the Map
     */
    public Envelope2D getBounds() {
        return getBounds(this.displayCRS);
    }

    /**
     * return the Bounds of the Map.
     * @param crs the CRS of the Bounding Box
     * @return the Bounds of the Map
     */
    public Envelope2D getBounds(CoordinateReferenceSystem crs) {
        return calculateBBox(this.coordinateX1TextField,
                this.coordinateX2TextField,
                this.coordinateY1TextField,
                this.coordinateY2TextField,
                crs);
    }

    /**
     * Calculates the bounds for 4 different text fields.
     * @param x1 tf with x1
     * @param x2 tf with x2
     * @param y1 tf with y1
     * @param y2 tf with y2
     * @param crs the CRS of the Bounding Box
     * @return the bounding box
     */
    public static Envelope2D calculateBBox(TextField x1,
                                           TextField x2,
                                           TextField y1,
                                           TextField y2,
                                           CoordinateReferenceSystem crs) {
        if (x1 != null
                && x2 != null
                && y1 != null
                && y2 != null) {
            if (!x1.getText().toString().equals("")
                    && !x2.toString().equals("")
                    && !y1.toString().equals("")
                    && !y2.toString().equals("")) {
                //System.out.println("TextFields not empty");
                Double x1Coordinate = Double.parseDouble(
                        x1.getText().toString());
                Double x2Coordinate = Double.parseDouble(
                        x2.getText().toString());
                Double y1Coordinate = Double.parseDouble(
                        y1.getText().toString());
                Double y2Coordinate = Double.parseDouble(
                        y2.getText().toString());
                Envelope env = new ReferencedEnvelope(
                        x1Coordinate,
                        x2Coordinate,
                        y1Coordinate,
                        y2Coordinate,
                        crs);
                Envelope2D env2D = new Envelope2D(env);
                return env2D;
            }
        }
        return null;
    }

    /**
     * resets the map.
     */
    public void reset() {
        clearCoordinateDisplay();
        this.mapContent.layers().stream()
                .filter(layer -> layer.getTitle() != null)
                .filter(layer -> layer.getTitle().equals(POLYGON_LAYER_TITLE))
                .forEach(layer -> {
                    mapContent.removeLayer(layer);
                });
        this.polygonFeatureCollection = null;
        this.geomDesc = null;
        this.geometryAttributeName = null;
    }

    //TODO - Destructor for Swing Item with Maplayer Dispose

    /**
     * Private extension of JMapPane.
     */
    private class ExtJMapPane extends JMapPane {
        private Rectangle rect;

        public ExtJMapPane(MapContent content) {
            super(content);
        }

        public void setDrawRect(Rectangle rectangle) {
            this.rect = rectangle;
        }

        @Override
        public void repaint() {
            super.repaint();
            if (this.rect != null) {
                Graphics2D graphic =
                        (Graphics2D) this.getGraphics();
                graphic.setColor(Color.WHITE);
                graphic.setXORMode(Color.RED);
                graphic.drawRect(
                        rect.x,
                        rect.y,
                        rect.width,
                        rect.height);
            }
        }

        public void deleteGraphics() {
            clearCoordinateDisplay();
            setDrawRect(null);
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (this.rect != null) {
                g.setColor(Color.WHITE);
                g.setXORMode(Color.RED);
                g.drawRect(
                        rect.x,
                        rect.y,
                        rect.width,
                        rect.height);
            }
        }
    }

}
