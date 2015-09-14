/*
 * Copyright 2005-2008 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.mediation.throttle;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.text.MessageFormat;

/**
 * Exception class which is used to throw Throttle component specific
 * exceptions.
 */

public class ThrottleComponentException extends Exception {

    private static ResourceBundle resources;

    static {
        try {
            resources = ResourceBundle.getBundle("org.wso2.carbon.mediation.throttle.errors");
        } catch (MissingResourceException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ThrottleComponentException(String errorCode, Object[] args) {
        super(getMessage(errorCode, args));
    }

    public ThrottleComponentException(String errorCode) {
        this(errorCode, (Object[]) null);
    }

    public ThrottleComponentException(String errorCode, Object[] args, Throwable e) {
        super(getMessage(errorCode, args), e);
    }

    public ThrottleComponentException(String errorCode, Throwable e) {
        this(errorCode, null, e);
    }

    /**
     * get the message from resource bundle.
     *
     * @return the message translated from the property (message) file.
     */
    protected static String getMessage(String errorCode, Object[] args) {
        String msg;
        try {
            msg = MessageFormat.format(resources.getString(errorCode), args);
        } catch (MissingResourceException e) {
            throw new RuntimeException("Undefined '" + errorCode + "' resource property");
        }
        return msg;
    }
}
