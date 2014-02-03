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

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.service.builtin.SequenceMediatorService;
import org.wso2.carbon.mediator.service.builtin.UILessMediatorService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of the mediators that are registered for the sequence editor UI
 */
public class MediatorStore {

    /** Singleton instance */
    private static MediatorStore instance = null;
    /** MediatorService instances registered for the given mediator name */
    private Map<String, MediatorService> store = new HashMap<String, MediatorService>();

    // make sure the singleton behavior
    private MediatorStore() {}

    /**
     * Singleton access to the MediatorStore
     * @return the singleton instance
     */
    public synchronized static MediatorStore getInstance() {
        if (instance == null) {
            MediatorService service = new SequenceMediatorService();
            instance = new MediatorStore();
            instance.registerMediator(service.getTagLocalName(), service);
        }
        return instance;
    }

    /**
     * Registers a {@link org.wso2.carbon.mediator.service.MediatorService} with the store
     *
     * @param mediatorName name of the mediator being registered
     * @param mediatorService implementation of the mediator service for
     * the mediator with name <code>mediatorName</code>
     */
    public void registerMediator(String mediatorName, MediatorService mediatorService) {
        store.put(mediatorName, mediatorService);
    }

    /**
     * Un registers a {@link org.wso2.carbon.mediator.service.MediatorService} from the store
     *
     * @param mediatorName name of the mediator of the service to be un registered
     */
    public void unRegisterMediator(String mediatorName) {
        store.remove(mediatorName);
    }

    /**
     * Retrieves the {@link org.wso2.carbon.mediator.service.MediatorService} with
     * the given <code>mediatorName</code>
     *
     * @param mediatorName name of the mediator to retrieve the mediator service
     * @return the mediator service mapped to the given mediator name
     */
    public MediatorService getMediatorService(String mediatorName) {
        MediatorService service = store.get(mediatorName);
        if (service == null) {
            UILessMediatorService defService = new UILessMediatorService();
            defService.setLocalName(mediatorName);
            return defService;
        }

        return service;

    }

    /**
     * Retrieves the registered {@link org.wso2.carbon.mediator.service.MediatorService} collection
     *
     * @return all the registered mediator services in the store
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public Collection<MediatorService> getRegisteredMediators() {
        return store.values();
    }

    /**
     * Retrieves the menu item of mediators, which is being used by the UI
     *
     * @return the mapped data model for the mediator menu items
     */
    public HashMap<String, HashMap<String, String>> getMediatorMenuItems() {
        HashMap<String, HashMap<String, String>> parentMap
                = new HashMap<String, HashMap<String, String>>();
        for (MediatorService mediatorService : store.values()) {
            String group = mediatorService.getGroupName();
            if (group != null && !"uiless".equals(group.toLowerCase())) {
                HashMap<String, String> menuMap;
                if (parentMap.containsKey(group)) {
                    menuMap = parentMap.get(group);
                } else {
                    menuMap = new HashMap<String, String>();
                    parentMap.put(group, menuMap);
                }
                menuMap.put(mediatorService.getTagLocalName(), mediatorService.getDisplayName());
            }
        }
        return parentMap;
    }

    /**
     * Helper method for retrieving the {@link org.wso2.carbon.mediator.service.MediatorService}
     * by looking at the given serialized mediator element
     *
     * @param element serialized representation of a mediator of which
     * the mediator service is looked up
     * @return the looked up mediator service from the mediator serialization format
     */
    public MediatorService getMediatorService(OMElement element) {                     
        return getMediatorService(element.getLocalName());
	}
}
