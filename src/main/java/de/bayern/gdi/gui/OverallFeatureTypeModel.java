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
package de.bayern.gdi.gui;

import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.utils.I18n;

import java.util.List;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class OverallFeatureTypeModel implements ItemModel {

    private List<WFSMeta.Feature> features;

    /**
     * Constructor.
     *
     * @param features encapsulated feature types
     */
    public OverallFeatureTypeModel(List<WFSMeta.Feature> features) {
        this.features = features;
    }

    @Override
    public Object getItem() {
        return features;
    }

    @Override
    public String getDataset() {
        return I18n.getMsg("typ.overall.query");
    }

    @Override
    public String toString() {
        return getDataset();
    }
}
