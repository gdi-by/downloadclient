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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlSchemaType;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class DownloadStep {

    private static final Logger log
        = Logger.getLogger(DownloadStep.class.getName());

    @XmlSchemaType(name = "string")
    private String serviceType;

    @XmlSchemaType(name = "string")
    private String serviceURL;

    @XmlSchemaType(name = "string")
    private String dataSet;

    @XmlSchemaType(name = "string")
    private String parameters;

    public DownloadStep() {
    }

    /**
     * @param file The file to load data from.
     */
    public DownloadStep(File file) {
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
    public String getDataSet() {
        return dataSet;
    }

    /**
     * @param dataSet the dataSet to set
     */
    public void setDataSet(String dataSet) {
        this.dataSet = dataSet;
    }

    /**
     * @return the parameters
     */
    public String getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}
