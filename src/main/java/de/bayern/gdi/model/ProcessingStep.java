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
package de.bayern.gdi.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * One step in a process sequence.
 */
@XmlRootElement(name = "Verarbeitungsschritt")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessingStep {

    @XmlElement(name = "Name")
    private String name;

    @XmlElementWrapper(name = "Parameters")
    @XmlElement(name = "Parameter")
    private ArrayList<Parameter> parameters;

    public ProcessingStep() {
    }

    /**
     * @return the parameters
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the parameters
     */
    public ArrayList<Parameter> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(ArrayList<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Looks up a parameter value for a given key.
     * @param key The key.
     * @return The value if found null otherwise.
     */
    public String findParameter(String key) {
        for (Parameter parameter: parameters) {
            String pkey = parameter.getKey();
            if (pkey != null && pkey.equals(key)) {
                return parameter.getValue();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[processing step: ");
        sb.append("name = \"").append(name).append("\" ");
        sb.append(" parameters: [");
        for (int i = 0, n = parameters.size(); i < n; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameters.get(i));
        }
        sb.append(']');
        return sb.toString();
    }
}

