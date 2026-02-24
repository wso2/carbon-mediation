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
import javax.crypto.spec.GCMParameterSpec;

import com.google.gson.JsonObject;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axis2.AxisFault;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.security.vault.external.ExternalVaultException;
import org.wso2.carbon.mediation.security.vault.external.hashicorp.HashiCorpVaultLookupHandlerImpl;
import org.wso2.carbon.mediation.security.vault.util.SecureVaultUtil;
import org.wso2.securevault.DecryptionProvider;

import java.nio.charset.StandardCharsets;

public class MediationSecurityAdminService extends AbstractServiceBusAdmin {

	private static Log log = LogFactory.getLog(MediationSecurityAdminService.class);

	private static final String EXTERNAL_VAULTS = "[EI_HOME]/conf/security/external-vaults.xml";

	/**
	 * Operation to do the encryption ops by invoking secure vault api
	 *
	 * @param plainTextPass Plain text password to encrypt
	 * @return Encrypted password
	 * @throws AxisFault if an error occurs while encrypting the plain text
	 */
	public String doEncrypt(String plainTextPass) throws AxisFault {
		CipherInitializer cipherInitializer = CipherInitializer.getInstance();
		byte[] plainTextPassByte = plainTextPass.getBytes(StandardCharsets.UTF_8);

		try {
			boolean isGCMMode = cipherInitializer.getAlgorithm().equals(SecureVaultConstants.AES_GCM_NO_PADDING);
			Cipher cipher;

			if (isGCMMode) {
				cipher = cipherInitializer.getGCMEncryptionProvider();
			} else {
				cipher = cipherInitializer.getEncryptionProvider();
			}

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
			String base64EncodedCipherText = new String(Base64.encodeBase64(encryptedPassword), StandardCharsets.UTF_8);

			if (isGCMMode) {
				return SecureVaultUtil.createSelfContainedCiphertextWithGCMMode(base64EncodedCipherText,
						cipherInitializer.getIv());
			} else {
				return base64EncodedCipherText;
			}
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			handleException(log, "Error encrypting password ", e);
		}

		return null;

	}

	/**
	 * Operation to do the decryption ops by invoking secure vault api
	 *
	 * @param cipherText The cipher text to be decrypted
	 * @return The decrypted plain text
	 * @throws AxisFault if an error occurs while loading the keystore
	 */
	public String doDecrypt(String cipherText) throws AxisFault {
		CipherInitializer cipherInitializer = CipherInitializer.getInstance();

		DecryptionProvider decryptionProvider = cipherInitializer.getDecryptionProvider();
		if (decryptionProvider == null) {
			log.error("Either Configuration properties can not be loaded or No secret"
					+ " repositories have been configured please check PRODUCT_HOME/conf/security "
					+ " refer links related to configure WSO2 Secure vault");
			handleException(log,
					"Failed to load security key store information ," +
							"Configure secret-conf.properties properly by referring to " +
							"\"Carbon Secure Vault Implementation\" in WSO2 Documentation",
					null);
		}
		if (cipherInitializer.getAlgorithm().equals((SecureVaultConstants.AES_GCM_NO_PADDING))) {
			JsonObject jsonObject = SecureVaultUtil.getJsonObject(cipherText.trim());
			byte[] cipherTextBytes = SecureVaultUtil.getValueFromJson(jsonObject, SecureVaultConstants.CIPHER_TEXT)
					.getBytes(StandardCharsets.UTF_8);
			byte[] iv = Base64Utils.decode(SecureVaultUtil.getValueFromJson(jsonObject, SecureVaultConstants.IV));
			return new String(decryptionProvider.decrypt(cipherTextBytes,
					new GCMParameterSpec(SecureVaultConstants.GCM_TAG_LENGTH, iv)));
		} else {
			return new String(decryptionProvider.decrypt(cipherText.trim().getBytes(StandardCharsets.UTF_8)));
		}
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

	public void setSecretIdForHashiCorpVault(String secretId) throws ExternalVaultException {
		HashiCorpVaultLookupHandlerImpl instance = HashiCorpVaultLookupHandlerImpl.getDefaultSecurityService();
		instance.setSecretId(secretId);

		log.info("SecretId value is updated in HashiCorp vault runtime configurations");
		log.warn("To persist the new SecretId in the next server startup, please update the " + EXTERNAL_VAULTS
				+ " file" );
	}
}
