/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.transports.sap.bapi.util;

import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;

/** This will contain the required methods that can be use to parser a
 * meta data description of a BAPI/RFC call.
 * So the BNF grammer for the meta data would looks like :
 *  bapirfc   -> import | tables | both
 *  import    -> structure | field | tables | all
 *  structure -> 1 or more fields
 *  tables    -> 1 or more table
 *  table     -> row
 *  row       -> 1 or more fields
 *  field     -> name and value
 *
 * See resources folder for sample meta data description files
 *
 */
public class RFCMetaDataParser {

    private static Log log = LogFactory.getLog(RFCMetaDataParser.class);

    /**
     * Start processing of the document
     * @param document the document node
     * @param function the RFC function to execute
     * @throws AxisFault throws in case of an error
     */
    public static void processMetaDataDocument(OMElement document, JCoFunction function) throws AxisFault{
        Iterator itr = document.getChildElements();
        while(itr.hasNext()){
            OMElement childElement = (OMElement)itr.next();
            processElement(childElement, function);
        }
        if (log.isDebugEnabled()) {
            log.debug("Processed metadata document");
        }
    }

    /**
     * Returns the BAPI/RFC function name.
     *
     * @param rootElement root document element
     * @return the BAPI/RFC function name
     * @throws AxisFault throws in case of an error
     */
    public static String getBAPIRFCFunctionName(OMElement rootElement) throws AxisFault {
        if (rootElement != null) {
            String rfcFunctionName = rootElement.getAttributeValue(RFCConstants.NAME_Q);
            if (rfcFunctionName != null) {
                return rfcFunctionName;
            } else {
                throw new AxisFault("BAPI/RFC function name is mandatory in meta data configuration");
            }
        } else {
            throw new AxisFault("Invalid meta data root element.Found: " + rootElement + "" +
                                ". Required:" + RFCConstants.BAPIRFC);
        }
    }

    private static void processElement(OMElement element, JCoFunction function) throws AxisFault{
        String qname = element.getQName().toString();
        if(qname != null){
            if(qname.equals("import")){
                processImport(element, function);
            }else if(qname.equals("tables")){
                processTables(element, function);
            }else {
                log.warn("Unknown meta data type tag :" + qname + " detected. " +
                        "This meta data element will be discarded!");
            }
        }
    }

    private static void processImport(OMElement element, JCoFunction function) throws AxisFault{
        Iterator itr = element.getChildElements();
        while (itr.hasNext()){
            OMElement childElement = (OMElement) itr.next();
            String qname = childElement.getQName().toString();
            String name = childElement.getAttributeValue(RFCConstants.NAME_Q);
            if(qname.equals("structure")){
                processStructure(childElement, function, name);
            }else if(qname.equals("field")){
                processField(childElement, function, name);
            }else if(qname.equals("tables")){
                processTablesParameter(childElement, function);
            }else{
                log.warn("Unknown meta data type tag :" + qname + " detected. " +
                        "This meta data element will be discarded!");
            }
        }
    }

    private static void processStructure(OMElement element, JCoFunction function, String strcutName)
            throws AxisFault {
        if (strcutName == null) {
            throw new AxisFault("A structure should have a name!");
        }
        JCoStructure jcoStrcture = function.getImportParameterList().getStructure(strcutName);
        if (jcoStrcture != null) {
            Iterator itr = element.getChildElements();
            boolean isRecordFound = false;
            while (itr.hasNext()) {
                OMElement childElement = (OMElement) itr.next();
                String qname = childElement.getQName().toString();
                if (qname.equals("field")) {
                    String fieldName = childElement.getAttributeValue(RFCConstants.NAME_Q);
                    String fieldValue = childElement.getText();
                    for (JCoField field : jcoStrcture) {
                        if (fieldName != null && fieldName.equals(field.getName())) {
                            isRecordFound = true;
                            field.setValue(fieldValue); // TODO - may be we need to check for null ?
                            break;
                        }
                    }
                    if(!isRecordFound){
                        throw new AxisFault("Invalid configuration! The field : "+ fieldName + "" +
                                " did not find the the strcture : " + strcutName);
                    }
                } else {
                    log.warn("Invalid meta data type element found : " + qname + " .This meta data " +
                            "type will be ignored");
                }
            }
        } else {
            log.error("Didn't find the specified structure : " + strcutName + " on the RFC" +
                    " repository. This structure will be ignored");
        }
    }

    private static void processField(OMElement element, JCoFunction function, String fieldName)
            throws AxisFault{
        if(fieldName == null){
            throw new AxisFault("A field should have a name!");
        }
        String fieldValue = element.getText();
        if (fieldValue != null) {
            // TODO-check for avalibility of the field
            function.getImportParameterList().setValue(fieldName, fieldValue);
        }
    }
    public static void processFieldValue(String fieldName, String fieldValue, JCoFunction function) throws AxisFault {
        if (fieldValue != null) {
            function.getImportParameterList().setValue(fieldName, fieldValue);
        } else {
            log.warn(fieldName + "is set with an empty value");
        }
    }

    private static void processTables(OMElement element, JCoFunction function) throws AxisFault{
        Iterator itr = element.getChildElements();
        while (itr.hasNext()){
            OMElement childElement = (OMElement) itr.next();
            String qname = childElement.getQName().toString();
            String tableName = childElement.getAttributeValue(RFCConstants.NAME_Q);
            if(qname.equals("table")){
                processTable(childElement, function, tableName);
            }else{
                log.warn("Invalid meta data type element found : " + qname + " .This meta data " +
                            "type will be ignored");
            }
        }
    }

    private static void processTablesParameter(OMElement element, JCoFunction function) throws AxisFault{
        Iterator itr = element.getChildElements();
        while (itr.hasNext()){
            OMElement childElement = (OMElement) itr.next();
            String qname = childElement.getQName().toString();
            String tableName = childElement.getAttributeValue(RFCConstants.NAME_Q);
            if(qname.equals("table")){
                processTableParameter(childElement, function, tableName);
            }else{
                log.warn("Invalid meta data type element found : " + qname + " .This meta data " +
                            "type will be ignored");
            }
        }
    }

    private static void processTable(OMElement element, JCoFunction function, String tableName)
            throws AxisFault{
        JCoTable inputTable = function.getTableParameterList().getTable(tableName);
        if(inputTable == null){
            throw new AxisFault("Input table :" + tableName + " does not exist");
        }
        Iterator itr = element.getChildElements();
        while (itr.hasNext()){
            OMElement childElement = (OMElement)itr.next();
            String qname = childElement.getQName().toString();
            String id = childElement.getAttributeValue(RFCConstants.ID_Q);
            if(qname.equals("row")){
                processRow(childElement, inputTable, id);
            }else{
                log.warn("Invalid meta data type element found : " + qname + " .This meta data " +
                            "type will be ignored");
            }

        }
    }

    private static void processTableParameter(OMElement element, JCoFunction function, String tableName)
            throws AxisFault{
        JCoTable inputTable = function.getImportParameterList().getTable(tableName);
        if(inputTable == null){
            throw new AxisFault("Input table parameter :" + tableName + " does not exist");
        }
        Iterator itr = element.getChildElements();
        while (itr.hasNext()){
            OMElement childElement = (OMElement)itr.next();
            String qname = childElement.getQName().toString();
            String id = childElement.getAttributeValue(RFCConstants.ID_Q);
            if(qname.equals("row")){
                processRow(childElement, inputTable, id);
            }else{
                log.warn("Invalid meta data type element found : " + qname + " .This meta data " +
                            "type will be ignored");
            }

        }
    }

    private static void processRow(OMElement element, JCoTable table, String id)
            throws AxisFault {

        int rowId;
        try {
            rowId = Integer.parseInt(id);
        } catch (NumberFormatException ex) {
            log.warn("Row ID should be a integer, found " + id + ". Skipping row", ex);
            return;
        }

        if (table.getNumRows() <= rowId) {
            //which mean this is a new row
            table.appendRow();

        } else {
            //handle existing row
            table.setRow(rowId);
        }

        Iterator itr = element.getChildElements();
        while (itr.hasNext()) {
            OMElement childElement = (OMElement) itr.next();
            String qname = childElement.getQName().toString();
            if (qname != null && qname.equals("field")) {
                processField(childElement, table);
            } else {
                log.warn("Invalid meta data type element found : " + qname + " .This meta data " +
                        "type will be ignored");
            }
        }
    }

    private static void processField(OMElement element, JCoTable table)
            throws AxisFault {
        String fieldName = element.getAttributeValue(RFCConstants.NAME_Q);
        String fieldValue = element.getText();

        if (fieldName == null) {
            throw new AxisFault("A field should have a name!");
        }
        if (fieldValue != null) {
            table.setValue(fieldName, fieldValue);
        }
    }
}
