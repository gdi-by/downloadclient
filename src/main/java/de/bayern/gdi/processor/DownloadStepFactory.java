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

import de.bayern.gdi.gui.DataBean;
import de.bayern.gdi.gui.View;
import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.model.Parameter;
import de.bayern.gdi.model.ProcessingStep;
import de.bayern.gdi.services.WebService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class DownloadStepFactory {

    private static DownloadStepFactory instance;

    private static final Logger log
            = Logger.getLogger(DownloadStepFactory.class.getName());

    private DownloadStepFactory() {

    }

    /**
     * gets the instance of the Factory.
     * @return DownloadstepFactory
     */
    public static synchronized DownloadStepFactory getInstance() {
        if (DownloadStepFactory.instance == null) {
            DownloadStepFactory.instance = new DownloadStepFactory();
        }
        return DownloadStepFactory.instance;
    }

    /**
     * gets Downloadstep from Frontend.
     * @param view the view
     * @param bean the databean
     * @param savePath the save path
     * @return downloadStep
     */
    public DownloadStep getStep(View view, DataBean bean, String savePath) {
        try {

            //DownloadStep step = new DownloadStep();
            String serviceURL = bean.getWebService().getServiceURL();
            //step.setServiceURL(bean.getWebService().getServiceURL());
            WebService.Type serviceType =
                    bean.getWebService().getServiceType();
            //step.setServiceType(bean.getWebService().getServiceType());
            //step.setPath(savePath);
            ArrayList<Parameter> parameters = new ArrayList<>();
            Map<String, String> paramMap = bean.getAttributes();
            Iterator paramMapIT = paramMap.entrySet().iterator();
            while (paramMapIT.hasNext()) {
                Map.Entry pair = (Map.Entry) paramMapIT.next();
                Parameter param = new Parameter(
                        (String) pair.getKey(),
                        (String) pair.getValue());
                parameters.add(param);
            }
            //step.setParameters(parameters);
            String dataset = view.getTypeComboBox().getSelectionModel()
                    .getSelectedItem()
                    .toString();
            //step.setDataset(dataset);
            ArrayList<ProcessingStep> processingSteps = new ArrayList<>();
            //step.setProcessingSteps(processingSteps);
            DownloadStep step = new DownloadStep(dataset,
                    parameters,
                    serviceType,
                    serviceURL,
                    savePath,
                    processingSteps);
            return step;
        } catch (Exception ex) {
            log.log(Level.SEVERE, ex.getMessage() , ex);
        }
        return null;
    }
}