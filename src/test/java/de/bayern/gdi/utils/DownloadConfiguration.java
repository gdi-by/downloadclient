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

/**
 * Encapsulates XML configurations for download test cases.
 * @author thomas
 */
public class DownloadConfiguration {

    /**
     * Simple configuration.
     */
    private static final String SIMPLE_CONFIG_TMPL =
        "<?xml version=\"1.0\""
            + " encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<DownloadSchritt>\n"
            + "    <ServiceTyp>ATOM</ServiceTyp>\n"
            + "    <URL>https://geoportal.bayern.de"
            + "/gdiadmin/ausgabe/ATOM_SERVICE"
            + "/a90c75a0-f1b5-46e7-9e45-c0385fd0c200</URL>\n"
            + "    <DownloadPfad>%s</DownloadPfad>\n"
            + "    <Dataset>2496b2ed-8a64-465a-95dd-170799788982</Dataset>\n"
            + "    <Verarbeitungskette/>\n"
            + "    <Parameters>\n"
            + "        <Parameter>\n"
            + "            <Name>VARIATION</Name>\n"
            + "            <Wert>http://www.geodaten.bayern.de/ba-data/Themen/"
            + "kml/biergarten.kml</Wert>\n"
            + "        </Parameter>\n"
            + "        <Parameter>\n"
            + "            <Name>outputformat</Name>\n"
            + "            <Wert>application/vnd.google-earth.kml+xml</Wert>\n"
            + "        </Parameter>\n"
            + "    </Parameters>\n"
            + "</DownloadSchritt>";

    /**
     * Retrieves a simple config.
     * @return simple config
     */
    public String getSimpleConfiguration(String path) {
        return String.format(SIMPLE_CONFIG_TMPL, path);
    }

}
