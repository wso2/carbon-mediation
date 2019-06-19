/*
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rest.api.ui.util;

public final class RestAPIConstants {

    public static int APIS_PER_PAGE = 10;
    public static final String VERSION_TYPE_NONE = "none";
    public static final String DEFAULT_PORT = "-1";

    public static String DEFAULT_JSON_SWAGGER = "{\n" +
            "  \"swagger\": \"2.0\",\n" +
            "  \"info\": {\n" +
            "    \"description\": \"Place Your Description Here\",\n" +
            "    \"version\": \"1.0.0\",\n" +
            "    \"title\": \"API_Name\"\n" +
            "  },\n" +
            "  \"host\": \"localhost:8280\",\n" +
            "  \"basePath\": \"/APIBasePath\",\n" +
            "  \"schemes\": [\n" +
            "    \"https\",\n" +
            "    \"http\"\n" +
            "  ],\n" +
            "  \"paths\": {\n" +
            "    \"/samplePath\": {\n" +
            "      \"post\": {\n" +
            "        \"parameters\": [\n" +
            "          {\n" +
            "            \"in\": \"body\",\n" +
            "            \"name\": \"body\",\n" +
            "            \"description\": \"Sample body parameter\",\n" +
            "            \"required\": true,\n" +
            "            \"schema\": {\n" +
            "              \"$ref\": \"#/definitions/sampleSchema\"\n" +
            "            }\n" +
            "          }\n" +
            "        ],\n" +
            "        \"responses\": {\n" +
            "          \"default\": {\n" +
            "            \"description\": \"default response\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"definitions\": {\n" +
            "    \"sampleSchema\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"properties\": {\n" +
            "        \"payload\": {\n" +
            "          \"type\": \"string\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

}
