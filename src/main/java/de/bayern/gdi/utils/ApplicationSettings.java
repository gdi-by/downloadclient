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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class managing application settings.
 * @author Alexander Woestmann (awoestmann@intevation)
 */

public class ApplicationSettings {

    private Map<String, String> settings;

    public ApplicationSettings(Document doc) throws IOException {
        parseDocument(doc);
    }

    private void parseDocument(Document xmlDocument) throws IOException {
        this.settings = parseNodeForElements(
                xmlDocument, "application");
    }

    /**Parse Node by name, save all elements to map.*/
    private Map<String, String> parseNodeForElements(Document doc,
            String nodeName) throws IOException {

        NodeList nodes = doc.getElementsByTagName(nodeName);
        if (nodes.getLength() < 1) {
            throw new IOException("Node " + nodeName + " not found");
        }
        Node parent = nodes.item(0);

        Map<String, String> elements = new HashMap<>();

        NodeList childs = parent.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                elements.put(node.getNodeName(), node.getTextContent());
            }
        }
        return elements;
    }

    /**
     * Returns string value of a application settings item.
     * @param name Name of the setting
     * @return String value
     */
    public String getApplicationSetting(String name) {
        return settings.get(name);
    }

    @Override
    public String toString() {
        return "ApplicationSettings: {"
            +  settings
            + "}";
    }
}
