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

import com.sothawo.mapjfx.MapView;
import de.bayern.gdi.config.ServiceSettings;
import javafx.event.EventTarget;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */


public class MapHandlerBuilder {

    private static ServiceSettings serviceSettings;
    private BboxCoordinates bboxCoordinates;
    private EventTarget eventTarget;
    private MapView mapView;
    private MapActionToolbar mapActionToolbar;

    /**
     * Instantiates a new {@link MapHandlerBuilder}.
     *
     * @param serviceSettingsToSet never <code>null</code>
     * @return the {@link MapHandlerBuilder} instance, never <code>null</code>
     */
    public static MapHandlerBuilder newBuilder(
        ServiceSettings serviceSettingsToSet) {
        MapHandlerBuilder.serviceSettings = serviceSettingsToSet;
        return new MapHandlerBuilder();
    }

    /**
     * @param eventTargetToSet target of the {@link PolygonClickedEvent},
     *                    never <code>null</code>
     * @return the {@link MapHandlerBuilder} instance, never <code>null</code>
     */
    public MapHandlerBuilder withEventTarget(EventTarget eventTargetToSet) {
        this.eventTarget = eventTargetToSet;
        return this;
    }

    /**
     * @param mapViewToSet {@link MapView} component to handle,
     *                never <code>null</code>
     * @return the {@link MapHandlerBuilder} instance, never<code>null</code>
     */
    public MapHandlerBuilder withMapView(MapView mapViewToSet) {
        this.mapView = mapViewToSet;
        return this;
    }

    /**
     * @param wmsSourceLabel label to display the source of the map,
     *                       never <code>null</code>
     * @return the {@link MapHandlerBuilder} instance, never <code>null</code>
     */
    public MapHandlerBuilder withWmsSourceLabel(Label wmsSourceLabel) {
        String wmsSource = serviceSettings.getWMSSource();
        if (wmsSource != null) {
            wmsSourceLabel.setText(wmsSource);
        }
        return this;
    }

    /**
     * @param bboxButton button used to draw a rectangle on the map,
     *                   never <code>null</code>
     * @return the {@link MapHandlerBuilder} instance, never <code>null</code>
     */
    public MapHandlerBuilder withBboxButton(ToggleButton bboxButton) {
        initToolbar();
        this.mapActionToolbar.setBboxButton(bboxButton);
        return this;
    }

    /**
     * @param selectButton button used to select features,
     *                     never <code>null</code>
     * @return the {@link MapHandlerBuilder} instance, never <code>null</code>
     */
    public MapHandlerBuilder withSelectButton(ToggleButton selectButton) {
        initToolbar();
        this.mapActionToolbar.setSelectButtton(selectButton);
        return this;
    }

    /**
     * @param infoButton button used to get feature info,
     *                   never <code>null</code>
     * @return the {@link MapHandlerBuilder} instance, never <code>null</code>
     */
    public MapHandlerBuilder withInfoButton(ToggleButton infoButton) {
        initToolbar();
        this.mapActionToolbar.setInfoButton(infoButton);
        return this;
    }

    /**
     * @param resizeButton button used to resize the map,
     *                     never <code>null</code>
     * @return the {@link MapHandlerBuilder} instance, never <code>null</code>
     */
    public MapHandlerBuilder withResizeButtton(Button resizeButton) {
        initToolbar();
        this.mapActionToolbar.setResizeButtton(resizeButton);
        return this;
    }

    /**
     * sets text fields for coordinates.
     *
     * @param textX1 x1
     * @param textX2 y1
     * @param textY1 x2
     * @param textY2 y2
     * @return the {@link MapHandlerBuilder} instance, never <code>null</code>
     */
    public MapHandlerBuilder withCoordinateDisplay(
        TextField textX1,
        TextField textX2,
        TextField textY1,
        TextField textY2) {
        initBboxCoordinates();
        bboxCoordinates.setCoordinateDisplay(textX1, textX2, textY1, textY2);
        return this;
    }

    /**
     * Sets the Labels.
     *
     * @param labelx1 label x1
     * @param labelx2 label x2
     * @param labely1 label y1
     * @param labely2 label y2
     * @return the {@link MapHandlerBuilder} instance, never <code>null</code>
     */
    public MapHandlerBuilder withCoordinateLabel(
        Label labelx1,
        Label labelx2,
        Label labely1,
        Label labely2
    ) {
        initBboxCoordinates();
        bboxCoordinates.setCoordinateLabel(labelx1, labelx2, labely1, labely2);
        return this;
    }

    /**
     * Sets the Button to apply the bbox from coordinate input.
     *
     * @param applyCoordsToMap button
     * @return the {@link MapHandlerBuilder} instance, never <code>null</code>
     */
    public MapHandlerBuilder withApplyCoordsToMapButton(
        Button applyCoordsToMap) {
        initBboxCoordinates();
        bboxCoordinates.setApplyCoordsToMapButton(applyCoordsToMap);
        return this;
    }

    /**
     * Builds the MapHandler instance.
     *
     * @return the MapHandler instance, never <code>null</code>
     */
    public MapHandler build() {
        MapHandler wmsMapHandler = new MapHandler(
            serviceSettings,
            eventTarget,
            mapView,
            bboxCoordinates,
            mapActionToolbar);
        if (bboxCoordinates != null) {
            this.bboxCoordinates.registerWmsMapHandler(wmsMapHandler);
        }
        this.mapActionToolbar.registerResizeHandler(wmsMapHandler);
        return wmsMapHandler;
    }


    private void initBboxCoordinates() {
        if (this.bboxCoordinates == null) {
            this.bboxCoordinates = new BboxCoordinates();
        }
    }

    private void initToolbar() {
        if (this.mapActionToolbar == null) {
            this.mapActionToolbar = new MapActionToolbar();
        }
    }
}
