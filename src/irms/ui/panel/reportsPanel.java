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
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author USER
 */
public class reportsPanel extends javax.swing.JPanel {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(reportsPanel.class.getName());

    /**
     * Creates new form reportsPanel
     */
    public reportsPanel() {
        initComponents();
        lblBackground.setIcon(new javax.swing.ImageIcon(
            getClass().getResource("/irms/resources/background/Reports.png")
        ));
        jLabel1.setText("VAT Collected");
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
                "SELECT sale_id, DATE(sale_date) AS report_date, vatable_sales, vat_amount, total, cash_received, change_amount " +
                "FROM sales " +
                "WHERE " + filterCondition + " " +
                "ORDER BY sale_date DESC";

        String summarySql =
                "SELECT COUNT(*) AS transactions, " +
                "       COALESCE(SUM(total), 0) AS revenue, " +
                "       COALESCE(SUM(vat_amount), 0) AS total_vat " +
                "FROM sales " +
                "WHERE " + filterCondition;

        try (Connection conn = MySQLConnect.getConnection()) {

            try (PreparedStatement pst = conn.prepareStatement(detailSql)) {
                pst.setString(1, dateText);

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        int saleId = rs.getInt("sale_id");
                        String saleDate = rs.getString("report_date");
                        double vatableSales = rs.getDouble("vatable_sales");
                        double vatAmount = rs.getDouble("vat_amount");
                        double totalAmount = rs.getDouble("total");
                        double cashReceived = rs.getDouble("cash_received");
                        double changeAmount = rs.getDouble("change_amount");

                        model.addRow(new Object[]{
                            saleDate,
                            saleId,
                            String.format("₱%.2f", vatableSales),
                            String.format("₱%.2f", vatAmount),
                            String.format("₱%.2f", totalAmount),
                            String.format("₱%.2f", cashReceived),
                            String.format("₱%.2f", changeAmount)
                        });
                    }
                }
            }

            int totalTransactions = 0;
            double totalRevenue = 0.0;
            double totalVat = 0.0;

            try (PreparedStatement pst = conn.prepareStatement(summarySql)) {
                pst.setString(1, dateText);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        totalTransactions = rs.getInt("transactions");
                        totalRevenue = rs.getDouble("revenue");
                        totalVat = rs.getDouble("total_vat");
                    }
                }
            }

            jLabel4.setText(String.format("₱%.2f", totalRevenue));
            jLabel2.setText(String.format("₱%.2f", totalVat));
            jLabel6.setText(String.valueOf(totalTransactions));
            lblTotalRevenueValue.setText(String.format("₱%.2f", totalRevenue));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Report error: " + e.getMessage());
        }
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
        pnlRevenue = new irms.ui.components.RoundedPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        pnlReportType = new irms.ui.components.RoundedPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        pnlTransactions = new irms.ui.components.RoundedPanel();
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
        lblBackground = new javax.swing.JLabel();

        setBackground(new java.awt.Color(200, 212, 222));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblDate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblDate.setText("Date:");
        add(lblDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 170, -1, 40));

        cmbReportType.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        add(cmbReportType, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 170, 250, 40));

        txtDate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDate.addActionListener(this::txtDateActionPerformed);
        add(txtDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 170, 250, 40));

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

        add(pnlRevenue, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 220, 340, 120));

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

        add(pnlReportType, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, 340, 120));

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

        add(pnlTransactions, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 220, 340, 120));

        lblReportType.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblReportType.setText("Report Type:");
        add(lblReportType, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, 40));

        btnRefresh.setBackground(new java.awt.Color(124, 144, 84));
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnRefresh.setForeground(new java.awt.Color(255, 255, 255));
        btnRefresh.setText("Refresh");
        btnRefresh.setBorder(null);
        add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 170, 140, 40));

        btnGenerate.setBackground(new java.awt.Color(72, 92, 13));
        btnGenerate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnGenerate.setForeground(new java.awt.Color(255, 255, 255));
        btnGenerate.setText("Generate");
        btnGenerate.setBorder(null);
        add(btnGenerate, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 170, 140, 40));

        jTable1.setBackground(new java.awt.Color(245, 245, 245));
        jTable1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTable1.setForeground(new java.awt.Color(33, 33, 33));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Date", "Sale ID", "VATable Sales", "VAT Amount", "Total Amount", "Cash Received", "Change"
            }
        ));
        jTable1.setEnabled(false);
        jTable1.setGridColor(new java.awt.Color(210, 210, 210));
        jTable1.setRowHeight(28);
        jTable1.getTableHeader().setResizingAllowed(false);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 360, 1040, 320));

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

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 680, 1040, 50));

        lblBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/design/Reports.png"))); // NOI18N
        add(lblBackground, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void txtDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDateActionPerformed
        // TODO add your handling code here:
        generateReport();
    }//GEN-LAST:event_txtDateActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGenerate;
    private javax.swing.JButton btnRefresh;
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
