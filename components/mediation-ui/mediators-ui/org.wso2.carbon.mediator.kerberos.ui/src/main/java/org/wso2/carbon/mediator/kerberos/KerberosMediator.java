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
    private Value keytabPath;
    private Value krb5ConfigKey;
    private Value spnConfigKey;
    private Value loginConfigKey;
    private Value keyTabKey;
    private OMAttribute attKrb5ConfigKey;
    private OMAttribute attSpnConfigKey;
    private OMElement krb5ConfigElem;
    private OMElement spnConfigElem;
    private OMAttribute registryKeyTabPath;
    private OMElement keyTabPathConfigElem;

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

        if (loginContextName != null) {
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
        if (clientPrincipal != null) {
            new ValueSerializer().serializeValue(clientPrincipal, KerberosConstants.CLIENT_PRINCIPAL_STRING,
                    kerberosService);
        }
        if (password != null) {
            new ValueSerializer().serializeValue(password, KerberosConstants.PASSWORD_STRING, kerberosService);
        }
        if (keyTabKey != null) {
            OMElement keyTabKeyEle = fac.createOMElement(KerberosConstants.KEYTAB_PATH_STRING, synNS);
            keyTabKeyEle.addAttribute(fac.createOMAttribute(XMLConfigConstants.KEY, nullNS, keyTabKey.getKeyValue()));
            kerberosService.addChild(keyTabKeyEle);
        } else if (keytabPath != null) {
            new ValueSerializer().serializeValue(keytabPath, KerberosConstants.KEYTAB_PATH_STRING, kerberosService);
        }
        if (krb5ConfigKey != null) {
            OMElement krb5ConfigEle = fac.createOMElement(KerberosConstants.KRB5_CONFIG_STRING, synNS);
            krb5ConfigEle.addAttribute(fac.createOMAttribute(XMLConfigConstants.KEY, nullNS, krb5ConfigKey.getKeyValue()));
            kerberosService.addChild(krb5ConfigEle);
        } else if (krb5Config != null) {
            kerberosService.addAttribute(fac.createOMAttribute(KerberosConstants.KRB5_CONFIG_STRING, nullNS, krb5Config));
        }
        if (loginConfigKey != null) {
            OMElement loginConfigEle = fac.createOMElement(KerberosConstants.LOGIN_CONFIG_STRING, synNS);
            loginConfigEle.addAttribute(fac.createOMAttribute(XMLConfigConstants.KEY, nullNS, loginConfigKey.getKeyValue()));
            kerberosService.addChild(loginConfigEle);
        } else if (loginConfig != null) {
            kerberosService.addAttribute(fac.createOMAttribute(KerberosConstants.LOGIN_CONFIG_STRING, nullNS,
                    loginConfig));
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
        OMAttribute attClientPrincipal = elem.getAttribute(PROP_NAME_CLIENT);
        OMAttribute attPassword = elem.getAttribute(PROP_NAME_PASSWORD);
        OMAttribute attKeytabPath = elem.getAttribute(PROP_NAME_KEYTAB_PATH);
        Iterator krb5ConfigKeyAttr = elem.getChildrenWithName(PROP_NAME_KRB5_CONFIG);
        Iterator spnConfigKeyAttr = elem.getChildrenWithName(PROP_NAME_SPN);
        Iterator loginConfigKeyAttr = elem.getChildrenWithName(PROP_NAME_LOGIN_CONFIG);
        Iterator keyTabPathKeyAttr = elem.getChildrenWithName(PROP_NAME_KEYTAB_PATH);
        ValueFactory valueFactory = new ValueFactory();

        if (krb5ConfigKeyAttr != null && krb5ConfigKeyAttr.hasNext()) {
            krb5ConfigElem = (OMElement) krb5ConfigKeyAttr.next();
            if (krb5ConfigElem != null) {
                attKrb5ConfigKey = krb5ConfigElem.getAttribute(ATT_KEY);
            }
        }
        if (spnConfigKeyAttr != null && spnConfigKeyAttr.hasNext()) {
            spnConfigElem = (OMElement) spnConfigKeyAttr.next();
            if (spnConfigElem != null) {
                attSpnConfigKey = spnConfigElem.getAttribute(ATT_KEY);
            }
        }
        if (loginConfigKeyAttr != null && loginConfigKeyAttr.hasNext()) {
            OMElement loginConfigElem = (OMElement) loginConfigKeyAttr.next();
            if (loginConfigElem != null) {
                OMAttribute attloginConfigKey = loginConfigElem.getAttribute(ATT_KEY);
                if (attloginConfigKey != null && attloginConfigKey.getAttributeValue() != null) {
                    this.loginConfigKey = valueFactory.createValue(XMLConfigConstants.KEY, loginConfigElem);
                }
            }
        }
        if (keyTabPathKeyAttr != null && keyTabPathKeyAttr.hasNext()) {
            keyTabPathConfigElem = (OMElement) keyTabPathKeyAttr.next();
            if (keyTabPathConfigElem != null) {
                registryKeyTabPath = keyTabPathConfigElem.getAttribute(ATT_KEY);
            }
        }
        if (attLoginContextName != null) {
            this.loginContextName = attLoginContextName.getAttributeValue();
        }
        if (attLoginConfig != null) {
            this.loginConfig = attLoginConfig.getAttributeValue();
        }
        if (attKrb5ConfigKey != null && attKrb5ConfigKey.getAttributeValue() != null) {
            this.krb5ConfigKey = valueFactory.createValue(XMLConfigConstants.KEY, krb5ConfigElem);
        } else if (attKrb5Config != null) {
            this.krb5Config = attKrb5Config.getAttributeValue();
        } else {
            throw new MediatorException("The 'krb5Config' attribute is required for the Kerberos mediator ");
        }
        if (attSpnConfigKey != null && attSpnConfigKey.getAttributeValue() != null) {
            this.spnConfigKey = valueFactory.createValue(XMLConfigConstants.KEY, spnConfigElem);
        } else if (attSPN != null) {
            this.spn = attSPN.getAttributeValue();
        } else {
            throw new MediatorException("The 'spn' attribute is required for the Kerberos mediator ");
        }
        if (attClientPrincipal != null) {
            try {
                Value client = valueFactory.createValue(KerberosConstants.CLIENT_PRINCIPAL_STRING, elem);
                this.clientPrincipal = client;
            } catch (Exception e) {
                throw new MediatorException("Unable to load the corelate XPATH expression" + e.getMessage());
            }
        } else {
            throw new MediatorException("The 'clientPrincipal' is required for the Kerberos mediator ");
        }
        if (attPassword == null && attKeytabPath == null && registryKeyTabPath == null) {
            throw new MediatorException("The 'keytabPath' or 'password' is required for the Kerberos mediator");
        } else {
            if (registryKeyTabPath != null && registryKeyTabPath.getAttributeValue() != null) {
                this.keyTabKey = valueFactory.createValue(XMLConfigConstants.KEY, keyTabPathConfigElem);
            } else if (attPassword != null && attPassword.getAttributeValue() != null) {
                try {
                    Value pass = valueFactory.createValue(KerberosConstants.PASSWORD_STRING, elem);
                    this.password = pass;
                } catch (Exception e) {
                    throw new MediatorException("Unable to load the corelate XPATH expression" + e.getMessage());
                }
            } else if (attKeytabPath != null && attKeytabPath.getAttributeValue() != null) {
                try {
                    Value keytab = valueFactory.createValue(KerberosConstants.KEYTAB_PATH_STRING, elem);
                    this.keytabPath = keytab;
                } catch (Exception e) {
                    throw new MediatorException("Unable to load the corelate XPATH expression" + e.getMessage());
                }
            }
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

    public Value getKeytabPath() {

        return keytabPath;
    }

    public void setKeytabPath(Value keytabPath) {

        this.keytabPath = keytabPath;
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

    public Value getKrb5ConfigKey() {

        return krb5ConfigKey;
    }

    public void setKrb5ConfigKey(Value krb5ConfigKey) {

        this.krb5ConfigKey = krb5ConfigKey;
    }

    public Value getSpnConfigKey() {

        return spnConfigKey;
    }

    public void setSpnConfigKey(Value spnConfigKey) {

        this.spnConfigKey = spnConfigKey;
    }

    public Value getLoginConfigKey() {

        return loginConfigKey;
    }

    public void setLoginConfigKey(Value loginConfigKey) {

        this.loginConfigKey = loginConfigKey;
    }

    public Value getRegistryKeyTabValue() {

        return keyTabKey;
    }

    public void setRegistryKeyTabValue(Value keyTabKey) {

        this.keyTabKey = keyTabKey;
    }
}
