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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.bayern.gdi.utils.StringUtils;

/** Model for mapping MIME types to file name extensions. */
@XmlRootElement(name = "MIMETypes")
@XmlAccessorType(XmlAccessType.FIELD)
public class MIMETypes {

    private static final Logger log
        = Logger.getLogger(MIMETypes.class.getName());

    /** Name of the config file. */
    public static final String MIME_TYPES_FILE =
        "mimetypes.xml";

    @XmlElement(name = "Type")
    private List<MIMEType> types;

    public MIMETypes() {
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

    @Override
    public String toString() {
        return "[" + StringUtils.join(types, ", ") + "]";
    }

    /**
     * Find the extension for a given type.
     * @param typeName The name of the type.
     * @param def Default if not found.
     * @return The extension if found def otherwise.
     */
    public String findExtension(String typeName, String def) {
        String ext = findExtension(typeName);
        return ext != null ? ext : def;
    }

    /**
     * Find the extension for a given type.
     * @param typeName The name of the type.
     * @return The extension if found null otherwise.
     */
    public String findExtension(String typeName) {

        if (typeName == null) {
            return null;
        }

        for (MIMEType type: types) {
            String name = type.getName();
            if (name != null && name.equalsIgnoreCase(typeName)) {
                return type.getExt();
            }
        }

        return null;
    }

    /**
     * Find the MIME type for a given type name.
     * @param name The name of the type.
     * @return The MIME type if found null otherwise.
     */
    public MIMEType findByName(String name) {

        if (name == null) {
            return null;
        }

        for (MIMEType type: types) {
            String n = type.getName();
            if (n != null && n.equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
    }

    /**
     * Loads MIME from a file.
     * @param file The file to load the MIMETypes from.
     * @return The restored MIME.
     * @throws IOException Something went wrong.
     */
    public static MIMETypes read(File file) throws IOException {
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
    public static MIMETypes read(InputStream is) throws IOException {
        try {
            JAXBContext context = JAXBContext.newInstance(MIMETypes.class);
            Unmarshaller um = context.createUnmarshaller();
            BufferedInputStream bis = new BufferedInputStream(is);
            return (MIMETypes)um.unmarshal(bis);
        } catch (JAXBException je) {
            throw new IOException(je.getMessage(), je);
        }
    }

    /**
     * Load mime types from ressources.
     * @return The default mime types.
     */
    public static MIMETypes loadDefault() {
        InputStream in = null;
        try {
            in = MIMETypes.class.getResourceAsStream(MIME_TYPES_FILE);
            if (in == null) {
                log.log(Level.SEVERE,
                    MIME_TYPES_FILE + " not found");
                return new MIMETypes();
            }
            return read(in);
        } catch (IOException ioe) {
            log.log(Level.SEVERE, "Failed to load mimetypes", ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }
        return new MIMETypes();
    }
}
