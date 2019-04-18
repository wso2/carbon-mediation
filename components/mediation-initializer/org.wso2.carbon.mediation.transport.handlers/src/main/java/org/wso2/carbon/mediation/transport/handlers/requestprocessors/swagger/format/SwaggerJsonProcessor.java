/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediation.transport.handlers.requestprocessors.swagger.format;

import net.minidev.json.JSONObject;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.rest.API;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.transports.CarbonHttpRequest;
import org.wso2.carbon.core.transports.CarbonHttpResponse;
import org.wso2.carbon.core.transports.HttpGetRequestProcessor;
import org.wso2.carbon.mediation.registry.RegistryServiceHolder;
import org.wso2.carbon.mediation.transport.handlers.requestprocessors.swagger.SwaggerConstants;
import org.wso2.carbon.mediation.transport.handlers.requestprocessors.swagger.GenericApiObjectDefinition;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import java.nio.charset.Charset;

/**
 * Provides Swagger definition for the API in JSON format.
 */
public class SwaggerJsonProcessor extends SwaggerGenerator implements HttpGetRequestProcessor {
    Log log = LogFactory.getLog(SwaggerJsonProcessor.class);
    /**
     * Process incoming GET request and update the response with the swagger definition for the requested API
     *
     * @param request              CarbonHttpRequest contains request information
     * @param response             CarbonHttpResponse which will be updated with response information
     * @param configurationContext axis2 configuration context
     * @throws Exception if any exception occurred during definition generation
     */
    public void process(CarbonHttpRequest request, CarbonHttpResponse response,
                        ConfigurationContext configurationContext) throws AxisFault {
        API api = getAPIFromSynapseConfig(request);

        if (api == null) {
            handleException(request.getRequestURI());
        } else {
            String resourcePath = SwaggerConstants.Registry_path + api.getAPIName() + ":v" + api.getVersion() + "/swagger.json";
            RegistryService registryService = RegistryServiceHolder.getInstance().getRegistryService();
            String responseString;
            try {

                String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(request.getRequestURI());
                tenantDomain = (tenantDomain != null) ? tenantDomain : MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

                Registry registry = registryService.getConfigSystemRegistry(tenantId);
                Resource resource;
                if (registry.resourceExists(resourcePath)) {
                    resource = registry.get(resourcePath);
                    responseString = new String((byte[]) resource.getContent(), Charset.defaultCharset());
                } else {
                    JSONObject jsonDefinition = new JSONObject(new GenericApiObjectDefinition(api).getDefinitionMap());
                    responseString = jsonDefinition.toString();
                }
            } catch (RegistryException e) {
                log.error("Could not get swagger document", e);
                throw new AxisFault("Could not get swagger document", e);

            }
            updateResponse(response, responseString, SwaggerConstants.CONTENT_TYPE_JSON);
        }
    }
}
