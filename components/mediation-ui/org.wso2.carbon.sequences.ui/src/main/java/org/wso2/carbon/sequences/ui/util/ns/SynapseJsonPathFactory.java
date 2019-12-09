/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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
package org.wso2.carbon.sequences.ui.util.ns;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.util.xpath.SynapseJsonPath;
import org.jaxen.JaxenException;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is used to generate SynapseJsonPath instances from the management console UI.
 */
public class SynapseJsonPathFactory {

    private static final Log log = LogFactory.getLog(SynapseJsonPathFactory.class);

    private final static SynapseJsonPathFactory instance = new SynapseJsonPathFactory();

    private final static String JSON_EVAL = "json-eval(";

    private SynapseJsonPathFactory() {
        super();
    }

    public static SynapseJsonPathFactory getInstance() {
        return instance;
    }

    /**
     * Generate a JSON path instance from HTTP request.
     * @param id id of the parameter
     * @param request http request
     * @return Js
     */
    public SynapseJsonPath createSynapseJsonPath(String id, HttpServletRequest request) {
        return createSynapseJsonPath(id, request.getParameter(id));
    }

    public SynapseJsonPath createSynapseJsonPath(String id, String source) {
        try {
            if (!assertIDNotEmpty(id) || !assertSourceNotEmpty(source)) {
                return null;
            }
            String expression = source.trim();
            if (expression.startsWith(JSON_EVAL)) {
                int expLength = expression.length();
                expression = expression.substring(JSON_EVAL.length(), expLength - 1);
            }
            SynapseJsonPath jsonPath = new SynapseJsonPath(expression);
            return jsonPath;
        } catch (JaxenException e) {
            String msg = "Error creating a JsonPath from text : " + source;
            throw new RuntimeException(msg, e);
        }
    }

    private static boolean assertIDNotEmpty(String id) {
        if (id == null || id.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Provided id is empty or null ,returning a null as JsonPath");
            }
            return false;
        }
        return true;
    }

    private static boolean assertSourceNotEmpty(String source) {
        if (source == null || "".equals(source)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided source is empty or null ,returning a null as JsonPath");
            }
            return false;
        }
        return true;
    }

}
