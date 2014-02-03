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
package org.wso2.carbon.business.messaging.salesforce.core.impl;

import java.rmi.RemoteException;
import java.util.Calendar;

import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.business.messaging.salesforce.stub.*;
import org.wso2.carbon.business.messaging.salesforce.stub.fault.ExceptionCode;
import org.wso2.carbon.business.messaging.salesforce.stub.sobject.Account;
import org.wso2.carbon.business.messaging.salesforce.stub.sobject.Contact;
import org.wso2.carbon.business.messaging.salesforce.stub.sobject.SObject;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.business.messaging.salesforce.core.SalesforceProxy;
import org.wso2.carbon.business.messaging.salesforce.core.session.SalesforceSession;
import org.wso2.carbon.business.messaging.salesforce.core.session.SessionManager;

import javax.xml.bind.DatatypeConverter;

/**
 * This provides the implementation for the Java API to call the Salesforce
 * Proxy services.
 *
 * @see org.wso2.carbon.business.messaging.salesforce.core.SalesforceProxy
 */
public class SalesforceProxyImpl implements SalesforceProxy {

    private static final Log log = LogFactory.getLog(SalesforceProxyImpl.class);
    /**
     * Holds the reference to the actual <code>SforceServiceStub</code> stub
     */


    /**
     * Holds the reference to the client repository configuration.
     */
    private ConfigurationContext cfgCtx;

    /**
     * Holds the logged in state of the current user.
     */
    private boolean loggedIn = false;

    /**
     * Holds the login information for the session.
     */
    private LoginResult loginResult;

    private SalesforceSession bindingSession;
    private static final int DEFAULT_QUERY_SIZE = 200;


    public static final String ERROR_MESSAGE = "SFDC_ERROR_MESSAGE";

    /**
     * Constructor to accept the client's configuration context.
     *
     * @param configurationContext the configuration context.
     */
    public SalesforceProxyImpl(ConfigurationContext configurationContext) {
        cfgCtx = configurationContext;
    }

    public SalesforceProxyImpl(ConfigurationContext configurationContext, SalesforceSession session) {
        cfgCtx = configurationContext;
        setBindingSession(session);
    }


    /**
     * Method signature Create a set of new sObjects
     *
     * @param sObjects the set of objects to be created.
     * @throws RemoteException      Error in remote connection. connection failed
     * @throws InvalidSObjectFault  An invalid sObject in a describeSObject(), describeSObjects(),describeLayout(),
     *                              describeDataCategoryGroups(), describeDataCategoryGroupStructures(), create(),
     *                              update(), retrieve(), or query()  call.
     * @throws InvalidIdFault       A specified ID was invalid in a setPassword()  or resetPassword()  call.
     * @throws UnexpectedErrorFault An unexpected error occurred. The error is not associated with any other API fault.
     * @throws InvalidFieldFault    An invalid field in a retrieve()  or query()  call.
     *                              :
     */
    public SaveResult[] create(SObject[] sObjects) throws RemoteException, InvalidSObjectFault,
            InvalidIdFault, UnexpectedErrorFault,
            InvalidFieldFault {
        MessageContext currentMsg = MessageContext.getCurrentMessageContext();
        MessageContext.setCurrentMessageContext(null);
        if (loggedIn) {
            SaveResult[] results = null;
            try {
                SalesforceSession session = SessionManager.getManager().getSalesforceSession(getBindingSession().getId());
                SforceServiceStub binding = null;
                SessionHeader sh;
                QueryResult qr;
                if (session != null) {
                    synchronized (session) {
                        //check if not removed already
                        if (SessionManager.getManager().getSalesforceSession(session.getId()) != null) {
                            binding = createAuthServiceInstance(session.getAuthSalsesforceSession());
                            sh = getAuthHeader(session.getAuthSalsesforceSession());
                            results = binding.create(sObjects, sh,null, null, null, null, null, null, null);
                        } else {
                            //session removed/expired
                            //TODO handle this ? retry / ignore
                            return null;
                        }
                    }
                } else {
                    //session removed/expired
                    //TODO handle this ? retry / ignore
                }

            } catch (InvalidSObjectFault e) {
                String msg = e.getFaultMessage().getInvalidSObjectFault().getExceptionMessage();
                log.error("Invalid Sobject Fault  while invoking create operation : " + msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;
            } catch (UnexpectedErrorFault e) {
                String msg = e.getFaultMessage().getUnexpectedErrorFault().getExceptionMessage();
                log.error("Unexpected Error while invoking login Operation : " + msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;
            } catch (InvalidIdFault e) {
                String msg = e.getFaultMessage().getInvalidIdFault().getExceptionMessage();
                log.error("InvalidIdFault while invoking create Operation : " +
                        msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;
            } catch (RemoteException e) {
                String msg = e.getMessage();
                log.error("Remote exception encountered while invoking create Operation :"
                        + e.getMessage());
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;
            }

            return results;

        }

        return null;
    }


     public GetDeletedResult getDeleted(String sObjectType, Calendar startDate, Calendar endDate)
            throws InvalidSObjectFault, UnexpectedErrorFault, RemoteException {
        MessageContext currentMsg = MessageContext.getCurrentMessageContext();
        MessageContext.setCurrentMessageContext(null);
        if (loggedIn) {
            GetDeletedResult results = null;
            try {
                SalesforceSession session =
                        SessionManager.getManager().getSalesforceSession(
                                getBindingSession().getId());
                SforceServiceStub binding = null;
                SessionHeader sh;


                if (session != null) {
                    synchronized (session) {
                        //check if not removed already
                        if (SessionManager.getManager().getSalesforceSession(session.getId())
                                != null) {
                            binding = createAuthServiceInstance(session.getAuthSalsesforceSession());
                            sh = getAuthHeader(session.getAuthSalsesforceSession());

                            results = binding.getDeleted(sObjectType, startDate, endDate, sh);

                        } else {
                            //session removed/expired
                            //TODO handle this ? retry / ignore
                            return null;
                        }
                    }
                } else {
                    //session removed/expired
                    //TODO handle this ? retry / ignore
                }

            } catch (InvalidSObjectFault e) {
                String msg = e.getFaultMessage().getInvalidSObjectFault().
                        getExceptionMessage();
                log.error("Invalid object exceptionSaveResult[ encountered:\n\n" +
                        msg);
                removeSession();
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                throw e;

            } catch (UnexpectedErrorFault e) {
                String msg = e.getFaultMessage().getUnexpectedErrorFault().getExceptionMessage();
                log.error("Unexpected error exception encountered:\n\n" +
                        msg);
                removeSession();
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                throw e;
            } catch (RemoteException e) {
                String msg = e.getMessage();
                log.error("Remote exception encountered:\n\n" +
                        msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;
            }

            return results;

        }

        return null;
    }

    /**
     * This method is responsible for creating an Account Collection on salesforce.com
     *
     * @param sObjects account objects to be created
     * @return objects representing resut of create operation
     * @throws RemoteException      Error in remote connection. connection failed
     * @throws InvalidSObjectFault  An invalid sObject in a describeSObject(), describeSObjects(),describeLayout(),
     *                              describeDataCategoryGroups(), describeDataCategoryGroupStructures(), create(),
     *                              update(), retrieve(), or query()  call.
     * @throws InvalidIdFault       A specified ID was invalid in a setPassword()  or resetPassword()  call.
     * @throws UnexpectedErrorFault An unexpected error occurred. The error is not associated with any other API fault.
     * @throws InvalidFieldFault    An invalid field in a retrieve()  or query()  call.
     */
    public UpsertResult[] upsert(String externalIDFieldName, SObject[] sObjects)
            throws org.wso2.carbon.business.messaging.salesforce.stub.InvalidFieldFault,
            InvalidSObjectFault, UnexpectedErrorFault, InvalidIdFault, RemoteException {

        MessageContext currentMsg = MessageContext.getCurrentMessageContext();
        MessageContext.setCurrentMessageContext(null);
        if (loggedIn) {
            UpsertResult[] results = null;
            try {
                SalesforceSession session = SessionManager.getManager().getSalesforceSession(getBindingSession().getId());
                SforceServiceStub binding = null;
                SessionHeader sh;
                if (session != null) {
                    synchronized (session) {
                        //check if not removed already
                        if (SessionManager.getManager().getSalesforceSession(session.getId())
                                != null) {
                            binding = createAuthServiceInstance(session.getAuthSalsesforceSession());
                            sh = getAuthHeader(session.getAuthSalsesforceSession());
                            results = binding.upsert(externalIDFieldName, sObjects, sh, null, null, null,
                                    null, null, null, null);
                        } else {
                            //session removed/expired
                            //TODO handle this ? retry / ignore
                            return null;
                        }
                    }
                } else {
                    //session removed/expired
                    //TODO handle this ? retry / ignore
                }

            } catch (InvalidSObjectFault e) {
                String msg = e.getFaultMessage().getInvalidSObjectFault().getExceptionMessage();
                log.error("Invalid object exceptionSaveResult[ encountered:\n\n" +
                        e.getFaultMessage().getInvalidSObjectFault().getExceptionMessage());
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;
            } catch (UnexpectedErrorFault e) {
                String msg = e.getFaultMessage().getUnexpectedErrorFault().getExceptionMessage();
                log.error("Unexpected error exception encountered:\n\n" +
                        msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;
            } catch (InvalidIdFault e) {
                String msg = e.getFaultMessage().getInvalidIdFault().getExceptionMessage();
                log.error("Invalid Id exception encountered:\n\n" +
                        msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;
            } catch (RemoteException e) {
                String msg = e.getMessage();
                log.error("Remote exception encountered:\n\n" +
                        msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;
            } catch (InvalidFieldFault e) {
                String msg = e.getFaultMessage().getInvalidFieldFault().getExceptionMessage();
                log.error("Invalid Id Fault:\n\n" +
                        msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;
            }

            return results;

        }

        return null;
    }


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
    public SaveResult[] update(SObject[] sObjects)
            throws InvalidFieldFault, RemoteException, InvalidSObjectFault,
            InvalidIdFault, UnexpectedErrorFault, InvalidFieldFault {

    MessageContext currentMsg = MessageContext.getCurrentMessageContext();
        MessageContext.setCurrentMessageContext(null);
        if (loggedIn) {
            SaveResult[] results = null;
            try {
                SalesforceSession session = SessionManager.getManager().getSalesforceSession(getBindingSession().getId());
                SforceServiceStub binding = null;
                SessionHeader sh;
                if (session != null) {
                    synchronized (session) {
                        //check if not removed already
                        if (SessionManager.getManager().getSalesforceSession(session.getId())
                                != null) {
                            binding = createAuthServiceInstance(session.getAuthSalsesforceSession());
                            sh = getAuthHeader(session.getAuthSalsesforceSession());
                            results = binding.update(sObjects, sh, null, null, null,
                                    null, null, null, null);
                        } else {
                            //session removed/expired
                            //TODO handle this ? retry / ignore
                            return null;
                        }
                    }
                } else {
                    //session removed/expired
                    //TODO handle this ? retry / ignore
                }

            } catch (InvalidSObjectFault e) {
                String msg = e.getFaultMessage().getInvalidSObjectFault().getExceptionMessage();
                log.error("Invalid object exceptionSaveResult[ encountered:\n\n" +
                        e.getFaultMessage().getInvalidSObjectFault().getExceptionMessage());
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;
            } catch (UnexpectedErrorFault e) {
                String msg = e.getFaultMessage().getUnexpectedErrorFault().getExceptionMessage();
                log.error("Unexpected error exception encountered:\n\n" +
                        msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;
            } catch (InvalidIdFault e) {
                String msg = e.getFaultMessage().getInvalidIdFault().getExceptionMessage();
                log.error("Invalid Id exception encountered:\n\n" +
                        msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;
            } catch (RemoteException e) {
                String msg = e.getMessage();
                log.error("Remote exception encountered:\n\n" +
                        msg);
                removeSession();
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                throw e;
            } catch (InvalidFieldFault e) {
                String msg = e.getFaultMessage().getInvalidFieldFault().getExceptionMessage();
                log.error("Invalid Id Fault:\n\n" +
                        msg);
                removeSession();
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                throw e;
            }

            return results;

        }

        return null;

    }

    /**
     * This method is responsible for getting the Updated sObjects between given data and time
     *
     * @param sObjectType Object type we are looking for
     * @param startDate   Start Data and time to lookup for updates
     * @param endDate     End Date and time to lookup for updates
     * @return Results of the getUpdated method invocation. In successful invocation it will return
     *         set of ids that are  updated between given date and time.
     */
    public GetUpdatedResult getUpdated(String sObjectType, Calendar startDate, Calendar endDate)
            throws InvalidSObjectFault, UnexpectedErrorFault, RemoteException {

        MessageContext currentMsg = MessageContext.getCurrentMessageContext();
        MessageContext.setCurrentMessageContext(null);
        if (loggedIn) {
            GetUpdatedResult results = null;
            try {
                SalesforceSession session =
                        SessionManager.getManager().getSalesforceSession(
                                getBindingSession().getId());
                SforceServiceStub binding = null;
                SessionHeader sh;


                if (session != null) {
                    synchronized (session) {
                        //check if not removed already
                        if (SessionManager.getManager().getSalesforceSession(session.getId())
                                != null) {
                            binding = createAuthServiceInstance(session.getAuthSalsesforceSession());
                            sh = getAuthHeader(session.getAuthSalsesforceSession());

                            results = binding.getUpdated(sObjectType, startDate, endDate, sh);

                        } else {
                            //session removed/expired

                            return null;
                        }
                    }
                } else {

                }

            } catch (InvalidSObjectFault e) {
                String msg = e.getFaultMessage().getInvalidSObjectFault().
                        getExceptionMessage();
                log.error("Invalid object exceptionSaveResult[ encountered:\n\n" +
                        msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;

            } catch (UnexpectedErrorFault e) {
                String msg = e.getFaultMessage().getUnexpectedErrorFault().getExceptionMessage();
                log.error("Unexpected error exception encountered:\n\n" +
                        msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw e;
            } catch (RemoteException e) {
                String msg = e.getMessage();
                log.error("Remote exception encountered:\n\n" +
                        msg);
                removeSession();
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                throw e;
            }

            return results;

        }

        return null;

    }

    /**
     * This method is reponsible for creating an Account Collection on salesforce.com
     *
     * @param sObjects account objects to be created
     * @return objects representing resut of create operation
     * @throws RemoteException      Error in remote connection. connection failed
     * @throws InvalidSObjectFault  An invalid sObject in a describeSObject(), describeSObjects(),describeLayout(),
     *                              describeDataCategoryGroups(), describeDataCategoryGroupStructures(), create(),
     *                              update(), retrieve(), or query()  call.
     * @throws InvalidIdFault       A specified ID was invalid in a setPassword()  or resetPassword()  call.
     * @throws UnexpectedErrorFault An unexpected error occurred. The error is not associated with any other API fault.
     * @throws InvalidFieldFault    An invalid field in a retrieve()  or query()  call.
     */
    public SaveResult[] createAccount(Account[] sObjects) throws RemoteException, InvalidSObjectFault,
            InvalidIdFault, UnexpectedErrorFault,
            InvalidFieldFault {
        return create(sObjects);
    }

    /**
     * Method signature Create a Query Cursor
     *
     * @param queryString the query string to execute salesforce.com query.
     * @throws com.sforce.soap.enterprise.MalformedQueryFault
     *                             A problem in the queryString passed in a query()  call.
     * @throws RemoteException     Error in remote connection. connection failed
     * @throws InvalidSObjectFault An invalid sObject in a describeSObject(), describeSObjects(),describeLayout(),
     *                             describeDataCategoryGroups(), describeDataCategoryGroupStructures(), create(),
     *                             update(), retrieve(), or query()  call.
     * @throws com.sforce.soap.enterprise.InvalidFieldFault
     *                             An invalid field in a retrieve()  or query()  call.
     * @throws com.sforce.soap.enterprise.UnexpectedErrorFault
     *                             An unexpected error occurred. The error is not associated with any other API fault.
     */
    public QueryResult query(String queryString) throws RemoteException, InvalidSObjectFault,
            MalformedQueryFault, InvalidFieldFault,
            UnexpectedErrorFault, InvalidIdFault,
            InvalidQueryLocatorFault {

        MessageContext currentMsg = MessageContext.getCurrentMessageContext();
        MessageContext.setCurrentMessageContext(null);
        if (loggedIn) {
            try {
                SalesforceSession session = SessionManager.getManager().getSalesforceSession(getBindingSession().getId());
                SforceServiceStub binding = null;
                SessionHeader sh = null;
                QueryOptions qo = null;
                QueryResult qr = null;
                if (session != null) {
                    synchronized (session) {
                        //check if not removed already
                        if (SessionManager.getManager().getSalesforceSession(session.getId()) != null) {
                            binding = createAuthServiceInstance(session.getAuthSalsesforceSession());
                            sh = getAuthHeader(session.getAuthSalsesforceSession());
                            qo = new QueryOptions();
                            qo.setBatchSize(DEFAULT_QUERY_SIZE);
                            qr = binding.query(queryString, sh, qo, null, null);
                        } else {
                            return null;
                        }
                    }
                } else {
                    //TODO handle this ? retry / ignore
                }

//                QueryResult qr = binding.query("select FirstName, LastName from Contact", sh, qo,
//                                               null, null);
                if (log.isDebugEnabled()) {
                    if (qr.getSize() > 0) {
                        log.debug("Logged in user can see " + qr.getRecords().length +
                                " contact records. ");
                        do {
                            for (int i = 0; i < qr.getRecords().length; i++) {
                                Contact con = (Contact) qr.getRecords()[i];
                                String fName = con.getFirstName();
                                String lName = con.getLastName();
                                String msgStr;
                                if (fName == null) {
                                    msgStr = "Contact " + (i + 1) + ": " + lName;
                                    log.debug(msgStr);
                                } else {
                                    msgStr = "Contact " + (i + 1) + ": " + fName + " " + lName;
                                    log.debug(msgStr);
                                }
                            }
                            if (!qr.getDone()) {
                                qr = binding.queryMore(qr.getQueryLocator(), sh, qo);
                            } else {
                                break;
                            }

                        } while (qr.getSize() > 0);
                    } else {
                        log.debug("No records found.");
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug("Query succesfully executed....");
                }
                return qr;
            } catch (RemoteException ex) {
                String msg = ex.getMessage();
                log.error("\nFailed to execute query succesfully, error message was:" +
                        "\n" + ex.getMessage());
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                checkSessionValidity(ex);
                removeSession();
                throw ex;
            } catch (UnexpectedErrorFault unexpectedErrorFault) {
                String msg = unexpectedErrorFault.getFaultMessage().getUnexpectedErrorFault().
                        getExceptionMessage();
                log.error("Unexpected Error while Executing query " + queryString + " : "
                        + msg);
                checkSessionValidity(unexpectedErrorFault);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw unexpectedErrorFault;
            } catch (InvalidSObjectFault invalidSObjectFault) {
                String msg = invalidSObjectFault.getFaultMessage().getInvalidSObjectFault().
                                getExceptionMessage();
                log.error("Invalid Sobject Fault while Executing query: " + queryString + " : " +
                        msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw invalidSObjectFault;
            } catch (InvalidIdFault invalidIdFault) {
                String msg = invalidIdFault.getFaultMessage().
                        getInvalidIdFault().getExceptionMessage();
                log.error("InvalidIdFault  while executing Query: " + queryString + " : " +
                        msg);
                removeSession();
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                throw invalidIdFault;
            } catch (InvalidQueryLocatorFault invalidQueryLocatorFault) {

                String msg = invalidQueryLocatorFault.getFaultMessage().getInvalidQueryLocatorFault().
                                getExceptionMessage();
                log.error("InvalidQueryLocatorFault   while executing Query: " + queryString + " : "
                        + msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw invalidQueryLocatorFault;
            } catch (InvalidFieldFault invalidFieldFault) {
                String msg = invalidFieldFault.getFaultMessage().getInvalidFieldFault().
                                getExceptionMessage();
                log.error("InvalidFieldFault   while executing Query: " + queryString + " : " +
                        msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw invalidFieldFault;
            } catch (MalformedQueryFault malformedQueryFault) {

                String msg = malformedQueryFault.getFaultMessage().getMalformedQueryFault().
                                getExceptionMessage();
                log.error("MalformedQueryFault    while executing Query: " + queryString + " : " +
                        msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
                removeSession();
                throw malformedQueryFault;
            }
        }

        return null;

    }

    private boolean checkSessionValidity(Exception ex) {
        boolean isSessionInvalid = false;
        ExceptionCode exCode = ExceptionCode.Factory.fromValue(ex.getMessage());
        if (exCode == ExceptionCode.INVALID_SESSION_ID) {
            removeSession();
            isSessionInvalid = true;
        }
        return isSessionInvalid;
    }

    /**
     * @return logout success or not
     * @throws UnexpectedErrorFault
     * @throws RemoteException
     */
    public boolean logout() throws UnexpectedErrorFault, RemoteException {

        try {
            SalesforceSession session = SessionManager.getManager().getSalesforceSession(getBindingSession().getId());
            SforceServiceStub binding = null;
            SessionHeader sh;
            if (session != null) {
                synchronized (session) {
                    //check if not removed already
                    if (SessionManager.getManager().getSalesforceSession(session.getId()) != null) {
                        binding = createAuthServiceInstance(session.getAuthSalsesforceSession());
                        sh = getAuthHeader(session.getAuthSalsesforceSession());
                        binding.logout(sh);
                        removeSession();
                    } else {
                        return false;
                    }
                }
            } else {
                //TODO handle this ? retry / ignore
            }
            if (log.isDebugEnabled()) {
                log.debug("Logout Success");
            }
        } catch (AxisFault axisFault) {
            if (log.isErrorEnabled()) {
                log.error("Logout Error", axisFault);
            }
            return false;
        } catch (RemoteException e) {
            if (log.isErrorEnabled()) {
                log.error("Logout Error", e);
            }
            return false;
        } catch (UnexpectedErrorFault unexpectedErrorFault) {
            if (log.isErrorEnabled()) {
                log.error("Logout Error", unexpectedErrorFault);
            }
            return false;
        }
        loggedIn = false;

        return true;
    }

    /**
     * @param username the username to login to the salesforce account.
     * @param password the password to login to the salesforce account.
     * @return
     */
    public boolean login(String username, String password) throws Exception{
        loginResult = null;
        SforceServiceStub binding = null;
        MessageContext currentMsg = MessageContext.getCurrentMessageContext();
        MessageContext.setCurrentMessageContext(null);

        try {
            //check globally available sessions
            SalesforceSession session = SessionManager.createNewSession(username, password, cfgCtx);
            //check if oldSession exist
            SalesforceSession oldSession = SessionManager.getManager().getSalesforceSession(session.getId());
            if (oldSession != null) {
                synchronized (oldSession) {
                    //check if sessions has been removed
                    if (SessionManager.getManager().getSalesforceSession(oldSession.getId()) != null) {
                        setBindingSession(oldSession);
                        loginWithSession(oldSession);
                    } else {
                        //if already removed use new session
                        SessionManager.getManager().addSalesforceSession(session.getId(), session);
                        synchronized (session) {
                            setBindingSession(session);
                            loginWithSession(session);
                        }
                    }
                }
            } else {
                SessionManager.getManager().addSalesforceSession(session.getId(), session);
                synchronized (session) {
                    //new session
                    setBindingSession(session);
                    loginWithSession(session);
                }
            }

        } catch (LoginFault ex) {
            // The LoginFault derives from AxisFault
            removeSession();

            String msg = ex.getFaultMessage().getLoginFault().getExceptionMessage();
            log.error("An unexpected error has occurred." + msg);
            currentMsg.setProperty(ERROR_MESSAGE,msg);
            loggedIn = false;

            throw ex;
        } catch (RemoteException ex) {
            //remove and cleanup session info
            removeSession();
            String msg = ex.getMessage();
            log.error("An unexpected error has occurred." + msg);
            currentMsg.setProperty(ERROR_MESSAGE, msg);
            loggedIn = false;


            throw ex;
        } catch (UnexpectedErrorFault e) {

            removeSession();
            String msg = e.getFaultMessage().getUnexpectedErrorFault().getExceptionMessage();
            log.error("An unexpected error has occurred." + msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
            loggedIn = false;

            currentMsg.setProperty(ERROR_MESSAGE,msg);

            throw e;
        } catch (InvalidIdFault e) {
            removeSession();
            String msg = e.getFaultMessage().getInvalidIdFault().getExceptionMessage();
            log.error("An unexpected error has occurred." + msg);
                currentMsg.setProperty(ERROR_MESSAGE, msg);
            loggedIn = false;

            currentMsg.setProperty(ERROR_MESSAGE, msg);

            throw e;
        }
        // Check if the password has expired

        if (loginResult.getPasswordExpired()) {

            if (log.isDebugEnabled()) {
                log.debug("An error has occurred. Your password has expired.");
            }
            loggedIn = false;
            return loggedIn;
        }

        if (log.isDebugEnabled()) {
            //System.out.println("Login was successful.");
            log.info("Salesforce.com API Login was successful.");
            log.debug("\nThe session id is: " + loginResult.getSessionId());
            log.debug("\nThe new server url is: " + loginResult.getServerUrl());
        }

        loggedIn = true;
        return loggedIn;

    }

    private void loginWithSession(SalesforceSession currentSession) throws RemoteException, InvalidIdFault, UnexpectedErrorFault, LoginFault {
        SforceServiceStub binding;
        String username = currentSession.getUsername();
        String password = currentSession.getPassword();
        //skip login if authenticated Salesforce session is present
        if (currentSession.getAuthSalsesforceSession() == null) {
            //    get binding object
            binding = (SforceServiceStub) new SforceServiceStub();
            loginResult = binding.login(username, password, null);
            currentSession.setUsername(username);
            currentSession.setPassword(password);
            currentSession.setAuthSalsesforceSession(loginResult);
        } else {
            log.debug("Already logged in");
            loginResult = currentSession.getAuthSalsesforceSession();
            loggedIn = true;
        }
    }

    public GetUserInfoResult getUserInfo() {
        if (loggedIn) {
            GetUserInfoResult userInfo = null;

            try {
                SalesforceSession session = SessionManager.getManager().getSalesforceSession(getBindingSession().getId());
                SforceServiceStub binding = null;
                SessionHeader sh;
                if (session != null) {
                    synchronized (session) {
                        //check if not removed already
                        if (SessionManager.getManager().getSalesforceSession(session.getId()) != null) {
                            binding = createAuthServiceInstance(session.getAuthSalsesforceSession());
                            sh = getAuthHeader(session.getAuthSalsesforceSession());
                            userInfo = binding.getUserInfo(sh);
                        } else {
                            return null;
                        }
                    }
                } else {

                }

                if (log.isDebugEnabled()) {
                    log.debug("user Info :" + userInfo.getUserFullName());
                }
            } catch (AxisFault axisFault) {
                if (log.isErrorEnabled()) {
                    log.error("Logout Error", axisFault);
                }
            } catch (RemoteException e) {
                if (log.isErrorEnabled()) {
                    log.error("Logout Error", e);
                }
            } catch (UnexpectedErrorFault unexpectedErrorFault) {
                if (log.isErrorEnabled()) {
                    log.error("Logout Error", unexpectedErrorFault);
                }
            }
            return userInfo;
        }
        return null;
    }

    private SforceServiceStub createAuthServiceInstance(LoginResult loginInfo) throws
            AxisFault {
        SforceServiceStub binding = null;
        binding = (SforceServiceStub) new SforceServiceStub(loginInfo.getServerUrl());
        return binding;
    }

    private SessionHeader getAuthHeader(LoginResult loginInfo) {
        SessionHeader sh = new SessionHeader();
        sh.setSessionId(loginInfo.getSessionId());

        return sh;
    }

    public void setLoginState(boolean loginState) {
        loggedIn = loginState;
    }

    public void setAuthSalesforceSession(LoginResult authResult) {
        loginResult = authResult;
    }

    public LoginResult getAuthSalesforceSession() {
        return loginResult;
    }


    public SalesforceSession getBindingSession() {
        return bindingSession;
    }

    public void setBindingSession(SalesforceSession bindingSession) {
        this.bindingSession = bindingSession;
    }


    private void removeSession() {
        SalesforceSession bindedSession = SessionManager.getManager().getSalesforceSession(getBindingSession().getId());
        if (bindedSession != null) {
            synchronized (bindedSession) {
                SessionManager.getManager().removeSession(bindedSession.getId());
                loggedIn = false;
                loginResult = null;
                setBindingSession(null);
            }
        }
    }
}
