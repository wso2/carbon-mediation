/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediator.cache.digest;

import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.mediator.cache.CachingException;

import java.io.Serializable;

/**
 * This is the primary interface for the DigestGenerator which is the unique SOAP request
 * identifier generation interface to be used by the CacheManager inorder to generate a
 * unique identifier key for the normalized XML/SOAP message. This has to be serializable
 * because the DigestGenerator implementations has to be serializable to support clustered
 * caching
 *
 * @see java.io.Serializable
 */
public interface DigestGenerator extends Serializable {

    /**
     * This method will be implemented to return the unique XML node identifier
     * on the given XML node
     * 
     * @param msgContext - MessageContext on which the unique identifier will be generated
     * @return Object representing the unique identifier for the msgContext
     * @throws CachingException if there is an error in generating the digest key
     */
    String getDigest(MessageContext msgContext) throws CachingException;
}
