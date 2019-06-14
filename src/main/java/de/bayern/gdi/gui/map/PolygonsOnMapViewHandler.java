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

import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.CoordinateLine;
import com.sothawo.mapjfx.MapView;
import javafx.scene.paint.Color;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.GeometryAttributeImpl;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.bayern.gdi.gui.map.MapHandler.POLYGON_LAYER_TITLE;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class PolygonsOnMapViewHandler implements MapLayerListListener {

    private static final Logger LOG
        = LoggerFactory.getLogger(PolygonsOnMapViewHandler.class.getName());

    private static final Color OUTLINE = Color.rgb(0, 0, 0, 0.8);
    private static final Color SELECTED_FILL = Color.rgb(255, 255, 0, 0.4);
    private static final Color FILL = Color.rgb(0, 255, 255, 0.4);

    private final Map<String, CoordinateLine> polygonFeatures = new HashMap<>();

    private final MapView mapView;
    private CoordinateLine highlightedCoordinateLine;

    /**
     * Constructor.
     *
     * @param mapView mapView, never <code>null</code>
     */
    public PolygonsOnMapViewHandler(MapView mapView) {
        this.mapView = mapView;
    }

    @Override
    public void layerAdded(MapLayerListEvent mapLayerListEvent) {
        MapLayer layer = mapLayerListEvent.getLayer();
        if (POLYGON_LAYER_TITLE.equals(layer.getTitle())) {
            try {
                FeatureIterator features = layer.toLayer().getFeatureSource()
                    .getFeatures().features();
                while (features.hasNext()) {
                    Feature feature = features.next();
                    Property id = feature.getProperty("id");
                    Property geometry = feature.getProperty("geometry");
                    if (id != null && geometry != null) {
                        Polygon polygon = (Polygon) ((GeometryAttributeImpl)
                            geometry).getValue();
                        String polygonId = id.getValue().toString();
                        createAndAddCoordinateLine(polygonId, polygon);
                    }
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void layerRemoved(MapLayerListEvent mapLayerListEvent) {
        String title = mapLayerListEvent.getLayer().getTitle();
        if (POLYGON_LAYER_TITLE.equals(title)) {
            for (CoordinateLine cl : polygonFeatures.values()) {
                mapView.removeCoordinateLine(cl);
            }
            polygonFeatures.clear();
            highlightedCoordinateLine = null;
        }
    }

    @Override
    public void layerChanged(MapLayerListEvent mapLayerListEvent) {

    }

    @Override
    public void layerMoved(MapLayerListEvent mapLayerListEvent) {

    }

    @Override
    public void layerPreDispose(MapLayerListEvent mapLayerListEvent) {

    }

    /**
     * Highlight the selected polygon.
     *
     * @param polygonId Id of the polygon to select
     */
    public void highlightSelectedPolygon(String polygonId) {
        CoordinateLine coordinateLine = polygonFeatures.get(polygonId);
        updateFill(highlightedCoordinateLine, FILL);
        updateFill(coordinateLine, SELECTED_FILL);
        this.highlightedCoordinateLine = coordinateLine;
    }

    private void updateFill(CoordinateLine coordinateLine, Color newFill) {
        if (coordinateLine != null) {
            mapView.removeCoordinateLine(coordinateLine);
            coordinateLine.setFillColor(newFill);
            mapView.addCoordinateLine(coordinateLine);
        }
    }

    private void createAndAddCoordinateLine(String polygonId, Polygon polygon) {
        org.locationtech.jts.geom.Coordinate[] externalRingCoordinates =
            polygon.getExteriorRing().getCoordinates();
        List<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < externalRingCoordinates.length; i++) {
            org.locationtech.jts.geom.Coordinate externalRingCoordinate =
                externalRingCoordinates[i];
            coordinates.add(new Coordinate(externalRingCoordinate.x,
                externalRingCoordinate.y));
        }
        CoordinateLine cl = new CoordinateLine(coordinates)
            .setColor(OUTLINE)
            .setWidth(1)
            .setFillColor(FILL)
            .setClosed(true)
            .setVisible(true);
        polygonFeatures.put(polygonId, cl);
        mapView.addCoordinateLine(cl);
    }
}
