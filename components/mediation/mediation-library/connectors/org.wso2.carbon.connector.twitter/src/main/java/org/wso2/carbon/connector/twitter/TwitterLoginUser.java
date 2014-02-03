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

public class TwitterLoginUser extends AbstractConnector {

    private static Log log = LogFactory.getLog(TwitterLoginUser.class);

    public static final String CONSUMER_KEY = "consumerKey";
    public static final String CONSUMER_SECRET = "consumerSecret";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String ACCESS_TOKEN_SECRET = "accessTokenSecret";

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
	try {
	    String consumerKey = TwitterUtils.lookupTemplateParamater(messageContext, CONSUMER_KEY);
	    String consumerSecret = TwitterUtils.lookupTemplateParamater(messageContext,
		    CONSUMER_SECRET);
	    String accessToken = TwitterUtils.lookupTemplateParamater(messageContext, ACCESS_TOKEN);
	    String accessTokenSecret = TwitterUtils.lookupTemplateParamater(messageContext,
		    ACCESS_TOKEN_SECRET);
	    TwitterUtils.storeLoginUser(messageContext, consumerKey, consumerSecret, accessToken,
		    accessTokenSecret);
	    if (log.isDebugEnabled()) {
		log.info("login user status done");
	    }
	} catch (Exception e) {
	    log.error("Failed to login user: " + e.getMessage(), e);
	    TwitterUtils.storeErrorResponseStatus(messageContext, e);
	}
    }
}
