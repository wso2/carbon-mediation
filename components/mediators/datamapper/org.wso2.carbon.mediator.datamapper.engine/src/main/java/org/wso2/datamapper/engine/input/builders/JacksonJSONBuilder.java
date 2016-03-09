/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.datamapper.engine.input.builders;

import com.fasterxml.jackson.core.JsonFactory;
import org.wso2.datamapper.engine.core.Model;
import org.wso2.datamapper.engine.input.Buildable;

/**
 *
 */
public class JacksonJSONBuilder implements Buildable {
    @Override
    public Model build(Object model) {
        return null;
    }

    @Override
    public Model buildObject(String name, Model modelObject) {
        JsonFactory jsonFactory = new JsonFactory();
        return null;
    }

    @Override
    public Model buildArray(String arrayName, Model modelArray) {
        return null;
    }

    @Override
    public Model buildField(String name, Object value) {
        return null;
    }
}
