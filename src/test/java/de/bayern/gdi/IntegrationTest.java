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

import static org.junit.Assert.assertTrue;

/**
 * @author thomas
 */
public class IntegrationTest extends TestBase {

    /**
     * The processingChainValidationTest.
     *
     * @throws Exception if something went wrong.
     */
    @Test
    public void initialState() throws Exception {
        waitUntilReady();
        assertTrue(titlePaneShows(READY_STATUS));
        assertTrue(isEmpty(SEARCH));
        assertTrue(isEmpty(URL));
        assertTrue(isEmpty(USERNAME));
        assertTrue(isEmpty(PASSWORD));
        assertTrue(isEmpty(LIST_OF_SERVICES));
    }


}
