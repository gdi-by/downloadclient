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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * JobList is a job of a sequence of depended jobs.
 */
public class JobList implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(JobList.class.getName());

    private List<Job> jobs;

    public JobList() {
        jobs = new ArrayList<>();
    }

    /**
     * Add a job to this job list.
     * @param job The job to be added.
     */
    public void addJob(Job job) {
        jobs.add(job);
    }

    /**
     * Add all jobs to this job list.
     * @param other The jobs to add.
     */
    public void addJobs(List<Job> other) {
        jobs.addAll(other);
    }

    @Override
    public void run(Processor p) throws JobExecutionException {
        LOG.info("Executing job list");
        int i = 0;
        try {
            for (; i < jobs.size(); i++) {
                jobs.get(i).run(p);
            }
        } finally {
            for (i++; i < jobs.size(); i++) {
                Job job = jobs.get(i);
                if (job instanceof DeferredJob) {
                    job.run(p);
                }
            }
        }
    }

    /**
     * Returns the List of Jobs.
     * @return list of Jobs
     */
    public List<Job> getJobList() {
        return this.jobs;
    }
}

