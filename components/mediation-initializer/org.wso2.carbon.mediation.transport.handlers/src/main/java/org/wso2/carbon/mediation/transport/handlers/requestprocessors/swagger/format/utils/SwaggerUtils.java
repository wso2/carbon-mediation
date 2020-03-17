/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediation.transport.handlers.requestprocessors.swagger.format.utils;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.models.Info;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.*;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisResource;
import org.apache.axis2.description.AxisResourceMap;
import org.apache.axis2.description.AxisResourceParameter;
import org.apache.axis2.description.AxisResources;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.commons.rest.api.swagger.ServerConfig;
import org.wso2.carbon.mediation.registry.RegistryServiceHolder;
import org.wso2.carbon.mediation.transport.handlers.requestprocessors.swagger.format.CarbonServerConfig;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Util class with methods to generate swagger definition and fetch them from the registry.
 */
public final class SwaggerUtils {

    private static Log logger = LogFactory.getLog(SwaggerUtils.class.getName());

    /**
     * Fetch the swagger from registry if available or create one from scratch.
     *
     * @param requestURI           request URI.
     * @param configurationContext Configuration context with details.
     * @param isJSON               result format JSON or YAML.
     * @return Swagger definition as string
     * @throws AxisFault Error occurred while fetching the host details.
     */
    public static String getDataServiceSwagger(String requestURI, ConfigurationContext configurationContext,
                                               boolean isJSON) throws AxisFault {

        String dataServiceName = requestURI.substring(requestURI.lastIndexOf("/") + 1);
        String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(requestURI);
        AxisService dataService;
        if (tenantDomain != null) {
            ConcurrentHashMap<String, ConfigurationContext> contextConcurrentHashMap =
                    (ConcurrentHashMap<String, ConfigurationContext>) configurationContext
                            .getProperty(SwaggerProcessorConstants.TENANT_CONTEXT_PROPERTY_NAME);
            ConfigurationContext tenantConfigContext = contextConcurrentHashMap.get(tenantDomain);
            dataService = tenantConfigContext.getAxisConfiguration().getService(dataServiceName);
        } else {
            dataService = configurationContext.getAxisConfiguration().getService(dataServiceName);
        }

        if (dataService != null) {
            ServerConfig serverConfig = new CarbonServerConfig();
            Object dataServiceObject =
                    dataService.getParameter(SwaggerProcessorConstants.DATA_SERVICE_OBJECT).getValue();
            if (dataService.getParameter(SwaggerProcessorConstants.SWAGGER_RESOURCE_PATH) != null) {
                String swaggerLocation =
                        (String) dataService.getParameter(SwaggerProcessorConstants.SWAGGER_RESOURCE_PATH).getValue();
                if (swaggerLocation != null && !swaggerLocation.isEmpty()) {
                    try {
                        return SwaggerUtils.fetchSwaggerFromRegistry(requestURI, swaggerLocation);
                    } catch (RegistryException e) {
                        logger.error("Error occurred while fetching swagger from the registry", e);
                    }
                }
            } else {
                List<String> transports = dataService.getExposedTransports();
                if (dataServiceObject instanceof AxisResources) {
                    AxisResourceMap axisResourceMap = ((AxisResources) dataServiceObject).getAxisResourceMap();
                    return SwaggerUtils.createSwaggerFromDefinition(axisResourceMap, dataServiceName, transports,
                            serverConfig, isJSON);
                }
            }
            return null;
        }
        return null;
    }

    /**
     * Create a swagger definition from data-service resource details.
     *
     * @param axisResourceMap AxisResourceMap containing resource details.
     * @param dataServiceName Name of the data service.
     * @param transports      Transports supported from the data-service.
     * @param serverConfig    Server config details.
     * @param isJSON          result format JSON or YAML.
     * @return Swagger definition as string.
     * @throws AxisFault Error occurred while fetching the host address.
     */
    private static String createSwaggerFromDefinition(AxisResourceMap axisResourceMap, String dataServiceName,
                                                      List<String> transports, ServerConfig serverConfig,
                                                      boolean isJSON)
            throws AxisFault {

        Swagger swaggerDoc = new Swagger();
        swaggerDoc.basePath("/" + SwaggerProcessorConstants.SERVICES_PREFIX + "/" + dataServiceName);

        if (transports.contains("https")) {
            swaggerDoc.addScheme(Scheme.HTTPS);
            swaggerDoc.addScheme(Scheme.HTTP);
            swaggerDoc.setHost(serverConfig.getHost("https"));
        } else {
            swaggerDoc.addScheme(Scheme.HTTP);
            swaggerDoc.setHost(serverConfig.getHost("http"));
        }

        Info info = new Info();
        info.title(dataServiceName);
        info.setVersion("1.0");
        info.description("API Definition of dataservice : " + dataServiceName);
        swaggerDoc.setInfo(info);

        swaggerDoc.addConsumes("application/json");
        swaggerDoc.addConsumes("application/xml");

        swaggerDoc.addProduces("application/json");
        swaggerDoc.addProduces("application/xml");

        Map<String, Path> paths = new HashMap<>();

        for (Map.Entry<String, AxisResource> entry : axisResourceMap.getResources().entrySet()) {
            Path path = new Path();
            for (String method : entry.getValue().getMethods()) {
                Operation operation = new Operation();
                List<AxisResourceParameter> parameterList = entry.getValue().getResourceParameterList(method);
                if (!parameterList.isEmpty()) {
                    for (AxisResourceParameter resourceParameter : parameterList) {
                        AxisResourceParameter.ParameterType resourceParameterType =
                                resourceParameter.getParameterType();
                        if (resourceParameterType.equals(AxisResourceParameter.ParameterType.URL_PARAMETER)) {
                            PathParameter pathParameter = new PathParameter();
                            pathParameter.setName(resourceParameter.getParameterName());
                            pathParameter.setType(resourceParameter.getParameterDataType());
                            pathParameter.required(true);
                            operation.addParameter(pathParameter);
                        } else if (resourceParameterType
                                .equals(AxisResourceParameter.ParameterType.QUERY_PARAMETER) && method.equals("GET")) {
                            //  Currently handling query parameter only for GET requests.
                            QueryParameter queryParameter = new QueryParameter();
                            queryParameter.setName(resourceParameter.getParameterName());
                            queryParameter.setType(resourceParameter.getParameterDataType());
                            queryParameter.required(true);
                            operation.addParameter(queryParameter);
                        }
                    }
                }
                // Adding a sample request payload for methods except GET.
                if (!method.equals("GET")) {
                    BodyParameter bodyParameter = new BodyParameter();
                    bodyParameter.description("Sample Payload");
                    bodyParameter.name("payload");
                    bodyParameter.setRequired(false);

                    ModelImpl modelschema = new ModelImpl();
                    modelschema.setType("object");
                    Map<String, Property> propertyMap = new HashMap<>(1);
                    ObjectProperty objectProperty = new ObjectProperty();
                    objectProperty.name("payload");

                    Map<String, Property> payloadProperties = new HashMap<>();
                    for (AxisResourceParameter resourceParameter : parameterList) {
                        switch (resourceParameter.getParameterDataType()) {
                            case SwaggerProcessorConstants.INTEGER:
                                payloadProperties.put(resourceParameter.getParameterName(), new IntegerProperty());
                                break;
                            case SwaggerProcessorConstants.NUMBER:
                                payloadProperties.put(resourceParameter.getParameterName(), new DoubleProperty());
                                break;
                            case SwaggerProcessorConstants.BOOLEAN:
                                payloadProperties.put(resourceParameter.getParameterName(), new BooleanProperty());
                                break;
                            default:
                                payloadProperties.put(resourceParameter.getParameterName(), new StringProperty());
                        }
                    }

                    objectProperty.setProperties(payloadProperties);
                    propertyMap.put("payload", objectProperty);
                    modelschema.setProperties(propertyMap);
                    bodyParameter.setSchema(modelschema);
                    operation.addParameter(bodyParameter);
                }
                Response response = new Response();
                response.description("this is the default response");
                operation.addResponse("default", response);
                switch (method) {
                    case "GET":
                        path.get(operation);
                        break;
                    case "POST":
                        path.post(operation);
                        break;
                    case "DELETE":
                        path.delete(operation);
                        break;
                    case "PUT":
                        path.put(operation);
                        break;
                }
            }
            paths.put(entry.getKey(), path);
        }
        swaggerDoc.setPaths(paths);
        if (isJSON) return Json.pretty(swaggerDoc);
        try {
            return Yaml.pretty().writeValueAsString(swaggerDoc);
        } catch (JsonProcessingException e) {
            logger.error("Error occurred while creating the YAML configuration", e);
            return null;
        }
    }

    /**
     * Util method to fetch a swagger resource from the registry.
     * @param requestURI    request URI.
     * @param resourcePath registry path to the resource.
     * @return  Swagger definition as string or null if error occurred.
     * @throws RegistryException error occured while fetching the resource from registry.
     */
    public static String fetchSwaggerFromRegistry(String requestURI, String resourcePath) throws
            RegistryException {

        boolean isSwaggerInConfReg = true;
        if (resourcePath.startsWith(SwaggerProcessorConstants.CONFIG_REG_PREFIX)) {
            resourcePath = resourcePath.substring(5);
        } else {
            resourcePath = resourcePath.substring(4);
            isSwaggerInConfReg = false;
        }

        RegistryService registryService = RegistryServiceHolder.getInstance().getRegistryService();
        String defString = null;
        String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(requestURI);
        tenantDomain = (tenantDomain != null) ? tenantDomain : MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        Registry registry;
        if (isSwaggerInConfReg) {
            registry = registryService.getConfigSystemRegistry(tenantId);
        } else {
            registry = registryService.getGovernanceSystemRegistry(tenantId);
        }

        Resource resource;
        if (registry.resourceExists(resourcePath)) {
            resource = registry.get(resourcePath);
            if (resource.getContent() != null && (resource.getContent() instanceof byte[]) &&
                    (((byte[]) resource.getContent()).length > 0)) {
                defString = new String((byte[]) resource.getContent(), Charset.defaultCharset());
            }
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Retrieving swagger definition form registry path : " + resourcePath + " with definition: " +
                                defString);
            }
        }
        return defString;
    }
}
