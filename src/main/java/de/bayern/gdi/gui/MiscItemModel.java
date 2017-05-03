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
 * Class for holding misc information that doesn't fit in other models.
 * @author Alexander Wöstmann (awoestmann@intevation.de)
 */
public class MiscItemModel implements ItemModel {

    private String item;
    private String dataset;

    /**
     * Returns the item.
     *
     * @return The item
     */
    public Object getItem() {
        return item;
    }

    /**
     * Returns the dataset.
     *
     * @return The dataset
     */
    public String getDataset() {
        return dataset;
    }

    /**
     * Sets the item.
     *
     * @param item The new item
     */
    public void setItem(String item) {
        this.item = item;
    }

    /**
     * Sets the dataset.
     *
     * @param dataset The new dataset
     */
    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

   /**
    * Returns a string representation of this item.
    *
    * @return The string representation
    */
    public String toString() {
        return item;
    }
}
