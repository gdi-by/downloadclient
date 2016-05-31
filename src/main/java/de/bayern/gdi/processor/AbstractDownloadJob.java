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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.impl.client.CloseableHttpClient;

import de.bayern.gdi.utils.CountingInputStream;
import de.bayern.gdi.utils.HTTP;
import de.bayern.gdi.utils.I18n;

/** A base class for different download jobs. */
public abstract class AbstractDownloadJob
    implements Job, CountingInputStream.CountListener {

    /** optional user. */
    protected String user;
    /** optional password. */
    protected String password;
    /** Processor executing this job. */
    protected Processor processor;

    public AbstractDownloadJob() {
    }

    public AbstractDownloadJob(String user, String password) {
        this.user = user;
        this.password = password;
    }

    /**
     * Returns a HTTP client suitable for given URL.
     * @param url The URL.
     * @return The HTTP client.
     */
    protected CloseableHttpClient getClient(URL url) {
        return HTTP.getClient(url, user, password);
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

    /**
     * Broadcasts a message to a processor if set.
     * @param message The message to broadcast.
     */
    protected void broadcastMessage(String message) {
        if (this.processor != null) {
            this.processor.broadcastMessage(message);
        }
    }

    /**
     * Broadcasts an exception to a processor if set.
     * @param e The exception to broadcast.
     */
    protected void broadcastException(JobExecutionException e) {
        if (this.processor != null) {
            this.processor.broadcastException(e);
        }
    }

    /**
     * Override this for the concrete download.
     * @throws JobExecutionException Something went wrong during
     * the download.
     */
    protected abstract void download() throws JobExecutionException;

    /**
     * Converts a string into an URL.
     * @param urlString The string.
     * @return an URL.
     * @throws JobExecutionException If the URL is not valid.
     */
    protected static URL toURL(String urlString) throws JobExecutionException {
        try {
            return new URL(urlString);
        } catch (MalformedURLException mue) {
            throw new JobExecutionException(
                I18n.format("file.download.bad.url", urlString));
        }
    }
}

