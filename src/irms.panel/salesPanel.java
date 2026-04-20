/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
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
 * @author USER
 */
public class salesPanel extends javax.swing.JPanel {

    /**
     * Creates new form salesPanel
     */
    public salesPanel() {
        initComponents();
//        applyRoleAccess();
    
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
        java.awt.Window parentWindow = javax.swing.SwingUtilities.getWindowAncestor(this);
        javax.swing.JDialog dialog;

        if (parentWindow instanceof java.awt.Frame) {
            dialog = new javax.swing.JDialog((java.awt.Frame) parentWindow, "Select Kilos", true);
        } else {
            dialog = new javax.swing.JDialog();
            dialog.setModal(true);
            dialog.setTitle("Select Kilos");
        }
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
    
//    private void applyRoleAccess() {
//        if (session.role == null) {
//            return;
//        }
//
//        if (session.role.equalsIgnoreCase("CASHIER")) {
//            btnBrand.setVisible(false); 
//            btnCategory.setVisible(false);  
//            btnProducts.setVisible(false);  
//            btnStocks.setVisible(false);
//            btnCustomers.setVisible(false);
//            btnReports.setVisible(false);   
//
//        } else if (session.role.equalsIgnoreCase("ADMIN")) {
//            btnBrand.setVisible(true);
//            btnCategory.setVisible(true);
//            btnProducts.setVisible(true);
//            btnStocks.setVisible(true);
//            btnCustomers.setVisible(true);
//            btnReports.setVisible(true);
//
//        }
//    }
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

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        add(searchbar, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 160, 280, 30));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setText("Search:");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 60, 30));

        lblCart1.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        lblCart1.setText("Products");
        add(lblCart1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 200, 227, -1));

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

        add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 240, 490, 447));

        btnAddToCart.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAddToCart.setText("Add to Cart");
        btnAddToCart.addActionListener(this::btnAddToCartActionPerformed);
        add(btnAddToCart, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 700, -1, -1));

        lblCart.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        lblCart.setText("Cart");
        add(lblCart, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 200, 227, -1));

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

        add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 240, 490, 400));

        lblSubT.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblSubT.setText("Change:");
        add(lblSubT, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 650, -1, -1));

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTotal.setText(" Total:");
        add(lblTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 670, -1, -1));

        btnClearCart.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnClearCart.setText("Clear Cart");
        btnClearCart.addActionListener(this::btnClearCartActionPerformed);
        add(btnClearCart, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 700, -1, -1));

        btnRemoveItem.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnRemoveItem.setText("Remove Item");
        btnRemoveItem.addActionListener(this::btnRemoveItemActionPerformed);
        add(btnRemoveItem, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 700, -1, -1));

        btnPay.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnPay.setText("Pay");
        btnPay.addActionListener(this::btnPayActionPerformed);
        add(btnPay, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 700, 100, -1));
    }// </editor-fold>//GEN-END:initComponents

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

    private void btnClearCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearCartActionPerformed
        // TODO add your handling code here:
        clearCart();
    }//GEN-LAST:event_btnClearCartActionPerformed

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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddToCart;
    private javax.swing.JButton btnClearCart;
    private javax.swing.JButton btnPay;
    private javax.swing.JButton btnRemoveItem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblCart;
    private javax.swing.JLabel lblCart1;
    private javax.swing.JLabel lblSubT;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTextField searchbar;
    private javax.swing.JTable tblCart;
    private javax.swing.JTable tblProducts;
    // End of variables declaration//GEN-END:variables
}
