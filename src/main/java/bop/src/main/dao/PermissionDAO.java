package bop.src.main.dao;

import bop.src.main.db.DBConnection;
import bop.src.main.util.ConfigLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * @author Ashraf.M.Fahmawi
 */
public class PermissionDAO {

    private static final String CHECK_PERMISSION_SQL
            = "SELECT 1 "
            + "  FROM ADM_USERS U "
            + "  JOIN ADM_USER_GROUPS UG "
            + "    ON UG.USER_ID = U.USER_ID "
            + "   AND UG.IS_ACTIVE = 1 "
            + "   AND (UG.EXPIRY_DATE IS NULL "
            + "        OR UG.EXPIRY_DATE > SYSTIMESTAMP) "
            + "  JOIN ADM_GROUPS G "
            + "    ON G.GROUP_ID = UG.GROUP_ID "
            + "   AND G.GROUP_STATUS = 'ACTIVE' "
            + "  JOIN ADM_GROUP_ROLES GR "
            + "    ON GR.GROUP_ID = G.GROUP_ID "
            + "   AND GR.IS_ACTIVE = 1 "
            + "  JOIN ADM_ROLES R "
            + "    ON R.ROLE_ID = GR.ROLE_ID "
            + "   AND R.ROLE_STATUS = 'ACTIVE' "
            + "  JOIN ADM_ROLE_PERMISSIONS RP "
            + "    ON RP.ROLE_ID = R.ROLE_ID "
            + "  JOIN ADM_PERMISSIONS P "
            + "    ON P.PERMISSION_ID = RP.PERMISSION_ID "
            + "   AND P.PERMISSION_STATUS = 'ACTIVE' "
            + " WHERE U.USER_ID = ? "
            + "   AND U.ACCOUNT_STATUS = 'ACTIVE' "
            + "   AND P.APPLICATION_NAME = ? "
            + "   AND P.PERMISSION_CODE = ? "
            + "   AND ROWNUM = 1";

    public boolean hasPermission(
            long userId,
            String applicationName,
            String permissionCode) throws SQLException {

        Connection connection = openConnection();

        if (connection == null) {
            throw new SQLException(
                    "Unable to open database connection"
            );
        }

        try (
            Connection autoCloseConnection = connection;
            PreparedStatement statement
                    = autoCloseConnection.prepareStatement(
                            CHECK_PERMISSION_SQL
                    )
        ) {
            statement.setLong(1, userId);
            statement.setString(2, applicationName);
            statement.setString(3, permissionCode);

            try (ResultSet resultSet
                    = statement.executeQuery()) {

                return resultSet.next();
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