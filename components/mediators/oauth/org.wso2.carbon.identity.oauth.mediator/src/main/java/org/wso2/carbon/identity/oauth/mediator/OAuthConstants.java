package org.wso2.carbon.identity.oauth.mediator;

public final class OAuthConstants {

	// OAuth request parameters
	public static final String OAUTH_VERSION = "oauth_version";
	public static final String OAUTH_NONCE = "oauth_nonce";
	public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
	public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
	public static final String OAUTH_CALLBACK = "oauth_callback";
	public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
	public static final String OAUTH_SIGNATURE = "oauth_signature";
	public static final String SCOPE = "scope";
	public static final String OAUTH_DISPLAY_NAME = "xoauth_displayname";


	// OAuth response parameters
	public static final String OAUTH_TOKEN = "oauth_token";
	public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
	public static final String OAUTH_CALLBACK_CONFIRMED = "oauth_callback_confirmed";
	public static final String OAUTH_VERIFIER = "oauth_verifier";

	
	public static final String ASSOCIATION_OAUTH_CONSUMER_TOKEN = "ASSOCIATION_OAUTH_CONSUMER_TOKEN";
	public static final String OAUTHORIZED_USER = "OAUTHORIZED_USER";

	// OAuth 2.0 parameters
	public static final String BEARER = "Bearer ";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String BEARER_TOKEN_TYPE="bearer";

}
