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
package org.wso2.carbon.cloud.gateway.agent.heartbeat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.gateway.agent.observer.CGAgentSubject;
import org.wso2.carbon.cloud.gateway.common.CGUtils;

/**
 * Represent a heat beat task which check the health of a remote server, and also notify the
 * set of observers upon detecting the server is alive
 */
public class CGAgentHeartBeatTask implements Runnable {
    private double reconnectionProgressionFactor;
    private int initialReconnectDuration;
    private String host;
    private int port;
    private CGAgentSubject subject;

    private Log log = LogFactory.getLog(CGAgentHeartBeatTask.class);

    public CGAgentHeartBeatTask(CGAgentSubject subject,
                                double reconnectionProgressionFactor,
                                int initialReconnectDuration,
                                String remoteHost,
                                int port) {
        this.subject = subject;
        this.reconnectionProgressionFactor = reconnectionProgressionFactor;
        this.initialReconnectDuration = initialReconnectDuration;
        this.host = remoteHost;
        this.port = port;
    }

    public void run() {
        long retryDuration = initialReconnectDuration;
        log.info("A heart beat task for the remote server '" + host + ":" + port + "' has been " +
                "added");
        while (true) {
            // try to connect to the remote csg server if success notify the observers
            // heart beat will work on an exponential(configurable) loop until success
            // FIXME - this may not the optimal way to check the server status, fix later if required
            if (CGUtils.isServerAlive(host, port)) {
                // notify the observers and exit
                log.info("Hear beat task detected remote server '" + host + ":" + port + "', is alive. " +
                        "Observers will be notified");
                subject.connected(host, port);
                return;
            } else {
                // re-try until success
                retryDuration = (long) (retryDuration * reconnectionProgressionFactor);
                log.info("Remote server '" + host + ":" + port + "' is not alive. Next re-attempt " +
                        "is after '" + (retryDuration / 1000) + "' seconds");
                try {
                    Thread.sleep(retryDuration);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }
}
