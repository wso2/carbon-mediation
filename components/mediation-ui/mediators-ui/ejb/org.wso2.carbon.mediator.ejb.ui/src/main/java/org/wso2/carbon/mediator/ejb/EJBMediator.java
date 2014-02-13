/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mediator.ejb;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.ValueFactory;
import org.apache.synapse.config.xml.ValueSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.bean.BeanConstants;
import org.apache.synapse.mediators.bean.enterprise.EJBConstants;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

public class EJBMediator extends AbstractMediator {

	private static final QName EJB_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "ejb");
	private static final QName ARGS_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "args");
	private static final QName ARG_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "arg");

	private static final QName ATT_CLASS = new QName("class");
	private static final QName ATT_BEANSTALK = new QName("beanstalk");
	private static final QName ATT_METHOD = new QName("method");
	private static final QName ATT_TARGET = new QName("target");
	private static final QName ATT_JNDI_NAME = new QName("jndiName");
	private static final QName ATT_STATEFUL = new QName("stateful");
	private static final QName ATT_ID = new QName("id");
	private static final QName ATT_REMOVE = new QName("remove");

	private String clazz;
	private String beanstalk;
	private String method;
	private String target;
	private String jndiName;
	private Value id;
	private Boolean stateful;
	private Boolean remove;
	private List<Value> arguments = new ArrayList<Value>();

	public String getTagLocalName() {
		return "ejb";
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getBeanstalk() {
		return beanstalk;
	}

	public void setBeanstalk(String beanstalk) {
		this.beanstalk = beanstalk;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getJndiName() {
		return jndiName;
	}

	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	public Value getId() {
		return id;
	}

	public void setId(Value id) {
		this.id = id;
	}

	public Boolean getStateful() {
		return stateful;
	}

	public void setStateful(Boolean stateful) {
		this.stateful = stateful;
	}

	public List<Value> getArguments() {
		return arguments;
	}

	public void setArguments(List<Value> arguments) {
		this.arguments = arguments;
	}

	public void addArguments(Value value) {
		this.arguments.add(value);
	}

	public Boolean getRemove() {
		return remove;
	}

	public void setRemove(Boolean remove) {
		this.remove = remove;
	}

	public OMElement serialize(OMElement parent) {
		OMElement ejbElem = fac.createOMElement(EJB_Q);
		
				
		if (clazz != null && !clazz.isEmpty()) {
			OMAttribute clazzAtt = fac.createOMAttribute("class", nullNS, clazz);
			ejbElem.addAttribute(clazzAtt);
		}
		if (beanstalk != null && !beanstalk.isEmpty()) {
			OMAttribute beanstalkAtt = fac.createOMAttribute("beanstalk", nullNS, beanstalk);
			ejbElem.addAttribute(beanstalkAtt);
		}
		if (method != null && !method.isEmpty()) {
			OMAttribute methodAtt = fac.createOMAttribute("method", nullNS, method);
			ejbElem.addAttribute(methodAtt);
		}
		if (target != null && !target.isEmpty()) {
			OMAttribute targetAtt = fac.createOMAttribute("target", nullNS, target);
			ejbElem.addAttribute(targetAtt);
		}
		if (jndiName != null && !jndiName.isEmpty()) {
			OMAttribute jndiNameAtt = fac.createOMAttribute("jndiName", nullNS, jndiName);
			ejbElem.addAttribute(jndiNameAtt);
		}

		if (id != null) {
			new ValueSerializer().serializeValue(id, EJBConstants.BEAN_ID, ejbElem);

		}

		if (stateful != null) {
			OMAttribute statefulAttr =
			                           fac.createOMAttribute("stateful", nullNS,
			                                                 stateful.toString());
			ejbElem.addAttribute(statefulAttr);
		}

		if (remove != null) {
			OMAttribute removeAttr = fac.createOMAttribute("remove", nullNS, remove.toString());
			ejbElem.addAttribute(removeAttr);
		}

		if (arguments != null && arguments.size() > 0) {
			OMElement argumentsElem = fac.createOMElement(EJBConstants.ARGS, synNS);
			for (Value arg : arguments) {
				OMElement argElem = fac.createOMElement(EJBConstants.ARG, synNS);
				new ValueSerializer().serializeValue(arg, BeanConstants.VALUE, argElem);
				argumentsElem.addChild(argElem);
			}
			ejbElem.addChild(argumentsElem);
		}
		

		try {
	        this.validateEJBMethod();
        } catch (MediatorException e) {
	        throw e;
        }

        

		if (parent != null) {
			parent.addChild(ejbElem);
		}

		return ejbElem;
	}

	
    /**
     * Validating EJB method parameters
     *      * 
     * @throws Exception
     */
    private void validateEJBMethod() throws MediatorException {
	    try {
			if (this.clazz != null) {
				//first validate the class existence..
				Class.forName(this.clazz.trim());
			}

			if (this.clazz != null && this.method != null) {
				//Second checking the method definition existence
				Class clazz = Class.forName(this.clazz.trim());
				if (clazz != null) {
					boolean foundMethod = false;
					for (Method method : clazz.getMethods()){
						if (this.method.equals(method.getName())) {
							foundMethod = true;
						}
					}
					if(!foundMethod){
						throw new MediatorException("Couldn't load method definition "+method+" name under the class " + this.clazz);
					}

				}
			}
			
			if(this.clazz != null && this.method != null && this.arguments != null){
				Class clazz = Class.forName(this.clazz);
				if (clazz != null) {
					boolean argumetnsOk = false;
					for (Method method : clazz.getMethods()){
						if (this.method.equals(method.getName())) {
							if (method.getName().equals(this.method) && method.getParameterTypes().length == this.arguments.size()) {
								argumetnsOk =  true;
							}
						}
					}
					if(!argumetnsOk){
						throw new MediatorException("Invalid number of argments provided for method "+ this.method);
					}
				}
			}
			
		} catch (ClassNotFoundException e) {
			throw new MediatorException("Couldn't load class name " + this.clazz);
		}
    }

	public void build(OMElement elem) {
		// after successfully creating the mediator
		// set its common attributes such as tracing etc
		processAuditStatus(this, elem);
		ValueFactory valueFactory = new ValueFactory();

		OMAttribute clazz = elem.getAttribute(ATT_CLASS);
		if (clazz != null) {
			this.clazz = clazz.getAttributeValue();
		}
		OMAttribute beanstalk = elem.getAttribute(ATT_BEANSTALK);
		if (beanstalk != null) {
			this.beanstalk = beanstalk.getAttributeValue();
		}
		OMAttribute stateful = elem.getAttribute(ATT_STATEFUL);
		if (stateful != null) {
			this.stateful = new Boolean(stateful.getAttributeValue());
		}

		OMAttribute remove = elem.getAttribute(ATT_REMOVE);
		if (remove != null) {
			this.remove = new Boolean(remove.getAttributeValue());
		}

		OMAttribute jndiName = elem.getAttribute(ATT_JNDI_NAME);
		if (jndiName != null) {
			this.jndiName = jndiName.getAttributeValue();
		}
		OMAttribute target = elem.getAttribute(ATT_TARGET);
		if (target != null) {
			this.target = target.getAttributeValue();
		}
		OMAttribute method = elem.getAttribute(ATT_METHOD);
		if (method != null) {
			this.method = method.getAttributeValue();
		}
		OMAttribute id = elem.getAttribute(ATT_ID);
		if (id != null) {
			Value beanId = valueFactory.createValue("id", elem);
			this.id = beanId;
		}

		Iterator argsitr = elem.getChildrenWithName(ARGS_Q);

		if (argsitr != null) {
			List<Value> args = new ArrayList<Value>();
			while (argsitr.hasNext()) {
				OMElement argsElm = (OMElement) argsitr.next();
				if (argsElm != null) {
					Iterator argItr = argsElm.getChildrenWithName(ARG_Q);
					if (argItr != null) {
						while (argItr.hasNext()) {
							OMElement argElm = (OMElement) argItr.next();
							if (argElm != null) {
								Value value = valueFactory.createValue("value", argElm);
								args.add(value);
							}
						}
					}
				}
			}
			this.arguments.addAll(args);
		}

	}

	private void handleException(String msg) {
		LogFactory.getLog(this.getClass()).error(msg);
		throw new SynapseException(msg);
	}
}
