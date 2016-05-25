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

import com.vividsolutions.jts.geom.Envelope;

import de.bayern.gdi.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import javax.xml.parsers.ParserConfigurationException;

import net.opengis.ows11.OperationType;
import net.opengis.ows11.OperationsMetadataType;

import net.opengis.wfs20.DescribeStoredQueriesResponseType;
import net.opengis.wfs20.ParameterExpressionType;
import net.opengis.wfs20.StoredQueryDescriptionType;
import net.opengis.wfs20.WFSCapabilitiesType;

import org.eclipse.emf.common.util.EList;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.geotools.xml.Parser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class WFSTwo extends WebService {

    private static final Logger log
            = Logger.getLogger(WFSTwo.class.getName());

    private String serviceURL;
    private ArrayList<String> requestMethods;
    private WFSOne wfsOne;
    private String password;
    private String userName;

    /**
     * Constructor.
     *
     * @param serviceURL URL to Service
     * @param userName userName if auth is needed else null
     * @param password pw if auth is needed else null
     */
    public WFSTwo(String serviceURL, String userName, String password) {
        this.serviceURL = serviceURL;
        this.userName = userName;
        this.password = password;
        this.requestMethods = new ArrayList();
        org.geotools.wfs.v2_0.WFSCapabilitiesConfiguration configuration =
                new org.geotools.wfs.v2_0.WFSCapabilitiesConfiguration();
        Parser parser = new Parser(configuration);
        URLConnection conn = null;
        try {
            URL url = new URL(this.serviceURL);
            if (url.toString().toLowerCase().startsWith("https")) {
                HttpsURLConnection con
                    = (HttpsURLConnection)url.openConnection();
                conn = (URLConnection) con;
            }  else {
                conn = url.openConnection();
            }
            if (StringUtils.getBase64EncAuth(
                this.userName, this.password) != null) {
                conn.setRequestProperty("Authorization", "Basic "
                        + StringUtils.getBase64EncAuth(
                            this.userName, this.password));
            }
            InputStream is = conn.getInputStream();
            /*
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is));
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
            */
            InputSource xml = new InputSource(is);
            Object parsed = parser.parse(xml);
            WFSCapabilitiesType caps = (WFSCapabilitiesType) parsed;
            OperationsMetadataType om = caps.getOperationsMetadata();
            for (int i = 0; i < om.getOperation().size(); i++) {
                this.requestMethods.add(
                        ((OperationType)
                                om.getOperation().get(i)).getName());
            }
            this.wfsOne = new WFSOne(this.serviceURL,
                    this.userName, this.password);
        } catch (RuntimeException
                | IOException
                | SAXException
                | ParserConfigurationException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * @return the Types of the Service
     * @inheritDoc
     */
    public ArrayList<String> getTypes() {
        return this.wfsOne.getTypes();
    }

    /**
     * @param type the Type
     * @return the Attributes of a Type
     * @inheritDoc
     */
    public Map<String, String> getAttributes(String type) {
        return this.wfsOne.getAttributes(type);
    }

    /**
     * Experimental Class to get the Bounds of a Type.
     *
     * @param outerBBOX the Outer Bounding Box
     * @param typeName  the Type Name
     * @return the Bounds
     */
    public ReferencedEnvelope getBounds(Envelope outerBBOX,
                                        String typeName) {
        return null;
    }

    /**
     * @return the URL of the Service
     * @inheritDoc
     */
    public String getServiceURL() {
        return this.serviceURL;
    }

    /**
     * @return the stored Queries
     * @inheritDoc
     */
    @Override
    public ArrayList<String> getStoredQueries() {
        ArrayList<String> storedQueries = new ArrayList();
        EList<StoredQueryDescriptionType> storedQueryDescription =
                getDescribeStoredQueries();
        for (Iterator it = storedQueryDescription.iterator(); it.hasNext();) {
            StoredQueryDescriptionType sqdt =
                    (StoredQueryDescriptionType) it.next();
            storedQueries.add(sqdt.getId());
        }
        return storedQueries;
    }

    private EList<StoredQueryDescriptionType> getDescribeStoredQueries() {
        String describeStroedQueriesURL =
                setURLRequest("DescribeStoredQueries");
        org.geotools.wfs.v2_0.WFSCapabilitiesConfiguration configuration =
                new org.geotools.wfs.v2_0.WFSCapabilitiesConfiguration();
        Parser parser = new Parser(configuration);
        EList<StoredQueryDescriptionType> storedQueryDescription = null;
        try {
            URL url = new URL(describeStroedQueriesURL);
            InputSource xml = new InputSource(url.openStream());
            Object parsed = parser.parse(xml);

            DescribeStoredQueriesResponseType storedQueriesType =
                    (DescribeStoredQueriesResponseType) parsed;
            storedQueryDescription =
                    storedQueriesType.getStoredQueryDescription();
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return storedQueryDescription;
    }

    /**
     * @return NULL
     * @inheritDoc
     */
    @Override
    public Map<String, String> getParameters(String queryName) {
        Map<String, String> parameters = new HashMap<String, String>();
        EList<StoredQueryDescriptionType> storedQueryDescription =
                getDescribeStoredQueries();
        for (Iterator it = storedQueryDescription.iterator(); it.hasNext();) {
            StoredQueryDescriptionType sqdt =
                    (StoredQueryDescriptionType) it.next();
            if (sqdt.getId().equals(queryName)) {
                EList<ParameterExpressionType> parameterList
                        = sqdt.getParameter();
                for (Iterator it2 = parameterList.iterator();
                     it2.hasNext();) {
                    ParameterExpressionType parameter =
                            (ParameterExpressionType) it2.next();
                    parameters.put(parameter.getName(),
                            parameter.getType().toString());
                }
            }
        }
        return parameters;
    }

    /**
     * @return the Methods that can be requested
     * @inheritDoc
     */
    @Override
    public ArrayList<String> getRequestMethods() {
        return this.requestMethods;
    }

    private String setURLRequest(String request) {
        String newURL = "";
        try {
            if (this.requestMethods.contains(request)) {
                newURL = this.serviceURL.replace("GetCapabilities", request);
            } else {
                throw new MalformedURLException("Service not capable for this"
                        + "request");
            }
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return newURL;
    }

    /**
     * @return the Type
     * @inheritDoc
     */
    public WebService.Type getServiceType() {
        return Type.WFSTwo;
    }

}
