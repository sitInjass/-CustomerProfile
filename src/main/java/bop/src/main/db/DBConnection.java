package bop.src.main.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 
 * @author ِAshraf.M.Fahmawi
 */
public class DBConnection {

    public static Connection openConnection(String InputURL, String InputUserName, String inputPassword, String oracleDriver) {
        Connection connection = null;
        String URL = InputURL;

        try {
            String USERNAME = InputUserName;
            String PASSWORD = inputPassword;
            Class.forName(oracleDriver);
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }

    public static void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
