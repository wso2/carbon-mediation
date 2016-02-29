/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.rest.api;

/**
 * This class represents an exception, which is intended to be thrown when some client code accesses a portion of
 * business logic, i.e method or a block of code, that the it is not authorized or privileged to access.
 */
public class UnauthorizedAccessException extends RuntimeException {

    private static final long serialVersionUID = -7034897190745766941L;

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedAccessException(Throwable cause) {
        super(cause);
    }

    public UnauthorizedAccessException(String message) {
        super(message);
    }

}
