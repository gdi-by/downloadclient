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
package de.bayern.gdi.processor.job;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.bayern.gdi.processor.JobExecutionException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;

import de.bayern.gdi.utils.CountingInputStream;
import de.bayern.gdi.utils.FileResponseHandler;
import de.bayern.gdi.utils.HTTP;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Log;
import de.bayern.gdi.utils.WrapInputStreamFactory;

/** FileDownloadJob is a job to download features from a service. */
public class FileDownloadJob extends AbstractDownloadJob {

    private String urlString;
    private File file;
    private HttpEntity postParams;

    public FileDownloadJob(
            String urlString,
            File   file,
            String user,
            String password,
            Log    logger) {
        this(urlString, file, user, password, null, logger);
    }

    public FileDownloadJob(
            String urlString,
            File   file,
            String user,
            String password,
            HttpEntity postParams,
            Log    logger) {
        super(user, password, logger);
        this.urlString = urlString;
        this.file = file;
        this.postParams = postParams;
        this.listener.add(this);
    }

    @Override
    public void bytesCounted(long count) {
        broadcastMessage(I18n.format("file.download.bytes", count), false);
    }

    @Override
    public void download()
        throws JobExecutionException, InterruptedException {
        URL url = toURL(this.urlString);

        WrapInputStreamFactory wrapFactory
            = CountingInputStream.createWrapFactory(listener);

        CloseableHttpClient httpclient = getClient(url);

        String msg = I18n.format("download.file", url, this.file);
        broadcastMessage(msg);

        try {
            HttpRequestBase httpRequest;
            if (postParams == null) {
                httpRequest = getGetRequest(url);
            } else {
                HttpPost httppost = new HttpPost(url.toString());
                httppost.setHeader("Content-Type", "application/xml");
                httppost.setEntity(postParams);
                httpRequest = httppost;
            }
            FileResponseHandler responseHandler = new FileResponseHandler(this.file, wrapFactory, httpRequest);

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            HttpDownloadExecutor downloadTask = new HttpDownloadExecutor(httpclient, httpRequest, responseHandler,
                this);
            Future<?> submit = executorService.submit(downloadTask);
            while (!submit.isDone()) {
                if (Thread.currentThread().isInterrupted()) {
                    httpRequest.abort();
                    throw new InterruptedException("Download interrupted.");
                }
            }
            if (downloadTask.isFailed()) {
                throw downloadTask.getJobExecutionException();
            }
        } finally {
            HTTP.closeGraceful(httpclient);
        }
        broadcastMessage(I18n.getMsg("file.download.finished"));
    }

    /**
     * @return the HttpEntity used a POST body.
     */
    public HttpEntity getPostParams() {
        return postParams;
    }

}
