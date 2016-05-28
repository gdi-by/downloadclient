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
import de.bayern.gdi.utils.DocumentResponseHandler;
import de.bayern.gdi.utils.WrapInputStreamFactory;

import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.w3c.dom.Document;

/** DocumentDownloadJob is a job to download features from a service. */
public class DocumentDownloadJob
    implements Job, CountingInputStream.CountListener {

    private static final Logger log
        = Logger.getLogger(DocumentDownloadJob.class.getName());

    private String url;

    private Processor processor;

    public DocumentDownloadJob() {
    }

    public DocumentDownloadJob(String url) {
        this.url = url;
    }

    @Override
    public void bytesCounted(long count) {
        String message = "bytes downloaded: " + count;
        processor.broadcastMessage(message);
    }

    @Override
    public void run(Processor p) throws JobExecutionException {
        Processor old = this.processor;
        this.processor = p;
        try {
            innerRun(p);
        } finally {
            this.processor = old;
        }
    }

    private void innerRun(Processor p) throws JobExecutionException {
        // TODO: Do more fancy stuff like e.g. auth.
        WrapInputStreamFactory wrapFactory
            = CountingInputStream.createWrapFactory(this);

        DocumentResponseHandler responseHandler
            = new DocumentResponseHandler(wrapFactory);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(this.url);
            Document doc = httpclient.execute(httpget, responseHandler);
            // TODO: Do something with document loaded.
        } catch (IOException ioe) {
            throw new JobExecutionException("Download failed", ioe);
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
