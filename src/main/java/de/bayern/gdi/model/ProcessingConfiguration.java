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

import java.util.ArrayList;
import java.util.List;

/** 'Verarbeitungskonfiguration' of processing step configuration. */
public class ProcessingConfiguration {

    private List<ProcessingFormat> formats;
    private List<InputElement> inputElements;

    public ProcessingConfiguration() {
        this.formats = new ArrayList<>();
        this.inputElements = new ArrayList<>();
    }

    /**
     * @return the formats
     */
    public List<ProcessingFormat> getFormats() {
        return formats;
    }

    /**
     * @param formats the formats to set
     */
    public void setFormats(List<ProcessingFormat> formats) {
        this.formats = formats;
    }

    /**
     * @return the inputElements
     */
    public List<InputElement> getInputElements() {
        return inputElements;
    }

    /**
     * @param inputElements the inputElements to set
     */
    public void setInputElements(List<InputElement> inputElements) {
        this.inputElements = inputElements;
    }
}
