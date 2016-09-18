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
package org.wso2.carbon.mediator.payloadfactory;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.SynapseJsonPathFactory;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.util.xpath.SynapseJsonPath;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class PayloadFactoryMediator extends AbstractMediator {

    private static Log log = LogFactory.getLog(PayloadFactoryMediator.class);

    private static final String PAYLOAD_FACTORY = "payloadFactory";
    private static final String FORMAT = "format";
    private static final String ARGS = "args";
    private static final String ARG = "arg";
    private static final String VALUE = "value";
    private static final String EXPRESSION = "expression";
    private static final String EVAL = "evaluator";
    private static final String DEEP_CHECK = "deepCheck";
    private static final String LITERAL = "literal";
    private static final String TYPE = "media-type";

    private static final QName FORMAT_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "format");
    private static final QName ARGS_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "args");
    protected static final QName ATT_KEY = new QName("key");
    protected static final QName ATT_EVAL   = new QName("evaluator");
    protected static final QName ATT_DEEP_CHECK   = new QName("deepCheck");
    protected static final QName ATT_LITERAL   = new QName("literal");
    protected static final QName ATT_MEDIA   = new QName("media-type");

    private final String JSON_TYPE="json";
    private final String XML_TYPE="xml";
    private final String TEXT_TYPE="text";

    private String format = null;
    private String type = null;
    private boolean dynamic = false;
    private String formatKey = null;
    private List<Argument> argumentList = new ArrayList<Argument>();

    /// This the error private String evaluator = null;

    public OMElement serialize(OMElement parent) {

        OMElement payloadFactoryElem = fac.createOMElement(PAYLOAD_FACTORY, synNS);
        if(type!=null){
            payloadFactoryElem.addAttribute(fac.createOMAttribute(TYPE, nullNS,type));
        }
        saveTracingState(payloadFactoryElem, this);

        if (!dynamic) {
            if (format != null) {
                try {
                    OMElement formatElem = fac.createOMElement(FORMAT, synNS);
                    if(type!=null && (type.contains(JSON_TYPE) || type.contains(TEXT_TYPE))){
                        formatElem.setText(format);
                    } else{
                        formatElem.addChild(AXIOMUtil.stringToOM(format));
                    }
                    payloadFactoryElem.addChild(formatElem);
                } catch (XMLStreamException e) {
                    handleException("Error while serializing payloadFactory mediator", e);
                }
            } else {
                handleException("Invalid payloadFactory mediator, format is required");
            }
        } else {
            if (formatKey != null) {
                OMElement formatElem = fac.createOMElement(FORMAT, synNS);
                formatElem.addAttribute(fac.createOMAttribute(
                        "key", nullNS, formatKey));
                payloadFactoryElem.addChild(formatElem);
            } else {
                handleException("Invalid payloadFactory mediator, format is required");
            }
        }

        if (argumentList != null && argumentList.size() > 0) {

            OMElement argumentsElem = fac.createOMElement(ARGS, synNS);

            for (Argument arg : argumentList) {

                OMElement argElem = fac.createOMElement(ARG, synNS);

                if (arg.isDeepCheck()) {
                    argElem.addAttribute(fac.createOMAttribute(DEEP_CHECK, nullNS, "true"));
                } else {
                    argElem.addAttribute(fac.createOMAttribute(DEEP_CHECK, nullNS, "false"));
                }

                if (arg.isLiteral()) {
                    argElem.addAttribute(fac.createOMAttribute(LITERAL, nullNS, "true"));
                } else {
                    argElem.addAttribute(fac.createOMAttribute(LITERAL, nullNS, "false"));
                }

                if (arg.getValue() != null) {
                    argElem.addAttribute(fac.createOMAttribute(VALUE, nullNS, arg.getValue()));
                } else if (arg.getExpression() != null) {
                        SynapseXPathSerializer.serializeXPath(arg.getExpression(), argElem, EXPRESSION);
                } else if(arg.getJsonPath()!=null){
                    argElem.addAttribute(fac.createOMAttribute(EXPRESSION, nullNS, arg.getJsonPath().getExpression()));
                }
                 //error happens somewhere here
                if (arg.getEvaluator() != null) {
                    argElem.addAttribute(fac.createOMAttribute(EVAL, nullNS, arg.getEvaluator()));
                }
                argumentsElem.addChild(argElem);

            }
            payloadFactoryElem.addChild(argumentsElem);
        }

        if (parent != null) {
            parent.addChild(payloadFactoryElem);
        }

        return payloadFactoryElem;
    }

    public void build(OMElement elem) {

        OMAttribute mediaType = elem.getAttribute(ATT_MEDIA);
        if(mediaType!=null){
            this.type=mediaType.getAttributeValue();
        }
        OMElement formatElem = elem.getFirstChildWithName(FORMAT_Q);

        if (formatElem != null) {
            OMAttribute n = formatElem.getAttribute(ATT_KEY);
            if (n == null) {
                if(type!=null && (type.contains(JSON_TYPE) || type.contains(TEXT_TYPE))){
                    this.format = formatElem.getText();
                } else{
                    this.format = formatElem.getFirstElement().toString();
                }
            } else {
                this.formatKey = n.getAttributeValue();
                this.dynamic = true;
            }
        } else {
            handleException("format element of payloadFactoryMediator is required");
        }

        OMElement argumentsElem = elem.getFirstChildWithName(ARGS_Q);

        if (argumentsElem != null) {

            Iterator itr = argumentsElem.getChildElements();

            while (itr.hasNext()) {
                OMElement argElem = (OMElement) itr.next();
                Argument arg = new Argument();
                String attrValue;
                String deepCheckValue;
                String isLiteral;
                if ((deepCheckValue = argElem.getAttributeValue(ATT_DEEP_CHECK)) != null) {
                    if (deepCheckValue.equalsIgnoreCase("false")) {
                        arg.setDeepCheck(false);
                    } else {
                        arg.setDeepCheck(true);
                    }
                }

                if ((isLiteral = argElem.getAttributeValue(ATT_LITERAL)) != null) {
                    if (isLiteral.equalsIgnoreCase("false")) {
                        arg.setLiteral(false);
                    } else {
                        arg.setLiteral(true);
                    }
                }

                if ((attrValue = argElem.getAttributeValue(ATT_VALUE)) != null) {

                    arg.setValue(attrValue);
                    argumentList.add(arg);
                } else if ((attrValue = argElem.getAttributeValue(ATT_EXPRN)) != null) {
                    if (attrValue.trim().length() == 0) {
                        handleException("Attribute value for expression cannot be empty");
                    } else {
                        try {
                            String evaluator;

                            if((evaluator=argElem.getAttributeValue(ATT_EVAL))!=null && evaluator.contains(JSON_TYPE)){

                                arg.setEvaluator(evaluator);
                                arg.setJsonPath(SynapseJsonPathFactory.getSynapseJsonPath(attrValue));
                                argumentList.add(arg);

                            } else{

                                if(evaluator!=null && evaluator.equals(XML_TYPE)){
                                    arg.setEvaluator(XML_TYPE);
                                }
                                arg.setExpression(SynapseXPathFactory.getSynapseXPath(argElem, ATT_EXPRN));
                                argumentList.add(arg);
                            }

                        } catch (JaxenException e) {
                            handleException("Invalid XPath expression for attribute expression : " +
                                    attrValue, e);
                        }
                    }

                } else {
                    handleException("Unsupported arg type or expression attribute required");
                }

                  ///argumentList.add(arg); values and args all get added to this.
                // need to change this part of the cord.
            }
        }

        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public String getFormatKey() {
        return formatKey;
    }

    public void setFormatKey(String formatKey) {
        this.formatKey = formatKey;
    }

    public String getFormat() {
        return format;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type=type;
    }

    public void addArgument(Argument arg) {
        argumentList.add(arg);
    }

    public List<Argument> getArgumentList() {
        return argumentList;
    }

    public String getTagLocalName() {
        return PAYLOAD_FACTORY;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new MediatorException(msg);
    }

    private void handleException(String msg, Exception ex) {
        log.error(msg, ex);
        throw new MediatorException(msg + " Caused by " + ex.getMessage());
    }

    public static class Argument {

        private String value;
        private SynapseXPath expression;
        private SynapseJsonPath jsonPath;
        private String evaluator;
        private boolean deepCheck = true;
        private boolean literal = false;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public SynapseXPath getExpression() {
            return expression;
        }

        public void setExpression(SynapseXPath expression) {
            this.expression = expression;
        }

        public SynapseJsonPath getJsonPath() {
            return jsonPath;
        }

        public void setJsonPath(SynapseJsonPath jsonPath) {
            this.jsonPath = jsonPath;
        }

        public String getEvaluator() {
            return evaluator;
        }

        public void setEvaluator(String evaluator) {
            this.evaluator = evaluator;
        }

        public boolean isDeepCheck() {
            return deepCheck;
        }

        public void setDeepCheck(boolean deepCheck) {
            this.deepCheck = deepCheck;
        }

        public boolean isLiteral() {
            return literal;
        }

        public void setLiteral(boolean literal) {
            this.literal = literal;
        }
    }
}
