/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediator.kerberos;

public final class KerberosConstants {

    //Oid for SPENGO mechanism.
    public static final String SPNEGO_BASED_OID = "1.3.6.1.5.5.2";

    //Negotiate header string.
    public static final String NEGOTIATE = "Negotiate";

    public static final String UTF8 = "UTF-8";

    public static final String KERBEROS_CONFIG_PROPERTY = "java.security.krb5.conf";

    public static final String JAAS_CONFIG_PROPERTY = "java.security.auth.login.config";

    public static final String KERBEROS_SERVICE_STRING = "kerberosService";
    public static final String SPN_STRING = "spn";
    public static final String CLIENT_PRINCIPAL_STRING = "clientPrincipal";
    public static final String PASSWORD_STRING = "password";
    public static final String KEYTAB_PATH_STRING = "keytabPath";
    public static final String KRB5_CONFIG_STRING = "krb5Config";
    public static final String LOGIN_CONFIG_STRING = "loginConfig";
    public static final String LOGIN_CONTEXT_NAME_STRING = "loginContextName";
    public static final String IS_INITIATOR = "isInitiator";
    public static final String PRINCIPAL = "principal";
    public static final String USE_KEYTAB = "useKeyTab";
    public static final String KEYTAB = "keyTab";
    public static final String DEBUG = "debug";
}
