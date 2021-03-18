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
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.conn.ConnectTimeoutException;

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
    public void download() throws JobExecutionException {
        URL url = toURL(this.urlString);

        WrapInputStreamFactory wrapFactory
            = CountingInputStream.createWrapFactory(listener);

        CloseableHttpClient httpclient = getClient(url);

        String msg = I18n.format("download.file", url, this.file);
        broadcastMessage(msg);

        try {
            if (postParams == null) {
                HttpGet httpget = getGetRequest(url);
                FileResponseHandler responseHandler
                        = new FileResponseHandler(
                                this.file, wrapFactory, httpget);

                httpclient.execute(httpget, responseHandler);
            } else {
                HttpPost httppost = new HttpPost(url.toString());
                httppost.setHeader("Content-Type", "application/xml");
                httppost.setEntity(postParams);
                FileResponseHandler responseHandler
                        = new FileResponseHandler(
                                this.file, wrapFactory, httppost);

                httpclient.execute(httppost, responseHandler);

            }
        } catch (ConnectTimeoutException | SocketTimeoutException te) {
            JobExecutionException jee =
            new JobExecutionException(
                    I18n.format(
                            "file.download.failed_reason",
                            I18n.getMsg("file.download.failed.timeout")),
                    te);
            broadcastException(jee);
            throw jee;

        } catch (IOException ioe) {
            JobExecutionException jee =
                new JobExecutionException(
                    I18n.getMsg("file.download.failed"), ioe);
            broadcastException(jee);
            throw jee;
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
