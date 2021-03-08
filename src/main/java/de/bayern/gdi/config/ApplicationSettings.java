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

package de.bayern.gdi.config;

import de.bayern.gdi.utils.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;

/**
 * Class managing application settings.
 *
 * @author Alexander Woestmann (awoestmann@intevation)
 */

public class ApplicationSettings {

    private static final int DEFAULT_TIMEOUT = 10000;

    private static final int S_TO_MS = 1000;

    private static final Logger LOG = LoggerFactory.getLogger(HTTP.class.getName());

    private int requestTimeOutInMS = DEFAULT_TIMEOUT;

    public ApplicationSettings(Document doc) throws IOException {
        parseDocument(doc);
    }

    /**
     * Returns the configured request timeout in ms.
     *
     * @return the request timeout in ms
     */
    public int getRequestTimeoutInMs() {
        return requestTimeOutInMS;
    }

    public Credentials getCredentials() {
        //TODO!
        return new Credentials("bc", "pw");
    }

    @Override
    public String toString() {
        return "ApplicationSettings: {"
            + "requestTimeOutInMS: " + requestTimeOutInMS
            + "}";
    }


    private void parseDocument(Document xmlDocument) throws IOException {
        parseNodeForElements(xmlDocument, "application");
    }

    /**
     * Parse application from settings.xml.
     */
    private void parseNodeForElements(Document doc,
                                      String nodeName) throws IOException {
        NodeList nodes = doc.getElementsByTagName(nodeName);
        if (nodes.getLength() < 1) {
            throw new IOException("Node " + nodeName + " not found");
        }
        Node parent = nodes.item(0);

        NodeList childs = parent.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if ("requestTimeout_s".equals(node.getNodeName())) {
                    parseRequestTimeout(node);
                }
            }
        }
    }

    private void parseRequestTimeout(Node node) {
        String ts = node.getTextContent();
        if (ts != null) {
            try {
                requestTimeOutInMS = S_TO_MS * Integer.parseInt(ts);
            } catch (NumberFormatException nfe) {
                LOG.error(nfe.getMessage());
            }
        }
    }

}
