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

import de.bayern.gdi.processor.job.DownloadStepJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Processor runs job out of a queue.
 */
public class Processor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Processor.class.getName());

    private Deque<DownloadStepJob> jobs = new ArrayDeque<>();;

    private List<ProcessorListener> listeners = new CopyOnWriteArrayList<>();

    public Processor(DownloadStepJob jobToExecute) {
        jobs.add(jobToExecute);
    }

    public Processor(List<DownloadStepJob> jobsToExecute) {
        jobs.addAll(jobsToExecute);
    }

    /**
     * Adds listeners to the list of listeners.
     *
     * @param listenersToAdd The listeners to add.
     * @return the processor instance, never <code>null</code>
     */
    public Processor withListeners(ProcessorListener... listenersToAdd) {
        Arrays.stream(listenersToAdd).forEach(listenerToAdd -> {
                if (!listeners.contains(listenerToAdd)) {
                    listeners.add(listenerToAdd);
                }
            }
        );
        return this;
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

    @Override
    public void run() {
        while (!this.jobs.isEmpty()) {
            try {
                DownloadStepJob job = this.jobs.poll();
                job.run(this);
                downloadStepJobFinished();
            } catch (JobExecutionException jee) {
                LOG.error(jee.getMessage(), jee);
                downloadStepJobFailed(jee);
            }
        }
    }

    private void downloadStepJobFailed(JobExecutionException jee) {
        ProcessorEvent pe = new ProcessorEvent(this, jee);
        for (ProcessorListener pl: listeners) {
            pl.processingFailed(pe);
        }
    }

    private void downloadStepJobFinished() {
        for (ProcessorListener pl: listeners) {
            pl.processingFinished();
        }
    }
}

