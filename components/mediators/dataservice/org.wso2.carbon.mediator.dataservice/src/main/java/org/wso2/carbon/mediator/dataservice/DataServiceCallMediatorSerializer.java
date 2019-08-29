/*
 *
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  you may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.mediator.dataservice;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.config.xml.SynapsePathSerializer;
import org.wso2.carbon.mediator.dataservice.DataServiceCallMediator.Operation;
import org.wso2.carbon.mediator.dataservice.DataServiceCallMediator.Operations;
import org.wso2.carbon.mediator.dataservice.DataServiceCallMediator.Param;

import java.util.Map;

/**
 * Serializer for {@link DataServiceCallMediator} instances.
 *
 * @see DataServiceCallMediatorSerializer
 */

public class DataServiceCallMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof DataServiceCallMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        DataServiceCallMediator mediator = (DataServiceCallMediator) m;
        OMElement dsCallEle = fac.createOMElement(DataServiceCallMediatorConstants.DATA_SERVICES_CALL, synNS);
        OMElement targetEle = fac.createOMElement(DataServiceCallMediatorConstants.TARGET, synNS);
        OMAttribute serviceName = fac.createOMAttribute(DataServiceCallMediatorConstants.SERVICE_NAME, nullNS,
                mediator.getDsName());
        dsCallEle.addAttribute(serviceName);
        Operations operationsObj = mediator.getOperations();
        OMElement operationsEle = fac.createOMElement(DataServiceCallMediatorConstants.OPERATIONS, synNS);
        operationsEle.addAttribute(DataServiceCallMediatorConstants.TYPE, operationsObj.getType().toString().
                toLowerCase(), nullNS);
        OMElement operationEle = extractOperations(operationsObj, operationsEle);
        operationsEle.addChild(operationEle);
        dsCallEle.addChild(operationEle);
        targetEle.addAttribute(DataServiceCallMediatorConstants.TYPE, mediator.getTargetType(), nullNS);
        if (DataServiceCallMediatorConstants.PROPERTY.equals(mediator.getTargetType())) {
            targetEle.addAttribute(DataServiceCallMediatorConstants.NAME, mediator.getPropertyName(), nullNS);
        }
        dsCallEle.addChild(targetEle);
        saveTracingState(dsCallEle, mediator);
        return dsCallEle;
    }


    private OMElement extractOperations(Operations operationsObj, OMElement operationEle) {
        for (Object operationObj : operationsObj.getOperations()) {
            if (operationObj instanceof Operation) {
                Operation nestedOperation = (Operation) operationObj;
                OMElement nestedOperationEle = fac.createOMElement(DataServiceCallMediatorConstants.OPERATION, synNS);
                nestedOperationEle.addAttribute(DataServiceCallMediatorConstants.NAME, nestedOperation.
                        getOperationName(), nullNS);
                for (Param param : nestedOperation.getParams()) {
                    OMElement paramEle = extractParams(param);
                    nestedOperationEle.addChild(paramEle);
                }
                operationEle.addChild(nestedOperationEle);
            } else if (operationObj instanceof Operations) {
                OMElement operationsEle = fac.createOMElement(DataServiceCallMediatorConstants.OPERATIONS, synNS);
                operationsEle.addAttribute(DataServiceCallMediatorConstants.TYPE, operationsObj.getType().toString().
                        toLowerCase(), nullNS);
                OMElement nestedOperationEle = extractOperations((Operations)operationObj, operationsEle);
                operationsEle.addChild(nestedOperationEle);
                operationEle.addChild(operationsEle);
            }
        }
        return operationEle;
    }

    private OMElement extractParams(Param param) {
        OMElement paramEle = fac.createOMElement(DataServiceCallMediatorConstants.PARAM, synNS);
        paramEle.addAttribute(DataServiceCallMediatorConstants.NAME, param.getParamName(), nullNS);
        if (param.getParamType() != null) {
            paramEle.addAttribute(DataServiceCallMediatorConstants.TYPE, param.getParamType(), nullNS);
        }
        if (param.getParamValue() != null) {
            paramEle.addAttribute(DataServiceCallMediatorConstants.VALUE, param.getParamValue(), nullNS);
        }
        if (param.getEvaluator() != null) {
            paramEle.addAttribute(DataServiceCallMediatorConstants.EVALUATOR, param.getEvaluator(), nullNS);
        }
        if (param.getParamExpression() != null) {
            SynapsePathSerializer.serializePath(param.getParamExpression(), paramEle, DataServiceCallMediatorConstants.
                    EXPRESSION);
        }
        return paramEle;
    }

    public String getMediatorClassName() {
        return DataServiceCallMediator.class.getName();
    }
}