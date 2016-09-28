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
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Misc helper functions. */
public final class Misc {

    private static final SimpleDateFormat DF_FORMAT
        = new SimpleDateFormat("yyyyMMddHHmmss");

    /** Number of tries before giving up searching for collisions. */
    private static final int MAX_TRIES = 5000;

    /** Common prefix. */
    public static final String PREFIX = "gdibydl-";

    private static final Logger log
            = Logger.getLogger(Misc.class.getName());

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

        String f = prefix + DF_FORMAT.format(now) + "." + ext;
        File path = new File(parent, f);
        int count = 0;
        while (count < MAX_TRIES
        && (path.exists() || (tmpFiles != null && tmpFiles.contains(path)))) {
            ++count;
            f = prefix + DF_FORMAT.format(now) + "-" + count + "." + ext;
            path = new File(parent, f);
        }

        return count < MAX_TRIES ? path : null;
    }

    /**
     * returns file from resources.
     * @param path path or filename in rsources
     * @return returns the file
     */
    public static InputStream getResource(String path) {
        return Misc.class.getClassLoader().getResourceAsStream(path);
    }

    /**
     * converts an input Stream to String.
     * @param stream stream
     * @return String
     */
    public static String inputStreamToString(InputStream stream) {
        java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * Tries to start the external default browser.
     * @param url url
     */
    public static void startExternalBrowser(String url) {
        try {
            new ProcessBuilder("x-www-browser", url).start();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException ex) {
                log.log(Level.SEVERE, ex.getMessage(), ex);
                //When every standard fails, do the hard work
                String os = System.getProperty("os.name").toLowerCase();
                Runtime rt = Runtime.getRuntime();
                try {
                    if (os.contains("win")) {
                        rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
                    } else if (os.contains("mac")) {
                        rt.exec("open" + url);
                    } else {
                        String[] browsers = {"epiphany",
                                "firefox",
                                "mozilla",
                                "konqueror",
                                "netscape",
                                "opera",
                                "links",
                                "lynx"};

                        StringBuffer cmd = new StringBuffer();
                        for (int i = 0; i < browsers.length; i++) {
                            cmd.append((i == 0 ? "" : " || ")
                                    + browsers[i]
                                    + " \""
                                    + url
                                    + "\" ");
                        }
                        rt.exec(new String[] {"sh", "-c", cmd.toString()});
                    }
                } catch (IOException exc) {
                    log.log(Level.SEVERE, exc.getMessage(), exc);
                }
            }
        }
    }
}
