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


import de.bayern.gdi.services.Atom;
import de.bayern.gdi.services.WFSTwo;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bayern.gdi.gui.DataBean;
import de.bayern.gdi.gui.View;
import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.model.Parameter;
import de.bayern.gdi.model.ProcessingStep;
import de.bayern.gdi.services.ServiceType;

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
            serviceURL = serviceURL.substring(0, serviceURL.lastIndexOf("?"));
            //step.setServiceURL(bean.getWebService().getServiceURL());
            ServiceType serviceType =
                    bean.getWebService().getServiceType();
            //step.setServiceType(bean.getWebService().getServiceType());
            //step.setPath(savePath);
            Map<String, String> paramMap = bean.getAttributes();
            ArrayList<Parameter> parameters = new ArrayList<>(paramMap.size());
            for (Map.Entry<String, String> entry: paramMap.entrySet()) {
                if (!entry.getValue().equals("")) {
                    Parameter param = new Parameter(
                            entry.getKey(), entry.getValue());
                    parameters.add(param);
                }
            }
            //step.setParameters(parameters);
            String dataset = view.getTypeComboBox().getSelectionModel()
                    .getSelectedItem()
                    .toString();
            //step.setDataset(dataset);
            ArrayList<ProcessingStep> processingSteps = new ArrayList<>();
            //step.setProcessingSteps(processingSteps);
            //System.out.println(serviceType.toString());
            String serviceTypeStr = null;
            switch (serviceType) {
                case WFSOne:
                    serviceTypeStr = "WFS1";
                    break;
                case WFSTwo:
                    if (dataset.startsWith(WFSTwo.getSimplePrefix())) {
                        serviceTypeStr = "WFS2_SIMPLE";
                        dataset = dataset.substring(WFSTwo.getSimplePrefix()
                                .length() + 1);
                    } else {
                        serviceTypeStr = "WFS2_BASIC";
                        dataset = dataset.substring(WFSTwo.getBasicPrefix()
                                .length() + 1);
                    }
                    break;
                case Atom:
                    serviceTypeStr = "ATOM";
                    Atom atom = (Atom) bean.getWebService();
                    dataset = atom.getURLforType(dataset);
                    break;
                default:
            }
            DownloadStep step = new DownloadStep(dataset,
                    parameters,
                    serviceTypeStr,
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
