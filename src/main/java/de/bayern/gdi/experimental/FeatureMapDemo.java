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

import com.vividsolutions.jts.io.WKTReader;
import java.awt.Color;
import org.geotools.data.DataUtilities;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Einfaches Beispiel für die Darstellung von Features.
 *
 * @author Jürgen Weichand (juergen.weichand@ldbv.bayern.de)
 */
public class FeatureMapDemo {

    private FeatureMapDemo() {
    }


   /**
     * @param args the command line arguments.
     * @throws Exception If something went wrong.
     */
    public static void main(String[] args) throws Exception {

        // Definition eines einfachen Datenmodells zur Repräsentation
        // der Datensätze in der Karte (Name, ID, Geometry)
        SimpleFeatureType featureType = DataUtilities.createType(
            "Dataset",
                "geometry:Geometry:srid=4326,"
                + "name:String,"
                + "id:String"
        );


        // Erstellen von Feature-Instanzen. Können aus dem Service-Feed
        // (GeoRSS) ausgelesen werden.
        // Anmerkung: Für WKT untypische Achsenreihenfolge lat/lon?
        SimpleFeatureBuilder featureBuilder =
                new SimpleFeatureBuilder(featureType);
        featureBuilder.add(
                new WKTReader().read(
                        "POLYGON ((50.198833035498446 9.665521369798398, "
                                + "50.198833536973559 9.832163281468624, "
                                + "50.098844085813319 9.832165483538606, "
                                + "50.098843740001172 9.665523507246684, "
                                + "50.198833035498446 9.665521369798398 ))"));
        featureBuilder.add("Digitale Topographische Karte "
                + "1:25000 Füssen 8430");
        featureBuilder.add("DEBY_54f02b69-ff27-3f55-8410-5e2c6b5f982b");
        SimpleFeature feature = featureBuilder.buildFeature(null);



        // Anlegen einer FeatureCollection aus den Features
        DefaultFeatureCollection featureCollection =
                new DefaultFeatureCollection("internal", featureType);
        featureCollection.add(feature);



        MapContent map = new MapContent();
        map.setTitle("GDI-BY DownloadClient Demo");

        // Hinzufügen der FeatureColletion zu der Karte
        Layer layer = new FeatureLayer(featureCollection, createStyle());
        map.addLayer(layer);


        // Zoomen und CRS wählen
        final double x1 =  9.66;
        final double x2 =  9.84;
        final double y1 = 50.09;
        final double y2 = 50.20;

        map.getViewport().setBounds(
            new ReferencedEnvelope(x1, x2, y1, y2,
                    CRS.decode("EPSG:4326", true)));

        map.getViewport().setCoordinateReferenceSystem(
                CRS.decode("EPSG:3857"));

        // Now display the map
        JMapFrame.showMap(map);
    }


    private static Style createStyle() {

        StyleBuilder builder = new StyleBuilder();

        PolygonSymbolizer polygonSymbolizer =
                builder.createPolygonSymbolizer(Color.gray, Color.BLACK, 2);

        return builder.createStyle(polygonSymbolizer);
    }


}
