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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class UnauthorizedLog implements Unauthorized {

    private static final Logger log
            = Logger.getLogger(UnauthorizedLog.class.getName());

    /**
     * Constructor.
     */
    public UnauthorizedLog() {
    }

    /**
     * writes to the log, when user and pw is wrong.
     */
    public void unauthorized() {
        log.log(Level.SEVERE, I18n.getMsg("gui.wrong.user.and.pw"), this);
    }
}
