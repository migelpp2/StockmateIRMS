/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package irms.ui.panel;

import irms.ui.dialog.usersDialog;
import irms.ui.frame.MainFrame;
import irms.auth.session;
import irms.db.MySQLConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author miggy
 */
public class userMaintenance extends javax.swing.JFrame {
    
    private int dialogX;
    private int dialogY;

    /**
     * Creates new form userMaintenance
     */
    public userMaintenance(int dialogX, int dialogY) {
        initComponents();
        setLocationRelativeTo(null);
        
        this.dialogX = dialogX;
        this.dialogY = dialogY;

        if (session.role == null || !session.role.equalsIgnoreCase("ADMIN")) {
            JOptionPane.showMessageDialog(this, "Access denied.");
            this.dispose();
            new MainFrame().setVisible(true);
            return;
        }

        setupTable();
        loadUsers();
        autoSearchUsers();
    }

    private void setupTable() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new String[]{"User ID", "Full Name", "Username", "Role"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblUsers.setModel(model);
        tblUsers.setRowSelectionAllowed(true);
    }

    private void loadUsers() {
        DefaultTableModel model = (DefaultTableModel) tblUsers.getModel();
        model.setRowCount(0);

        String sql = "SELECT user_id, full_name, username, role FROM users ORDER BY user_id";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getString("username"),
                    rs.getString("role")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Load users error: " + e.getMessage());
        }
    }

    private void searchUsers(String keyword) {
        DefaultTableModel model = (DefaultTableModel) tblUsers.getModel();
        model.setRowCount(0);

        String sql = "SELECT user_id, full_name, username, role " +
                     "FROM users " +
                     "WHERE CAST(user_id AS CHAR) LIKE ? " +
                     "   OR full_name LIKE ? " +
                     "   OR username LIKE ? " +
                     "   OR role LIKE ? " +
                     "ORDER BY user_id";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            pst.setString(1, like);
            pst.setString(2, like);
            pst.setString(3, like);
            pst.setString(4, like);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("username"),
                        rs.getString("role")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Search users error: " + e.getMessage());
        }
    }
    
    private void autoSearchUsers() {
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchNow();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchNow();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchNow();
            }

            private void searchNow() {
                String text = txtSearch.getText().trim();
                if (text.isEmpty()) {
                    loadUsers();
                } else {
                    searchUsers(text);
                }
            }
        });
    }

    private boolean usernameExists(String username, Integer excludeUserId) {
        String sql;

        if (excludeUserId == null) {
            sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        } else {
            sql = "SELECT COUNT(*) FROM users WHERE username = ? AND user_id <> ?";
        }

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, username);

            if (excludeUserId != null) {
                pst.setInt(2, excludeUserId);
            }

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Username check error: " + e.getMessage());
        }

        return false;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        lblSearch = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(200, 212, 222));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTitle.setText("User Maintenance");

        tblUsers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "User ID", "Full Name", "Username", "Role"
            }
        ));
        jScrollPane1.setViewportView(tblUsers);

        lblSearch.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSearch.setText("Search:");

        btnAdd.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnEdit.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnClose.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblTitle)
                            .addGap(673, 673, 673))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 820, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(29, 29, 29)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEdit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose)
                        .addContainerGap())))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnAdd, btnClose, btnDelete, btnEdit});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSearch)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblSearch)
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnEdit)
                        .addComponent(btnDelete)
                        .addComponent(btnClose)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 406, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnAdd, btnClose, btnDelete, btnEdit});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        JTextField txtFirstName = new JTextField();
        JTextField txtMiddleName = new JTextField();
        JTextField txtLastName = new JTextField();
        JTextField txtUsernameField = new JTextField();
        JPasswordField txtPassword = new JPasswordField();

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("First Name:"));
        panel.add(txtFirstName);
        panel.add(new JLabel("Middle Name:"));
        panel.add(txtMiddleName);
        panel.add(new JLabel("Last Name:"));
        panel.add(txtLastName);
        panel.add(new JLabel("Username:"));
        panel.add(txtUsernameField);
        panel.add(new JLabel("Password:"));
        panel.add(txtPassword);

        int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Add User",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String firstName = txtFirstName.getText().trim();
        String middleName = txtMiddleName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String username = txtUsernameField.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.");
            return;
        }

        if (usernameExists(username, null)) {
            JOptionPane.showMessageDialog(this, "Username already exists.");
            return;
        }

        String fullName;
        if (middleName.isEmpty()) {
            fullName = firstName + " " + lastName;
        } else {
            fullName = firstName + " " + middleName + " " + lastName;
        }

        String sql = "INSERT INTO users (first_name, middle_name, last_name, full_name, username, password, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'CASHIER')";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, firstName);
            pst.setString(2, middleName.isEmpty() ? null : middleName);
            pst.setString(3, lastName);
            pst.setString(4, fullName);
            pst.setString(5, username);
            pst.setString(6, password);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "User added successfully.");
            loadUsers();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Add user error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        int row = tblUsers.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return;
        }

        int userId = Integer.parseInt(tblUsers.getValueAt(row, 0).toString());

        String currentFullName = "";
        String currentUsername = "";
        String currentRole = "";

        String sql = "SELECT full_name, username, role FROM users WHERE user_id = ?";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, userId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    currentFullName = rs.getString("full_name");
                    currentUsername = rs.getString("username");
                    currentRole = rs.getString("role");
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Load selected user error: " + e.getMessage());
            return;
        }

        JTextField txtFullName = new JTextField(currentFullName);
        JTextField txtUsernameField = new JTextField(currentUsername);
        JPasswordField txtPassword = new JPasswordField();

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Full Name:"));
        panel.add(txtFullName);
        panel.add(new JLabel("Username:"));
        panel.add(txtUsernameField);
        panel.add(new JLabel("New Password:"));
        panel.add(txtPassword);

        int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Edit User",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String fullName = txtFullName.getText().trim();
        String username = txtUsernameField.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (fullName.isEmpty() || username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name and username are required.");
            return;
        }

        if (usernameExists(username, userId)) {
            JOptionPane.showMessageDialog(this, "Username already exists.");
            return;
        }

        String updateSql;
        boolean updatePassword = !password.isEmpty();

        if (updatePassword) {
            updateSql = "UPDATE users SET full_name = ?, username = ?, password = ? WHERE user_id = ?";
        } else {
            updateSql = "UPDATE users SET full_name = ?, username = ? WHERE user_id = ?";
        }

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(updateSql)) {

            pst.setString(1, fullName);
            pst.setString(2, username);

            if (updatePassword) {
                pst.setString(3, password);
                pst.setInt(4, userId);
            } else {
                pst.setInt(3, userId);
            }

            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "User updated successfully.");
            loadUsers();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Edit user error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        int row = tblUsers.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return;
        }

        int userId = Integer.parseInt(tblUsers.getValueAt(row, 0).toString());
        String fullName = tblUsers.getValueAt(row, 1).toString();
        String role = tblUsers.getValueAt(row, 3).toString();

        if (userId == session.userId) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account while logged in.");
            return;
        }

        int adminCount = 0;
        String countSql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(countSql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                adminCount = rs.getInt(1);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Admin count error: " + e.getMessage());
            return;
        }

        if ("ADMIN".equalsIgnoreCase(role) && adminCount <= 1) {
            JOptionPane.showMessageDialog(this, "You cannot delete the last admin account.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Delete user: " + fullName + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, userId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "User deleted successfully.");
            loadUsers();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Delete user error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        // TODO add your handling code here:
        this.dispose();

        usersDialog dialog = new usersDialog(new javax.swing.JFrame(), true);
        dialog.setLocation(dialogX, dialogY);
        dialog.setVisible(true);
    }//GEN-LAST:event_btnCloseActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(userMaintenance.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(userMaintenance.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(userMaintenance.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(userMaintenance.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new userMaintenance(300, 200).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JTable tblUsers;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
