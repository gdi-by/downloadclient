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

    private File configFolderWithCredentials;

    private File configFolderPersistTest;

    private File configFolderWithCredentialsPersistTest;

    /**
     * Copies the test configuration to the tmp folder.
     *
     * @throws IOException
     */
    @Before
    public void copyResourcesToTemporaryFolder() throws IOException {
        this.configFolder = folder.newFolder();
        this.configFolderWithCredentials = folder.newFolder();
        this.configFolderPersistTest = folder.newFolder();
        this.configFolderWithCredentialsPersistTest = folder.newFolder();
        writeToConfigDirectory("settings.xml", this.configFolder);
        writeToConfigDirectory("settings-withCredentials.xml", this.configFolderWithCredentials);
        writeToConfigDirectory("settings.xml", this.configFolderPersistTest);
        writeToConfigDirectory("settings-withCredentials.xml", this.configFolderWithCredentialsPersistTest);
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
    public void testInitializeSettingsWithCredentials() throws IOException {
        Config.initialize(configFolderWithCredentials.getAbsolutePath());
        ApplicationSettings applicationSettings = Config.getInstance().getApplicationSettings();

        assertThat(applicationSettings.getRequestTimeoutInMs(), is(EXPECTED_TIMEOUT));
        assertThat(applicationSettings.getCredentials().getUsername(), is("name"));
        assertThat(applicationSettings.getCredentials().getPassword(), is("pw"));
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
        assertThat(applicationSettings.getCredentials(), is(nullValue()));
    }

    /**
     * Tests the persisting of credentials if the settings.xml does not contain credentials.
     *
     * @throws IOException
     */
    @Test
    public void testPersistCredentials() throws IOException {
        Config.initialize(configFolderPersistTest.getAbsolutePath());
        ApplicationSettings applicationSettings = Config.getInstance().getApplicationSettings();
        String newUsername = "newuser";
        String newPassword = "newpw";
        applicationSettings.persistCredentials(new Credentials(newUsername, newPassword));

        Config.initialize(configFolderPersistTest.getAbsolutePath());
        ApplicationSettings reinitializedApplicationSettings = Config.getInstance().getApplicationSettings();
        assertThat(reinitializedApplicationSettings.getCredentials().getUsername(), is(newUsername));
        assertThat(reinitializedApplicationSettings.getCredentials().getPassword(), is(newPassword));
    }

    /**
     * Tests the overwriting of credentials.
     *
     * @throws IOException
     */
    @Test
    public void testPersistCredentialsOverwrite() throws IOException {
        Config.initialize(configFolderWithCredentialsPersistTest.getAbsolutePath());
        ApplicationSettings applicationSettings = Config.getInstance().getApplicationSettings();
        String newUsername = "newuser";
        String newPassword = "newpw";
        applicationSettings.persistCredentials(new Credentials(newUsername, newPassword));

        Config.initialize(configFolderWithCredentialsPersistTest.getAbsolutePath());
        ApplicationSettings reinitializedApplicationSettings = Config.getInstance().getApplicationSettings();
        assertThat(reinitializedApplicationSettings.getCredentials().getUsername(), is(newUsername));
        assertThat(reinitializedApplicationSettings.getCredentials().getPassword(), is(newPassword));
    }

    private void writeToConfigDirectory(String resourceToCopy, File targetFolder) throws IOException {
        File targetFile = new File(targetFolder, "settings.xml");
        try (InputStream settingsSource = ConfigTest.class.
            getResourceAsStream(resourceToCopy);
             FileOutputStream settingsTarget = new
                 FileOutputStream(targetFile)) {
            IOUtils.copy(settingsSource, settingsTarget);
        }
    }

}
