/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mediator.kerberos;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.config.xml.ValueFactory;
import org.apache.synapse.config.xml.ValueSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.Value;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.MediatorException;

import java.util.Iterator;
import javax.xml.namespace.QName;

public class KerberosMediator extends AbstractMediator {

    private String loginContextName;
    private String loginConfig;
    private String krb5Config;
    private String spn;
    private Value clientPrincipal;
    private Value password;
    private Value keytabConfig;
    private Value spnConfigKey;
    private OMAttribute attSpnConfigKey;
    private OMElement spnConfigElem;

    private static final QName PROP_NAME_LOGIN_CONTEXT_NAME = new QName(KerberosConstants.LOGIN_CONTEXT_NAME_STRING);
    private static final QName PROP_NAME_LOGIN_CONFIG = new QName(KerberosConstants.LOGIN_CONFIG_STRING);
    private static final QName PROP_NAME_KRB5_CONFIG = new QName(KerberosConstants.KRB5_CONFIG_STRING);
    private static final QName PROP_NAME_SPN = new QName(KerberosConstants.SPN_STRING);
    private static final QName PROP_NAME_CLIENT = new QName(KerberosConstants.CLIENT_PRINCIPAL_STRING);
    private static final QName PROP_NAME_PASSWORD = new QName(KerberosConstants.PASSWORD_STRING);
    private static final QName PROP_NAME_KEYTAB_PATH = new QName(KerberosConstants.KEYTAB_PATH_STRING);

    /**
     * {@inheritDoc}
     */
    public OMElement serialize(OMElement parent) {

        OMElement kerberosService = fac.createOMElement(KerberosConstants.KERBEROS_SERVICE_STRING, synNS);

        if (loginContextName != null && StringUtils.isNotEmpty(loginContextName)) {
            kerberosService.addAttribute(fac.createOMAttribute(KerberosConstants.LOGIN_CONTEXT_NAME_STRING, nullNS,
                    loginContextName));
        }
        if (spnConfigKey != null) {
            OMElement spnConfigEle = fac.createOMElement(KerberosConstants.SPN_STRING, synNS);
            spnConfigEle.addAttribute(fac.createOMAttribute(XMLConfigConstants.KEY, nullNS, spnConfigKey.getKeyValue()));
            kerberosService.addChild(spnConfigEle);
        } else if (spn != null) {
            kerberosService.addAttribute(fac.createOMAttribute(KerberosConstants.SPN_STRING, nullNS, spn));
        }
        if (krb5Config != null && StringUtils.isNotEmpty(krb5Config)) {
            kerberosService.addAttribute(fac.createOMAttribute(KerberosConstants.KRB5_CONFIG_STRING, nullNS, krb5Config));
        }
        if (loginConfig != null && StringUtils.isNotEmpty(loginConfig)) {
            kerberosService.addAttribute(fac.createOMAttribute(KerberosConstants.LOGIN_CONFIG_STRING, nullNS,
                    loginConfig));
        }
        if (clientPrincipal != null && !clientPrincipal.getKeyValue().isEmpty()) {
            new ValueSerializer().serializeValue(clientPrincipal, KerberosConstants.CLIENT_PRINCIPAL_STRING,
                    kerberosService);
        }
        if (password != null && !password.getKeyValue().isEmpty()) {
            new ValueSerializer().serializeValue(password, KerberosConstants.PASSWORD_STRING, kerberosService);
        }

        saveTracingState(kerberosService, this);

        if (parent != null) {
            parent.addChild(kerberosService);
        }
        return kerberosService;
    }

    /**
     * {@inheritDoc}
     */
    public void build(OMElement elem) {

        OMAttribute attLoginContextName = elem.getAttribute(PROP_NAME_LOGIN_CONTEXT_NAME);
        OMAttribute attLoginConfig = elem.getAttribute(PROP_NAME_LOGIN_CONFIG);
        OMAttribute attKrb5Config = elem.getAttribute(PROP_NAME_KRB5_CONFIG);
        OMAttribute attSPN = elem.getAttribute(PROP_NAME_SPN);
        Iterator spnConfigKeyAttr = elem.getChildrenWithName(PROP_NAME_SPN);
        OMAttribute attClientPrincipal = elem.getAttribute(PROP_NAME_CLIENT);
        OMAttribute attPassword = elem.getAttribute(PROP_NAME_PASSWORD);
        ValueFactory valueFactory = new ValueFactory();

        if (spnConfigKeyAttr != null && spnConfigKeyAttr.hasNext()) {
            spnConfigElem = (OMElement) spnConfigKeyAttr.next();
            if (spnConfigElem != null) {
                attSpnConfigKey = spnConfigElem.getAttribute(ATT_KEY);
            }
        }

        if((attLoginContextName != null && StringUtils.isNotEmpty(attLoginContextName.getAttributeValue()))
                || (attLoginConfig != null && StringUtils.isNotEmpty(attLoginConfig.getAttributeValue()))
                || (attKrb5Config != null && StringUtils.isNotEmpty(attKrb5Config.getAttributeValue()))) {
            if (attLoginContextName != null && StringUtils.isNotEmpty(attLoginContextName.getAttributeValue())) {
                this.loginContextName = attLoginContextName.getAttributeValue();
            } else {
                throw new MediatorException("The 'loginContextName' attribute is required for the Kerberos mediator ");
            }
            if (attLoginConfig != null && StringUtils.isNotEmpty(attLoginConfig.getAttributeValue())) {
                this.loginConfig = attLoginConfig.getAttributeValue();
            }

            if (attKrb5Config != null && StringUtils.isNotEmpty(attKrb5Config.getAttributeValue())) {
                this.krb5Config = attKrb5Config.getAttributeValue();
            }
        } else if ((attClientPrincipal != null && StringUtils.isNotEmpty(attClientPrincipal.getAttributeValue()))
                || (attPassword != null && StringUtils.isNotEmpty(attPassword.getAttributeValue()))) {

            if (attClientPrincipal != null && StringUtils.isNotEmpty(attClientPrincipal.getAttributeValue())) {
                try {
                    this.clientPrincipal = new Value(attClientPrincipal.getAttributeValue());
                } catch (Exception e) {
                    throw new MediatorException("Unable to load the corelate XPATH expression" + e.getMessage());
                }
            } else {
                throw new MediatorException("The 'clientPrincipal' is required for the Kerberos mediator ");
            }

           if (attPassword != null && StringUtils.isNotEmpty(attPassword.getAttributeValue())) {
                try {
                    this.password = new Value(attPassword.getAttributeValue());
                } catch (Exception e) {
                    throw new MediatorException("Unable to load the corelate XPATH expression" + e.getMessage());
                }
            } else {
                throw new MediatorException("The 'password' is required for the Kerberos mediator");
            }

        }

        if (attSpnConfigKey != null && attSpnConfigKey.getAttributeValue() != null) {
            this.spnConfigKey = valueFactory.createValue(XMLConfigConstants.KEY, spnConfigElem);
        } else if (attSPN != null) {
            this.spn = attSPN.getAttributeValue();
        } else {
            throw new MediatorException("The 'spn' attribute is required for the Kerberos mediator ");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getTagLocalName() {

        return "kerberosService";
    }

    public String getLoginConfig() {

        return loginConfig;
    }

    public void setLoginConfig(String loginConfig) {

        this.loginConfig = loginConfig;
    }

    public String getLoginContextName() {

        return loginContextName;
    }

    public void setLoginContextName(String loginContextName) {

        this.loginContextName = loginContextName;
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

    public Value getKeytabConfig() {

        return keytabConfig;
    }

    public void setKeytabConfig(Value keytabConfig) {

        this.keytabConfig = keytabConfig;
    }

    public Value getClientPrincipal() {

        return clientPrincipal;
    }

    public void setClientPrincipal(Value clientPrincipal) {

        this.clientPrincipal = clientPrincipal;
    }

    public Value getPassword() {

        return password;
    }

    public void setPassword(Value password) {

        this.password = password;
    }

    public Value getSpnConfigKey() {

        return spnConfigKey;
    }

    public void setSpnConfigKey(Value spnConfigKey) {

        this.spnConfigKey = spnConfigKey;
    }
}
