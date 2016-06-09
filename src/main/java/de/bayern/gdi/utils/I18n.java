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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/** Providing convenience methods for internationalisation.
 *
 * Using a singleton -- with unsynchronised access to the instance.
 *
 * @author Bernhard E. Reiter (bernhard.reiter@intevation.de)
 */
public final class I18n {

    /** Inner class to implicit synchronize the instance access. */
    private static final class Holder {
        static final I18n INSTANCE = new I18n();
    }

    /** Avoiding more instances. */
    private I18n() {
    }

    public static I18n getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Get the current i18n resource bundle.
     * @return the resource bundle
     */
    public static ResourceBundle getBundle() {
        // ResourceBundle caches according to documentation.
        return ResourceBundle.getBundle("messages");
    }

    /**
     * Return translation if found, otherwise key.
     *
     * @param key to be translated
     * @return translated key or key if no translation is found
     */
    public static String getMsg(String key) {
        try {
            return getBundle().getString(key);
        } catch (MissingResourceException mre) {
            return key;
        }
    }


    /**
     * Returns a formatted translation or the key if translation
     * is missing.
     * @param key The key to be translated.
     * @param args The arguments for the formatting.
     * @return The formatted translation.
     */
    public static String format(String key, Object ... args) {
        ResourceBundle bundle = getBundle();
        String tmpl;
        try {
            tmpl = bundle.getString(key);
        } catch (MissingResourceException mre) {
            return key;
        }

        MessageFormat mf = new MessageFormat(tmpl, bundle.getLocale());

        return mf.format(args, new StringBuffer(), null).toString();
    }

    /** Returns the locale of the resource bundle.
     *
     * @return Locale used.
     */
    public static Locale getLocale() {
        return getBundle().getLocale();
    }
}
