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

import java.util.EventObject;

/** Event sent by a processor. */
public class ProcessorEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private JobExecutionException exception;
    private String message;

    /**
     * @param source The processor causing this event.
     */
    public ProcessorEvent(Object source) {
        super(source);
    }

    /**
     * @param source The processor causing this event.
     * @param exception The exception causing this event.
     */
    public ProcessorEvent(Object source, JobExecutionException exception) {
        this(source);
        this.exception = exception;
    }

    /**
     * @param source The processor causing this event.
     * @param message The message causing this event.
     */
    public ProcessorEvent(Object source, String message) {
        this(source);
        this.message = message;
    }

    /**
     * @return The exception causing this event.
     * NULL if it was message caused.
     */
    public JobExecutionException getException() {
        return this.exception;
    }

    /**
     * @return The message causing this event.
     * NULL if was exception caused.
     */
    public String getMessage() {
        return this.message;
    }
}
