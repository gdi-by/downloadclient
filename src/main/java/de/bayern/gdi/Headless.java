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
package de.bayern.gdi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.processor.ConverterException;
import de.bayern.gdi.processor.DownloadStepConverter;
import de.bayern.gdi.processor.JobExecutionException;
import de.bayern.gdi.processor.JobList;
import de.bayern.gdi.processor.Processor;
import de.bayern.gdi.processor.ProcessorEvent;
import de.bayern.gdi.processor.ProcessorListener;
import de.bayern.gdi.utils.Unauthorized;
import de.bayern.gdi.utils.UnauthorizedLog;
import de.bayern.gdi.utils.DocumentResponseHandler;
import de.bayern.gdi.utils.FileResponseHandler;

/**
 * The command line tool.
 */
public class Headless implements ProcessorListener {

    private static final Logger log
        = Logger.getLogger(Headless.class.getName());

    private Headless() {
    }

    @Override
    public void receivedMessage(ProcessorEvent pe) {
        log.info(pe.getMessage());
    }

    @Override
    public void receivedException(ProcessorEvent pe) {
        JobExecutionException jee = pe.getException();
        log.log(Level.SEVERE, jee.getMessage(), jee);
    }

    /**
     * @param args The command line arguments.
     * @param user Optional user name.
     * @param password Optional user name.
     * @return Non zero if the operation fails.
     */
    public static int main(String [] args, String user, String password) {

        log.info("Running in headless mode");

        Unauthorized unauthorized = new UnauthorizedLog();
        DocumentResponseHandler.setUnauthorized(unauthorized);
        FileResponseHandler.setUnauthorized(unauthorized);

        ArrayList<File> files = new ArrayList<>();

        for (String arg: args) {
            if (!arg.startsWith("-")) {
                File file = new File(arg);
                if (file.isFile() && file.canRead()) {
                    files.add(file);
                } else {
                    log.log(
                        Level.WARNING, "'" + arg + "' is not a readable file.");
                }
            }
        }

        Processor processor = new Processor();
        processor.addListener(new Headless());
        Thread thread = new Thread(processor);
        thread.start();

        for (File file: files) {
            DownloadStep dls;
            try {
                dls = DownloadStep.read(file);
            } catch (IOException ioe) {
                log.log(
                    Level.WARNING,
                    "Cannot load file: " + file, ioe);
                continue;
            }
            log.info("Download steps: " + dls);
            JobList jobs;

            DownloadStepConverter dsc =
                new DownloadStepConverter(user, password);

            try {
                jobs = dsc.convert(dls);
            } catch (ConverterException ce) {
                log.log(
                    Level.WARNING,
                    "Creating download jobs failed",
                    ce);
                continue;
            }
            processor.addJob(jobs);
        }

        processor.addJob(Processor.QUIT);

        try {
            thread.join();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return 1;
        }

        return 0;
    }
}

