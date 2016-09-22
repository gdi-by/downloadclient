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

import de.bayern.gdi.utils.I18n;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class WebViewWindow extends Dialog {

    private WebView webView;
    private static final Logger log
            = Logger.getLogger(WebViewWindow.class.getName());

    public WebViewWindow(WebView wv, String title) {
        super();
        this.webView = wv;
        this.webView.getEngine().setCreatePopupHandler(param -> {
            Object o = webView
                    .getEngine()
                    .executeScript(
                         "var list = document.querySelectorAll( ':hover' );"
                         + "for (i=list.length-1; i>-1; i--) "
                         + "{ if ( list.item(i).getAttribute('href') ) "
                         + "{ list.item(i).getAttribute('href'); break; } }");
            if (o != null) {
                try {
                    new ProcessBuilder("x-www-browser", o.toString()).start();
                } catch (IOException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            return null;
        });
        super.setTitle(title);
    }

    public void popup() {
        ButtonType confirm = new ButtonType(I18n.getMsg("gui.ok"),
                ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(confirm);
        getDialogPane().setContent(webView);
        Optional<ButtonType> res = showAndWait();
    }

}
