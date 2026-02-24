/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediation.security.vault;

import com.google.gson.JsonObject;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.wso2.carbon.mediation.security.vault.util.SecureVaultUtil;
import org.wso2.securevault.DecryptionProvider;
import org.wso2.securevault.secret.SecretRepository;

import javax.crypto.spec.GCMParameterSpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Implementation of file based secret repository
 */
public class FileSecretRepository implements SecretRepository {

    private static Log LOG = LogFactory.getLog(FileSecretRepository.class);

    /* Parent secret repository */
    private SecretRepository parentRepository;

    public FileSecretRepository() {
        super();
    }

    @Override
    public void init(Properties properties, String id) {
        // nothing to do here
    }

    /**
     * Returns the secret of provided alias name . An alias represents the logical name
     * for a look up secret
     *
     * @param alias file path to the secret file
     * @return
     */
    @Override
    public String getSecret(String alias) {
        // Read from file
        String secretRawValue = getPlainTextSecret(alias);
        CipherInitializer cipherInitializer = CipherInitializer.getInstance();
        DecryptionProvider decryptionProvider = cipherInitializer.getDecryptionProvider();

        if (decryptionProvider == null) {
            LOG.error("Can not proceed decryption due to the secret repository initialization error");
            return null;
        }
        if (cipherInitializer.getAlgorithm().equals((SecureVaultConstants.AES_GCM_NO_PADDING))) {
            JsonObject jsonObject = SecureVaultUtil.getJsonObject(secretRawValue.trim());
            byte[] cipherTextBytes = SecureVaultUtil.getValueFromJson(jsonObject, SecureVaultConstants.CIPHER_TEXT)
                    .getBytes(StandardCharsets.UTF_8);
            byte[] iv = Base64Utils.decode(SecureVaultUtil.getValueFromJson(jsonObject, SecureVaultConstants.IV));
            return new String(decryptionProvider.decrypt(cipherTextBytes,
                    new GCMParameterSpec(SecureVaultConstants.GCM_TAG_LENGTH, iv)));
        } else {
            return new String(decryptionProvider.decrypt(secretRawValue.trim().getBytes(StandardCharsets.UTF_8)));
        }
    }

    /**
     * Function to retrieve plain text secret located in the secret file
     * @param alias
     * @return
     */
    public String getPlainTextSecret (String alias) {
        // Read from file
        // At this point alias must represent the file path
        try {
            String plainText = readFile(alias);
            return plainText != null ? plainText.trim() : null;
        } catch (IOException e) {
            handleException("Error occurred while reading file resource : " + alias, e);
        }
        // Will not reach here
        return null;
    }

    @Override
    public String getEncryptedData(String alias) {
        return null;
    }

    @Override
    public void setParent(SecretRepository secretRepository) {
        parentRepository = secretRepository;
    }

    @Override
    public SecretRepository getParent() {
        return this.parentRepository;
    }

    private String readFile (String filePath) throws IOException {
        URL url = null;
        try {
            url = new URL(filePath);
        } catch (MalformedURLException e) {
            handleException("Invalid path '" + filePath + "' for URL", e);
        }
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        try (InputStream inputStream = urlConnection.getInputStream()) {
            if (inputStream == null) {
                return null;
            }
            StringBuilder strBuilder = new StringBuilder();
            try (BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = bReader.readLine()) != null) {
                    strBuilder.append(line);
                }
            }
            return strBuilder.toString();
        }
    }

    private void handleException(String msg, Exception e) {
        throw new SynapseException(msg, e);
    }

}
