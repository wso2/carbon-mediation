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
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTUtils;
import org.apache.synapse.rest.dispatch.DispatcherHelper;
import org.apache.synapse.rest.dispatch.URITemplateHelper;

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
     * @param synCtx the Synapse Message Context.
     */
    public void dispatch(MessageContext synCtx) {
        InternalAPI internalApi = findAPI(synCtx);
        if (internalApi == null) {
            log.warn("No Internal API found to dispatch the message");
            return;
        }

        APIResource resource = findResource(synCtx, internalApi);
        if (resource == null) {
            log.warn("No matching Resource found in " + internalApi.getName() +
                    " InternalAPI to dispatch the message");
            return;
        }

        resource.invoke(synCtx);
        respond(synCtx);
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

        String method = (String) synCtx.getProperty(Constants.Configuration.HTTP_METHOD);

        for (APIResource resource : internalApi.getResources()) {
            if (!resource.getMethods().contains(method)) {
                continue;
            }
            String url = RESTUtils.getSubRequestPath(synCtx);
            DispatcherHelper helper = resource.getDispatcherHelper();
            URITemplateHelper templateHelper = (URITemplateHelper) helper;
            Map<String, String> variables = new HashMap<>();
            if (templateHelper.getUriTemplate().matches(url, variables)) {
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    synCtx.setProperty(RESTConstants.REST_URI_VARIABLE_PREFIX + entry.getKey(),
                            entry.getValue());
                }
                return resource;
            }
        }
        return null;
    }

    /* Sends the respond back to the client */
    private void respond(MessageContext synCtx) {
        synCtx.setTo(null);
        synCtx.setResponse(true);
        Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
        org.apache.axis2.context.MessageContext axis2MessageCtx = axis2smc.getAxis2MessageContext();
        axis2MessageCtx.getOperationContext()
                .setProperty(Constants.RESPONSE_WRITTEN, "SKIP");
        Axis2Sender.sendBack(synCtx);
    }
}
