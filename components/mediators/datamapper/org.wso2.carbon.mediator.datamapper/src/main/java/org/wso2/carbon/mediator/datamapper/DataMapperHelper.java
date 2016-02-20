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
package org.wso2.carbon.mediator.datamapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.namespace.QName;

import org.apache.avro.generic.GenericRecord;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.util.AXIOMUtils;
import org.wso2.datamapper.engine.core.MappingHandler;
import org.wso2.datamapper.engine.core.MappingResourceLoader;
import org.wso2.datamapper.engine.inputAdapters.*;
import org.wso2.carbon.mediator.datamapper.datatypes.*;

/**
 * Using the input schema, output schema,mapping configuration, input and output
 * data types DataMapperHelper generates the required output
 */
public class DataMapperHelper {

	private static final String ENVELOPE = "Envelope";
	private static final Log log = LogFactory.getLog(DataMapperHelper.class);

	/**
	 * Does message conversion and gives the output message as the final result
	 * 
	 * @param context
	 *            the message context
	 * @param configkey
	 *            registry location of the mapping configuration
	 * @param inSchemaKey
	 *            registry location of the input schema
	 * @param outSchemaKey
	 *            registry location of the output schema
	 * @param inputType
	 *            input data type
	 * @param outputType
	 *            output data type
	 * @param uuid
	 *            unique ID for the DataMapperMediator instance
	 * @throws SynapseException
	 * @throws IOException
	 */
	public static void transform(MessageContext context, String configkey,
			String inSchemaKey, String outSchemaKey, String inputType,
			String outputType, String uuid) throws SynapseException,
			IOException {

		MappingResourceLoader mappingResourceLoader = null;
		OMElement outputMessage = null;

		try {
			// mapping resources needed to get the final output
			//FIXME : remove caching part?? use in-memory for schemas
			mappingResourceLoader = CacheResources.getCachedResources(context,
					configkey, inSchemaKey, outSchemaKey, uuid);
			
			InputStream inputSstream = null;
            // FIXME include DatumReaders
            InputDataReaderAdapter inputReader = convertInputMessage(inputType);

            switch (InputOutputDataTypes.DataType.fromString(inputType)) {
                case XML:
                    OMElement inputMessage = context.getEnvelope().getBody().getFirstElement();
                    inputSstream = new ByteArrayInputStream(inputMessage.toString().getBytes(StandardCharsets.UTF_8));
                    break;
                case JSON:
                    org.apache.axis2.context.MessageContext a2mc = ((Axis2MessageContext) context).getAxis2MessageContext();
                    if (JsonUtil.hasAJsonPayload(a2mc)) {
                        inputSstream = JsonUtil.getJsonPayload(a2mc);
                    }
                    break;
                default:
            }

			GenericRecord result = MappingHandler.doMap(inputSstream, mappingResourceLoader, inputReader);

			// Output message
			OutputWriter writer = OutputWriterFactory.getWriter(outputType);
			outputMessage = writer.getOutputMessage(outputType, result);

			if (outputMessage != null) {
				if (log.isDebugEnabled()) {
					log.debug("Output message received ... ");
				}
				// Use to create the SOAP message
				if (outputMessage != null) {
					OMElement firstChild = outputMessage.getFirstElement();
					if (firstChild != null) {
						if (log.isDebugEnabled()) {
							log.debug("Contains a first child");
						}
						QName resultQName = firstChild.getQName();
						// TODO use XPath
						if (resultQName.getLocalPart().equals(ENVELOPE)
								&& (resultQName
										.getNamespaceURI()
										.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI) || resultQName
										.getNamespaceURI()
										.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI))) {
							SOAPEnvelope soapEnvelope = AXIOMUtils
									.getSOAPEnvFromOM(outputMessage
											.getFirstElement());
							if (soapEnvelope != null) {
								try {
									if (log.isDebugEnabled()) {
										log.debug("Valid Envelope");
									}
									context.setEnvelope(soapEnvelope);
								} catch (AxisFault axisFault) {
									handleException("Invalid Envelope",
											axisFault);
								}
							}
						} else {
							context.getEnvelope().getBody().getFirstElement()
									.detach();
							context.getEnvelope().getBody()
									.addChild(outputMessage);

						}
					} else {
						context.getEnvelope().getBody().getFirstElement()
								.detach();
						context.getEnvelope().getBody().addChild(outputMessage);
					}
				}
			}
		} catch (Exception e) {
			handleException("Mapping failed", e);
		}

	}

	/**
	 * Give the Input for the mapping
	 * 
	 * @param inputDataType
	 *            input data type
	 * @return the input as a OMElement
	 * @throws IOException
	 */
	private static InputDataReaderAdapter convertInputMessage(
			String inputDataType) throws IOException {
		InputDataReaderAdapter inputReader = null;
		if (inputDataType != null) {
			if (log.isDebugEnabled()) {
				log.debug("Input data type is ... " + inputDataType);
			}
			switch (InputOutputDataTypes.DataType.fromString(inputDataType)) {
			case CSV:
				inputReader = new CsvInputReader();
				break;
			case XML:
				inputReader = new XmlInputReader();
				break;
            case JSON:
                inputReader = new JsonInputReader();
                break;
			default:
				// HandleJSONMessages.getOutputMessage(outputDataType, result);
			}
		} else {
			// FIXME with default dataType if user didn't mention input dataType
			inputReader = new XmlInputReader();
		}
		return inputReader;
	}

	private static void handleException(String message, Exception e) {
		log.error(message, e);
		throw new SynapseException(message, e);
	}

}
