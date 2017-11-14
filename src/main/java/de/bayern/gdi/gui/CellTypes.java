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

import de.bayern.gdi.model.ProcessingStepConfiguration;

import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;

/**
 * A Class containing various cell types for combo boxes.
 * @author Alexander Woestmann (awoestmann@intevation.de)
 */
public final class CellTypes {

    private static final String FX_BOLD = "-fx-font-weight: bold;";
    private static final String FX_NORMAL = "-fx-font-weight: normal;";

    private CellTypes() {
    }

   /**
    * Cell class, changing its font color and weight
    * depending on its content.
    */
    public static class ItemCell extends ListCell<ItemModel> {
        public ItemCell() { }
        @Override
        protected void updateItem(ItemModel item,
                boolean empty) {
            super.updateItem(item, empty);
            setText(item == null ? "" : item.toString());
            if (item instanceof MiscItemModel) {
                setTextFill(Color.RED);
                setStyle(FX_BOLD);
            } else {
                setTextFill(Color.BLACK);
                setStyle(FX_NORMAL);
            }
        }
    }

   /**
    * Cell class, changing its font color and weight
    * depending whether the crs is available on the current WFS.
    */
    public static class CRSCell extends ListCell<CRSModel> {
        @Override
        protected void updateItem(CRSModel item,
                boolean empty) {
            super.updateItem(item, empty);
            setText(item == null ? "" : item.toString());
            if (item != null && !item.isAvailable()) {
                setTextFill(Color.RED);
                setStyle(FX_BOLD);
            } else {
                setTextFill(Color.BLACK);
                setStyle(FX_NORMAL);
            }
        }
    }

   /**
    * Cell class, changing its font color and weight
    * depending whether the output format is available on the current WFS.
    */
    public static class StringCell extends ListCell<OutputFormatModel> {
        @Override
        protected void updateItem(OutputFormatModel item,
                boolean empty) {
            super.updateItem(item, empty);
            if (item == null) {
                return;
            }
            if (!item.isAvailable()) {
                setTextFill(Color.RED);
                setStyle(FX_BOLD);

            } else {
                setTextFill(Color.BLACK);
                setStyle(FX_NORMAL);

            }
            setText(item.getItem());
        }
    }

   /**
    * Cell class, changing its font color and weight
    * depending whether the processing step configuration
    * is compatible to the given output format.
    */
    public static class ProcessCfgCell
            extends ListCell<ProcessingStepConfiguration> {
        @Override
        protected void updateItem(
                ProcessingStepConfiguration item,
                boolean empty) {
            super.updateItem(item, empty);
            if (item == null) {
                return;
            }
            if (!item.isCompatible()) {
                setTextFill(Color.RED);
                setStyle(FX_BOLD);
            } else {
                setTextFill(Color.BLACK);
                setStyle(FX_NORMAL);
            }

            setText(item.toString());
        }
    }
}
