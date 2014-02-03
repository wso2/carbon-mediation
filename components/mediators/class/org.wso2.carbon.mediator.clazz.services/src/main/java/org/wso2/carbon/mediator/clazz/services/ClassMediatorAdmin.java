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
package org.wso2.carbon.mediator.clazz.services;

import org.apache.axis2.AxisFault;

import java.util.ArrayList;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ClassMediatorAdmin {
     public String[] getClassSetProps(String className) throws AxisFault{
        ArrayList<String> setters = new ArrayList<String>();
        try {
            Class clazz = Class.forName(className);
            Field[] field = clazz.getDeclaredFields();
            Method[] methods = clazz.getMethods();
            for (int i = 0; i < methods.length; i++) {
                String methodName = methods[i].getName();
                if (methodName.startsWith("set")) {
                    for (int j = 0; j < field.length; j++) {
                        if (field[j].getName().equalsIgnoreCase(methodName.substring(3))) {
                            setters.add(field[j].getName());
                            break;
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new AxisFault("Class " + className + " not found in the path",e);
        }
          if(setters.toArray(new String[setters.size()]).length > 0){
            return setters.toArray(new String[setters.size()]);
        }
        return null;
    }
}