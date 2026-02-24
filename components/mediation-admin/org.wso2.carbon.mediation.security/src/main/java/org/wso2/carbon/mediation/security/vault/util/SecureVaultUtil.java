package org.wso2.carbon.mediation.security.vault.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.security.vault.SecureVaultConstants;
import org.wso2.securevault.SecureVaultException;

public class SecureVaultUtil {

	private static Log log = LogFactory.getLog(SecureVaultUtil.class);

	public static Properties loadProperties() {
		Properties properties = new Properties();
		String confPath = System.getProperty(SecureVaultConstants.CONF_LOCATION);
		if (confPath == null) {
			confPath = Paths.get("repository", "conf").toString();
		}
		String filePath = Paths.get(confPath, SecureVaultConstants.SECURITY_DIR, SecureVaultConstants.SECRET_CONF).toString();

		File dataSourceFile = new File(filePath);
		if (!dataSourceFile.exists()) {
			return properties;
		}

		InputStream in = null;
		try {
			in = new FileInputStream(dataSourceFile);
			properties.load(in);
		} catch (IOException e) {
			String msg = "Error loading properties from a file at :" + filePath;
			log.warn(msg, e);
			return properties;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignored) {

				}
			}
		}
		return properties;
	}

	/**
	 * Decodes a Base64-encoded string into a JSON object.
	 *
	 * @param encryptedText Base64-encoded JSON string.
	 * @return Parsed {@link JsonObject}.
	 * @throws SecureVaultException If decoding or JSON parsing fails.
	 */
	public static JsonObject getJsonObject(String encryptedText) {

		try {
			String jsonString = new String(Base64Utils.decode(encryptedText));
			return JsonParser.parseString(jsonString).getAsJsonObject();
		} catch (JsonSyntaxException e) {
			throw new SecureVaultException("Invalid encrypted text: JSON parsing failed.", log);
		}
	}

	/**
	 * Retrieves a string value from a given {@link JsonObject}.
	 *
	 * @param jsonObject The JSON object containing the value.
	 * @param value      The key whose associated value should be returned.
	 * @return The string value mapped to the given key.
	 * @throws SecureVaultException If the key is not present in the JSON object.
	 */
	public static String getValueFromJson(JsonObject jsonObject, String value) {

		JsonElement jsonElement = jsonObject.get(value);
		if (jsonElement == null) {
			throw new SecureVaultException(String.format("Value \"%s\" not found in JSON", value), log);
		}
		return jsonElement.getAsString();
	}

	/**
	 * Creates a self-contained ciphertext with GCM mode.
	 *
	 * @param ciphertext Original ciphertext to be included in the JSON object.
	 * @param iv         Initialization vector.
	 * @return Base64 encoded JSON object containing the ciphertext and IV.
	 */
	public static String createSelfContainedCiphertextWithGCMMode(String ciphertext, byte[] iv) {

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(SecureVaultConstants.CIPHER_TEXT, ciphertext);
		jsonObject.addProperty(SecureVaultConstants.IV, java.util.Base64.getEncoder().encodeToString(iv));
		return java.util.Base64.getEncoder().encodeToString(new Gson().toJson(jsonObject).getBytes());
	}
}
