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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import de.bayern.gdi.utils.CountingInputStream;
import de.bayern.gdi.utils.FileResponseHandler;
import de.bayern.gdi.utils.HTTP;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.WrapInputStreamFactory;

/** FileDownloadJob is a job to download features from a service. */
public class FileDownloadJob
    implements Job, CountingInputStream.CountListener {

    private static final Logger log
        = Logger.getLogger(FileDownloadJob.class.getName());

    private String urlString;
    private File file;
    private String user;
    private String password;

    private Processor processor;

    public FileDownloadJob() {
    }

    public FileDownloadJob(
        String urlString, File file, String user, String password) {
        this.urlString = urlString;
        this.file = file;
        this.user = user;
        this.password = password;
    }

    @Override
    public void bytesCounted(long count) {
        String message = I18n.format("file.download.bytes", count);
        processor.broadcastMessage(message);
    }

    @Override
    public void run(Processor p) throws JobExecutionException {
        Processor old = this.processor;
        this.processor = p;
        try {
            download();
        } finally {
            this.processor = old;
        }
    }

    private void download() throws JobExecutionException {
        URL url;
        try {
            url = new URL(this.urlString);
        } catch (MalformedURLException e) {
            throw new JobExecutionException(
                I18n.format("file.download.bad.url", this.urlString));
        }

        WrapInputStreamFactory wrapFactory
            = CountingInputStream.createWrapFactory(this);

        FileResponseHandler responseHandler
            = new FileResponseHandler(this.file, wrapFactory);

        CloseableHttpClient httpclient
            = HTTP.getClient(url, this.user, this.password);

        this.processor.broadcastMessage(I18n.getMsg("file.download.start"));

        try {
            HttpGet httpget = new HttpGet(this.urlString);
            httpclient.execute(httpget, responseHandler);
        } catch (IOException ioe) {
            throw new JobExecutionException(
                I18n.getMsg("file.download.failed"), ioe);
        } finally {
            try {
                httpclient.close();
            } catch (IOException ioe) {
                // Only log this.
                log.log(Level.SEVERE,
                    "Closing HTTP client failed: " + ioe.getMessage(), ioe);
            }
        }
        this.processor.broadcastMessage(I18n.getMsg("file.download.finished"));
    }
}
