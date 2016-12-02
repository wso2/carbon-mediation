/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.http2.transport.util;

import org.apache.http.nio.IOControl;

import java.io.IOException;

/**
 * Created by chanakabalasooriya on 11/25/16.
 */
public class Http2CosumerIoControl implements IOControl {

    @Override
    public void requestInput() {

    }

    @Override
    public void suspendInput() {

    }

    @Override
    public void requestOutput() {

    }

    @Override
    public void suspendOutput() {

    }

    @Override
    public void shutdown() throws IOException {

    }
}
