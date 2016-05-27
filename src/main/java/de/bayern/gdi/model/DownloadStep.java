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

import de.bayern.gdi.services.WebService;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
@XmlRootElement(name = "DownloadSchritt")
@XmlAccessorType(XmlAccessType.FIELD)
public class DownloadStep {

    private static final Logger log
        = Logger.getLogger(DownloadStep.class.getName());

    @XmlElement(name = "ServiceTyp")
    private String serviceType;

    @XmlElement(name = "URL")
    private String serviceURL;

    @XmlElement(name = "DownloadPfad")
    private String path;

    @XmlElement(name = "Dataset")
    private String dataset;

    @XmlElementWrapper(name = "Verarbeitungskette")
    @XmlElement(name = "Verarbeitungsschritt")
    private ArrayList<ProcessingStep> processingSteps;

    @XmlElementWrapper(name = "Parameters")
    @XmlElement(name = "Parameter")
    private ArrayList<Parameter> parameters;

    public DownloadStep() {
    }

    public DownloadStep(String dataset,
                        ArrayList<Parameter> parameters,
                        String serviceType,
                        String serviceURL,
                        String path,
                        ArrayList<ProcessingStep> processingSteps) {
        this.dataset = dataset;
        this.parameters = parameters;
        this.serviceType = serviceType;
        this.serviceURL = serviceURL;
        this.path = path;
        this.processingSteps = processingSteps;
    }
    public DownloadStep(String dataset,
                        ArrayList<Parameter> parameters,
                        WebService.Type serviceType,
                        String serviceURL,
                        String path,
                        ArrayList<ProcessingStep> processingSteps) {
        this(dataset,
                parameters,
                serviceType.toString(),
                serviceURL,path,
                processingSteps);
    }

    public DownloadStep(String dataset,
                        ArrayList<Parameter> parameters,
                        WebService.Type serviceType,
                        String serviceURL,
                        String path) {
        this(dataset,
                parameters,
                serviceType.toString(),
                serviceURL,
                path,
                new ArrayList<ProcessingStep>());
    }

    public DownloadStep(String dataset,
                        ArrayList<Parameter> parameters,
                        String serviceType,
                        String serviceURL,
                        String path) {
        this(dataset,
                parameters,
                serviceType,
                serviceURL,
                path,
                new ArrayList<ProcessingStep>());
    }

    /**
     * @param file The file to load data from.
     */
    public DownloadStep(File file) {
    }

    /**
     * @return the dataset
     */
    public String getDataset() {
        return dataset;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public ArrayList<ProcessingStep> getProcessingSteps() {
        return this.processingSteps;
    }

    public void setProcessingSteps(
        ArrayList<ProcessingStep> processingSteps) {
        this.processingSteps = processingSteps;
    }

    /**
     * @return the parameters
     */
    public ArrayList<Parameter> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(ArrayList<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the serviceType
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * @param serviceType the serviceType to set
     */
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    /**
     * @param serviceType the serviceType to set
     */
    public void setServiceType(WebService.Type serviceType) {
        this.setServiceType(serviceType.toString());
    }

    /**
     * @return the serviceURL
     */
    public String getServiceURL() {
        return serviceURL;
    }

    /**
     * @param serviceURL the serviceURL to set
     */
    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    /**
     * @return the dataSet
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Finds a value for a given parameter key.
     * @param key The key.
     * @return The value if found else null.
     */
    public String findParameter(String key) {
        for (Parameter p: this.parameters) {
            if (p.getKey().equals(key)) {
                return p.getValue();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[DownloadStep:\n");
        sb.append("\tserviceType: \"").append(serviceType).append("\"\n");
        sb.append("\tserviceURL: \"").append(serviceURL).append("\"\n");
        sb.append("\tdataset: \"").append(dataset).append("\"\n");
        sb.append("\tpath: \"").append(path).append("\"\n");
        sb.append("\tparameters: ");
        for (int i = 0,
            n = parameters != null ? parameters.size() : 0; i < n; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameters.get(i));
        }
        sb.append("]\n");
        sb.append("\tprocessing steps:\n");
        for (int i = 0,
            n = processingSteps != null ? processingSteps.size() : 0;
            i < n; i++) {
            sb.append("\t\t");
            sb.append(processingSteps.get(i));
            sb.append('\n');
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Save the Object.
     * @param file File to save to
     * @throws IOException if space does not exist
     */
    public void write(File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            JAXBContext context = JAXBContext.newInstance(DownloadStep.class);
            Marshaller m = context.createMarshaller();
            // m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            m.marshal(this, bos);
            bos.flush();
        } catch (JAXBException je) {
            throw new IOException("", je);
        }
    }


    /**
     * Loads DownloadStep from a file.
     * @param file The file to load the DownloadStep from.
     * @return The restored DownloadStep.
     * @throws IOException Something went wrong.
     */
    public static DownloadStep read(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            JAXBContext context = JAXBContext.newInstance(DownloadStep.class);
            Unmarshaller um = context.createUnmarshaller();
            BufferedInputStream bis = new BufferedInputStream(fis);
            return (DownloadStep)um.unmarshal(bis);
        } catch (JAXBException je) {
            throw new IOException("", je);
        }
    }
}
