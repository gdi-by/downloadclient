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

import org.apache.commons.codec.binary.Base64;

/**
 * Common string operations.
 */
public class StringUtils {

    private StringUtils() {
    }

    /**
     * Encodes user name and password into base64 encoded string.
     * @param user The user name.
     * @param password The passord.
     * @return The encoded password. null if user or password is null.
     */
    public static String getBase64EncAuth(String user, String password) {
        if (user == null || password == null) {
            return null;
        }
        String auth = user + ":" + password;
        return new String(Base64.encodeBase64(auth.getBytes()));
    }
}
