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
package org.wso2.carbon.mediator.datamapper;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.AXIOMUtils;
import org.wso2.datamapper.engine.core.Executable;
import org.wso2.datamapper.engine.core.MappingHandler;
import org.wso2.datamapper.engine.core.MappingResourceLoader;
import org.wso2.datamapper.engine.core.executors.ScriptExecutorFactory;
import org.wso2.datamapper.engine.input.InputModelBuilder;
import org.wso2.datamapper.engine.output.OutputMessageBuilder;
import org.wso2.datamapper.engine.types.DMModelTypes;
import org.wso2.datamapper.engine.types.InputOutputDataTypes;
import org.apache.synapse.commons.json.JsonUtil;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


/**
 * By using the input schema, output schema and mapping configuration,
 * DataMapperMediator generates the output required by the next mediator for the
 * input received by the previous mediator.
 */
public class DataMapperMediator extends AbstractMediator implements ManagedLifecycle {

    private Value mappingConfigurationKey = null;
    private Value inputSchemaKey = null;
    private Value outputSchemaKey = null;
    private String inputType = null;
    private String outputType = null;
    private static MappingResourceLoader mappingResourceLoader = null;
    private static final Log log = LogFactory.getLog(DataMapperMediator.class);

    /**
     * Gets the key which is used to pick the mapping configuration from the
     * registry
     *
     * @return the key which is used to pick the mapping configuration from the
     * registry
     */
    public Value getMappingConfigurationKey() {
        return mappingConfigurationKey;
    }

    /**
     * Sets the registry key in order to pick the mapping configuration
     *
     * @param dataMapperconfigKey registry key for the mapping configuration
     */
    public void setMappingConfigurationKey(Value dataMapperconfigKey) {
        this.mappingConfigurationKey = dataMapperconfigKey;
    }

    /**
     * Gets the key which is used to pick the input schema from the
     * registry
     *
     * @return the key which is used to pick the input schema from the
     * registry
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
     * registry
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
        if (inputType != null) {
            return inputType;
        } else {
            log.warn("Input data type not found. Set to default value : " + InputOutputDataTypes.DataType.XML);
            return InputOutputDataTypes.DataType.XML.toString();
        }
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
        if (outputType != null) {
            return outputType;
        } else {
            log.warn("Output data type not found. Set to default value : " + InputOutputDataTypes.DataType.XML);
            return InputOutputDataTypes.DataType.XML.toString();
        }
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
     * Get the values from the message context to do the data mapping
     *
     * @param synCtx current message for the mediation
     * @return true if mediation happened successfully else false.
     */
    @Override
    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synCtx.getEnvironment().isDebugEnabled()) {
            if (super.divertMediationRoute(synCtx)) {
                return true;
            }
        }
        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : DataMapper mediator");
            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message :" + synCtx.getEnvelope());
            }
        }

        String configKey = mappingConfigurationKey.evaluateValue(synCtx);
        String inSchemaKey = inputSchemaKey.evaluateValue(synCtx);
        String outSchemaKey = outputSchemaKey.evaluateValue(synCtx);

        //checks the availability of the inputs for data mapping
        if (!(StringUtils.isNotEmpty(configKey)
                && StringUtils.isNotEmpty(inSchemaKey) && StringUtils
                .isNotEmpty(outSchemaKey))) {
            handleException("DataMapper mediator : Invalid configurations", synCtx);
        } else {
            try {
                // Does message conversion and gives the final result
                transform(synCtx, configKey, inSchemaKey, outSchemaKey, getInputType(), getOutputType());

            } catch (SynapseException e) {
                handleException("DataMapper mediator mediation failed", e, synCtx);
            }
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End : DataMapper mediator");
            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }
        return true;
    }

    /**
     * Does message conversion and gives the output message as the final result
     *
     * @param synCtx       the message synCtx
     * @param configkey    registry location of the mapping configuration
     * @param inSchemaKey  registry location of the input schema
     * @param outSchemaKey registry location of the output schema
     * @param inputType    input data type
     * @param outputType   output data type
     * @throws SynapseException
     * @throws IOException
     */
    private void transform(MessageContext synCtx, String configkey,
                           String inSchemaKey, String outSchemaKey, String inputType,
                           String outputType) {
        //MappingResourceLoader mappingResourceLoader = null;
        OMElement outputMessage = null;
        try {
            // mapping resources needed to get the final output
            mappingResourceLoader = getMappingResourceLoader(synCtx, configkey, inSchemaKey, outSchemaKey);

            // create input model builder to convert input payload to generic data holder
            InputModelBuilder inputModelBuilder = new InputModelBuilder(getDataType(inputType),
                    DMModelTypes.ModelType.JSON_STRING, mappingResourceLoader.getInputSchema());
            //execute mapping on the input stream
            MappingHandler mappingHandler = new MappingHandler();
            OutputMessageBuilder outputMessageBuilder = new OutputMessageBuilder(getDataType(outputType),
                    DMModelTypes.ModelType.JAVA_MAP, mappingResourceLoader.getOutputSchema());

            Executable executor = ScriptExecutorFactory.getScriptExecutor();

            String outputVariable=mappingHandler.doMap(getInputStream(synCtx, inputType), mappingResourceLoader,
                    inputModelBuilder, outputMessageBuilder, executor);

            ScriptExecutorFactory.releaseScriptExecutor(executor);

            if(InputOutputDataTypes.DataType.XML.toString().equals(outputType)){
                outputMessage = AXIOMUtil.stringToOM(outputVariable);
                if (outputMessage != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Output message received ");
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
                            if (resultQName.getLocalPart().equals("Envelope")
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
                                        synCtx.setEnvelope(soapEnvelope);
                                    } catch (AxisFault axisFault) {
                                        handleException("Invalid Envelope",
                                                axisFault, synCtx);
                                    }
                                }
                            } else {
                                synCtx.getEnvelope().getBody().getFirstElement()
                                        .detach();
                                synCtx.getEnvelope().getBody()
                                        .addChild(outputMessage);

                            }
                        } else {
                            synCtx.getEnvelope().getBody().getFirstElement()
                                    .detach();
                            synCtx.getEnvelope().getBody().addChild(outputMessage);
                        }
                    }
                }

            }else if(InputOutputDataTypes.DataType.JSON.toString().equals(outputType)){
                org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
                JsonUtil.newJsonPayload(axis2MessageContext, outputVariable, true, true);
            }
        } catch (Exception e) {
            handleException("Mapping failed", e, synCtx);
        }

    }

    private static InputOutputDataTypes.DataType getDataType(String inputType) {
        return InputOutputDataTypes.DataType.fromString(inputType);
    }

    private InputStream getInputStream(MessageContext context, String inputType) {

        InputStream inputStream = null;
        switch (InputOutputDataTypes.DataType.fromString(inputType)) {
            case XML:
            case CSV:
                inputStream = new ByteArrayInputStream(
                        context.getEnvelope().getBody().getFirstElement().toString().getBytes(StandardCharsets.UTF_8));
                break;
            case JSON:
                org.apache.axis2.context.MessageContext a2mc = ((Axis2MessageContext) context).getAxis2MessageContext();
                if (JsonUtil.hasAJsonPayload(a2mc)) {
                    inputStream = JsonUtil.getJsonPayload(a2mc);
                }
                break;
            default:
                inputStream = new ByteArrayInputStream(
                        context.getEnvelope().getBody().getFirstElement().toString().getBytes(StandardCharsets.UTF_8));
                break;
        }
        return inputStream;
    }


    /**
     * State that DataMapperMediator interacts with the message context
     *
     * @return true if the DataMapperMediator is intending to interact with the
     * message context
     */
    @Override
    public boolean isContentAware() {
        return true;
    }

    @Override
    public void init(SynapseEnvironment se) {

    }

    /**
     * destroy the generated unique ID for the DataMapperMediator instance
     */
    @Override
    public void destroy() {
    }

    /**
     * When Data mapper mediator has been invoked initially, this creates a new mapping resource loader
     *
     * @param context      message context
     * @param configkey    the location of the mapping configuration
     * @param inSchemaKey  the location of the input schema
     * @param outSchemaKey the location of the output schema
     * @return the MappingResourceLoader object
     * @throws IOException
     */
    private static MappingResourceLoader getMappingResourceLoader(
            MessageContext context, String configkey, String inSchemaKey, String outSchemaKey) throws IOException {

        if(mappingResourceLoader == null){
            InputStream configFileInputStream = getRegistryResource(context, configkey);
            InputStream inputSchemaStream = getRegistryResource(context, inSchemaKey);
            InputStream outputSchemaStream = getRegistryResource(context, outSchemaKey);

            // Creates a new mappingResourceLoader
            mappingResourceLoader = new MappingResourceLoader(inputSchemaStream,
                    outputSchemaStream, configFileInputStream);
        }
        return mappingResourceLoader;
    }

    /**
     * Returns registry resources as input streams to create the MappingResourceLoader object
     *
     * @param context Message context
     * @param key     registry key
     * @return mapping configuration, inputSchema and outputSchema as inputStreams
     */
    private static InputStream getRegistryResource(MessageContext context, String key) {

        InputStream inputStream = null;
        Object entry = context.getEntry(key);
        if (entry instanceof OMTextImpl) {
            if (log.isDebugEnabled()) {
                log.debug("Value for the key is ");
            }
            OMTextImpl text = (OMTextImpl) entry;
            String content = text.getText();
            inputStream = new ByteArrayInputStream(content.getBytes());
        }
        return inputStream;
    }

}