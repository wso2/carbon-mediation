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
package org.wso2.carbon.business.messaging.paypal.integration;

import com.paypal.sdk.exceptions.PayPalException;
import com.paypal.sdk.profiles.APIProfile;
import com.paypal.sdk.profiles.ProfileFactory;
import com.paypal.soap.api.AbstractRequestType;

public abstract class AbstractProxy<T> {
    protected String operation;
    protected APIProfile profile;
    protected String version = "51.0";
    protected String environment = "sandbox";

    public AbstractProxy(String apiUsername, String apiPassword, String apiSignature) throws PayPalException {
        profile = ProfileFactory.createSignatureAPIProfile();
        profile.setAPIUsername(apiUsername);
        profile.setAPIPassword(apiPassword);
        profile.setSignature(apiSignature);
        profile.setEnvironment(environment);
    }

    public AbstractProxy(APIProfile profile) throws PayPalException {
        this.profile = profile;
    }

    public abstract T call(String operation, AbstractRequestType request) throws PayPalException;

    public abstract T call(String operation) throws PayPalException;

    /**
     * set Paypal service version
     *
     * @param version
     */
    public void setAPIVersion(String version) {
        this.version = version;
    }

    /**
     * set Environment for this api call
     *
     * @param env string representation for the envitonemnt ie:-"sandbox"
     */
    public void setEnvironment(String env) {
        this.environment = env;
        profile.setEnvironment(environment);
    }
}
