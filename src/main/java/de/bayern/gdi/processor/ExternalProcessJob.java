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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.bayern.gdi.utils.I18n;

/**
 * Starts an external process with optional arguments and
 * an optional working directory.
 */
public class ExternalProcessJob implements Job {

    private String command;
    private File workingDir;
    private String [] arguments;

    public ExternalProcessJob() {
    }

    public ExternalProcessJob(
        String command, File workingDir, String [] arguments) {

        this.command = command;
        this.workingDir = workingDir;
        this.arguments = arguments;
    }

    private List<String> commandList() {
        int n = this.arguments != null
            ? this.arguments.length
            : 0;
        List<String> list = new ArrayList<String>(n + 1);
        list.add(command);
        if (n > 0) {
            for (String argument: arguments) {
                list.add(argument);
            }
        }
        return list;
    }

    /**
     * Runs the external process.
     * @throws JobExecutionException Thrown
     *         if the external proccess could not be started.
     */
    @Override
    public void run(Processor p) throws JobExecutionException {
        ProcessBuilder builder = new ProcessBuilder(commandList());
        if (this.workingDir != null) {
            builder.directory(this.workingDir);
        }

        if (p != null) {
            p.broadcastMessage(
                I18n.format("external.process.start", command));
        }

        try {
            Process process = builder.start();
            // XXX: Implement some kind of cancellation mechanism.
            int exitcode = process.waitFor();
            if (exitcode != 0) {
                throw new JobExecutionException(
                    I18n.format("external.process.error", command, exitcode));
            }
        } catch (IOException | InterruptedException e) {
            throw new JobExecutionException(
                I18n.format("external.process.failed", command), e);
        }

        if (p != null) {
            p.broadcastMessage(
                I18n.format("external.process.end", command));
        }
    }
}
