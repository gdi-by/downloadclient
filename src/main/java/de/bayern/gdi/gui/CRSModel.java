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

    /**
     * Constructor.
     * @param crs crs
     */
    public CRSModel(CoordinateReferenceSystem crs) {
        this.crs = crs;
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

    public boolean equals(CRSModel crsModel) {
        if(this.toString().equals(crsModel.toString())) {
            return true;
        }
        return false;
    }
}
