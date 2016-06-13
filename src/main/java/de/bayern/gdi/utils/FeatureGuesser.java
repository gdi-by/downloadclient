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
package de.bayern.gdi.utils;

/** A number of features guesser for paged WFS. */
public class FeatureGuesser {

    private static final int DEFAULT_MAX_FEATURES = 10;

    /** How many features are fetched at a given start offset? */
    public interface Callback {
        /**
         * How many features a fetch when a page request from
         * given start offset is issued.
         * @param start The start offset.
         * @param maxFeatures Max features per page.
         * @return Number of features per page.
         * @throws Exception if something went wrong.
         */
        int numFeatures(int start, int maxFeatures) throws Exception;
    }

    private int maxFeatures;

    public FeatureGuesser() {
        this(DEFAULT_MAX_FEATURES);
    }

    /**
     * @param Max features per page.
     */
    public FeatureGuesser(int maxFeatures) {
        this.maxFeatures = maxFeatures;
    }

    /**
     * Figures out the total number of features with a given callback.
     * @param callback The callback used to find out how many offsets
     *                 are fetched at a given offset.
     * @return The total number of features.
     * @throws Exception if something went wrong.
     */
    public int totalNumFeatures(Callback callback) throws Exception {
        int numFeatures = callback.numFeatures(0, this.maxFeatures);
        if (numFeatures < this.maxFeatures) {
            return numFeatures;
        }

        int lo = numFeatures;
        int hi = lo * 2;
        for (;;) {
            numFeatures = callback.numFeatures(hi, this.maxFeatures);
            if (numFeatures == 0) {
                break;
            }
            if (numFeatures < this.maxFeatures) {
                return hi + this.maxFeatures;
            }
            lo = hi;
            hi *= 2;
        }

        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            numFeatures = callback.numFeatures(mid, maxFeatures);
            if (numFeatures == 0) {
                hi = mid - 1;
            } else if (numFeatures == this.maxFeatures) {
                lo = mid + this.maxFeatures + 1;
            } else {
                return mid + this.maxFeatures;
            }
        }

        return -1;
    }
}
