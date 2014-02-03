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
package org.wso2.carbon.business.messaging.salesforce.mediator.handler;


import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.wso2.carbon.business.messaging.salesforce.stub.sobject.SObject;
import sun.misc.BASE64Decoder;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PropertyHandler {
    /**
     * Log variable for the logging purposes
     */
    private static final Log log = LogFactory.getLog(PropertyHandler.class);

    private final static String PACKAGE_NAME = "org.wso2.carbon.business.messaging.salesforce.stub";

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
                                        + "or boolean parameter : FieldName may be incorrect , check wsdl");
                    }
                    if (val instanceof String) {
                        String value = (String) val;
                        if (params[0].equals(String.class)) {
                            if(value.equals("")) {
                                value = " ";
                            }
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
                        } else if(params[0].equals(Date.class)) {
                            //To handle xs:date
                            value = value.trim();
                            Date date = DatatypeConverter.parseDate(value).getTime();
                            invoke(obj,mName,new Object[]{date});

                        } else if(params[0].equals(Calendar.class)) {
                            //to handle xs:DateTime
                            Calendar cal = DatatypeConverter.parseDateTime(value.trim());
                            invoke(obj,mName,new Object[]{cal});
                        } else if(params[0].equals(String[].class)) {
                            String[] vales = ((String)val).split(",");
                            invoke(obj,mName,new Object[]{vales});

                        } else if(params[0].equals(DataHandler.class)) {
                            // handle base64 encoded strings
                            value = value.trim();
                            BASE64Decoder decoder = new BASE64Decoder();
                            final byte[] data = decoder.decodeBuffer(value);
                            DataHandler dataHandler = new DataHandler(
                                    new DataSource(){

                                        public InputStream getInputStream() throws IOException {
                                            return new ByteArrayInputStream(data);
                                        }

                                        public OutputStream getOutputStream() throws IOException {
                                            return null;
                                        }

                                        public String getContentType() {
                                            return "base64Binary";
                                        }

                                        public String getName() {
                                            return null;
                                        }
                                    });
                            invoke(obj,mName,new Object[]{dataHandler});
                        }else {
                            handleException("Did not find a setter method named : "
                                            + mName
                                            + "() that takes a single String, int, long, float, double "
                                            + "or boolean parameter : FieldName may be incorrect , check wsdl");
                        }
                    } else if (val instanceof List) {
                        method = obj.getClass().getMethod(mName, List.class);
                        method.invoke(obj, new Object[]{val});
                    } else if (val instanceof SObject[]) {
                        method = obj.getClass().getMethod(mName, SObject[].class);
                        method1.invoke(obj, val);
                    } else {
                        method = obj.getClass().getMethod(mName,val.getClass());
                        if(method != null) {
                            method.invoke(obj,val);
                        }
                    }
                }
            }
        } catch (Exception e) {
            handleException(
                    "Error invoking setter method named : "
                    + mName
                    + "() that takes a single String, int, long, float, double "
                    + "or boolean parameter : FieldName may be incorrect , check wsdl", e);
        }
    }

    /**
     * Find and invoke the setter method with the name of form setXXX passing in
     * the value given on the POJO object
     *
     * @param property name of the setter field
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
     * @param obj  instance that the method will be invoked on
     * @param methodName name of the instance
     * @param params parameters
     */
    @SuppressWarnings("unchecked")
    public static Object invoke(Object obj, String methodName, Object... params) {

        Object result = null;
        try {

            if (methodName != null) {

                Class[] paramTypes = new Class[params.length];
                int index = 0;
                for (Object param : params) {
                    if (param instanceof SObject) {
                        paramTypes[index++] = SObject.class;
                    } else if (param instanceof SObject[]) {
                        paramTypes[index++] = SObject[].class;
                    } else {

                        if(param.getClass().equals(Integer.class)) {
                            paramTypes[index++] = int.class;
                        } else if(param.getClass().equals(Long.class)) {
                            paramTypes[index++] = long.class;
                        } else if(param.getClass().equals(Double.class)) {
                            paramTypes[index++] = double.class;
                        }else if(param.getClass().equals(Float.class)) {
                            paramTypes[index++] = float.class;
                        }else if(param.getClass().equals(Boolean.class)) {
                            paramTypes[index++] = boolean.class;
                        } else if(param.getClass().equals(java.util.GregorianCalendar.class)) {
                            paramTypes[index++] = java.util.Calendar.class;
                        } else  {
                            paramTypes[index++] = param.getClass();
                        }
                    }
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
            newInstace = Class.forName(PACKAGE_NAME + "." + clazzName).newInstance();
        } catch (Exception e) {
            handleException("Error creating new instace of type: " + clazzName,
                            e);
        }
        return newInstace;
    }
}
