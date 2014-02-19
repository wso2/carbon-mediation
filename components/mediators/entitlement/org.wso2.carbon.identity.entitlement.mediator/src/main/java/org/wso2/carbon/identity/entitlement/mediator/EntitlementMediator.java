/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.entitlement.mediator;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.util.MessageHelper;
import org.jaxen.JaxenException;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.entitlement.mediator.callback.EntitlementCallbackHandler;
import org.wso2.carbon.identity.entitlement.mediator.callback.UTEntitlementCallbackHandler;
import org.wso2.carbon.identity.entitlement.proxy.Attribute;
import org.wso2.carbon.identity.entitlement.proxy.PEPProxy;
import org.wso2.carbon.identity.entitlement.proxy.PEPProxyConfig;
import org.wso2.carbon.identity.entitlement.proxy.ProxyConstants;
import org.wso2.carbon.identity.entitlement.proxy.exception.EntitlementProxyException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EntitlementMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(EntitlementMediator.class);

    private String remoteServiceUserName;
    private String remoteServicePassword;
    private String remoteServiceUrl;
    private String callbackClass;
    private String client;
    private String thriftPort;
    private String thriftHost;
    private String reuseSession;
    private String cacheType;
    private int invalidationInterval;
    private int maxCacheEntries;
    EntitlementCallbackHandler callback = null;
    /* The reference to the sequence which will execute when access is denied   */
    private String onRejectSeqKey = null;
    /* The in-line sequence which will execute when access is denied */
    private Mediator onRejectMediator = null;
    /* The reference to the sequence which will execute when access is allowed  */
    private String onAcceptSeqKey = null;
    /* The in-line sequence which will execute when access is allowed */
    private Mediator onAcceptMediator = null;
    /* The reference to the obligations sequence   */
    private String obligationsSeqKey = null;
    /* The in-line obligation sequence */
    private Mediator obligationsMediator = null;
    /* The reference to the advice sequence */
    private String adviceSeqKey = null;
    /* The in-line advice sequence */
    private Mediator adviceMediator = null;
    private PEPProxy pepProxy;


    /**
     * {@inheritDoc}
     */
    public boolean mediate(MessageContext synCtx) {

        String decisionString;
        String userName;
        String serviceName;
        String operationName;
        String action;
        String resourceName;
        Attribute[] otherAttributes;

        if (log.isDebugEnabled()) {
            log.debug("Mediation for Entitlement started");
        }

        try {
            userName = callback.getUserName(synCtx);
            serviceName = callback.findServiceName(synCtx);
            operationName = callback.findOperationName(synCtx);
            action = callback.findAction(synCtx);
            otherAttributes = callback.findOtherAttributes(synCtx);

            if (userName == null) {
                throw new SynapseException(
                        "User name not provided for the Entitlement mediator - can't proceed");
            }

            if (operationName != null) {
                resourceName = serviceName + "/" + operationName;
            } else {
                resourceName = serviceName;
            }

            if(otherAttributes == null){
                otherAttributes = new Attribute[0];
            }

            if (log.isDebugEnabled()) {
                StringBuilder debugOtherAttributes = new StringBuilder();
                debugOtherAttributes.append("Subject ID is : "  + userName +
                        " Resource ID is : "  + resourceName +
                        " Action ID is : "  + action + ".");
                if(otherAttributes.length > 0){
                    debugOtherAttributes.append("Other attributes are ");
                    for(int i = 0; i < otherAttributes.length; i++){
                        debugOtherAttributes.append("Attribute ID : ").append(otherAttributes[i].getId())
                                .append(" of Category : ").append(otherAttributes[i].getCategory())
                                .append(" of Type : ").append(otherAttributes[i].getType())
                                .append(" and Value : ").append(otherAttributes[i].getValue());
                        if(i < otherAttributes.length - 2){
                            debugOtherAttributes.append(", ");
                        } else if(i == otherAttributes.length - 2){
                            debugOtherAttributes.append(" and ");
                        } else {
                            debugOtherAttributes.append(".");
                        }
                    }
                }
                log.debug(debugOtherAttributes);
            }

            // if decision cache is disabled
            // Creating the XACML 3.0 Attributes to Send XACML Request
            Attribute[] tempArr = new Attribute[otherAttributes.length + 3];
            tempArr[0] = new Attribute("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject", "urn:oasis:names:tc:xacml:1.0:subject:subject-id", ProxyConstants.DEFAULT_DATA_TYPE, userName);
            tempArr[1] = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:action", "urn:oasis:names:tc:xacml:1.0:action:action-id", ProxyConstants.DEFAULT_DATA_TYPE, action);
            tempArr[2] = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:resource", "urn:oasis:names:tc:xacml:1.0:resource:resource-id", ProxyConstants.DEFAULT_DATA_TYPE, resourceName);
            for(int i=0;i<otherAttributes.length;i++){
                tempArr[3+i]= otherAttributes[i];
            }

            decisionString = pepProxy.getDecision(tempArr);
            String simpleDecision;
            OMElement obligations;
            OMElement advice;
            if(decisionString != null){
                String nameSpace =  null;
                OMElement decisionElement = AXIOMUtil.stringToOM(decisionString);
                OMNamespace omNamespace = decisionElement.getDefaultNamespace();
                if (omNamespace != null){
                    nameSpace = omNamespace.getNamespaceURI();
                }
                if(nameSpace == null){
                    simpleDecision = decisionElement.getFirstChildWithName(new QName("Result")).
                                                             getFirstChildWithName(new QName("Decision")).getText();
                    obligations = decisionElement.getFirstChildWithName(new QName("Obligations"));
                    advice = decisionElement.getFirstChildWithName(new QName("AdviceExpressions"));
                } else {
                    simpleDecision = decisionElement.getFirstChildWithName(new QName(nameSpace,"Result")).
                                                   getFirstChildWithName(new QName(nameSpace,"Decision")).getText();
                    obligations = decisionElement.getFirstChildWithName(new QName(nameSpace,"Obligations"));
                    advice = decisionElement.getFirstChildWithName(new QName(nameSpace,"AdviceExpressions"));
                }
                if(log.isDebugEnabled()){
                    log.debug("Entitlement Decision is : " + simpleDecision);
                }
            } else {
                //undefined decision;
                throw new SynapseException("Undefined Decision is received");
            }

            // assume entitlement mediator always acts as base PEP
            // then behavior for not-applicable and indeterminate results are undefined
            // but here assume to be deny
            if("Permit".equals(simpleDecision) || "Deny".equals(simpleDecision)){

                MessageContext obligationsSynCtx = null;
                MessageContext adviceSynCtx = null;
                // 1st check for advice
                if(advice != null){
                    adviceSynCtx = getOMElementInserted(advice,getClonedMessageContext(synCtx));
                    if(adviceSeqKey != null){
                        SequenceMediator sequence = (SequenceMediator) adviceSynCtx.getSequence(adviceSeqKey);
                        adviceSynCtx.getEnvironment().injectAsync(adviceSynCtx,sequence);
                    } else if(adviceMediator != null) {
                        adviceSynCtx.getEnvironment().injectAsync(adviceSynCtx,(SequenceMediator)adviceMediator);
                    }
                }

                if(obligations != null){
                    obligationsSynCtx = getOMElementInserted(obligations,getClonedMessageContext(synCtx));
                    Mediator localObligationsMediator;
                    if(obligationsSeqKey != null){
                        localObligationsMediator = obligationsSynCtx.getSequence(obligationsSeqKey);
                    } else {
                        localObligationsMediator = obligationsMediator;
                    }

                    boolean  areObligationsDone = localObligationsMediator.mediate(obligationsSynCtx);
                    if(!areObligationsDone){
                        // if return false, obligations are not correctly performed.
                        // So message is mediated through the OnReject sequence
                        log.debug("Obligations are not correctly performed");
                        simpleDecision = "Deny";
                    }
                }
            }

            if ("Permit".equals(simpleDecision)) {
                if (log.isDebugEnabled()) {
                    log.debug("User is authorized to perform the action");
                }
                Mediator localOnAcceptMediator;
                if(onAcceptSeqKey != null){
                    localOnAcceptMediator = synCtx.getSequence(onAcceptSeqKey);
                } else if (onAcceptMediator != null){
                    localOnAcceptMediator = onAcceptMediator;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("OnAccept sequence is not defined.");
                    }
                    return true;
                }
                localOnAcceptMediator.mediate(synCtx);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("User is not authorized to perform the action");
                }
                Mediator localOnRejectMediator;
                if(onRejectSeqKey != null){
                    localOnRejectMediator = synCtx.getSequence(onRejectSeqKey);
                } else if (onRejectMediator != null) {
                    localOnRejectMediator = onRejectMediator;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("OnReject sequence is not defined.");
                    }
                    throw new SynapseException("User is not authorized to perform the action");
                }
                localOnRejectMediator.mediate(synCtx);
            }

            return true;
        } catch (SynapseException e){
            log.error(e);
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while evaluating the policy", e);
            throw new SynapseException("Error occurred while evaluating the policy");
        }

    }

  /**
     *
     * @param url
     * @param config
     * @return
     */
    private static String getServerURL(String url, ConfigurationContext config) {
        if (url.indexOf("${carbon.https.port}") != -1) {
            String httpsPort = CarbonUtils.getTransportPort(config, "https") + "";
            url = url.replace("${carbon.https.port}", httpsPort);
        }

        if (url.indexOf("${carbon.management.port}") != -1) {
            String httpsPort = CarbonUtils.getTransportPort(config, "https") + "";
            url = url.replace("${carbon.management.port}", httpsPort);
        }

        if (url.indexOf("${carbon.context}") != -1) {
            // We need not to worry about context here - just need the server url for logging
            url = url.replace("${carbon.context}", "");
        }
        return url;
    }

    private Object loadClass(String className) throws AxisFault {
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            return  clazz.newInstance();
        } catch (Exception e) {
            log.error("Error occurred while loading " + className, e);
        }
        return null;
    }

    public void init(SynapseEnvironment synEnv) {

        try {
            if (callbackClass != null && callbackClass.trim().length() > 0) {
                Object loadedClass = loadClass(callbackClass);
                if(loadedClass instanceof EntitlementCallbackHandler){
                    callback = (EntitlementCallbackHandler) loadedClass;
                }
            } else {
                callback = new UTEntitlementCallbackHandler();
            }

            Map<String,Map<String,String>> appToPDPClientConfigMap = new HashMap<String, Map<String,String>>();
            Map<String,String> clientConfigMap = new HashMap<String, String>();

            if(client !=null && client.equals(EntitlementConstants.SOAP)){
                clientConfigMap.put(EntitlementConstants.CLIENT, client);
                clientConfigMap.put(EntitlementConstants.SERVER_URL, remoteServiceUrl);
                clientConfigMap.put(EntitlementConstants.USERNAME, remoteServiceUserName);
                clientConfigMap.put(EntitlementConstants.PASSWORD, remoteServicePassword);
                clientConfigMap.put(EntitlementConstants.REUSE_SESSION, reuseSession);
            }else if(client !=null && client.equals(EntitlementConstants.BASIC_AUTH)){
                clientConfigMap.put(EntitlementConstants.CLIENT, client);
                clientConfigMap.put(EntitlementConstants.SERVER_URL, remoteServiceUrl);
                clientConfigMap.put(EntitlementConstants.USERNAME, remoteServiceUserName);
                clientConfigMap.put(EntitlementConstants.PASSWORD, remoteServicePassword);
            }else if(client !=null && client.equals(EntitlementConstants.THRIFT)){
                clientConfigMap.put(EntitlementConstants.CLIENT, client);
                clientConfigMap.put(EntitlementConstants.SERVER_URL, remoteServiceUrl);
                clientConfigMap.put(EntitlementConstants.USERNAME, remoteServiceUserName);
                clientConfigMap.put(EntitlementConstants.PASSWORD, remoteServicePassword);
                clientConfigMap.put(EntitlementConstants.REUSE_SESSION, reuseSession);
                clientConfigMap.put(EntitlementConstants.THRIFT_HOST, thriftHost);
                clientConfigMap.put(EntitlementConstants.THRIFT_PORT, thriftPort);
            } else if(client !=null && client.equals(EntitlementConstants.WS_XACML)){
                clientConfigMap.put(EntitlementConstants.CLIENT, client);
                clientConfigMap.put(EntitlementConstants.SERVER_URL, remoteServiceUrl);
                clientConfigMap.put(EntitlementConstants.USERNAME, remoteServiceUserName);
                clientConfigMap.put(EntitlementConstants.PASSWORD, remoteServicePassword);
            }else if(client == null){
                clientConfigMap.put(EntitlementConstants.SERVER_URL, remoteServiceUrl);
                clientConfigMap.put(EntitlementConstants.USERNAME, remoteServiceUserName);
                clientConfigMap.put(EntitlementConstants.PASSWORD, remoteServicePassword);
            } else {
                log.error("EntitlementMediator initialization error: Unsupported client");
                throw new SynapseException("EntitlementMediator initialization error: Unsupported client");
            }

            appToPDPClientConfigMap.put("EntitlementMediator", clientConfigMap);
            PEPProxyConfig config = new PEPProxyConfig(appToPDPClientConfigMap,"EntitlementMediator", cacheType, invalidationInterval, maxCacheEntries);

            try {
                pepProxy = new PEPProxy(config);
            } catch (EntitlementProxyException e) {
                log.error("Error while initializing the PEP Proxy" + e);
                throw new SynapseException("Error while initializing the Entitlement PEP Proxy");
            }

            if (onAcceptMediator instanceof ManagedLifecycle) {
                ((ManagedLifecycle) onAcceptMediator).init(synEnv);
            }
            if (onRejectMediator instanceof ManagedLifecycle) {
                ((ManagedLifecycle) onRejectMediator).init(synEnv);
            }
            if (obligationsMediator instanceof ManagedLifecycle) {
                ((ManagedLifecycle) obligationsMediator).init(synEnv);
            }
            if (adviceMediator instanceof ManagedLifecycle) {
                ((ManagedLifecycle) adviceMediator).init(synEnv);
            }

        } catch (AxisFault e) {
            String msg = "Error initializing entitlement mediator : " + e.getMessage();
            log.error(msg, e);
            throw new SynapseException(msg, e);
        }
    }

    @Override
    public void destroy() {

        remoteServiceUserName = null;
        remoteServicePassword = null;
        remoteServiceUrl = null;
        callbackClass = null;
        client = null;
        thriftPort = null;
        thriftHost = null;
        reuseSession = null;
        cacheType = null;
        callback = null;
        onRejectSeqKey = null;
        onAcceptSeqKey = null;
        obligationsSeqKey = null;
        adviceSeqKey = null;
        pepProxy = null;

        if (onAcceptMediator instanceof ManagedLifecycle) {
            ((ManagedLifecycle) onAcceptMediator).destroy();
        }
        if (onRejectMediator instanceof ManagedLifecycle) {
            ((ManagedLifecycle) onRejectMediator).destroy();
        }
        if (obligationsMediator instanceof ManagedLifecycle) {
            ((ManagedLifecycle) obligationsMediator).destroy();
        }
        if (adviceMediator instanceof ManagedLifecycle) {
            ((ManagedLifecycle) adviceMediator).destroy();
        }

    }

    /**
     * Clone the provided message context
     *
     * @param synCtx - MessageContext which is subjected to the cloning
     *
     * @return MessageContext the cloned message context
     */
    private MessageContext getClonedMessageContext(MessageContext synCtx) {

        MessageContext newCtx = null;
        try {
            newCtx = MessageHelper.cloneMessageContext(synCtx);
            // Set isServerSide property in the cloned message context
            ((Axis2MessageContext) newCtx).getAxis2MessageContext().setServerSide(
                    ((Axis2MessageContext) synCtx).getAxis2MessageContext().isServerSide());
        } catch (AxisFault axisFault) {
            handleException("Error cloning the message context", axisFault, synCtx);
        }
        return newCtx;
    }

    /**
     * Create a new SOAP envelope and insert the
     * the given omElement into its body.
     *
     * @param synCtx    - original message context
     * @return newCtx created by the iteration
     * @throws AxisFault if there is a message creation failure
     * @throws JaxenException if the expression evauation failure
     */
    private MessageContext getOMElementInserted(OMElement omElement, MessageContext synCtx)
            throws AxisFault, JaxenException {

        Iterator<OMNode> children = synCtx.getEnvelope().getBody().getChildren();
        while(children.hasNext()){
            children.next().detach();
        }
        synCtx.getEnvelope().getBody().addChild(omElement);
        return synCtx;
    }

    /* Creating a soap response according the the soap namespce uri */
    private SOAPEnvelope createDefaultSOAPEnvelope(MessageContext inMsgCtx) {

        String soapNamespace = inMsgCtx.getEnvelope().getNamespace()
                .getNamespaceURI();
        SOAPFactory soapFactory = null;
        if (soapNamespace.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
        } else if (soapNamespace
                .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
        } else {
            log.error("Unknown SOAP Envelope");
        }
        return soapFactory.getDefaultEnvelope();
    }

    public String getCallbackClass() {
        return callbackClass;
    }

    public void setCallbackClass(String callbackClass) {
        this.callbackClass = callbackClass;
    }

    public String getRemoteServiceUserName() {
        return remoteServiceUserName;
    }

    public void setRemoteServiceUserName(String remoteServiceUserName) {
        this.remoteServiceUserName = remoteServiceUserName;
    }

    public String getRemoteServicePassword() {
        if (!remoteServicePassword.startsWith("enc:")) {
            try {
                return "enc:"
                        + CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(
                                remoteServicePassword.getBytes());
            } catch (CryptoException e) {
                log.error(e);
            }
        }
        return remoteServicePassword;
    }

    public void setRemoteServicePassword(String remoteServicePassword) {
        if (remoteServicePassword.startsWith("enc:")) {
            try {
                this.remoteServicePassword = new String(CryptoUtil.getDefaultCryptoUtil()
                        .base64DecodeAndDecrypt(remoteServicePassword.substring(4)));
            } catch (CryptoException e) {
                 log.error(e);
            }
        } else {
            this.remoteServicePassword = remoteServicePassword;
        }
    }
    
    public String getRemoteServiceUrl() {
        return remoteServiceUrl;
    }

    public void setRemoteServiceUrl(String remoteServiceUrl) {
        this.remoteServiceUrl = remoteServiceUrl;
    }

    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    public int getInvalidationInterval() {
        return invalidationInterval;
    }

    public void setInvalidationInterval(int invalidationInterval) {
        this.invalidationInterval = invalidationInterval;
    }

    public int getMaxCacheEntries() {
        return maxCacheEntries;
    }

    public void setMaxCacheEntries(int maxCacheEntries) {
        this.maxCacheEntries = maxCacheEntries;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getThriftPort() {
        return thriftPort;
    }

    public void setThriftPort(String thriftPort) {
        this.thriftPort = thriftPort;
    }

    public String getThriftHost() {
        return thriftHost;
    }

    public void setThriftHost(String thriftHost) {
        this.thriftHost = thriftHost;
    }

    public String getReuseSession() {
        return reuseSession;
    }

    public void setReuseSession(String reuseSession) {
        this.reuseSession = reuseSession;
    }

    public String getOnRejectSeqKey() {
        return onRejectSeqKey;
    }

    public void setOnRejectMediator(Mediator onRejectMediator) {
        this.onRejectMediator = onRejectMediator;
    }

    public String getOnAcceptSeqKey() {
        return onAcceptSeqKey;
    }

    public void setOnAcceptMediator(Mediator onAcceptMediator) {
        this.onAcceptMediator = onAcceptMediator;
    }

    public Mediator getOnRejectMediator() {
        return onRejectMediator;
    }

    public void setOnRejectSeqKey(String onRejectSeqKey) {
        this.onRejectSeqKey = onRejectSeqKey;
    }

    public Mediator getOnAcceptMediator() {
        return onAcceptMediator;
    }

    public void setOnAcceptSeqKey(String onAcceptSeqKey) {
        this.onAcceptSeqKey = onAcceptSeqKey;
    }

    public String getObligationsSeqKey() {
        return obligationsSeqKey;
    }

    public void setObligationsMediator(Mediator obligationsMediator) {
        this.obligationsMediator = obligationsMediator;
    }

    public Mediator getObligationsMediator() {
        return obligationsMediator;
    }

    public void setObligationsSeqKey(String obligationsSeqKey) {
        this.obligationsSeqKey = obligationsSeqKey;
    }

    public Mediator getAdviceMediator() {
        return adviceMediator;
    }

    public void setAdviceMediator(Mediator adviceMediator) {
        this.adviceMediator = adviceMediator;
    }

    public String getAdviceSeqKey() {
        return adviceSeqKey;
    }

    public void setAdviceSeqKey(String adviceSeqKey) {
        this.adviceSeqKey = adviceSeqKey;
    }


}
