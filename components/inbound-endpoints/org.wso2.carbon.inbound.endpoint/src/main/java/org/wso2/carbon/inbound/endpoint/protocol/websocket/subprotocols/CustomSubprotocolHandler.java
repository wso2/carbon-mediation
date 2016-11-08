package org.wso2.carbon.inbound.endpoint.protocol.websocket.subprotocols;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.AbstractSubprotocolHandler;


public class CustomSubprotocolHandler extends AbstractSubprotocolHandler {

    public CustomSubprotocolHandler(){
        super.setSubprotocolIdentifier("text/plain");
    }

    @Override
    public boolean handle(ChannelHandlerContext context, WebSocketFrame frame,
                                    String subscriberPath) {
        return true;
    }
}
