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

import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.processor.ConverterException;
import de.bayern.gdi.processor.DownloadStepConverter;
import de.bayern.gdi.processor.JobExecutionException;
import de.bayern.gdi.processor.Processor;
import de.bayern.gdi.processor.ProcessorEvent;
import de.bayern.gdi.processor.ProcessorListener;
import de.bayern.gdi.utils.DocumentResponseHandler;
import de.bayern.gdi.utils.FileResponseHandler;
import de.bayern.gdi.utils.Unauthorized;
import de.bayern.gdi.utils.UnauthorizedLog;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The command line tool.
 */
public class Headless implements ProcessorListener {

    private static final Logger log
        = Logger.getLogger(Headless.class.getName());

    private Headless() {
    }

    /**
     * @param args     The command line arguments.
     * @param user     Optional user name.
     * @param password Optional user name.
     * @return Non zero if the operation fails.
     */
    public static int main(String[] args, String user, String password) {

        log.info("Running in headless mode");

        Unauthorized unauthorized = new UnauthorizedLog();
        DocumentResponseHandler.setUnauthorized(unauthorized);
        FileResponseHandler.setUnauthorized(unauthorized);

        List<DownloadStep> steps = new ArrayList<>();

        for (String arg : args) {
            File file = new File(arg);
            if (file.isFile() && file.canRead()) {
                try {
                    steps.add(DownloadStep.read(file));
                    log.info(() -> "Download steps: " + file.getName());
                } catch (IOException ioe) {
                    log.log(
                        Level.WARNING,
                        "Cannot load file: " + file.getName(), ioe);
                }
            } else {
                log.log(Level.WARNING,
                    () -> "'" + arg + "' is not a readable file.");
            }
        }

        return runHeadless(user, password, steps);
    }

    /**
     * Triggers the actual Headless Mode.
     *
     * @param user     username
     * @param password password
     * @param steps    downloadSteps
     * @return exit code
     */
    static int runHeadless(String user,
                                   String password,
                                   List<DownloadStep> steps) {
        Processor processor = new Processor();
        processor.addListener(new Headless());
        Thread thread = new Thread(processor);
        thread.start();

        for (DownloadStep step : steps) {
            try {
                DownloadStepConverter dsc =
                    new DownloadStepConverter(user, password);
                processor.addJob(dsc.convert(step));
            } catch (ConverterException ce) {
                log.log(
                    Level.WARNING,
                    "Creating download jobs failed",
                    ce);
            }
        }

        processor.addJob(Processor.QUIT_JOB);

        try {
            thread.join();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return 1;
        }

        return 0;
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
}

