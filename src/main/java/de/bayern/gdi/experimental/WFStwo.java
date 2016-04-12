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

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class WFStwo {

    private String url;
    private Vector<String> types = new Vector();
    private DataStore data;

    /**
     * Constructor.
     * @param url URL to Service
     * @throws Exception when something goes wrong
     */
    public WFStwo(String url) throws Exception {
        this.url = url;
        Iterator<DataStoreFactorySpi> fi
                = DataStoreFinder.getAvailableDataStores();

        while (fi.hasNext()) {
            DataStoreFactorySpi f = fi.next();
            /*
            System.out.println("\t'"
                    + f.getDisplayName() + "': '"
                    + f.getDescription() + "'");
                    */
        }

        Map connectionParameters = new HashMap();
        connectionParameters.put(
                "WFSDataStoreFactory:GET_CAPABILITIES_URL", this.url);

        // Step 2 - connection
        this.data = DataStoreFinder.getDataStore(connectionParameters);

        //System.out.println("data store class: " + data.getClass());

        // Step 3 - discovery
        String [] typeNames = data.getTypeNames();


        for (String tName: typeNames) {
            this.types.add(tName);
        }

        /*
        SimpleFeatureType schema = data.getSchema(typeName);

        System.out.println("schema class: " + schema.getClass());

        // Step 4 - target
        FeatureSource<SimpleFeatureType, SimpleFeature>
                source = data.getFeatureSource(typeName);

        ReferencedEnvelope bbox = source.getBounds();
        System.out.println("Metadata Bounds:" + bbox);

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
*/
        /*
        // Step 5 - query

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(
                GeoTools.getDefaultHints());

        Object polygon = JTS.toGeometry(BBOX);

        Filter filter = ff.bbox(
                ff.property(geomName),
                new ReferencedEnvelope(
                        BBOX, bbox.getCoordinateReferenceSystem()));

        Query query = new Query(typeName,
                filter,
                new String[]{geomName});

        FeatureCollection<SimpleFeatureType, SimpleFeature>
                features = source.getFeatures(query);

        ReferencedEnvelope bounds = new ReferencedEnvelope();
        try (FeatureIterator<SimpleFeature> iterator = features.features()) {
            int count = 0;
            while (iterator.hasNext()) {
                Feature feature = iterator.next();
                ++count;
                System.out.println(count + " " + feature.getBounds());
                bounds.include(feature.getBounds());
            }
        }
        System.out.println("Calculated Bounds:" + bounds);
        */
    }

    /**
     * gets the types of a service.
     * @return the types
     */
    public Vector<String> getTypes() {
        return this.types;
    }

    /**
     * gets the attributes of a tye.
     * @param type type to get attributes of
     * @return the attributes
     * @throws Exception when something goes wrong
     */
    public Vector<AttributeType> getAttributes(String type) throws Exception {
        Vector<AttributeType> attributes = new Vector();
        SimpleFeatureType schema = this.data.getSchema(type);

        FeatureSource<SimpleFeatureType, SimpleFeature>
                source = data.getFeatureSource(type);

        //System.out.println("types:");
        for (AttributeType aType: schema.getTypes()) {
            attributes.add(aType);
        }
        return attributes;
    }
}
