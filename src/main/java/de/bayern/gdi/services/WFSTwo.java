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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import net.opengis.ows11.OperationType;
import net.opengis.ows11.OperationsMetadataType;
import net.opengis.wfs20.ListStoredQueriesResponseType;
import net.opengis.wfs20.StoredQueryListItemType;
import net.opengis.wfs20.WFSCapabilitiesType;
import org.eclipse.emf.common.util.EList;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.xml.Parser;
import org.opengis.feature.type.AttributeType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class WFSTwo extends WebService {

    private String serviceURL;
    private static final Logger log
            = Logger.getLogger(WFSTwo.class.getName());
    private ArrayList<String> requestMethods;
    private WFSOne wfsOne;

    /**
     * Constructor.
     * @param serviceURL URL to Service
     */
    public WFSTwo(String serviceURL) {
        this.serviceURL = serviceURL;
            this.requestMethods = new ArrayList();
            org.geotools.wfs.v2_0.WFSCapabilitiesConfiguration configuration =
                    new org.geotools.wfs.v2_0.WFSCapabilitiesConfiguration();
            Parser parser = new Parser(configuration);
            try {
                URL url = new URL(this.serviceURL);
                InputSource xml = new InputSource(url.openStream());
                Object parsed = parser.parse(xml);
                WFSCapabilitiesType caps = (WFSCapabilitiesType) parsed;
                OperationsMetadataType om = caps.getOperationsMetadata();
                for (int i = 0; i < om.getOperation().size(); i++) {
                    this.requestMethods.add(
                            ((OperationType)
                                    om.getOperation().get(i)).getName());
                }
                this.wfsOne = new WFSOne(this.serviceURL);
            } catch (IOException
                    | SAXException
                    | ParserConfigurationException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
    }

    /**
     * @inheritDoc
     * @return the Types of the Service
     */
    public ArrayList<String> getTypes() {
        return this.wfsOne.getTypes();
    }

    /**
     * @inheritDoc
     * @param type the Type
     * @return the Attributes of a Type
     */
    public ArrayList<AttributeType> getAttributes(String type) {
        return this.wfsOne.getAttributes(type);
    }

    /**
     * Experimental Class to get the Bounds of a Type.
     * @param outerBBOX the Outer Bounding Box
     * @param typeName the Type Name
     * @return the Bounds
     */
    public ReferencedEnvelope getBounds(Envelope outerBBOX,
                                        String typeName) {
        return null;
    }

    /**
     * @inheritDoc
     * @return the URL of the Service
     */
    public String getServiceURL() {
        return this.serviceURL;
    }

    /**
     * @inheritDoc
     * @return the stored Queries
     */
    @Override
    public ArrayList<String> getStoredQueries()  {
        ArrayList<String> storedQueries = new ArrayList();
        String storedQueriesURL = setURLRequest("ListStoredQueries");

        org.geotools.wfs.v2_0.WFSCapabilitiesConfiguration configuration =
                new org.geotools.wfs.v2_0.WFSCapabilitiesConfiguration();
        Parser parser = new Parser(configuration);
        try {
            URL url = new URL(storedQueriesURL);
            InputSource xml = new InputSource(url.openStream());
            Object parsed = parser.parse(xml);
            ListStoredQueriesResponseType storedQueriesResponse =
                    (ListStoredQueriesResponseType) parsed;
            EList<StoredQueryListItemType> storedQueryList =
                    storedQueriesResponse.getStoredQuery();

            for (Iterator it = storedQueryList.iterator(); it.hasNext();) {
                StoredQueryListItemType sle =
                        (StoredQueryListItemType) it.next();
                storedQueries.add(sle.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return storedQueries;
    }

    /**
     * @inheritDoc
     * @return the Methods that can be requested
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

}
