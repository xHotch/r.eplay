package at.ac.tuwien.sepm.assignment.group.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class JDBCConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${application.db.connection.string}")
    private String connectionString;

    private Connection connection;

    public Connection getConnection() throws SQLException {
        LOG.debug("Get database connection");
        if (connection == null) {
            LOG.debug("try to get new database connection with: {}", connectionString);
            connection = DriverManager.getConnection(connectionString);
        }
        LOG.debug("Database connection status: {}" ,!connection.isClosed());

        return connection;
    }

    @PreDestroy
    public void closeConnection() {
        LOG.info("closing db connection");
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOG.error("Failed to close connection '{}'", e.getMessage(), e);
            }
            connection = null;
        }
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
}
