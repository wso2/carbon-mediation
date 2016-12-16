/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.http2.transport.util;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Headers;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.SOAPMessageFormatter;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.nhttp.util.MessageFormatterDecoratorFactory;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.transport.passthru.config.TargetConfiguration;
import org.apache.synapse.transport.passthru.util.PassThroughTransportUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Http2TargetRequestUtil {
    TargetConfiguration configuration;
    HttpRoute route;
    private URL url;
    /**
     * HTTP Method
     */
    private String method;
    /**
     * HTTP request created for sending the message
     */
    private HttpRequest request = null;
    /**
     * Weather chunk encoding should be used
     */
    private boolean chunk = true;
    /**
     * HTTP version that should be used
     */
    private ProtocolVersion version = null;
    /**
     * Weather full url is used for the request
     */
    private boolean fullUrl = false;
    /**
     * Port to be used for the request
     */
    private int port = 80;
    /**
     * Weather this request has a body
     */
    private boolean hasEntityBody = true;
    /**
     * Keep alive request
     */
    private boolean keepAlive = true;
    private boolean disableChunk = false;
    private String[] http2headerNames = { "method", "authority", "path", "scheme", "status" };
    private Set<String> defaultHttp2Headers = new HashSet(Arrays.asList(http2headerNames));

    public Http2TargetRequestUtil(TargetConfiguration configuration, HttpRoute route) {
        this.configuration = configuration;
        this.route = route;

    }

    private static String getContentType(MessageContext msgCtx,
            boolean isContentTypePreservedHeader) throws AxisFault {

        if (isContentTypePreservedHeader) {
            if (msgCtx.getProperty(Constants.Configuration.CONTENT_TYPE) != null) {
                return (String) msgCtx.getProperty(Constants.Configuration.CONTENT_TYPE);
            } else if (msgCtx.getProperty(Constants.Configuration.MESSAGE_TYPE) != null) {
                return (String) msgCtx.getProperty(Constants.Configuration.MESSAGE_TYPE);
            }
        }

        MessageFormatter formatter = MessageProcessorSelector.getMessageFormatter(msgCtx);
        OMOutputFormat format = PassThroughTransportUtils.getOMOutputFormat(msgCtx);

        if (formatter != null) {
            String contentType = formatter.getContentType(msgCtx, format, msgCtx.getSoapAction());
            //keep the formatter information to prevent multipart boundary override (this will be the content writing to header)
            msgCtx.setProperty(PassThroughConstants.MESSAGE_OUTPUT_FORMAT, format);
            return contentType;

        } else {
            String contentType = (String) msgCtx.getProperty(Constants.Configuration.CONTENT_TYPE);
            if (contentType != null) {
                return contentType;
            } else {
                return new SOAPMessageFormatter()
                        .getContentType(msgCtx, format, msgCtx.getSoapAction());
            }
        }
    }

    public Http2Headers getHeaders(MessageContext msgContext) {
        Http2Headers http2Headers = new DefaultHttp2Headers();
        Map<String, String> reqeustHeaders = new TreeMap<>();

        String httpMethod = (String) msgContext.getProperty(Constants.Configuration.HTTP_METHOD);
        if (httpMethod == null) {
            httpMethod = "POST";
        }
        reqeustHeaders.put(Http2Headers.PseudoHeaderName.METHOD.value().toString(), httpMethod);

        EndpointReference epr = PassThroughTransportUtils.getDestinationEPR(msgContext);
        String targetEPR = epr.getAddress();
        if (targetEPR.toLowerCase().contains("http2://")) {
            targetEPR = targetEPR.replaceFirst("http2://", "http://");
        } else if (targetEPR.toLowerCase().contains("https2://")) {
            targetEPR = targetEPR.replaceFirst("https2://", "https://");
        }
        epr.setAddress(targetEPR);
        URL url = null;
        try {
            url = new URL(epr.getAddress());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //this code block is needed to replace the host header in service chaining with REQUEST_HOST_HEADER
        //adding host header since it is not available in response message.
        //otherwise Host header will not replaced after first call
        if (msgContext.getProperty(NhttpConstants.REQUEST_HOST_HEADER) != null) {
            Object headers = msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
            if (headers != null) {
                Map headersMap = (Map) headers;
                if (!headersMap.containsKey(HTTPConstants.HEADER_HOST)) {
                    headersMap.put(HttpHeaderNames.HOST,
                            msgContext.getProperty(NhttpConstants.REQUEST_HOST_HEADER));
                }
            }
        }

        // headers
        PassThroughTransportUtils.removeUnwantedHeaders(msgContext, configuration);

        Object o = msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);

        if (o != null && o instanceof Map) {
            Map headers = (Map) o;
            for (Object entryObj : headers.entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;
                if (entry.getValue() != null && entry.getKey() instanceof String && entry
                        .getValue() instanceof String) {
                    if (HTTPConstants.HEADER_HOST.equalsIgnoreCase((String) entry.getKey())
                            && !configuration.isPreserveHttpHeader(HTTPConstants.HEADER_HOST)) {
                        if (msgContext.getProperty(NhttpConstants.REQUEST_HOST_HEADER) != null) {
                            reqeustHeaders.put(((String) entry.getKey()).toLowerCase(),
                                    (String) msgContext
                                            .getProperty(NhttpConstants.REQUEST_HOST_HEADER));
                        }

                    } else {
                        if (!defaultHttp2Headers.contains(entry.getKey()))
                            reqeustHeaders.put(((String) entry.getKey()).toLowerCase(),
                                    (String) entry.getValue());
                        else {
                            String keyV = ":" + entry.getKey().toString().toLowerCase();
                            reqeustHeaders.put(keyV, (String) entry.getValue());
                        }
                    }
                }
            }
        }

        String cType = null;
        try {
            cType = getContentType(msgContext,
                    configuration.isPreserveHttpHeader(HTTP.CONTENT_TYPE));
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        if (cType != null && (!httpMethod.equals("GET") && !httpMethod.equals("DELETE"))) {
            String messageType = (String) msgContext.getProperty("messageType");
            if (messageType != null) {
                boolean builderInvoked = false;
                final Pipe pipe = (Pipe) msgContext
                        .getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
                if (pipe != null) {
                    builderInvoked = Boolean.TRUE.equals(msgContext
                            .getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED));
                }

                // if multipart related message type and unless if message
                // not get build we should
                // skip of setting formatter specific content Type
                if (messageType.indexOf(HTTPConstants.MEDIA_TYPE_MULTIPART_RELATED) == -1
                        && messageType.indexOf(HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA)
                        == -1) {
                    Map msgCtxheaders = (Map) o;
                    if (msgCtxheaders != null && !cType.isEmpty()) {
                        msgCtxheaders.put(HTTP.CONTENT_TYPE, cType);
                    }
                    reqeustHeaders.put(HttpHeaderNames.CONTENT_TYPE.toString(), cType);
                }

                // if messageType is related to multipart and if message
                // already built we need to set new
                // boundary related content type at Content-Type header
                if (builderInvoked && ((
                        (messageType.indexOf(HTTPConstants.MEDIA_TYPE_MULTIPART_RELATED) != -1) || (
                                messageType.indexOf(HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA)
                                        != -1)))) {
                    reqeustHeaders.put(HttpHeaderNames.CONTENT_TYPE.toString(), cType);
                }

            } else {
                reqeustHeaders.put(HttpHeaderNames.CONTENT_TYPE.toString(), cType);
            }

        }

        // version
        String forceHttp10 = (String) msgContext.getProperty(PassThroughConstants.FORCE_HTTP_1_0);
        if ("true".equals(forceHttp10)) {
            //request.setVersion(HttpVersion.HTTP_1_0);
        }

        // keep alive
        String noKeepAlie = (String) msgContext.getProperty(PassThroughConstants.NO_KEEPALIVE);
        if ("true".equals(noKeepAlie)) {
            keepAlive = false;
        }

        // port
        port = url.getPort();

        // chunk
        String disableChunking = (String) msgContext
                .getProperty(PassThroughConstants.DISABLE_CHUNKING);
        if ("true".equals(disableChunking)) {
            disableChunk = true;
        }

        // full url
        String fullUr = (String) msgContext.getProperty(PassThroughConstants.FULL_URI);
        if ("true".equals(fullUr)) {
            fullUrl = true;
        }

        // Add excess respsonse header.
        String excessProp = NhttpConstants.EXCESS_TRANSPORT_HEADERS;
        Map excessHeaders = (Map) msgContext.getProperty(excessProp);
        if (excessHeaders != null) {
            for (Iterator iterator = excessHeaders.keySet().iterator(); iterator.hasNext(); ) {
                String key = (String) iterator.next();
                for (String excessVal : (Collection<String>) excessHeaders.get(key)) {
                    reqeustHeaders.put(key.toLowerCase(), (String) excessVal);
                }
            }
        }
        String path = fullUrl || (route.getProxyHost() != null && !route.isTunnelled()) ?
                url.toString() :
                url.getPath() + (url.getQuery() != null ? "?" + url.getQuery() : "");

        if ((("GET").equals(msgContext.getProperty(Constants.Configuration.HTTP_METHOD)))
                || (("DELETE")
                .equals(msgContext.getProperty(Constants.Configuration.HTTP_METHOD)))) {
            try {
                hasEntityBody = false;
                MessageFormatter formatter = MessageProcessorSelector
                        .getMessageFormatter(msgContext);
                OMOutputFormat format = PassThroughTransportUtils.getOMOutputFormat(msgContext);
                if (formatter != null && format != null) {
                    URL _url = formatter.getTargetAddress(msgContext, format, url);
                    if (_url != null && !_url.toString().isEmpty()) {
                        if (msgContext.getProperty(NhttpConstants.POST_TO_URI) != null
                                && Boolean.TRUE.toString()
                                .equals(msgContext.getProperty(NhttpConstants.POST_TO_URI))) {
                            path = _url.toString();
                        } else {
                            path = _url.getPath() + ((_url.getQuery() != null && !_url.getQuery()
                                    .isEmpty()) ? ("?" + _url.getQuery()) : "");
                        }

                    }
                    if (reqeustHeaders.containsKey(HttpHeaderNames.CONTENT_TYPE))
                        reqeustHeaders.remove(HttpHeaderNames.CONTENT_TYPE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //fix for  POST_TO_URI
        if (msgContext.isPropertyTrue(NhttpConstants.POST_TO_URI)) {
            path = url.toString();
        }

        if (path != null || !path.isEmpty()) {
            reqeustHeaders.put(Http2Headers.PseudoHeaderName.PATH.value().toString(), path);
        }

        if (hasEntityBody) {
            long contentLength = -1;
            String contentLengthHeader = null;
            if (reqeustHeaders.containsKey(HttpHeaderNames.CONTENT_LENGTH.toString()) &&
                    Integer.parseInt(reqeustHeaders.get(HttpHeaderNames.CONTENT_LENGTH).toString())
                            > 0) {
                contentLengthHeader = reqeustHeaders.get(HttpHeaderNames.CONTENT_LENGTH).toString();
            }

            if (contentLengthHeader != null) {
                contentLength = Integer.parseInt(contentLengthHeader);
                reqeustHeaders.remove(HttpHeaderNames.CONTENT_LENGTH);
            }

            if (msgContext.getProperty(PassThroughConstants.PASSTROUGH_MESSAGE_LENGTH) != null) {
                contentLength = (Long) msgContext
                        .getProperty(PassThroughConstants.PASSTROUGH_MESSAGE_LENGTH);
            }
            boolean forceContentLength = msgContext
                    .isPropertyTrue(NhttpConstants.FORCE_HTTP_CONTENT_LENGTH);
            boolean forceContentLengthCopy = msgContext
                    .isPropertyTrue(PassThroughConstants.COPY_CONTENT_LENGTH_FROM_INCOMING);

            if (forceContentLength) {
                if (forceContentLengthCopy && contentLength > 0) {
                    reqeustHeaders.put(HttpHeaderNames.CONTENT_LENGTH.toString(),
                            Long.toString(contentLength));
                }
            } else {
                if (contentLength != -1) {
                    reqeustHeaders.put(HttpHeaderNames.CONTENT_LENGTH.toString(),
                            Long.toString(contentLength));
                }
            }
        }

        String soapAction = msgContext.getSoapAction();
        if (soapAction == null) {
            soapAction = msgContext.getWSAAction();
            msgContext.getAxisOperation().getInputAction();
        }

        if (msgContext.isSOAP11() && soapAction != null && soapAction.length() > 0) {
            String existingHeader = reqeustHeaders.get(HTTPConstants.HEADER_SOAP_ACTION).toString();
            if (existingHeader != null) {
                reqeustHeaders.remove(existingHeader);
            }
            MessageFormatter messageFormatter = MessageFormatterDecoratorFactory
                    .createMessageFormatterDecorator(msgContext);
            reqeustHeaders.put(HTTPConstants.HEADER_SOAP_ACTION.toLowerCase(),
                    messageFormatter.formatSOAPAction(msgContext, null, soapAction));
            request.setHeader(HTTPConstants.USER_AGENT.toLowerCase(),
                    "Synapse-PT-HttpComponents-NIO");
        }

        if (reqeustHeaders.containsKey(HttpHeaderNames.HOST)) {
            reqeustHeaders.remove(HttpHeaderNames.HOST);
        }
        if (!reqeustHeaders.containsKey(Http2Headers.PseudoHeaderName.SCHEME.value()))
            reqeustHeaders.put(Http2Headers.PseudoHeaderName.SCHEME.value().toString(),
                    route.getTargetHost().getSchemeName());
        if (!reqeustHeaders.containsKey(Http2Headers.PseudoHeaderName.AUTHORITY.value()))
            reqeustHeaders.put(Http2Headers.PseudoHeaderName.AUTHORITY.value().toString(),
                    route.getTargetHost().toString());
        Iterator<Map.Entry<String, String>> iterator = reqeustHeaders.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> head = iterator.next();
            http2Headers.add(head.getKey(), head.getValue());
        }
        return http2Headers;
    }

    public boolean isHasEntityBody() {
        return hasEntityBody;
    }
}
