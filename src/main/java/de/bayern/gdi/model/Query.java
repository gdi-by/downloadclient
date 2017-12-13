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


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Represent an CQL-Request.
 */
@XmlRootElement(name = "Query")
@XmlAccessorType(XmlAccessType.FIELD)
public class Query {

    @XmlElement(name = "FeatureTypeName")
    private String key;

    @XmlElement(name = "Wert")
    private String value;

    @XmlElement(name = "Filter")
    private String eCQLFilter;

    @XmlElement(name = "ComplexNameSpace")
    private String complexNameSpace;

    @XmlElement(name = "isECQLEntireRequest")
    private boolean isECQLEntireRequest;

    @XmlElement(name = "prefix")
    private String prefix;


    public Query() {
    }

    public Query(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Query(String key, String value, boolean isECQLEntireRequest) {
        this.key = key;
        this.value = value;
        this.isECQLEntireRequest = isECQLEntireRequest;
    }

    public Query(String key, String value, String eCQLFilter) {
        this.key = key;
        this.value = value;
        this.eCQLFilter = eCQLFilter;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
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

    /**
     * @return the eCQL-Filter
     */
    public String geteCQLFilter() {
        return eCQLFilter;
    }

    /**
     * @param eCQLFilter  the filters to set
     */
    public void seteCQLFilter(String eCQLFilter) {
        this.eCQLFilter = eCQLFilter;
    }

    /**
     *  Give back the Name space.
     * @return complexNameSpace - The nameSpace
     */
    public String getComplexNameSpace() {
        return complexNameSpace;
    }

    /**
     * Initialise the namespace.
     * @param complexNameSpace  the Name Space
     */
    public void setComplexNameSpace(String complexNameSpace) {
        this.complexNameSpace = complexNameSpace;
    }


    /**
     * Inform about the eCQL-Type.
     * @return isECQLEntireRequest - true or False
     */
    public boolean isECQLEntireRequest() {
        return isECQLEntireRequest;
    }

    /**
     * Initialise the eCQL.
     * @param entireRequest Complete eCQL
     */
    public void setECQLEntireRequest(boolean entireRequest) {
        this.isECQLEntireRequest = entireRequest;
    }

    /**
     * get the feature type prefix.
     * @return prefix - for example bvv
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * set the feature type prefix.
     * @param  prefix - for example bvv
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return "[Query: key = \"" + key + "\" value = \"" + value + "\"]";
    }
}
