/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.websocket;

import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;

import java.lang.reflect.Constructor;

public class HandshakeHandlerBuilderUtil {
    private static final Log log = LogFactory.getLog(HandshakeHandlerBuilderUtil.class);

    public static ChannelInboundHandlerAdapter stringToHandshakeHandlers(String handlerClass) {
        ChannelInboundHandlerAdapter hand = null;
        if (handlerClass != null) {


                try {
                    Class c = Class.forName(handlerClass);
                    Constructor cons = c.getConstructor();
                    ChannelInboundHandlerAdapter handlerInstance = (ChannelInboundHandlerAdapter) cons.newInstance();
                    return handlerInstance;
                } catch (ClassNotFoundException e) {
                    String msg = "Class " + handlerClass +
                            " not found. Please check the required class is added to the classpath.";
                    log.error(msg, e);
                    throw new SynapseException(e);
                } catch (NoSuchMethodException e) {
                    String msg = "Required constructor is not implemented.";
                    log.error(msg, e);
                    throw new SynapseException(e);
                } catch (InstantiationException |
                        IllegalAccessException |
                        java.lang.reflect.InvocationTargetException e) {
                    String msg = "Couldn't create the class instance.";
                    log.error(msg, e);
                    throw new SynapseException(e);
                }

        }
        return hand;
    }

}
