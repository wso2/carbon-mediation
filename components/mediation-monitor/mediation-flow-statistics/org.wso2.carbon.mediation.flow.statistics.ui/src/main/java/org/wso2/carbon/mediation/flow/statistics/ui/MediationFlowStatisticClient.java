package org.wso2.carbon.mediation.flow.statistics.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.flow.statistics.stub.AdminData;

import org.wso2.carbon.mediation.flow.statistics.stub.MediationFlowStatisticsAdminStub;
import org.wso2.carbon.mediation.flow.statistics.stub.StatisticTreeWrapper;

public class MediationFlowStatisticClient {

	private static final Log log = LogFactory.getLog(MediationFlowStatisticClient.class);
	private MediationFlowStatisticsAdminStub stub;

	public MediationFlowStatisticClient(ConfigurationContext configCtx, String backendServerURL,
	                                    String cookie) throws AxisFault {
		String serviceURL = backendServerURL + "MediationFlowStatisticsAdmin";
		stub = new MediationFlowStatisticsAdminStub(configCtx, serviceURL);
		ServiceClient client = stub._getServiceClient();
		Options options = client.getOptions();
		options.setManageSession(true);
		options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	}

	public AdminData[] getAllApiStatistics() throws AxisFault {
		try {
			return stub.getAllApiStatistics();
		}catch (Exception e) {
			String msg = "Cannot get stat data. Backend service may be unavailable";
			handleException(msg, e);
		}
		return null;
	}

	public AdminData[] getAllProxyStatistics() throws AxisFault {
		try {
			return stub.getAllProxyStatistics();
		}catch (Exception e) {
			String msg = "Cannot get stat data. Backend service may be unavailable";
			handleException(msg, e);
		}
		return null;
	}

	public AdminData[] getAllSequenceStatistics() throws AxisFault {
		try {
			return stub.getAllSequenceStatistics();
		}catch (Exception e) {
			String msg = "Cannot get stat data. Backend service may be unavailable";
			handleException(msg, e);
		}
		return null;
	}

	public AdminData[] getAllInboundEndpointStatistics() throws AxisFault {
		try {
			return stub.getAllInboundEndpointStatistics();
		}catch (Exception e) {
			String msg = "Cannot get stat data. Backend service may be unavailable";
			handleException(msg, e);
		}
		return null;
	}

	public StatisticTreeWrapper getProxyStatistic(String componentId) throws AxisFault {
		try {
			return stub.getProxyStatistics(componentId);
		}catch (Exception e) {
			String msg = "Cannot get stat data. Backend service may be unavailable";
			handleException(msg, e);
		}
		return null;
	}

	public StatisticTreeWrapper getApiStatistic(String componentId) throws AxisFault {
		try {
			return stub.getApiStatistics(componentId);
		}catch (Exception e) {
			String msg = "Cannot get stat data. Backend service may be unavailable";
			handleException(msg, e);
		}
		return null;
	}

	public StatisticTreeWrapper getSequenceStatistic(String componentId) throws AxisFault {
		try {
			return stub.getSequenceStatistics(componentId);
		}catch (Exception e) {
			String msg = "Cannot get stat data. Backend service may be unavailable";
			handleException(msg, e);
		}
		return null;
	}

	public StatisticTreeWrapper getInboundEndpointStatistic(String componentId) throws AxisFault {
		try {
			return stub.getInboundEndpointStatistics(componentId);
		}catch (Exception e) {
			String msg = "Cannot get stat data. Backend service may be unavailable";
			handleException(msg, e);
		}
		return null;
	}

	private void handleException(String msg, Exception e) throws AxisFault {
		if (e instanceof AxisFault) {
			msg = e.getMessage();
		}
		log.error(msg, e);
		throw new AxisFault(msg, e);
	}
}
