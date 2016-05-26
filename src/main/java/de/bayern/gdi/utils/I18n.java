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

import java.util.Locale;
//import java.util.logging.Logger;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/** Providing convenience methods for internationalisation.
 *
 * Using a singleton -- with unsynchronised access to the instance.
 */
public final class I18n {
    /**
     */
    private static class Holder {
        private static final I18n INSTANCE = new I18n();

    }

   // private static final Logger log
   //     = Logger.getLogger(FileResponseHandler.class.getName());
   // later use log.info() or similiar

    /** Avoiding more instances. */
    private I18n() {
    }

    public static I18n getInstance() {
        return Holder.INSTANCE;
    }

    /** Return translation if found, otherwise key.
     *
     * @param key to be translated
     * @return translated key or key if no translation is found
    */
    public static String getMsg(String key) {
        /** ResourceBundle caches according to documentation. */
        ResourceBundle messages =  ResourceBundle.getBundle("messages");
        try {
            return messages.getString(key);
        } catch (MissingResourceException exc) {
            return key;
        }
    }

    /** Returns the locale of the resource bundle.
     *
     * @return Locale used.
     */
    public static Locale getLocale() {
         ResourceBundle messages =  ResourceBundle.getBundle("messages");
        return messages.getLocale();
    }
}
