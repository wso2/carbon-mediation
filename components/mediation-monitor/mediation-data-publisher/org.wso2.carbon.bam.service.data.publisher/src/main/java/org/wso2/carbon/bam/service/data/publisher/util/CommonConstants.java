/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.service.data.publisher.util;


import org.wso2.carbon.core.RegistryResources;

public class CommonConstants {

    public static final String IP_ADDRESS = "ip_address";

    public static final String ADMIN_SERVICE_PARAMETER = "adminService";
    public static final String HIDDEN_SERVICE_PARAMETER = "hiddenService";

    public static final String BAM_URL = "BAMUrl";
    public static final String BAM_USER_NAME = "BAMUserName";
    public static final String BAM_PASSWORD = "BAMPassword";


    public static final String SERVICE_COMMON_REG_PATH = RegistryResources.COMPONENTS
                                                   + "org.wso2.carbon.bam.service.data.publisher/common/";

    public static final String SERVICE_PROPERTIES_REG_PATH = RegistryResources.COMPONENTS
                                                   + "org.wso2.carbon.bam.service.data.publisher/properties";

    public  static final String BAM_CONFIG_XML = "bam.xml";
    public static final String BAM_SERVICE_PUBLISH_OMELEMENT = "ServiceDataPublishing";
    public static final String BAM_SERVICE_PUBLISH_ENABLED = "enable";

    public static final String CLOUD_DEPLOYMENT_PROP = "IsCloudDeployment";

    public static final String SERVER_CONFIG_BAM_URL = "BamServerURL";

    public static final String DEFAULT_BAM_SERVER_URL = "tcp://127.0.0.1:7611";


}
