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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/** 'Verarbeitungskonfiguration' of processing step configuration. */
@XmlRootElement(name = "Verarbeitungskonfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessingConfiguration implements Configuration {

    private static final Logger log
        = Logger.getLogger(ProcessingStepConfiguration.class.getName());

    /** Name of the config file. */
    public static final String PROCESSING_CONFIG_FILE =
        "verarbeitungsschritte.xml";

    @XmlElementWrapper(name = "Eingabeelemente")
    @XmlElement(name = "Eingabeelement")
    private List<InputElement> inputElements;

    @XmlElementWrapper(name = "Verarbeitungsschritte")
    @XmlElement(name = "Verarbeitungsschritt")
    private List<ProcessingStepConfiguration> processingSteps;

    private static File sourceFile;

    private static final String NAME =
            "ProcessingConfig";

    public ProcessingConfiguration() {
        this.inputElements = new ArrayList<>();
        this.processingSteps = new ArrayList<>();
    }

    /**
     * Look for a step configuation with a given name.
     * @param name The name of the config to find.
     * @return the matching step configuration if found null otherwise.
     */
    public ProcessingStepConfiguration findProcessingStepConfiguration(
        String name
    ) {
        for (ProcessingStepConfiguration psc: this.processingSteps) {
            String pname = psc.getName();
            if (pname != null && pname.equals(name)) {
                return psc;
            }
        }
        return null;
    }

    /**
     * Generate a on-the-fly list of ProcessingConfigurations
     * matching given type.
     * @param type The type to match,
     * @return a list of ProcessingConfigurations matching.
     */
    public List<ProcessingStepConfiguration> filterStepsByType(String type) {
        ArrayList<ProcessingStepConfiguration> steps = new ArrayList<>();
        for (ProcessingStepConfiguration psc: this.processingSteps) {
            String t = psc.getFormatType();
            if (t != null) {
                for (String u: type.split(",")) {
                    if (u.trim().equals(type)) {
                        steps.add(psc);
                    }
                }

            }
        }
        return steps;
    }

    /**
     * @return the inputElements
     */
    public List<InputElement> getInputElements() {
        return inputElements;
    }

    /**
     * @param inputElements the inputElements to set
     */
    public void setInputElements(List<InputElement> inputElements) {
        this.inputElements = inputElements;
    }

    /**
     * @return the processingSteps
     */
    public List<ProcessingStepConfiguration> getProcessingSteps() {
        return processingSteps;
    }

    /**
     * @param processingSteps the processingSteps to set
     */
    public void setProcessingSteps(
        List<ProcessingStepConfiguration> processingSteps) {
        this.processingSteps = processingSteps;
    }

    /**
     * Loads ProcessingConfiguration from a file.
     * @param file The file to load the ProcessingConfiguration from.
     * @return The restored ProcessingConfiguration.
     * @throws IOException Something went wrong.
     */
     public static ProcessingConfiguration read(File file) throws IOException {
        sourceFile = file;
        try (FileInputStream fis = new FileInputStream(file)) {
            return read(fis);
        }
    }

    /**
     * Loads ProcessingConfiguration from an input stream.
     * @param fis The input stram to load the ProcessingConfiguration from.
     * @return The restored ProcessingConfiguration.
     * @throws IOException Something went wrong.
     */
    private static ProcessingConfiguration read(InputStream fis)
        throws IOException {
        try {
            JAXBContext context =
                JAXBContext.newInstance(ProcessingConfiguration.class);
            Unmarshaller um = context.createUnmarshaller();
            BufferedInputStream bis = new BufferedInputStream(fis);
            return (ProcessingConfiguration)um.unmarshal(bis);
        } catch (JAXBException je) {
            throw new IOException("", je);
        }
    }

    /**
     * Load configuration from ressources.
     * @return The default configuation.
     */
    public static ProcessingConfiguration loadDefault() {
        InputStream in = null;
        try {
            in = ProcessingConfiguration.class.getResourceAsStream(
                PROCESSING_CONFIG_FILE);
            sourceFile = new File(ProcessingConfiguration.class.
                    getResource(
                    PROCESSING_CONFIG_FILE).toURI()
            );
            if (in == null) {
                log.log(Level.SEVERE,
                    PROCESSING_CONFIG_FILE + " not found");
                return new ProcessingConfiguration();
            }
            return read(in);
        } catch (URISyntaxException | IOException ioe) {
            log.log(Level.SEVERE, "Failed to load configuration", ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }
        return new ProcessingConfiguration();
    }

    /**
     * gets the file of the source.
     * @return source file
     */
    public File getSourceFile() {
        return sourceFile;
    }

    /**
     * gets the name of the configuration.
     * @return name of the config
     */
    public static String getName() {
        return NAME;
    }
}
