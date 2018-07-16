/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediation.registry;

import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.core.RegistryResources;

import java.io.File;

/**
 *
 */
public class ESBRegistryConstants {

    // ESBRegistryConstants for ESB registry
    public static final int LOCAL_HOST_REGISTRY = 100;
    public static final int REMOTE_HOST_REGISTRY = 101;
    public static final int REGISTRY_MODE = LOCAL_HOST_REGISTRY;
    // this will be overwritten if localRegistry parameter is set
    public static final String LOCAL_REGISTRY_ROOT = "registry/";
    public static final String REGISTRY_FILE = "file";
    public static final String REGISTRY_FOLDER = "folder";
    public static final String FOLDER = "http://wso2.org/projects/esb/registry/types/folder";
    // use if the exact FILE type is not known
    public static final String FILE = "http://wso2.org/projects/esb/registry/types/file";
    public static final String IMPORT_ROOT = "importRoot";
    public static final String IMPORT_LOCAL_REGISTRY_ROOT =
            "repository" + File.separator + "samples" + File.separator + "resources";
    public final static String ROOT_PATH = RegistryResources.ROOT + "esb"
            + RegistryConstants.PATH_SEPARATOR + "registry";

    public final static String SYNAPSE_REGISTRY_ROOT = "SYNAPSE_REGISTRY_ROOT";

    public static final String CONF_REG_ROOT = "ConfigRegRoot";
    public static final String GOV_REG_ROOT = "GovRegRoot";
    public static final String LOCAL_REG_ROOT = "LocalRegRoot";

    public static final String PROTOCOL_FILE = "file";
    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_HTTPS = "https";


    public static final String CONFIG_REGISTRY_PREFIX = "conf:";
    public static final String GOVERNANCE_REGISTRY_PREFIX = "gov:";
    public static final String LOCAL_REGISTRY_PREFIX = "local:";
    public static final String FILE_URI_PREFIX = "file:";

    public static final char URL_SEPARATOR_CHAR = '/';
    public static final String URL_SEPARATOR = "/";
    public static final String PROPERTY_EXTENTION = ".properties";
}
