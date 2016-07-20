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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A poor mans logging class.
 */
public class Log {

    private static final SimpleDateFormat DF_FORMAT
        = new SimpleDateFormat("yyyy-MM-dd-HH-mm:ss");

    private File file;
    private PrintWriter out;

    /**
     * @param The writer to log to.
     */
    public Log(File file) {
        this.file = file;
    }

    /**
     * Opens the underlaying log file for writing.
     * @throws IOException opening the underlaying file failed.
     */
    public synchronized void open() throws IOException {
        if (this.out != null) {
            this.out.close();
            this.out = new PrintWriter(new FileWriter(this.file), true);
        }
    }

    /**
     * @param msg The message to log.
     */
    public synchronized void log(String msg) {
        if (this.out != null) {
            Date now = new Date();
            out.format("%s: %s", DF_FORMAT.format(now), msg);
        }
    }

    /**
     * Closes this logger.
     */
    public synchronized void close() {
        if (this.out != null) {
            this.out.close();
            this.out = null;
        }
    }
}
