package org.wso2.carbon.mediator.bam.builders;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.bam.PropertyTypeConverter;
import org.wso2.carbon.mediator.bam.config.BamMediatorException;
import org.wso2.carbon.mediator.bam.config.stream.Property;
import org.wso2.carbon.mediator.bam.config.stream.StreamConfiguration;
import org.wso2.carbon.mediator.bam.util.BamMediatorConstants;

public class PayloadDataBuilder {

    private static final Log log = LogFactory.getLog(MetaDataBuilder.class);
    private PropertyTypeConverter propertyTypeConverter = new PropertyTypeConverter();

    public PayloadDataBuilder(){
        //propertyTypeConverter = new PropertyTypeConverter();
    }

    public Object[] createPayloadData(MessageContext messageContext, org.apache.axis2.context.MessageContext msgCtx,
                                      StreamConfiguration streamConfiguration) throws BamMediatorException{
        try{
            int numOfProperties = streamConfiguration.getProperties().size();
            int numOfEntities = streamConfiguration.getEntries().size();
            int i;
            Object[] payloadData = new Object[numOfProperties + numOfEntities + BamMediatorConstants.NUM_OF_CONST_PAYLOAD_PARAMS];
            this.produceAndSetConstantValues(messageContext, msgCtx, payloadData);
            for (i=0; i<numOfProperties; i++) {
                payloadData[BamMediatorConstants.NUM_OF_CONST_PAYLOAD_PARAMS + i] =
                        this.producePropertyValue(streamConfiguration.getProperties().get(i), messageContext);
            }
            for (i=0; i<numOfEntities; i++) {
                payloadData[BamMediatorConstants.NUM_OF_CONST_PAYLOAD_PARAMS + numOfProperties + i] =
                        this.produceEntityValue(streamConfiguration.getEntries().get(i).getValue(), messageContext);
            }
            return payloadData;
        } catch (Exception e) {
            String errorMsg = "Error occurred while producing values for Payload Data. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        }
    }
    
    private void produceAndSetConstantValues(MessageContext messageContext, org.apache.axis2.context.MessageContext msgCtx,
                                             Object[] payloadData) throws BamMediatorException {
        int i = 0;
        boolean direction;
        String service, operation, messageID;
        try{
            try{
                direction = (!messageContext.isResponse() && !messageContext.isFaultResponse());
            } catch (Exception e) {
                String errorMsg = "Error occurred while Message Direction is extracted. " + e.getMessage();
                log.error(errorMsg, e);
                direction = true;
            }
            try{
                service = msgCtx.getAxisService().getName();
            } catch (Exception e) {
                String errorMsg = "Error occurred while Service Name is extracted. " + e.getMessage();
                log.error(errorMsg, e);
                service = "";
            }
            try{
                operation = msgCtx.getAxisOperation().getName().getLocalPart();
            } catch (Exception e) {
                String errorMsg = "Error occurred while Operation Name is extracted. " + e.getMessage();
                log.error(errorMsg, e);
                operation = "";
            }
            try{
                messageID = messageContext.getMessageID();
            } catch (Exception e) {
                String errorMsg = "Error occurred while Message ID is extracted. " + e.getMessage();
                log.error(errorMsg, e);
                messageID = "";
            }
            payloadData[i++] = direction ? BamMediatorConstants.DIRECTION_IN : BamMediatorConstants.DIRECTION_OUT;
            payloadData[i++] = service;
            payloadData[i++] = operation;
            payloadData[i++] = messageID;
            payloadData[i] = System.currentTimeMillis();
        } catch (Exception e) {
            String errorMsg = "Error occurred while producing constant fields for Payload Data. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        }
    }

    private Object producePropertyValue(Property property, MessageContext messageContext){
        try {
            String stringProperty;
            String propertyType;
            if(property.isExpression()){
                SynapseXPath synapseXPath = new SynapseXPath(property.getValue());
                stringProperty = synapseXPath.stringValueOf(messageContext);
            } else {
                stringProperty =  property.getValue();
            }
            propertyType = property.getType();
            if ("STRING".equals(propertyType)){
                return this.propertyTypeConverter.convertToString(stringProperty);
            } else if ("INTEGER".equals(propertyType)) {
                return this.propertyTypeConverter.convertToInt(stringProperty);
            } else if ("FLOAT".equals(propertyType)) {
                return this.propertyTypeConverter.convertToFloat(stringProperty);
            } else if ("DOUBLE".equals(propertyType)) {
                return this.propertyTypeConverter.convertToDouble(stringProperty);
            } else if ("BOOLEAN".equals(propertyType)) {
                return this.propertyTypeConverter.convertToBoolean(stringProperty);
            } else if ("LONG".equals(propertyType)) {
                return this.propertyTypeConverter.convertToLong(stringProperty);
            } else {
                return stringProperty;
            }
        } catch (JaxenException e) {
            String errorMsg = "SynapseXPath cannot be created for the Stream Property. " + e.getMessage();
            log.error(errorMsg, e);
            return null;
        } catch (Exception e) {
            String errorMsg = "Error occurred while converting to data types. " + e.getMessage();
            log.error(errorMsg, e);
            return null;
        }
    }

    private Object produceEntityValue(String valueName, MessageContext messageContext){
        try{
            if(valueName.startsWith("$")){ // When entity value is a mediator parameter
                if("$SOAPHeader".equals(valueName)){
                    return messageContext.getEnvelope().getHeader().toString();
                } else if ("$SOAPBody".equals(valueName)){
                    return messageContext.getEnvelope().getBody().toString();
                } else {
                    return "Invalid Entity Parameter !";
                }
            } else {
                return valueName;
            }
        } catch (Exception e) {
            String errorMsg = "Error occurred while extracting the SOAP header or SOAP body. " + e.getMessage();
            log.error(errorMsg, e);
            return "";
        }
    }
}
