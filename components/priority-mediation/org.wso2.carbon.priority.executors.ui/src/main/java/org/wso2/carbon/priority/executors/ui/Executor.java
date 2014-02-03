/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.priority.executors.ui;

import org.apache.axiom.om.*;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class Executor {
    private Log log = LogFactory.getLog(Executor.class);

    public static final QName NAME_ATT = new QName(ExecutorConstants.NAME);
    public static final QName CLASS_ATT = new QName("class");

    public static final QName ATT_NAME    = new QName(ExecutorConstants.NAME);
    public static final QName ATT_VALUE   = new QName(ExecutorConstants.VALUE);

    public static final QName SIZE_ATT   = new QName(ExecutorConstants.SIZE);
    public static final QName PRIORITY_ATT   = new QName(ExecutorConstants.PRIORITY);

    public static final QName IS_FIXED_ATT = new QName(ExecutorConstants.IS_FIXED_SIZE);
    public static final QName BEFORE_EXECUTE_HANDLER =
            new QName(ExecutorConstants.BEFORE_EXECUTE_HANDLER);

    public static final QName NEXT_QUEUE_ATT = new QName(ExecutorConstants.NEXT_QUEUE);

    public static final QName MAX_ATT = new QName(ExecutorConstants.MAX);
    public static final QName CORE_ATT = new QName(ExecutorConstants.CORE);
    public static final QName KEEP_ALIVE_ATT = new QName(ExecutorConstants.KEEP_ALIVE);

    private int core = 20;
    private int max = 100;
    private int keepAlive = 5;

    private List<Queue> queues = new ArrayList<Queue>();

    private String algorithm = null;
    private String beforeExecuteHandler = null;
    private String name = "";
    private boolean isFixedSize = true;

    public static final String NAMESPACE = SynapseConstants.SYNAPSE_NAMESPACE;

    public void build(OMElement e) throws AxisFault {

        QName queuesQName = createQname(NAMESPACE, ExecutorConstants.QUEUES);
        QName queueQName = createQname(NAMESPACE, ExecutorConstants.QUEUE);

        QName threadsQName = createQname(NAMESPACE, ExecutorConstants.THREADS);


        OMAttribute nameAtt = e.getAttribute(NAME_ATT);
        if (nameAtt != null && !"".equals(nameAtt.getAttributeValue())) {
            setName(nameAtt.getAttributeValue());
        }

        // set the handler for calling before the message is put in to the queue
        OMAttribute handlerAtt = e.getAttribute(BEFORE_EXECUTE_HANDLER);
        if (handlerAtt != null) {
            beforeExecuteHandler = handlerAtt.getAttributeValue();
        }

        // create the queue configuration
        OMElement queuesEle = e.getFirstChildWithName(queuesQName);
        if (queuesEle != null) {
            OMAttribute nextQueueAtt = queuesEle.getAttribute(NEXT_QUEUE_ATT);

            if (nextQueueAtt != null) {
                 algorithm = nextQueueAtt.getAttributeValue();
            }

            OMAttribute fixedSizeAtt = queuesEle.getAttribute(IS_FIXED_ATT);
            if (fixedSizeAtt != null) {
                isFixedSize = Boolean.parseBoolean(fixedSizeAtt.getAttributeValue());
            }

            // create the queue configuration
            this.queues = createQueues(queueQName, queuesEle, isFixedSize);
        } else {
            handlerException("Queues configuration is mandatory");
        }

        OMElement threadsEle = e.getFirstChildWithName(threadsQName);
        if (threadsEle != null) {
            OMAttribute maxAttr = threadsEle.getAttribute(MAX_ATT);
            if (maxAttr != null) {
                setMax(Integer.parseInt(maxAttr.getAttributeValue()));
            }
            OMAttribute coreAttr = threadsEle.getAttribute(CORE_ATT);
            if (coreAttr != null) {
                setCore(Integer.parseInt(coreAttr.getAttributeValue()));
            }
            OMAttribute keepAliveAttr = threadsEle.getAttribute(KEEP_ALIVE_ATT);
            if (keepAliveAttr != null) {
                setKeepAlive(Integer.parseInt(keepAliveAttr.getAttributeValue()));
            }
        }
    }

    public OMElement serialize() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace nullNS = fac.createOMNamespace("", "");

        OMElement executorElement = createElement(ExecutorConstants.PRIORITY_EXECUTOR, NAMESPACE);

        if (name != null) {
            executorElement.addAttribute(fac.createOMAttribute(ExecutorConstants.NAME,
                    nullNS, name));
        }

        if (beforeExecuteHandler != null) {
            executorElement.addAttribute(fac.createOMAttribute(
                    ExecutorConstants.BEFORE_EXECUTE_HANDLER, nullNS,
                    beforeExecuteHandler));
        }

        // create the queues configuration
        OMElement queuesEle = createElement(ExecutorConstants.QUEUES, NAMESPACE);
        if (algorithm != null) {
            queuesEle.addAttribute(fac.createOMAttribute(ExecutorConstants.NEXT_QUEUE, nullNS,
                    algorithm));
        }

        if (!isFixedSize) {
            queuesEle.addAttribute(fac.createOMAttribute(ExecutorConstants.IS_FIXED_SIZE,
                    nullNS, Boolean.toString(false)));
        }

        for (Queue intQueue : queues) {
            OMElement queueEle = createElement(ExecutorConstants.QUEUE, NAMESPACE);

            if (isFixedSize) {
                queueEle.addAttribute(fac.createOMAttribute(ExecutorConstants.SIZE, nullNS,
                        Integer.toString(intQueue.getCapacity())));
            }

            queueEle.addAttribute(fac.createOMAttribute(ExecutorConstants.PRIORITY, nullNS,
                    Integer.toString(intQueue.getPriority())));

            queuesEle.addChild(queueEle);

        }
        executorElement.addChild(queuesEle);

        // create the Threads configuration
        OMElement threadsEle = createElement(ExecutorConstants.THREADS, NAMESPACE);
        threadsEle.addAttribute(fac.createOMAttribute(
                ExecutorConstants.MAX, nullNS, Integer.toString(max)));
        threadsEle.addAttribute(fac.createOMAttribute(
                ExecutorConstants.CORE, nullNS, Integer.toString(core)));
        threadsEle.addAttribute(fac.createOMAttribute(
                ExecutorConstants.KEEP_ALIVE, nullNS, Integer.toString(keepAlive)));

        executorElement.addChild(threadsEle);


        return executorElement;
    }

    private List<Queue> createQueues(
            QName qQName, OMElement queuesEle, boolean isFixedSize) throws AxisFault {

        List<Queue> internalQueues =
                new ArrayList<Queue>();

        Iterator it = queuesEle.getChildrenWithName(qQName);
        while (it.hasNext()) {
            int s = 0;
            int p = 0;

            OMElement qElement = (OMElement) it.next();
            String size = qElement.getAttributeValue(SIZE_ATT);
            String priority = qElement.getAttributeValue(PRIORITY_ATT);

            if (priority != null) {
                p = Integer.parseInt(priority);
            } else {
                handlerException("Priority must be specified");
            }

            if (size != null) {
                s = Integer.parseInt(size);
            } else if (isFixedSize) {
                handlerException("Queues should have a " + ExecutorConstants.SIZE);
            }

            Queue queue;
            if (isFixedSize) {
                queue = new Queue(p, s);
            } else {
                queue = new Queue(p);
            }

            internalQueues.add(queue);
        }

        return internalQueues;
    }

    private void handlerException(String s) throws AxisFault {
        log.error(s);
        throw new AxisFault(s);
    }

    private static QName createQname(String namespace, String name) {
        if (namespace == null) {
            return new QName(name);
        }
        return new QName(namespace, name);
    }

    private static OMElement createElement(String name, String namespace) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        if (namespace == null) {
            return fac.createOMElement(new QName(name));
        }

        OMNamespace omNamespace = fac.createOMNamespace(namespace, "");
        return fac.createOMElement(name, omNamespace);
    }

    public int getCore() {
        return core;
    }

    public int getMax() {
        return max;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getBeforeExecuteHandler() {
        return beforeExecuteHandler;
    }

    public void setCore(int core) {
        this.core = core;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setKeepAlive(int keep_alive) {
        this.keepAlive = keep_alive;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBeforeExecuteHandler(String beforeExecuteHandler) {
        this.beforeExecuteHandler = beforeExecuteHandler;
    }

    public List<Queue> getQueues() {
        return queues;
    }

    public void setQueues(List<Queue> queues) {
        this.queues = queues;
    }

    public boolean isFixedSize() {
        return isFixedSize;
    }

    public void setFixedSize(boolean fixedSize) {
        isFixedSize = fixedSize;
    }
}
