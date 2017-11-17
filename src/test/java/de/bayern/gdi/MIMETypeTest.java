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

import junit.framework.TestCase;

import org.junit.Test;

import de.bayern.gdi.model.MIMEType;

/**
 * Test for Mimetypes
 */
public class MIMETypeTest extends TestCase {

    public MIMETypeTest(String testName) {
        super(testName);
    }

    /** Test the name getter/stetter. */
    @Test
    public void testName() {
        MIMEType mt = new MIMEType();
        mt.setName("Hello");
        assertEquals("Hello", mt.getName());
    }

    /** Test the name getter/stetter. */
    @Test
    public void testExt() {
        MIMEType mt = new MIMEType();
        mt.setExt("Ext");
        assertEquals("Ext", mt.getExt());
    }

    /** Test the name getter/stetter. */
    @Test
    public void testType() {
        MIMEType mt = new MIMEType();
        mt.setType("raster");
        assertEquals("raster", mt.getType());
    }

    /** Test toString. */
    @Test
    public void testToString() {
        MIMEType mt = new MIMEType();
        mt.setName("A");
        mt.setExt("B");
        mt.setType("C");
        assertEquals("[name: 'A' ext: 'B' type: 'C']", mt.toString());
    }
}
