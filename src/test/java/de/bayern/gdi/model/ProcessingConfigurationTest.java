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
 * Tests for Processing the configuration.
 */
public class ProcessingConfigurationTest extends TestCase {

    /** Test the findProcessingStepConfiguration. */
    @Test
    public void testFindProcessingStepConfiguration() {
        ProcessingStepConfiguration psc = new ProcessingStepConfiguration();
        ProcessingConfiguration procConf = new ProcessingConfiguration();
        String name = null;

        assertEquals(name, procConf.findProcessingStepConfiguration(name));

        name = "name";
        assertEquals(psc.getName()
            , procConf.findProcessingStepConfiguration(name));
    }

    /** Test the FilterStepsByType. */
    @Test
    public void testFilterStepsByType() {
        ProcessingConfiguration procConf = new ProcessingConfiguration();
        ArrayList<ProcessingStepConfiguration> emptyList = new ArrayList<>();
        ArrayList<ProcessingStepConfiguration> steps = new ArrayList<>();

        procConf.setProcessingSteps(steps);
        String type = "unknown";
        assertEquals(emptyList, procConf.filterStepsByType(type));
    }

    /** Test the inputElements getter/setter. */
    @Test
    public void testInputElements() {
        InputElement inputElemt1 = new InputElement();
        InputElement inputElemt2 = new InputElement();
        ArrayList<InputElement> listOfInputelements = new ArrayList<>();
        listOfInputelements.add(inputElemt1);
        listOfInputelements.add(inputElemt2);

        ProcessingConfiguration procConf = new ProcessingConfiguration();
        procConf.setInputElements(listOfInputelements);

        assertEquals(listOfInputelements, procConf.getInputElements());
    }

    /** Test the ProcessingSteps getter/setter. */
    @Test
    public void testProcessingSteps() {
        ProcessingStepConfiguration psc1 = new ProcessingStepConfiguration();
        ProcessingStepConfiguration psc2 = new ProcessingStepConfiguration();
        ArrayList<ProcessingStepConfiguration> listOfPSC = new ArrayList<>();
        listOfPSC.add(psc1);
        listOfPSC.add(psc2);

        ProcessingConfiguration procConf = new ProcessingConfiguration();
        procConf.setProcessingSteps(listOfPSC);

        assertEquals(listOfPSC, procConf.getProcessingSteps());

    }

//    /** TODO: Test the read with InputStream and File */
//    @Test
//    public void read() {
//    }

    /** Test the loadDefault. */
    @Test
    public void testLoadDefault() {
        assertNotNull(ProcessingConfiguration.loadDefault());
    }

    /** Test the name getter. */
    @Test
    public void testName() {
        assertEquals("ProcessingConfig", ProcessingConfiguration.getName());
    }

}
