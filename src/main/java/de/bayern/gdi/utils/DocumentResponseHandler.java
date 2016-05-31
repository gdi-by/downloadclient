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
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import org.w3c.dom.Document;

/**
 * ResponseHandler for HttpClient.
 */
public class DocumentResponseHandler implements ResponseHandler<Document> {

    private WrapInputStreamFactory wrapFactory;
    private Boolean namespaceAware;

    public DocumentResponseHandler() {
    }

    public DocumentResponseHandler(WrapInputStreamFactory wrapFactory) {
        this.wrapFactory = wrapFactory;
    }

    /**
     * Set the XML parse to be namespace aware or not.
     * @param namespaceAware Should the parse be namespace aware?
     */
    public void setNamespaceAware(Boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    private InputStream wrap(InputStream in) {
        return this.wrapFactory != null
            ? this.wrapFactory.wrap(in)
            : in;
    }

    @Override
    public Document handleResponse(HttpResponse response)
        throws ClientProtocolException, IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status < HttpStatus.SC_OK
            || status >= HttpStatus.SC_MULTIPLE_CHOICES) {
            throw new ClientProtocolException("Unexpected response status: "
                    + status);
        }
        HttpEntity entity = response.getEntity();
        return entity == null
            ? null
            : XML.getDocument(wrap(entity.getContent()), namespaceAware);
    }
}
