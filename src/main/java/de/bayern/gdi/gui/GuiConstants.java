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

/**
 * GUI constants.
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public final class GuiConstants {

    private GuiConstants() {
    }
    /** user directory. */
    public static final String USER_DIR = "user.dir";
    /** ready status. */
    public static final String STATUS_READY = "status.ready";
    /** output format. */
    public static final String OUTPUTFORMAT = "outputformat";
    /** border color null. */
    public static final String FX_BORDER_COLOR_NULL = "-fx-border-color: null;";
    /** border color red. */
    public static final String FX_BORDER_COLOR_RED = "-fx-border-color: red;";
    /** GUI process no format bundle key. */
    public static final String GUI_PROCESS_NO_FORMAT = "gui.process.no.format";
    /** GUI process format not found bundle key. */
    public static final String GUI_PROCESS_FORMAT_NOT_FOUND = "gui.process.format.not.found";
    /** GUI process not compatible bundle key. */
    public static final String GUI_PROCESS_NOT_COMPATIBLE = "gui.process.not.compatible";
    /** GUI no format selected bundle key. */
    public static final String GUI_FORMAT_NOT_SELECTED = "gui.no-format-selected";
    /** GUI background color. */
    public static final int BGCOLOR = 244;
    /** default CRS. */
    public static final String EPSG4326 = "EPSG:4326";
    /** initial CRS. */
    public static final String INITIAL_CRS_DISPLAY = EPSG4326;
    /** default CRS for Atom. */
    public static final String ATOM_CRS_STRING = EPSG4326;
    /** max size of logging buffer. */
    public static final int MAX_APP_LOG_BYTES = 8096;
    /** bbox index x1. */
    public static final int BBOX_X1_INDEX = 0;
    /** bbox index y1. */
    public static final int BBOX_Y1_INDEX = 1;
    /** bbox index x2. */
    public static final int BBOX_X2_INDEX = 2;
    /** bbox index y2. */
    public static final int BBOX_Y2_INDEX = 3;

}
