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
package org.wso2.datamapper.engine.core.executors.nashorn;

import org.wso2.datamapper.engine.core.Executable;
import org.wso2.datamapper.engine.core.MappingResourceLoader;
import org.wso2.datamapper.engine.core.Model;
import org.wso2.datamapper.engine.core.exceptions.JSException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * This class implements script executor for data mapper using java 8 NasHorn JS executor
 */
public class NasHornJava8Executor implements Executable {

    private ScriptEngine scriptEngine;
    public NasHornJava8Executor(){
        scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
    }

    @Override
    public Model execute(MappingResourceLoader resourceModel, String inputRecord) throws JSException {
        try {
            scriptEngine.eval("var inputemployees="+inputRecord);
            scriptEngine.eval("function map_S_employees_S_engineers(){ \n" +
                    "\n" +
                    "\n" +
                    "var outputengineers={};"+
                    "outputengineers.engineer = [];"+
                    "for(i_employeeRecord in inputemployees.employee){\n" +
                    "\n" + "outputengineers.engineer[i_employeeRecord]={};"+
                    "outputengineers.engineer[i_employeeRecord].fullname= inputemployees.employee[i_employeeRecord].firstname + inputemployees.employee[i_employeeRecord].lastname;\n" +
                    "\n" +
                    "outputengineers.engineer[i_employeeRecord].address= inputemployees.employee[i_employeeRecord].address.no + inputemployees.employee[i_employeeRecord].address.city;\n" +
                    "\n" +
                    "\n" +
                    "}\n" +
                    "return outputengineers;\n" +
                    "}");
            Invocable invocable = (Invocable) scriptEngine;

            Object result = invocable.invokeFunction("map_S_employees_S_engineers");
            System.out.println(result);

        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
