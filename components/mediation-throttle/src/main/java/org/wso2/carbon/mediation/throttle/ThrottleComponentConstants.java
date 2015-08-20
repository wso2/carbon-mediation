/*
 * Copyright 2005-2008 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.mediation.throttle;

import org.wso2.carbon.core.RegistryResources;

import javax.xml.namespace.QName;

/**
 * Class containing constants used in the throttle component
 */

public final class ThrottleComponentConstants {

    public static final String TEMPLATE_URI =
            RegistryResources.COMPONENTS + "org.wso2.carbon.mediation.throttle/templates/";

    public static final QName ALL = new
                    QName("http://schemas.xmlsoap.org/ws/2004/09/policy", "All");

    public static final QName EXACTLY_ONE = new
                    QName("http://schemas.xmlsoap.org/ws/2004/09/policy", "ExactlyOne");

    public final static String THROTTLE_MODULE = "wso2throttle";
    public static final String THROTTLE_POLICY_ID = "throttle_policy_id";
    public static final String DOMIN_ATT_VALUE = "DOMAIN";

    public static final String GLOBAL_LEVEL = "global";
    public static final String SERVICE_LEVEL = "service";
    public static final String OPERATION_LEVEL = "operation";
    public static final String MEDIATION_LEVEL = "mediator";

}
