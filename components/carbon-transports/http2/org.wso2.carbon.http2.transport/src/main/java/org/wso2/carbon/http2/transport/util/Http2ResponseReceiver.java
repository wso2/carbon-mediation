package org.wso2.carbon.http2.transport.util;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2StreamFrame;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.inbound.InboundEndpointConstants;
import org.apache.synapse.inbound.InboundResponseSender;
import org.apache.synapse.mediators.MediatorFaultHandler;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.transport.passthru.config.TargetConfiguration;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.http2.transport.service.ServiceReferenceHolder;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class Http2ResponseReceiver {

    private static final Log log = LogFactory.getLog(Http2ResponseReceiver.class);
    Map<Integer,MessageContext> incompleteResponses;
    Map<Integer,Integer> serverPushes;  //promise-di maps request-id
    Map<Integer,LinkedList<Http2StreamFrame>> pushedData; //request-id , pushed-frames
    private TargetConfiguration targetConfiguration;
    private String tenantDomain;
    private InboundResponseSender responseSender;
    private boolean serverPushAccepted=true;


    public Http2ResponseReceiver(String tenantDomain, InboundResponseSender responseSender,
            boolean serverPushAccepted, TargetConfiguration targetConfiguration) {
        this.tenantDomain = tenantDomain;
        this.responseSender = responseSender;
        this.serverPushAccepted = serverPushAccepted;
        incompleteResponses=new TreeMap<>();
        serverPushes=new TreeMap<>();
        pushedData=new ConcurrentHashMap<>();
        this.targetConfiguration=targetConfiguration;
    }

    public void onDataFrameRead(Http2DataFrame frame,MessageContext msgContext){
        if(serverPushes.containsKey(frame.streamId())){
            LinkedList<Http2StreamFrame> pushframes=pushedData.get(this.serverPushes.get(frame.streamId()));
            pushframes.add(frame);
            if(frame.isEndStream())
                serverPushes.remove(frame.streamId());
            return;
        }
        if(!incompleteResponses.containsKey(frame.streamId())){
            log.error("No response headers found for received dataframe of streamID : "+frame.streamId());
            return;
        }
        MessageContext response=incompleteResponses.get(frame.streamId());
        if(response==null)
            return;
        Map <String,String>headers=(response.getProperty(MessageContext.TRANSPORT_HEADERS)!=null)?
                (Map)response.getProperty(MessageContext.TRANSPORT_HEADERS):new TreeMap<String,String>();
        String cType = headers.get(HTTP.CONTENT_TYPE.toString());
        if (cType == null && headers.containsKey(HTTP.CONTENT_TYPE.toLowerCase())) {
            cType = headers.get(HTTP.CONTENT_TYPE.toLowerCase());
        }
        String contentType;
        if (cType != null) {
            contentType = cType;
        } else {
            contentType = inferContentType(headers,response);
        }
        response.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);

        String charSetEnc = BuilderUtil.getCharSetEncoding(contentType);
        if (charSetEnc == null) {
            charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }
        if (contentType != null) {
            response.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING,
                    contentType.indexOf("charset") > 0 ?
                            charSetEnc :
                            MessageContext.DEFAULT_CHAR_SET_ENCODING);
        }
        Pipe pipe;
        if(response.getProperty(PassThroughConstants.PASS_THROUGH_PIPE)==null) {
            pipe = new Pipe(new HTTP2Producer(), targetConfiguration.getBufferFactory().getBuffer(),
                    "target", targetConfiguration);
            response.setProperty(PassThroughConstants.PASS_THROUGH_PIPE,pipe);
        }else
            pipe=(Pipe)response.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
        try {
            pipe.produce(new HTTP2Decoder(frame));
            response.setServerSide(true);
            response.removeProperty(PassThroughConstants.NO_ENTITY_BODY);
        }catch (Exception e){
            log.error("Error occured during pipe producing "+e);
        }
        if(response.getProperty(Http2Constants.HTTP2_RESPONSE_SENT)==null){
            try {
                response.setEnvelope(new SOAP11Factory().getDefaultEnvelope());
                AxisEngine.receive(response);
                response.setProperty(Http2Constants.HTTP2_RESPONSE_SENT,true);
            } catch (AxisFault af) {
                log.error("Fault processing response message through Axis2", af);
            }
        }
        if(frame.isEndStream()){
            incompleteResponses.remove(frame.streamId());
            if(serverPushes.containsKey(frame.streamId()))
                serverPushes.remove(frame.streamId());
        }
    }

    public void onHeadersFrameRead(Http2HeadersFrame frame,MessageContext msgContext) throws AxisFault{

        if(serverPushes.containsKey(frame.streamId())){
            LinkedList<Http2StreamFrame> pushframes=pushedData.get(this.serverPushes.get(frame.streamId()));
            pushframes.add(frame);
            if(frame.isEndStream())
                serverPushes.remove(frame.streamId());
            return;
        }

        MessageContext response=null;
        if (incompleteResponses.containsKey(frame.streamId())) {
            response = incompleteResponses.get(frame.streamId());
        }
        if(response==null) {
            response = msgContext.getOperationContext().
                    getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
            if (response != null) {
                response.setSoapAction("");
            }
            incompleteResponses.put(frame.streamId(), response);

            if (response == null) {
                if (msgContext.getOperationContext().isComplete()) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "Error getting IN message context from the operation context. "
                                        + "Possibly an RM terminate sequence message");
                    }
                    return;

                }
                response = new MessageContext();
                response.setOperationContext(msgContext.getOperationContext());
            }
        }

        addHeaders(msgContext, response, frame);
        if(pushedData.containsKey(frame.streamId()) && response.getProperty(Http2Constants.HTTP2_REQUEST_TYPE)==null){
            response.setProperty(Http2Constants.HTTP2_REQUEST_TYPE,Http2Constants.HTTP2_PUSH_PROMISE_REQEUST);
            response.setProperty(Http2Constants.HTTP2_PUSH_PROMISE_DATA,pushedData.remove(frame.streamId()));
        }else
            response.setProperty(Http2Constants.HTTP2_REQUEST_TYPE,Http2Constants.HTTP2_CLIENT_SENT_REQEUST);

        if (frame.isEndStream()) {
            response.setProperty(PassThroughConstants.NO_ENTITY_BODY, Boolean.TRUE);
            incompleteResponses.remove(frame.streamId());
            response.setEnvelope(new SOAP11Factory().getDefaultEnvelope());
            AxisEngine.receive(response);
            response.setProperty(Http2Constants.HTTP2_RESPONSE_SENT, true);
            if(serverPushes.containsKey(frame.streamId()))
                serverPushes.remove(frame.streamId());
        }
    }

    public void onPushPromiseFrameRead(Http2PushPromiseFrame frame,MessageContext msgContext){
        serverPushes.put(frame.getPushPromiseId(),frame.streamId());
        LinkedList<Http2StreamFrame> pushframes=new LinkedList<>();
        pushframes.add(frame);
        this.pushedData.put(frame.streamId(),pushframes);
    }

    public void onUnknownFrameRead(Object frame){
        if(log.isDebugEnabled()){
            log.debug("unhandled frame received : "+frame.getClass().getName());
        }
    }

    private String inferContentType(Map<String,String> headers,MessageContext responseMsgCtx) {
        //Check whether server sent Content-Type in different case
       // Map<String, String> headers = response.getHeaders();
        for (String header : headers.keySet()) {
            if (HTTP.CONTENT_TYPE.equalsIgnoreCase(header)) {
                return headers.get(header);
            }
        }
        String cType = headers.get("content-type");
        if (cType != null) {
            return cType;
        }
        cType = headers.get("Content-type");
        if (cType != null) {
            return cType;
        }

        // Try to get the content type from the message context
        Object cTypeProperty = responseMsgCtx.getProperty(PassThroughConstants.CONTENT_TYPE);
        if (cTypeProperty != null) {
            return cTypeProperty.toString();
        }
        // Try to get the content type from the axis configuration
        Parameter cTypeParam = targetConfiguration.getConfigurationContext().getAxisConfiguration()
                .getParameter(PassThroughConstants.CONTENT_TYPE);
        if (cTypeParam != null) {
            return cTypeParam.getValue().toString();
        }

        // When the response from backend does not have the body(Content-Length is 0 )
        // and Content-Type is not set;
        // ESB should not do any modification to the response and pass-through as it is.
        if (headers.get(HTTP.CONTENT_LEN) == null || "0".equals(headers.get(HTTP.CONTENT_LEN))) {
            return null;
        }

        // Unable to determine the content type - Return default value
        return PassThroughConstants.DEFAULT_CONTENT_TYPE;
    }

    private void addHeaders(MessageContext request,MessageContext response,Http2HeadersFrame frame){
        Map<String, String> headers = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
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
            if(request!=null){
                String prfix = (String) request.getProperty(PassThroughConstants.SERVICE_PREFIX);
                if (prfix != null) {
                    if (urlContext != null && urlContext.startsWith("/")) {
                        //Remove the preceding '/' character
                        urlContext = urlContext.substring(1);
                    }
                    headers.put(PassThroughConstants.LOCATION, prfix + urlContext);
                }
            }
        }

        String tenantDomain = this.tenantDomain;
        tenantDomain = (tenantDomain != null) ?
                tenantDomain :
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        response.setProperty(MultitenantConstants.TENANT_DOMAIN, tenantDomain);

        response.setProperty("PRE_LOCATION_HEADER",oriURL);
        // copy the important properties from the original message context

        if(request!=null) {

            response.setServerSide(true);
            response.setDoingREST(request.isDoingREST());
            response.setProperty(MessageContext.TRANSPORT_IN, request.getProperty(MessageContext.TRANSPORT_IN));
            response.setTransportIn(request.getTransportIn());
            response.setTransportOut(request.getTransportOut());

            response.setProperty(PassThroughConstants.INVOKED_REST, request.isDoingREST());

            response.setAxisMessage(request.getOperationContext().getAxisOperation().
                    getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE));
            response.setOperationContext(request.getOperationContext());
            response.setConfigurationContext(request.getConfigurationContext());
        }

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

        response.setTo(null);
    }

    public boolean isServerPushAccepted() {
        return serverPushAccepted;
    }

}
