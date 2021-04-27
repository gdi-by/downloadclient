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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CryptoUtilsTest {

    private static final String CLEAR_TEXT = "PASSWORD";

    private static final String ENCRYPTED_TEXT = "kLcsCVDIo78TdqzKWTZR8A==";


    /**
     * Tests the encryption and decryption.
     *
     * @throws Exception
     */
    @Test
    public void testEncryptAndDecrypt() throws Exception {
        String encrypted = CryptoUtils.encrypt(CLEAR_TEXT);
        String decrypted = CryptoUtils.decrypt(encrypted);

        assertThat(decrypted, is(CLEAR_TEXT));
    }

    /**
     * Tests the encryption.
     *
     * @throws Exception
     */
    @Test
    public void testEncrypt() throws Exception {
        String encrypt = CryptoUtils.encrypt(CLEAR_TEXT);
        assertThat(encrypt, is(ENCRYPTED_TEXT));
    }

    /**
     * Tests the decryption.
     *
     * @throws Exception
     */
    @Test
    public void testDecrypt() throws Exception {
        String encrypt = CryptoUtils.decrypt(ENCRYPTED_TEXT);
        assertThat(encrypt, is(CLEAR_TEXT));
    }

}
