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

import de.bayern.gdi.utils.StringUtils;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Test for Mimetypes.
 */

public class MIMETypesTest extends TestCase {

    public MIMETypesTest(String testName) {
        super(testName);
    }

    /** Test the types getter/stetter. */
    @Test
    public void testTypes() {

        MIMEType mimeType1 = new MIMEType();
        MIMEType mimeType2 = new MIMEType();
        mimeType1.setType("Hello");
        mimeType2.setType("World");

        ArrayList<MIMEType> actualMimeType = new ArrayList<>();
        actualMimeType.add(mimeType1);
        actualMimeType.add(mimeType2);

        ArrayList<MIMEType> expectedMimeType = new ArrayList<>();
        expectedMimeType.add(mimeType1);
        expectedMimeType.add(mimeType2);

        MIMETypes mimeTypesActual = new MIMETypes();
        mimeTypesActual.setTypes(actualMimeType);

        MIMETypes mimeTypesExpected = new MIMETypes();
        mimeTypesExpected.setTypes(expectedMimeType);

        assertEquals(mimeTypesExpected.getTypes()
            , mimeTypesActual.getTypes());
    }

    @Test
    public void testToString() {

        MIMEType mimeType1 = new MIMEType();
        MIMEType mimeType2 = new MIMEType();
        mimeType1.setType("Hello");
        mimeType2.setType("World");

        ArrayList<MIMEType> actualMimeType = new ArrayList<>();
        actualMimeType.add(mimeType1);
        actualMimeType.add(mimeType2);

        ArrayList<MIMEType> expectedMimeType = new ArrayList<>();
        expectedMimeType.add(mimeType1);
        expectedMimeType.add(mimeType2);

        MIMETypes actualMimeTypes = new MIMETypes();
        actualMimeTypes.setTypes(actualMimeType);

        MIMETypes expectedMimeTypes = new MIMETypes();
        expectedMimeTypes.setTypes(expectedMimeType);

        assertEquals("[" + StringUtils.join(expectedMimeTypes.getTypes(), ", ")
            + "]", actualMimeTypes.toString());
    }

    /** TODO Test the findExtensions with typeName and default*/
//    @Test
//    public void testFindExtension() {
//        String typeName = "Hallo";
//    }

    /** TODO Test the findByName */
//    @Test
//    public void testFindByName() {
//        String typeName = "Hallo";
//    }

    /** TODO Test the read File and InputStream */
//    @Test
//    public void testRead() {
//        String typeName = "Hallo";
//    }

    /** TODO Test the loadDefault */
//    @Test
//    public void testLoadDefault() {
//        String typeName = "Hallo";
//    }

    /** Test the mimetypefile */
    @Test
    public void testMIMETYPESFILE() {
        assertEquals("mimetypes.xml", MIMETypes.MIME_TYPES_FILE); }

    /** Test the name getter */
    @Test
    public void testGetName() {
        assertEquals("MimeTypeConfig", MIMETypes.getName());
    }

}
