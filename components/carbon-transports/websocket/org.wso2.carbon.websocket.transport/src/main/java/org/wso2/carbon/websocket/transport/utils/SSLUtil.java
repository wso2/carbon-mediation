/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.websocket.transport.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class SSLUtil {
    private static String KEY_STORE_TYPE = "JKS";
    private static String TRUST_STORE_TYPE = "JKS";
    private static String KEY_MANAGER_TYPE = "SunX509";
    private static String TRUST_MANAGER_TYPE = "SunX509";
    private static String PROTOCOL = "TLS";

    private static SSLContext serverSSLCtx = null;
    private static SSLContext clientSSLCtx = null;

    private static final Log LOGGER = LogFactory.getLog(SSLUtil.class);

    public static SSLContext createServerSSLContext(final String keyStoreLocation,
                                                    final String keyStorePwd) {
        try {
            if (serverSSLCtx == null) {
                KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
                keyStore.load(new FileInputStream(keyStoreLocation), keyStorePwd.toCharArray());
                KeyManagerFactory keyManagerFactory =
                        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, keyStorePwd.toCharArray());
                serverSSLCtx = SSLContext.getInstance(PROTOCOL);
                serverSSLCtx.init(keyManagerFactory.getKeyManagers(), null, null);
            }
        } catch (UnrecoverableKeyException e) {
            LOGGER.error("Exception was thrown while building the server SSL Context", e);
        } catch (KeyManagementException e) {
            LOGGER.error("Exception was thrown while building the server SSL Context", e);
        } catch (KeyStoreException e) {
            LOGGER.error("Exception was thrown while building the server SSL Context", e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Exception was thrown while building the server SSL Context", e);
        } catch (CertificateException e) {
            LOGGER.error("Exception was thrown while building the server SSL Context", e);
        } catch (FileNotFoundException e) {
            LOGGER.error("Exception was thrown while building the server SSL Context", e);
        } catch (IOException e) {
            LOGGER.error("Exception was thrown while building the server SSL Context", e);
        }

        return serverSSLCtx;
    }

    public static SSLContext createClientSSLContext(final String trustStoreLocation,
                                                    final String trustStorePwd) {
        try {
            if (clientSSLCtx == null) {
                KeyStore trustStore = KeyStore.getInstance(TRUST_STORE_TYPE);
                trustStore.load(new FileInputStream(trustStoreLocation),
                        trustStorePwd.toCharArray());
                TrustManagerFactory trustManagerFactory =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);
                clientSSLCtx = SSLContext.getInstance(PROTOCOL);
                clientSSLCtx.init(null, trustManagerFactory.getTrustManagers(), null);
            }
        } catch (KeyManagementException e) {
            LOGGER.error("Exception was thrown while building the client SSL Context", e);
        } catch (KeyStoreException e) {
            LOGGER.error("Exception was thrown while building the client SSL Context", e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Exception was thrown while building the client SSL Context", e);
        } catch (CertificateException e) {
            LOGGER.error("Exception was thrown while building the client SSL Context", e);
        } catch (FileNotFoundException e) {
            LOGGER.error("Exception was thrown while building the client SSL Context", e);
        } catch (IOException e) {
            LOGGER.error("Exception was thrown while building the client SSL Context", e);
        }

        return clientSSLCtx;

    }

    public static TrustManagerFactory createTrustmanager(final String trustStoreLocation,
                                                         final String trustStorePwd) {
        TrustManagerFactory trustManagerFactory = null;
        try {
            if (clientSSLCtx == null) {
                KeyStore trustStore = KeyStore.getInstance(TRUST_STORE_TYPE);
                trustStore.load(new FileInputStream(trustStoreLocation),
                        trustStorePwd.toCharArray());
                trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);
                // clientSSLCtx = SSLContext.getInstance(PROTOCOL);
                // clientSSLCtx.init(null,
                // trustManagerFactory.getTrustManagers(), null);
            }
        } catch (KeyStoreException e) {
            LOGGER.error("Exception was thrown while building the client SSL Context", e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Exception was thrown while building the client SSL Context", e);
        } catch (CertificateException e) {
            LOGGER.error("Exception was thrown while building the client SSL Context", e);
        } catch (FileNotFoundException e) {
            LOGGER.error("Exception was thrown while building the client SSL Context", e);
        } catch (IOException e) {
            LOGGER.error("Exception was thrown while building the client SSL Context", e);
        }
        return trustManagerFactory;
    }

}
