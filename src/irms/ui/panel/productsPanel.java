/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package irms.ui.panel;

import irms.db.MySQLConnect;
import java.awt.Color;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
/**
 *
 * @author USER
 */
public class productsPanel extends javax.swing.JPanel {

    /**
     * Creates new form productsPanel
     */
    public productsPanel() {
        initComponents();
        txtSearch.setEditable(true);
    txtSearch.setEnabled(true);
    txtSearch.setFocusable(true);

    attachTableSelectionHandlers();

    loadProducts();
    autoSearchProducts();
    styleProductTable();
    updateActionButtons();
    }
    
    private int getCategoryIdByName(Connection conn, String categoryName) throws SQLException {
        String sql = "SELECT category_id FROM categories WHERE category_name = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, categoryName);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("category_id");
                }
            }
        }
        return -1;
    }

    private void fillCategories(Connection conn, JComboBox<String> cmbCategory) throws SQLException {
        cmbCategory.removeAllItems();

        String sql = "SELECT category_name FROM categories ORDER BY category_name";

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                cmbCategory.addItem(rs.getString("category_name"));
            }
        }
    }
    
    private int getBrandIdByName(Connection conn, String brandName) throws SQLException {
    String sql = "SELECT brand_id FROM brands WHERE brand_name = ?";
    try (PreparedStatement pst = conn.prepareStatement(sql)) {
        pst.setString(1, brandName);
        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("brand_id");
            }
        }
    }
    return -1;
}
    
    private void fillBrands(Connection conn, JComboBox<String> cmbBrand) throws SQLException {
    cmbBrand.removeAllItems();

    String sql = "SELECT brand_name FROM brands ORDER BY brand_name";

    try (PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            cmbBrand.addItem(rs.getString("brand_name"));
        }
    }
}
    
    public void loadProducts() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);

    String sql = "SELECT " +
             "p.product_id, " +
             "p.product_name, " +
             "c.category_name, " +
             "b.brand_name, " +
             "p.cost_price, " +
             "p.selling_price, " +
             "p.unit_type, " +
             "p.status " +
             "FROM products p " +
             "INNER JOIN categories c ON p.category_id = c.category_id " +
             "LEFT JOIN brands b ON p.brand_id = b.brand_id " +
             "ORDER BY p.product_id";

    try (Connection conn = MySQLConnect.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getString("category_name"),
                rs.getString("brand_name"),
                String.format("₱ %.2f", rs.getBigDecimal("cost_price")),
                String.format("₱ %.2f", rs.getBigDecimal("selling_price")),
                rs.getString("unit_type"),
                rs.getString("status")
        });
        }
        updateActionButtons();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Load error: " + e.getMessage());
    }
    
    }
    
    public void searchProducts(String keyword) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);

    String sql = "SELECT " +
             "p.product_id, " +
             "p.product_name, " +
             "c.category_name, " +
             "b.brand_name, " +
             "p.cost_price, " +
             "p.selling_price, " +
             "p.unit_type, " +
             "p.status " +
             "FROM products p " +
             "INNER JOIN categories c ON p.category_id = c.category_id " +
             "LEFT JOIN brands b ON p.brand_id = b.brand_id " +
             "WHERE CAST(p.product_id AS CHAR) LIKE ? " +
             "   OR p.product_name LIKE ? " +
             "   OR c.category_name LIKE ? " +
             "   OR b.brand_name LIKE ? " +
             "   OR p.unit_type LIKE ? " +
             "   OR p.status LIKE ? " +
             "ORDER BY p.product_id";

    try (Connection conn = MySQLConnect.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        String like = "%" + keyword + "%";
        pst.setString(1, like);
        pst.setString(2, like);
        pst.setString(3, like);
        pst.setString(4, like);
        pst.setString(5, like);
        pst.setString(6, like);

        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category_name"),
                    rs.getString("brand_name"),
                    String.format("₱ %.2f", rs.getBigDecimal("cost_price")),
                    String.format("₱ %.2f", rs.getBigDecimal("selling_price")),
                    rs.getString("unit_type"),
                    rs.getString("status")
            });
            }
        }
        updateActionButtons();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage());
    }
    }
    
    public void autoSearchProducts() {
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
                    loadProducts();
                } else {
                    searchProducts(keyword);
                }
            }
        });
    }

    public void styleProductTable() {
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
    
    private void updateActionButtons() {
    int row = jTable1.getSelectedRow();

    if (row == -1 || jTable1.getRowCount() == 0) {
        btnActivate.setEnabled(false);
        btnDeactivate.setEnabled(false);
        return;
    }

    Object statusValue = jTable1.getValueAt(row, 7);

    if (statusValue == null) {
        btnActivate.setEnabled(false);
        btnDeactivate.setEnabled(false);
        return;
    }

    String status = statusValue.toString().trim();

    if ("ACTIVE".equalsIgnoreCase(status)) {
        btnActivate.setEnabled(false);
        btnDeactivate.setEnabled(true);
    } else if ("INACTIVE".equalsIgnoreCase(status)) {
        btnActivate.setEnabled(true);
        btnDeactivate.setEnabled(false);
    } else {
        btnActivate.setEnabled(false);
        btnDeactivate.setEnabled(false);
    }
    }
    private void attachTableSelectionHandlers() {
    jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            updateActionButtons();
        }
    });

    jTable1.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
        @Override
        public void valueChanged(javax.swing.event.ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                updateActionButtons();
            }
        }
    });
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
        txtSearch = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnDeactivate = new javax.swing.JButton();
        btnActivate = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        lblBackground = new javax.swing.JLabel();

        setBackground(new java.awt.Color(200, 212, 222));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Product Name", "Category", "Brand", "Cost Price", "Selling Price", "Unit Type", "Status"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, 860, 500));

        txtSearch.addActionListener(this::txtSearchActionPerformed);
        add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 160, 380, 30));

        btnAdd.setText("ADD PRODUCT");
        btnAdd.addActionListener(this::btnAddActionPerformed);
        add(btnAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 260, 130, 40));

        btnEdit.setText("EDIT PRODUCT");
        btnEdit.addActionListener(this::btnEditActionPerformed);
        add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 300, 130, 40));

        btnDeactivate.setText("DEACTIVATE PRODUCT");
        btnDeactivate.addActionListener(this::btnDeactivateActionPerformed);
        add(btnDeactivate, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 400, 180, 40));

        btnActivate.setText("ACTIVATE PRODUCT");
        btnActivate.addActionListener(this::btnActivateActionPerformed);
        add(btnActivate, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 360, 180, 40));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setText("Search:");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 160, 60, 30));

        lblBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/design/Product List.png"))); // NOI18N
        add(lblBackground, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        try (Connection conn = MySQLConnect.getConnection()) {

            JComboBox<String> cmbCategory = new JComboBox<>();
            JComboBox<String> cmbBrand = new JComboBox<>();

            fillCategories(conn, cmbCategory);
            fillBrands(conn, cmbBrand);

            if (cmbCategory.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No categories found. Please add a category first.");
                return;
            }

            if (cmbBrand.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No brands found. Please add a brand first.");
                return;
            }

            JTextField txtName = new JTextField();
            JTextField txtCost = new JTextField();
            JTextField txtSelling = new JTextField();
            JComboBox<String> cmbUnitType = new JComboBox<>(new String[]{"PIECE", "KILO"});

            JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
            panel.add(new JLabel("Product Name:"));
            panel.add(txtName);
            panel.add(new JLabel("Category:"));
            panel.add(cmbCategory);
            panel.add(new JLabel("Brand:"));
            panel.add(cmbBrand);
            panel.add(new JLabel("Cost Price:"));
            panel.add(txtCost);
            panel.add(new JLabel("Selling Price:"));
            panel.add(txtSelling);
            panel.add(new JLabel("Unit Type:"));
            panel.add(cmbUnitType);

            int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Product",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String productName = txtName.getText().trim();
            String costText = txtCost.getText().trim();
            String sellingText = txtSelling.getText().trim();
            String categoryName = cmbCategory.getSelectedItem().toString();
            String brandName = cmbBrand.getSelectedItem().toString();
            String unitType = cmbUnitType.getSelectedItem().toString();
            String unitLabel = unitType.equals("KILO") ? "kg" : "pc";

            if (productName.isEmpty() || costText.isEmpty() || sellingText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            BigDecimal costPrice;
            BigDecimal sellingPrice;

            try {
                costPrice = new BigDecimal(costText);
                sellingPrice = new BigDecimal(sellingText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Cost Price and Selling Price must be valid numbers.");
                return;
            }

            if (costPrice.compareTo(BigDecimal.ZERO) < 0 ||
                sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Values cannot be negative.");
                return;
            }

            if (sellingPrice.compareTo(costPrice) <= 0) {
                JOptionPane.showMessageDialog(this, "Selling Price must be higher than Cost Price.");
                return;
            }

            int categoryId = getCategoryIdByName(conn, categoryName);
            int brandId = getBrandIdByName(conn, brandName);

            if (categoryId == -1) {
                JOptionPane.showMessageDialog(this, "Selected category not found.");
                return;
            }

            if (brandId == -1) {
                JOptionPane.showMessageDialog(this, "Selected brand not found.");
                return;
            }

            String checkSql = "SELECT COUNT(*) FROM products WHERE product_name = ?";
            try (PreparedStatement pst = conn.prepareStatement(checkSql)) {
                pst.setString(1, productName);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "Product name already exists.");
                        return;
                    }
                }
            }

            String insertSql = "INSERT INTO products " +
            "(category_id, brand_id, product_name, cost_price, selling_price, unit_type, unit_label, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";

            try (PreparedStatement pst = conn.prepareStatement(insertSql)) {
                pst.setInt(1, categoryId);
                pst.setInt(2, brandId);
                pst.setString(3, productName);
                pst.setBigDecimal(4, costPrice);
                pst.setBigDecimal(5, sellingPrice);
                pst.setString(6, unitType);
                pst.setString(7, unitLabel);
                pst.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Product added successfully!");
            loadProducts();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Add error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        int row = jTable1.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.");
            return;
        }

        int productId = Integer.parseInt(jTable1.getValueAt(row, 0).toString());

        try (Connection conn = MySQLConnect.getConnection()) {

            String sql = "SELECT p.product_name, p.cost_price, p.selling_price, p.unit_type, " +
            "c.category_name, b.brand_name " +
            "FROM products p " +
            "INNER JOIN categories c ON p.category_id = c.category_id " +
            "LEFT JOIN brands b ON p.brand_id = b.brand_id " +
            "WHERE p.product_id = ?";

            String currentName = "";
            String currentCategory = "";
            String currentBrand = "";
            String currentCost = "";
            String currentSelling = "";
            String currentUnitType = "PIECE";

            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setInt(1, productId);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        currentName = rs.getString("product_name");
                        currentCategory = rs.getString("category_name");
                        currentBrand = rs.getString("brand_name");
                        currentCost = rs.getBigDecimal("cost_price").toString();
                        currentSelling = rs.getBigDecimal("selling_price").toString();
                        currentUnitType = rs.getString("unit_type");
                    }
                }
            }

            JTextField txtName = new JTextField(currentName);
            JTextField txtCost = new JTextField(currentCost);
            JTextField txtSelling = new JTextField(currentSelling);

            JComboBox<String> cmbCategory = new JComboBox<>();
            JComboBox<String> cmbBrand = new JComboBox<>();
            JComboBox<String> cmbUnitType = new JComboBox<>(new String[]{"PIECE", "KILO"});
            cmbUnitType.setSelectedItem(currentUnitType);

            fillCategories(conn, cmbCategory);
            fillBrands(conn, cmbBrand);

            cmbCategory.setSelectedItem(currentCategory);
            cmbBrand.setSelectedItem(currentBrand);

            JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
            panel.add(new JLabel("Product Name:"));
            panel.add(txtName);
            panel.add(new JLabel("Category:"));
            panel.add(cmbCategory);
            panel.add(new JLabel("Brand:"));
            panel.add(cmbBrand);
            panel.add(new JLabel("Cost Price:"));
            panel.add(txtCost);
            panel.add(new JLabel("Selling Price:"));
            panel.add(txtSelling);
            panel.add(new JLabel("Unit Type:"));
            panel.add(cmbUnitType);

            int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Product",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String newName = txtName.getText().trim();
            String newCategory = cmbCategory.getSelectedItem().toString();
            String newBrand = cmbBrand.getSelectedItem().toString();
            String newUnitType = cmbUnitType.getSelectedItem().toString();
            String newUnitLabel = newUnitType.equals("KILO") ? "kg" : "pc";
            String costText = txtCost.getText().trim();
            String sellingText = txtSelling.getText().trim();

            if (newName.isEmpty() || costText.isEmpty() || sellingText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            BigDecimal newCost;
            BigDecimal newSelling;

            try {
                newCost = new BigDecimal(costText);
                newSelling = new BigDecimal(sellingText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Cost Price and Selling Price must be valid numbers.");
                return;
            }

            if (newCost.compareTo(BigDecimal.ZERO) < 0 ||
                newSelling.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Values cannot be negative.");
                return;
            }

            if (newSelling.compareTo(newCost) <= 0) {
                JOptionPane.showMessageDialog(this, "Selling Price must be higher than Cost Price.");
                return;
            }

            int categoryId = getCategoryIdByName(conn, newCategory);
            int brandId = getBrandIdByName(conn, newBrand);

            if (categoryId == -1) {
                JOptionPane.showMessageDialog(this, "Selected category not found.");
                return;
            }

            if (brandId == -1) {
                JOptionPane.showMessageDialog(this, "Selected brand not found.");
                return;
            }

            String duplicateSql = "SELECT COUNT(*) FROM products WHERE product_name = ? AND product_id <> ?";
            try (PreparedStatement pst = conn.prepareStatement(duplicateSql)) {
                pst.setString(1, newName);
                pst.setInt(2, productId);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "Product name already exists.");
                        return;
                    }
                }
            }

            String updateSql = "UPDATE products SET " +
            "product_name = ?, " +
            "category_id = ?, " +
            "brand_id = ?, " +
            "cost_price = ?, " +
            "selling_price = ?, " +
            "unit_type = ?, " +
            "unit_label = ? " +
            "WHERE product_id = ?";

            try (PreparedStatement pst = conn.prepareStatement(updateSql)) {
                pst.setString(1, newName);
                pst.setInt(2, categoryId);
                pst.setInt(3, brandId);
                pst.setBigDecimal(4, newCost);
                pst.setBigDecimal(5, newSelling);
                pst.setString(6, newUnitType);
                pst.setString(7, newUnitLabel);
                pst.setInt(8, productId);
                pst.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Product updated successfully!");
            loadProducts();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Edit error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnDeactivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeactivateActionPerformed
        // TODO add your handling code here:
        int row = jTable1.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to deactivate.");
            return;
        }

        int productId = Integer.parseInt(jTable1.getValueAt(row, 0).toString());
        String productName = jTable1.getValueAt(row, 1).toString();
        String currentStatus = jTable1.getValueAt(row, 7).toString();

        if ("INACTIVE".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This product is already inactive.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Deactivate product: " + productName + "?",
            "Confirm Deactivate",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = MySQLConnect.getConnection()) {
            String sql = "UPDATE products SET status = 'INACTIVE' WHERE product_id = ?";

            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setInt(1, productId);
                pst.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Product deactivated successfully!");
            loadProducts();
            updateActionButtons();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Deactivate error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnDeactivateActionPerformed

    private void btnActivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActivateActionPerformed
        // TODO add your handling code here:
        int row = jTable1.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to activate.");
            return;
        }

        int productId = Integer.parseInt(jTable1.getValueAt(row, 0).toString());
        String productName = jTable1.getValueAt(row, 1).toString();
        String currentStatus = jTable1.getValueAt(row, 7).toString();

        if ("ACTIVE".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This product is already active.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Activate product: " + productName + "?",
            "Confirm Activate",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = MySQLConnect.getConnection()) {
            String sql = "UPDATE products SET status = 'ACTIVE' WHERE product_id = ?";

            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setInt(1, productId);
                pst.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Product activated successfully!");
            loadProducts();
            updateActionButtons();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Activate error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnActivateActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActivate;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDeactivate;
    private javax.swing.JButton btnEdit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblBackground;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
