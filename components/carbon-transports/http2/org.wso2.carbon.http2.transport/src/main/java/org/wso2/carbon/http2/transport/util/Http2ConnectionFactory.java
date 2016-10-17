package org.wso2.carbon.http2.transport.util;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AsciiString;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.http.protocol.HTTP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.websocket.transport.WebsocketConstants;
import org.wso2.carbon.websocket.transport.utils.SSLUtil;

import javax.net.ssl.SSLException;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.AbstractMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by chanakabalasooriya on 9/13/16.
 */
public class Http2ConnectionFactory {

    private static Http2ConnectionFactory factory;
    private static volatile TreeMap<String ,Http2ClientHandler> connections;
    private Log log=LogFactory.getLog(Http2ConnectionFactory.class);
    private TransportOutDescription trasportOut;

    private Http2ConnectionFactory(TransportOutDescription transportOut){
        this.trasportOut=transportOut;
        connections=new TreeMap<>();
    }
    public static Http2ConnectionFactory getInstance(TransportOutDescription transportOut){
       // Http2ConnectionFactory.trasportOut=transportOut;
        if(factory==null){
            factory=new Http2ConnectionFactory(transportOut);
        }
        return factory;
    }

    public Http2ClientHandler getChannelHandler(URI uri) {
        Http2ClientHandler handler;

        handler=getClientHandlerFromPool(uri);
        if(handler==null){
            handler=cacheNewConnection(uri);
            log.info("New connection created for "+uri);
        }else {
            log.info("Get connection from pool");
        }
        return handler;
    }

    public Http2ClientHandler cacheNewConnection(URI uri){

        final SslContext sslCtx;
        final boolean SSL;
        if(uri.getScheme().equalsIgnoreCase(Http2Constants.HTTPS2) || uri.getScheme().equalsIgnoreCase("https")){
            SSL=true;
        }else
            SSL=false;
        try {
            /**
             *Handling SSL*/
            if (SSL) {
                Parameter trustParam = trasportOut.getParameter(Http2Constants.TRUST_STORE_CONFIG_ELEMENT);
                OMElement tsEle = null;
                if (trustParam != null) {
                    tsEle = trustParam.getParameterElement();
                }
                final String location =
                        tsEle.getFirstChildWithName(new QName(Http2Constants.TRUST_STORE_LOCATION))
                                .getText();
                final String storePassword =
                        tsEle.getFirstChildWithName(new QName(Http2Constants.TRUST_STORE_PASSWORD))
                                .getText();



                SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
                sslCtx = SslContextBuilder.forClient()
                        .trustManager(SSLUtil.createTrustmanager(location,
                                storePassword))
                        .sslProvider(provider)
                /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                 * Please refer to the HTTP/2 specification for cipher requirements. */
                        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .applicationProtocolConfig(new ApplicationProtocolConfig(
                                ApplicationProtocolConfig.Protocol.ALPN,
                                // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                ApplicationProtocolNames.HTTP_2,
                                ApplicationProtocolNames.HTTP_1_1))
                        .build();
            } else {
                sslCtx = null;
            }

            EventLoopGroup workerGroup = new NioEventLoopGroup();
            Http2ClientInitializer initializer = new Http2ClientInitializer(sslCtx, Integer.MAX_VALUE);

            String HOST = uri.getHost();
            Integer PORT = uri.getPort();
            // Configure the client.
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.remoteAddress(HOST, PORT);
            b.handler(initializer);
            // Start the client.
            Channel channel = b.connect().syncUninterruptibly().channel();
            log.debug("Connected to [" + HOST + ':' + PORT + ']');
            // Wait for the HTTP/2 upgrade to occur.
            Http2SettingsHandler http2SettingsHandler = initializer.settingsHandler();
            http2SettingsHandler.awaitSettings(5, TimeUnit.SECONDS);

            String key = generateKey(uri);
            Http2ClientHandler handler = initializer.responseHandler();
            handler.setChannel(channel);
            connections.put(key, handler);
            return initializer.responseHandler();
        }catch (SSLException e){
            log.error("Error while connection establishment:"+e.fillInStackTrace());
            return null;
        }
        catch (Exception e){
            log.error("Error while connection establishment:"+e.fillInStackTrace());
            return null;
        }
    }

    public Http2ClientHandler getClientHandlerFromPool(URI uri){
        String key=generateKey(uri);
        Http2ClientHandler handler= connections.get(key);
        if(handler!=null){
            Channel c=handler.getChannel();
            if(!c.isActive()){
                connections.remove(key);
                handler= cacheNewConnection(uri);
            }else if(handler.isStreamIdOverflow()){
                c.close().syncUninterruptibly();
                connections.remove(key);
                handler= cacheNewConnection(uri);
            }
        }
        return handler;
    }

    /**
     *
     * @param uri
     * @return key to merge connection (scheme+host+port)
     */
    public String generateKey(URI uri){
        String host=uri.getHost();
        int port=uri.getPort();
        String ssl;
        if(uri.getScheme().equalsIgnoreCase(Http2Constants.HTTPS2) || uri.getScheme().equalsIgnoreCase("https")){
            ssl="https://";
        }else
            ssl="http://";
        return ssl+host+":"+port;
    }
}
