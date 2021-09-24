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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.eclipse.jetty.util.ArrayQueue;
import org.wso2.securevault.DecryptionProvider;
import org.wso2.securevault.secret.SecretRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Implementation of properties file based secret repository
 */
public class PropertySecretRepository implements SecretRepository {

    private static Log LOG = LogFactory.getLog(PropertySecretRepository.class);

    /* Parent secret repository */
    private SecretRepository parentRepository;

    public PropertySecretRepository() {
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
     * @param alias filepath:property to the secret file and property name
     * @return
     */
    @Override
    public String getSecret(String alias) {
        // Read from file
        String secretRawValue = getPlainTextSecret(alias);
        DecryptionProvider decyptProvider = CipherInitializer.getInstance().getDecryptionProvider();

        if (decyptProvider == null) {
            // This cannot happen unless someone mess with OSGI references
            LOG.error("Can not proceed decryption due to the secret repository initialization error");
            return null;
        }
        return new String(decyptProvider.decrypt(secretRawValue.trim().getBytes()));
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
            String plainText = readProp(alias);
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

    private String readProp (String filePropPath) throws IOException {
        URL url = null;
        Deque<String> filePropParts = new ArrayDeque(Arrays.asList(filePropPath.split(":")));
        String propName = filePropParts.pollLast();
        String filePath = filePropParts.stream().collect(Collectors.joining(":"));
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
            Properties props = new Properties();
            props.load(inputStream);
            return props.getProperty(propName);
        }
    }

    private void handleException(String msg, Exception e) {
        throw new SynapseException(msg, e);
    }

}
