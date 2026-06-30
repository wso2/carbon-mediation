/**
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.commons.MiscellaneousUtil;

import javax.xml.namespace.QName;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Parses and resolves {@code ws.proxyProfiles} configuration for the WebSocket transport sender.
 * <p>
 * At construction time all {@code <profile>} entries inside the {@code ws.proxyProfiles} parameter
 * element are parsed and validated. Invalid entries are skipped with a warning. Per-connection,
 * {@link #resolveProxyHandler(String)} matches the target host against the loaded profiles and
 * returns a ready-to-use Netty {@link HttpProxyHandler}, or {@code null} for a direct connection.
 */
class WsProxyProfileRegistry {

    private static final Log log = LogFactory.getLog(WsProxyProfileRegistry.class);

    private static final QName Q_PROFILE       = new QName("profile");
    private static final QName Q_TARGET_HOSTS  = new QName("targetHosts");
    private static final QName Q_PROXY_HOST    = new QName("proxyHost");
    private static final QName Q_PROXY_PORT    = new QName("proxyPort");
    private static final QName Q_PROXY_USERNAME = new QName("proxyUserName");
    private static final QName Q_PROXY_PASSWORD = new QName("proxyPassword");
    private static final QName Q_BYPASS        = new QName("bypass");

    /**
     * Holds the resolved proxy settings for a single profile entry.
     * Immutable after construction and shared across threads via the profile maps.
     */
    static class WsProxyProfileConfig {
        final String proxyHost;
        final int proxyPort;
        final String proxyUsername;
        final String proxyPassword;
        final Set<String> bypass;

        /**
         * @param proxyHost     proxy server hostname or IP
         * @param proxyPort     proxy server port (1–65535)
         * @param proxyUsername proxy username, or {@code null} for anonymous access
         * @param proxyPassword proxy password; ignored when {@code proxyUsername} is {@code null}
         * @param bypass        set of regex patterns for hosts that bypass this proxy
         */
        WsProxyProfileConfig(String proxyHost, int proxyPort,
                             String proxyUsername, String proxyPassword,
                             Set<String> bypass) {
            this.proxyHost     = proxyHost;
            this.proxyPort     = proxyPort;
            this.proxyUsername = proxyUsername;
            this.proxyPassword = proxyPassword;
            this.bypass        = bypass;
        }
    }

    private final Map<String, WsProxyProfileConfig> proxyProfileMap = new LinkedHashMap<>();
    private final Set<String> knownDirectHosts =
            Collections.synchronizedSet(new HashSet<>());
    private final Map<String, WsProxyProfileConfig> knownProxyConfigMap =
            new ConcurrentHashMap<>();

    /**
     * Parses all {@code <profile>} children of {@code profilesElement} into
     * {@link #proxyProfileMap}. Invalid or incomplete profiles are skipped with a warning.
     *
     * @param profilesElement the {@code ws.proxyProfiles} parameter element from axis2.xml
     * @param secretResolver  resolver for SecureVault aliases in proxy passwords; may be {@code null}
     */
    WsProxyProfileRegistry(OMElement profilesElement, SecretResolver secretResolver) {
        parseProfiles(profilesElement, secretResolver);
    }

    /**
     * Returns a configured {@link HttpProxyHandler} for the given backend host, or {@code null}
     * for a direct connection.
     *
     * @param targetHost the backend WebSocket host (e.g., {@code backend.example.com})
     * @return a ready-to-use {@link HttpProxyHandler}, or {@code null} for a direct connection
     */
    HttpProxyHandler resolveProxyHandler(String targetHost) {
        WsProxyProfileConfig matchedProfile = getProfileForHost(targetHost);
        if (matchedProfile != null) {
            return buildProxyHandler(matchedProfile.proxyHost, matchedProfile.proxyPort,
                    matchedProfile.proxyUsername, matchedProfile.proxyPassword);
        }
        return null;
    }

    /**
     * Parses all {@code <profile>} children of {@code profilesElement} and populates
     * {@link #proxyProfileMap}. Each target-host pattern from {@code <targetHosts>} becomes
     * a key mapping to its resolved {@link WsProxyProfileConfig}. Proxy passwords referencing
     * SecureVault aliases are resolved via {@code secretResolver}. Invalid or incomplete
     * profiles are skipped with a warning log.
     *
     * @param profilesElement the {@code ws.proxyProfiles} parameter element from axis2.xml
     * @param secretResolver  resolver for SecureVault aliases in proxy passwords; may be {@code null}
     */
    private void parseProfiles(OMElement profilesElement, SecretResolver secretResolver) {
        Iterator<?> profiles = profilesElement.getChildrenWithName(Q_PROFILE);
        while (profiles.hasNext()) {
            OMElement profile = (OMElement) profiles.next();
            OMElement targetHostsElement = profile.getFirstChildWithName(Q_TARGET_HOSTS);
            if (targetHostsElement == null || targetHostsElement.getText().trim().isEmpty()) {
                log.warn("Skipping ws proxy profile: missing or empty <targetHosts>");
                continue;
            }
            OMElement proxyHostElement = profile.getFirstChildWithName(Q_PROXY_HOST);
            OMElement proxyPortElement = profile.getFirstChildWithName(Q_PROXY_PORT);
            if (proxyHostElement == null || proxyPortElement == null) {
                log.warn("Skipping ws proxy profile for [" + targetHostsElement.getText()
                        + "]: missing <proxyHost> or <proxyPort>");
                continue;
            }
            String proxyHost = proxyHostElement.getText().trim();
            if (proxyHost.isEmpty()) {
                log.warn("Skipping ws proxy profile for [" + targetHostsElement.getText()
                        + "]: empty <proxyHost>");
                continue;
            }
            int proxyPort;
            String proxyPortText = proxyPortElement.getText().trim();
            try {
                proxyPort = Integer.parseInt(proxyPortText);
            } catch (NumberFormatException e) {
                log.warn("Skipping ws proxy profile for [" + targetHostsElement.getText()
                        + "]: invalid <proxyPort> value '" + proxyPortText + "'");
                continue;
            }
            if (proxyPort < 1 || proxyPort > 65535) {
                log.warn("Skipping ws proxy profile for [" + targetHostsElement.getText()
                        + "]: invalid <proxyPort> value '" + proxyPortText + "'");
                continue;
            }
            String proxyUsername = null;
            String proxyPassword = null;
            OMElement usernameElement = profile.getFirstChildWithName(Q_PROXY_USERNAME);
            OMElement passwordElement = profile.getFirstChildWithName(Q_PROXY_PASSWORD);
            if (usernameElement != null) {
                proxyUsername = usernameElement.getText().trim();
                proxyPassword = passwordElement != null
                        ? MiscellaneousUtil.resolve(passwordElement.getText().trim(), secretResolver)
                        : "";
            }
            Set<String> bypassSet = new HashSet<>();
            OMElement bypassElement = profile.getFirstChildWithName(Q_BYPASS);
            if (bypassElement != null && !bypassElement.getText().trim().isEmpty()) {
                for (String rawEntry : bypassElement.getText().split(",")) {
                    String bypassPattern = rawEntry.trim();
                    try {
                        Pattern.compile(bypassPattern);
                        bypassSet.add(bypassPattern);
                    } catch (PatternSyntaxException e) {
                        log.warn("Skipping invalid bypass regex '" + bypassPattern
                                + "' in ws proxy profile for [" + targetHostsElement.getText()
                                + "]: " + e.getMessage());
                    }
                }
            }
            WsProxyProfileConfig profileConfig =
                    new WsProxyProfileConfig(proxyHost, proxyPort, proxyUsername, proxyPassword, bypassSet);
            for (String targetHostPattern : targetHostsElement.getText().split(",")) {
                targetHostPattern = targetHostPattern.trim();
                if (!"*".equals(targetHostPattern)) {
                    try {
                        Pattern.compile(targetHostPattern);
                    } catch (PatternSyntaxException e) {
                        log.warn("Skipping invalid targetHost regex '" + targetHostPattern
                                + "' in ws proxy profile: " + e.getMessage());
                        continue;
                    }
                }
                if (!proxyProfileMap.containsKey(targetHostPattern)) {
                    proxyProfileMap.put(targetHostPattern, profileConfig);
                } else {
                    log.warn("Duplicate ws proxy profile for targetHost [" + targetHostPattern
                            + "] — ignoring");
                }
            }
        }
        if (!proxyProfileMap.isEmpty()) {
            log.info("ws proxy profiles loaded for " + proxyProfileMap.size() + " targetHost(s)");
        }
    }

    /**
     * Selects the proxy profile for the given target host using the following precedence:
     * <ol>
     *   <li>Returns the cached result from {@link #knownProxyConfigMap} if already resolved.</li>
     *   <li>Returns {@code null} (direct) if the host is already in {@link #knownDirectHosts}.</li>
     *   <li>Iterates {@link #proxyProfileMap}: matches specific patterns first (Java regex via
     *       {@link String#matches}), then falls back to the {@code "*"} default profile if present.</li>
     * </ol>
     * The bypass check is delegated to {@link #resolveWithBypass(String, String)}, which also
     * populates the caches so subsequent lookups for the same host return immediately.
     * <p>
     * Note: {@code targetHosts} patterns use Java regex syntax, not glob. The wildcard default
     * profile must be configured as {@code <targetHosts>*</targetHosts>} (literal asterisk).
     *
     * @param targetHost the backend WebSocket host to match against configured profiles
     * @return the matching {@link WsProxyProfileConfig}, or {@code null} if the host should connect directly
     */
    private WsProxyProfileConfig getProfileForHost(String targetHost) {
        if (knownProxyConfigMap.containsKey(targetHost)) {
            return knownProxyConfigMap.get(targetHost);
        }
        if (knownDirectHosts.contains(targetHost)) {
            return null;
        }
        boolean hasCatchAllProfile = false;
        for (String profileKey : proxyProfileMap.keySet()) {
            if ("*".equals(profileKey)) {
                hasCatchAllProfile = true;
                continue;
            }
            if (targetHost.matches(profileKey)) {
                return resolveWithBypass(targetHost, profileKey);
            }
        }
        if (hasCatchAllProfile) {
            return resolveWithBypass(targetHost, "*");
        }
        return null;
    }

    /**
     * Checks the bypass list of the matched profile and caches the outcome.
     * <p>
     * If any bypass pattern matches {@code targetHost}, the host is added to
     * {@link #knownDirectHosts} and {@code null} is returned for a direct connection.
     * Otherwise the profile is cached in {@link #knownProxyConfigMap} and returned.
     *
     * @param targetHost the backend WebSocket host that matched the profile keyed by {@code profileKey}
     * @param profileKey the key in {@link #proxyProfileMap} whose profile was matched ({@code "*"} for the default)
     * @return the matched {@link WsProxyProfileConfig} to proxy through, or {@code null} to connect directly
     */
    private WsProxyProfileConfig resolveWithBypass(String targetHost, String profileKey) {
        WsProxyProfileConfig matchedProfile = proxyProfileMap.get(profileKey);
        for (String bypassPattern : matchedProfile.bypass) {
            if (targetHost.matches(bypassPattern)) {
                knownDirectHosts.add(targetHost);
                if (log.isDebugEnabled()) {
                    log.debug("ws proxy bypass matched: host=" + targetHost
                            + " bypass=" + bypassPattern);
                }
                return null;
            }
        }
        knownProxyConfigMap.put(targetHost, matchedProfile);
        return matchedProfile;
    }

    /**
     * Constructs a Netty {@link HttpProxyHandler} for the given proxy coordinates.
     * Uses the authenticated constructor when credentials are provided, anonymous otherwise.
     *
     * @param host     proxy server hostname or IP
     * @param port     proxy server port
     * @param username proxy username, or {@code null} for anonymous access
     * @param password proxy password; ignored when {@code username} is {@code null}
     * @return a configured {@link HttpProxyHandler} ready to be added to the Netty pipeline
     */
    private HttpProxyHandler buildProxyHandler(String host, int port,
                                               String username, String password) {
        InetSocketAddress proxyAddress = new InetSocketAddress(host, port);
        if (username != null && !username.isEmpty()) {
            return new HttpProxyHandler(proxyAddress, username, password);
        }
        return new HttpProxyHandler(proxyAddress);
    }
}
