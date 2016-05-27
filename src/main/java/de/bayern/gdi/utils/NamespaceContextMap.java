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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public final class NamespaceContextMap implements
        NamespaceContext {

    private final Map<String, String> prefixMap;
    private final Map<String, Set<String>> nsMap;

    /**
     * Constructor.
     * @param prefixMappings
     *          a map of prefix:namespaceURI values
     */
    public NamespaceContextMap(
            Map<String, String> prefixMappings) {
        prefixMap = createPrefixMap(prefixMappings);
        nsMap = createNamespaceMap(prefixMap);
    }

    /**
     * Constructor.
     * @param mappingPairs
     *          pairs of prefix-namespaceURI values
     */
    public NamespaceContextMap(String... mappingPairs) {
        this(toMap(mappingPairs));
    }

    private static Map<String, String> toMap(
            String... mappingPairs) {
        Map<String, String> prefixMappings = new HashMap<String, String>(
                mappingPairs.length / 2);
        for (int i = 0; i < mappingPairs.length; i++) {
            prefixMappings
                    .put(mappingPairs[i], mappingPairs[++i]);
        }
        return prefixMappings;
    }

    private Map<String, String> createPrefixMap(
            Map<String, String> prefixMappings) {
        Map<String, String> prefMap = new HashMap<String, String>(
                prefixMappings);
        addConstant(prefMap, XMLConstants.XML_NS_PREFIX,
                XMLConstants.XML_NS_URI);
        addConstant(prefMap, XMLConstants.XMLNS_ATTRIBUTE,
                XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        return Collections.unmodifiableMap(prefMap);
    }

    private void addConstant(Map<String, String> prefMap,
                             String prefix, String nsURI) {
        String previous = prefMap.put(prefix, nsURI);
        if (previous != null && !previous.equals(nsURI)) {
            throw new IllegalArgumentException(prefix + " -> "
                    + previous + "; see NamespaceContext contract");
        }
    }

    private Map<String, Set<String>> createNamespaceMap(
            Map<String, String> prefMap) {
        Map<String, Set<String>> namespMap = new HashMap<String, Set<String>>();
        for (Map.Entry<String, String> entry : prefMap
                .entrySet()) {
            String nsURI = entry.getValue();
            Set<String> prefixes = namespMap.get(nsURI);
            if (prefixes == null) {
                prefixes = new HashSet<String>();
                namespMap.put(nsURI, prefixes);
            }
            prefixes.add(entry.getKey());
        }
        for (Map.Entry<String, Set<String>> entry : namespMap
                .entrySet()) {
            Set<String> readOnly = Collections
                    .unmodifiableSet(entry.getValue());
            entry.setValue(readOnly);
        }
        return namespMap;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        checkNotNull(prefix);
        String nsURI = prefixMap.get(prefix);
        return nsURI == null ? XMLConstants.NULL_NS_URI : nsURI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        checkNotNull(namespaceURI);
        Set<String> set = nsMap.get(namespaceURI);
        return set == null ? null : set.iterator().next();
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        checkNotNull(namespaceURI);
        Set<String> set = nsMap.get(namespaceURI);
        return set.iterator();
    }

    private void checkNotNull(String value) {
        if (value == null) {
            throw new IllegalArgumentException("null");
        }
    }

    /**
     * Map of the mappings.
     * @return an unmodifiable map of the mappings.
     */
    public Map<String, String> getMap() {
        return prefixMap;
    }
}
