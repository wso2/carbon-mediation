/**
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediator.cache.ui;

import org.wso2.carbon.mediator.cache.CachingConstants;
import org.wso2.carbon.mediator.service.AbstractMediatorService;
import org.wso2.carbon.mediator.service.ui.Mediator;

/**
 * Class that interfaces the mediator service
 */
public class CacheMediatorService extends AbstractMediatorService {

	/**
	 * This gives the mediator serialization tag local name.
	 *
	 * @return tag local name of the mediator tag QName
	 */
    public String getTagLocalName() {
        return CachingConstants.CACHE_LOCAL_NAME;
    }

	/**
	 * This gives the display name for the mediator in the add mediator menu, and this can
	 * be any {@link String}. It is recommended to put a meaning full descriptive short name
	 * as the display name
	 *
	 * @return display name in the add mediator menu of the mediator
	 */
    public String getDisplayName() {
        return "Cache";
    }

	/**
	 * This should be equivalent to {@link org.apache.synapse.Mediator#getType()} of the
	 * mediator. The value of this is generally the class name without the package declaration.
	 *
	 * @return logical name of the mediator
	 */
    public String getLogicalName() {
        return "CacheMediator";
    }

	/**
	 * Gives the mediator categorization in the add mediator menu. This should be a descriptive
	 * meaning full and short text and it is recommended to use existing group names if possible,
	 * to reduce the number of groups in the add mediator menu. it is possible to add a new group by putting any
	 * String to this.
	 *
	 * @return group name of the mediator to which this mediator is categorized in the
	 *      add mediator menu
	 */
    public String getGroupName() {
        return "Advanced";
    }

	/**
	 * Retrieves a default new mediator instances of the representing mediator. This method
	 * is used by the mediator addition and will be called to get a new instance of the
	 * mediator.
	 *
	 * @return new instance of the mediator with the default values filled
	 */
    public Mediator getMediator() {
        return new CacheMediator();
    }
}
