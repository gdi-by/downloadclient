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

//import java.util.Locale;
//import java.util.MissingResourceException;
//import java.util.ResourceBundle;

/** Providing convenience methods for internationalisation.
 *
 * Using a singleton.
 */

public class I18n {
    /**
     */
    private static final class InstanceHolder {
    static final I18n INSTANCE = new I18n();
  }

  private I18n() {
  }

  public static I18n getInstance() {
    return InstanceHolder.INSTANCE;
  }

}

// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

