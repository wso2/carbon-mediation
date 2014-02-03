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

import com.paypal.sdk.core.nvp.NVPDecoder;
import com.paypal.sdk.core.nvp.NVPEncoder;
import com.paypal.sdk.exceptions.PayPalException;
import com.paypal.sdk.profiles.APIProfile;
import com.paypal.sdk.services.NVPCallerServices;
import com.paypal.soap.api.AbstractRequestType;
import com.paypal.soap.api.AbstractResponseType;

public class PaypalNVPProxy extends AbstractProxy {
    private NVPCallerServices caller;
    private NVPEncoder encoder;
    private NVPDecoder decoder = new NVPDecoder();

    private PaypalNVPProxy(APIProfile profile) throws PayPalException {
        super(profile);
        caller = new NVPCallerServices();
        caller.setAPIProfile(this.profile);
    }

    private PaypalNVPProxy(String apiUsername, String apiPassword) throws PayPalException {
        this(apiUsername, apiPassword, null);
    }

    private PaypalNVPProxy(String apiUsername, String apiPassword, String apiSignature) throws PayPalException {
        super(apiUsername, apiPassword, apiSignature);
        caller = new NVPCallerServices();
        caller.setAPIProfile(profile);
    }

    @Override
    public String call(String operation) throws PayPalException {
        encoder.add("METHOD", operation);
        //API profile is set when creating the proxy object.
        //Therefore no need to set it here
        //caller.setAPIProfile(profile);
        String NVPRequest = encoder.encode();
        String NVPResponse = (String) caller.call(NVPRequest);
        decoder.decode(NVPResponse);
        return NVPResponse;
    }

    @Override
    public AbstractResponseType call(String operation, AbstractRequestType request){
        return null;    
    }

    public void setEncoder(NVPEncoder encoder) {
        this.encoder = encoder;
        this.encoder.add("VERSION", version);
    }

    public NVPDecoder getDecoder() {
        return decoder;
    }

    /**
     * returns if is this operation a sucess afer a #call()
     *
     * @return true on sucessful operation
     *         false on failure
     */
    public boolean isSuccess() {
        return Boolean.getBoolean(decoder.get("ACK"));
    }


    public static PaypalNVPProxy createPaypalNVPProxy(APIProfile profile)
            throws PayPalException {
        return new PaypalNVPProxy(profile);
    }

    public static PaypalNVPProxy createPaypalNVPProxy(String apiUsername, String apiPassword)
            throws PayPalException {
        return new PaypalNVPProxy(apiUsername, apiPassword);
    }

    public static PaypalNVPProxy createPaypalNVPProxy(String apiUsername,
                                                      String apiPassword, String apiSignature)
            throws PayPalException {
        return new PaypalNVPProxy(apiUsername, apiPassword, apiSignature);
    }
}
