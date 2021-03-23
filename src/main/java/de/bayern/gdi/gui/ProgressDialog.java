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

import de.bayern.gdi.gui.controller.Controller;
import de.bayern.gdi.gui.controller.ProgressDialogController;
import de.bayern.gdi.processor.listener.CountListener;
import de.bayern.gdi.utils.I18n;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

import java.io.IOException;

public class ProgressDialog extends Dialog<ButtonType> implements CountListener {

    private ProgressDialogController progressDialogController;

    public ProgressDialog(Controller controller) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/progress-dialog.fxml"), I18n.getBundle());
            DialogPane dialogPane = fxmlLoader.load();
            dialogPane.getButtonTypes().add(ButtonType.CANCEL);
            this.progressDialogController = fxmlLoader.getController();
            this.progressDialogController.init(controller);
            setDialogPane(dialogPane);
            setTitle(I18n.getMsg("progressdialog.title"));
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize ProgressDialog.", e);
        }
    }

    @Override
    public void bytesCounted(long counter) {
        progressDialogController.setBytesCountedText(counter);
    }
}
