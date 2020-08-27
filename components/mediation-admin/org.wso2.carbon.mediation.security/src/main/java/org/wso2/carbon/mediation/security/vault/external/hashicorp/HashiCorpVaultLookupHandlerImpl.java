/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediation.security.vault.external.hashicorp;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Logical;
import com.bettercloud.vault.rest.RestException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.wso2.carbon.mediation.security.vault.SecureVaultCacheContext;
import org.wso2.carbon.mediation.security.vault.external.ExternalVaultConfigLoader;
import org.wso2.carbon.mediation.security.vault.external.ExternalVaultException;
import org.wso2.carbon.mediation.security.vault.external.ExternalVaultLookupHandler;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.Calendar;
import java.util.Map;

/**
 * Class responsible for the HashiCorp vault integration model.
 */
public class HashiCorpVaultLookupHandlerImpl implements ExternalVaultLookupHandler {

	private static Log log = LogFactory.getLog(HashiCorpVaultLookupHandlerImpl.class);

	private static HashiCorpVaultLookupHandlerImpl instance = null;
	
	private Object decryptLockObj = new Object();

	private Vault vaultConnection;

	private String cachableDuration = "15000";

	private String engineVersion = "2";

	private String vaultNamespace;

	private HashiCorpVaultLookupHandlerImpl() throws ExternalVaultException {
		try {
			initialize();
		} catch (ExternalVaultException e) {
			throw new ExternalVaultException("Error while initializing the secure vault configs", e);
		}
	}

	static HashiCorpVaultLookupHandlerImpl getDefaultSecurityService() throws ExternalVaultException {
		if (instance == null) {
			instance = new HashiCorpVaultLookupHandlerImpl();
		}
		return instance;
	}

	@Override
	public String name() {
		return "hashicorp";
	}

	/**
	 * Check parameters and initialize the the connection with external HashiCorp vault.
	 */
	private void initialize() throws ExternalVaultException {
		Map<String, String> parameters = ExternalVaultConfigLoader.getVaultParameters(name());
		if (parameters == null || parameters.size() < 2) {
			throw new ExternalVaultException("Required configurations of the " + name()
					+ " secure vault can not found");
		} else if (!parameters.containsKey(HashiCorpVaultConstant.ADDRESS_PARAMETER)) {
			throw new ExternalVaultException(HashiCorpVaultConstant.ADDRESS_PARAMETER
					+ " parameter can not found in " + name() + " secure vault configurations");
		} else if (!parameters.containsKey(HashiCorpVaultConstant.TOKEN_PARAMETER)) {
			throw new ExternalVaultException(HashiCorpVaultConstant.TOKEN_PARAMETER
					+ " parameter can not found in " + name() + " secure vault configurations");
		}

		processHashiCorpParameters(parameters);

		try {
			vaultConnection = new Vault(createHashiCorpVaultConfig(parameters));
		} catch (VaultException e) {
			if (e.getCause() instanceof RestException) {
				throw new ExternalVaultException("Error in connecting to HashiCorp vault. Vault address: "
						+ parameters.get(HashiCorpVaultConstant.ADDRESS_PARAMETER), e);
			} else {
				throw new ExternalVaultException("Error while connecting the HashiCorp vault", e);
			}
		}
	}

	/**
	 * Process parameters loaded from external-vault.xml file.
	 *
	 * @param parameters values loaded map.
	 */
	private void processHashiCorpParameters(Map<String, String> parameters) {
		// set cacheable duration value if it defines in external-vaults configurations
		if (parameters.containsKey(HashiCorpVaultConstant.CACHEABLE_DURATION_PARAMETER)
				&& !parameters.get(HashiCorpVaultConstant.CACHEABLE_DURATION_PARAMETER).isEmpty()
				&& parameters.get(HashiCorpVaultConstant.CACHEABLE_DURATION_PARAMETER)
				.matches(HashiCorpVaultConstant.NUMBER_REGEX)) {
			cachableDuration = parameters.get(HashiCorpVaultConstant.CACHEABLE_DURATION_PARAMETER);
		}

		// set engineVersion value if it defines in external-vaults configurations
		if (parameters.containsKey(HashiCorpVaultConstant.ENGINE_TYPE_PARAMETER)
				&& !parameters.get(HashiCorpVaultConstant.ENGINE_TYPE_PARAMETER).isEmpty()
				&& parameters.get(HashiCorpVaultConstant.ENGINE_TYPE_PARAMETER)
				.matches(HashiCorpVaultConstant.NUMBER_REGEX)) {
			engineVersion = parameters.get(HashiCorpVaultConstant.ENGINE_TYPE_PARAMETER);
		}

		// set namespace value if it defines in external-vaults configurations
		if (parameters.containsKey(HashiCorpVaultConstant.NAMESPACE_PARAMETER)
				&& !parameters.get(HashiCorpVaultConstant.NAMESPACE_PARAMETER).isEmpty()) {
			vaultNamespace = parameters.get(HashiCorpVaultConstant.NAMESPACE_PARAMETER);
		}

		// resolve ${carbon.home} variable when file paths are relative
		if (parameters.containsKey(HashiCorpVaultConstant.TRUST_STORE_PARAMETER)) {
			String trustStoreFilePath = parameters.get(HashiCorpVaultConstant.TRUST_STORE_PARAMETER);
			if (trustStoreFilePath.startsWith(HashiCorpVaultConstant.CARBON_HOME_VARIABLE)) {
				trustStoreFilePath = trustStoreFilePath.replace(HashiCorpVaultConstant.CARBON_HOME_VARIABLE,
						CarbonUtils.getCarbonHome());
				parameters.put(HashiCorpVaultConstant.TRUST_STORE_PARAMETER, trustStoreFilePath);
			}
		}

		if (parameters.containsKey(HashiCorpVaultConstant.KEY_STORE_PARAMETER)) {
			String keyStoreFilePath = parameters.get(HashiCorpVaultConstant.KEY_STORE_PARAMETER);
			if (keyStoreFilePath.startsWith(HashiCorpVaultConstant.CARBON_HOME_VARIABLE)) {
				keyStoreFilePath = keyStoreFilePath.replace(HashiCorpVaultConstant.CARBON_HOME_VARIABLE,
						CarbonUtils.getCarbonHome());
				parameters.put(HashiCorpVaultConstant.KEY_STORE_PARAMETER, keyStoreFilePath);
			}
		}
	}

	/**
	 * Create HashiCorp vault configuration using the loaded parameters from external-vaults.
	 *
	 * @param parameters values loaded map.
	 * @return vault configuration
	 */
	private VaultConfig createHashiCorpVaultConfig(Map<String, String> parameters)
			throws VaultException, ExternalVaultException {
		VaultConfig config = new VaultConfig().address(parameters.get(HashiCorpVaultConstant.ADDRESS_PARAMETER));

		if (parameters.get(HashiCorpVaultConstant.ADDRESS_PARAMETER)
				.startsWith(HashiCorpVaultConstant.HTTPS_PARAMETER)) {
			//configure SSL configurations to connect with HTTPS
			SslConfig sslConfig = new SslConfig();

			if (!parameters.containsKey(HashiCorpVaultConstant.TRUST_STORE_PARAMETER)
					&& !parameters.containsKey(HashiCorpVaultConstant.KEY_STORE_PARAMETER)) {
				throw new ExternalVaultException(HashiCorpVaultConstant.TRUST_STORE_PARAMETER
						+ " parameter or " + HashiCorpVaultConstant.TRUST_STORE_PARAMETER
						+ "parameter can not found in " + name() + " secure vault configurations");
			}

			// add trust store file path to the VaultConfig if exists
			if (parameters.containsKey(HashiCorpVaultConstant.TRUST_STORE_PARAMETER)) {
				sslConfig = sslConfig.trustStoreFile(
						new File(parameters.get(HashiCorpVaultConstant.TRUST_STORE_PARAMETER)));
			}

			// add key store file path and keystore password to the VaultConfig if exists
			if (parameters.containsKey(HashiCorpVaultConstant.KEY_STORE_PARAMETER) &&
					parameters.containsKey(HashiCorpVaultConstant.KEY_STORE_PASSWORD_PARAMETER)) {
				sslConfig = sslConfig.keyStoreFile(
						new File(parameters.get(HashiCorpVaultConstant.KEY_STORE_PARAMETER)),
						parameters.get(HashiCorpVaultConstant.KEY_STORE_PASSWORD_PARAMETER));
			} else if (parameters.containsKey(HashiCorpVaultConstant.KEY_STORE_PARAMETER) &&
					!parameters.containsKey(HashiCorpVaultConstant.KEY_STORE_PASSWORD_PARAMETER)) {
				throw new ExternalVaultException(HashiCorpVaultConstant.KEY_STORE_PASSWORD_PARAMETER
						+ " parameter can not found in " + name() + " secure vault configurations");
			} else if (!parameters.containsKey(HashiCorpVaultConstant.KEY_STORE_PARAMETER) &&
					parameters.containsKey(HashiCorpVaultConstant.KEY_STORE_PASSWORD_PARAMETER)) {
				throw new ExternalVaultException(HashiCorpVaultConstant.KEY_STORE_PARAMETER
						+ " parameter can not found in " + name() + " secure vault configurations");
			}

			// add the sslConfig configuration to the VaultConfig if the protocol is HTTPS
			config = config.sslConfig(sslConfig);
		}
		config = config.token(parameters.get(HashiCorpVaultConstant.TOKEN_PARAMETER))
				.engineVersion(Integer.parseInt(engineVersion)).build();

		return config;
	}

	@Override
	public String evaluate(Map<String, String> vaultParameters, MessageContext synCtx) throws ExternalVaultException {
		SynapseConfiguration synapseConfiguration = synCtx.getConfiguration();
		Map<String, Object> decryptedCacheMap = synapseConfiguration.getDecryptedCacheMap();
		String pathParameter = vaultParameters.get(HashiCorpVaultConstant.PATH_PARAMETER);
		String fieldParameter = vaultParameters.get(HashiCorpVaultConstant.FIELD_PARAMETER);

		// set aliasPassword value based on the namespace. Here namespace can be inject either as the first parameter
		// of the vault-lookup function or the namespace parameter in the external-vault configurations
		String aliasPassword = pathParameter + "-" + fieldParameter;
		String namespaceForEvaluation = null;
		if (vaultParameters.containsKey(HashiCorpVaultConstant.VAULT_NAMESPACE_PARAMETER)) {
			aliasPassword = vaultParameters.get(HashiCorpVaultConstant.VAULT_NAMESPACE_PARAMETER) + "-" + aliasPassword;
			namespaceForEvaluation = vaultParameters.get(HashiCorpVaultConstant.VAULT_NAMESPACE_PARAMETER);
		} else if (vaultNamespace != null) {
			aliasPassword = vaultNamespace + "-" + aliasPassword;
			namespaceForEvaluation = vaultNamespace;
		}


		if (decryptedCacheMap.containsKey(aliasPassword)) {
			SecureVaultCacheContext cacheContext =
					(SecureVaultCacheContext) decryptedCacheMap.get(aliasPassword);
			if (cacheContext != null) {
				long cacheTime = Long.parseLong(cachableDuration);
				if ((cacheContext.getDateTime().getTime() + cacheTime) >= System.currentTimeMillis()) {
					// which means the given value between the cacheable limit
					return cacheContext.getDecryptedValue();
				} else {
					decryptedCacheMap.remove(aliasPassword);
					return vaultLookup(namespaceForEvaluation, pathParameter, fieldParameter, decryptedCacheMap);
				}
			} else {
				return vaultLookup(namespaceForEvaluation, pathParameter, fieldParameter, decryptedCacheMap);
			}
		} else {
			return vaultLookup(namespaceForEvaluation, pathParameter, fieldParameter, decryptedCacheMap);
		}
	}

	/**
	 * Resolves the secret by fetching that secret from HashiCorp vault if it does not exist in the cache.
	 *
	 * @param namespace namespace of the secret
	 * @param pathParameter pathParameter of the secret
	 * @param fieldParameter fieldParameter of the secret
	 * @param decryptedCacheMap map which contains fetched secrets
	 * @return resolved string
	 * @throws ExternalVaultException when failed to resolve the text from the vault
	 */
	private String vaultLookup(String namespace, String pathParameter, String fieldParameter,
							   Map<String, Object> decryptedCacheMap) throws ExternalVaultException {
		String aliasPassword = pathParameter + "-" + fieldParameter;
		synchronized (decryptLockObj) {
			if (vaultConnection == null) {
				initialize();
			}

			String decryptedValue;
			try {
				Logical logical = vaultConnection.logical();

				if (namespace != null) {
					logical = logical.withNameSpace(namespace);
					aliasPassword = namespace + "-" + aliasPassword;
				}

				decryptedValue = logical.read(pathParameter).getData().get(fieldParameter);

			} catch (VaultException e) {
				throw new ExternalVaultException("Cannot read the vault secret from the HashiCorp vault. "
						+ (namespace != null ? "Namespace: " + namespace + ", " : "")
						+ "Path: " + pathParameter + ", Field: " + fieldParameter, e);
			}

			if (decryptedCacheMap == null || decryptedValue == null) {
				log.warn("Cannot find a vault secret from the HashiCorp vault for, "
						+ (namespace != null ? "Namespace: " + namespace + ", " : "")
						+ "Path: " + pathParameter + ", Field: " + fieldParameter);
				return null;
			}

			if (decryptedValue.isEmpty()) {
				SecureVaultCacheContext cacheContext =
						(SecureVaultCacheContext) decryptedCacheMap.get(aliasPassword);
				if (cacheContext != null) {
					return cacheContext.getDecryptedValue();
				}
			}

			decryptedCacheMap.put(aliasPassword,
					new SecureVaultCacheContext(Calendar.getInstance().getTime(), decryptedValue));
			return decryptedValue;
		}
	}
}
