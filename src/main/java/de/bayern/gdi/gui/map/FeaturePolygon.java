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


import org.locationtech.jts.geom.Polygon;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Represents all Infos needed for drawing a Polyon.
 **/
public class FeaturePolygon {

    /**
     * the polygon.
     **/
    private Polygon polygon;
    /**
     * name of the polygon.
     **/
    private String name;
    /**
     * id of the polygon.
     **/
    private String id;

    /**
     * crs of the polygon.
     */
    private CoordinateReferenceSystem crs;

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

    /**
     * @return the polygon.
     */
    public Polygon getPolygon() {
        return polygon;
    }

    /**
     * @return name of the polygon.
     */
    public String getName() {
        return name;
    }

    /**
     * @return id of the polygon.
     */
    public String getId() {
        return id;
    }

    /**
     * @return crs of the polygon.
     */
    public CoordinateReferenceSystem getCrs() {
        return crs;
    }
}
