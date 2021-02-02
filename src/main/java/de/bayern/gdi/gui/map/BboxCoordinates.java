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

package de.bayern.gdi.gui.map;

import de.bayern.gdi.utils.I18n;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bounding box coordinates.
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class BboxCoordinates {

    private static final Logger LOG = LoggerFactory.getLogger(BboxCoordinates.class.getName());

    private static final Double HOUNDREDTHOUSAND = 100000.0D;

    private TextField coordinateX1TextField;
    private TextField coordinateY1TextField;
    private TextField coordinateX2TextField;
    private TextField coordinateY2TextField;
    private Label coordinateX1Label;
    private Label coordinateX2Label;
    private Label coordinateY1Label;
    private Label coordinateY2Label;
    private CoordinateReferenceSystem displayCRS;
    private CoordinateReferenceSystem oldDisplayCRS;
    private Button applyCoordsToMap;
    private MapHandler wmsMapHandler;

    /**
     * Sets the CRS the coords under the map should be displayed in.
     *
     * @param crs Coordinate Reference System
     * @throws FactoryException when the CRS can't be found
     */
    public void setDisplayCRS(String crs) throws FactoryException {
        CoordinateReferenceSystem coordinateReferenceSystem = CRS.decode(crs);
        setDisplayCRS(coordinateReferenceSystem);
    }

    /**
     * Sets the CRS to Display the coordinates under the map.
     *
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
                .isEmpty()
                && !this.coordinateY1TextField.getText()
                .isEmpty()
                && !this.coordinateX2TextField.getText()
                .isEmpty()
                && !this.coordinateY2TextField.getText().
                isEmpty()) {
                Double x1Coordinate = Double.parseDouble(
                    this.coordinateX1TextField.getText());
                Double x2Coordinate = Double.parseDouble(
                    this.coordinateX2TextField.getText());
                Double y1Coordinate = Double.parseDouble(
                    this.coordinateY1TextField.getText());
                Double y2Coordinate = Double.parseDouble(
                    this.coordinateY2TextField.getText());
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
                        LOG.error(e.getMessage(), e);
                    }
                } else {
                    clearCoordinateDisplay();
                }
            } else {
                clearCoordinateDisplay();
            }
        }
    }

    /**
     * Calculates the bounds for 4 different text fields.
     *
     * @param crs the CRS of the Bounding Box
     * @return the bounding box
     */
    public Envelope2D getBounds(CoordinateReferenceSystem crs) {
        if (coordinateX1TextField != null
            && coordinateX2TextField != null
            && coordinateY1TextField != null
            && coordinateY2TextField != null
            && !coordinateX1TextField.getText().isEmpty()
            && !coordinateX2TextField.getText().isEmpty()
            && !coordinateY1TextField.getText().isEmpty()
            && !coordinateY2TextField.getText().isEmpty()) {
            Double x1Coordinate = Double.parseDouble(
                coordinateX1TextField.getText());
            Double x2Coordinate = Double.parseDouble(
                coordinateX2TextField.getText());
            Double y1Coordinate = Double.parseDouble(
                coordinateY1TextField.getText());
            Double y2Coordinate = Double.parseDouble(
                coordinateY2TextField.getText());
            Envelope env = new ReferencedEnvelope(
                x1Coordinate,
                x2Coordinate,
                y1Coordinate,
                y2Coordinate,
                crs);
            return new Envelope2D(env);
        }
        return null;
    }

    /**
     * Sets text fields for coordinates.
     *
     * @param x1 x1
     * @param x2 x2
     * @param y1 y1
     * @param y2 y2
     */
    public void setCoordinateDisplay(
        TextField x1,
        TextField x2,
        TextField y1,
        TextField y2) {
        this.coordinateX1TextField = x1;
        this.coordinateY1TextField = y1;
        this.coordinateX2TextField = x2;
        this.coordinateY2TextField = y2;

        this.coordinateX1TextField.setOnMouseClicked(event -> {
            enableApplyCoordsToMapInput();
        });
        this.coordinateY1TextField.setOnMouseClicked(event -> {
            enableApplyCoordsToMapInput();
        });
        this.coordinateX2TextField.setOnMouseClicked(event -> {
            enableApplyCoordsToMapInput();
        });
        this.coordinateY2TextField.setOnMouseClicked(event -> {
            enableApplyCoordsToMapInput();
        });
    }

    /**
     * Sets the Labels.
     *
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

    /**
     * Sets the coordinates to the display.
     *
     * @param x1     x1
     * @param x2     y1
     * @param y1     x2
     * @param y2     y2
     * @param mapCRS CRS of the map
     */
    public void setDisplayCoordinates(
        Double x1,
        Double x2,
        Double y1,
        Double y2,
        CoordinateReferenceSystem mapCRS) {
        try {
            convertAndDisplayBoundingBox(x1,
                x2,
                y1,
                y2,
                mapCRS,
                this.displayCRS);
        } catch (FactoryException | TransformException e) {
            clearCoordinateDisplay();
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Register {@link MapHandler}.
     *
     * @param wmsMapHandlerToRegister handler to register
     */
    public void registerWmsMapHandler(MapHandler wmsMapHandlerToRegister) {
        this.wmsMapHandler = wmsMapHandlerToRegister;
        if (applyCoordsToMap != null) {
            this.applyCoordsToMap.setOnAction(event -> {
                Envelope2D bounds = getBounds(displayCRS);
                wmsMapHandler.applyBbox(bounds);
            });
        }
    }

    /**
     * Sets the button to apply the bbox from coordinate input.
     *
     * @param applyCoordsToMapBt button
     */
    public void setApplyCoordsToMapButton(Button applyCoordsToMapBt) {
        this.applyCoordsToMap = applyCoordsToMapBt;
    }

    /**
     * Disables the button to apply the bbox from coordinate input.
     */
    public void disableApplyCoordsToMapInput() {
        this.applyCoordsToMap.setDisable(true);
    }

    /**
     * Enables the button to apply the bbox from coordinate input.
     */
    public void enableApplyCoordsToMapInput() {
        this.applyCoordsToMap.setDisable(false);
    }

    private void convertAndDisplayBoundingBox(
        Double x1,
        Double x2,
        Double y1,
        Double y2,
        CoordinateReferenceSystem sourceCRS,
        CoordinateReferenceSystem targetCRS
    ) throws TransformException, FactoryException {
        org.locationtech.jts.geom.Point p1 = convertDoublesToPoint(
            x1,
            y1,
            sourceCRS,
            targetCRS);
        org.locationtech.jts.geom.Point p2 = convertDoublesToPoint(
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
            Platform.runLater(() -> {
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
            });
        }
    }

    private org.locationtech.jts.geom.Point convertDoublesToPoint(
        Double x,
        Double y,
        CoordinateReferenceSystem sourceCRS,
        CoordinateReferenceSystem targetCRS)
        throws TransformException, FactoryException {
        org.locationtech.jts.geom.GeometryFactory gf = new
            org.locationtech.jts.geom.GeometryFactory();
        org.locationtech.jts.geom.Coordinate coo = new
            org.locationtech.jts.geom.Coordinate(x, y);
        org.locationtech.jts.geom.Point p = gf.createPoint(coo);
        MathTransform transform = CRS.findMathTransform(
            sourceCRS, targetCRS);
        return (org.locationtech.jts.geom.Point) JTS.transform(p, transform);
    }

    /**
     * Clears the coordinates display.
     */
    public void clearCoordinateDisplay() {
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

}
