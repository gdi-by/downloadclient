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

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import de.bayern.gdi.utils.CountingInputStream;
import de.bayern.gdi.utils.FileResponseHandler;
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
        //TODO: Forward to UI.
        log.log(Level.INFO, "bytes downloaded: " + count);
    }

    private CloseableHttpClient getClient(URL url) {

        if (this.password == null || this.user == null) {
            return HttpClients.createDefault();
        }

        UsernamePasswordCredentials defaultCreds
            = new UsernamePasswordCredentials(this.user, this.password);

        HttpClientContext context = HttpClientContext.create();
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();

        credsProv.setCredentials(
            new AuthScope(url.getHost(), url.getPort()), defaultCreds);

        context.setCredentialsProvider(credsProv);

        BasicAuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        HttpHost target = new HttpHost(url.getHost(), url.getPort());

        authCache.put(target, basicAuth);
        context.setAuthCache(authCache);

        return HttpClients
            .custom()
            .setDefaultCredentialsProvider(credsProv)
            .build();
    }

    @Override
    public void run(Processor p) throws JobExecutionException {
        URL url;
        try {
            url = new URL(this.urlString);
        } catch (MalformedURLException e) {
            throw new JobExecutionException(
                "bad URL \"" + this.urlString + "\"", e);
        }

        WrapInputStreamFactory wrapFactory
            = CountingInputStream.createWrapFactory(this);

        FileResponseHandler responseHandler
            = new FileResponseHandler(this.file, wrapFactory);

        CloseableHttpClient httpclient = getClient(url);

        try {
            HttpGet httpget = new HttpGet(this.urlString);
            httpclient.execute(httpget, responseHandler);
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
        p.broadcastMessage("Download finished.");
    }
}
