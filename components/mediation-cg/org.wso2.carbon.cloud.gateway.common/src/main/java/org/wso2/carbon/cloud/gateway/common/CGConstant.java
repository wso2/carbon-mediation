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
package org.wso2.carbon.cloud.gateway.common;

public class CGConstant {

    /**
     * The csg thirft server port
     */
    public static final String THRIFT_SERVER_PORT = "cg-thrift-server-port";

    /**
     * Host name in which csg thrift server is running
     */
    public static final String THRIFT_SERVER_HOST_NAME = "cg-thrift-server-hostname";

    public static final String INITIAL_RECONNECT_DURATION = "cg-thirft-re-connect-duration";

    /**
     * Defines the timeout parameter for thrift server
     */
    public static final String CG_THRIFT_CLIENT_TIMEOUT = "cg-thrift-timeout";

    public static final int DEFAULT_PORT = 15001;

    public static final int DEFAULT_TIMEOUT = 60 * 15 * 1000;

    /**
     * The type of csg thrift server
     */
    public static final String SERVER_TYPE = "cg-thrift-server-type";

    /**
     * The type of protocol for thrift server
     */
    public static final String PROTOCOL_TYPE = "cg-thrift-protocol-type";

    /**
     * The type of transport for thrift server
     */
    public static final String TRANSPORT_TYPE = "cg-thrift-server-transport-type";

    /**
     * The time that the csg transport should block until a response comes
     */
    public static final String CG_SEMAPHORE_TIMEOUT = "cg-so-timeout";

    // worker thread pool default values
    public static final int WORKERS_CORE_THREADS = 20;
    public static final int WORKERS_MAX_THREADS = 500;
    public static final int CG_WORKERS_MAX_THREADS = 500;
    public static final int WORKER_KEEP_ALIVE = 5;
    public static final int WORKER_BLOCKING_QUEUE_LENGTH = -1;

    // csg transport sender thread pool param
    public static final String CG_T_CORE = "cg-t-core";
    public static final String CG_T_MAX = "cg-t-max";
    public static final String CG_T_ALIVE = "cg-t-alive-sec";
    public static final String CG_T_QLEN = "cg-t-qlen";

    // csg thrift transport thread pool param
    public static final String CG_THRIFT_T_CORE = "cg-thrift-t-core";
    public static final String CG_THRIFT_T_MAX = "cg-thrift-t-max";
    public static final String CG_THRIFT_T_ALIVE = "cg-thrift-t-alive";
    public static final String CG_THRIFT_T_QLEN = "cg-thrift-t-qlen";

    /**
     * No of concurrent consumers to poll the server from csg thrift transport receiver
     */
    public static final String NO_OF_CONCURRENT_CONSUMERS = "cg-thrift-t-c-c";

    /**
     * The no of messages that the  this client should read from the server
     */
    public static final String MESSAGE_BLOCK_SIZE = "cg-thrift-t-m-s";

    /**
     * The size of response message block that csg thrift transport should send to
     * client
     */
    public static final String RESPONSE_MESSAGE_BLOCK_SIZE = "cg-thrift-r-m-s";

    /**
     * Use this no of message block for processing
     */
    public static final String MESSAGE_PROCESSING_BLOCK_SIZE = "cg-thrift-p-r-m-s";

    public static final int DEFAULT_MESSAGE_PROCESSING_BLOCK_SIZE = 5;

    public static final String KEY_STORE_FILE_LOCATION = "cg-key-store-location";

    public static final String KEY_STORE_PASSWORD = "cg-key-store-password";

    public static final String TRUST_STORE_FILE_LOCATION = "cg-trust-store-location";

    public static final String TRUST_STORE_PASSWORD = "cg-trust-store-password";

    /**
     * The csg thrift server's buffer key
     */
    public static final String CG_POLLING_TRANSPORT_BUF_KEY = "CG_POLLING_TRANSPORT_BUF_KEY";

    public static final String PROGRESSION_FACTOR = "cg-progression-factor";

    public static final String TIME_UNIT = "cg-time-unit";

    public static final String NO_OF_SCHEDULER_TIME_UNITS = "no-of-cg-scheduler-time-units";

    public static final String NO_OF_IDLE_MESSAGE_TIME_UNITS = "no-of-idle-msg-time-units";

    public static final String MILLISECOND = "millisecond";

    public static final String SECOND = "second";

    public static final String MINUTE = "minute";

    public static final String HOUR = "hour";

    public static final String DAY = "day";

    public static final String CG_CORRELATION_KEY = "CG_CORRELATION_KEY";

    public static final String CG_TRANSPORT_PREFIX = "cg://";

    public static final String CG_SERVER_HOST = "host";

    public static final String CG_SERVER_PORT = "port";

    public static final String CG_SERVER_USER_NAME = "username";

    public static final String CG_SERVER_PASS_WORD = "password";

    public static final String CG_SERVER_NAME = "name";

    public static final String CG_SERVER_DOMAIN_NAME = "domain";

    /**
     * The registry path for CG storage
     */
    public static final String REGISTRY_CG_RESOURCE_PATH =
            "/repository/components/org.wso2.carbon.cloud.cg/";

    /**
     * The CG_TRANSPORT_NAME server collection for storing CSG server information
     */
    public static final String REGISTRY_SERVER_RESOURCE_PATH =
            REGISTRY_CG_RESOURCE_PATH + "servers";

    /**
     * The CSG flag collection for keeping track of published services etc..
     */
    public static final String REGISTRY_FLAG_RESOURCE_PATH = REGISTRY_CG_RESOURCE_PATH + "flags";

    /**
     * The path where WSDLs of published services are stored.
     */
    public static final String REGISTRY_CG_WSDL_RESOURCE_PATH = "/trunk/services/wsdls";

    /**
     * Client axis2.xml for admin services when using with ESB
     */
    public static final String CLIENT_AXIS2_XML = "repository/conf/axis2/axis2_client.xml";

    /**
     * CSG Transport name
     */
    public static final String CG_TRANSPORT_NAME = "cg";

    /**
     * The CSG Thrift transport name
     */
    public static final String CG_POLLING_TRANSPORT_NAME = "cgpolling";

    public static final String CG_SERVICE_STATUS_PUBLISHED = "Published";

    public static final String CG_SERVICE_STATUS_UNPUBLISHED = "Unpublished";

    public static final String CG_SERVICE_STATUS_AUTO_MATIC = "AutoMatic";

    public static final String CG_SERVICE_ACTION_PUBLISH = "publish";

    public static final String CG_SERVICE_ACTION_UNPUBLISH = "unpublish";

    public static final String CG_SERVICE_ACTION_AUTOMATIC = "automatic";

    public static final String CG_SERVICE_ACTION_MANUAL = "manual";

    public static final String CG_SERVICE_ACTION_RESTART = "restart";

    public static final String TOKEN = "token";

    /**
     * The csg server component connection read time out to csg agent when reading the private
     * service's WSDL
     */
    public static final String READTIMEOUT = "cg-connection-read-timeout";

    /**
     * Default value of {@link READTIMEOUT}
     */
    public static final int DEFAULT_READTIMEOUT = 100000;

    /**
     * The csg server component connection timeout to csg agent when reading the private
     * service's WSDL
     */
    public static final String CONNECTTIMEOUT = "cg-connection-connect-timeout";

    /**
     * Default value of {@link CONNECTTIMEOUT}
     */
    public static final int DEFAULT_CONNECTTIMEOUT = 200000;

    public static final int MAX_MESSAGE_PROCESSING_BLOCK_SIZE = 200;

    public static final String CG_PROXY_PREFIX = "cg-proxy-prefix";

    public static final String CG_PROXY_DELIMITER = "cg-proxy-delimiter";

    /**
     * The parameter that need to set in carbon.xml to provide the port of the thrift server
     */
    public static final String CG_CARBON_PORT = "Ports.CG";

    public static final String CG_SERVER_BEAN = "CG_SERVER_BEAN";

    /**
     * The no of worker that need to run for processing
     */
    public static final String NO_OF_DISPATCH_TASK = "cg-no-of-dispatch-worker";

    public static final String DEFAULT_CONTENT_TYPE = "text/xml";

    public enum DEPLOYMENT_TYPE {SERVICE, WEBAPP}

    public static final String CG_USER_NAME = "cg-user-name";

    public static final String DEFAULT_CG_USER = "cguser";

    public static final String CG_USER_PASSWORD = "cg-user-password";

    public static final String DEFAULT_CG_USER_PASSWORD = "wso2@123";

    public static final String CG_USER_PERMISSION_LIST = "cg-user-permission-list";

    public static final String ADMIN_PERMISSION_STRING = "/permission/admin";

    public static final String MANAGE_MEDIATION_PERMISSION_STRING =
            "/permission/admin/manage/mediation";

    public static final String MANAGE_SERVICE_PERMISSION_STRING =
            "/permission/admin/manage/modify/service";

    public static final String ADMIN_LOGIN_PERMISSION_STRING =
            "/permission/admin/login";

    public static final String ADMIN_PUBLISH_SERVICE_PERMISSION_STRING =
            "/permission/admin/manage/publish";

    public static final String ADMIN_UN_PUBLISH_SERVICE_PERMISSION_STRING =
            "/permission/admin/manage/un-publish";

    public static final String[] CG_PUBLISH_PERMISSION_LIST = new String[]
            {
                    ADMIN_LOGIN_PERMISSION_STRING,
                    MANAGE_SERVICE_PERMISSION_STRING,
                    ADMIN_PUBLISH_SERVICE_PERMISSION_STRING,
            };

    public static final String[] CG_UNPUBLISH_PERMISSION_LIST = new String[]
            {
                    ADMIN_LOGIN_PERMISSION_STRING,
                    MANAGE_SERVICE_PERMISSION_STRING,
                    ADMIN_UN_PUBLISH_SERVICE_PERMISSION_STRING,
            };

    public static final String[] CG_USER_DEFAULT_PERMISSION_LIST = new String[]
            {
                    ADMIN_LOGIN_PERMISSION_STRING,
                    MANAGE_MEDIATION_PERMISSION_STRING,
                    MANAGE_SERVICE_PERMISSION_STRING
            };

    public static final String CG_ROLE_NAME = "cloud-gateway-role";

    public static final String DEFAULT_CG_ROLE_NAME = "cloud-gateway";

    public static final String CG_PUBLISH_ROLE_NAME = "cg_publisher";

    public static final String CG_UNPUBLISH_ROLE_NAME = "cg_unpublisher";

    /**
     * The duration to suspend the CG polling task in milliseconds
     */
    public static final String CG_POLLING_TASK_SUSPEND_DURATION = "cg-polling-task-suspend-duration";

    /**
     * Prevents instantiation of this class.
     */
    private CGConstant() {

    }

}
