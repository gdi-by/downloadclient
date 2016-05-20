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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlSchemaType;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class DownloadStep implements Serializable {

    private static final Logger log
        = Logger.getLogger(DownloadStep.class.getName());

    private static final long serialVersionUID = 7526471155622776147L;

    @XmlSchemaType(name = "string")
    private String serviceType;

    @XmlSchemaType(name = "string")
    private String serviceURL;

    @XmlSchemaType(name = "string")
    private String dataSet;

    @XmlSchemaType(name = "string")
    private String parameters;

    /**
     * Save the Object.
     * @param file File to save to
     * @throws IOException if space does not exist
     */
    public void write(File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            writeObject(oos);
            oos.flush();
        }
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {

    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {

    }

    private void readObjectNoData()
            throws ObjectStreamException {
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
