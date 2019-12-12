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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;

import java.io.File;

/**
 * Entry point for manage secrets
 */
public class SecretCipherHander {

	private static Log log = LogFactory.getLog(SecretCipherHander.class);
	private static String DOCKER_SECRET_ROOT;
	private static String FILE_SECRET_ROOT;

	static {
		String dockerSecretProp = System.getProperty(SecureVaultConstants.PROP_DOCKER_SECRET_ROOT_DIRECTORY);
		if (dockerSecretProp != null && !dockerSecretProp.trim().isEmpty()) {
			DOCKER_SECRET_ROOT = dockerSecretProp.trim();
		} else {
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				DOCKER_SECRET_ROOT = SecureVaultConstants.PROP_DOCKER_SECRET_ROOT_DIRECTORY_DEFAULT_WIN;
			} else {
				DOCKER_SECRET_ROOT = SecureVaultConstants.PROP_DOCKER_SECRET_ROOT_DIRECTORY_DEFAULT;
			}
		}
		if (!DOCKER_SECRET_ROOT.endsWith(File.separator)) {
			DOCKER_SECRET_ROOT = DOCKER_SECRET_ROOT + File.separator;
		}
		DOCKER_SECRET_ROOT = SecureVaultConstants.FILE_PROTOCOL_PREFIX + DOCKER_SECRET_ROOT;

		String fileSecretProp = System.getProperty(SecureVaultConstants.PROP_FILE_SECRET_ROOT_DIRECTORY);
		if (fileSecretProp != null && !fileSecretProp.trim().isEmpty()) {
			FILE_SECRET_ROOT = fileSecretProp.trim();
		} else {
			FILE_SECRET_ROOT = SecureVaultConstants.PROP_FILE_SECRET_ROOT_DIRECTORY_DEFAULT;
		}
		if (!FILE_SECRET_ROOT.endsWith(File.separator)) {
			FILE_SECRET_ROOT = FILE_SECRET_ROOT + File.separator;
		}
		FILE_SECRET_ROOT = SecureVaultConstants.FILE_PROTOCOL_PREFIX + FILE_SECRET_ROOT;
	}

	/* Root Secret Repository */
	private RegistrySecretRepository parentRepository = new RegistrySecretRepository();
	private FileSecretRepository fileSecretRepository = new FileSecretRepository();

	private org.apache.synapse.MessageContext synCtx;

	CipherInitializer ciperInitializer = CipherInitializer.getInstance();

	public SecretCipherHander(org.apache.synapse.MessageContext synCtx) {
		super();
		this.synCtx = synCtx;
		parentRepository.setSynCtx(synCtx);
	}

	/**
	 * Returns the secret corresponding to the given alias name
	 * 
	 * @param alias
	 *            The logical or alias name
	 * @return If there is a secret , otherwise , alias itself
	 */
	public String getSecret(String alias) {
		return parentRepository.getSecret(alias);
	}

	public String getSecret(String alias, SecretSrcData secretSrcData) {
		switch (secretSrcData.getVaultType()) {
			case DOCKER:
				// resolve path and alias
				String resolvedDockerAlias = DOCKER_SECRET_ROOT + alias;
				// For docker type we support plaintext as well
				if (secretSrcData.isEncrypted()) {
					return fileSecretRepository.getSecret(resolvedDockerAlias);
				}
				return fileSecretRepository.getPlainTextSecret(resolvedDockerAlias);

			case FILE:
				// resolve path and alias
				String resolvedFileAlias = FILE_SECRET_ROOT + alias;
				// For file type we support plaintext as well
				if (secretSrcData.isEncrypted()) {
					return fileSecretRepository.getSecret(resolvedFileAlias);
				}
				return fileSecretRepository.getPlainTextSecret(resolvedFileAlias);
			case REG:
				// For registry type we only support plain text
				return parentRepository.getSecret(alias);
			default:
				// Will never reach here unless customized
				throw new SynapseException("Unknown secret type : " + secretSrcData.getVaultType().toString());
		}
	}

	/**
	 * Returns the encrypted value corresponding to the given alias name
	 * 
	 * @param alias
	 *            The logical or alias name
	 * @return If there is a encrypted value , otherwise , alias itself
	 */
	public String getEncryptedData(String alias) {
		return parentRepository.getEncryptedData(alias);

	}

	public void shoutDown() {
		this.parentRepository = null;

	}

	public org.apache.synapse.MessageContext getSynCtx() {
		return synCtx;
	}

	public void setSynCtx(org.apache.synapse.MessageContext synCtx) {
		this.synCtx = synCtx;
	}

}