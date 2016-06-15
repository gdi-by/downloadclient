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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.StringUtils;
import de.bayern.gdi.utils.XML;

/** Extract meta data from a WFS. */
public class WFSMetaExtractor {

    private static final CoordinateReferenceSystem WGS84;

    static {
        try {
            WGS84 = CRS.decode("EPSG:4326");
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String OWS = "http://www.opengis.net/ows/1.1";
    private static final String WFS = "http://www.opengis.net/wfs/2.0";
    private static final String XLINK = "http://www.w3.org/1999/xlink";
    private static final String XSD = "http://www.w3.org/2001/XMLSchema";

    /** Common namespaces for WFS documents. */
    public static final NamespaceContext NAMESPACES =
        new NamespaceContextMap(
            "ows",   OWS,
            "wfs",   WFS,
            "xlink", XLINK,
            "xsd",   XSD);

    private static final String XPATH_TITLE
        = "//ows:ServiceIdentification/ows:Title/text()";

    private static final String XPATH_ABSTRACT
        = "//ows:ServiceIdentification/ows:Abstract/text()";

    private static final String XPATH_OPERATIONS
        = "//ows:OperationsMetadata/ows:Operation";

    private static final String XPATH_CONSTRAINTS
        = "//ows:OperationsMetadata/ows:Constraint";

    private static final String XPATH_FEATURETYPES
        = "//wfs:FeatureTypeList/wfs:FeatureType";

    private static final String XPATH_CORNERS
        = "//*[substring(local-name(),"
        + "string-length(local-name()) - string-length('Corner') +1)"
        + "= 'Corner']/text()";

    private static final String XPATH_OPERATION_GET
        = "ows:DCP/ows:HTTP/ows:Get/@xlink:href";

    private static final String XPATH_OPERATIONS_VERSIONS
        = "//ows:OperationsMetadata"
        + "/ows:Parameter[@name='version']"
        + "/ows:AllowedValues/ows:Value/text()";

    private static final String XPATH_OPERATION_OUT_FORMATS
        = "ows:Parameter[@name='outputFormat']"
        + "/ows:AllowedValues/ows:Value/text()";

    private static final String XPATH_FEATURE_OUT_FORMATS
        = "wfs:OutputFormats/wfs:Format/text()";

    private static final String XPATH_STORED_QUERIES
        = "//wfs:StoredQueryDescription";

    private String user;
    private String password;
    private String capURLString;

    public WFSMetaExtractor(String capURLString) {
        this.capURLString = capURLString;
    }

    public WFSMetaExtractor(
        String capURLString, String user, String password) {
        this(capURLString);
        this.user = user;
        this.password = password;
    }

    private static double[] toDouble(String s) {
        try {
            String[] parts = s.split("[ \\t]+");
            double[] x = new double[parts.length];
            for (int i = 0; i < parts.length; i++) {
                x[i] = Double.parseDouble(parts[i]);
            }
            return x;
        } catch (NumberFormatException nfe) {
            return new double[0];
        }
    }

    private static ReferencedEnvelope getBounds(
            Element feature, NamespaceContext nc) {

        NodeList corners = (NodeList)
            XML.xpath(feature, XPATH_CORNERS, XPathConstants.NODESET, nc);

        int n = corners.getLength();
        if (n == 0) {
            return null;
        }

        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            double[] x = toDouble(corners.item(i).getTextContent());
            if (x == null) {
                return null;
            }
            if (x[0] < minX) {
                minX = x[0];
            }
            if (x[0] > maxX) {
                maxX = x[0];
            }
            if (x[1] < minY) {
                minY = x[1];
            }
            if (x[1] > maxY) {
                maxY = x[1];
            }
        }
        return new ReferencedEnvelope(minX, maxX, minY, maxY, WGS84);
    }

    /**
     * Extracts meta information from a given capabilities path.
     * @return The extracted meta data.
     * @throws IOException If something went wrong.
     */
    public WFSMeta parse() throws IOException {
        WFSMeta meta = new WFSMeta();
        meta.url = capURLString;
        parseCapabilites(meta);
        parseDescribeStoredQueries(meta);
        return meta;
    }

    private Document getDocument(String url) throws IOException {
        Document doc = XML.getDocument(new URL(url), this.user, this.password);
        if (doc != null) {
            return doc;
        }
        throw new IOException("Cannot load document.");
    }

    private static String url(
        String  url,
        String  request,
        WFSMeta meta
    ) throws IOException {
        String post = "";
        int idx = url.lastIndexOf('?');
        if (idx >= 0) {
            post = url.substring(idx + 1);
            url = url.substring(0, idx);
        }
        url += "?request=" + request
            + "&service=wfs"
            + "&version="
            + StringUtils.urlEncode(meta.highestVersion("2.0.0"));
        if (post.length() > 0) {
            url += "&" + post;
        }
        return url;
    }

    private void parseDescribeStoredQueries(WFSMeta meta)
        throws IOException {

        WFSMeta.Operation op = meta.findOperation("DescribeStoredQueries");
        if (op == null) {
            return;
        }

        String urlString = op.get != null
            ? url(op.get, "DescribeStoredQueries", meta)
            : capURLString.replace("GetCapabilities", "DescribeStoredQueries");

        Document dsqDoc = getDocument(urlString);
        meta.namespaces.join(dsqDoc);

        NodeList storedQueriesDesc = (NodeList)XML.xpath(
            dsqDoc, XPATH_STORED_QUERIES,
            XPathConstants.NODESET, NAMESPACES);

        for (int i = 0, n = storedQueriesDesc.getLength(); i < n; i++) {
            Element sqd = (Element)storedQueriesDesc.item(i);
            WFSMeta.StoredQuery sq = new WFSMeta.StoredQuery();
            sq.id = sqd.getAttribute("id");
            NodeList titles = sqd.getElementsByTagNameNS(WFS, "Title");
            if (titles.getLength() > 0) {
                sq.title = titles.item(0).getTextContent();
            }
            NodeList abstracts = sqd.getElementsByTagNameNS(WFS, "Abstract");
            if (abstracts.getLength() > 0) {
                sq.abstractDescription = abstracts.item(0).getTextContent();
            }
            NodeList parameters =
                sqd.getElementsByTagNameNS(WFS, "Parameter");
            for (int j = 0, m = parameters.getLength(); j < m; j++) {
                Element parameter = (Element)parameters.item(j);
                Field p = new Field(
                    parameter.getAttribute("name"),
                    parameter.getAttribute("type"));
                sq.parameters.add(p);
            }

            meta.storedQueries.add(sq);
        }
    }

    private void parseCapabilites(WFSMeta meta) throws IOException {

        Document capDoc = getDocument(this.capURLString);
        meta.namespaces.join(capDoc);

        meta.title = XML.xpathString(capDoc, XPATH_TITLE, NAMESPACES);
        meta.abstractDescription
            = XML.xpathString(capDoc, XPATH_ABSTRACT, NAMESPACES);

        NodeList versions = (NodeList)XML.xpath(
            capDoc, XPATH_OPERATIONS_VERSIONS,
            XPathConstants.NODESET, NAMESPACES);
        for (int i = 0, n = versions.getLength(); i < n; i++) {
            meta.versions.add(versions.item(i).getTextContent());
        }
        Collections.sort(meta.versions);

        NodeList nl = (NodeList)XML.xpath(
            capDoc, XPATH_OPERATIONS, XPathConstants.NODESET, NAMESPACES);
        for (int i = 0, n = nl.getLength(); i < n; i++) {
            WFSMeta.Operation operation = new WFSMeta.Operation();
            Element node = (Element)nl.item(i);
            operation.name = node.getAttribute("name");
            operation.get = XML.xpathString(
                node, XPATH_OPERATION_GET, NAMESPACES);

            NodeList outs = (NodeList)XML.xpath(
                node, XPATH_OPERATION_OUT_FORMATS,
                XPathConstants.NODESET, NAMESPACES);
            for (int j = 0, m = outs.getLength(); j < m; j++) {
                operation.outputFormats.add(outs.item(j).getTextContent());
            }

            NodeList constraints =
                (NodeList)node.getElementsByTagNameNS(OWS, "Constraint");

            for (int j = 0, m = constraints.getLength(); j < m; j++) {
                Element c = (Element)constraints.item(j);
                WFSMeta.Constraint constraint = new WFSMeta.Constraint();
                constraint.name = c.getAttribute("name");
                NodeList defVals = c.getElementsByTagNameNS(
                    OWS, "DefaultValue");
                if (defVals.getLength() > 0) {
                    constraint.value = defVals.item(0).getTextContent();
                }
                operation.constraints.add(constraint);
            }
            meta.operations.add(operation);
        }

        nl = (NodeList)XML.xpath(
            capDoc, XPATH_CONSTRAINTS,
            XPathConstants.NODESET, NAMESPACES);
        for (int i = 0, n = nl.getLength(); i < n; i++) {
            Element el = (Element)nl.item(i);
            WFSMeta.Constraint constraint = new WFSMeta.Constraint();
            constraint.name = el.getAttribute("name");
            NodeList defVals = el.getElementsByTagNameNS(OWS, "DefaultValue");
            if (defVals.getLength() > 0) {
                constraint.value = defVals.item(0).getTextContent();
            }
            meta.constraints.add(constraint);
        }

        nl = (NodeList)XML.xpath(
            capDoc, XPATH_FEATURETYPES, XPathConstants.NODESET, NAMESPACES);
        for (int i = 0, n = nl.getLength(); i < n; i++) {
            Element el = (Element)nl.item(i);

            WFSMeta.Feature feature = new WFSMeta.Feature();

            NodeList names = el.getElementsByTagNameNS(WFS, "Name");
            if (names.getLength() > 0) {
                feature.name = names.item(0).getTextContent();
            }

            NodeList titles = el.getElementsByTagNameNS(WFS, "Title");
            if (titles.getLength() > 0) {
                feature.title = titles.item(0).getTextContent();
            }

            NodeList abstracts = el.getElementsByTagNameNS(WFS, "Abstract");
            if (abstracts.getLength() > 0) {
                feature.abstractDescription
                    = abstracts.item(0).getTextContent();
            }

            NodeList defaultCRS = el.getElementsByTagNameNS(WFS, "DefaultCRS");
            if (defaultCRS.getLength() > 0) {
                feature.defaultCRS
                    = defaultCRS.item(0).getTextContent();
            }

            NodeList otherCRSs = el.getElementsByTagNameNS(WFS, "OtherCRS");
            for (int j = 0, m = otherCRSs.getLength(); j < m; j++) {
                feature.otherCRSs.add(otherCRSs.item(j).getTextContent());
            }

            NodeList outs = (NodeList)XML.xpath(
                el, XPATH_FEATURE_OUT_FORMATS,
                XPathConstants.NODESET, NAMESPACES);
            for (int j = 0, m = outs.getLength(); j < m; j++) {
                feature.outputFormats.add(outs.item(j).getTextContent());
            }

            feature.bbox = getBounds(el, NAMESPACES);

            meta.features.add(feature);
        }
    }
}
