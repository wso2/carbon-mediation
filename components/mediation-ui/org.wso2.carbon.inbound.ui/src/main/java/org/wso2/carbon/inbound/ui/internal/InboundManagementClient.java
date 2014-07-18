package org.wso2.carbon.inbound.ui.internal;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.inbound.stub.InboundAdminStub;
import org.wso2.carbon.inbound.stub.types.carbon.InboundEndpointDTO;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

public class InboundManagementClient {

	private static final Log log = LogFactory
			.getLog(InboundManagementClient.class);

	private InboundAdminStub stub;

	private InboundManagementClient(String cookie, String backendServerURL,
			ConfigurationContext configCtx) throws AxisFault {

		String serviceURL = backendServerURL + "InboundAdmin";
		stub = new InboundAdminStub(configCtx, serviceURL);
		ServiceClient client = stub._getServiceClient();
		Options option = client.getOptions();
		option.setManageSession(true);
		option.setProperty(
				org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
				cookie);

	}

	public static InboundManagementClient getInstance(ServletConfig config,
			HttpSession session) throws AxisFault {

		String backendServerURL = CarbonUIUtil.getServerURL(
				config.getServletContext(), session);
		ConfigurationContext configContext = (ConfigurationContext) config
				.getServletContext().getAttribute(
						CarbonConstants.CONFIGURATION_CONTEXT);

		String cookie = (String) session
				.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
		return new InboundManagementClient(cookie, backendServerURL,
				configContext);
	}

	public List<InboundDescription> getAllInboundDescriptions()
			throws Exception {

		String strInboundNames = stub.getAllInboundEndpointNames();
		if (log.isDebugEnabled()) {
			log.debug("All Inbound configurations :" + strInboundNames);
		}
		List<InboundDescription> descriptions = new ArrayList<InboundDescription>();
		if (strInboundNames == null || strInboundNames.equals("")) {
			return descriptions;
		}

		for (String strName : strInboundNames.split("~:~")) {
			InboundDescription inboundDescription = new InboundDescription(
					strName);
			descriptions.add(inboundDescription);
		}
		if (log.isDebugEnabled()) {
			log.debug("All Inbound Descriptions :" + descriptions);
		}
		return descriptions;
	}

	public List<String> getDefaultParameters(String strType) {
		List<String> rtnList = new ArrayList<String>();
		if (strType.equals(InboundClientConstants.TYPE_FILE)) {
			rtnList.add("transport.vfs.FileURI" + InboundClientConstants.STRING_SPLITTER);					
		} else if (strType.equals(InboundClientConstants.TYPE_JMS)) {
			rtnList.add("java.naming.factory.initial" + InboundClientConstants.STRING_SPLITTER);
			rtnList.add("java.naming.provider.url" + InboundClientConstants.STRING_SPLITTER);
			rtnList.add("transport.jms.ConnectionFactoryJNDIName" + InboundClientConstants.STRING_SPLITTER);
			rtnList.add("transport.jms.ConnectionFactoryType" + InboundClientConstants.STRING_SPLITTER + "topic"  + InboundClientConstants.STRING_SPLITTER + "queue");			
			rtnList.add("transport.jms.Destination");
			rtnList.add("transport.jms.SessionTransacted" + InboundClientConstants.STRING_SPLITTER + "false" + InboundClientConstants.STRING_SPLITTER + "true");
			rtnList.add("transport.jms.SessionAcknowledgement"+ InboundClientConstants.STRING_SPLITTER + "AUTO_ACKNOWLEDGE" + InboundClientConstants.STRING_SPLITTER + "CLIENT_ACKNOWLEDGE" + InboundClientConstants.STRING_SPLITTER + "DUPS_OK_ACKNOWLEDGE" + InboundClientConstants.STRING_SPLITTER + "SESSION_TRANSACTED");
			rtnList.add("transport.jms.CacheLevel" + InboundClientConstants.STRING_SPLITTER + "1" + InboundClientConstants.STRING_SPLITTER + "2" + InboundClientConstants.STRING_SPLITTER + "3" + InboundClientConstants.STRING_SPLITTER + "4" + InboundClientConstants.STRING_SPLITTER + "5");
		}
		return rtnList;
	}
	public List<String> getAdvParameters(String strType) {
		List<String> rtnList = new ArrayList<String>();
		if (strType.equals(InboundClientConstants.TYPE_FILE)) {
			rtnList.add("transport.vfs.FileNamePattern" + InboundClientConstants.STRING_SPLITTER);
			rtnList.add("transport.vfs.FileProcessInterval");
			rtnList.add("transport.vfs.FileProcessCount");
			rtnList.add("transport.vfs.Locking" + InboundClientConstants.STRING_SPLITTER + "enable" + InboundClientConstants.STRING_SPLITTER + "disable");
			rtnList.add("transport.vfs.MaxRetryCount");
			rtnList.add("transport.vfs.ReconnectTimeout");
			rtnList.add("transport.vfs.MoveAfterProcess");
			rtnList.add("transport.vfs.ActionAfterProcess" + InboundClientConstants.STRING_SPLITTER + "NONE" + InboundClientConstants.STRING_SPLITTER + "MOVE");
			rtnList.add("transport.vfs.MoveAfterProcess");
		    rtnList.add("transport.vfs.ActionAfterErrors" + InboundClientConstants.STRING_SPLITTER + "NONE" + InboundClientConstants.STRING_SPLITTER + "MOVE");
		    rtnList.add("transport.vfs.MoveAfterErrors");
			rtnList.add("transport.vfs.ActionAfterFailure" + InboundClientConstants.STRING_SPLITTER + "NONE" + InboundClientConstants.STRING_SPLITTER + "MOVE");
			rtnList.add("transport.vfs.MoveAfterFailure");
		} else if (strType.equals(InboundClientConstants.TYPE_JMS)) {
			rtnList.add("transport.jms.UserName");
			rtnList.add("transport.jms.Password");
			rtnList.add("transport.jms.JMSSpecVersion");
			rtnList.add("transport.jms.SubscriptionDurable");
			rtnList.add("transport.jms.DurableSubscriberClientID");
			rtnList.add("transport.jms.MessageSelector");
		}
		return rtnList;
	}		

	public boolean addInboundEndpoint(String name, String sequence,
			String onError, String interval, String protocol, String classImpl,
			List<String> lParameters) {
		try {
			String[] sParams = new String[lParameters.size()];
			int i = 0;
			for (String parameter : lParameters) {
				sParams[i] = parameter;
				i++;
			}
			stub.addInboundEndpoint(name, sequence, onError, interval,
					protocol, classImpl, sParams);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean removeInboundEndpoint(String name) {
		try {
			stub.removeInboundEndpoint(name);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public InboundDescription getInboundDescription(String name) {
		try {
			InboundEndpointDTO inboundEndpointDTO = stub
					.getInboundEndpointbyName(name);
			return new InboundDescription(inboundEndpointDTO);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean updteInboundEndpoint(String name, String sequence,
			String onError, String interval, String protocol, String classImpl,
			List<String> lParameters) {
		try {
			String[] sParams = new String[lParameters.size()];
			int i = 0;
			for (String parameter : lParameters) {
				sParams[i] = parameter;
				i++;
			}
			stub.updateInboundEndpoint(name, sequence, onError, interval,
					protocol, classImpl, sParams);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}	

}
