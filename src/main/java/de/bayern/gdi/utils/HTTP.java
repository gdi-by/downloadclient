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
package de.bayern.gdi.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/** Helper for HTTP. */
public final class HTTP {

    private static final Logger log
        = Logger.getLogger(HTTP.class.getName());

    private HTTP() {
    }

    /**
     * Returns a HTTP client doing Basic Auth if needed.
     * @param url The URL to browse.
     * @param user The user. Maybe null.
     * @param password The password. Maybe null.
     * @return The HTTP client.
     */
    public static CloseableHttpClient getClient(
        URL url, String user, String password
    ) {
        if (user == null || password == null) {
            return HttpClients.createDefault();
        }

        UsernamePasswordCredentials defaultCreds
            = new UsernamePasswordCredentials(user, password);

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

    /**
     * Closes a client without throwing exceptions.
     * @param client The client to close.
     */
    public static void closeGraceful(CloseableHttpClient client) {
        if (client == null) {
            return;
        }
        try {
            client.close();
        } catch (IOException ioe) {
            // Only log this.
            log.log(Level.SEVERE,
                "Closing HTTP client failed: " + ioe.getMessage(), ioe);
        }
    }

    private static RequestConfig getProxyConfig(String proto) {
        String proxy = System.getProperty(proto + ".proxyHost");
        Integer port = Integer.getInteger(proto + ".proxyPort");
        HttpHost proxyHost = new HttpHost(proxy, port, proto);
        return RequestConfig.custom()
                .setProxy(proxyHost)
                .build();
    }

    private static void configureProxy(HttpGet request, String proto) {
        String proxy = System.getProperty(proto + ".proxyHost");
        if (proxy != null) {
            request.setConfig(getProxyConfig(proto));
        }
    }

    private static void configureProxy(HttpPost request, String proto) {
        String proxy = System.getProperty(proto + ".proxyHost");
        if (proxy != null) {
            request.setConfig(getProxyConfig(proto));
        }
    }

    /**
     * Returns a configured GET request.
     * @param url The URL to browse.
     * @return The GET requst object.
     * @throws URISyntaxException If the URL is not valid.
     */
    public static HttpGet getGetRequest(URL url) throws URISyntaxException {
        HttpGet request = new HttpGet(url.toURI());
        configureProxy(request, url.getProtocol().equals("https")
            ? "https"
            : "http");
        return request;
    }

    /**
     * Ret urns a configured Post request.
     * @param url the URL to post to
     * @return the POST Request Object
     * @throws URISyntaxException if the url is not valid
     */
    public static HttpPost getPostRequest(URL url)  throws URISyntaxException {
        HttpPost request = new HttpPost(url.toURI());
        configureProxy(request, url.getProtocol().equals("https")
                ? "https"
                : "http");
        return request;
    }
}
