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

import org.geotools.geometry.DirectPosition2D;
import org.geotools.swing.dialog.JTextReporter;
import org.geotools.swing.dialog.TextReporterListener;
import org.geotools.swing.tool.InfoToolResult;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureInfoReporter {


    private static final int FLAGS = 6;
    private static final int TEXT_AREA_ROWS = 20;
    private static final int TEXT_AREA_COLS = 40;
    private static final int INDENT = 4;
    private static final int SEPERATOR_LENGTH = 10;

    private JTextReporter.Connection textReporterConnection;

    /**
     * Initialises the reporter.
     */
    public void createReporter() {
        if (this.textReporterConnection == null) {
            this.textReporterConnection = JTextReporter.showDialog(
                "Feature info", null, FLAGS, TEXT_AREA_ROWS, TEXT_AREA_COLS);
            this.textReporterConnection.addListener(new TextReporterListener() {
                @Override
                public void onReporterClosed() {
                    textReporterConnection = null;
                }

                @Override
                public void onReporterUpdated() {

                }
            });
        }
    }

    /**
     * Reports the passed information.
     *
     * @param layerName name of the layer
     * @param result    infor result
     */
    public void report(String layerName, InfoToolResult result) {
        this.textReporterConnection.append(layerName + "\n");
        this.textReporterConnection.append(result.toString(), INDENT);
        this.textReporterConnection.appendSeparatorLine(SEPERATOR_LENGTH, '-');
        this.textReporterConnection.appendNewline();
    }

    /**
     * Reports the passed information.
     *
     * @param pos position
     */
    public void report(DirectPosition2D pos) {
        this.textReporterConnection.append(
            String.format("Pos x=%.4f y=%.4f\n", pos.x, pos.y));
    }

}
