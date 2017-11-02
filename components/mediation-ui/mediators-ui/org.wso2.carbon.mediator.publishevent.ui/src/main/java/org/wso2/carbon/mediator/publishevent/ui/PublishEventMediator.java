/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediator.publishevent.ui;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.xpath.operations.Bool;
import org.jaxen.JaxenException;
import org.omg.CORBA.TIMEOUT;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PublishEventMediator extends AbstractMediator {
	public static final QName EVENT_SINK_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "eventSink");
	public static final QName STREAM_NAME_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "streamName");
	public static final QName STREAM_VERSION_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "streamVersion");
	public static final QName ASYNC_Q = new QName( "async");
	public static final QName ASYNC_TIMEOUT_Q = new QName("timeout");
	public static final QName ATTRIBUTES_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "attributes");
	public static final QName ATTRIBUTE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "attribute");
	public static final QName META_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "meta");
	public static final QName CORRELATION_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "correlation");
	public static final QName ARBITRARY_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "arbitrary");
	public static final QName PAYLOAD_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "payload");
	public static final QName TYPE_Q = new QName("type");
	public static final QName DEFAULT_Q = new QName("defaultValue");
	private String streamName;
	private String streamVersion;
	private String eventSink;
	private String isAsync = "true";
	private String timeout;
	private List<Property> metaProperties = new ArrayList<Property>();
	private List<Property> correlationProperties = new ArrayList<Property>();
	private List<Property> payloadProperties = new ArrayList<Property>();
	private List<Property> arbitraryProperties = new ArrayList<Property>();

	/**
	 *
	 * @return local name of mediator
	 */
	public String getTagLocalName() {
		return "publishEvent";
	}

	/**
	 * Creates XML representation of the mediator as an OMElement
	 *
	 * @param parent OMElement which take child as created OMElement
	 *
	 */
	public OMElement serialize(OMElement parent) {

		OMElement publishEventElement = fac.createOMElement("publishEvent", synNS);
		saveTracingState(publishEventElement, this);

		if (isAsync() != null) {
			OMAttribute isAsyncAttribute = fac
					.createOMAttribute(PublishEventMediator.ASYNC_Q.getLocalPart(), nullNS, this.isAsync());
			publishEventElement.addAttribute(isAsyncAttribute);
		}

		if (getTimeout() != null) {
			OMAttribute asyncTimeout = fac
					.createOMAttribute(PublishEventMediator.ASYNC_TIMEOUT_Q.getLocalPart(), nullNS, this.getTimeout());
			publishEventElement.addAttribute(asyncTimeout);
		}

		if (streamName != null && !streamName.equals("")) {
			OMElement streamNameElement = fac.createOMElement(PublishEventMediator.STREAM_NAME_Q.getLocalPart(), synNS);
			streamNameElement.setText(this.getStreamName());
			publishEventElement.addChild(streamNameElement);

		} else {
			throw new MediatorException("Stream name not specified");
		}
		if (streamVersion != null && !streamName.equals("")) {

			OMElement streamVersionElement =
					fac.createOMElement(PublishEventMediator.STREAM_VERSION_Q.getLocalPart(), synNS);
			streamVersionElement.setText(this.getStreamVersion());
			publishEventElement.addChild(streamVersionElement);
		} else {
			throw new MediatorException("Stream version not specified");
		}

		OMElement eventSinkElement = fac.createOMElement(PublishEventMediator.EVENT_SINK_Q.getLocalPart(), synNS);
		eventSinkElement.setText(this.getEventSink());
		publishEventElement.addChild(eventSinkElement);

		OMElement streamAttributesElement =
				fac.createOMElement(PublishEventMediator.ATTRIBUTES_Q.getLocalPart(), synNS);

		OMElement metaAttributesElement = fac.createOMElement(PublishEventMediator.META_Q.getLocalPart(), synNS);
		for (Property property : this.getMetaProperties()) {
			metaAttributesElement.addChild(createElementForProperty(property));
		}
		streamAttributesElement.addChild(metaAttributesElement);

		OMElement correlationAttributesElement =
				fac.createOMElement(PublishEventMediator.CORRELATION_Q.getLocalPart(), synNS);
		for (Property property : this.getCorrelationProperties()) {
			correlationAttributesElement.addChild(createElementForProperty(property));
		}
		streamAttributesElement.addChild(correlationAttributesElement);

		OMElement payloadAttributesElement = fac.createOMElement(PublishEventMediator.PAYLOAD_Q.getLocalPart(), synNS);
		for (Property property : this.getPayloadProperties()) {
			payloadAttributesElement.addChild(createElementForProperty(property));
		}
		streamAttributesElement.addChild(payloadAttributesElement);

		OMElement arbitrarynAttributesElement =
				fac.createOMElement(PublishEventMediator.ARBITRARY_Q.getLocalPart(), synNS);
		for (Property property : this.getArbitraryProperties()) {
			arbitrarynAttributesElement.addChild(createElementForProperty(property));
		}
		streamAttributesElement.addChild(arbitrarynAttributesElement);

		publishEventElement.addChild(streamAttributesElement);

		if (parent != null) {
			parent.addChild(publishEventElement);
		}

		return publishEventElement;
	}

	/**
	 * Creates the publishEvent mediator with given configuration XML as OMElement
	 *
	 * @param elem OMElement to be converted to publishEvent Mediator Object.
	 */
	public void build(OMElement elem) {
		String async = elem.getAttributeValue(ASYNC_Q);
		this.setIsAsync(async);

		if (Boolean.parseBoolean(async)) {
			this.setTimeout(elem.getAttributeValue(ASYNC_TIMEOUT_Q));
		}

		OMElement streamName = elem.getFirstChildWithName(STREAM_NAME_Q);
		if (streamName == null) {
			throw new SynapseException(STREAM_NAME_Q.getLocalPart() + " element missing");
		}
		this.setStreamName(streamName.getText());

		OMElement streamVersion = elem.getFirstChildWithName(STREAM_VERSION_Q);
		if (streamVersion == null) {
			throw new SynapseException(STREAM_VERSION_Q.getLocalPart() + " element missing");
		}
		this.setStreamVersion(streamVersion.getText());

		OMElement eventSinkName = elem.getFirstChildWithName(EVENT_SINK_Q);
		if (eventSinkName == null) {
			throw new SynapseException(EVENT_SINK_Q.getLocalPart() + " element missing");
		}
		this.setEventSink(eventSinkName.getText());

		OMElement attributes = elem.getFirstChildWithName(ATTRIBUTES_Q);
		if (attributes != null) {
			OMElement meta = attributes.getFirstChildWithName(META_Q);
			if (meta != null) {
				Iterator<OMElement> iterator = meta.getChildrenWithName(ATTRIBUTE_Q);
				List<Property> propertyList = generatePropertyList(iterator);
				this.setMetaProperties(propertyList);
			}
			OMElement correlation = attributes.getFirstChildWithName(CORRELATION_Q);
			if (correlation != null) {
				Iterator<OMElement> iterator = correlation.getChildrenWithName(ATTRIBUTE_Q);
				List<Property> propertyList = generatePropertyList(iterator);
				this.setCorrelationProperties(propertyList);
			}
			OMElement payload = attributes.getFirstChildWithName(PAYLOAD_Q);
			if (payload != null) {
				Iterator<OMElement> iterator = payload.getChildrenWithName(ATTRIBUTE_Q);
				List<Property> propertyList = generatePropertyList(iterator);
				this.setPayloadProperties(propertyList);
			}
			OMElement arbitrary = attributes.getFirstChildWithName(ARBITRARY_Q);
			if (arbitrary != null) {
				Iterator<OMElement> iterator = arbitrary.getChildrenWithName(ATTRIBUTE_Q);
				List<Property> propertyList = generatePropertyList(iterator);
				this.setArbitraryProperties(propertyList);
			}
		} else {
			throw new SynapseException(ATTRIBUTES_Q.getLocalPart() + " attribute missing");
		}

	}

	/**
	 * Creates the XML representation of the given mediator property
	 *
	 * @param property Property for which the XML representation should be created
	 * @return XML representation of the property as an OMElement
	 */
	private OMElement createElementForProperty(Property property) {
		OMElement attributeElement = fac.createOMElement(PublishEventMediator.ATTRIBUTE_Q.getLocalPart(), synNS);
		attributeElement.addAttribute(
				fac.createOMAttribute(PublishEventMediator.getNameAttributeQ().getLocalPart(), nullNS,
				                      property.getName()));
		attributeElement.addAttribute(
				fac.createOMAttribute(PublishEventMediator.TYPE_Q.getLocalPart(), nullNS, property.getType()));
		attributeElement.addAttribute(fac.createOMAttribute(PublishEventMediator.DEFAULT_Q.getLocalPart(), nullNS,
		                                                    property.getDefaultValue()));

		if (property.getExpression() != null) {
			SynapseXPathSerializer.serializeXPath(property.getExpression(), attributeElement,
			                                      PublishEventMediator.getExpressionAttributeQ().getLocalPart());
		} else {
			attributeElement.addAttribute(
					fac.createOMAttribute(PublishEventMediator.getValueAttributeQ().getLocalPart(), nullNS,
					                      property.getValue()));
		}
		return attributeElement;
	}


	private List<Property> generatePropertyList(Iterator<OMElement> iterator) {
		List<Property> propertyList = new ArrayList<Property>();
		while (iterator.hasNext()) {
			OMElement element = iterator.next();
			OMAttribute nameAttr = element.getAttribute(ATT_NAME);
			if (nameAttr == null) {
				throw new SynapseException(ATT_NAME.getLocalPart() + " attribute missing in " + element.getLocalName());
			}
			OMAttribute typeAttr = element.getAttribute(TYPE_Q);
			if (typeAttr == null) {
				throw new SynapseException(TYPE_Q.getLocalPart() + " attribute missing in " + element.getLocalName());
			}
			OMAttribute valueAttr = element.getAttribute(ATT_VALUE);
			OMAttribute expressionAttr = element.getAttribute(ATT_EXPRN);
			if (valueAttr != null && expressionAttr != null) {
				throw new SynapseException(
						element.getLocalName() + " element can either have \"" + ATT_VALUE.getLocalPart() +
						"\" or \"" + ATT_EXPRN.getLocalPart() + "\" attribute but not both");
			}

			if (valueAttr == null && expressionAttr == null) {
				throw new SynapseException(
						element.getLocalName() + " element must have either \"" + ATT_VALUE.getLocalPart() +
						"\" or \"" + ATT_EXPRN.getLocalPart() + "\" attribute");
			}

			Property property = new Property();
			property.setName(nameAttr.getAttributeValue());
			property.setType(typeAttr.getAttributeValue());
			if (valueAttr != null) {
				property.setValue(valueAttr.getAttributeValue());
			} else {
				try {
					//TODO : get it to local variable and append to exception
					property.setExpression(SynapseXPathFactory.getSynapseXPath(element, ATT_EXPRN));
				} catch (JaxenException e) {
					throw new SynapseException("Invalid expression attribute in " + element.getLocalName() +". " +
					                           "expression : "+element.getAttribute(ATT_EXPRN)
					                                                  .getAttributeValue(), e);
				}
			}

			OMAttribute defaultAtr = element.getAttribute(DEFAULT_Q);
			if (defaultAtr != null) {
				property.setDefaultValue(defaultAtr.getAttributeValue());
			}

			propertyList.add(property);
		}
		return propertyList;
	}

	/**
	 * Makes Lists are Empty
	 *
	 * @param type List type to be made empty
	 */
	public void clearList(String type) {
		if (type.equals("meta")) {
			metaProperties.clear();
		} else if (type.equals("correlation")) {
			correlationProperties.clear();
		} else if (type.equals("payload")) {
			payloadProperties.clear();
		}
	}

	public String getStreamVersion() {
		return streamVersion;
	}

	public String getEventSink() {
		return eventSink;
	}

	public List<Property> getMetaProperties() {
		return metaProperties;
	}

	public List<Property> getCorrelationProperties() {
		return correlationProperties;
	}

	public List<Property> getPayloadProperties() {
		return payloadProperties;
	}

	public List<Property> getArbitraryProperties() {
		return arbitraryProperties;
	}

	public String getStreamName() {
		return streamName;
	}

	public String isAsync() {
		return isAsync;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	public void setStreamVersion(String streamVersion) {
		this.streamVersion = streamVersion;
	}

	public void setEventSink(String eventSink) {
		this.eventSink = eventSink;
	}

	public void setMetaProperties(List<Property> metaProperties) {
		this.metaProperties = metaProperties;
	}

	public void setCorrelationProperties(List<Property> correlationProperties) {
		this.correlationProperties = correlationProperties;
	}

	public void setIsAsync(String isAsync) {
		this.isAsync = isAsync;
	}

	public void setPayloadProperties(List<Property> payloadProperties) {
		this.payloadProperties = payloadProperties;
	}

	public void setArbitraryProperties(List<Property> arbitraryProperties) {
		this.arbitraryProperties = arbitraryProperties;
	}

	public static QName getNameAttributeQ() {
		return ATT_NAME;
	}

	public static QName getValueAttributeQ() {
		return ATT_VALUE;
	}

	public static QName getExpressionAttributeQ() {
		return ATT_EXPRN;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public String getTimeout() {
		return timeout;
	}
}