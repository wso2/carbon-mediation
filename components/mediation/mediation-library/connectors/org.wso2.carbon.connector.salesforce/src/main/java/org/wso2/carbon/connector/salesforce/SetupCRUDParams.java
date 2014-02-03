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

package org.wso2.carbon.connector.salesforce;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.util.ConnectorUtils;

public class SetupCRUDParams extends AbstractConnector {
	public void connect(MessageContext messageContext) {
		setDefaultValue(messageContext, SalesforceUtil.SALESFORCE_CRUD_ALLORNONE, "1");
		setDefaultValue(messageContext, SalesforceUtil.SALESFORCE_CRUD_ALLOWFIELDTRUNCATE, "0");
		setDefaultValue(messageContext, SalesforceUtil.SALESFORCE_EXTERNALID, "Id");
	}

	private void setDefaultValue(MessageContext messageContext, String strParamName,
	                             String strDefaultValue) {
		String strValue =
		                  (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                  strParamName);
		if (strValue == null || "".equals(strValue)) {
			strValue = strDefaultValue;
		}
		messageContext.setProperty(SalesforceUtil.SALESFORCE_CRUD_PREFIX + strParamName, strValue);
	}

}
