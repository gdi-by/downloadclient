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
//12345678901234567890123456789012345678901234567890123456789012345678901234567890
    /**
     * FilterType (Filter or BBox).
     */
    public enum FilterType {
        /**
         * Filter.
         */
        FILTER("(Filter)"),
        /**
         * BBox.
         */
        BBOX("(BBOX)");

        private String labelSuffix;

        FilterType(String labelSuffix) {
            this.labelSuffix = labelSuffix;
        }

        /**
         * @return the label suffix of the filter type
         */
        public String getLabelSuffix() {
            return labelSuffix;
        }
    }

    private WFSMeta.Feature feature;

    private FilterType filterType;

    /**
     * Constructor to wrap.
     */
    public FeatureModel(WFSMeta.Feature f) {
        this.feature = f;
    }

    /**
     * Constructor to wrap.
     */
    public FeatureModel(WFSMeta.Feature f, FilterType filterType) {
        this.feature = f;
        this.filterType = filterType;
    }

    @Override
    public Object getItem() {
        return this.feature;
    }

    @Override
    public String getDataset() {
        return feature.getName();
    }

    /**
     * @return the encapsulated feature type
     */
    public WFSMeta.Feature getFeature() {
        return feature;
    }

    /**
     * @return the filter type, <code>null</code> if not supported
     */
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public String toString() {
        String prefix = this.feature.getTitle();
        if (prefix == null || prefix.isEmpty()) {
            prefix = this.feature.getName();
        }

        if (filterType != null) {
            return prefix + " " + filterType.labelSuffix;
        }
        return prefix;
    }

}
