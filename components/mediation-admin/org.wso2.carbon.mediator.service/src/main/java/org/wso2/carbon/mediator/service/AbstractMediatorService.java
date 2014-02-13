/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 **/

package org.wso2.carbon.mediator.service;

/**
 * Abstract implementation of the {@link org.wso2.carbon.mediator.service.MediatorService}
 */
public abstract class AbstractMediatorService implements MediatorService {

    /**
     * Mediators which doesn't have a group doesn't show up in the add menu,
     * generally these are inner parts
     */
    public String getGroupName() {
        return null;
    }

    /** by default adding siblings to the mediator is enabled */
    public boolean isAddSiblingEnabled() {
        return true;
    }

    /** by default adding children to the mediator is enabled */
    public boolean isAddChildEnabled() {
        return true;
    }

    /** by default mediator moving is allowed */
    public boolean isMovingAllowed() {
        return true;
    }

    /** by default mediators are editable */
    public boolean isEditable() {
        return true;
    }

    /** UIFolderName defaults to the tag local name of the mediator */
    public String getUIFolderName() {
        return getTagLocalName();
    }

    /** by default refreshing the sequence is not required */
    public boolean isSequenceRefreshRequired() {
        return false;
    }
}
