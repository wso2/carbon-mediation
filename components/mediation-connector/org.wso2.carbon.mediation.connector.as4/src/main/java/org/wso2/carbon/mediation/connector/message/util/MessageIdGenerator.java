/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediation.connector.message.util;

import org.wso2.carbon.mediation.connector.AS4Constants;

import java.util.UUID;

public class MessageIdGenerator {

    public static String createMessageId() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }

    public static String generateHrefForAttachment(String cid) {
        return AS4Constants.ATTACHMENT_HREF_PREFIX + cid;
    }

    public static String generateHrefForSOAPBodyPayload(String id) {
        return AS4Constants.SOAP_BODY_HREF_PREFIX + id;
    }

    public static String generateConversationId() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }
}
