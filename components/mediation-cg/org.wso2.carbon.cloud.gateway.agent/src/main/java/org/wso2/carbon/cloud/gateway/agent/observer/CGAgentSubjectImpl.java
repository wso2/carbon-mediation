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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation for {@link CGAgentSubject}
 */
public class CGAgentSubjectImpl implements CGAgentSubject {
    private static CGAgentSubjectImpl instance;
    private List<CGAgentObserver> observers;
    private Lock lock = new ReentrantLock();

    private CGAgentSubjectImpl() {
        observers = Collections.synchronizedList(new ArrayList<CGAgentObserver>());
    }

    public static CGAgentSubjectImpl getInstance() {
        if (instance == null) {
            synchronized (CGAgentSubjectImpl.class) {
                if (instance == null) {
                    instance = new CGAgentSubjectImpl();
                }
            }
        }
        return instance;
    }

    public void addObserver(CGAgentObserver o) {
        lock.lock();
        try {
            if (!observers.contains(o)) {
                observers.add(o);
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeObserver(CGAgentObserver o) {
        lock.lock();
        try {
            observers.remove(o);
        } finally {
            lock.unlock();
        }
    }

    public void connected(String host, int port) {
        lock.lock();
        try {
            notifyObservers(host, port);
        } finally {
            lock.unlock();
        }
    }

    private void notifyObservers(String host, int port) {
        Iterator<CGAgentObserver> cgAgentObserverIterator = observers.iterator();
        while (cgAgentObserverIterator.hasNext()) {
            CGAgentObserver observer = cgAgentObserverIterator.next();
            if (host != null && host.equals(observer.getHostName()) && port == observer.getPort()) {
                observer.update(this);
                cgAgentObserverIterator.remove();
            }
        }
    }
}
