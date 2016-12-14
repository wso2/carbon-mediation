/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.http2.ssl;

import org.wso2.carbon.inbound.endpoint.protocol.websocket.ssl.InboundWebsocketSSLConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public class SSLHandlerFactory {

    private KeyManagerFactory keyStoreFactory;
    private TrustManagerFactory trustStoreFactory;

    public SSLHandlerFactory(InboundWebsocketSSLConfiguration sslConfiguration) {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        try {
            KeyStore keyStore = getKeyStore(sslConfiguration.getKeyStore(),
                    sslConfiguration.getKeyStorePass());
            keyStoreFactory = KeyManagerFactory.getInstance(algorithm);
            keyStoreFactory.init(keyStore, sslConfiguration.getCertPass() != null ?
                    sslConfiguration.getCertPass().toCharArray() :
                    sslConfiguration.getKeyStorePass().toCharArray());
            if (sslConfiguration.getTrustStore() != null) {
                KeyStore trustStore = getKeyStore(sslConfiguration.getTrustStore(),
                        sslConfiguration.getTrustStorePass());
                trustStoreFactory = TrustManagerFactory.getInstance(algorithm);
                trustStoreFactory.init(trustStore);
            }
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | IOException ex) {
            throw new IllegalArgumentException("Failed to initialize the server side SSLContext",
                    ex);
        }
    }

    private static KeyStore getKeyStore(File keyStore, String keyStorePassword) throws IOException {
        KeyStore keyStoreInstance;
        try (InputStream is = new FileInputStream(keyStore)) {
            keyStoreInstance = KeyStore.getInstance("JKS");
            keyStoreInstance.load(is, keyStorePassword.toCharArray());
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IOException(e);
        }
        return keyStoreInstance;
    }

    public KeyManagerFactory getKeyStoreFactory() {
        return keyStoreFactory;
    }

    public TrustManagerFactory getTrustStoreFactory() {
        return trustStoreFactory;
    }
}
