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

import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/** Helper for HTTP. */
public final class HTTP {

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
}
