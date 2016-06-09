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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.bayern.gdi.gui.ServiceModel;
import de.bayern.gdi.services.CatalogService;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class DebugCatalog {

    private CatalogService catalog;
    private String urlString;
    private String userName;
    private String password;


    private DebugCatalog(String url, String userName, String password) {
        this.urlString = url;
        this.userName = userName;
        this.password = password;
    }

    /**
     * Starter Method.
     * @param args Single Argument, just a URL to a WFSTwo Service
     */
    public static void main(String[] args) {
        DebugCatalog dcata;
        if (args.length > 1) {
            dcata = new DebugCatalog(args[0], args[1], args[2]);
        } else {
            dcata = new DebugCatalog(args[0], null, null);
        }
        dcata.go();
    }

    private void go() {
        try {
            catalog = new CatalogService(this.urlString,
                    this.userName,
                    this.password);
            System.out.println("Servicename: " + catalog.getProviderName());
            String first = "bayer";
            String second = "baye";
            String third = "atom";
            String fourth = "wfs";
            printService(catalog.getServicesByFilter(first));
            printService(catalog.getServicesByFilter(second));
            printService(catalog.getServicesByFilter(third));
            printService(catalog.getServicesByFilter(fourth));

        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        }
    }



    private void printArrayStringList(ArrayList<String> als) {
        for (String str : als) {
            System.out.println(str);
        }
    }

    private void printService(List<ServiceModel> services)  {
        Iterator it = services.iterator();
        while (it.hasNext()) {
            ServiceModel model = (ServiceModel)it.next();
            System.out.println(model.getName() + " = " + model.getUrl());
        }
    }
}
