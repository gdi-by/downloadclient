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

import de.bayern.gdi.config.ApplicationSettings;
import de.bayern.gdi.config.Config;
import de.bayern.gdi.config.Credentials;
import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.processor.ConverterException;
import de.bayern.gdi.processor.DownloadStepConverter;
import de.bayern.gdi.processor.JobExecutionException;
import de.bayern.gdi.processor.Processor;
import de.bayern.gdi.processor.ProcessorEvent;
import de.bayern.gdi.processor.ProcessorListener;
import de.bayern.gdi.processor.job.DownloadStepJob;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * The command line tool.
 */
public class Headless implements ProcessorListener {

    private static final Logger LOG = LoggerFactory.getLogger(Headless.class);

    private Headless() {
    }

    /**
     * @param downloadFiles The command line arguments.
     * @param credentials   Optional credentials
     * @return Non zero if the operation fails.
     */
    public static int runHeadless(String[] downloadFiles, Credentials credentials) {

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

        return runHeadless(credentials, steps);
    }

    /**
     * Triggers the actual Headless Mode.
     *
     * @param credentials optional credentials
     * @param steps       downloadSteps
     * @return exit code
     */
    static int runHeadless(Credentials credentials,
                           List<DownloadStep> steps) {

        LOG.info("Executing download steps " + steps);
        List<DownloadStepJob> jobs = createJobs(user, password, steps);
        Processor processor = new Processor(jobs)
            .withListeners(new Headless());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            executorService.submit(processor);
        } finally {
            executorService.shutdown();
        }
        return 0;
    }

    private static List<DownloadStepJob> createJobs(Credentials credentials, List<DownloadStep> steps) {
        return steps.stream().map(step -> {
            try {
                DownloadStepConverter dsc = createDownloadStepConverter(credentials);;
                return dsc.convert(step);
            } catch (ConverterException ce) {
                LOG.warn("Creating download jobs failed", ce);
                return null;
            }
        }).filter(job -> job != null)
                    .collect(Collectors.toList());
    }

    @Override
    public void receivedMessage(ProcessorEvent pe) {
        LOG.info(pe.getMessage());
    }

    @Override
    public void processingFailed(ProcessorEvent pe) {
        JobExecutionException jee = pe.getException();
        LOG.error(jee.getMessage(), jee);
    }

    @Override
    public void processingFinished() {
    }

    private static DownloadStepConverter createDownloadStepConverter(Credentials credentials) {
        ApplicationSettings applicationSettings = Config.getInstance().getApplicationSettings();
        if (credentials != null) {
            applicationSettings.persistCredentials(credentials);
        }
        Credentials configuredCredentials = applicationSettings.getCredentials();
        if (configuredCredentials != null) {
            return new DownloadStepConverter(configuredCredentials.getUsername(), configuredCredentials.getPassword());
        }
        return new DownloadStepConverter();
    }

}

