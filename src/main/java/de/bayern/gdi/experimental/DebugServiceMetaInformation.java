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

import de.bayern.gdi.model.ServiceMetaInformation;
import java.net.MalformedURLException;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class DebugServiceMetaInformation {

    private ServiceMetaInformation smi;
    private String urlString;
    private String userName;
    private String password;


    private DebugServiceMetaInformation(String url,
                                        String userName,
                                        String password) {
        this.urlString = url;
        this.userName = userName;
        this.password = password;
    }

    /**
     * Starter Method.
     * @param args up to three args for url, username and pw
     */
    public static void main(String[] args) {
        DebugServiceMetaInformation dsmi;
        if (args.length > 1) {
            dsmi = new DebugServiceMetaInformation(args[0], args[1], args[2]);
        } else {
            dsmi = new DebugServiceMetaInformation(args[0], null, null);
        }
        dsmi.go();
    }

    private void go() {
        try {
            smi = new ServiceMetaInformation(
                    this.urlString,
                    this.userName,
                    this.password);
            System.out.println("ServiceType: "
                    + smi.getServiceType().toString());
            System.out.println("ServiceURL: "
                    + smi.getServiceURL());
            System.out.println("Is Restricted: "
                    + smi.isRestricted().toString());
            System.out.println("Additional: "
                    + smi.getAdditionalMessage());
            System.out.println("Password: "
                    + smi.getPassword());
            System.out.println("Username: "
                    + smi.getUsername());

        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        }
    }
}
