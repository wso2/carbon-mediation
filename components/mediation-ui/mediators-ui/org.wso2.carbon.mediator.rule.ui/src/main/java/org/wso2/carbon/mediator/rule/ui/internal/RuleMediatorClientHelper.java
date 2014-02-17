/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.mediator.rule.ui.internal;

import org.wso2.carbon.rule.common.Fact;
import org.wso2.carbon.rule.common.Input;
import org.wso2.carbon.rule.common.Output;
import org.wso2.carbon.rule.mediator.config.RuleMediatorConfig;
import org.wso2.carbon.rule.mediator.config.Source;
import org.wso2.carbon.rule.mediator.config.Target;
import org.wso2.carbon.sequences.ui.util.SequenceEditorHelper;
import org.wso2.carbon.sequences.ui.util.ns.NameSpacesInformation;
import org.wso2.carbon.sequences.ui.util.ns.NameSpacesInformationRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.Map;


/**
 *
 */
public class RuleMediatorClientHelper {
    static NameSpacesInformationRepository repository;
    static NameSpacesInformation information = null;
    static NameSpacesInformation targetNSInformation = null;
    static String ownerID;

    public static void init(HttpServletRequest request) {
        repository = (NameSpacesInformationRepository) request.getSession().getAttribute(
                NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);
        ownerID = SequenceEditorHelper.getEditingMediatorPosition(request.getSession());


    }

    public static void populateSource(HttpServletRequest request, RuleMediatorConfig ruleMediatorConfig) {

        Source source = ruleMediatorConfig.getSource();

        if (request.getParameter("mediator.rule.source.value") != null) {
            source.setValue(request.getParameter("mediator.rule.source.value"));
        }

        if (request.getParameter("mediator.rule.source.xpath") != null) {
            source.setXpath(request.getParameter("mediator.rule.source.xpath"));
        }
        if (repository != null) {
            information = repository.getNameSpacesInformation(ownerID, "sourceValue");
            if (information != null) {
                source.setPrefixToNamespaceMap(information.getNameSpaces());
            }
        }

    }

    public static void populateTarget(HttpServletRequest request, RuleMediatorConfig ruleMediatorConfig) {

        Target target = ruleMediatorConfig.getTarget();
        if (request.getParameter("mediator.rule.target.value") != null) {
            target.setValue(request.getParameter("mediator.rule.target.value"));
        }

        if (request.getParameter("mediator.rule.target.resultXpath") != null) {
            target.setResultXpath(request.getParameter("mediator.rule.target.resultXpath"));
        }

        if (request.getParameter("mediator.rule.target.xpath") != null) {
            target.setXpath(request.getParameter("mediator.rule.target.xpath"));
        }

        if (request.getParameter("mediator.rule.target.action") != null) {
            target.setAction(request.getParameter("mediator.rule.target.action"));
        }
        if (repository != null) {
            information = repository.getNameSpacesInformation(ownerID, "resultValue");
            if (information != null) {
                target.setPrefixToNamespaceMap(information.getNameSpaces());
            }
//            targetNSInformation = repository.getNameSpacesInformation(ownerID, "targetNS");
//            if (targetNSInformation != null) {
//                target.setPrefixToTargetNamespaceMap(targetNSInformation.getNameSpaces());
//
//            }
        }

    }


    public static void registerNameSpaces(Map<String, String> properties, String baseId,
                                          HttpSession httpSession) {

        if (properties == null || baseId == null || "".equals(baseId)) {
            return;
        }

        int i = 0;
//        for (String key : properties.keySet()) {
//            if (key != null) {
//                BaseXPath xPath = property.getExpression();
//                if (xPath instanceof AXIOMXPath) {
//                    NameSpacesRegistrar.getInstance().registerNameSpaces((AXIOMXPath) xPath,
//                            baseId + String.valueOf(i), httpSession);
//                }
//            }
//            i++;
//        }
    }

    public static void setProperty(HttpServletRequest request,
                                   Object configuration, String mName, String id) {

        String registrationPropertyCount = request.getParameter(id + "propertyCount");
        if (registrationPropertyCount != null && !"".equals(registrationPropertyCount)) {
            int propertyCount = 0;
            try {
                propertyCount = Integer.parseInt(registrationPropertyCount.trim());

                for (int i = 0; i <= propertyCount; i++) {
                    String name = request.getParameter(id + "propertyName" + i);
                    if (name != null && !"".equals(name)) {
                        String valueId = id + "propertyValue" + i;
                        String value = request.getParameter(valueId);
                        if (value == null || "".equals(value.trim())) {
                            continue;
                        }
//                        PropertyDescription mp = new PropertyDescription();
//                        mp.setName(name.trim());
//                        mp.setValue(value.trim());
//                        invokeInstanceProperty(mName, mp, configuration);
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public static void updateInputFacts(HttpServletRequest request,
                                        RuleMediatorConfig ruleMediatorConfig,
                                        String id) {

        String inputCountParameter = request.getParameter(id + "Count");
        if (inputCountParameter != null && !"".equals(inputCountParameter)) {
            int inputCount = 0;
            try {
                inputCount = Integer.parseInt(inputCountParameter.trim());
                if (inputCount > 0) {
                    Input input = ruleMediatorConfig.getInput();
                    input.setWrapperElementName(request.getParameter("inputWrapperName"));
                    input.setNameSpace(request.getParameter("inputNameSpace"));
                    input.getFacts().clear();
                    Fact inputFact = null;
                    for (int i = 0; i < inputCount; i++) {
                        String type = request.getParameter(id + "Type" + i);
                        String elementName = request.getParameter(id + "ElementName" + i);
                        String namespace = request.getParameter(id + "Namespace" + i);
                        String xpath = request.getParameter(id + "Xpath" + i);
                        String nsID = id + "Value" + i;
                        if (type != null && !"".equals(type)) {

                            inputFact = new Fact();
                            inputFact.setType(type);
                            inputFact.setElementName(elementName);
                            inputFact.setNamespace(namespace);
                            inputFact.setXpath(xpath);
                            input.addFact(inputFact);
                        }
                        if (repository != null) {

                            information = repository.getNameSpacesInformation(ownerID, nsID);
                            if (information != null) {
                                inputFact.setPrefixToNamespaceMap(information.getNameSpaces());
                            }
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public static void updateOutputFacts(HttpServletRequest request,
                                         RuleMediatorConfig ruleMediatorConfig,
                                         String id) {
        String outputCountParameter = request.getParameter(id + "Count");
        if (outputCountParameter != null && !"".equals(outputCountParameter)) {
            int outputCount = 0;
            try {
                outputCount = Integer.parseInt(outputCountParameter.trim());
                if (outputCount > 0) {
                    Output outPut = ruleMediatorConfig.getOutput();
                    outPut.setWrapperElementName(request.getParameter("outputWrapperName"));
                    outPut.setNameSpace(request.getParameter("outputNameSpace"));
                    outPut.getFacts().clear();
                    Fact outputFact = null;
                    for (int i = 0; i < outputCount; i++) {
                        String type = request.getParameter(id + "Type" + i);
                        String elementName = request.getParameter(id + "ElementName" + i);
                        String namespace = request.getParameter(id + "Namespace" + i);
//                        String xpath = request.getParameter(id + "Xpath" + i);
                        if (type != null && !"".equals(type)) {

                            outputFact = new Fact();
                            outputFact.setType(type);
                            outputFact.setElementName(elementName);
                            outputFact.setNamespace(namespace);
//                            outputFact.setXpath(xpath);
                            outPut.addFact(outputFact);
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static void invokeInstanceProperty(String mName, Object val, Object target) {
        Class<?> aClass = target.getClass();
        try {
            Method method = aClass.getMethod(mName, val.getClass());
            method.invoke(target, val);
        } catch (Exception e) {
            throw new RuntimeException("Error setting property : " + mName
                    + " into" + aClass + " : " + e.getMessage(), e);
        }
    }

}
