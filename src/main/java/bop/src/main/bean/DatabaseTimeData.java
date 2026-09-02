package bop.src.main.bean;

public class DatabaseTimeData {

    private String databaseTimestamp;

    public DatabaseTimeData() {
    }

    public DatabaseTimeData(
            String databaseTimestamp) {

        this.databaseTimestamp
                = databaseTimestamp;
    }

    public String getDatabaseTimestamp() {
        return databaseTimestamp;
    }

    public void setDatabaseTimestamp(
            String databaseTimestamp) {

        this.databaseTimestamp
                = databaseTimestamp;
    }
}