/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.connector.twitter;

import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.ConnectException;

import twitter4j.Location;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

public class TwiterGetAvailableTrends extends AbstractTwitterConnector {

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {
		// TODO Auto-generated method stub
		try {
			Twitter twitter = new TwitterClientLoader(messageContext).loadApiClient();
			List<Location> locations = twitter.getAvailableTrends();
			OMElement resultElement = AXIOMUtil.stringToOM("<jsonObject><trends/></jsonObject>");
			for (Location location : locations) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("{ \"trend\" : ");
				String json = DataObjectFactory.getRawJSON(location);
				stringBuilder.append(json);
				stringBuilder.append("} ");
				OMElement element = super.parseJsonToXml(stringBuilder.toString());
				resultElement.addChild(element);
			}

			if (locations.size() == 0) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("{ \"trend\" :{} ");
				stringBuilder.append("} ");
				OMElement element = super.parseJsonToXml(stringBuilder.toString());
				resultElement.addChild(element);
			}

			super.preparePayload(messageContext, resultElement);

		} catch (TwitterException te) {
			log.error("Failed to load  available trends: " + te.getMessage(), te);
			TwitterUtils.storeErrorResponseStatus(messageContext, te);
		} catch (Exception te) {
			// TODO Auto-generated catch block
			log.error("Failed to load  available trends: " + te.getMessage(), te);
			TwitterUtils.storeErrorResponseStatus(messageContext, te);
		}

	}

}
