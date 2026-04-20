/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package irms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author USER
 */
public class stockPanel extends javax.swing.JPanel {

    /**
     * Creates new form stockPanel
     */
    public stockPanel() {
        initComponents();
        lblTotalProductsValue = new javax.swing.JLabel("0");
        lblLowStockValue = new javax.swing.JLabel("0");
        lblNoStockValue = new javax.swing.JLabel("0");

        jPanel1.removeAll();
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 10));

        jPanel1.add(new javax.swing.JLabel("Total Products:"));
        jPanel1.add(lblTotalProductsValue);
        jPanel1.add(new javax.swing.JLabel("Low Stock Items:"));
        jPanel1.add(lblLowStockValue);
        jPanel1.add(new javax.swing.JLabel("No Stock Items:"));
        jPanel1.add(lblNoStockValue);

        jPanel1.revalidate();
        jPanel1.repaint();

        loadCategories();
        loadStocks();
        autoSearchStocks();
        autoFilterCategory();
        styleTable();
    }
    
    private javax.swing.JLabel lblTotalProductsValue;
    private javax.swing.JLabel lblLowStockValue;
    private javax.swing.JLabel lblNoStockValue;
    
    private void loadCategories() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("All");

        String sql = "SELECT category_name FROM categories ORDER BY category_name";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                model.addElement(rs.getString("category_name"));
            }

            cmbCategory.setModel(model);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Category load error: " + e.getMessage());
        }
    }

    private void loadStocks() {
        DefaultTableModel model = (DefaultTableModel) tblStocks.getModel();
        model.setRowCount(0);

        String sql =
            "SELECT s.stock_id, p.product_name, b.brand_name, s.quantity, s.unit_price, " +
            "p.unit_label, s.stock_date, s.stock_time, " +
            "CASE " +
            "   WHEN s.quantity <= 0 THEN 'OUT OF STOCK' " +
            "   WHEN s.quantity <= s.reorder_level THEN 'LOW STOCK' " +
            "   ELSE 'IN STOCK' " +
            "END AS stock_status " +
            "FROM stocks s " +
            "INNER JOIN products p ON s.product_id = p.product_id " +
            "LEFT JOIN brands b ON p.brand_id = b.brand_id " +
            "LEFT JOIN categories c ON p.category_id = c.category_id " +
            "ORDER BY s.stock_id";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            int totalProducts = 0;
            int lowStockItems = 0;
            int noStockItems = 0;

            while (rs.next()) {
                String status = rs.getString("stock_status");

                String unitLabel = rs.getString("unit_label");
                BigDecimal quantity = rs.getBigDecimal("quantity");

                model.addRow(new Object[]{
                    rs.getInt("stock_id"),
                    rs.getString("product_name"),
                    rs.getString("brand_name"),
                    formatStockDisplay(quantity, unitLabel),
                    unitLabel,
                    String.format("₱%.2f", rs.getBigDecimal("unit_price")),
                    rs.getDate("stock_date"),
                    rs.getTime("stock_time"),
                    status
                });

                totalProducts++;

                if ("LOW STOCK".equalsIgnoreCase(status)) {
                    lowStockItems++;
                } else if ("OUT OF STOCK".equalsIgnoreCase(status)) {
                    noStockItems++;
                }
            }

            lblTotalProductsValue.setText(String.valueOf(totalProducts));
            lblLowStockValue.setText(String.valueOf(lowStockItems));
            lblNoStockValue.setText(String.valueOf(noStockItems));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Load stocks error: " + e.getMessage());
        }
    }

    private void searchAndFilterStocks() {
        DefaultTableModel model = (DefaultTableModel) tblStocks.getModel();
        model.setRowCount(0);

        String keyword = searchbar.getText().trim();
        String selectedCategory = cmbCategory.getSelectedItem() == null ? "All" : cmbCategory.getSelectedItem().toString();

        String sql =
            "SELECT s.stock_id, p.product_name, b.brand_name, s.quantity, s.unit_price, " +
            "p.unit_label, s.stock_date, s.stock_time, " +
            "CASE " +
            "   WHEN s.quantity <= 0 THEN 'OUT OF STOCK' " +
            "   WHEN s.quantity <= s.reorder_level THEN 'LOW STOCK' " +
            "   ELSE 'IN STOCK' " +
            "END AS stock_status " +
            "FROM stocks s " +
            "INNER JOIN products p ON s.product_id = p.product_id " +
            "LEFT JOIN brands b ON p.brand_id = b.brand_id " +
            "LEFT JOIN categories c ON p.category_id = c.category_id " +
            "WHERE (p.product_name LIKE ? OR b.brand_name LIKE ? OR CAST(s.stock_id AS CHAR) LIKE ?) " +
            "AND (? = 'All' OR c.category_name = ?) " +
            "ORDER BY s.stock_id";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";

            pst.setString(1, like);
            pst.setString(2, like);
            pst.setString(3, like);
            pst.setString(4, selectedCategory);
            pst.setString(5, selectedCategory);

            try (ResultSet rs = pst.executeQuery()) {
                int totalProducts = 0;
                int lowStockItems = 0;
                int noStockItems = 0;
                
                while (rs.next()) {
                    String status = rs.getString("stock_status");

                    String unitLabel = rs.getString("unit_label");
                    BigDecimal quantity = rs.getBigDecimal("quantity");

                    model.addRow(new Object[]{
                        rs.getInt("stock_id"),
                        rs.getString("product_name"),
                        rs.getString("brand_name"),
                        formatStockDisplay(quantity, unitLabel),
                        unitLabel,
                        String.format("₱%.2f", rs.getBigDecimal("unit_price")),
                        rs.getDate("stock_date"),
                        rs.getTime("stock_time"),
                        status
                    });

                    totalProducts++;

                    if ("LOW STOCK".equalsIgnoreCase(status)) {
                        lowStockItems++;
                    } else if ("OUT OF STOCK".equalsIgnoreCase(status)) {
                        noStockItems++;
                    }
                }

                lblTotalProductsValue.setText(String.valueOf(totalProducts));
                lblLowStockValue.setText(String.valueOf(lowStockItems));
                lblNoStockValue.setText(String.valueOf(noStockItems));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Search/filter error: " + e.getMessage());
        }
    }

    private void autoSearchStocks() {
        searchbar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchAndFilterStocks();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchAndFilterStocks();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchAndFilterStocks();
            }
        });
    }

    private void autoFilterCategory() {
        cmbCategory.addActionListener(e -> searchAndFilterStocks());
    }

    private int getProductIdByName(String productName) {
        String sql = "SELECT product_id FROM products WHERE product_name = ?";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, productName);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("product_id");
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Product lookup error: " + e.getMessage());
        }

        return -1;
    }
    
    private BigDecimal getProductCostPriceById(int productId) {
    String sql = "SELECT cost_price FROM products WHERE product_id = ?";

    try (Connection conn = MySQLConnect.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setInt(1, productId);

        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal("cost_price");
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Cost price lookup error: " + e.getMessage());
    }

    return null;
}
    
    private String getProductUnitLabelById(int productId) {
        String sql = "SELECT unit_label FROM products WHERE product_id = ?";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, productId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String unitLabel = rs.getString("unit_label");
                    return unitLabel == null || unitLabel.trim().isEmpty() ? "pc" : unitLabel;
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Unit label lookup error: " + e.getMessage());
        }

        return "pc";
    }
    
    private String formatStockDisplay(BigDecimal quantity, String unitLabel) {
        if (quantity == null) {
            return "0";
        }

        if (unitLabel != null && unitLabel.equalsIgnoreCase("kg")) {
            return String.format("%.2f", quantity);
        }

        return String.valueOf(quantity.intValue());
    }
    
    private BigDecimal parseStockValue(String stockText, String unitLabel) {
        stockText = stockText.trim();

        try {
            if (unitLabel != null && unitLabel.equalsIgnoreCase("kg")) {
                return new BigDecimal(stockText);
            } else {
                return new BigDecimal(Integer.parseInt(stockText));
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private void logStockMovement(
        int productId,
        String movementType,
        BigDecimal quantity,
        String unitLabel,
        BigDecimal previousStock,
        BigDecimal newStock,
        String remarks
    ) {
        String sql = "INSERT INTO stock_movements " +
                     "(product_id, movement_type, quantity, unit_label, previous_stock, new_stock, remarks, moved_by, movement_date, movement_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURDATE(), CURTIME())";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, productId);
            pst.setString(2, movementType);
            pst.setBigDecimal(3, quantity);
            pst.setString(4, unitLabel);
            pst.setBigDecimal(5, previousStock);
            pst.setBigDecimal(6, newStock);
            pst.setString(7, remarks);
            pst.setInt(8, session.userId);

            pst.executeUpdate();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Log movement error: " + e.getMessage());
        }
    }

    private void fillProductCombo(JComboBox<String> cmbProduct) {
        String sql = "SELECT product_name FROM products WHERE status = 'ACTIVE' ORDER BY product_name";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                cmbProduct.addItem(rs.getString("product_name"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Product combo error: " + e.getMessage());
        }
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

        cmbCategory = new javax.swing.JComboBox<>();
        btnStockOut = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btnAdd = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnStockOut1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblStocks = new javax.swing.JTable();
        searchbar = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        add(cmbCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 160, 190, 30));

        btnStockOut.setText("Stock Movement");
        btnStockOut.addActionListener(this::btnStockOutActionPerformed);
        add(btnStockOut, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 700, 150, 30));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setText("Category:");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 160, 70, 30));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Search:");
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 50, 30));

        btnAdd.setText("Add");
        btnAdd.addActionListener(this::btnAddActionPerformed);
        add(btnAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 160, 100, 30));

        btnDelete.setText("Delete");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);
        add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 160, 100, 30));

        btnStockOut1.setText("Stock Out");
        btnStockOut1.addActionListener(this::btnStockOut1ActionPerformed);
        add(btnStockOut1, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 160, 100, 30));

        tblStocks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Stock ID", "Product Name", "Brand", "Stock", "Unit Type", "Unit Price", "Date", "Time", "Status"
            }
        ));
        jScrollPane1.setViewportView(tblStocks);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, 1060, 440));

        searchbar.addActionListener(this::searchbarActionPerformed);
        add(searchbar, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 160, 270, 30));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Total Products: ");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Low Stock Items: ");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("No Stock Items: ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addGap(71, 71, 71)
                .addComponent(jLabel5)
                .addGap(93, 93, 93)
                .addComponent(jLabel6)
                .addContainerGap(576, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 650, 1060, 40));
    }// </editor-fold>//GEN-END:initComponents

    private void btnStockOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStockOutActionPerformed
        // TODO add your handling code here:
        stockHistory sh = new stockHistory();
        sh.setVisible(true);
        
    }//GEN-LAST:event_btnStockOutActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        JComboBox<String> cmbProduct = new JComboBox<>();
        fillProductCombo(cmbProduct);

        JLabel lblUnitDisplayTitle = new JLabel("Unit:");
        JLabel lblUnitDisplayValue = new JLabel("-");

        JTextField txtQty = new JTextField();
        JTextField txtReorder = new JTextField("5");

        if (cmbProduct.getItemCount() > 0) {
            int firstProductId = getProductIdByName(cmbProduct.getSelectedItem().toString());
            lblUnitDisplayValue.setText(getProductUnitLabelById(firstProductId));
        }

        cmbProduct.addActionListener(e -> {
            if (cmbProduct.getSelectedItem() != null) {
                int selectedProductId = getProductIdByName(cmbProduct.getSelectedItem().toString());
                lblUnitDisplayValue.setText(getProductUnitLabelById(selectedProductId));
            }
        });

        JPanel panel = new JPanel(new java.awt.GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Product:"));
        panel.add(cmbProduct);
        panel.add(lblUnitDisplayTitle);
        panel.add(lblUnitDisplayValue);
        panel.add(new JLabel("Stock:"));
        panel.add(txtQty);
        panel.add(new JLabel("Reorder Level:"));
        panel.add(txtReorder);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Stock", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String productName = cmbProduct.getSelectedItem() == null ? "" : cmbProduct.getSelectedItem().toString();
        String qtyText = txtQty.getText().trim();
        String reorderText = txtReorder.getText().trim();

        if (productName.isEmpty() || qtyText.isEmpty() || reorderText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        int productId = getProductIdByName(productName);
        if (productId == -1) {
            JOptionPane.showMessageDialog(this, "Product not found.");
            return;
        }

        BigDecimal quantity;
        BigDecimal reorderLevel;
        BigDecimal unitPrice;
        String unitLabel = getProductUnitLabelById(productId);

        try {
            quantity = new BigDecimal(qtyText);
            reorderLevel = new BigDecimal(reorderText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format.");
            return;
        }

        unitPrice = getProductCostPriceById(productId);

        if (unitPrice == null) {
            JOptionPane.showMessageDialog(this, "Could not get product cost price.");
            return;
        }

        if (quantity.compareTo(BigDecimal.ZERO) <= 0 ||
            reorderLevel.compareTo(BigDecimal.ZERO) < 0 ||
            unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            JOptionPane.showMessageDialog(this, "Stock must be greater than 0 and values cannot be negative.");
            return;
        }

        if (!unitLabel.equalsIgnoreCase("kg") && quantity.stripTrailingZeros().scale() > 0) {
            JOptionPane.showMessageDialog(this, "Piece items must use whole numbers only.");
            return;
        }

        String checkSql = "SELECT quantity FROM stocks WHERE product_id = ?";
        String updateSql = "UPDATE stocks " +
        "SET quantity = quantity + ?, unit_price = ?, reorder_level = ?, stock_date = CURDATE(), stock_time = CURTIME() " +
        "WHERE product_id = ?";
        String insertSql = "INSERT INTO stocks (product_id, quantity, unit_price, reorder_level, stock_date, stock_time) " +
        "VALUES (?, ?, ?, ?, CURDATE(), CURTIME())";

        try (Connection conn = MySQLConnect.getConnection()) {

            boolean exists = false;
            BigDecimal previousStock = BigDecimal.ZERO;

            try (PreparedStatement checkPst = conn.prepareStatement(checkSql)) {
                checkPst.setInt(1, productId);

                try (ResultSet rs = checkPst.executeQuery()) {
                    if (rs.next()) {
                        exists = true;
                        previousStock = rs.getBigDecimal("quantity");
                        if (previousStock == null) {
                            previousStock = BigDecimal.ZERO;
                        }
                    }
                }
            }

            BigDecimal newStock = previousStock.add(quantity);

            if (exists) {
                try (PreparedStatement pst = conn.prepareStatement(updateSql)) {
                    pst.setBigDecimal(1, quantity);
                    pst.setBigDecimal(2, unitPrice);
                    pst.setBigDecimal(3, reorderLevel);
                    pst.setInt(4, productId);
                    pst.executeUpdate();
                }

                logStockMovement(
                    productId,
                    "IN",
                    quantity,
                    unitLabel,
                    previousStock,
                    newStock,
                    "Stock added"
                );

                JOptionPane.showMessageDialog(this, "Stock added and stacked successfully.");
            } else {
                try (PreparedStatement pst = conn.prepareStatement(insertSql)) {
                    pst.setInt(1, productId);
                    pst.setBigDecimal(2, quantity);
                    pst.setBigDecimal(3, unitPrice);
                    pst.setBigDecimal(4, reorderLevel);
                    pst.executeUpdate();
                }

                logStockMovement(
                    productId,
                    "IN",
                    quantity,
                    unitLabel,
                    BigDecimal.ZERO,
                    quantity,
                    "Initial stock"
                );

                JOptionPane.showMessageDialog(this, "Stock added successfully.");
            }

            loadStocks();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Add stock error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        int row = tblStocks.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock record first.");
            return;
        }

        int stockId = Integer.parseInt(tblStocks.getValueAt(row, 0).toString());
        String productName = tblStocks.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Delete stock record for " + productName + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM stocks WHERE stock_id = ?";

        try (Connection conn = MySQLConnect.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, stockId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Stock deleted successfully.");
            loadStocks();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Delete stock error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnStockOut1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStockOut1ActionPerformed
        // TODO add your handling code here:
        int row = tblStocks.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock record first.");
            return;
        }

        int stockId = Integer.parseInt(tblStocks.getValueAt(row, 0).toString());
        String productName = tblStocks.getValueAt(row, 1).toString();
        String stockText = tblStocks.getValueAt(row, 3).toString();
        String unitLabel = tblStocks.getValueAt(row, 4).toString();

        BigDecimal currentStock = parseStockValue(stockText, unitLabel);

        if (currentStock == null) {
            JOptionPane.showMessageDialog(this, "Invalid stock value.");
            return;
        }

        if (currentStock.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "This item is already out of stock.");
            return;
        }

        String qtyInput = JOptionPane.showInputDialog(
            this,
            "Enter stock out amount for " + productName + " (" + unitLabel + "):",
            "0"
        );

        if (qtyInput == null) {
            return;
        }

        qtyInput = qtyInput.trim();

        if (qtyInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Stock out amount is required.");
            return;
        }

        BigDecimal stockOutQty;

        try {
            stockOutQty = new BigDecimal(qtyInput);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format.");
            return;
        }

        if (stockOutQty.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Stock out amount must be greater than 0.");
            return;
        }

        if (!unitLabel.equalsIgnoreCase("kg") && stockOutQty.stripTrailingZeros().scale() > 0) {
            JOptionPane.showMessageDialog(this, "Piece items must use whole numbers only.");
            return;
        }

        if (stockOutQty.compareTo(currentStock) > 0) {
            JOptionPane.showMessageDialog(this, "Stock out amount cannot exceed current stock.");
            return;
        }
        String[] remarkOptions = {"Sold", "Damaged", "Expired", "Custom"};

        String selectedRemark = (String) JOptionPane.showInputDialog(
            this,
            "Select remark:",
            "Stock Out Remark",
            JOptionPane.PLAIN_MESSAGE,
            null,
            remarkOptions,
            "Sold"
        );

        if (selectedRemark == null) {
            return;
        }

        String finalRemark;

        if (selectedRemark.equals("Custom")) {
            finalRemark = JOptionPane.showInputDialog(this, "Enter custom remark:");

            if (finalRemark == null) {
                return;
            }

            finalRemark = finalRemark.trim();

            if (finalRemark.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Custom remark is required.");
                return;
            }
        } else {
            finalRemark = selectedRemark;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Deduct " + stockOutQty + " " + unitLabel + " from " + productName +
            "?\nRemark: " + finalRemark,
            "Confirm Stock Out",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        int productId = getProductIdByName(productName);
        BigDecimal newStock = currentStock.subtract(stockOutQty);

        String sql = "UPDATE stocks SET quantity = quantity - ?, stock_date = CURDATE(), stock_time = CURTIME() WHERE stock_id = ?";

        try (Connection conn = MySQLConnect.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setBigDecimal(1, stockOutQty);
            pst.setInt(2, stockId);
            pst.executeUpdate();

            logStockMovement(
                productId,
                "OUT",
                stockOutQty,
                unitLabel,
                currentStock,
                newStock,
                finalRemark
            );

            JOptionPane.showMessageDialog(this, "Stock out recorded successfully.");
            loadStocks();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Stock out error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnStockOut1ActionPerformed

    private void searchbarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchbarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchbarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnStockOut;
    private javax.swing.JButton btnStockOut1;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField searchbar;
    private javax.swing.JTable tblStocks;
    // End of variables declaration//GEN-END:variables
}
