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

package de.bayern.gdi.helper;

import de.bayern.gdi.services.WebService;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.net.URLConnection;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class ServiceChecker {

    private static final Logger log
        = Logger.getLogger(ServiceChecker.class.getName());

    private ServiceChecker() {
    }

    /**
     * checks the service type.
     * @param serviceURL the service url
     * @return the type of service; null if failed
     */
    public static WebService.Type checkService(String serviceURL) {
        try {
            URL url = new URL(serviceURL);
            URLConnection conn = url.openConnection();

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(conn.getInputStream());

            NodeList nl = doc.getElementsByTagName("wfs:WFS_Capabilities");
            if (nl.getLength() != 0) {
                NamedNodeMap nnm = nl.item(0).getAttributes();
                switch (nnm.getNamedItem("version").getNodeValue()) {
                    case "1.0.0":
                    case "1.1.0":
                        return WebService.Type.WFSOne;
                    case "2.0.0":
                        return WebService.Type.WFSTwo;
                    default:
                        return null;
                }
            }
            nl = doc.getElementsByTagName("feed");
            if (nl.getLength() != 0) {
                Node n = nl.item(0);
                NamedNodeMap nnm = n.getAttributes();
                String wfsVersion = nnm.getNamedItem("xmlns").getNodeValue();
                if (wfsVersion.toLowerCase().endsWith("atom")) {
                    return WebService.Type.Atom;
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }
}
