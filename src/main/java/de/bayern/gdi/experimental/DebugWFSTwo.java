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

import de.bayern.gdi.services.WFSTwo;
import java.net.URL;
import java.util.ArrayList;
import net.opengis.wfs20.StoredQueryListItemType;
import net.opengis.wfs20.impl.ListStoredQueriesResponseTypeImpl;
import org.geotools.xml.Parser;
import org.xml.sax.InputSource;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class DebugWFSTwo {

    private WFSTwo wfstwo;
    private String urlString;


    private DebugWFSTwo(String url) {
        this.urlString = url;
    }

    /**
     * Starter Method.
     * @param args Single Argument, just a URL to a WFSTwo Service
     */
    public static void main(String[] args) {
        DebugWFSTwo dwfs = new DebugWFSTwo(args[0]);
        dwfs.go();
    }

    private void go() {
        wfstwo = new WFSTwo(this.urlString);
        printArrayStringList(wfstwo.getRequestMethods());
        printArrayStringList(wfstwo.getStoredQueries());
    }

    private ArrayList<String> storedQueires() {

        ArrayList<String> als = new ArrayList();
        org.geotools.wfs.v2_0.WFSCapabilitiesConfiguration configuration =
                new org.geotools.wfs.v2_0.WFSCapabilitiesConfiguration();
        Parser parser = new Parser(configuration);
        try {
            URL url = new URL(wfstwo.getServiceURL());
            InputSource xml = new InputSource(url.openStream());
            Object parsed = parser.parse(xml);
            net.opengis.wfs20.impl.ListStoredQueriesResponseTypeImpl caps =
                    (ListStoredQueriesResponseTypeImpl) parsed;
            net.opengis.wfs20.StoredQueryListItemType om =
                    (StoredQueryListItemType) caps.getStoredQuery();
            als.add(om.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


        /* WORKING - Returns Names of features
        try {
            List<Name> names = wfstwo.getData().getNames();
            for(Iterator<Name> it = names.iterator(); it.hasNext();) {
                als.add(it.next().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        /* NON WORKING
        try {
            SimpleFeatureSource featureSource
                   = wfstwo.getData().getFeatureSource("DescribeStoredQueries");
            SimpleFeatureCollection features = featureSource.getFeatures();
            SimpleFeatureIterator features1 = features.features();

            for (; features1.hasNext(); )  {
                SimpleFeature next = features1.next();
                als.add(next.getID());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        /* Non Working
        Transaction t = new DefaultTransaction("handle");
        Query query = new Query("DescribeFeatureType");



        try {
            FeatureReader<SimpleFeatureType, SimpleFeature> featureReader
                    = wfstwo.getData().getFeatureReader(query, t);
            for(; featureReader.hasNext(); ){
                SimpleFeature f = featureReader.next();
                System.out.println(f.toString());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        */
        return als;
    }

    private void printArrayStringList(ArrayList<String> als) {
        for (String str : als) {
            System.out.println(str);
        }
    }
}
