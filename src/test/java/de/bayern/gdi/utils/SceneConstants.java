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
package de.bayern.gdi.utils;

/**
 * Contains element constants from the scene.
 * @author thomas
 */
public class SceneConstants {

    /**
     * Constructor.
     */
    private SceneConstants() {
    }

    // DIMENSIONS

    /**
     * Height of the scene.
     */
    public static final int HEIGHT = 768;

    /**
     * Width of the scene.
     */
    public static final int WIDTH = 1024;

    //ELEMENTS

    /**
     * ACTIVATE_FURTHER_PROCESSING.
     */
    public static final String ACTIVATE_FURTHER_PROCESSING =
        "#chkChain";

    /**
     * ADD_PROCESSING_STEP.
     */
    public static final String ADD_PROCESSING_STEP =
        "#addChainItem";

    /**
     * Auth.
     */
    public static final String AUTH =
        "#serviceAuthenticationCbx";

    /**
     * Close Button.
     */
    public static final String CLOSE_BUTTON = "#buttonClose";

    /**
     * DataFormatChooser Combobox.
     */
    public static final String DATAFORMATCHOOSER =
        "#dataFormatChooser";

    /**
     * Download Button.
     */
    public static final String DOWNLOAD_BUTTON = "#buttonDownload";

    /**
     * Titlepane.
     */
    public static final String HISTORY_PARENT =
        "#logHistoryParent";

    /**
     * Password field.
     */
    public static final String PASSWORD =
        "#servicePW";

    /**
     * Processing steps Containerelement.
     */
    public static final String PROCESSINGSTEPS =
        "#processStepContainter";

    /**
     * ProcessStep Combobox.
     */
    public static final String PROCESS_SELECTION =
        "#process_name";

    /**
     * Save Button.
     */
    public static final String SAVE_BUTTON = "#buttonSaveConfig";

    /**
     * Search field.
     */
    public static final String SEARCH =
        "#searchField";

    /**
     * The list of services.
     */
    public static final String SERVICE_LIST =
        "#serviceList";

    /**
     * Selection field.
     */
    public static final String SERVICE_SELECTION =
        "#serviceSelection";

    /**
     * Selection of service kinds.
     */
    public static final String SERVICE_TYPE_CHOOSER =
        "#serviceTypeChooser";

    /**
     * ServiceURL element.
     */
    public static final String URL =
        "#serviceURL";

    /**
     * Username field.
     */
    public static final String USERNAME =
        "#serviceUser";

    /**
     * CQL input field.
     */
    public static final String CQL_INPUT =
        "#sqlTextarea";

    // MESSAGES

    /**
     * Calling the service msg.
     */
    public static final String CALLING_SERVICE =
        "status.calling-service";

    /**
     * No format chosen.
     */
    public static final String NO_FORMAT_CHOSEN =
        "gui.process.no.format";

    /**
     * No URL.
     */
    public static final String NO_URL =
        "status.no-url";

    /**
     * Protected state.
     */
    public static final String PROTECTED_STATE =
        "status.service-needs-auth";

    /**
     * Ready state.
     */
    public static final String READY_STATUS =
        "status.ready";
}
