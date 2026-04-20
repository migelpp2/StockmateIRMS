/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package irms;

/**
 *
 * @author miggy
 */

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class stockHistory extends javax.swing.JFrame {
    
    
    /**
     * Creates new form stockHistory
     */
    public stockHistory() {
        initComponents();
        setLocationRelativeTo(null);
        loadHistory();
        autoSearchHistory();
        styleTable();
    }
    
    private String formatMovementQty(BigDecimal qty, String unitLabel) {
        if (qty == null) {
            return "0";
        }

        if (unitLabel != null && unitLabel.equalsIgnoreCase("kg")) {
            return String.format("%.2f", qty);
        }

        return String.valueOf(qty.intValue());
    }

    private void loadHistory() {
        DefaultTableModel model = (DefaultTableModel) tblStocks.getModel();
        model.setRowCount(0);

        String sql = "SELECT sm.movement_id, p.product_name, sm.movement_type, sm.quantity, sm.unit_label, " +
                     "sm.previous_stock, sm.new_stock, sm.remarks, u.username, sm.movement_date, sm.movement_time " +
                     "FROM stock_movements sm " +
                     "INNER JOIN products p ON sm.product_id = p.product_id " +
                     "LEFT JOIN users u ON sm.moved_by = u.user_id " +
                     "ORDER BY sm.movement_id DESC";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String unitLabel = rs.getString("unit_label");

                model.addRow(new Object[]{
                    rs.getInt("movement_id"),
                    rs.getString("product_name"),
                    rs.getString("movement_type"),
                    formatMovementQty(rs.getBigDecimal("quantity"), unitLabel),
                    unitLabel,
                    formatMovementQty(rs.getBigDecimal("previous_stock"), unitLabel),
                    formatMovementQty(rs.getBigDecimal("new_stock"), unitLabel),
                    rs.getString("remarks"),
                    rs.getString("username"),
                    rs.getDate("movement_date"),
                    rs.getTime("movement_time")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Load history error: " + e.getMessage());
        }
    }

    private void searchHistory(String keyword) {
        DefaultTableModel model = (DefaultTableModel) tblStocks.getModel();
        model.setRowCount(0);

        String sql = "SELECT sm.movement_id, p.product_name, sm.movement_type, sm.quantity, sm.unit_label, " +
                     "sm.previous_stock, sm.new_stock, sm.remarks, u.username, sm.movement_date, sm.movement_time " +
                     "FROM stock_movements sm " +
                     "INNER JOIN products p ON sm.product_id = p.product_id " +
                     "LEFT JOIN users u ON sm.moved_by = u.user_id " +
                     "WHERE CAST(sm.movement_id AS CHAR) LIKE ? " +
                     "   OR p.product_name LIKE ? " +
                     "   OR sm.movement_type LIKE ? " +
                     "   OR sm.remarks LIKE ? " +
                     "   OR COALESCE(u.username, '') LIKE ? " +
                     "ORDER BY sm.movement_id DESC";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            pst.setString(1, like);
            pst.setString(2, like);
            pst.setString(3, like);
            pst.setString(4, like);
            pst.setString(5, like);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String unitLabel = rs.getString("unit_label");

                    model.addRow(new Object[]{
                        rs.getInt("movement_id"),
                        rs.getString("product_name"),
                        rs.getString("movement_type"),
                        formatMovementQty(rs.getBigDecimal("quantity"), unitLabel),
                        unitLabel,
                        formatMovementQty(rs.getBigDecimal("previous_stock"), unitLabel),
                        formatMovementQty(rs.getBigDecimal("new_stock"), unitLabel),
                        rs.getString("remarks"),
                        rs.getString("username"),
                        rs.getDate("movement_date"),
                        rs.getTime("movement_time")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Search history error: " + e.getMessage());
        }
    }

    private void autoSearchHistory() {
        searchbar.getDocument().addDocumentListener(new DocumentListener() {
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
                String text = searchbar.getText().trim();
                if (text.isEmpty()) {
                    loadHistory();
                } else {
                    searchHistory(text);
                }
            }
        });
    }
    
    public void styleTable() {
        tblStocks.setRowHeight(28);
        tblStocks.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        tblStocks.setGridColor(new java.awt.Color(180, 180, 180));
        tblStocks.setSelectionBackground(new java.awt.Color(180, 200, 160));
        tblStocks.setSelectionForeground(java.awt.Color.BLACK);
        tblStocks.setRowSelectionAllowed(true);
        tblStocks.setFocusable(false);

        javax.swing.table.JTableHeader header = tblStocks.getTableHeader();
        header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        header.setBackground(new java.awt.Color(220, 229, 236));
        header.setForeground(new java.awt.Color(54, 67, 20));
        header.setReorderingAllowed(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnClose = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblStocks = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        searchbar = new javax.swing.JTextField();
        btnSales = new javax.swing.JButton();
        btnCustomers = new javax.swing.JButton();
        btnBrand = new javax.swing.JButton();
        btnCategory = new javax.swing.JButton();
        btnProducts = new javax.swing.JButton();
        btnStocks = new javax.swing.JButton();
        btnReports = new javax.swing.JButton();
        btnHome = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });
        getContentPane().add(btnClose, new org.netbeans.lib.awtextra.AbsoluteConstraints(1210, 700, 100, 30));

        tblStocks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Movement ID", "Product Name", "Movement Type", "Quantity", "Unit Label", "Previous Stock", "New Stock", "Remarks", "Moved By", "Date", "Time"
            }
        ));
        tblStocks.setEnabled(false);
        jScrollPane1.setViewportView(tblStocks);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 210, 1060, 440));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Search:");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 160, 50, 30));

        searchbar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchbarActionPerformed(evt);
            }
        });
        getContentPane().add(searchbar, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 160, 270, 30));

        btnSales.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnSales.setText("SALES");
        btnSales.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalesActionPerformed(evt);
            }
        });
        getContentPane().add(btnSales, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 360, 140, -1));

        btnCustomers.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnCustomers.setText("CUSTOMERS");
        btnCustomers.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnCustomers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustomersActionPerformed(evt);
            }
        });
        getContentPane().add(btnCustomers, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 400, 140, -1));

        btnBrand.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnBrand.setText("BRAND");
        btnBrand.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnBrand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrandActionPerformed(evt);
            }
        });
        getContentPane().add(btnBrand, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 440, 140, -1));

        btnCategory.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnCategory.setText("CATEGORY");
        btnCategory.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCategoryActionPerformed(evt);
            }
        });
        getContentPane().add(btnCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 480, 140, -1));

        btnProducts.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnProducts.setText("PRODUCTS");
        btnProducts.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnProducts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProductsActionPerformed(evt);
            }
        });
        getContentPane().add(btnProducts, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 520, 140, -1));

        btnStocks.setBackground(new java.awt.Color(126, 139, 74));
        btnStocks.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnStocks.setForeground(new java.awt.Color(255, 255, 255));
        btnStocks.setText("STOCKS");
        btnStocks.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnStocks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStocksActionPerformed(evt);
            }
        });
        getContentPane().add(btnStocks, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 560, 140, -1));

        btnReports.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnReports.setText("REPORTS");
        btnReports.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnReports.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReportsActionPerformed(evt);
            }
        });
        getContentPane().add(btnReports, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 600, 140, -1));

        btnHome.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnHome.setText("HOME");
        btnHome.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHomeActionPerformed(evt);
            }
        });
        getContentPane().add(btnHome, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 290, 140, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/pages/Stock list.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        // TODO add your handling code here:
        stockslist sl = new stockslist();
        sl.setVisible(true);
        this.dispose();
        
    }//GEN-LAST:event_btnCloseActionPerformed

    private void searchbarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchbarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchbarActionPerformed

    private void btnSalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalesActionPerformed
        // TODO add your handling code here:
        sales s = new sales();
        s.setLocation(this.getLocation());
        s.setExtendedState(this.getExtendedState());

        this.setVisible(false);
        s.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnSalesActionPerformed

    private void btnCustomersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustomersActionPerformed
        // TODO add your handling code here:
        customers cu = new customers();
        cu.setLocation(this.getLocation());
        cu.setExtendedState(this.getExtendedState());

        this.setVisible(false);
        cu.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnCustomersActionPerformed

    private void btnBrandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrandActionPerformed
        // TODO add your handling code here:
        brand b = new brand();
        b.setLocation(this.getLocation());
        b.setExtendedState(this.getExtendedState());

        this.setVisible(false);
        b.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnBrandActionPerformed

    private void btnCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCategoryActionPerformed
        // TODO add your handling code here:
        category cat = new category();
        cat.setLocation(this.getLocation());
        cat.setExtendedState(this.getExtendedState());

        this.setVisible(false);
        cat.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnCategoryActionPerformed

    private void btnProductsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProductsActionPerformed
        // TODO add your handling code here:
        products p = new products();
        p.setLocation(this.getLocation());
        p.setExtendedState(this.getExtendedState());

        this.setVisible(false);
        p.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnProductsActionPerformed

    private void btnStocksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStocksActionPerformed
        // TODO add your handling code here:
        stockslist st = new stockslist();
        st.setLocation(this.getLocation());
        st.setExtendedState(this.getExtendedState());

        this.setVisible(false);
        st.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnStocksActionPerformed

    private void btnReportsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReportsActionPerformed
        // TODO add your handling code here:
        reports r = new reports();
        r.setLocation(this.getLocation());
        r.setExtendedState(this.getExtendedState());

        this.setVisible(false);
        r.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnReportsActionPerformed

    private void btnHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHomeActionPerformed
        // TODO add your handling code here:
        MainFrame1 m = new MainFrame1();
        m.setLocation(this.getLocation());
        m.setExtendedState(this.getExtendedState());

        this.setVisible(false);
        m.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnHomeActionPerformed

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
            java.util.logging.Logger.getLogger(stockHistory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(stockHistory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(stockHistory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(stockHistory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new stockHistory().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrand;
    private javax.swing.JButton btnCategory;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnCustomers;
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnProducts;
    private javax.swing.JButton btnReports;
    private javax.swing.JButton btnSales;
    private javax.swing.JButton btnStocks;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField searchbar;
    private javax.swing.JTable tblStocks;
    // End of variables declaration//GEN-END:variables
}
