/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediation.commons.rest.api.swagger;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.dispatch.DispatcherHelper;
import org.apache.synapse.rest.dispatch.URLMappingBasedDispatcher;
import org.apache.synapse.rest.version.URLBasedVersionStrategy;
import org.wso2.carbon.mediation.commons.internal.MediationCommonsComponent;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Generalized object structure for Swagger definitions of APIs. This structure contains set of Maps which compatible
 * with both JSON and YAML formats.
 */
public class GenericApiObjectDefinition {
    private static final Log log = LogFactory.getLog(GenericApiObjectDefinition.class);
    private API api;

    public GenericApiObjectDefinition(API api) {
        this.api = api;
    }

    /**
     * Provides a map which represents the structure of swagger definition.
     *
     * @return Map containing information for swagger definition
     */
    public Map<String, Object> getDefinitionMap() throws AxisFault {
        Map<String, Object> apiMap = new LinkedHashMap<>();
        //Swagger version
        apiMap.put(SwaggerConstants.SWAGGER, SwaggerConstants.SWAGGER_VERSION);

        //Info section
        apiMap.put(SwaggerConstants.INFO, getInfoMap());

        //Host is mandatory for TryIt
        apiMap.put(SwaggerConstants.HOST, getHost());

        //Schemes
        apiMap.put(SwaggerConstants.SCHEMES, getSchemes());

        //Base path
        if (api.getVersionStrategy() instanceof URLBasedVersionStrategy) {
            apiMap.put(SwaggerConstants.BASE_PATH, api.getContext() + "/" + api.getVersionStrategy().getVersion());
        } else {
            apiMap.put(SwaggerConstants.BASE_PATH, api.getContext());
        }

        //Default consume and produce MIME Types (Adding default once since API definition does not contain details)
        apiMap.put(SwaggerConstants.CONSUMES, SwaggerConstants.DEFAULT_CONSUMES);
        apiMap.put(SwaggerConstants.PRODUCES, SwaggerConstants.DEFAULT_PRODUCES);

        //Paths
        if (getPathMap() != null && !getPathMap().isEmpty()) {
            apiMap.put(SwaggerConstants.PATHS, getPathMap());
        }
        return apiMap;
    }

    /**
     * Function to retrieve host for the swagger definition
     * Host is extracted from following sources
     *  1. Host configured in API definition
     *  2. WSDLEPRPrefix configured in axis2.xml under http or https transport listeners
     *  3. "hostname" parameter in axis2.xml (combined with http port configured for transport listener)
     *  4. Server (machine) host (combined with http port configured for transport listener)
     *
     * @return return host
     */
    private String getHost() throws AxisFault {

        if (api.getHost() != null) {
            return api.getHost();
        } else {
            AxisConfiguration axisConfiguration =
                    MediationCommonsComponent.getContextService().getServerConfigContext().getAxisConfiguration();

            //Retrieve WSDLPrefix to retrieve host
            //If transport is limited to https, https host will generate. Otherwise http will be generated
            TransportInDescription transportIn = axisConfiguration.getTransportIn(
                    api.getProtocol() == RESTConstants.PROTOCOL_HTTP_ONLY ? "http" : "https");

            if (transportIn != null) {
                // Give priority to WSDLEPRPrefix
                if (transportIn.getParameter(SwaggerConstants.WSDL_EPR_PREFIX) != null) {
                    String wsdlPrefixParam = (String) transportIn.getParameter(SwaggerConstants.WSDL_EPR_PREFIX).getValue();
                    if (!wsdlPrefixParam.isEmpty()) {
                        //WSDLEPRPrefix available
                        try {
                            URI hostUri = new URI(wsdlPrefixParam);
                            //Resolve port
                            try {
                                String protocol = transportIn.getName();

                                if (("https".equals(protocol) && hostUri.getPort() == 443) ||
                                        ("http".equals(protocol) && hostUri.getPort() == 80)) {
                                    return hostUri.getHost();
                                }
                            } catch (NumberFormatException e) {
                                throw new AxisFault("Error occurred while parsing the port", e);
                            }

                            return hostUri.getHost() + ":" + hostUri.getPort();
                        } catch (URISyntaxException e) {
                            log.error("WSDLEPRPrefix is not a valid URI", e);
                        }
                    } else {
                        log.error("\"WSDLEPRPrefix\" is empty. Please provide relevant URI or comment out parameter");
                    }
                }

                String portStr = (String) transportIn.getParameter("port").getValue();
                String hostname = "localhost";

                //Resolve hostname
                if (axisConfiguration.getParameter("hostname") != null) {
                    hostname = (String) axisConfiguration.getParameter("hostname").getValue();
                } else {
                    try {
                        hostname = NetworkUtils.getLocalHostname();
                    } catch (SocketException e) {
                        log.warn("SocketException occurred when trying to obtain IP address of local machine");
                    }
                }
                return hostname + ':' + portStr;
            }

            throw new AxisFault("http/https transport listeners are required in axis2.xml");
        }
    }

    /**
     * Provides structure for the "responses" element in swagger definition.
     *
     * @return Map containing information for responses element
     */
    private Map<String, Object> getResponsesMap() {
        Map<String, Object> responsesMap = new LinkedHashMap<>();
        Map<String, Object> responseDetailsMap = new LinkedHashMap<>();
        //Use a default response since these information is not available in synapse configuration for APIs
        responseDetailsMap.put(SwaggerConstants.DESCRIPTION, SwaggerConstants.DEFAULT_RESPONSE);
        responsesMap.put(SwaggerConstants.DEFAULT_VALUE, responseDetailsMap);
        if(log.isDebugEnabled()){
            log.debug("Response map created with size " + responsesMap.size());
        }
        return responsesMap;
    }

    /**
     * Provides structure for the "info" element in swagger definition.
     *
     * @return Map containing information for info element
     */
    private Map<String, Object> getInfoMap() {
        Map<String, Object> infoMap = new LinkedHashMap<>();
        infoMap.put(SwaggerConstants.DESCRIPTION, (SwaggerConstants.API_DESC_PREFIX + api.getAPIName()));
        infoMap.put(SwaggerConstants.TITLE, api.getName());
        infoMap.put(SwaggerConstants.VERSION, (api.getVersion() != null && !api.getVersion().equals(""))
                ? api.getVersion() : SwaggerConstants.DEFAULT_API_VERSION);
        if(log.isDebugEnabled()){
            log.debug("Info map created with size " + infoMap.size());
        }
        return infoMap;
    }

    /**
     * Provides structure for the "paths" element in swagger definition.
     *
     * @return Map containing information for paths element
     */
    private Map<String, Object> getPathMap() {
        Map<String, Object> pathsMap = new LinkedHashMap<>();
        for (Resource resource : api.getResources()) {
            Map<String, Object> methodMap = new LinkedHashMap<>();
            DispatcherHelper resourceDispatcherHelper = resource.getDispatcherHelper();

            for (String method : resource.getMethods()) {
                if (method != null) {
                    Map<String, Object> methodInfoMap = new LinkedHashMap<>();
                    methodInfoMap.put(SwaggerConstants.RESPONSES, getResponsesMap());
                    if (resourceDispatcherHelper != null) {
                        Object[] parameters = getResourceParameters(resource, method);
                        if (parameters.length > 0) {
                            methodInfoMap.put(SwaggerConstants.PARAMETERS, parameters);
                        }
                    }
                    methodMap.put(method.toLowerCase(), methodInfoMap);
                }
            }
            pathsMap.put(getPathFromUrl(resourceDispatcherHelper == null ? SwaggerConstants.PATH_SEPARATOR
                    : resourceDispatcherHelper.getString()), methodMap);
        }
        if(log.isDebugEnabled()){
            log.debug("Paths map created with size " + pathsMap.size());
        }
        return pathsMap;
    }

    /**
     * Provides list of schemas support by the API.
     *
     * @return Array of String containing schemas list
     */
    private String[] getSchemes() {
        String[] protocols;
        switch (api.getProtocol()) {
            case SwaggerConstants.PROTOCOL_HTTP_ONLY:
                protocols = new String[]{SwaggerConstants.PROTOCOL_HTTP};
                break;
            case SwaggerConstants.PROTOCOL_HTTPS_ONLY:
                protocols = new String[]{SwaggerConstants.PROTOCOL_HTTPS};
                break;
            default:
                protocols = new String[]{SwaggerConstants.PROTOCOL_HTTPS, SwaggerConstants.PROTOCOL_HTTP};
                break;
        }
        return protocols;
    }

    /**
     * Generate resource parameters for the given resource.
     *
     * @param resource instance of Resource in the API
     * @return Array of parameter objects supported by the API
     */
    private Object[] getResourceParameters(Resource resource) {
        ArrayList<Map<String, Object>> parameterList = new ArrayList<>();
        String uri = resource.getDispatcherHelper().getString();

        if (resource.getDispatcherHelper() instanceof URLMappingBasedDispatcher) {
            generateParameterList(parameterList, uri, false);
        } else {
            generateParameterList(parameterList, uri, true);
        }
        if(log.isDebugEnabled()){
            log.debug("Parameters processed for the URI + " + uri + " size " + parameterList.size());
        }
        return parameterList.toArray();
    }

    /**
     * Generate resource parameters for the given resource for given method.
     *
     * @param resource instance of Resource in the API
     * @return Array of parameter objects supported by the API
     */
    private Object[] getResourceParameters(Resource resource, String method) {
        ArrayList<Map<String, Object>> parameterList = new ArrayList<>();

        String uri = resource.getDispatcherHelper().getString();

        if (resource.getDispatcherHelper() instanceof URLMappingBasedDispatcher) {
            generateParameterList(parameterList, uri, false);
        } else {
            generateParameterList(parameterList, uri, true);
        }
        if(log.isDebugEnabled()){
            log.debug("Parameters processed for the URI + " + uri + " size " + parameterList.size());
        }

        generateBodyParameter(parameterList, resource, method);

        return parameterList.toArray();
    }


    /**
     * Generate URI and Path parameters for the given URI.
     *
     * @param parameterList     List of maps to be populated with parameters
     * @param uriString         URI string to be used to extract parameters
     * @param generateBothTypes Indicates whether to consider both query and uri parameters. True if both to be
     *                          considered.
     */
    private void generateParameterList(ArrayList<Map<String, Object>> parameterList, String uriString, boolean
            generateBothTypes) {
        if (uriString == null) {
            return;
        }
        if (generateBothTypes) {
            String[] params = getQueryStringFromUrl(uriString).split("&");
            for (String parameter : params) {
                if (parameter != null) {
                    int pos = parameter.indexOf('=');
                    if (pos > 0) {
                        parameterList.add(getParametersMap(parameter.substring(0, pos), SwaggerConstants.PARAMETER_IN_QUERY));
                    }
                }
            }
        }
        Matcher matcher = SwaggerConstants.PATH_PARAMETER_PATTERN.matcher(getPathFromUrl(uriString));
        while (matcher.find()) {
            parameterList.add(getParametersMap(matcher.group(1), SwaggerConstants.PARAMETER_IN_PATH));
        }
    }

    private void generateBodyParameter(ArrayList<Map<String, Object>> parameterList, Resource resource, String method) {
        if (method.equalsIgnoreCase(SwaggerConstants.OPERATION_HTTP_POST) ||
                method.equalsIgnoreCase(SwaggerConstants.OPERATION_HTTP_PUT) ||
                method.equalsIgnoreCase(SwaggerConstants.OPERATION_HTTP_PATCH)) {
            parameterList.add(getParametersMap("payload", SwaggerConstants.PARAMETER_IN_BODY, "Sample Payload", false));
        }
    }

    /**
     * Create map of parameters from give name and type. Default values are used for other fields since those are not
     * provided by the synapse configuration of the API.
     *
     * @param parameterName Name of the parameter
     * @param parameterType Type of the parameter
     * @return Map containing parameter properties
     */
    private Map<String, Object> getParametersMap(String parameterName, String parameterType) {
        return getParametersMap(parameterName, parameterType, parameterName, true);
    }

    private Map<String, Object> getParametersMap(String parameterName, String parameterType, String description,
                                                 boolean required) {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put(SwaggerConstants.PARAMETER_DESCRIPTION, description);
        parameterMap.put(SwaggerConstants.PARAMETER_IN, parameterType);
        parameterMap.put(SwaggerConstants.PARAMETER_NAME, parameterName);
        parameterMap.put(SwaggerConstants.PARAMETER_REQUIRED, required);

        if (parameterType.equals(SwaggerConstants.PARAMETER_IN_BODY)) {
            /*
             * If the parameter in: body, then it should contain schema describing the body structure. Since synapse
             * configuration does not contain such information DEFAULT schema will be generated for default payload:
             *          {
             *              "payload" : "string"
             *          }
             */
            Map<String, Object> schemaMap = new LinkedHashMap<>();
            Map<String, Object> schemaPropertiesMap = new LinkedHashMap<>();
            Map<String, Object> schemaPropertiesTypeMap = new LinkedHashMap<>();

            schemaPropertiesTypeMap.put("type", "string");
            schemaPropertiesMap.put("payload", schemaPropertiesTypeMap);
            schemaMap.put(SwaggerConstants.PARAMETER_TYPE, "object");
            schemaMap.put(SwaggerConstants.PARAMETER_PROPERTIES, schemaPropertiesMap);

            parameterMap.put(SwaggerConstants.PARAMETER_BODY_SCHEMA, schemaMap);

        } else  {
            /* Type will be "string" for all parameters since synapse configuration does
            not contain such information for APIs. */
            parameterMap.put(SwaggerConstants.PARAMETER_TYPE, SwaggerConstants.PARAMETER_TYPE_STRING);
        }

        return parameterMap;
    }

    /**
     * Get the path portion from the URI.
     *
     * @param uri String URI to be analysed
     * @return String containing the path portion of the URI
     */
    private String getPathFromUrl(String uri) {
        int pos = uri.indexOf("?");
        if (pos > 0) {
            return uri.substring(0, pos);
        }
        return uri;
    }

    /**
     * Get query parameter portion from the URI.
     *
     * @param uri String URI to be analysed
     * @return String containing the URI parameter portion of the URI
     */
    private String getQueryStringFromUrl(String uri) {
        int pos = uri.indexOf("?");
        if (pos > 0) {
            return uri.substring(pos + 1);
        }
        return "";
    }
}
