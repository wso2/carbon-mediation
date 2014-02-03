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
import com.paypal.sdk.services.CallerServices;
import com.paypal.soap.api.AbstractRequestType;
import com.paypal.soap.api.AbstractResponseType;
import com.paypal.soap.api.AckCodeType;

public class PaypalSOAPProxy extends AbstractProxy {
    private CallerServices caller;
    private AbstractRequestType request;
    private AbstractResponseType response;

    private PaypalSOAPProxy(APIProfile profile) throws PayPalException {
        super(profile);
        caller = new CallerServices();
        caller.setAPIProfile(profile);
        
    }

    private PaypalSOAPProxy(String apiUsername, String apiPassword) throws PayPalException {
        this(apiUsername, apiPassword, null);
    }

    private PaypalSOAPProxy(String apiUsername, String apiPassword, String apiSignature)
            throws PayPalException {
        super(apiUsername, apiPassword, apiSignature);
        caller = new CallerServices();
        caller.setAPIProfile(profile);
    }

    /**
     * set SOAP request for this operation
     *
     * @param request SOAP request
     */
    private void setRequest(AbstractRequestType request) {
        this.request = request;
        this.request.setVersion(version);
    }

    /**
     * Set the operation name
     * @param operation Operation name e.g. SetExpressCheckout
     */
    private void setOperation(String operation){
        this.operation = operation;
    }

    /**
     * call operation. This has a temporal coupling with #setRequest.(make sure you do setRequest
     * first)
     *
     * @return SOAP response
     * @throws PayPalException error invoking Paypal Service
     */
    public AbstractResponseType call(String operation, AbstractRequestType request) throws PayPalException {
        //setOperation(operation);
        //setRequest(request);
        response = caller.call(operation, request);
        return response;
    }

    public AbstractResponseType call(String operation) throws PayPalException {
        return null;
    }

    /**
     * returns if is this operation a sucess afer a #call()
     *
     * @return true on sucessful operation
     *         false on failure
     */
    public boolean isSuccess() {
        return response != null && response.getAck().equals(AckCodeType.Success) ||
               response.getAck().equals(AckCodeType.SuccessWithWarning);
    }

    /**
     * create paypal proxy
     *
     * @param profile   paypal API profile
     * @return Paypal proxy instance
     * @throws PayPalException if error occurs creating an API profile for this oepration
     */
    public static PaypalSOAPProxy createPaypalSOAPProxy(APIProfile profile) throws
            PayPalException {
        return new PaypalSOAPProxy(profile);
    }

    /**
     * create paypal proxy corresponding to a API credentials
     *
     * @param apiUsername username for API access
     * @param apiPassword password for API access
     * @return Paypal proxy instance
     * @throws PayPalException if error occurs creating an API profile for this oepration
     */
    public static PaypalSOAPProxy createPaypalSOAPProxy(String apiUsername,
                                                        String apiPassword) throws PayPalException {
        return new PaypalSOAPProxy(apiUsername, apiPassword);
    }

    /**
     * create paypal proxy corresponding to API credentials
     *
     * @param apiUsername  username for API access
     * @param apiPassword  password for API access
     * @param apiSignature signature for API access
     * @return Paypal proxy instance
     * @throws PayPalException if error occurs creating an API profile for this oepration
     */
    public static PaypalSOAPProxy createPaypalSOAPProxy(String apiUsername,
                                                        String apiPassword, String apiSignature)
            throws PayPalException {
        return new PaypalSOAPProxy(apiUsername, apiPassword, apiSignature);
    }
}
