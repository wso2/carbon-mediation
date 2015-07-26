package org.wso2.carbon.rest.api.ui.util;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;
import org.wso2.carbon.rest.api.stub.types.carbon.ResourceData;
import org.wso2.carbon.utils.xml.XMLPrettyPrinter;

public class ApiEditorHelper {
	
	public static String parseStringToPrettyfiedString(String ugly){
		ByteArrayInputStream byteArrayInputStream
		= new ByteArrayInputStream(ugly.getBytes());
		XMLPrettyPrinter printer = new XMLPrettyPrinter(byteArrayInputStream);
		return printer.xmlFormat();
	}
	
	public static APIData convertStringToAPIData(String xml) throws XMLStreamException{
		APIData apiData = new APIData();
		
		OMElement apiOM = AXIOMUtil.stringToOM(xml);
		
		OMAttribute name = apiOM.getAttribute(new QName("name"));
		if(name != null){
			apiData.setName(name.getAttributeValue());
		}
		
		OMAttribute context = apiOM.getAttribute(new QName("context"));
		if(context != null){
			apiData.setContext(context.getAttributeValue());
		}
		
		OMAttribute host = apiOM.getAttribute(new QName("hostname"));
		if(host != null){
			apiData.setHost(host.getAttributeValue());
		}
		
		OMAttribute port = apiOM.getAttribute(new QName("port"));
		if(port != null){
			apiData.setPort(Integer.parseInt(port.getAttributeValue()));
		}
		else{
			apiData.setPort(-1);
		}
		
		Iterator childIterator = apiOM.getChildElements();
		if(childIterator == null){
			return apiData;
		}
		
		List<ResourceData> resources = new ArrayList<ResourceData>();
		
		while(childIterator.hasNext()){
			OMElement resourceOM = (OMElement)childIterator.next();
			ResourceData resource = new ResourceData();
			convertResource(resourceOM, resource);
			resources.add(resource);
		}
		
		ResourceData[] resourceArray = new ResourceData[resources.size()];
		apiData.setResources(resources.toArray(resourceArray));
		
		return apiData;
	}

    public static ResourceData convertStringToResourceData(String xml) throws XMLStreamException {
        ResourceData resourceData = new ResourceData();
        OMElement resourceOM = AXIOMUtil.stringToOM(xml);
        convertResource(resourceOM, resourceData);
        return resourceData;
    }

    private static void convertResource(OMElement resourceOM, ResourceData resourceData)
        throws XMLStreamException {
        OMAttribute methods = resourceOM.getAttribute(new QName("methods"));
        if (methods != null) {
            resourceData.setMethods(methods.getAttributeValue().split(" "));
        }

        OMAttribute uriTemplate = resourceOM.getAttribute(new QName("uri-template"));
        if (uriTemplate != null) {
            resourceData.setUriTemplate(uriTemplate.getAttributeValue());
        }

        OMAttribute urlMapping = resourceOM.getAttribute(new QName("url-mapping"));
        if (urlMapping != null) {
            resourceData.setUrlMapping(urlMapping.getAttributeValue());
        }

        OMAttribute contentType = resourceOM.getAttribute(new QName("contentType"));
        if (contentType != null) {
            resourceData.setContentType(contentType.getAttributeValue());
        }

        OMAttribute protocol = resourceOM.getAttribute(new QName("protocol"));
        if (protocol != null) {
            resourceData.setProtocol(Integer.parseInt(protocol.getAttributeValue()));
        }

        OMAttribute userAgent = resourceOM.getAttribute(new QName("userAgent"));
        if (userAgent != null) {
            resourceData.setUserAgent(userAgent.getAttributeValue());
        }

        OMAttribute inSequence = resourceOM.getAttribute(new QName("inSequence"));
        if (inSequence != null) {
            resourceData.setInSequenceKey(inSequence.getAttributeValue());
        }

        OMAttribute outSequence = resourceOM.getAttribute(new QName("outSequence"));
        if (outSequence != null) {
            resourceData.setOutSequenceKey(outSequence.getAttributeValue());
        }

        OMAttribute faultSequence = resourceOM.getAttribute(new QName("faultSequence"));
        if (faultSequence != null) {
            resourceData.setFaultSequenceKey(faultSequence.getAttributeValue());
        }

        if (resourceOM.getChildElements() != null) {
            Iterator<OMElement> iterator = resourceOM.getChildElements();
            for (; iterator.hasNext(); ) {
                OMElement elem = iterator.next();
                if ("inSequence".equals(elem.getLocalName())
                    && inSequence == null) {
                    resourceData.setInSeqXml(elem.toString());
                } else if ("outSequence".equals(elem.getLocalName())
                           && outSequence == null) {
                    resourceData.setOutSeqXml(elem.toString());
                } else if ("faultSequence".equals(elem.getLocalName())
                           && faultSequence == null) {
                    resourceData.setFaultSeqXml(elem.toString());
                }
            }
        }
    }
}
