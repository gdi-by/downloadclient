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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import de.bayern.gdi.utils.HTTP;
import de.bayern.gdi.utils.Misc;
import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.ServiceChecker;
import de.bayern.gdi.utils.XML;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
    private List<Item> items;
    private NamespaceContext nscontext;
    private static final String ATTRIBUTENAME = "VARIATION";
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final int SEVEN = 7;
    private static final int EIGHT = 8;
    private static final int NINE = 9;
    private static final int TEN = 10;

    /** Field for the Atom Item. */
    public static class Field {
        /**
         * name.
         */
        private String name;
        /**
         * type.
         */
        private String type;
        /**
         * description.
         */
        private String description;
        /**
         * crs.
         */
        private String crs;
        /**
         * format.
         */
        private String format;

        public Field() {
        }

        public String getCRS() {
            return this.crs;
        }

        public Field(String format, String crs) {
            this.format = format;
            this.crs = crs;
        }

        public String getName() {
            return this.name;
        }

        public String getType() {
            return this.type;
        }

        public String getDescription() {
            return this.description;
        }

        public String getFormat() {
            return this.format;
        }

        /**
         * @param name name.
         * @param type type.
         */
        public Field(String name, String type, String crs, String format,
                     String description) {
            this.name = name;
            this.type = type;
            this.crs = crs;
            this.format = format;
            this.description = description;
        }

        @Override
        public String toString() {
            return "field: { name: " + name + " type: " + type
                    + " crs:" + crs + " format: " + format
                    + "descrption: " + description + " }";
        }
    }

    /** feature. */
    public static final class Item {
        /**
         * title.
         */
        private String title;
        /**
         * id.
         */
        private String id;
        /**
         * default description.
         */
        private String description;
        /**
         * default CRS.
         */
        private String defaultCRS;
        /**
         * other CRSs.
         */
        private List<String> otherCRSs;
        /**
         * fields.
         */
        private List<Field> fields;

        /**
         * describedBy.
         */
        private String describedBy;

        /**
         * bounding Polygon.
         */
        private Polygon polygon;

        /**
         * mimetype.
         */
        private String format;

        /**
         * username.
         */
        private String username;

        /**
         * password.
         */
        private String password;

        private NamespaceContext context;

        private URL baseURL;

        public String getTitle() {
            return this.title;
        }

        public String getID() {
            return this.id;
        }

        public String getDescription() {
            return this.description;
        }

        public List<Field> getFields() {
            return this.fields;
        }

        public Polygon getPolygon() {
            return this.polygon;
        }

        public Item(URL url) {
            this.baseURL = url;
            otherCRSs = new ArrayList<>();
            fields = new ArrayList<>();
            format = null;
            context = new NamespaceContextMap(
                    "", "http://www.w3.org/2005/Atom",
                    "georss", "http://www.georss.org/georss",
                    "inspire_dls",
                    "http://inspire.ec.europa.eu/schemas/inspire_dls/1.0");
        }

        public Item(URL url, String description) {
            this(url);
            this.description = description;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder()
                .append("title: ").append(title).append('\n')
                .append("id: ").append(id).append('\n')
                .append("description: ").append(description).append('\n')
                .append("descrined by: ").append(describedBy).append('\n')
                .append("format: ").append(format).append('\n')
                .append("CRS: ").append(defaultCRS).append('\n')
                .append("Other CRS:\n");
            for (String crs: otherCRSs) {
                sb.append('\t').append(crs);
            }
            sb.append("Other Fields:\n");
            for (Field f: fields) {
                sb.append('\t')
                  .append(f.name).append(": ").append(f.type).append('\n');
            }
            return sb.toString();
        }

        /**
         * Loads the "costly" details.
         * @throws URISyntaxException if the url is wrong
         * @throws SAXException if the xml is wrong
         * @throws ParserConfigurationException if the config is wrong
         * @throws IOException if something in io is wrong
         */
        public void load()
            throws URISyntaxException, SAXException,
                ParserConfigurationException, IOException {
            Document entryDoc = null;
            URL entryDocUrl = HTTP.buildAbsoluteURL(
                    this.baseURL, this.describedBy);
            entryDoc = Atom.getDocument(entryDocUrl,
                    this.username, this.password);

            if (entryDoc == null) {
                entryDoc = XML.getDocument(this.describedBy);
            }
            format = getFormat(entryDoc, entryDocUrl);
            fields = getFieldForEntry(entryDoc);
        }

        private String getFormat(Document entryDoc,
                                 URL entryDocUrl) {
            String getType = "//entry/link/@type";
            String itemformat = (String) XML.xpath(entryDoc,
                    getType,
                    XPathConstants.STRING,
                    context);
            //If the Format is not at the link, then extract it
            if (itemformat.isEmpty()) {
                String getTarget = "//entry/link/@href";
                String targetURLStr = (String) XML.xpath(entryDoc,
                        getTarget,
                        XPathConstants.STRING,
                        context);
                try {
                    URL targetURL = HTTP.buildAbsoluteURL(
                            entryDocUrl, targetURLStr);
                    itemformat = targetURL.getFile().substring(
                            targetURL.getFile().lastIndexOf('.') + 1,
                            targetURL.getFile().length());
                } catch (URISyntaxException | MalformedURLException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            return itemformat;
        }

        private ArrayList<Field> getFieldForEntry(Document entryDoc) {
            ArrayList<Field> attrFields = new ArrayList<>();
            //Predefined in ATOM Service
            String getCategories = "//entry";
            NodeList cL = (NodeList) XML.xpath(entryDoc,
                    getCategories,
                    XPathConstants.NODESET,
                    this.context);
            for (int i = 0; i < cL.getLength(); i++) {
                Node entryNode = cL.item(i);
                String entryId = (String) XML.xpath(entryNode,
                        "id",
                        XPathConstants.STRING,
                        this.context);
                String entryDescription = (String) XML.xpath(entryNode,
                        "title",
                        XPathConstants.STRING,
                        this.context);
                String type = (String) XML.xpath(entryNode,
                        "link/@type",
                        XPathConstants.STRING,
                        this.context);
                if (type.isEmpty()) {
                    type = entryId.substring(entryId.lastIndexOf('.') + 1,
                            entryId.length());
                }
                String crs = (String) XML.xpath(entryNode,
                        "category/@label",
                        XPathConstants.STRING,
                        this.context);
                if (crs.isEmpty()) {
                    if (entryDescription.contains("EPSG:")) {
                        String temp = entryDescription.substring(
                                entryDescription.lastIndexOf("EPSG:"),
                                entryDescription.length());
                        String epsgNum = "";
                        for (int j = "EPSG:".length();
                            j < temp.length() - 1;
                            j++) {
                            String isNum = temp.substring(j, j + 1);
                            if (Misc.isInteger(isNum)) {
                                epsgNum += isNum;
                            } else {
                                break;
                            }
                        }
                        crs = "EPSG:" + epsgNum;
                    }
                }
                Field field = new Field(ATTRIBUTENAME,
                        entryId,
                        crs,
                        type,
                        entryDescription);
                attrFields.add(field);
            }
            return attrFields;
        }
    }


    /**
     * @inheritDoc
     * @param serviceURL the url of the service
     * @throws URISyntaxException if the url is wrong
     * @throws ParserConfigurationException if the config is wrong
     * @throws IOException if something in io is wrong
     */
    public Atom(String serviceURL)
            throws URISyntaxException,
            ParserConfigurationException, IOException {
        this(serviceURL, null, null);
    }

    private static Document getDocument(URL url,
                                        String username,
                                        String password)
            throws IOException, URISyntaxException {
        Document doc = null;
        if (ServiceChecker.simpleRestricted(url)) {
            if (username == null && password == null) {
                //This case shouldn't happen, we check for this beforehand
                return null;
            } else {
                doc = XML.getDocument(url,
                        username,
                        password,
                        false);
            }
        } else {
            doc = XML.getDocument(url,
                    null,
                    null,
                    false);
        }
        return doc;
    }
    /**
     * Constuctor.
     * @param serviceURL the URL to the service
     * @param userName username
     * @param password password
     * @throws URISyntaxException if the url is wrong
     * @throws ParserConfigurationException if the config is wrong
     * @throws IOException if something in io is wrong
     */
    public Atom(String serviceURL, String userName, String password)
            throws URISyntaxException,
            ParserConfigurationException, IOException {
        this.serviceURL = serviceURL;
        this.username = userName;
        this.password = password;
        URL url = null;
        try {
            url = new URL(this.serviceURL);
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        this.nscontext = new NamespaceContextMap(
                null, "http://www.w3.org/2005/Atom",
                "georss", "http://www.georss.org/georss",
                "inspire_dls",
                "http://inspire.ec.europa.eu/schemas/inspire_dls/1.0");
        this.mainDoc = getDocument(url, this.username, this.password);
        preLoad();
    }

    private void preLoad() throws MalformedURLException,
            ParserConfigurationException {
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
            Node titleN = (Node) XML.xpath(entry,
                    getEntryTitle,
                    XPathConstants.NODE,
                    this.nscontext);
            String getEntryid = "*[local-name()"
                    + "='spatial_dataset_identifier_code']";
            Node id = (Node) XML.xpath(entry,
                    getEntryid,
                    XPathConstants.NODE,
                    this.nscontext);
            if (id == null) {
                getEntryid = "id";
                id = (Node) XML.xpath(entry,
                        getEntryid,
                        XPathConstants.NODE,
                        this.nscontext);
            }
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
            if (describedBy == null) {
                describedByExpr = "link/@href";
                describedBy = (Node) XML.xpath(entry,
                        describedByExpr,
                        XPathConstants.NODE,
                        this.nscontext);
            }
            String borderPolygonExpr = "*[local-name()"
                                       + "='polygon']";
            Node borderPolyGonN = (Node) XML.xpath(entry,
                    borderPolygonExpr,
                    XPathConstants.NODE,
                    this.nscontext);
            if (borderPolyGonN == null) {
                borderPolygonExpr = "*[local-name()"
                        + "='box']";
                borderPolyGonN = (Node) XML.xpath(entry,
                        borderPolygonExpr,
                        XPathConstants.NODE,
                        this.nscontext);
            }
            Item it = new Item(new URL(this.serviceURL));
            if (id != null) {
                it.id = id.getTextContent();
            } else {
                throw new ParserConfigurationException("Could not Parse "
                        + "Service. ID not found");
            }
            if (title != null) {
                it.title = titleN.getTextContent();
            } else {
                throw new ParserConfigurationException("Could not Parse "
                        + "Service. Title not found");
            }
            if (description != null) {
                it.description = description.getTextContent();
            } else {
                it.description = it.title;
            }
            if (describedBy != null) {
                it.describedBy = describedBy.getTextContent();
            } else {
                throw new ParserConfigurationException("Could not Parse "
                        + "Service. DescribedBy not found");
            }
            if (entry != null) {
                it.otherCRSs = getCRS(entry);
            } else {
                throw new ParserConfigurationException("Could not Parse "
                        + "Service. Entry not found");
            }
            if (it.otherCRSs != null) {
                it.defaultCRS = it.otherCRSs.get(0);
            } else {
                throw new ParserConfigurationException("Could not Parse "
                        + "Service. CRSs not found");
            }
            it.username = this.username;
            it.password = this.password;

            // XXX: GML, anyone?
            if (borderPolyGonN != null) {
                WKTReader reader = new WKTReader(new GeometryFactory());
                String bboxStr = convertPolygonToWKT(
                        borderPolyGonN.getTextContent());

                Geometry polygon = null;
                try {
                    polygon = reader.read(bboxStr);
                } catch (ParseException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                    continue;
                } catch (IllegalArgumentException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                    throw e;
                }

                Envelope env = polygon.getEnvelopeInternal();
                if (env == null || !(polygon instanceof Polygon)) {
                    continue;
                }

                it.polygon = (Polygon) polygon;
            } else {
                throw new ParserConfigurationException("Could not Parse "
                        + "Service. Bounding Box not Found");
            }
            items.add(it);
        }
    }

    private static String convertPolygonToWKT(String text) {
        String[] sep = text.split(" ");
        String[] seperators = new String[TWO];
        seperators[ZERO] = ",";
        seperators[ONE] = ".";
        //cleaning the strings if they have trailing or beginning , or .
        for (int i = 0; i < sep.length; i++) {
            String aSep = sep[i];
            for (String seperator: seperators) {
                if (aSep.contains(seperator)) {
                    if (aSep.lastIndexOf(seperator) == aSep.length() - 1) {
                        aSep = aSep.substring(ZERO,
                                aSep.lastIndexOf(seperator));

                    }
                    if (aSep.indexOf(seperator) == ZERO) {
                        aSep = aSep.substring(ONE, aSep.length());
                    }
                }
            }
            sep[i] = aSep;
        }
        //Check for length. If Only Two Pairs are in there, calculate the Box
        //of it.
        if (sep.length == FOUR) {
            ReferencedEnvelope ref = new ReferencedEnvelope();
            ref.include(Double.parseDouble(sep[ZERO]),
                    Double.parseDouble(sep[ONE]));
            ref.include(Double.parseDouble(sep[TWO]),
                    Double.parseDouble(sep[THREE]));
            Double maxX = ref.getMaxX();
            Double minX = ref.getMinX();
            Double maxY = ref.getMaxY();
            Double minY = ref.getMinY();
            sep = new String[TEN];
            //Upper Left
            sep[ZERO] = String.valueOf(minX);
            sep[ONE] = String.valueOf(maxY);
            //Upper Right
            sep[TWO] = String.valueOf(maxX);
            sep[THREE] = String.valueOf(maxY);
            //Lower Right
            sep[FOUR] = String.valueOf(maxX);
            sep[FIVE] = String.valueOf(minY);
            //Lower Left
            sep[SIX] = String.valueOf(minX);
            sep[SEVEN] = String.valueOf(minY);
            //Upper Right
            sep[EIGHT] = sep[ZERO];
            sep[NINE] = sep[ONE];
        }
        StringBuilder sb = new StringBuilder("POLYGON((");
        for (int j = 0; j < sep.length; j += 2) {
            if (j > 0) {
                sb.append(", ");
            }
            sb.append(sep[j]).append(' ').append(sep[j + 1]);
        }
        return sb.append("))").toString();
    }

    /**
     * Items of the services.
     * @return the Items of the service
     */
    public List<Item> getItems() {
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
}
