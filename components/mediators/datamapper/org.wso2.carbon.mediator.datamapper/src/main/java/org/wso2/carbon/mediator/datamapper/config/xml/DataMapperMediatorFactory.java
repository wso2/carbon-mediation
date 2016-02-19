/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.mediator.datamapper.config.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.*;
import org.apache.synapse.mediators.Value;
import org.wso2.carbon.mediator.datamapper.DataMapperMediator;

import javax.xml.namespace.QName;

import java.util.Properties;
import java.util.UUID;

/**
 * Configuration syntax: <datamapper config="gov:datamapper/mappingConfig.dmc"
 * inputSchema="gov:datamapper/inputSchema.avsc"
 * outputSchema="gov:datamapper/outputSchema.avsc" inputType="application/xml"
 * outputType="application/xml" />
 * 
 */

public class DataMapperMediatorFactory extends AbstractMediatorFactory {
	/**
	 * Holds the QName for the DataMapperMediator xml configuration
	 */
	private static final QName TAG_QNAME = new QName(
			XMLConfigConstants.SYNAPSE_NAMESPACE,
			ConfigurationProperties.DATAMAPPER);

	/**
	 * Get the DataMapperMediator configuration tag name
	 * 
	 * @return QName specifying the DataMapperMediator tag name of the xml
	 *         configuration
	 */
	@Override
	public QName getTagQName() {
		return TAG_QNAME;
	}

	/**
	 * Creates the DataMapperMediator by parsing the given xml
	 * configuration
	 * 
	 * @param element
	 *            OMElement describing the configuration of the
	 *            DataMapperMediator
	 * @param properties
	 *            Properties passed
	 * @return DataMapperMediator created from the given configuration
	 */
	@Override
	protected Mediator createSpecificMediator(OMElement element,
			Properties properties) {

		DataMapperMediator datamapperMediator = new DataMapperMediator();

		OMAttribute configKeyAttribute = element.getAttribute(new QName(
				ConfigurationProperties.CONFIG));
		OMAttribute inputSchemaKeyAttribute = element.getAttribute(new QName(
				ConfigurationProperties.INPUTSCHEMA));
		OMAttribute outputSchemaKeyAttribute = element.getAttribute(new QName(
				ConfigurationProperties.OUTPUTSCHEMA));
		OMAttribute inputTypeAttribute = element.getAttribute(new QName(
				ConfigurationProperties.INPUTTYPE));
		OMAttribute outputTypeAttribute = element.getAttribute(new QName(
				ConfigurationProperties.OUTPUTTYPE));

		/*
		 * ValueFactory for creating dynamic or static Value and provide methods
		 * to create value objects
		 */
		ValueFactory keyFac = new ValueFactory();

		if (configKeyAttribute != null) {
			// Create dynamic or static key based on OMElement
			Value configKeyValue = keyFac.createValue(
					configKeyAttribute.getLocalName(), element);
			// set key as the Value
			datamapperMediator.setConfigurationKey(configKeyValue);
		} else {
			handleException("The attribute config is required for the DataMapper mediator");
		}

		if (inputSchemaKeyAttribute != null) {
			Value inputSchemaKeyValue = keyFac.createValue(
					inputSchemaKeyAttribute.getLocalName(), element);
			datamapperMediator.setInputSchemaKey(inputSchemaKeyValue);
		} else {
			handleException("The attribute inputSchema is required for the DataMapper mediator");
		}

		if (outputSchemaKeyAttribute != null) {
			Value outputSchemaKeyValue = keyFac.createValue(
					outputSchemaKeyAttribute.getLocalName(), element);
			datamapperMediator.setOutputSchemaKey(outputSchemaKeyValue);
		} else {
			handleException("The outputSchema attribute is required for the DataMapper mediator");
		}

		if (inputTypeAttribute != null) {
			datamapperMediator.setInputType(inputTypeAttribute
					.getAttributeValue());
		} else {
			handleException("The input DataType is required for the DataMapper mediator");
		}

		if (outputTypeAttribute != null) {
			datamapperMediator.setOutputType(outputTypeAttribute
					.getAttributeValue());
		} else {
			handleException("The output DataType is required for the DataMapper mediator");
		}
		
		// set the unique ID for the DataMapperMediator instance
		if(generateID() != null){
		datamapperMediator.setUniqueID(generateID());
		}
		else {
			generateID();
		}
		processAuditStatus(datamapperMediator, element);

		return datamapperMediator;
	}

	// Generated a unique ID
	private UUID generateID() {
		UUID uuid = UUID.randomUUID();
		return uuid;

	}
}
