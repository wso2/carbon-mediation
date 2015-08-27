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


import org.wso2.carbon.bam.service.data.publisher.conf.EventConfigNStreamDef;

import java.util.HashMap;
import java.util.Map;

public class TenantEventConfigData {

    private volatile static Map<Integer, EventConfigNStreamDef> tenantEventingConfigDataMap;

    public static Map<Integer, EventConfigNStreamDef> getTenantSpecificEventingConfigData() {
        if (tenantEventingConfigDataMap == null) {
            synchronized (TenantEventConfigData.class) {
                if (tenantEventingConfigDataMap == null) {
                    tenantEventingConfigDataMap = new HashMap<Integer, EventConfigNStreamDef>();
                }
            }
        }
        return tenantEventingConfigDataMap;
    }

}
