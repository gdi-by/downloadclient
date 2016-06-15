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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.XML;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class Atom {

    private String serviceURL;
    private String username;
    private String password;
    private String title;
    private String subTitle;
    private String serviceID;
    private Document mainDoc;
    private static final Logger log
            = Logger.getLogger(CatalogService.class.getName());
    private ArrayList<Item> items;
    private NamespaceContext nscontext;
    private static final String ATTRIBUTENAME = "VARIATION";
    private static final String EPSG = "EPSG";


    /** feature. */
    public static class Item {
        /**
         * title.
         */
        public String title;
        /**
         * id.
         */
        public String id;
        /**
         * default description.
         */
        public String description;
        /**
         * default CRS.
         */
        public String defaultCRS;
        /**
         * other CRSs.
         */
        public List<String> otherCRSs;
        /**
         * bbox.
         */
        public ReferencedEnvelope bbox;
        /**
         * fields.
         */
        public List<Field> fields;

        /**
         * describedBy.
         */
        public String describedBy;

        /**
         * bounding Polygon
         */
        public Polygon polygon;

        /**
         * mimetype.
         */
        public String format;

        public Item() {
            otherCRSs = new ArrayList<>();
            fields = new ArrayList<>();
            format = null;
        }

        @Override
        public String toString() {
            String str = null;
            str += "title: " + title + "\n";
            str += "id: " + id + "\n";
            str += "description: " + description + "\n";
            str += "described by: " + describedBy + "\n";
            str += "format: " + format + "\n";
            str += "CRS: " + defaultCRS + "\n";
            str += "Other CRS:\n";
            for (String crs: otherCRSs) {
                str += "\t" + crs;
            }
            str += "Other Fields:\n";
            for (Field f: fields) {
                str += "\t" + f.name + ": " + f.type + "\n";
            }
            return str;
        }

        /**
         * Loads the "costly" details.
         */
        public void load() {
            format = getFormat(this.describedBy);
        }

        private String getFormat(String itemid) {
            NamespaceContext nscontext = new NamespaceContextMap(
                    "", "http://www.w3.org/2005/Atom",
                    "georss", "http://www.georss.org/georss",
                    "inspire_dls",
                    "http://inspire.ec.europa.eu/schemas/inspire_dls/1.0");
            String itemformat = null;
            String attributeURL = itemid;
            Document entryDoc = XML.getDocument(attributeURL);
            String getType = "//entry/link/@type";
            itemformat = (String) XML.xpath(entryDoc,
                    getType,
                    XPathConstants.STRING,
                    nscontext);
            return itemformat;
        }
    }


    /**
     * @inheritDoc
     * @return the URL of the Service
     */
    public Atom(String serviceURL) {
        this(serviceURL, null, null);
    }

    /**
     * Constuctor.
     * @param serviceURL the URL to the service
     * @param userName username
     * @param password password
     */
    public Atom(String serviceURL, String userName, String password) {
        this.serviceURL = serviceURL;
        this.username = userName;
        this.password = password;
        URL url = null;

        try {
            url = new URL(this.serviceURL);
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        this.mainDoc = XML.getDocument(url,
                this.username,
                this.password,
                false);
        this.nscontext = new NamespaceContextMap(
                null , "http://www.w3.org/2005/Atom",
                "georss", "http://www.georss.org/georss",
                "inspire_dls",
                "http://inspire.ec.europa.eu/schemas/inspire_dls/1.0");
        items = new ArrayList<>();
        String getTitle = "//title";
        this.title = (String) XML.xpath(this.mainDoc,
                getTitle,
                XPathConstants.STRING,
                this.nscontext);
        String getSubtitle = "//subtitle";
        this.subTitle = (String) XML.xpath(this.mainDoc,
                getSubtitle,
                XPathConstants.STRING,
                this.nscontext);
        String getID = "//id";
        this.serviceID = (String) XML.xpath(this.mainDoc,
                getID,
                XPathConstants.STRING,
                this.nscontext);
        String getEntriesQuery = "//entry";
        NodeList entries = (NodeList) XML.xpath(this.mainDoc,
                getEntriesQuery,
                XPathConstants.NODESET,
                this.nscontext);
        for (int i = 0; i < entries.getLength(); i++) {
            //ong beginRead = System.currentTimeMillis();
            Node entry = entries.item(i);
            String getEntryTitle = "title";
            Node titleN = (Node) XML.xpath(entry,
                    getEntryTitle,
                    XPathConstants.NODE,
                    this.nscontext);
            //System.out.println((System.currentTimeMillis() - beginRead)
            //        + "  ms\tTitle: " + titleN.getTextContent());
            String getEntryid = "*[local-name()"
                    + "='spatial_dataset_identifier_code']";
            Node id = (Node) XML.xpath(entry,
                    getEntryid,
                    XPathConstants.NODE,
                    this.nscontext);
            //System.out.println((System.currentTimeMillis() - beginRead)
            //        + "ms \tID: " + id.getTextContent());
            String summaryExpr = "summary";
            Node description = (Node) XML.xpath(entry,
                    summaryExpr,
                    XPathConstants.NODE,
                    this.nscontext);
            String describedByExpr = "link[@rel='alternate']/@href";
            Node describedBy = (Node) XML.xpath(entry,
                    describedByExpr,
                    XPathConstants.NODE,
                    this.nscontext);
            //System.out.println((System.currentTimeMillis() - beginRead)
            //        + " ms\tDescirption: " + description.getTextContent());
            String borderPolygonExpr = "*[local-name()"
                                       + "='polygon']";
            Node borderPolyGonN= (Node) XML.xpath(entry,
                    borderPolygonExpr,
                    XPathConstants.NODE,
                    this.nscontext);
            Item it = new Item();
            it.id = id.getTextContent();
            it.title = titleN.getTextContent();
            it.description = description.getTextContent();
            it.describedBy = describedBy.getTextContent();
            it.otherCRSs = getCRS(entry);
            //System.out.println((System.currentTimeMillis() - beginRead)
            //        + " ms\totherCRS: " + it.otherCRSs);
            it.defaultCRS = it.otherCRSs.get(0);
            //System.out.println((System.currentTimeMillis() - beginRead)
            //        + " ms\tdefaultCRS: " + it.defaultCRS);
            //System.out.println((System.currentTimeMillis() - beginRead)
            //        + " ms\tformat: " + it.format);
            it.bbox = new ReferencedEnvelope();
            String bboxSepStr[] = borderPolyGonN.getTextContent().split(" ");
            String bboxStr = "";
            for(int j = 0; j< bboxSepStr.length ; j = j + 2) {
                bboxStr += bboxSepStr[j] + " " + bboxSepStr[j+1] + ", ";
            }
            bboxStr = bboxStr.substring(0,bboxStr.length()-2);
            WKTReader reader = new WKTReader( JTSFactoryFinder.getGeometryFactory( null ) );
            Geometry polygon = null;
            try{
                polygon = reader.read("POLYGON((" + bboxStr
                        + "))");
                it.polygon = (Polygon) polygon;
                Envelope env = polygon.getEnvelopeInternal();
                String getCategories = "(category/@term)[1]";
                String categoryTerm = (String) XML.xpath(entry,
                        getCategories,
                        XPathConstants.STRING,
                        this.nscontext);
                String epsgNumber = categoryTerm.substring(categoryTerm
                        .lastIndexOf("/")+1, categoryTerm.length());
                String epsgUnit = categoryTerm.substring(0, categoryTerm
                        .lastIndexOf(epsgNumber)-1);
                epsgUnit = epsgUnit.substring(0, epsgUnit.lastIndexOf("/"));
                epsgUnit = epsgUnit.substring(epsgUnit.lastIndexOf("/")+1,
                        epsgUnit.length());
                String defaultCRS = epsgUnit + ":" + epsgNumber;
                CoordinateReferenceSystem crs = CRS.decode(defaultCRS);
                it.bbox = new ReferencedEnvelope(env, crs);
                //it.bbox = new ReferencedEnvelope(env.getMaxX(), env.getMinX()
                //        , env.getMaxY(), env.getMinY(), crs);
                //System.out.println((System.currentTimeMillis() - beginRead)
                //        + " ms\t bbox: " + it.bbox);
            } catch (ParseException
                    | FactoryException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
            it.fields = getFieldForEntry(entry, it.id);
            //System.out.println((System.currentTimeMillis() - beginRead)
            //        + " ms\tfields: " + it.fields);
            items.add(it);
        }
    }
    /**
     * Items of the services.
     * @return the Items of the service
     */
    public ArrayList<Item> getItems() {
        return this.items;
    }

    /**
     * returns the title of the service.
     * @return title of the serivce
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * subtitle of the service.
     * @return subtitle of the service.
     */
    public String getSubTitle() {
        return this.subTitle;
    }

    /**
     * id of the service.
     * @return id of the serivce
     */
    public String getID() {
        return this.serviceID;
    }

    /**
     * URL of the service.
     * @return URL of the service
     */
    public String getURL() {
        return this.serviceURL;
    }


    private ArrayList<String> getCRS(Node entry) {
        ArrayList<String> crs = new ArrayList<>();
        //Predefined in ATOM Service
        String getCategories = "category";
        NodeList cL = (NodeList) XML.xpath(entry,
                getCategories,
                XPathConstants.NODESET,
                this.nscontext);
        for (int i = 0; i < cL.getLength(); i++) {
            Node cat = cL.item(i);
            NamedNodeMap catAttributes = cat.getAttributes();
            String epsg = null;
            for (int j = 0; j < catAttributes.getLength(); j++) {
                Node catAttr = catAttributes.item(j);
                if (catAttr.getNodeName().equals("label")) {
                    epsg = catAttr.getTextContent();
                    crs.add(epsg);
                }
            }
        }
        return crs;
    }

    private ArrayList<Field> getFieldForEntry(Node entry, String id) {
        ArrayList<Field> fields = new ArrayList<>();
        //Predefined in ATOM Service
        String getCategories = "category";
        NodeList cL = (NodeList) XML.xpath(entry,
                getCategories,
                XPathConstants.NODESET,
                this.nscontext);
        for (int i = 0; i < cL.getLength(); i++) {
            Node cat = cL.item(i);
            NamedNodeMap catAttributes = cat.getAttributes();
            String epsg = null;
            String attrVal = null;
            String csr = null;
            for (int j = 0; j < catAttributes.getLength(); j++) {
                Node catAttr = catAttributes.item(j);
                if (catAttr.getNodeName().equals("term")) {
                    epsg = catAttr.getTextContent();
                    attrVal = makeAttributeValue(id, epsg);
                    Field f = new Field(ATTRIBUTENAME, attrVal);
                    fields.add(f);
                }
            }
        }
        return fields;
    }

    private String makeAttributeValue(String id, String categoryTerm) {
        categoryTerm =
            categoryTerm.substring(categoryTerm.lastIndexOf("/") + 1);
        categoryTerm = EPSG + categoryTerm;
        return id + "_" + categoryTerm;
    }
}
