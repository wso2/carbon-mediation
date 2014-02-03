/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.wso2.carbon.mediation.dependency.mgt.resolvers;

import org.apache.synapse.mediators.Value;
import org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject;
import org.apache.synapse.Mediator;
import org.apache.synapse.util.resolver.ResourceMap;
import org.apache.synapse.mediators.transform.XSLTMediator;

import java.util.List;
import java.util.ArrayList;

public class XSLTMediatorResolver extends AbstractDependencyResolver {

    public List<ConfigurationObject> resolve(Mediator m) {
        if (!(m instanceof XSLTMediator)) {
            return null;
        }

        List<ConfigurationObject> providers = new ArrayList<ConfigurationObject>();
        XSLTMediator xsltMediator = (XSLTMediator) m;
        Value xsltKey = xsltMediator.getXsltKey();
        if (xsltKey.getKeyValue() != null) {
            addProvider(new ConfigurationObject(ConfigurationObject.TYPE_ENTRY,
                    xsltKey.getKeyValue()),
                    providers);
        }

        if (xsltMediator.getResourceMap() != null) {
            ResourceMap resources = xsltMediator.getResourceMap();
            for (String key : resources.getResources().values()) {
                addProvider(new ConfigurationObject(ConfigurationObject.TYPE_ENTRY, key),
                        providers);
            }
        }
        
        return providers;
    }
}
