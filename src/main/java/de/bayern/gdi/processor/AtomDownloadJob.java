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
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPathConstants;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.bayern.gdi.utils.DocumentResponseHandler;
import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.XML;

/** AtomDownloadJob is a job to download things from a ATOM service. */
public class AtomDownloadJob extends AbstractDownloadJob {

    private static final Logger log
        = Logger.getLogger(AtomDownloadJob.class.getName());

    private String dataset;
    private String variation;
    private File workingDir;

    private long total;

    private Processor processor;

    public AtomDownloadJob() {
    }

    public AtomDownloadJob(
        String dataset,
        String variation,
        File workingDir,
        String user,
        String password
    ) {
        super(user, password);
        this.dataset = dataset;
        this.variation = variation;
        this.workingDir = workingDir;
    }

    @Override
    public void bytesCounted(long count) {
        this.total += count;
    }

    private Document getDocument(String urlString)
        throws JobExecutionException {

        CloseableHttpClient client = getClient(toURL(urlString));
        HttpGet httpget = new HttpGet(urlString);

        try {
            DocumentResponseHandler responseHandler
                = new DocumentResponseHandler();
            responseHandler.setNamespaceAware(true);

            Document document = client.execute(httpget, responseHandler);
            if (document == null) {
                throw new JobExecutionException("Cannot parse as XML");
            }
            return document;
        } catch (IOException ioe) {
            throw new JobExecutionException("Download failed", ioe);
        } finally {
            try {
                client.close();
            } catch (IOException ioe) {
                log.log(Level.SEVERE, "Cannot close HTTP client.", ioe);
            }
        }
    }

    private static final String XPATH_LINKS =
        "//atom:entry[atom:id/text()=$VARIATION]/atom:link";

    private static final String[][] MINETYPE2EXT = {
        {"image/tiff", "tiff"},
        {"image/jpeg", "jpg"},
        {"image/png", "png"},
        {"image/gif", "gif"},
        {"application/pdf", "pdf"},
        {"text/xml", "xml"}
        // TODO: Add more.
    };

    private static String minetypeToExt(String type) {
        type = type.toLowerCase();
        for (String[] pair: MINETYPE2EXT) {
            if (pair[0].equals(type)) {
                return pair[1];
            }
        }
        return "dat";
    }

    @Override
    protected void download() throws JobExecutionException {
        Document ds = getDocument(this.dataset);
        HashMap<String, String> vars = new HashMap<>();
        vars.put("VARIATION", this.variation);
        NamespaceContextMap nsm
            = new NamespaceContextMap("atom", "http://www.w3.org/2005/Atom");
        NodeList nl = (NodeList)XML.xpath(
            ds, XPATH_LINKS, XPathConstants.NODESET, nsm, vars);

        log.log(Level.INFO, "" + nl.getLength());
        for (int i = 0, n = nl.getLength(); i < n; i++) {
            Node node = nl.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node href = attributes.getNamedItem("href");
            Node type = attributes.getNamedItem("type");
            if (href == null || type == null) {
                continue;
            }
            String ext = minetypeToExt(type.getTextContent());
            String url = href.getTextContent();
            log.log(Level.INFO, ext + " " + url);
        }
    }
}
