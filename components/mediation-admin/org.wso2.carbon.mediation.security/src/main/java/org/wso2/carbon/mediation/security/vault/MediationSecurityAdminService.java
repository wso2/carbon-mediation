/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediation.security.vault;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.apache.axis2.AxisFault;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;

public class MediationSecurityAdminService extends AbstractServiceBusAdmin {

	private static Log log = LogFactory.getLog(MediationSecurityAdminService.class);

	/**
	 * Operation to do the encryption ops by invoking secure vault api
	 * 
	 * @param plainTextPass
	 * @return
	 * @throws AxisFault
	 */
	public String doEncrypt(String plainTextPass) throws AxisFault {
		CipherInitializer ciperInitializer = CipherInitializer.getInstance();
		byte[] plainTextPassByte = plainTextPass.getBytes();

		try {
			Cipher cipher = ciperInitializer.getEncryptionProvider();
			if (cipher == null) {
                log.error("Either Configuration properties can not be loaded or No secret"
                          + " repositories have been configured please check PRODUCT_HOME/conf/security "
                          + " refer links related to configure WSO2 Secure vault");
                handleException(log,
                                "Failed to load security key store information ," +
                                "Configure secret-conf.properties properly by referring to " +
                                        "\"Carbon Secure Vault Implementation\" in WSO2 Documentation",
                                null);
            }
			byte[] encryptedPassword = cipher.doFinal(plainTextPassByte);
            return new String(Base64.encodeBase64(encryptedPassword));
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			handleException(log, "Error encrypting password ", e);
		}

        return null;

	}

	public String doDecrypt(String cipherText) throws AxisFault {
		// TODO:yet to implement
		return null;
	}

	private void handleException(Log log, String message, Exception e) throws AxisFault {

		if (e == null) {

			AxisFault exception = new AxisFault(message);
			log.error(message, exception);
			throw exception;

		} else {
			message = message + " :: " + e.getMessage();
			log.error(message, e);
			throw new AxisFault(message, e);
		}
	}

}
