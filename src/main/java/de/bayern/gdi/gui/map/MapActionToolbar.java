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
package de.bayern.gdi.gui.map;

import de.bayern.gdi.utils.I18n;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class MapActionToolbar {

    private ToggleButton bboxButton;

    private ToggleButton selectButton;

    private ToggleButton infoButton;

    private Button resizeButton;

    /**
     * @param selectBt button used to select features,
     *                     never <code>null</code>
     */
    public void setSelectButtton(ToggleButton selectBt) {
        this.selectButton = selectBt;
        ImageView selectIcon = new ImageView(
            "/org/geotools/swing/icons/pointer.png");
        Tooltip selectTooltip = new Tooltip(I18n.format("tooltip.select"));
        this.selectButton.setGraphic(selectIcon);
        this.selectButton.setTooltip(selectTooltip);
    }

    /**
     * @param bboxBt button used to draw a rectangle on the map,
     *                   never <code>null</code>
     */
    public void setBboxButton(ToggleButton bboxBt) {
        this.bboxButton = bboxBt;
        ImageView bboxIcon = new ImageView(
            "/img/drawbbox.png");
        Tooltip bboxTooltip = new Tooltip(I18n.format("tooltip.bbox"));
        this.bboxButton.setGraphic(bboxIcon);
        this.bboxButton.setTooltip(bboxTooltip);
    }

    /**
     * @param infoBt button used to get feature info,
     *                   never <code>null</code>
     */
    public void setInfoButton(ToggleButton infoBt) {
        this.infoButton = infoBt;
        ImageView infoIcon = new ImageView(
            "/org/geotools/swing/icons/mActionIdentify.png");
        Tooltip infoTooltip = new Tooltip(I18n.format("tooltip.info"));
        this.infoButton.setGraphic(infoIcon);
        this.infoButton.setTooltip(infoTooltip);
    }

    /**
     * @param resizeBt button used to resize the map,
     *                     never <code>null</code>
     */
    public void setResizeButtton(Button resizeBt) {
        this.resizeButton = resizeBt;
        ImageView resizeIcon = new ImageView(
            "/org/geotools/swing/icons/mActionZoomFullExtent.png");
        Tooltip resizeTooltip = new Tooltip(I18n.format("tooltip.resize"));
        this.resizeButton.setGraphic(resizeIcon);
        this.resizeButton.setTooltip(resizeTooltip);
    }

    /**
     * @param mapHandler to register resize button, never <code>null</code>
     */
    public void registerResizeHandler(MapHandler mapHandler) {
        mapHandler.registerResizeAction(this.resizeButton);
    }

    /**
     * Check if select button is selected.
     *
     * @return <code>true</code> if select button is selected,
     * <code>false</code> otherwise.
     */
    public boolean isSelectButtonSelected() {
        if (selectButton != null) {
            return selectButton.isSelected();
        }
        return false;
    }

    /**
     * Check if bbox button is selected.
     *
     * @return <code>true</code> if bbox button is selected,
     * <code>false</code> otherwise.
     */
    public boolean isBboxButtonSelected() {
        if (bboxButton != null) {
            return bboxButton.isSelected();
        }
        return false;
    }

    /**
     * Check if info button is selected.
     *
     * @return <code>true</code> if info button is selected,
     * <code>false</code> otherwise.
     */
    public boolean isInfoButtonSelected() {
        if (infoButton != null) {
            return infoButton.isSelected();
        }
        return false;
    }
}
