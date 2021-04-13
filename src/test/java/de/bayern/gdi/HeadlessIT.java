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
package de.bayern.gdi;

import org.junit.Test;

/**
 * Integrationtest verifing start of CLI.
 */
public class HeadlessIT {

    private static String[] cliargs = new String[0];

    /**
     * Start CLI with property for log4j.
     *
     * @param args none
     */
    public static void main(String[] args) {
        cliargs = args;
        new HeadlessIT().testExecutionOfCLI();
    }

    /**
     * Simple test to verify that CLI is executable.
     */
    @Test
    public void testExecutionOfCLI() {
        System.setProperty("logfilename", "logdlc-TEST.txt");
        Headless.runHeadless(cliargs, null);
    }
}
