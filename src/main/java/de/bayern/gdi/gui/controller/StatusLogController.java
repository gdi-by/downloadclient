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
package de.bayern.gdi.gui.controller;

import de.bayern.gdi.utils.I18n;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Named
@Singleton
public class StatusLogController {

    @FXML
    private Label logHistory;
    @FXML
    private TitledPane logHistoryParent;
    @FXML
    private ScrollPane logHistoryPanel;

    /** Initializes tehe status log controller. */
    @FXML
    protected void initialize() {
        logHistoryParent.expandedProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal,
                                Object newVal) {
                boolean val = (boolean) newVal;
                if (val) {
                    logHistoryParent.getTooltip().setText(
                        I18n.format("tooltip.log_history_expanded"));
                } else {
                    logHistoryParent.getTooltip().setText(
                        I18n.format("tooltip.log_history_hidden"));
                }
            }
        });
    }

    /**
     * Sets the style of the log history panel.
     * @param style
     */
    public void setLogHistoryStyle(String style) {
        logHistoryParent.setStyle(style);
    }

    /**
     * Set the text of the status bar in UI thread.
     * Adds current message to log history.
     *
     * @param msg the text to set.
     */
    public void setStatusTextUI(String msg) {
        String logText;
        String regexAtom = I18n.format("atom.bytes.downloaded",
            "[\\d|\\.|\\,]+");
        String regexWfs = I18n.format("file.download.bytes", "[\\d|\\.|\\,]+");
        //Filter atom/wfs download messages
        if (!logHistoryParent.getText().matches(regexAtom)
            && !logHistoryParent.getText().matches(regexWfs)) {
            logText = logHistoryParent.getText() + "\n"
                + logHistory.getText();
        } else {
            logText = logHistory.getText();
        }
        Controller.logToAppLog(msg);

        Platform.runLater(() -> {
            logHistoryParent.setText(msg);
            logHistory.setText(logText);
        });
    }

}
