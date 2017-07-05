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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.geometry.jts.ReferencedEnvelope;

import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.StringUtils;

/** Stores meta data about WFS. */
public class WFSMeta {

    private static final Logger log
        = Logger.getLogger(WFSMeta.class.getName());

    /** operation. */
    public static class Operation {
        /** name. */
        public String name;
        /** get. */
        public String get;
        /** post. */
        public String post;
        /** output formats. */
        public List<String> outputFormats;
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
            String postString = (post != null)
                    ? " post: " + post + " " : "";
            return "operation: { name: " + name + " "
                + " get: " + get + " "
                + postString
                + " constraints: " + constraints() + " "
                + " output formats: " + outputFormats() + " }";
        }

        /**
         * Figure out if this service implements paging.
         * @return number of features per page.
         *         null if paging is not supported.
         */
        public Integer featuresPerPage() {
            return WFSMeta.featuresPerPage(this.constraints);
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
        /** allowed. */
        public List<String> allowed;

        public Constraint() {
            allowed = new ArrayList<>();
        }

        @Override
        public String toString() {
            return "[ name: " + name
                + ", value: " + value
                + ", allowed: [" + StringUtils.join(allowed, ", ")
                + "]]";
        }
    }

    /** Version. */
    public static class Version implements Comparable<Version> {
        /** version. */
        public String version;
        /** parsed. */
        public int[] parsed;

        /** Version. */
        public Version() {
        }

        /** @param version The version. */
        public Version(String version) {
            this.version = version;
            parse();
        }

        /** Parses the version string to numbers. */
        public void parse() {
            try {
                String[] parts = version.split("\\.");
                int[] converted = new int[parts.length];
                for (int i = 0; i < converted.length; i++) {
                    converted[i] = Integer.parseInt(parts[i]);
                }
                this.parsed = converted;
            } catch (NumberFormatException nfe) {
                log.log(Level.SEVERE, nfe.getMessage(), nfe);
            }
        }

        @Override
        public int compareTo(Version other) {
            if (other.parsed == null && this.parsed == null) {
                return this.version.compareTo(other.version);
            }
            if (this.parsed == null) {
                return +1;
            }
            if (other.parsed == null) {
                return -1;
            }
            int n = Math.min(this.parsed.length, other.parsed.length);
            for (int i = 0; i < n; i++) {
                int diff = this.parsed[i] - other.parsed[i];
                if (diff != 0) {
                    return diff;
                }
            }
            return 0;
        }

        @Override
        public String toString() {
            return this.version;
        }
    }

    /** WFS 2.0.0. */
    public static final Version WFS2_0_0 = new Version("2.0.0");

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
    /** output formats. */
    public List<String> outputFormats;
    /** versions. */
    public List<Version> versions;
    /** namespaces. */
    public NamespaceContextMap namespaces;

    public WFSMeta() {
        operations = new ArrayList<>();
        constraints = new ArrayList<>();
        features = new ArrayList<>();
        storedQueries = new ArrayList<>();
        versions = new ArrayList<>();
        outputFormats = new ArrayList<>();
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
    public Version highestVersion(Version def) {
        return versions.size() > 0
            ? versions.get(versions.size() - 1)
            : def;
    }

    /**
     * Tells if this service is a SimpleWFS.
     * @return true if it is simple otherwise false.
     */
    public boolean isSimple() {
        for (Constraint c: constraints) {
            if (c.name.equals("QueryExpressions")) {
                boolean query = false;
                boolean storedQuery = false;
                for (String value: c.allowed) {
                    if (value.equals("wfs:Query")) {
                        query = true;
                    }
                    if (value.equals("wfs:StoredQuery")) {
                        storedQuery = true;
                    }
                }
                return !query && storedQuery;
            }
        }
        return false;
    }

    /**
     * Tells if this service is a BasicWFS.
     * @return true if it is basic otherwise false.
     */
    public boolean isBasic() {
        for (Constraint c: constraints) {
            if (c.name.equals("QueryExpressions")) {
                boolean query = false;
                boolean storedQuery = false;
                for (String value: c.allowed) {
                    if (value.equals("wfs:Query")) {
                        query = true;
                    }
                    if (value.equals("wfs:StoredQuery")) {
                        storedQuery = true;
                    }
                }
                return query && storedQuery;
            }
        }
        return false;
    }

    private static Integer featuresPerPage(List<Constraint> constraints) {
        for (Constraint constraint: constraints) {
            if (constraint.name != null
            && constraint.value != null
            && constraint.name.equals("CountDefault")) {
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

    /**
     * Figure out if this service implements paging.
     * @return number of features per page.
     *         null if paging is not supported.
     */
    public Integer featuresPerPage() {
        return featuresPerPage(this.constraints);
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
        for (Version version: versions) {
            sb.append("\t\t").append(version).append("\n");
        }
        sb.append("\t}\n");
        sb.append("\toutput formats: {\n");
        for (String outputFormat: outputFormats) {
            sb.append("\t\t").append(outputFormat).append("\n");
        }
        sb.append("\t}\n");
        sb.append("\tconstraints: {\n");
        for (Constraint constraint: constraints) {
            sb.append("\t\t").append(constraint).append("\n");
        }
        sb.append("\t}\n");
        sb.append("}");
        return sb.toString();
    }
}
