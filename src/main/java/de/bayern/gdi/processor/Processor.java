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

import de.bayern.gdi.processor.job.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Processor runs job out of a queue.
 */
public class Processor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Processor.class.getName());

    private Deque<Job> jobs = new ArrayDeque<>();;

    private List<ProcessorListener> listeners = new CopyOnWriteArrayList<>();

    public Processor(Job jobToExecute) {
        jobs.add(jobToExecute);
    }

    public Processor(List<Job> jobsToExecute) {
        jobs.addAll(jobsToExecute);
    }

    /**
     * Adds a listener to the list of listeners.
     * @param listener The listener to add.
     */
    public void addListener(ProcessorListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener from the list of listeners.
     * @param listener The listener to remove.
     */
    public void removeListener(ProcessorListener listener) {
        listeners.remove(listener);
    }

    /** Broadcasts an exception to all listeners.
     * @param jee The exception to broadcast.
     */
    public void broadcastException(JobExecutionException jee) {
        ProcessorEvent pe = new ProcessorEvent(this, jee);
        for (ProcessorListener pl: listeners) {
            pl.receivedException(pe);
        }
    }

    /** Broadcasts a message to all listeners.
     * @param message The message to broadcast.
     */
    public void broadcastMessage(String message) {
        ProcessorEvent pe = new ProcessorEvent(this, message);
        for (ProcessorListener pl: listeners) {
            pl.receivedMessage(pe);
        }
    }

    /**
     * Informs that all jobs was finished.
     */
    public void jobFinished() {
        ProcessorEvent pe = new ProcessorEvent(this);
        for (ProcessorListener pl: listeners) {
            pl.jobFinished(pe);
        }
    }

    @Override
    public void run() {
        while (!this.jobs.isEmpty()) {
            try {
                Job job = this.jobs.poll();
                job.run(this);
            } catch (JobExecutionException jee) {
                LOG.error(jee.getMessage(), jee);
                broadcastException(jee);
            }
        }
        jobFinished();
    }
}

