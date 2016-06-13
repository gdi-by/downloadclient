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

import java.net.URL;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;

import de.bayern.gdi.services.WFSMetaExtractor;

/** Fetches number of features from WFS paging infos. */
public class NumberMatched implements FeatureGuesser.Callback {

    private String getFeatureTmpl;
    private String user;
    private String password;

    private static final String XPATH_NUMBER_MATCHED
        = "/wfs:FeatureCollection/@numberMatched";

    public NumberMatched(String getFeatureTmpl) {
        this(getFeatureTmpl, null, null);
    }

    public NumberMatched(String getFeatureTmpl, String user, String password) {
        this.getFeatureTmpl = getFeatureTmpl;
        this.user = user;
        this.password = password;
    }

    /**
     * Returns a GetFeature URL for a given page.
     * @param start The start offset of the page.
     * @param maxFeatures Number of features of the page.
     * @return The GetFeature URL.
     */
    public String getFeatureURL(int start, int maxFeatures) {
        return getFeatureTmpl
            + "&startIndex=" + start
            + "&maxFeatures=" + maxFeatures;
    }

    /**
     * Returns a GetFeature hits URL for a given page.
     * @param start The start offset of the page.
     * @param maxFeatures Number of features of the page.
     * @return The GetFeature hits URL.
     */
    public String getHitsURL(int start, int maxFeatures) {
        return getFeatureURL(start, maxFeatures) + "&returnType=hits";
    }

    @Override
    public int numFeatures(int start, int maxFeatures) throws Exception {
        URL url = new URL(getHitsURL(start, maxFeatures));
        Document hitsDoc = XML.getDocument(url, user, password);
        if (hitsDoc == null) {
            throw new IllegalArgumentException("cannot load hits document");
        }
        String numberMatchedString = (String)XML.xpath(
            hitsDoc, XPATH_NUMBER_MATCHED,
            XPathConstants.STRING,
            WFSMetaExtractor.NAMESPACES);
        if (numberMatchedString == null) {
            throw new IllegalArgumentException("numberMatched not found");
        }
        return Integer.parseInt(numberMatchedString);
    }
}
