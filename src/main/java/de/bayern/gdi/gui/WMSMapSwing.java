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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
import org.geotools.map.MapContent;
import org.geotools.map.WMSLayer;
import org.geotools.ows.ServiceException;
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

import de.bayern.gdi.utils.I18n;

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
    private int mapWidth;
    private int mapHeight;
    private SwingNode mapNode;

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
    public WMSMapSwing(String mapURL, int width, int heigth) throws
            MalformedURLException {
        this(new URL(mapURL), width, heigth);
    }

    /**
     * Constructor.
     *
     * @param mapURL The URL of the WMS Service
     */
    public WMSMapSwing(URL mapURL, int width, int heigth) {
        initGeotoolsLocale();
        try {
            this.mapHeight = heigth;
            this.mapWidth = width;
            this.vBox = new VBox();
            this.wms = new WebMapServer(mapURL);
            List<Layer> layers = this.wms.getCapabilities().getLayerList();
            ObservableList<String> layerList = FXCollections
                    .observableArrayList();
            for (Layer layer : layers) {
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
            this.layerLabel.setText(I18n.getMsg("gui.layer") + ": ");
            this.mapNode = new SwingNode();
            //this.add(this.layerLabel);
            //this.add(this.wmsLayers);
            this.add(this.mapNode);
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
        WMSLayer displayLayer = new WMSLayer(this.wms, wmsLayer);
        this.mapContent.addLayer(displayLayer);
        createSwingContent(this.mapNode);
        //JMapPane mapPane = new JMapPane(this.mapContent);

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
                for (Layer lay : layers) {
                    if (layerName == lay.getName()) {
                        layer = lay;
                        break;
                    }
                }
                displayMap(layer);
            }
        }
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

                ExtJMapPane mapPane = new ExtJMapPane(mapContent);
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
                                    clickCount++;
                                } else if (clickCount == 1) {
                                    end = ev.getPoint();
                                    mapEndPos = ev.getWorldPos();
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
            }
        });
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
