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

package de.bayern.gdi.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Helper to handle XML documents.
 */
public class XML {

    private static final Logger log
        = Logger.getLogger(XML.class.getName());

    private XML() {
    }

    /**
     * Loads an XML document from a file.
     * @param fileName the name of the XML file.
     * @return the loaded XML document of null if there was an error.
     */
    public static Document getDocument(File fileName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(fileName);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return document;
    }

    /**
     * Loads an XML document from an input stream.
     * @param input the input stream.
     * @return the loaded XML document of null if there was an error.
     */
    public static Document getDocument(InputStream input) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(input);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return document;
    }

    /**
     * Gets an XML Document from a remote location.
     * @param url the URL
     * @return and XML Document
     */
    public static Document getDocument(URL url) {
        return getDocument(url, null, null);
    }

    public static Node getChildWithName(Node node, String nodeName) {
        return getChildWithName(node.getChildNodes(), nodeName);
    }

    public static Node getChildWithName(NodeList nl, String nodeName) {
        Node retNode = null;
        for (int i = 0; i < nl.getLength(); i++) {
            Node curNode = nl.item(i);
            if (curNode.getNodeName().equals(nodeName)) {
                return curNode;
            }
        }
        return retNode;
    }

    /**
     * Gets an XML Document from a remote location.
     * @param url the URL
     * @param userName the Username {NULL if none needed}
     * @param password the Password {NULLL if none needed}
     * @return an XML Document
     */
    public static Document getDocument(URL url, String userName, String
            password) {
        Document doc = null;
        try {
            URLConnection conn = null;
            if (url.toString().toLowerCase().startsWith("https")) {
                HttpsURLConnection con
                        = (HttpsURLConnection) url.openConnection();
                conn = (URLConnection) con;
            } else {
                conn = url.openConnection();
            }
            if (StringUtils.getBase64EncAuth(
                    userName, password) != null) {
                conn.setRequestProperty("Authorization", "Basic "
                        + StringUtils.getBase64EncAuth(
                        userName, password));
            }
            //String xmlStr = streamToString(conn.getInputStream());
            //doc = getDocument(xmlStr);
            doc = getDocument(conn.getInputStream());
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return doc;
    }

    /**
     * TODO.
     * @param xmlString TODO
     * @return TODO
     */
    public static final Document getDocument(String xmlString) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(xmlString);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return document;
    }
    /**
     * Creates a new XPath without a namespace context.
     * @return the new XPath.
     */
    public static final XPath newXPath() {
        return newXPath(null, null);
    }

    /**
     * Creates a new XPath with a given namespace context.
     * @param namespaceContext The namespace context to be used or null
     * if none should be used.
     * @param resolver The name space resolver.
     * @return The new XPath
     */
    public static final XPath newXPath(
        NamespaceContext      namespaceContext,
        XPathVariableResolver resolver
    ) {
        XPathFactory factory = XPathFactory.newInstance();
        XPath        xpath   = factory.newXPath();
        if (namespaceContext != null) {
            xpath.setNamespaceContext(namespaceContext);
        }

        if (resolver != null) {
            xpath.setXPathVariableResolver(resolver);
        }
        return xpath;
    }

    /**
     * Evaluates an XPath query on a given object and returns the result
     * as a given type. No namespace context is used.
     * @param root  The object which is used as the root of the tree to
     * be searched in.
     * @param query The XPath query
     * @param returnTyp The type of the result.
     * @return The result of type 'returnTyp' or null if something
     * went wrong during XPath evaluation.
     */
    public static final Object xpath(
        Object root,
        String query,
        QName  returnTyp
    ) {
        return xpath(root, query, returnTyp, null);
    }

    /**
     * Evaluates an XPath query on a given object and returns the result
     * as a string. A given namespace context is used.
     * @param root  The object which is used as the root of the tree to
     * be searched in.
     * @param query The XPath query
     * @param namespaceContext The namespace context to be used or null
     * if none should be used.
     * @return The result of the query or null if something went wrong
     * during XPath evaluation.
     */
    public static final String xpathString(
        Object root, String query, NamespaceContext namespaceContext
    ) {
        return (String)xpath(
            root, query, XPathConstants.STRING, namespaceContext);
    }

    /**
     * Evaluates an XPath query on a given object and returns the result
     * as a given type. Optionally a namespace context is used.
     * @param root The object which is used as the root of the tree to
     * be searched in.
     * @param query The XPath query
     * @param returnType The type of the result.
     * @param namespaceContext The namespace context to be used or null
     * if none should be used.
     * @return The result of type 'returnTyp' or null if something
     * went wrong during XPath evaluation.
     */
    public static final Object xpath(
        Object           root,
        String           query,
        QName            returnType,
        NamespaceContext namespaceContext
    ) {
        return xpath(root, query, returnType, namespaceContext, null);
    }

    /**
     * xpath evaluates ax XPath expression.
     * @param root The root object of the evaluation being applied to.
     * @param query The XPath query.
     * @param returnType The type to be returned.
     * @param namespaceContext An optional namespace context.
     * @param variables A map variables to be used during evaluation.
     * @return The result of the evaluation.
     */
    public static final Object xpath(
        Object           root,
        String           query,
        QName            returnType,
        NamespaceContext namespaceContext,
        Map<String, String> variables
    ) {
        if (root == null) {
            return null;
        }

        XPathVariableResolver resolver = variables != null
            ? new MapXPathVariableResolver(variables)
            : null;

        try {
            XPath xpath = newXPath(namespaceContext, resolver);
            if (xpath != null) {
                return xpath.evaluate(query, root, returnType);
            }
        } catch (XPathExpressionException xpee) {
            log.log(Level.SEVERE, xpee.getLocalizedMessage(), xpee);
        }

        return null;
    }

    /** containsTags is a cheap way to detect if a XML file
     *  contains some special tags. Useful for error detection.
     *  @param file The XML file to check.
     *  @param tags the list of tags to check for.
     *  @return The first tag that was found. Null if the tags are not in file.
     *  @throws XMLStreamException if an exception happends while
     *          processoing the XML file.
     *  @throws IOException if an I/O error happens.
     */
    public static final String containsTags(File file, String []tags)
        throws XMLStreamException, IOException {

        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        XMLStreamReader xmlr = xmlif.createXMLStreamReader(
                new BufferedInputStream(
                new FileInputStream(file)));
        try {
            while (xmlr.hasNext()) {
                if (xmlr.next() == XMLEvent.START_ELEMENT) {
                    String needle = xmlr.getLocalName();
                    for (String tag: tags) {
                        if (needle.equals(tag)) {
                            return tag;
                        }
                    }
                }
            }
        } finally {
            xmlr.close();
        }
        return null;
    }

    private static String streamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

}
