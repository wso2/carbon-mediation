package org.wso2.carbon.rest.api;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.rest.RESTConstants;

import javax.xml.stream.XMLStreamException;

public class RestApiAdminUtils {
	
	public static OMElement retrieveAPIOMElement(APIData apiData) {

		OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace syn = SynapseConstants.SYNAPSE_OMNAMESPACE;
        OMNamespace nullNS = fac.createOMNamespace("","");

        OMElement api = fac.createOMElement("api", syn);
        api.addAttribute("name", apiData.getName(), nullNS);

        if(apiData.getContext() != null){
            api.addAttribute("context", apiData.getContext(), nullNS);
        }

        if(apiData.getHost() != null){
            api.addAttribute("hostname", apiData.getHost(), nullNS);
        }

        if(apiData.getPort() != -1){
            api.addAttribute("port", String.valueOf(apiData.getPort()), nullNS);
        }
		if(apiData.getResources() != null && apiData.getResources().length != 0){
			for(ResourceData resourceData : apiData.getResources()){
				api.addChild(retrieveResourceOMElement(resourceData));
			}
		}
		
        return api;
	}
	
	public static OMElement retrieveResourceOMElement(ResourceData resourceData) {
		OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace syn = SynapseConstants.SYNAPSE_OMNAMESPACE;
        OMNamespace nullNS = fac.createOMNamespace("","");

        OMElement resource = fac.createOMElement("resource", syn);
        
        if(resourceData.getMethods() != null && resourceData.getMethods().length != 0){
        	String methodsString = createSSString(resourceData.getMethods());
        	resource.addAttribute("methods", methodsString, nullNS);
        }
        if(resourceData.getUriTemplate() != null){
        	resource.addAttribute("uri-template", resourceData.getUriTemplate(), nullNS);
        }
        else if(resourceData.getUrlMapping() != null){
        	resource.addAttribute("url-mapping", resourceData.getUrlMapping(), nullNS);
        }
        if(resourceData.getContentType() != null){
        	resource.addAttribute("contentType", resourceData.getContentType(), nullNS);
        }
        if(resourceData.getUserAgent() != null){
        	resource.addAttribute("userAgent", resourceData.getUserAgent(), nullNS);
        }
        if(resourceData.getProtocol() != RESTConstants.PROTOCOL_HTTP_AND_HTTPS){
        	resource.addAttribute("protocol", String.valueOf(resourceData.getProtocol()), nullNS);
        }

        try {
            if(resourceData.getInSequenceKey() != null){
                resource.addAttribute("inSequence", resourceData.getInSequenceKey(), nullNS);
            } else if (resourceData.getInSeqXml() != null && !"".equals(resourceData.getInSeqXml())) {
                resource.addChild(AXIOMUtil.stringToOM(resourceData.getInSeqXml()));
            }
            if(resourceData.getOutSequenceKey() != null){
                resource.addAttribute("outSequence", resourceData.getOutSequenceKey(), nullNS);
            } else if (resourceData.getOutSeqXml() != null && !"".equals(resourceData.getOutSeqXml())) {
                resource.addChild(AXIOMUtil.stringToOM(resourceData.getOutSeqXml()));
            }
            if(resourceData.getFaultSequenceKey() != null){
                resource.addAttribute("faultSequence", resourceData.getFaultSequenceKey(), nullNS);
            } else if (resourceData.getFaultSeqXml() != null && !"".equals(resourceData.getFaultSeqXml())) {
                resource.addChild(AXIOMUtil.stringToOM(resourceData.getFaultSeqXml()));
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
            return null;
        }
        return resource;
	}
	
	/**
     * Creates a space separated string from the given list
     * @param lst the array of strings
     * @return a space separated string
     */
    private static String createSSString (String [] lst) {
        String str = "";
        for (Object item : lst) {
            str += item + " ";
        }
        return str.substring(0, str.length() - 1);
    }
}
