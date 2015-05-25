/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.persistence;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.transport.passthru.core.ssl.SSLConfiguration;

import javax.xml.namespace.QName;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceUtils {

    // Attributes
    private static final String NAME_ATT = "name";
    private static final String PORT_ATT = "port";
    private static final String DOMAIN_ATT = "domain";
    private static final String PROTOCOL_ATT = "protocol";
    private static final String INJECT_SEQ_ATT = "injectingSeq";
    private static final String ONERROR_SEQ_ATT = "onErrorSeq";
    private static final String CLASS_IMPL_ATT = "classImpl";
    private static final String PARAM_NAME_ATT = "paramName";
    private static final String PARAM_VALUE_ATT = "paramValue";

    private static final String KEYSTORE_ATT = "keystore";
    private static final String TRUSTSTORE_ATT = "truststore";
    private static final String CLIENTAUTH_ATT ="SSLVerifyClient";
    private static final String SSLPROTOCOL_ATT = "SSLProtocol";
    private static final String HTTPSPROTOCOLS_ATT = "HttpsProtocols";
    private static final String REVOCATIONVERIFIER_ATT = "CertificateRevocationVerifier";

    // QNames
    private static final QName INBOUND_ENDPOINTS_QN = new QName("inboundEndpoints");
    private static final QName INBOUND_ENDPOINT_LISTENER_QN = new QName("inboundEndpointListener");
    private static final QName ENDPOINT_QN = new QName("endpoint");
    private static final QName PARAMS_QN = new QName("inboundParameters");
    private static final QName PARAM_QN = new QName("inboundParameter");

    private static final QName NAME_QN = new QName(NAME_ATT);
    private static final QName PORT_QN = new QName(PORT_ATT);
    private static final QName DOMAIN_QN = new QName(DOMAIN_ATT);
    private static final QName PROTOCOL_QN = new QName(PROTOCOL_ATT);
    private static final QName INJECT_SEQ_QN = new QName(INJECT_SEQ_ATT);
    private static final QName ONERROR_SEQ_QN = new QName(ONERROR_SEQ_ATT);
    private static final QName CLASS_IMPL_QN = new QName(CLASS_IMPL_ATT);
    private static final QName PARAM_NAME_QN = new QName(PARAM_NAME_ATT);
    private static final QName PARAM_VALUE_QN = new QName(PARAM_VALUE_ATT);
    private static final QName KEYSTORE_QN = new QName(KEYSTORE_ATT);
    private static final QName TRUSTORE_QN = new QName(TRUSTSTORE_ATT);
    private static final QName CLIENTAUTH_QN = new QName(CLIENTAUTH_ATT);
    private static final QName SSLPROTOCOL_QN = new QName(SSLPROTOCOL_ATT);
    private static final QName HTTPSPROTOCOL_QN = new QName(HTTPSPROTOCOLS_ATT);
    private static final QName REVOCATIONVERIFIER_QN = new QName(REVOCATIONVERIFIER_ATT);

    private static OMFactory fac = OMAbstractFactory.getOMFactory();
    private static final OMNamespace nullNS =
            fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "");

    /**
     * Convert EndpointInfo to a OMElement
     *
     * @param endpointInfo tenant data map
     * @return equivalent OMElement for EndpointInfo
     */
    public static OMElement convertEndpointInfoToOM(
               Map<Integer, List<InboundEndpointInfoDTO>> endpointInfo) {

        OMElement parentElement = fac.createOMElement(INBOUND_ENDPOINTS_QN);

        for (Map.Entry<Integer, List<InboundEndpointInfoDTO>> mapEntry : endpointInfo.entrySet()) {
            int port = mapEntry.getKey();

            OMElement listenerElem = fac.createOMElement(INBOUND_ENDPOINT_LISTENER_QN, parentElement);
            listenerElem.addAttribute(PORT_ATT, String.valueOf(port), nullNS);

            List<InboundEndpointInfoDTO> tenantDomains = mapEntry.getValue();
            for (InboundEndpointInfoDTO inboundEndpointInfoDTO : tenantDomains) {
                OMElement endpointElem = fac.createOMElement(ENDPOINT_QN, listenerElem);

                endpointElem.addAttribute(NAME_ATT, inboundEndpointInfoDTO.getEndpointName(), nullNS);
                endpointElem.addAttribute(DOMAIN_ATT, inboundEndpointInfoDTO.getTenantDomain(), nullNS);
                endpointElem.addAttribute(PROTOCOL_ATT, inboundEndpointInfoDTO.getProtocol(), nullNS);

                OMElement paramsElem = fac.createOMElement(PARAMS_QN, endpointElem);
                if (inboundEndpointInfoDTO.getInboundParams() != null) {

                    if (inboundEndpointInfoDTO.getInboundParams().getInjectingSeq() != null) {
                        endpointElem.addAttribute(INJECT_SEQ_ATT,
                            inboundEndpointInfoDTO.getInboundParams().getInjectingSeq(), nullNS);
                    }

                    if (inboundEndpointInfoDTO.getInboundParams().getOnErrorSeq()    != null) {
                        endpointElem.addAttribute(ONERROR_SEQ_ATT,
                            inboundEndpointInfoDTO.getInboundParams().getOnErrorSeq(), nullNS);
                    }

                    if (inboundEndpointInfoDTO.getInboundParams().getClassImpl() != null) {
                        endpointElem.addAttribute(CLASS_IMPL_ATT,
                                inboundEndpointInfoDTO.getInboundParams().getClassImpl(), nullNS);
                    }

                    for (Map.Entry<Object, Object> e :
                            inboundEndpointInfoDTO.getInboundParams().getProperties().entrySet()) {
                        OMElement paramElem = fac.createOMElement(PARAM_QN, paramsElem);

                        paramElem.addAttribute(PARAM_NAME_ATT, (String) e.getKey(), nullNS);
                        paramElem.addAttribute(PARAM_VALUE_ATT, (String) e.getValue(), nullNS);
                    }
                }


                if (inboundEndpointInfoDTO.getSslConfiguration() != null) {
                    if (inboundEndpointInfoDTO.getSslConfiguration().getKeyStore() != null) {
                        endpointElem.addAttribute(KEYSTORE_ATT, inboundEndpointInfoDTO.getSslConfiguration().getKeyStore(), nullNS);
                    }
                    if (inboundEndpointInfoDTO.getSslConfiguration().getTrustStore() != null) {
                        endpointElem.addAttribute(TRUSTSTORE_ATT, inboundEndpointInfoDTO.getSslConfiguration().getTrustStore(), nullNS);
                    }
                    if (inboundEndpointInfoDTO.getSslConfiguration().getClientAuthEl() != null) {
                        endpointElem.addAttribute(CLIENTAUTH_ATT, inboundEndpointInfoDTO.getSslConfiguration().getClientAuthEl(), nullNS);
                    }
                    if (inboundEndpointInfoDTO.getSslConfiguration().getSslProtocol() != null) {
                        endpointElem.addAttribute(SSLPROTOCOL_ATT, inboundEndpointInfoDTO.getSslConfiguration().getSslProtocol(), nullNS);
                    }
                    if (inboundEndpointInfoDTO.getSslConfiguration().getHttpsProtocolsEl() != null) {
                        endpointElem.addAttribute(HTTPSPROTOCOLS_ATT, inboundEndpointInfoDTO.getSslConfiguration().getHttpsProtocolsEl(), nullNS);
                    }
                    if (inboundEndpointInfoDTO.getSslConfiguration().getRevocationVerifier() != null) {
                        endpointElem.addAttribute(REVOCATIONVERIFIER_ATT, inboundEndpointInfoDTO.getSslConfiguration().getRevocationVerifier(), nullNS);
                    }
                }
            }
        }
        return parentElement;
    }

    /**
     * Create EndpointInfo from OMElement
     *
     * @param endpointInfoOM OMElement containing endpoint information
     * @return equivalent EndpointInfo for OMElement
     */
    public static Map<Integer,List<InboundEndpointInfoDTO>> convertOMToEndpointInfo(
            OMElement endpointInfoOM) {

        Map<Integer, List<InboundEndpointInfoDTO>> endpointInfo =
                new ConcurrentHashMap<Integer, List<InboundEndpointInfoDTO>>();

        Iterator listenerElementsItr =
                endpointInfoOM.getChildrenWithName(INBOUND_ENDPOINT_LISTENER_QN);
        while (listenerElementsItr.hasNext()) {

            List<InboundEndpointInfoDTO> tenantList = new ArrayList<InboundEndpointInfoDTO>();
            OMElement listenerElement = (OMElement) listenerElementsItr.next();
            int port = Integer.parseInt(listenerElement.getAttributeValue(PORT_QN));

            Iterator endpointsItr = listenerElement.getChildrenWithName(ENDPOINT_QN);
            while (endpointsItr.hasNext()) {
                OMElement endpointElement = (OMElement) endpointsItr.next();

                InboundProcessorParams params = deserializeInboundParameters(endpointElement);
                InboundEndpointInfoDTO inboundEndpointInfoDTO =
                        new InboundEndpointInfoDTO(endpointElement.getAttributeValue(DOMAIN_QN),
                                          endpointElement.getAttributeValue(PROTOCOL_QN),
                                          endpointElement.getAttributeValue(NAME_QN), params);
                if (endpointElement.getAttributeValue(PROTOCOL_QN).equals("https")) {
                    SSLConfiguration sslConfiguration =
                               new SSLConfiguration(endpointElement.getAttributeValue(KEYSTORE_QN),
                                                    endpointElement.getAttributeValue(TRUSTORE_QN),
                                                    endpointElement.getAttributeValue(CLIENTAUTH_QN),
                                                    endpointElement.getAttributeValue(HTTPSPROTOCOL_QN),
                                                    endpointElement.getAttributeValue(REVOCATIONVERIFIER_QN),
                                                    endpointElement.getAttributeValue(SSLPROTOCOL_QN));

                    inboundEndpointInfoDTO.setSslConfiguration(sslConfiguration);
                }

                tenantList.add(inboundEndpointInfoDTO);
            }
            endpointInfo.put(port, tenantList);
        }
        return endpointInfo;
    }

    private static InboundProcessorParams deserializeInboundParameters(OMElement endpointElement) {
        InboundProcessorParams inboundParams = new InboundProcessorParams();

        inboundParams.setName(endpointElement.getAttributeValue(NAME_QN));
        inboundParams.setProtocol(endpointElement.getAttributeValue(PROTOCOL_QN));
        inboundParams.setInjectingSeq(endpointElement.getAttributeValue(INJECT_SEQ_QN));
        inboundParams.setOnErrorSeq(endpointElement.getAttributeValue(ONERROR_SEQ_QN));
        inboundParams.setClassImpl(endpointElement.getAttributeValue(CLASS_IMPL_QN));

        Properties props = new Properties();
        OMElement paramsEle = endpointElement.getFirstChildWithName(PARAMS_QN);
        if (paramsEle != null) {
            Iterator parameters = paramsEle.getChildrenWithName(PARAM_QN);
            while (parameters.hasNext()) {
                OMElement parameter = (OMElement) parameters.next();
                props.setProperty(parameter.getAttributeValue(PARAM_NAME_QN),
                                  parameter.getAttributeValue(PARAM_VALUE_QN));
            }
        }
        inboundParams.setProperties(props);
        return inboundParams;
    }

}
