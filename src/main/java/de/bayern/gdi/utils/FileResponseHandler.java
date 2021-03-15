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

import de.bayern.gdi.gui.controller.Controller;

import java.nio.file.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File handler for HttpClient.
 */
public class FileResponseHandler implements ResponseHandler<Boolean> {



    private static Unauthorized unauthorized;

    /** Set the Unauthorized handler.
     * @param setUnauthorized The handler to set.
     */
    public static synchronized void setUnauthorized(
            Unauthorized setUnauthorized) {
        unauthorized = setUnauthorized;

    }

    private static synchronized void callUnauthorized() {
        if (unauthorized != null) {
            unauthorized.unauthorized();
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(FileResponseHandler.class.getName());

    private static final int BUF_SIZE = 4096;

    private File file;

    private HttpUriRequest request;

    private WrapInputStreamFactory wrapFactory;

    public FileResponseHandler() {
    }

    public FileResponseHandler(File file, HttpUriRequest request) {
        this(file, null, request);
    }

    public FileResponseHandler(File file, WrapInputStreamFactory wrapFactory,
            HttpUriRequest request) {
        this.file = file;
        this.wrapFactory = wrapFactory;
        this.request = request;
    }

    private InputStream wrap(InputStream in) {
        return this.wrapFactory != null
            ? this.wrapFactory.wrap(in)
            : in;
    }

    @Override
    public Boolean handleResponse(HttpResponse response)
        throws IOException {
        int status = response.getStatusLine().getStatusCode();
        String reason = response.getStatusLine().getReasonPhrase();
        Controller.logToAppLog(status + " " + reason + " "
                + request.toString());
        if (status < HttpStatus.SC_OK
            || status >= HttpStatus.SC_MULTIPLE_CHOICES) {
            if (status == HttpStatus.SC_UNAUTHORIZED) {
                callUnauthorized();
            }
            throw new ClientProtocolException("Unexpected response status: "
                    + status);
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return Boolean.FALSE;
        }
        try {
            try (FileOutputStream out = new FileOutputStream(this.file)) {
                byte[] buf = new byte[BUF_SIZE];
                int r;
                InputStream ins = wrap(entity.getContent());
                while ((r = ins.read(buf)) >= 0) {
                    out.write(buf, 0, r);
                }
                out.flush();
            }
        } catch (IOException ioe) {
            // Clean up debris if there was an error.
            // XXX: Maybe keep it to recover download?
            deleteGraceful();
            throw ioe;
        }
        return Boolean.TRUE;
    }

    private void deleteGraceful() {
        try {
            Files.delete(this.file.toPath());
        } catch (IOException ioe) {
            LOG.warn("delete failed: {}", ioe.getMessage());
        }
    }
}
