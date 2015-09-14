/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.inbound.endpoint.protocol.jms;

import java.util.Iterator;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Maintain the common methods used by inbound JMS protocol
 *
 */
public class JMSUtils {

    private static final Log log = LogFactory.getLog(JMSUtils.class);
    
    public static String inferJMSMessageType(Message msg) {
        if(inferTextMessage(msg)) {
            return TextMessage.class.getName();
        } else if(inferByteMessage(msg)) {
            return BytesMessage.class.getName();
        } else if(inferObjectMessage(msg)) {
            return ObjectMessage.class.getName();
        } else if(inferStreamMessage(msg)) {
            return StreamMessage.class.getName();
        } else {
            return null;
        }
    }
    
    public static void convertXMLtoJMSMap(OMElement element, MapMessage message) throws JMSException{

        Iterator itr = element.getChildElements();
        while (itr.hasNext()){
            OMElement elem = (OMElement)itr.next();
            message.setString(elem.getLocalName(),elem.getText());
        }
    }

    /**
     * Set transport headers from the axis message context, into the JMS message
     *
     * @param msgContext the axis message context
     * @param message the JMS Message
     * @throws JMSException on exception
     */
    public static void setTransportHeaders(MessageContext msgContext, Message message)
        throws JMSException {

        Map<?,?> headerMap = (Map<?,?>) msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);

        if (headerMap == null) {
            return;
        }

        for (Object headerName : headerMap.keySet()) {

            String name = (String) headerName;

            if (name.startsWith(JMSConstants.JMSX_PREFIX) &&
                !(name.equals(JMSConstants.JMSX_GROUP_ID) || name.equals(JMSConstants.JMSX_GROUP_SEQ))) {
                continue;
            }

            if (JMSConstants.JMS_COORELATION_ID.equals(name)) {
                message.setJMSCorrelationID(
                        (String) headerMap.get(JMSConstants.JMS_COORELATION_ID));
            } else if (JMSConstants.JMS_DELIVERY_MODE.equals(name)) {
                Object o = headerMap.get(JMSConstants.JMS_DELIVERY_MODE);
                if (o instanceof Integer) {
                    message.setJMSDeliveryMode((Integer) o);
                } else if (o instanceof String) {
                    try {
                        message.setJMSDeliveryMode(Integer.parseInt((String) o));
                    } catch (NumberFormatException nfe) {
                        log.warn("Invalid delivery mode ignored : " + o, nfe);
                    }
                } else {
                    log.warn("Invalid delivery mode ignored : " + o);
                }

            } else if (JMSConstants.JMS_EXPIRATION.equals(name)) {
                message.setJMSExpiration(
                    Long.parseLong((String) headerMap.get(JMSConstants.JMS_EXPIRATION)));
            } else if (JMSConstants.JMS_MESSAGE_ID.equals(name)) {
                message.setJMSMessageID((String) headerMap.get(JMSConstants.JMS_MESSAGE_ID));
            } else if (JMSConstants.JMS_PRIORITY.equals(name)) {
                message.setJMSPriority(
                    Integer.parseInt((String) headerMap.get(JMSConstants.JMS_PRIORITY)));
            } else if (JMSConstants.JMS_TIMESTAMP.equals(name)) {
                message.setJMSTimestamp(
                    Long.parseLong((String) headerMap.get(JMSConstants.JMS_TIMESTAMP)));
            } else if (JMSConstants.JMS_MESSAGE_TYPE.equals(name)) {
                message.setJMSType((String) headerMap.get(JMSConstants.JMS_MESSAGE_TYPE));

            } else {
                Object value = headerMap.get(name);
                if (value instanceof String) {
                    if (name.contains("-")) {
                        if (isHyphenReplaceMode(msgContext)) { // we replace
                            message.setStringProperty(transformHyphenatedString(name), (String) value);
                        } else if (isHyphenDeleteMode(msgContext)) { // we skip
                            continue;
                        } else {
                            message.setStringProperty(name, (String) value);
                        }
                    } else {
                        message.setStringProperty(name, (String) value);
                    }
                } else if (value instanceof Boolean) {
                    message.setBooleanProperty(name, (Boolean) value);
                } else if (value instanceof Integer) {
                    message.setIntProperty(name, (Integer) value);
                } else if (value instanceof Long) {
                    message.setLongProperty(name, (Long) value);
                } else if (value instanceof Double) {
                    message.setDoubleProperty(name, (Double) value);
                } else if (value instanceof Float) {
                    message.setFloatProperty(name, (Float) value);
                }
            }
        }
    }    
    private static boolean isHyphenReplaceMode(MessageContext msgContext) {
        if (msgContext == null) {
            return false;
        }

        String hyphenSupport = (String) msgContext.getProperty(JMSConstants.PARAM_JMS_HYPHEN_MODE);
        if (hyphenSupport != null && hyphenSupport.equals(JMSConstants.HYPHEN_MODE_REPLACE)) {
            return true;
        }

        return false;
    }
    /**
     * This method is to fix ESBJAVA-3687 - certain brokers do not support '-' in JMS property name, in such scenarios
     * we will replace the dash with a special character sequence. This support is configurable and is turned off by
     * default.
     * @return modified string name if broker does not support name format
     */
    private static String transformHyphenatedString(String name) {
        return name.replaceAll("-", JMSConstants.HYPHEN_REPLACEMENT_STR);
    }
    private static boolean isHyphenDeleteMode(MessageContext msgContext) {
        if (msgContext == null) {
            return false;
        }

        String hyphenSupport = (String) msgContext.getProperty(JMSConstants.PARAM_JMS_HYPHEN_MODE);
        if (hyphenSupport != null && hyphenSupport.equals(JMSConstants.HYPHEN_MODE_DELETE)) {
            return true;
        }

        return false;
    }
    private static boolean inferTextMessage(Message msg) {
        if (msg instanceof TextMessage) {
            return true;
        }
        return false;
    }

    private static boolean inferStreamMessage(Message msg) {
        if (msg instanceof StreamMessage) {
            return true;
        }
        return false;
    }

    private static boolean inferObjectMessage(Message msg) {
        if (msg instanceof ObjectMessage) {
            return true;
        }
        return false;
    }

    private static boolean inferByteMessage(Message msg) {
        if (msg instanceof BytesMessage) {
            return true;
        }
        return false;
    }
}
