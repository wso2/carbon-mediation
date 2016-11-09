package org.wso2.carbon.inbound.endpoint.protocol.websocket.subprotocols;

import org.wso2.carbon.inbound.endpoint.protocol.websocket.AbstractSubprotocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.channel.ChannelHandlerContext;


public class BinarySubprotocolHandler extends AbstractSubprotocolHandler {

    public BinarySubprotocolHandler(){
        super.setSubprotocolIdentifier("binary/octet-stream");
    }

    @Override
    public boolean handle(ChannelHandlerContext context, WebSocketFrame frame,
                                    String subscriberPath) {
        return true;
    }
}
