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
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import de.bayern.gdi.utils.CountingInputStream;
import de.bayern.gdi.utils.HTTP;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Log;

/** A base class for different download jobs. */
public abstract class AbstractDownloadJob
    implements Job, CountingInputStream.CountListener {

    /** optional user. */
    protected String user;
    /** optional password. */
    protected String password;
    /** Processor executing this job. */
    protected Processor processor;
    /** The logger to log to. */
    protected Log logger;

    public AbstractDownloadJob() {
    }

    public AbstractDownloadJob(String user, String password, Log logger) {
        this.user = user;
        this.password = password;
        this.logger = logger;
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

    /** Log message to download log.
     * @param msg The message to log.
     */
    protected void log(String msg) {
        if (logger != null) {
            logger.log(msg);
        }
    }

    /**
     * Broadcasts a message to a processor if set.
     * @param message The message to broadcast.
     */
    protected void broadcastMessage(String message) {
        log(message);
        if (this.processor != null) {
            this.processor.broadcastMessage(message);
        }
    }

    /**
     * Broadcasts an exception to a processor if set.
     * @param e The exception to broadcast.
     */
    protected void broadcastException(JobExecutionException e) {
        log(e.getMessage());
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
    protected URL toURL(String urlString) throws JobExecutionException {
        try {
            return new URL(urlString);
        } catch (MalformedURLException mue) {
            String msg = I18n.format("file.download.bad.url", urlString);
            log(msg);
            throw new JobExecutionException(msg);
        }
    }


    /**
     * Creates an absolute URL from a base and a relative parts.
     * @param base The base part.
     * @param rel The relative part.
     * @return The created absoluet URL.
     * @throws JobExecutionException Something went wrong.
     */
    protected URL absoluteURL(String base, String rel)
        throws JobExecutionException {
        return absoluteURL(toURL(base), rel);
    }

    /**
     * Creates an absolute URL from a base and a relative parts.
     * @param base The base part.
     * @param rel The relative part.
     * @return The created absoluet URL.
     * @throws JobExecutionException Something went wrong.
     */
    protected URL absoluteURL(URL base, String rel)
        throws JobExecutionException {
        try {
            return HTTP.buildAbsoluteURL(base, rel);
        } catch (MalformedURLException | URISyntaxException e) {
            String msg = I18n.format("file.download.bad.url", e.getMessage());
            log(msg);
            throw new JobExecutionException(msg);
        }
    }

    /**
     * Creates a configured GET request.
     * @param url The URL to browse.
     * @return The GET request object.
     * @throws JobExecutionException If the URL is not valid.
     */
    protected HttpGet getGetRequest(URL url)
        throws JobExecutionException {
        try {
            return HTTP.getGetRequest(url);
        } catch (URISyntaxException use) {
            String msg = I18n.format("file.download.bad.url", url);
            log(msg);
            throw new JobExecutionException(msg);
        }
    }
}

