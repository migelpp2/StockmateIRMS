/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package irms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author USER
 */
public class reports extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(reports.class.getName());

    /**
     * Creates new form reports
     */
    public reports() {
        initComponents();
        setLocationRelativeTo(null);

        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"Daily", "Weekly", "Monthly"}
        ));

        txtDate.setText(java.time.LocalDate.now().toString());

        jLabel3.setText("Daily Sales");
        jLabel4.setText("₱0.00");
        jLabel2.setText("₱0.00");
        jLabel6.setText("0");
        lblTotalRevenueValue.setText("₱0.00");

        btnGenerate.addActionListener(e -> generateReport());
        btnRefresh.addActionListener(e -> refreshReport());

        refreshReport();
        styleTable();
    }
    
    private void refreshReport() {
        txtDate.setText(java.time.LocalDate.now().toString());
        cmbReportType.setSelectedItem("Daily");
        generateReport();
    }

    private void generateReport() {
        String reportType = cmbReportType.getSelectedItem().toString();
        String dateText = txtDate.getText().trim();

        if (dateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Date is required.");
            return;
        }

        loadReportData(reportType, dateText);
    }

    private void loadReportData(String reportType, String dateText) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        String filterCondition;

        if (reportType.equalsIgnoreCase("Daily")) {
            filterCondition = "DATE(sale_date) = ?";
            jLabel3.setText("Daily Sales");
        } else if (reportType.equalsIgnoreCase("Weekly")) {
            filterCondition = "YEARWEEK(sale_date, 1) = YEARWEEK(?, 1)";
            jLabel3.setText("Weekly Sales");
        } else {
            filterCondition = "DATE_FORMAT(sale_date, '%Y-%m') = DATE_FORMAT(?, '%Y-%m')";
            jLabel3.setText("Monthly Sales");
        }

        String detailSql =
                "SELECT sale_id, DATE(sale_date) AS report_date, total, notes " +
                "FROM sales " +
                "WHERE " + filterCondition + " " +
                "ORDER BY sale_date DESC";

        String summarySql =
                "SELECT COUNT(*) AS transactions, COALESCE(SUM(total), 0) AS revenue " +
                "FROM sales " +
                "WHERE " + filterCondition;

        try (Connection conn = MySQLConnect.getConnection()) {

            try (PreparedStatement pst = conn.prepareStatement(detailSql)) {
                pst.setString(1, dateText);

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        int saleId = rs.getInt("sale_id");
                        String saleDate = rs.getString("report_date");
                        double totalAmount = rs.getDouble("total");
                        String notes = rs.getString("notes");

                        String cashReceived = extractCash(notes);
                        String change = extractChange(notes);

                        model.addRow(new Object[]{
                            saleDate,
                            saleId,
                            String.format("₱%.2f", totalAmount),
                            cashReceived,
                            change,
                            notes == null ? "" : notes
                        });
                    }
                }
            }

            int totalTransactions = 0;
            double totalRevenue = 0.0;

            try (PreparedStatement pst = conn.prepareStatement(summarySql)) {
                pst.setString(1, dateText);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        totalTransactions = rs.getInt("transactions");
                        totalRevenue = rs.getDouble("revenue");
                    }
                }
            }

            jLabel4.setText(String.format("₱%.2f", totalRevenue));
            jLabel2.setText(String.format("₱%.2f", totalRevenue));
            jLabel6.setText(String.valueOf(totalTransactions));
            lblTotalRevenueValue.setText(String.format("₱%.2f", totalRevenue));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Report error: " + e.getMessage());
        }
    }

    private String extractCash(String notes) {
        if (notes == null || notes.trim().isEmpty()) {
            return "-";
        }

        try {
            if (notes.contains("Cash:")) {
                int start = notes.indexOf("Cash:") + 5;
                int end = notes.contains(", Change:") ? notes.indexOf(", Change:") : notes.length();
                return notes.substring(start, end).trim();
            }
        } catch (Exception e) {
            return "-";
        }

        return "-";
    }

    private String extractChange(String notes) {
        if (notes == null || notes.trim().isEmpty()) {
            return "-";
        }

        try {
            if (notes.contains("Change:")) {
                int start = notes.indexOf("Change:") + 7;
                return notes.substring(start).trim();
            }
        } catch (Exception e) {
            return "-";
        }

        return "-";
    }
    
    public void styleTable() {
        jTable1.setRowHeight(28);
        jTable1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        jTable1.setGridColor(new java.awt.Color(180, 180, 180));
        jTable1.setSelectionBackground(new java.awt.Color(180, 200, 160));
        jTable1.setSelectionForeground(java.awt.Color.BLACK);
        jTable1.setRowSelectionAllowed(true);
        jTable1.setFocusable(false);

        javax.swing.table.JTableHeader header = jTable1.getTableHeader();
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

        lblDate = new javax.swing.JLabel();
        cmbReportType = new javax.swing.JComboBox<>();
        txtDate = new javax.swing.JTextField();
        pnlRevenue = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        pnlReportType = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        pnlTransactions = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblReportType = new javax.swing.JLabel();
        btnRefresh = new javax.swing.JButton();
        btnGenerate = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        lblTotalRevenueValue = new javax.swing.JLabel();
        lblTotalRevenueText = new javax.swing.JLabel();
        btnReports = new javax.swing.JButton();
        btnHome = new javax.swing.JButton();
        btnSales = new javax.swing.JButton();
        btnCustomers = new javax.swing.JButton();
        btnBrand = new javax.swing.JButton();
        btnCategory = new javax.swing.JButton();
        btnProducts = new javax.swing.JButton();
        btnStocks = new javax.swing.JButton();
        lblBackground = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblDate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblDate.setText("Date:");
        getContentPane().add(lblDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 170, -1, 40));

        cmbReportType.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbReportType, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 170, 250, 40));

        txtDate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDate.addActionListener(this::txtDateActionPerformed);
        getContentPane().add(txtDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 170, 250, 40));

        pnlRevenue.setBackground(new java.awt.Color(154, 151, 33));
        pnlRevenue.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Revenue");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Value");

        javax.swing.GroupLayout pnlRevenueLayout = new javax.swing.GroupLayout(pnlRevenue);
        pnlRevenue.setLayout(pnlRevenueLayout);
        pnlRevenueLayout.setHorizontalGroup(
            pnlRevenueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRevenueLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(pnlRevenueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(99, Short.MAX_VALUE))
        );
        pnlRevenueLayout.setVerticalGroup(
            pnlRevenueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRevenueLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        getContentPane().add(pnlRevenue, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 220, 340, 120));

        pnlReportType.setBackground(new java.awt.Color(52, 86, 109));
        pnlReportType.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Selected report type");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Value");

        javax.swing.GroupLayout pnlReportTypeLayout = new javax.swing.GroupLayout(pnlReportType);
        pnlReportType.setLayout(pnlReportTypeLayout);
        pnlReportTypeLayout.setHorizontalGroup(
            pnlReportTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlReportTypeLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(pnlReportTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(99, Short.MAX_VALUE))
        );
        pnlReportTypeLayout.setVerticalGroup(
            pnlReportTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlReportTypeLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        getContentPane().add(pnlReportType, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 220, 340, 120));

        pnlTransactions.setBackground(new java.awt.Color(126, 139, 74));
        pnlTransactions.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Transactions");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Value");

        javax.swing.GroupLayout pnlTransactionsLayout = new javax.swing.GroupLayout(pnlTransactions);
        pnlTransactions.setLayout(pnlTransactionsLayout);
        pnlTransactionsLayout.setHorizontalGroup(
            pnlTransactionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTransactionsLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(pnlTransactionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(99, Short.MAX_VALUE))
        );
        pnlTransactionsLayout.setVerticalGroup(
            pnlTransactionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTransactionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        getContentPane().add(pnlTransactions, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 220, 340, 120));

        lblReportType.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblReportType.setText("Report Type:");
        getContentPane().add(lblReportType, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 170, -1, 40));

        btnRefresh.setBackground(new java.awt.Color(124, 144, 84));
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnRefresh.setForeground(new java.awt.Color(255, 255, 255));
        btnRefresh.setText("Refresh");
        btnRefresh.setBorder(null);
        getContentPane().add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(1080, 170, 140, 40));

        btnGenerate.setBackground(new java.awt.Color(72, 92, 13));
        btnGenerate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnGenerate.setForeground(new java.awt.Color(255, 255, 255));
        btnGenerate.setText("Generate");
        btnGenerate.setBorder(null);
        getContentPane().add(btnGenerate, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 170, 140, 40));

        jTable1.setBackground(new java.awt.Color(245, 245, 245));
        jTable1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTable1.setForeground(new java.awt.Color(33, 33, 33));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Date", "Sale ID", "Total Amount", "Cash Received", "Change"
            }
        ));
        jTable1.setEnabled(false);
        jTable1.setGridColor(new java.awt.Color(210, 210, 210));
        jTable1.setRowHeight(28);
        jTable1.getTableHeader().setResizingAllowed(false);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable1);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 360, 1040, 320));

        jPanel1.setBackground(new java.awt.Color(245, 245, 245));
        jPanel1.setPreferredSize(new java.awt.Dimension(375, 112));

        lblTotalRevenueValue.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTotalRevenueValue.setText("P00.000");

        lblTotalRevenueText.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblTotalRevenueText.setText("Total Revenue:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(728, Short.MAX_VALUE)
                .addComponent(lblTotalRevenueText)
                .addGap(24, 24, 24)
                .addComponent(lblTotalRevenueValue)
                .addGap(43, 43, 43))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotalRevenueText)
                    .addComponent(lblTotalRevenueValue))
                .addContainerGap())
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 680, 1040, 50));

        btnReports.setBackground(new java.awt.Color(126, 139, 74));
        btnReports.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnReports.setForeground(new java.awt.Color(255, 255, 255));
        btnReports.setText("REPORTS");
        btnReports.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnReports.addActionListener(this::btnReportsActionPerformed);
        getContentPane().add(btnReports, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 600, 140, -1));

        btnHome.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnHome.setText("HOME");
        btnHome.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnHome.addActionListener(this::btnHomeActionPerformed);
        getContentPane().add(btnHome, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 290, 140, -1));

        btnSales.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnSales.setText("SALES");
        btnSales.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSales.addActionListener(this::btnSalesActionPerformed);
        getContentPane().add(btnSales, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 360, 140, -1));

        btnCustomers.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnCustomers.setText("CUSTOMERS");
        btnCustomers.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnCustomers.addActionListener(this::btnCustomersActionPerformed);
        getContentPane().add(btnCustomers, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 400, 140, -1));

        btnBrand.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnBrand.setText("BRAND");
        btnBrand.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnBrand.addActionListener(this::btnBrandActionPerformed);
        getContentPane().add(btnBrand, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 440, 140, -1));

        btnCategory.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnCategory.setText("CATEGORY");
        btnCategory.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnCategory.addActionListener(this::btnCategoryActionPerformed);
        getContentPane().add(btnCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 480, 140, -1));

        btnProducts.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnProducts.setText("PRODUCTS");
        btnProducts.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnProducts.addActionListener(this::btnProductsActionPerformed);
        getContentPane().add(btnProducts, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 520, 140, -1));

        btnStocks.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnStocks.setText("STOCKS");
        btnStocks.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnStocks.addActionListener(this::btnStocksActionPerformed);
        getContentPane().add(btnStocks, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 560, 140, -1));

        lblBackground.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/pages/Home page_A.png"))); // NOI18N
        lblBackground.setPreferredSize(new java.awt.Dimension(1633, 768));
        getContentPane().add(lblBackground, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1364, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDateActionPerformed
        // TODO add your handling code here:
        generateReport();
    }//GEN-LAST:event_txtDateActionPerformed

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
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new reports().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrand;
    private javax.swing.JButton btnCategory;
    private javax.swing.JButton btnCustomers;
    private javax.swing.JButton btnGenerate;
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnProducts;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnReports;
    private javax.swing.JButton btnSales;
    private javax.swing.JButton btnStocks;
    private javax.swing.JComboBox<String> cmbReportType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblBackground;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblTotalRevenueText;
    private javax.swing.JLabel lblTotalRevenueValue;
    private javax.swing.JPanel pnlReportType;
    private javax.swing.JPanel pnlRevenue;
    private javax.swing.JPanel pnlTransactions;
    private javax.swing.JTextField txtDate;
    // End of variables declaration//GEN-END:variables
}
