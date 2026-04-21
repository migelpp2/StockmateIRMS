/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package irms.auth;

/**
 *
 * @author miggy
 */
public class session {
    public static int userId;
    public static String username;
    public static String fullname;
    public static String role;

    public static void clear() {
        userId = 0;
        username = null;
        fullname = null;
        role = null;
    }
}
