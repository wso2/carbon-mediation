/**
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 **/
package org.wso2.carbon.mediator.dblookup;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMAttribute;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.MediatorException;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import java.util.*;
import java.sql.Types;

public class DBLookupMediator extends AbstractMediator {
    public static final QName URL_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "url");
    static final QName DRIVER_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "driver");
    static final QName USER_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "user");
    static final QName PASS_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "password");

    public static final QName DSNAME_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "dsName");
    static final QName ICCLASS_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "icClass");

    static final QName STMNT_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "statement");
    static final QName SQL_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "sql");
    static final QName PARAM_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "parameter");
    static final QName RESULT_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "result");

    static final QName ATT_COLUMN = new QName("column");
    static final QName ATT_TYPE = new QName("type");


    protected final Map dataSourceProps = new HashMap();

    private final List<Statement> statementList = new ArrayList<Statement>();

    public void addDataSourceProperty(QName name, String value) {
        dataSourceProps.put(name, value);
    }

    public void addDataSourceProperty(String name, String value) {
        dataSourceProps.put(name, value);
    }

    public Map getDataSourceProps() {
        return dataSourceProps;
    }

    public void addStatement(Statement stmnt) {
        statementList.add(stmnt);
    }

    public List getStatementList() {
        return statementList;
    }

    public OMElement serialize(OMElement parent) {
        OMElement dbLookup = fac.createOMElement("dblookup", synNS);
        saveTracingState(dbLookup, this);
        serializeDBInformation(dbLookup);

        if (parent != null) {
            parent.addChild(dbLookup);
        }
        return dbLookup;
    }

    private void serializeDBInformation(OMElement dbLookup) {
        OMElement connElt = fac.createOMElement("connection", synNS);
        OMElement poolElt = fac.createOMElement("pool", synNS);

        Iterator iter = dataSourceProps.keySet().iterator();
        while (iter.hasNext()) {

            Object o = iter.next();
            String value = (String) dataSourceProps.get(o);

            if (o instanceof QName) {
                QName name = (QName) o;
                OMElement elt = fac.createOMElement(name.getLocalPart(), synNS);
                elt.setText(value);
                poolElt.addChild(elt);

            } else if (o instanceof String) {
                OMElement elt = fac.createOMElement(
                    PROP_Q.getLocalPart(), synNS);
                elt.addAttribute(fac.createOMAttribute("name", nullNS, (String) o));
                elt.addAttribute(fac.createOMAttribute("value", nullNS, value));
                poolElt.addChild(elt);
            }
        }

        connElt.addChild(poolElt);
        dbLookup.addChild(connElt);

        // process statements
        Iterator statementIter = statementList.iterator();
        while (statementIter.hasNext()) {

            Statement statement = (Statement) statementIter.next();
            OMElement stmntElt = fac.createOMElement(
                STMNT_Q.getLocalPart(), synNS);

            OMElement sqlElt = fac.createOMElement(
                SQL_Q.getLocalPart(), synNS);
            OMText sqlText = fac.createOMText(statement.getRawStatement(), XMLStreamConstants.CDATA);
            sqlElt.addChild(sqlText);
            stmntElt.addChild(sqlElt);

            // serialize parameters of the statement
            for (Iterator it = statement.getParameters().iterator(); it.hasNext(); ) {

                Statement.Parameter param = (Statement.Parameter) it.next();
                OMElement paramElt = fac.createOMElement(
                    PARAM_Q.getLocalPart(), synNS);

                if (param.getPropertyName() != null) {
                    paramElt.addAttribute(
                        fac.createOMAttribute("value", nullNS, param.getPropertyName()));
                }
                if (param.getXpath() != null) {
                    SynapseXPathSerializer.serializeXPath(param.getXpath(), paramElt, "expression");
                }

                switch (param.getType()) {
                    case Types.CHAR: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "CHAR"));
                        break;
                    }
                    case Types.VARCHAR: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "VARCHAR"));
                        break;
                    }
                    case Types.LONGVARCHAR: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "LONGVARCHAR"));
                        break;
                    }
                    case Types.NUMERIC: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "NUMERIC"));
                        break;
                    }
                    case Types.DECIMAL: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "DECIMAL"));
                        break;
                    }
                    case Types.BIT: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "BIT"));
                        break;
                    }
                    case Types.TINYINT: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "TINYINT"));
                        break;
                    }
                    case Types.SMALLINT: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "SMALLINT"));
                        break;
                    }
                    case Types.INTEGER: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "INTEGER"));
                        break;
                    }
                    case Types.BIGINT: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "BIGINT"));
                        break;
                    }
                    case Types.REAL: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "REAL"));
                        break;
                    }
                    case Types.FLOAT: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "FLOAT"));
                        break;
                    }
                    case Types.DOUBLE: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "DOUBLE"));
                        break;
                    }
                    case Types.DATE: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "DATE"));
                        break;
                    }
                    case Types.TIME: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "TIME"));
                        break;
                    }
                    case Types.TIMESTAMP: {
                        paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "TIMESTAMP"));
                        break;
                    }
                    default: {
                        throw new MediatorException("Unknown or unsupported JDBC type : " +
                            param.getType());
                    }
                }

                stmntElt.addChild(paramElt);
            }

            // serialize any optional results of the statement
            for (Iterator it = statement.getResultsMap().keySet().iterator(); it.hasNext(); ) {

                String name = (String) it.next();
                String columnStr = (String) statement.getResultsMap().get(name);

                OMElement resultElt = fac.createOMElement(
                    RESULT_Q.getLocalPart(), synNS);

                resultElt.addAttribute(
                    fac.createOMAttribute("name", nullNS, name));
                resultElt.addAttribute(
                    fac.createOMAttribute("column", nullNS, columnStr));

                stmntElt.addChild(resultElt);
            }

            dbLookup.addChild(stmntElt);
        }
    }

    public void build(OMElement elem) {
        dataSourceProps.clear();
        statementList.clear();

        buildDataSource(elem);
        processStatements(elem);
    }

    private void processStatements(OMElement elem) {
        Iterator iter = elem.getChildrenWithName(STMNT_Q);
        while (iter.hasNext()) {

            OMElement stmntElt = (OMElement) iter.next();
            Statement statement = new Statement(getValue(stmntElt, SQL_Q));

            Iterator paramIter = stmntElt.getChildrenWithName(PARAM_Q);
            while (paramIter.hasNext()) {

                OMElement paramElt = (OMElement) paramIter.next();
                String xpath = getAttribute(paramElt, ATT_EXPRN);
                String value = getAttribute(paramElt, ATT_VALUE);

                if (xpath != null || value != null) {

                    SynapseXPath xp = null;
                    if (xpath != null) {
                        try {
                            xp = SynapseXPathFactory.getSynapseXPath(paramElt, ATT_EXPRN);

                        } catch (JaxenException e) {
                            throw new MediatorException("Invalid XPath specified for the source attribute : " +
                                    xpath);
                        }
                    }
                    statement.addParameter(
                            value,
                            xp,
                            getAttribute(paramElt, ATT_TYPE));
                }
            }

            Iterator resultIter = stmntElt.getChildrenWithName(RESULT_Q);
            while (resultIter.hasNext()) {

                OMElement resultElt = (OMElement) resultIter.next();
                statement.addResult(
                        getAttribute(resultElt, ATT_NAME),
                        getAttribute(resultElt, ATT_COLUMN));
            }

            statementList.add(statement);
        }

    }

    private void buildDataSource(OMElement elem) {
        OMElement pool = null;
        // get the 'pool' element and determine if we need to create a DataSource or
        // look up using JNDI
        try {
            SynapseXPath xpath = new SynapseXPath("self::node()/syn:connection/syn:pool");
            xpath.addNamespace("syn", XMLConfigConstants.SYNAPSE_NAMESPACE);
            pool = (OMElement) xpath.selectSingleNode(elem);

            if (pool.getFirstChildWithName(DRIVER_Q) != null) {
                createCustomDataSource(pool);

            } else if (pool.getFirstChildWithName(DSNAME_Q) != null) {
                lookupDataSource(pool);
            } else {
                throw new MediatorException("The DataSource connection information must be specified for " +
                        "using a custom DataSource connection pool or for a JNDI lookup");
            }

        } catch (JaxenException e) {
            throw new MediatorException("Error looking up DataSource connection information");
        }
    }

    private void lookupDataSource(OMElement pool) {
        String dsName = getValue(pool, DSNAME_Q);
        dataSourceProps.put(DSNAME_Q, dsName);

        if (getValue(pool, ICCLASS_Q) != null) {
            dataSourceProps.put(ICCLASS_Q, getValue(pool, ICCLASS_Q));
        }
        if (getValue(pool, URL_Q) != null) {
            dataSourceProps.put(URL_Q, getValue(pool, URL_Q));
        }
        if (getValue(pool, USER_Q) != null) {
            dataSourceProps.put(USER_Q, getValue(pool, USER_Q));
        }
        if (getValue(pool, PASS_Q) != null) {
            dataSourceProps.put(PASS_Q, getValue(pool, PASS_Q));
        }
    }

    private void createCustomDataSource(OMElement pool) {
        //save loaded properties for later
        dataSourceProps.put(DRIVER_Q, getValue(pool, DRIVER_Q));
        dataSourceProps.put(URL_Q, getValue(pool, URL_Q));
        dataSourceProps.put(USER_Q, getValue(pool, USER_Q));
        dataSourceProps.put(PASS_Q, getValue(pool, PASS_Q));

        Iterator props = pool.getChildrenWithName(PROP_Q);
        while (props.hasNext()) {

            OMElement prop = (OMElement) props.next();
            String name = prop.getAttribute(ATT_NAME).getAttributeValue();
            String value = prop.getAttribute(ATT_VALUE).getAttributeValue();            
            dataSourceProps.put(name, value);
        }
    }

    protected String getValue(OMElement elt, QName qName) {
        OMElement e = elt.getFirstChildWithName(qName);
        if (e != null) {
            return e.getText();
        }
        return null;
    }

     protected String getAttribute(OMElement elt, QName qName) {
        OMAttribute a = elt.getAttribute(qName);
        if (a != null) {
            return a.getAttributeValue();
        }
        return null;
    }

    public String getTagLocalName() {
        return "dblookup";
    }
}

