/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mediation.statistics.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.statistics.stub.MediationStatisticsAdminStub;
import org.wso2.carbon.mediation.statistics.stub.InOutStatisticsRecord;
import org.wso2.carbon.mediation.statistics.stub.GraphData;

import java.rmi.RemoteException;

public class MediationStatisticsClient {

    public static final int SERVER_STATISTICS = 3;
    public static final int SEQUENCE_STATISTICS = 2;
    public static final int PROXYSERVICE_STATISTICS = 1;
    public static final int ENDPOINT_STATISTICS = 0;

    private static final Log log = LogFactory.getLog(MediationStatisticsClient.class);
    public MediationStatisticsAdminStub stub;

    public MediationStatisticsClient(ConfigurationContext configCtx, String backendServerURL,
            String cookie) throws AxisFault {
        String serviceURL = backendServerURL + "MediationStatisticsAdmin";
        stub = new MediationStatisticsAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public double calculateAverageTime(InOutStatisticsRecord record) {
        double t1 = 0.0, t2 = 0.0;
        int c1 = 0, c2 = 0;

        if (record.getInRecord() != null) {
            t1 = record.getInRecord().getAvgTime();
            c1 = record.getInRecord().getTotalCount();
        }

        if (record.getOutRecord() != null) {
            t2 = record.getOutRecord().getAvgTime();
            c2 = record.getOutRecord().getTotalCount();
        }

        if (c1 + c2 == 0) {
            return 0D;
        }

        return (t1*c1 + t2*c2)/(c1+c2);
    }

   public GraphData getDataForGraph() throws AxisFault {
        try {
            return stub.getDataForGraph();
        } catch (Exception e) {
            String msg = "Cannot get graph data. Backepnd service may be unvailable";
            handleException(msg, e);
        }
       return null;
    }

    public InOutStatisticsRecord getCategoryStatistics(int category) throws AxisFault {
        try {
            return stub.getCategoryStatistics(category);
        } catch (Exception e) {
            String msg = "Cannot get category statistics. Backepnd service may be unvailable";
            handleException(msg, e);
        }
        return null;
    }

    public String[] listSequence() throws AxisFault {
        String[] sequences = null;
        try {
            sequences = stub.listSequence();
        } catch (Exception e) {
            String msg = "Cannot list sequences. Backepnd service may be unvailable";
            handleException(msg, e);
        }

        if (sequences == null || sequences.length == 0 || sequences[0] == null) {
            return null;
        }
        return sequences;
    }

    public String[] listEndPoints() throws AxisFault {
        String[] endpoints = null;
        try {
            endpoints = stub.listEndPoint();
        } catch (Exception e) {
            String msg = "Cannot list endpoints. Backend service may be unvailable";
            handleException(msg, e);
        }

        if (endpoints == null || endpoints.length == 0 || endpoints[0] == null) {
            return null;
        }
        return endpoints;
    }

    public String[] listProxyServices() throws AxisFault {
        String[] services = null;
        try {
            services = stub.listProxyServices();
        } catch (Exception e) {
            String msg = "Cannot list proxy services. Backend service may be unvailable";
            handleException(msg, e);
        }

        if (services == null || services.length == 0 || services[0] == null) {
            return null;
        }
        return services;
    }

    public String[] listServers() throws AxisFault {
        try {
            return stub.listServers();
        } catch (Exception e) {
            String msg = "Cannot list servers. Backepnd service may be unvailable";
            handleException(msg, e);
        }
        return null;
    }


    public InOutStatisticsRecord getSequenceStatistics(String name) throws AxisFault {
        try {
            return stub.getSequenceStatistics(name);
        } catch (Exception e) {
            String msg = "Cannot get squence data. Backend service may be unvailable";
            handleException(msg, e);
        }
        return null;
    }

    public InOutStatisticsRecord getEndPointStatistics(String name) throws AxisFault {
        try {
            return stub.getEndPointStatistics(name);
        } catch (Exception e) {
            String msg = "Cannot get endpoint data. Backepnd service may be unvailable";
            handleException(msg, e);
        }
        return null;
    }

    public InOutStatisticsRecord getProxyServiceStatistics(String name) throws AxisFault {
        try {
            return stub.getProxyServiceStatistics(name);
        } catch (RemoteException e) {
            String msg = "Cannot get proxy service data. Backend service may be unavailable.";
            handleException(msg, e);
        }
        return null;
    }

    public InOutStatisticsRecord getServerStatistics() throws AxisFault {
        try {
            return stub.getServerStatistics();
        } catch (Exception e) {
            String msg = "Cannot get server data. Backend service may be unvailable";
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