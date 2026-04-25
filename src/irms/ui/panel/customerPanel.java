/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package irms.ui.panel;

import irms.db.MySQLConnect;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JTable;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
/**
 *
 * @author USER
 */
public class customerPanel extends javax.swing.JPanel {

    /**
     * Creates new form customerPanel
     */
    public customerPanel() {
        initComponents();
        lblBackground.setIcon(new javax.swing.ImageIcon(
            getClass().getResource("/irms/resources/background/Customer Debt Panel.png")
        ));
        styleTable();
        loadDebts();
        autoSearchDebts();
        
        jTable1.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
    }
    
    private void styleTable() {
        jTable1.setRowHeight(28);
        jTable1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        jTable1.setGridColor(new Color(180, 180, 180));
        jTable1.setSelectionBackground(new Color(180, 200, 160));
        jTable1.setSelectionForeground(Color.BLACK);
        jTable1.setRowSelectionAllowed(true);
        jTable1.setFocusable(false);

        JTableHeader header = jTable1.getTableHeader();
        header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        header.setBackground(new Color(220, 229, 236));
        header.setForeground(new Color(54, 67, 20));
        header.setReorderingAllowed(false);
    }

    private void loadDebts() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        String sql =
            "SELECT u.utang_id, c.customer_name, COALESCE(c.contact_number, '') AS contact_number, " +
            "u.total_amount, u.amount_paid, u.status " +
            "FROM utang u " +
            "INNER JOIN customers c ON u.customer_id = c.customer_id " +
            "ORDER BY u.utang_id DESC";

        BigDecimal totalOutstanding = BigDecimal.ZERO;
        int totalCustomersWithDebt = 0;

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                BigDecimal totalDebt = rs.getBigDecimal("total_amount");
                BigDecimal amountPaid = rs.getBigDecimal("amount_paid");

                model.addRow(new Object[]{
                    rs.getInt("utang_id"),
                    rs.getString("customer_name"),
                    rs.getString("contact_number"),
                    String.format("₱%.2f", totalDebt),
                    String.format("₱%.2f", amountPaid),
                    rs.getString("status")
                });

                totalCustomersWithDebt++;
                totalOutstanding = totalOutstanding.add(totalDebt.subtract(amountPaid));
            }

            jLabel5.setText(String.valueOf(totalCustomersWithDebt));
            jLabel4.setText(String.format("₱%.2f", totalOutstanding));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Load debts error: " + e.getMessage());
        }
    }

    private void searchDebts(String keyword) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        String sql =
            "SELECT u.utang_id, c.customer_name, COALESCE(c.contact_number, '') AS contact_number, " +
            "u.total_amount, u.amount_paid, u.status " +
            "FROM utang u " +
            "INNER JOIN customers c ON u.customer_id = c.customer_id " +
            "WHERE CAST(u.utang_id AS CHAR) LIKE ? " +
            "   OR c.customer_name LIKE ? " +
            "   OR COALESCE(c.contact_number, '') LIKE ? " +
            "   OR u.status LIKE ? " +
            "ORDER BY u.utang_id DESC";

        BigDecimal totalOutstanding = BigDecimal.ZERO;
        int totalCustomersWithDebt = 0;

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            pst.setString(1, like);
            pst.setString(2, like);
            pst.setString(3, like);
            pst.setString(4, like);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    BigDecimal totalDebt = rs.getBigDecimal("total_amount");
                    BigDecimal amountPaid = rs.getBigDecimal("amount_paid");

                    model.addRow(new Object[]{
                        rs.getInt("utang_id"),
                        rs.getString("customer_name"),
                        rs.getString("contact_number"),
                        String.format("₱%.2f", totalDebt),
                        String.format("₱%.2f", amountPaid),
                        rs.getString("status")
                    });

                    totalCustomersWithDebt++;
                    totalOutstanding = totalOutstanding.add(totalDebt.subtract(amountPaid));
                }
            }

            jLabel5.setText(String.valueOf(totalCustomersWithDebt));
            jLabel4.setText(String.format("₱%.2f", totalOutstanding));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Search debts error: " + e.getMessage());
        }
    }

    private void autoSearchDebts() {
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
                String keyword = txtSearch.getText().trim();
                if (keyword.isEmpty()) {
                    loadDebts();
                } else {
                    searchDebts(keyword);
                }
            }
        });
    }

    private int getOrCreateCustomer(Connection conn, String fullName, String contactNumber) throws SQLException {
        String findSql = "SELECT customer_id FROM customers WHERE customer_name = ? AND COALESCE(contact_number, '') = ?";
        try (PreparedStatement pst = conn.prepareStatement(findSql)) {
            pst.setString(1, fullName);
            pst.setString(2, contactNumber == null ? "" : contactNumber);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customer_id");
                }
            }
        }

        String insertSql = "INSERT INTO customers (customer_name, contact_number) VALUES (?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, fullName);
            pst.setString(2, contactNumber);

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    private String computeStatus(BigDecimal totalAmount, BigDecimal amountPaid) {
        BigDecimal remaining = totalAmount.subtract(amountPaid);

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            return "PAID";
        } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            return "PARTIALLY PAID";
        } else {
            return "UNPAID";
        }
    }

    private int getSelectedUtangId() {
        int row = jTable1.getSelectedRow();
        if (row == -1) {
            return -1;
        }
        return Integer.parseInt(jTable1.getValueAt(row, 0).toString());
    }

    private void showAddDebtDialog() {
        JTextField txtName = new JTextField();
        JTextField txtPhone = new JTextField();
        JTextField txtAmount = new JTextField();
        JTextField txtRemarks = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.add(new JLabel("Customer Name:"));
        panel.add(txtName);
        panel.add(new JLabel("Phone Number:"));
        panel.add(txtPhone);
        panel.add(new JLabel("Debt Amount:"));
        panel.add(txtAmount);
        panel.add(new JLabel("Remarks (Optional):"));
        panel.add(txtRemarks);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Debt",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String fullName = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String amountText = txtAmount.getText().trim();
        String remarks = txtRemarks.getText().trim();

        if (fullName.isEmpty() || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Customer name and debt amount are required.");
            return;
        }

        BigDecimal totalAmount;
        try {
            totalAmount = new BigDecimal(amountText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Debt amount must be a valid number.");
            return;
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Debt amount must be greater than 0.");
            return;
        }

        try (Connection conn = MySQLConnect.getConnection()) {
            int customerId = getOrCreateCustomer(conn, fullName, phone);

            if (customerId == -1) {
                JOptionPane.showMessageDialog(this, "Could not create or find customer.");
                return;
            }

            String sql = "INSERT INTO utang (customer_id, sale_id, utang_date, total_amount, amount_paid, remaining_balance, status, remarks) " +
                         "VALUES (?, NULL, NOW(), ?, 0.00, ?, 'UNPAID', ?)";

            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setInt(1, customerId);
                pst.setBigDecimal(2, totalAmount);
                pst.setBigDecimal(3, totalAmount);
                pst.setString(4, remarks.isEmpty() ? null : remarks);
                pst.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Debt added successfully.");
            loadDebts();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Add debt error: " + e.getMessage());
        }
    }

    private void showRecordPaymentDialog() {
        int utangId = getSelectedUtangId();

        if (utangId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a debt record first.");
            return;
        }

        String customerName = "";
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal amountPaid = BigDecimal.ZERO;
        BigDecimal remaining = BigDecimal.ZERO;

        try (Connection conn = MySQLConnect.getConnection()) {
            String fetchSql =
                "SELECT c.customer_name, u.total_amount, u.amount_paid, u.remaining_balance " +
                "FROM utang u " +
                "INNER JOIN customers c ON u.customer_id = c.customer_id " +
                "WHERE u.utang_id = ?";

            try (PreparedStatement pst = conn.prepareStatement(fetchSql)) {
                pst.setInt(1, utangId);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        customerName = rs.getString("customer_name");
                        totalAmount = rs.getBigDecimal("total_amount");
                        amountPaid = rs.getBigDecimal("amount_paid");
                        remaining = rs.getBigDecimal("remaining_balance");
                    } else {
                        JOptionPane.showMessageDialog(this, "Debt record not found.");
                        return;
                    }
                }
            }

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "This debt is already fully paid.");
                return;
            }

            JLabel lblCustomer = new JLabel("Customer: " + customerName);
            JLabel lblOriginal = new JLabel("Original Debt: ₱" + totalAmount.toPlainString());
            JLabel lblPaid = new JLabel("Total Paid: ₱" + amountPaid.toPlainString());
            JLabel lblRemaining = new JLabel("Remaining Balance: ₱" + remaining.toPlainString());

            JTextField txtPayment = new JTextField();
            JTextField txtRemarks = new JTextField();

            JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
            panel.add(lblCustomer);
            panel.add(lblOriginal);
            panel.add(lblPaid);
            panel.add(lblRemaining);
            panel.add(new JLabel("Payment Amount:"));
            panel.add(txtPayment);
            panel.add(new JLabel("Remarks (Optional):"));
            panel.add(txtRemarks);

            int result = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    "Record Payment",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String paymentText = txtPayment.getText().trim();
            String remarks = txtRemarks.getText().trim();

            if (paymentText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Payment amount is required.");
                return;
            }

            BigDecimal paymentAmount;
            try {
                paymentAmount = new BigDecimal(paymentText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Payment amount must be a valid number.");
                return;
            }

            if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Payment amount must be greater than 0.");
                return;
            }

            if (paymentAmount.compareTo(remaining) > 0) {
                JOptionPane.showMessageDialog(this, "Payment amount cannot exceed remaining balance.");
                return;
            }

            BigDecimal newAmountPaid = amountPaid.add(paymentAmount);
            BigDecimal newRemaining = totalAmount.subtract(newAmountPaid);
            String newStatus = computeStatus(totalAmount, newAmountPaid);

            conn.setAutoCommit(false);

            String insertPaymentSql = "INSERT INTO utang_payments (utang_id, payment_date, payment_amount, remarks) VALUES (?, NOW(), ?, ?)";
            try (PreparedStatement pst = conn.prepareStatement(insertPaymentSql)) {
                pst.setInt(1, utangId);
                pst.setBigDecimal(2, paymentAmount);
                pst.setString(3, remarks.isEmpty() ? null : remarks);
                pst.executeUpdate();
            }

            String updateUtangSql = "UPDATE utang SET amount_paid = ?, remaining_balance = ?, status = ? WHERE utang_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(updateUtangSql)) {
                pst.setBigDecimal(1, newAmountPaid);
                pst.setBigDecimal(2, newRemaining);
                pst.setString(3, newStatus);
                pst.setInt(4, utangId);
                pst.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            JOptionPane.showMessageDialog(this, "Payment recorded successfully.");
            loadDebts();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Record payment error: " + e.getMessage());
        }
    }

    private void showDebtDetailsDialog() {
        int utangId = getSelectedUtangId();

        if (utangId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a debt record first.");
            return;
        }

        try (Connection conn = MySQLConnect.getConnection()) {
            String debtSql =
                "SELECT u.utang_id, c.customer_name, COALESCE(c.contact_number, '') AS contact_number, " +
                "u.utang_date, u.total_amount, u.amount_paid, u.remaining_balance, u.status, COALESCE(u.remarks, '') AS remarks " +
                "FROM utang u " +
                "INNER JOIN customers c ON u.customer_id = c.customer_id " +
                "WHERE u.utang_id = ?";

            StringBuilder details = new StringBuilder();

            try (PreparedStatement pst = conn.prepareStatement(debtSql)) {
                pst.setInt(1, utangId);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        details.append("Debt ID: ").append(rs.getInt("utang_id")).append("\n");
                        details.append("Customer Name: ").append(rs.getString("customer_name")).append("\n");
                        details.append("Phone Number: ").append(rs.getString("contact_number")).append("\n");
                        details.append("Debt Date: ").append(rs.getString("utang_date")).append("\n");
                        details.append("Original Debt: ₱").append(rs.getBigDecimal("total_amount")).append("\n");
                        details.append("Amount Paid: ₱").append(rs.getBigDecimal("amount_paid")).append("\n");
                        details.append("Remaining Balance: ₱").append(rs.getBigDecimal("remaining_balance")).append("\n");
                        details.append("Status: ").append(rs.getString("status")).append("\n");
                        details.append("Remarks: ").append(rs.getString("remarks")).append("\n\n");
                        details.append("----- Payment History -----\n");
                    } else {
                        JOptionPane.showMessageDialog(this, "Debt record not found.");
                        return;
                    }
                }
            }

            String paymentSql =
                "SELECT payment_date, payment_amount, COALESCE(remarks, '') AS remarks " +
                "FROM utang_payments " +
                "WHERE utang_id = ? " +
                "ORDER BY payment_date DESC";

            boolean hasPayments = false;

            try (PreparedStatement pst = conn.prepareStatement(paymentSql)) {
                pst.setInt(1, utangId);

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        hasPayments = true;
                        details.append("Date: ").append(rs.getString("payment_date"))
                               .append(" | Amount: ₱").append(rs.getBigDecimal("payment_amount"))
                               .append(" | Remarks: ").append(rs.getString("remarks"))
                               .append("\n");
                    }
                }
            }

            if (!hasPayments) {
                details.append("No payment history yet.");
            }

            JTextArea textArea = new JTextArea(details.toString(), 20, 40);
            textArea.setEditable(false);
            textArea.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            JOptionPane.showMessageDialog(
                    this,
                    new JScrollPane(textArea),
                    "View Details",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "View details error: " + e.getMessage());
        }
    }
    
    private class StatusCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            String status = value == null ? "" : value.toString();

            if (!isSelected) {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);

                switch (status) {
                    case "PAID":
                        c.setBackground(new Color(198, 239, 206));
                        c.setForeground(new Color(0, 97, 0));
                        break;

                    case "PARTIALLY PAID":
                        c.setBackground(new Color(255, 235, 156));
                        c.setForeground(new Color(156, 101, 0));
                        break;

                    case "UNPAID":
                        c.setBackground(new Color(255, 199, 206));
                        c.setForeground(new Color(156, 0, 6));
                        break;
                }
            }

            setHorizontalAlignment(CENTER);
            return c;
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnAddDebt = new irms.ui.components.RoundedButtons();
        btnRecordPayment = new irms.ui.components.RoundedButtons();
        btnDetails = new irms.ui.components.RoundedButtons();
        lblBackground = new javax.swing.JLabel();

        setBackground(new java.awt.Color(200, 212, 222));
        setPreferredSize(new java.awt.Dimension(1130, 768));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Debt ID", "Customer Name", "Phone Number", "Total Debt", "Amount Paid", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, true, true, true, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 1050, 450));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setText("Search:");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, 60, 30));

        txtSearch.addActionListener(this::txtSearchActionPerformed);
        add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 160, 380, 30));

        jPanel1.setPreferredSize(new java.awt.Dimension(450, 100));

        jLabel2.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel2.setText("Total Customer with Debt:");

        jLabel3.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel3.setText("Total Outstanding Balance:");

        jLabel4.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel4.setText("0");

        jLabel5.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel5.setText("0");

        btnAddDebt.setBackground(new java.awt.Color(72, 92, 13));
        btnAddDebt.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddDebt.setForeground(new java.awt.Color(255, 255, 255));
        btnAddDebt.setText("Add Debt");
        btnAddDebt.addActionListener(this::btnAddDebtActionPerformed);

        btnRecordPayment.setBackground(new java.awt.Color(154, 151, 33));
        btnRecordPayment.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRecordPayment.setForeground(new java.awt.Color(255, 255, 255));
        btnRecordPayment.setText("Record Payment");
        btnRecordPayment.addActionListener(this::btnRecordPaymentActionPerformed);

        btnDetails.setBackground(new java.awt.Color(72, 92, 13));
        btnDetails.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDetails.setForeground(new java.awt.Color(255, 255, 255));
        btnDetails.setText("View Details");
        btnDetails.addActionListener(this::btnDetailsActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddDebt, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnRecordPayment, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(202, 202, 202)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jLabel2))
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddDebt, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnRecordPayment, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDetails, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(jLabel5))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(jLabel4))
                            .addGap(0, 0, Short.MAX_VALUE))))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 640, 1060, -1));

        lblBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/background/Customer Debt Panel.png"))); // NOI18N
        add(lblBackground, new org.netbeans.lib.awtextra.AbsoluteConstraints(-240, 0, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnAddDebtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddDebtActionPerformed
        // TODO add your handling code here:
        showAddDebtDialog();
    }//GEN-LAST:event_btnAddDebtActionPerformed

    private void btnRecordPaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecordPaymentActionPerformed
        // TODO add your handling code here:
        showRecordPaymentDialog();
    }//GEN-LAST:event_btnRecordPaymentActionPerformed

    private void btnDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetailsActionPerformed
        // TODO add your handling code here:
        showDebtDetailsDialog();
    }//GEN-LAST:event_btnDetailsActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddDebt;
    private javax.swing.JButton btnDetails;
    private javax.swing.JButton btnRecordPayment;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblBackground;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
