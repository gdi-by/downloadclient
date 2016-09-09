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

import java.util.List;

import de.bayern.gdi.model.Parameter;
import de.bayern.gdi.model.DownloadStep;

import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Log;

/**
 * A job to log the meta data of a download.
 */
public class LogMetaJob implements Job {

    private Log          logger;
    private DownloadStep dls;

    public LogMetaJob(Log logger, DownloadStep dls) {
        this.logger = logger;
        this.dls    = dls;
    }

    @Override
    public void run(Processor p) throws JobExecutionException {
        if (this.logger == null) {
            return;
        }

        this.logger.log(I18n.format("meta.log.service", dls.getServiceType()));
        this.logger.log(I18n.format("meta.log.url", dls.getServiceURL()));
        this.logger.log(I18n.format("meta.log.dataset", dls.getDataset()));
        List<Parameter> parameters = dls.getParameters();
        if (parameters != null && !parameters.isEmpty()) {
            this.logger.log(I18n.getMsg("meta.log.parameters"));
            for (Parameter para: parameters) {
                this.logger.log(
                    "\t" + para.getKey() + ": " + para.getValue());
            }
        }
    }
}
