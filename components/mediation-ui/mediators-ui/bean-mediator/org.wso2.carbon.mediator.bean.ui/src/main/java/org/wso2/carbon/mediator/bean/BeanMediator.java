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
package org.wso2.carbon.mediator.bean;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.ValueFactory;
import org.apache.synapse.config.xml.ValueSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.bean.BeanConstants;
import org.apache.synapse.mediators.bean.enterprise.EJBConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.util.MediatorProperty;


import javax.swing.text.html.HTMLDocument.HTMLReader.PreAction;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//<ejb class="org.test.ejb.StoreRegister" beanstalk="jack" method="getClosestStore" target="loc_id" jndiName="StoreRegsiterBean/remote">
//<args>
//    <arg value="{get-property('loc')}"/>
//</args>
//</ejb>
public class BeanMediator extends AbstractMediator {

	private static final QName BEAN_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "bean");
	
	private static final QName ATT_CLASS = new QName("class");
	private static final QName ATT_ACTION = new QName("action");
	private static final QName ATT_VAR = new QName("var");
	private static final QName ATT_PROPERTY = new QName("property");
	private static final QName ATT_VALUE = new QName("value");
	private static final QName ATT_TARGET = new QName("target");
	
	private String clazz;
	private String action;
	private String var;
	private String property;
	private Value value;
	private Value target;
	
	
	public String getTagLocalName() {
		return "bean";
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}
	
	public Value getTarget() {
		return target;
	}

	public void setTarget(Value target) {
		this.target = target;
	}

	public OMElement serialize(OMElement parent) {
		OMElement beanElem = fac.createOMElement(BEAN_Q);
		if(clazz != null && !clazz.isEmpty()) {
            OMAttribute clazzAtt = fac.createOMAttribute(BeanConstants.CLASS , nullNS , clazz);
            beanElem.addAttribute(clazzAtt);
        }if(action != null && !action.isEmpty()) {
            OMAttribute beanstalkAtt = fac.createOMAttribute(BeanConstants.ACTION , nullNS , action);
            beanElem.addAttribute(beanstalkAtt);
        }
        if(var != null && !var.isEmpty()) {
            OMAttribute methodAtt = fac.createOMAttribute(BeanConstants.VAR , nullNS , var);
            beanElem.addAttribute(methodAtt);
        }
        if(property != null && !property.isEmpty()) {
            OMAttribute targetAtt = fac.createOMAttribute(BeanConstants.PROPERTY, nullNS , property);
            beanElem.addAttribute(targetAtt);
        }
		if(value != null) {
        	new ValueSerializer().serializeValue(value, BeanConstants.VALUE, beanElem);
		
        }
		
		if(target != null) {
        	new ValueSerializer().serializeValue(target, BeanConstants.TARGET, beanElem);
		
        }
		
				
		 if (parent != null) {
	            parent.addChild(beanElem);
	      }
		 
		return beanElem;
	}

	public void build(OMElement elem) {
		// after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);
        ValueFactory valueFactory = new ValueFactory();
        
        OMAttribute clazz = elem.getAttribute(ATT_CLASS);
        if(clazz !=null){
        	this.clazz = clazz.getAttributeValue();
        }
        OMAttribute actionElm = elem.getAttribute(ATT_ACTION);
        if(actionElm != null){
        	this.action= actionElm.getAttributeValue();
        }
        OMAttribute varElm = elem.getAttribute(ATT_VAR);
        if(varElm != null){
        	this.var = varElm.getAttributeValue();
        }
        
        OMAttribute propertyElm = elem.getAttribute(ATT_PROPERTY);
        if(propertyElm != null){
        	this.property = propertyElm.getAttributeValue();
        }
        
        OMAttribute value = elem.getAttribute(ATT_VALUE);
        if(value != null){
        	Value beanId = valueFactory.createValue(BeanConstants.VALUE, elem);
        	this.value=beanId;
        }
        
        OMAttribute targetElm = elem.getAttribute(ATT_TARGET);
        if(targetElm != null){
        	Value target = valueFactory.createValue(BeanConstants.TARGET, elem);
        	this.target=target;
        }
  	}

	private void handleException(String msg) {
		LogFactory.getLog(this.getClass()).error(msg);
		throw new SynapseException(msg);
	}
}
