package org.wso2.carbon.http2.transport;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.*;
import io.netty.util.CharsetUtil;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.synapse.transport.http.conn.ProxyConfig;
import org.apache.synapse.transport.nhttp.config.ProxyConfigBuilder;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.config.TargetConfiguration;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.http2.transport.util.Http2ClientHandler;
import org.wso2.carbon.http2.transport.util.Http2ConnectionFactory;
import org.wso2.carbon.http2.transport.util.Http2Constants;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Http2TransportSender extends AbstractTransportSender {
    private Http2ConnectionFactory connectionFactory;
    private ProxyConfig proxyConfig;

    private TargetConfiguration targetConfiguration;
    private static final Log log = LogFactory.getLog(Http2TransportSender.class);

    public void init(ConfigurationContext cfgCtx, TransportOutDescription transportOut)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Initializing Http2 Connection Factory.");
        }
        super.init(cfgCtx, transportOut);
        connectionFactory = Http2ConnectionFactory.getInstance(transportOut);
        proxyConfig = new ProxyConfigBuilder().build(transportOut);
        log.info(proxyConfig.logProxyConfig());
        targetConfiguration = new TargetConfiguration(cfgCtx,
                transportOut, null, null,
                proxyConfig.createProxyAuthenticator());
        targetConfiguration.build();

    }

    public void sendMessage(MessageContext msgCtx, String targetEPR, OutTransportInfo trpOut)
            throws AxisFault {
        try {
            if (targetEPR.toLowerCase().contains("http2")) {
                targetEPR = targetEPR.replaceFirst("http2", "http");
            } else if (targetEPR.toLowerCase().contains("https2")) {
                targetEPR = targetEPR.replaceFirst("https2", "https");
            }
            URI uri = new URI(targetEPR);
            String scheme = uri.getScheme() != null ? uri.getScheme() : "http";
            String hostname = uri.getHost();
            int port = uri.getPort();
            if (port == -1) {
                // use default
                if ("http".equals(scheme)) {
                    port = 80;
                } else if ("https".equals(scheme)) {
                    port = 443;
                }
            }
            HttpHost target = new HttpHost(hostname, port, scheme);
            boolean secure = "https".equalsIgnoreCase(target.getSchemeName());

            msgCtx.setProperty(PassThroughConstants.PROXY_PROFILE_TARGET_HOST, target.getHostName());

            if (log.isDebugEnabled()) {
                log.debug("Fetching a Connection from the Http2(Https2) Connection Factory.");
            }
            Http2ClientHandler clientHandler = connectionFactory.getChannelHandler(uri);

            clientHandler.setTargetConfig(targetConfiguration);
            //For steaming data
            int streamId;
            if (msgCtx.getProperty(Http2Constants.HTTP2_DATA_FRAME_PRESENT) != null
                    && msgCtx.getProperty(Http2Constants.HTTP2_DATA_FRAME_PRESENT).equals(true)) {
                Http2DataFrame frame = (Http2DataFrame) msgCtx.getProperty(Http2Constants.HTTP2_DATA_FRAME);
                if (log.isDebugEnabled()) {
                    log.debug("Sending the data frame to the Http2 server on channel id : "
                            + clientHandler.getChannel().toString());
                }
                streamId = clientHandler.getStreamId();
                Channel channel = clientHandler.getChannel();
                if (channel.isActive()) {
                    clientHandler.setRequest(streamId, msgCtx);
                    clientHandler.put(streamId, frame.retain());
                }
            } else {
                RelayUtils.buildMessage(msgCtx, false);
                OMOutputFormat format = BaseUtils.getOMOutputFormat(msgCtx);
                MessageFormatter messageFormatter =
                        MessageProcessorSelector.getMessageFormatter(msgCtx);
                StringWriter sw = new StringWriter();
                OutputStream out = new WriterOutputStream(sw, format.getCharSetEncoding());
                messageFormatter.writeTo(msgCtx, format, out, true);
                String contentType = messageFormatter.getContentType(msgCtx, format, msgCtx.getSoapAction());
                out.close();
                String msg = sw.toString();

                if (log.isDebugEnabled()) {
                    log.debug("Sending the default request to the Http2 server on context id : "
                            + clientHandler.getChannel().toString());
                }
                streamId = clientHandler.getStreamId();
                Channel channel = clientHandler.getChannel();
                log.debug("Channel created to send message");
                if (channel.isActive()) {
                    clientHandler.setRequest(streamId, msgCtx);
                    log.debug("Sending message to backend: " + msg);
                    String method = (msgCtx.getProperty(Constants.Configuration.HTTP_METHOD) != null ? msgCtx.getProperty(Constants.Configuration.HTTP_METHOD).toString() : POST.toString());
                    //Set content type and required frames
                    HttpMethod m = new HttpMethod(method);
                    FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, m, uri.getPath(),
                            Unpooled.copiedBuffer(msg.getBytes(CharsetUtil.UTF_8)));
                    request.headers().add(HttpHeaderNames.HOST, new URI(targetEPR).getHost());
                    request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), secure ? HttpScheme.HTTPS : HttpScheme.HTTP);
                    request.headers().add(HttpHeaderNames.CONTENT_TYPE, contentType);
                    request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
                    request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
                    clientHandler.put(streamId, request);
                    log.info("Request sent to backend with stream id:" + streamId);
                }
            }

        } catch (URISyntaxException e) {
            log.error("Error parsing the WS endpoint url", e);
        } catch (IOException e) {
            log.error("Error writting to the websocket channel", e);
        } catch (XMLStreamException e) {
            handleException("Error while building message", e);
        }
    }
}