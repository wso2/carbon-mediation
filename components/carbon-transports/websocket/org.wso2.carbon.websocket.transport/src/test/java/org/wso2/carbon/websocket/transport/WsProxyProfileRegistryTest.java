/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import io.netty.handler.proxy.HttpProxyHandler;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link WsProxyProfileRegistry}.
 *
 * Each test builds an {@code OMElement} from an inline XML string using
 * {@link AXIOMUtil#stringToOM(String)} and passes it directly to
 * {@code WsProxyProfileRegistry}. No Netty infrastructure is started — the
 * returned {@link HttpProxyHandler} instances are inspected through their
 * public accessors only.
 */
public class WsProxyProfileRegistryTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static WsProxyProfileRegistry buildRegistry(String xml) throws Exception {
        OMElement element = AXIOMUtil.stringToOM(xml);
        return new WsProxyProfileRegistry(element, null);
    }

    @SuppressWarnings("unchecked")
    private static InetSocketAddress proxyAddress(HttpProxyHandler handler) {
        return handler.proxyAddress();
    }

    // -----------------------------------------------------------------------
    // Basic resolution
    // -----------------------------------------------------------------------

    @Test
    public void testEmptyProfiles_returnsNull() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry("<ws.proxyProfiles/>");
        assertNull(reg.resolveProxyHandler("any.host.com"));
    }

    @Test
    public void testAnonymousProfile_matchingHost_proxied() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>backend\\.example\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        HttpProxyHandler handler = reg.resolveProxyHandler("backend.example.com");
        assertNotNull(handler);
        assertEquals("proxy.corp.com", proxyAddress(handler).getHostString());
        assertEquals(3128, proxyAddress(handler).getPort());
        assertEquals("none", handler.authScheme());
    }

    @Test
    public void testAuthenticatedProfile_credentialsForwarded() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>backend\\.example\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "    <proxyUserName>proxyuser</proxyUserName>" +
                "    <proxyPassword>proxypass</proxyPassword>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        HttpProxyHandler handler = reg.resolveProxyHandler("backend.example.com");
        assertNotNull(handler);
        assertEquals("proxy.corp.com", proxyAddress(handler).getHostString());
        assertEquals(3128, proxyAddress(handler).getPort());
        assertEquals("basic", handler.authScheme());
        assertEquals("proxyuser", handler.username());
        assertEquals("proxypass", handler.password());
    }

    @Test
    public void testNonMatchingHost_directConnection() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>backend\\.example\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNull(reg.resolveProxyHandler("other.host.com"));
    }

    // -----------------------------------------------------------------------
    // Catch-all and profile precedence
    // -----------------------------------------------------------------------

    @Test
    public void testCatchAllProfile_matchesAnyHost() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>*</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNotNull(reg.resolveProxyHandler("any.random.host.com"));
        assertNotNull(reg.resolveProxyHandler("another.host"));
    }

    @Test
    public void testSpecificPatternBeats_catchAll() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>*</targetHosts>" +
                "    <proxyHost>default.proxy</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "  </profile>" +
                "  <profile>" +
                "    <targetHosts>special\\.host\\.com</targetHosts>" +
                "    <proxyHost>special.proxy</proxyHost>" +
                "    <proxyPort>8080</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        HttpProxyHandler specialHandler = reg.resolveProxyHandler("special.host.com");
        assertNotNull(specialHandler);
        assertEquals("special.proxy", proxyAddress(specialHandler).getHostString());
        assertEquals(8080, proxyAddress(specialHandler).getPort());

        HttpProxyHandler defaultHandler = reg.resolveProxyHandler("other.host.com");
        assertNotNull(defaultHandler);
        assertEquals("default.proxy", proxyAddress(defaultHandler).getHostString());
    }

    @Test
    public void testDeclarationOrderDeterminesFirstMatch() throws Exception {
        // Both patterns match internal.corp.com; first declared wins.
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>.*\\.corp\\.com</targetHosts>" +
                "    <proxyHost>first.proxy</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "  </profile>" +
                "  <profile>" +
                "    <targetHosts>internal\\.corp\\.com</targetHosts>" +
                "    <proxyHost>second.proxy</proxyHost>" +
                "    <proxyPort>8080</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        HttpProxyHandler handler = reg.resolveProxyHandler("internal.corp.com");
        assertNotNull(handler);
        assertEquals("first.proxy", proxyAddress(handler).getHostString());
    }

    @Test
    public void testMultipleTargetHostsInSingleProfile_eachResolved() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>host1\\.com, host2\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNotNull(reg.resolveProxyHandler("host1.com"));
        assertNotNull(reg.resolveProxyHandler("host2.com"));
        assertNull(reg.resolveProxyHandler("host3.com"));
    }

    // -----------------------------------------------------------------------
    // Bypass
    // -----------------------------------------------------------------------

    @Test
    public void testBypassHost_directConnection() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>.*\\.example\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "    <bypass>direct\\.example\\.com</bypass>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNull(reg.resolveProxyHandler("direct.example.com"));
        assertNotNull(reg.resolveProxyHandler("proxied.example.com"));
    }

    @Test
    public void testBypassUnderCatchAll_directConnection() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>*</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "    <bypass>local\\.internal</bypass>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNull(reg.resolveProxyHandler("local.internal"));
        assertNotNull(reg.resolveProxyHandler("external.host.com"));
    }

    // -----------------------------------------------------------------------
    // Caching
    // -----------------------------------------------------------------------

    @Test
    public void testProxiedHost_secondCallReturnsSameProxyConfig() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>backend\\.example\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        HttpProxyHandler first = reg.resolveProxyHandler("backend.example.com");
        HttpProxyHandler second = reg.resolveProxyHandler("backend.example.com");
        assertNotNull(first);
        assertNotNull(second);
        assertEquals(proxyAddress(first).getHostString(), proxyAddress(second).getHostString());
        assertEquals(proxyAddress(first).getPort(), proxyAddress(second).getPort());
    }

    @Test
    public void testBypassedHost_secondCallAlsoReturnsDirect() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>*</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "    <bypass>direct\\.example\\.com</bypass>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNull(reg.resolveProxyHandler("direct.example.com"));
        assertNull(reg.resolveProxyHandler("direct.example.com")); // served from cache
    }

    // -----------------------------------------------------------------------
    // Profile validation — missing / empty required fields
    // -----------------------------------------------------------------------

    @Test
    public void testMissingTargetHosts_profileSkipped() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNull(reg.resolveProxyHandler("any.host.com"));
    }

    @Test
    public void testWhitespaceOnlyTargetHosts_profileSkipped() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>   </targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNull(reg.resolveProxyHandler("any.host.com"));
    }

    @Test
    public void testMissingProxyHost_profileSkipped() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>backend\\.example\\.com</targetHosts>" +
                "    <proxyPort>3128</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNull(reg.resolveProxyHandler("backend.example.com"));
    }

    @Test
    public void testMissingProxyPort_profileSkipped() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>backend\\.example\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNull(reg.resolveProxyHandler("backend.example.com"));
    }

    @Test
    public void testWhitespaceOnlyProxyHost_profileSkipped() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>backend\\.example\\.com</targetHosts>" +
                "    <proxyHost>   </proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNull(reg.resolveProxyHandler("backend.example.com"));
    }

    // -----------------------------------------------------------------------
    // Profile validation — port range checks
    // -----------------------------------------------------------------------

    @Test
    public void testNonNumericPort_profileSkipped() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>backend\\.example\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>notaport</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNull(reg.resolveProxyHandler("backend.example.com"));
    }

    @Test
    public void testPortZero_profileSkipped() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>backend\\.example\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>0</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNull(reg.resolveProxyHandler("backend.example.com"));
    }

    @Test
    public void testNegativePort_profileSkipped() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>backend\\.example\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>-1</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNull(reg.resolveProxyHandler("backend.example.com"));
    }

    @Test
    public void testPortAboveRange_profileSkipped() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>backend\\.example\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>65536</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNull(reg.resolveProxyHandler("backend.example.com"));
    }

    @Test
    public void testBoundaryPorts_valid() throws Exception {
        WsProxyProfileRegistry reg1 = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>host1\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>1</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNotNull(reg1.resolveProxyHandler("host1.com"));
        assertEquals(1, proxyAddress(reg1.resolveProxyHandler("host1.com")).getPort());

        WsProxyProfileRegistry reg2 = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>host2\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>65535</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNotNull(reg2.resolveProxyHandler("host2.com"));
        assertEquals(65535, proxyAddress(reg2.resolveProxyHandler("host2.com")).getPort());
    }

    // -----------------------------------------------------------------------
    // Profile validation — regex errors
    // -----------------------------------------------------------------------

    @Test
    public void testInvalidTargetHostRegex_validPatternsStillLoaded() throws Exception {
        // The profile has two comma-separated targetHosts; only the invalid one is dropped.
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>valid\\.host\\.com, [invalid</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNotNull(reg.resolveProxyHandler("valid.host.com"));
    }

    @Test
    public void testInvalidBypassRegex_validBypassStillApplied() throws Exception {
        // One bypass entry is invalid; the remaining valid entry still bypasses.
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>.*\\.example\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "    <bypass>[invalid,direct\\.example\\.com</bypass>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        assertNull(reg.resolveProxyHandler("direct.example.com"));
        assertNotNull(reg.resolveProxyHandler("other.example.com"));
    }

    // -----------------------------------------------------------------------
    // Duplicate target hosts
    // -----------------------------------------------------------------------

    @Test
    public void testDuplicateTargetHost_firstProfileWins() throws Exception {
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>backend\\.example\\.com</targetHosts>" +
                "    <proxyHost>first.proxy</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "  </profile>" +
                "  <profile>" +
                "    <targetHosts>backend\\.example\\.com</targetHosts>" +
                "    <proxyHost>second.proxy</proxyHost>" +
                "    <proxyPort>9999</proxyPort>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        HttpProxyHandler handler = reg.resolveProxyHandler("backend.example.com");
        assertNotNull(handler);
        assertEquals("first.proxy", proxyAddress(handler).getHostString());
    }

    // -----------------------------------------------------------------------
    // Credentials edge cases
    // -----------------------------------------------------------------------

    @Test
    public void testUsernameWithoutPassword_emptyPasswordUsed() throws Exception {
        // Code sets password to "" when <proxyUserName> is present but <proxyPassword> is absent.
        WsProxyProfileRegistry reg = buildRegistry(
                "<ws.proxyProfiles>" +
                "  <profile>" +
                "    <targetHosts>backend\\.example\\.com</targetHosts>" +
                "    <proxyHost>proxy.corp.com</proxyHost>" +
                "    <proxyPort>3128</proxyPort>" +
                "    <proxyUserName>user</proxyUserName>" +
                "  </profile>" +
                "</ws.proxyProfiles>");
        HttpProxyHandler handler = reg.resolveProxyHandler("backend.example.com");
        assertNotNull(handler);
        assertEquals("basic", handler.authScheme());
        assertEquals("user", handler.username());
        assertEquals("", handler.password());
    }
}
