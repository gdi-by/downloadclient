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
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Tracks files in a directory. */
public class FileTracker {

    private static final Logger log
        = Logger.getLogger(FileTracker.class.getName());

    private File directory;
    private Set<File> last;
    private Set<File> current;

    /**
     * @param directory The directory to track.
     */
    public FileTracker(File directory) {
        this.directory = directory;
    }

    /**
     * Scans the directory.
     * @return true if success false otherwise.
     */
    public boolean scan() {
        File[] files = this.directory.listFiles();
        if (files == null) {
            log.log(
                Level.SEVERE, "Cannot read files from '" + directory + "'.");
            return false;
        }

        this.current = new TreeSet<>();
        for (File file: files) {
            this.current.add(file);
        }
        return true;
    }

    /**
     * Pushes the current scan for later comparison.
     */
    public void push() {
        this.last = this.current;
        this.current = null;
    }

    /**
     * Returns list of files new since last scan.
     * @return The list of new files.
     */
    public List<File> newFiles() {
        ArrayList<File> files = new ArrayList<>();
        for (File file: this.current) {
            if (!this.last.contains(file)) {
                files.add(file);
            }
        }
        return files;
    }

    /**
     * @return the directory
     */
    public File getDirectory() {
        return directory;
    }

    private List<File> glob(Collection<File> all, String pattern) {
        ArrayList<File> files = new ArrayList<>(all.size());
        PathMatcher matcher =
            FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        for (File file: all) {
            if (matcher.matches(file.toPath())) {
                files.add(file);
            }
        }
        return files;
    }

    /**
     * Globs pattern in new files.
     * @param pattern The glob pattern.
     * @return The new files that match the pattern.
     */
    public List<File> deltaGlob(String pattern) {
        return glob(newFiles(), pattern);
    }

    /**
     * Globs pattern in all files.
     * @param pattern The glob pattern.
     * @return The files that match the pattern.
     */
    public List<File> globalGlob(String pattern) {
        return glob(current, pattern);
    }
}
