package irms.app;
//import java.sql.Connection;

import irms.auth.login;

public class IRMS {
    public static void main(String[] args) {
        //Connection conn = MySQLConnect.getConnection();
        login p = new login();
        p.setVisible(true);
    }
    
}