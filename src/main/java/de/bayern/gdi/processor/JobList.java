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

import java.util.ArrayList;
import java.util.List;

/**
 * JobList is a job of a sequence of depended jobs.
 */
public class JobList implements Job {

    private List<Job> jobs;

    public JobList() {
        jobs = new ArrayList<Job>();
    }

    /**
     * Add a job to this job list.
     * @param job The job to be added.
     */
    public void add(Job job) {
        jobs.add(job);
    }

    @Override
    public void run() throws JobExecutionException {
        for (Job job: jobs) {
            job.run();
        }
    }
}

