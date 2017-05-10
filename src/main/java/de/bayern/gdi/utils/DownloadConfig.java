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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
/**
 * @author Alexander Wöstmann (awoestmann@intevation.de)
 */
public class DownloadConfig {

    private Document configDoc;
    private File configFile;
    private String dataset;
    private String downloadPath;
    private String serviceType;
    private String serviceURL;
    private HashMap<String, String> parameters = null;
    private ArrayList<ProcessingStep> procSteps = null;

   /**
    * Exception, thrown if no service URL is found in the config file.
    */
    public class NoServiceURLException extends Exception { };

   /**
    * Default Constructor.
    */
    public DownloadConfig() { };

    /**
    * Constructor.
    *
    * @param path The config xml file.
    */
    public DownloadConfig(File configFile)
            throws IOException, ParserConfigurationException,
            SAXException, NoServiceURLException {

        if (configFile == null) {
            return;
        }

        this.configFile = configFile;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        configDoc = db.parse(configFile);

        //Read service type, URL and Dataset
        Element root = configDoc.getDocumentElement();

        serviceType = getValueByTagName("ServiceTyp", root);
        try {
            serviceURL = getValueByTagName("URL", root);
        } catch (Exception e) {
            throw new NoServiceURLException();
        }
        try {
            dataset = getValueByTagName("Dataset", root);
        } catch (Exception ex) {
            dataset = null;
        }
        try {
            downloadPath = getValueByTagName("DownloadPfad", root);
        } catch (Exception exc) {
            downloadPath = null;
        }

        //Read parameters
        parameters = new HashMap<String, String>();
        NodeList paramNodes = configDoc.getElementsByTagName("Parameter");

        for (int i = 0; i < paramNodes.getLength(); i++) {
            Node iNode = paramNodes.item(i);
            if (iNode.getParentNode().getParentNode()
                    .getNodeName() != "Verarbeitungsschritt") {
                parameters.put(
                    getValueByTagName("Name", (Element) iNode),
                    getValueByTagName("Wert", (Element) iNode));
            }
        }

        //Read Processing steps
        NodeList stepNodes = configDoc.
                getElementsByTagName("Verarbeitungsschritt");

        if (stepNodes.getLength() == 0) {
            procSteps = null;
        } else {
            procSteps = new ArrayList<ProcessingStep>();
            //Verarbeitungsschritt-Nodes
            for (int i = 0; i < stepNodes.getLength(); i++) {
                ProcessingStep newStep = new ProcessingStep();
                newStep.name = getValueByTagName("Name",
                        (Element) stepNodes.item(i));
                Element step = (Element) stepNodes.item(i);
                NodeList stepParams = step.getElementsByTagName("Parameter");
                for (int j = 0; j < stepParams.getLength(); j++) {
                    newStep.params.put(
                        getValueByTagName("Name", (Element) stepParams.item(j)),
                        getValueByTagName("Wert", (Element) stepParams.item(j))
                    );
                }
                procSteps.add(newStep);
            }
        }
    }

   /**
    * Returns the atom variation type as string.
    *
    * @return The variation string
    */
    public String getAtomVariation() {
        return parameters.get("VARIATION");
    }

   /**
    * Returns the bounding box as string.
    *
    * @return The bound box
    */
    public String getBoundingBox() {
        return parameters.get("bbox");
    }

   /**
    * Returns the dataset as string.
    *
    * @return The dataset
    */
    public String getDataset() {
        return dataset;
    }

   /**
    * Returns the download path as string.
    *
    * @return The download path
    */
    public String getDownloadPath() {
        return downloadPath;
    }

   /**
    * Returns the config file object.
    *
    * @return The file object
    */
    public File getFile() {
        return configFile;
    }

   /**
    * Returns the output format as string.
    *
    * @return The output format
    */
    public String getOutputFormat() {
        return parameters.get("outputformat");
    }

   /**
    * Returns all parameters as a HashMap.
    *
    * @return The params
    */
    public HashMap<String, String> getParams() {
        return parameters;
    }

   /**
    * Returns all processing steps as an ArrayList.
    *
    * @return The processing step list
    */
    public ArrayList<ProcessingStep> getProcessingSteps() {
        return procSteps;
    }

   /**
    * Returns the service type as string.
    *
    * @return The service type as string
    */
    public String getServiceType() {
        return serviceType;
    }

   /**
    * Returns the service URL as string.
    *
    * @return The URL as string
    */
    public String getServiceURL() {
        return serviceURL;
    }

   /**
    * Returns the SRS name.
    *
    * @return The SRS name
    */
    public String getSRSName() {
        return parameters.get("srsName");
    }

   /**
    * Returns the value of a tag by the name of the Tag.
    *
    * @param tagName The name of the tag.
    * @param parent The parent element from which the query shall be started.
    * @return The value as string
    */
    private String getValueByTagName(String tagName, Element parent) {
        try {
            NodeList nodes = parent.getElementsByTagName(tagName).item(0)
                    .getChildNodes();
            Node valueNode = (Node) nodes.item(0);
            return valueNode.getNodeValue();
        } catch (Exception e) {
            return null;
        }
    }

    /**
      * Class, serving as a simple model for processing chain steps.
      */
    public class ProcessingStep {
       /**
        * The name of the processing step.
        */
        public String name;

       /**
        * Name and value of all given parameters for this step.
        */
        public HashMap<String, String> params;

        public ProcessingStep() {
            params = new HashMap<String, String>();
        }
    }
}
