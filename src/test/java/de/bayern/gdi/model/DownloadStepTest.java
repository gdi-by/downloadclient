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
package de.bayern.gdi.model;

import junit.framework.TestCase;
import org.junit.Test;

public class DownloadStepTest extends TestCase {


    /** Test the dataset getter/setter. */
    @Test
    public void testDataset() {
        DownloadStep dlst = new DownloadStep();
        dlst.setDataset("dataset");
        assertEquals("dataset", dlst.getDataset());
    }

//    /** Test the processingSteps getter/setter. */
//    @Test
//    public void testProcessingSteps() throws Exception {
//        DownloadStep dlst = new DownloadStep();
//        dlst.setProcessingSteps("dataset");
//        assertEquals("dataset", dlst.getDataset());
//    }
//
//    @Test
//    public void testParameters() throws Exception {
//        DownloadStep dlst = new DownloadStep();
//        dlst.setParameters("dataset");
//        assertEquals("dataset", dlst.getDataset());
//    }

    /** Test the ServiceType getters and setters. */
    @Test
    public void testServiceType() {
        DownloadStep dlst = new DownloadStep();
        dlst.setServiceType("service type");
        assertEquals("service type", dlst.getServiceType());
    }

    /** Test the serviceURL getter/setter. */
    @Test
    public void testServiceURL() {
        DownloadStep dlst = new DownloadStep();
        dlst.setServiceURL("service url");
        assertEquals("service url", dlst.getServiceURL());
    }

    /** Test the path getter/setter. */
    @Test
    public void testPath() {
        DownloadStep dlst = new DownloadStep();
        dlst.setPath("path");
        assertEquals("path", dlst.getPath());
    }

}
