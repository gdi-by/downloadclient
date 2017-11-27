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

package de.bayern.gdi.utils;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import javax.xml.xpath.XPathVariableResolver;

/**
 *  A variable resolver for XPath expressions.
 */
public class MapXPathVariableResolver implements XPathVariableResolver {

    private Map<String, String> variables;

    public MapXPathVariableResolver() {
        this.variables = new HashMap<>();
    }


    public MapXPathVariableResolver(Map<String, String> variables) {
        this.variables = variables;
    }

    /**
     * addVariable adds none null named variable to the map of variables.
     * @param name The name of the variable.
     * @param value The value of the variable.
     */
    public void addVariable(String name, String value) {
        if (name != null && value != null) {
            variables.put(name, value);
        }
    }

    @Override
    public Object resolveVariable(QName variableName) {
        String key = variableName.getLocalPart();
        return variables.get(key);
    }
}
