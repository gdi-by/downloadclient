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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
     * Creates a context from the prefixes/namespaceURI of a given node.
     * @param node The node.
     */
    public NamespaceContextMap(Node node) {
        this.prefixMap = collectNS(node);
        this.nsMap = createNamespaceMap(this.prefixMap);
    }

    private static Map<String, String> collectNS(Node node) {

        Map<String, String> prefixMap = new HashMap<>();
        ArrayDeque<Node> stack = new ArrayDeque<>();

        stack.push(node);

        while (!stack.isEmpty()) {
            node = stack.pop();
            String prefix = node.getPrefix();
            String ns = node.getNamespaceURI();

            if (prefix != null && ns != null
            && !prefixMap.containsKey(prefix)) {
                prefixMap.put(prefix, ns);
            }

            NodeList children = node.getChildNodes();
            for (int i = 0, n = children.getLength(); i < n; i++) {
                stack.push(children.item(i));
            }
            NamedNodeMap attrs = node.getAttributes();
            if (attrs != null) {
                for (int i = 0, n = attrs.getLength(); i < n; i++) {
                    Attr attr = (Attr)attrs.item(i);
                    String nsa = attr.getNamespaceURI();
                    if (nsa != null
                    && nsa.equals("http://www.w3.org/2000/xmlns/")) {
                        String nsp = attr.getLocalName();
                        String nsv = attr.getValue();
                        if (!prefixMap.containsKey(nsp)) {
                            prefixMap.put(nsp, nsv);
                        }
                    }
                    stack.push(attr);
                }
            }
        }

        return prefixMap;
    }

    /** Joins the prefix/namespaces recursivly
     * from a given node into this context.
     * @param node The new document.
     */
    public void join(Node node) {
        Map<String, String> newPrefixMap = collectNS(node);
        for (Map.Entry<String, String> entry: newPrefixMap.entrySet()) {
            if (!this.prefixMap.containsKey(entry.getKey())) {
                this.prefixMap.put(entry.getKey(), entry.getValue());
            }
        }
        this.nsMap.clear();
        updateNamespaceMap(this.prefixMap, this.nsMap);
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
        Map<String, String> prefixMappings
            = new HashMap<>(mappingPairs.length / 2);
        for (int i = 0; i < mappingPairs.length; i++) {
            prefixMappings
                    .put(mappingPairs[i], mappingPairs[++i]);
        }
        return prefixMappings;
    }

    private Map<String, String> createPrefixMap(
            Map<String, String> prefixMappings) {
        Map<String, String> prefMap = new HashMap<>(prefixMappings);
        addConstant(prefMap, XMLConstants.XML_NS_PREFIX,
                XMLConstants.XML_NS_URI);
        addConstant(prefMap, XMLConstants.XMLNS_ATTRIBUTE,
                XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        return prefMap;
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
        Map<String, Set<String>> namespMap = new HashMap<>();
        return updateNamespaceMap(prefMap, namespMap);
    }

    private Map<String, Set<String>> updateNamespaceMap(
            Map<String, String> prefMap,
            Map<String, Set<String>> namespMap
    ) {
        for (Map.Entry<String, String> entry : prefMap
                .entrySet()) {
            String nsURI = entry.getValue();
            Set<String> prefixes = namespMap.get(nsURI);
            if (prefixes == null) {
                prefixes = new HashSet<>();
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
