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
 * A class to automatically generate formated filenames.
 * The extension is determined by a MIME type.
 */
public final class AutoFileNames {

    private String format;
    private int fileNo;

    /**
     * Constructor.
     * @param numFiles expected number of files.
     */
    public AutoFileNames(int numFiles) {
        // 1000 files -> 000, 000, ..., 999
        int places = StringUtils.places(Math.max(0, numFiles - 1));
        this.format = "%0" + places + "d.%s";
    }

    private static String mimetypeToExt(String type) {
        return Config.getInstance().getMimeTypes().findExtension(type, "gml");
    }

    /**
     * Generate a new filename.
     *
     * @param type the MIME type of the file. Determines the extension.
     * @return The generated filename.
     */
    public String nextFileName(String type) {
        String ext = mimetypeToExt(type);
        String fileName = String.format(format, this.fileNo, ext);
        this.fileNo++;
        return fileName;
    }
}
