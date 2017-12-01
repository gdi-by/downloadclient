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
 * Test for Option.
 */
public class OptionTest extends TestCase {

    /** Test the title getter/setter. */
    @Test
    public void testTitle() {
        Option option = new Option();
        option.setTitle("a title");
        assertEquals("a title", option.getTitle());
    }

    /** Test the value getter/setter. */
    @Test
    public void testValue() {
        Option option = new Option();
        option.setValue("a value");
        assertEquals("a value", option.getValue());
    }

    /** Test toString. */
    @Test
    public void testToString() {
        Option option = new Option();
        option.setTitle("a title");
        assertEquals("a title", option.toString());
    }

}
