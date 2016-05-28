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

import java.util.EventListener;

/** Implementors of this interface recieve async messages
 *  from a Processor.
 */
public interface ProcessorListener extends EventListener {

    /**
     * Called if an exception was thrown during job execution.
     * @param pe The event. pe.getException() returns the
     * thrown exception.
     */
    void recievedException(ProcessorEvent pe);

    /**
     * Called if a message was sent during job execution.
     * @param pe The event. pe.getMessage() returns the
     * message.
     */
    void recievedMessage(ProcessorEvent pe);
}
