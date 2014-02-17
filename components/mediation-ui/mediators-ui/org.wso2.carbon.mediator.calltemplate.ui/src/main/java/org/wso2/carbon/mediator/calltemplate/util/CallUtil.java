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
package org.wso2.carbon.mediator.calltemplate.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.util.xpath.SynapseXPath;

import java.util.Map;
import java.util.Set;

public class CallUtil {

    public static void addNamespacesTo(Value val, SynapseXPath expression){
        if(val != null){
            Map namespaces = expression.getNamespaces();
            Set keySet = namespaces.keySet();
            for (Object key : keySet) {
                Object ns = namespaces.get(key);
                if (ns instanceof String) {
                    val.addNamespace(OMAbstractFactory.getOMFactory().createOMNamespace((String) ns, (String) key));
                } else if (ns instanceof OMNamespace) {
                    val.addNamespace((OMNamespace) ns);
                }
            }
            /*for (Object ns : ) {
                namespaces.
                if (ns instanceof String) {
                    val.addNamespace(OMAbstractFactory.getOMFactory().createOMNamespace("", (String) ns));
                }else if (ns instanceof OMNamespace){
                    val.addNamespace((OMNamespace) ns);
                }
            }*/

        }
    }

    public static String[] extractParamNames(String paramExpr){
        if(paramExpr!=null){
            return paramExpr.split(";");
        }
        return new String[0];
    }

}
