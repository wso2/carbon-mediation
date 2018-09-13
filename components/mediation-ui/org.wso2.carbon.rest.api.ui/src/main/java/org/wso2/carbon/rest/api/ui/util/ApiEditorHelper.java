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
import org.wso2.carbon.rest.api.stub.types.carbon.HandlerData;
import org.wso2.carbon.rest.api.stub.types.carbon.ResourceData;
import org.wso2.carbon.utils.xml.XMLPrettyPrinter;
import org.apache.synapse.rest.RESTConstants;

public class ApiEditorHelper {

    private static String PROTOCOL_HTTP = "http";
    private static String PROTOCOL_HTTPS = "https";

    public static String parseStringToPrettyfiedString(String ugly) {
        ByteArrayInputStream byteArrayInputStream
                = new ByteArrayInputStream(ugly.getBytes());
        XMLPrettyPrinter printer = new XMLPrettyPrinter(byteArrayInputStream);
        return printer.xmlFormat();
    }

    public static APIData convertStringToAPIData(String xml) throws XMLStreamException {
        APIData apiData = new APIData();

        OMElement apiOM = AXIOMUtil.stringToOM(xml);

        OMAttribute name = apiOM.getAttribute(new QName("name"));
        if (name != null) {
            apiData.setName(name.getAttributeValue());
        }

        OMAttribute context = apiOM.getAttribute(new QName("context"));
        if (context != null) {
            apiData.setContext(context.getAttributeValue());
        }

        OMAttribute host = apiOM.getAttribute(new QName("hostname"));
        if (host != null) {
            apiData.setHost(host.getAttributeValue());
        }

        OMAttribute port = apiOM.getAttribute(new QName("port"));
        if (port != null) {
            apiData.setPort(Integer.parseInt(port.getAttributeValue()));
        } else {
            apiData.setPort(-1);
        }

        OMAttribute version = apiOM.getAttribute(new QName("version"));
        if (version != null) {
            apiData.setVersion(version.getAttributeValue());
        }

        OMAttribute versionType = apiOM.getAttribute(new QName("version-type"));
        if (versionType != null) {
            apiData.setVersionType(versionType.getAttributeValue());
        }

        Iterator childIterator = apiOM.getChildElements();
        if (childIterator == null) {
            return apiData;
        }

        List<ResourceData> resources = new ArrayList<ResourceData>();
        List<HandlerData> handlers = new ArrayList<HandlerData>();

        while (childIterator.hasNext()) {
            OMElement childOM = (OMElement) childIterator.next();
            if ("handlers".equals(childOM.getLocalName())) {
                Iterator handlerIterator = childOM.getChildElements();
                if (handlerIterator == null) {
                    return apiData;
                }
                while (handlerIterator.hasNext()) {
                    OMElement handlerdOM = (OMElement) handlerIterator.next();
                    HandlerData handler = new HandlerData();
                    convertHandler(handlerdOM, handler);
                    handlers.add(handler);
                }
            } else {
                ResourceData resource = new ResourceData();
                convertResource(childOM, resource);
                resources.add(resource);
            }
        }

        ResourceData[] resourceArray = new ResourceData[resources.size()];
        apiData.setResources(resources.toArray(resourceArray));

        HandlerData[] handlerArray = new HandlerData[handlers.size()];
        apiData.setHandlers(handlers.toArray(handlerArray));

        return apiData;
    }

    /**
     * Convert the handler OMElement to a HandlerData object
     *
     * @param handlerOM handler OMElement
     * @param handlerData the HandlerData object created using the handler OMElement
     */
    private static void convertHandler(OMElement handlerOM, HandlerData handlerData) {
        OMAttribute classPath = handlerOM.getAttribute(new QName("class"));
        Iterator propertyIterator = handlerOM.getChildElements();
        if (propertyIterator != null) {
            ArrayList<String> properties = new ArrayList<>();
            while (propertyIterator.hasNext()) {
                OMElement property = (OMElement) propertyIterator.next();
                String propertyName = property.getAttribute(new QName("name")).getAttributeValue().trim();
                String propertyValue = property.getAttribute(new QName("value")).getAttributeValue().trim();
                properties.add(propertyName + RestAPIConstants.PROPERTY_KEY_VALUE_DELIMITER + propertyValue);
            }
            handlerData.setProperties(properties.toArray(new String[0]));
        }
        if (classPath != null) {
            handlerData.setHandler(classPath.getAttributeValue().trim());
        }
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
            if (protocol.getAttributeValue().equals(PROTOCOL_HTTP)) {
                resourceData.setProtocol(RESTConstants.PROTOCOL_HTTP_ONLY);
            } else if (protocol.getAttributeValue().equals(PROTOCOL_HTTPS)) {
                resourceData.setProtocol(RESTConstants.PROTOCOL_HTTPS_ONLY);
            } else {
                resourceData.setProtocol(Integer.parseInt(protocol.getAttributeValue()));
            }
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
                if ("inSequence".equals(elem.getLocalName()) && inSequence == null) {
                    resourceData.setInSeqXml(elem.toString());
                } else if ("outSequence".equals(elem.getLocalName()) && outSequence == null) {
                    resourceData.setOutSeqXml(elem.toString());
                } else if ("faultSequence".equals(elem.getLocalName()) && faultSequence == null) {
                    resourceData.setFaultSeqXml(elem.toString());
                }
            }
        }
    }
}
