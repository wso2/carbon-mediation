/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.mediation.dependency.mgt;

public class ConfigurationObject {

    private int type;
    private String id;

    public static final int TYPE_ENDPOINT      = 0;
    public static final int TYPE_SEQUENCE      = 1;
    public static final int TYPE_PROXY         = 2;
    public static final int TYPE_ENTRY         = 3;
    public static final int TYPE_STARTUP       = 4;
    public static final int TYPE_EVENTSOURCE   = 5;
    public static final int TYPE_UNKNOWN       = 6;

    public ConfigurationObject(int type, String id) {
        this.type = type;
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigurationObject that = (ConfigurationObject) o;

        return type == that.type && !(id != null ? !id.equals(that.id) : that.id != null);
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    public String getTypeName() {
        switch (type) {
            case TYPE_ENDPOINT      : return "endpoint";
            case TYPE_SEQUENCE      : return "sequence";
            case TYPE_PROXY         : return "proxy service";
            case TYPE_ENTRY         : return "localentry";
            case TYPE_STARTUP       : return "startup";
            case TYPE_EVENTSOURCE   : return "event source";
            default                 : return "unknown";
        }
    }
}
