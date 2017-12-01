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

import java.util.ArrayList;

/**
 * Test for ProcessingStepConfiguration.
 */
public class ProcessingStepConfigurationTest extends TestCase {

    /** Test the format getter/setter. */
    @Test
    public void testFormatType() {
        ProcessingStepConfiguration procStepConf
            = new ProcessingStepConfiguration();
        procStepConf.setFormatType("a format type");
        assertEquals("a format type", procStepConf.getFormatType());
    }

    /** Test the title getter/setter. */
    @Test
    public void testTitle() {
        ProcessingStepConfiguration procStepConf
            = new ProcessingStepConfiguration();
        procStepConf.setTitle("a title");
        assertEquals("a title", procStepConf.getTitle());
    }

    /** Test the description getter/setter. */
    @Test
    public void testDescription() {
        ProcessingStepConfiguration procStepConf
            = new ProcessingStepConfiguration();
        procStepConf.setDescription("a description");
        assertEquals("a description", procStepConf.getDescription());
    }

    /** Test the command getter/setter. */
    @Test
    public void testCommand() {
        ProcessingStepConfiguration procStepConf
            = new ProcessingStepConfiguration();
        procStepConf.setCommand("a command");
        assertEquals("a command", procStepConf.getCommand());
    }

    /** Test the parameter getter/setter. */
    @Test
    public void testParameters() {
        ProcessingStepConfiguration procStepConf
            = new ProcessingStepConfiguration();

        ArrayList<ConfigurationParameter> cp
            = new ArrayList<ConfigurationParameter>();

        ArrayList<ConfigurationParameter> emptyList
            = new ArrayList<ConfigurationParameter>();

        procStepConf.setParameters(cp);
        assertEquals(emptyList, procStepConf.getParameters());
    }

}
