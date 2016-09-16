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

package de.bayern.gdi.gui;

import javafx.scene.control.TextField;

/**
 * This class extends the JavaFX Textfield
 * with a NumberTextField.
 */
public class NumberTextField extends TextField {
    @Override
    public void replaceText(int start, int end, String text) {
        if (validate(getCharacters() + text)) {
            super.replaceText(start, end, text);
        }
    }

    @Override
    public void replaceSelection(String text) {
        if (validate(getCharacters() + text)) {
            super.replaceSelection(text);
        }
    }

    private boolean validate(String text) {
        return text.matches("^-?\\d*\\.?\\d*$");
    }
}
