package org.wso2.carbon.http2.transport.util;

import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2StreamFrame;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.transport.passthru.config.TargetConfiguration;
import org.omg.PortableInterceptor.INACTIVE;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.http2.transport.service.ServiceReferenceHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by chanakabalasooriya on 12/3/16.
 */
public class Http2ResponseReceiver {

    private static final Log log = LogFactory.getLog(Http2ResponseReceiver.class);
    Map<Integer,MessageContext> incompleteResponses;
    private TargetConfiguration targetConfiguration;

    public Http2ResponseReceiver(TargetConfiguration targetConfiguration) {
        this.targetConfiguration = targetConfiguration;
    }

    public Http2ResponseReceiver() {
        this.incompleteResponses = new TreeMap<>();
    }

    public void onDataFrameRead(Http2DataFrame frame,MessageContext msgContext){
        /**if not any incomplete response reject frame throwing a error
         * get pipe from response if not create new one
         * decode data into pipe
         * if not sent to axis2engine sent to it
         * add a property to identify whether sent to axis2engine or not
         */

        if(!incompleteResponses.containsKey(frame.streamId())){
            log.error("No response headers found for received dataframe of streamID : "+frame.streamId());
            return;
        }
        MessageContext response=incompleteResponses.get(frame.streamId());
        Pipe pipe;
        if(response.getProperty(PassThroughConstants.PASS_THROUGH_PIPE)==null) {
            pipe = new Pipe(new HTTP2Producer(), targetConfiguration.getBufferFactory().getBuffer(),
                    "target", targetConfiguration);
            response.setProperty(PassThroughConstants.PASS_THROUGH_PIPE,pipe);
        }else
            pipe=(Pipe)response.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
        try {
            pipe.produce(new HTTP2Decoder(frame));
        }catch (Exception e){
            log.error("Error occured during pipe producing "+e);
        }
        if(response.getProperty(Http2Constants.HTTP2_RESPONSE_SENT)==null){
            try {
                AxisEngine.receive(response);
                response.setProperty(Http2Constants.HTTP2_RESPONSE_SENT,true);
            } catch (AxisFault af) {
                log.error("Fault processing response message through Axis2", af);
            }
        }
        if(frame.isEndStream()){
            incompleteResponses.remove(frame.streamId());
        }
    }

    public void onHeadersFrameRead(Http2HeadersFrame frame,MessageContext msgContext){
            //every response MsgCtx should have a requestType (normal,push-promise)

        /**
         * a. check a response in incomplete list
         * b. if not create new one from msgContext
         * c. set headers into tranport headers
         * d. if reqeust type is not pushpromise, make it simpe client request type
         * e. check end of stream
         * f. if not add to incomplete list
         * g. unless axis2engine.receive()
         */
        MessageContext response;
        if(incompleteResponses.containsKey(frame.streamId())){
            response=incompleteResponses.get(frame.streamId());
        }else{
            try {
                response= msgContext.getOperationContext().
                        getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
                if (response != null) {
                    response.setSoapAction("");
                }
                incompleteResponses.put(frame.streamId(),response);
            } catch (AxisFault af) {
                log.error("Error getting IN message context from the operation context", af);
                return;
            }

            if (response == null) {
                if (msgContext.getOperationContext().isComplete()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Error getting IN message context from the operation context. "
                                + "Possibly an RM terminate sequence message");
                    }
                    return;

                }
                response = new MessageContext();
                response.setOperationContext(msgContext.getOperationContext());
            }
        }
        Map<String, String> headers = new HashMap();
        Map<String, String> excessHeaders = new HashMap();
        for (Map.Entry header : frame.headers()) {
            if (header.getKey().toString().equalsIgnoreCase(
                    HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.toString())) {
                continue;
            }
            String key = header.getKey().toString();
            key = (key.charAt(0) == ':') ? key.substring(1) : key;
            if (headers.containsKey(key)) {
                excessHeaders.put(key, header.getValue().toString());
            } else {
                headers.put(key, header.getValue().toString());
            }
        }

        String oriURL = headers.get(PassThroughConstants.LOCATION);

        HttpResponseStatus status=HttpResponseStatus.parseLine(frame.headers().status());
        if (oriURL != null && ((status.code() != HttpStatus.SC_MOVED_TEMPORARILY) && (
                status.code() != HttpStatus.SC_MOVED_PERMANENTLY) && (status.code()
                != HttpStatus.SC_CREATED) && (status.code() != HttpStatus.SC_SEE_OTHER) && (
                status.code() != HttpStatus.SC_TEMPORARY_REDIRECT) && !targetConfiguration
                .isPreserveHttpHeader(PassThroughConstants.LOCATION))) {
            URL url;
            String urlContext = null;
            try {
                url = new URL(oriURL);
                urlContext = url.getFile();
            } catch (MalformedURLException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Relative URL received for Location : " + oriURL, e);
                }
                urlContext = oriURL;
            }

            headers.remove(PassThroughConstants.LOCATION);
            String prfix = (String) msgContext.getProperty(PassThroughConstants.SERVICE_PREFIX);
            if (prfix != null) {
                if (urlContext != null && urlContext.startsWith("/")) {
                    //Remove the preceding '/' character
                    urlContext = urlContext.substring(1);
                }
                headers.put(PassThroughConstants.LOCATION, prfix + urlContext);
            }
        }
        response.setProperty("PRE_LOCATION_HEADER",oriURL);
        // copy the important properties from the original message context
        response.setProperty(Http2Constants.PASS_THROUGH_SOURCE_CONNECTION,
                msgContext.getProperty(Http2Constants.PASS_THROUGH_SOURCE_CONNECTION));
        response.setProperty(Http2Constants.PASS_THROUGH_SOURCE_CONFIGURATION,
                msgContext.getProperty(Http2Constants.PASS_THROUGH_SOURCE_CONFIGURATION));

        if(response.getProperty(MessageContext.TRANSPORT_HEADERS)==null){
            response.setProperty(MessageContext.TRANSPORT_HEADERS,headers);
        }else{
            Map existing=(Map)response.getProperty(MessageContext.TRANSPORT_HEADERS);
            response.removeProperty(MessageContext.TRANSPORT_HEADERS);
            existing.putAll(headers);
            response.setProperty(MessageContext.TRANSPORT_HEADERS,existing);

        }
        if(response.getProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS)==null)
            response.setProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS, excessHeaders);
        else{
            Map existing=(Map)response.getProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS);
            response.removeProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS);
            existing.putAll(headers);
            response.setProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS,existing);
        }
        if (status.code() == 202) {
            response.setProperty(AddressingConstants.
                    DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
            response.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.FALSE);
            response.setProperty(NhttpConstants.SC_ACCEPTED, Boolean.TRUE);
        }
        if(response.getProperty(Http2Constants.HTTP2_REQUEST_TYPE)==null)
            response.setProperty(Http2Constants.HTTP2_REQUEST_TYPE,Http2Constants.HTTP2_CLIENT_SENT_REQEUST);


        if(frame.isEndStream()){
            incompleteResponses.remove(frame.streamId());
            try {
                AxisEngine.receive(response);
                response.setProperty(Http2Constants.HTTP2_RESPONSE_SENT,true);
            } catch (AxisFault af) {
                log.error("Fault processing response message through Axis2", af);
            }
        }

    }

    public void onPushPromiseFrameRead(Http2PushPromiseFrame frame,MessageContext msgContext){
        /**
         * create new context for push promise data and add to incomplete list
         * add some property to identify its a push promise response
         */
        MessageContext pushPromiseResponse=createAxis2MessageContext();
        fillMessageContext(pushPromiseResponse,msgContext.getProperty(MultitenantConstants.TENANT_DOMAIN).toString());
        pushPromiseResponse.setProperty(Http2Constants.HTTP2_PUSH_PROMISE_ID,frame.getPushPromiseId());
        pushPromiseResponse.setProperty(Http2Constants.HTTP2_PUSH_PROMISE_HEADERS,frame.getHeaders());
        pushPromiseResponse.setProperty(Http2Constants.HTTP2_REQUEST_TYPE,Http2Constants.HTTP2_PUSH_PROMISE_REQEUST);
        incompleteResponses.put(frame.getPushPromiseId(),pushPromiseResponse);
    }

    public void  onSettingsRead(Http2Settings settings){

    }

    public void onUnknownFrameRead(Object frame){
        if(log.isDebugEnabled()){
            log.debug("unhandled frame received : "+frame.getClass().getName());
        }
    }

    private static org.apache.axis2.context.MessageContext createAxis2MessageContext() {
        org.apache.axis2.context.MessageContext axis2MsgCtx = new org.apache.axis2.context.MessageContext();
        axis2MsgCtx.setMessageID(UIDGenerator.generateURNString());
        axis2MsgCtx.setConfigurationContext(ServiceReferenceHolder.getInstance().getConfigurationContextService()
                .getServerConfigContext());
        axis2MsgCtx.setProperty(org.apache.axis2.context.MessageContext.CLIENT_API_NON_BLOCKING,
                Boolean.FALSE);
        axis2MsgCtx.setServerSide(true);
        return axis2MsgCtx;
    }

    private static void fillMessageContext(MessageContext newCtx,String tenantDomain){
        org.apache.axis2.context.MessageContext axis2MsgCtx = createAxis2MessageContext();
        ServiceContext svcCtx = new ServiceContext();
        OperationContext opCtx = new OperationContext(new InOutAxisOperation(), svcCtx);
        axis2MsgCtx.setServiceContext(svcCtx);
        axis2MsgCtx.setOperationContext(opCtx);
        if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            ConfigurationContext tenantConfigCtx =
                    TenantAxisUtils.getTenantConfigurationContext(tenantDomain,
                            axis2MsgCtx.getConfigurationContext());
            axis2MsgCtx.setConfigurationContext(tenantConfigCtx);
            axis2MsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, tenantDomain);
        } else {
            axis2MsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN,
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        }
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        try {
            axis2MsgCtx.setEnvelope(envelope);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    private static void copyAndfillMessageContext(MessageContext responseMsgCtx,MessageContext outMsgCtx){
        String tenantDomain = outMsgCtx.getProperty(MultitenantConstants.TENANT_DOMAIN).toString();
        tenantDomain = (tenantDomain != null) ?
                tenantDomain :
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        responseMsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, tenantDomain);

       // responseMsgCtx.setProperty("PRE_LOCATION_HEADER", oriURL);

        responseMsgCtx.setServerSide(true);
        responseMsgCtx.setDoingREST(outMsgCtx.isDoingREST());
        responseMsgCtx.setProperty(MessageContext.TRANSPORT_IN,
                outMsgCtx.getProperty(MessageContext.TRANSPORT_IN));
        responseMsgCtx.setTransportIn(outMsgCtx.getTransportIn());
        responseMsgCtx.setTransportOut(outMsgCtx.getTransportOut());

        responseMsgCtx.setProperty(PassThroughConstants.INVOKED_REST, outMsgCtx.isDoingREST());


        responseMsgCtx.setAxisMessage(outMsgCtx.getOperationContext().getAxisOperation().
                getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE));
        responseMsgCtx.setOperationContext(outMsgCtx.getOperationContext());
        responseMsgCtx.setConfigurationContext(outMsgCtx.getConfigurationContext());
        responseMsgCtx.setTo(null);
    }

}
