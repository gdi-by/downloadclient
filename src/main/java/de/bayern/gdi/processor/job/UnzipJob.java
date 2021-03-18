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
package de.bayern.gdi.processor.job;

import de.bayern.gdi.processor.JobExecutionException;
import de.bayern.gdi.processor.Processor;
import de.bayern.gdi.utils.FileTracker;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Unzippes from download directory.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class UnzipJob implements Job {
    private static final int BUFFER_SIZE = 4096;
    private final FileTracker fileTracker;
    private final Log logger;

    /**
     * @param fileTracker never <code>null</code>
     * @param logger      never <code>null</code>
     */
    public UnzipJob(FileTracker fileTracker, Log logger) {
        this.fileTracker = fileTracker;
        this.logger = logger;
    }

    @Override
    public void run(Processor p) throws JobExecutionException {
        updateFileTracker(p);
        List<File> filesToUnzip = fileTracker.deltaGlob("*.{zip}");
        I18n.getMsg("unzip.start");
        unzip(p, filesToUnzip);
        I18n.getMsg("unzip.end");
    }

    private void unzip(Processor p, List<File> filesToUnzip)
        throws JobExecutionException {
        for (File fileToUnzip : filesToUnzip) {
            try {
                unzip(fileTracker.getDirectory(), fileToUnzip);
            } catch (IOException e) {
                String msg = "Could not unzip file " + fileToUnzip;
                JobExecutionException jee = new JobExecutionException(msg, e);
                broadcastException(p, jee);
                throw jee;
            }
        }
    }

    private void unzip(File directory, File fileToUnzip) throws IOException {
        try (FileInputStream in = new FileInputStream(fileToUnzip);
             ZipInputStream zipIn = new ZipInputStream(in)) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = directory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    extractFile(zipIn, filePath);
                } else {
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    private void extractFile(ZipInputStream zipIn, String filePath)
        throws IOException {
        try (FileOutputStream out = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(out)) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    private void updateFileTracker(Processor p) throws JobExecutionException {
        this.fileTracker.push();
        if (!this.fileTracker.scan()) {
            String msg = I18n.format(
                "external.process.scan.dir.failed",
                this.fileTracker.getDirectory());
            JobExecutionException jee = new JobExecutionException(msg);
            broadcastException(p, jee);
            throw jee;
        }
    }

    private void broadcastMessage(Processor p, String msg) {
        logger.log(msg);
        if (p != null) {
            p.broadcastMessage(msg);
        }
    }

    private void broadcastException(Processor p, JobExecutionException jee) {
        logger.log(jee.getMessage());
        if (p != null) {
            p.broadcastException(jee);
        }
    }
}
