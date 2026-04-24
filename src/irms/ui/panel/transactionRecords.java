/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package irms.ui.panel;

import irms.db.MySQLConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import irms.ui.frame.MainFrame;

/**
 *
 * @author USER
 */
public class transactionRecords extends javax.swing.JPanel {

    /**
     * Creates new form transactionRecords
     */
    public transactionRecords() {
        initComponents();

        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel<>(
            new String[]{"Sales Transactions", "Sold Items"}
        ));

        jDateChooser1.setDate(new Date());
        jDateChooser1.setDateFormatString("yyyy-MM-dd");

        setupTable();
        styleTable();

        btnGenerate.addActionListener(e -> generateRecords());
        btnRefresh.addActionListener(e -> refreshRecords());

        generateRecords();
    }
    
    private void setupTable() {
        String type = cmbReportType.getSelectedItem() == null
                ? "Sales Transactions"
                : cmbReportType.getSelectedItem().toString();

        DefaultTableModel model;

        if (type.equals("Sales Transactions")) {
            model = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Sale ID", "Date/Time", "Cashier", "Total", "Cash Received", "Change"}
            );
        } else {
            model = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Sale ID", "Product", "Quantity", "Unit Price", "Line Total", "Date/Time"}
            );
        }

        jTable1.setModel(model);
    }

    private void generateRecords() {
        Date selectedDate = jDateChooser1.getDate();

        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a date.");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateText = sdf.format(selectedDate);

        String type = cmbReportType.getSelectedItem().toString();

        setupTable();

        if (type.equals("Sales Transactions")) {
            loadSalesTransactions(dateText);
        } else {
            loadSoldItems(dateText);
        }
    }

    private void refreshRecords() {
        jDateChooser1.setDate(new Date());
        generateRecords();
    }

    private void loadSalesTransactions(String dateText) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        String sql =
            "SELECT sale_id, sale_date, cashier_name, total, cash_received, change_amount " +
            "FROM sales " +
            "WHERE DATE(sale_date) = ? " +
            "ORDER BY sale_date DESC";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, dateText);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("sale_id"),
                        rs.getTimestamp("sale_date"),
                        rs.getString("cashier_name"),
                        String.format("₱%.2f", rs.getBigDecimal("total")),
                        String.format("₱%.2f", rs.getBigDecimal("cash_received")),
                        String.format("₱%.2f", rs.getBigDecimal("change_amount"))
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Load sales transactions error: " + e.getMessage());
        }
    }

    private void loadSoldItems(String dateText) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        String sql =
            "SELECT s.sale_id, p.product_name, si.quantity, si.price, si.line_total, s.sale_date " +
            "FROM sale_items si " +
            "INNER JOIN sales s ON si.sale_id = s.sale_id " +
            "INNER JOIN products p ON si.product_id = p.product_id " +
            "WHERE DATE(s.sale_date) = ? " +
            "ORDER BY s.sale_date DESC";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, dateText);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("sale_id"),
                        rs.getString("product_name"),
                        rs.getBigDecimal("quantity"),
                        String.format("₱%.2f", rs.getBigDecimal("price")),
                        String.format("₱%.2f", rs.getBigDecimal("line_total")),
                        rs.getTimestamp("sale_date")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Load sold items error: " + e.getMessage());
        }
    }

    private void styleTable() {
        jTable1.setRowHeight(28);
        jTable1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        jTable1.setGridColor(new java.awt.Color(180, 180, 180));
        jTable1.setSelectionBackground(new java.awt.Color(180, 200, 160));
        jTable1.setSelectionForeground(java.awt.Color.BLACK);
        jTable1.setRowSelectionAllowed(true);
        jTable1.setFocusable(false);

        JTableHeader header = jTable1.getTableHeader();
        header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        header.setBackground(new java.awt.Color(220, 229, 236));
        header.setForeground(new java.awt.Color(54, 67, 20));
        header.setReorderingAllowed(false);
    }
    
    private void openPanel(javax.swing.JPanel panel) {
        java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);

        if (window instanceof MainFrame) {
            ((MainFrame) window).showPanel(panel);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblReportType = new javax.swing.JLabel();
        cmbReportType = new javax.swing.JComboBox<>();
        lblDate = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        btnGenerate = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnReturn = new javax.swing.JButton();
        lblBackground = new javax.swing.JLabel();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblReportType.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblReportType.setText("Transaction Type:");
        add(lblReportType, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, 40));

        cmbReportType.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        add(cmbReportType, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 170, 250, 40));

        lblDate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblDate.setText("Date:");
        add(lblDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 170, -1, 40));
        add(jDateChooser1, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 170, 250, 40));

        btnGenerate.setBackground(new java.awt.Color(72, 92, 13));
        btnGenerate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnGenerate.setForeground(new java.awt.Color(255, 255, 255));
        btnGenerate.setText("Generate");
        btnGenerate.setBorder(null);
        add(btnGenerate, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 710, 110, 40));

        btnRefresh.setBackground(new java.awt.Color(124, 144, 84));
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnRefresh.setForeground(new java.awt.Color(255, 255, 255));
        btnRefresh.setText("Refresh");
        btnRefresh.setBorder(null);
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);
        add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 710, 110, 40));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, 1050, 460));

        btnReturn.setBackground(new java.awt.Color(124, 144, 84));
        btnReturn.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnReturn.setForeground(new java.awt.Color(255, 255, 255));
        btnReturn.setText("Return");
        btnReturn.setBorder(null);
        btnReturn.addActionListener(this::btnReturnActionPerformed);
        add(btnReturn, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 170, 110, 40));

        lblBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/background/Reports.png"))); // NOI18N
        add(lblBackground, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:                      
        refreshRecords();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnReturnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReturnActionPerformed
        // TODO add your handling code here:
        openPanel(new reportsPanel());
    }//GEN-LAST:event_btnReturnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGenerate;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnReturn;
    private javax.swing.JComboBox<String> cmbReportType;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblBackground;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblReportType;
    // End of variables declaration//GEN-END:variables
}
