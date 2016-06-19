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
import javax.xml.bind.annotation.XmlRootElement;

/** Model for mapping MIME types to file name extensions. */
@XmlRootElement(name = "MIMETypes")
@XmlAccessorType(XmlAccessType.FIELD)
public class MIME {

    @XmlElement(name = "Type")
    private List<MIMEType> types;

    public MIME() {
        types = new ArrayList<>();
    }

    /**
     * @return the types
     */
    public List<MIMEType> getTypes() {
        return types;
    }

    /**
     * @param types the types to set
     */
    public void setTypes(List<MIMEType> types) {
        this.types = types;
    }

    /**
     * Find the extension for a given type.
     * @param typeName The name of the type.
     * @return The extension if found null otherwise.
     */
    public String findExtension(String typeName) {

        for (MIMEType type: types) {
            String name = type.getName();
            if (name != null && name.equalsIgnoreCase(typeName)) {
                return type.getExt();
            }
        }

        return null;
    }

    /**
     * Loads MIME from a file.
     * @param file The file to load the MIME from.
     * @return The restored MIME.
     * @throws IOException Something went wrong.
     */
    public static MIME read(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return read(fis);
        }
    }

    /**
     * Loads MIME from an input stream.
     * @param is The input stream to load the MIME from.
     * @return The restored MIME.
     * @throws IOException Something went wrong.
     */
    public static MIME read(InputStream is) throws IOException {
        try {
            JAXBContext context = JAXBContext.newInstance(MIME.class);
            Unmarshaller um = context.createUnmarshaller();
            BufferedInputStream bis = new BufferedInputStream(is);
            return (MIME)um.unmarshal(bis);
        } catch (JAXBException je) {
            throw new IOException(je.getMessage(), je);
        }
    }
}
