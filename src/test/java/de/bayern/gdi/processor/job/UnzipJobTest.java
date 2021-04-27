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

import de.bayern.gdi.utils.FileTracker;
import de.bayern.gdi.utils.Log;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.size;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class UnzipJobTest {

    /**
     * Mocks the download directory.
     */
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    /**
     * Test unzip.
     *
     * @throws Exception e.
     */
    @Test
    public void testUnzip() throws Exception {
        FileTracker fileTracker = createFileTracker();
        copyZipFileToTmpDir();
        Log log = createLog();

        UnzipJob unzipJob = new UnzipJob(fileTracker, log);
        unzipJob.run(null);


        Path zipFileContent = testFolder.getRoot().toPath()
            .resolve("zippedFileContent.txt");
        assertThat(exists(zipFileContent), is(true));
        assertThat(size(zipFileContent), is(not(0)));
    }

    private Log createLog() throws IOException {
        File logfile = testFolder.newFile("unzipTest.log");
        return new Log(logfile);
    }

    private FileTracker createFileTracker() {
        FileTracker fileTracker = new FileTracker(testFolder.getRoot());
        fileTracker.scan();
        return fileTracker;
    }

    private void copyZipFileToTmpDir() throws IOException {
        FileOutputStream dest = new FileOutputStream(
            testFolder.newFile("zippedFile.zip"));
        IOUtils.copy(UnzipJobTest.class.getResourceAsStream(
            "zippedFile.zip"), dest);
        dest.close();
    }

}
