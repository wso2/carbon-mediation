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

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.Value;


/**
 * By using the input schema, output schema and mapping configuration,
 * DataMapperMediator generates the output required by the next mediator for the
 * input received by the previous mediator.
 */
public class DataMapperMediator extends AbstractMediator implements ManagedLifecycle{
//TODO : ManagedLifecycle why is this needed? what happens in init and destroy

	private Value configurationKey = null;
	private Value inputSchemaKey = null;
	private Value outputSchemaKey = null;
	private String inputType = null;
	private String outputType = null;
	private UUID id = null;
	private static final Log log = LogFactory.getLog(DataMapperMediator.class);

	/**
	 * Gets the key which is used to pick the mapping configuration from the
	 * registry
	 * 
	 * @return the key which is used to pick the mapping configuration from the
	 *         registry
	 */
	public Value getConfigurationKey() {
		return configurationKey;
	}
	/**
	 * Sets the registry key in order to pick the mapping configuration
	 * 
	 * @param dataMapperconfigKey registry key for the mapping configuration
	 */
	public void setConfigurationKey(Value dataMapperconfigKey) {
		this.configurationKey = dataMapperconfigKey;
	}

	/**
	 * Gets the key which is used to pick the input schema from the
	 * registry
	 * 
	 * @return the key which is used to pick the input schema from the
	 *         registry
	 */
	public Value getInputSchemaKey() {
		return inputSchemaKey;
	}
	/**
	 * Sets the registry key in order to pick the input schema
	 * 
	 * @param dataMapperInSchemaKey registry key for the input schema
	 */
	public void setInputSchemaKey(Value dataMapperInSchemaKey) {
		this.inputSchemaKey = dataMapperInSchemaKey;
	}
	
	/**
	 * Gets the key which is used to pick the output schema from the
	 * registry
	 * 
	 * @return the key which is used to pick the output schema from the
	 *         registry
	 */
	public Value getOutputSchemaKey() {
		return outputSchemaKey;
	}
	/**
	 * Sets the registry key in order to pick the output schema
	 * 
	 * @param dataMapperOutSchemaKey registry key for the output schema
	 */
	public void setOutputSchemaKey(Value dataMapperOutSchemaKey) {
		this.outputSchemaKey = dataMapperOutSchemaKey;
	}

	/**
	 * Gets the input data type
	 * 
	 * @return the input data type
	 */
	public String getInputType() {
		return inputType;
	}
	/**
	 * Sets the input data type
	 * 
	 * @param type the input data type
	 */
	public void setInputType(String type) {
		this.inputType = type;
	}
	
	/**
	 * Gets the output data type
	 * 
	 * @return the output data type
	 */
	public String getOutputType() {
		return outputType;
	}
	/**
	 * Sets the output data type
	 * 
	 * @param type the output data type
	 */
	public void setOutputType(String type) {
		this.outputType = type;
	}

	/**
	 * Gets the unique ID for the DataMapperMediator instance
	 * 
	 * @return the unique ID
	 */
	public String getUniqueID() {
		String uuid = id.toString();
		return uuid;
	}	
	/**
	 * Sets the unique ID for the DataMapperMediator instance
	 * 
	 * @param id the unique ID
	 */
	public void setUniqueID(UUID id) {
		this.id = id;
	}

	/**
	 * Get the values from the message context to do the data mapping
	 * 
	 * @param messageContext current message for the mediation
	 * @return true if mediation happened successfully else false.
	 */
	@Override
	public boolean mediate(MessageContext messageContext) {

		SynapseLog synLog = getLog(messageContext);
		if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("DataMapper mediator : started");
			if (synLog.isTraceTraceEnabled()) {
				synLog.traceTrace("Message :" + messageContext.getEnvelope());
			}
		}

		boolean result = true;

        //TODO : debug : what happens here?
		String configkey = configurationKey.evaluateValue(messageContext);
		String inSchemaKey = inputSchemaKey.evaluateValue(messageContext);
		String outSchemaKey = outputSchemaKey.evaluateValue(messageContext);

		//checks the availability of the inputs for data mapping
		if (!(StringUtils.isNotEmpty(configkey)
				&& StringUtils.isNotEmpty(inSchemaKey) && StringUtils
					.isNotEmpty(outSchemaKey))) {
			log.error("Invalid configurations for the DataMapperMediator");
			result = false;
		} else {
			try {
				// Does message conversion and gives the final result
				//FIXME : move transform method to mediator class. remove helper class. remove synchronized
				synchronized(DataMapperHelper.class){
				DataMapperHelper.transform(messageContext, configkey, inSchemaKey,
						outSchemaKey, inputType, outputType, getUniqueID());
				}
			} catch (SynapseException synExp) {
                //FIXME : throw synapse exception
				log.error("Mediation failed at DataMapperMediator");
				result = false;
			} catch (IOException e) {
                //FIXME : throw synapse exception
				log.error("Mediation failed at DataMapperMediator");
				result = false;
			}
		}

		if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("DataMapper mediator : Done");
			if (synLog.isTraceTraceEnabled()) {
				synLog.traceTrace("Message : " + messageContext.getEnvelope());
			}
		}
		return result;
	}

	/**
	 * State that DataMapperMediator interacts with the message context
	 * 
	 * @return true if the DataMapperMediator is intending to interact with the
	 *         message context
	 */
	@Override
	public boolean isContentAware() {
		return true;
	}
	@Override
	public void init(SynapseEnvironment se) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * destroy the generated unique ID for the DataMapperMediator instance
	 */
	@Override
	public void destroy() {
        //TODO :
		if (id != null) {
			setUniqueID(id);
        }		
	}

}