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

import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
/** Wrapper Class for CRS. */

public class CRSModel {
    private CoordinateReferenceSystem crs;
    private String oldName;
    private boolean available;
    /**
     * Constructor.
     * @param crs crs
     */
    public CRSModel(CoordinateReferenceSystem crs) {
        this.crs = crs;
        available = true;
    }

    /**
     * gets the CRS.
     * @return crs
     */
    public CoordinateReferenceSystem getCRS() {
        return this.crs;
    }

    /**
     * gives a readable String.
     * @return string
     */
    @Override
    public String toString() {
        return this.crs.getName().toString();
    }

    /**
     * Checks of another crsModel is equal.
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CRSModel)) {
            return false;
        }
        CRSModel c = (CRSModel)o;
        return c.getCRS().toString().equals(this.getCRS().toString());
    }

    /**
     * returns the hashCode for the object.
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return this.getCRS().toString().hashCode();
    }

   /**
    * Returns true if CRS is available on the current
    * WFS.
    * @return true if available, false if not
    */
    public boolean isAvailable() {
        return available;
    }

   /**
    * Sets the available status.
    *
    * @param available The new value
    */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * gets the old name of the crs.
     * @return old name of crs
     */
    public String getOldName() {
        return oldName;
    }

    /**
     * sets the old name of the crs.
     * @param oldName old name of the crs
     */
    public void setOldName(String oldName) {
        this.oldName = oldName;
    }
}
