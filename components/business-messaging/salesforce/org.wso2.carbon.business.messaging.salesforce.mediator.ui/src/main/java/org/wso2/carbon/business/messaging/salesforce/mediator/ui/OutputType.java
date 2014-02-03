/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.business.messaging.salesforce.mediator.ui;

import org.apache.synapse.util.xpath.SynapseXPath;

/**
 * This class stores output parameter type information
 * handles input parameter processing
 */
public class OutputType extends Type {

    /**
     * <p>
     * This holds refernce to the key under which reponse or part of the reponse would be stored.
     * </p>
     */
    private String targetKey;
    /**
     * <p>
     * XPath describing the element or the attribute of the message which will
     * be matched against the <code>source-xpath</code> to check the matching.
     * </p>
     *
     * @see org.apache.synapse.util.xpath.SynapseXPath
     */
    private SynapseXPath sourceXpath;
    /**
     * <p>
     * XPath describing the reponse message  will
     * be matched against the <code>target-xpath</code>.
     * on a sucessful match result of corresponding  <code>source-xpath</code> will be
     * embedded inside the matched element
     * </p>
     *
     * @see org.apache.synapse.util.xpath.SynapseXPath
     */
    private SynapseXPath targetXpath;
    /**
     * output type name
     */
    private static final String NAME = "key";


    /**
     * @return the type
     */


    /**
     * @return the targetKey
     */
    public String getTargetKey() {
        return targetKey;
    }

    /**
     * @param targetKey the targetKey to set
     */
    public void setTargetKey(String targetKey) {
        this.targetKey = targetKey;
    }

    /**
     * @param sourceXpath
     */
    public void setSourceXpath(SynapseXPath sourceXpath) {
        this.sourceXpath = sourceXpath;
    }

    /**
     * @return source xpath expression
     */
    public SynapseXPath getSourceXPath() {
        return sourceXpath;
    }

    /**
     * @return target xpath expression
     */
    public SynapseXPath getTargetXPath() {
        return targetXpath;
    }

    /**
     * @param targetXpath
     */
    public void setTargetXpath(SynapseXPath targetXpath) {
        this.targetXpath = targetXpath;
    }

    /**
     * @return
     */
    public String getName() {
        return NAME;
    }

}
