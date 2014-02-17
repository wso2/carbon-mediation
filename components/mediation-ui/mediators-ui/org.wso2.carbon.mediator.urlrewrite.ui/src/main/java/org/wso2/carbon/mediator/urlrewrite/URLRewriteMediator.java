/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.wso2.carbon.mediator.urlrewrite;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;

/**
 * Definig the configuration params
 * 
 */
public class URLRewriteMediator extends AbstractListMediator {

		private String inProperty;
		private String outProperty;	

		public String getTagLocalName() {
				return "rewrite";
		}

		public String getInProperty() {
				return inProperty;
		}

		public void setInProperty(String inProperty) {
				this.inProperty = inProperty;
		}

		public String getOutProperty() {
				return outProperty;
		}

		public void setOutProperty(String outProperty) {
				this.outProperty = outProperty;
		}

		/**
		 * Constructing mediator conf using user input
		 * 
		 * @param parent
		 */
		public OMElement serialize(OMElement parent) {
				OMElement urlRewrite = fac.createOMElement("rewrite", synNS);
				saveTracingState(urlRewrite, this);

				if (inProperty != null) {
						urlRewrite.addAttribute(fac.createOMAttribute("inProperty", nullNS,
						                                              inProperty));
				}
				if (outProperty != null) {
						urlRewrite.addAttribute(fac.createOMAttribute("outProperty", nullNS,
						                                              outProperty));
				}

				serializeChildren(urlRewrite, getList());

				if (parent != null) {
						parent.addChild(urlRewrite);
				}

				return urlRewrite;
		}

		/**
		 * Building esb mediator object
		 */
		public void build(OMElement elem) {

				if (getList() != null) {
						getList().clear();
				}

				QName ATT_IN_PROPERTY = new QName("inProperty");
				QName ATT_OUT_PROPERTY = new QName("outProperty");

				OMAttribute inPropertyAttr = elem.getAttribute(ATT_IN_PROPERTY);
				OMAttribute outPropertyAttr = elem.getAttribute(ATT_OUT_PROPERTY);

				if (inPropertyAttr != null) {
						this.inProperty = inPropertyAttr.getAttributeValue();
				}
				if (outPropertyAttr != null) {
						this.outProperty = outPropertyAttr.getAttributeValue();
				}

				// after successfully creating the mediator
				// set its common attributes such as tracing etc
				processAuditStatus(this, elem);
				addChildren(elem, this);
		}

}
