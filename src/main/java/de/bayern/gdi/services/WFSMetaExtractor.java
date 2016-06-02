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

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.bayern.gdi.utils.NamespaceContextMap;
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
    private static final NamespaceContext NAMESPACES =
        new NamespaceContextMap(
            "ows", "http://www.opengis.net/ows/1.1",
            "wfs", "http://www.opengis.net/wfs/2.0",
            "xlink", "http://www.w3.org/1999/xlink");

    private static final String XPATH_TITLE
        = "//ows:ServiceIdentification/ows:Title/text()";

    private static final String XPATH_ABSTRACT
        = "//ows:ServiceIdentification/ows:Abstract/text()";

    private static final String XPATH_OPERATIONS
        = "//ows:OperationsMetadata/ows:Operation";

    private static final String XPATH_SUPPORTED_CONSTRAINTS
        = "//ows:OperationsMetadata/ows:Constraint"
        + "[ows:DefaultValue/text()='TRUE']/@name";

    private static final String XPATH_UNSUPPORTED_CONSTRAINTS
        = "//ows:OperationsMetadata/ows:Constraint"
        + "[ows:DefaultValue/text()='FALSE']/@name";

    private static final String XPATH_FEATURETYPES
        = "//wfs:FeatureTypeList/wfs:FeatureType";

    private static final String XPATH_CORNERS
        = "//*[substring(local-name(),"
        + "string-length(local-name()) - string-length('Corner') +1)"
        + "= 'Corner']/text()";

    private static final String XPATH_OPERATION_GET
        = "ows:DCP/ows:HTTP/ows:Get/@xlink:href";

    private WFSMetaExtractor() {
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
     * @param capURLString The URL of the capabilities document.
     * @return The extracted meta data.
     * @throws IOException If something went wrong.
     */
    public static WFSMeta parse(String capURLString) throws IOException {
        WFSMeta meta = new WFSMeta();
        parseCapabilites(capURLString, meta);
        parseDescribeFeatures(capURLString, meta);
        parseDescribeStoredQueries(capURLString, meta);
        return meta;
    }

    private static void parseDescribeFeatures(
        String capURLString, WFSMeta meta) throws IOException {

        WFSMeta.Operation op = meta.findOperation("DescribeFeatureType");
        if (op == null) {
            System.out.println("DescribeFeatureType not supported.");
            return;
        }

        String urlString = op.get != null
            ? op.get + "?request=DescribeFeatureType"
            : capURLString.replace("GetCapabilities", "DescribeFeatureType");

        System.out.println("DescribeFeatureType: " + urlString);

        Document dfDoc = XML.getDocument(new URL(urlString));
        if (dfDoc == null) {
            throw new IOException("Cannot load DescribeFeatureType document.");
        }

        // TODO: Implement me!
    }

    private static void parseDescribeStoredQueries(
        String capURLString, WFSMeta meta) throws IOException {

        WFSMeta.Operation op = meta.findOperation("DescribeStoredQueries");
        if (op == null) {
            System.out.println("DescribeStoredQueries not supported.");
            return;
        }

        String urlString = op.get != null
            ? op.get + "?request=DescribeStoredQueries"
            : capURLString.replace("GetCapabilities", "DescribeStoredQueries");

        System.out.println("DescribeStoredQueries: " + urlString);

        Document dsqDoc = XML.getDocument(new URL(urlString));
        if (dsqDoc == null) {
            throw new IOException(
                "Cannot load DescribeStoredQueries document.");
        }

        // TODO: Implement me!

    }

    private static void parseCapabilites(String capURLString, WFSMeta meta)
        throws IOException {

        URL capURL = new URL(capURLString);
        Document capDoc = XML.getDocument(capURL);
        if (capDoc == null) {
            throw new IOException("Cannot load capabilities document.");
        }

        meta.title = XML.xpathString(capDoc, XPATH_TITLE, NAMESPACES);
        meta.abstractDescription
            = XML.xpathString(capDoc, XPATH_ABSTRACT, NAMESPACES);

        NodeList nl = (NodeList)XML.xpath(
            capDoc, XPATH_OPERATIONS, XPathConstants.NODESET, NAMESPACES);
        for (int i = 0, n = nl.getLength(); i < n; i++) {
            WFSMeta.Operation operation = new WFSMeta.Operation();
            Element node = (Element)nl.item(i);
            operation.name = node.getAttribute("name");
            operation.get = XML.xpathString(
                node, XPATH_OPERATION_GET, NAMESPACES);
            meta.operations.add(operation);
        }

        nl = (NodeList)XML.xpath(
            capDoc, XPATH_SUPPORTED_CONSTRAINTS,
            XPathConstants.NODESET, NAMESPACES);
        for (int i = 0, n = nl.getLength(); i < n; i++) {
            Node node = nl.item(i);
            meta.supportedConstraints.add(node.getTextContent());
        }

        nl = (NodeList)XML.xpath(
            capDoc, XPATH_UNSUPPORTED_CONSTRAINTS,
            XPathConstants.NODESET, NAMESPACES);
        for (int i = 0, n = nl.getLength(); i < n; i++) {
            Node node = nl.item(i);
            meta.unsupportedConstraints.add(node.getTextContent());
        }

        nl = (NodeList)XML.xpath(
            capDoc, XPATH_FEATURETYPES, XPathConstants.NODESET, NAMESPACES);
        for (int i = 0, n = nl.getLength(); i < n; i++) {
            Element el = (Element)nl.item(i);

            WFSMeta.Feature feature = new WFSMeta.Feature();

            NodeList names = el.getElementsByTagName("Name");
            if (names.getLength() > 0) {
                feature.name = names.item(0).getTextContent();
            }

            NodeList titles = el.getElementsByTagName("Title");
            if (titles.getLength() > 0) {
                feature.title = titles.item(0).getTextContent();
            }

            NodeList abstracts = el.getElementsByTagName("Abstract");
            if (abstracts.getLength() > 0) {
                feature.abstractDescription
                    = abstracts.item(0).getTextContent();
            }

            NodeList defaultCRS = el.getElementsByTagName("DefaultCRS");
            if (defaultCRS.getLength() > 0) {
                feature.defaultCRS
                    = defaultCRS.item(0).getTextContent();
            }

            NodeList otherCRSs = el.getElementsByTagName("OtherCRS");
            for (int j = 0, m = otherCRSs.getLength(); j < m; j++) {
                feature.otherCRSs.add(otherCRSs.item(j).getTextContent());
            }

            feature.bbox = getBounds(el, NAMESPACES);

            meta.features.add(feature);
        }
    }
}
