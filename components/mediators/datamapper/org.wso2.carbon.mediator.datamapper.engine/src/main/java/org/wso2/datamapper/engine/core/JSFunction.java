/*
 * Copyright 2016 WSO2, Inc. http://www.wso2.org
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
package org.wso2.datamapper.engine.core;

/**
 * This class will hold the data mapper mapping configuration
 */
public class JSFunction {

    private String functioName;
    private String functionBody;

    public JSFunction(String name, String body) {
        this.setFunctioName(name);
        this.setFunctionBody(body);
    }

    public String getFunctioName() {
        return functioName;
    }

    public void setFunctioName(String functioName) {
        this.functioName = functioName;
    }

    public String getFunctionBody() {
        return functionBody;
    }

    public void setFunctionBody(String functionBody) {
        this.functionBody = functionBody;
    }

}
