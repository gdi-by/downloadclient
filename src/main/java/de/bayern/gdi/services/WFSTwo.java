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

package de.bayern.gdi.services;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class WFSTwo extends WebService {

    private static final Logger log
        = Logger.getLogger(WFSTwo.class.getName());

    private String serviceURL;

    private ArrayList<String> types;
    private DataStore data;

    private FeatureSource<SimpleFeatureType, SimpleFeature>
            source;

    /**
     * Constructor.
     * @param serviceURL URL to Service
     */
    public WFSTwo(String serviceURL) {
        this.serviceURL = serviceURL;

        Map connectionParameters = new HashMap();
        connectionParameters.put(
                "WFSDataStoreFactory:GET_CAPABILITIES_URL", this.serviceURL);

        try {
            this.data = DataStoreFinder.getDataStore(connectionParameters);
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * gets the types of a service.
     * @return the types
     */
    public ArrayList<String> getTypes() {
        if (this.types == null) {
            this.types = new ArrayList();
            try {
                String[] typeNames = this.data.getTypeNames();

                for (String tName : typeNames) {
                    this.types.add(tName);
                }
            } catch (Exception e) {
                //TODO: Be more specific about execptions
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return this.types;
    }

    /**
     * gets the attributes of a tye.
     * @param type type to get attributes of
     * @return the attributes
     */
    public ArrayList<AttributeType> getAttributes(String type) {
        ArrayList<AttributeType> attributes = new ArrayList();
        try {
            SimpleFeatureType schema = this.data.getSchema(type);
            this.source = this.data.getFeatureSource(type);
            attributes.addAll(schema.getTypes());
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return attributes;
    }

    /**
     * Experimental Class to get the Bounds of a Type.
     * @param outerBBOX the Outer Bounding Box
     * @param typeName the Type Name
     * @return the Bounds
     */
    public ReferencedEnvelope getBounds(Envelope outerBBOX,
                                        String typeName) {
        SimpleFeatureType schema = null;
        ReferencedEnvelope bbox = null;
        FeatureCollection<SimpleFeatureType, SimpleFeature>
                features = null;
        try {
            schema = this.data.getSchema(typeName);
            this.source = this.data.getFeatureSource(typeName);
            bbox = source.getBounds();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
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
            log.log(Level.SEVERE, "No geometry found.");
            return null;
            //throw new Exception("No geometry found.");
        }

        String geomName = gt.getName().getLocalPart();

        System.out.println("Using '" + geomName + "' as geometry column.");


        // Step 5 - query

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(
                GeoTools.getDefaultHints());

        Object polygon = JTS.toGeometry(outerBBOX);

        Filter filter = ff.bbox(
                ff.property(geomName),
                new ReferencedEnvelope(
                        outerBBOX, bbox.getCoordinateReferenceSystem()));

        Query query = new Query(typeName,
                filter,
                new String[]{geomName});
        try {
            features = source.getFeatures(query);
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
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
        return bounds;
    }

    /**
     * gets the service URL.
     * @return the service URL
     */
    public String getServiceURL() {
        return this.serviceURL;
    }

    /**
     * gets the dataStore.
     * @return datastore
     */
    public DataStore getData() {
        return this.data;
    }
}
