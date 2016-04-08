/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediator.datamapper.ui;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.ValueFactory;
import org.apache.synapse.config.xml.ValueSerializer;
import org.apache.synapse.mediators.Value;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import javax.xml.namespace.QName;

/**
 * By using the input schema, output schema and mapping configuration,
 * DataMapperMediator generates the output required by the next mediator for the
 * input received by the previous mediator.
 */
public class DataMapperMediator extends AbstractMediator {

	private static final String ATT_CONFIGURATION_KEY = "config";
	private static final String ATT_INPUT_SCHEMA_KEY = "inputSchema";
	private static final String ATT_OUTPUT_SCHEMA_KEY = "outputSchema";
	private static final String ATT_INPUT_TYPE = "inputType";
	private static final String ATT_OUTPUT_TYPE = "outputType";

	private Value configurationKey;
	private Value inputSchemaKey;
	private Value outputSchemaKey;

	private static final String CSV = "CSV";
	private static final String XML = "XML";
	private static final String JSON = "JSON";

	public static final int CSV_VALUE = 0;
	public static final int XML_VALUE = 1;
	public static final int JSON_VALUE = 2;

	/**
	 * The default inputType is CSV
	 */
	private int inputType = CSV_VALUE;
	private int outputType = CSV_VALUE;

	public String getTagLocalName() {
		return "datamapper";
	}

	/**
	 * Gets the key which is used to pick the mapping configuration from the
	 * registry
	 *
	 * @return the key which is used to pick the mapping configuration from the
	 * registry
	 */
	public Value getConfigurationKey() {
		return configurationKey;
	}

	/**
	 * Sets the registry key in order to pick the mapping configuration
	 *
	 * @return set the registry key to pick mapping configuration
	 */
	public void setConfigurationKey(Value dataMapperKey) {
		this.configurationKey = dataMapperKey;
	}

	/**
	 * Gets the registry key of the inputSchema
	 */
	public Value getInputSchemaKey() {
		return inputSchemaKey;
	}

	/**
	 * Sets the registry key in order to pick the inputSchema
	 *
	 * @return set the local registry key to pick inputSchema
	 */

	public void setInputSchemaKey(Value dataMapperKey) {
		this.inputSchemaKey = dataMapperKey;
	}

	/**
	 * Gets the registry key of the outputSchema
	 */
	public Value getOutputSchemaKey() {
		return outputSchemaKey;
	}

	/**
	 * Sets the registry key in order to pick the outputSchema
	 *
	 * @return set the local registry key to pick outputSchema
	 */
	public void setOutputSchemaKey(Value dataMapperKey) {
		this.outputSchemaKey = dataMapperKey;
	}

	/**
	 * Gets the inputDataType
	 */
	public int getInputType() {
		return inputType;
	}

	/**
	 * Sets the inputDataType
	 */
	public void setInputType(int type) {
		this.inputType = type;
	}

	/**
	 * Gets the outputDataType
	 */
	public int getOutputType() {
		return outputType;
	}

	/**
	 * Sets the outputDataType
	 */

	public void setOutputType(int type) {
		this.outputType = type;
	}

	public OMElement serialize(OMElement parent) {
		OMElement dataMapperElement = fac.createOMElement("datamapper", synNS);

		if (configurationKey != null) {
			// Serialize Value using ValueSerializer
			ValueSerializer keySerializer = new ValueSerializer();
			keySerializer.serializeValue(configurationKey, ATT_CONFIGURATION_KEY, dataMapperElement);
		} else {
			handleException("Invalid DataMapper mediator. Configuration registry key is required");
		}

		if (inputSchemaKey != null) {
			ValueSerializer keySerializer = new ValueSerializer();
			keySerializer.serializeValue(inputSchemaKey, ATT_INPUT_SCHEMA_KEY, dataMapperElement);
		} else {
			handleException("Invalid DataMapper mediator. InputSchema registry key is required");
		}

		if (outputSchemaKey != null) {
			ValueSerializer keySerializer = new ValueSerializer();
			keySerializer.serializeValue(outputSchemaKey, ATT_OUTPUT_SCHEMA_KEY, dataMapperElement);
		} else {
			handleException("Invalid DataMapper mediator. OutputSchema registry key is required");
		}

		if (inputType != CSV_VALUE) {
			dataMapperElement.addAttribute(fac.createOMAttribute(ATT_INPUT_TYPE, nullNS,
			                                                     inputType == XML_VALUE ? "XML" :
			                                                     inputType == JSON_VALUE ? "JSON" : "CSV"));
		}
		if (outputType != CSV_VALUE) {
			dataMapperElement.addAttribute(fac.createOMAttribute(ATT_OUTPUT_TYPE, nullNS,
			                                                     outputType == XML_VALUE ? "XML" :
			                                                     outputType == JSON_VALUE ? "JSON" : "CSV"));
		}

		saveTracingState(dataMapperElement, this);
		if (parent != null) {
			parent.addChild(dataMapperElement);
		}

		return dataMapperElement;
	}

	public void build(OMElement element) {

		OMAttribute configKeyAttribute = element.getAttribute(new QName(ATT_CONFIGURATION_KEY));
		OMAttribute inputSchemaKeyAttribute = element.getAttribute(new QName(ATT_INPUT_SCHEMA_KEY));
		OMAttribute outputSchemaKeyAttribute = element.getAttribute(new QName(ATT_OUTPUT_SCHEMA_KEY));
		OMAttribute inputTypeAttribute = element.getAttribute(new QName(ATT_INPUT_TYPE));
		OMAttribute outputTypeAttribute = element.getAttribute(new QName(ATT_OUTPUT_TYPE));

		/*
	     * ValueFactory for creating dynamic or static Value and provide methods
		 * to create value objects
		 */
		ValueFactory keyFac = new ValueFactory();

		if (configKeyAttribute != null) {
			// Create dynamic or static key based on OMElement
			Value configKeyValue = keyFac.createValue(configKeyAttribute.getLocalName(), element);
			// set key as the Value
			setConfigurationKey(configKeyValue);
		} else {
			handleException("The attribute config is required for the DataMapper mediator");
		}

		if (inputSchemaKeyAttribute != null) {
			Value inputSchemaKeyValue = keyFac.createValue(inputSchemaKeyAttribute.getLocalName(), element);
			setInputSchemaKey(inputSchemaKeyValue);
		} else {
			handleException("The attribute inputSchema is required for the DataMapper mediator");
		}

		if (outputSchemaKeyAttribute != null) {
			Value outputSchemaKeyValue = keyFac.createValue(outputSchemaKeyAttribute.getLocalName(), element);
			setOutputSchemaKey(outputSchemaKeyValue);
		} else {
			handleException("The outputSchema attribute is required for the DataMapper mediator");
		}

		if (inputTypeAttribute != null) {
			String inputTypeStr = inputTypeAttribute.getAttributeValue();
			if (CSV.equals(inputTypeStr)) {
				inputType = CSV_VALUE;
			} else if (XML.equals(inputTypeStr)) {
				inputType = XML_VALUE;
			} else if (JSON.equals(inputTypeStr)) {
				inputType = JSON_VALUE;
			}
		}

		if (outputTypeAttribute != null) {
			String outputTypeStr = outputTypeAttribute.getAttributeValue();
			if (CSV.equals(outputTypeStr)) {
				outputType = CSV_VALUE;
			} else if (XML.equals(outputTypeStr)) {
				outputType = XML_VALUE;
			} else if (JSON.equals(outputTypeStr)) {
				outputType = JSON_VALUE;
			}
		}

		processAuditStatus(this, element);
	}

	private void handleException(String msg) {
		LogFactory.getLog(this.getClass()).error(msg);
		throw new SynapseException(msg);
	}
}
