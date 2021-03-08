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
package de.bayern.gdi.config;

/**
 * Encapsulates the configured credentials.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class Credentials {

    private String username;

    private String password;

    /**
     * Instantiate with username and password.
     *
     * @param username never <code>null</code>
     * @param password never <code>null</code>
     */
    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * @return the name of the user, never <code>null</code>
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password of the user, never <code>null</code>
     */
    public String getPassword() {
        return password;
    }

}
