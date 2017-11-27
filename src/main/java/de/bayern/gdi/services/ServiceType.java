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
 * Types of Webservices.
 */
public enum ServiceType {
    /**
     * WFSOne Webservice.
     */
    WFSOne,
    /**
     * WFSTwo Webservice.
     */
    WFSTwo,
    /**
     * Atom Webservice.
     */
    Atom;

    private static final String ATOM = "atom";
    private static final String WFSONE = "wfs 1";
    private static final String WFSTWO = "wfs 2";

    /**
     * Checks if two service types are equal or both null.
     * @param a The first service type.
     * @param b The second service type.
     * @return true if both service type are equal or both null.
     */
    public static boolean nullOrEquals(ServiceType a, ServiceType b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    /**
     * guesses the service Type based on String.
     * @param typeString the string
     * @return service Type
     */
    public static ServiceType guess(String typeString) {
        typeString = typeString.toLowerCase();
        if (typeString.contains(ATOM)) {
            return ServiceType.Atom;
        }
        if (typeString.contains(WFSONE)) {
            return ServiceType.WFSOne;
        }
        if (typeString.contains(WFSTWO)) {
            return ServiceType.WFSTwo;
        }
        return null;
    }
}
