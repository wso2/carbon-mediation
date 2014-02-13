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

package org.wso2.carbon.mediator.service.ui;

import java.util.List;

/**
 * Defines a mediator with child mediators within its configuration
 */
@SuppressWarnings({"UnusedDeclaration"})
public interface ListMediator extends Mediator {

    /**
     * Get the child list of mediators
     * @return list of child mediators
     */
    public List<Mediator> getList();

    /**
     * Get the child from the position
     * @param pos position of the child
     * @return mediator of this position. If the position invalid null will be returned
     */
    public Mediator getChild(int pos);

    /**
     * Remove a child from this position
     * @param pos position of the child
     * @return mediator that is removed
     */
    public Mediator removeChild(int pos);

    /**
     * Remove this child
     * @param mediator mediator to be removed
     * @return true if the remove successful
     */
    public boolean removeChild(Mediator mediator);

    /**
     * Add the child to this mediator
     * @param mediator child mediator to be added
     */
    public void addChild(Mediator mediator);
}
