/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mediation.statistics.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This scheduler is responsible of repeatedly calling the exposeData method of the consumer
 * with an interval of 1 minute
 */
public class StatScheduler {
    private Log log = LogFactory.getLog(StatScheduler.class);

    private Timer timer = new Timer("ESB-JMX-TIMER");
    private Calendar calendar = Calendar.getInstance();
    private EsbStatisticsConsumer consumer;

    public StatScheduler(EsbStatisticsConsumer consumer) {
        this.consumer = consumer;
    }

    public void schedule() {
        timer.schedule(new StatTimerTask(), getTime());
    }

    private Date getTime() {
        calendar.add(Calendar.MINUTE, 1);
        return calendar.getTime();
    }

    public class StatTimerTask extends TimerTask {

        public void run() {
            try {
                consumer.exposeData();
            } catch (Throwable t) {
                log.error("Error in the Timer", t);
            } finally {
                schedule();
            }
        }
    }

}

