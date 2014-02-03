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
/**
 *
 */
package org.wso2.carbon.business.messaging.salesforce.core;

import org.wso2.carbon.business.messaging.salesforce.stub.*;

import java.rmi.RemoteException;
import java.util.Calendar;

/**
 * This interface provides the Java API to call the Salesforce Proxy services.
 *
 * @see org.wso2.carbon.business.messaging.salesforce.core.impl.SalesforceProxyImpl
 */
public interface SalesforceProxy {

    /**
     * Method signature Login to the Salesforce.com SOAP Api
     *
     * @param username         the username to login to the salesforce account.
     * @param password         the password to login to the salesforce account.
     * @param loginScopeHeader the login scope header.
     * @throws org.wso2.carbon.business.messaging.salesforce.stub.InvalidIdFault
     *          :
     * @throws org.wso2.carbon.business.messaging.salesforce.stub.UnexpectedErrorFault
     *          :
     * @throws org.wso2.carbon.business.messaging.salesforce.stub.LoginFault
     *          :
     */
    public boolean login(

            java.lang.String username, java.lang.String password) throws java.rmi.RemoteException,
                                                                         org.wso2.carbon.business.messaging.salesforce.stub.InvalidIdFault,
                                                                         org.wso2.carbon.business.messaging.salesforce.stub.UnexpectedErrorFault, org.wso2.carbon.business.messaging.salesforce.stub.LoginFault, Exception;

    /**
     * Method signature Create a set of new sObjects
     *
     * @param sObjects             the set of objects to be created.
     * @param sessionHeader        the session header.
     * @param assignmentRuleHeader the assignment rule header.
     * @param mruHeader            the mru header.
     * @param emailHeader          the email header
     * @throws org.wso2.carbon.business.messaging.salesforce.stub.InvalidSObjectFault
     *          :
     * @throws org.wso2.carbon.business.messaging.salesforce.stub.InvalidIdFault
     *          :
     * @throws org.wso2.carbon.business.messaging.salesforce.stub.UnexpectedErrorFault
     *          :
     */
    public org.wso2.carbon.business.messaging.salesforce.stub.SaveResult[] create(
            org.wso2.carbon.business.messaging.salesforce.stub.sobject.SObject[] sObjects) throws RemoteException,
                                                                          InvalidSObjectFault,
                                                                          InvalidIdFault,
                                                                          UnexpectedErrorFault,
                                                                          InvalidFieldFault;

    /**
     * This Method responsible for doing upsert operation on Salesforce objects
     * @param externalId field name that is used as the external id
     * @param sObjects
     * @return
     * @throws InvalidFieldFault
     * @throws RemoteException
     * @throws InvalidSObjectFault
     * @throws InvalidIdFault
     * @throws UnexpectedErrorFault
     * @throws InvalidFieldFault
     */
    public UpsertResult[] upsert(String externalId ,
                      org.wso2.carbon.business.messaging.salesforce.stub.sobject.SObject[] sObjects)
            throws InvalidFieldFault , RemoteException,InvalidSObjectFault,InvalidIdFault,
            UnexpectedErrorFault,InvalidFieldFault;


     /**
     * This Method responsible for doing update operation on Salesforce objects
     * @param sObjects
     * @return an array of SaveResult objects. Each element in the SaveResult array corresponds
      * to the sObject[] array passed as the sObjects parameter in the update() call. For example, the object returned in the first index in the SaveResult array matches the object specified in the first index of the sObject[] array.
     * @throws InvalidFieldFault
     * @throws RemoteException
     * @throws InvalidSObjectFault
     * @throws InvalidIdFault
     * @throws UnexpectedErrorFault
     * @throws InvalidFieldFault
     */
    public SaveResult[] update(org.wso2.carbon.business.messaging.salesforce.stub.sobject.SObject[] sObjects)
            throws InvalidFieldFault , RemoteException,InvalidSObjectFault,InvalidIdFault,
            UnexpectedErrorFault,InvalidFieldFault;
    /**
     * This Method responsible for doing getUpdated Operation for a Salesforce Object.
     * @param sObjectType  SObject type we are interested in
     * @param startDate start date for the getUpdated specified time span
     * @param endDate  end date  for the getUpdated specified time span
     * @return object containing the ID of each created or updated object and the date/time
     * @throws InvalidSObjectFault
     * @throws UnexpectedErrorFault
     * @throws RemoteException
     */
    public GetUpdatedResult getUpdated(String sObjectType, Calendar startDate, Calendar endDate) throws InvalidSObjectFault, UnexpectedErrorFault, RemoteException;


    /**
     * This Method responsible for doing getDeleted Operation for a Salesforce Object
     * @param sObjectType SObject type we are interested in
     * @param startDate start date for the getUpdated specified time span
     * @param endDate  end date  for the getUpdated specified time span
     * @return object containing the ID of each deleted object and the date/time
     * @throws InvalidSObjectFault
     * @throws UnexpectedErrorFault
     * @throws RemoteException
     */
    public GetDeletedResult getDeleted(String sObjectType , Calendar startDate , Calendar endDate) throws InvalidSObjectFault, UnexpectedErrorFault, RemoteException;
    /**
     * Method signature Create a Query Cursor
     *
     * @param queryString   the query string to execute.
     * @param sessionHeader the session header information.
     * @param queryOptions  the query option information
     * @param mruHeader     the mru header information.
     * @throws org.wso2.carbon.business.messaging.salesforce.stub.InvalidSObjectFault
     *          :
     * @throws org.wso2.carbon.business.messaging.salesforce.stub.MalformedQueryFault
     *          :
     * @throws org.wso2.carbon.business.messaging.salesforce.stub.InvalidFieldFault
     *          :
     * @throws org.wso2.carbon.business.messaging.salesforce.stub.UnexpectedErrorFault
     *          :
     */
    public org.wso2.carbon.business.messaging.salesforce.stub.QueryResult query(

            java.lang.String queryString) throws java.rmi.RemoteException,
                                                 org.wso2.carbon.business.messaging.salesforce.stub.InvalidSObjectFault,
                                                 org.wso2.carbon.business.messaging.salesforce.stub.MalformedQueryFault,
                                                 org.wso2.carbon.business.messaging.salesforce.stub.InvalidFieldFault,
                                                 org.wso2.carbon.business.messaging.salesforce.stub.UnexpectedErrorFault, InvalidIdFault, InvalidQueryLocatorFault;

    /**
     * Method signature Logout from the Salesforce.com SOAP Api
     */
    public boolean logout() throws UnexpectedErrorFault, RemoteException;

    public GetUserInfoResult getUserInfo();
}
