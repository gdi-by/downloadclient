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

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import de.bayern.gdi.services.Field;
import de.bayern.gdi.services.WFSMeta;
//import de.bayern.gdi.services.ServiceType;
import de.bayern.gdi.utils.I18n;

/**
 * @author Raimund Renkert (raimund.renkert@intevation.de)
 */
public class UIFactory {

    private static final double LABEL_MIN_WIDTH = 150f;
    private static final double TEXTFIELD_MIN_WIDTH = 170f;
    private static final double FONT_BIG = 14f;
    private static final int MARGIN_0 = 0;
    private static final int MARGIN_5 = 5;
    private static final int MARGIN_15 = 15;
    private static final int PREF_HEIGHT = 31;


    /**
     * Creates a stack pane with content based on the selected service.
     *
     * @param dataBean The data
     * @param type The selected service type
     * @param container The container node
     *
     */
    public void fillSimpleWFS(
        DataBean dataBean,
        VBox container,
        WFSMeta.StoredQuery type
    ) {
        createSimpleWFS(dataBean, type, container);
    }

    private void createSimpleWFS(
        DataBean dataBean,
        WFSMeta.StoredQuery type,
        VBox container
    ) {
        container.getChildren().clear();
        Label descriptionHead = new Label();
        descriptionHead.setText(I18n.getMsg("gui.description"));
        Font font = descriptionHead.getFont();
        descriptionHead.setFont(
            Font.font(font.getFamily(),
                FontWeight.BOLD,
                FONT_BIG));
        Label description = new Label();
        description.setWrapText(true);
        description.setText(type.abstractDescription);
        container.getChildren().add(descriptionHead);
        container.getChildren().add(description);
        container.setMargin(descriptionHead,
            new Insets(MARGIN_5, MARGIN_5, MARGIN_5, MARGIN_5));
        container.setMargin(description,
            new Insets(MARGIN_5, MARGIN_5, MARGIN_5, MARGIN_15));

        for (Field entry : type.parameters) {
            HBox attributeItem = createAttributeItem(entry);
            container.getChildren().add(attributeItem);
        }
    }

    private HBox createAttributeItem(Field field) {
        HBox root = new HBox();
        root.setPrefHeight(PREF_HEIGHT);
        root.setMaxHeight(PREF_HEIGHT);
        Label label = new Label();
        label.setText(field.name);
        label.setMinWidth(LABEL_MIN_WIDTH);
        TextField textField = new TextField();
        textField.setMinWidth(TEXTFIELD_MIN_WIDTH);
        Label type = new Label();
        type.setText(field.type.replace("xsd:", "").replace("xs:", ""));
        type.setMinWidth(LABEL_MIN_WIDTH);
        root.setMargin(label,
            new Insets(MARGIN_5, MARGIN_5, MARGIN_5, MARGIN_15));
        root.setMargin(textField,
            new Insets(MARGIN_0, MARGIN_5, MARGIN_0, MARGIN_5));
        root.setMargin(type,
            new Insets(MARGIN_5, MARGIN_5, MARGIN_5, MARGIN_5));
        root.getChildren().add(label);
        root.getChildren().add(textField);
        root.getChildren().add(type);
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
        root.setMargin(box, new Insets(MARGIN_5,
            MARGIN_5, MARGIN_5, MARGIN_5));
        root.setMargin(field, new Insets(MARGIN_5,
            MARGIN_5, MARGIN_5, MARGIN_5));
        root.setMargin(remove, new Insets(MARGIN_5,
            MARGIN_5, MARGIN_5, MARGIN_5));

        container.getChildren().add(root);
    }
}
