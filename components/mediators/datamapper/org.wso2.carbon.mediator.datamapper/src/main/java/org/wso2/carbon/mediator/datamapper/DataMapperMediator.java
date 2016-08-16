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
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.SynapsePropertiesLoader;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.template.TemplateContext;
import org.apache.synapse.util.AXIOMUtils;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.JSException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.ReaderException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.WriterException;
import org.wso2.carbon.mediator.datamapper.engine.core.mapper.MappingHandler;
import org.wso2.carbon.mediator.datamapper.engine.core.mapper.MappingResource;
import org.wso2.carbon.mediator.datamapper.engine.utils.InputOutputDataType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.mediator.datamapper.config.xml.DataMapperMediatorConstants.AXIS2_CLIENT_CONTEXT;
import static org.wso2.carbon.mediator.datamapper.config.xml.DataMapperMediatorConstants.AXIS2_CONTEXT;
import static org.wso2.carbon.mediator.datamapper.config.xml.DataMapperMediatorConstants.DEFAULT_CONTEXT;
import static org.wso2.carbon.mediator.datamapper.config.xml.DataMapperMediatorConstants.EMPTY_STRING;
import static org.wso2.carbon.mediator.datamapper.config.xml.DataMapperMediatorConstants.FUNCTION_CONTEXT;
import static org.wso2.carbon.mediator.datamapper.config.xml.DataMapperMediatorConstants.OPERATIONS_CONTEXT;
import static org.wso2.carbon.mediator.datamapper.config.xml.DataMapperMediatorConstants.SYNAPSE_CONTEXT;
import static org.wso2.carbon.mediator.datamapper.config.xml.DataMapperMediatorConstants.TRANSPORT_CONTEXT;
import static org.wso2.carbon.mediator.datamapper.config.xml.DataMapperMediatorConstants.TRANSPORT_HEADERS;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.ORG_APACHE_SYNAPSE_DATAMAPPER_EXECUTOR_POOL_SIZE;

/**
 * By using the input schema, output schema and mapping configuration,
 * DataMapperMediator generates the output required by the next mediator for the
 * input received by the previous mediator.
 */
public class DataMapperMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(DataMapperMediator.class);
    private static final String cSVToXMLOpeningTag = "<text xmlns=\"http://ws.apache.org/commons/ns/payload\">";
    private static final String cSVToXMLClosingTag = "</text>";
    private Value mappingConfigurationKey = null;
    private Value inputSchemaKey = null;
    private Value outputSchemaKey = null;
    private String inputType = null;
    private String outputType = null;
    private MappingResource mappingResource = null;

    /**
     * Returns registry resources as input streams to create the MappingResourceLoader object
     *
     * @param synCtx Message context
     * @param key    location in the registry
     * @return mapping configuration, inputSchema and outputSchema as inputStreams
     */
    private static InputStream getRegistryResource(MessageContext synCtx, String key) {
        InputStream inputStream = null;
        Object entry = synCtx.getEntry(key);
        if (entry instanceof OMTextImpl) {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving the key :" + key);
            }
            OMTextImpl text = (OMTextImpl) entry;
            String content = text.getText();
            inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        }
        return inputStream;
    }

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
            log.warn("Input data type not found. Set to default value : " + InputOutputDataType.XML);
            return InputOutputDataType.XML.toString();
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
            log.warn("Output data type not found. Set to default value : " + InputOutputDataType.XML);
            return InputOutputDataType.XML.toString();
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

        if (synCtx.getEnvironment().isDebuggerEnabled()) {
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

        if (mappingResource == null) {
            String configKey = mappingConfigurationKey.evaluateValue(synCtx);
            String inSchemaKey = inputSchemaKey.evaluateValue(synCtx);
            String outSchemaKey = outputSchemaKey.evaluateValue(synCtx);
            if (!(StringUtils.isNotEmpty(configKey) && StringUtils.isNotEmpty(inSchemaKey) &&
                    StringUtils.isNotEmpty(outSchemaKey))) {
                handleException("DataMapper mediator : Invalid configurations", synCtx);
            } else {
                // mapping resources needed to get the final output
                try {
                    mappingResource = getMappingResource(synCtx, configKey, inSchemaKey, outSchemaKey);
                } catch (IOException e) {
                    handleException("DataMapper mediator mapping resource generation failed", e, synCtx);
                }
            }
        }
        // Does message conversion and gives the final result
        transform(synCtx, getInputType(), getOutputType());
        //setting output type in the axis2 message context
        switch (getOutputType()) {
        case "JSON":
            ((Axis2MessageContext) synCtx).getAxis2MessageContext().setProperty("messageType", "application/json");
            ((Axis2MessageContext) synCtx).getAxis2MessageContext().setProperty("ContentType", "application/json");
            break;
        case "XML":
            ((Axis2MessageContext) synCtx).getAxis2MessageContext().setProperty("messageType", "application/xml");
            ((Axis2MessageContext) synCtx).getAxis2MessageContext().setProperty("ContentType", "application/xml");
            break;
        case "CSV":
            ((Axis2MessageContext) synCtx).getAxis2MessageContext().setProperty("messageType", "text/xml");
            ((Axis2MessageContext) synCtx).getAxis2MessageContext().setProperty("ContentType", "text/xml");
            break;
        default:
            throw new SynapseException("Unsupported output data type found : " + getOutputType());
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
     * @param synCtx      the message synCtx
     * @param configKey   registry location of the mapping configuration
     * @param inSchemaKey registry location of the input schema
     */
    private void transform(MessageContext synCtx, String configKey, String inSchemaKey) {
        try {
            String outputResult = null;
            Map<String, Map<String, Object>> propertiesMap;

            String dmExecutorPoolSize = SynapsePropertiesLoader
                    .getPropertyValue(ORG_APACHE_SYNAPSE_DATAMAPPER_EXECUTOR_POOL_SIZE, null);

            MappingHandler mappingHandler = new MappingHandler(mappingResource, inputType, outputType,
                    dmExecutorPoolSize);

            propertiesMap = getPropertiesMap(mappingResource.getPropertiesList(), synCtx);

            /* execute mapping on the input stream */
            outputResult = mappingHandler
                    .doMap(getInputStream(synCtx, inputType, mappingResource.getInputSchema().getName()),
                            propertiesMap);

            if (InputOutputDataType.CSV.toString().equals(outputType)) {
                outputResult = cSVToXMLOpeningTag + outputResult + cSVToXMLClosingTag;
            }

            if (InputOutputDataType.XML.toString().equals(outputType) || InputOutputDataType.CSV.toString()
                    .equals(outputType)) {
                OMElement outputMessage = AXIOMUtil.stringToOM(outputResult);
                if (outputMessage != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Output message received ");
                    }
                    // Use to create the SOAP message
                    QName resultQName = outputMessage.getQName();
                    if (resultQName.getLocalPart().equals("Envelope") && (
                            resultQName.getNamespaceURI().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)
                                    || resultQName.getNamespaceURI()
                                    .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI))) {
                        SOAPEnvelope soapEnvelope = AXIOMUtils.getSOAPEnvFromOM(outputMessage);
                        if (soapEnvelope != null) {
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("Valid Envelope");
                                }
                                synCtx.setEnvelope(soapEnvelope);
                            } catch (AxisFault axisFault) {
                                handleException("Invalid Envelope", axisFault, synCtx);
                            }
                        }
                    } else {
                        synCtx.getEnvelope().getBody().getFirstElement().detach();
                        synCtx.getEnvelope().getBody().addChild(outputMessage);
                    }
                } else {
                    synCtx.getEnvelope().getBody().getFirstElement().detach();
                }

            } else if (InputOutputDataType.JSON.toString().equals(outputType)) {
                org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
                        .getAxis2MessageContext();
                JsonUtil.newJsonPayload(axis2MessageContext, outputResult, true, true);
            }
        } catch (ReaderException | InterruptedException | XMLStreamException | SchemaException
                | IOException | JSException | WriterException e) {
            handleException("DataMapper mediator : mapping failed", e, synCtx);
        }
    }

    private InputStream getInputStream(MessageContext context, String inputType, String inputStartElement) {
        InputStream inputStream = null;
        try {
            switch (InputOutputDataType.fromString(inputType)) {
            case XML:
            case CSV:
                if ("soapenv:Envelope".equals(inputStartElement)) {
                    inputStream = new ByteArrayInputStream(
                            context.getEnvelope().toString().getBytes(StandardCharsets.UTF_8));
                } else {
                    inputStream = new ByteArrayInputStream(context.getEnvelope().getBody().getFirstElement().toString()
                            .getBytes(StandardCharsets.UTF_8));
                }
                break;
            case JSON:
                org.apache.axis2.context.MessageContext a2mc = ((Axis2MessageContext) context).getAxis2MessageContext();
                if (JsonUtil.hasAJsonPayload(a2mc)) {
                    inputStream = JsonUtil.getJsonPayload(a2mc);
                }
                break;
            default:
                inputStream = new ByteArrayInputStream(
                        context.getEnvelope().toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            handleException("Unable to read input message in Data Mapper mediator reason : " + e.getMessage(), e,
                    context);
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
    public boolean isContentAltering() {
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
     * When Data mapper mediator has been invoked initially, this creates a new mapping resource
     * loader
     *
     * @param synCtx       message context
     * @param configKey    the location of the mapping configuration
     * @param inSchemaKey  the location of the input schema
     * @param outSchemaKey the location of the output schema
     * @return the MappingResourceLoader object
     * @throws IOException
     */
    private MappingResource getMappingResource(MessageContext synCtx, String configKey, String inSchemaKey,
            String outSchemaKey) throws IOException {

        InputStream configFileInputStream = getRegistryResource(synCtx, configKey);
        InputStream inputSchemaStream = getRegistryResource(synCtx, inSchemaKey);
        InputStream outputSchemaStream = getRegistryResource(synCtx, outSchemaKey);

        if (configFileInputStream == null) {
            handleException("DataMapper mediator : mapping configuration is null", synCtx);
        }

        if (inputSchemaStream == null) {
            handleException("DataMapper mediator : input schema is null", synCtx);
        }

        if (outputSchemaStream == null) {
            handleException("DataMapper mediator : output schema is null", synCtx);
        }

        try {
            // Creates a new mappingResourceLoader
            return new MappingResource(inputSchemaStream, outputSchemaStream, configFileInputStream, outputType);
        } catch (SchemaException | JSException e) {
            handleException(e.getMessage(), synCtx);
        }
        return null;
    }

    /**
     * Retrieve property values and insert into a map
     *
     * @param propertiesNamesList Required properties
     * @param synCtx              Message context
     * @return Map filed with property name and the value
     */
    private Map<String, Map<String, Object>> getPropertiesMap(List<String> propertiesNamesList, MessageContext synCtx) {
        Map<String, Map<String, Object>> propertiesMap = new HashMap<>();
        String[] contextAndName;
        Object value;
        org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        HashMap functionProperties = new HashMap();
        Stack<TemplateContext> templeteContextStack = ((Stack) synCtx
                .getProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK));
        if (templeteContextStack != null && !templeteContextStack.isEmpty()) {
            TemplateContext templateContext = templeteContextStack.peek();
            functionProperties.putAll(templateContext.getMappedValues());
        }
        for (String propertyName : propertiesNamesList) {
            contextAndName = propertyName.split("\\['|'\\]");
            switch (contextAndName[0].toUpperCase()) {
            case DEFAULT_CONTEXT:
            case SYNAPSE_CONTEXT:
                value = synCtx.getProperty(contextAndName[1]);
                break;
            case TRANSPORT_CONTEXT:
                value = ((Map) axis2MsgCtx.getProperty(TRANSPORT_HEADERS)).get(contextAndName[1]);
                break;
            case AXIS2_CONTEXT:
                value = axis2MsgCtx.getProperty(contextAndName[1]);
                break;
            case AXIS2_CLIENT_CONTEXT:
                value = axis2MsgCtx.getOptions().getProperty(contextAndName[1]);
                break;
            case OPERATIONS_CONTEXT:
                value = axis2MsgCtx.getOperationContext().getProperty(contextAndName[1]);
                break;
            case FUNCTION_CONTEXT:
                value = functionProperties.get(contextAndName[1]);
                break;
            default:
                log.warn(contextAndName[0] + " scope is not found. Setting it to an empty value.");
                value = EMPTY_STRING;
            }
            if (value == null) {
                log.warn(propertyName + "not found. Setting it to an empty value.");
                value = EMPTY_STRING;
            }
            insertToMap(propertiesMap, contextAndName, value);
        }

        return propertiesMap;
    }

    /**
     * Insert a given value to the properties map
     *
     * @param propertiesMap  Reference to the properties map
     * @param contextAndName Context and the name of the property
     * @param value          Current value of the property
     */
    private void insertToMap(Map<String, Map<String, Object>> propertiesMap, String[] contextAndName, Object value) {
        Map<String, Object> insideMap = propertiesMap.get(contextAndName[0]);
        if (insideMap == null) {
            insideMap = new HashMap();
            propertiesMap.put(contextAndName[0], insideMap);
        }
        insideMap.put(contextAndName[1], value);
    }
}
