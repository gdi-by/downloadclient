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

        public Operation() {
            this.outputFormats = new ArrayList<>();
        }

        private String outputFormats() {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < outputFormats.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(outputFormats.get(i));
            }
            return sb.append("]").toString();
        }

        @Override
        public String toString() {
            return "operation: { name: " + name + " "
                + " get: " + get + " "
                + " output formats: " + outputFormats() + " }";
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
        /** bbox. */
        public ReferencedEnvelope bbox;
        /** fields. */
        public List<Field> fields;

        public Feature() {
            otherCRSs = new ArrayList<>();
            fields = new ArrayList<>();
        }

        private String otherCRSs() {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < otherCRSs.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(otherCRSs.get(i));
            }
            return sb.append("]").toString();
        }

        private String fields() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(fields.get(i));
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return "feature: { "
                + "name: " + name + " "
                + "title: " + title + " "
                + "abstract: " + abstractDescription + " "
                + "defaultCRS: " + defaultCRS + " "
                + "otherCRSs: " +  otherCRSs() + " "
                + "bbox: " + bbox + " "
                + "fields: " + fields() + " }";
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
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parameters.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(parameters.get(i));
            }
            return sb.toString();
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

    /** title. */
    public String title;
    /** URL. */
    public String url;
    /** abstract. */
    public String abstractDescription;
    /** operations. */
    public List<Operation> operations;
    /** supported constraints. */
    public List<String> supportedConstraints;
    /** unsupported constraints. */
    public List<String> unsupportedConstraints;
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
        supportedConstraints = new ArrayList<>();
        unsupportedConstraints = new ArrayList<>();
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
        sb.append("\tsupported constraints: {\n");
        for (String c: supportedConstraints) {
            sb.append("\t\t").append(c).append("\n");
        }
        sb.append("\t}\n");
        sb.append("\tunsupported constraints: {\n");
        for (String c: unsupportedConstraints) {
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
