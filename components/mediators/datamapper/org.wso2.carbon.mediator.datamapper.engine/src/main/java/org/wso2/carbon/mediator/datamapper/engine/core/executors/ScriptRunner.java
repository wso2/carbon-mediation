/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.mediator.datamapper.engine.core.executors;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class ScriptRunner {

    private final Context context;
    private final Value mapFunction;

    public ScriptRunner(String jsCode) {
        this.context = Context.create("js");
        // we need to evaluate the script only once
        this.mapFunction = context.eval("js", jsCode);
    }

    public String runScript(String inputJson, String variablesJson) {
        // provide input/payload and variables as JSON objects
        Value input = context.eval("js", "(" + inputJson + ")");
        Value variables = context.eval("js", "(" + variablesJson + ")");
        Value result = mapFunction.execute(input, variables);
        return result.toString();
    }

    public void close() {
        context.close();
    }
}
