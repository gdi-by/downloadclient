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
package de.bayern.gdi.config;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Encapsulates XML configurations for download test cases.
 *
 * @author thomas
 */
public class DownloadConfiguration {

    /**
     * Biergarten.
     */
    private static final String BIERGARTEN = "/downloadconfig/biergarten.xml";
    /**
     * Nuremburg.
     */
    private static final String NUREMBURG = "/downloadconfig/nuremburg.xml";
    /**
     * AGZ.
     */
    private static final String AGZ = "/downloadconfig/agz.xml";
    /**
     * CQL.
     */
    private static final String CQL = "/downloadconfig/cql.xml";

    /**
     * AGZ.
     *
     * @param path tempdir path
     * @return config
     */
    public String getAGZConfiguration(String path) {
        return getConfiguration(AGZ, path);
    }

    /**
     * Biergarten.
     *
     * @param path temppath
     * @return config
     */
    public String getBiergartenConfiguration(String path) {
        return getConfiguration(BIERGARTEN, path);
    }

    /**
     * Nuremburg.
     *
     * @param path temppath
     * @return config
     */
    public String getNuremburgConfig(String path) {
        return getConfiguration(NUREMBURG, path);
    }

    /**
     * CQL.
     *
     * @param path temppath
     * @return config
     */
    public String getCqlConfig(String path) {
        return getConfiguration(CQL, path);
    }


    private String getConfiguration(String resource, String path) {
        try (InputStream resourceStream =
                 getClass().getResourceAsStream(resource)) {
            String resourceAsString = IOUtils.toString(resourceStream);
            return String.format(resourceAsString, path);
        } catch (IOException e) {
            throw new IllegalArgumentException("Resource " + resource
                + " could not be read.", e);
        }
    }

}
