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
package org.wso2.carbon.cloud.gateway.agent.transport;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.cloud.gateway.common.CGUtils;
import org.wso2.carbon.cloud.gateway.common.thrift.gen.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represent the transport level exchange buffers for workers ( csg polling transport)
 */
public class CGPollingTransportBuffers {

    /**
     * The request message buffer which holds the request messages
     */
    private BlockingQueue<Message> requestBuffer = new LinkedBlockingQueue<Message>();

    /**
     * The response message buffer which holds the responses for processed messages
     */
    private BlockingQueue<Message> responseBuffer = new LinkedBlockingQueue<Message>();

    /**
     * Returns the response messages as a list
     *
     * @param blockSize the block blockSize of the response message list
     * @return the block of the response messages of blockSize
     * @throws AxisFault in case of an error
     */
    public List<Message> getResponseMessageList(final int blockSize) throws AxisFault {
        List<Message> msgList = new ArrayList<Message>();
        if (responseBuffer.size() > 0) {
            CGUtils.moveElements(responseBuffer, msgList, blockSize);
        }
        return msgList;
    }

    /**
     * Add a response message to the response buffer
     *
     * @param msg the response message
     * @throws AxisFault throws in case of an error
     */
    public void addResponseMessage(Message msg) throws AxisFault {
        try {
            // it's ok to block here until space available,
            responseBuffer.put(msg);
        } catch (InterruptedException e) {
            throw new AxisFault("Could not get the response message", e);
        }
    }

    /**
     * Returns the request message buffer in transport
     *
     * @return the request message buffer
     */
    public BlockingQueue<Message> getRequestMessageBuffer() {
        return requestBuffer;
    }

    /**
     * Returns a request message from the request message buffer
     * @return the thrift message
     */
    public Message getRequestMessage() {
        try {
            // block if there is no messages
            return requestBuffer.take();
        } catch (InterruptedException e) {
            // ignore
        }
        return null;
    }
}
