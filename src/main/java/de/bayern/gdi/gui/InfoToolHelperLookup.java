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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import org.geotools.map.Layer;
import org.geotools.swing.tool.InfoToolHelper;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

class InfoToolHelperLookup {
    private static final Logger log
            = Logger.getLogger(InfoToolHelperLookup.class.getName());

    private static List<InfoToolHelper> cachedInstances;

    private InfoToolHelperLookup() {
    }

    /**
     * gets the helper for the given layer.
     * @param layer the layer
     * @return the helper
     */
    public static InfoToolHelper getHelper(Layer layer) {
        loadProviders();

        for (InfoToolHelper helper : cachedInstances) {
            try {
                if (helper.isSupportedLayer(layer)) {
                    return helper.getClass().newInstance();
                }

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        return null;
    }

    /**
     * Caches available classes which implement the InfoToolHelper SPI.
     */
    private static void loadProviders() {
        if (cachedInstances == null) {
            cachedInstances = new ArrayList<InfoToolHelper>();

            ServiceLoader<InfoToolHelper> loader =
                    ServiceLoader.load(InfoToolHelper.class);

            Iterator<InfoToolHelper> iter = loader.iterator();
            while (iter.hasNext()) {
                cachedInstances.add(iter.next());
            }
        }
    }

}
