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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.bayern.gdi.utils.CountingInputStream;
import de.bayern.gdi.utils.DocumentResponseHandler;
import de.bayern.gdi.utils.FileResponseHandler;
import de.bayern.gdi.utils.HTTP;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.WrapInputStreamFactory;
import de.bayern.gdi.utils.XML;

/** AtomDownloadJob is a job to download things from a ATOM service. */
public class AtomDownloadJob extends AbstractDownloadJob {

    private static final Logger log
        = Logger.getLogger(AtomDownloadJob.class.getName());

    private static final String XPATH_LINKS =
        "//atom:entry[atom:id/text()=$VARIATION]/atom:link";

    private static final Properties
        MINETYPE2EXT = loadMine2Ext();

    private String url;
    private String dataset;
    private String variation;
    private File workingDir;

    private long currentCount;
    private long totalCount;

    public AtomDownloadJob() {
    }

    public AtomDownloadJob(
        String url,
        String dataset,
        String variation,
        File workingDir,
        String user,
        String password
    ) {
        super(user, password);
        this.url = url;
        this.dataset = dataset;
        this.variation = variation;
        this.workingDir = workingDir;
    }

    private static Properties loadMine2Ext() {
        InputStream in = null;
        try {
            in = AtomDownloadJob.class.getResourceAsStream(
                "mine2ext.properties");
            if (in == null) {
                throw new RuntimeException("resource not found");
            }
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    @Override
    public void bytesCounted(long count) {
        broadcastMessage(
            I18n.format("atom.bytes.downloaded", this.totalCount + count));
        this.currentCount = count;
    }

    private Document getDocument(String urlString)
        throws JobExecutionException {

        URL docURL = toURL(urlString);
        CloseableHttpClient client = getClient(docURL);
        HttpGet httpget = getGetRequest(docURL);

        try {
            DocumentResponseHandler responseHandler
                = new DocumentResponseHandler();
            responseHandler.setNamespaceAware(true);

            Document document = client.execute(httpget, responseHandler);
            if (document == null) {
                throw new JobExecutionException(
                    I18n.format("atom.bad.xml", urlString));
            }
            return document;
        } catch (IOException ioe) {
            throw new JobExecutionException(
                I18n.format("atom.bad.download", urlString), ioe);
        } finally {
            HTTP.closeGraceful(client);
        }
    }

    private static String minetypeToExt(String type) {
        type = type.toLowerCase();
        return MINETYPE2EXT.getProperty(type, "dat");
    }

    private static final int TEN = 10;

    private static int places(int n) {
        int places = 1;
        for (int value = TEN; n > value; value *= TEN) {
            places++;
        }
        return places;
    }

    /** Stores a file location to down from and to. */
    private static class DLFile {

        /** Destination location of the file. */
        File file;
        /** The url to download from. */
        URL url;
        /** The number of tries yet. */
        int tries;

        DLFile(File file, URL url) {
            this.file = file;
            this.url = url;
        }
    }

    private boolean downloadFile(DLFile dlf) throws JobExecutionException {

        log.log(Level.INFO,
            "Downloading '" + dlf.url + "' to '" + dlf.file + "'");
        this.currentCount = 0;

        CloseableHttpClient client = getClient(dlf.url);
        HttpGet httpget = getGetRequest(dlf.url);

        WrapInputStreamFactory wrapFactory
            = CountingInputStream.createWrapFactory(this);

        try {
            FileResponseHandler frh
                = new FileResponseHandler(dlf.file, wrapFactory);
            client.execute(httpget, frh);
            return true;
        } catch (IOException ioe) {
            return false;
        } finally {
            HTTP.closeGraceful(client);
            this.totalCount += this.currentCount;
        }
    }

    private static final int MAX_TRIES = 5;
    private static final long FAIL_SLEEP = 30 * 1000;

    private static final String DATASOURCE_XPATH
        = "/atom:feed/atom:entry[atom:id/text()=$CODE or"
        + " inspire_dls:spatial_dataset_identifier_code/text()=$CODE]"
        + "/atom:link[@type='application/atom+xml']/@href";

    private static final NamespaceContext NAMESPACE_CONTEXT =
        new NamespaceContextMap(
        "atom", "http://www.w3.org/2005/Atom",
        "inspire_dls", "http://inspire.ec.europa.eu/schemas/inspire_dls/1.0",
        "georss", "http://www.georss.org/georss");

    private String figureoutDatasource() throws JobExecutionException {
        HashMap<String, String> vars = new HashMap<>();
        vars.put("CODE", this.dataset);

        String ds = (String)XML.xpath(getDocument(this.url),
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
        Document ds = getDocument(figureoutDatasource());
        HashMap<String, String> vars = new HashMap<>();
        vars.put("VARIATION", this.variation);
        NodeList nl = (NodeList)XML.xpath(
            ds, XPATH_LINKS, XPathConstants.NODESET,
            NAMESPACE_CONTEXT, vars);

        ArrayList<DLFile> files = new ArrayList<>(nl.getLength());

        String format = "%0" + places(nl.getLength()) + "d.%s";
        for (int i = 0, j = 0, n = nl.getLength(); i < n; i++) {
            Element link = (Element)nl.item(i);
            String href = link.getAttribute("href");
            if (href.isEmpty()) {
                continue;
            }
            URL dataURL = toURL(href);
            String fileName;
            // Service call?
            if (dataURL.getQuery() != null) {
                String type = link.getAttribute("type");
                String ext = minetypeToExt(type);
                fileName = String.format(format, j, ext);
                j++;
            } else { // Direct download.
                // XXX: Do more to prevent directory traversals?
                fileName = new File(dataURL.getPath())
                    .getName().replaceAll("\\.+", ".");

                if (fileName.isEmpty()) {
                    String type = link.getAttribute("type");
                    String ext = minetypeToExt(type);
                    fileName = String.format(format, j, ext);
                    j++;
                }
            }
            File file = new File(this.workingDir, fileName);
            files.add(new DLFile(file, dataURL));
        }

        int failed = 0;
        int numFiles = files.size();

        for (;;) {
            for (int i = 0; i < files.size();) {
                DLFile file = files.get(i);
                if (downloadFile(file)) {
                    files.remove(i);
                } else {
                    if (++file.tries < MAX_TRIES) {
                        i++;
                    } else {
                        failed++;
                        files.remove(i);
                    }
                }
                broadcastMessage(
                    I18n.format(
                        "atom.downloaded.files",
                        numFiles - failed - files.size(),
                        files.size()));
            }
            if (files.isEmpty()) {
                break;
            }
            try {
                Thread.sleep(FAIL_SLEEP);
            } catch (InterruptedException ie) {
                break;
            }
        }

        log.log(Level.INFO, "Bytes downloaded: " + this.totalCount);

        if (failed > 0) {
            throw new JobExecutionException(
                I18n.format("atom.downloaded.failed",
                    numFiles - failed, failed));
        }

        broadcastMessage(
            I18n.format("atom.downloaded.success", numFiles));
    }
}
