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

import de.bayern.gdi.utils.StringUtils;
import de.bayern.gdi.utils.XML;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;
import net.opengis.cat.csw20.CapabilitiesType;
import net.opengis.ows10.ServiceProviderType;
import org.geotools.csw.CSWConfiguration;
import org.geotools.xml.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class CatalogService {

    private URL catalogURL;
    private String userName;
    private String password;
    private static final Logger log
            = Logger.getLogger(CatalogService.class.getName());
    private ServiceProviderType serviceProvider;

    /**
     * Constructor.
     * @param url String URL
     * @param userName Username
     * @param password Password
     * @throws MalformedURLException
     */
    public CatalogService(String url, String userName, String password) throws
            MalformedURLException {
        this(new URL(url), userName, password);
    }

    /**
     * Constructor.
     * @param url Stirng URL
     * @throws MalformedURLException When URL has bad format
     */
    public CatalogService(String url) throws MalformedURLException {
        this(url, null, null);
    }

    /**
     * Constructor.
     * @param url URL
     */
    public CatalogService(URL url) {
        this(url, null, null);
    }

    /**
     * Constructor.
     * @param url URL
     * @param userName Username
     * @param password Password
     */
    public CatalogService(URL url, String userName, String password) {
        this.catalogURL = url;
        this.userName = userName;
        this.password = password;
        Object parsed = getParsedObject(this.catalogURL);
        CapabilitiesType servciceCapType = (CapabilitiesType) parsed;
        this.serviceProvider = servciceCapType.getServiceProvider();
    }

    /**
     * gets the Name of the service Provider.
     * @return Name of the service Provider
     */
    public String getProviderName() {
        return this.serviceProvider.getProviderName();
    }

    /**
     * retrieves a Map of ServiceNames and URLs for a Filter-Word.
     * @param filter the Word to filter to
     * @return Map of Service Names and URLs
     */
    public Map<String, String> getServicesByFilter(String filter) {
        System.out.println("Search for: " + filter);
        Map<String, String> map = new HashMap<>();
        URL requestURL = setURLRequestAndSearch(filter);
        /* FIXME - WHEN TRYING TO USE IMPLEMENTED STUFF EXCEPTION RISES:
        java.lang.NoSuchMethodException:
        net.opengis.cat.csw20.impl.Csw20FactoryImpl.createAbstractRecordType()
        Use this piece of code to retrieve expected Excpetion
        Object parsed = this.getParsedObject(requestURL);
        GetRecordsResponseType records = (GetRecordsResponseType) parsed;
        SearchResultsType searchResults = records.getSearchResults();
        for (AbstractRecordType recordType : searchResults.getAbstractRecord
                ()) {
            System.out.println(recordType.toString());
        }
        */

        Document xml = XML.getDocument(requestURL,
                this.userName,
                this.password);
        NodeList mdMetadataNL = xml.getElementsByTagName("gmd:MD_Metadata");
        Node result;
        //TODO - Implement Handling when Returned Set is empty
        for (int i = 0; i < /*mdMetadataNL.getLength()*/ 1; i++) {
            result = mdMetadataNL.item(i);
            String serviceName = null;
            String serviceURL = null;
            for(int j = 0; j < result.getChildNodes().getLength(); j++) {
                Node outerNode;
                outerNode = result.getChildNodes().item(j);
                //Node nameNode =
                //if(nameNode != null) {
                //    serviceName = nameNode.getTextContent();
                //}
                /*
                if(outerNode.getNodeName().equals
                        ("gmd:metadataStandardName")) {
                    //System.out.println("Name Node Found!");
                    //System.out.println("Name Node Found!");
                    //System.out.println(outerNode.getNodeName());
                    NodeList nameNL = outerNode.getChildNodes();
                    for(int k = 0; k < nameNL.getLength(); k++) {
                        if(nameNL.item(k).getNodeName().equals
                                ("gco:CharacterString")) {
                            Node nameNode = nameNL.item(k);
                            //System.out.println(nameNode.getTextContent());
                            serviceName = nameNode.getTextContent();
                        }
                    }

                }*/
                /*
                if(outerNode.getNodeName().equals
                        ("gmd:distributionInfo")) {
                    NodeList distrInfoNL = outerNode.getChildNodes();
                    for(int k = 0; k < distrInfoNL.getLength(); k++ ) {
                        if(distrInfoNL.item(k).getNodeName().equals
                                ("gmd:MD_Distribution")) {
                            NodeList mdDistrNL = distrInfoNL.item(k)
                                    .getChildNodes();
                            for(int l = 0; l < mdDistrNL.getLength(); l++) {
                                if(mdDistrNL.item(l).getNodeName().equals
                                        ("gmd:transferOptions")) {
                                    NodeList to = mdDistrNL.item(l)
                                            .getChildNodes();
                                    for(int m = 0; m < to.getLength(); m++) {
                                        if(to.item(m).getNodeName().equals
                                                ("gmd:MD_DigitalTransferOptions")) {
                                            FUUUUUUUU
                                        }
                                    }
                                }
                            }
                        }
                    }
                }*/
                //Node urlNode = getURLNode(outerNode);
                //if(urlNode != null) {
                //    serviceURL =urlNode.getTextContent();
                //}
            }
            System.out.println(serviceName);
            System.out.println(serviceURL);
            if(serviceName != null && serviceURL != null ) {
                map.put(serviceName, serviceURL);
            }
        }
        return map;
    }

    private URL setURLRequestAndSearch(String search) {
        URL newURL = null;
        try {
            newURL = new URL(this.catalogURL.toString().replace(
                    "GetCapabilities", "GetRecords"
                            + "&version=2.0.2"
                            + "&namespace=xmlns"
                            + "(csw=http://www.opengis.net/cat/csw/2.0.2),"
                            + "xmlns(gmd=http://www.isotc211.org/2005/gmd)"
                            + "&resultType=results"
                            + "&outputFormat=application/xml"
                            + "&outputSchema=http://www.isotc211.org/2005/gmd"
                            + "&startPosition=1"
                            + "&maxRecords=20"
                            + "&typeNames=csw:Record"
                            + "&elementSetName=full"
                            + "&constraintLanguage=CQL_TEXT"
                            + "&constraint_language_version=1.1.0"
                            + "&constraint=csw:AnyText='" + search + "'"));
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return newURL;
    }

    private Object getParsedObject(URL url) {
        Object parsed = null;
        try {
            CSWConfiguration configuration =
                    new CSWConfiguration();
            Parser parser = new Parser(configuration);
            URLConnection conn = null;
            if (url.toString().toLowerCase().startsWith("https")) {
                HttpsURLConnection con
                        = (HttpsURLConnection) url.openConnection();
                conn = (URLConnection) con;
            } else {
                conn = url.openConnection();
            }
            if (StringUtils.getBase64EncAuth(
                    this.userName, this.password) != null) {
                conn.setRequestProperty("Authorization", "Basic "
                        + StringUtils.getBase64EncAuth(
                        this.userName, this.password));
            }
            InputStream is = conn.getInputStream();
            InputSource xml = new InputSource(is);
            parsed = parser.parse(xml);
        } catch (IOException
                | SAXException
                | ParserConfigurationException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return parsed;
    }
}
