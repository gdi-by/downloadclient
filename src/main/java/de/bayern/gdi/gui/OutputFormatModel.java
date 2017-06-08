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


package de.bayern.gdi.gui;

/**
* Simple Model containing a format string and a bool if the format is
* available on the WFS.
* @author Alexander Woestmann(awoestmann@intevation.de)
*/
public class OutputFormatModel {
    private String item;
    private boolean available;

   /**
    * Constructor.
    */
    public OutputFormatModel() {
        available = true;
    }

   /**
    * Returns item.
    *
    * @return item as String
    */
    public String getItem() {
        return item;
    }

   /**
    * Sets item.
    *
    * @param item The new item value
    */
    public void setItem(String item) {
        this.item = item;
    }

   /**
    * Returns true if output format is available.
    *
    * @return available
    */
    public boolean isAvailable() {
        return available;
    }

   /**
    * Sets available status.
    *
    * @param available The new available status
    */
    public void setAvailable(boolean available) {
        this.available = available;
    }

   /**
    * Returns a string representation.
    *
    * @return String representation
    */
    public String toString() {
        return item;
    }
}

