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

import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.services.WFSMetaExtractor;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Jürgen Weichand (LDBV Bayern)
 */
public class WfsTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public WfsTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(WfsTest.class);
    }

    /**
     * Check several WFS
     *
     * @throws java.net.MalformedURLException
     */
    public void testWfs() throws IOException  {
        String[] urls =
        {
            "http://geoserv.weichand.de:8080/geoserver/ows?"
                + "service=wfs&version=2.0.0&request=GetCapabilities",
            "http://geoserv.weichand.de/cgi-bin/mapserv-dev?"
                + "map=/home/wei/wfs20-example.map&"
                + "service=WFS&acceptversions=2.0.0&request=GetCapabilities",
            "http://geoserv.weichand.de/cgi-bin/test-mapserver7.cgi?"
                + "service=WFS&acceptversions=2.0.0&request=GetCapabilities",
            "http://demo.deegree.org/inspire-workspace/services/wfs?"
                + "service=WFS&acceptversions=2.0.0&request=GetCapabilities"
        };

        for (String url : urls) {
            WFSMeta wfsMeta = new WFSMetaExtractor(url).parse();
            hasOutputformats(wfsMeta);
        }
    }


    private void hasOutputformats(WFSMeta wfsMeta) {
        if (wfsMeta.findOperation("GetFeature").outputFormats.size() < 1) {
            StringBuilder sb = new StringBuilder();
            sb.append("No Outputformat found!");
            sb.append(" | ");
            sb.append(wfsMeta.url);
            assertFalse(sb.toString(), true);
        }
    }

}
