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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Provides access to the version of the DCL.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class Version {

    private static final ResourceBundle VERSION_RESOURCE_BUNDLE =
        ResourceBundle.getBundle("version");

    private Version() {
    }

    /**
     * Return the version of the DCL, otherwise 'UNKNOWN'.
     *
     * @return the version of the DCL or 'UNKNOWN' if not found
     */
    public static String getVersion() {
        try {
            return VERSION_RESOURCE_BUNDLE.getString("dlc.version");
        } catch (MissingResourceException mre) {
            return "UNKNOWN";
        }
    }

}
