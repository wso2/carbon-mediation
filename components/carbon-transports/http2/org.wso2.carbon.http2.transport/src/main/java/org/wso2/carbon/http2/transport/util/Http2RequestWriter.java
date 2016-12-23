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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Headers;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.transport.passthru.util.PassThroughTransportUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Http2RequestWriter {

    private static final Log log = LogFactory.getLog(Http2RequestWriter.class);

    Http2ConnectionEncoder encoder;
    Http2Connection connection;
    ChannelHandlerContext chContext;

    public void writeSimpleReqeust(int streamId,MessageContext msgContext) {   //sending a normal request


        Http2TargetRequestUtil util = (Http2TargetRequestUtil) msgContext.getProperty(Http2Constants.PASSTHROUGH_TARGET);
        Http2Headers headers=util.getHeaders(msgContext);
        ChannelPromise promise=chContext.newPromise();

        if(util.isHasEntityBody() && headers.contains(HttpHeaderNames.CONTENT_TYPE)) {
            http2Encoder pipeEncoder=new http2Encoder(chContext,streamId,encoder,promise);
            Pipe pipe = (Pipe) msgContext.getProperty("pass-through.pipe");
            encoder.writeHeaders(chContext,streamId,headers,0,false,promise);
            if (pipe != null) {
                pipe.attachConsumer(new Http2CosumerIoControl());
                try {
                    if (Boolean.TRUE.equals(msgContext
                            .getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED))) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        MessageFormatter formatter = MessageProcessorSelector
                                .getMessageFormatter(msgContext);
                        OMOutputFormat format = PassThroughTransportUtils
                                .getOMOutputFormat(msgContext);
                        formatter.writeTo(msgContext, format, out, false);
                        OutputStream _out = pipe.getOutputStream();
                        IOUtils.write(out.toByteArray(), _out);
                    }
                    int t = pipe.consume(pipeEncoder);
                }catch (IOException e){

                    //throw ex
                    //log.error(e);
                }
            }
        }else{
            encoder.writeHeaders(chContext,streamId,headers,0,true,promise);
        }
        try {
            encoder.flowController().writePendingBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        chContext.flush();
    }

    public void writeRestSreamRequest(int streamId,Http2Error code){
        encoder.writeRstStream(chContext,streamId,code.code(),chContext.newPromise());  //sending a restStreamFrame
        chContext.flush();
    }

    public void writeGoAwayReqeust(int lastStreamId,Http2Error code){
        encoder.writeGoAway(chContext,lastStreamId,0,null,chContext.newPromise());  //sending a goAwayFrame
        chContext.flush();
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
