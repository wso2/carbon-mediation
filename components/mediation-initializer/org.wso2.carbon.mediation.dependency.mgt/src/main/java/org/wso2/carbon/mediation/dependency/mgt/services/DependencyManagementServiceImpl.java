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

package org.wso2.carbon.mediation.dependency.mgt.services;

import org.wso2.carbon.mediation.dependency.mgt.DependencyTracker;
import org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;

public class DependencyManagementServiceImpl implements DependencyManagementService {

    private Map<Integer,DependencyTracker> trackers = new HashMap<Integer,DependencyTracker>();

    public DependencyManagementServiceImpl() {

    }

    public DependencyManagementServiceImpl(DependencyTracker tracker) {
        trackers.put(MultitenantConstants.SUPER_TENANT_ID, tracker);
    }

    protected synchronized void setDependencyTracker(int tenantId, DependencyTracker tracker) {
        trackers.put(tenantId, tracker);
    }

    public boolean hasDependents(int tenantId, int type, String key) {
        DependencyTracker tracker = trackers.get(tenantId);
        return tracker != null && tracker.hasDependents(type, key);
    }

    public boolean hasActiveDependents(int tenantId, int type, String key) {
        DependencyTracker tracker = trackers.get(tenantId);
        return tracker != null && tracker.hasActiveDependents(type, key);
    }

    public ConfigurationObject[] getDependents(int tenantId, int type, String key) {
        DependencyTracker tracker = trackers.get(tenantId);
        if (tracker != null) {
            ConfigurationObject[] dependents = tracker.getDependents(type, key);
            if (dependents != null) {
                return dependents.clone();
            }
        }
        return null;
    }

    public boolean hasDependents(int type, String key) {
        return hasDependents(MultitenantConstants.SUPER_TENANT_ID, type, key);
    }

    public boolean hasActiveDependents(int type, String key) {
        return hasActiveDependents(MultitenantConstants.SUPER_TENANT_ID, type, key);
    }

    public ConfigurationObject[] getDependents(int type, String key) {
        return getDependents(MultitenantConstants.SUPER_TENANT_ID, type, key);
    }
}
