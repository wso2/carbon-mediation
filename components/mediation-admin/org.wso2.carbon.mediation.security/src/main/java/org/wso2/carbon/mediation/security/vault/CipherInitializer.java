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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.security.vault.util.SecureVaultUtil;
import org.wso2.securevault.CipherFactory;
import org.wso2.securevault.CipherOperationMode;
import org.wso2.securevault.DecryptionProvider;
import org.wso2.securevault.EncodingType;
import org.wso2.securevault.commons.MiscellaneousUtil;
import org.wso2.securevault.definition.CipherInformation;
import org.wso2.securevault.definition.IdentityKeyStoreInformation;
import org.wso2.securevault.definition.KeyStoreInformationFactory;
import org.wso2.securevault.definition.TrustKeyStoreInformation;
import org.wso2.securevault.keystore.IdentityKeyStoreWrapper;
import org.wso2.securevault.keystore.KeyStoreWrapper;
import org.wso2.securevault.keystore.TrustKeyStoreWrapper;
import org.wso2.securevault.secret.SecretRepository;

public class CipherInitializer {
	private static Log log = LogFactory.getLog(CipherInitializer.class);

	private static final String LOCATION = "location";
	private static final String KEY_STORE = "keyStore";
	private static final String DOT = ".";
	private static final String ALGORITHM = "algorithm";
	private static final String DEFAULT_ALGORITHM = "RSA";
	private static final String TRUSTED = "trusted";
	private static CipherInitializer cipherInitializer  = new CipherInitializer();;

	// global password provider implementation class if defined in secret
	// manager conf file
	private String globalSecretProvider = null;

	private IdentityKeyStoreWrapper identityKeyStoreWrapper;

	private TrustKeyStoreWrapper trustKeyStoreWrapper;

	private DecryptionProvider decryptionProvider = null;

	private Cipher encryptionProvider = null;
	
	Object cipherLockObj = new Object();

	private CipherInitializer() {
		super();
		synchronized (cipherLockObj) {

			boolean initPro = init();
			if (initPro) {
				initCipherDecryptProvider();
				initEncrypt();
			} else {
				log.error("Either Configuration properties can not be loaded or No secret"
				          + " repositories have been configured please check PRODUCT_HOME/repository/conf/security "
				          + " refer links related to configure WSO2 Secure vault");
			}
		}

	}

	public static CipherInitializer getInstance() {
	   	return cipherInitializer;
	}

	private boolean init() {

		Properties properties = SecureVaultUtil.loadProperties();// loadProperties();

		if (properties == null) {
			log.error("KeyStore configuration properties cannot be found");
			return false;
		}

		String configurationFile =
		                           MiscellaneousUtil.getProperty(properties,
		                                                         SecureVaultConstants.PROP_SECRET_MANAGER_CONF,
		                                                         SecureVaultConstants.PROP_DEFAULT_CONF_LOCATION);

		Properties configurationProperties = MiscellaneousUtil.loadProperties(configurationFile);
		if (configurationProperties == null || configurationProperties.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("Configuration properties can not be loaded form : " + configurationFile +
				          " Will use synapse properties");
			}
			configurationProperties = properties;

		}

		globalSecretProvider =
		                       MiscellaneousUtil.getProperty(configurationProperties,
		                                                     SecureVaultConstants.PROP_SECRET_PROVIDER,
		                                                     null);
		if (globalSecretProvider == null || "".equals(globalSecretProvider)) {
			if (log.isDebugEnabled()) {
				log.debug("No global secret provider is configured.");
			}
		}

		String repositoriesString =
		                            MiscellaneousUtil.getProperty(configurationProperties,
		                                                          SecureVaultConstants.PROP_SECRET_REPOSITORIES,
		                                                          null);
		if (repositoriesString == null || "".equals(repositoriesString)) {
			log.error("No secret repositories have been configured");
			return false;
		}

		String[] repositories = repositoriesString.split(",");
		if (repositories == null || repositories.length == 0) {
			log.error("No secret repositories have been configured");
			return false;
		}

		// Create a KeyStore Information for private key entry KeyStore
		IdentityKeyStoreInformation identityInformation =
		                                                  KeyStoreInformationFactory.createIdentityKeyStoreInformation(properties);

		// Create a KeyStore Information for trusted certificate KeyStore
		TrustKeyStoreInformation trustInformation =
		                                            KeyStoreInformationFactory.createTrustKeyStoreInformation(properties);

		String identityKeyPass = null;
		String identityStorePass = null;
		String trustStorePass = null;
		if (identityInformation != null) {
			identityKeyPass = identityInformation.getKeyPasswordProvider().getResolvedSecret();
			identityStorePass =
			                    identityInformation.getKeyStorePasswordProvider()
			                                       .getResolvedSecret();
		}

		if (trustInformation != null) {
			trustStorePass = trustInformation.getKeyStorePasswordProvider().getResolvedSecret();
		}

		if (!validatePasswords(identityStorePass, identityKeyPass, trustStorePass)) {

			log.error("Either Identity or Trust keystore password is mandatory"
			          + " in order to initialized secret manager.");
			return false;
		}

		identityKeyStoreWrapper = new IdentityKeyStoreWrapper();
		identityKeyStoreWrapper.init(identityInformation, identityKeyPass);

		trustKeyStoreWrapper = new TrustKeyStoreWrapper();
		if (trustInformation != null) {
			trustKeyStoreWrapper.init(trustInformation);
		}

		SecretRepository currentParent = null;
		for (String secretRepo : repositories) {

			StringBuffer sb = new StringBuffer();
			sb.append(SecureVaultConstants.PROP_SECRET_REPOSITORIES);
			sb.append(SecureVaultConstants.DOT);
			sb.append(secretRepo);
			String id = sb.toString();
			sb.append(SecureVaultConstants.DOT);
			sb.append(SecureVaultConstants.PROP_PROVIDER);

			String provider =
			                  MiscellaneousUtil.getProperty(configurationProperties, sb.toString(),
			                                                null);
			if (provider == null || "".equals(provider)) {
				handleException("Repository provider cannot be null ");
			}

			if (log.isDebugEnabled()) {
				log.debug("Initiating a File Based Secret Repository");
			}

		}
		return true;
	}

	private boolean validatePasswords(String identityStorePass, String identityKeyPass,
	                                  String trustStorePass) {
		boolean isValid = false;
		if (trustStorePass != null && !"".equals(trustStorePass)) {
			if (log.isDebugEnabled()) {
				log.debug("Trust Store Password cannot be found.");
			}
			isValid = true;
		} else {
			if (identityStorePass != null && !"".equals(identityStorePass) &&
			    identityKeyPass != null && !"".equals(identityKeyPass)) {
				if (log.isDebugEnabled()) {
					log.debug("Identity Store Password "
					          + "and Identity Store private key Password cannot be found.");
				}
				isValid = true;
			}
		}
		return isValid;
	}

	protected void initCipherDecryptProvider() {
		if(decryptionProvider !=null) return;
		Properties properties = SecureVaultUtil.loadProperties();
		StringBuffer sb = new StringBuffer();
		// sb.append(id);
		sb.append(DOT);
		sb.append(LOCATION);

		StringBuffer sbTwo = new StringBuffer();
		// sbTwo.append(id);
		sbTwo.append(DOT);
		sbTwo.append(ALGORITHM);
		// Load algorithm
		String algorithm =
		                   MiscellaneousUtil.getProperty(properties, sbTwo.toString(),
		                                                 DEFAULT_ALGORITHM);
		StringBuffer buffer = new StringBuffer();
		buffer.append(DOT);
		buffer.append(KEY_STORE);

		// Load keyStore
		String keyStore = MiscellaneousUtil.getProperty(properties, buffer.toString(), null);

		KeyStoreWrapper keyStoreWrapper;

		if (TRUSTED.equals(keyStore)) {
			keyStoreWrapper = trustKeyStoreWrapper;

		} else {
			keyStoreWrapper = identityKeyStoreWrapper;
		}

		CipherInformation cipherInformation = new CipherInformation();
		cipherInformation.setAlgorithm(algorithm);
		cipherInformation.setCipherOperationMode(CipherOperationMode.DECRYPT);
		cipherInformation.setInType(EncodingType.BASE64); // TODO
		decryptionProvider = CipherFactory.createCipher(cipherInformation, keyStoreWrapper);

	}


	
	/**
	 * Initializing the encryption key store which uses to encrypt the given
	 * plain text
	 * 
	 */
	public void initEncrypt() {
	
		if(encryptionProvider != null) return;
		
		Properties properties = SecureVaultUtil.loadProperties();
			
		String keyStoreFile;
		String keyType;
		String aliasName;
		String password;
		String provider = null;
		Cipher cipher = null;

		keyStoreFile = properties.getProperty("keystore.identity.location");

		File keyStore = new File(keyStoreFile);

		if (!keyStore.exists()) {
			handleException("Primary Key Store Can not be found at Default location");
		}
		
		keyType =  properties.getProperty("keystore.identity.type"); 
		aliasName = properties.getProperty("keystore.identity.alias"); ;
	
		// Create a KeyStore Information for private key entry KeyStore
		IdentityKeyStoreInformation identityInformation = KeyStoreInformationFactory.createIdentityKeyStoreInformation(properties);

		password = identityInformation.getKeyStorePasswordProvider().getResolvedSecret();

		try {
			KeyStore primaryKeyStore = getKeyStore(keyStoreFile, password, keyType, provider);
			java.security.cert.Certificate certs = primaryKeyStore.getCertificate(aliasName);
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, certs);
		} catch (InvalidKeyException e) {
			handleException("Error initializing Cipher ", e);
		} catch (NoSuchAlgorithmException e) {
			handleException("Error initializing Cipher ", e);
		} catch (KeyStoreException e) {
			handleException("Error initializing Cipher ", e);
		} catch (NoSuchPaddingException e) {
			handleException("Error initializing Cipher ", e);
		}
		encryptionProvider = cipher;
	}
	
	
	/**
	 * get the primary key store instant
	 * 
	 * @param location
	 *            location of key store
	 * @param storePassword
	 *            password of key store
	 * @param storeType
	 *            key store type
	 * @param provider
	 *            key store provider
	 * @return KeyStore instant
	 */
	private static KeyStore getKeyStore(String location, String storePassword, String storeType,
	                                    String provider) {

		File keyStoreFile = new File(location);
		if (!keyStoreFile.exists()) {
			handleException("KeyStore can not be found at ' " + keyStoreFile + " '");
		}
		if (storePassword == null) {
			handleException("KeyStore password can not be null");
		}
		if (storeType == null) {
			handleException("KeyStore Type can not be null");
		}
		BufferedInputStream bufferedInputStream = null;
		try {
			bufferedInputStream = new BufferedInputStream(new FileInputStream(keyStoreFile));
			KeyStore keyStore;
			if (provider != null) {
				keyStore = KeyStore.getInstance(storeType, provider);
			} else {
				keyStore = KeyStore.getInstance(storeType);
			}
			keyStore.load(bufferedInputStream, storePassword.toCharArray());
			return keyStore;
		} catch (KeyStoreException e) {
			handleException("Error loading keyStore from ' " + location + " ' ", e);
		} catch (IOException e) {
			handleException("IOError loading keyStore from ' " + location + " ' ", e);
		} catch (NoSuchAlgorithmException e) {
			handleException("Error loading keyStore from ' " + location + " ' ", e);
		} catch (CertificateException e) {
			handleException("Error loading keyStore from ' " + location + " ' ", e);
		} catch (NoSuchProviderException e) {
			handleException("Error loading keyStore from ' " + location + " ' ", e);
		} finally {
			if (bufferedInputStream != null) {
				try {
					bufferedInputStream.close();
				} catch (IOException ignored) {
					System.err.println("Error while closing input stream");
				}
			}
		}
		return null;
	}
	
	
	protected static void handleException(String msg, Exception e) {
		//throw new CipherToolException(msg, e);
	}

	protected static void handleException(String msg) {
		//throw new CipherToolException(msg);
	}


	public IdentityKeyStoreWrapper getIdentityKeyStoreWrapper() {
		return identityKeyStoreWrapper;
	}

	public void setIdentityKeyStoreWrapper(IdentityKeyStoreWrapper identityKeyStoreWrapper) {
		this.identityKeyStoreWrapper = identityKeyStoreWrapper;
	}

	public TrustKeyStoreWrapper getTrustKeyStoreWrapper() {
		return trustKeyStoreWrapper;
	}

	public void setTrustKeyStoreWrapper(TrustKeyStoreWrapper trustKeyStoreWrapper) {
		this.trustKeyStoreWrapper = trustKeyStoreWrapper;
	}

	public DecryptionProvider getDecryptionProvider() {
		return decryptionProvider;
	}

	public Cipher getEncryptionProvider() {
		return encryptionProvider;
	}

}
