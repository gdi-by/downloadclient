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

import org.geotools.data.ows.Layer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.geometry.jts.ReferencedEnvelope;


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
    private static final String INIT_SPACIAL_REF_SYS = "EPSG:3857";
    private String spacialRefSystem;
    WebMapServer wms;
    private static final Logger log
            = Logger.getLogger(WMSMap.class.getName());
    WMSCapabilities capabilities;

    private ImageView imView;

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
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.spacialRefSystem = spacialRefSystem;
        System.out.println(serviceURL);
        System.out.println(serviceName);

        try {
            URL serviceURLObj = new URL(this.serviceURL);
            wms = new WebMapServer(serviceURLObj);
            capabilities = wms.getCapabilities();
            List layers = capabilities.getLayerList();
            GetMapRequest request = wms.createGetMapRequest();
            request.setFormat(this.FORMAT);
            request.setDimensions(this.dimensionX, this.dimensionY);
            request.setTransparent(this.TRANSPARACY);
            request.setSRS(this.spacialRefSystem);
            request.setBBox(this.outerBBOX);
            //WMSLayer layer = new WMSLayer(wms, (Layer) layers.get(1));
            request.addLayer((Layer) layers.get(1));

            GetMapResponse response
                    = (GetMapResponse) wms.issueRequest(request);
            BufferedImage bfimage = ImageIO.read(response.getInputStream());
            WritableImage wr = null;
            if (bfimage != null) {
                wr = new WritableImage(bfimage.getWidth(), bfimage.getHeight());
                PixelWriter pw = wr.getPixelWriter();
                for (int x = 0; x < bfimage.getWidth(); x++) {
                    for (int y = 0; y < bfimage.getHeight(); y++) {
                        pw.setArgb(x, y, bfimage.getRGB(x, y));
                    }
                }
            }
            this.imView = new ImageView(wr);

        } catch (IOException | org.geotools.ows.ServiceException e) {
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

    public ReferencedEnvelope getBounds() {
        return new ReferencedEnvelope();
    }
}
