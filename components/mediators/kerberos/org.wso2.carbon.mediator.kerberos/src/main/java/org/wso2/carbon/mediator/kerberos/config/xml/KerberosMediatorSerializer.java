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

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.config.xml.ValueSerializer;
import org.wso2.carbon.mediator.kerberos.KerberosConstants;
import org.wso2.carbon.mediator.kerberos.KerberosMediator;

public class KerberosMediatorSerializer extends AbstractMediatorSerializer {

    /**
     * {@inheritDoc}
     */
    public String getMediatorClassName() {

        return KerberosMediator.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    public OMElement serializeSpecificMediator(Mediator mediator) {

        if (!(mediator instanceof KerberosMediator)) {
            handleException("Unsupported mediator passed in for serialization : "
                    + mediator.getType());
        }

        KerberosMediator kerberos = (KerberosMediator) mediator;
        OMElement kerberosElem = fac.createOMElement(KerberosConstants.KERBEROS_SERVICE_STRING, synNS);
        saveTracingState(kerberosElem, kerberos);

        if (kerberos.getSpnKey() != null) {
            OMElement spnConfEle = fac.createOMElement(KerberosConstants.SPN_STRING, synNS);
            spnConfEle.addAttribute(fac.createOMAttribute(KerberosConstants.KEY, nullNS, kerberos.getSpnKey().getKeyValue()));
            kerberosElem.addChild(spnConfEle);
        } else if (StringUtils.isNotEmpty(kerberos.getSpn())) {
            kerberosElem.addAttribute(fac.createOMAttribute(KerberosConstants.SPN_STRING, nullNS, kerberos.getSpn()));
        }

        if (kerberos.getKrb5ConfigKey() != null) {
            OMElement krb5ConfigEle = fac.createOMElement(KerberosConstants.KRB5_CONFIG_STRING, synNS);
            krb5ConfigEle.addAttribute(fac.createOMAttribute(KerberosConstants.KEY, nullNS, kerberos.getKrb5ConfigKey().getKeyValue()));
            kerberosElem.addChild(krb5ConfigEle);
        } else if (StringUtils.isNotEmpty(kerberos.getKrb5Config())) {
            kerberosElem.addAttribute(fac.createOMAttribute(KerberosConstants.KRB5_CONFIG_STRING, nullNS,
                    kerberos.getKrb5Config()));
        }
        if (StringUtils.isNotEmpty(kerberos.getLoginContextName())) {
            kerberosElem.addAttribute(fac.createOMAttribute(KerberosConstants.LOGIN_CONTEXT_NAME_STRING, nullNS,
                    kerberos.getLoginContextName()));
        }
        if (kerberos.getLoginConfigKey() != null) {
            OMElement loginConfigEle = fac.createOMElement(KerberosConstants.LOGIN_CONFIG_STRING, synNS);
            loginConfigEle.addAttribute(fac.createOMAttribute(KerberosConstants.KEY, nullNS, kerberos.getLoginConfigKey().getKeyValue()));
            kerberosElem.addChild(loginConfigEle);
        } else if (StringUtils.isNotEmpty(kerberos.getLoginConfig())) {
            kerberosElem.addAttribute(fac.createOMAttribute(KerberosConstants.LOGIN_CONFIG_STRING, nullNS,
                    kerberos.getLoginConfig()));
        }
        if (kerberos.getClientPrincipal() != null) {
            new ValueSerializer().serializeValue(kerberos.getClientPrincipal(),
                    KerberosConstants.CLIENT_PRINCIPAL_STRING, kerberosElem);
        }
        if (kerberos.getPassword() != null) {
            new ValueSerializer().serializeValue(kerberos.getPassword(), KerberosConstants.PASSWORD_STRING,
                    kerberosElem);
        }
        if (kerberos.getRegistryKeyTabValue() != null) {
            OMElement keyTabKeyEle = fac.createOMElement(KerberosConstants.KEYTAB_PATH_STRING, synNS);
            keyTabKeyEle.addAttribute(fac.createOMAttribute(KerberosConstants.KEY, nullNS, kerberos.getRegistryKeyTabValue().getKeyValue()));
            kerberosElem.addChild(keyTabKeyEle);
        } else if (kerberos.getKeytabPath() != null) {
            new ValueSerializer().serializeValue(kerberos.getKeytabPath(), KerberosConstants.KEYTAB_PATH_STRING,
                    kerberosElem);
        }

        return kerberosElem;
    }
}
