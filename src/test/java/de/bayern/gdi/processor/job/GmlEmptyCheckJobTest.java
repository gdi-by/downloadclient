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
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlEmptyCheckJobTest {

    /**
     * Temporary download directory.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Check download file.
     * @throws Exception
     */
    @Test
    public void verifyProcessCollection()
        throws Exception {
        FileTracker fileTracker = mockFileTracker("containsFeatures.gml");
        GmlEmptyCheckJob gmlEmptyCheckJob = new GmlEmptyCheckJob(null, fileTracker);
        gmlEmptyCheckJob.run(mockProcessor());
    }

    /**
     * Check empty download file.
     * @throws Exception
     */
    @Test(expected = JobExecutionException.class)
    public void verifyProcessEmptyCollectionShouldFail()
        throws Exception {
        FileTracker fileTracker = mockFileTracker("emptyFeatureCollection.gml");
        GmlEmptyCheckJob gmlEmptyCheckJob = new GmlEmptyCheckJob(null, fileTracker);
        gmlEmptyCheckJob.run(mockProcessor());
    }

    private Processor mockProcessor() {
        return mock(Processor.class);
    }

    private FileTracker mockFileTracker(String resource)
        throws IOException {
        InputStream resourceAsStream = getClass().getResourceAsStream(resource);
        File file = temporaryFolder.newFile();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            IOUtils.copy(resourceAsStream, fos);
        }
        FileTracker mock = mock(FileTracker.class);
        when(mock.retrieveFilesWithoutScan(anyString())).thenReturn(singletonList(file));
        return mock;
    }
}
