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
package org.wso2.carbon.mediator.kerberos.config.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.ValueFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.Value;
import org.wso2.carbon.mediator.kerberos.KerberosConstants;
import org.wso2.carbon.mediator.kerberos.KerberosMediator;
import org.wso2.carbon.mediator.service.MediatorException;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

public class KerberosMediatorFactory extends AbstractMediatorFactory {

    private static final QName ELEMENT_KERBEROS = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
            KerberosConstants.KERBEROS_SERVICE_STRING);
    private static final QName ATTR_NAME_SPN = new QName(KerberosConstants.SPN_STRING);
    private static final QName ATTR_NAME_CLIENT_PRINCIPAL = new QName(KerberosConstants.CLIENT_PRINCIPAL_STRING);
    private static final QName ATTR_NAME_PASSWORD = new QName(KerberosConstants.PASSWORD_STRING);
    private static final QName ATTR_NAME_KEYTAB_PATH = new QName(KerberosConstants.KEYTAB_PATH_STRING);
    private static final QName ATTR_NAME_KRB5_CONFIG = new QName(KerberosConstants.KRB5_CONFIG_STRING);
    private static final QName ATTR_NAME_LOGIN_CONTEXT_NAME = new QName(KerberosConstants.LOGIN_CONTEXT_NAME_STRING);
    private static final QName ATTR_NAME_LOGIN_CONFIG = new QName(KerberosConstants.LOGIN_CONFIG_STRING);

    /**
     * {@inheritDoc}
     */
    public Mediator createSpecificMediator(OMElement element, Properties properties) {

        if (!ELEMENT_KERBEROS.equals(element.getQName())) {
            handleException("Unable to create the Kerberos mediator. "
                    + "Unexpected element as the Kerberos mediator configuration");
        }

        KerberosMediator mediator;
        OMAttribute loginContextName;
        boolean isPasswordProvided = false;
        mediator = new KerberosMediator();
        Iterator krb5ConfigKey = element.getChildrenWithName(ATTR_NAME_KRB5_CONFIG);
        if (krb5ConfigKey != null && krb5ConfigKey.hasNext()) {
            OMElement krb5ConfigElem = (OMElement) krb5ConfigKey.next();
            if (krb5ConfigElem != null) {
                OMAttribute n = krb5ConfigElem.getAttribute(ATT_KEY);
                if (n != null) {
                    ValueFactory keyFac = new ValueFactory();
                    Value generatedKey = keyFac.createValue(XMLConfigConstants.KEY, krb5ConfigElem);
                    mediator.setKrb5ConfigKey(generatedKey);
                }
            }
        } else {
            OMAttribute krb5Config = element.getAttribute(ATTR_NAME_KRB5_CONFIG);
            if (krb5Config != null && StringUtils.isNotEmpty(krb5Config.getAttributeValue())) {
                mediator.setKrb5Config(krb5Config.getAttributeValue());
            } else {
                throw new MediatorException("The 'krb5Config' attribute is required for the Kerberos mediator");
            }
        }

        Iterator spnConfigKey = element.getChildrenWithName(ATTR_NAME_SPN);
        if (spnConfigKey != null && spnConfigKey.hasNext()) {
            OMElement spnConfigElem = (OMElement) spnConfigKey.next();
            if (spnConfigElem != null) {
                OMAttribute n = spnConfigElem.getAttribute(ATT_KEY);
                if (n != null) {
                    ValueFactory keyFac = new ValueFactory();
                    Value generatedKey = keyFac.createValue(XMLConfigConstants.KEY, spnConfigElem);
                    mediator.setSpnKey(generatedKey);
                }
            }
        } else {
            OMAttribute spn = element.getAttribute(ATTR_NAME_SPN);
            if (spn != null && StringUtils.isNotEmpty(spn.getAttributeValue())) {
                mediator.setSpn(spn.getAttributeValue());
            } else {
                throw new MediatorException("The 'spn' attribute is required for the Kerberos mediator");
            }
        }

        Iterator loginConfigKey = element.getChildrenWithName(ATTR_NAME_LOGIN_CONFIG);
        if (loginConfigKey != null && loginConfigKey.hasNext()) {
            OMElement loginConfigElem = (OMElement) loginConfigKey.next();
            if (loginConfigElem != null) {
                OMAttribute n = loginConfigElem.getAttribute(ATT_KEY);
                if (n != null) {
                    ValueFactory keyFac = new ValueFactory();
                    Value generatedKey = keyFac.createValue(XMLConfigConstants.KEY, loginConfigElem);
                    mediator.setLoginConfigKey(generatedKey);
                }
                loginContextName = element.getAttribute(ATTR_NAME_LOGIN_CONTEXT_NAME);
                if (loginContextName != null && StringUtils.isNotEmpty(loginContextName.getAttributeValue())) {
                    mediator.setLoginContextName(loginContextName.getAttributeValue());
                } else {
                    throw new MediatorException(
                            "The 'loginContextName' attribute is required for the Kerberos mediator");
                }
            }
        } else {
            OMAttribute loginConfig = element.getAttribute(ATTR_NAME_LOGIN_CONFIG);
            if (loginConfig != null && StringUtils.isNotEmpty(loginConfig.getAttributeValue())) {
                mediator.setLoginConfig(loginConfig.getAttributeValue());
                loginContextName = element.getAttribute(ATTR_NAME_LOGIN_CONTEXT_NAME);
                if (loginContextName != null && StringUtils.isNotEmpty(loginContextName.getAttributeValue())) {
                    mediator.setLoginContextName(loginContextName.getAttributeValue());
                } else {
                    throw new MediatorException(
                            "The 'loginContextName' attribute is required for the Kerberos mediator");
                }
            }
        }

        OMAttribute client = element.getAttribute(ATTR_NAME_CLIENT_PRINCIPAL);
        if (client != null && StringUtils.isNotEmpty(client.getAttributeValue())) {
            mediator.setClientPrincipal(new ValueFactory().createValue(KerberosConstants.CLIENT_PRINCIPAL_STRING,
                    element));
        }

        OMAttribute clientPassword = element.getAttribute(ATTR_NAME_PASSWORD);
        if (clientPassword != null && StringUtils.isNotEmpty(clientPassword.getAttributeValue())) {
            mediator.setPassword(new ValueFactory().createValue(KerberosConstants.PASSWORD_STRING, element));
            isPasswordProvided = true;
        }

        Iterator keytabConfig = element.getChildrenWithName(ATTR_NAME_KEYTAB_PATH);
        if (keytabConfig != null && keytabConfig.hasNext()) {
            OMElement keytabConfigElem = (OMElement) keytabConfig.next();
            if (keytabConfigElem != null) {
                OMAttribute n = keytabConfigElem.getAttribute(ATT_KEY);
                if (n != null) {
                    mediator.setRegistryKeyTabValue(new ValueFactory().createValue(XMLConfigConstants.KEY, keytabConfigElem));
                }
                isPasswordProvided = true;
            }
        } else {
            OMAttribute keytab = element.getAttribute(ATTR_NAME_KEYTAB_PATH);
            if (keytab != null && StringUtils.isNotEmpty(keytab.getAttributeValue())) {
                mediator.setKeytabPath(new ValueFactory().createValue(KerberosConstants.KEYTAB_PATH_STRING, element));
                isPasswordProvided = true;
            }
        }

        if (!isPasswordProvided) {
            throw new MediatorException("The 'password' or 'keytabPath' attribute is required for" +
                    " the Kerberos mediator.");
        }
        return mediator;
    }

    /**
     * {@inheritDoc}
     */
    public QName getTagQName() {

        return ELEMENT_KERBEROS;
    }

}
