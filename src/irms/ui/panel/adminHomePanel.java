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
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

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
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Receipt ID", "Time", "Cashier", "Amount"
            }
        ));
        loadDashboardData();
        styleRecentTransactionsTable();
        startDateTimeClock();
        loadRecentTransactions();
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
        loadRecentTransactions();
    }

    private int getTotalProductsCount() {
        String sql = "SELECT COUNT(*) FROM products";

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
    
    private void startDateTimeClock() {
        Timer timer = new Timer(1000, e -> {
            Date now = new Date();

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");

            lblRealTime.setText(timeFormat.format(now));
            lblDate.setText(dateFormat.format(now));
            lblDay.setText(dayFormat.format(now));
        });

        timer.setInitialDelay(0);
        timer.start();
    }
    
    private void styleRecentTransactionsTable() {
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
    
    private void loadRecentTransactions() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        String sql = "SELECT sale_id, sale_date, cashier_name, total " +
                     "FROM sales " +
                     "ORDER BY sale_date DESC " +
                     "LIMIT 10";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

            while (rs.next()) {
                java.sql.Timestamp saleDate = rs.getTimestamp("sale_date");
                String timeText = saleDate != null ? timeFormat.format(saleDate) : "-";

                model.addRow(new Object[]{
                    rs.getInt("sale_id"),
                    timeText,
                    rs.getString("cashier_name"),
                    String.format("₱%.2f", rs.getBigDecimal("total"))
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Recent transactions error: " + e.getMessage());
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
        lblRealTime = new javax.swing.JLabel();
        lblDay = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        lblIcon = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        pnlProductsNumber = new irms.ui.components.RoundedPanel();
        lblTotalProduct = new javax.swing.JLabel();
        lblProductsText = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        pnlQuickAction = new irms.ui.components.RoundedPanel();
        btnViewStocks = new irms.ui.components.RoundedButtons();
        btnReports = new irms.ui.components.RoundedButtons();
        btnNewSales = new irms.ui.components.RoundedButtons();
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
        jLabel5 = new javax.swing.JLabel();
        pnlTodaySales = new irms.ui.components.RoundedPanel();
        lblTotalProduct2 = new javax.swing.JLabel();
        lblTodaySalesText = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        pnlLowStock = new irms.ui.components.RoundedPanel();
        lblTotalProduct3 = new javax.swing.JLabel();
        lblLowStockText = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        pnlRecentTransactions = new irms.ui.components.RoundedPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        lblWelcome1 = new javax.swing.JLabel();
        lblBackground = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlWelcome.setBackground(new java.awt.Color(255, 255, 255));
        pnlWelcome.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblWelcome.setForeground(new java.awt.Color(54, 67, 20));
        lblWelcome.setText("Welcome to StockMate");
        pnlWelcome.add(lblWelcome, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 20, -1, -1));

        lblSubheading.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblSubheading.setForeground(new java.awt.Color(45, 45, 45));
        lblSubheading.setText("Inventory and Sales Overview");
        pnlWelcome.add(lblSubheading, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 60, -1, -1));

        lblRealTime.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblRealTime.setText("Real Time");
        pnlWelcome.add(lblRealTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 20, 110, -1));

        lblDay.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblDay.setText("Monday");
        pnlWelcome.add(lblDay, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 60, 110, -1));

        lblDate.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDate.setText("January 1, 2026");
        pnlWelcome.add(lblDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 40, -1, -1));
        pnlWelcome.add(lblIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 50, -1, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/icon/STOCKMATE_LOGO.png"))); // NOI18N
        pnlWelcome.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, -1, -1));

        add(pnlWelcome, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, 1070, 130));

        pnlProductsNumber.setBackground(new java.awt.Color(255, 255, 255));
        pnlProductsNumber.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalProduct.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTotalProduct.setText("---");
        pnlProductsNumber.add(lblTotalProduct, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 10, -1, -1));

        lblProductsText.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblProductsText.setForeground(new java.awt.Color(45, 45, 45));
        lblProductsText.setText("Products");
        pnlProductsNumber.add(lblProductsText, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 40, -1, -1));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/icon/product_icon.png"))); // NOI18N
        pnlProductsNumber.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, -1, -1));

        add(pnlProductsNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, 260, 80));

        pnlQuickAction.setBackground(new java.awt.Color(255, 255, 255));
        pnlQuickAction.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnViewStocks.setBackground(new java.awt.Color(126, 139, 74));
        btnViewStocks.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnViewStocks.setForeground(new java.awt.Color(255, 255, 255));
        btnViewStocks.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/icon/Stocks Icon Button.png"))); // NOI18N
        btnViewStocks.addActionListener(this::btnViewStocksActionPerformed);
        pnlQuickAction.add(btnViewStocks, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 50, 131, 80));

        btnReports.setBackground(new java.awt.Color(126, 139, 74));
        btnReports.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnReports.setForeground(new java.awt.Color(255, 255, 255));
        btnReports.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/icon/reports icon button.png"))); // NOI18N
        btnReports.addActionListener(this::btnReportsActionPerformed);
        pnlQuickAction.add(btnReports, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 50, 131, 80));

        btnNewSales.setBackground(new java.awt.Color(126, 139, 74));
        btnNewSales.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnNewSales.setForeground(new java.awt.Color(255, 255, 255));
        btnNewSales.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/icon/sales icon button.png"))); // NOI18N
        btnNewSales.addActionListener(this::btnNewSalesActionPerformed);
        pnlQuickAction.add(btnNewSales, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 50, 131, 80));

        lblQuickActionText.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblQuickActionText.setForeground(new java.awt.Color(54, 67, 20));
        lblQuickActionText.setText("Quick Action");
        pnlQuickAction.add(lblQuickActionText, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, 180, -1));

        add(pnlQuickAction, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 390, 530, 150));

        pnlOverview.setBackground(new java.awt.Color(255, 255, 255));
        pnlOverview.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTodayOverviewText.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTodayOverviewText.setForeground(new java.awt.Color(54, 67, 20));
        lblTodayOverviewText.setText("Today Overview");
        pnlOverview.add(lblTodayOverviewText, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 10, -1, -1));

        lblOverview3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblOverview3.setText("---");
        pnlOverview.add(lblOverview3, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 110, -1, -1));

        lblOverview1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblOverview1.setText("---");
        pnlOverview.add(lblOverview1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 50, -1, -1));

        lblOverview2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblOverview2.setText("---");
        pnlOverview.add(lblOverview2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 80, -1, -1));

        add(pnlOverview, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 390, 530, 150));

        pnlCustomerDebt.setBackground(new java.awt.Color(255, 255, 255));
        pnlCustomerDebt.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalProduct1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTotalProduct1.setText("---");
        pnlCustomerDebt.add(lblTotalProduct1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 2, -1, 40));

        lblWithDebtText.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblWithDebtText.setForeground(new java.awt.Color(45, 45, 45));
        lblWithDebtText.setText("with Debt");
        pnlCustomerDebt.add(lblWithDebtText, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 50, -1, 20));

        lblCustomerText.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblCustomerText.setForeground(new java.awt.Color(45, 45, 45));
        lblCustomerText.setText("Customers ");
        pnlCustomerDebt.add(lblCustomerText, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 30, -1, 20));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/icon/customer_icon.png"))); // NOI18N
        pnlCustomerDebt.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, -1, -1));

        add(pnlCustomerDebt, new org.netbeans.lib.awtextra.AbsoluteConstraints(820, 300, 260, 80));

        pnlTodaySales.setBackground(new java.awt.Color(255, 255, 255));
        pnlTodaySales.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalProduct2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTotalProduct2.setText("---");
        pnlTodaySales.add(lblTotalProduct2, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 10, -1, -1));

        lblTodaySalesText.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblTodaySalesText.setForeground(new java.awt.Color(45, 45, 45));
        lblTodaySalesText.setText("Today Sales");
        pnlTodaySales.add(lblTodaySalesText, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 40, -1, -1));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/icon/sales_icon.png"))); // NOI18N
        pnlTodaySales.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, -1, -1));

        add(pnlTodaySales, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 300, 260, 80));

        pnlLowStock.setBackground(new java.awt.Color(255, 255, 255));
        pnlLowStock.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalProduct3.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTotalProduct3.setText("---");
        pnlLowStock.add(lblTotalProduct3, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 10, -1, -1));

        lblLowStockText.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblLowStockText.setForeground(new java.awt.Color(45, 45, 45));
        lblLowStockText.setText("Low Stock");
        pnlLowStock.add(lblLowStockText, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 40, -1, -1));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/icon/stocks_icon.png"))); // NOI18N
        pnlLowStock.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, -1, -1));

        add(pnlLowStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 300, 260, 80));

        pnlRecentTransactions.setBackground(new java.awt.Color(255, 255, 255));
        pnlRecentTransactions.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Receipt ID", "Time", "Cashier", "Amount"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        pnlRecentTransactions.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 40, 920, 160));

        lblWelcome1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblWelcome1.setForeground(new java.awt.Color(54, 67, 20));
        lblWelcome1.setText("Recent Transactions");
        pnlRecentTransactions.add(lblWelcome1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 0, -1, -1));

        add(pnlRecentTransactions, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 550, 1070, 210));

        lblBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/background/Home.png"))); // NOI18N
        add(lblBackground, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 770));
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblBackground;
    private javax.swing.JLabel lblCustomerText;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDay;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblLowStockText;
    private javax.swing.JLabel lblOverview1;
    private javax.swing.JLabel lblOverview2;
    private javax.swing.JLabel lblOverview3;
    private javax.swing.JLabel lblProductsText;
    private javax.swing.JLabel lblQuickActionText;
    private javax.swing.JLabel lblRealTime;
    private javax.swing.JLabel lblSubheading;
    private javax.swing.JLabel lblTodayOverviewText;
    private javax.swing.JLabel lblTodaySalesText;
    private javax.swing.JLabel lblTotalProduct;
    private javax.swing.JLabel lblTotalProduct1;
    private javax.swing.JLabel lblTotalProduct2;
    private javax.swing.JLabel lblTotalProduct3;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JLabel lblWelcome1;
    private javax.swing.JLabel lblWithDebtText;
    private javax.swing.JPanel pnlCustomerDebt;
    private javax.swing.JPanel pnlLowStock;
    private javax.swing.JPanel pnlOverview;
    private javax.swing.JPanel pnlProductsNumber;
    private javax.swing.JPanel pnlQuickAction;
    private javax.swing.JPanel pnlRecentTransactions;
    private javax.swing.JPanel pnlTodaySales;
    private javax.swing.JPanel pnlWelcome;
    // End of variables declaration//GEN-END:variables
}
