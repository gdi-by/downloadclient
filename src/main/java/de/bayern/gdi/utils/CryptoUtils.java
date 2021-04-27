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

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Encrypts/Decrypts text.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CryptoUtils {

    private static final String ALGORITHM = "AES";

    private static final String KEY_PASSWORD = "gh_hr6fNo!Ftz4FkDL0kG4dD";

    private CryptoUtils() {
    }

    /**
     * Encrypts the passed input.
     *
     * @param input to encrypt
     * @return the encrypted input
     * @throws Exception
     */
    public static String encrypt(String input) throws Exception {
        Cipher cipher = initCypher(Cipher.ENCRYPT_MODE);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    /**
     * Decrypts the passed cipherText.
     *
     * @param cipherText to decrypt
     * @return the decrypted input
     * @throws Exception
     */
    public static String decrypt(String cipherText) throws Exception {
        Cipher cipher = initCypher(Cipher.DECRYPT_MODE);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }

    private static Cipher initCypher(int decryptMode)
        throws NoSuchPaddingException, NoSuchAlgorithmException,
        InvalidAlgorithmParameterException, InvalidKeyException {
        SecretKeySpec sks = getSecretKeySpec();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(decryptMode, sks, cipher.getParameters());
        return cipher;
    }

    private static SecretKeySpec getSecretKeySpec() {
        byte[] key = KEY_PASSWORD.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec sks = new SecretKeySpec(key, ALGORITHM);
        return sks;
    }

}
