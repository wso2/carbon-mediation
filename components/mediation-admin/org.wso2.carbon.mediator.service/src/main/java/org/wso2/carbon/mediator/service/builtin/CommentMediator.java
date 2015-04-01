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
 **/

package org.wso2.carbon.mediator.service.builtin;

import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;

/**
 * Comment Mediator handles comment nodes in Synapse Configurations
 */
public class CommentMediator extends AbstractListMediator {

    private String commentText;

    /**
     * Serialize Mediator into the repective OMElement structire and add to the provided parent
     *
     * @param parent if present the serialize node will be added to the parent
     * @return Serialized OMElement structure
     */
    public OMElement serialize(OMElement parent) {
        OMComment commentElem = fac.createOMComment(parent, this.getCommentText());
        parent.addChild(commentElem);
        return parent;
    }

    /**
     * Build the mediator with given OMElement node
     *
     * @param elem synapse mediator configuration
     */
    public void build(OMElement elem) {
    }

    /**
     * Build the mediator with given OMComment node
     *
     * @param elem synapse mediator configuration
     */
    public void build(OMComment elem) {
        this.setCommentText(elem.getValue());
    }

    /**
     * Returns TagLocalName of the mediator
     *
     * @return
     */
    public String getTagLocalName() {
        return "comment";
    }

    /**
     * Returns the comment text value
     *
     * @return String value of comment text
     */
    public String getCommentText() {
        return commentText;
    }

    /**
     * Set the comment text value
     *
     * @param commentText value
     */
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
}
