/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.message.store.ui.utils;

import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.util.xpath.SynapseJsonPath;
import org.apache.synapse.util.xpath.SynapseXPath;

/**
 * <p>
 * Specifies the path info which will be included if xPath or jsonPath is being specified.
 * </p>
 */
public class PathInfo {
    /**
     * If the specified path is based on xml this will be populated.
     */
    private SynapsePath xPath;
    /**
     * If the specified path is json this will be populated.
     */
    private SynapseJsonPath jsonPath;

    public SynapsePath getXPath() {
        return xPath;
    }

    public void setxPath(SynapseXPath xPath) {
        this.xPath = xPath;
    }

    public SynapseJsonPath getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(SynapseJsonPath jsonPath) {
        this.jsonPath = jsonPath;
    }
}
