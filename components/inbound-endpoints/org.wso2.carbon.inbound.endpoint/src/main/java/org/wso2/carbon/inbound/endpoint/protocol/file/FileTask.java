/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.inbound.endpoint.protocol.file;

import org.apache.abdera.i18n.rfc4646.Lang;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.inbound.endpoint.common.InboundTask;

/**
 *
 * FileTask class is use to schedue tasks for inbound file processor when
 * required (coordination==true)
 *
 */
public class FileTask extends InboundTask {

    private static final Log logger = LogFactory.getLog(FileTask.class.getName());

    private FilePollingConsumer fileScanner;

    public FileTask(FilePollingConsumer fileScanner,long interval,String cron) {
        logger.debug("File Task initalize.");
        this.fileScanner = fileScanner;
        this.interval = interval;
        this.cron = cron;
    }

    protected void taskExecute() {
        logger.debug("File Task executing.");
        fileScanner.execute();
    }

    public void init(SynapseEnvironment synapseEnvironment) {
        logger.debug("Initializing Task.");
    }

    public void destroy() {
        logger.debug("Destroying Task. ");
    }
}
