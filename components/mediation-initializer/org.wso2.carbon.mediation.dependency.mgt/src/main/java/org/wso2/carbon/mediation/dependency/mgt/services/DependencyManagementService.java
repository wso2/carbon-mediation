/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.carbon.mediation.dependency.mgt.services;

import org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject;

/**
 * This service interface enables access to the underlying configuration dependency
 * management model used by the mediation initializer thus allowing other components
 * to retrieve and resolve inter dependencies among different mediation configuration
 * elements (eg: sequences, endpoints).
 */
public interface DependencyManagementService {

    /**
     * Check whether the specified object has any dependent objects. This also includes
     * non-active (type UNKNOWN) objects which may still have a dependency on the given
     * object.
     *
     * @param tenantId Tenant ID
     * @param type Type of the object
     * @param key Name (ID) of the object
     * @return true if the object has at least one dependent and false otherwise
     */
    public boolean hasDependents(int tenantId, int type, String key);

    /**
     * Check whether the specified object has any dependent objects. This does not include
     * non-active (type UNKNOWN) objects which may still have a dependency on the given
     * object.
     *
     * @param tenantId Tenant ID
     * @param type Type of the object
     * @param key Name (ID) of the object
     * @return true if the object has at least one active dependent and false otherwise
     */
    public boolean hasActiveDependents(int tenantId, int type, String key);

    /**
     * Find all the configuration objects dependent on a specified configuration object.
     * This method also returns the non-active (type UNKNOWN) configuration objects which
     * are dependent on the specified object.
     *
     * @param tenantId Tenant ID
     * @param type integer value representing the type of the object
     * @param key name of the object
     * @return an array of dependent objects or null if there are no dependents
     */
    public ConfigurationObject[] getDependents(int tenantId, int type, String key);

    /**
     * Check whether the specified object has any dependent objects in super tenant
     * SynapseConfiguration. This also includes non-active (type UNKNOWN) objects which may
     * still have a dependency on the given object.
     *
     * @param type Type of the object
     * @param key Name (ID) of the object
     * @return true if the object has at least one dependent and false otherwise
     */
    public boolean hasDependents(int type, String key);

    /**
     * Check whether the specified object has any dependent objects in super tenant
     * SynapseConfiguration. This does not include non-active (type UNKNOWN) objects
     * which may still have a dependency on the given object.
     *
     * @param type Type of the object
     * @param key Name (ID) of the object
     * @return true if the object has at least one active dependent and false otherwise
     */
    public boolean hasActiveDependents(int type, String key);

    /**
     * Find all the configuration objects dependent on a specified configuration object in the
     * super tenant super tenant SynapseConfiguration. This method also returns the non-active
     * (type UNKNOWN) configuration objects which are dependent on the specified object.
     *
     * @param type integer value representing the type of the object
     * @param key name of the object
     * @return an array of dependent objects or null if there are no dependents
     */
    public ConfigurationObject[] getDependents(int type, String key);
}
