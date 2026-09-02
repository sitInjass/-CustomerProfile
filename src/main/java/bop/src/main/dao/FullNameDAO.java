package bop.src.main.dao;

import bop.src.main.db.DBConnection;
import bop.src.main.util.ConfigLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * @author ِAshraf.M.Fahmawi
 */
public class FullNameDAO {

    private static final String BUILD_FULL_NAME_SQL
            = "SELECT ? || ' ' || ? AS FULL_NAME "
            + "FROM DUAL";

    public String buildFullName(
            String firstName,
            String secondName)
            throws SQLException {

        Connection connection
                = openConnection();

        if (connection == null) {
            throw new SQLException(
                    "Unable to open database connection"
            );
        }

        try (
                Connection autoCloseConnection = connection; PreparedStatement statement
                = autoCloseConnection.prepareStatement(
                        BUILD_FULL_NAME_SQL
                )) {
            statement.setString(1, firstName);
            statement.setString(2, secondName);

            try (ResultSet resultSet
                    = statement.executeQuery()) {

                if (!resultSet.next()) {
                    throw new SQLException(
                            "Full name was not returned"
                    );
                }

                return resultSet.getString(
                        "FULL_NAME"
                );
            }
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
