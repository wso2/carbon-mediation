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
package org.wso2.business.messaging.salesforce.mediator.samples.test.factory;

import org.apache.axiom.om.OMElement;
import org.wso2.business.messaging.salesforce.mediator.samples.test.factory.impl.*;

public abstract class PayloadFactory {
    public final static String LOGIN_REQ = "_login";
    public final static String QUERY_REQ = "_query";
    public final static String LOGOUT_REQ = "_logout";
    public final static String GETINFO_REQ = "_get_info";
    public final static String SAMPLE1_REQ = "_sample1";


    public abstract OMElement createPayload(Object... params);

    public static PayloadFactory getInstance(String arg) {
        if (LOGIN_REQ.equals(arg)) {
            return new LoginPayloadFactory();
        } else if (QUERY_REQ.equals(arg)) {
            return new QueryPayloadFactory();
        } else if (LOGOUT_REQ.equals(arg)) {
            return new LogoutPayloadFactory();
        } else if (GETINFO_REQ.equals(arg)) {
            return new GetInfoPayloadFactory();
        } else if (SAMPLE1_REQ.equals(arg)) {
            return new Sample1PayloadFactory();
        }

        return null;
    }
}
