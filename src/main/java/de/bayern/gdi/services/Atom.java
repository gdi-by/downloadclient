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

import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.XML;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
public class Atom extends WebService {

    private String serviceURL;
    private String username;
    private String password;
    private Document mainDoc;
    private static final Logger log
            = Logger.getLogger(CatalogService.class.getName());
    private ArrayList<String> types;
    private Map<String, String> typesWithURLS;
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
        this.types = null;
        URL url = null;

        try {
            url = new URL(this.serviceURL);
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        this.mainDoc = XML.getDocument(url,
                this.username,
                this.password);
        this.nscontext = new NamespaceContextMap("",
                "http://www.w3.org/2005/Atom");
    }
    /**
     * @inheritDoc
     * @return the Types of the service
     */
    public ArrayList<String> getTypes() {
        if (this.types == null) {
            types = new ArrayList<>();
            typesWithURLS = new HashMap<String, String>();
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
                types.add(title.getTextContent());
                Node id = (Node) XML.xpath(entry,
                        getEntryid,
                        XPathConstants.NODE,
                        this.nscontext);
                typesWithURLS.put(title.getTextContent(), id.getTextContent());
            }
        }
        return types;
    }

    /**
     * returns the URL for the selected Type.
     * @param type the type
     * @return URL
     */
    public String getURLforType(String type) {
        return (String) this.typesWithURLS.get(type);
    }

    /**
     * @inheritDoc
     * @param type the Type
     * @return The Attributes of the Service
     */
    public Map<String, String> getAttributes(String type) {
        Map<String, String> attributes = new HashMap<>();
        String attributeURL = getURLforType(type);
        String getEntry = "//entry/link[@href='" + attributeURL + "']";
        Node n = (Node) XML.xpath(this.mainDoc,
                getEntry,
                XPathConstants.NODE,
                this.nscontext);
        Node entry = n.getParentNode();
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
                if (catAttr.getNodeName().equals("term")) {
                    epsg = catAttr.getTextContent();
                    break;
                }
            }
            if (epsg != null) {
                epsg = epsg.substring(epsg.lastIndexOf("/") + 1);
                epsg = EPSG + epsg;
                String attrVal = null;
                attrVal = attributeURL.substring(0,
                        attributeURL.lastIndexOf("."));
                attrVal = attrVal.substring(attrVal.lastIndexOf(".") + 1,
                        attrVal.length());
                attributes.put(ATTRIBUTENAME, attrVal + "_" + epsg);
            }
        }
        return attributes;
    }

    /**
     * @inheritDoc
     * @return the Type of the Service
     */
    public ServiceType getServiceType() {
        return ServiceType.Atom;
    }
}
