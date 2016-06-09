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
import java.util.ArrayList;

import de.bayern.gdi.model.ConfigurationParameter;
import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.model.ProcessingConfiguration;
import de.bayern.gdi.model.ProcessingStep;
import de.bayern.gdi.model.ProcessingStepConfiguration;
import de.bayern.gdi.utils.StringUtils;

/** Converts processing steps to jobs of external program calls. */
public class ProcessingStepConverter {

    private ProcessingConfiguration config;

    public ProcessingStepConverter(ProcessingConfiguration config) {
        this.config = config;
    }

    /**
     * Converts the processing steps from the download step to
     * a list of jobs and appends the to the given list of jobs.
     * @param dls The download step.
     * @param jl The list of job to append on.
     * @param workingDir The working directory of the external program calls.
     * @throws ConverterException If something went wrong.
     */
    public void convert(
        DownloadStep dls,
        JobList      jl,
        File workingDir
    ) throws ConverterException {
        ArrayList<ProcessingStep> steps = dls.getProcessingSteps();
        if (steps == null) {
            return;
        }

        for (ProcessingStep step: steps) {
            ProcessingStepConfiguration psc =
                this.config.findProcessingStepConfiguration(step.getName());
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
            ArrayList<String> params = new ArrayList<>();

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

                    ArrayList<String> row = new ArrayList<>();

                    String[] atoms = StringUtils.split(
                        part, ConfigurationParameter.VARS_RE, true);

                    for (String atom: atoms) {
                        String var =
                            ConfigurationParameter.extractVariable(atom);

                        if (var == null) {
                            row.add(atom);
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
                    } // for all atoms

                    params.addAll(row);
                }
            } // for all config parameters.

            ExternalProcessJob epj = new ExternalProcessJob(
                command,
                workingDir,
                params.toArray(new String[params.size()]));

            jl.addJob(epj);
        }
    }
}
