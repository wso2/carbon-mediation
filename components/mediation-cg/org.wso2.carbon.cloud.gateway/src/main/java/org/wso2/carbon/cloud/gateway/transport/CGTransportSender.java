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
package org.wso2.carbon.cloud.gateway.transport;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.base.threads.WorkerPoolFactory;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.cloud.gateway.common.CGConstant;
import org.wso2.carbon.cloud.gateway.common.CGUtils;
import org.wso2.carbon.cloud.gateway.common.thrift.gen.Message;
import org.wso2.carbon.cloud.gateway.transport.server.CGThriftServerHandler;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.relay.BinaryRelayBuilder;
import org.wso2.carbon.relay.ExpandingMessageFormatter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * The CGTransport sender implementation. For one way messages this will just send the message to
 * to the Thrift server's request message buffer using an in memory copy and for two way messages
 * a semaphore will be blocked the current thread of execution until a response is received
 */
public class CGTransportSender extends AbstractTransportSender {
    /**
     * The time out for the semaphore
     */
    private long semaphoreTimeOut;

    /**
     * The periodic task to clean up the dead messages in case of back end is gone
     */
    private ScheduledExecutorService deadMsgCleanupScheduler;

    /**
     * The worker pool for processing
     */
    private WorkerPool workerPool;

    /**
     * The builder for pass through
     */
    private BinaryRelayBuilder builder;

    /**
     * The formatter for pass through
     */
    private ExpandingMessageFormatter formatter;

    private static Log log = LogFactory.getLog(CGTransportSender.class);

    @Override
    public void init(ConfigurationContext cfgCtx,
                     TransportOutDescription transportOut) throws AxisFault {
        super.init(cfgCtx, transportOut);
        builder = new BinaryRelayBuilder();
        formatter = new ExpandingMessageFormatter();

        semaphoreTimeOut = CGUtils.getLongProperty(CGConstant.CG_SEMAPHORE_TIMEOUT,
                86400L);

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String groupName = "CGTransportSender-tenant-" + tenantId + "-worker-thread-group";
        String groupId = "CGTransportSender-tenant-" + tenantId + "-worker";

        workerPool =
                WorkerPoolFactory.getWorkerPool(
                        CGUtils.getIntProperty(
                                CGConstant.CG_T_CORE, CGConstant.WORKERS_CORE_THREADS),
                        CGUtils.getIntProperty(
                                CGConstant.CG_T_MAX, CGConstant.WORKERS_MAX_THREADS),
                        CGUtils.getIntProperty(
                                CGConstant.CG_T_ALIVE, CGConstant.WORKER_KEEP_ALIVE),
                        CGUtils.getIntProperty(
                                CGConstant.CG_T_QLEN, CGConstant.WORKER_BLOCKING_QUEUE_LENGTH),
                        groupName,
                        groupId);

        //let the task run per once a day by default
        String timeUnitAsString = CGUtils.getStringProperty(
                CGConstant.TIME_UNIT, CGConstant.HOUR);

        // both the scheduler and the idle message time will be used the same time unit
        // given by CGConstant#TIME_UNIT
        long noOfSchedulerTimeUnits = CGUtils.getLongProperty(
                CGConstant.NO_OF_SCHEDULER_TIME_UNITS, 24L);
        long noOfIdleMessageUnits = CGUtils.getLongProperty(
                CGConstant.NO_OF_IDLE_MESSAGE_TIME_UNITS, 24L);

        checkSchedulePreConditions(timeUnitAsString, noOfIdleMessageUnits, noOfSchedulerTimeUnits);
        TimeUnit schedulerTimeUnit = getTimeUnit(timeUnitAsString);

        // schedule the message clean up task in order to avoid server goes OOM in case of the
        // back end server is offline
        deadMsgCleanupScheduler = Executors.newSingleThreadScheduledExecutor();
        deadMsgCleanupScheduler.scheduleWithFixedDelay(
                new DeadMessageCleanupTask(
                        CGThriftServerHandler.getRequestBuffers(),
                        getDurationAsMillisecond(schedulerTimeUnit, noOfIdleMessageUnits)),
                noOfSchedulerTimeUnits,
                noOfSchedulerTimeUnits,
                schedulerTimeUnit);


        // start the response message dispatching tasks
        int noOfDispatchingTask = CGUtils.getIntProperty(CGConstant.NO_OF_DISPATCH_TASK, 2);
        for (int i = 0; i < noOfDispatchingTask; i++) {
            workerPool.execute(new ResponseMessageDispatchingTask());
        }

        log.info("CGTransportSender started for tenant [" + tenantId + "]...");
    }


    @Override
    public void cleanup(MessageContext msgContext) throws AxisFault {
        super.cleanup(msgContext);
        if (!deadMsgCleanupScheduler.isShutdown()) {
            deadMsgCleanupScheduler.shutdown();
        }
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void sendMessage(MessageContext msgContext,
                            String targetEPR,
                            OutTransportInfo outTransportInfo) throws AxisFault {


        try {
            String requestUri = (String) msgContext.getProperty(
                    Constants.Configuration.TRANSPORT_IN_URL);
            if (requestUri == null) {
                handleException("The request URI is null");
            }
            
            String endpointPrefix = (String)msgContext.getProperty(NhttpConstants.ENDPOINT_PREFIX);
            if (endpointPrefix == null) {
                handleException("The ENDPOINT_PREFIX(EPR) is not found");
            }

            Object headers = msgContext.getProperty(
                    org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            if (headers == null) {
                handleException("Transport headers are null");
            }

            String requestMsgIdMsgId = msgContext.getMessageID();
            if (requestMsgIdMsgId == null) {
                requestMsgIdMsgId = UUID.randomUUID().toString();
            }

            Message thriftMsg = new Message();

            if (msgContext.isDoingMTOM()) {
                thriftMsg.setIsDoingMTOM(msgContext.isDoingMTOM());
                msgContext.setProperty(
                        org.apache.axis2.Constants.Configuration.ENABLE_MTOM,
                        org.apache.axis2.Constants.VALUE_TRUE);
            } else if (msgContext.isDoingSwA()) {
                thriftMsg.setIsDoingSwA(msgContext.isDoingSwA());
                msgContext.setProperty(
                        org.apache.axis2.Constants.Configuration.ENABLE_SWA,
                        org.apache.axis2.Constants.VALUE_TRUE);
            } else if (msgContext.isDoingREST()) {
                thriftMsg.setIsDoingREST(msgContext.isDoingREST());
            }

            thriftMsg.setHttpMethod((String) msgContext.getProperty(
                    Constants.Configuration.HTTP_METHOD));
            thriftMsg.setMessageId(requestMsgIdMsgId);
            thriftMsg.setEpoch(System.currentTimeMillis());

            // a class cast exception (if any) will be logged in case mismatch type is returned,
            // we will not worry about the type because correct type should be returned
            thriftMsg.setRequestURI(requestUri);
            thriftMsg.setSoapAction(msgContext.getSoapAction());

            OMOutputFormat format = BaseUtils.getOMOutputFormat(msgContext);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            formatter.writeTo(msgContext, format, out, false);
            thriftMsg.setMessage(out.toByteArray());
            String contentType = formatter.getContentType(msgContext, format, msgContext.getSoapAction());
            thriftMsg.setContentType(contentType);

            if (((Map) headers).containsKey(HTTP.CONTENT_TYPE)) {
                ((Map) headers).put(HTTP.CONTENT_TYPE, contentType);
            }
            thriftMsg.setTransportHeaders((Map) headers);

            Semaphore available = null;

            // The csg polling transport on the other side will directly use the EPR as the key for
            // message buffer. Although this introduce a tight couple between the CGTransport
            // and CGPollingTransport this is done this way to achieve maximum performance
            String token = CGThriftServerHandler.getSecureUUID(endpointPrefix);
            if (token == null) {
                handleException("No permission to access the server buffers");
            }

            boolean isOutIn = waitForSynchronousResponse(msgContext);
            if (isOutIn) {
                available = new Semaphore(0, true);
                CGThriftServerHandler.getSemaphoreMap().put(requestMsgIdMsgId, available);
            }
            CGThriftServerHandler.addRequestMessage(thriftMsg, token);
            try {
                if (isOutIn) {
                    // wait until the response is available, this thread will signal by the
                    // semaphore checking thread or send a timeout error if there is no response
                    // with the configured semaphore timeout or if the semaphore received an
                    // interrupted exception
                    try {
                        available.tryAcquire(semaphoreTimeOut, TimeUnit.SECONDS);
                    } catch (InterruptedException ignore) {
                    }
                    // make sure we don't run out of the main memory
                    CGThriftServerHandler.getSemaphoreMap().remove(requestMsgIdMsgId);
                    Message msg = CGThriftServerHandler.getMiddleBuffer().remove(requestMsgIdMsgId);
                    if (msg != null) {
                        handleSyncResponse(msgContext, msg, contentType);
                    } else {
                        // we don't have a response come yet, so send a fault to client
                        log.warn("The semaphore with id '" + requestMsgIdMsgId + "' was time out while "
                                + "waiting for a response, sending a fault to client..");
                        sendFault(msgContext,
                                new Exception("Times out occurs while waiting for a response"));
                    }
                }
            } catch (Exception e) {
                handleException("Could not process the response message", e);
            }
        } catch (Exception e) {
            handleException("Could not process the request message", e);
        }
    }

    private void handleSyncResponse(MessageContext requestMsgCtx, Message message, String requestContentType)
            throws AxisFault {
        try {
            MessageContext responseMsgCtx = createResponseMessageContext(requestMsgCtx);
            // set the message type of the original message, this is required for REST to work
            // properly
            responseMsgCtx.setProperty(Constants.Configuration.MESSAGE_TYPE,
                    requestMsgCtx.getProperty(Constants.Configuration.MESSAGE_TYPE));

            responseMsgCtx.setProperty(Constants.Configuration.CONTENT_TYPE,
                    requestMsgCtx.getProperty(Constants.Configuration.CONTENT_TYPE));

            if (message.isIsDoingMTOM()) {
                responseMsgCtx.setProperty(
                        org.apache.axis2.Constants.Configuration.ENABLE_MTOM,
                        org.apache.axis2.Constants.VALUE_TRUE);
            } else if (message.isIsDoingSwA()) {
                responseMsgCtx.setProperty(
                        org.apache.axis2.Constants.Configuration.ENABLE_SWA,
                        org.apache.axis2.Constants.VALUE_TRUE);
            }

            String contentType = message.getContentType();
            if (contentType == null) {
                contentType = inferContentType(requestContentType, responseMsgCtx);
            }

            ByteArrayInputStream inputStream = new ByteArrayInputStream(message.getMessage());
            // a class cast will be thrown if incorrect type was return, we are not worrying about
            // that because that should be handle by the builder
            SOAPEnvelope envelope = (SOAPEnvelope) builder.processDocument(
                    inputStream, contentType, responseMsgCtx);
            responseMsgCtx.setEnvelope(envelope);

            String charSetEnc = BuilderUtil.getCharSetEncoding(contentType);
            if (charSetEnc == null) {
                charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }
            responseMsgCtx.setProperty(
                    Constants.Configuration.CHARACTER_SET_ENCODING,
                    contentType.indexOf(HTTP.CHARSET_PARAM) > 0
                            ? charSetEnc : MessageContext.DEFAULT_CHAR_SET_ENCODING);
            responseMsgCtx.setProperty(
                    MessageContext.TRANSPORT_HEADERS, message.getTransportHeaders());

            if (message.getSoapAction() != null) {
                responseMsgCtx.setSoapAction(message.getSoapAction());
            }
            AxisEngine.receive(responseMsgCtx);

        } catch (AxisFault axisFault) {
            handleException("Could not handle the response message ", axisFault);
        }
    }

    private void sendFault(MessageContext msgContext, Exception e) {
        try {
            MessageContext faultContext = MessageContextBuilder.createFaultMessageContext(
                    msgContext, e);
            faultContext.setProperty("ERROR_MESSAGE", e.getMessage());
            faultContext.setProperty("SENDING_FAULT", Boolean.TRUE);
            AxisEngine.sendFault(faultContext);
        } catch (AxisFault axisFault) {
            log.fatal("Could not create the fault message.", axisFault);
        }
    }

    /**
     * A periodic task which submit the response for processing
     */
    private class ResponseMessageDispatchingTask implements Runnable {

        public void run() {
            while (true) {
                // if there is no response messages the current thread will block,
                // BlockingQueue#drainTo drains a block of message but it doesn't seems block
                // without eating up the CPU
                Message msg = CGThriftServerHandler.getResponseMessage();
                if (msg != null) {
                    workerPool.execute(new ResponseMessageProcessingTask(msg));
                }
            }
        }
    }

    /**
     * The task which send the response message back to client
     */
    private class ResponseMessageProcessingTask implements Runnable {
        private Message msg;

        private ResponseMessageProcessingTask(Message msg) {
            this.msg = msg;
        }

        public void run() {
            String msgId = msg.getMessageId();
            Map<String, Semaphore> semaphoreMap = CGThriftServerHandler.getSemaphoreMap();
            Set<String> keySet = semaphoreMap.keySet();
            if (keySet.contains(msgId)) {
                CGThriftServerHandler.getMiddleBuffer().put(msgId, msg);
                Semaphore semaphore = semaphoreMap.get(msgId);
                semaphore.release();
            } else {
                log.warn("A response was received with id '" + msgId + "', but no registered" +
                        " call back found. Message will be ignored!");
            }
        }
    }

    /**
     * A cleanup task to remove the messages from the server buffers in case the back end has gone
     */
    private class DeadMessageCleanupTask implements Runnable {
        private Map<String, BlockingQueue<Message>> requestMessageBuffers;
        private long idleMessageTime;

        private DeadMessageCleanupTask(Map<String, BlockingQueue<Message>> requestMessageBuffers,
                                       long idleMessageTime) {
            this.requestMessageBuffers = requestMessageBuffers;
            this.idleMessageTime = idleMessageTime;
        }

        public void run() {
            long currentTime = System.currentTimeMillis();
            for (Map.Entry<String, BlockingQueue<Message>> entry : requestMessageBuffers.entrySet()) {
                BlockingQueue<Message> buffer = entry.getValue();

                Message msg = buffer.peek();
                while (msg != null && (msg.getEpoch() + idleMessageTime) > currentTime) {
                    String msgID = msg.getMessageId();
                    log.info("The cleaning up task is sweeping the message with id '"
                            + msgID + "' and callback will be removed too.");
                    CGThriftServerHandler.getSemaphoreMap().remove(msgID);
                    buffer.remove();
                    msg = buffer.peek();
                }
            }
        }
    }

    private static TimeUnit getTimeUnit(String timeUnit) {
        if (timeUnit.equals(CGConstant.MILLISECOND)) {
            return TimeUnit.MILLISECONDS;
        } else if (timeUnit.equals(CGConstant.SECOND)) {
            return TimeUnit.SECONDS;
        } else if (timeUnit.equals(CGConstant.MINUTE)) {
            return TimeUnit.MINUTES;
        } else if (timeUnit.equals(CGConstant.HOUR)) {
            return TimeUnit.HOURS;
        } else if (timeUnit.equals(CGConstant.DAY)) {
            return TimeUnit.DAYS;
        } else {
            // the default
            return TimeUnit.DAYS;
        }
    }

    private static void checkSchedulePreConditions(String timeUnits,
                                                   long noOfIdleMsgTimeUnits,
                                                   long noOfSchedulerTimeUnits) throws AxisFault {
        if (noOfIdleMsgTimeUnits > noOfSchedulerTimeUnits) {
            String msg = "A possible configuration error. The ScheduledExecutorService is " +
                    "configured to run once a every '" + noOfSchedulerTimeUnits + "' " +
                    (noOfSchedulerTimeUnits == 1 ? timeUnits : timeUnits + "s") + " to sweep " +
                    "messages which are '" + noOfIdleMsgTimeUnits + "' " +
                    (noOfIdleMsgTimeUnits == 1 ? timeUnits : timeUnits + "s") + "old. The " +
                    "scheduler may idle without doing any actual work!";
            log.error(msg);
            throw new AxisFault(msg);
        }
    }

    private static long getDurationAsMillisecond(TimeUnit timeUnit, long duration) {
        if (timeUnit == TimeUnit.MILLISECONDS) {
            return TimeUnit.MILLISECONDS.toMillis(duration);
        } else if (timeUnit == TimeUnit.SECONDS) {
            return TimeUnit.SECONDS.toMillis(duration);
        } else if (timeUnit == TimeUnit.MINUTES) {
            return TimeUnit.MINUTES.toMillis(duration);
        } else if (timeUnit == TimeUnit.HOURS) {
            return TimeUnit.HOURS.toMillis(duration);
        } else if (timeUnit == TimeUnit.DAYS) {
            return TimeUnit.DAYS.toMillis(duration);
        } else {
            log.warn("TimeUnit type '" + timeUnit + "' is not supported. Default TimeUnit will be " +
                    "assumed");
            return TimeUnit.DAYS.toMillis(duration);
        }
    }

    private String inferContentType(String requestContentType, MessageContext responseMsgCtx) {
        // Try to get the content type from the message context
        Object cTypeProperty = responseMsgCtx.getProperty(Constants.Configuration.CONTENT_TYPE);
        if (cTypeProperty != null) {
            return cTypeProperty.toString();
        }
        // Try to get the content type from the axis configuration
        Parameter cTypeParam = cfgCtx.getAxisConfiguration().getParameter(
                Constants.Configuration.CONTENT_TYPE);
        if (cTypeParam != null) {
            return cTypeParam.getValue().toString();
        }

        if (requestContentType != null) {
            return requestContentType;
        }

        // Unable to determine the content type - Return default value
        return CGConstant.DEFAULT_CONTENT_TYPE;
    }

    private static String calculateBufferKey(String fullEPR) {
        // cg://server1/SimpleStockQuoteService/operation1/argument1
        // 5 is the length(cg://) used this way for better performance
        String split[] = fullEPR.substring(5).split("/");
        StringBuilder buf = new StringBuilder(CGConstant.CG_TRANSPORT_PREFIX);
        // following is not thread safe, but there will be a thread per request
        // http://vanillajava.blogspot.com/2012/08/java-memes-which-refuse-to-die.html
        buf.append(split[0]).append("/").append(split[1]);
        return buf.toString();
    }
}
