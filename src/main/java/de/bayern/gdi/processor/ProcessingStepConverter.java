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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.bayern.gdi.model.ConfigurationParameter;
import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.model.ProcessingConfiguration;
import de.bayern.gdi.model.ProcessingStep;
import de.bayern.gdi.model.ProcessingStepConfiguration;
import de.bayern.gdi.processor.ExternalProcessJob.Arg;
import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.FileTracker;
import de.bayern.gdi.utils.StringUtils;

/** Converts processing steps to jobs of external program calls. */
public class ProcessingStepConverter {

    private Set<String> usedVars;
    private List<Job> jobs;

    public ProcessingStepConverter() {
        this.usedVars = new HashSet<>();
        this.jobs = new ArrayList<>();
    }

    /**
     * Converts the processing steps from the download step to
     * a list of jobs and appends the to the given list of jobs.
     * @param dls The download step.
     * @param fileTracker The file tracker for the external program calls.
     * @throws ConverterException If something went wrong.
     */
    public void convert(DownloadStep dls, FileTracker fileTracker)
    throws ConverterException {

        ArrayList<ProcessingStep> steps = dls.getProcessingSteps();
        if (steps == null) {
            return;
        }

        ProcessingConfiguration config =
            Config.getInstance().getProcessingConfig();

        for (ProcessingStep step: steps) {
            ProcessingStepConfiguration psc =
                config.findProcessingStepConfiguration(step.getName());
            if (psc == null) {
                // TODO: I18n
                throw new ConverterException(
                    "Cannot find config for " + step.getName());
            }
            String command = psc.getCommand();
            if (command == null || command.isEmpty()) {
                // TODO: I18n
                throw new ConverterException(
                    "config " + step.getName() + " has no command.");
            }
            ArrayList<Arg> params = new ArrayList<>();

            parameters:
            for (ConfigurationParameter cp: psc.getParameters()) {
                String value = cp.getValue();
                if (value == null) {
                    continue;
                }
                value = value.trim();
                if (value.isEmpty()) {
                    continue;
                }
                String[] parts = StringUtils.splitCommandLine(value);

                for (String part: parts) {

                    ArrayList<Arg> row = new ArrayList<>();

                    String[] atoms = StringUtils.split(
                        part, ConfigurationParameter.VARS_RE, true);

                    for (String atom: atoms) {
                        String var =
                            ConfigurationParameter.extractVariable(atom);

                        if (var == null) {
                            row.add(new Arg(atom));
                            continue;
                        }

                        String val = step.findParameter(var);
                        if (val == null) {
                            if (cp.isMandatory()) {
                                // TODO: I18n
                                throw new ConverterException(
                                    "Parameter " + var + " not found");
                            }
                            // This parameter is incomplete -> skip it!
                            continue parameters;
                        }
                        usedVars.add(var);
                        row.add(new Arg(val));
                    } // for all atoms

                    params.addAll(row);
                }
            } // for all config parameters.

            ExternalProcessJob epj = new ExternalProcessJob(
                command,
                fileTracker,
                params.toArray(new Arg[params.size()]));

            jobs.add(epj);
        }
    }

    /**
     * @return the usedVars
     */
    public Set<String> getUsedVars() {
        return usedVars;
    }

    /**
     * @return the jobs
     */
    public List<Job> getJobs() {
        return jobs;
    }
}
