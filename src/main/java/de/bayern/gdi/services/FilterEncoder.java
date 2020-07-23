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

import de.bayern.gdi.processor.ConverterException;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.filter.v2_0.FES;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.opengis.filter.Filter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Geotools 16.1 FilterEncoder (eCQL Filter).
 *
 * @author Juergen Weichand, Guy Nokam
 */
public class FilterEncoder {

    private static final int INDENTSIZE = 3;

    /**
     * Validates the userInput.
     *
     * @param userInput the queries to validate
     * @throws CQLException       if the CQL is not valid
     * @throws ConverterException if the validation fails unexpected
     */
    public void validateCql(String userInput)
        throws CQLException, ConverterException {
        initializeQueries(userInput);
    }

    /**
     * We want to hold all the queries.
     *
     * @param userInput extract all queries
     * @return the encoder filters
     * @throws CQLException       CQL was not valid
     * @throws ConverterException if the creation of the filter fails
     * @throws IllegalArgumentException if no CQL expression is provided
     */
    public List<QueryToFeatureType> initializeQueries(String userInput)
        throws CQLException, ConverterException {
        if (userInput == null) {
            throw new IllegalArgumentException("CQL must not be null");
        }
        return cleanUpQueries(userInput);
    }

    private List<QueryToFeatureType> cleanUpQueries(String inputs)
        throws CQLException, ConverterException {
        List<QueryToFeatureType> complexQueries = new ArrayList<>();
        String[] queries = inputs.split("\\r\\n|\\n|\\r");
        for (String query : queries) {
            if (query != null) {
                if (query.toLowerCase().contains("where")) {
                    QueryToFeatureType queryToFeatureType =
                        createFilterForQueryWithFeatureType(query);
                    complexQueries.add(queryToFeatureType);
                } else {
                    Document filter = createFilter(query);
                    QueryToFeatureType queryToFeatureType =
                        new QueryToFeatureType(filter);
                    complexQueries.add(queryToFeatureType);
                }
            }
        }
        return complexQueries;
    }

    private QueryToFeatureType createFilterForQueryWithFeatureType(
        String query)
        throws CQLException, ConverterException {

        String[] wheres = query.split("(?i)WHERE");
        if (wheres.length > 1) {
            String featureType = cleanUpFeatureTypeName(wheres[0]);
            String cqlQuery = wheres[1];
            Document filter = createFilter(cqlQuery);
            return new QueryToFeatureType(featureType, filter);
        }
        return null;
    }

    private Document createFilter(String cqlQuery)
        throws CQLException, ConverterException {
        try {
            Filter filter = ECQL.toFilter(cqlQuery);
            Configuration configuration =
                new org.geotools.filter.v2_0.FESConfiguration();
            Encoder encoder = new Encoder(configuration);
            encoder.setIndenting(true);
            encoder.setIndentSize(INDENTSIZE);
            encoder.setOmitXMLDeclaration(true);

            return encoder.encodeAsDOM(filter, FES.Filter);
        } catch (IOException | TransformerException | SAXException e) {
            throw new ConverterException(
                "Converting from CQL to Filter failed", e);
        }
    }

    private String cleanUpFeatureTypeName(String where) {
        return where.replace("\"", "").trim();
    }

    /**
     * Encapsulates the feature type and filter.
     */
    public class QueryToFeatureType {

        private final String featureType;

        private final Document filter;

        /**
         * Instantiates a QueryToFeatureType without feature type,
         * if not parseable from the CQL.
         *
         * @param filter the created filter. never <code>null</code>
         */
        public QueryToFeatureType(Document filter) {
            this(null, filter);
        }

        /**
         * Instantiates a QueryToFeatureType with feature type.
         *
         * @param featureType name of the fetaure type
         * @param filter      the created filter. never <code>null</code>
         */
        public QueryToFeatureType(String featureType, Document filter) {
            this.featureType = featureType;
            this.filter = filter;
        }

        /**
         * @return the name of the feature type, may be <code>null</code>
         * if not parseable from CQL
         */
        public String getFeatureType() {
            return featureType;
        }

        /**
         * @return the parsed filter, never <code>null</code>
         */
        public Document getFilter() {
            return filter;
        }

    }

}
