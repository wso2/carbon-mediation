package org.wso2.carbon.inbound.ui.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

	private Properties prop = null;
	
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

	private void loadProperties(){
        if (prop == null) {
            prop = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream(
                    "/config/inbound.properties");
            if (is != null) {
                try {
                    prop.load(is);
                } catch (IOException e) {
                    log.error("Unable to load properties.", e);
                }
            }
        }
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
		if(!strType.equals(InboundClientConstants.TYPE_HTTP)){
		    rtnList.addAll(getList("common",true));
		}
		if(!strType.equals(InboundClientConstants.TYPE_CLASS)){
		    rtnList.addAll(getList(strType,true));
		}
		return rtnList;
	}
	public List<String> getAdvParameters(String strType) {
		List<String> rtnList = new ArrayList<String>();
        if(!strType.equals(InboundClientConstants.TYPE_CLASS)){
            rtnList.addAll(getList(strType,false));
        }		
		return rtnList;
	}		

	public boolean addInboundEndpoint(String name, String sequence,
			String onError,  String protocol, String classImpl,
			List<String> lParameters) {
		try {
			String[] sParams = new String[lParameters.size()];
			int i = 0;
			for (String parameter : lParameters) {
				sParams[i] = parameter;
				i++;
			}
			stub.addInboundEndpoint(name, sequence, onError,
					protocol, classImpl, sParams);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private List<String> getList(String strProtocol, boolean mandatory){
	    List<String> rtnList = new ArrayList<String>();
	    loadProperties();
	    if(prop != null){
	        String strKey = strProtocol;
	        if(mandatory){
	            strKey += ".mandatory";
	        }else{
	            strKey += ".optional";
	        }
	        String strLength = prop.getProperty(strKey);
	        Integer iLength = null;
	        if(strLength != null){
	            try{
	                iLength = Integer.parseInt(strLength);
	            }catch(Exception e){
	                iLength = null;
	            }
	        }
	        if(iLength != null){
	            for(int i = 1;i <= iLength;i++){
	                String tmpString = strKey + "." + i;
	                String strVal = prop.getProperty(tmpString);
	                if(strVal != null){
	                    rtnList.add(strVal);
	                }
	            }
	        }
	    }
	    return rtnList;
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
			String onError,  String protocol, String classImpl,
			List<String> lParameters) {
		try {
			String[] sParams = new String[lParameters.size()];
			int i = 0;
			for (String parameter : lParameters) {
				sParams[i] = parameter;
				i++;
			}
			stub.updateInboundEndpoint(name, sequence, onError,
					protocol, classImpl, sParams);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}	

}
