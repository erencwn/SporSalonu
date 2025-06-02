package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=spor_salonu;encrypt=true;trustServerCertificate=true";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "Can,34087"; 

    public static Connection connect() {
        try {
        	
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            return conn;
        } catch (SQLException e) {
            System.out.println("Bağlantı hatası: " + e.getMessage());
            return null;
        }
    }
}
