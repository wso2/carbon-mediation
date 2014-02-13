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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Namespace information holder
 */
public class NameSpacesInformation {

    private final Map<String, String> nameSpaces = new HashMap<String, String>();

    public void addNameSpace(String prefix, String nsURI) {
        nameSpaces.put(prefix, nsURI);
    }

    public String getNameSpaceURI(String prefix) {
        return nameSpaces.get(prefix);
    }

    public Iterator<String> getPrefixes() {
        return nameSpaces.keySet().iterator();
    }

    public void removeAllNameSpaces() {
        nameSpaces.clear();
    }

    public String toString() {
        return nameSpaces.toString();
    }
    public Map<String, String> getNameSpaces(){
        return nameSpaces;
    }
     public void setNameSpaces(Map<String, String> namespaceMap) {
        Collection<String> prefixes = namespaceMap.keySet();
        if (!prefixes.isEmpty()) {
            for (String prefix : prefixes) {
                String uri = namespaceMap.get(prefix);
                nameSpaces.put(prefix, uri);
            }
        }

    }
}
