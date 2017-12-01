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

import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.AutoFileNames;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Test for AutoFileNames.
 */
public class AutoFileNamesTest extends TestCase {

    public AutoFileNamesTest(String testName) {
        super(testName);
    }

    /** setup the config. */
    @Before
    @Override
    public void setUp() throws IOException {
        Config.initialize(null);
    }

    private static final int HUNDRED = 100;

    /** test AutoFileNames. */
    @Test
    public void testAutoFileNames() {
        AutoFileNames afn = new AutoFileNames(HUNDRED);

        for (int i = 0; i < HUNDRED; i++) {
            String want = String.format("%02d.gif", i);
            String got = afn.nextFileName("image/gif");
            assertEquals(want, got);
        }
    }
}
