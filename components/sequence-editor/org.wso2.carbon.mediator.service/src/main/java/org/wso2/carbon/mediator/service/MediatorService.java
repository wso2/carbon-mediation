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

package org.wso2.carbon.mediator.service;

import org.wso2.carbon.mediator.service.ui.Mediator;

/**
 * This interface provides the base for the mediator OSGi Service and each and
 * every mediator which has a user interface on the <code>Carbon sequence editor</code>
 * has to implement this interface and that implementation has to be registered as an
 * OSGi Service.</p>
 *
 * <p>This provides the basic structure of a mediator for the sequence editor framework,
 * and this also enables to add the mediators to the sequence editor user interface
 * dynamically at runtime.
 *
 * @see AbstractMediatorService
 */
@SuppressWarnings({"UnusedDeclaration"})
public interface MediatorService {

    /**
     * This gives the mediator serialization tag local name.
     *
     * <em>Example: if you take the {@link org.apache.synapse.mediators.builtin.LogMediator}
     * derived from the {@link org.apache.synapse.config.xml.LogMediatorSerializer} the
     * tag local name is <code>log</code> </em>
     *
     * @return tag local name of the mediator tag QName
     */
    public String getTagLocalName();

    /**
     * This gives the display name for the mediator in the add mediator menu, and this can
     * be any {@link String}. It is recommended to put a meaning full descriptive short name
     * as the display name
     *
     * @return display name in the add mediator menu of the mediator
     */
    public String getDisplayName();

    /**
     * This should be equivalent to {@link org.apache.synapse.Mediator#getType()} of the
     * mediator. The value of this is generally the class name without the package declaration.
     *
     * <em>Example: logical name of the {@link org.apache.synapse.mediators.builtin.LogMediator}
     * is <code>LogMediator</code> </em>
     *
     * @return logical name of the mediator
     */
    public String getLogicalName();

    /**
     * Gives the mediator categorization in the add mediator menu. This should be a descriptive
     * meaning full and short text and it is recommended to use existing group names if possible,
     * to reduce the number of groups in the add mediator menu.</p>
     *
     * <p>Defined group names are:
     *      <ul>
     *          <li>Core</li>
     *          <li>Extension</li>
     *          <li>Filter</li>
     *          <li>Transform</li>
     *          <li>Advanced</li>
     *      <ul>
     * Of course it is possible to add a new group by putting any String to this.
     *
     * @return group name of the mediator to which this mediator is categorized in the
     *      add mediator menu
     */
    public String getGroupName();      


    /*
     * Get the folder in which this mediators, UI components are in. We construct a folder name
     * getUIFolderName() + "-mediator". In this folder we assume there are files called
     * edit-mediator.jsp,
     * update-mediator.jsp.
     * Files. Also if the mediator has icons they should be in the images folder with the
     * mediator-icon.gif name.
     * If the mediator has context sensitive documentation, they should be in the docs folder
     * with the name userguide.html.
     */
    public String getUIFolderName();

    /**
     * Whether this particular mediator can have siblings? <em>Example of this usage is
     * <code>then</code> child of the {@link org.apache.synapse.mediators.filters.FilterMediator}
     * cannot have mediator siblings.</p>
     *
     * <p>This can also be used to enforce logical constraints like the
     * {@link org.apache.synapse.mediators.builtin.DropMediator}, it is not logically effective
     * to have siblings after the <code>drop</code>
     *
     * @return true if the particular mediator can have siblings
     */
    public boolean isAddSiblingEnabled();

    /**
     * Whether this particular mediator can have children? <em>Example of this usage is
     * <code>filter</code> {@link org.apache.synapse.mediators.filters.FilterMediator}
     * it cannot have children except for the <code>then</code> and <code>else</code>
     * mediators.
     *
     * @return true if the particular mediator can have children
     */
    public boolean isAddChildEnabled();

    /**
     * Is it possible to move this mediator in the sequence tree? <em>Example of this usage is
     * <code>default</code> child of a {@link org.apache.synapse.mediators.filters.SwitchMediator}
     * it cannot be moved, there is no meaning of movement for the <code>default</code>
     *
     * @return true if the particular mediator is allowed to move over
     */
    public boolean isMovingAllowed();

    /**
     * If the mediator has editable configurations this method should return true. For example
     * mediators like {@link org.apache.synapse.mediators.filters.InMediator} cannot be updated.
     * @return true if the mediator is editable
     */
    public boolean isEditable();

    /**
     * Retrieves a default new mediator instances of the representing mediator. This method
     * is used by the mediator addition and will be called to get a new instance of the
     * mediator.</p>
     *
     * <p>It is recommended to fill the required fields of the mediator with the default values
     * if possible before returning the new instance, so that the user can just save this
     * mediator if he/she is not smart.
     *
     * @return new instance of the mediator with the default values filled
     */
    public Mediator getMediator();

    /**
     * Some mediator updates require a refresh in the sequence editor. i.e ThrottleMediator.
     * If the mediator update requires a update in the sequence editor this method should
     * return true.
     * @return if mediator update requires a sequence editor refresh
     */
    public boolean isSequenceRefreshRequired();
}
