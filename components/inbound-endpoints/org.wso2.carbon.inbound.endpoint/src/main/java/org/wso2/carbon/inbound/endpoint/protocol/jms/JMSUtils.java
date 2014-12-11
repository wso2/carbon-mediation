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

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

/**
 * 
 * Maintain the common methods used by inbound JMS protocol
 *
 */
public class JMSUtils {

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
