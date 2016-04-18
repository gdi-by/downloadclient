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

import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Helper to handle XML documents.
 */
public class XML {

    private static final Logger log
        = Logger.getLogger(XML.class.getName());

    private XML() {
    }

    /**
     * Loads an XML document from a file.
     * @param fileName the name of the XML file.
     * @return the loaded XML document of null if there was an error.
     */
    public static Document getDocument(File fileName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(fileName);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return document;
    }

    /**
     * Loads an XML document from an input stream.
     * @param input the input stream.
     * @return the loaded XML document of null if there was an error.
     */
    public static Document getDocument(InputStream input) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(input);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return document;
    }
}
