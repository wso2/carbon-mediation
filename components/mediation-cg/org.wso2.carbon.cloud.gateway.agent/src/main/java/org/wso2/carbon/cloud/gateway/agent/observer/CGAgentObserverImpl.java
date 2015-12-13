/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.gateway.agent.observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.gateway.agent.service.CGAgentAdminService;
import org.wso2.carbon.cloud.gateway.common.CGConstant;
import org.wso2.carbon.cloud.gateway.common.CGException;

/**
 * Implementation for {@link CGAgentObserver}
 */
public class CGAgentObserverImpl implements CGAgentObserver {

    private String host;
    
    private int port;

    private String serviceName;

    private Log log = LogFactory.getLog(CGAgentObserverImpl.class);

    public CGAgentObserverImpl(String host, String serviceName, int port) {
        this.host = host;
        this.serviceName = serviceName;
        this.port = port;
    }

    public void update(CGAgentSubject subject) {
        try {
            CGAgentAdminService service = new CGAgentAdminService();
            String status = service.getServiceStatus(serviceName);
            if (status.equals(CGConstant.CG_SERVICE_STATUS_UNPUBLISHED)) {
                return;
            }
            // do the re-publishing of the service again
            boolean isAutomatic = true;
            if (!status.equals(CGConstant.CG_SERVICE_STATUS_PUBLISHED)) {
                isAutomatic = false;
            }
            String serverName = service.getPublishedServer(serviceName);
            service.unPublishService(serviceName, serverName, true);
            service.publishService(serviceName, serverName, isAutomatic);
        } catch (CGException e) {
            log.error("Error while re-publishing the service '" + serviceName + "' via " +
                    "received publish notification. You may need to re-publish the service manually!");
        }
    }

    public String getHostName() {
        return host;
    }
    
    public int getPort(){
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CGAgentObserverImpl that = (CGAgentObserverImpl) o;

        if (port != that.port) {
            return false;
        }
        if (host != null ? !host.equals(that.host) : that.host != null) {
            return false;
        }
        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
        return result;
    }
}
