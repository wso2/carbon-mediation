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
 */

package org.wso2.carbon.sequences.ui.util.ns;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

/**
 * Represents a namespace repository containing the information about the namespaces in it
 */
@SuppressWarnings({"UnusedDeclaration"})
public class NameSpacesInformationRepository implements Serializable {

    public static final String NAMESPACES_INFORMATION_REPOSITORY
            = "NameSpacesInformationRepository";

    private final Map<String, NameSpacesOwner> nameSpacesOwnerInfo
            = new HashMap<String, NameSpacesOwner>();

    public void addNameSpacesInformation(String ownerID, String id,
                                         NameSpacesInformation nameSpacesInformation) {
        NameSpacesOwner owner = nameSpacesOwnerInfo.get(ownerID);
        if (owner == null) {
            owner = new NameSpacesOwner(ownerID);
            nameSpacesOwnerInfo.put(ownerID, owner);
        }
        owner.addNameSpacesInformation(id, nameSpacesInformation);
    }

    public NameSpacesInformation getNameSpacesInformation(String ownerID, String id) {
        NameSpacesOwner owner = nameSpacesOwnerInfo.get(ownerID);
        if (owner != null) {
            return owner.getNameSpacesInformation(id);
        }
        return null;
    }

    public void removeAllNameSpacesInformation(String ownerId) {
        NameSpacesOwner owner = nameSpacesOwnerInfo.get(ownerId);
        if (owner != null) {
            owner.removeAllNameSpacesInformation();
        }
    }

    public void removeAllNameSpacesInformation() {
        nameSpacesOwnerInfo.clear();
    }

    private static class NameSpacesOwner {

        private final Map<String, NameSpacesInformation> nameSpacesInfoMap
                = new HashMap<String, NameSpacesInformation>();
        private String ownerID;

        private NameSpacesOwner(String ownerID) {
            this.ownerID = ownerID;
        }

        public void addNameSpacesInformation(String id,
                                             NameSpacesInformation nameSpacesInformation) {
            nameSpacesInfoMap.put(id, nameSpacesInformation);
        }

        public NameSpacesInformation getNameSpacesInformation(String id) {
            return nameSpacesInfoMap.get(id);
        }

        public void removeAllNameSpacesInformation() {
            nameSpacesInfoMap.clear();
        }

        public String getOwnerID() {
            return ownerID;
        }
    }
}
