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
/**
 * Test for Parameter.
 */
public class ProxyConfigurationTest extends TestCase {

    /** Define a constant for testing ports. */
    public static final Integer PORT = 1234;

    /** Test the overrideSystemSettings getter/setter. */
    @Test
    public void testOverrideSystemSettings() {
        ProxyConfiguration pct = new ProxyConfiguration();
        pct.setOverrideSystemSettings(true);
        assertTrue(pct.getOverrideSystemSettings());
    }

    /** Test the httpProxyHost getter/setter. */
    @Test
    public void testHttpProxyHost() {
        ProxyConfiguration pct = new ProxyConfiguration();
        pct.setHttpProxyHost("a host");
        assertEquals("a host", pct.getHttpProxyHost());
    }

    /** Test the httpProxyPort getter/setter. */
    @Test
    public void testHttpProxyPort() {
        ProxyConfiguration pct = new ProxyConfiguration();
        pct.setHttpProxyPort(PORT);
        assertEquals(PORT, pct.getHttpProxyPort());
    }

    /** Test the httpProxyString getter. */
    @Test
    public void testHttpProxyString() {
        ProxyConfiguration pct = new ProxyConfiguration();
        assertEquals(null, pct.getHttpProxyString());
    }

    /** Test the httpProxyUser getter/setter. */
    @Test
    public void testHttpProxyUser() {
        ProxyConfiguration pct = new ProxyConfiguration();
        pct.setHttpProxyUser("a proxy user");
        assertEquals("a proxy user", pct.getHttpProxyUser());
    }

    /** Test the httpProxyPasswort getter/setter. */
    @Test
    public void testHttpProxyPasswort() {
        ProxyConfiguration pct = new ProxyConfiguration();
        pct.setHttpProxyPasswort("a password");
        assertEquals("a password", pct.getHttpProxyPasswort());
    }

    /** Test the httpNonProxyHosts getter/setter. */
    @Test
    public void testHttpNonProxyHosts() {
        ProxyConfiguration pct = new ProxyConfiguration();
        pct.setHttpNonProxyHosts("a non proxy host");
        assertEquals("a non proxy host", pct.getHttpNonProxyHosts());
    }

    /** Test the httpsProxyHost getter/setter. */
    @Test
    public void testHttpsProxyHost() {
        ProxyConfiguration pct = new ProxyConfiguration();
        pct.setHttpsProxyHost("an encrypted proxy host");
        assertEquals("an encrypted proxy host", pct.getHttpsProxyHost());
    }

    /** Test the httpsProxyPort getter/setter. */
    @Test
    public void testHttpsProxyPort() {
        ProxyConfiguration pct = new ProxyConfiguration();
        pct.setHttpsProxyPort(PORT);
        assertEquals(PORT, pct.getHttpsProxyPort());
    }

    /** Test the httpsProxyString getter/setter. */
    @Test
    public void testHttpsProxyString() {
        ProxyConfiguration pct = new ProxyConfiguration();
        assertEquals(null, pct.getHttpsProxyString());
    }

    /** Test the httpsProxyUser getter/setter. */
    @Test
    public void testHttpsProxyUser() {
        ProxyConfiguration pct = new ProxyConfiguration();
        pct.setHttpsProxyUser("an encrypted proxy user");
        assertEquals("an encrypted proxy user", pct.getHttpsProxyUser());
    }

    /** Test the httpsProxyPasswort getter/setter. */
    @Test
    public void testHttpsProxyPasswort() {
        ProxyConfiguration pct = new ProxyConfiguration();
        pct.setHttpsProxyPasswort("an encrypted password");
        assertEquals("an encrypted password", pct.getHttpsProxyPasswort());
    }

    /** Test the httpsNonProxyHosts getter/setter. */
    @Test
    public void testHttpsNonProxyHosts() {
        ProxyConfiguration pct = new ProxyConfiguration();
        pct.setHttpsNonProxyHosts("an encrypted non proxy host");
        assertEquals("an encrypted non proxy host", pct.getHttpsNonProxyHosts());
    }

    /** Test the name getter/setter. */
    @Test
    public void testName() {
        ProxyConfiguration pct = new ProxyConfiguration();
        assertEquals("ProxyConfig", pct.getName());
    }

}
