package org.wso2.carbon.business.messaging.salesforce.core.test;

import org.wso2.carbon.business.messaging.salesforce.stub.*;
import org.wso2.carbon.business.messaging.salesforce.stub.fault.ExceptionCode;
import org.wso2.carbon.business.messaging.salesforce.stub.sobject.Contact;
import org.apache.axis2.AxisFault;

import java.rmi.RemoteException;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Oct 12, 2010
 * Time: 11:00:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProxyTest {
    private static boolean LOGGED_IN = false;

    public static void main(String[] args) {
        //testLogin();
        //testGetUserInfo();
        //testDescribeGlobal();
        testQuery();
    }

    private static void testLogin() {
        LoginResult result = login();
        testLoginFailed(result);
    }

    private static void testGetUserInfo() {
        LoginResult result = login();
        if (!testLoginFailed(result)) {
            try {
                SforceServiceStub binding = createAuthServiceInstance(result);
                SessionHeader sh = getAuthHeader(result);
                GetUserInfoResult userInfo = binding.getUserInfo(sh);
                System.out.println("user Info :" + userInfo.getUserFullName());
            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (RemoteException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (UnexpectedErrorFault unexpectedErrorFault) {
                unexpectedErrorFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }

    private static void testDescribeGlobal() {
        LoginResult result = login();
        if (!testLoginFailed(result)) {
            try {
                SforceServiceStub binding = createAuthServiceInstance(result);
                SessionHeader sh = getAuthHeader(result);

                DescribeGlobalResult describeGlobalResult = null;
                describeGlobalResult = binding.describeGlobal(sh, null);
                DescribeGlobalSObjectResult[] sobjectResults = describeGlobalResult.getSobjects();
                for (int i = 0; i < sobjectResults.length; i++) {
                    System.out.println(sobjectResults[i].getName());
                }

            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (RemoteException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (UnexpectedErrorFault unexpectedErrorFault) {
                unexpectedErrorFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }

    public static void testQuery() {
        LoginResult result = login();

        if (!testLoginFailed(result)) {
            try {
                SforceServiceStub binding = createAuthServiceInstance(result);
                SessionHeader sh = getAuthHeader(result);
                QueryOptions qo = new QueryOptions();
                qo.setBatchSize(200);
                QueryResult qr = binding.query("select FirstName, LastName from Contact", sh, qo,
                                               null, null);

                if (qr.getSize() > 0) {
                    System.out.println("Logged in user can see " + qr.getRecords().length +
                                       " contact records. ");
                    do {
                        for (int i = 0; i < qr.getRecords().length; i++) {
                            Contact con = (Contact) qr.getRecords()[i];
                            String fName = con.getFirstName();
                            String lName = con.getLastName();
                            if (fName == null) {
                                System.out.println("Contact " + (i + 1) + ": " + lName);
                            } else {
                                System.out.println("Contact " + (i + 1) + ": " + fName + " " + lName);
                            }
                        }
                        if (!qr.getDone()) {
                            qr = binding.queryMore(qr.getQueryLocator(), sh, qo);
                        } else {
                            break;
                        }

                    } while (qr.getSize() > 0);
                } else {
                    System.out.println("No records found.");
                }

                System.out.println("Query succesfully executed....");
            }
            catch (RemoteException ex) {
                System.out.println("\nFailed to execute query succesfully, error message was:" +
                                   "\n" + ex.getMessage());
            }
            catch (UnexpectedErrorFault unexpectedErrorFault) {
                unexpectedErrorFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvalidSObjectFault invalidSObjectFault) {
                invalidSObjectFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvalidIdFault invalidIdFault) {
                invalidIdFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvalidQueryLocatorFault invalidQueryLocatorFault) {
                invalidQueryLocatorFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvalidFieldFault invalidFieldFault) {
                invalidFieldFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (MalformedQueryFault malformedQueryFault) {
                malformedQueryFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }

    public static boolean testLoginFailed(LoginResult result) {
        if (result == null) {
            System.out.println("Login failed!!!!!!!!");
            return true;
        }

        return false;
    }

    private static void testLogout() {
        LoginResult result = login();
        if (!testLoginFailed(result)) {
            try {
                SforceServiceStub binding = createAuthServiceInstance(result);
                SessionHeader sh = getAuthHeader(result);
                binding.logout(sh);
                System.out.println("Logout Success");
            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (RemoteException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (UnexpectedErrorFault unexpectedErrorFault) {
                unexpectedErrorFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }

    private static SforceServiceStub createAuthServiceInstance(LoginResult loginInfo) throws
                                                                                      AxisFault {
        SforceServiceStub binding = null;
        SessionHeader sh = new SessionHeader();
        sh.setSessionId(loginInfo.getSessionId());

        binding = (SforceServiceStub) new SforceServiceStub(loginInfo.getServerUrl());
        return binding;
    }

    private static SessionHeader getAuthHeader(LoginResult loginInfo) {
        SessionHeader sh = new SessionHeader();
        sh.setSessionId(loginInfo.getSessionId());

        return sh;
    }

    private static LoginResult login() {
        LoginResult loginResult = null;
        SforceServiceStub binding = null;
        try {
            //    Create binding object
            binding = (SforceServiceStub) new SforceServiceStub();
            //    login
            loginResult = binding.login("udayangaw@wso2.com", "Welcomeusw1233MmrAxtmUed7JGOwbUsfhXg1v",
                                        null);
        } catch (LoginFault ex) {
            // The LoginFault derives from AxisFault

            ExceptionCode exCode = ExceptionCode.Factory.fromValue(ex.getMessage());
            if (exCode == ExceptionCode.FUNCTIONALITY_NOT_ENABLED ||
                exCode == ExceptionCode.INVALID_CLIENT ||
                exCode == ExceptionCode.INVALID_LOGIN ||
                exCode == ExceptionCode.LOGIN_DURING_RESTRICTED_DOMAIN ||
                exCode == ExceptionCode.LOGIN_DURING_RESTRICTED_TIME ||
                exCode == ExceptionCode.ORG_LOCKED ||
                exCode == ExceptionCode.PASSWORD_LOCKOUT ||
                exCode == ExceptionCode.SERVER_UNAVAILABLE ||
                exCode == ExceptionCode.TRIAL_EXPIRED ||
                exCode == ExceptionCode.UNSUPPORTED_CLIENT) {
                System.out.println("Please be sure that you have a valid username " +
                                   "and password.");
            } else {
                // Write the fault code to the console

                System.out.println(ex);
                // Write the fault message to the console

                System.out.println("An unexpected error has occurred." + ex.getMessage());
            }
            return null;
        } catch (Exception ex) {
            System.out.println("An unexpected error has occurred: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
        // Check if the password has expired

        if (loginResult.getPasswordExpired()) {
            System.out.println("An error has occurred. Your password has expired.");
            return null;
        }

        System.out.println("Login was successful.");
        return loginResult;

    }


}
