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

import de.bayern.gdi.utils.CountingInputStream;
import de.bayern.gdi.utils.FileResponseHandler;
import de.bayern.gdi.utils.WrapInputStreamFactory;

import java.io.File;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/** FileDownloadJob is a job to download features from a service. */
public class FileDownloadJob
    implements Job, CountingInputStream.CountListener {

    private static final Logger log
        = Logger.getLogger(FileDownloadJob.class.getName());

    private String url;
    private File file;
    private String user;
    private String password;

    public FileDownloadJob() {
    }

    public FileDownloadJob(
        String url, File file, String user, String password) {
        this.url = url;
        this.file = file;
        this.user = user;
        this.password = password;
    }

    @Override
    public void bytesCounted(long count) {
        //TODO: Forward to UI.
        log.log(Level.INFO, "bytes downloaded: " + count);
    }

    @Override
    public void run() {
        // TODO: Do more fancy stuff like e.g. auth.
        WrapInputStreamFactory wrapFactory
            = CountingInputStream.createWrapFactory(this);

        FileResponseHandler responseHandler
            = new FileResponseHandler(this.file, wrapFactory);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(this.url);
            httpclient.execute(httpget, responseHandler);
            // TODO: Do something with file loaded.
        } catch (IOException ioe) {
            log.log(Level.SEVERE,
                "Download failed: " + ioe.getLocalizedMessage(), ioe);
        } finally {
            try {
                httpclient.close();
            } catch (IOException ioe) {
                log.log(Level.SEVERE,
                    "Closing HTTP client failed: "
                        + ioe.getLocalizedMessage(), ioe);
            }
        }
    }
}
