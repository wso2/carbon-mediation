/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.transports.sap;

import org.apache.axis2.transport.base.AbstractTransportListenerEx;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.transports.sap.idoc.IDocEndpoint;
import org.wso2.carbon.transports.sap.bapi.BAPIEndpoint;
import com.sap.conn.jco.ext.Environment;

/**
 * <code>SAPTransportListener </code> provides the transport listener implementation for SAP adapter
 * Listner parameters can be configured either in axis2.xml ( golbal) or using service level parameters
 * (services.xml or parameters in proxy service configurations)
 */
public class SAPTransportListener extends AbstractTransportListenerEx<SAPEndpoint> {

    @Override
    public void doInit() throws AxisFault {
        CarbonDestinationDataProvider provider = new CarbonDestinationDataProvider();
        if (!Environment.isServerDataProviderRegistered()) {
            Environment.registerServerDataProvider(provider);
        }
        if (!Environment.isDestinationDataProviderRegistered()) {
            Environment.registerDestinationDataProvider(provider);
        }
        //super.init(cfgCtx, transportIn);
    }

    protected SAPEndpoint createEndpoint() {
        String transportName = getTransportName();
        if (SAPConstants.SAP_IDOC_PROTOCOL_NAME.equals(transportName)) {
            return new IDocEndpoint();
        } else if (SAPConstants.SAP_BAPI_PROTOCOL_NAME.equals(transportName)) {
            return new BAPIEndpoint();
        } else {
            throw new UnsupportedOperationException("Protocol name: " + transportName + " is not " +
                    "supported");
        }
    }

    protected void startEndpoint(SAPEndpoint endpoint) throws AxisFault {
        endpoint.startEndpoint(workerPool);
    }

    protected void stopEndpoint(SAPEndpoint endpoint) {
        endpoint.stopEndpoint();
    }
}
