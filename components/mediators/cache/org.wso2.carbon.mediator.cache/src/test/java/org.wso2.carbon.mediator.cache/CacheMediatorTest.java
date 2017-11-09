/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediator.cache;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.SynapseConfigUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

import java.util.Arrays;
import java.util.Properties;

/**
 * Test the functionality of the {@link CacheMediatorFactory} and the {@link CacheMediatorSerializer}
 */
public class CacheMediatorTest extends XMLTestCase {

    private static final String mediatorXml =
            "<cache xmlns=\"http://ws.apache.org/ns/synapse\" collector=\"false\" timeout=\"60\" " +
                    "maxMessageSize=\"1000\">\n" +
                    "            <onCacheHit>\n" +
                    "               <log>\n" +
                    "                  <property name=\"name\" value=\"Riyafa\"/>\n" +
                    "               </log>\n" +
                    "               <respond/>\n" +
                    "            </onCacheHit>\n" +
                    "            <protocol type=\"HTTP\">\n" +
                    "               <methods>POST, GET</methods>\n" +
                    "               <headersToExcludeInHash>ab, abc</headersToExcludeInHash>\n" +
                    "               <responseCodes>2|5[0-9][0-9]</responseCodes>\n" +
                    "               <hashGenerator>org.wso2.carbon.mediator.cache.digest" +
                    ".HttpRequestHashGenerator</hashGenerator>\n" +
                    "            </protocol>\n" +
                    "            <implementation maxSize=\"20\"/>\n" +
                    "         </cache>";

    public CacheMediatorTest(String name) {
        super(name);
    }

    public void testMediatorFactory() {
        OMElement mediatorElement = SynapseConfigUtils.stringToOM(mediatorXml);

        CacheMediatorFactory factory = new CacheMediatorFactory();
        CacheMediator mediator =
                (CacheMediator) factory.createSpecificMediator(mediatorElement, new Properties());

        assertFalse("Incorrect value for the collector", mediator.isCollector());
        assertEquals("Incorrect value for the timeout", mediator.getTimeout(), 60);
        assertEquals("Incorrect value for the protocol type", mediator.getProtocolType(), "HTTP");
        assertTrue("Incorrect value for the httpMethodsToCache",
                   Arrays.equals(mediator.getHTTPMethodsToCache(), new String[]{"POST", "GET"}));
        assertTrue("Incorrect value for the headersToExcludeInHash",Arrays.equals(mediator.getHeadersToExcludeInHash(), new String[]{"ab", "abc"}));
        assertEquals("Incorrect value for the for the responsecodes",mediator.getResponseCodes(), "2|5[0-9][0-9]");
        assertEquals("Incorrect value for the hashGenerator",mediator.getDigestGenerator().getClass().getName(),
                     "org.wso2.carbon.mediator.cache.digest.HttpRequestHashGenerator");
        assertEquals("Incorrect value for the maxSize",mediator.getInMemoryCacheSize(), 20);
    }


    public void testMediatorSerializer() {
        OMElement mediatorElement = SynapseConfigUtils.stringToOM(mediatorXml);

        CacheMediatorFactory factory = new CacheMediatorFactory();
        CacheMediator mediator =
                (CacheMediator) factory.createSpecificMediator(mediatorElement, new Properties());
        CacheMediatorSerializer serializer = new CacheMediatorSerializer();
        OMElement serializedMediatorElement = serializer.serializeSpecificMediator(mediator);

        XMLUnit.setIgnoreWhitespace(true);

        try {
            assertXMLEqual(serializedMediatorElement.toString(), mediatorXml);
        } catch (Exception ignored) {
        }
    }
}