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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.security.vault.util.SecureVaultUtil;
import org.wso2.securevault.CipherFactory;
import org.wso2.securevault.CipherOperationMode;
import org.wso2.securevault.DecryptionProvider;
import org.wso2.securevault.EncodingType;
import org.wso2.securevault.SymmetricCipher;
import org.wso2.securevault.commons.MiscellaneousUtil;
import org.wso2.securevault.definition.CipherInformation;
import org.wso2.securevault.definition.IdentityKeyStoreInformation;
import org.wso2.securevault.definition.KeyStoreInformationFactory;
import org.wso2.securevault.definition.TrustKeyStoreInformation;
import org.wso2.securevault.encryption.EncryptionKeyWrapper;
import org.wso2.securevault.keystore.IdentityKeyStoreWrapper;
import org.wso2.securevault.keystore.KeyStoreWrapper;
import org.wso2.securevault.keystore.TrustKeyStoreWrapper;
import org.wso2.securevault.secret.SecretInformation;
import org.wso2.securevault.secret.SecretInformationFactory;
import org.wso2.securevault.secret.SecretRepository;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.SecureRandom;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherInitializer {
	private static Log log = LogFactory.getLog(CipherInitializer.class);

	private static final String LOCATION = "location";
	private static final String KEY_STORE = "keyStore";
	private static final String DOT = ".";
	private static final String ALGORITHM = "algorithm";
	private static final String TRUSTED = "trusted";
	private static final String CIPHER_TRANSFORMATION_SECRET_CONF_PROPERTY = "keystore.identity.CipherTransformation";
	private static final String CIPHER_TRANSFORMATION_SYSTEM_PROPERTY = "org.wso2.CipherTransformation";
	private static CipherInitializer cipherInitializer  = new CipherInitializer();;

	// global password provider implementation class if defined in secret
	// manager conf file
	private String globalSecretProvider = null;

	private IdentityKeyStoreWrapper identityKeyStoreWrapper;

	private TrustKeyStoreWrapper trustKeyStoreWrapper;

	private EncryptionKeyWrapper encryptionKeyWrapper;

	private DecryptionProvider decryptionProvider = null;

	private Cipher encryptionProvider = null;

	private String algorithm = SecureVaultConstants.RSA;

	private byte[] iv;
	
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

	/**
	 * Wrapper class to atomically pair a Cipher with its corresponding IV.
	 */
	public static class CipherWithIV {
		private final Cipher cipher;
		private final byte[] iv;

		public CipherWithIV(Cipher cipher, byte[] iv) {
			this.cipher = cipher;
			this.iv = iv != null ? iv.clone() : null;
		}

		public Cipher getCipher() {
			return cipher;
		}

		public byte[] getIv() {
			return iv != null ? iv.clone() : null;
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

		String secretRepositoryEncryptionMode = MiscellaneousUtil.getProperty(properties,
				SecureVaultConstants.SECRET_FILE_ENCRYPTION_MODE, null);
		boolean keyBasedSymmetricEncryption = isKeyBasedSymmetricEncryption(secretRepositoryEncryptionMode);

		if (keyBasedSymmetricEncryption) {
			log.debug("Symmetric key encryption is configured. Hence skipping the initialization of keystores.");
			SecretInformation secretInformation = SecretInformationFactory.createSecretInformation(properties,
					SecureVaultConstants.KEY_BASED_SECRET_PROVIDER + DOT,
					SecureVaultConstants.SYMMETRIC_ENCRYPTION_KEY_PROMPT);
			String encryptionKey = createEncryptionKey(secretInformation);
			if (encryptionKey == null || encryptionKey.isEmpty()) {
				log.error("Encryption key is mandatory in order to initialize secret manager.");
				return false;
			}
			encryptionKeyWrapper = new EncryptionKeyWrapper();
			encryptionKeyWrapper.init(secretInformation, encryptionKey);
            log.debug("Encryption key wrapper initialized successfully for symmetric encryption.");
		} else {
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
				identityStorePass = identityInformation.getKeyStorePasswordProvider().getResolvedSecret();
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
		String algorithm = getCipherTransformation(properties);

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

		this.algorithm = algorithm;
		CipherInformation cipherInformation = new CipherInformation();
		cipherInformation.setAlgorithm(algorithm);
		cipherInformation.setCipherOperationMode(CipherOperationMode.DECRYPT);
		cipherInformation.setInType(EncodingType.BASE64); // TODO
		String secretRepositoryEncryptionMode = MiscellaneousUtil.getProperty(properties,
				SecureVaultConstants.SECRET_FILE_ENCRYPTION_MODE, null);
		boolean keyBasedSymmetricEncryption = isKeyBasedSymmetricEncryption(secretRepositoryEncryptionMode);
		if (keyBasedSymmetricEncryption) {
			cipherInformation.setType(SecureVaultConstants.SYMMETRIC);
			cipherInformation.setKeyBasedSymmetricEncryption(true);
			decryptionProvider = new SymmetricCipher(cipherInformation, encryptionKeyWrapper);
		} else {
			decryptionProvider = CipherFactory.createCipher(cipherInformation, keyStoreWrapper);
		}
	}

	/**
	 * Checks whether symmetric encryption is configured using the provided encryption mode.
	 */
	public boolean isKeyBasedSymmetricEncryption(String secretRepositoryEncryptionMode) {

		if (secretRepositoryEncryptionMode == null) {
			log.debug("Symmetric key encryption is not configured.");
			return false;
		}
		if (secretRepositoryEncryptionMode.equals(SecureVaultConstants.KEY_BASED_SYMMETRIC_ENCRYPTION)) {
			log.debug("Input key based symmetric encryption is configured.");
			return true;
		}
		return false;
	}

	/**
	 * Create the encryption key.
	 *
	 * @param secretInformation Encryption Information for symmetric key encryption.
	 * @return encryptionKey.
	 */
	private String createEncryptionKey(SecretInformation secretInformation) {

		String encryptionKey = null;

		if (secretInformation != null) {
			encryptionKey = secretInformation.getResolvedSecret();
		}
		return encryptionKey;
	}

	/**
	 * Generates a new initialization vector (IV) for GCM encryption.
	 *
	 * @return the generated IV as a byte array
	 */
	private byte[] getInitializationVector() {

		byte[] iv = new byte[SecureVaultConstants.GCM_IV_LENGTH];
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(iv);
		return iv;
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

		String secretRepositoryEncryptionMode = MiscellaneousUtil.getProperty(properties,
				SecureVaultConstants.SECRET_FILE_ENCRYPTION_MODE, null);
		boolean keyBasedSymmetricEncryption = isKeyBasedSymmetricEncryption(secretRepositoryEncryptionMode);
		if (keyBasedSymmetricEncryption) {
			try {
				String cipherTransformation = System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY);
				String algorithm = StringUtils.isNotBlank(cipherTransformation) ?
						cipherTransformation :
						SecureVaultConstants.AES_GCM_NO_PADDING;
				if (this.encryptionKeyWrapper == null) {
					SecretInformation secretInformation = SecretInformationFactory.createSecretInformation(properties,
							SecureVaultConstants.KEY_BASED_SECRET_PROVIDER + DOT,
							SecureVaultConstants.SYMMETRIC_ENCRYPTION_KEY_PROMPT);
					String encryptionKey = createEncryptionKey(secretInformation);
					if (encryptionKey == null || encryptionKey.isEmpty()) {
						handleException("Encryption key is mandatory in order to initialize cipher.");
					}
					this.encryptionKeyWrapper = new EncryptionKeyWrapper();
					this.encryptionKeyWrapper.init(secretInformation, encryptionKey);
				}
				this.algorithm = algorithm;
				byte[] keyBytes = this.encryptionKeyWrapper.getSecretKeyBytes();
				String baseAlgorithm = algorithm.split("/")[0];
				Key key = new SecretKeySpec(keyBytes, baseAlgorithm);
				cipher = Cipher.getInstance(algorithm);
				if (SecureVaultConstants.AES_GCM_NO_PADDING.equals(algorithm)) {
					byte[] iv = getInitializationVector();
					this.iv = iv;
					cipher.init(Cipher.ENCRYPT_MODE, key,
							new GCMParameterSpec(SecureVaultConstants.GCM_TAG_LENGTH, iv));
				} else {
					cipher.init(Cipher.ENCRYPT_MODE, key);
				}
			} catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException |
					NoSuchPaddingException e) {
				handleException("Error initializing Cipher ", e);
			}
		} else {

			keyStoreFile = properties.getProperty("keystore.identity.location");

			File keyStore = new File(keyStoreFile);

			if (!keyStore.exists()) {
				handleException("Primary Key Store Can not be found at Default location");
			}

			keyType = properties.getProperty("keystore.identity.type");
			aliasName = properties.getProperty("keystore.identity.alias");

			// Create a KeyStore Information for private key entry KeyStore
			IdentityKeyStoreInformation identityInformation =
					KeyStoreInformationFactory.createIdentityKeyStoreInformation(properties);

			password = identityInformation.getKeyStorePasswordProvider().getResolvedSecret();

			try {
				KeyStore primaryKeyStore = getKeyStore(keyStoreFile, password, keyType, provider);
				java.security.cert.Certificate certs = primaryKeyStore.getCertificate(aliasName);

				String algorithm = getCipherTransformation(properties);

				cipher = Cipher.getInstance(algorithm);
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
		}
		encryptionProvider = cipher;
	}

	/**
	 * Get the Cipher Transformation to be used by the Cipher. We have the option of configuring this globally as a
	 * System Property '-Dorg.wso2.CipherTransformation', which can be overridden at the 'secret-conf.properties' level
	 * by specifying the property 'keystore.identity.CipherTransformation'. If neither are configured the default
	 * will be used
	 *
	 * @param properties Properties from the 'secret-conf.properties' file
	 * @return Cipher Transformation String
	 */
	private String getCipherTransformation(Properties properties) {
		String cipherTransformation = System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY);

		String secretRepositoryEncryptionMode = MiscellaneousUtil.getProperty(properties,
				SecureVaultConstants.SECRET_FILE_ENCRYPTION_MODE, null);
		boolean symmetricEncryptionEnabled = SecureVaultConstants.SYMMETRIC.equals(
				secretRepositoryEncryptionMode) || SecureVaultConstants.KEY_BASED_SYMMETRIC_ENCRYPTION.equals(
				secretRepositoryEncryptionMode);

		if (cipherTransformation == null) {
			if (symmetricEncryptionEnabled) {
				return SecureVaultConstants.AES_GCM_NO_PADDING;
			}
			cipherTransformation = SecureVaultConstants.RSA;
		}

		if (symmetricEncryptionEnabled) {
			return cipherTransformation;
		}

		return MiscellaneousUtil.getProperty(properties, CIPHER_TRANSFORMATION_SECRET_CONF_PROPERTY,
				cipherTransformation);
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
		throw new RuntimeException(msg, e);
	}

	protected static void handleException(String msg) {
		throw new RuntimeException(msg);
	}


	public IdentityKeyStoreWrapper getIdentityKeyStoreWrapper() {
		return identityKeyStoreWrapper;
	}

	public void setIdentityKeyStoreWrapper(IdentityKeyStoreWrapper identityKeyStoreWrapper) {
		this.identityKeyStoreWrapper = identityKeyStoreWrapper;
	}

	public EncryptionKeyWrapper getEncryptionKeyWrapper() {
		return encryptionKeyWrapper;
	}

	public void setEncryptionKeyWrapper(EncryptionKeyWrapper encryptionKeyWrapper) {
		this.encryptionKeyWrapper = encryptionKeyWrapper;
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

	public String getAlgorithm() {
		return algorithm;
	}

	public byte[] getIv() {
		return iv;
	}

	/**
	 * Gets an encryption provider for GCM mode with a new IV.
	 * This method should be called for each GCM encryption operation to ensure
	 * that a unique key-IV combination is used, which is required for GCM security.
	 *
	 * @return A CipherWithIV instance containing both the Cipher and its corresponding IV
	 */
	public synchronized CipherWithIV getGCMEncryptionProvider() {
		if (!SecureVaultConstants.AES_GCM_NO_PADDING.equals(algorithm)) {
			throw new IllegalStateException(
					"GCM encryption is only used for AES-GCM algorithm, but algorithm is: " + algorithm);
		}

		if (this.encryptionKeyWrapper == null) {
			handleException("GCM encryption requires symmetric key encryption to be configured");
			return null;
		}
		
		try {
			byte[] gcmIv = getInitializationVector();

			byte[] keyBytes = this.encryptionKeyWrapper.getSecretKeyBytes();
			String baseAlgorithm = algorithm.split("/")[0];
			Key key = new SecretKeySpec(keyBytes, baseAlgorithm);
			
			Cipher gcmCipher = Cipher.getInstance(algorithm);
			gcmCipher.init(Cipher.ENCRYPT_MODE, key,
				new GCMParameterSpec(SecureVaultConstants.GCM_TAG_LENGTH, gcmIv));
			
			return new CipherWithIV(gcmCipher, gcmIv);
		} catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException |
				NoSuchPaddingException e) {
			handleException("Error creating GCM cipher ", e);
			return null;
		}
	}
}
