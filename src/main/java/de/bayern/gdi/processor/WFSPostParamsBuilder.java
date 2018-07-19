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
package de.bayern.gdi.processor;

import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.model.Parameter;
import de.bayern.gdi.processor.DownloadStepConverter.QueryType;
import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.utils.I18n;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A builder for WFS POST request bodies.
 */
public class WFSPostParamsBuilder {

    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;

    private static final String SRS_NAME = "srsName";

    private static final String WFS_NS = "http://www.opengis.net/wfs/2.0";
    private static final String FES_NS = "http://www.opengis.net/fes/2.0";
    private static final String GML_NS = "http://www.opengis.net/gml/3.2";


    private static final String STOREDQUERY_ID = "STOREDQUERY_ID";

    private WFSPostParamsBuilder() {
        // Not for public use.
    }

    /**
     * Creates a new XML document to be send as a POST request.
     *
     * @param dls      The download step.
     * @param usedVars The used variables.
     * @param meta     The WFS meta data.
     * @return The XML document.
     * @throws ConverterException XML setup is bad.
     */
    public static Document create(
        DownloadStep dls,
        Set<String> usedVars,
        WFSMeta meta
    ) throws ConverterException {
        return create(dls, usedVars, meta, false, -1, -1, false);
    }

    /**
     * Creates a new XML document to be send as a POST request.
     *
     * @param dls      The download step.
     * @param usedVars The used variables.
     * @param meta     The WFS meta data.
     * @param ofs      Offset of features.
     * @param count    Limit number of features.
     * @param wfs2     Generate a WFS2 document.
     * @return The XML document.
     * @throws ConverterException XML setup is bad.
     */
    public static Document create(
        DownloadStep dls,
        Set<String> usedVars,
        WFSMeta meta,
        int ofs,
        int count,
        boolean wfs2
    ) throws ConverterException {
        return create(dls, usedVars, meta, false, ofs, count, wfs2);
    }

    /**
     * Creates a new XML document to be send as a POST request.
     *
     * @param dls      The download step.
     * @param usedVars The used variables.
     * @param meta     The WFS meta data.
     * @param hits     Generate a hits document.
     * @return The XML document.
     * @throws ConverterException XML setup is bad.
     */
    public static Document create(
        DownloadStep dls,
        Set<String> usedVars,
        WFSMeta meta,
        boolean hits
    ) throws ConverterException {
        return create(dls, usedVars, meta, hits, -1, -1, false);
    }

    /**
     * Creates a new XML document to be send as a POST request.
     *
     * @param dls      The download step.
     * @param usedVars The used variables.
     * @param meta     The WFS meta data.
     * @param hits     Generate a hits document.
     * @param ofs      Offset of features.
     * @param count    Limit number of features.
     * @param wfs2     Generate a WFS2 document.
     * @return The XML document.
     * @throws ConverterException XML setup is bad.
     */
    public static Document create(
        DownloadStep dls,
        Set<String> usedVars,
        WFSMeta meta,
        boolean hits,
        int ofs,
        int count,
        boolean wfs2
    ) throws ConverterException {

        Document doc = newDocument();

        String outputFormat = "";
        String srsName = "";
        String bbox = "";

        LinkedHashMap<String, String> params = new LinkedHashMap<>();

        for (Parameter p : dls.getParameters()) {
            String value = p.getValue();
            if (!value.isEmpty() && !usedVars.contains(p.getKey())) {
                switch (p.getKey()) {
                    case "outputformat":
                        outputFormat = value;
                        break;
                    case SRS_NAME:
                        srsName = value;
                        break;
                    case "bbox":
                        bbox = value;
                        break;
                    default:
                        params.put(p.getKey(), value);
                        break;
                }
            }
        }

        Element getFeature = createGetFeatureElement(meta, hits, ofs,
            count, wfs2, doc, outputFormat);

        String dataset = dls.getDataset();

        QueryType queryType = DownloadStepConverter.findQueryType(
            dls.getServiceType());

        boolean storedQuery = queryType.equals(QueryType.STOREDQUERY);
        if (storedQuery) {
            Element sqEl = doc.createElementNS(WFS_NS, "wfs:StoredQuery");
            sqEl.setAttribute("id", dataset);
            appendParameters(doc, sqEl, params.entrySet());
            getFeature.appendChild(sqEl);
        } else {
            Element queryEl = doc.createElementNS(WFS_NS, "wfs:Query");
            queryEl.setAttribute("typeNames", dataset);
            int idx = dataset.indexOf(':');
            if (idx >= 0) {
                String namespacePrefix = dataset.substring(0, idx);
                String namespaceUri = meta.getNamespaces()
                    .getNamespaceURI(namespacePrefix);
                queryEl.setAttribute("xmlns:" + namespacePrefix, namespaceUri);
            }
            queryEl.setAttribute(SRS_NAME, srsName);
            appendBBox(doc, bbox, queryEl);
            getFeature.appendChild(queryEl);
        }

        doc.appendChild(getFeature);

        return doc;
    }

    private static Element createGetFeatureElement(WFSMeta meta,
                                                   boolean hits,
                                                   int ofs,
                                                   int count,
                                                   boolean wfs2,
                                                   Document doc,
                                                   String outputFormat) {
        Element getFeature = doc.createElementNS(
            WFS_NS, "wfs:GetFeature");

        getFeature.setAttribute("service", "WFS");

        getFeature.setAttribute("version",
            meta.highestVersion(WFSMeta.WFS2_0_0).toString());

        getFeature.setAttribute("outputFormat", outputFormat);

        if (hits) {
            getFeature.setAttribute("resultType", "hits");
        }

        if (ofs != -1) {
            getFeature.setAttribute(
                "startIndex", String.valueOf(ofs));
            getFeature.setAttribute(wfs2
                    ? "count"
                    : "maxFeatures",
                String.valueOf(count));
        }
        return getFeature;
    }

    private static Document newDocument() throws ConverterException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.newDocument();
        } catch (ParserConfigurationException pce) {
            throw new ConverterException(
                I18n.format("dls.converter.bad.xml", pce));
        }
    }

    private static void appendParameters(
        Document doc,
        Element parent,
        Set<Map.Entry<String, String>> params
    ) {
        for (Map.Entry<String, String> p : params) {
            Element child = doc.createElementNS(WFS_NS, "wfs:Parameter");
            child.setAttribute("name", p.getKey());
            child.setTextContent(p.getValue());
            parent.appendChild(child);
        }
    }

    private static void appendBBox(
        Document doc,
        String bbox,
        Element parent
    ) {
        String[] bboxArr = bbox.split(",");
        if (bboxArr.length == FIVE) {
            Element filterEl = doc.createElementNS(FES_NS, "fes:Filter");
            Element bboxEl = doc.createElementNS(FES_NS, "fes:BBOX");

            Element envEl = doc.createElementNS(GML_NS, "gml:Envelope");
            envEl.setAttribute(SRS_NAME, bboxArr[FOUR]);

            Element lcEl = doc.createElementNS(GML_NS, "gml:lowerCorner");
            lcEl.setTextContent(bboxArr[ZERO] + " " + bboxArr[ONE]);

            Element ucEl = doc.createElementNS(GML_NS, "gml:upperCorner");
            ucEl.setTextContent(bboxArr[TWO] + " " + bboxArr[THREE]);

            envEl.appendChild(lcEl);
            envEl.appendChild(ucEl);

            bboxEl.appendChild(envEl);
            filterEl.appendChild(bboxEl);
            parent.appendChild(filterEl);
        }
    }
}
