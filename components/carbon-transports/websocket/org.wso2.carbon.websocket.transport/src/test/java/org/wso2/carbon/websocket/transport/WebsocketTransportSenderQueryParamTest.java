/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.websocket.transport;

import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests merging of the inbound request query string into the deployment time backend url.
 */
public class WebsocketTransportSenderQueryParamTest {

    // targetEPR points at the backend; TRANSPORT_IN_URL is the absolute gateway url that
    // InboundWebsocketSourceHandler builds as scheme + Host header + request uri.
    private static final String EPR = "ws://localhost:8081";

    private String merge(String targetEPR, String inboundUrl) {

        MessageContext msgCtx = new MessageContext();
        if (inboundUrl != null) {
            msgCtx.setProperty(Constants.Configuration.TRANSPORT_IN_URL, inboundUrl);
        }
        return new WebsocketTransportSender().mergeInboundQueryParams(targetEPR, msgCtx);
    }

    @Test
    public void testQueryForwarded() {
        Assert.assertEquals(EPR + "?authToken=secret123", merge(EPR, "ws://localhost:9099/wsecho/1?authToken=secret123"));
    }

    @Test
    public void testMultipleParamsForwarded() {
        Assert.assertEquals(EPR + "?a=1&b=2", merge(EPR, "ws://localhost:9099/wsecho/1?a=1&b=2"));
    }

    @Test
    public void testRepeatedParamsPreserved() {
        Assert.assertEquals(EPR + "?tag=x&tag=y&tag=z", merge(EPR, "ws://localhost:9099/wsecho/1?tag=x&tag=y&tag=z"));
    }

    @Test
    public void testNoInboundQuery() {
        Assert.assertEquals(EPR, merge(EPR, "ws://localhost:9099/wsecho/1"));
    }

    @Test
    public void testEmptyInboundQuery() {
        Assert.assertEquals(EPR, merge(EPR, "ws://localhost:9099/wsecho/1?"));
    }

    @Test
    public void testMissingTransportInUrl() {
        Assert.assertEquals(EPR, merge(EPR, null));
    }

    @Test
    public void testEncodedValuesPassThroughVerbatim() {
        Assert.assertEquals(EPR + "?p=a%2Fb%20c+d", merge(EPR, "ws://localhost:9099/wsecho/1?p=a%2Fb%20c+d"));
    }

    @Test
    public void testQuestionMarkInsideValuePreserved() {
        Assert.assertEquals(EPR + "?p=a?b", merge(EPR, "ws://localhost:9099/wsecho/1?p=a?b"));
    }

    @Test
    public void testEndpointAlreadyHasQuery() {
        Assert.assertEquals(EPR + "?fixed=1&token=x", merge(EPR + "?fixed=1", "ws://localhost:9099/wsecho/1?token=x"));
    }

    @Test
    public void testEndpointEndingWithQuestionMark() {
        Assert.assertEquals(EPR + "?token=x", merge(EPR + "?", "ws://localhost:9099/wsecho/1?token=x"));
    }

    @Test
    public void testEndpointEndingWithAmpersand() {
        Assert.assertEquals(EPR + "?fixed=1&token=x", merge(EPR + "?fixed=1&", "ws://localhost:9099/wsecho/1?token=x"));
    }

    @Test
    public void testEndpointValueWinsOverInbound() {
        Assert.assertEquals(EPR + "?token=fixed", merge(EPR + "?token=fixed", "ws://localhost:9099/wsecho/1?token=caller"));
    }

    @Test
    public void testEmptyPairsSkipped() {
        Assert.assertEquals(EPR + "?a=1&b=2", merge(EPR, "ws://localhost:9099/wsecho/1?a=1&&b=2"));
    }

    @Test
    public void testValuelessParamForwarded() {
        Assert.assertEquals(EPR + "?flag", merge(EPR, "ws://localhost:9099/wsecho/1?flag"));
    }

    @Test
    public void testNullTargetEprReturnedUnchanged() {
        Assert.assertNull(merge(null, "ws://localhost:9099/wsecho/1?token=x"));
    }

    // The query is caller controlled, and java.net.URI rejects characters a caller can send.
    // A merged url that will not parse must be discarded rather than blow up the connection.

    @Test
    public void testUnparsableMergeFallsBackOnSpace() {
        Assert.assertEquals(EPR, merge(EPR, "ws://localhost:9099/wsecho/1?p=hello world"));
    }

    @Test
    public void testUnparsableMergeFallsBackOnPipe() {
        Assert.assertEquals(EPR, merge(EPR, "ws://localhost:9099/wsecho/1?p=x|y"));
    }

    @Test
    public void testUnparsableMergeFallsBackOnBraces() {
        Assert.assertEquals(EPR, merge(EPR, "ws://localhost:9099/wsecho/1?p={id}"));
    }

    // Endpoint precedence must not be bypassed by encoding the parameter name differently.

    @Test
    public void testEncodedNameCannotBypassEndpointPrecedence() {
        Assert.assertEquals(EPR + "?token=fixed",
                merge(EPR + "?token=fixed", "ws://localhost:9099/wsecho/1?%74oken=caller"));
    }

    @Test
    public void testEncodedEndpointNameStillMatches() {
        Assert.assertEquals(EPR + "?%74oken=fixed",
                merge(EPR + "?%74oken=fixed", "ws://localhost:9099/wsecho/1?token=caller"));
    }

    @Test
    public void testSemicolonSeparatedInboundPairsBothForwarded() {
        Assert.assertEquals(EPR + "?a=1&b=2", merge(EPR, "ws://localhost:9099/wsecho/1?a=1;b=2"));
    }

    @Test
    public void testPairValuesKeptVerbatimWhenNameDecodes() {
        Assert.assertEquals(EPR + "?%74oken=a%2Fb", merge(EPR, "ws://localhost:9099/wsecho/1?%74oken=a%2Fb"));
    }
}
