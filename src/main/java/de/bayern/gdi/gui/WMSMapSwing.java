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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Parent;
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.map.MapContent;
import org.geotools.map.WMSLayer;
import org.geotools.ows.ServiceException;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.wms.WMSLayerChooser;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public class WMSMapSwing extends Parent {

    private static final Logger log
            = Logger.getLogger(WMSMapFX.class.getName());

    public WMSMapSwing() {

    }

    public WMSMapSwing(String mapURL) throws MalformedURLException {
        this(new URL(mapURL));
    }

    public WMSMapSwing(URL mapURL) {
        try {
            WebMapServer wms = new WebMapServer(mapURL);
            List<Layer> wmsLayers = WMSLayerChooser.showSelectLayer(wms);
            if (wmsLayers == null) {
                throw new ServiceException("could not connect to url");
            }
            MapContent mapcontent = new MapContent();
            mapcontent.setTitle(wms.getCapabilities().getService().getTitle());

            for (Layer wmsLayer : wmsLayers) {
                WMSLayer displayLayer = new WMSLayer(wms, wmsLayer);
                mapcontent.addLayer(displayLayer);
            }
            // Now display the map
            JMapFrame.showMap(mapcontent);
        } catch (IOException | ServiceException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
