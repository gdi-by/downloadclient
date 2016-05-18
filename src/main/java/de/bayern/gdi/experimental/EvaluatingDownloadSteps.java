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

import de.bayern.gdi.utils.DownloadStepFactory;

import java.io.File;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public class EvaluatingDownloadSteps {

    private EvaluatingDownloadSteps() { }

    private static boolean inArgs(String[] args, String param) {
        for (String arg: args) {
            if (arg.equals(param)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Start Here to test the serialisation of download steps.
     * @param args arguments
     */
    public static void main(String[] args) {

        File file = new File(args[0]);
        if (inArgs(args, "-write")) {
            try {
                DownloadStepFactory dsf = new DownloadStepFactory();
                dsf.write(file);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        if (inArgs(args, "-read")) {
            try {
                DownloadStepFactory dsf = new DownloadStepFactory(file);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
