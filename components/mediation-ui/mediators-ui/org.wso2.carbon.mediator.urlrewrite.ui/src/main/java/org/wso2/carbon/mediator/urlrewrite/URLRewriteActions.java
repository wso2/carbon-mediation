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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;

public class URLRewriteActions {
	private SynapseXPath xpath;
	private String value;
	private String fragment;
	private String regex;

	// Action types
	public static final String TYPE_APPEND = "append";
	public static final String TYPE_PREPEND = "prepend";
	public static final String TYPE_REPLACE = "replace";
	public static final String TYPE_REMOVE = "remove";
	public static final String TYPE_SET = "set";
	// default action
	private String type = TYPE_SET;

	// URL fragment types
	public static final String FRAGMENT_PROTOCOL = "protocol";
	public static final String FRAGMENT_HOST = "host";
	public static final String FRAGMENT_PORT = "port";
	public static final String FRAGMENT_PATH = "path";
	public static final String FRAGMENT_QUERY = "query";
	public static final String FRAGMENT_REF = "ref";
	public static final String FRAGMENT_USER = "user";
	public static final String FRAGMENT_FULL = "full";
	// default URL fragment i s"full"
	private String url = FRAGMENT_FULL;

	public void setAction(String type) {
		this.type = type;
	}

	public String getAction() {
		return type;
	}

	public void setURL(String url) {
		this.url = url;
	}

	public String getURL() {
		return url;
	}

	public SynapseXPath getXpath() {
		// There is a duplicate namespace comes, remove that.(That namespace is
		// added at two seperate places(ie: when we serialize @ mediator ui+
		// backend serialization)
		List<String> nsUriList = new ArrayList<String>();

		if (xpath != null) {
	        for (Object o : xpath.getNamespaces().keySet()) {
		        int i = 0;
		        String prefix = (String) o;
		        String uri = xpath.getNamespaceContext().translateNamespacePrefixToUri(prefix);
		        for (int j = 0; j <= i; j++) {
			        if (nsUriList.size() > 0) {
				        if (uri.equals(nsUriList.get(j).toString())) {
					        xpath.getNamespaces().remove(prefix);
				        } else {
					        nsUriList.add(uri);
				        }
			        } else {
				        nsUriList.add(uri);
			        }
		        }

		        i++;
	        }
        }
		return xpath;
	}

	public void setXpath(SynapseXPath xpath) {
		this.xpath = xpath;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getFragment() {
		return fragment;
	}

	public void setFragment(String fragment) {
		this.fragment = fragment;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public static List<URLRewriteActions> getAllActions(OMElement elem) {
		QName VALUE_Q = new QName("value");
		QName XPATH_Q = new QName("xpath");
		QName REGEX_Q = new QName("regex");
		QName FRAGMENT_Q = new QName("fragment");
		QName TYPE_Q = new QName("type");

		QName ACTION_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "action");
		

		List<URLRewriteActions> actionList = new ArrayList<URLRewriteActions>();
		Iterator itr = elem.getChildrenWithName(ACTION_Q);
	

		while (itr.hasNext()) {
			OMElement actionEle = (OMElement) itr.next();
			OMAttribute xpathAttr = actionEle.getAttribute(XPATH_Q);
			OMAttribute valueAttr = actionEle.getAttribute(VALUE_Q);
			OMAttribute regexAttr = actionEle.getAttribute(REGEX_Q);
			OMAttribute fragmentAttr = actionEle.getAttribute(FRAGMENT_Q);
			OMAttribute typeAttr = actionEle.getAttribute(TYPE_Q);
			
			URLRewriteActions urlRewriteActions = new URLRewriteActions();
			
			if (("set".equals(typeAttr.getAttributeValue())) ||
			    ("append".equals(typeAttr.getAttributeValue())) ||
			    ("prepend".equals(typeAttr.getAttributeValue())) ||
			    ("replace".equals(typeAttr.getAttributeValue()))) {
				if (valueAttr == null && xpathAttr == null) {
					String msg =
					             "The 'value'/'xpath' attribute is required for "
					                     + "the configuration for the 'set'/'append'"
					                     + "/'prepend' or repalce action";
					throw new MediatorException(msg);
				}

				if (valueAttr != null && valueAttr.getAttributeValue() != null) {
					urlRewriteActions.setValue(valueAttr.getAttributeValue());
				}
				if (xpathAttr != null && xpathAttr.getAttributeValue() != null) {
					try {
						urlRewriteActions.setXpath(SynapseXPathFactory.getSynapseXPath(actionEle,
						                                                               XPATH_Q));

					} catch (JaxenException e) {
						throw new MediatorException("Could not construct the" + " xpath");
					}
				}
			}

			if ("replace".equals(typeAttr.getAttributeValue())) {
				if (regexAttr == null) {
					String msg =
					             "The 'regex' attribute is required for the "
					                     + "configuration for the 'replace' action";
					throw new MediatorException(msg);
				}
				urlRewriteActions.setRegex(regexAttr.getAttributeValue());
			}
			if (typeAttr.getAttributeValue() != null) {
				urlRewriteActions.setAction(typeAttr.getAttributeValue());
			}

			if (fragmentAttr != null) {
				urlRewriteActions.setFragment(fragmentAttr.getAttributeValue());
			}

			// if a value is specified, use it, else look for an expression
			if (valueAttr != null) {
				urlRewriteActions.setValue(valueAttr.getAttributeValue());
			} else if (xpathAttr != null) {
				try {
					urlRewriteActions.setXpath(SynapseXPathFactory.getSynapseXPath(actionEle,
					                                                               XPATH_Q));

				} catch (JaxenException e) {
					String msg = "Invalid XPapth expression : " + xpathAttr.getAttributeValue();
					throw new MediatorException(msg);
				}
			}
			actionList.add(urlRewriteActions);
		}
		return actionList;
	}
}
