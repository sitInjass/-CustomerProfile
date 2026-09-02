package bop.src.main.dao;

import bop.src.main.db.DBConnection;
import bop.src.main.util.ConfigLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * 
 * @author ِAshraf.M.Fahmawi
 */
public class DatabaseTimeDAO {

    private static final String SELECT_DATABASE_TIME_SQL
            = "SELECT SYSTIMESTAMP "
            + "FROM DUAL";

    public Timestamp getDatabaseTime()
            throws SQLException {

        Connection connection
                = openConnection();

        if (connection == null) {
            throw new SQLException(
                    "Unable to open database connection"
            );
        }

        try (
            Connection autoCloseConnection = connection;

            PreparedStatement statement
                    = autoCloseConnection.prepareStatement(
                            SELECT_DATABASE_TIME_SQL
                    );

            ResultSet resultSet
                    = statement.executeQuery()
        ) {

            if (!resultSet.next()) {
                throw new SQLException(
                        "Database timestamp was not returned"
                );
            }

            return resultSet.getTimestamp(1);
        }
    }

    private Connection openConnection() {

        return DBConnection.openConnection(
                ConfigLoader.getProperty("DB_URL"),
                ConfigLoader.getProperty("DB_USER_NAME"),
                ConfigLoader.getProperty("DB_PASSWORD"),
                ConfigLoader.getProperty("DB_DRIVER_NAME")
        );
    }
}