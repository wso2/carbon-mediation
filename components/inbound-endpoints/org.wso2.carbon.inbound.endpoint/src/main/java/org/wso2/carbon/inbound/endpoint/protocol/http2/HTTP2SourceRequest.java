package org.wso2.carbon.inbound.endpoint.protocol.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Frame;
import org.apache.log4j.Logger;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by chanakabalasooriya on 8/31/16.
 */
public class HTTP2SourceRequest {
    private Logger log = Logger.getLogger(HTTP2SourceRequest.class);
    private int streamID;
    private ChannelHandlerContext channel;
    private HashMap<Byte,Http2Frame> frames=new HashMap<Byte,Http2Frame>();
    private Map<String,String> headers=new TreeMap<String, String>(new Comparator<String>() {
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    });
    private String method=null;
    private String uri=null;
    private String scheme=null;

    public void setUri(String uri) {
        this.uri = uri;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public void setScheme(String scheme) {
        this.scheme = scheme.toLowerCase();
    }
    public String getScheme() {
        if(scheme!=null){
            return scheme;
        }
        else if(headers.containsKey("scheme")){
            return headers.get("scheme");
        }else{
            return "http";
        }
    }

    public void setStreamID(int streamID) {
        this.streamID = streamID;
    }
    public int getStreamID() {
        return streamID;
    }

    public Map<String, String> getExcessHeaders() {
        return excessHeaders;
    }

    private Map<String,String> excessHeaders=new TreeMap<String, String>();
    public HTTP2SourceRequest(int streamID,ChannelHandlerContext channel) {
        this.streamID = streamID;
        this.channel=channel;
    }

    public ChannelHandlerContext getChannel() {
        return channel;
    }

    public void setChannel(ChannelHandlerContext channel) {
        this.channel = channel;
    }

    public Map<String, String> getHeaders() {

        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getMethod(){
        if(method!=null){
            return method;
        }
        else if(headers.containsKey("method")){
            return headers.get("method");
        }else{
            return null;
        }
    }
    public String getHeader(String key){
        if(headers.containsKey(key)){
            return headers.get(key);
        }else{
            return null;
        }
    }

    public String getUri(){
        if(uri!=null){
            return uri;
        }
        else if(headers.containsKey("path")){
            return "/"+headers.get("path");
        }else{
            return null;
        }
    }

    public boolean addFrame(Byte frameType,Http2Frame frame) {
        if (!frames.containsKey(frameType)){
            frames.put(frameType, frame);
            return true;
        }else
            return false;
    }

    public Http2Frame getFrame(byte frameType){
        if(frames.containsKey(frameType)){
            return frames.get(frameType);
        }else{
            return null;
        }
    }

    @Override
    public String toString(){
        String name="";
        name+="Stream Id:"+streamID+"/n";
        if(headers.size()>0) {
            name += "Headers:/n";
            for (Map.Entry h : headers.entrySet()) {
                name += h.getKey() + ":" + h.getValue() + "/n";
            }
        }
        if(frames.size()>0){
            name+="Frames : /n";
            for (Map.Entry h:frames.entrySet()) {
                name+=h.getKey().toString()+":"+h.getValue()+"/n";
            }
        }
        return name;
    }
    public void setHeader(String key,String value){
        if(key.charAt(0)==':'){
            key=key.substring(1);
        }
        if(key.equalsIgnoreCase("authority")){
            key="host";
        }
        if(headers.containsKey(key)){
            excessHeaders.put(key,value);
        }else{
            headers.put(key,value);
        }
    }
}
