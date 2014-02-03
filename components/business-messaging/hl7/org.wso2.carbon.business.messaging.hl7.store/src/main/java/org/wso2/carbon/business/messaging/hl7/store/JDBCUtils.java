package org.wso2.carbon.business.messaging.hl7.store;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;

public class JDBCUtils {

    public static String getTableExistsQuery(String tableName) {
        return "SHOW TABLES LIKE '" + tableName + "';";
    }

    public static String getCreateTableQuery(String tableName) {
        return "CREATE TABLE "+tableName+" (id INT AUTO_INCREMENT, messageId VARCHAR(50), message TEXT, timestamp TIMESTAMP, PRIMARY KEY (id));";
    }

    public static String setMessage(String tableName, String messageId, String message) {
        return "INSERT INTO "+tableName+" (messageId, message) " +
               "VALUES (?, ?);";
    }





}
