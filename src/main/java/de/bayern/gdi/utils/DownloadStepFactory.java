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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;

import javax.xml.bind.annotation.XmlSchemaType;

import javax.xml.transform.Result;

import javax.xml.transform.stream.StreamResult;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public class DownloadStepFactory implements Serializable {

    @XmlSchemaType(name = "string")
    private String serviceType;

    @XmlSchemaType(name = "string")
    private String serviceURL;

    @XmlSchemaType(name = "string")
    private String dataSet;

    @XmlSchemaType(name = "string")
    private String parameters;

    private static final transient Logger log
            = Logger.getLogger(DownloadStepFactory.class.getName());

    @XmlSchemaType(name = "string")
    private static final long serialVersionUID = 7526471155622776147L;


    /**
     * Constructor.
     */
    public DownloadStepFactory() {

    }

    /**
     * Constructor.
     * @param file file to read from
     */
    public DownloadStepFactory(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            readObject(ois);
        } catch (IOException | ClassNotFoundException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Save the Object.
     * @param file File to save to
     * @throws IOException if space does not exist
     */
    public void write(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        writeObject(oos);
    }

    private boolean validateState() {
        //TODO: Check for parameters and validate them.
        return true;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        if (validateState()) {
            try {
                JAXBContext jc =
                        JAXBContext.newInstance(DownloadStepFactory.class);
                jc.generateSchema(new SchemaOutputResolver() {

                    @Override
                    public Result createOutput(String namespaceUri,
                                               String suggestedFileName)
                            throws IOException {
                        StreamResult result = new StreamResult(out);
                        result.setSystemId(suggestedFileName);
                        return result;
                    }
                });

            } catch (JAXBException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {

    }

    private void readObjectNoData()
            throws ObjectStreamException {

    }
}
