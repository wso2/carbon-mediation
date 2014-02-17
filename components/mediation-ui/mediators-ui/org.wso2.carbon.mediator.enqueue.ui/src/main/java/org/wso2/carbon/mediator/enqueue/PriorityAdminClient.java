package org.wso2.carbon.mediator.enqueue;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.priority.executors.stub.PriorityMediationAdminStub;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: charitha
 * Date: 9/26/12
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class PriorityAdminClient {

    private static final Log log = LogFactory.getLog(PriorityAdminClient.class);

    private PriorityMediationAdminStub stub;

    public PriorityAdminClient(ServletConfig config, HttpSession session) throws AxisFault {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext)
                config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serviceURL = backendServerURL + "PriorityMediationAdmin";
        stub = new PriorityMediationAdminStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public List<String> getExecutors() throws RemoteException {
        String []list = stub.getExecutorList();

        List<String> l = new ArrayList<String>();
        if (list != null && !(list.length == 1 && list[0] == null)) {
            l.addAll(Arrays.asList(list));
        }

        return l;
    }
}
