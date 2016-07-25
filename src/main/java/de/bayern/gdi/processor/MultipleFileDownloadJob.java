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
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import de.bayern.gdi.utils.CountingInputStream;
import de.bayern.gdi.utils.FileResponseHandler;
import de.bayern.gdi.utils.HTTP;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Log;
import de.bayern.gdi.utils.WrapInputStreamFactory;

/** Abstract class to do multiple file downloads. */
public abstract class MultipleFileDownloadJob extends AbstractDownloadJob {

    private static final Logger log
        = Logger.getLogger(MultipleFileDownloadJob.class.getName());

    /** Number to re-tries for a failed download. */
    protected static final int MAX_TRIES = 5;
    /** Pause after failed downloads. */
    protected static final long FAIL_SLEEP = 30 * 1000;

    /** Stores a file location to down from and to. */
    protected static class DLFile {

        /** Destination location of the file. */
        File file;
        /** The url to download from. */
        URL url;
        /** The number of tries yet. */
        int tries;

        DLFile(File file, URL url) {
            this.file = file;
            this.url = url;
        }
    }

    /** Number of bytes of the currently downloading file. */
    protected long currentCount;
    /** Total number of bytes of downloaded files do far. */
    protected long totalCount;

    public MultipleFileDownloadJob() {
    }

    public MultipleFileDownloadJob(String user, String password, Log logger) {
        super(user, password, logger);
    }

    @Override
    public void bytesCounted(long count) {
        String msg = I18n.format(
            "atom.bytes.downloaded", this.totalCount + count);
        broadcastMessage(msg);
        this.currentCount = count;
    }

    /**
     * Downloads a file.
     * @param dlf The file to download.
     * @return true if download succeed false otherwise.
     * @throws JobExecutionException If something went wrong.
     */
    protected boolean downloadFile(DLFile dlf) throws JobExecutionException {

        // TODO: i18n
        String msg = "Downloading '" + dlf.url + "' to '" + dlf.file + "'";
        log.log(Level.INFO, msg);
        this.currentCount = 0;

        CloseableHttpClient client = getClient(dlf.url);
        HttpGet httpget = getGetRequest(dlf.url);

        WrapInputStreamFactory wrapFactory
            = CountingInputStream.createWrapFactory(this);

        try {
            FileResponseHandler frh
                = new FileResponseHandler(dlf.file, wrapFactory);
            client.execute(httpget, frh);

            return true;
        } catch (IOException ioe) {
            return false;
        } finally {
            HTTP.closeGraceful(client);
            this.totalCount += this.currentCount;
        }
    }

    /**
     * Downloads a list of files.
     * @param files The files to download.
     * @throws JobExecutionException If something went wrong.
     */
    protected void downloadFiles(List<DLFile> files)
    throws JobExecutionException {
        int failed = 0;
        int numFiles = files.size();

        for (;;) {
            for (int i = 0; i < files.size();) {
                DLFile file = files.get(i);
                if (downloadFile(file)) {
                    files.remove(i);
                } else {
                    if (++file.tries < MAX_TRIES) {
                        i++;
                    } else {
                        failed++;
                        files.remove(i);
                    }
                }
                broadcastMessage(I18n.format(
                    "atom.downloaded.files",
                    numFiles - failed - files.size(),
                    files.size()));
            }
            if (files.isEmpty()) {
                break;
            }
            try {
                Thread.sleep(FAIL_SLEEP);
            } catch (InterruptedException ie) {
                break;
            }
        }

        // TODO: i18n
        String msg = "Bytes downloaded: " + this.totalCount;
        logger.log(msg);
        log.log(Level.INFO, msg);

        if (failed > 0) {
            msg = I18n.format(
                "atom.downloaded.failed", numFiles - failed, failed);
            JobExecutionException jee = new JobExecutionException(msg);
            broadcastException(jee);
            throw jee;
        }
        msg = I18n.format("atom.downloaded.success", numFiles);
        broadcastMessage(msg);
    }
}
