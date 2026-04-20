/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package irms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author miggy
 */
public class sales extends javax.swing.JFrame {

    /**
     * Creates new form sales
     */    
    public sales() {
    initComponents();
    setLocationRelativeTo(null);
    applyRoleAccess();
    
    lblSubT.setText("Change: ₱0.00");
    lblTotal.setText("Total: ₱0.00");

    setupTables();
    styleOneTable(tblProducts);
    styleOneTable(tblCart);
    
    loadProducts();
    autoSearchProducts();
    }
    
    private void setupTables() {
        DefaultTableModel productModel = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Product", "Brand", "Stock", "Price", "Unit Type", "Unit"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblProducts.setModel(productModel);

        DefaultTableModel cartModel = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Product", "Qty", "Unit Price", "Total"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCart.setModel(cartModel);

        tblProducts.setRowSelectionAllowed(true);
        tblCart.setRowSelectionAllowed(true);
    }

    private void loadProducts() {
        DefaultTableModel model = (DefaultTableModel) tblProducts.getModel();
        model.setRowCount(0);

        String sql =
            "SELECT p.product_name, " +
            "       COALESCE(b.brand_name, '') AS brand_name, " +
            "       s.quantity, " +
            "       p.selling_price, " +
            "       p.unit_type, " +
            "       p.unit_label " +
            "FROM products p " +
            "LEFT JOIN brands b ON p.brand_id = b.brand_id " +
            "INNER JOIN stocks s ON p.product_id = s.product_id " +
            "WHERE p.status = 'ACTIVE' " +
            "ORDER BY p.product_name";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String unitLabel = rs.getString("unit_label");
                double stockQty = rs.getBigDecimal("quantity").doubleValue();

                model.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getString("brand_name"),
                    formatStockDisplay(stockQty, unitLabel),
                    String.format("₱%.2f", rs.getBigDecimal("selling_price")),
                    rs.getString("unit_type"),
                    unitLabel
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Load products error: " + e.getMessage());
        }
    }

    private void searchProducts(String keyword) {
        DefaultTableModel model = (DefaultTableModel) tblProducts.getModel();
        model.setRowCount(0);

        String sql =
            "SELECT p.product_name, " +
            "       COALESCE(b.brand_name, '') AS brand_name, " +
            "       s.quantity, " +
            "       p.selling_price, " +
            "       p.unit_type, " +
            "       p.unit_label " +
            "FROM products p " +
            "LEFT JOIN brands b ON p.brand_id = b.brand_id " +
            "INNER JOIN stocks s ON p.product_id = s.product_id " +
            "WHERE p.status = 'ACTIVE' " +
            "AND (p.product_name LIKE ? OR COALESCE(b.brand_name, '') LIKE ?) " +
            "ORDER BY p.product_name";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            pst.setString(1, like);
            pst.setString(2, like);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String unitLabel = rs.getString("unit_label");
                    double stockQty = rs.getBigDecimal("quantity").doubleValue();

                    model.addRow(new Object[]{
                        rs.getString("product_name"),
                        rs.getString("brand_name"),
                        formatStockDisplay(stockQty, unitLabel),
                        String.format("₱%.2f", rs.getBigDecimal("selling_price")),
                        rs.getString("unit_type"),
                        unitLabel
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage());
        }
    }

    private void autoSearchProducts() {
        searchbar.getDocument().addDocumentListener(new DocumentListener() {
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
                String text = searchbar.getText().trim();
                if (text.isEmpty()) {
                    loadProducts();
                } else {
                    searchProducts(text);
                }
            }
        });
    }

    private double getCartQuantity(String productName) {
        DefaultTableModel cartModel = (DefaultTableModel) tblCart.getModel();

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if (productName.equalsIgnoreCase(cartModel.getValueAt(i, 0).toString())) {
                return parseCartQty(cartModel.getValueAt(i, 1).toString());
            }
        }
        return 0.0;
    }
    
    private String formatStockDisplay(double qty, String unitLabel) {
        if (unitLabel != null && unitLabel.equalsIgnoreCase("kg")) {
            return String.format("%.2f", qty);
        }
        return String.valueOf((int) qty);
    }

    private String formatCartQtyDisplay(double qty, String unitLabel) {
        if (unitLabel != null && unitLabel.equalsIgnoreCase("kg")) {
            if (Math.abs(qty - 0.25) < 0.001) return "1/4";
            if (Math.abs(qty - 0.50) < 0.001) return "1/2";
            if (Math.abs(qty - 0.75) < 0.001) return "3/4";
            if (Math.abs(qty - 1.00) < 0.001) return "1";
            return String.format("%.2f", qty);
        }
        return String.valueOf((int) qty);
    }
    
    private double parseCartQty(String qtyText) {
        qtyText = qtyText.trim();

        if (qtyText.equals("1/4")) return 0.25;
        if (qtyText.equals("1/2")) return 0.50;
        if (qtyText.equals("3/4")) return 0.75;
        if (qtyText.equals("1")) return 1.00;

        return Double.parseDouble(qtyText);
    }
    
    private Double showKiloPickerDialog() {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Select Kilos", true);
        dialog.setUndecorated(true);
        dialog.setSize(300, 220);
        dialog.setLayout(new java.awt.BorderLayout());
        dialog.setLocationRelativeTo(this);

        javax.swing.JLabel lblTitle = new javax.swing.JLabel("Select kilos to add", javax.swing.SwingConstants.CENTER);
        lblTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        lblTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 10, 10, 10));

        javax.swing.JPanel buttonPanel = new javax.swing.JPanel(new java.awt.GridLayout(3, 2, 10, 10));
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 15, 15));

        final Double[] selectedQty = {null};

        javax.swing.JButton btnQuarter = new javax.swing.JButton("1/4 kg");
        javax.swing.JButton btnHalf = new javax.swing.JButton("1/2 kg");
        javax.swing.JButton btnThreeQuarter = new javax.swing.JButton("3/4 kg");
        javax.swing.JButton btnOne = new javax.swing.JButton("1 kg");
        javax.swing.JButton btnCustom = new javax.swing.JButton("Custom");
        javax.swing.JButton btnCancel = new javax.swing.JButton("Cancel");

        btnQuarter.addActionListener(e -> {
            selectedQty[0] = 0.25;
            dialog.dispose();
        });

        btnHalf.addActionListener(e -> {
            selectedQty[0] = 0.50;
            dialog.dispose();
        });

        btnThreeQuarter.addActionListener(e -> {
            selectedQty[0] = 0.75;
            dialog.dispose();
        });

        btnOne.addActionListener(e -> {
            selectedQty[0] = 1.00;
            dialog.dispose();
        });

        btnCustom.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(dialog, "Enter kilos:", "0.50");

            if (input == null) {
                return;
            }

            input = input.trim();

            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Kilos are required.");
                return;
            }

            try {
                double value = Double.parseDouble(input);

                if (value <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Kilos must be greater than 0.");
                    return;
                }

                selectedQty[0] = value;
                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format.");
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnQuarter);
        buttonPanel.add(btnHalf);
        buttonPanel.add(btnThreeQuarter);
        buttonPanel.add(btnOne);
        buttonPanel.add(btnCustom);
        buttonPanel.add(btnCancel);

        dialog.add(lblTitle, java.awt.BorderLayout.NORTH);
        dialog.add(buttonPanel, java.awt.BorderLayout.CENTER);

        dialog.setVisible(true);

        return selectedQty[0];
    }

    private void addOrUpdateCart(String productName, double qtyToAdd, double price, double stock, String unitLabel) {
    DefaultTableModel cartModel = (DefaultTableModel) tblCart.getModel();

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String existingName = cartModel.getValueAt(i, 0).toString();

            if (existingName.equalsIgnoreCase(productName)) {
                double oldQty = parseCartQty(cartModel.getValueAt(i, 1).toString());
                double newQty = oldQty + qtyToAdd;

                if (newQty > stock) {
                    JOptionPane.showMessageDialog(this, "Not enough stock.");
                    return;
                }

                double lineTotal = newQty * price;

                cartModel.setValueAt(formatCartQtyDisplay(newQty, unitLabel), i, 1);
                cartModel.setValueAt(String.format("₱%.2f", price), i, 2);
                cartModel.setValueAt(String.format("₱%.2f", lineTotal), i, 3);
                updateTotals();
                return;
            }
    }

    if (qtyToAdd > stock) {
        JOptionPane.showMessageDialog(this, "Not enough stock.");
        return;
    }

    double total = qtyToAdd * price;
    cartModel.addRow(new Object[]{
        productName,
        formatCartQtyDisplay(qtyToAdd, unitLabel),
        String.format("₱%.2f", price),
        String.format("₱%.2f", total)
    });

    updateTotals();
}

    private void updateTotals() {
        DefaultTableModel cartModel = (DefaultTableModel) tblCart.getModel();
        double total = 0;

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String totalText = cartModel.getValueAt(i, 3).toString().replace("₱", "").trim();
            total += Double.parseDouble(totalText);
        }

        lblTotal.setText(String.format("Total: ₱%.2f", total));
        lblSubT.setText("Change: ₱0.00");
    }

    private void clearCart() {
        DefaultTableModel cartModel = (DefaultTableModel) tblCart.getModel();
        cartModel.setRowCount(0);
        updateTotals();
    }

    private int getProductIdByName(Connection conn, String productName) throws SQLException {
        String sql = "SELECT product_id FROM products WHERE product_name = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, productName);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("product_id");
                }
            }
        }
        return -1;
    }
    
    private BigDecimal getCurrentStockByProductId(Connection conn, int productId) throws SQLException {
        String sql = "SELECT quantity FROM stocks WHERE product_id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, productId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    BigDecimal qty = rs.getBigDecimal("quantity");
                    return qty == null ? BigDecimal.ZERO : qty;
                }
            }
        }

        return BigDecimal.ZERO;
    }

    private String getUnitLabelByProductId(Connection conn, int productId) throws SQLException {
        String sql = "SELECT unit_label FROM products WHERE product_id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, productId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String unitLabel = rs.getString("unit_label");
                    return unitLabel == null || unitLabel.trim().isEmpty() ? "pc" : unitLabel;
                }
            }
        }

        return "pc";
    }

    private void logSoldStockMovement(
            Connection conn,
            int productId,
            BigDecimal soldQty,
            String unitLabel,
            BigDecimal previousStock,
            BigDecimal newStock,
            String remarks
    ) throws SQLException {
        String sql = "INSERT INTO stock_movements " +
                     "(product_id, movement_type, quantity, unit_label, previous_stock, new_stock, remarks, moved_by, movement_date, movement_time) " +
                     "VALUES (?, 'OUT', ?, ?, ?, ?, ?, ?, CURDATE(), CURTIME())";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, productId);
            pst.setBigDecimal(2, soldQty);
            pst.setString(3, unitLabel);
            pst.setBigDecimal(4, previousStock);
            pst.setBigDecimal(5, newStock);
            pst.setString(6, remarks);
            pst.setInt(7, session.userId);
            pst.executeUpdate();
        }
    }

    private String buildReceipt(int saleId) {
        DefaultTableModel cartModel = (DefaultTableModel) tblCart.getModel();

        StringBuilder receipt = new StringBuilder();
        receipt.append("=========== RECEIPT ===========\n");
        receipt.append("Sale ID: ").append(saleId).append("\n");
        receipt.append("STOCKMATE\n\n");

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String productName = cartModel.getValueAt(i, 0).toString();
            String qtyText = cartModel.getValueAt(i, 1).toString();

            receipt.append(productName).append("\n");
            receipt.append("Qty: ").append(qtyText);

            if (qtyText.equals("1/4") || qtyText.equals("1/2") || qtyText.equals("3/4")) {
                receipt.append(" kg");
            }

            receipt.append("   Price: ").append(cartModel.getValueAt(i, 2))
                   .append("   Total: ").append(cartModel.getValueAt(i, 3)).append("\n\n");
        }

        receipt.append(lblTotal.getText()).append("\n");
        receipt.append(lblSubT.getText()).append("\n");
        receipt.append("================================");

        return receipt.toString();
    }
    
    private void applyRoleAccess() {
        if (session.role == null) {
            return;
        }

        if (session.role.equalsIgnoreCase("CASHIER")) {
            btnBrand.setVisible(false); 
            btnCategory.setVisible(false);  
            btnProducts.setVisible(false);  
            btnStocks.setVisible(false);
            btnCustomers.setVisible(false);
            btnReports.setVisible(false);   

        } else if (session.role.equalsIgnoreCase("ADMIN")) {
            btnBrand.setVisible(true);
            btnCategory.setVisible(true);
            btnProducts.setVisible(true);
            btnStocks.setVisible(true);
            btnCustomers.setVisible(true);
            btnReports.setVisible(true);

        }
    }
    public void styleOneTable(javax.swing.JTable table) {
        table.setRowHeight(28);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        table.setGridColor(new java.awt.Color(180, 180, 180));
        table.setSelectionBackground(new java.awt.Color(180, 200, 160));
        table.setSelectionForeground(java.awt.Color.BLACK);
        table.setRowSelectionAllowed(true);
        table.setFocusable(false);

        javax.swing.table.JTableHeader header = table.getTableHeader();
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

        searchbar = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        lblCart1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblProducts = new javax.swing.JTable();
        btnAddToCart = new javax.swing.JButton();
        lblCart = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblCart = new javax.swing.JTable();
        lblSubT = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        btnClearCart = new javax.swing.JButton();
        btnRemoveItem = new javax.swing.JButton();
        btnPay = new javax.swing.JButton();
        btnSales = new javax.swing.JButton();
        btnCustomers = new javax.swing.JButton();
        btnBrand = new javax.swing.JButton();
        btnCategory = new javax.swing.JButton();
        btnProducts = new javax.swing.JButton();
        btnStocks = new javax.swing.JButton();
        btnReports = new javax.swing.JButton();
        btnHome = new javax.swing.JButton();
        lblBackground = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 0));
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(searchbar, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 160, 280, 30));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setText("Search:");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 160, 60, 30));

        lblCart1.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        lblCart1.setText("Products");
        getContentPane().add(lblCart1, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 200, 227, -1));

        tblProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Product", "Brand", "Stock", "Price"
            }
        ));
        jScrollPane2.setViewportView(tblProducts);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 240, 490, 447));

        btnAddToCart.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAddToCart.setText("Add to Cart");
        btnAddToCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddToCartActionPerformed(evt);
            }
        });
        getContentPane().add(btnAddToCart, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 700, -1, -1));

        lblCart.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        lblCart.setText("Cart");
        getContentPane().add(lblCart, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 200, 227, -1));

        tblCart.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Product", "Qty", "Unit Price", "Total"
            }
        ));
        jScrollPane3.setViewportView(tblCart);

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 240, 490, 400));

        lblSubT.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblSubT.setText("Change:");
        getContentPane().add(lblSubT, new org.netbeans.lib.awtextra.AbsoluteConstraints(1090, 650, -1, -1));

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTotal.setText(" Total:");
        getContentPane().add(lblTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 670, -1, -1));

        btnClearCart.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnClearCart.setText("Clear Cart");
        btnClearCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearCartActionPerformed(evt);
            }
        });
        getContentPane().add(btnClearCart, new org.netbeans.lib.awtextra.AbsoluteConstraints(1080, 700, -1, -1));

        btnRemoveItem.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnRemoveItem.setText("Remove Item");
        btnRemoveItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveItemActionPerformed(evt);
            }
        });
        getContentPane().add(btnRemoveItem, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 700, -1, -1));

        btnPay.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnPay.setText("Pay");
        btnPay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPayActionPerformed(evt);
            }
        });
        getContentPane().add(btnPay, new org.netbeans.lib.awtextra.AbsoluteConstraints(1180, 700, 100, -1));

        btnSales.setBackground(new java.awt.Color(126, 139, 74));
        btnSales.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnSales.setForeground(new java.awt.Color(255, 255, 255));
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

        btnProducts.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
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

        lblBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/pages/Sales List.png"))); // NOI18N
        getContentPane().add(lblBackground, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnPayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPayActionPerformed
        // TODO add your handling code here:
            DefaultTableModel cartModel = (DefaultTableModel) tblCart.getModel();

        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        Connection conn = null;

        try {
            conn = MySQLConnect.getConnection();
            conn.setAutoCommit(false);

            double totalValue = Double.parseDouble(lblTotal.getText().replace("Total: ₱", "").trim());

        String cashInput = JOptionPane.showInputDialog(this, "Enter cash amount:");
        if (cashInput == null) {
            return;
        }

        cashInput = cashInput.trim();

        if (cashInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cash amount is required.");
            return;
        }

        double cashAmount;

        try {
            cashAmount = Double.parseDouble(cashInput);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid cash amount.");
            return;
        }

        if (cashAmount < totalValue) {
            JOptionPane.showMessageDialog(this, "Insufficient cash.");
            return;
        }

        double changeValue = cashAmount - totalValue;
        lblSubT.setText(String.format("Change: ₱%.2f", changeValue));

        String saleSql = "INSERT INTO sales (subtotal, total, notes) VALUES (?, ?, ?)";
        int saleId = 0;

        try (PreparedStatement pstSale = conn.prepareStatement(saleSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstSale.setBigDecimal(1, BigDecimal.valueOf(totalValue));
            pstSale.setBigDecimal(2, BigDecimal.valueOf(totalValue));
            pstSale.setString(3, "Cash: ₱" + String.format("%.2f", cashAmount) + ", Change: ₱" + String.format("%.2f", changeValue));
            pstSale.executeUpdate();

                try (ResultSet rs = pstSale.getGeneratedKeys()) {
                    if (rs.next()) {
                        saleId = rs.getInt(1);
                    }
                }
            }

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                String productName = cartModel.getValueAt(i, 0).toString();
                double qty = parseCartQty(cartModel.getValueAt(i, 1).toString());
                double price = Double.parseDouble(cartModel.getValueAt(i, 2).toString().replace("₱", "").trim());
                double lineTotal = Double.parseDouble(cartModel.getValueAt(i, 3).toString().replace("₱", "").trim());

                int productId = getProductIdByName(conn, productName);

                String saleItemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, price, line_total) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstItem = conn.prepareStatement(saleItemSql)) {
                    pstItem.setInt(1, saleId);
                    pstItem.setInt(2, productId);
                    pstItem.setBigDecimal(3, BigDecimal.valueOf(qty));
                    pstItem.setBigDecimal(4, BigDecimal.valueOf(price));
                    pstItem.setBigDecimal(5, BigDecimal.valueOf(lineTotal));
                    pstItem.executeUpdate();
                }

                BigDecimal soldQty = BigDecimal.valueOf(qty);
                BigDecimal previousStock = getCurrentStockByProductId(conn, productId);
                BigDecimal newStock = previousStock.subtract(soldQty);
                String unitLabel = getUnitLabelByProductId(conn, productId);

                String updateStockSql = "UPDATE stocks SET quantity = quantity - ?, stock_date = CURDATE(), stock_time = CURTIME() WHERE product_id = ?";
                try (PreparedStatement pstStock = conn.prepareStatement(updateStockSql)) {
                    pstStock.setBigDecimal(1, soldQty);
                    pstStock.setInt(2, productId);
                    pstStock.executeUpdate();
                }

                logSoldStockMovement(
                        conn,
                        productId,
                        soldQty,
                        unitLabel,
                        previousStock,
                        newStock,
                        "Sold - Sale ID " + saleId
                );
            }

            conn.commit();

            String receipt = buildReceipt(saleId);
            JTextArea area = new JTextArea(receipt, 20, 30);
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new javax.swing.JScrollPane(area), "Receipt", JOptionPane.INFORMATION_MESSAGE);

            clearCart();
            loadProducts();

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Rollback error: " + ex.getMessage());
            }

            JOptionPane.showMessageDialog(this, "Pay error: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Close error: " + ex.getMessage());
            }
        }
    }//GEN-LAST:event_btnPayActionPerformed

    private void btnRemoveItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveItemActionPerformed
        // TODO add your handling code here:
        int row = tblCart.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item in the cart.");
            return;
        }

        DefaultTableModel cartModel = (DefaultTableModel) tblCart.getModel();
        cartModel.removeRow(row);
        updateTotals();
    }//GEN-LAST:event_btnRemoveItemActionPerformed

    private void btnClearCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearCartActionPerformed
        // TODO add your handling code here:
        clearCart();
    }//GEN-LAST:event_btnClearCartActionPerformed

    private void btnAddToCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddToCartActionPerformed
        // TODO add your handling code here:
            int row = tblProducts.getSelectedRow();

    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Please select a product first.");
        return;
    }

    String productName = tblProducts.getValueAt(row, 0).toString();
    double stock = Double.parseDouble(tblProducts.getValueAt(row, 2).toString());

    String priceText = tblProducts.getValueAt(row, 3).toString().replace("₱", "").trim();
    double price = Double.parseDouble(priceText);

    String unitType = tblProducts.getValueAt(row, 4).toString();
    String unitLabel = tblProducts.getValueAt(row, 5).toString();

    if (stock <= 0) {
        JOptionPane.showMessageDialog(this, "This product is out of stock.");
        return;
    }

    double qtyToAdd;

if ("KILO".equalsIgnoreCase(unitType)) {
    Double selectedKilos = showKiloPickerDialog();

    if (selectedKilos == null) {
        return;
    }

    qtyToAdd = selectedKilos;

} else {
    String qtyInput = JOptionPane.showInputDialog(this, "Enter quantity to add:", "1");

    if (qtyInput == null) {
        return;
    }

    qtyInput = qtyInput.trim();

    if (qtyInput.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Quantity is required.");
        return;
    }

    try {
        qtyToAdd = Double.parseDouble(qtyInput);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Quantity must be a valid number.");
        return;
    }

    if (qtyToAdd <= 0) {
        JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
        return;
    }
}

    if ("PIECE".equalsIgnoreCase(unitType) && qtyToAdd != Math.floor(qtyToAdd)) {
        JOptionPane.showMessageDialog(this, "Piece items must use whole numbers only.");
        return;
    }

    double currentCartQty = getCartQuantity(productName);

    if ((currentCartQty + qtyToAdd) > stock) {
        JOptionPane.showMessageDialog(this, "Not enough stock.");
        return;
    }

    addOrUpdateCart(productName, qtyToAdd, price, stock, unitLabel);
    }//GEN-LAST:event_btnAddToCartActionPerformed

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
            java.util.logging.Logger.getLogger(sales.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(sales.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(sales.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(sales.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new sales().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddToCart;
    private javax.swing.JButton btnBrand;
    private javax.swing.JButton btnCategory;
    private javax.swing.JButton btnClearCart;
    private javax.swing.JButton btnCustomers;
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnPay;
    private javax.swing.JButton btnProducts;
    private javax.swing.JButton btnRemoveItem;
    private javax.swing.JButton btnReports;
    private javax.swing.JButton btnSales;
    private javax.swing.JButton btnStocks;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblBackground;
    private javax.swing.JLabel lblCart;
    private javax.swing.JLabel lblCart1;
    private javax.swing.JLabel lblSubT;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTextField searchbar;
    private javax.swing.JTable tblCart;
    private javax.swing.JTable tblProducts;
    // End of variables declaration//GEN-END:variables
}
