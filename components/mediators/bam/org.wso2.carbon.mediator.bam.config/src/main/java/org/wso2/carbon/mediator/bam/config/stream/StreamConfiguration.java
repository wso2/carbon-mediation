/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.mediator.bam.config.stream;

import java.util.ArrayList;
import java.util.List;

/**
 * Stream Configuration Definition of an Event
 */
public class StreamConfiguration {

    private String name = "";
    private String nickname = "";
    private String description = "";
    private String version = "";
    private List<StreamEntry> entries = new ArrayList<StreamEntry>();
    private List<Property> properties = new ArrayList<Property>();

    public void setName(String name){
        this.name = name;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setVersion(String version){
        this.version = version;
    }

    public String getName(){
        return this.name;
    }

    public String getNickname(){
        return this.nickname;
    }

    public String getDescription(){
        return this.description;
    }

    public String getVersion(){
        return this.version;
    }

    public List<StreamEntry> getEntries(){
        return this.entries;
    }

    public List<Property> getProperties(){
        return this.properties;
    }

}
