/**
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 **/
package org.wso2.carbon.mediator.aggregate;

import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.drop.DropMediator;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.ValueFactory;
import org.apache.synapse.config.xml.ValueSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;

public class AggregateMediator extends AbstractListMediator {
    protected static final QName CORELATE_ON_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "correlateOn");
    protected static final QName COMPLETE_CONDITION_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "completeCondition");
    protected static final QName MESSAGE_COUNT_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "messageCount");
    protected static final QName ON_COMPLETE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "onComplete");

    /** Attribute QName definitions **/
    private static final QName EXPRESSION_Q
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "expression");
    private static final QName TIMEOUT_Q
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "timeout");
    private static final QName MIN_Q
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "min");
    private static final QName MAX_Q
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "max");
    private static final QName SEQUENCE_Q
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "sequence");
    private static final QName ID_Q = new QName("id");

    private static final QName ENCLOSING_ELEMENT_PROPERTY
                    = new QName(XMLConfigConstants.NULL_NAMESPACE, "enclosingElementProperty");

    private long completionTimeoutSec = 0;
    private Value minMessagesToComplete;
    private Value maxMessagesToComplete;

    private SynapseXPath correlateExpression = null;
    private SynapseXPath aggregationExpression = null;

    private String onCompleteSequenceRef = null;
    private String id;

    public String getEnclosingElementPropertyName() {
        return enclosingElementPropertyName;
    }

    public void setEnclosingElementPropertyName(String enclosingElementPropertyName) {
        this.enclosingElementPropertyName = enclosingElementPropertyName;
    }

    private String enclosingElementPropertyName = null;
    
	public long getCompletionTimeoutSec() {
        return completionTimeoutSec;
    }

    public Value getMinMessagesToComplete() {
    	return minMessagesToComplete;
    }

	public void setMinMessagesToComplete(Value minMessagesToComplete) {
    	this.minMessagesToComplete = minMessagesToComplete;
    }

	public Value getMaxMessagesToComplete() {
    	return maxMessagesToComplete;
    }

	public void setMaxMessagesToComplete(Value maxMessagesToComplete) {
    	this.maxMessagesToComplete = maxMessagesToComplete;
    }

	public void setCompletionTimeoutSec(long completionTimeoutSec) {
        this.completionTimeoutSec = completionTimeoutSec;
    }

    public SynapseXPath getCorrelateExpression() {
        return correlateExpression;
    }

    public void setCorrelateExpression(SynapseXPath correlateExpression) {
        this.correlateExpression = correlateExpression;
    }

    public SynapseXPath getAggregationExpression() {
        return aggregationExpression;
    }

    public void setAggregationExpression(SynapseXPath aggregationExpression) {
        this.aggregationExpression = aggregationExpression;
    }

    public String getOnCompleteSequenceRef() {
        return onCompleteSequenceRef;
    }

    public void setOnCompleteSequenceRef(String onCompleteSequenceRef) {
        this.onCompleteSequenceRef = onCompleteSequenceRef;
    }

    public String getId() {
    		return id;
    }
    
    public void setId(String id) {
    		this.id = id;
    }
    
    public String getTagLocalName() {
        return "aggregate";
    }

    public OMElement serialize(OMElement parent) {
        OMElement aggregator = fac.createOMElement("aggregate", synNS);
        saveTracingState(aggregator, this);

        if (correlateExpression != null) {
            OMElement corelateOn = fac.createOMElement("correlateOn", synNS);
            SynapseXPathSerializer.serializeXPath(
                correlateExpression, corelateOn, "expression");
            aggregator.addChild(corelateOn);
        }

        OMElement completeCond = fac.createOMElement("completeCondition", synNS);
        if (completionTimeoutSec != 0) {
            completeCond.addAttribute("timeout",
                    Long.toString(completionTimeoutSec), nullNS);
        }

        OMElement messageCount = fac.createOMElement("messageCount", synNS);
        if (minMessagesToComplete != null) {
        	 OMElement min = fac.createOMElement("min", synNS);
        	 new ValueSerializer().serializeValue(minMessagesToComplete,"min", messageCount);
        }
        if (maxMessagesToComplete != null) {
        	 OMElement max = fac.createOMElement("min", synNS);
        	 new ValueSerializer().serializeValue(maxMessagesToComplete,"max", messageCount);
        }
        completeCond.addChild(messageCount);
        aggregator.addChild(completeCond);

        OMElement onCompleteElem = fac.createOMElement("onComplete", synNS);
        if (aggregationExpression != null) {
            SynapseXPathSerializer.serializeXPath(
                aggregationExpression, onCompleteElem, "expression");
        }
        if (onCompleteSequenceRef != null) {
            onCompleteElem.addAttribute("sequence", onCompleteSequenceRef, nullNS);
        } else if (getList().size() > 0) {
            serializeChildren(onCompleteElem, getList());
        }

        if (enclosingElementPropertyName != null) {
            onCompleteElem.addAttribute("enclosingElementProperty", enclosingElementPropertyName, nullNS);
        }

        aggregator.addChild(onCompleteElem);

        if(id != null) {
        	aggregator.addAttribute("id", id, nullNS);
        }
        
        if (parent != null) {
            parent.addChild(aggregator);
        }

        return aggregator;
    }

    public void build(OMElement elem) {
        OMElement corelateOn = elem.getFirstChildWithName(CORELATE_ON_Q);
        OMAttribute idAttr = elem.getAttribute(ID_Q);
        ValueFactory valueFactory = new ValueFactory();
        
        if(idAttr !=null) {
        		this.id = idAttr.getAttributeValue();
        }
        
        if (corelateOn != null) {
            OMAttribute corelateExpr = corelateOn.getAttribute(EXPRESSION_Q);
            if (corelateExpr != null) {
                try {
                    correlateExpression =
                        SynapseXPathFactory.getSynapseXPath(corelateOn, EXPRESSION_Q);
                } catch (JaxenException e) {
                    throw new MediatorException("Unable to load the corelate XPATH expression");
                }
            }
        }

        OMElement completeCond = elem.getFirstChildWithName(COMPLETE_CONDITION_Q);
        if (completeCond != null) {
            OMAttribute completeTimeout = completeCond.getAttribute(TIMEOUT_Q);
            if (completeTimeout != null) {
                completionTimeoutSec =
                        Long.parseLong(completeTimeout.getAttributeValue());
            }

            OMElement messageCount = completeCond.getFirstChildWithName(MESSAGE_COUNT_Q);
            if (messageCount != null) {
               OMAttribute minExpr = messageCount.getAttribute(MIN_Q);
                if (minExpr != null) {
                	try {
                		Value min = valueFactory.createValue("min", messageCount);
                		this.minMessagesToComplete = min;
                    } catch (Exception e) {
                    	e.printStackTrace();
                        throw new MediatorException("Unable to load the corelate XPATH expression");
                    }
                }

                OMAttribute maxExpr = messageCount.getAttribute(MAX_Q);
                if (maxExpr != null) {
                	try {
                		Value max = valueFactory.createValue("max", messageCount);
                		this.maxMessagesToComplete = max;
                    } catch (Exception e) {
                    	e.printStackTrace();
                        throw new MediatorException("Unable to load the corelate XPATH expression");
                    }
                }
            }
        }

        OMElement onComplete = elem.getFirstChildWithName(ON_COMPLETE_Q);
        if (onComplete != null) {

            OMAttribute aggregateExpr = onComplete.getAttribute(EXPRESSION_Q);
            if (aggregateExpr != null) {
                try {
                    aggregationExpression =
                        SynapseXPathFactory.getSynapseXPath(onComplete, EXPRESSION_Q);
                } catch (JaxenException e) {
                    throw new MediatorException("Unable to load the aggregating XPATH");
                }
            }

            OMAttribute enclosingElementPropertyName = onComplete.getAttribute(ENCLOSING_ELEMENT_PROPERTY);
            if (enclosingElementPropertyName != null) {
                this.enclosingElementPropertyName = enclosingElementPropertyName.getAttributeValue();
            }
           
            OMAttribute onCompleteSequence = onComplete.getAttribute(SEQUENCE_Q);
            if (onCompleteSequence != null) {
                onCompleteSequenceRef = onCompleteSequence.getAttributeValue();
            } else if (onComplete.getFirstElement() != null) {
                addChildren(onComplete, this);
            } else {
                addChild(new DropMediator());                
            }
        }                        
    }
}
