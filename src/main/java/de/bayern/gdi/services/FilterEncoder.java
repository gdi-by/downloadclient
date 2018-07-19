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
import java.util.logging.Logger;

/**
 * Geotools 16.1 FilterEncoder (eCQL Filter).
 *
 * @author Juergen Weichand, Guy Nokam
 */
public class FilterEncoder {

    private static final Logger log =
            Logger.getLogger(FilterEncoder.class.getName());
    private static final String PERCENT_NUMBER = "\u0025";
    private static final String ASTERISK = "\u002A";
    private List<String> queries;
    private List<String> eCQLFilters;
    private String inputs;
    private static final int INDENTSIZE = 3;
    private ArrayList<Query> entireQueries;
    private ArrayList<Query> complexQueries;
    private List<Document> filters = new ArrayList<>();


    public FilterEncoder() {
        identifySoloQuery();
    }


    /**
     * Identify every single query.
     * (Coming from the textarea)
     */
    private void identifySoloQuery() {
        if (inputs != null) {
            String[] queriesString = inputs.split("\\r\\n|\\n|\\r");
            queries = Arrays.asList(queriesString);
        }
    }

    /**
     * Generate the eCQL-Filter.
     * (based on the query list that has been establish)
     */
    private void doProduceEcqlFilters() throws CQLException {

        ArrayList<Query> queryList = new ArrayList<>();

        if (complexQueries != null && complexQueries.size() > 0) {
            for (Query query: complexQueries) {
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
                        //query.seteCQLFilter(filterString);
                        //queryList.add(query);
                    }
                } catch (IOException e) {
                    // log.log(log.getLevel(), e.getMessage(), e.getCause());
                    e.printStackTrace();
                } catch (TransformerException e) {
                    // TODO
                    e.printStackTrace();
                } catch (SAXException e) {
                    // TODO
                    e.printStackTrace();
                }
            }
            complexQueries = queryList;
        }
    }

    /**
     * Collect all the entire queries.
     * @return help - a map
     */
    private void collectEntireQueries() {

        List<String> newQueries = new ArrayList<>();
        entireQueries = new ArrayList<>();
        complexQueries = new ArrayList<>();

        for (String query: this.queries) {
            if (query != null) {
                if (query.contains("WHERE")
                        || query.contains("Where")
                        || query.contains("where")) {

                    String[] wheres = splitCQLRequest(query);

                    if (wheres.length != 0) {
                        Query aQuery = initializeQueryObject(wheres);
                        entireQueries.add(aQuery);
                        complexQueries.add(aQuery);
                        newQueries.add(wheres[1]);
                    }
                } else {
                    Query aQuery = new Query("", query, false);
                    complexQueries.add(aQuery);
                }
            }
        }

        if (newQueries.size() > 0) {
            queries = newQueries;
        }
    }

    /**
     * Initialize the query Object.
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

    /**
     * Give back the list of all produced eCQL-Filters.
     * @return eCQLFilters
     */
    public List<String> geteCQLFilters() {
        return eCQLFilters;
    }

    /**
     * Save all the eCQL-Filters.
     * @param eCQLFilters the list of all eCQL-Filter
     */
    public void seteCQLFilters(List<String> eCQLFilters) {
        this.eCQLFilters = eCQLFilters;
    }

    /**
     * Give back a list of every single lines introduce by the user.
     * @return queries This represents every single line \n
     */
    public List<String> getQueries() {
        return queries;
    }

    /**
     * We want to hold all the queries.
     * @param userInput extract all queries
     * @throws CQLException something went wrong.
     */
    public void initializeQueries(String userInput) throws CQLException {
        this.inputs = userInput;
        this.eCQLFilters = new ArrayList<>();
        identifySoloQuery();
        collectEntireQueries();
        doProduceEcqlFilters();
/*
        if (entireQueries != null && entireQueries.size() > 0) {
            queries = new ArrayList<>();
           for (Query query: entireQueries) {
               queries.add(query.getValue());
           }
        } */
    }

    /**
     * Form the entire queries.
     * @return entireQueries Map
     */
    public ArrayList<Query> getEntireQueries() {
        return entireQueries;
    }

    /**
     * Initialize the entire queries.
     * @param entireQueries a Map
     */
    public void setEntireQueries(ArrayList<Query> entireQueries) {
        this.entireQueries = entireQueries;
    }

    /**
     * Set the complexe queries objects.
     * @return complexQueries list
     */
    public ArrayList<Query> getComplexQueries() {
        return complexQueries;
    }

    /**
     * Initialize the entire queries.
     * @param complexQueries a List of Objects
     */
    public void setComplexQueries(ArrayList<Query> complexQueries) {
        this.complexQueries = complexQueries;
    }

    /**
     * @return one Document encoded as "Filter Encoding Specification"
     * per query line, an empty list if the input is not parsed yet,
     * never <code></code>
     */
    public List<Document> getFilters() {
        return filters;
    }

}
