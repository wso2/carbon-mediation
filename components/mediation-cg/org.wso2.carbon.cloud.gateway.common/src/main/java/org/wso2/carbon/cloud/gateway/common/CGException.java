/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.cloud.gateway.common;

/**
 * <code>CGException </code> contains the exception class deceleration
 */
public class CGException extends Exception {
    public CGException(String message) {
        super(message);
    }

    public CGException(String message, Throwable t) {
        super(message, t);
    }

    public CGException(Throwable t) {
        super(t);
    }
}
