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

import com.vividsolutions.jts.geom.Envelope;

/**
 * This class is going to Manage the Display of a Map based on a WFS Service
 * It should have some widgets to zoom and to draw a Bounding Box
 */
public class WMSMap {

    //http://docs.geotools.org/latest/userguide/tutorial/raster/image.html
    private Envelope outerBBOX;
    private String serviceURL;
    private int dimensionX;
    private int dimensionY;
    private static final String FORMAT = "image/png";
    private static final boolean TRANSPARACY = true;
    private static final String INIT_SPACIAL_REF_SYS = "EPSG:4326";
    private String spacialRefSystem;

    public WMSMap(String serviceURL,
                  Envelope outerBBOX,
                  int dimensionX,
                  int dimensionY,
                  String spacialRefSystem) {
        this.serviceURL = serviceURL;
        this.outerBBOX = outerBBOX;
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.spacialRefSystem = spacialRefSystem;
    }

    public WMSMap(String serviceURL,
                  Envelope outerBBOX,
                  int dimensionX,
                  int dimensionY) {
        this(serviceURL,
                outerBBOX,
                dimensionX,
                dimensionY,
                INIT_SPACIAL_REF_SYS);

    }

}
