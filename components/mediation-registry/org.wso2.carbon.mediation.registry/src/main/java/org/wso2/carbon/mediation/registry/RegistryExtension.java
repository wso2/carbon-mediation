/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mediation.registry;

import org.apache.axiom.om.OMNode;

import java.util.Properties;

/**
 * Implementations of this interface can be engaged with the WSO2Registry registry
 * adapter so that when a given resource cannot be found in the registry the lookup
 * operations can be gracefully handled by an extension. Extensions add more value
 * and power to the registry adapter by extending its abilities use custom protocols,
 *  databases and other external registries to fetch resources.
 */
public interface RegistryExtension {

    /**
     * Initialize the extension implementation with a set of properties
     *
     * @param props the Set of proeprties with which to initialize the extension
     */
    public void init(Properties props);

    /**
     * Attempt to find a resource represented by a given key in a custom manner
     *
     * @param key The key representing the resource
     * @return The XML OMNode for the resource or null if the resource cannot be located
     */
    public OMNode lookup(String key);
}
