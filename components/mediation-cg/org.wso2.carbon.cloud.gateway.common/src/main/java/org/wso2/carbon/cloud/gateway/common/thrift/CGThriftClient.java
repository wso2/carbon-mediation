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
package org.wso2.carbon.cloud.gateway.common.thrift;

import org.apache.thrift.TException;
import org.wso2.carbon.cloud.gateway.common.thrift.gen.CloudGatewayService;
import org.wso2.carbon.cloud.gateway.common.thrift.gen.Message;
import org.wso2.carbon.cloud.gateway.common.thrift.gen.NotAuthorizedException;

import java.util.List;

/**
 * A wrapper client for the native thrift client
 */
public class CGThriftClient {
    private CloudGatewayService.Client client;

    public CGThriftClient(CloudGatewayService.Client client) {
        this.client = client;
    }

    /**
     * The login method. Returns a token for exchange operation upon successful login or throws
     * an exception in case of an error
     * @param userName user name of the user
     * @param passWord pass word of the user
     * @param queueName the buffer name to which user should allow access to
     * @return token for exchange operation
     * @throws NotAuthorizedException in case of user is not allowed
     * @throws TException in case of an connection error
     */
    public String login(final String userName, final String passWord, final String queueName)
            throws NotAuthorizedException, TException {
        return client.login(userName, passWord, queueName);
    }

    /**
     * The exchange operation, this will pass the response buffer to the server and will
     * receive request buffer from the server
     *
     * @param src       the response buffer to pass to the server
     * @param size      the size of the request message block that server should return
     * @param token     the token for successive exchange operations
     * @return the request buffer from server
     * @throws NotAuthorizedException in case the token in invalid
     * @throws TException throws in case of an error
     */
    public List<Message> exchange(List<Message> src, int size, String token)
            throws NotAuthorizedException, TException {
        return client.exchange(src, size, token);
    }
}
