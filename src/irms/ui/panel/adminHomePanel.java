/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package irms.ui.panel;

import irms.ui.frame.MainFrame;
import irms.db.MySQLConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author USER
 */
public class adminHomePanel extends javax.swing.JPanel {

    /**
     * Creates new form HomePanel
     */
    public adminHomePanel() {
        initComponents();
        lblBackground.setIcon(new javax.swing.ImageIcon(
            getClass().getResource("/irms/resources/background/Home.png")
        ));
        loadDashboardData();
    }
    
//    private void styleDashboardCards() {
//        java.awt.Color cardBg = new java.awt.Color(245, 245, 245);
//
//        pnlWelcome.setOpaque(false);
//        pnlWelcome.setBackground(cardBg);
//
//        pnlProductsNumber.setBackground(cardBg);
//        pnlLowStock.setBackground(cardBg);
//        pnlTodaySales.setBackground(cardBg);
//        pnlCustomerDebt.setBackground(cardBg);
//        pnlQuickAction.setBackground(cardBg);
//        pnlOverview.setBackground(cardBg);
//    }

    private void loadDashboardData() {
        lblTotalProduct.setText(String.valueOf(getTotalProductsCount()));
        lblTotalProduct3.setText(String.valueOf(getLowStockCount()));
        lblTotalProduct2.setText(String.format("₱%.2f", getTodaySalesAmount()));
        lblTotalProduct1.setText(String.valueOf(getCustomersWithDebtCount()));

        int lowStock = getLowStockCount();
        int pendingDebts = getCustomersWithDebtCount();
        int salesToday = getTodaySalesCount();

        if (lowStock == 0) {
            lblOverview1.setText("• No low stock items today");
        } else {
            lblOverview1.setText("• " + lowStock + " low stock items need attention");
        }

        if (pendingDebts == 0) {
            lblOverview2.setText("• No pending customer debts");
        } else {
            lblOverview2.setText("• " + pendingDebts + " pending customer debts");
        }

        if (salesToday == 0) {
            lblOverview3.setText("• No sales recorded today");
        } else {
            lblOverview3.setText("• " + salesToday + " sales updated today");
        }
    }

    private int getTotalProductsCount() {
        String sql = "SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Total products error: " + e.getMessage());
        }

        return 0;
    }

    private int getLowStockCount() {
        String sql = "SELECT COUNT(*) FROM stocks WHERE quantity <= reorder_level AND quantity > 0";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Low stock error: " + e.getMessage());
        }

        return 0;
    }

    private double getTodaySalesAmount() {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM sales WHERE DATE(sale_date) = CURDATE()";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Today sales amount error: " + e.getMessage());
        }

        return 0.0;
    }

    private int getTodaySalesCount() {
        String sql = "SELECT COUNT(*) FROM sales WHERE DATE(sale_date) = CURDATE()";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Today sales count error: " + e.getMessage());
        }

        return 0;
    }

    private int getCustomersWithDebtCount() {
        String sql = "SELECT COUNT(DISTINCT customer_id) FROM utang WHERE status IN ('UNPAID', 'PARTIALLY PAID')";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Customer debt count error: " + e.getMessage());
        }

        return 0;
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

        pnlWelcome = new irms.ui.components.RoundedPanel();
        lblWelcome = new javax.swing.JLabel();
        lblSubheading = new javax.swing.JLabel();
        pnlProductsNumber = new irms.ui.components.RoundedPanel();
        lblTotalProduct = new javax.swing.JLabel();
        lblProductsText = new javax.swing.JLabel();
        pnlQuickAction = new irms.ui.components.RoundedPanel();
        btnViewStocks = new javax.swing.JButton();
        btnReports = new javax.swing.JButton();
        btnNewSales = new javax.swing.JButton();
        lblQuickActionText = new javax.swing.JLabel();
        pnlOverview = new irms.ui.components.RoundedPanel();
        lblTodayOverviewText = new javax.swing.JLabel();
        lblOverview3 = new javax.swing.JLabel();
        lblOverview1 = new javax.swing.JLabel();
        lblOverview2 = new javax.swing.JLabel();
        pnlCustomerDebt = new irms.ui.components.RoundedPanel();
        lblTotalProduct1 = new javax.swing.JLabel();
        lblWithDebtText = new javax.swing.JLabel();
        lblCustomerText = new javax.swing.JLabel();
        pnlTodaySales = new irms.ui.components.RoundedPanel();
        lblTotalProduct2 = new javax.swing.JLabel();
        lblTodaySalesText = new javax.swing.JLabel();
        pnlLowStock = new irms.ui.components.RoundedPanel();
        lblTotalProduct3 = new javax.swing.JLabel();
        lblLowStockText = new javax.swing.JLabel();
        lblBackground = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlWelcome.setBackground(new java.awt.Color(255, 255, 255));
        pnlWelcome.setOpaque(false);
        pnlWelcome.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblWelcome.setForeground(new java.awt.Color(54, 67, 20));
        lblWelcome.setText("Welcome to StockMate");
        pnlWelcome.add(lblWelcome, new org.netbeans.lib.awtextra.AbsoluteConstraints(83, 34, -1, -1));

        lblSubheading.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblSubheading.setForeground(new java.awt.Color(45, 45, 45));
        lblSubheading.setText("Inventory and Sales Overview");
        pnlWelcome.add(lblSubheading, new org.netbeans.lib.awtextra.AbsoluteConstraints(83, 72, -1, -1));

        add(pnlWelcome, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, 530, 130));

        pnlProductsNumber.setBackground(new java.awt.Color(198, 214, 165));
        pnlProductsNumber.setOpaque(false);
        pnlProductsNumber.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalProduct.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTotalProduct.setText("---");
        pnlProductsNumber.add(lblTotalProduct, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, -1, -1));

        lblProductsText.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblProductsText.setForeground(new java.awt.Color(45, 45, 45));
        lblProductsText.setText("Products");
        pnlProductsNumber.add(lblProductsText, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 60, -1, -1));

        add(pnlProductsNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 310, 260, 110));

        pnlQuickAction.setBackground(new java.awt.Color(214, 223, 208));
        pnlQuickAction.setOpaque(false);
        pnlQuickAction.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnViewStocks.setText("Stocks");
        btnViewStocks.addActionListener(this::btnViewStocksActionPerformed);
        pnlQuickAction.add(btnViewStocks, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 90, 131, 91));

        btnReports.setText("Reports");
        btnReports.addActionListener(this::btnReportsActionPerformed);
        pnlQuickAction.add(btnReports, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 90, 131, 91));

        btnNewSales.setText("Sales");
        btnNewSales.addActionListener(this::btnNewSalesActionPerformed);
        pnlQuickAction.add(btnNewSales, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 90, 131, 91));

        lblQuickActionText.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblQuickActionText.setForeground(new java.awt.Color(54, 67, 20));
        lblQuickActionText.setText("Quick Action");
        pnlQuickAction.add(lblQuickActionText, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 30, -1, -1));

        add(pnlQuickAction, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 430, 530, 250));

        pnlOverview.setBackground(new java.awt.Color(210, 222, 232));
        pnlOverview.setOpaque(false);
        pnlOverview.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTodayOverviewText.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTodayOverviewText.setForeground(new java.awt.Color(54, 67, 20));
        lblTodayOverviewText.setText("Today Overview");
        pnlOverview.add(lblTodayOverviewText, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 30, -1, -1));

        lblOverview3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblOverview3.setText("---");
        pnlOverview.add(lblOverview3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 150, -1, -1));

        lblOverview1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblOverview1.setText("---");
        pnlOverview.add(lblOverview1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, -1, -1));

        lblOverview2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblOverview2.setText("---");
        pnlOverview.add(lblOverview2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, -1, -1));

        add(pnlOverview, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 430, 530, 250));

        pnlCustomerDebt.setBackground(new java.awt.Color(220, 176, 176));
        pnlCustomerDebt.setOpaque(false);
        pnlCustomerDebt.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalProduct1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTotalProduct1.setText("---");
        pnlCustomerDebt.add(lblTotalProduct1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, -1, -1));

        lblWithDebtText.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblWithDebtText.setForeground(new java.awt.Color(45, 45, 45));
        lblWithDebtText.setText("with Debt");
        pnlCustomerDebt.add(lblWithDebtText, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 70, -1, 20));

        lblCustomerText.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblCustomerText.setForeground(new java.awt.Color(45, 45, 45));
        lblCustomerText.setText("Customers ");
        pnlCustomerDebt.add(lblCustomerText, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 50, -1, -1));

        add(pnlCustomerDebt, new org.netbeans.lib.awtextra.AbsoluteConstraints(820, 310, 260, 110));

        pnlTodaySales.setBackground(new java.awt.Color(156, 189, 219));
        pnlTodaySales.setOpaque(false);
        pnlTodaySales.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalProduct2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTotalProduct2.setText("---");
        pnlTodaySales.add(lblTotalProduct2, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 20, -1, -1));

        lblTodaySalesText.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblTodaySalesText.setForeground(new java.awt.Color(45, 45, 45));
        lblTodaySalesText.setText("Today Sales");
        pnlTodaySales.add(lblTodaySalesText, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 60, -1, -1));

        add(pnlTodaySales, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 310, 260, 110));

        pnlLowStock.setBackground(new java.awt.Color(230, 205, 126));
        pnlLowStock.setOpaque(false);
        pnlLowStock.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalProduct3.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTotalProduct3.setText("---");
        pnlLowStock.add(lblTotalProduct3, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, -1, -1));

        lblLowStockText.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblLowStockText.setForeground(new java.awt.Color(45, 45, 45));
        lblLowStockText.setText("Low Stock");
        pnlLowStock.add(lblLowStockText, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 60, -1, -1));

        add(pnlLowStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 310, 260, 110));

        lblBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/design/Home.png"))); // NOI18N
        add(lblBackground, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1130, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewSalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewSalesActionPerformed
        // TODO add your handling code here:
            openPanel(new salesPanel());
    }//GEN-LAST:event_btnNewSalesActionPerformed

    private void btnViewStocksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewStocksActionPerformed
        // TODO add your handling code here:
            openPanel(new stockPanel());
    }//GEN-LAST:event_btnViewStocksActionPerformed

    private void btnReportsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReportsActionPerformed
        // TODO add your handling code here:
            openPanel(new reportsPanel());
    }//GEN-LAST:event_btnReportsActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnNewSales;
    private javax.swing.JButton btnReports;
    private javax.swing.JButton btnViewStocks;
    private javax.swing.JLabel lblBackground;
    private javax.swing.JLabel lblCustomerText;
    private javax.swing.JLabel lblLowStockText;
    private javax.swing.JLabel lblOverview1;
    private javax.swing.JLabel lblOverview2;
    private javax.swing.JLabel lblOverview3;
    private javax.swing.JLabel lblProductsText;
    private javax.swing.JLabel lblQuickActionText;
    private javax.swing.JLabel lblSubheading;
    private javax.swing.JLabel lblTodayOverviewText;
    private javax.swing.JLabel lblTodaySalesText;
    private javax.swing.JLabel lblTotalProduct;
    private javax.swing.JLabel lblTotalProduct1;
    private javax.swing.JLabel lblTotalProduct2;
    private javax.swing.JLabel lblTotalProduct3;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JLabel lblWithDebtText;
    private javax.swing.JPanel pnlCustomerDebt;
    private javax.swing.JPanel pnlLowStock;
    private javax.swing.JPanel pnlOverview;
    private javax.swing.JPanel pnlProductsNumber;
    private javax.swing.JPanel pnlQuickAction;
    private javax.swing.JPanel pnlTodaySales;
    private javax.swing.JPanel pnlWelcome;
    // End of variables declaration//GEN-END:variables
}
