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

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.ows.ServiceException;


/**
 * This class is going to Manage the Display of a Map based on a WFS Service.
 * It should have some widgets to zoom and to draw a Bounding Box.
 */
public class WMSMap extends Parent {

    //http://docs.geotools.org/latest/userguide/tutorial/raster/image.html
    //https://github.com/rafalrusin/geotools-fx-test/blob/master/src/geotools
    // /fx/test/GeotoolsFxTest.java
    private String outerBBOX;
    private String serviceURL;
    private String serviceName;
    private int dimensionX;
    private int dimensionY;
    private static final String FORMAT = "image/png";
    private static final boolean TRANSPARACY = true;
    private static final String INIT_SPACIAL_REF_SYS = "EPSG:31468";
    private static final int INIT_LAYER_NUMBER = 1;
    private String spacialRefSystem;
    WebMapServer wms;
    private static final Logger log
            = Logger.getLogger(WMSMap.class.getName());
    private WMSCapabilities capabilities;
    private List layers;
    private VBox vBox;
    private Label sourceLabel;
    private ImageView iw;
    private Group ig;

    private TextField epsgField;
    private TextField boundingBoxField;
    private Button updateImageButton;

    private int markerCount;

    private double mouseXPosOnClick;
    private double mouseYPosOnClick;

    private double previousMouseXPosOnClick;
    private double previousMouseYPosOnClick;

    private static final double DRAGGING_OFFSET = 4;
    private Group boxGroup;

    /**
     * gets the children of this node.
     * @return the children of the node
     */
    @Override
    public ObservableList getChildren() {
        return super.getChildren();
    }

    /**
     * adds a node to this map.
     * @param n the node
     */
    public void add(Node n) {
        this.vBox.getChildren().remove(n);
        this.vBox.getChildren().add(n);
    }

    /**
     * Constructor.
     * @param serviceURL URL of the Service
     * @param serviceName Name of the Service
     * @param outerBBOX Outer Bounds of the Picture
     * @param dimensionX X Dimension of the picuter
     * @param dimensionY Y Dimenstion of the Picture
     * @param spacialRefSystem Spacial Ref System ID
     */
    public WMSMap(String serviceURL,
                  String serviceName,
                  String outerBBOX,
                  int dimensionX,
                  int dimensionY,
                  String spacialRefSystem) {
        this.serviceURL = serviceURL;
        this.serviceName = serviceName;
        this.outerBBOX = outerBBOX;
        // TODO:
        // Make subclass from OuterBBOX with fouir fields, for x1, x2, y1, y2
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.spacialRefSystem = spacialRefSystem;
        this.iw = new ImageView();
        this.epsgField = new TextField(this.spacialRefSystem);
        this.boundingBoxField = new TextField(this.outerBBOX);
        this.updateImageButton = new Button("Update Image");
        vBox = new VBox();
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, new
                OnMouseReleasedEvent());
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, new
                OnMousePressedEvent());
        this.addEventHandler(MouseEvent.MOUSE_PRESSED, new
                OnMousePressedEvent());
        this.addEventHandler(ScrollEvent.SCROLL, new
                OnMouseScrollEvent());
        this.updateImageButton.setOnAction(
                new UpdateImageButtonEventHandler()
        );
        try {
            URL serviceURLObj = new URL(this.serviceURL);
            wms = new WebMapServer(serviceURLObj);
            capabilities = wms.getCapabilities();
            layers = capabilities.getLayerList();
            this.ig = new Group();
            boxGroup = new Group();
            this.setMapImage(this.outerBBOX,
                    this.spacialRefSystem,
                    this.INIT_LAYER_NUMBER);

            sourceLabel = new Label(this.serviceName);
            sourceLabel.setLabelFor(this.ig);
            this.ig.getChildren().add(boxGroup);
            this.add(ig);
            this.add(sourceLabel);
            this.add(epsgField);
            this.add(boundingBoxField);
            this.add(updateImageButton);
            this.getChildren().add(vBox);

        } catch (IOException | ServiceException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Constructor.
     * @param serviceURL URL of the Service
     * @param serviceName Name of the Service
     * @param outerBBOX Outer Bounds of the Picture
     * @param dimensionX X Dimension of the picuter
     * @param dimensionY Y Dimenstion of the Picture
     */
    public WMSMap(String serviceURL,
                  String serviceName,
                  String outerBBOX,
                  int dimensionX,
                  int dimensionY) {
        this(serviceURL,
                serviceName,
                outerBBOX,
                dimensionX,
                dimensionY,
                INIT_SPACIAL_REF_SYS);
    }

    /**
     * Constructor.
     */
    public WMSMap() {
    }

    /**
     * sets the Map Image.
     * @param bBox the Bounding Box
     * @param spacialRefSys The EPSG of the Bounding Box
     * @param layerNumber The number of the Layer
     */
    private void setMapImage(String bBox,
                             String spacialRefSys,
                             int layerNumber) {
        try {
            boxGroup.getChildren().clear();
            GetMapRequest request = wms.createGetMapRequest();
            request.setFormat(this.FORMAT);
            request.setDimensions(this.dimensionX, this.dimensionY);
            request.setTransparent(this.TRANSPARACY);
            request.setSRS(spacialRefSys);
            request.setBBox(bBox);
            this.outerBBOX = bBox;
            this.spacialRefSystem = spacialRefSys;
            request.addLayer((Layer) layers.get(layerNumber));

            GetMapResponse response
                    = (GetMapResponse) wms.issueRequest(request);
            log.log(Level.INFO, "WMS Call for Map Image: "
                    + request.getFinalURL().toString());
            Image im = new Image(response.getInputStream());
            this.ig.getChildren().clear();
            this.iw = new ImageView();
            this.iw.setImage(im);
            this.ig.getChildren().add(this.iw);
        } catch (IOException | ServiceException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            this.errorPopup(e);
        }
    }

    /**
     * gets the referenced Evelope from the Map.
     * @return the reference Evelope
     */
    public String getBounds() {
        return this.outerBBOX;
    }

    /**
     * gets the spacial reference system.
     * @return spacial ref system
     */
    public String getSpacialRefSystem() {
        return this.spacialRefSystem;
    }

    /**
     * raises a dialogue with an exception.
     * @param ex the exception
     */
    public void errorPopup(Exception ex) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Something went wrong");
        alert.setHeaderText("An Excpetion was raised!");
        alert.setContentText(ex.getMessage());


// Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

// Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    private void zoomIn() {
        System.out.println("Zoom In");
    }

    private void zoomOut() {
        System.out.println("Zomm Out");
    }

    private static final double HUNDRED = 100d;

    private void drawMarker(double xPosition, double yPosition) {
        double markerSpan = this.iw.getImage().getWidth() / HUNDRED;
        double upperLeftX = xPosition - markerSpan;
        double upperLeftY = yPosition + markerSpan;
        double upperRightX = xPosition + markerSpan;
        double upperRightY = yPosition + markerSpan;
        double lowerLeftX = xPosition - markerSpan;
        double lowerLeftY = yPosition - markerSpan;
        double lowerRightX = xPosition + markerSpan;
        double lowerRightY = yPosition - markerSpan;
        Line upperLeftToLowerRight = new Line(upperLeftX, upperLeftY,
                lowerRightX, lowerRightY);
        Line upperRightToLowerLeft = new Line(upperRightX, upperRightY,
                lowerLeftX, lowerLeftY);
        upperLeftToLowerRight.setFill(null);
        upperLeftToLowerRight.setStroke(Color.RED);
        upperLeftToLowerRight.setStrokeWidth(2);
        upperRightToLowerLeft.setFill(null);
        upperRightToLowerLeft.setStroke(Color.RED);
        upperRightToLowerLeft.setStrokeWidth(2);
        boxGroup.getChildren().add(upperLeftToLowerRight);
        boxGroup.getChildren().add(upperRightToLowerLeft);
    }

    private void drawBox(double beginX, double beginY, double endX, double
            endY) {
        Line upperLine = new Line(beginX, beginY, endX, beginY);
        upperLine.setFill(null);
        upperLine.setStroke(Color.RED);
        upperLine.setStrokeWidth(2);
        boxGroup.getChildren().add(upperLine);

        Line leftLine = new Line(beginX, beginY, beginX, endY);
        leftLine.setFill(null);
        leftLine.setStroke(Color.RED);
        leftLine.setStrokeWidth(2);
        boxGroup.getChildren().add(leftLine);

        Line buttomLine = new Line(beginX, endY, endX, endY);
        buttomLine.setFill(null);
        buttomLine.setStroke(Color.RED);
        buttomLine.setStrokeWidth(2);
        boxGroup.getChildren().add(buttomLine);

        Line rightLine = new Line(endX, beginY , endX, endY);
        rightLine.setFill(null);
        rightLine.setStroke(Color.RED);
        rightLine.setStrokeWidth(2);
        boxGroup.getChildren().add(rightLine);
    }
    /**
     * Event Handler for the choose Service Button.
     */
    private class UpdateImageButtonEventHandler implements
            EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            setMapImage(boundingBoxField.getText(),
                    epsgField.getText(),
                    INIT_LAYER_NUMBER);
        }
    }

    /** Eventhandler for mouse events on map. */
    private class OnMousePressedEvent
            implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            //WHEN ON SAME X,Y SET MARKER, WEHN MARKER SET, MAKE BBBOX, WHEN
            //ON DIFFERENT, MOVE MAP. WHEN DOUBLE LEFT-CLICKED, ZOOM IN, WHEN
            //DOUBLE RIGHT, ZOOM OUT
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                if (e.getClickCount() > 1) {
                    zoomIn();
                }
                if (e.getClickCount() == 1) {
                    mouseXPosOnClick = e.getX();
                    mouseYPosOnClick = e.getY();
                }
            }
            if (e.getButton().equals(MouseButton.SECONDARY)) {
                if (e.getClickCount() > 1) {
                    zoomOut();

                }
                if (e.getClickCount() == 1) {
                    boxGroup.getChildren().clear();
                }
            }
        }
    }

    /** Eventhandler for mouse events on map. */
    private class OnMouseReleasedEvent
            implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            //SAVE STATES WHEN MOUSE IS RELEASED TO DETERMINE IF DRAGGED OR
            //IF MARKER WAS SET
            if (e.getX() < (mouseXPosOnClick + DRAGGING_OFFSET)
                    && e.getX() > (mouseXPosOnClick - DRAGGING_OFFSET)
                    && e.getY() < (mouseYPosOnClick + DRAGGING_OFFSET)
                    && e.getY() > (mouseYPosOnClick - DRAGGING_OFFSET)) {
                System.out.println("Maker Set");
                drawMarker(mouseXPosOnClick, mouseYPosOnClick);
                markerCount++;
                if (markerCount == 2) {
                    //TODO: Bounding Box
                    if (mouseXPosOnClick > previousMouseXPosOnClick) {
                        drawBox(mouseXPosOnClick, mouseYPosOnClick,
                                previousMouseXPosOnClick,
                                previousMouseYPosOnClick);
                    } else {
                        drawBox(previousMouseXPosOnClick,
                                previousMouseYPosOnClick, mouseXPosOnClick,
                                mouseYPosOnClick);
                    }
                    System.out.println("Draw Bounding-Box");
                } else if (markerCount > 2) {
                    boxGroup.getChildren().clear();
                    markerCount = 0;
                }
                previousMouseXPosOnClick = mouseXPosOnClick;
                previousMouseYPosOnClick = mouseYPosOnClick;
            } else {
                System.out.println("Dragged image");
                //TODO: Calculate new Coordinates for Picture
                boxGroup.getChildren().clear();
                markerCount = 0;
            }
        }
    }

    /** Eventhandler for mouse events on map. */
    private class OnMouseScrollEvent
            implements EventHandler<ScrollEvent> {
        @Override
        public void handle(ScrollEvent e) {
            //WHEN SCROLLED IN, ZOOOM IN, WHEN SCROLLED OUT, ZOOM OUT
            if (e.getDeltaY() > 0) {
                zoomOut();
            }
            if (e.getDeltaY() < 0) {
                zoomIn();
            }
        }
    }

    //Does not work, because the map itself aint an Input-Field
    /*
    private class OnPressedPlusOrMinusEvent
            implements EventHandler<KeyEvent> {
        @Override
        public void handle(KeyEvent e) {
            //WHEN PRESSED PLUS, ZOOOM IN, WHEN PRESSED MINUS, ZOOM OUT
            System.out.println(e.getCharacter());
            System.out.println(e.getCode());
            if (e.getCode() == KeyCode.MINUS) {
                zoomOut();
            }
            if (e.getCode() == KeyCode.PLUS) {
                zoomIn();
            }
        }
    }
    */
}
