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

    /** title. */
    public String title;
    /** abstract. */
    public String abstractDescription;
    /** operations. */
    public List<Operation> operations;

    public WFSMeta() {
        operations = new ArrayList<>();
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
        sb.append("}");
        return sb.toString();
    }
}
