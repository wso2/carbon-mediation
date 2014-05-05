/*
 * Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.Calendar;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

public class SecureVaultLookupHandlerImpl implements SecureVaultLookupHandler {

	private static Log log = LogFactory.getLog(SecureVaultLookupHandlerImpl.class);

	private static SecureVaultLookupHandlerImpl instance = null;

	private ServerConfigurationService serverConfigService;

	private RegistryService registryService;

	UserRegistry registry = null;
	
	Object decryptlockObj = new Object();

	private SecureVaultLookupHandlerImpl(ServerConfigurationService serverConfigurationService,
	                                     RegistryService registryService) throws RegistryException {
		this.serverConfigService = serverConfigurationService;
		this.registryService = registryService;
		try {
			init();
		} catch (RegistryException e) {
			throw new RegistryException("Error while intializing the registry");
		}

	}

	public static SecureVaultLookupHandlerImpl getDefaultSecurityService() throws RegistryException {
		return getDefaultSecurityService(SecurityServiceHolder.getInstance()
		                                                      .getServerConfigurationService(),
		                                 SecurityServiceHolder.getInstance().getRegistryService());
	}

	private static SecureVaultLookupHandlerImpl getDefaultSecurityService(ServerConfigurationService serverConfigurationService,
	                                                                      RegistryService registryService)
	                                                                                                      throws RegistryException {
		if (instance == null) {
			instance =
			           new SecureVaultLookupHandlerImpl(serverConfigurationService, registryService);
		}
		return instance;
	}

	private void init() throws RegistryException {
		try {
			registry = registryService.getConfigSystemRegistry();

			// creating vault-specific storage repository (this happens only if
			// not resource not existing)
			initRegistryRepo();
		} catch (RegistryException e) {
			throw new RegistryException("Error while intializing the registry");
		}
	}

	/**
	 * Initializing the repository which requires to store the secure vault
	 * cipher text
	 * 
	 * @throws RegistryException
	 */
	private void initRegistryRepo() throws RegistryException {

		if (!isRepoExists()) {
			org.wso2.carbon.registry.core.Collection secureVaultCollection =
			                                                                 registry.newCollection();
			registry.put(SecureVaultConstants.CONNECTOR_SECURE_VAULT_CONFIG_REPOSITORY,
			             secureVaultCollection);
		}
	}

	/**
	 * Checks whether the given repository already existing.
	 * 
	 * @return
	 */
	protected boolean isRepoExists() {
		try {
			registry.get(SecureVaultConstants.CONNECTOR_SECURE_VAULT_CONFIG_REPOSITORY);
		} catch (RegistryException e) {
			return false;
		}
		return true;
	}

	public String getProviderClass() {
		return this.getClass().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wso2.carbon.mediation.secure.vault.MediationSecurity#evaluate(java
	 * .lang.String,
	 * org.wso2.carbon.mediation.secure.vault.MediationSrecurtyClient
	 * .LookupType)
	 */
	@Override
	public String evaluate(String aliasPasword, MessageContext synCtx) throws RegistryException {
		SynapseConfiguration synapseConfiguration = synCtx.getConfiguration();
		Map<String, Object> decryptedCacheMap = synapseConfiguration.getDecryptedCacheMap();
		if (decryptedCacheMap.containsKey(aliasPasword)) {
			SecureVaultCacheContext cacheContext =
			                                       (SecureVaultCacheContext) decryptedCacheMap.get(aliasPasword);
			String cacheDurable =
			                      synCtx.getConfiguration().getRegistry()
			                            .getConfigurationProperties()
			                            .getProperty("cachableDuration");
			long cacheTime =
			                 (cacheDurable != null && !cacheDurable.isEmpty())
			                                                                  ? Long.parseLong(cacheDurable)
			                                                                  : 10000;
			if ((cacheContext.getDateTime().getTime() + cacheTime) >= System.currentTimeMillis()) {
				// which means the given value between the cachable limit
				return cacheContext.getDecryptedValue();
			} else {
				decryptedCacheMap.remove(aliasPasword);
				return vaultLookup(aliasPasword, synCtx, decryptedCacheMap);
			}

		} else {
			String decryptedValue = vaultLookup(aliasPasword, synCtx, decryptedCacheMap);
			return decryptedValue;
		}
	}

	private String vaultLookup(String aliasPasword, MessageContext synCtx,
	                           Map<String, Object> decryptedCacheMap) {
		synchronized (decryptlockObj) {
			SecretCipherHander secretManager = new SecretCipherHander(synCtx);
			String decryptedValue = secretManager.getSecret(aliasPasword);
			if (decryptedCacheMap == null) {
				return null;
			}

			if (decryptedValue.isEmpty()) {
				SecureVaultCacheContext cacheContext =
				                                       (SecureVaultCacheContext) decryptedCacheMap.get(aliasPasword);
				if (cacheContext != null) {
					return cacheContext.getDecryptedValue();
				}
			}

			decryptedCacheMap.put(aliasPasword, new SecureVaultCacheContext(Calendar.getInstance()
			                                                                        .getTime(),
			                                                                decryptedValue));
			return decryptedValue;
		}
	}

}
