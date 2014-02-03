/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.business.messaging.salesforce.mediator.handler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.business.messaging.salesforce.mediator.OperationType;
import org.wso2.carbon.business.messaging.salesforce.mediator.OutputType;

/**
 * <p>
 * Hold the <code>Operation</code> and constructs the request header and the
 * payload of the soap envilope based on the configuratoin in the
 * <code>SalesforceMedaitor</code>.
 * </p>
 *
 * @see org.wso2.carbon.business.messaging.salesforce.mediator.OperationType
 * @see org.wso2.carbon.business.messaging.salesforce.mediator.InputType
 * @see org.wso2.carbon.business.messaging.salesforce.mediator.OutputType
 */
public class ResponseHandler {

    /**
     * <p>
     * Holds the log4j based log for the login purposes
     * </p>
     */
    private static final Log log = LogFactory.getLog(ResponseHandler.class);

    /**
     * <p>
     * If both the <code>expression</code> provided then the evaluated string
     * value of the <code>expression</code> over the message will be returned.
     * </p>
     *
     * @param synCtx message to be evaluated.
     * @return the evaluated string value of the <code>expression</code>
     */
    public OMElement handleOutput(OutputType output, MessageContext synCtx,
                                  Object result) {
        String type = output.getType();
        String propertyKey = output.getTargetKey();
        SynapseXPath targetXPath = output.getTargetXPath();
        SynapseXPath srcXPath = output.getSourceXPath();

        if (type == null) {
            log.info("Response handler Types not defined for salesforce.com configuration");
            return null;
        }

        String operatonName = output.getName();
        log.debug(String.format("Start handling response for %s",
                                null != type ? type : result.getClass().getName()));

        OMElement response = null;
        try {

            if (isWrapperOrPrimitiveType(result.getClass())) {
                //is primitive
                if (null == output) {
                    handleException("Operation name was null",
                                    new IllegalArgumentException(
                                            "Operation name was null"));
                }
                OMFactory fac = OMAbstractFactory.getOMFactory();
                OMNamespace ns = fac.createOMNamespace(
                        "urn:enterprise.soap.sforce.com", "ns1");
                OMElement rootElem = fac.createOMElement(operatonName
                        .toUpperCase().charAt(0)
                                                         + operatonName.substring(1) + "Response", ns);
                OMElement resultElem = fac.createOMElement("result", ns);
                resultElem.addAttribute("value", result.toString(), null);
                resultElem.addAttribute("type", result.getClass()
                        .getCanonicalName(), null);

                rootElem.addChild(resultElem);
                response = rootElem;
                handlePropertyInjection(response, propertyKey, synCtx);
            } else {
                Object instance = PropertyHandler.newInstance(type);
                PropertyHandler.invoke(instance, "setResult", result);

                Field field = instance.getClass().getDeclaredField("MY_QNAME");
                Method getOMElemMethod = instance.getClass().getMethod(
                        "getOMElement",
                        new Class[]{javax.xml.namespace.QName.class,
                                    org.apache.axiom.om.OMFactory.class});
                response = (OMElement) getOMElemMethod.invoke(instance,
                                                              new Object[]{(QName) field.get(null),
                                                                           OMAbstractFactory.getOMFactory()});
                if (response != null) {
                    handlePropertyInjection(response, targetXPath, srcXPath, synCtx, propertyKey);
                }

            }

        } catch (Exception e) {
            handleException(String.format("Error handling response for %s",
                                          null != type ? type : result.getClass().getName()), e);
        }

        log.debug(String.format("End handling response for %s",
                                null != type ? type : result.getClass().getName()));


        return response;
    }

    /**
     * <p>
     * If both the <code>expression</code> provided then the evaluated string
     * value of the <code>expression</code> over the message will be returned.
     * </p>
     *
     * @param synCtx message to be evaluated.
     * @return the evaluated string value of the <code>expression</code>
     */
    public void handle(OperationType operation, MessageContext synCtx,
                       Object result) {

        if (result != null) {
            Iterator<OutputType> iter = operation.getOutputs().iterator();

            while (iter.hasNext()) {
                OutputType outputKey = iter.next();
                if (checkOutputTypeForProcessing(outputKey)) {
                    handleOutput(outputKey, synCtx, result);
                }
            }
        } else {
            log.debug("Salesforce Proxy Response returned is null");
        }
    }

    /**
     * output Type validation
     *
     * @param outputKey
     * @return
     */
    private boolean checkOutputTypeForProcessing(OutputType outputKey) {
        SynapseXPath srcXPath = outputKey.getSourceXPath();
        SynapseXPath targetXPath = outputKey.getTargetXPath();
        String targetKey = outputKey.getTargetKey();
        if ((srcXPath != null && (targetKey != null && !"".equals(targetKey) || targetXPath != null)) ||
            (targetKey != null && !"".equals(targetKey))) {
            return true;
        }
        return false;
    }

    private void handlePropertyInjection(Object target, SynapseXPath targetXpath, MessageContext synCtx) {
        if (targetXpath != null) {
            try {
                OMElement evalNode = (OMElement) targetXpath.selectSingleNode(synCtx.getEnvelope().getBody());
                if (evalNode != null) {
                    if (target instanceof OMNode) {
                        evalNode.addChild((OMNode) target);
                    } else if (target instanceof List) {
                        List OMElementList = (List) target;
                        Iterator nodes = OMElementList.iterator();
                        while (nodes.hasNext()) {
                            OMNode node = (OMNode) nodes.next();
                            evalNode.addChild(node);
                        }
                    }
                }
            } catch (JaxenException e) {
                log.debug(String.format("Error handling Salesforce Response Element for target XPath Expression %s",
                                        targetXpath.toString()));

            }
        }
    }

    private void handlePropertyInjection(OMElement response, SynapseXPath targetXpath,
                                         SynapseXPath srcXPath, MessageContext ctxt, String key) {
        try {
            Object evalNode = null;
            if (srcXPath != null) {
                evalNode = srcXPath.selectNodes(response);
            } else {
                evalNode = response;
            }

            if (evalNode != null) {
                handlePropertyInjection(evalNode, targetXpath, ctxt);
                handlePropertyInjection(evalNode, key, ctxt);
            }
        } catch (JaxenException e) {
            log.debug(String.format("Error handling Salesforce Response Element for source XPath Expression %s",
                                    srcXPath.toString()));
        }
    }

    private void handlePropertyInjection(Object response, String key, MessageContext ctxt) {
        if (key != null && !"".equals(key)) {
            ctxt.setProperty(key, response);
        }
    }

    /**
     * Validates to check weather the passed class is a primitive or a wrapper
     * type.
     */
    @SuppressWarnings("unchecked")
    private boolean isWrapperOrPrimitiveType(Class clazz) {

        boolean flag = false;
        if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
            flag = true;
        } else if (clazz.equals(long.class) || clazz.equals(Long.class)) {
            flag = true;
        } else if (clazz.equals(float.class) || clazz.equals(Float.class)) {
            flag = true;
        } else if (clazz.equals(double.class) || clazz.equals(Double.class)) {
            flag = true;
        } else if (clazz.equals(String.class)) {
            flag = true;
        } else if (clazz.equals(char.class) || clazz.equals(Character.class)) {
            flag = true;
        } else if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
            flag = true;
        }

        return flag;
    }

    /**
     * Logs the exception and wraps the source message into a
     * <code>SynapseException</code> exception.
     *
     * @param msg the source message
     * @param e   the exception
     */
    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }
}
