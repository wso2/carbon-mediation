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
package org.wso2.carbon.cloud.gateway.transport.server;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.wso2.carbon.cloud.gateway.common.CGUtils;
import org.wso2.carbon.cloud.gateway.common.thrift.gen.CloudGatewayService;
import org.wso2.carbon.cloud.gateway.common.thrift.gen.Message;
import org.wso2.carbon.cloud.gateway.common.thrift.gen.NotAuthorizedException;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.core.services.authentication.AuthenticationAdmin;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * This implements the handler for Thrift server. This uses static objects(for buffers) in order to make
 * sure that standalone deployment and Stratos based deployment will work.
 * Since static objects can be accessed from anywhere make sure to review (in order to avoid stealing
 * from this buffers) any custom code deployed into the same JVM.
 */
public class CGThriftServerHandler implements CloudGatewayService.Iface {

    private static Log log = LogFactory.getLog(CGThriftServerHandler.class);

    private WorkerPool workerPool;

    public CGThriftServerHandler(WorkerPool workerPool) {
        this.workerPool = workerPool;
    }

    /**
     * A list of semaphores for two way messages
     */
    private static Map<String, Semaphore> semaphoreMap = new ConcurrentHashMap<String, Semaphore>();

    /**
     * When the back end worker task received a response that will be placed in this buffer to pick
     * up by the blocked thread. This buffer is used to communicate between two threads
     */
    private static Map<String, Message> middleBuffer = new ConcurrentHashMap<String, Message>();

    /**
     * Keep track of authorized list of queues in the form of queueName->SecureUUID
     */
    private static Map<String, String> authorizedQueues = new ConcurrentHashMap<String, String>();

    /**
     * List of request buffers which holds the request messages in the form of
     * SecureUUID->BlockingQueue buffer
     */
    private static Map<String, BlockingQueue<Message>> requestBuffers = new ConcurrentHashMap<String, BlockingQueue<Message>>();

    /**
     * The response message buffer
     */
    private static BlockingQueue<Message> responseBuffer = new LinkedBlockingQueue<Message>();

    /**
     * Allow access to user userName for the buffer queueName and return the token or throw
     * exception if user can't allow access to the buffer
     *
     * @param userName  user name of the client
     * @param password  password of the client
     * @param queueName the name of the buffer to use should allow access to
     * @return a token to use for server buffer access
     * @throws NotAuthorizedException throws in case of illegal access
     * @throws TException             throws in case of an connection error
     */
    public String login(String userName, String password, String queueName)
            throws NotAuthorizedException, TException {
        // check if this user is configured
        AuthenticationAdmin authAdmin = new AuthenticationAdmin();
        try {
            if (!authAdmin.login(userName, password, "localhost")) {
                throw new NotAuthorizedException("User '" + userName + "' not authorized to access" +
                        " buffers");
            }
        } catch (AuthenticationException e) {
            throw new NotAuthorizedException(e.getMessage());
        }
        SecureRandom rand = new SecureRandom();
        String token = Integer.toString(rand.nextInt());
        if (authorizedQueues.containsKey(queueName)) {
            // an already authorized user try to login again let him/her login again
            String oldToken = authorizedQueues.get(queueName);
            authorizedQueues.remove(queueName);
            if (requestBuffers.containsKey(oldToken)) {
                BlockingQueue<Message> oldBuffer = requestBuffers.remove(oldToken);
                // initialize the new buffer with the existing messages
                requestBuffers.put(token, new LinkedBlockingQueue<Message>(oldBuffer));
            }
        } else {
            // initialize the buffer for this request
            requestBuffers.put(token, new LinkedBlockingQueue<Message>());
        }
        authorizedQueues.put(queueName, token);


        return token;
    }

    /**
     * This will perform the exchange of data buffers between the client and the server.
     * This must do it's all operations without blocking as much as possible.
     *
     * @param responseMessageList The response buffer from thrift client
     * @param blockSize           size of the message bulk that need to return client
     * @param token               the token to authorize the exchange operation
     * @return a message bulk of size, 'size' if possible
     * @throws NotAuthorizedException in case the provided token is invalid
     * @throws TException             in case of an error
     */
    public List<Message> exchange(List<Message> responseMessageList,
                                  int blockSize,
                                  String token)
            throws NotAuthorizedException, TException {
        if (!authorizedQueues.containsValue(token)) {
            throw new NotAuthorizedException("You don't have required permission to access the buffers");
        }

        // if there is any response messages copy the response from the client to server's response
        // buffer and then hand over the copy/move operations into separate threads because
        // the processing of buffers are independent of the exchange operation
        if (responseMessageList.size() > 0) {
            workerPool.execute(new MessageCopyTask(responseMessageList, responseBuffer));
        }
        List<Message> requestMsgList = new ArrayList<Message>();

        // if there is any request messages send them to the client
        if (requestBuffers.size() > 0) {
            BlockingQueue<Message> requestBuffer = requestBuffers.get(token);
            if (requestBuffer != null) {
                try {
                    CGUtils.moveElements(requestBuffer, requestMsgList, blockSize);
                } catch (AxisFault axisFault) {
                    log.error("Error while moving elements :", axisFault);
                }
            }
        }
        return requestMsgList;
    }

    /**
     * Add a message into the server's request buffer, wait if the buffer is full.
     *
     * @param msg   the new Thrift message
     * @param token the token to look up the real buffer
     * @throws AxisFault in case of an error - for e.g. out of space in the queue
     */
    public static void addRequestMessage(Message msg, String token) throws AxisFault {
        try {
            BlockingQueue<Message> buffer = requestBuffers.get(token);
            if (buffer == null) {
                throw new AxisFault("The requested buffer is not found");
            }
            buffer.put(msg);
        } catch (Exception e) {
            throw new AxisFault(e.getMessage(), e);
        }
    }

    /**
     * Return the head of the response buffer, not that if there are no message it'll
     * block if no messages available.
     * <p/>
     * Note that since the buffers are static objects anybody has the access to buffers (and
     * the data inside them) but before deploy any custom be warned to review them!
     *
     * @return the message if there is any or null in case of this thread is interrupted
     */
    public static Message getResponseMessage() {
        try {
            // block if there is no messages
            return responseBuffer.take();
        } catch (InterruptedException e) {
            // ignore
        }
        return null;
    }

    /**
     * Return the request message buffer
     * <p/>
     * Note that since the buffers are static objects anybody has the access to buffers (and
     * the data inside them) but before deploy any custom be warned to review them!
     *
     * @param token the token to the buffer is bound to
     * @return request message buffer
     */
    public BlockingQueue<Message> getRequestBuffer(String token) {
        return requestBuffers.get(token);
    }

    /**
     * Return the token give the queue name
     *
     * @param queueName queue name
     * @return the secure token that this queue is bound
     */
    public static String getSecureUUID(String queueName) {
        return authorizedQueues.get(queueName);
    }

    /**
     * Return the list of request buffers
     * <p/>
     * Note that since the buffers are static objects anybody has the access to buffers (and
     * the data inside them) but before deploy any custom be warned to review them!
     *
     * @return request buffer list
     */
    public static Map<String, BlockingQueue<Message>> getRequestBuffers() {
        return requestBuffers;
    }

    /**
     * Add a new request buffer to the list of request buffers
     *
     * @param token the token for the new buffer
     */
    public static void addNewRequestBuffer(final String token) {
        requestBuffers.put(token, new LinkedBlockingQueue<Message>());
    }

    public static Map<String, Semaphore> getSemaphoreMap() {
        return semaphoreMap;
    }

    public static Map<String, Message> getMiddleBuffer() {
        return middleBuffer;
    }

    /**
     * An asynchronous message copy task among buffers
     */
    private class MessageCopyTask implements Runnable {
        private List<Message> src;
        private BlockingQueue<Message> dest;

        private MessageCopyTask(List<Message> src, BlockingQueue<Message> dest) {
            this.src = src;
            this.dest = dest;
        }

        public void run() {
            copyElements(this.src, this.dest);
        }

        private void copyElements(final List<Message> src, BlockingQueue<Message> dest) {
            dest.addAll(src);
        }
    }
}
