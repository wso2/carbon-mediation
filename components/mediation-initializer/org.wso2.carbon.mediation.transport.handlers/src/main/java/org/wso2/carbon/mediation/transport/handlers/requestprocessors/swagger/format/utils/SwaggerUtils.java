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

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Util class with methods to generate swagger definition and fetch them from the registry.
 */
public final class SwaggerUtils {

    static final String PROTOCOL_HTTP = "http";
    static final String PROTOCOL_HTTPS = "https";
    public static final String DATA_TYPE_INTEGER = "integer";
    public static final String DATA_TYPE_NUMBER = "number";
    public static final String DATA_TYPE_BOOLEAN = "boolean";

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

        OpenAPI openAPI = new OpenAPI();

        Info info = new Info();
        info.title(dataServiceName);
        info.setVersion("1.0");
        info.description("API Definition of dataservice : " + dataServiceName);
        openAPI.setInfo(info);

        addServersSection(dataServiceName, transports, serverConfig, openAPI);

        Paths paths = new Paths();

        for (Map.Entry<String, AxisResource> entry : axisResourceMap.getResources().entrySet()) {
            PathItem pathItem = new PathItem();
            for (String method : entry.getValue().getMethods()) {
                Operation operation = new Operation();
                List<AxisResourceParameter> parameterList = entry.getValue().getResourceParameterList(method);
                addPathAndQueryParameters(method, operation, parameterList);
                // Adding a sample request payload for methods except GET and DELETE ( OAS3 onwards )
                addSampleRequestBody(method, operation, parameterList);
                addDefaultResponseAndPathItem(pathItem, method, operation);
            }
            // adding the resource. all the paths should starts with "/"
            paths.put(entry.getKey().startsWith("/") ? entry.getKey() : "/" + entry.getKey(), pathItem);
        }
        openAPI.setPaths(paths);
        try {
            if (isJSON) return Json.mapper().writeValueAsString(openAPI);
            return Yaml.mapper().writeValueAsString(openAPI);
        } catch (JsonProcessingException e) {
            logger.error("Error occurred while creating the YAML configuration", e);
            return null;
        }
    }

    /**
     * Add request body schema for methods except GET and DELETE.
     *
     * @param method        HTTP method.
     * @param operation     Operation object.
     * @param parameterList Body param list.
     */
    private static void addSampleRequestBody(String method, Operation operation,
                                             List<AxisResourceParameter> parameterList) {

        if (!method.equals("GET") && !method.equals("DELETE")) {
            RequestBody requestBody = new RequestBody();
            requestBody.description("Sample Payload");
            requestBody.setRequired(false);

            MediaType mediaType = new MediaType();
            Schema bodySchema = new Schema();
            bodySchema.setType("object");

            Map<String, Schema> inputProperties = new HashMap<>();
            ObjectSchema objectSchema = new ObjectSchema();
            Map<String, Schema> payloadProperties = new HashMap<>();
            for (AxisResourceParameter resourceParameter : parameterList) {
                switch (resourceParameter.getParameterDataType()) {
                    case SwaggerProcessorConstants.INTEGER:
                        payloadProperties.put(resourceParameter.getParameterName(), new IntegerSchema());
                        break;
                    case SwaggerProcessorConstants.NUMBER:
                        payloadProperties.put(resourceParameter.getParameterName(), new NumberSchema());
                        break;
                    case SwaggerProcessorConstants.BOOLEAN:
                        payloadProperties.put(resourceParameter.getParameterName(), new BooleanSchema());
                        break;
                    default:
                        payloadProperties.put(resourceParameter.getParameterName(), new StringSchema());
                }
            }
            objectSchema.setProperties(payloadProperties);
            bodySchema.setProperties(inputProperties);
            inputProperties.put("payload", objectSchema);
            mediaType.setSchema(bodySchema);
            Content content = new Content();
            content.addMediaType("application/json", mediaType);
            requestBody.setContent(content);
            operation.setRequestBody(requestBody);
        }
    }

    /**
     * Add path parameters and query parameters to the operation.
     *
     * @param method        HTTP method.
     * @param operation     Operation object.
     * @param parameterList Path and Query parameter list.
     */
    private static void addPathAndQueryParameters(String method, Operation operation,
                                                  List<AxisResourceParameter> parameterList) {

        if (!parameterList.isEmpty()) {
            for (AxisResourceParameter resourceParameter : parameterList) {
                AxisResourceParameter.ParameterType resourceParameterType =
                        resourceParameter.getParameterType();
                if (resourceParameterType.equals(AxisResourceParameter.ParameterType.URL_PARAMETER)) {
                    PathParameter pathParameter = new PathParameter();
                    pathParameter.setName(resourceParameter.getParameterName());
                    switch (resourceParameter.getParameterDataType()) {
                        case DATA_TYPE_INTEGER:
                            pathParameter.setSchema(new IntegerSchema());
                            break;
                        case DATA_TYPE_NUMBER:
                            pathParameter.setSchema(new NumberSchema());
                            break;
                        case DATA_TYPE_BOOLEAN:
                            pathParameter.setSchema(new BooleanSchema());
                            break;
                        default:
                            pathParameter.setSchema(new StringSchema());
                            break;
                    }
                    pathParameter.required(true);
                    operation.addParametersItem(pathParameter);
                } else if (resourceParameterType
                        .equals(AxisResourceParameter.ParameterType.QUERY_PARAMETER) && method.equals("GET")) {
                    //  Currently handling query parameter only for GET requests.
                    QueryParameter queryParameter = new QueryParameter();
                    queryParameter.setName(resourceParameter.getParameterName());
                    switch (resourceParameter.getParameterDataType()) {
                        case DATA_TYPE_INTEGER:
                            queryParameter.setSchema(new IntegerSchema());
                            break;
                        case DATA_TYPE_NUMBER:
                            queryParameter.setSchema(new NumberSchema());
                            break;
                        case DATA_TYPE_BOOLEAN:
                            queryParameter.setSchema(new BooleanSchema());
                            break;
                        default:
                            queryParameter.setSchema(new StringSchema());
                            break;
                    }
                    queryParameter.required(true);
                    operation.addParametersItem(queryParameter);
                }
            }
        }
    }

    /**
     * Add the default response ( since we cannot define it ) and pathItems to path map
     * @param pathItem  PathItem object.
     * @param method    HTTP method.
     * @param operation Operation object.
     */
    private static void addDefaultResponseAndPathItem(PathItem pathItem, String method, Operation operation) {

        ApiResponses apiResponses = new ApiResponses();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Default response");
        apiResponses.addApiResponse("default", apiResponse);
        operation.setResponses(apiResponses);

        switch (method) {
            case "GET":
                pathItem.setGet(operation);
                break;
            case "POST":
                pathItem.setPost(operation);
                break;
            case "DELETE":
                pathItem.setDelete(operation);
                break;
            case "PUT":
                pathItem.setPut(operation);
                break;
        }
    }

    /**
     * Add servers section to the OpenApi definition.
     *
     * @param dataServiceName Name of the dataservice.
     * @param transports      List of supported transports.
     * @param serverConfig    Server config object.
     * @param openAPI         OpenApi object.
     * @throws AxisFault Error occurred while getting host details.
     */
    private static void addServersSection(String dataServiceName, List<String> transports, ServerConfig serverConfig,
                                          OpenAPI openAPI) throws AxisFault {

        String scheme;
        String host;
        if (transports.contains(PROTOCOL_HTTPS)) {
            scheme = PROTOCOL_HTTPS;
            host = serverConfig.getHost(PROTOCOL_HTTPS);
        } else {
            scheme = PROTOCOL_HTTP;
            host = serverConfig.getHost(PROTOCOL_HTTP);
        }
        String basePath = "/" + SwaggerProcessorConstants.SERVICES_PREFIX + "/" + dataServiceName;

        Server server = new Server();
        server.setUrl(scheme + "://" + host + basePath);
        openAPI.setServers(Arrays.asList(server));
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
