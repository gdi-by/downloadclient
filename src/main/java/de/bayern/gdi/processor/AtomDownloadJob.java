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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.bayern.gdi.gui.Controller;
import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.DocumentResponseHandler;
import de.bayern.gdi.utils.HTTP;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Log;
import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.StringUtils;
import de.bayern.gdi.utils.XML;

/** AtomDownloadJob is a job to download things from a ATOM service. */
public class AtomDownloadJob extends MultipleFileDownloadJob {

    private static final String XPATH_LINKS =
        "//atom:entry[atom:id/text()=$VARIATION]/atom:link";

    private String url;
    private String dataset;
    private String variation;
    private File workingDir;

    public AtomDownloadJob() {
    }

    public AtomDownloadJob(
        String url,
        String dataset,
        String variation,
        File workingDir,
        String user,
        String password,
        Log logger
    ) {
        super(user, password, logger);
        this.url = url;
        this.dataset = dataset;
        this.variation = variation;
        this.workingDir = workingDir;
    }

    private Document getDocument(String urlString)
        throws JobExecutionException {
        return getDocument(toURL(urlString));
    }

    private Document getDocument(URL docURL)
        throws JobExecutionException {

        CloseableHttpClient client = getClient(docURL);
        HttpGet httpget = getGetRequest(docURL);

        try {
            DocumentResponseHandler responseHandler
                = new DocumentResponseHandler();
            responseHandler.setNamespaceAware(true);

            Document document = client.execute(httpget, responseHandler);
            if (document == null) {
                throw new JobExecutionException(
                    I18n.format("atom.bad.xml", docURL.toString()));
            }
            return document;
        } catch (IOException ioe) {
            throw new JobExecutionException(
                I18n.format("atom.bad.download", docURL.toString()), ioe);
        } finally {
            Controller.logToAppLog("Atom download request:\n" + docURL);
            HTTP.closeGraceful(client);
        }
    }

    private static String mimetypeToExt(String type) {
        return Config.getInstance().getMimeTypes().findExtension(type, "gml");
    }

    private static final String DATASOURCE_XPATH
        = "/atom:feed/atom:entry[atom:id/text()=$CODE or"
        + " inspire_dls:spatial_dataset_identifier_code/text()=$CODE]"
        + "/atom:link[not(boolean(@type)) or"
        + " @type='application/atom+xml']/@href";

    private static final NamespaceContext NAMESPACE_CONTEXT =
        new NamespaceContextMap(
        "atom", "http://www.w3.org/2005/Atom",
        "inspire_dls", "http://inspire.ec.europa.eu/schemas/inspire_dls/1.0",
        "georss", "http://www.georss.org/georss");

    private String figureoutDatasource() throws JobExecutionException {

        Document doc = getDocument(this.url);
        HashMap<String, String> vars = new HashMap<>();
        vars.put("CODE", this.dataset);

        String ds = (String)XML.xpath(doc,
            DATASOURCE_XPATH, XPathConstants.STRING,
            NAMESPACE_CONTEXT, vars);

        if (ds == null || ds.isEmpty()) {
            throw new JobExecutionException(
                I18n.format("atom.dataset.not.found", this.dataset));
        }
        return ds;
    }

    @Override
    protected void download() throws JobExecutionException {
        String dsURL = figureoutDatasource();
        URL root = absoluteURL(this.url, dsURL);
        Document ds = getDocument(root);
        HashMap<String, String> vars = new HashMap<>();
        vars.put("VARIATION", this.variation);
        NodeList nl = (NodeList)XML.xpath(
            ds, XPATH_LINKS, XPathConstants.NODESET,
            NAMESPACE_CONTEXT, vars);

        ArrayList<DLFile> files = new ArrayList<>(nl.getLength());

        String format = "%0" + StringUtils.places(nl.getLength()) + "d.%s";
        for (int i = 0, j = 0, n = nl.getLength(); i < n; i++) {
            Element link = (Element)nl.item(i);
            String href = link.getAttribute("href");
            if (href.isEmpty()) {
                continue;
            }
            URL dataURL = absoluteURL(root, href);
            String fileName;
            // Service call?
            if (dataURL.getQuery() != null) {
                String type = link.getAttribute("type");
                String ext = mimetypeToExt(type);
                fileName = String.format(format, j, ext);
                j++;
            } else { // Direct download.
                // XXX: Do more to prevent directory traversals?
                fileName = new File(dataURL.getPath())
                    .getName().replaceAll("\\.+", ".");

                if (fileName.isEmpty()) {
                    String type = link.getAttribute("type");
                    String ext = mimetypeToExt(type);
                    fileName = String.format(format, j, ext);
                    j++;
                }
            }
            File file = new File(this.workingDir, fileName);
            files.add(new DLFile(file, dataURL));
        }

        downloadFiles(files);
    }
}
