/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediator.cache.util;

import java.io.Serializable;

/**
 * Represents a SOAP Request Hash
 */
public class RequestHash implements Serializable {

	/**
	 * This holds the hash value of the request payload which is calculated form the specified DigestGenerator,
	 * and is used to index the cached response.
	 */
	public String requestHash;

	/**
	 * RequestHash constructor sets the hash of the request to the cache
	 *
	 * @param requestHash - hash of the request payload to be set as an String
	 */
	public RequestHash(String requestHash) {
		this.requestHash = requestHash;
	}

	/**
	 * This method gives the hash value of the request payload stored in the cache
	 *
	 * @return String hash of the request payload
	 */
	public String getRequestHash() {
		return requestHash;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestHash that = (RequestHash) o;
        return requestHash.equals(that.requestHash);
    }

    @Override
    public int hashCode() {
        return requestHash.hashCode();
    }
}
