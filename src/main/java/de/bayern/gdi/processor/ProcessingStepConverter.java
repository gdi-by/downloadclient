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
import de.bayern.gdi.processor.ExternalProcessJob.DeltaGlob;
import de.bayern.gdi.processor.ExternalProcessJob.GlobalGlob;
import de.bayern.gdi.processor.ExternalProcessJob.UniqueArg;
import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.FileTracker;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Log;
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
     * @param logger The logger to log to.
     * @throws ConverterException If something went wrong.
     */
    public void convert(DownloadStep dls, FileTracker fileTracker, Log logger)
    throws ConverterException {

        List<ProcessingStep> steps = dls.getProcessingSteps();
        if (steps == null || steps.isEmpty()) {
            return;
        }

        jobs.add(new BroadcastJob(I18n.getMsg("processing_chain.start")));
        ProcessingConfiguration config =
            Config.getInstance().getProcessingConfig();

        for (ProcessingStep step: steps) {
            ProcessingStepConfiguration psc =
                config.findProcessingStepConfiguration(step.getName());
            if (psc == null) {
                throw new ConverterException(
                    I18n.format(
                        "processing_chain.no.config", step.getName()));
            }
            String command = psc.getCommand();
            if (command == null || command.isEmpty()) {
                throw new ConverterException(
                    I18n.format(
                        "processing_chain.no.command", step.getName()));
            }
            ArrayList<Arg> params = new ArrayList<>();

            parameters:
            for (ConfigurationParameter cp: psc.getParameters()) {
                String ext = cp.getExt();
                if (ext != null) { // The <Parameter ext="gml"/> case.
                    params.add(new UniqueArg(ext));
                    continue;
                }

                String glob = cp.getGlob();
                if (glob != null) {
                    switch (glob) {
                        case "delta":
                            params.add(new DeltaGlob(cp.getValue()));
                            break;
                        case "global":
                            params.add(new GlobalGlob(cp.getValue()));
                            break;
                        default:
                            throw new ConverterException(
                                I18n.format(
                                    "processing_chain.unknown.glob", glob));
                    }
                    continue;
                }

                String value = cp.getValue();
                if (value == null) {
                    continue;
                }
                value = value.trim();
                if (value.isEmpty()) {
                    continue;
                }
                String[] parts = StringUtils.splitCommandLine(value);

                ArrayList<Arg> row = new ArrayList<>();

                for (String part: parts) {
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
                                throw new ConverterException(I18n.format(
                                    "processing_chain.param.not.found",
                                    var));
                            }
                            // This parameter is incomplete -> skip it!
                            continue parameters;
                        }
                        usedVars.add(var);
                        row.add(new Arg(val));
                    } // for all atoms
                }
                params.addAll(row);
            } // for all config parameters.

            ExternalProcessJob epj = new ExternalProcessJob(
                command,
                fileTracker,
                params.toArray(new Arg[params.size()]),
                logger);

            jobs.add(epj);
            jobs.add(new BroadcastJob(I18n.getMsg("processing_chain.end")));
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
