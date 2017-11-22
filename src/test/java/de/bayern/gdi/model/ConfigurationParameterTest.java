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

import java.util.Collections;

/**
 * Class to test  ConfigurationParameter.
 */
public class ConfigurationParameterTest extends TestCase {

    /** Test the mandatory setter and state. */
    @Test
    public void testMandatory() {
        ConfigurationParameter confPara = new ConfigurationParameter();
        confPara.setMandatory(true);
        assertTrue(confPara.isMandatory());
    }

    /** Test the inputElement getter/setter. */
    @Test
    public void testInputElement() {
        ConfigurationParameter confPara = new ConfigurationParameter();
        confPara.setInputElement("a test String");
        assertEquals("a test String", confPara.getInputElement());
    }

    /** Test the glob getter/setter. */
    @Test
    public void testGlob() {
        ConfigurationParameter confPara = new ConfigurationParameter();
        confPara.setGlob("a test String");
        assertEquals("a test String", confPara.getGlob());
    }

    /** Test the ext getter/setter. */
    @Test
    public void testExt() {
        ConfigurationParameter confPara = new ConfigurationParameter();
        confPara.setExt("a test String");
        assertEquals("a test String", confPara.getExt());
    }

    /** Test the value getter/setter. */
    @Test
    public void testValue() {
        ConfigurationParameter confPara = new ConfigurationParameter();
        confPara.setValue("a test String");
        assertEquals("a test String", confPara.getValue());
    }

    /** Test the extractVariables. */
    @Test
    public void testExtractVariables() {
        ConfigurationParameter confPara1 = new ConfigurationParameter();
//        ConfigurationParameter confPara2 = new ConfigurationParameter();
        assertEquals(Collections.<String>emptyList()
            , confPara1.extractVariables());
    }

}
