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

import de.bayern.gdi.utils.I18n;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;
import org.geotools.map.WMSLayer;
import org.geotools.ows.ServiceException;
import org.geotools.referencing.CRS;
import org.geotools.swing.JMapPane;
import org.geotools.swing.action.InfoAction;
import org.geotools.swing.action.NoToolAction;
import org.geotools.swing.action.PanAction;
import org.geotools.swing.action.ResetAction;
import org.geotools.swing.action.ZoomInAction;
import org.geotools.swing.action.ZoomOutAction;
import org.geotools.swing.control.JMapStatusBar;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.locale.LocaleUtils;
import org.geotools.swing.tool.ZoomInTool;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
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
    //private ObservableList<String> layerList;
    //private ListView wmsLayers;
    private int mapWidth;
    private int mapHeight;
    private SwingNode mapNode;
    private TextField coordinateX1;
    private TextField coordinateY1;
    private TextField coordinateX2;
    private TextField coordinateY2;
    private ExtJMapPane mapPane;

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
     *
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
     *
     * @param mapURL The URL of the WMS Service
     */
    public WMSMapSwing(URL mapURL, int width, int heigth, String layer) {
        initGeotoolsLocale();
        try {
            this.mapHeight = heigth;
            this.mapWidth = width;
            this.vBox = new VBox();
            this.wms = new WebMapServer(mapURL);
            List<Layer> layers = this.wms.getCapabilities().getLayerList();
            Layer myLayer = null;
            for (Layer wmsLayer: layers) {
                if (wmsLayer.getTitle().toLowerCase().equals(
                        layer.toLowerCase())) {
                    myLayer = wmsLayer;
                    break;
                }
            }
            this.mapContent = new MapContent();
            this.title = this.wms.getCapabilities().getService().getTitle();
            this.mapContent.setTitle(this.title);

            this.mapNode = new SwingNode();
            //this.add(this.layerLabel);
            //this.add(this.wmsLayers);
            this.add(this.mapNode);
            this.getChildren().add(vBox);
            displayMap(myLayer);
        } catch (IOException | ServiceException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void displayMap(Layer wmsLayer) {
        WMSLayer displayLayer = new WMSLayer(this.wms, wmsLayer);
        this.mapContent.addLayer(displayLayer);
        createSwingContent(this.mapNode);
        //JMapPane mapPane = new JMapPane(this.mapContent);

    }

    /**
     * Set TextFields to display the selected coordinates.
     *
     * @param x1 X1
     * @param y1 Y1
     * @param x2 X2
     * @param y2 Y2
     */
    public void setCoordinateDisplay(
        TextField x1,
        TextField y1,
        TextField x2,
        TextField y2
    ) {
        this.coordinateX1 = x1;
        this.coordinateY1 = y1;
        this.coordinateX2 = x2;
        this.coordinateY2 = y2;
    }

    private void createSwingContent(final SwingNode swingNode) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                sb.append("[]");
                sb.append("[min!]");
                JPanel panel = new JPanel(new MigLayout(
                        "wrap 1, insets 0",
                        "[grow]",
                        sb.toString()));

                mapPane = new ExtJMapPane(mapContent);
                mapPane.setPreferredSize(new Dimension(mapWidth,
                        mapHeight));
                mapPane.setSize(mapWidth, mapHeight);
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
                              BorderFactory.createLineBorder(Color.LIGHT_GRAY));
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
                NoToolAction noAction = new NoToolAction(mapPane) {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent ev) {
                        ZoomInTool tool = new ZoomInTool() {
                            private Point start;
                            private Point end;
                            private DirectPosition2D mapStartPos;
                            private DirectPosition2D mapEndPos;
                            private int clickCount = 0;
                            @Override
                            public void onMouseClicked(MapMouseEvent ev) {
                                if (clickCount == 0) {
                                    end = null;
                                    mapEndPos = null;
                                    mapPane.setSelectedEnvelope(null);
                                    start = ev.getPoint();
                                    mapStartPos = ev.getWorldPos();
                                    coordinateX1.setText(
                                        String.valueOf(mapStartPos.getX()));
                                    coordinateY1.setText(
                                        String.valueOf(mapStartPos.getY()));
                                    coordinateX2.setText("");
                                    coordinateY2.setText("");
                                    clickCount++;
                                } else if (clickCount == 1) {
                                    end = ev.getPoint();
                                    mapEndPos = ev.getWorldPos();
                                    coordinateX2.setText(
                                        String.valueOf(mapEndPos.getX()));
                                    coordinateY2.setText(
                                        String.valueOf(mapEndPos.getY()));
                                    Rectangle rect = new Rectangle();
                                    rect.setFrameFromDiagonal(start, end);
                                    mapPane.setDrawRect(rect);
                                    Envelope2D env = new Envelope2D();
                                    env.setFrameFromDiagonal(
                                        mapStartPos,
                                        ev.getWorldPos());
                                    mapPane.setSelectedEnvelope(env);
                                    clickCount = 0;
                                } else {
                                    clickCount = 0;
                                }
                                mapPane.repaint();
                            }
                            @Override
                            public void onMousePressed(MapMouseEvent ev) { }
                            @Override
                            public void onMouseDragged(MapMouseEvent ev) {
                            }
                        };
                        getMapPane().setCursorTool(tool);
                    }
                };
                tbtn = new JToggleButton(noAction);
                tbtn.setName(TOOLBAR_POINTER_BUTTON_NAME);
                toolBar.add(tbtn);
                cursorToolGrp.add(tbtn);
                tbtn = new JToggleButton(new ZoomInAction(mapPane));
                tbtn.setName(TOOLBAR_ZOOMIN_BUTTON_NAME);
                toolBar.add(tbtn);
                cursorToolGrp.add(tbtn);
                tbtn = new JToggleButton(new ZoomOutAction(mapPane));
                tbtn.setName(TOOLBAR_ZOOMOUT_BUTTON_NAME);
                toolBar.add(tbtn);
                cursorToolGrp.add(tbtn);
                toolBar.addSeparator();
                tbtn = new JToggleButton(new PanAction(mapPane));
                tbtn.setName(TOOLBAR_PAN_BUTTON_NAME);
                toolBar.add(tbtn);
                cursorToolGrp.add(tbtn);
                toolBar.addSeparator();
                tbtn = new JToggleButton(new InfoAction(mapPane));
                tbtn.setName(TOOLBAR_INFO_BUTTON_NAME);
                toolBar.add(tbtn);
                cursorToolGrp.add(tbtn);
                toolBar.addSeparator();
                btn = new JButton(new ResetAction(mapPane));
                btn.setName(TOOLBAR_RESET_BUTTON_NAME);
                toolBar.add(btn);
                panel.add(toolBar, "grow");
                panel.add(mapPane, "grow");
                panel.add(
                        JMapStatusBar.createDefaultStatusBar(mapPane), "grow");
                swingNode.setContent(panel);
                setExtend(INITIAL_EXTEND_X1, INITIAL_EXTEND_X2,
                        INITIAL_EXTEND_Y1, INITIAL_EXTEND_Y2, INITIAL_CRS);
            }
        });
    }

    private void setExtend(Envelope env) {
        mapPane.setDisplayArea(env);
    }

    private void setExtend(ReferencedEnvelope extend) {
        try {
            extend = extend.transform(this.mapContent.getViewport()
                .getCoordinateReferenceSystem(), true);
            setExtend((Envelope) extend);
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
     * @return the Bounds of the Map
     */
    public Envelope2D getBounds() {
        Component[] components = this.mapNode.getContent().getComponents();
        for (Component c : components) {
            if (c.getClass().equals(ExtJMapPane.class)) {
                return ((ExtJMapPane)c).getSelectedEnvelope();
            }
        }
        return null;
    }

    //TODO - Destructor for Swing Item with Maplayer Dispose

    /**
     * Private extension of JMapPane.
     */
    private class ExtJMapPane extends JMapPane {
        private Rectangle rect;
        private Envelope2D selectedEnv;

        public ExtJMapPane(MapContent content) {
            super(content);
        }

        public void setDrawRect(Rectangle rectangle) {
            this.rect = rectangle;
        }

        public void setSelectedEnvelope(Envelope2D env) {
            this.selectedEnv = env;
        }

        public Envelope2D getSelectedEnvelope() {
            return this.selectedEnv;
        }

        @Override
        public void repaint() {
            super.repaint();
            if (this.rect != null) {
                Graphics2D graphic =
                    (Graphics2D)this.getGraphics();
                graphic.setColor(Color.WHITE);
                graphic.setXORMode(Color.RED);
                graphic.drawRect(
                    rect.x,
                    rect.y,
                    rect.width,
                    rect.height);
            }
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
