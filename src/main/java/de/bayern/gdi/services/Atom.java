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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;
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
    private Document mainDoc;
    private static final Logger log
            = Logger.getLogger(CatalogService.class.getName());
    private ArrayList<Field> items;
    private NamespaceContext nscontext;
    private static final String ATTRIBUTENAME = "VARIATION";
    private static final String EPSG = "EPSG";




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
        this.items = null;
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
                "inspire_dls", "http://inspire.ec.europa.eu/schemas/inspire_dls/1.0");
    }
    /**
     * @inheritDoc
     * @return the Types of the service
     */
    public ArrayList<Field> getItems() {
        if (this.items == null) {
            items = new ArrayList<>();
            String getEntriesQuery = "//entry";
            NodeList entries = (NodeList) XML.xpath(this.mainDoc,
                    getEntriesQuery,
                    XPathConstants.NODESET,
                    this.nscontext);
            for (int i = 0; i < entries.getLength(); i++) {
                Node entry = entries.item(i);
                String getEntryTitle = "title";
                String getEntryid = "id";
                Node title = (Node) XML.xpath(entry,
                        getEntryTitle,
                        XPathConstants.NODE,
                        this.nscontext);

                Node id = (Node) XML.xpath(entry,
                        getEntryid,
                        XPathConstants.NODE,
                        this.nscontext);
                Field f = new Field(id.getTextContent(), title.getTextContent
                        ());
                items.add(f);
            }
        }
        return items;
    }

    /**
     * returns the URL for the selected item
     * @param item the item
     * @return URL
     */
    public String getURLforItem(String item) {
        for(Field f: this.items) {
            if(f.name == item) {
                return f.type;
            }
        }
        return null;
    }

    /**
     * @inheritDoc
     * @param item the item
     * @return The Feilds of the item
     */
    public ArrayList<Field> getFields(Field item) {
        ArrayList<Field> fields = new ArrayList<>();
        String attributeURL = item.name;
        Node entry = getEntry(attributeURL);
        //Predefined in ATOM Service
        String getId = "id";
        String id = (String) XML.xpath(entry,
                getId,
                XPathConstants.STRING,
                this.nscontext);
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
                if (catAttr.getNodeName().equals("term")) {
                    epsg = catAttr.getTextContent();
                    String attrVal = makeAttributeValue(id, epsg);
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

    /**
     * returns the serviceType.
     * @return the Type of the Service
     */
    public ServiceType getServiceType() {
        return ServiceType.Atom;
    }

    /**
     * returns the description for an Item.
     * @param item The typeName.
     * @return The description.
     */
    public String getDescription(Field item) {
        String description = null;
        String attributeURL = item.name;
        Node entry = getEntry(attributeURL);
        String summaryExpr = "summary";
        description = (String) XML.xpath(entry,
                summaryExpr,
                XPathConstants.STRING,
                this.nscontext);
        return description;
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
