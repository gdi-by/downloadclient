/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bayern.gdi;

import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.services.WFSMetaExtractor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import static java.util.concurrent.TimeUnit.SECONDS;
import junit.framework.TestCase;
import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadler;

import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author weich_ju
 */
public class WfsTest extends TestCase {

    public WfsTest(String testName) {
        super(testName);
    }

    @Before
    @Override
    public void setUp() {
        initJadler();
    }

    @After
    @Override
    public void tearDown() {
        closeJadler();
    }

    @Test
    public void testInspireDemoServer() throws IOException {
        run("/geoserver/wfs", "/geoserver.xml");
    }
    
    
    private void run(String queryPath, String queryResource) 
                throws IOException {
        
        InputStream is = 
                WfsTest.class.getResourceAsStream(queryResource);

        onRequest()
                .havingMethodEqualTo("GET")
                .havingPathEqualTo(queryPath)
                // .havingBody(isEmptyOrNullString())
                // .havingHeaderEqualTo("Accept", "application/xml")
                .respond()
                 // .withDelay(1, SECONDS)
                .withStatus(200)
                .withBody(is)
                .withEncoding(Charset.forName("UTF-8"))
                .withContentType("application/xml; charset=UTF-8");
        
        
        StringBuilder sb = new StringBuilder();
        sb.append("http://localhost:");
        sb.append(port());
        sb.append(queryPath);
        
        System.out.println(sb.toString());
      
        
        WFSMeta wfsMeta = 
                new WFSMetaExtractor(sb.toString()).parse();
        
        System.out.println(wfsMeta.title);

    }

}
