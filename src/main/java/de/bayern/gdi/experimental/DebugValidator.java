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

package de.bayern.gdi.experimental;

import de.bayern.gdi.gui.Validator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class DebugValidator {


    private Validator validator;
    private DebugValidator() {
        validator = Validator.getInstance();
    }

    /**
     * Starter Method.
     * @param args Single Argument, just a URL to a WFSTwo Service
     */
    public static void main(String[] args) {
        DebugValidator debugValidator = new DebugValidator();
        debugValidator.go();

    }

    private void go() {
        Map<String, String> map = new HashMap<>();
        map.put("String", "Test12");
        map.put("Integer", "Test12");
        map.put("Double", "12,25");
        map.put("Float", "42.23");
        map.put("Short", "8000");
        Set<Map.Entry<String, String>> entries = map.entrySet();
        Iterator it = entries.iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            boolean ret = validator.isValid((String) pair.getKey(),
                    (String) pair.getValue());
            System.out.println(pair.getValue() + "\t\t try to fit in " + pair
                    .getKey() + ":\t" + ret);
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

}
