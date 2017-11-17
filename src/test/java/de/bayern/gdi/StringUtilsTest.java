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

import de.bayern.gdi.utils.StringUtils;

public class StringUtilsTest extends TestCase {

    public StringUtilsTest(String testName) {
        super(testName);
    }

    private static final String [] PREFIXES = {
        "b:",
        "d:"
    };

    /** Test StringUtils.ignorePartsWithPrefix. */
    @Test
    public void testIgnorePartsWithPrefix() {

        String in = "a:a1 b:Ignored c:c1 d:Ignored a:a2 c:c2";
        String want = "a:a1 c:c1 a:a2 c:c2";

        String out = StringUtils.ignorePartsWithPrefix(in, " ", PREFIXES);

        assertEquals(want, out);
    }
}
