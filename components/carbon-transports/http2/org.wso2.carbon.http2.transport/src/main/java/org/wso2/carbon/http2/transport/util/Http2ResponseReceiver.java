package org.wso2.carbon.http2.transport.util;

import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2StreamFrame;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.PortableInterceptor.INACTIVE;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by chanakabalasooriya on 12/3/16.
 */
public class Http2ResponseReceiver {

    private static final Log log = LogFactory.getLog(Http2ResponseReceiver.class);
    Map<Integer,MessageContext> incompleteResponses;


    public Http2ResponseReceiver() {
        this.incompleteResponses = new TreeMap<>();
    }

    public void onDataFrameRead(Http2DataFrame frame,MessageContext msgContext){

    }

    public void onHeadersFrameRead(Http2HeadersFrame frame,MessageContext msgContext){
            //every response MsgCtx should have a requestType (normal,push-promise)
    }

    public void onPushPromiseFrameRead(Http2PushPromiseFrame frame,MessageContext msgContext){
        //create new axis2Response message for push-promise data and add it to a incompleteResponses
        //add reqeust_type as push promise
        //add push_promise headers  (path,method,authority)

    }

    public void  onSettingsRead(Http2Settings settings){

    }

    public void onUnknownFrameRead(Object frame){
        if(log.isDebugEnabled()){
            log.debug("unhandled frame received : "+frame.getClass().getName());
        }
    }

}
