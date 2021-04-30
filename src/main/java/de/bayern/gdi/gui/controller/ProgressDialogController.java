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

import de.bayern.gdi.processor.JobExecutionException;
import de.bayern.gdi.utils.I18n;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;


/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ProgressDialogController {

    @FXML
    private Text downloadUrlLabel;

    @FXML
    private Text downloadUrl;

    @FXML
    private Text status;

    @FXML
    private Text statusDetails;

    private Controller controller;

    /**
     * Initialize ProgressDialogController with the Controller.
     *
     * @param mainController never <code>null</code>
     */
    public void init(Controller mainController) {
        this.controller = mainController;
        downloadUrl.setText(mainController.dataBean.getSelectedService().getServiceURL().toString());
        status.setText(I18n.getMsg("progressdialog.download.status.started"));

    }

    /**
     * Sets the bytes counted text.
     *
     * @param count download bytes to set as text.
     */
    public void setBytesCountedText(long count) {
        statusDetails.setText(I18n.format("file.download.bytes", count));
    }

    /**
     * Shows the status in the progress dialog.
     */
    public void showDownloadFinished() {
        status.setText(I18n.getMsg("progressdialog.download.status.finished"));
        downloadUrl.setVisible( false );
        downloadUrlLabel.setVisible( false );
    }

    /**
     * Shows the exception in the progress dialog.
     *
     * @param exception never <code>null</code>
     */
    public void showDownloadFailed(JobExecutionException exception) {
        status.setFill(Color.RED);
        statusDetails.setFill(Color.RED);
        status.setText(I18n.getMsg("progressdialog.download.status.failed"));
        statusDetails.setText(exception.getMessage());
        downloadUrl.setVisible( false );
        downloadUrlLabel.setVisible( false );
    }
}
