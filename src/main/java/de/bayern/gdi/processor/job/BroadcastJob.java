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

/**
 * A job broadcasting a message.
 */
public class BroadcastJob implements Job {

    private String msg;

    public BroadcastJob(String msg) {
        this.msg = msg;
    }

    @Override
    public void run(Processor p) throws JobExecutionException {
        if (p == null) {
            throw new JobExecutionException();
        }
        p.broadcastMessage(msg);
    }
}
