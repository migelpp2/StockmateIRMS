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
import javax.swing.Timer;
/**
 *
 * @author USER
 */
public class cashierHomePanel extends javax.swing.JPanel {

    /**
     * Creates new form cashierHomePanel
     */
    public cashierHomePanel() {
        initComponents();
        lblBackground.setIcon(new javax.swing.ImageIcon(
            getClass().getResource("/irms/resources/background/Home.png")
        ));
        startDateTimeClock();
        loadDashboardData();
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

    private void loadDashboardData() {
        double todaySales = getTodaySalesAmount();
        int transactionsToday = getTodayTransactionsCount();
        int itemsSoldToday = getItemsSoldToday();
        double averageSale = getAverageSaleToday();

        lblSalesValue.setText(String.format("₱%.2f", todaySales));
        lblTransactionsValue.setText(String.valueOf(transactionsToday));
        lblSoldValue.setText(String.valueOf(itemsSoldToday));
        lblAveSale.setText(String.format("₱%.2f", averageSale));

        if (todaySales == 0) {
            lblOverview1.setText("• No sales recorded today");
        } else {
            lblOverview1.setText("• Sales today: " + String.format("₱%.2f", todaySales));
        }

        if (transactionsToday == 0) {
            lblOverview2.setText("• No transactions yet");
        } else {
            lblOverview2.setText("• " + transactionsToday + " transaction(s) completed");
        }

        if (itemsSoldToday == 0) {
            lblOverview3.setText("• No items sold yet");
        } else {
            lblOverview3.setText("• " + itemsSoldToday + " item(s) sold today");
        }
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
            JOptionPane.showMessageDialog(this, "Today sales error: " + e.getMessage());
        }

        return 0.0;
    }

    private int getTodayTransactionsCount() {
        String sql = "SELECT COUNT(*) FROM sales WHERE DATE(sale_date) = CURDATE()";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Transactions count error: " + e.getMessage());
        }

        return 0;
    }

    private int getItemsSoldToday() {
        String sql =
            "SELECT COALESCE(SUM(si.quantity), 0) " +
            "FROM sale_items si " +
            "INNER JOIN sales s ON si.sale_id = s.sale_id " +
            "WHERE DATE(s.sale_date) = CURDATE()";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal(1).intValue();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Items sold error: " + e.getMessage());
        }

        return 0;
    }

    private double getAverageSaleToday() {
        String sql = "SELECT COALESCE(AVG(total), 0) FROM sales WHERE DATE(sale_date) = CURDATE()";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Average sale error: " + e.getMessage());
        }

        return 0.0;
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
        lblDate = new javax.swing.JLabel();
        lblDay = new javax.swing.JLabel();
        pnlLowStock = new irms.ui.components.RoundedPanel();
        lblTransactionsValue = new javax.swing.JLabel();
        lblTransactionToday = new javax.swing.JLabel();
        pnlTodaySales = new irms.ui.components.RoundedPanel();
        lblSoldValue = new javax.swing.JLabel();
        lblItemSold = new javax.swing.JLabel();
        pnlCustomerDebt = new irms.ui.components.RoundedPanel();
        lblAverageSale = new javax.swing.JLabel();
        lblAveSale = new javax.swing.JLabel();
        pnlProductsNumber = new irms.ui.components.RoundedPanel();
        lblSalesValue = new javax.swing.JLabel();
        lblTodaySales = new javax.swing.JLabel();
        pnlOverview = new irms.ui.components.RoundedPanel();
        lblTodayOverviewText = new javax.swing.JLabel();
        lblOverview3 = new javax.swing.JLabel();
        lblOverview1 = new javax.swing.JLabel();
        lblOverview2 = new javax.swing.JLabel();
        lblBackground = new javax.swing.JLabel();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlWelcome.setBackground(new java.awt.Color(255, 255, 255));
        pnlWelcome.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblWelcome.setForeground(new java.awt.Color(54, 67, 20));
        lblWelcome.setText("Welcome to StockMate");
        pnlWelcome.add(lblWelcome, new org.netbeans.lib.awtextra.AbsoluteConstraints(83, 34, -1, -1));

        lblSubheading.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblSubheading.setForeground(new java.awt.Color(45, 45, 45));
        lblSubheading.setText("Inventory and Sales Overview");
        pnlWelcome.add(lblSubheading, new org.netbeans.lib.awtextra.AbsoluteConstraints(83, 72, -1, -1));

        lblRealTime.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblRealTime.setText("Real Time");
        pnlWelcome.add(lblRealTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 30, 110, 20));

        lblDate.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDate.setText("January 1, 2026");
        pnlWelcome.add(lblDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 50, 110, 20));

        lblDay.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblDay.setText("Monday");
        pnlWelcome.add(lblDay, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 70, 110, 20));

        add(pnlWelcome, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, 1070, 130));

        pnlLowStock.setBackground(new java.awt.Color(255, 255, 255));
        pnlLowStock.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTransactionsValue.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTransactionsValue.setText("---");
        pnlLowStock.add(lblTransactionsValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 10, -1, -1));

        lblTransactionToday.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblTransactionToday.setForeground(new java.awt.Color(45, 45, 45));
        lblTransactionToday.setText("Transactions Today");
        pnlLowStock.add(lblTransactionToday, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 50, -1, -1));

        add(pnlLowStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 320, 260, 90));

        pnlTodaySales.setBackground(new java.awt.Color(255, 255, 255));
        pnlTodaySales.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblSoldValue.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblSoldValue.setText("---");
        pnlTodaySales.add(lblSoldValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 10, -1, -1));

        lblItemSold.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblItemSold.setForeground(new java.awt.Color(45, 45, 45));
        lblItemSold.setText("Item Sold");
        pnlTodaySales.add(lblItemSold, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 50, -1, -1));

        add(pnlTodaySales, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 320, 260, 90));

        pnlCustomerDebt.setBackground(new java.awt.Color(255, 255, 255));
        pnlCustomerDebt.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblAverageSale.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblAverageSale.setForeground(new java.awt.Color(45, 45, 45));
        lblAverageSale.setText("Average Sale");
        pnlCustomerDebt.add(lblAverageSale, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 50, -1, 30));

        lblAveSale.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblAveSale.setText("---");
        pnlCustomerDebt.add(lblAveSale, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 10, -1, -1));

        add(pnlCustomerDebt, new org.netbeans.lib.awtextra.AbsoluteConstraints(820, 320, 260, 90));

        pnlProductsNumber.setBackground(new java.awt.Color(255, 255, 255));
        pnlProductsNumber.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblSalesValue.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblSalesValue.setText("---");
        pnlProductsNumber.add(lblSalesValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 10, -1, -1));

        lblTodaySales.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblTodaySales.setForeground(new java.awt.Color(45, 45, 45));
        lblTodaySales.setText("Today Sales");
        pnlProductsNumber.add(lblTodaySales, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 50, -1, -1));

        add(pnlProductsNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 320, 260, 90));

        pnlOverview.setBackground(new java.awt.Color(255, 255, 255));
        pnlOverview.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTodayOverviewText.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTodayOverviewText.setForeground(new java.awt.Color(54, 67, 20));
        lblTodayOverviewText.setText("Today Overview");
        pnlOverview.add(lblTodayOverviewText, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 40, -1, -1));

        lblOverview3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblOverview3.setText("---");
        pnlOverview.add(lblOverview3, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 140, -1, -1));

        lblOverview1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblOverview1.setText("---");
        pnlOverview.add(lblOverview1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 80, -1, -1));

        lblOverview2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblOverview2.setText("---");
        pnlOverview.add(lblOverview2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 110, -1, -1));

        add(pnlOverview, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 430, 610, 240));

        lblBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/background/Home.png"))); // NOI18N
        add(lblBackground, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblAveSale;
    private javax.swing.JLabel lblAverageSale;
    private javax.swing.JLabel lblBackground;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDay;
    private javax.swing.JLabel lblItemSold;
    private javax.swing.JLabel lblOverview1;
    private javax.swing.JLabel lblOverview2;
    private javax.swing.JLabel lblOverview3;
    private javax.swing.JLabel lblRealTime;
    private javax.swing.JLabel lblSalesValue;
    private javax.swing.JLabel lblSoldValue;
    private javax.swing.JLabel lblSubheading;
    private javax.swing.JLabel lblTodayOverviewText;
    private javax.swing.JLabel lblTodaySales;
    private javax.swing.JLabel lblTransactionToday;
    private javax.swing.JLabel lblTransactionsValue;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel pnlCustomerDebt;
    private javax.swing.JPanel pnlLowStock;
    private javax.swing.JPanel pnlOverview;
    private javax.swing.JPanel pnlProductsNumber;
    private javax.swing.JPanel pnlTodaySales;
    private javax.swing.JPanel pnlWelcome;
    // End of variables declaration//GEN-END:variables
}
