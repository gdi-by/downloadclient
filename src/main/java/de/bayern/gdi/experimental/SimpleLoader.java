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
import java.util.HashMap;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;

import org.geotools.data.FeatureSource;

import org.opengis.feature.simple.SimpleFeature;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.opengis.filter.FilterFactory2;

import org.geotools.factory.CommonFactoryFinder;

import org.opengis.filter.spatial.Intersects;

//import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;

import org.opengis.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;

import org.opengis.feature.simple.SimpleFeatureType;

import org.geotools.geometry.jts.JTS;

import org.geotools.factory.GeoTools;


/** An experimental class to check if GeoTools is capable of WFS 2.0. */
public class SimpleLoader {

    private String url;

    private static final com.vividsolutions.jts.geom.Envelope BBOX
        = new com.vividsolutions.jts.geom.Envelope(
            5234456.559480272, 5609330.972506456,
            4268854.282683062, 4644619.626498722);

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

        System.out.println("data store class: " + data.getClass());

        // Step 3 - discouvery
        String [] typeNames = data.getTypeNames();
        String typeName = typeNames[0];


        for (String tName: typeNames) {
            System.out.println("\ttype: '" + tName + "'");
        }

        SimpleFeatureType schema = data.getSchema(typeName);

        System.out.println("schema class: " + schema.getClass());

        // Step 4 - target
        FeatureSource<SimpleFeatureType, SimpleFeature>
            source = data.getFeatureSource(typeName);
        System.out.println("Metadata Bounds:" + source.getBounds());

        // XXX: This is a bit cumbersome.
        GeometryType gt = null;
        System.out.println("types:");
        for (AttributeType type: schema.getTypes()) {
            System.out.println("\t'" + type + "'");
            if (type instanceof GeometryType) {
                gt = (GeometryType)type;

            }
        }

        if (gt == null) {
            throw new Exception("No geometry found.");
        }

        String geomName = gt.getName().getLocalPart();

        System.out.println("Using '" + geomName + "' as geometry column.");


        // Step 5 - query

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(
            GeoTools.getDefaultHints());
        Object polygon = JTS.toGeometry(BBOX);
        Intersects filter = ff.intersects(
            ff.property(geomName),
            ff.literal(polygon));

        Query query = Query.ALL;
        //Query query = new DefaultQuery(
        //    typeName, filter, new String[]{geomName});

        FeatureCollection<SimpleFeatureType, SimpleFeature>
            features = source.getFeatures(query);

        ReferencedEnvelope bounds = new ReferencedEnvelope();
        try (FeatureIterator<SimpleFeature> iterator = features.features()) {
            int count = 0;
            while (iterator.hasNext()) {
                Feature feature = iterator.next();
                ++count;
                System.out.println(count);
                bounds.include(feature.getBounds());
            }
        }
        System.out.println("Calculated Bounds:" + bounds);
    }
}
