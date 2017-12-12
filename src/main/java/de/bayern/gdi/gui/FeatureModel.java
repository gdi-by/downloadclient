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

/**
 * Model class to display WFS features.
 */
public class FeatureModel implements ItemModel {

    private WFSMeta.Feature feature;

    /**
     * Contructor to wrap.
     */
    public FeatureModel(WFSMeta.Feature f) {
        this.feature = f;
    }

    public Object getItem() {
        return this.feature;
    }

    public String getDataset() {
        return feature.getName();
    }

    @Override
    public String toString() {
        String title = this.feature.getTitle();
        return title != null && !title.isEmpty()
            ? title
            : this.feature.getName();
    }

    public WFSMeta.Feature getFeature() {
        return feature;
    }
}
