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

/** AtomDownloadJob is a job to download things from a ATOM service. */
public class AtomDownloadJob extends AbstractDownloadJob {

    private String dataset;
    private String variation;
    private File workingDir;

    private long total;

    private Processor processor;

    public AtomDownloadJob() {
    }

    public AtomDownloadJob(
        String dataset,
        String variation,
        File workingDir,
        String user,
        String password
    ) {
        super(user, password);
        this.dataset = dataset;
        this.variation = variation;
        this.workingDir = workingDir;
    }

    @Override
    public void bytesCounted(long count) {
        this.total += count;
    }

    @Override
    protected void download() throws JobExecutionException {
        // TODO: Implement me!
    }
}
