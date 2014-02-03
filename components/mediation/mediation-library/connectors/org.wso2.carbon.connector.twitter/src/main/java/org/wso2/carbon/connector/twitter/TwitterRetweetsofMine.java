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

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.codehaus.jettison.json.JSONException;
import org.wso2.carbon.connector.core.ConnectException;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

public class TwitterRetweetsofMine extends AbstractTwitterConnector {

    private static Log log = LogFactory.getLog(TwitterRetweetsofMine.class);

    public static final String USER_ID = "userID";

    public static final String PAGE = "page";

    public static final String SCREEN_NAME = "screenName";

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
	if (log.isDebugEnabled()) {
	    log.info("executing twitter get user time line");
	}

	try {
	    String page = TwitterUtils.lookupTemplateParamater(messageContext, PAGE);

	    Twitter twitter = new TwitterClientLoader(messageContext).loadApiClient();

	    List<Status> results = null;
	    if (page != null && !page.isEmpty()) {
		results = twitter.getRetweetsOfMe(new Paging(Long.parseLong(page)));
	    } else {
		results = twitter.getRetweetsOfMe();
	    }
	    OMElement element = this.performSearch(results);

	    super.preparePayload(messageContext, element);

	} catch (TwitterException te) {
	    log.error("Failed to search twitter : " + te.getMessage(), te);
	    TwitterUtils.storeErrorResponseStatus(messageContext, te);
	} catch (Exception te) {
	    log.error("Failed to search generic: " + te.getMessage(), te);
	    TwitterUtils.storeErrorResponseStatus(messageContext, te);
	}
    }

    /**
     * Performing the searching operation for the given Geo Query criteria.
     * 
     * @param twitter
     * @param query
     * @return
     * @throws XMLStreamException
     * @throws TwitterException
     * @throws JSONException
     * @throws IOException
     */
    private OMElement performSearch(List<Status> results) throws XMLStreamException,
	    TwitterException, JSONException, IOException {
   
    OMElement resultElement = AXIOMUtil.stringToOM("<jsonObject><statuses/></jsonObject>");
    OMElement childElment =  resultElement.getFirstElement();
	
	for (Status place : results) {
	    StringBuilder stringBuilder = new StringBuilder();
	    stringBuilder.append("{ \"status\" : ");
	    String json = DataObjectFactory.getRawJSON(place);
	    stringBuilder.append(json);
	    stringBuilder.append("} ");
	    // System.out.println(stringBuilder.toString());
	    OMElement element = super.parseJsonToXml(stringBuilder.toString());
	    childElment.addChild(element.getFirstOMChild());
	}
	
	if(results.size()==0){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{ \"status\" :{} ");
		stringBuilder.append("} ");
		OMElement element = super.parseJsonToXml(stringBuilder.toString());
		resultElement.addChild(element);
	}
	
	return resultElement;

    }

}
