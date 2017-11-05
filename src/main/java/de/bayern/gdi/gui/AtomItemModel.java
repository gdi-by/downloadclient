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

import de.bayern.gdi.services.Atom;

/**
 * Wrapper for Atom service items.
 */
public class AtomItemModel implements ItemModel {

    private Atom.Item item;

    /**
     * Construct the wrapper.
     * @param i the wrapped item
     */
    public AtomItemModel(Atom.Item i) {
        this.item = i;
    }

    public Object getItem() {
        return this.item;
    }

    public String getDataset() {
        return item.getID();
    }

    @Override
    public String toString() {
        return this.item.getTitle();
    }
}
