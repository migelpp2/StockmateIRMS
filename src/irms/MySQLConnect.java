package irms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class MySQLConnect {
    private static final String URL = "jdbc:mysql://localhost:3306/ims_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException err) {
            JOptionPane.showMessageDialog(null, "Database Connection Error:\n" + err.getMessage());
            return null;
        }
    }
    public static void main(String[] args) {
        Connection conn = MySQLConnect.getConnection();
        if (conn != null) {
            System.out.println("Connected successfully!");
        } else {
            System.out.println("Connection failed.");
        }
    }
}
