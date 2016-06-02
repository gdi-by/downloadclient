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

import de.bayern.gdi.services.Atom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class DebugAtom {

    private Atom atom;
    private String urlString;
    private String userName;
    private String password;


    private DebugAtom(String url, String userName, String password) {
        this.urlString = url;
        this.userName = userName;
        this.password = password;
    }

    /**
     * Starter Method.
     * @param args Single Argument, just a URL to a WFSTwo Service
     */
    public static void main(String[] args) {
        DebugAtom datom;
        if (args.length > 1) {
            datom = new DebugAtom(args[0], args[1], args[2]);
        } else {
            datom = new DebugAtom(args[0], null, null);
        }
        datom.go();
    }

    private void go() {
        atom = new Atom(this.urlString, this.userName, this.password);
        printArrayStringList(atom.getTypes());
        printStringMap(atom.getAttributes(atom.getTypes().get(1)));
    }



    private void printArrayStringList(ArrayList<String> als) {
        for (String str : als) {
            System.out.println(str);
        }
    }

    private void printStringMap(Map<String, String> map)  {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }
}
