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

package org.wso2.carbon.sequences.ui;

/**
 * Holds the constants for the <em>sequence-editor</em> component user interface
 */
@SuppressWarnings({"UnusedDeclaration"})
public class SequenceEditorConstants {

    public static final String REQ_PARAM_ACTION = "action";
    public static final String ACTION_PARAM_VALUE_EDIT = "edit";
    public static final String ACTION_PARAM_VALUE_ADD = "add";
    public static final String ACTION_PARAM_VALUE_ANONIFY = "anonify";

    public static final String ADD_CHILD_TYPE = "child";
    public static final String ADD_SYBLING_TYPE = "sibling";

    public static final String STATISTICS_TYPE = "statistics";
    public static final String TRACING_TYPE = "tracing";

    public static final String ENABLE_ACTION = "enable";
    public static final String DISABLE_ACTION = "disable";

    //pagination, item count per page
    public static final int SEQUENCE_PER_PAGE = 10;
}
