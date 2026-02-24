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

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import com.google.gson.JsonObject;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.registry.Registry;
import org.wso2.carbon.mediation.security.vault.util.SecureVaultUtil;
import org.wso2.securevault.DecryptionProvider;
import org.wso2.securevault.secret.SecretRepository;

import javax.crypto.spec.GCMParameterSpec;

/**
 * Holds all secrets in a file
 */
public class RegistrySecretRepository implements SecretRepository {

	private static Log log = LogFactory.getLog(RegistrySecretRepository.class);

	/* Parent secret repository */
	private SecretRepository parentRepository;

	private MessageContext synCtx;

	public RegistrySecretRepository() {
		super();

	}

	/**
	 * @param alias
	 *            Alias name for look up a secret
	 * @return Secret if there is any , otherwise ,alias itself
	 * @see org.wso2.securevault.secret.SecretRepository
	 */
	public String getSecret(String alias) {

		Entry propEntry =
		                  synCtx.getConfiguration()
		                        .getEntryDefinition(SecureVaultConstants.CONF_CONNECTOR_SECURE_VAULT_CONFIG_PROP_LOOK);

		Registry registry = synCtx.getConfiguration().getRegistry();

		String propertyValue = "";

		if (registry != null) {
			registry.getResource(propEntry, new Properties());
			if (alias != null) {
				Properties reqProperties = propEntry.getEntryProperties();
				if (reqProperties != null) {
					if (reqProperties.get(alias) != null) {
						propertyValue = reqProperties.getProperty(alias);
					}
				}
			}
		}
		CipherInitializer cipherInitializer = CipherInitializer.getInstance();
		DecryptionProvider decryptionProvider = cipherInitializer.getDecryptionProvider();

		if (decryptionProvider == null) {
			log.error("Can not proceed decryption due to the secret repository initialization error");
			return null;
		}
		String decryptedText;
		if (cipherInitializer.getAlgorithm().equals((SecureVaultConstants.AES_GCM_NO_PADDING))) {
			JsonObject jsonObject = SecureVaultUtil.getJsonObject(propertyValue.trim());
			byte[] cipherTextBytes = SecureVaultUtil.getValueFromJson(jsonObject, SecureVaultConstants.CIPHER_TEXT)
					.getBytes(StandardCharsets.UTF_8);
			byte[] iv = Base64Utils.decode(SecureVaultUtil.getValueFromJson(jsonObject, SecureVaultConstants.IV));
			decryptedText = new String(decryptionProvider.decrypt(cipherTextBytes,
					new GCMParameterSpec(SecureVaultConstants.GCM_TAG_LENGTH, iv)));
		} else {
			decryptedText = new String(
					decryptionProvider.decrypt(propertyValue.trim().getBytes(StandardCharsets.UTF_8)));
		}

		if (log.isDebugEnabled()) {
			log.debug("evaluation completed successfully " + decryptedText);
		}
		return decryptedText;

	}

	/**
	 * @param alias
	 *            Alias name for look up a encrypted Value
	 * @return encrypted Value if there is any , otherwise ,alias itself
	 * @see org.wso2.securevault.secret.SecretRepository
	 */
	public String getEncryptedData(String alias) {

		return null;
	}

	public void setParent(SecretRepository parent) {
		this.parentRepository = parent;
	}

	public SecretRepository getParent() {
		return this.parentRepository;
	}

	public MessageContext getSynCtx() {
		return synCtx;
	}

	public void setSynCtx(MessageContext synCtx) {
		this.synCtx = synCtx;
	}

	@Override
	public void init(Properties arg0, String arg1) {
		// TODO Auto-generated method stub

	}

}
