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

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

import javafx.collections.ObservableList;

import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Class to build a Autocomplete Combobox (Dropdown List).
 *
 * @param <T>
 */
public class AutoCompleteComboBoxListener<T> {

    private ComboBox<T> comboBox;
    private StringBuilder sb;
    private int lastLength;

    public AutoCompleteComboBoxListener(ComboBox<T> comboBox) {
        this.comboBox = comboBox;
        sb = new StringBuilder();

        this.comboBox.setEditable(true);
        this.comboBox.setOnKeyReleased(event -> {
            // this variable is used to bypass the auto complete process if
            // the length is the same. this occurs if user types fast, the
            // length of textfield will record after the user has typed after
            // a certain delay.
            if (lastLength != (comboBox.getEditor().getLength()
                    - comboBox.getEditor().getSelectedText().length())) {
                lastLength = comboBox.getEditor().getLength()
                        - comboBox.getEditor().getSelectedText().length();
            }

            if (event.isControlDown() || event.getCode()
                    == KeyCode.BACK_SPACE || event.getCode()
                    == KeyCode.RIGHT || event.getCode()
                    == KeyCode.LEFT || event.getCode()
                    == KeyCode.DELETE || event.getCode()
                    == KeyCode.HOME || event.getCode()
                    == KeyCode.END || event.getCode()
                    == KeyCode.TAB
                    ) {
                return;
            }

            if (event.getCode().equals(KeyCode.DOWN)) {
                comboBox.show();
                return;
            }

            IndexRange ir = comboBox.getEditor().getSelection();
            sb.delete(0, sb.length());
            sb.append(comboBox.getEditor().getText());
            // remove selected string index until end so only unselected text
            // will be recorded
            try {
                sb.delete(ir.getStart(), sb.length());
            } catch (Exception ignored) {
            }

            ObservableList<T> items = comboBox.getItems();
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i)
                        != null && comboBox.getEditor().getText()
                        != null && items.
                        get(i).toString().toLowerCase().startsWith(
                        comboBox.getEditor().getText().toLowerCase()
                )
                        ) {
                    try {
                        comboBox.getEditor().setText(sb.toString()
                                        + items.get(i).toString().substring(
                                sb.toString().length()
                                )
                        );
                        comboBox.setValue(items.get(i));
                        comboBox.getSelectionModel().select(i);
                    } catch (Exception e) {
                        comboBox.getEditor().setText(sb.toString());
                    }
                    comboBox.getEditor().positionCaret(sb.toString().length());
                    comboBox.getEditor().selectEnd();
                    break;
                }
            }
        });

        // add a focus listener such that if not in focus, reset the filtered
        // typed keys
        this.comboBox.getEditor().focusedProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue) {
                        lastLength = 0;
                        sb.delete(0, sb.length());
                        selectClosestResultBasedOnTextFieldValue(false, false);
                    }
                });

        this.comboBox.setOnMouseClicked(event ->
                selectClosestResultBasedOnTextFieldValue(true, true));
    }

    /**
     * selects the item and scrolls to it when the popup is shown.
     *
     * @param affect  true if combobox is clicked to show popup so text and
     *                caret position will be readjusted.
     * @param inFocus true if combobox has focus. If not, programmatically
     *                press enter key to add new entry to list.
     */
    private void selectClosestResultBasedOnTextFieldValue(boolean affect,
                                                          boolean inFocus) {
        ObservableList items = this.comboBox.getItems();

        boolean found = false;
        String selectedText = this.comboBox.getEditor().getText();

        if (selectedText != null) {
            for (int i = 0; i < items.size(); i++) {
                if (isItemTextEqualSelectedText(items, selectedText, i)) {
                    try {
                        comboBox.getSelectionModel().clearAndSelect(i);
                        comboBox.getSelectionModel().getSelectedIndex();
                        found = true;
                        break;
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        String s = comboBox.getEditor().getText();
        if (!found && affect) {
            comboBox.getSelectionModel().clearSelection();
            comboBox.getEditor().setText(s);
            comboBox.getEditor().end();
        }

        if (!found) {
            comboBox.getEditor().setText(null);
            comboBox.getSelectionModel().select(null);
            comboBox.setValue(null);
        }

        if (!inFocus
                && comboBox.getEditor().getText() != null
                && comboBox.getEditor().getText().trim().length() > 0) {
            // press enter key programmatically to have this entry added
            KeyEvent ke = new KeyEvent(KeyEvent.KEY_RELEASED,
                    KeyCode.ENTER.toString(),
                    KeyCode.ENTER.toString(),
                    KeyCode.ENTER,
                    false,
                    false,
                    false,
                    false);
            comboBox.fireEvent(ke);
        }
    }

    private boolean isItemTextEqualSelectedText(ObservableList item, String selectedText, int i) {
        return item.get(i) != null
                && selectedText.equalsIgnoreCase(item.get(i).toString());
    }

}
