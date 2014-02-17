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
package org.wso2.carbon.mediator.rule;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.wso2.carbon.rule.mediator.config.RuleMediatorConfig;
import org.wso2.carbon.rule.mediator.internal.config.RuleMediatorConfigHelper;

import javax.xml.namespace.QName;

/**
 *
 */
public class RuleMediator extends AbstractListMediator {

    private static final Log log = LogFactory.getLog(RuleMediator.class);

    public static final QName ATT_KEY_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "key");

    public static final QName ELE_CHILD_MEDIATORS_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "childMediators");

    private RuleMediatorConfig ruleMediatorConfig;

//    /*Configuration for Rule Invoker*/
//    private RuleMediatorDescription ruleMediatorDescription;
//    private final static ExtensionSerializer EXTENSION_SERIALIZER =
//            new RuleMediatorExtensionSerializer();
//    private final static ExtensionBuilder EXTENSION_BUILDER = new RuleMediatorExtensionBuilder();

    public OMElement serialize(OMElement parent) {

        OMElement ruleElement = this.ruleMediatorConfig.toOM();
        saveTracingState(ruleElement, this);
        if (parent != null) {
            parent.addChild(ruleElement);
        }
        return ruleElement;

    }

    public void build(OMElement element) {
        this.ruleMediatorConfig = RuleMediatorConfigHelper.getRuleMediatorConfig(element);
    }

    public String getTagLocalName() {
        return "rule";
    }

    public RuleMediatorConfig getRuleMediatorConfig() {
        return ruleMediatorConfig;
    }

    public void setRuleMediatorConfig(RuleMediatorConfig ruleMediatorConfig) {
        this.ruleMediatorConfig = ruleMediatorConfig;
    }
}
