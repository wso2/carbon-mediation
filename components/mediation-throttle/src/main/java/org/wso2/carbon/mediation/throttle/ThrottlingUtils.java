/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.mediation.throttle;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.mediation.throttle.internal.ThrottleServiceComponent;

import java.io.IOException;

/**
 * Utils for throttling
 */
public class ThrottlingUtils {

    public static void saveTemplatePoliciesToRegistry(Registry registry) throws RegistryException,
                                                                                IOException {

        BundleContext bundleContext = ThrottleServiceComponent.getBundleContext();

        // service level
        String regPath = ThrottleComponentConstants.TEMPLATE_URI +
                ThrottleComponentConstants.SERVICE_LEVEL;
        if (!registry.resourceExists(regPath)) {
            Resource serviceTemplate = registry.newResource();
            serviceTemplate.setContentStream(bundleContext
                    .getBundle().getResource("template-service-policy.xml").openStream());
            registry.put(regPath, serviceTemplate);
        }

        // global level
        regPath = ThrottleComponentConstants.TEMPLATE_URI +
                ThrottleComponentConstants.GLOBAL_LEVEL;
        if (!registry.resourceExists(regPath)) {
            Resource moduleTemplate = registry.newResource();
            moduleTemplate.setContentStream(bundleContext
                    .getBundle().getResource("template-module-policy.xml").openStream());
            registry.put(regPath, moduleTemplate);
        }

        // operation level
        regPath = ThrottleComponentConstants.TEMPLATE_URI +
                ThrottleComponentConstants.OPERATION_LEVEL;
        if (!registry.resourceExists(regPath)) {
            Resource operationTemplate = registry.newResource();
            operationTemplate.setContentStream(bundleContext
                    .getBundle().getResource("template-operation-policy.xml").openStream());
            registry.put(regPath, operationTemplate);
        }

        // mediation level
        regPath = ThrottleComponentConstants.TEMPLATE_URI +
                ThrottleComponentConstants.MEDIATION_LEVEL;
        if (!registry.resourceExists(regPath)) {
            Resource mediatorTemplate = registry.newResource();
            mediatorTemplate.setContentStream(bundleContext
                    .getBundle().getResource("template-mediator-policy.xml").openStream());
            registry.put(regPath, mediatorTemplate);
        }
    }
}
