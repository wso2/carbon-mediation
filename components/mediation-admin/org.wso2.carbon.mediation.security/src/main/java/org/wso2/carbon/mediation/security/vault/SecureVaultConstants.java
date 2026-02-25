package org.wso2.carbon.mediation.security.vault;

public interface SecureVaultConstants {
	public static final String SYSTEM_CONFIG_CONNECTOR_SECURE_VAULT_CONFIG =
	                                                                         "/_system/config/repository/components/secure-vault";
	public static final String CONNECTOR_SECURE_VAULT_CONFIG_REPOSITORY =
	                                                                      "/repository/components/secure-vault";
	public static final String CARBON_HOME = "carbon.home";
	public static final String SECRET_CONF = "secret-conf.properties";
	public static final String CONF_LOCATION = "conf.location";
	public static final String CONF_DIR = "conf";
	public static final String REPOSITORY_DIR = "repository";
	public static final String SECURITY_DIR = "security";
	/* Default configuration file path for secret manager */
	public final static String PROP_DEFAULT_CONF_LOCATION = "secret-manager.properties";
	/*
	 * If the location of the secret manager configuration is provided as a
	 * property- it's name
	 */
	public final static String PROP_SECRET_MANAGER_CONF = "secret.manager.conf";
	/* Property key for secretRepositories */
	public final static String PROP_SECRET_REPOSITORIES = "secretRepositories";
	/* Type of the secret repository */
	public final static String PROP_PROVIDER = "provider";
	/* Dot string */
	public final static String DOT = ".";

	// property key for global secret provider
	public final static String PROP_SECRET_PROVIDER = "carbon.secretProvider";

	public final static String SERVELT_SESSION = "comp.mgt.servlet.session";

	public static final String CONF_CONNECTOR_SECURE_VAULT_CONFIG_PROP_LOOK =
	                                                                          "conf:/repository/components/secure-vault";

	/**
	 * System property to specify customized docker secret root directory
	 */
	public static final String PROP_DOCKER_SECRET_ROOT_DIRECTORY = "ei.secret.docker.root.dir";
	public static final String PROP_DOCKER_SECRET_ROOT_DIRECTORY_DEFAULT = "/run/secrets/";
	public static final String PROP_DOCKER_SECRET_ROOT_DIRECTORY_DEFAULT_WIN = "C:\\ProgramData\\Docker\\secrets";

	/**
	 * System property to specify customized file secret root directory
	 */
	public static final String PROP_FILE_SECRET_ROOT_DIRECTORY = "ei.secret.file.root.dir";
	public static final String PROP_FILE_SECRET_ROOT_DIRECTORY_DEFAULT = System.getProperty(CARBON_HOME);

	public static final String FILE_PROTOCOL_PREFIX = "file:";

	public static final String SYMMETRIC = "symmetric";
	public static final String RSA = "RSA";
	public static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
	public static final String CIPHER_TEXT = "cipherText";
	public static final String IV = "iv";
	public static final int GCM_TAG_LENGTH = 128;
	public static final int GCM_IV_LENGTH = 12;

	public final static String SYMMETRIC_ENCRYPTION_KEY_PROMPT = "Symmetric Encryption Key > ";
	public final static String KEY_BASED_SECRET_PROVIDER = "key.based";
	public static final String SECRET_FILE_ENCRYPTION_MODE = "secretRepositories.file.encryptionMode";
	public static final String KEY_BASED_SYMMETRIC_ENCRYPTION = "key.based.symmetric.encryption";

}
