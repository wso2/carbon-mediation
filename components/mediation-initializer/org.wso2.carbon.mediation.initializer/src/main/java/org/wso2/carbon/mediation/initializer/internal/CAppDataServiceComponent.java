/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediation.initializer.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.mediation.initializer.services.CAppArtifactDataService;
import org.wso2.carbon.mediation.initializer.services.CAppArtifactDataServiceImpl;

/**
 * @scr.component name="org.wso2.carbon.capp.component" immediate="true"
 */

public class CAppDataServiceComponent {

    private static Log log = LogFactory.getLog(CAppDataServiceComponent.class);

    public CAppDataServiceComponent() {
    }

    protected void activate(ComponentContext ctxt) {
        ctxt.getBundleContext().registerService(CAppArtifactDataService.class.getName(),
                new CAppArtifactDataServiceImpl(), null);
    }
}