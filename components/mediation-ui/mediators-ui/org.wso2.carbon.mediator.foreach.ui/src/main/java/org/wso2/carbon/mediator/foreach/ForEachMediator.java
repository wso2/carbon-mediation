/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediator.foreach;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.wso2.carbon.mediator.service.ui.Mediator;
import org.wso2.carbon.mediator.target.TargetMediator;

public class ForEachMediator extends AbstractListMediator {

	private SynapseXPath expression = null;

	public ForEachMediator() {

	}

	public SynapseXPath getExpression() {
		return expression;
	}

	public void setExpression(SynapseXPath expression) {
		this.expression = expression;
	}

	public String getTagLocalName() {
		return "foreach";
	}

	public OMElement serialize(OMElement parent) {
		OMElement itrElem = fac.createOMElement("foreach", synNS);
		saveTracingState(itrElem, this);

		if (expression != null) {
			SynapseXPathSerializer.serializeXPath(expression, itrElem, "expression");
		} else {
			throw new MediatorException("Missing expression of the ForEach which is required.");
		}

		serializeChildren(itrElem, getList());
		
		// attach the serialized element to the parent if specified
		if (parent != null) {
			parent.addChild(itrElem);
		}

		return itrElem;
	}

	public void build(OMElement elem) {

		processAuditStatus(this, elem);

		OMAttribute expression = elem.getAttribute(ATT_EXPRN);
		if (expression != null) {
			try {
				this.expression = SynapseXPathFactory.getSynapseXPath(elem, ATT_EXPRN);
			} catch (JaxenException e) {
				throw new MediatorException("Unable to build the ForEach Mediator. " +
				                            "Invalid XPath " +
				                            expression.getAttributeValue());
			}
		} else {
			throw new MediatorException(
			                            "XPath expression is required "
			                                    + "for a ForEach Mediator under the \"expression\" attribute");
		}
		OMElement targetElement = elem.getFirstChildWithName(TARGET_Q);
		if (targetElement != null) {
			addChildren(elem, this);
		} else {
			throw new MediatorException(
			                            "Target for an foreach mediator is required :: missing target");
		}

	}

}
