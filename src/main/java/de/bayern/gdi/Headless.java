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
     * @param args     The command line arguments.
     * @param user     Optional user name.
     * @param password Optional user name.
     * @return Non zero if the operation fails.
     */
    public static int main(String[] args, String user, String password) {

        LOG.info("Running in headless mode");

        Unauthorized unauthorized = new UnauthorizedLog();
        DocumentResponseHandler.setUnauthorized(unauthorized);
        FileResponseHandler.setUnauthorized(unauthorized);

        List<DownloadStep> steps = new ArrayList<>();

        for (String arg : args) {
            File file = new File(arg);
            if (file.isFile() && file.canRead()) {
                try {
                    steps.add(DownloadStep.read(file));
                    LOG.info("Download steps: " + file.getName());
                } catch (IOException ioe) {
                    LOG.warn("Cannot load file: " + file.getName(), ioe);
                }
            } else {
                LOG.warn("'" + arg + "' is not a readable file.");
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

    private static List<DownloadStepJob> createJobs(String user, String password, List<DownloadStep> steps) {
        return steps.stream().map(step -> {
            try {
                DownloadStepConverter dsc =
                    new DownloadStepConverter(user, password);
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
    public void receivedException(ProcessorEvent pe) {
        JobExecutionException jee = pe.getException();
        LOG.error(jee.getMessage(), jee);
    }

    @Override
    public void jobFinished(ProcessorEvent pe) {
    }
}

