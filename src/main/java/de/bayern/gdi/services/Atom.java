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

import de.bayern.gdi.utils.Field;
import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.XML;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;
import org.geotools.geometry.jts.ReferencedEnvelope;
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
         * mimetype.
         */
        public String type;

        public Item() {
            otherCRSs = new ArrayList<>();
            fields = new ArrayList<>();
            defaultCRS = null;
        }
        @Override
        public String toString() {
            String str = null;
            str += "title: " + title + "\n";
            str += "id: " + id + "\n";
            str += "description: " + description + "\n";
            str += "type: " + type + "\n";
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
                "", "http://www.w3.org/2005/Atom",
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
            Node entry = entries.item(i);
            String getEntryTitle = "title";
            String getEntryid = "id";
            Node titleN = (Node) XML.xpath(entry,
                    getEntryTitle,
                    XPathConstants.NODE,
                    this.nscontext);
            Node id = (Node) XML.xpath(entry,
                    getEntryid,
                    XPathConstants.NODE,
                    this.nscontext);
            String summaryExpr = "summary";
            Node description = (Node) XML.xpath(entry,
                    summaryExpr,
                    XPathConstants.NODE,
                    this.nscontext);
            Item it = new Item();
            it.id = id.getTextContent();
            it.title = titleN.getTextContent();
            it.description = description.getTextContent();
            it.otherCRSs = getCRS(it.id);
            it.defaultCRS = it.otherCRSs.get(0);
            it.type = getType(it.id);
            String bboxExpr = "//*[local-name()='polygon']";
            Node bbox = (Node) XML.xpath(entry,
                    bboxExpr,
                    XPathConstants.NODE,
                    this.nscontext);
            it.bbox = new ReferencedEnvelope();
            //TODO: Calculate Bounding Box
            it.fields = getFieldForEntry(it.id);
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

    private String getType(String id) {
        String type = null;
        String attributeURL = id;
        Node entry = getEntry(attributeURL);
        Document entryDoc = XML.getDocument(attributeURL);
        String getType = "//entry/link/@type";
        type = (String) XML.xpath(entryDoc,
                getType,
                XPathConstants.STRING,
                this.nscontext);
        return type;
    }

    private ArrayList<String> getCRS(String id) {
        ArrayList<String> crs = new ArrayList<>();
        String attributeURL = id;
        Node entry = getEntry(attributeURL);
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

    private ArrayList<Field> getFieldForEntry(String id) {
        ArrayList<Field> fields = new ArrayList<>();
        String attributeURL = id;
        Node entry = getEntry(attributeURL);
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
        String attrVal = null;
        attrVal = id.substring(0,
                id.lastIndexOf("."));
        attrVal = attrVal.substring(attrVal.lastIndexOf(".") + 1,
                attrVal.length());
        attrVal = attrVal + "_" + categoryTerm;
        return attrVal;
    }


    private Node getEntry(String attributeURL) {
        String getEntry = "//entry/link[@href=$HREF]";
        HashMap<String, String> vars = new HashMap<>();
        vars.put("HREF", attributeURL);
        Node n = (Node) XML.xpath(this.mainDoc,
                getEntry,
                XPathConstants.NODE,
                this.nscontext, vars);
        return n.getParentNode();
    }

}
