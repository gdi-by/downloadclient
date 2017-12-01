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
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

/** Helper for HTTP. */
public final class HTTP {

    private static final int DEFAULT_TIMEOUT = 10000;
    private static final int S_TO_MS = 1000;

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
        int timeout = DEFAULT_TIMEOUT;

        ApplicationSettings set = Config
            .getInstance()
            .getApplicationSettings();

        String ts = set.getApplicationSetting("requestTimeout_s");
        if (ts != null) {
            try {
                timeout = S_TO_MS * Integer.parseInt(ts);
            } catch (NumberFormatException nfe) {
                log.log(Level.SEVERE, nfe.getMessage());
            }
        }

        // Use JVM proxy settings.
        SystemDefaultRoutePlanner routePlanner
            = new SystemDefaultRoutePlanner(ProxySelector.getDefault());

        RequestConfig requestConfig = RequestConfig.
                custom().
                setSocketTimeout(timeout).build();
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig);
        builder.setRetryHandler(new StandardHttpRequestRetryHandler(1, true));
        builder.setRoutePlanner(routePlanner);

        builder.setUserAgent(getUserAgent());
        if (user != null && password != null) {

            UsernamePasswordCredentials defaultCreds =
                new UsernamePasswordCredentials(user, password);

            BasicCredentialsProvider credsProv = new BasicCredentialsProvider();

            credsProv.setCredentials(
                new AuthScope(url.getHost(), url.getPort()), defaultCreds);

            HttpClientContext context = HttpClientContext.create();
            context.setCredentialsProvider(credsProv);

            BasicAuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            HttpHost target = new HttpHost(url.getHost(), url.getPort());

            authCache.put(target, basicAuth);
            context.setAuthCache(authCache);

            builder.setDefaultCredentialsProvider(credsProv);
        }


        return builder.build();
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

    /**
     * Returns a configured GET request.
     * @param url The URL to browse.
     * @return The GET requst object.
     * @throws URISyntaxException If the URL is not valid.
     */
    public static HttpGet getGetRequest(URL url) throws URISyntaxException {
        return new HttpGet(url.toURI());
    }

    /**
     * Ret urns a configured Post request.
     * @param url the URL to post to
     * @return the POST Request Object
     * @throws URISyntaxException if the url is not valid
     */
    public static HttpPost getPostRequest(URL url)  throws URISyntaxException {
        return new HttpPost(url.toURI());
    }

    /**
     * Ret urns a configured Post request.
     * @param url the URL to post to
     * @return the POST Request Object
     * @throws URISyntaxException if the url is not valid
     */
    public static HttpHead getHeadRequest(URL url)  throws URISyntaxException {
        return new HttpHead(url.toURI());
    }

    /**
     * buildAbsoluteURL creates an absolute URL from
     * a base URL and a path. If the relative part
     * is already an absolute URL this is returned.
     * @param baseURL The base URL to derive from.
     * @param url The relative part of the URL.
     * @return The new absolute URL.
     * @throws MalformedURLException if the URL construction failed.
     * @throws URISyntaxException if the URL scheme is not valid.
     */
    public static URL buildAbsoluteURL(URL baseURL, String url)
    throws MalformedURLException, URISyntaxException {
        if (url == null || url.isEmpty()) {
            return baseURL;
        }
        URI uri = new URI(url);
        if (uri.isAbsolute()) {
            return uri.toURL();
        }
        return new URL(baseURL, url);
    }

    /**
     * gets the user Agent.
     * @return user agent
     */
    public static String getUserAgent() {
        //https://tools.ietf.org/html/rfc2616#section-14.43
        return Info.getName() + "/" + Info.getVersion() + " ("
                + Info.getComment() + ")";
    }
}
