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

import com.google.gson.JsonParser;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.rest.API;
import org.wso2.carbon.core.transports.CarbonHttpRequest;
import org.wso2.carbon.core.transports.CarbonHttpResponse;
import org.wso2.carbon.core.transports.HttpGetRequestProcessor;
import org.wso2.carbon.integrator.core.json.utils.GSONUtils;
import org.wso2.carbon.mediation.commons.rest.api.swagger.GenericApiObjectDefinition;
import org.wso2.carbon.mediation.commons.rest.api.swagger.ServerConfig;
import org.wso2.carbon.mediation.commons.rest.api.swagger.SwaggerConstants;
import org.wso2.carbon.mediation.transport.handlers.DataHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.yaml.snakeyaml.Yaml;

/**
 * Provides Swagger definition for the API in YAML format.
 */
public class SwaggerYamlProcessor extends SwaggerGenerator implements HttpGetRequestProcessor {

    private static final Log log = LogFactory.getLog(SwaggerYamlProcessor.class);

    /**
     * Process incoming GET request and update the response with the swagger definition for the requested API.
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
            //Retrieve from registry
            String responseString;
            try {
                Yaml yamlDefinition = new Yaml();
                String defFromRegistry = retrieveFromRegistry(api, request);
                if (defFromRegistry != null) {
                    JsonParser jsonParser = new JsonParser();
                    responseString =
                            yamlDefinition.dumpAsMap(GSONUtils.gsonJsonObjectToMap(jsonParser.parse(defFromRegistry)));
                } else {
                    ServerConfig serverConfig = new CarbonServerConfig();
                    responseString =
                            yamlDefinition.dumpAsMap(new GenericApiObjectDefinition(api, serverConfig).getDefinitionMap());
                }
            } catch (RegistryException e) {
                throw new AxisFault("Error occurred while retrieving swagger definition from registry", e);
            }

            updateResponse(response, responseString, SwaggerConstants.CONTENT_TYPE_YAML);
        }
    }

    /**
     * Function to convert from GSON object model to generic map model.
     *
     * @param jsonElement gson object to transform
     * @return
     */
    /*private Map jsonObjectToMap(JsonElement jsonElement) {
        Map<String, Object> gsonMap = new LinkedHashMap<>();

        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();

            for (Map.Entry<String, JsonElement> entry : entrySet) {
                if (entry.getValue().isJsonPrimitive()) {
                    JsonPrimitive jsonPrimitive = entry.getValue().getAsJsonPrimitive();

                    if (jsonPrimitive.isString()) {
                        gsonMap.put(entry.getKey(), jsonPrimitive.getAsString());
                    } else if (jsonPrimitive.isBoolean()) {
                        gsonMap.put(entry.getKey(), jsonPrimitive.getAsBoolean());
                    } else if (jsonPrimitive.isNumber()) {
                        gsonMap.put(entry.getKey(), jsonPrimitive.getAsNumber());
                    } else {
                        //unknown type
                        log.warn("Unknown JsonPrimitive type found : " + jsonPrimitive.toString());
                    }
                } else if (entry.getValue().isJsonObject()) {
                    gsonMap.put(entry.getKey(), jsonObjectToMap(entry.getValue()));

                } else if (entry.getValue().isJsonArray()) {
                    gsonMap.put(entry.getKey(), jsonArrayToObjectArray(entry.getValue().getAsJsonArray()));

                } else {
                    // remaining JsonNull type
                    gsonMap.put(entry.getKey(), null);

                }
            }
        } else {
            //Not a JsonObject hence return empty map
            log.error("Provided gson model does not represent json object");
        }

        return gsonMap;
    }*/

    /**
     * Function to convert GSON array model to generic array model.
     *
     * @param jsonElement gson array to transform
     * @return
     */
    /*private Object[] jsonArrayToObjectArray(JsonElement jsonElement) {
        ArrayList<Object> jsonArrayList = new ArrayList<>();

        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement arrayElement = jsonArray.get(i);
                if (arrayElement.isJsonPrimitive()) {
                    JsonPrimitive jsonPrimitive = arrayElement.getAsJsonPrimitive();

                    if (jsonPrimitive.isString()) {
                        jsonArrayList.add(jsonPrimitive.getAsString());
                    } else if (jsonPrimitive.isBoolean()) {
                        jsonArrayList.add(jsonPrimitive.getAsBoolean());
                    } else if (jsonPrimitive.isNumber()) {
                        jsonArrayList.add(jsonPrimitive.getAsNumber());
                    } else {
                        //unknown type
                        log.warn("Unknown JsonPrimitive type found : " + jsonPrimitive.toString());
                    }
                } else if (arrayElement.isJsonObject()) {
                    jsonArrayList.add(jsonObjectToMap(arrayElement));

                } else if (arrayElement.isJsonArray()) {
                    jsonArrayList.add(jsonArrayToObjectArray(arrayElement.getAsJsonArray()));
                } else {
                    // remaining JsonNull
                    jsonArrayList.add(null);
                }
            }
        } else {
            //Not a JsonObject hence return empty array
            log.error("Provided gson model does not represent json array");
        }

        return jsonArrayList.toArray();
    }*/

}
