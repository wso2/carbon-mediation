package org.wso2.carbon.inbound;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.SynapseXMLConfigurationFactory;
import org.apache.synapse.inbound.InboundEndpoint;
import org.apache.synapse.inbound.InboundEndpointConstants;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class CarbonInboundManagementService extends AbstractServiceBusAdmin {

	private static Log log = LogFactory
			.getLog(CarbonInboundManagementService.class);

	public String getAllInboundEndpointNames()
			throws InboundManagementException {
		String strInboundNames = null;
		Collection<InboundEndpoint> inboundEndpoints = getSynapseConfiguration()
				.getInboundEndpoints();
		for (InboundEndpoint inboundEndpoint : inboundEndpoints) {
			if (strInboundNames != null) {
				strInboundNames += "~:~" + inboundEndpoint.getName();
			} else {
				strInboundNames = inboundEndpoint.getName();
			}
		}
		return strInboundNames;
	}

	public InboundEndpointDTO getInboundEndpointbyName(String endointName)
			throws InboundManagementException {
		InboundEndpoint inboundEndpoint = getInboundEndpoint(endointName);
		if(inboundEndpoint != null){
			return new InboundEndpointDTO(inboundEndpoint);
		}
		return null;
	}

	public void addInboundEndpoint(String name, String sequence,
			String onError, String interval, String protocol,String outsequence, String classImpl,
			String[] sParams) throws InboundManagementException {
		SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace(
				"http://ws.apache.org/ns/synapse", "syn");
		OMElement elem = fac.createOMElement("inboundEndpoint", omNs);
		elem.addAttribute(fac.createOMAttribute("name", null, name));
		elem.addAttribute(fac.createOMAttribute("sequence", null, sequence));
		elem.addAttribute(fac.createOMAttribute("onError", null, onError));
		elem.addAttribute(fac.createOMAttribute("interval", null, interval));
        elem.addAttribute(fac.createOMAttribute("outsequence", null, outsequence));
		if (protocol != null) {
			elem.addAttribute(fac.createOMAttribute("protocol", null, protocol));
		} else {
			elem.addAttribute(fac.createOMAttribute("class", null, classImpl));
		}
		OMElement params = fac.createOMElement("parameters", omNs);
		for (String sParam : sParams) {
			String[] aParam = sParam.split("~:~");
			OMElement param = fac.createOMElement("parameter", omNs);
			param.addAttribute(fac.createOMAttribute("name", null, aParam[0]));
			if (aParam.length >= 2) {
				param.setText(aParam[1]);
			}
			params.addChild(param);
		}
		elem.addChild(params);
		SynapseXMLConfigurationFactory.defineInboundEndpoint(
				synapseConfiguration, elem,
				synapseConfiguration.getProperties());
		InboundEndpoint inboundEndpoint = getInboundEndpoint(name);
		persistInboundEndpoint(inboundEndpoint);
		inboundEndpoint.init(getSynapseEnvironment());
	}

    public void addInboundEndpointFromXMLString(String inboundElement){
        XMLStreamReader reader = null;
        try {
            reader = XMLInputFactory.newInstance().createXMLStreamReader(
                    new StringReader(inboundElement));
        } catch (XMLStreamException e) {
            log.error(e.getMessage());
        }
        StAXOMBuilder builder = new StAXOMBuilder(reader);
        OMElement omElement = builder.getDocumentElement();
        SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
        SynapseXMLConfigurationFactory.defineInboundEndpoint(
                synapseConfiguration, omElement,
                synapseConfiguration.getProperties());
        String name = omElement.getAttributeValue(new QName("name"));
        InboundEndpoint inboundEndpoint = null;
        try {
            inboundEndpoint = getInboundEndpoint(name);
        } catch (InboundManagementException e) {
            log.error(e.getMessage());
        }
        persistInboundEndpoint(inboundEndpoint);
        inboundEndpoint.init(getSynapseEnvironment());
    }
	public void updateInboundEndpoint(String name, String sequence,
			String onError, String interval, String protocol,String outsequence, String classImpl,
			String[] sParams) throws InboundManagementException {
		SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace(
				"http://ws.apache.org/ns/synapse", "syn");
		OMElement elem = fac.createOMElement("inboundEndpoint", omNs);
		elem.addAttribute(fac.createOMAttribute("name", null, name));
		elem.addAttribute(fac.createOMAttribute("sequence", null, sequence));
		elem.addAttribute(fac.createOMAttribute("onError", null, onError));
		elem.addAttribute(fac.createOMAttribute("interval", null, interval));
        elem.addAttribute(fac.createOMAttribute("outsequence", null, outsequence));
		if (protocol != null) {
			elem.addAttribute(fac.createOMAttribute("protocol", null, protocol));
		} else {
			elem.addAttribute(fac.createOMAttribute("class", null, classImpl));
		}
		OMElement params = fac.createOMElement("parameters", omNs);
		for (String sParam : sParams) {
			String[] aParam = sParam.split("~:~");
			OMElement param = fac.createOMElement("parameter", omNs);
			param.addAttribute(fac.createOMAttribute("name", null, aParam[0]));
			if (aParam.length >= 2) {
				param.setText(aParam[1]);
			}
			params.addChild(param);
		}
		elem.addChild(params);
		
		InboundEndpoint oldInboundEndpoint = synapseConfiguration.getInboundEndpoint(name);
		if(oldInboundEndpoint != null){
			oldInboundEndpoint.destroy();
			synapseConfiguration.removeInboundEndpoint(name);
		}		
		
		SynapseXMLConfigurationFactory.defineInboundEndpoint(
				synapseConfiguration, elem,
				synapseConfiguration.getProperties());
		InboundEndpoint inboundEndpoint = getInboundEndpoint(name);
		persistInboundEndpoint(inboundEndpoint);
		inboundEndpoint.init(getSynapseEnvironment());
	}

	public void removeInboundEndpoint(String name)
			throws InboundManagementException {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Deleting inbound service : " + name);
			}
			SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
			InboundEndpoint inboundEndpoint = synapseConfiguration
					.getInboundEndpoint(name);
			if (inboundEndpoint != null) {
				synapseConfiguration.removeInboundEndpoint(name);
				inboundEndpoint.destroy();
				MediationPersistenceManager pm = getMediationPersistenceManager();
				pm.deleteItem(name, inboundEndpoint.getFileName(),
						ServiceBusConstants.ITEM_TYPE_INBOUND);
				if (log.isDebugEnabled()) {
					log.debug("Inbound service : " + name + " deleted");
				}
			} else {
				log.warn("No Inbound service exists by the name : " + name);
			}
		} catch (Exception e) {
			log.error("Unable to delete inbound service : " + name, e);
		}

	}

	private void persistInboundEndpoint(InboundEndpoint inboundEndpoint) {
		MediationPersistenceManager pm = getMediationPersistenceManager();
		if (pm == null) {
			log.error("Cannot Persist sequence because persistence manager is null, "
					+ "probably persistence is disabled");
		} else {
			pm.saveItem(inboundEndpoint.getName(),
					ServiceBusConstants.ITEM_TYPE_INBOUND);
		}
	}

	private InboundEndpoint getInboundEndpoint(String endointName)
			throws InboundManagementException {
		Collection<InboundEndpoint> inboundEndpoints = getSynapseConfiguration()
				.getInboundEndpoints();
		for (InboundEndpoint inboundEndpoint : inboundEndpoints) {
			if (endointName.equals(inboundEndpoint.getName())) {
				return (inboundEndpoint);
			}
		}
		return null;
	}
}
