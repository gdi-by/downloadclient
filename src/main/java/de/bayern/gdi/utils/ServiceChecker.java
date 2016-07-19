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

import de.bayern.gdi.services.ServiceType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
     * @param user The optional user name.
     * @param password The optional password.
     * @return the type of service; null if failed
     */
    public static ServiceType checkService(
        String serviceURL,
        String user,
        String password
    ) {
        try {
            Document doc = XML.getDocument(
                new URL(serviceURL),
                user, password);
            if (doc == null) {
                return null;
            }

            //It seems that there is more than one implementation of this
            //stuff...
            final String wfs = "http://www.opengis.net/wfs/2.0";
            NodeList nl = doc.getElementsByTagNameNS(wfs, "WFS_Capabilities");
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

    public static boolean isRestricted(URL url) {
        try{
            CloseableHttpClient httpCl = HTTP.getClient(url, null, null);
            HttpGet getRequest = HTTP.getGetRequest(url);
            CloseableHttpResponse execute = httpCl.execute(getRequest);
            StatusLine statusLine = execute.getStatusLine();
            if(statusLine.getStatusCode() != 200) {
                return true;
            }
        } catch (URISyntaxException | IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return false;
    }
}
