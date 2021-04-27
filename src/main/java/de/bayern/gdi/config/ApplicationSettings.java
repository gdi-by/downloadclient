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

import de.bayern.gdi.utils.CryptoUtils;
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

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationSettings.class.getName());

    private static final int DEFAULT_TIMEOUT = 10000;

    private static final int S_TO_MS = 1000;

    private final Document doc;

    private final Settings settings;

    private int requestTimeOutInMS = DEFAULT_TIMEOUT;

    private Credentials credentials;

    public ApplicationSettings(Document doc, Settings settings) throws IOException {
        this.doc = doc;
        this.settings = settings;
        parseNodeForElements("application");
    }

    /**
     * Returns the configured request timeout in ms.
     *
     * @return the request timeout in ms
     */
    public int getRequestTimeoutInMs() {
        return requestTimeOutInMS;
    }

    /**
     * Returns the configured credentials.
     *
     * @return the configured credentials, <code>null</code> if not configured
     */
    public Credentials getCredentials() {
        return this.credentials;
    }

    /**
     * Writes the passed credentials to the settings.xml.
     *
     * @param credentialsToPersist the credentials to store, if <code>null</code>
     *                             or username or password <code>null</code>
     *                             nothing is persisted.
     */
    public void persistCredentials(Credentials credentialsToPersist) {
        if (credentialsToPersist != null
            && credentialsToPersist.getUsername() != null
            && !credentialsToPersist.getUsername().isEmpty()
            && credentialsToPersist.getPassword() != null
            && !credentialsToPersist.getPassword().isEmpty()
            && !credentialsToPersist.equals(this.credentials)) {
            LOG.info("Saving credentials in settings.xml");
            modifyDocument(credentialsToPersist);
            settings.persistSettingsFile(doc);
        }
        this.credentials = credentialsToPersist;
    }

    @Override
    public String toString() {
        return "ApplicationSettings: ["
            + "requestTimeOutInMS: " + requestTimeOutInMS
            + ", credentials: " + credentials
            + "]";
    }

    /**
     * Parse application from settings.xml.
     */
    private void parseNodeForElements(String nodeName) throws IOException {
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
                } else if ("credentials".equals(node.getNodeName())) {
                    parseCredentials(node);
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

    private void parseCredentials(Node credentialsNode) {
        String username = null;
        String password = null;
        NodeList childs = credentialsNode.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            if ("username".equals(node.getNodeName())) {
                username = node.getTextContent();
            } else if ("password".equals(node.getNodeName())) {
                password = decrypt(node.getTextContent());
            }
        }
        if (username != null) {
            this.credentials = new Credentials(username, password);
        }
        LOG.debug("Found credentials for username " + username);
    }

    private void modifyDocument(Credentials newCredentials) {
        NodeList nodes = doc.getElementsByTagName("application");
        if (nodes.getLength() < 1) {
            LOG.warn("Credentials could not be stored. No application element found in settings.xml");
        }
        Node applicationNode = nodes.item(0);

        Node credentialsNode = parseNode(applicationNode, "credentials");
        if (credentialsNode == null) {
            credentialsNode = doc.createElement("credentials");
            applicationNode.appendChild(credentialsNode);
        }
        persistCredentials(credentialsNode, newCredentials);
    }

    private void persistCredentials(Node credentialsNode, Credentials newCredentials) {
        Node usernameNode = parseNode(credentialsNode, "username");
        Node passwordNode = parseNode(credentialsNode, "password");
        if (usernameNode == null) {
            usernameNode = doc.createElement("username");
            credentialsNode.appendChild(usernameNode);
        }
        if (passwordNode == null) {
            passwordNode = doc.createElement("password");
            credentialsNode.appendChild(passwordNode);
        }
        usernameNode.setTextContent(newCredentials.getUsername());
        passwordNode.setTextContent(encrypt(newCredentials.getPassword()));
    }

    private Node parseNode(Node parentNode, String nodeName) {
        NodeList childs = parentNode.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (nodeName.equals(node.getNodeName())) {
                    return node;
                }
            }
        }
        return null;
    }

    private String decrypt(String password) {
        if (password != null) {
            try {
                return CryptoUtils.decrypt(password);
            } catch (Exception e) {
                LOG.error("Password could not be decrypted", e);
            }
        }
        return password;
    }

    private String encrypt(String password) {
        if (password != null) {
            try {
                return CryptoUtils.encrypt(password);
            } catch (Exception e) {
                LOG.error("Password could not be encrypted", e);
            }
        }
        return password;
    }
}
