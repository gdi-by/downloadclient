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

import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;

import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.StringUtils;

/** Stores meta data about WFS. */
public class WFSMeta {

    /** operation. */
    public static class Operation {
        /** name. */
        public String name;
        /** get. */
        public String get;
        /** output formats. */
        public ArrayList<String> outputFormats;
        /** constraints. */
        public List<Constraint> constraints;

        public Operation() {
            this.outputFormats = new ArrayList<>();
            this.constraints = new ArrayList<>();
        }

        private String outputFormats() {
            return "[" + StringUtils.join(outputFormats, ", ") + "]";
        }

        private String constraints() {
            return "[" + StringUtils.join(constraints, ", ") + "]";
        }

        @Override
        public String toString() {
            return "operation: { name: " + name + " "
                + " get: " + get + " "
                + " constraints: " +  constraints() + " "
                + " output formats: " + outputFormats() + " }";
        }

        /**
         * Figure out if this service implements paging.
         * @return number of features per page.
         *         null if paging is not supported.
         */
        public Integer featuresPerPage() {
            for (Constraint constraint: this.constraints) {
                if (constraint.name != null
                && constraint.value != null
                    && constraint.equals("CountDefault")) {
                    try {
                        return Integer.valueOf(constraint.value);
                    } catch (NumberFormatException nfe) {
                        // Ignore me.
                    }
                    return null;
                }
            }
            return null;
        }
    }


    /** feature. */
    public static class Feature {
        /** name. */
        public String name;
        /** title. */
        public String title;
        /** abstract. */
        public String abstractDescription;
        /** default CRS. */
        public String defaultCRS;
        /** other CRSs. */
        public List<String> otherCRSs;
        /** outputFormats. */
        public List<String> outputFormats;
        /** bbox. */
        public ReferencedEnvelope bbox;

        public Feature() {
            this.otherCRSs = new ArrayList<>();
            this.outputFormats = new ArrayList<>();
        }

        private String otherCRSs() {
            return "[" + StringUtils.join(otherCRSs, ", ") + "]";
        }

        private String outputFormats() {
            return "[" + StringUtils.join(outputFormats, ", ") + "]";
        }

        @Override
        public String toString() {
            return "feature: { "
                + "name: " + name + " "
                + "title: " + title + " "
                + "abstract: " + abstractDescription + " "
                + "defaultCRS: " + defaultCRS + " "
                + "otherCRSs: " +  otherCRSs() + " "
                + "outputFormats: " +  outputFormats() + " "
                + "bbox: " + bbox + " }";
        }
    }

    /** stored Query. */
    public static class StoredQuery {
        /** id. */
        public String id;
        /** title. */
        public String title;
        /** abstract. */
        public String abstractDescription;
        /** parameters. */
        public ArrayList<Field> parameters;

        public StoredQuery() {
            parameters = new ArrayList<>();
        }

        private String parameters() {
            return "[" + StringUtils.join(parameters, ", ") + "]";
        }

        @Override
        public String toString() {
            return "stored query: { "
                + "id: " + id + " "
                + "title: " + title + " "
                + "abstract: " + abstractDescription + " "
                + "parameters: " + parameters() + " }";
        }
    }

    /** Constraint. */
    public static class Constraint {
        /** name. */
        public String name;
        /** value. */
        public String value;

        public Constraint() {
        }

        @Override
        public String toString() {
            return "[ name: " + name + ", value: " + value + "]";
        }
    }

    /** title. */
    public String title;
    /** URL. */
    public String url;
    /** abstract. */
    public String abstractDescription;
    /** operations. */
    public List<Operation> operations;
    /** constraints. */
    public List<Constraint> constraints;
    /** features. */
    public List<Feature> features;
    /** stored queries. */
    public List<StoredQuery> storedQueries;
    /** versions. */
    public List<String> versions;
    /** namespaces. */
    public NamespaceContextMap namespaces;

    public WFSMeta() {
        operations = new ArrayList<>();
        constraints = new ArrayList<>();
        features = new ArrayList<>();
        storedQueries = new ArrayList<>();
        versions = new ArrayList<>();
        namespaces = new NamespaceContextMap();
    }

    /**
     * Is operation supported?
     * @param name The name of the operation.
     * @return true if operation is supported false otherwise.
     */
    public boolean isOperationSupported(String name) {
        return findOperation(name) != null;
    }

    /**
     * Find an operation by name.
     * @param name The name of the operation.
     * @return The operation if found null otherwise.
     */
    public Operation findOperation(String name) {
        for (Operation op: this.operations) {
            if (op.name.equals(name)) {
                return op;
            }
        }
        return null;
    }

    /**
     * Return the highest supported version.
     * @param def The default. Used if there are no version informations.
     * @return The highest version.
     */
    public String highestVersion(String def) {
        return versions.size() > 0
            ? versions.get(versions.size() - 1)
            : def;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("WFSMeta {\n");
        sb.append("\ttitle: ").append(this.title).append("\n");
        sb.append("\tabstract: ").append(this.abstractDescription)
          .append("\n");
        sb.append("\toperations: {\n");
        for (Operation op: operations) {
            sb.append("\t\t").append(op).append("\n");
        }
        sb.append("\t}\n");
        sb.append("\tconstraints: {\n");
        for (Constraint c: constraints) {
            sb.append("\t\t").append(c).append("\n");
        }
        sb.append("\t}\n");
        sb.append("\tfeatures: {\n");
        for (Feature f: features) {
            sb.append("\t\t").append(f).append("\n");
        }
        sb.append("\t}\n");
        sb.append("\tstored queries: {\n");
        for (StoredQuery sq: storedQueries) {
            sb.append("\t\t").append(sq).append("\n");
        }
        sb.append("\t}\n");
        sb.append("\tversions: {\n");
        for (String version: versions) {
            sb.append("\t\t").append(version).append("\n");
        }
        sb.append("\t}\n");
        sb.append("}");
        return sb.toString();
    }
}
