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

package de.bayern.gdi.services;

/**
 * Created by jochen on 03.06.16.
 */
public class Field {

    /** name. */
    private String name;
    /** type. */
    private String type;

    public Field() {
    }

    /**
     * Returns the name of this field.
     * @return the name of this field.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the type of this field.
     * @return the type of this field.
     */
    public String getType() {
        return this.type;
    }

    /**
     * @param name name.
     * @param type type.
     */
    public Field(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return "field: { name: " + name + " type: " + type + " }";
    }
}
