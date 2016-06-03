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

import java.util.Map;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;

//import de.bayern.gdi.services.ServiceType;
import de.bayern.gdi.utils.I18n;

/**
 * @author Raimund Renkert (raimund.renkert@intevation.de)
 */
public class UIFactory {

    private static final double LABEL_MIN_WIDTH = 150f;
    private static final double TEXTFIELD_MIN_WIDTH = 170f;
    private static final double FONT_BIG = 14f;
    private static final int MARGIN_FIVE = 5;
    private static final int MARGIN_15 = 15;


    /**
     * Creates a stack pane with content based on the selected service.
     *
     * @param dataBean The data
     * @param type The selected service type
     * @param container The container node
     *
     */
    public void fillAtom(
        DataBean dataBean,
        VBox container,
        String type
    ) {
    }

    private void createSimpleWFS(
        DataBean dataBean,
        String type,
        VBox container
    ) {
        container.getChildren().clear();
        Label descriptionHead = new Label();
        descriptionHead.setText(I18n.getMsg("description"));
        Label description = new Label();
        Font font = description.getFont();
        description.setFont(
            Font.font(font.getFamily(),
                FontWeight.BOLD,
                FONT_BIG));
        description.setWrapText(true);
        description.setText("some example text. real descrption goes here.");
        container.getChildren().add(descriptionHead);
        container.getChildren().add(description);

/*        Map<String, String> attributes =
            dataBean.getWebService().getAttributes(type);
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            HBox attributeItem = createAttributeItem(entry);
            container.getChildren().add(attributeItem);
        }
        */
    }

    private HBox createAttributeItem(Map.Entry<String, String> item) {
        HBox root = new HBox();
        Label label = new Label();
        label.setText(item.getKey());
        label.setMinWidth(LABEL_MIN_WIDTH);
        TextField textField = new TextField();
        textField.setText(item.getValue());
        textField.setMinWidth(TEXTFIELD_MIN_WIDTH);
        root.getChildren().add(label);
        root.getChildren().add(textField);
        return root;
    }

    /**
     * Add new post process chain item.
     *
     * @param dataBean The databean
     * @param container The container
     */
    public void addChainAttribute(DataBean dataBean, VBox container) {
        HBox root = new HBox();
        ComboBox box = new ComboBox();
        // TODO add elements to box.
        TextField field = new TextField();
        Button remove = new Button(I18n.getMsg("gui.remove"));
        remove.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                ObservableList<Node> items = container.getChildren();
                items.remove(root);
            }
        });
        root.getChildren().addAll(box, field, remove);
        root.setMargin(box, new Insets(MARGIN_FIVE,
            MARGIN_FIVE, MARGIN_FIVE, MARGIN_FIVE));
        root.setMargin(field, new Insets(MARGIN_FIVE,
            MARGIN_FIVE, MARGIN_FIVE, MARGIN_FIVE));
        root.setMargin(remove, new Insets(MARGIN_FIVE,
            MARGIN_FIVE, MARGIN_FIVE, MARGIN_FIVE));

        container.getChildren().add(root);
    }
}
