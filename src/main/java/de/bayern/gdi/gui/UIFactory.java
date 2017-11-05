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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.bayern.gdi.model.ConfigurationParameter;
import de.bayern.gdi.model.InputElement;
import de.bayern.gdi.model.Option;
import de.bayern.gdi.model.ProcessingConfiguration;
import de.bayern.gdi.model.ProcessingStepConfiguration;
import de.bayern.gdi.services.Field;
import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.I18n;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;

/**
 * @author Raimund Renkert (raimund.renkert@intevation.de)
 */
public class UIFactory {

    private static final int LABEL_MIN_WIDTH = 150;
    private static final double TEXTFIELD_MIN_WIDTH = 170f;
    private static final double FONT_BIG = 14f;
    private static final int MARGIN_0 = 0;
    private static final int MARGIN_5 = 5;
    private static final int MARGIN_15 = 15;
    private static final int MARGIN_20 = 20;
    private static final int PREF_HEIGHT = 31;
    private static final int BGCOLOR = 244;
    private static final double FONTSCALE = 0.8;
    private static final int WEBVIEW_PREF_WIDTH = 200;
    private static final int WEBVIEW_MIN_WIDTH = 200;
    private static final int WEBVIEW_PREF_HEIGHT = 50;
    private static final int WEBVIEW_MIN_HEIGHT = 0;
    private static final int OUTPUTFORMAT_PREF_WIDTH = LABEL_MIN_WIDTH;
    private static final String DATAFORMAT_ID = "simpleDataformat";

    public static String getDataFormatID() {
        return DATAFORMAT_ID;
    }

    /**
     * Creates a stack pane with content based on the selected service.
     *
     * @param type The selected service type
     * @param container The container node
     * @param outputFormats the Outputformats of the selected service
     *
     */
    public void fillSimpleWFS(
        VBox container,
        WFSMeta.StoredQuery type,
        List <OutputFormatModel> outputFormats
    ) {
        createSimpleWFS(
            type,
            container,
            outputFormats);
    }

    private void createSimpleWFS(
        WFSMeta.StoredQuery type,
        VBox container,
        List <OutputFormatModel> outputFormats
    ) {
        container.getChildren().clear();
        Label descriptionHead = new Label();
        descriptionHead.setText(I18n.getMsg("gui.description"));
        Font font = descriptionHead.getFont();
        descriptionHead.setFont(
            Font.font(font.getFamily(),
                FontWeight.BOLD,
                FONT_BIG));
        WebView description = new WebView();
        description.setFontScale(FONTSCALE);
        description.setPrefHeight(WEBVIEW_PREF_HEIGHT);
        description.setPrefWidth(WEBVIEW_PREF_WIDTH);
        description.setMinHeight(WEBVIEW_MIN_HEIGHT);
        description.setMinWidth(WEBVIEW_MIN_WIDTH);
        WebEngine engine = description.getEngine();
        java.lang.reflect.Field f;
        try {
            f = engine.getClass().getDeclaredField("page");
            f.setAccessible(true);
            com.sun.webkit.WebPage page =
                (com.sun.webkit.WebPage) f.get(engine);
            page.setBackgroundColor(
                (new java.awt.Color(BGCOLOR, BGCOLOR, BGCOLOR)).getRGB());
        } catch (NoSuchFieldException
            | SecurityException
            | IllegalArgumentException
            | IllegalAccessException e) {
            // Displays the webview with white background...
        }
        engine.loadContent(type.abstractDescription);

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
        Separator sep = new Separator();
        HBox outputFormatBox = new HBox();
        Label outputFormatLabel = new Label();
        outputFormatLabel.setText(I18n.format("gui.data-format"));
        ComboBox outputFormat = new ComboBox();
        outputFormat.setId(DATAFORMAT_ID);
        ObservableList<OutputFormatModel> out =
                FXCollections.observableArrayList();
        out.addAll(outputFormats);
        outputFormat.setItems(out);
        outputFormatLabel.setLabelFor(outputFormat);
        outputFormatLabel.setPrefWidth(OUTPUTFORMAT_PREF_WIDTH);
        outputFormatBox.getChildren().add(outputFormatLabel);
        outputFormatBox.getChildren().add(outputFormat);
        outputFormatBox.setMargin(outputFormatLabel,
                new Insets(MARGIN_5, MARGIN_5, MARGIN_5, MARGIN_15));
        outputFormatBox.setMargin(outputFormatLabel,
                new Insets(MARGIN_5, MARGIN_5, MARGIN_5, MARGIN_15));
        container.getChildren().add(sep);
        container.getChildren().add(outputFormatBox);
    }

    private HBox createAttributeItem(Field field) {
        HBox root = new HBox();
        root.setPrefHeight(PREF_HEIGHT);
        root.setMaxHeight(PREF_HEIGHT);
        Label label = new Label();
        label.setText(field.name);
        label.setMinWidth(LABEL_MIN_WIDTH);
        TextField textField = new TextField();
        textField.setUserData(field.name);
        textField.setId("parameter");
        textField.setMinWidth(TEXTFIELD_MIN_WIDTH);
        Label type = new Label();
        //TODO - Save the type w/o the replace
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
     * @param container The container
     */
    public void addChainAttribute(VBox container) {
        addChainAttribute(container, null);
    }

    /**
     * Add new post process chain item.
     *
     * @param container The container
     * @param onChange A function called if the ComboBox value changed
     */
    public void addChainAttribute(VBox container, Runnable onChange) {
        VBox root = new VBox();
        VBox dynroot = new VBox();
        HBox subroot = new HBox();
        ComboBox box = new ComboBox();

        box.setCellFactory(new Callback <ListView<ProcessingStepConfiguration>,
                ListCell<ProcessingStepConfiguration>>() {
            @Override
            public ListCell<ProcessingStepConfiguration> call(
                    ListView<ProcessingStepConfiguration> list) {
                return new CellTypes.ProcessCfgCell();
            }
        });

        ProcessingConfiguration config =
            Config.getInstance().getProcessingConfig();
        List<ProcessingStepConfiguration> steps = config.getProcessingSteps();
        ObservableList<ProcessingStepConfiguration> conf =
            FXCollections.observableArrayList(steps);
        box.setItems(conf);
        box.setId("process_name");
        box.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                generateChainItem(
                    (ProcessingStepConfiguration)box.getValue(),
                    dynroot,
                    config);
                if (onChange != null) {
                    onChange.run();
                }
            }
        });
        Button remove = new Button(I18n.getMsg("gui.remove"));
        remove.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                ObservableList<Node> items = container.getChildren();
                items.remove(root);
            }
        });
        subroot.getChildren().addAll(box, remove);
        subroot.setMargin(box, new Insets(MARGIN_5,
            MARGIN_5, MARGIN_5, MARGIN_20));
        subroot.setMargin(remove, new Insets(MARGIN_5,
            MARGIN_5, MARGIN_5, MARGIN_5));
        Separator sep = new Separator();
        root.getChildren().addAll(subroot, dynroot, sep);
        root.setId("process_parameter");

        container.getChildren().add(root);
    }

    /**
     * Add new post process chain item with pre-selected process name
     * and parameters.
     *
     * @param container The container
     * @param processName The process' name
     * @param params The parameters
     */
    public void addChainAttribute(VBox container,
            String processName, Map<String, String> params) {
        VBox root = new VBox();
        VBox dynroot = new VBox();
        HBox subroot = new HBox();
        ComboBox<ProcessingStepConfiguration> box = new ComboBox<>();
        ProcessingConfiguration config =
            Config.getInstance().getProcessingConfig();
        List<ProcessingStepConfiguration> steps = config.getProcessingSteps();
        ObservableList<ProcessingStepConfiguration> conf =
            FXCollections.observableArrayList(steps);
        box.setItems(conf);
        box.setId("process_name");
        box.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                generateChainItem(
                    (ProcessingStepConfiguration)box.getValue(),
                    dynroot,
                    config);
            }
        });
        Button remove = new Button(I18n.getMsg("gui.remove"));
        remove.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                ObservableList<Node> items = container.getChildren();
                items.remove(root);
            }
        });
        subroot.getChildren().addAll(box, remove);
        subroot.setMargin(box, new Insets(MARGIN_5,
            MARGIN_5, MARGIN_5, MARGIN_20));
        subroot.setMargin(remove, new Insets(MARGIN_5,
            MARGIN_5, MARGIN_5, MARGIN_5));
        Separator sep = new Separator();
        root.getChildren().addAll(subroot, dynroot, sep);
        root.setId("process_parameter");

        for (ProcessingStepConfiguration cfg : box.getItems()) {
            if (cfg.getName().equals(processName)) {
                box.getSelectionModel().select(cfg);
            }
        }
        generateChainItem(
                (ProcessingStepConfiguration)box.getValue(),
                dynroot,
                config);

        for (Node node: dynroot.getChildren()) {
            HBox n = (HBox) node;
            ComboBox<Option> cb = (ComboBox<Option>) n.getChildren().get(1);
            Label paramLabel = (Label) n.getChildren().get(0);
            String targetvalue = params.get(paramLabel.getText());
            for (Option o: cb.getItems()) {
                if (o.getValue().equals(targetvalue)) {
                    cb.getSelectionModel().select(o);
                }
            }
        }
        container.getChildren().add(root);
    }


    /**
     * removes all Processing attributes.
     * @param container the container they should be removed from
     */
    public void removeAllChainAttributes(VBox container) {
        Collection<Node> nodeColl = new ArrayList<>();
        for (Node node : container.getChildren()) {
            if (node.getId().equals("process_parameter")) {
                nodeColl.add(node);
            }
        }
        container.getChildren().removeAll(nodeColl);
    }

    private void generateChainItem(
        ProcessingStepConfiguration item,
        VBox container,
        ProcessingConfiguration config
    ) {
        container.getChildren().clear();
        List<ConfigurationParameter> parameters = item.getParameters();
        for (ConfigurationParameter p : parameters) {
            if (p.getExt() != null || p.getGlob() != null) {
                // Ignore automatic parameters.
                continue;
            }
            if (p.getInputElement() != null) {
                String ie = p.getInputElement();
                List<InputElement> inputs = config.getInputElements();
                for (InputElement input : inputs) {
                    HBox root = new HBox();
                    List<String> var = p.extractVariables();
                    Label label = new Label();
                    label.setText(var.get(0));
                    label.setMinWidth(LABEL_MIN_WIDTH);
                    if (input.getName().equals(ie)
                        && "ComboBox".equals(input.getType())) {
                        ComboBox inputEl = new ComboBox();
                        inputEl.setUserData(var.get(0));
                        inputEl.setId("process_var");
                        List<Option> opts = input.getOptions();
                        inputEl.setItems(
                            FXCollections.observableArrayList(opts));
                        root.getChildren().add(label);
                        root.getChildren().add(inputEl);
                        root.setPrefHeight(PREF_HEIGHT);
                        root.setMaxHeight(PREF_HEIGHT);
                        root.setMargin(label, new Insets(MARGIN_5,
                            MARGIN_5, MARGIN_5, MARGIN_15));
                        container.getChildren().add(root);
                        container.setMargin(root, new Insets(MARGIN_5,
                            MARGIN_5, MARGIN_5, MARGIN_5));
                        break;
                    }
                    if (input.getName().equals(ie)
                        && "TextField".equals(input.getType())) {
                        TextField inputEl = new TextField();
                        inputEl.setUserData(var.get(0));
                        inputEl.setId("process_var");
                        root.getChildren().add(label);
                        root.getChildren().add(inputEl);
                        root.setPrefHeight(PREF_HEIGHT);
                        root.setMaxHeight(PREF_HEIGHT);
                        root.setMargin(label, new Insets(MARGIN_5,
                            MARGIN_5, MARGIN_5, MARGIN_15));
                        container.getChildren().add(root);
                        container.setMargin(root, new Insets(MARGIN_5,
                            MARGIN_5, MARGIN_5, MARGIN_5));
                        break;
                    }
                }
            } else {
                List<String> vars = p.extractVariables();
                for (String var : vars) {
                    HBox root = new HBox();
                    root.setPrefHeight(PREF_HEIGHT);
                    root.setMaxHeight(PREF_HEIGHT);
                    Label label = new Label();
                    label.setText(var);
                    label.setMinWidth(LABEL_MIN_WIDTH);
                    TextField textField = new TextField();
                    textField.setUserData(var);
                    textField.setId("process_var");
                    textField.setMinWidth(TEXTFIELD_MIN_WIDTH);
                    root.getChildren().addAll(label, textField);
                    root.setMargin(label, new Insets(MARGIN_5,
                        MARGIN_5, MARGIN_5, MARGIN_15));
                    container.getChildren().add(root);
                    container.setMargin(root, new Insets(MARGIN_5,
                        MARGIN_5, MARGIN_5, MARGIN_5));
                }
            }
        }
    }
}
