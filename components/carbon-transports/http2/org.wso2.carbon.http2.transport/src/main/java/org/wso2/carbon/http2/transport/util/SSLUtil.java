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

package org.wso2.carbon.http2.transport.util;

import org.apache.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class SSLUtil {
    private static String TRUST_STORE_TYPE = "JKS";
    private static String TRUST_MANAGER_TYPE = "SunX509";
    private static TrustManagerFactory trustManagerFactory = null;

    private static final Logger LOGGER = Logger.getLogger(SSLUtil.class);

    public static TrustManagerFactory createTrustmanager(final String trustStoreLocation,
            final String trustStorePwd) {
        try {
            if (trustManagerFactory == null) {
                KeyStore trustStore = KeyStore.getInstance(TRUST_STORE_TYPE);
                trustStore
                        .load(new FileInputStream(trustStoreLocation), trustStorePwd.toCharArray());
                trustManagerFactory = TrustManagerFactory.getInstance(TRUST_MANAGER_TYPE);
                trustManagerFactory.init(trustStore);
            }
        } catch (KeyStoreException e) {
            LOGGER.error("Exception was thrown while building the client SSL context", e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Exception was thrown while building the client SSL context", e);
        } catch (CertificateException e) {
            LOGGER.error("Exception was thrown while building the client SSL context", e);
        } catch (FileNotFoundException e) {
            LOGGER.error("Exception was thrown while building the client SSL context", e);
        } catch (IOException e) {
            LOGGER.error("Exception was thrown while building the client SSL context", e);
        }
        return trustManagerFactory;
    }

}
