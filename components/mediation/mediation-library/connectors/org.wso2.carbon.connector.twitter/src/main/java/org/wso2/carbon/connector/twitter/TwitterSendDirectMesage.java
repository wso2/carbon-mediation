/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.connector.twitter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

import twitter4j.Twitter;

public class TwitterSendDirectMesage extends AbstractConnector {

    private static Log log = LogFactory.getLog(TwitterSendDirectMesage.class);

    public static final String USER_ID = "userID";
    public static final String MESSAGE = "message";
    public static final String SCREEN_NAME = "screenName";

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
	try {
	    String userID = TwitterUtils.lookupTemplateParamater(messageContext, USER_ID);
	    String screenName = TwitterUtils.lookupTemplateParamater(messageContext, SCREEN_NAME);
	    String message = TwitterUtils.lookupTemplateParamater(messageContext, MESSAGE);
	    Twitter twitter = new TwitterClientLoader(messageContext).loadApiClient();
	    if (userID != null && userID.isEmpty()) {
		twitter.sendDirectMessage(Long.parseLong(userID), message);
	    }

	    if (screenName != null && !screenName.isEmpty()) {
		twitter.sendDirectMessage(screenName, message);
	    }

	    if (log.isDebugEnabled()) {
		log.info("sending direct message to user completed!");
	    }
	} catch (Exception e) {
	    log.error("Failed to login user: " + e.getMessage(), e);
	    TwitterUtils.storeErrorResponseStatus(messageContext, e);
	}
    }
}
