/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package irms;

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
 * @author miggy
 */
public class products extends javax.swing.JFrame {

    /**
     * Creates new form products
     */
    
    public products() {
    initComponents();
    setLocationRelativeTo(null);
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
        btnSales = new javax.swing.JButton();
        btnCustomers = new javax.swing.JButton();
        btnBrand = new javax.swing.JButton();
        btnCategory = new javax.swing.JButton();
        btnProducts = new javax.swing.JButton();
        btnStocks = new javax.swing.JButton();
        btnReports = new javax.swing.JButton();
        btnHome = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

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

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 210, 860, 500));

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });
        getContentPane().add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 170, 380, 30));

        btnAdd.setText("ADD PRODUCT");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });
        getContentPane().add(btnAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(1150, 270, 130, 40));

        btnEdit.setText("EDIT PRODUCT");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        getContentPane().add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(1150, 310, 130, 40));

        btnDeactivate.setText("DEACTIVATE PRODUCT");
        btnDeactivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeactivateActionPerformed(evt);
            }
        });
        getContentPane().add(btnDeactivate, new org.netbeans.lib.awtextra.AbsoluteConstraints(1150, 410, 180, 40));

        btnActivate.setText("ACTIVATE PRODUCT");
        btnActivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActivateActionPerformed(evt);
            }
        });
        getContentPane().add(btnActivate, new org.netbeans.lib.awtextra.AbsoluteConstraints(1150, 370, 180, 40));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setText("Search:");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 170, 60, 30));

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

        btnProducts.setBackground(new java.awt.Color(126, 139, 74));
        btnProducts.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnProducts.setForeground(new java.awt.Color(255, 255, 255));
        btnProducts.setText("PRODUCTS");
        btnProducts.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnProducts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProductsActionPerformed(evt);
            }
        });
        getContentPane().add(btnProducts, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 520, 140, -1));

        btnStocks.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
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

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/pages/Product list.png"))); // NOI18N
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1370, 780));

        pack();
    }// </editor-fold>//GEN-END:initComponents

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

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

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
            java.util.logging.Logger.getLogger(products.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(products.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(products.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(products.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new products().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActivate;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnBrand;
    private javax.swing.JButton btnCategory;
    private javax.swing.JButton btnCustomers;
    private javax.swing.JButton btnDeactivate;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnProducts;
    private javax.swing.JButton btnReports;
    private javax.swing.JButton btnSales;
    private javax.swing.JButton btnStocks;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
