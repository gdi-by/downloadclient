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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Collection;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processor runs job out of a queue.
 */
public class Processor implements Runnable {

    private static final Logger log
        = Logger.getLogger(Processor.class.getName());

    private static final long WAIT_TIME = 1000;

    private Deque<Job> jobs;
    private boolean done;

    public Processor() {
        this.jobs = new ArrayDeque<Job>();
    }

    public Processor(Collection<Job> jobs) {
        this.jobs = new ArrayDeque<Job>(jobs);
    }

    /** quit the main loop og this processor. */
    public synchronized void quit() {
        this.done = true;
        notify();
    }

    /** Adds a job at the end of the queue.
     * @param job The job to add.
     */
    public synchronized void addJob(Job job) {
        this.jobs.add(job);
        notify();
    }

    @Override
    public void run() {
        for (;;) {
            Job job;
            synchronized (this) {
                try {
                    while (!this.done && this.jobs.isEmpty()) {
                        wait(WAIT_TIME);
                    }
                } catch (InterruptedException ie) {
                    break;
                }
                if (this.done) {
                    break;
                }
                job = this.jobs.poll();
            }
            try {
                job.run();
            } catch (JobExecutionException jee) {
                log.log(Level.SEVERE, jee.getMessage(), jee);
            }
        }
    }
}

