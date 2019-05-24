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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.inbound.endpoint.internal.http.api;

import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTUtils;
import org.apache.synapse.rest.dispatch.DispatcherHelper;
import org.apache.synapse.rest.dispatch.URITemplateHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code InternalAPIDispatcher} takes care of dispatching messages received over the internal inbound endpoint into
 * relevant {@link InternalAPI}.
 */
public class InternalAPIDispatcher {

    private static Log log = LogFactory.getLog(InternalAPIDispatcher.class);

    private List<InternalAPI> internalApis;

    public InternalAPIDispatcher(List<InternalAPI> internalApis) {
       this.internalApis = internalApis;
    }

    /**
     * Dispatches the message into relevant internal API.
     *
     * @param synCtx the Synapse Message Context
     * @return whether to continue with post dispatching actions
     */
    public boolean dispatch(MessageContext synCtx) {
        InternalAPI internalApi = findAPI(synCtx);
        if (internalApi == null) {
            log.warn("No Internal API found to dispatch the message");
            return false;
        }

        APIResource resource = findResource(synCtx, internalApi);
        if (resource == null) {
            log.warn("No matching Resource found in " + internalApi.getName() +
                    " InternalAPI to dispatch the message");
            return false;
        }
        return resource.invoke(synCtx);
    }

    /* Finds the API that the message should be dispatched to */
    private InternalAPI findAPI(MessageContext synCtx) {
        for (InternalAPI internalApi : internalApis) {
            String context = internalApi.getContext();
            String path = RESTUtils.getFullRequestPath(synCtx);
            if (path.startsWith(context + "/") || path.startsWith(context + "?") || context.equals(path)) {
                return internalApi;
            }
        }
        return null;
    }

    /* Finds the Resource that the message should be dispatched to */
    private APIResource findResource(MessageContext synCtx, InternalAPI internalApi) {

        org.apache.axis2.context.MessageContext axis2Ctx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        String method = (String) axis2Ctx.getProperty(Constants.Configuration.HTTP_METHOD);

        String path = (String) synCtx.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        String subPath = path.substring(internalApi.getContext().length());
        if ("".equals(subPath)) {
            subPath = "/";
        }

        for (APIResource resource : internalApi.getResources()) {
            if (!resource.getMethods().contains(method)) {
                continue;
            }
            DispatcherHelper helper = resource.getDispatcherHelper();
            URITemplateHelper templateHelper = (URITemplateHelper) helper;
            Map<String, String> variables = new HashMap<>();
            if (templateHelper.getUriTemplate().matches(subPath, variables)) {
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    synCtx.setProperty(RESTConstants.REST_URI_VARIABLE_PREFIX + entry.getKey(),
                            entry.getValue());
                }

                int queryIndex = path.indexOf('?');
                if (queryIndex != -1) {
                    String query = path.substring(queryIndex + 1);
                    String[] entries = query.split(RESTConstants.QUERY_PARAM_DELIMITER);
                    String name = null;
                    String value;
                    for (String entry : entries) {
                        int index = entry.indexOf('=');
                        if (index != -1) {
                            try {
                                name = entry.substring(0, index);
                                value = URLDecoder.decode(entry.substring(index + 1),
                                        RESTConstants.DEFAULT_ENCODING);
                                synCtx.setProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + name, value);
                            } catch (UnsupportedEncodingException uee) {
                                 log.error("Error processing " + method + " request for : " + path, uee);
                            } catch (IllegalArgumentException e) {
                                String errorMessage = "Error processing " + method + " request for : " + path
                                        + " due to an error in the request sent by the client";
                                synCtx.setProperty(SynapseConstants.ERROR_CODE, HttpStatus.SC_BAD_REQUEST);
                                synCtx.setProperty(SynapseConstants.ERROR_MESSAGE, errorMessage);
                                org.apache.axis2.context.MessageContext inAxisMsgCtx =
                                        ((Axis2MessageContext) synCtx).getAxis2MessageContext();
                                inAxisMsgCtx.setProperty(SynapseConstants.HTTP_SC, HttpStatus.SC_BAD_REQUEST);
                                log.error(errorMessage, e);
                            }
                        } else {
                            // If '=' sign isn't present in the entry means that the '&' character is part of
                            // the query parameter value. If so query parameter value should be updated appending
                            // the remaining characters.
                            String existingValue = (String) synCtx.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + name);
                            value = RESTConstants.QUERY_PARAM_DELIMITER + entry;
                            synCtx.setProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + name, existingValue + value);
                        }
                    }
                }
                return resource;
            }
        }
        return null;
    }
}
