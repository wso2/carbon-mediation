/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediation.security.vault.cipher.tool;

import java.rmi.RemoteException;

import org.wso2.carbon.mediation.security.stub.MediationSecurityAdminServiceStub;

public class CipherTool {

	private MediationSecurityAdminServiceStub mediationSecurityAdminServiceStub;

	public CipherTool(MediationSecurityAdminServiceStub mediationSecurityAdminServiceStub) {
		this.mediationSecurityAdminServiceStub = mediationSecurityAdminServiceStub;
	}

	/**
	 * encrypt the plain text password
	 * 
	 * @param cipher
	 *            init cipher
	 * @param plainTextPass
	 *            plain text password
	 * @return encrypted password
	 * @throws RemoteException
	 */
	public String doEncryption(String plainTextPass) throws RemoteException {
		String encodedValue = mediationSecurityAdminServiceStub.doEncrypt(plainTextPass);
		return encodedValue;
	}

	protected static void handleException(String msg, Exception e) {
		throw new CipherToolException(msg, e);
	}

	protected static void handleException(String msg) {
		throw new CipherToolException(msg);
	}

}
