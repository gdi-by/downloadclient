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

import de.bayern.gdi.config.Config;
import de.bayern.gdi.config.Credentials;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The command line tool.
 */
public class Headless implements ProcessorListener {

    private static final Logger LOG = LoggerFactory.getLogger(Headless.class);

    private Headless() {
    }

    /**
     * @param downloadFiles The command line arguments.
     * @param user          Optional user name.
     * @param password      Optional user name.
     * @return Non zero if the operation fails.
     */
    public static int runHeadless(String[] downloadFiles, String user, String password) {

        LOG.info("Running in headless mode");

        Unauthorized unauthorized = new UnauthorizedLog();
        DocumentResponseHandler.setUnauthorized(unauthorized);
        FileResponseHandler.setUnauthorized(unauthorized);

        List<DownloadStep> steps = new ArrayList<>();

        for (String downloadFile : downloadFiles) {
            File file = new File(downloadFile);
            if (file.isFile() && file.canRead()) {
                try {
                    steps.add(DownloadStep.read(file));
                    LOG.info("Download steps: {}", file.getName());
                } catch (IOException ioe) {
                    LOG.warn("Cannot load file: " + file.getName(), ioe);
                }
            } else {
                LOG.warn("'{}' is not a readable file.", downloadFile);
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

        LOG.info("Executing download steps " + steps);

        for (DownloadStep step : steps) {
            try {
                DownloadStepConverter dsc = createDownloadStepConverter(user, password);
                processor.addJob(dsc.convert(step));
            } catch (ConverterException ce) {
                LOG.warn("Creating download jobs failed", ce);
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
        LOG.info(pe.getMessage());
    }

    @Override
    public void receivedException(ProcessorEvent pe) {
        JobExecutionException jee = pe.getException();
        LOG.error(jee.getMessage(), jee);
    }

    private static DownloadStepConverter createDownloadStepConverter(String user, String password) {
        if (user != null) {
            return new DownloadStepConverter(user, password);
        }
        Credentials configuredCredentials = Config.getInstance().getApplicationSettings().getCredentials();
        if (configuredCredentials != null) {
            return new DownloadStepConverter(configuredCredentials.getUsername(), configuredCredentials.getPassword());
        }
        return new DownloadStepConverter();
    }

}

