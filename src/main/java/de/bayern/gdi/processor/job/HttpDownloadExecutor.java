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
/**
 * Executes the http request.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */

import de.bayern.gdi.processor.JobExecutionException;
import de.bayern.gdi.utils.FileResponseHandler;
import de.bayern.gdi.utils.I18n;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class HttpDownloadExecutor implements Runnable {

    private final CloseableHttpClient httpclient;

    private final HttpRequestBase httpRequest;

    private final FileResponseHandler responseHandler;

    private final AbstractDownloadJob downloadJob;

    private JobExecutionException exception;

    public HttpDownloadExecutor(CloseableHttpClient httpclient, HttpRequestBase httpRequest,
                                FileResponseHandler responseHandler,
                                AbstractDownloadJob downloadJob) {
        this.httpclient = httpclient;
        this.httpRequest = httpRequest;
        this.responseHandler = responseHandler;
        this.downloadJob = downloadJob;
    }

    @Override
    public void run() {
        try {
            httpclient.execute(httpRequest, responseHandler);
        } catch (ConnectTimeoutException | SocketTimeoutException te) {
            String failureMsg = I18n.format(
                "file.download.failed_reason",
                I18n.getMsg("file.download.failed.timeout"));
            exception = new JobExecutionException(failureMsg, te);
            downloadJob.log(failureMsg);
        } catch (ClientProtocolException cpe) {
            String failureMsg = I18n.getMsg("file.download.failed");
            exception = new JobExecutionException(failureMsg, cpe);
            downloadJob.log(failureMsg);
        } catch (IOException ioe) {
            String failureMsg = I18n.getMsg("file.download.failed");
            exception = new JobExecutionException(failureMsg, ioe);
            downloadJob.log(failureMsg);
        }
    }

    /**
     * Returns the status of the http request.
     *
     * @return <code>true</code> if an exception was thrown, <code>false</code> otherwise
     */
    public boolean isFailed() {
        return exception != null;
    }

    /**
     * The exception.
     *
     * @return the execption, <code>null</code> if no execption was thrown
     */
    public JobExecutionException getJobExecutionException() {
        return exception;
    }
}
