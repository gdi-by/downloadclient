package de.bayern.gdi.gui;

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
