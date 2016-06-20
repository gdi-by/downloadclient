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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/** Misc helper functions. */
public final class Misc {

    private static final SimpleDateFormat DF_FORMAT
        = new SimpleDateFormat("yyyyMMddHHmmss");

    /** Number of tries before giving up searching for collisions. */
    private static final int MAX_TRIES = 5000;

    /** Common prefix. */
    public static final String PREFIX = "gdibydl-";

    private Misc() {
    }

    /**
     * Creates a directory in the parent directory. The name
     * is created from the common prefix and the current time.
     * To avoid collisions a integer is append in case.
     * @param parent The parent directory.
     * @return The new created directory or null if the creation
     * failed.
     */
    public static File createDir(File parent) {
        return createDir(parent, PREFIX);
    }

    /**
     * Creates a directory in the parent directory. The name
     * is created from the given prefix and the current time.
     * To avoid collisions an integer is appended in case.
     * @param parent The parent directory.
     * @param prefix The used prefix.
     * @return The new created directory or null if the creation
     * failed.
     */
    public static File createDir(File parent, String prefix) {

        Date now = new Date();

        String dir = prefix + DF_FORMAT.format(now);
        File path = new File(parent, dir);
        int count = 0;
        while (count < MAX_TRIES && path.exists()) {
            ++count;
            dir = prefix + DF_FORMAT.format(now) + "-" + count;
            path = new File(parent, dir);
        }

        return count < MAX_TRIES && path.mkdirs() ? path : null;
    }

    /**
     * Creates a unique file name in the parent directory. The name
     * is created from the prefix, the extension and the current time.
     * To avoid collisions an integer is appended in case.
     * @param parent The parent directory.
     * @param prefix The used prefix.
     * @param ext The used extension.
     * @param tmpFiles Temporary files not in file system.
     * @return The unique or null if the creation failed.
     */
    public static File uniqueFile(
        File      parent,
        String    prefix,
        String    ext,
        Set<File> tmpFiles
    ) {
        Date now = new Date();

        String dir = prefix + DF_FORMAT.format(now);
        File path = new File(parent, dir);
        int count = 0;
        while (count < MAX_TRIES && path.exists() && tmpFiles.contains(path)) {
            ++count;
            dir = prefix + DF_FORMAT.format(now) + "-" + count;
            path = new File(parent, dir);
        }

        return count < MAX_TRIES ? path : null;
    }
}
