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
package org.wso2.carbon.business.messaging.salesforce.mediator.ui.handler;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;

/**
 * This class invokes instance methods of operation,input,output Types using java reflection API
 *
 * @see java.lang.reflect
 * @see com.sforce.soap.enterprise
 */
public class PropertyHandler {
    /**
     * Log variable for the logging purposes
     */
    private static final Log log = LogFactory.getLog(PropertyHandler.class);

    /**
     * Find and invoke the setter method with the name of form setXXX passing in
     * the value given on the POJO object
     *
     * @param name name of the setter field
     * @param val  value to be set
     * @param obj  POJO instance
     */
    @SuppressWarnings("unchecked")
    public static void setInstanceProperty(String name, Object val, Object obj) {

        String mName = "set" + Character.toUpperCase(name.charAt(0))
                       + name.substring(1);
        Method method;

        try {
            Method[] methods = obj.getClass().getMethods();

            for (Method method1 : methods) {
                if (method1 != null && mName.equals(method1.getName())) {
                    Class[] params = method1.getParameterTypes();
                    if (params.length != 1) {
                        handleException("Did not find a setter method named : "
                                        + mName
                                        + "() that takes a single String, int, long, float, double "
                                        + "or boolean parameter");
                    }
                    if (val instanceof String) {
                        String value = (String) val;
                        if (params[0].equals(String.class)) {
                            invoke(obj, mName, new Object[]{value});
                        } else if (params[0].equals(int.class)) {
                            invoke(obj, mName,
                                   new Object[]{new Integer(value)});
                        } else if (params[0].equals(long.class)) {
                            invoke(obj, mName, new Object[]{new Long(value)});
                        } else if (params[0].equals(float.class)) {
                            invoke(obj, mName,
                                   new Object[]{new Float(value)});
                        } else if (params[0].equals(double.class)) {
                            invoke(obj, mName,
                                   new Object[]{new Double(value)});
                        } else if (params[0].equals(boolean.class)) {
                            invoke(obj, mName,
                                   new Object[]{new Boolean(value)});
                        } else {
                            handleException("Did not find a setter method named : "
                                            + mName
                                            + "() that takes a single String, int, long, float, double "
                                            + "or boolean parameter");
                        }
                    } else if (val instanceof List) {
                        method = obj.getClass().getMethod(mName, List.class);
                        method.invoke(obj, new Object[]{val});
                    }
                }
            }
        } catch (Exception e) {
            handleException(
                    "Error invoking setter method named : "
                    + mName
                    + "() that takes a single String, int, long, float, double "
                    + "or boolean parameter", e);
        }
    }

    /**
     * Find and invoke the setter method with the name of form setXXX passing in
     * the value given on the POJO object
     *
     * @param name name of the setter field
     * @param obj  POJO instance
     */
    public static Object getInstanceProperty(String property, Object obj) {
        String mName = "get" + Character.toUpperCase(property.charAt(0))
                       + property.substring(1);
        return invoke(obj, mName, new Object[]{});
    }

    /**
     * Find and invoke the setter method with the name of form setXXX passing in
     * the value given on the POJO object
     *
     * @param name name of the setter field
     * @param val  value to be set
     * @param obj  POJO instance
     */
    @SuppressWarnings("unchecked")
    public static Object invoke(Object obj, String methodName, Object... params) {

        Object result = null;
        try {

            if (methodName != null) {

                Class[] paramTypes = new Class[params.length];
                int index = 0;
                for (Object param : params) {
                    paramTypes[index++] = param.getClass();
                }
                Method method = obj.getClass()
                        .getMethod(methodName, paramTypes);
                result = method.invoke(obj, params);
            } else {
                handleException("Did not find a method named : " + methodName
                                + "() that takes a the specified parameters");
            }
        } catch (Exception e) {
            handleException("Error invoking method named : " + methodName
                            + "() that takes a the specified parameters", e);
        }

        return result;
    }

    private static void handleException(String message, Throwable e) {
        log.error(message + e.getMessage());
        throw new SynapseException(message, e);
    }

    private static void handleException(String message) {
        log.error(message);
        throw new SynapseException(message);
    }

    public static Object newInstance(String clazzName) {
        Object newInstace = null;
        try {
            newInstace = Class.forName(clazzName).newInstance();
        } catch (Exception e) {
            handleException("Error creating new instace of type: " + clazzName,
                            e);
        }
        return newInstace;
    }
}
