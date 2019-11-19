/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mediator.jsontransform;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.util.MediatorProperty;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;


public class JSONTransformMediator extends AbstractMediator {
    /** Only properties specified to the  mediator */
    public static final int CUSTOM_VALUE = 0;
    /** To, From, WSAction, SOAPAction, ReplyTo, MessageID and any properties */
    public static final int SIMPLE_VALUE = 1;
    /** All SOAP header blocks and any properties */
    public static final int HEADERS_VALUE = 2;
    /** all attributes of level 'simple' and the SOAP envelope and any properties */
    public static final int FULL_VALUE = 3;

    private static final String SIMPLE  = "simple";
    private static final String HEADERS = "headers";
    private static final String FULL    = "full";
    private static final String CUSTOM  = "custom";
    private static final QName ATT_LEVEL = new QName("level");
    private static final QName ATT_SEPERATOR = new QName("separator");
    private static final QName ATT_CATEGORY = new QName("category");

    private static final String TRACE = "TRACE";
    private static final String DEBUG = "DEBUG";
    private static final String INFO = "INFO";
    private static final String WARN = "WARN";
    private static final String ERROR = "ERROR";
    private static final String FATAL = "FATAL";

    public static final int TRACE_VALUE = 0;
    public static final int DEBUG_VALUE = 1;
    public static final int INFO_VALUE = 2;
    public static final int WARN_VALUE = 3;
    public static final int ERROR_VALUE = 4;
    public static final int FATAL_VALUE = 5;

    public static final String DEFAULT_SEP = ", ";

    /** The default log level is set to SIMPLE_VALUE */
    private int logLevel = SIMPLE_VALUE;
    /**The default log category is INFO */
    private int logCategory = INFO_VALUE;
    /** The separator for which used to separate logging information */
    private String separator = DEFAULT_SEP;
    /** The holder for the custom properties */
    private final List<MediatorProperty> properties = new ArrayList<MediatorProperty>();

    public String getTagLocalName() {
        return "log";
    }

    public int getLogLevel() {
        return logLevel;
    }

    public String getSeparator() {
        return separator.replace("\n","\\n").replace("\t","\\t");
    }

    public int getLogCategory() {
        return logCategory;
    }

    public void setLogCategory(int logCategory) {
        this.logCategory = logCategory;
    }

    public List<MediatorProperty> getProperties() {
        return properties;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public void setSeparator(String separator) {
        this.separator = separator.replace("\\n", "\n").replace("\\t", "\t");
    }

    public void addProperty(MediatorProperty p) {
        properties.add(p);
    }

    public void addAllProperties(List<MediatorProperty> list) {
        properties.addAll(list);
    }

    public OMElement serialize(OMElement parent) {
        OMElement log = fac.createOMElement("log", synNS);
        saveTracingState(log, this);

        if (logLevel != SIMPLE_VALUE) {
            log.addAttribute(fac.createOMAttribute(
                "level", nullNS,
                    logLevel == HEADERS_VALUE ? "headers" :
                    logLevel == FULL_VALUE ? "full" :
                    logLevel == CUSTOM_VALUE ? "custom" : "simple"
                ));
        }

        if(logCategory != INFO_VALUE ){
            log.addAttribute(fac.createOMAttribute("category" , nullNS ,
                    logCategory == TRACE_VALUE ? "TRACE" :
                    logCategory == DEBUG_VALUE ? "DEBUG" :
                    logCategory == WARN_VALUE ? "WARN" :
                    logCategory == ERROR_VALUE ? "ERROR" :
                    logCategory == FATAL_VALUE ? "FATAL" : "INFO"
            ));
        }
        if (!DEFAULT_SEP.equals(separator)) {
            log.addAttribute(fac.createOMAttribute(
                    "separator", nullNS, separator));
        }

        serializeMediatorProperties(log, properties, PROP_Q);

        if (parent != null) {
            parent.addChild(log);
        }
        return log;
    }

    public void build(OMElement elem) {
        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);

        // Set the high level set of properties to be logged (i.e. log level)
        OMAttribute level = elem.getAttribute(ATT_LEVEL);
        if (level != null) {
            String levelstr = level.getAttributeValue();
            if (SIMPLE.equals(levelstr)) {
                 logLevel = SIMPLE_VALUE;
            } else if (HEADERS.equals(levelstr)) {
                 logLevel = HEADERS_VALUE;
            } else if (FULL.equals(levelstr)) {
                logLevel = FULL_VALUE;
            } else if (CUSTOM.equals(levelstr)) {
                logLevel = CUSTOM_VALUE;
            }
        }

        OMAttribute category = elem.getAttribute(ATT_CATEGORY);
        if(category != null) {
            String categoryStr = category.getAttributeValue();
            if (TRACE.equals(categoryStr)) {
                logCategory = TRACE_VALUE;
            } else if (DEBUG.equals(categoryStr)) {
                logCategory = DEBUG_VALUE;
            } else if (INFO.equals(categoryStr)) {
                logCategory = INFO_VALUE;
            } else if (WARN.equals(categoryStr)) {
                logCategory = WARN_VALUE;
            } else if (ERROR.equals(categoryStr)) {
                logCategory = ERROR_VALUE;
            } else if (FATAL.equals(categoryStr)) {
                logCategory = FATAL_VALUE;
            }
        }

        // check if a custom separator has been supplied, if so use it
        OMAttribute separator = elem.getAttribute(ATT_SEPERATOR);
        if (separator != null) {
            setSeparator(separator.getAttributeValue());
        }

        addAllProperties(getMediatorProperties(elem));
    }
}
