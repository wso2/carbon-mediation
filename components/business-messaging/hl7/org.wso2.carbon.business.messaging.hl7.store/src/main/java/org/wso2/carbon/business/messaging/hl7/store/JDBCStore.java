package org.wso2.carbon.business.messaging.hl7.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.message.MessageConsumer;
import org.apache.synapse.message.MessageProducer;
import org.apache.synapse.message.store.MessageStore;
import org.apache.synapse.message.store.Constants;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

public class JDBCStore implements MessageStore {

    private static final Log logger = LogFactory.getLog(JDBCStore.class.getName());

    private boolean isInitialized = false;
    private String name;
    private String description;
    private Map<String, Object> parameters;
    private String fileName;

    private int maxProducerId = Integer.MAX_VALUE;
    /** Message producer id */
    private AtomicInteger producerId = new AtomicInteger(0);
    /** Message consumer id */
    private AtomicInteger consumerId = new AtomicInteger(0);

    private Connection conn;

    private String driverClass = "com.mysql.jdbc.Driver";
    private String dbUrl = "jdbc:mysql://localhost/test";
    private String dbUser = "root";
    private String dbPass = "root";

    private void parseParameters() {
        this.driverClass = (String) parameters.get("jdbcDriverClass");
        this.dbUrl = (String) parameters.get("jdbcUrl");
        this.dbUser = (String) parameters.get("jdbcUser");
        this.dbPass = (String) parameters.get("jdbcPass");

        if(this.driverClass == null || this.dbUrl == null) {
            logger.error("Required parameters jdbcDriverClass and jdbcUrl missing");
        }
    }

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        if(synapseEnvironment == null) {
            logger.error("Cannot initialize HL7 JDBC Store");
            return;
        }

        parseParameters();

        this.isInitialized = initJDBCStore();

        if(this.isInitialized) {
            logger.info(toString() + ". Initialized... ");
        } else {
            logger.warn(toString() + ". Initialization Failed... ");
        }
    }

    public Connection getConnection() {
        return this.conn;
    }

    private boolean initJDBCStore() {
        try {
            Class.forName(driverClass);
            this.conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);

            if (!initTables()) {
                logger.error("Failed to initialize data tables for '" + dbUrl + "'");
                return false;
            } else {
                return true;
            }

        } catch (ClassNotFoundException e) {
            logger.error("Could not find JDBC driver '" + driverClass + "'");
            return false;
        } catch (SQLException e) {
            logger.error("Error on connection to '" + dbUrl + "'. " + e.getMessage());
            return false;
        }
    }

    private boolean initTables() throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet result = statement.executeQuery(JDBCUtils.getTableExistsQuery(getName()));

        if(!result.next()) {
            statement.execute(JDBCUtils.getCreateTableQuery(getName()));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void destroy() {
        try {
            if (!this.conn.isClosed()) {
                this.conn.close();
            }
        } catch (SQLException e) {
            logger.error("Error while destroying connection to '" + dbUrl + "'");
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return this.description;
    }


    @Override
    public MessageProducer getProducer() {
        JDBCProducer producer = new JDBCProducer(this);
        producer.setId(nextProducerId());

        return producer;
    }

    @Override
    public MessageConsumer getConsumer() {
        return null;
    }

    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    @Override
    public int getType() {
        return Constants.JDBC_MS;
    }

    @Override
    public MessageContext remove() throws NoSuchElementException {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public MessageContext remove(String s) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public MessageContext get(int i) {
        return null;
    }

    @Override
    public List<MessageContext> getAll() {
        return null;
    }

    @Override
    public MessageContext get(String s) {
        return null;
    }

    @Override
    public String toString() {
        return "HL7 Store [" + getName() + "]";
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    private int nextConsumerId() {
        int id = consumerId.incrementAndGet();
        return id;
    }

    private int nextProducerId() {
        int id = producerId.incrementAndGet();
        if (id == maxProducerId) {
            logger.info("Setting producer ID generator to 0...");
            producerId.set(0);
            id = producerId.incrementAndGet();
        }
        return id;
    }

}
