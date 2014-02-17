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
package org.wso2.carbon.mediator.conditionalrouter;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.commons.evaluators.Evaluator;
import org.apache.synapse.commons.evaluators.EvaluatorException;
import org.apache.synapse.commons.evaluators.config.EvaluatorFactoryFinder;
import org.apache.synapse.commons.evaluators.config.EvaluatorSerializer;
import org.apache.synapse.commons.evaluators.config.EvaluatorSerializerFinder;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.xmlbeans.impl.xb.xmlconfig.Qnameconfig;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.apache.axiom.om.OMElement;

import java.util.LinkedList;
import java.util.List;
import javax.swing.plaf.synth.SynthConstants;
import javax.xml.namespace.QName;

public class ConditionalRouteMediator extends AbstractListMediator {

    private boolean breakAfter = false;
    private boolean isAsynchronous = false;
    private Evaluator evaluator;
    private String targetSeq;

    private static final String ROUTE = "conditionalRoute";
    private static final String CONDITION = "condition";
    private static final String TARGET = "target";
    private static final String BREAK_ROUTE = "breakRoute";
    private static final String ASYNCHRONOUS = "asynchronous";
    private static final String TARGET_SEQ = "sequence";


    public static final QName BREAK_AFTER_Q = new QName(BREAK_ROUTE);
    public static final QName ASYNCHRONOUS_Q = new QName(ASYNCHRONOUS);
    public static final QName CONDITION_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,CONDITION);
    public static final QName TARGET_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, TARGET);
    private static final QName SEQUENCE_Q = new QName(TARGET_SEQ);


    public OMElement serialize(OMElement parent) {

        OMElement routeElem = fac.createOMElement(ROUTE, synNS);

        if (evaluator != null) {
            EvaluatorSerializer evaluatorSerializer =
                    EvaluatorSerializerFinder.getInstance().getSerializer(evaluator.getName());
            if (evaluatorSerializer != null) {
                OMElement conditionElem = fac.createOMElement(CONDITION, synNS);
                try {
                    evaluatorSerializer.serialize(conditionElem, evaluator);
                } catch (EvaluatorException ee) {
                }
                routeElem.addChild(conditionElem);
            }
        }

        if (String.valueOf(breakAfter) != null) {
            routeElem.addAttribute(BREAK_ROUTE, String.valueOf(breakAfter), null);
        }
        if (String.valueOf(isAsynchronous) != null) {
            routeElem.addAttribute(ASYNCHRONOUS, String.valueOf(isAsynchronous), null);
        }

        if (targetSeq != null) {
            OMElement targetElem = fac.createOMElement(TARGET, synNS);
            targetElem.addAttribute(TARGET_SEQ, targetSeq, null);
            routeElem.addChild(targetElem);
        }

        if (parent != null) {
            parent.addChild(routeElem);
        }

        return routeElem;
    }


    public String getConditionString() {
        String conditionStr = "";
        EvaluatorSerializer evaluatorSerializer =
                EvaluatorSerializerFinder.getInstance().getSerializer(evaluator.getName());
        if (evaluatorSerializer != null) {
            OMElement conditionElem = fac.createOMElement(CONDITION, synNS);
            try {
                evaluatorSerializer.serialize(conditionElem, evaluator);
            } catch (EvaluatorException ee) {
            }
            conditionStr = conditionElem.toString();
        }
        return conditionStr;
    }

    public void build(OMElement omElement) {
        OMAttribute breakAfterAttr = omElement.getAttribute(BREAK_AFTER_Q);
        OMAttribute asynchronousAttr = omElement.getAttribute(ASYNCHRONOUS_Q);

        if (breakAfterAttr != null && breakAfterAttr.getAttributeValue() != null) {
            breakAfter = Boolean.parseBoolean(breakAfterAttr.getAttributeValue());
        }
        if (asynchronousAttr != null && asynchronousAttr.getAttributeValue() != null) {
            isAsynchronous = Boolean.parseBoolean(asynchronousAttr.getAttributeValue());
        }

        OMElement conditionElem = omElement.getFirstChildWithName(CONDITION_Q);
        if (conditionElem != null) {
            try {
                evaluator = EvaluatorFactoryFinder.getInstance().getEvaluator(conditionElem.getFirstElement());
            } catch (EvaluatorException ee) {

            }
        }

        OMElement targetElem = omElement.getFirstChildWithName(TARGET_Q);
        if (targetElem != null) {
            OMAttribute targetSeqAttr = targetElem.getAttribute(SEQUENCE_Q);
            if (targetSeqAttr != null && targetSeqAttr.getAttributeValue() != null) {
                targetSeq = targetSeqAttr.getAttributeValue();
            }
        }
    }

    public String getTagLocalName() {
        return "conditionalRoute";
    }


    public boolean isBreakAfter() {
        return breakAfter;
    }

    public void setBreakAfter(boolean breakAfter) {
        this.breakAfter = breakAfter;
    }

    public boolean isAsynchronous() {
        return isAsynchronous;
    }

    public void setAsynchronous(boolean asynchronous) {
        isAsynchronous = asynchronous;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    public void setEvaluator(OMElement evaluatorElem) throws EvaluatorException{
        evaluator = EvaluatorFactoryFinder.getInstance().getEvaluator(evaluatorElem.getFirstElement());
    }


    public String getTargetSeq() {
        return targetSeq;
    }

    public void setTargetSeq(String targetSeq) {
        this.targetSeq = targetSeq;
    }
}
