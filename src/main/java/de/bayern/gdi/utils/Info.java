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

import de.bayern.gdi.App;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public class Info {

    private static Info instance;
    private static String version;
    private static String name;
    private static final String MAVEN_PACKAGE = "de.bayern.gdi";
    private static final String MAVEN_ARTIFACT = "downloadclient";
    private static final String COMMENT = "Geodateninfrastruktur Bayern";

    private static final Logger log
            = Logger.getLogger(Info.class.getName());

    private Info() {
        try {
            version = pullVersion();
            name = pullName();
        } catch (URISyntaxException
                | IOException e) {
            log.log(Level.SEVERE,
                    "Reading of Version or Name failed: " + e.getMessage(),
                    e);
            version = "unknown";
            name = "unknown";
        }
    }

    /**
     * gets the Version.
     *
     * @return version
     */
    public static String getVersion() {
        if (instance == null) {
            instance = new Info();
        }
        return version;
    }

    /**
     * gets the Name.
     * @return name
     */
    public static String getName() {
        if (instance == null) {
            instance = new Info();
        }
        return name;
    }

    /**
     * gets the Comment.
     * @return comment
     */
    public static String getComment() {
        return COMMENT;
    }

    private synchronized String pullName() throws URISyntaxException,
            IOException {
        String n = parseXPathInPom("/project/name");
        if (!n.isEmpty()) {
            return n;
        }

        n = getTagFromProperties("name");
        if (!n.isEmpty()) {
            return n;
        }

        Package pkg = App.class.getPackage();
        if (pkg != null) {
            n = pkg.getImplementationTitle();
            if (n == null) {
                n = pkg.getSpecificationTitle();
            }
        }
        n = n == null ? "" : n.trim();
        return n.isEmpty() ? "unknown" : n;
    }

    private synchronized String pullVersion() throws URISyntaxException,
            IOException {
        String v = parseXPathInPom("/project/version");
        if (!v.isEmpty()) {
            return v;
        }
        // Try to get version number from maven properties in jar's META-INF
        v = getTagFromProperties("version");
        if (!v.isEmpty()) {
            return v;
        }

        // Fallback to using Java API to get version from MANIFEST.MF
        v = null;
        Package pkg = App.class.getPackage();
        if (pkg != null) {
            v = pkg.getImplementationVersion();
            if (v == null) {
                v = pkg.getSpecificationVersion();
            }
        }
        v = v == null ? "" : v.trim();
        return v.isEmpty() ? "unknown" : v;
    }

    private Path getPomPath() throws URISyntaxException {
        try {
            String className = getClass().getName();
            String classfileName = "/" + className.replace('.', '/') + ".class";
            URL classfileResource = getClass().getResource(classfileName);
            if (classfileResource != null) {
                Path absolutePackagePath = Paths.get(classfileResource.toURI())
                        .getParent();
                int packagePathSegments = className.length()
                        - className.replace(".", "").length();
                Path path = absolutePackagePath;
                for (int i = 0, segmentsToRemove = packagePathSegments + 2;
                     i < segmentsToRemove; i++) {
                    path = path.getParent();
                }
                return path.resolve("pom.xml");
            }
        } catch (FileSystemNotFoundException e) {
            log.log(Level.INFO,
                    "Not in Filesystem-Mode: "
                            + e.getMessage(), e);
        }
        return null;
    }

    private String parseXPathInPom(String xPath) throws URISyntaxException,
    IOException {
        Path pom = getPomPath();
        if (pom != null) {
            InputStream is = Files.newInputStream(pom);
            try {
                Document doc = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder().parse(is);
                doc.getDocumentElement().normalize();
                String v = (String) XPathFactory.newInstance()
                        .newXPath().compile(xPath)
                        .evaluate(doc, XPathConstants.STRING);
                if (v != null) {
                    v = v.trim();
                    if (!v.isEmpty()) {
                        return v;
                    }
                }
            } catch (ParserConfigurationException
                    | XPathExpressionException
                    | SAXException e) {
                log.log(Level.SEVERE,
                        "Parsing of " + xPath + " in Pom failed: "
                                + e.getMessage(), e);
            }
        }
        return "";
    }

    private String getTagFromProperties(String tag) {
        String value;
        InputStream is = getClass()
                .getResourceAsStream("/META-INF/maven/" + MAVEN_PACKAGE + "/"
                        + MAVEN_ARTIFACT + "/pom.properties");
        if (is != null) {
            Properties p = new Properties();
            try {
                p.load(is);
                value = p.getProperty(tag, "").trim();
                if (!value.isEmpty()) {
                    return value;
                }
            } catch (IOException e) {
                log.log(Level.SEVERE,
                        "Parsing of " + tag + " in Properties failed: "
                                + e.getMessage(), e);
            }
        }
        return "";
    }
}
