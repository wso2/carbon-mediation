package org.wso2.carbon.http2.transport.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Error;
import org.apache.axis2.context.MessageContext;

public class Http2RequestWriter {

    Http2ConnectionEncoder encoder;
    Http2Connection connection;
    ChannelHandlerContext chContext;

    public void writeSimpleReqeust(int streamId,MessageContext msgContext){   //sending a normal request


    }

    public void writeRestSreamRequest(int streamId,Http2Error code){
        encoder.writeRstStream(chContext,streamId,code.code(),chContext.newPromise());  //sending a restStreamFrame
    }

    public void writeGoAwayReqeust(int lastStreamId,Http2Error code){
        encoder.writeGoAway(chContext,lastStreamId,0,null,chContext.newPromise());  //sending a goAwayFrame
    }



    public Http2RequestWriter(Http2Connection connection) {
        this.connection = connection;
    }

    public void setEncoder(Http2ConnectionEncoder encoder) {
        this.encoder = encoder;
    }

    public void setConnection(Http2Connection connection) {
        this.connection = connection;
    }

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.chContext = channelHandlerContext;
    }

    public int getNextStreamId() {
        return connection.local().incrementAndGetNextStreamId();
    }
}
