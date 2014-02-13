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

import javax.servlet.http.HttpSession;

/**
 * Registrar for the namespaces information
 */
public class NameSpacesInformationRepositoryRegistrar {

    public static void unRegisterNameSpacesInformationRepository(HttpSession httpSession) {
        httpSession.removeAttribute(
                NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);
    }

    public static void registerNameSpacesInformationRepository(
            NameSpacesInformationRepository repository, HttpSession httpSession) {
        httpSession.setAttribute(
                NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY, repository);
    }
}
