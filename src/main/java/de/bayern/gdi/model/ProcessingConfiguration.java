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
import java.util.ArrayList;
import java.util.List;

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
public class ProcessingConfiguration {

    /** Name of the config file. */
    public static final String PROCESSING_CONFIG_FILE =
        "verarbeitungsschritte.xml";

    @XmlElementWrapper(name = "Formate")
    @XmlElement(name = "Format")
    private List<ProcessingFormat> formats;

    @XmlElementWrapper(name = "Eingabeelemente")
    @XmlElement(name = "Eingabeelement")
    private List<InputElement> inputElements;

    @XmlElementWrapper(name = "Verarbeitungsschritte")
    @XmlElement(name = "Verarbeitungsschritt")
    private List<ProcessingStepConfiguration> processingSteps;

    public ProcessingConfiguration() {
        this.formats = new ArrayList<>();
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
     * @return the formats
     */
    public List<ProcessingFormat> getFormats() {
        return formats;
    }

    /**
     * @param formats the formats to set
     */
    public void setFormats(List<ProcessingFormat> formats) {
        this.formats = formats;
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
    public static ProcessingConfiguration read(InputStream fis)
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
}
