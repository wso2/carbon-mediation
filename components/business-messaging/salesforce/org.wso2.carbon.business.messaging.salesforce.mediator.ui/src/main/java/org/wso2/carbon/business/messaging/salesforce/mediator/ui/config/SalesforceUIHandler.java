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
package org.wso2.carbon.business.messaging.salesforce.mediator.ui.config;

import org.wso2.carbon.business.messaging.salesforce.mediator.ui.*;
import org.wso2.carbon.business.messaging.salesforce.mediator.ui.handler.ConfigHandler;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class acts as a bridge for Salesforce UI and Model
 */
public class SalesforceUIHandler {
    /**
     * reference for currently operations supported
     */
    public String[] operations;
    /**
     * contains UI parameter objects to correponding model objects mapping
     */
    private final HashMap model2UIMap = new HashMap();

    /**
     * get the index (ui) of the selected operation
     *
     * @param opName     operation selected
     * @param operations array containing supported operations
     * @return index
     */
    public static int getOperationPosition(String opName, String[] operations) {
        int i = 0;
        for (String operation : operations) {
            if (operation.equals(opName)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * @param opName operation selected
     * @return display name of the current operation
     * @throws FileNotFoundException when ui configuration descriptor not found
     * @throws XMLStreamException    error parsing xml
     */
    public String getOperationDisplayName(String opName) throws XMLStreamException, FileNotFoundException {
        return ConfigHandler.getInstance().getOperationDisplayName(opName);
    }

    public String[] getOperationParameters() throws XMLStreamException, FileNotFoundException {
        if (operations == null) {
            ConfigHandler handler = ConfigHandler.getInstance();
            operations = new String[handler.getOperationsSize()];
            //get operations from the map
            Iterator opNames = handler.getOperationNames();
            int i = 0;
            while (opNames.hasNext()) {
                operations[i] = (String) opNames.next();
                i++;
            }
        }
        return operations;
    }

    /**
     * registers model to ui component mapping
     *
     * @param model
     * @param component
     */
    public void setUIMapping(Type model, UIType component) {
        model2UIMap.put(model, component);
    }

    /**
     * provides the Operation Type UI parameters for a corresponding opearation
     *
     * @param model
     * @return operation ui params
     */
    public OperationTypeUI getOperationUIObject(OperationType model) {
        return (OperationTypeUI) model2UIMap.get(model);
    }

    /**
     * provides the Input Type UI parameters for a corresponding input
     *
     * @param model configuration model
     * @return input ui
     *         corresponfing ui parameter object
     */
    public InputTypeUI getInputUIObject(InputType model) {
        return (InputTypeUI) model2UIMap.get(model);
    }

    /**
     * provides the Output Type UI parameters for a corresponding output
     *
     * @param model
     * @return output ui
     */
    public OutputTypeUI getOutputUIObject(OutputType model) {
        return (OutputTypeUI) model2UIMap.get(model);
    }

    /**
     * clear state
     */
    public void flush() {
        model2UIMap.clear();
        operations = null;
    }
}
