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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.w3c.dom.Document;
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
     *
     * @param fileName the name of the XML file.
     * @return the loaded XML document of null if there was an error.
     */
    public static Document getDocument(File fileName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(fileName);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Loads an XML document from an input stream.
     *
     * @param input the input stream.
     * @return the loaded XML document of null if there was an error.
     */
    public static Document getDocument(InputStream input) {
        return getDocument(input, null);
    }

    /**
     * Loads an XML document from an input stream.
     *
     * @param input          the input stream.
     * @param namespaceAware Load namespace aware?
     * @return the loaded XML document of null if there was an error.
     */
    public static Document getDocument(
            InputStream input, Boolean namespaceAware
    ) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        if (namespaceAware != null) {
            factory.setNamespaceAware(namespaceAware);
        }
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(input);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets an XML Document from a remote location.
     *
     * @param url the URL
     * @return and XML Document
     */
    public static Document getDocument(URL url) {
        return getDocument(url, null, null);
    }

    /**
     * Gets an XML Document from a remote location.
     *
     * @param url the URL
     * @param nameSpaceaware if namespace aware or not
     * @return and XML Document
     */
    public static Document getDocument(URL url, boolean nameSpaceaware) {
        return getDocument(url, null, null, nameSpaceaware);
    }

    /**
     * Gets an XML Document from a remote location.
     *
     * @param url            the URL
     * @param userName       the Username {NULL if none needed}
     * @param password       the Password {NULLL if none needed}
     * @param nameSpaceAware if namespace aware or not
     * @return an XML Document
     */
    public static Document getDocument(
            URL url,
            String userName,
            String password,
            boolean nameSpaceAware) {

        CloseableHttpClient client = HTTP.getClient(url, userName, password);
        try {
            HttpGet request = HTTP.getGetRequest(url);
            DocumentResponseHandler handler = new DocumentResponseHandler();
            handler.setNamespaceAware(nameSpaceAware);
            return client.execute(request, handler);
        } catch (IOException | URISyntaxException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            HTTP.closeGraceful(client);
        }
        return null;
    }

    /**
     * Gets an XML Document from a remote location.
     *
     * @param url      the URL
     * @param userName the Username {NULL if none needed}
     * @param password the Password {NULLL if none needed}
     * @return an XML Document
     */
    public static Document getDocument(
            URL url,
            String userName,
            String password) {
        return getDocument(url, userName, password, true);
    }

    /**
     * Builds an XML Document from a String.
     *
     * @param xmlString The String
     * @return The build Document
     */
    public static final Document getDocument(String xmlString) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(xmlString);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Getting a document by posting an xml to a url.
     * @param url the url
     * @param userName the username
     * @param password the password
     * @param postXML the xml to post
     * @param nameSpaceAware if namespace aware
     * @return the returned document
     */
    public static final Document getDocument(
            URL url,
            String userName,
            String password,
            String postXML,
            boolean nameSpaceAware) {
        CloseableHttpClient client = HTTP.getClient(url, userName, password);
        try {
            DocumentResponseHandler handler = new DocumentResponseHandler();
            handler.setNamespaceAware(nameSpaceAware);
            HttpPost request = HTTP.getPostRequest(url);
            InputStream inputStream =
                    new ByteArrayInputStream(postXML.getBytes());
            InputStreamEntity inputStreamEntity = new InputStreamEntity(
                    inputStream);
            request.setHeader("Content-type", "text/xml;charset=utf-8");
            request.setEntity(inputStreamEntity);
            return client.execute(request, handler);
        } catch (IOException
                | URISyntaxException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            HTTP.closeGraceful(client);
        }
        return null;
    }

    /**
     * Gets a Document by posting an XML to the given URL.
     * @param url the URL to post to
     * @param userName the username
     * @param password the password
     * @param postXML the XML that should be posted
     * @param nameSpaceAware if namespace aware or not
     * @return the build document
     */
    public static final Document getDocument(
            URL url,
            String userName,
            String password,
            Document postXML,
            boolean nameSpaceAware) {
        Document doc = null;
        try {
            doc = getDocument(url, userName, password, documentToString(
                            postXML), nameSpaceAware);
        } catch (TransformerException e) {
            log.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
        return doc;
    }

    /**
     * Creates a new XPath without a namespace context.
     *
     * @return the new XPath.
     */
    public static final XPath newXPath() {
        return newXPath(null, null);
    }

    /**
     * Creates a new XPath with a given namespace context.
     *
     * @param namespaceContext The namespace context to be used or null
     *                         if none should be used.
     * @param resolver         The name space resolver.
     * @return The new XPath
     */
    public static final XPath newXPath(
            NamespaceContext namespaceContext,
            XPathVariableResolver resolver
    ) {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
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
     *
     * @param root      The object which is used as the root of the tree to
     *                  be searched in.
     * @param query     The XPath query
     * @param returnTyp The type of the result.
     * @return The result of type 'returnTyp' or null if something
     * went wrong during XPath evaluation.
     */
    public static final Object xpath(
            Object root,
            String query,
            QName returnTyp
    ) {
        return xpath(root, query, returnTyp, null);
    }

    /**
     * Evaluates an XPath query on a given object and returns the result
     * as a string. A given namespace context is used.
     *
     * @param root             The object which is used as the root of the tree
     *                         to be searched in.
     * @param query            The XPath query
     * @param namespaceContext The namespace context to be used or null
     *                         if none should be used.
     * @return The result of the query or null if something went wrong
     * during XPath evaluation.
     */
    public static final String xpathString(
            Object root, String query, NamespaceContext namespaceContext
    ) {
        return (String) xpath(
                root, query, XPathConstants.STRING, namespaceContext);
    }

    /**
     * Evaluates an XPath query on a given object and returns the result
     * as a given type. Optionally a namespace context is used.
     *
     * @param root             The object which is used as the root of the tree
     *                         to be searched in.
     * @param query            The XPath query
     * @param returnType       The type of the result.
     * @param namespaceContext The namespace context to be used or null
     *                         if none should be used.
     * @return The result of type 'returnTyp' or null if something
     * went wrong during XPath evaluation.
     */
    public static final Object xpath(
            Object root,
            String query,
            QName returnType,
            NamespaceContext namespaceContext
    ) {
        return xpath(root, query, returnType, namespaceContext, null);
    }

    /**
     * xpath evaluates ax XPath expression.
     *
     * @param root             The root object of the evaluation being applied
     *                         to.
     * @param query            The XPath query.
     * @param returnType       The type to be returned.
     * @param namespaceContext An optional namespace context.
     * @param variables        A map variables to be used during evaluation.
     * @return The result of the evaluation.
     */
    public static final Object xpath(
            Object root,
            String query,
            QName returnType,
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

    /**
     * containsTags is a cheap way to detect if a XML file
     * contains some special tags. Useful for error detection.
     *
     * @param file The XML file to check.
     * @param tags the list of tags to check for.
     * @return The first tag that was found. Null if the tags are not in file.
     * @throws XMLStreamException if an exception happends while
     *                            processoing the XML file.
     * @throws IOException        if an I/O error happens.
     */
    public static final String containsTags(File file, String[] tags)
            throws XMLStreamException, IOException {

        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        XMLStreamReader xmlr = xmlif.createXMLStreamReader(
                new BufferedInputStream(
                        new FileInputStream(file)));
        try {
            while (xmlr.hasNext()) {
                if (xmlr.next() == XMLEvent.START_ELEMENT) {
                    String needle = xmlr.getLocalName();
                    for (String tag : tags) {
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

    /**
     * Prints a XML Document to a output.
     *
     * @param doc the document
     * @param out the outputstream i.e. System.out
     */
    public static void printDocument(Document doc, OutputStream out) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(new DOMSource(doc),
                    new StreamResult(new OutputStreamWriter(out, "UTF-8")));
        } catch (IOException
                | TransformerException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Prints a Document to a String.
     * @param doc the XML document
     * @return the document as String
     * @throws TransformerException when something goes wrong
     */
    public static String documentToString(Document doc)
            throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString().replaceAll("\n|\r", "");
    }
}
