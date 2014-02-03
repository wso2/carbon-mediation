/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediation.tracer;

import org.wso2.carbon.logging.appenders.MemoryAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.Appender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.synapse.SynapseConstants;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.List;
import java.util.ArrayList;

public class MediationTracerService {
    private static final Log log = LogFactory.getLog(MediationTracerService.class);

    public String[] getTraceLogs() {
        int amount;
        int DEFAULT_NO_OF_LOGS = 100;
        Logger logger = Logger.getLogger(SynapseConstants.TRACE_LOGGER);
        Appender appender = logger.getAppender("TRACE_MEMORYAPPENDER");
        if (appender instanceof MemoryAppender) {
            MemoryAppender memoryAppender = (MemoryAppender) appender;
            if ((memoryAppender.getCircularQueue() != null)) {
                amount = memoryAppender.getBufferSize();
            } else {
                return new String[]{
                        "--- No trace entries found. " +
                                "You can enable tracing on sequences, " +
                                "proxies or endpoints by vising the relevant pages ---"};
            }
            if ((memoryAppender.getCircularQueue().getObjects(amount) == null) ||
                    (memoryAppender.getCircularQueue().getObjects(amount).length == 0)) {
                return new String[]{
                        "--- No trace entries found. " +
                                "You can enable tracing on sequences, " +
                                "proxies or endpoints by vising the relevant pages ---"};
            }
            Object[] objects;
            if (amount < 1) {
                objects = memoryAppender.getCircularQueue().getObjects(DEFAULT_NO_OF_LOGS);
            } else {
                objects = memoryAppender.getCircularQueue().getObjects(amount);
            }
            String[] resp = new String[objects.length];
            Layout layout = memoryAppender.getLayout();
            for (int i = 0; i < objects.length; i++) {
                LoggingEvent logEvt = (LoggingEvent) objects[i];
                if (logEvt != null) {
                    resp[i] = StringEscapeUtils.escapeHtml(layout.format(logEvt));
                }
            }
            return resp;
        } else {
            return new String[]{"The trace log must be configured to use the " +
                    "org.wso2.carbon.logging.appenders.MemoryAppender to view entries through the admin console"};
        }
    }

    public boolean clearTraceLogs() {
        Logger logger = Logger.getLogger(SynapseConstants.TRACE_LOGGER);
        Appender appender = logger.getAppender("TRACE_MEMORYAPPENDER");
        if (appender instanceof MemoryAppender) {
            try {
                MemoryAppender memoryAppender = (MemoryAppender) appender;
                if (memoryAppender.getCircularQueue() != null) {
                    memoryAppender.getCircularQueue().clear();
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public String[] searchTraceLog(String keyword,
                                   boolean ignoreCase) throws MediationTracerException {
        int DEFAULT_NO_OF_LOGS = 100;
        int definedamonut;
        if (keyword == null) {
            handleException("Key word can not be null");
        }
        if ("ALL".equals(keyword) || "".equals(keyword)) {
            return getTraceLogs();
        }
        Logger logger = Logger.getLogger(SynapseConstants.TRACE_LOGGER);
        Appender appender = logger.getAppender("TRACE_MEMORYAPPENDER");
        if (appender instanceof MemoryAppender) {
            MemoryAppender memoryAppender
                    = (MemoryAppender) appender;
            if ((memoryAppender.getCircularQueue() != null)) {
                definedamonut = memoryAppender.getBufferSize();
            } else {
                return new String[]{
                        "--- No trace entries found for " +
                                " " + keyword + " ---"
                };
            }
            if ((memoryAppender.getCircularQueue().getObjects(definedamonut) == null) ||
                    (memoryAppender.getCircularQueue().getObjects(definedamonut).length == 0)) {
                return new String[]{
                        "--- No trace entries found for " +
                                "the " + keyword + " ---"
                };
            }
            Object[] objects;
            if (definedamonut < 1) {
                objects = memoryAppender.getCircularQueue().getObjects(DEFAULT_NO_OF_LOGS);
            } else {
                objects = memoryAppender.getCircularQueue().getObjects(definedamonut);
            }
            Layout layout = memoryAppender.getLayout();
            List<String> resultList = new ArrayList<String>();
            for (Object object : objects) {
                LoggingEvent logEvt = (LoggingEvent) object;
                if (logEvt != null) {
                    String result = layout.format(logEvt);
                    if (result != null) {
                        if (!ignoreCase) {
                            if (result.indexOf(keyword) > -1) {
                                resultList.add(StringEscapeUtils.escapeHtml(result));
                            }
                        } else {
                            if (keyword != null &&
                                    result.toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
                                resultList.add(StringEscapeUtils.escapeHtml(result));
                            }
                        }
                    }
                }
            }
            if (resultList.isEmpty()) {
                return new String[]{
                        "--- No trace entries found for " +
                                "the  " + keyword + " ---"
                };
            }
            return resultList.toArray(new String[resultList.size()]);
        } else {
            return new String[]{"The trace log must be configured to use the " +
                    "org.wso2.carbon.logging.appenders.MemoryAppender to view entries through the " +
                    "admin console"};
        }

    }

    public String[] getLogs() {
        int DEFAULT_NO_OF_LOGS = 100;
        int definedamount;
        Appender appender
                = Logger.getRootLogger().getAppender("LOG_MEMORYAPPENDER");
        if (appender instanceof MemoryAppender) {
            MemoryAppender memoryAppender = (MemoryAppender) appender;
            if ((memoryAppender.getCircularQueue() != null)) {
                definedamount = memoryAppender.getBufferSize();
            } else {
                return new String[]{
                        "--- No log entries found. " +
                                "You may try increasing the log level ---"
                };
            }
            if ((memoryAppender.getCircularQueue().getObjects(definedamount) == null) ||
                    (memoryAppender.getCircularQueue().getObjects(definedamount).length == 0)) {
                return new String[]{
                        "--- No log entries found. " +
                                "You may try increasing the log level ---"
                };
            }
            Object[] objects;
            if (definedamount < 1) {
                objects = memoryAppender.getCircularQueue().getObjects(DEFAULT_NO_OF_LOGS);
            } else {
                objects = memoryAppender.getCircularQueue().getObjects(definedamount);
            }
            String[] resp = new String[objects.length];
            Layout layout = memoryAppender.getLayout();
            for (int i = 0; i < objects.length; i++) {
                LoggingEvent logEvt = (LoggingEvent) objects[i];
                if (logEvt != null) {
                    resp[i] = StringEscapeUtils.escapeHtml(layout.format(logEvt));
                }
            }
            return resp;
        } else {
            return new String[]{"The log must be configured to use the " +
                    "org.wso2.carbon.logging.appenders.MemoryAppender to view entries on the admin console"};
        }
    }

    private void handleException(String msg) throws MediationTracerException {
        log.error(msg);
        throw new MediationTracerException(msg);
    }
}
