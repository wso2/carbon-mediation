/**
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 **/
package org.wso2.carbon.mediator.urlrewrite;


import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.apache.synapse.commons.evaluators.Evaluator;
import org.apache.synapse.commons.evaluators.EvaluatorException;
import org.apache.synapse.commons.evaluators.config.EvaluatorFactoryFinder;
import org.apache.synapse.commons.evaluators.config.EvaluatorSerializer;
import org.apache.synapse.commons.evaluators.config.EvaluatorSerializerFinder;

public class URLRulesMediator extends AbstractMediator {

	private String condition;
	private Evaluator evaluator;
	 
	private final List<URLRewriteActions> actions = new ArrayList<URLRewriteActions>();

	public String getTagLocalName() {
		return "rewriterule";
	}

	public String getCondition() {		
	        EvaluatorSerializer evaluatorSerializer =
	                EvaluatorSerializerFinder.getInstance().getSerializer(evaluator.getName());
	        if (evaluatorSerializer != null) {
	            OMElement conditionElem = fac.createOMElement("condition", synNS);
	            try {
	                evaluatorSerializer.serialize(conditionElem, evaluator);
	            } catch (EvaluatorException e) {
					throw new MediatorException("Invalid condition"+e);
	            }
	            condition = conditionElem.getFirstOMChild().toString();
	        }
	        return condition;
	}


	public Evaluator getEvaluator() {
		return evaluator;
	}

	public void setEvaluator(Evaluator evaluator) {
		this.evaluator = evaluator;
	}

	public void setEvaluator() {
		this.evaluator = null;
	}
	
	public void setEvaluator(OMElement evaluatorElem) throws EvaluatorException {
		evaluator =
		            EvaluatorFactoryFinder.getInstance()
		                                  .getEvaluator(evaluatorElem.getFirstElement());
		
	}
	    
	public List<URLRewriteActions> getActions() {
		return actions;
	}

	public void addActions(URLRewriteActions urlRewriteActions) {
		actions.add(urlRewriteActions);
	}

	public void addAllActions(List<URLRewriteActions> list) {
		actions.addAll(list);
	}

	/**
	 * Constructing mediator conf using user input
	 * 
	 * @param parent
	 */
	public OMElement serialize(OMElement parent) {
		OMElement urlRule = fac.createOMElement("rewriterule", synNS);
		saveTracingState(urlRule, this);

		// add condition block, its optional
		  if (evaluator != null) {
	            EvaluatorSerializer evaluatorSerializer =
	                    EvaluatorSerializerFinder.getInstance().getSerializer(evaluator.getName());
	            if (evaluatorSerializer != null) {
	            	OMElement conditionElement = fac.createOMElement("condition", synNS);
	                try {
	                    evaluatorSerializer.serialize(conditionElement, evaluator);
	                } catch (EvaluatorException ee) {
	                }
	                urlRule.addChild(conditionElement);
	            }
	        }
		
		  serializeAction(urlRule);

		if (parent != null) {
			parent.addChild(urlRule);
		}
		return urlRule;
	}

	/**
	 * Serilize the "action" block
	 * 
	 * @param parent
	 */
	private void serializeAction(OMElement parent) {

		for (URLRewriteActions rewriteAction : actions) {

			String type = rewriteAction.getAction();
			String value = rewriteAction.getValue();
			SynapseXPath xpath = rewriteAction.getXpath();
			String regex = rewriteAction.getRegex();
			String fragment = rewriteAction.getFragment();

			// add action block
			OMElement actionElement = fac.createOMElement("action", synNS);

			// 'set', 'append' or 'prepend' needs 'value'
			// attribute or the
			// 'xpath' attribute.
			if (URLRewriteActions.TYPE_APPEND.equals(type) ||
			    URLRewriteActions.TYPE_PREPEND.equals(type) ||
			    URLRewriteActions.TYPE_SET.equals(type) ||
			    URLRewriteActions.TYPE_REPLACE.equals(type)) {
				if ((value == null && xpath == null) || (value != null && value.isEmpty())) {
					throw new MediatorException( "At URL rules definition if the action sets as (Set or Append or Prepend),"
					                                    + "then repalced actions required "
					                                    + "value or xpath expression");
				}
				if (xpath != null) {
					SynapseXPathSerializer.serializeXPath(xpath, actionElement, "xpath");
				}
				if (value != null) {
					actionElement.addAttribute(fac.createOMAttribute("value", nullNS, value));
				}
			}
			// 'regex' attribute must be specified
			if (URLRewriteActions.TYPE_REPLACE.equals(type)) {
				if (regex != null) {
					actionElement.addAttribute(fac.createOMAttribute("regex", nullNS, regex));
				} else {
					throw new MediatorException("Replace action needs " + "'regex'attribute to"
					                            + " be specified");
				}
			}
			if (type != null) {
				actionElement.addAttribute(fac.createOMAttribute("type", nullNS, type));
			}
			// specify the URL fragment
			if (fragment != URLRewriteActions.FRAGMENT_FULL) {
				actionElement.addAttribute(fac.createOMAttribute("fragment", nullNS,
				                                                 rewriteAction.getFragment()));
			}
			parent.addChild(actionElement);
		}
	}

	/**
	 * Building esb mediator object
	 */
	public void build(OMElement elem) {

		QName CONDITION_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,"condition");
		OMElement condEle = elem.getFirstChildWithName(CONDITION_Q);
			
		//clear the list then add elements
		if (getActions() != null) {
			getActions().clear(); //clear at initial
		}
		addAllActions(URLRewriteActions.getAllActions(elem));
		
		if (condEle != null) {
			try {
				evaluator =  EvaluatorFactoryFinder.getInstance()
				                                  .getEvaluator(condEle.getFirstElement());
			} catch (EvaluatorException ee) {
				String msg="Issue in the condition :";
				throw new MediatorException(msg + ee);
			}
		}

		// after successfully creating the mediator
		// set its common attributes such as tracing etc
		processAuditStatus(this, elem);

	}
}
