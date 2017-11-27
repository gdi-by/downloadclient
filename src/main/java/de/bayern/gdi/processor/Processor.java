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
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processor runs job out of a queue.
 */
public class Processor implements Runnable {

    private static final Logger log
        = Logger.getLogger(Processor.class.getName());

    private static final long WAIT_TIME = 1000;

    /** Add this job to shutdown the processor after
     *  finishing all previous jobs.
     */
    public static final Job QUIT_JOB = new Job() {
        @Override
        public void run(Processor p) throws JobExecutionException {
        }
    };

    private static Processor instance;

    private Deque<Job> jobs;
    private boolean done;

    private List<ProcessorListener> listeners;

    public Processor() {
        this.jobs = new ArrayDeque<>();
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public Processor(Collection<Job> jobs) {
        this.jobs = new ArrayDeque<>(jobs);
    }

    /** Returns a singleton processor started as a separate thread.
     * @return The processor.
     */
    public static synchronized Processor getInstance() {
        if (instance == null) {
            instance = new Processor();
            Thread thread = new Thread(instance);
            thread.setDaemon(true);
            thread.start();
        }
        return instance;
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

    /** quit the main loop og this processor. */
    public synchronized void quit() {
        this.done = true;
        notifyAll();
    }

    /** Adds a job at the end of the queue.
     * @param job The job to add.
     */
    public synchronized void addJob(Job job) {
        this.jobs.add(job);
        notifyAll();
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
                    Thread.currentThread().interrupt();
                    break;
                }
                if (this.done) {
                    break;
                }
                job = this.jobs.poll();
            }
            if (job == QUIT_JOB) {
                break;
            }
            try {
                job.run(this);
            } catch (JobExecutionException jee) {
                log.log(Level.SEVERE, jee.getMessage(), jee);
                broadcastException(jee);
            }
        }
    }
}

