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

import de.bayern.gdi.model.Query;
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
import java.util.Arrays;
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
     * @throws CQLException if the CQL is not valid
     */
    public void validateCql(String userInput)
        throws CQLException {
        initializeQueries(userInput);
    }

    /**
     * We want to hold all the queries.
     *
     * @param userInput extract all queries
     * @return the encoder filters
     * @throws CQLException something went wrong.
     */
    public List<Document> initializeQueries(String userInput)
        throws CQLException {
        List<String> queries = identifySoloQuery(userInput);
        List<Query> complexQueries = cleanUpQueries(queries);
        return doProduceEcqlFilters(complexQueries);
    }

    /**
     * Identify every single query.
     * (Coming from the textarea)
     *
     * @param inputs
     */
    private List<String> identifySoloQuery(String inputs) {
        if (inputs == null) {
            throw new IllegalArgumentException("CQL must not be null");
        }

        String[] queriesString = inputs.split("\\r\\n|\\n|\\r");
        return Arrays.asList(queriesString);
    }

    /**
     * Collect all the entire queries.
     *
     * @param queries
     */
    private List<Query> cleanUpQueries(List<String> queries) {
        List<Query> complexQueries = new ArrayList<>();

        for (String query : queries) {
            if (query != null) {
                if (query.contains("WHERE")
                    || query.contains("Where")
                    || query.contains("where")) {

                    String[] wheres = splitCQLRequest(query);

                    if (wheres.length != 0) {
                        Query aQuery = initializeQueryObject(wheres);
                        complexQueries.add(aQuery);
                    }
                } else {
                    Query aQuery = new Query("", query, false);
                    complexQueries.add(aQuery);
                }
            }
        }
        return complexQueries;
    }

    /**
     * Generate the eCQL-Filter.
     * (based on the query list that has been establish)
     *
     * @param complexQueries
     */
    private List<Document> doProduceEcqlFilters(List<Query> complexQueries)
        throws CQLException {
        List<Document> filters = new ArrayList<>();
        for (Query query : complexQueries) {
            try {
                Filter filter = ECQL.toFilter(query.getValue());
                Configuration configuration =
                    new org.geotools.filter.v2_0.FESConfiguration();
                Encoder encoder = new Encoder(configuration);
                encoder.setIndenting(true);
                encoder.setIndentSize(INDENTSIZE);
                encoder.setOmitXMLDeclaration(true);

                Document filterDocument = encoder.encodeAsDOM(
                    filter, FES.Filter);
                if (filterDocument != null) {
                    filters.add(filterDocument);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                // TODO
                e.printStackTrace();
            } catch (SAXException e) {
                // TODO
                e.printStackTrace();
            }
        }
        return filters;
    }

    /**
     * Initialize the query Object.
     *
     * @param wheres an array.
     * @return aQuery - An Object from type Query.
     */
    private Query initializeQueryObject(String[] wheres) {
        Query aQuery = new Query();
        String key = wheres[0];
        key = key.replace("\"", "");
        key = key.trim();
        aQuery.setKey(key);
        aQuery.setValue(wheres[1]);
        aQuery.setECQLEntireRequest(true);
        return aQuery;
    }

    /**
     * Retrieve the Where-Part of the eCQL-Request.
     *
     * @param query is suppose to be the entire eCQL
     * @return wheres, an array of two elements.
     */
    private String[] splitCQLRequest(String query) {
        String[] wheres = query.split("WHERE");
        if (wheres == null) {
            wheres = query.split("Where");
        }
        if (wheres == null) {
            wheres = query.split("where");
        }
        return wheres;
    }

}
