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

import org.apache.synapse.MessageContext;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by IntelliJ IDEA. User: charitha Date: 3/6/12 Time: 2:06 PM To change
 * this template use File | Settings | File Templates.
 */
public class TwitterClientLoader {

    private MessageContext messageContext;

    public TwitterClientLoader(MessageContext ctxt) {
	this.messageContext = ctxt;
    }

    public Twitter loadApiClient() throws TwitterException {
	Twitter twitter;
	if (messageContext.getProperty(TwitterConnectConstants.TWITTER_USER_CONSUMER_KEY) != null
		&& messageContext.getProperty(TwitterConnectConstants.TWITTER_USER_CONSUMER_SECRET) != null
		&& messageContext.getProperty(TwitterConnectConstants.TWITTER_USER_ACCESS_TOKEN) != null
		&& messageContext
			.getProperty(TwitterConnectConstants.TWITTER_USER_ACCESS_TOKEN_SECRET) != null) {
	    ConfigurationBuilder build = new ConfigurationBuilder();
	    build.setJSONStoreEnabled(true);
	    build.setOAuthAccessToken(messageContext.getProperty(
		    TwitterConnectConstants.TWITTER_USER_ACCESS_TOKEN).toString());
	    build.setOAuthAccessTokenSecret(messageContext.getProperty(
		    TwitterConnectConstants.TWITTER_USER_ACCESS_TOKEN_SECRET).toString());
	    build.setOAuthConsumerKey(messageContext.getProperty(
		    TwitterConnectConstants.TWITTER_USER_CONSUMER_KEY).toString());
	    build.setOAuthConsumerSecret(messageContext.getProperty(
		    TwitterConnectConstants.TWITTER_USER_CONSUMER_SECRET).toString());
	    twitter = new TwitterFactory(build.build()).getInstance();
	    twitter.verifyCredentials();
	} else {
	    twitter = new TwitterFactory().getInstance();
	}
	return twitter;
    }

}
