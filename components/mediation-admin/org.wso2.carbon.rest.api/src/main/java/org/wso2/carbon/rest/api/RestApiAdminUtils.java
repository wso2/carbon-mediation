package org.wso2.carbon.rest.api;

import org.apache.axiom.om.*;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.SequenceMediatorSerializer;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.dispatch.DispatcherHelper;
import org.apache.synapse.rest.dispatch.URITemplateHelper;
import org.apache.synapse.rest.dispatch.URLMappingHelper;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.rest.api.service.RestApiAdminConstants;

import javax.xml.stream.XMLStreamException;
import java.util.Iterator;

public class RestApiAdminUtils {

    private static String PROTOCOL_HTTP = "http";
    private static String PROTOCOL_HTTPS = "https";
	
	public static OMElement retrieveAPIOMElement(APIData apiData) {

		OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace syn = SynapseConstants.SYNAPSE_OMNAMESPACE;
        OMNamespace nullNS = fac.createOMNamespace("","");

        OMElement api = fac.createOMElement("api", syn);
        api.addAttribute("name", apiData.getName(), nullNS);

        if (apiData.getContext() != null) {
            api.addAttribute("context", apiData.getContext(), nullNS);
        }

        if (apiData.getHost() != null) {
            api.addAttribute("hostname", apiData.getHost(), nullNS);
        }

        if (apiData.getPort() != -1) {
            api.addAttribute("port", String.valueOf(apiData.getPort()), nullNS);
        }

        if (apiData.getVersion() != null) {
            api.addAttribute("version", String.valueOf(apiData.getVersion()), nullNS);
        }

        if (apiData.getVersionType() != null) {
            api.addAttribute("version-type", String.valueOf(apiData.getVersionType()), nullNS);
        }

		if (apiData.getResources() != null && apiData.getResources().length != 0) {
			for (ResourceData resourceData : apiData.getResources()) {
				api.addChild(retrieveResourceOMElement(resourceData));
			}
		}

        if (apiData.getHandlers() != null && apiData.getHandlers().length != 0) {

            OMElement handlers = fac.createOMElement("handlers", syn);
            for (HandlerData handlerData : apiData.getHandlers()) {
                handlers.addChild(retrieveHandlersOMElement(handlerData));
            }
            api.addChild(handlers);
        }
        return api;
	}

    private static OMNode retrieveHandlersOMElement(HandlerData handlerData) {
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace syn = SynapseConstants.SYNAPSE_OMNAMESPACE;
        OMNamespace nullNS = fac.createOMNamespace("","");

        OMElement handler = fac.createOMElement("handler", syn);

        if (handlerData.getHandler() != null) {
            handler.addAttribute("class", handlerData.getHandler(), nullNS);
        }
        String[] properties = handlerData.getProperties();
        if (properties != null && properties.length != 0) {
            for (String property : properties) {
                if (!property.equals("")) {
                    // the property value is split into it's key and value
                    String[] propertyItems = property.split(RestApiAdminConstants.PROPERTY_KEY_VALUE_DELIMITER);
                    OMElement propertyElem = fac.createOMElement("property", syn);
                    // the key and value fetched above are assigned as attributes
                    propertyElem.addAttribute("name", propertyItems[0], nullNS);
                    propertyElem.addAttribute("value", propertyItems[1], nullNS);
                    handler.addChild(propertyElem);
                }
            }
        }
        return handler;
    }

    public static OMElement retrieveResourceOMElement(ResourceData resourceData) {
		OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace syn = SynapseConstants.SYNAPSE_OMNAMESPACE;
        OMNamespace nullNS = fac.createOMNamespace("","");

        OMElement resource = fac.createOMElement("resource", syn);
        
        if (resourceData.getMethods() != null && resourceData.getMethods().length != 0) {
        	String methodsString = createSSString(resourceData.getMethods());
        	resource.addAttribute("methods", methodsString, nullNS);
        }
        if (resourceData.getUriTemplate() != null) {
        	resource.addAttribute("uri-template", resourceData.getUriTemplate(), nullNS);
        }
        else if (resourceData.getUrlMapping() != null) {
        	resource.addAttribute("url-mapping", resourceData.getUrlMapping(), nullNS);
        }
        if (resourceData.getContentType() != null) {
        	resource.addAttribute("contentType", resourceData.getContentType(), nullNS);
        }
        if (resourceData.getUserAgent() != null) {
        	resource.addAttribute("userAgent", resourceData.getUserAgent(), nullNS);
        }
        if (resourceData.getProtocol() != RESTConstants.PROTOCOL_HTTP_AND_HTTPS) {
            if (resourceData.getProtocol() == RESTConstants.PROTOCOL_HTTP_ONLY) {
        	    resource.addAttribute("protocol", PROTOCOL_HTTP, nullNS);
            } else if (resourceData.getProtocol() == RESTConstants.PROTOCOL_HTTPS_ONLY) {
                resource.addAttribute("protocol", PROTOCOL_HTTPS, nullNS);
            } else {
                resource.addAttribute("protocol", String.valueOf(resourceData.getProtocol()), nullNS);
            }
        }

        try {
            if (resourceData.getInSequenceKey() != null) {
                resource.addAttribute("inSequence", resourceData.getInSequenceKey(), nullNS);
            } else if (resourceData.getInSeqXml() != null && !resourceData.getInSeqXml().isEmpty()) {
                resource.addChild(AXIOMUtil.stringToOM(resourceData.getInSeqXml()));
            }
            if (resourceData.getOutSequenceKey() != null) {
                resource.addAttribute("outSequence", resourceData.getOutSequenceKey(), nullNS);
            } else if (resourceData.getOutSeqXml() != null && !resourceData.getOutSeqXml().isEmpty()) {
                resource.addChild(AXIOMUtil.stringToOM(resourceData.getOutSeqXml()));
            }
            if (resourceData.getFaultSequenceKey() != null) {
                resource.addAttribute("faultSequence", resourceData.getFaultSequenceKey(), nullNS);
            } else if (resourceData.getFaultSeqXml() != null && !resourceData.getFaultSeqXml().isEmpty()) {
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

    public static OMElement createAnonymousSequenceElement(SequenceMediator sequenceMediator, String seqElemName) {
        SequenceMediatorSerializer serializer = new SequenceMediatorSerializer();
        OMElement sequenceElem = serializer.serializeAnonymousSequence(null, sequenceMediator);
        if (!"inSequence".equals(seqElemName)
                && !"outSequence".equals(seqElemName)
                && !"faultSequence".equals(seqElemName)) {
            return null;
        }
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace syn = SynapseConstants.SYNAPSE_OMNAMESPACE;
        OMNamespace nullNS = fac.createOMNamespace("","");

        OMElement seq = fac.createOMElement(seqElemName, syn);
        Iterator<OMAttribute> attributes = sequenceElem.getAllAttributes();
        for (; attributes.hasNext(); ) {
            OMAttribute attrb = attributes.next();
            seq.addAttribute(attrb.getLocalName(), attrb.getAttributeValue(), nullNS);
        }
        Iterator<OMElement> children = sequenceElem.getChildElements();
        for (; children.hasNext(); ) {
            OMElement child = children.next();
            seq.addChild(child);
        }
        return seq;
    }

    /**
     * Override the parent's getSynapseconfig() method to retrieve the Synapse
     * configuration from the relevant axis configuration
     *
     * @return extracted SynapseConfiguration from the relevant
     *         AxisConfiguration
     */
    public static SynapseConfiguration getSynapseConfiguration() throws APIException {
        return (SynapseConfiguration) ConfigHolder.getInstance().getAxisConfiguration().getParameter(
                SynapseConstants.SYNAPSE_CONFIG).getValue() ;
    }

    /**
     * Helper method to get the persistence manger
     * @return persistence manager for this configuration context
     */
    public static MediationPersistenceManager getMediationPersistenceManager() throws APIException {
        return ServiceBusUtils.getMediationPersistenceManager(ConfigHolder.getInstance().getAxisConfiguration());
    }

    /**
     * Helper method to retrieve the Synapse environment from the relevant axis configuration
     *
     * @return extracted SynapseEnvironment from the relevant AxisConfiguration
     */
    public static SynapseEnvironment getSynapseEnvironment() throws APIException {
        return getSynapseEnvironment(ConfigHolder.getInstance().getAxisConfiguration());
    }

    public static SynapseEnvironment getSynapseEnvironment(AxisConfiguration axisCfg) {
        return (SynapseEnvironment) axisCfg.getParameter(
                SynapseConstants.SYNAPSE_ENV).getValue();
    }

    public static APIData convertApiToAPIData(API api) {
        if (api == null) {
            return null;
        }

        APIData apiData = new APIData();
        apiData.setName(api.getName());
        apiData.setContext(api.getContext());
        apiData.setHost(api.getHost());
        apiData.setPort(api.getPort());
        apiData.setFileName(api.getFileName());
        apiData.setVersion(api.getVersion());
        apiData.setVersionType(api.getVersionStrategy().getVersionType());

        Resource[] resources = api.getResources();
        ResourceData[] resourceDatas = new ResourceData[resources.length];

        for (int i = 0; i < resources.length; i++) {

            Resource resource = resources[i];
            ResourceData data = new ResourceData();

            String[] methods = resource.getMethods();
            data.setMethods(methods);
            data.setContentType(resource.getContentType());
            data.setProtocol(resource.getProtocol());
            DispatcherHelper dispatcherHelper = resource.getDispatcherHelper();
            if (dispatcherHelper instanceof URITemplateHelper) {
                data.setUriTemplate(dispatcherHelper.getString());
            } else if (dispatcherHelper instanceof URLMappingHelper) {
                data.setUrlMapping(dispatcherHelper.getString());
            }

            if (resource.getInSequenceKey() != null) {
                data.setInSequenceKey(resource.getInSequenceKey());
            } else if (resource.getInSequence() != null) {
                data.setInSeqXml(RestApiAdminUtils.createAnonymousSequenceElement(
                        resource.getInSequence(),
                        "inSequence"
                ).toString());
            }

            if (resource.getOutSequenceKey() != null) {
                data.setOutSequenceKey(resource.getOutSequenceKey());
            } else if (resource.getOutSequence() != null) {
                data.setOutSeqXml(RestApiAdminUtils.createAnonymousSequenceElement(
                        resource.getOutSequence(),
                        "outSequence"
                ).toString());
            }

            if (resource.getFaultSequenceKey() != null) {
                data.setFaultSequenceKey(resource.getFaultSequenceKey());
            } else if (resource.getFaultSequence() != null) {
                data.setFaultSeqXml(RestApiAdminUtils.createAnonymousSequenceElement(
                        resource.getFaultSequence(),
                        "faultSequence"
                ).toString());
            }
            data.setUserAgent(resource.getUserAgent());

            resourceDatas[i] = data;
        }
        apiData.setResources(resourceDatas);
        return apiData;
    }

    public static void persistApi(API api) throws APIException {
        if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
            MediationPersistenceManager pm = RestApiAdminUtils.getMediationPersistenceManager();
            if (pm != null) {
                pm.saveItem(api.getName(), ServiceBusConstants.ITEM_TYPE_REST_API);
            }
        }
    }

    public static boolean isNullOrEmpty(String s) {
        return (s == null || s.isEmpty());
    }

}
