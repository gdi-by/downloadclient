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
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.bayern.gdi.services.ServiceType;

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
     * @param authStr the String with the Authentication details
     * @return the type of service; null if failed
     */
    public static ServiceType checkService(String serviceURL, String
            authStr) {
        try {
            URL url = new URL(serviceURL);
            URLConnection conn = null;
            if (url.toString().toLowerCase().startsWith("https")) {
                System.setProperty("jsse.enableSNIExtension", "false");
                conn = url.openConnection();
            }
            conn = url.openConnection();
            if (authStr != null) {
                conn.setRequestProperty("Authorization", "Basic " + authStr);
            }
            Document doc = XML.getDocument(conn.getInputStream());
            if (doc == null) {
                return null;
            }

            //It seems that there is more than one implementation of this
            //stuff...
            NodeList nl = doc.getElementsByTagName("wfs:WFS_Capabilities");
            if (nl.getLength() == 0) {
                nl = doc.getElementsByTagName("WFS_Capabilities");
            }
            if (nl.getLength() == 0) {
                nl = doc.getElementsByTagName("WFS_CAPABILITIES");
            }
            if (nl.getLength() == 0) {
                nl = doc.getElementsByTagName("wfs_capabilities");
            }
            if (nl.getLength() == 0) {
                nl = doc.getElementsByTagName("wfs:wfs_capabilities");
            }
            if (nl.getLength() == 0) {
                nl = doc.getElementsByTagName("WFS:wfs_capabilities");
            }
            if (nl.getLength() == 0) {
                nl = doc.getElementsByTagName("wfs:WFS_CAPABILITIES");
            }
            if (nl.getLength() == 0) {
                nl = doc.getElementsByTagName("WFS:WFS_CAPABILITIES");
            }
            if (nl.getLength() != 0) {
                NamedNodeMap nnm = nl.item(0).getAttributes();
                switch (nnm.getNamedItem("version").getNodeValue()) {
                    case "1.0.0":
                    case "1.1.0":
                        return ServiceType.WFSOne;
                    case "2.0.0":
                        return ServiceType.WFSTwo;
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
                    return ServiceType.Atom;
                }
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }
}
