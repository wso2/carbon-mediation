/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.das.data.publisher.util.stats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Calendar;


public class StatsPrinterTimerTask implements Runnable {

    private static Log log = LogFactory.getLog(StatsPrinterTimerTask.class);
    public static final int INITIAL_DELAY = 60000;
    public static final int TIME_GAP_FOR_PRINTING_STATS = 2000;

    int previousValue;
    long previousTime;
    double previousNoOfEventsPerSec;
    double maxTPS;

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            printResult();
        }

    }

    private void printResult() {
        while (true) {
            if (AtomicIntSingleton.getAtomicInteger().get() == 0) {
                try {
                    Thread.sleep(INITIAL_DELAY);
                    printResults();
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            } else {
                try {
                    Thread.sleep(TIME_GAP_FOR_PRINTING_STATS);
                    printResults();
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    private void printResults() {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        int totalNoOfEvents = AtomicIntSingleton.getAtomicInteger().get();
        int difference = totalNoOfEvents - previousValue;
        long timeDifference = currentTime - previousTime;
        double noOfEventsPerSec = (difference * 1000) / (double)timeDifference;
        log.debug("Total no of events:" + totalNoOfEvents);
        log.debug("No of events per sec: " + noOfEventsPerSec);
        if (noOfEventsPerSec > maxTPS) {
            maxTPS = noOfEventsPerSec;
        }
        previousNoOfEventsPerSec = noOfEventsPerSec;
        log.debug("Max no of events per sec: " + maxTPS);
        previousValue = totalNoOfEvents;
        previousTime = currentTime;
    }
}

