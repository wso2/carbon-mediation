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

import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.dispatch.DispatcherHelper;
import org.apache.synapse.rest.dispatch.URITemplateHelper;

import java.util.Set;

/**
 * {@code APIResource} is the abstract implementation of a Resource in an Internal API.
 *
 * An {@link InternalAPI} must have one or more Resources. So if we want to register an internal api into EI,
 * we need to create one or more Resources extending this abstract class and make it accessible through
 * {@link InternalAPI#getResources()} method.
 *
 */
public abstract class APIResource {

    private DispatcherHelper dispatcherHelper;

    /**
     * Gets the HTTP methods supported by this Resource.
     *
     * @return the supported HTTP methods
     */
    abstract public Set<String> getMethods();

    /**
     * Invokes the API Resource.
     *
     * @param synCtx the Synapse Message Context
     */
    abstract public void invoke(MessageContext synCtx);

    /**
     * Constructor for creating an API Resource.
     *
     * @param urlTemplate   the url template of the Resource
     */
    public APIResource(String urlTemplate) {
        dispatcherHelper = new URITemplateHelper(urlTemplate);
    }

    /**
     * Gets the {@link DispatcherHelper} related to the the Resource.
     *
     * @return the DispatcherHelper
     */
    final DispatcherHelper getDispatcherHelper() {
        return dispatcherHelper;
    }

}
