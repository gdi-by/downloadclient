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
 * Unit Tests for ProcessingStep.
 */
public class ProcessingStepTest extends TestCase {

    /** Test the name getter/setter. */
    @Test
    public void testName() {
        ProcessingStep procStep = new ProcessingStep();
        procStep.setName("a name");
        assertEquals("a name", procStep.getName());
    }

    /** Test the parameters getter/setter. */
    @Test
    public void testParameters() {
        ProcessingStep procStep = new ProcessingStep();
        ArrayList<Parameter> listOfParameters = createListOfParameters();

        procStep.setParameters(listOfParameters);
        assertEquals(listOfParameters, procStep.getParameters());
    }


    /** Test the findParameter. */
    @Test
    public void testFindParameter() {
        ProcessingStep procStep = new ProcessingStep();
        ArrayList<Parameter> listOfParameters = createListOfParameters();
        String key = "an unknown key";

        procStep.setParameters(listOfParameters);
        assertNull(procStep.findParameter(key));

        key = "key 1";
        assertEquals("value 1", procStep.findParameter(key));
    }

    /** Test the toString. */
    @Test
    public void testToString() {
        Parameter parameter = createParameter();
        ArrayList<Parameter> listOfParameters = new ArrayList<Parameter>();
        listOfParameters.add(parameter);
        ProcessingStep procStep = new ProcessingStep();
        procStep.setName("ProcessStep name1");
        procStep.setParameters(listOfParameters);
        assertEquals("[processing step: name = \"" + procStep.getName()
            + "\"  parameters: [[Parameter: key = \"" + parameter.getKey()
            + "\" value = \"" + parameter.getValue() + "\"]]"
            , procStep.toString());

        procStep.setName("ProcessStep name2");
        ArrayList<Parameter> listOfParameters1 = createListOfParameters();
        procStep.setParameters(listOfParameters1);
        String parameterString = "[Parameter: key = \"";
        assertEquals("[processing step: name = \"" + procStep.getName()
                + "\"  parameters: [" + parameterString
                + "key 1\" value = \"value 1\"], " + parameterString
                + "key 2\" value = \"value 2\"]]"

            , procStep.toString());
    }

    private Parameter createParameter() {
        Parameter parameter = new Parameter();
        parameter.setKey("key 1");
        parameter.setValue("value 1");
        return parameter;
    }

    /** Creates a list of Parameters. */
    private ArrayList<Parameter> createListOfParameters() {
        Parameter para1 = createParameter();
        Parameter para2 = new Parameter();
        para2.setKey("key 2");
        para2.setValue("value 2");
        ArrayList<Parameter> listOfParameters = new ArrayList<Parameter>();
        listOfParameters.add(para1);
        listOfParameters.add(para2);
        return listOfParameters;
    }

}
