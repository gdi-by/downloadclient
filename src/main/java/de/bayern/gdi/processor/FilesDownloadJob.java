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
package de.bayern.gdi.processor;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;

/** Download a list of URLs to given files. */
public class FilesDownloadJob extends MultipleFileDownloadJob {

    private List<DLFile> files;

    public FilesDownloadJob() {
        this.files = new ArrayList<>();
    }

    public FilesDownloadJob(String user, String password) {
        this();
        this.user = user;
        this.password = password;
    }

    /** Downloads an URL to a file.
     * @param file The file.
     * @param url The URL.
     */
    public void add(File file, URL url) {
        this.files.add(new DLFile(file, url));
    }

    /** Downloads an URL to a file using post.
     * @param file The file.
     * @param url The URL.
     * @param postParams Post params
     */
    public void add(File file, URL url, HttpEntity postParams) {
        this.files.add(new DLFile(file, url, postParams));
    }

    @Override
    protected void download() throws JobExecutionException {
        downloadFiles(this.files);
    }
}
