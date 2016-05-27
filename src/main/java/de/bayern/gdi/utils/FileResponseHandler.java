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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

/**
 * File handler for HttpClient.
 */
public class FileResponseHandler implements ResponseHandler<Boolean> {

    private static final Logger log
        = Logger.getLogger(FileResponseHandler.class.getName());

    private static final int BUF_SIZE = 4096;

    private File file;

    private WrapInputStreamFactory wrapFactory;

    public FileResponseHandler() {
    }

    public FileResponseHandler(File file) {
        this(file, null);
    }

    public FileResponseHandler(File file, WrapInputStreamFactory wrapFactory) {
        this.file = file;
        this.wrapFactory = wrapFactory;
    }

    private InputStream wrap(InputStream in) {
        return this.wrapFactory != null
            ? this.wrapFactory.wrap(in)
            : in;
    }

    @Override
    public Boolean handleResponse(HttpResponse response)
        throws ClientProtocolException, IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status < HttpStatus.SC_OK
            || status >= HttpStatus.SC_MULTIPLE_CHOICES) {
            throw new ClientProtocolException("Unexpected response status: "
                    + status);
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return Boolean.FALSE;
        }
        try {
            try (FileOutputStream out = new FileOutputStream(this.file)) {
                byte [] buf = new byte[BUF_SIZE];
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
            if (!this.file.delete()) {
                log.log(
                    Level.WARNING, "Deleting file '" + file + "' failed.");
            }
            throw ioe;
        }
        return Boolean.TRUE;
    }
}
