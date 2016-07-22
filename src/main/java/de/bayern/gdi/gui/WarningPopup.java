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

import de.bayern.gdi.utils.DocumentResponseHandler;
import de.bayern.gdi.utils.I18n;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class WarningPopup extends Alert
        implements DocumentResponseHandler.Unauthorized {

    /**
     * Constructor.
     */
    public WarningPopup() {
        this(AlertType.ERROR);
    }

    private WarningPopup(@NamedArg("alertType") AlertType alertType) {
        super(alertType);
    }

    /**
     * Displays Poput Windows when the creds are wrong.
     */
    public void unauthorized() {
        Platform.runLater(() -> {
            popup(I18n.getMsg("gui.wrong.user.and.pw"));
        });
    }
    /**
     * Opens the Popup-Window with the given text.
     * @param text the text
     */
    public void popup(String text) {
        setTitle(I18n.getMsg("gui.failure"));
        setContentText(text);
        ButtonType confirm = new ButtonType(I18n.getMsg("gui.ok"),
                ButtonBar.ButtonData.CANCEL_CLOSE);
        getButtonTypes().setAll(confirm);
        Optional<ButtonType> res = showAndWait();
    }
}
