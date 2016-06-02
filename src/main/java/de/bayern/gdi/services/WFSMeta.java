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

/** Stores meta data about WFS. */
public class WFSMeta {

    /** operation. */
    public static class Operation {
        /** name. */
        public String name;
        /** get. */
        public String get;

        @Override
        public String toString() {
            return "operation: { name: " + name + " }";
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

        public Feature() {
            otherCRSs = new ArrayList<>();
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

        @Override
        public String toString() {
            return "feature: { "
                + "name: " + name + " "
                + "title: " + title + " "
                + "abstract: " + abstractDescription + " "
                + "defaultCRS: " + defaultCRS + " "
                + "otherCRSs: " +  otherCRSs() + " "
                + "bbox: " + bbox + " }";
        }
    }

    /** title. */
    public String title;
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

    public WFSMeta() {
        operations = new ArrayList<>();
        supportedConstraints = new ArrayList<>();
        unsupportedConstraints = new ArrayList<>();
        features = new ArrayList<>();
    }

    /**
     * Is operation supported?
     * @param name The name of the operation.
     * @return true if operation is supported false otherwise.
     */
    public boolean isOperationSupported(String name) {
        for (Operation op: this.operations) {
            if (op.name.equals(name)) {
                return true;
            }
        }
        return false;
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
        sb.append("}");
        return sb.toString();
    }
}
