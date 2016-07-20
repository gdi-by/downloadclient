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
import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.ServiceChecker;
import de.bayern.gdi.utils.XML;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;
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

    /** Field for the Atom Item. */
    public static class Field {
        /**
         * name.
         */
        public String name;
        /**
         * type.
         */
        public String type;
        /**
         * description.
         */
        public String description;
        /**
         * crs.
         */
        public String crs;
        /**
         * format.
         */
        public String format;

        public Field() {
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
         * bounding Polygon.
         */
        public Polygon polygon;

        /**
         * mimetype.
         */
        public String format;

        /**
         * username.
         */
        public String username;

        /**
         * password.
         */
        public String password;

        private NamespaceContext context;

        public Item() {
            otherCRSs = new ArrayList<>();
            fields = new ArrayList<>();
            format = null;
            context = new NamespaceContextMap(
                    "", "http://www.w3.org/2005/Atom",
                    "georss", "http://www.georss.org/georss",
                    "inspire_dls",
                    "http://inspire.ec.europa.eu/schemas/inspire_dls/1.0");
        }

        @Override
        public String toString() {
            String str = "title: " + title + "\n";
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
            Document entryDoc = null;
            try {
                URL url = new URL(this.describedBy);
                entryDoc = Atom.getDocument(url, this.username, this.password);
            } catch (MalformedURLException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
            if (entryDoc == null) {
                entryDoc = XML.getDocument(this.describedBy);
            }
            format = getFormat(entryDoc);
            fields = getFieldForEntry(entryDoc);
        }

        private String getFormat(Document entryDoc) {
            String getType = "//entry/link/@type";
            String itemformat = (String) XML.xpath(entryDoc,
                    getType,
                    XPathConstants.STRING,
                    context);
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
                String crs = (String) XML.xpath(entryNode,
                        "category/@label",
                        XPathConstants.STRING,
                        this.context);
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
     * @return the URL of the Service
     */
    public Atom(String serviceURL) {
        this(serviceURL, null, null);
    }

    private static Document getDocument(URL url,
                                        String username,
                                        String password) {
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
        //System.out.println(this.serviceURL);
        this.nscontext = new NamespaceContextMap(
                null , "http://www.w3.org/2005/Atom",
                "georss", "http://www.georss.org/georss",
                "inspire_dls",
                "http://inspire.ec.europa.eu/schemas/inspire_dls/1.0");
        this.mainDoc = getDocument(url, this.username, this.password);
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
            //System.out.println("title: " + title);
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
            Node borderPolyGonN = (Node) XML.xpath(entry,
                    borderPolygonExpr,
                    XPathConstants.NODE,
                    this.nscontext);
            Item it = new Item();
            it.id = id.getTextContent();
            it.title = titleN.getTextContent();
            it.description = description.getTextContent();
            it.describedBy = describedBy.getTextContent();
            it.otherCRSs = getCRS(entry);
            it.username = this.username;
            it.password = this.password;
            //System.out.println((System.currentTimeMillis() - beginRead)
            //        + " ms\totherCRS: " + it.otherCRSs);
            it.defaultCRS = it.otherCRSs.get(0);
            //System.out.println((System.currentTimeMillis() - beginRead)
            //        + " ms\tdefaultCRS: " + it.defaultCRS);
            //System.out.println((System.currentTimeMillis() - beginRead)
            //        + " ms\tformat: " + it.format);
            it.bbox = new ReferencedEnvelope();

            WKTReader reader = new WKTReader(new GeometryFactory());

            // XXX: GML, anyone?
            String bboxStr = convertPolygonToWKT(
                borderPolyGonN.getTextContent());

            Geometry polygon = null;
            try {
                polygon = reader.read(bboxStr);
            } catch (ParseException | IllegalArgumentException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
                continue;
            }

            Envelope env = polygon.getEnvelopeInternal();
            if (env == null || !(polygon instanceof Polygon)) {
                continue;
            }

            it.polygon = (Polygon)polygon;

            String getCategories = "(category/@term)[1]";
            String categoryTerm = (String)XML.xpath(entry,
                getCategories,
                XPathConstants.STRING,
                this.nscontext);

            CoordinateReferenceSystem crs = null;
            try {
                crs = decodeCRS(categoryTerm);
                it.bbox = new ReferencedEnvelope(env, crs);
            } catch (FactoryException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
                it.bbox = null;
                continue;
            } finally {
                items.add(it);
            }
        }
    }

    private static final Pattern CRS_RE
        = Pattern.compile("(/([^/]+)/([^/]+)/([^/]+)$)"
            + "|(EPSG:[0-9]*)");

    private static final Pattern CRS_CODE
            = Pattern.compile("[0-9]{2,}");

    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;

    // XXX: This should be coded more defensively!
    private static CoordinateReferenceSystem decodeCRS(String term)
    throws FactoryException {
        Matcher m = CRS_RE.matcher(term);
        if (m.find()) {
            String authority = null;
            String code = null;
            if (m.group(TWO) != null && m.group(TWO).equals("EPSG")) {
                authority = m.group(TWO);
                Matcher c = CRS_CODE.matcher(m.group(THREE));
                if (c.find()) {
                    code = m.group(THREE);
                } else {
                    code = m.group(FOUR);
                }
            } else {
                if (m.group(ONE) != null) {
                    authority = m.group(ONE);
                    Matcher c = CRS_CODE.matcher(m.group(THREE));
                    if (c.find()) {
                        code = m.group(THREE);
                    } else {
                        code = m.group(TWO);
                    }
                } else {
                    authority = m.group(ZERO).substring(0,
                            m.group(ZERO).lastIndexOf(":"));
                    code = m.group(ZERO).substring(authority.length() + 1,
                            m.group(ZERO).length());
                }
            }
            return CRS.decode(authority + ":" + code);
        }
        throw new FactoryException("Cannot parse '" + term + "'");
    }

    private static String convertPolygonToWKT(String text) {
        String[] sep = text.split(" ");

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
}
