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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SequenceType;
import org.apache.synapse.config.xml.rest.APIFactory;
import org.apache.synapse.config.xml.rest.APISerializer;
import org.apache.synapse.config.xml.rest.VersionStrategyFactory;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.builtin.CommentMediator;
import org.apache.synapse.mediators.builtin.LoopBackMediator;
import org.apache.synapse.mediators.builtin.PropertyMediator;
import org.apache.synapse.mediators.builtin.RespondMediator;
import org.apache.synapse.mediators.transform.PayloadFactoryMediator;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.dispatch.URITemplateHelper;
import org.apache.synapse.rest.dispatch.URLMappingHelper;
import org.apache.synapse.rest.version.ContextVersionStrategy;
import org.apache.synapse.rest.version.DefaultStrategy;
import org.apache.synapse.rest.version.URLBasedVersionStrategy;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import javax.xml.stream.XMLStreamException;

/**
 * This class will generate synapse Rest API configuration skeleton from swagger definition
 */
public class APIGenerator {

    private JsonObject swaggerJson;
    private static Log log = LogFactory.getLog(APIGenerator.class);

    public APIGenerator(JsonObject swaggerJson) {
        this.swaggerJson = swaggerJson;
    }

    /**
     * Generate API from provided swagger definition
     *
     * @return Generated API
     * @throws APIGenException
     */
    public API generateSynapseAPI() throws APIGenException {

        if (swaggerJson.get(SwaggerConstants.BASE_PATH) == null ||
                swaggerJson.get(SwaggerConstants.BASE_PATH).getAsString().isEmpty()) {
            throw new APIGenException("The \"basePath\" of the swagger definition is mandatory for API generation");
        }
        String apiContext = swaggerJson.get(SwaggerConstants.BASE_PATH).getAsString();
        //cleanup context : remove ending '/'
        if (apiContext.lastIndexOf('/') == (apiContext.length() - 1)) {
            apiContext = apiContext.substring(0, apiContext.length() - 1);
        }

        if (swaggerJson.get(SwaggerConstants.INFO) == null) {
            throw new APIGenException("The \"info\" section of the swagger definition is mandatory for API generation");
        }
        JsonObject swaggerInfo = swaggerJson.getAsJsonObject(SwaggerConstants.INFO);
        if (swaggerInfo.get(SwaggerConstants.TITLE) == null ||
                swaggerInfo.get(SwaggerConstants.TITLE).getAsString().isEmpty()) {
            throw new APIGenException("The title of the swagger definition is mandatory for API generation");
        }

        String apiName = swaggerInfo.get(SwaggerConstants.TITLE).getAsString();

        // Extract version information
        String versionType = VersionStrategyFactory.TYPE_NULL;
        String version = "";
        JsonElement swaggerVersionElement = swaggerInfo.get(SwaggerConstants.VERSION);
        if (swaggerVersionElement != null && swaggerVersionElement.isJsonPrimitive() &&
                swaggerVersionElement.getAsJsonPrimitive().isString()) {
            version = swaggerVersionElement.getAsString();
            if (apiContext.endsWith(version)) {
                // If the base path ends with the version, then it will be considered as version-type=url
                versionType = VersionStrategyFactory.TYPE_URL;
                //cleanup api context path : remove version from base path
                apiContext = apiContext.substring(0, apiContext.length() - version.length() - 1);
            } else {
                // otherwise context based version strategy
                versionType = VersionStrategyFactory.TYPE_CONTEXT;
            }
        }

        // Create API
        API genAPI = new API(apiName, apiContext);

        //Add version strategy
        if (versionType.equals(VersionStrategyFactory.TYPE_URL)) {
            // If the base path ends with the version, then it will be considered as version-type=url
            genAPI.setVersionStrategy(new URLBasedVersionStrategy(genAPI, version, ""));
        } else if (versionType.equals(VersionStrategyFactory.TYPE_CONTEXT)) {
            // otherwise context based version strategy
            genAPI.setVersionStrategy(new ContextVersionStrategy(genAPI, version, ""));
        } else {
            genAPI.setVersionStrategy(new DefaultStrategy(genAPI));
        }

        if (swaggerJson.get(SwaggerConstants.PATHS) != null) {
            JsonObject pathsObj = swaggerJson.getAsJsonObject(SwaggerConstants.PATHS);
            for (Map.Entry<String, JsonElement> pathEntry : pathsObj.entrySet()) {
                if (pathEntry.getValue() instanceof JsonObject) {
                    Resource resource = createResource(pathEntry.getKey(), pathEntry.getValue().getAsJsonObject());
                    genAPI.addResource(resource);
                }
            }
        }

        OMElement apiElement = APISerializer.serializeAPI(genAPI);
        if (log.isDebugEnabled()) {
            log.info("API generation completed : " + genAPI.getName() + " API: " + apiElement.toString());
        }
        return genAPI;
    }

    /**
     * Function to generate updated API extracting the implementation from an existing API
     *
     * @param existingAPI the existing API to extract resource implementations
     * @return generated API
     */
    public API generateUpdatedSynapseAPI(API existingAPI) throws APIGenException {
        if (existingAPI == null) {
            throw new APIGenException("Provided existing API is null");
        }
        API genAPI = generateSynapseAPI();
        // clone existing API
        API clonedAPI;
        try {
            clonedAPI = APIFactory.createAPI(AXIOMUtil.stringToOM(APISerializer.serializeAPI(existingAPI).toString()));
        } catch (XMLStreamException e) {
            throw new APIGenException("Error occurred while cloning the existing API", e);
        }
        // handle if the existing API use default version strategy (when generating default version strategy won't
        // get selected)
        if (existingAPI.getVersionStrategy() instanceof DefaultStrategy) {
            genAPI.setVersionStrategy(new DefaultStrategy(genAPI));
        }
        updateImplChanges(genAPI, clonedAPI);
        return genAPI;
    }

    /**
     * Function to create resource from swagger definition
     *
     * @param path
     * @param resourceObj
     * @return
     */
    private Resource createResource(String path, JsonObject resourceObj) throws APIGenException {

        if (log.isDebugEnabled()) {
            log.info("Generating resource for path : " + path);
        }
        Resource resource = new Resource();
        for (Map.Entry<String, JsonElement> methodEntry : resourceObj.entrySet()) {
            resource.addMethod(methodEntry.getKey().toUpperCase());
        }

        // Identify URL Mapping and template and create relevant helper
        Matcher matcher = SwaggerConstants.PATH_PARAMETER_PATTERN.matcher(path);
        ArrayList<String> pathParamList = new ArrayList<>();
        while (matcher.find()) {
            pathParamList.add(matcher.group(1));
        }
        if (pathParamList.isEmpty()) {
            // if the path is '/' then it should have none URL style
            if (!"/".equals(path)) {
                resource.setDispatcherHelper(new URLMappingHelper(path));
            }
        } else {
            resource.setDispatcherHelper(new URITemplateHelper(path));
        }

        resource.setInSequence(APIGenerator.getDefaultInSequence(pathParamList));
        resource.setOutSequence(APIGenerator.getDefaultOutSequence());

        return resource;
    }

    private void updateImplChanges(API newAPI, API currentAPI) {

        //Migrate version strategy
        newAPI.setVersionStrategy(currentAPI.getVersionStrategy());

        // Map of resources against resource url mapping or template
        HashMap<String, ArrayList<Resource>> currentResourceList = new HashMap<>();
        // Extract all existing resources and categorize according URL mapping or template
        for (Resource resource : currentAPI.getResources()) {

            String resourceMapping = resource.getDispatcherHelper() != null ?
                                                        resource.getDispatcherHelper().getString() : "/";
            ArrayList<Resource> resourceList;
            if (currentResourceList.get(resourceMapping) != null) {
                resourceList = currentResourceList.get(resourceMapping);
            } else {
                resourceList = new ArrayList<>();
            }
            resourceList.add(resource);
            currentResourceList.put(resourceMapping, resourceList);
        }

        for (Resource resource : newAPI.getResources()) {

            String resourceMapping = resource.getDispatcherHelper() != null ?
                                                        resource.getDispatcherHelper().getString() : "/";
            ArrayList<Resource> existingResources = currentResourceList.get(resourceMapping);

            if (existingResources != null) {
                // TODO handle multiple resources with same URL mapping or template with different methods
                compareAndUpdateResource(existingResources.get(0), resource);
            }
        }
    }

    /**
     * Function to compare and update resource implementation
     *
     * @param source source resource to extract implementation
     * @param target resource to update
     */
    private void compareAndUpdateResource(Resource source, Resource target) {
        if (source.getInSequenceKey() != null) {
            target.setInSequenceKey(source.getInSequenceKey());
        } else if (source.getInSequence() != null) {
            target.setInSequence(source.getInSequence());
        }

        if (source.getOutSequenceKey() != null) {
            target.setOutSequenceKey(source.getOutSequenceKey());
        } else if (source.getOutSequence() != null) {
            target.setOutSequence(source.getOutSequence());
        }

        if (source.getFaultSequenceKey() != null) {
            target.setFaultSequenceKey(source.getFaultSequenceKey());
        } else if (source.getFaultSequence() != null) {
            target.setFaultSequence(source.getFaultSequence());
        }
    }

    /**
     * Function to create default in sequence
     *
     * @return template API in-sequence
     */
    private static SequenceMediator getDefaultInSequence(List<String> pathParams) throws APIGenException {
        SequenceMediator defaultInSeq = new SequenceMediator();
        defaultInSeq.setSequenceType(SequenceType.ANON);

        CommentMediator generatedComment = new CommentMediator();
        generatedComment.setCommentText("This is generated API skeleton.");
        defaultInSeq.addChild(generatedComment);

        if (pathParams != null && pathParams.size() > 0) {
            // Create populate properties reading path parameters
            for (String param : pathParams) {
                try {
                    PropertyMediator propertyMediator = new PropertyMediator();
                    propertyMediator.setExpression(new SynapseXPath("get-property('uri.var." + param + "')"));
                    propertyMediator.setName(param);
                    defaultInSeq.addChild(propertyMediator);
                } catch (JaxenException e) {
                    throw new APIGenException("Error occurred while creating property mediator for extracting path " +
                            "params", e);
                }
            }
        }

        CommentMediator logicGoesHereComment = new CommentMediator();
        logicGoesHereComment.setCommentText("Business Logic Goes Here");
        defaultInSeq.addChild(logicGoesHereComment);

        PayloadFactoryMediator defaultPayload = new PayloadFactoryMediator();
        defaultPayload.setType("json");
        defaultPayload.setFormat("{\"Response\" : \"Sample Response\"}");
        defaultInSeq.addChild(defaultPayload);

        defaultInSeq.addChild(new LoopBackMediator());
        return defaultInSeq;
    }

    /**
     * Function to create default out sequence
     *
     * @return template API in-sequence
     */
    private static SequenceMediator getDefaultOutSequence() {
        SequenceMediator defaultOutSeq = new SequenceMediator();
        defaultOutSeq.setSequenceType(SequenceType.ANON);
        defaultOutSeq.addChild(new RespondMediator());
        return defaultOutSeq;
    }
}
