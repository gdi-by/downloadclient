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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/** 'Parameter' of processing step configuration. */
@XmlRootElement(name = "Parameter")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigurationParameter {

    @XmlAttribute(name = "verpflichtend")
    private boolean mandatory;

    @XmlAttribute(name = "eingabeelement")
    private String inputElement;

    @XmlAttribute(name = "glob")
    private String glob;

    @XmlAttribute(name = "ext")
    private String ext;

    @XmlValue
    private String value;

    public ConfigurationParameter() {
    }

    /**
     * @return the mandatory
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * @param mandatory the mandatory to set
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * @return the inputElement
     */
    public String getInputElement() {
        return inputElement;
    }

    /**
     * @param inputElement the inputElement to set
     */
    public void setInputElement(String inputElement) {
        this.inputElement = inputElement;
    }

    /**
     * @return the glob
     */
    public String getGlob() {
        return glob;
    }

    /**
     * @param glob the glob to set
     */
    public void setGlob(String glob) {
        this.glob = glob;
    }

    /**
     * @return the ext
     */
    public String getExt() {
        return ext;
    }

    /**
     * @param ext the ext to set
     */
    public void setExt(String ext) {
        this.ext = ext;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /** The defining pattern of a variable.*/
    public static final Pattern VARS_RE = Pattern.compile("\\{([^\\}]+)\\}");

    /**
     * Extract the first variable out of a string.
     * @param s The string to be searched.
     * @return The name of the variable if found null otherwise.
     */
    public static String extractVariable(String s) {
        Matcher matcher = VARS_RE.matcher(s);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Extracts the variables from the value.
     * @return The list of variables in the value.
     */
    public List<String> extractVariables() {
        if (this.value == null) {
            return Collections.<String>emptyList();
        }
        Matcher matcher = VARS_RE.matcher(this.value);
        ArrayList<String> vars = new ArrayList<>();
        while (matcher.find()) {
            vars.add(matcher.group(1));
        }
        return vars;
    }

    /**
     * Resolves the variable of the value with a map.
     * @param vars The variables.
     * @return The resolved value.
     */
    public String replaceVars(Map<String, String> vars) {
        if (this.value == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        Matcher matcher = VARS_RE.matcher(this.value);
        while (matcher.find()) {
            String name = matcher.group(1);
            String val = vars.get(name);
            if (value != null) {
                matcher.appendReplacement(sb, val);
            } else {
                matcher.appendReplacement(sb, name);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
