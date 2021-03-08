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
package de.bayern.gdi.config;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ConfigTest {

    private static final int EXPECTED_DEFAULT_TIMEOUT = 35000;

    private static final int EXPECTED_TIMEOUT = 60000;

    /**
     * tmp folder containing the settings.xml.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File configFolder;

    /**
     * Copies the test configuration to the tmp folder.
     *
     * @throws IOException
     */
    @Before
    public void copyResourcesToTemporaryFolder() throws IOException {
        this.configFolder = folder.newFolder();
        File targetFile = new File(configFolder, "settings.xml");
        try (InputStream settingsSource = ConfigTest.class.
            getResourceAsStream("settings.xml");
             FileOutputStream settingsTarget = new
                 FileOutputStream(targetFile)) {
            IOUtils.copy(settingsSource, settingsTarget);
        }
    }

    /**
     * Tests the initialization of the default configuration.
     *
     * @throws IOException
     */
    @Test
    public void testInitializeDefault() throws IOException {
        Config.initialize(null);
        ApplicationSettings applicationSettings = Config.getInstance().getApplicationSettings();

        assertThat(applicationSettings.getRequestTimeoutInMs(), is(EXPECTED_DEFAULT_TIMEOUT));
        assertThat(applicationSettings.getCredentials(), is(nullValue()));
    }


    /**
     * Tests the initialization of the test configuration.
     *
     * @throws IOException
     */
    @Test
    public void testInitialize() throws IOException {
        Config.initialize(configFolder.getAbsolutePath());
        ApplicationSettings applicationSettings = Config.getInstance().getApplicationSettings();

        assertThat(applicationSettings.getRequestTimeoutInMs(), is(EXPECTED_TIMEOUT));
        assertThat(applicationSettings.getCredentials().getUsername(), is("name"));
        assertThat(applicationSettings.getCredentials().getPassword(), is("pw"));
    }

}
