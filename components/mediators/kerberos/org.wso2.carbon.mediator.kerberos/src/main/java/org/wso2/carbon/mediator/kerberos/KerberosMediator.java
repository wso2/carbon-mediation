/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediator.kerberos;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import com.sun.security.auth.login.ConfigFile;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

import org.apache.synapse.mediators.Value;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

public class KerberosMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(KerberosMediator.class);

    // Basic configuration parameters.
    private String spn;
    private Value spnKey;
    private Value clientPrincipal;
    private Value password;
    private Value keytabFile;
    private String krb5Config;
    private String clientPrincipalValue;
    private String passwordValue;
    private String keytabPath;
    private Map<String, ?> logDetails = new HashMap<>();

    // Optional parameters.
    private String loginContextName;
    private String loginConfig;

    private GSSManager gssManager = GSSManager.getInstance();

    private static final String ROOT = System.getProperty(KerberosConstants.CARBON_HOME, ".");
    private static final String CONFIG_PATH = ROOT + File.separator + "repository" + File.separator
            + "resources" + File.separator + "security" + File.separator;
    private static final String DEFAULT_KERBEROS_CONFIG_PATH = ROOT + File.separator + "repository" + File.separator
            + "resources" + File.separator + "security" + File.separator + KerberosConstants.DEFAULT_KERBEROS_CONFIG_FILE;
    private static final String DEFAULT_LOGIN_CONFIG_PATH = ROOT + File.separator + "repository" + File.separator
            + "resources" + File.separator + "security" + File.separator + KerberosConstants.DEFAULT_LOGIN_CONFIG_FILE;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mediate(MessageContext synCtx) {

        if (synCtx.getEnvironment().isDebuggerEnabled() && super.divertMediationRoute(synCtx)) {
            return true;
        }

        //Evaluate values
        extractDataFromLoginConf(synCtx);

        //Set kerberos configurations.
        setKerberosConfigurations(synCtx);

        //Create Kerberos token and set to the message context header.
        GSSContext gssContext = null;
        GSSName serverName;
        try {
            Oid mechanismOId = new Oid(KerberosConstants.SPNEGO_BASED_OID);
            GSSCredential gssCredentials = createCredentials(mechanismOId, synCtx);
            if (StringUtils.isNotEmpty(getSpnValueFromRegistry(synCtx))) {
                serverName = gssManager.createName(getSpnValueFromRegistry(synCtx), GSSName.NT_USER_NAME);
            } else {
                serverName = gssManager.createName(getSpn(), GSSName.NT_USER_NAME);
            }
            gssContext = gssManager.createContext(serverName.canonicalize(mechanismOId),
                    mechanismOId, gssCredentials, GSSContext.DEFAULT_LIFETIME);
            byte[] token = new byte[0];
            byte[] serviceTicket = gssContext.initSecContext(token, 0, token.length);

            //Add authorization header to the message context.
            if (serviceTicket != null) {
                setAuthorizationHeader((Axis2MessageContext) synCtx, serviceTicket);
            } else {
                log.error("Unable to get the Kerberos service ticket.");
                return false;
            }
        } catch (LoginException | PrivilegedActionException | GSSException e) {
            log.error("Error while creating the Kerberos service ticket.", e);
            return false;
        } catch (UnsupportedEncodingException e) {
            log.error("Unable to encrypt the Kerberos service ticket.", e);
            return false;
        } finally {
            if (gssContext != null) {
                try {
                    gssContext.dispose();
                } catch (GSSException e) {
                    log.warn("Error while disposing GSS Context", e);
                }
            }
        }
        return true;
    }

    /**
     * Retrieve the spn value from registry.
     *
     * @param msgCtx message context.
     */
    private String getSpnValueFromRegistry(MessageContext msgCtx) {

        if (getSpnKey() != null) {
            String generatedSpnConfigKey = getSpnKey().evaluateValue(msgCtx);
            Object entry = msgCtx.getEntry(generatedSpnConfigKey);
            if (entry == null) {
                handleException("Key " + generatedSpnConfigKey + " not found ", msgCtx);
            }
            if (entry instanceof OMTextImpl) {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving the spnConfig key :" + generatedSpnConfigKey);
                }
                OMTextImpl text = (OMTextImpl) entry;
                return text.getText();
            }
        }
        return null;
    }

    /**
     * Set the values for username, password and keytab path.
     *
     * @param synCtx message context.
     */
    private void setElements(Map<String, ? > logDetails, MessageContext synCtx) {
        //Set username.
        if (getClientPrincipal() != null) {
            this.clientPrincipalValue = getClientPrincipal().getKeyValue();
        } else {
            this.clientPrincipalValue = logDetails.get("principal").toString();
        }

        //Set password.
        if (this.password != null) {
            this.passwordValue = this.password.getKeyValue();
        }

        //Set keytab path.
        if (getKeytabFileName() != null) {
            this.keytabPath = CONFIG_PATH + getKeytabFileName().evaluateValue(synCtx);
        } else {
            this.keytabPath = logDetails.get("keyTab").toString();
        }
    }

    /**
     * Set the authorization header to the message context.
     *
     * @param synCtx        message context.
     * @param serviceTicket Kerberos ticket.
     * @throws UnsupportedEncodingException on error while encrypting the token.
     */
    private void setAuthorizationHeader(Axis2MessageContext synCtx, byte[] serviceTicket)
            throws UnsupportedEncodingException {

        org.apache.axis2.context.MessageContext msgCtx = synCtx.getAxis2MessageContext();
        Map<String, Object> headerProp = new HashMap<>();
        headerProp.put(HttpHeaders.AUTHORIZATION, KerberosConstants.NEGOTIATE + " " +
                new String(Base64.encodeBase64(serviceTicket), "UTF-8"));
        msgCtx.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headerProp);

        Map<String, String> headers = (Map<String, String>) msgCtx.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        ConcurrentHashMap<String, Object> headerProperties = new ConcurrentHashMap<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headerProperties.put(entry.getKey(), entry.getValue());
        }
        headerProperties.put(HttpHeaders.AUTHORIZATION, KerberosConstants.NEGOTIATE + " " +
                new String(Base64.encodeBase64(serviceTicket), KerberosConstants.UTF8));
        msgCtx.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headerProperties);
    }

    /**
     * Create GSSCredential for the subject.
     *
     * @param mechanismOId Oid for the mechanism.
     * @return GSSCredential.
     * @throws LoginException
     * @throws PrivilegedActionException
     * @throws GSSException
     */
    private GSSCredential createCredentials(Oid mechanismOId, MessageContext synCtx) throws LoginException,
            PrivilegedActionException, GSSException {

        CallbackHandler callbackHandler;
        if (StringUtils.isNotEmpty(clientPrincipalValue)) {
            if (StringUtils.isNotEmpty(passwordValue)) {
                setJASSConfiguration(false, synCtx);
                callbackHandler = getUserNamePasswordCallbackHandler(clientPrincipalValue, passwordValue.toCharArray());
            } else if (StringUtils.isNotEmpty(keytabPath)) {
                setJASSConfiguration(true, synCtx);
                callbackHandler = null;
            } else {
                throw new SynapseException("Could not find the password or keyTab to authenticate the user.");
            }
        } else {
            throw new SynapseException("Could not find the username to authenticate the user.");
        }
        return createClientCredentials(callbackHandler, mechanismOId);
    }

    /**
     * Create GSSCredential for the user.
     *
     * @param callbackHandler callback handler.
     * @param mechanismOId    Oid for the mechanism.
     * @return GSSCredential.
     * @throws LoginException
     * @throws PrivilegedActionException
     * @throws GSSException
     */
    private GSSCredential createClientCredentials(CallbackHandler callbackHandler, final Oid mechanismOId)
            throws LoginException, PrivilegedActionException, GSSException {

        LoginContext loginContext;
        String loginName;
        if (StringUtils.isNotEmpty(getLoginContextName())) {
            loginName = getLoginContextName();
        } else {
            loginName = "com.sun.security.auth.module.Krb5LoginModule";
        }
        if (callbackHandler != null) {
            loginContext = new LoginContext(loginName, callbackHandler);
        } else {
            loginContext = new LoginContext(loginName);
        }
        loginContext.login();
        if (log.isDebugEnabled()) {
            log.debug("Pre-authentication successful for with Kerberos Server.");
        }

        // Create client credentials from pre authentication with the AD
        final GSSName clientName = gssManager.createName(clientPrincipalValue, GSSName.NT_USER_NAME);
        final PrivilegedExceptionAction<GSSCredential> action = new PrivilegedExceptionAction<GSSCredential>() {
            public GSSCredential run() throws GSSException {

                return gssManager.createCredential(clientName.canonicalize(mechanismOId),
                        GSSCredential.DEFAULT_LIFETIME, mechanismOId, GSSCredential.INITIATE_ONLY);
            }
        };

        if (log.isDebugEnabled()) {
            Set<Principal> principals = loginContext.getSubject().getPrincipals();
            String principalName = null;
            if (principals != null) {
                principalName = principals.toString();
            }
            log.debug("Creating gss credentials as principal : " + principalName);
        }
        return Subject.doAs(loginContext.getSubject(), action);
    }

    /**
     * Create call back handler using given username and password.
     *
     * @param username username.
     * @param password password.
     * @return CallbackHandler.
     */
    private CallbackHandler getUserNamePasswordCallbackHandler(final String username, final char[] password) {

        return new CallbackHandler() {
            public void handle(final Callback[] callback) {

                for (Callback currentCallBack : callback) {
                    if (currentCallBack instanceof NameCallback) {
                        final NameCallback nameCallback = (NameCallback) currentCallBack;
                        nameCallback.setName(username);
                    } else if (currentCallBack instanceof PasswordCallback) {
                        final PasswordCallback passCallback = (PasswordCallback) currentCallBack;
                        passCallback.setPassword(password);
                    } else {
                        log.error("Unsupported Callback class = " + currentCallBack.getClass().getName());
                    }
                }
            }
        };
    }

    /**
     * Set Kerberos configuration.
     *
     * @param msgCtx message context.
     */
    private void setKerberosConfigurations(MessageContext msgCtx) {
        String krb5ConfigPath = null;
        if (StringUtils.isNotEmpty(getKrb5Config())) {
            krb5ConfigPath = CONFIG_PATH + getKrb5Config();
        } else {
            krb5ConfigPath = DEFAULT_KERBEROS_CONFIG_PATH;
        }
        File file = new File(krb5ConfigPath);
        if (file.exists()) {
            System.setProperty(KerberosConstants.KERBEROS_CONFIG_PROPERTY, file.getAbsolutePath());
        } else {
            handleException("Could not find the Kerberos configuration.", msgCtx);
        }
    }

    /**
     * Extracts data from login conf and sets the values
     * @param msgCtx
     */
    private void extractDataFromLoginConf(MessageContext msgCtx) {

        //Read configuration again if type is keytab and config is not configFile
        if(StringUtils.isNotEmpty(getLoginContextName())) {
            Configuration.setConfiguration(null);
        }

        if (StringUtils.isNotEmpty(getLoginConfig())) {
            String loginConfigPath = CONFIG_PATH + getLoginConfig();
            File file = new File(loginConfigPath);
            if (file.exists()) {
                System.setProperty(KerberosConstants.JAAS_CONFIG_PROPERTY, file.getAbsolutePath());
                AppConfigurationEntry entries[] = Configuration.getConfiguration()
                        .getAppConfigurationEntry(getLoginContextName());
                if(entries != null && entries.length != 0) {
                    Map<String, ?> options = entries[0].getOptions();
                    //Evaluate and set the values for username, password and keytab elements
                    setElements(options, msgCtx);
                } else {
                    handleException("Could not find specified service account.", msgCtx);
                }
            } else {
                handleException("Could not find the login configuration.", msgCtx);
            }
        } else if (StringUtils.isNotEmpty(getLoginContextName())) {
            String loginConfigPath = DEFAULT_LOGIN_CONFIG_PATH;
            File file = new File(loginConfigPath);
            if (file.exists()) {
                System.setProperty(KerberosConstants.JAAS_CONFIG_PROPERTY, file.getAbsolutePath());
                AppConfigurationEntry entries[] = Configuration.getConfiguration()
                        .getAppConfigurationEntry(getLoginContextName());
                if(entries != null && entries.length != 0) {
                    Map<String, ?> options = entries[0].getOptions();
                    //Evaluate and set the values for username, password and keytab elements
                    setElements(options, msgCtx);
                } else {
                    handleException("Could not find specified service account.", msgCtx);
                }
            } else {
                handleException("Could not find the login configuration.", msgCtx);
            }
        } else {
            //Set username.
            if (getClientPrincipal() != null && StringUtils.isNotEmpty(getClientPrincipal().getKeyValue())) {
                this.clientPrincipalValue = getClientPrincipal().getKeyValue();
            }

            //Set password.
            if (this.password != null && StringUtils.isNotEmpty(this.password.getKeyValue())) {
                this.passwordValue = this.password.getKeyValue();
            }
        }
    }

    /**
     * Set JASS configuration with the principal and keyTab.
     */
    private void setJASSConfiguration(boolean useKeyTab, MessageContext msgCtx) {

        Map<String, Object> optionSet = new HashMap<>();
        if (StringUtils.isNotEmpty(getLoginConfig())) {
            String loginConfigPath = CONFIG_PATH + getLoginConfig();
            File file = new File(loginConfigPath);
            if (file.exists()) {
                System.setProperty(KerberosConstants.JAAS_CONFIG_PROPERTY, file.getAbsolutePath());
                AppConfigurationEntry entries[] = Configuration.getConfiguration()
                        .getAppConfigurationEntry(getLoginContextName());
                if(entries != null && entries.length != 0) {
                    Map<String, ?> options = entries[0].getOptions();
                    for (String s : options.keySet()) {
                        optionSet.put(s, options.get(s));
                    }
                } else {
                    handleException("Could not find specified service account.", msgCtx);
                }
            } else {
                handleException("Could not find the login configuration.", msgCtx);
            }
        } else if (StringUtils.isNotEmpty(getLoginContextName())) {
            String loginConfigPath = DEFAULT_LOGIN_CONFIG_PATH;
            File file = new File(loginConfigPath);
            if (file.exists()) {
                System.setProperty(KerberosConstants.JAAS_CONFIG_PROPERTY, file.getAbsolutePath());
                AppConfigurationEntry entries[] = Configuration.getConfiguration()
                        .getAppConfigurationEntry(getLoginContextName());
                if(entries != null && entries.length != 0) {
                    Map<String, ?> options = entries[0].getOptions();
                    for (String s : options.keySet()) {
                        optionSet.put(s, options.get(s));
                    }
                } else {
                    handleException("Could not find specified service account.", msgCtx);
                }
            } else {
                handleException("Could not find the login configuration.", msgCtx);
            }
        }

        optionSet.put(KerberosConstants.IS_INITIATOR, "true");
        optionSet.put(KerberosConstants.PRINCIPAL, clientPrincipalValue);
        optionSet.put(KerberosConstants.USE_KEYTAB, String.valueOf(useKeyTab));
        if (useKeyTab) {
            File keyTabFile = new File(keytabPath);
            if (keyTabFile.exists()) {
                optionSet.put(KerberosConstants.KEYTAB, keyTabFile.getAbsolutePath());
            } else {
                handleException("Could not find the keytab file " + keytabPath +
                        " in the location " + CONFIG_PATH, msgCtx);
            }
        } else {
            optionSet.put(KerberosConstants.KEYTAB, null);
        }
        if (log.isDebugEnabled()) {
            optionSet.put(KerberosConstants.DEBUG, "true");
        }
        final Map<String, Object> finalOptionSet = optionSet;
        Configuration.setConfiguration(new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {

                return new AppConfigurationEntry[]{
                        new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, finalOptionSet)
                };
            }
        });
    }

    public String getLoginContextName() {

        return loginContextName;
    }

    public void setLoginContextName(String loginContextName) {

        this.loginContextName = loginContextName;
    }

    public String getLoginConfig() {

        return loginConfig;
    }

    public void setLoginConfig(String loginConfig) {

        this.loginConfig = loginConfig;
    }

    public String getKrb5Config() {

        return krb5Config;
    }

    public void setKrb5Config(String krb5Config) {

        this.krb5Config = krb5Config;
    }

    public String getSpn() {

        return spn;
    }

    public void setSpn(String spn) {

        this.spn = spn;
    }

    public Value getClientPrincipal() {

        return clientPrincipal;
    }

    public void setClientPrincipal(Value clientPrincipal) {

        this.clientPrincipal = clientPrincipal;
    }

    public Value getPassword() {
        if (this.password != null && !this.password.getKeyValue().startsWith("enc:")) {
            try {
                return new Value("enc:"
                        + CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(
                        this.password.getKeyValue().getBytes()));
            } catch (CryptoException e) {
                log.error(e);
            }
        }
        return password;
    }

    public void setPassword(Value password) {
        if (password.getKeyValue().startsWith("enc:")) {
            try {
                this.password = new Value(String.valueOf(CryptoUtil.getDefaultCryptoUtil()
                        .base64DecodeAndDecrypt(password.getKeyValue().substring(4))));
            } catch (CryptoException e) {
                log.error(e);
            }
        } else {
            this.password = password;
        }
    }

    public Value getKeytabFileName() {

        return keytabFile;
    }

    public void setKeytabFileName(Value keytabFile) {

        this.keytabFile = keytabFile;
    }

    public Value getSpnKey() {

        return spnKey;
    }

    public void setSpnKey(Value spnKey) {

        this.spnKey = spnKey;
    }

    @Override
    public boolean isContentAware() {

        return false;
    }

}
