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
package de.bayern.gdi.experimental;

import java.util.Map;
//import java.util.Iterator;
import java.util.HashMap;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;

import org.geotools.data.FeatureSource;

import org.opengis.feature.simple.SimpleFeature;

//import org.geotools.geometry.jts.ReferencedEnvelope;

//import org.opengis.filter.FilterFactory2;

//import org.geotools.factory.CommonFactoryFinder;

//import org.opengis.filter.spatial.Intersects;

//import org.geotools.data.DefaultQuery;
//import org.geotools.data.Query;

//import org.opengis.feature.Feature;
//import org.geotools.feature.FeatureCollection;

import org.opengis.feature.simple.SimpleFeatureType;


/** An experimental class to check if GeoTools is capable of WFS 2.0. */
public class SimpleLoader {

    private String url;

    private static final com.vividsolutions.jts.geom.Envelope BBOX
        = new com.vividsolutions.jts.geom.Envelope(-100.0, -70, 25, 40);

    public SimpleLoader(String url) {
        this.url = url;
    }

    /** download.
     * @throws Exception if something goes wrong.
     */
    public void download() throws Exception {

        Map connectionParameters = new HashMap();
        connectionParameters.put(
            "WFSDataStoreFactory:GET_CAPABILITIES_URL", this.url);

        // Step 2 - connection
        DataStore data = DataStoreFinder.getDataStore(connectionParameters);

        // Step 3 - discouvery
        String [] typeNames = data.getTypeNames();
        String typeName = typeNames[0];
        SimpleFeatureType schema = data.getSchema(typeName);

        // Step 4 - target
        FeatureSource<SimpleFeatureType, SimpleFeature>
            source = data.getFeatureSource(typeName);
        System.out.println("Metadata Bounds:" + source.getBounds());

        /*

        // Step 5 - query
        String geomName = schema.getDefaultGeometry().getLocalName();

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(
            GeoTools.getDefaultHints());
        Object polygon = JTS.toGeometry(BBOX);
        Intersects filter = ff.intersects(
            ff.property(geomName),
            ff.literal(polygon));

        Query query = new DefaultQuery(
            typeName, filter, new String[]{geomName});
        FeatureCollection<SimpleFeatureType, SimpleFeature>
            features = source.getFeatures(query);

        ReferencedEnvelope bounds = new ReferencedEnvelope();
        Iterator<SimpleFeature> iterator = features.iterator();
        try {
            while (iterator.hasNext()) {
                Feature feature = (Feature)iterator.next();
                bounds.include(feature.getBounds());
            }
            System.out.println("Calculated Bounds:" + bounds);
        } finally {
            features.close(iterator);
        }
        */
    }
}
