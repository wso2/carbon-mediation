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
package org.wso2.carbon.cloud.gateway.agent.observer;

/**
 * Represent the subject in observer for CG Agent. When a server goes down an observer will be created
 * who is interested in this subject
 */
public interface CGAgentSubject {
    /**
     * Add an observer
     * @param o observer
     */
    void addObserver(CGAgentObserver o);

    /**
     * Remove observer
     * @param o observer
     */
    void removeObserver(CGAgentObserver o);

    /**
     * Invoked when the subject connected with the remote server to trigger the observers
     * @param host remote host
     * @param port remote host port
     */
    void connected(String host, int port);
}
