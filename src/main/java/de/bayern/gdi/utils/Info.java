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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public class Info {

    private static Info instance;

    private String version;
    private String name;

    private static final String MAVEN_PACKAGE = "de.bayern.gdi";
    private static final String MAVEN_ARTIFACT = "downloadclient";
    private static final String COMMENT = "Geodateninfrastruktur Bayern";
    private static final String UNKNOWN = "unknown";

    private static final Logger log
        = LoggerFactory.getLogger(Info.class.getName());

    private Info() {
        try {
            version = pullVersion();
            name = pullName();
        } catch (URISyntaxException
            | IOException e) {
            log.error("Reading of Version or Name failed: " + e.getMessage(),
                e);
            version = UNKNOWN;
            name = UNKNOWN;
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
        return instance.version;
    }

    /**
     * gets the Name.
     *
     * @return name
     */
    public static String getName() {
        if (instance == null) {
            instance = new Info();
        }
        return instance.name;
    }

    /**
     * gets the Comment.
     *
     * @return comment
     */
    public static String getComment() {
        return COMMENT;
    }

    private synchronized String pullName() throws URISyntaxException,
        IOException {
        String n = getNameFromManifestFile();
        if (!n.isEmpty()) {
            return n.trim();
        }

        n = parseXPathInPom("/project/name");
        if (!n.isEmpty()) {
            return n.trim();
        }

        n = getTagFromProperties("name");
        return n.isEmpty() ? UNKNOWN : n;
    }


    private synchronized String pullVersion() throws URISyntaxException,
        IOException {
        String v = getVersionFromManifestFile();
        if (!v.isEmpty()) {
            return v.trim();
        }

        v = parseXPathInPom("/project/version");
        if (!v.isEmpty()) {
            return v.trim();
        }

        v = getTagFromProperties("version");
        return v.isEmpty() ? UNKNOWN : v.trim();
    }

    private String getNameFromManifestFile() {
        Package pkg = App.class.getPackage();
        if (pkg != null) {
            String n = pkg.getImplementationTitle();
            if (n != null) {
                return n;
            }
            n = pkg.getSpecificationTitle();
            if (n != null) {
                return n;
            }
        }
        return "";
    }

    private String getVersionFromManifestFile() {
        Package pkg = App.class.getPackage();
        if (pkg != null) {
            String v = pkg.getImplementationVersion();
            if (v != null) {
                return v;
            }
            v = pkg.getSpecificationVersion();
            if (v != null) {
                return v;
            }
        }
        return "";
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
            log.info("Not in Filesystem-Mode: "
                + e.getMessage(), e);
        }
        return null;
    }

    private String parseXPathInPom(String xPath) throws URISyntaxException,
        IOException {
        Path pom = getPomPath();
        if (pom == null) {
            return "";
        }
        try (InputStream is = Files.newInputStream(pom)) {
            Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(is);
            doc.getDocumentElement().normalize();
            String v = (String) XPathFactory.newInstance()
                .newXPath().compile(xPath)
                .evaluate(doc, XPathConstants.STRING);

            return v != null ? v.trim() : "";
        } catch (ParserConfigurationException
            | XPathExpressionException
            | SAXException e) {
            log.error("Parsing of " + xPath + " in Pom failed: "
                + e.getMessage(), e);
            return "";
        }
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
                log.error("Parsing of " + tag + " in Properties failed: "
                    + e.getMessage(), e);
            }
        }
        return "";
    }
}
