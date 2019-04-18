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

package de.bayern.gdi.gui.map;

/**
 * Information about the Polygon.
 */
public class PolygonInfos {
    private String name;
    private String id;

    /**
     * Constructor.
     *
     * @param name the name
     * @param id   the id
     */
    public PolygonInfos(String name, String id) {
        this.name = name;
        this.id = id;
    }

    /**
     * returns the name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * returns the ID.
     *
     * @return the ID
     */
    public String getID() {
        return this.id;
    }
}
