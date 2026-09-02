package bop.src.main.service;

import bop.src.main.bean.DatabaseTimeData;
import bop.src.main.dao.DatabaseTimeDAO;

import java.sql.Timestamp;

/**
 * 
 * @author ِAshraf.M.Fahmawi
 */
public class DatabaseTimeService {

    private final DatabaseTimeDAO databaseTimeDAO;

    public DatabaseTimeService() {
        this.databaseTimeDAO
                = new DatabaseTimeDAO();
    }

    public DatabaseTimeData getDatabaseTime()
            throws Exception {

        
        Timestamp databaseTimestamp
                = databaseTimeDAO.getDatabaseTime();

        if (databaseTimestamp == null) {
            throw new Exception(
                    "Database timestamp is unavailable"
            );
        }

        return new DatabaseTimeData(
                databaseTimestamp.toString()
        );
    }
}