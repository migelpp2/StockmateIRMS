/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package irms.ui.panel;

import irms.db.MySQLConnect;
import irms.auth.session;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.table.DefaultTableCellRenderer;
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
        lblBackground.setIcon(new javax.swing.ImageIcon(
            getClass().getResource("/irms/resources/background/Sales List.png")
        ));
    
        lblSubT.setText("Change:");
        lblTotal.setText("Total:");
        lblChangeValue.setText("₱0.00");
        lblTotalValue.setText("₱0.00");

        setupTables();
        styleOneTable(tblProducts);
        styleOneTable(tblCart);
        centerTableText(tblCart);

        loadProducts();
        autoSearchProducts();
        setupProductTableClickAdd();
        setupCartQuantityButtons();
        setupCartQtyEditListener();
    }
    private static final String STORE_NAME = "STOCKMATE";
    private static final String STORE_ADDRESS = "67 Jian Chavez St. Ormoc City Leyte";
    private static final String STORE_TIN = "TIN: 911-676-100-690";
    private static final String STORE_CONTACT = "Contact: 0912-345-6789";
    
    private static final BigDecimal VAT_DIVISOR = new BigDecimal("1.12");

    private BigDecimal getCartGrandTotal() {
        DefaultTableModel cartModel = (DefaultTableModel) tblCart.getModel();
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String totalText = cartModel.getValueAt(i, 5).toString().replace("₱", "").trim();
            total = total.add(new BigDecimal(totalText));
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeVatableSales(BigDecimal grossTotal) {
        return grossTotal.divide(VAT_DIVISOR, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeVatAmount(BigDecimal grossTotal) {
        return grossTotal.subtract(computeVatableSales(grossTotal)).setScale(2, RoundingMode.HALF_UP);
    }
    
    private void setupTables() {
        DefaultTableModel productModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Product", "Brand", "Stock", "Price", "Unit Type", "Unit", ""}
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblProducts.setModel(productModel);

            DefaultTableCellRenderer addRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                java.awt.Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                c.setForeground(new java.awt.Color(72, 92, 13));
                c.setFont(c.getFont().deriveFont(java.awt.Font.BOLD));

                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

                return c;
            }
        };

        tblProducts.getColumnModel().getColumn(6).setCellRenderer(addRenderer);

            tblProducts.getColumnModel().getColumn(6).setPreferredWidth(45);
            tblProducts.getColumnModel().getColumn(6).setMaxWidth(50);

            DefaultTableModel cartModel = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Product", "-", "Qty", "+", "Unit Price", "Total"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Qty only
            }
        };
        tblCart.setModel(cartModel);
        tblCart.getColumnModel().getColumn(1).setPreferredWidth(30);
        tblCart.getColumnModel().getColumn(1).setMaxWidth(30);

        tblCart.getColumnModel().getColumn(3).setPreferredWidth(30);
        tblCart.getColumnModel().getColumn(3).setMaxWidth(30);
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
            "AND s.quantity > 0 " +
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
                    unitLabel,
                    "Add"
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
            "AND s.quantity > 0 " +
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
                        unitLabel,
                        "Add"
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
                return parseCartQty(cartModel.getValueAt(i, 2).toString());
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
                double oldQty = parseCartQty(cartModel.getValueAt(i, 2).toString());
                double newQty = oldQty + qtyToAdd;

                if (newQty > stock) {
                    JOptionPane.showMessageDialog(this, "Not enough stock.");
                    return;
                }

                double lineTotal = newQty * price;

                cartModel.setValueAt(formatCartQtyDisplay(newQty, unitLabel), i, 2);
                cartModel.setValueAt(String.format("₱%.2f", price), i, 4);
                cartModel.setValueAt(String.format("₱%.2f", lineTotal), i, 5);
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
            "-",
            formatCartQtyDisplay(qtyToAdd, unitLabel),
            "+",
            String.format("₱%.2f", price),
            String.format("₱%.2f", total)
        });

        updateTotals();
    }

    private void updateTotals() {
        BigDecimal total = getCartGrandTotal();
        lblTotalValue.setText(String.format("₱%.2f", total));
        lblChangeValue.setText("₱0.00");
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

   private String buildReceipt(
        int saleId,
        BigDecimal vatableSales,
        BigDecimal vatAmount,
        BigDecimal grossTotal,
        BigDecimal cashReceived,
        BigDecimal changeAmount
    ) {
        DefaultTableModel cartModel = (DefaultTableModel) tblCart.getModel();

        final int WIDTH = 40;
        String line = repeatChar('=', WIDTH);
        String dash = repeatChar('-', WIDTH);

        java.time.format.DateTimeFormatter formatter =
        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        String saleDateTime = java.time.LocalDateTime.now().format(formatter);

        StringBuilder receipt = new StringBuilder();

        receipt.append(line).append("\n");
        receipt.append(centerText(STORE_NAME, WIDTH)).append("\n");
        receipt.append(centerText(STORE_ADDRESS, WIDTH)).append("\n");
        receipt.append(centerText(STORE_CONTACT, WIDTH)).append("\n");
        receipt.append(centerText(STORE_TIN, WIDTH)).append("\n");
        receipt.append(line).append("\n");

        receipt.append("Sale ID      : ").append(saleId).append("\n");
        receipt.append("Date/Time  : ").append(saleDateTime).append("\n");
        receipt.append("Cashier      : ").append(session.username == null ? "-" : session.username).append("\n");
        receipt.append(dash).append("\n");

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String productName = cartModel.getValueAt(i, 0).toString();
            String qtyText = cartModel.getValueAt(i, 2).toString();
            String priceText = cartModel.getValueAt(i, 4).toString().replace("₱", "").trim();
            String totalText = cartModel.getValueAt(i, 5).toString().replace("₱", "").trim();
            
            if (qtyText.equals("1/4") || qtyText.equals("1/2") || qtyText.equals("3/4")) {
                qtyText = qtyText + "kg";
            }

            receipt.append(productName).append("\n");
            receipt.append("  ")
                   .append(padRight("Qty: " + qtyText, 12))
                   .append(padRight("Price: " + priceText, 14))
                   .append("Tot: ").append(totalText)
                   .append("\n");
        }

        receipt.append(dash).append("\n");
        receipt.append(padRight("VATable Sales", 24)).append(padLeft("₱" + vatableSales, 16)).append("\n");
        receipt.append(padRight("VAT Amount (12%)", 24)).append(padLeft("₱" + vatAmount, 16)).append("\n");
        receipt.append(padRight("Total Due", 24)).append(padLeft("₱" + grossTotal, 16)).append("\n");
        receipt.append(padRight("Cash Received", 24)).append(padLeft("₱" + cashReceived, 16)).append("\n");
        receipt.append(padRight("Change", 24)).append(padLeft("₱" + changeAmount, 16)).append("\n");
        receipt.append(dash).append("\n");
        receipt.append(centerText("Thank you for your purchase!", WIDTH)).append("\n");
        receipt.append(centerText("Please keep this receipt.", WIDTH)).append("\n");
        receipt.append(line).append("\n");

        return receipt.toString();
    }
    
    private String repeatChar(char ch, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    private String padRight(String text, int width) {
        if (text == null) text = "";
        if (text.length() > width) {
            return text.substring(0, width);
        }
        return String.format("%-" + width + "s", text);
    }

    private String padLeft(String text, int width) {
        if (text == null) text = "";
        if (text.length() > width) {
            return text.substring(0, width);
        }
        return String.format("%" + width + "s", text);
    }

    private String centerText(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) {
            return text.substring(0, width);
        }

        int leftPadding = (width - text.length()) / 2;
        int rightPadding = width - text.length() - leftPadding;

        return repeatChar(' ', leftPadding) + text + repeatChar(' ', rightPadding);
    }
    
    private void setupProductTableClickAdd() {
        tblProducts.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tblProducts.rowAtPoint(e.getPoint());
                int col = tblProducts.columnAtPoint(e.getPoint());

                if (row == -1 || col != 6) {
                    return;
                }

                String productName = tblProducts.getValueAt(row, 0).toString();
                double stock = Double.parseDouble(tblProducts.getValueAt(row, 2).toString());
                double price = Double.parseDouble(tblProducts.getValueAt(row, 3).toString().replace("₱", "").trim());
                String unitLabel = tblProducts.getValueAt(row, 5).toString();

                double qtyToAdd;

                if (unitLabel.equalsIgnoreCase("kg")) {
                    Double selectedQty = showKiloPickerDialog();
                    if (selectedQty == null) return;
                    qtyToAdd = selectedQty;
                } else {
                    qtyToAdd = 1;
                }

                addOrUpdateCart(productName, qtyToAdd, price, stock, unitLabel);
            }
        });
    }
    
    private void centerTableText(javax.swing.JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
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
    
    private BigDecimal showCashDenominationDialog(BigDecimal totalDue) {
        java.awt.Window parentWindow = javax.swing.SwingUtilities.getWindowAncestor(this);
        javax.swing.JDialog dialog;

        if (parentWindow instanceof java.awt.Frame) {
            dialog = new javax.swing.JDialog((java.awt.Frame) parentWindow, "Cash Payment", true);
        } else {
            dialog = new javax.swing.JDialog();
            dialog.setModal(true);
            dialog.setTitle("Cash Payment");
        }

        dialog.setSize(520, 520);
        dialog.setLayout(new java.awt.BorderLayout(10, 10));
        dialog.setLocationRelativeTo(this);

        final BigDecimal[] cashReceived = {BigDecimal.ZERO};

        javax.swing.JLabel lblTitle = new javax.swing.JLabel("Select Customer Cash", javax.swing.SwingConstants.CENTER);
        lblTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        lblTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 5, 10));

        javax.swing.JLabel lblTotalDue = new javax.swing.JLabel("Total Due: ₱" + totalDue.setScale(2, RoundingMode.HALF_UP), javax.swing.SwingConstants.CENTER);
        lblTotalDue.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));

        javax.swing.JLabel lblCashValue = new javax.swing.JLabel("Cash Received: ₱0.00", javax.swing.SwingConstants.CENTER);
        lblCashValue.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));

        javax.swing.JLabel lblChangeValue = new javax.swing.JLabel("Change: ₱0.00", javax.swing.SwingConstants.CENTER);
        lblChangeValue.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));

        java.awt.event.ActionListener updateLabels = e -> {
            lblCashValue.setText("Cash Received: ₱" + cashReceived[0].setScale(2, RoundingMode.HALF_UP));
            BigDecimal change = cashReceived[0].subtract(totalDue);
            if (change.compareTo(BigDecimal.ZERO) < 0) {
                lblChangeValue.setText("Remaining: ₱" + change.abs().setScale(2, RoundingMode.HALF_UP));
            } else {
                lblChangeValue.setText("Change: ₱" + change.setScale(2, RoundingMode.HALF_UP));
            }
        };

        javax.swing.JPanel infoPanel = new javax.swing.JPanel(new java.awt.GridLayout(3, 1, 5, 5));
        infoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));
        infoPanel.add(lblTotalDue);
        infoPanel.add(lblCashValue);
        infoPanel.add(lblChangeValue);

        javax.swing.JPanel denominationPanel = new javax.swing.JPanel(new java.awt.GridLayout(0, 4, 8, 8));
        denominationPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] denominations = {
            "1", "5",
            "10", "20", "50", "100",
            "200", "500", "1000"
        };

        for (String value : denominations) {
            javax.swing.JButton btn = new javax.swing.JButton("₱" + value);
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            btn.addActionListener(e -> {
                cashReceived[0] = cashReceived[0].add(new BigDecimal(value));
                updateLabels.actionPerformed(null);
            });
            denominationPanel.add(btn);
        }

        javax.swing.JPanel bottomPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 10));
        javax.swing.JButton btnClear = new javax.swing.JButton("Clear");
        javax.swing.JButton btnExact = new javax.swing.JButton("Exact");
        javax.swing.JButton btnCustom = new javax.swing.JButton("Custom");
        javax.swing.JButton btnConfirm = new javax.swing.JButton("Confirm");
        javax.swing.JButton btnCancel = new javax.swing.JButton("Cancel");

        btnClear.addActionListener(e -> {
            cashReceived[0] = BigDecimal.ZERO;
            updateLabels.actionPerformed(null);
        });

        btnExact.addActionListener(e -> {
            cashReceived[0] = totalDue;
            updateLabels.actionPerformed(null);
        });

        btnCustom.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(dialog, "Enter custom amount:", cashReceived[0].setScale(2, RoundingMode.HALF_UP).toPlainString());
            if (input == null) {
                return;
            }

            input = input.trim();

            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Amount is required.");
                return;
            }

            try {
                cashReceived[0] = new BigDecimal(input).setScale(2, RoundingMode.HALF_UP);
                if (cashReceived[0].compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(dialog, "Amount cannot be negative.");
                    cashReceived[0] = BigDecimal.ZERO;
                }
                updateLabels.actionPerformed(null);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid amount.");
            }
        });

        final BigDecimal[] confirmedValue = {null};

        btnConfirm.addActionListener(e -> {
            if (cashReceived[0].compareTo(totalDue) < 0) {
                JOptionPane.showMessageDialog(dialog, "Insufficient cash.");
                return;
            }
            confirmedValue[0] = cashReceived[0].setScale(2, RoundingMode.HALF_UP);
            dialog.dispose();
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        bottomPanel.add(btnClear);
        bottomPanel.add(btnExact);
        bottomPanel.add(btnCustom);
        bottomPanel.add(btnConfirm);
        bottomPanel.add(btnCancel);

        dialog.add(lblTitle, java.awt.BorderLayout.NORTH);
        dialog.add(infoPanel, java.awt.BorderLayout.BEFORE_FIRST_LINE);
        dialog.add(denominationPanel, java.awt.BorderLayout.CENTER);
        dialog.add(bottomPanel, java.awt.BorderLayout.SOUTH);

        updateLabels.actionPerformed(null);
        dialog.setVisible(true);

        return confirmedValue[0];
    }
    
    private String getLoggedInCashierFirstName() {
        if (session.fullname != null && !session.fullname.trim().isEmpty()) {
            String[] parts = session.fullname.trim().split("\\s+");
            return parts[0];
        }

        if (session.username != null && !session.username.trim().isEmpty()) {
            return session.username.trim();
        }

        return "-";
    }
    
    private BigDecimal computeAverageUsageFromSales(Connection conn, int productId, String unitLabel) throws SQLException {
        String sql = "SELECT COALESCE(SUM(si.quantity), 0) / 30 AS avg_usage " +
                     "FROM sale_items si " +
                     "INNER JOIN sales s ON si.sale_id = s.sale_id " +
                     "WHERE si.product_id = ? " +
                     "AND DATE(s.sale_date) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, productId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    BigDecimal avgUsage = rs.getBigDecimal("avg_usage");
                    if (avgUsage == null) {
                        avgUsage = BigDecimal.ZERO;
                    }

                    if (unitLabel != null && unitLabel.equalsIgnoreCase("kg")) {
                        return avgUsage.setScale(2, java.math.RoundingMode.HALF_UP);
                    } else {
                        return new BigDecimal(avgUsage.setScale(0, java.math.RoundingMode.CEILING).toPlainString());
                    }
                }
            }
        }

        return BigDecimal.ZERO;
    }
    
    private void updateProductAverageUsageAndReorderLevel(Connection conn, int productId) throws SQLException {
        String unitSql = "SELECT unit_label, safety_stock FROM products WHERE product_id = ?";

        String unitLabel = "pc";
        BigDecimal safetyStock = BigDecimal.ZERO;

        try (PreparedStatement pst = conn.prepareStatement(unitSql)) {
            pst.setInt(1, productId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    unitLabel = rs.getString("unit_label");
                    if (unitLabel == null || unitLabel.trim().isEmpty()) {
                        unitLabel = "pc";
                    }

                    safetyStock = rs.getBigDecimal("safety_stock");
                    if (safetyStock == null) {
                        safetyStock = BigDecimal.ZERO;
                    }
                }
            }
        }

        BigDecimal averageUsage = computeAverageUsageFromSales(conn, productId, unitLabel);
        BigDecimal reorderLevel = averageUsage.add(safetyStock);

        if (unitLabel.equalsIgnoreCase("kg")) {
            reorderLevel = reorderLevel.setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            reorderLevel = new BigDecimal(reorderLevel.setScale(0, java.math.RoundingMode.CEILING).toPlainString());
        }

        String updateProductSql = "UPDATE products SET average_usage = ? WHERE product_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(updateProductSql)) {
            pst.setBigDecimal(1, averageUsage);
            pst.setInt(2, productId);
            pst.executeUpdate();
        }

        String updateStockSql = "UPDATE stocks SET reorder_level = ? WHERE product_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(updateStockSql)) {
            pst.setBigDecimal(1, reorderLevel);
            pst.setInt(2, productId);
            pst.executeUpdate();
        }
    }
    
    private void setupCartQuantityButtons() {
        tblCart.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tblCart.rowAtPoint(e.getPoint());
                int col = tblCart.columnAtPoint(e.getPoint());

                if (row == -1) return;

                DefaultTableModel model = (DefaultTableModel) tblCart.getModel();

                try {
                    String productName = model.getValueAt(row, 0).toString();
                    double qty = parseCartQty(model.getValueAt(row, 2).toString());

                    double price = Double.parseDouble(
                        model.getValueAt(row, 4).toString().replace("₱", "").trim()
                    );

                    Connection conn = MySQLConnect.getConnection();
                    int productId = getProductIdByName(conn, productName);
                    double stock = getCurrentStockByProductId(conn, productId).doubleValue();

                    double step = qty % 1 != 0 ? 0.25 : 1.0;

                    if (col == 1) { // minus
                        qty -= step;

                        if (qty <= 0) {
                            model.removeRow(row);
                            updateTotals();
                            return;
                        }
                    } else if (col == 3) { // plus
                        qty += step;

                        if (qty > stock) {
                            JOptionPane.showMessageDialog(null, "Stock limit reached.");
                            return;
                        }
                    } else {
                        return;
                    }

                    double total = qty * price;

                    model.setValueAt(formatCartQtyDisplay(qty, "pc"), row, 2);
                    model.setValueAt(String.format("₱%.2f", total), row, 5);

                    updateTotals();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error updating quantity.");
                }
            }
        });
    }
    
    private void setupCartQtyEditListener() {
        tblCart.getModel().addTableModelListener(e -> {
            if (e.getColumn() != 2 || e.getFirstRow() < 0) return;

            int row = e.getFirstRow();
            DefaultTableModel model = (DefaultTableModel) tblCart.getModel();

            try {
                String productName = model.getValueAt(row, 0).toString();
                double qty = parseCartQty(model.getValueAt(row, 2).toString());

                if (qty <= 0) {
                    model.removeRow(row);
                    updateTotals();
                    return;
                }

                Connection conn = MySQLConnect.getConnection();
                int productId = getProductIdByName(conn, productName);
                double stock = getCurrentStockByProductId(conn, productId).doubleValue();

                if (qty > stock) {
                    JOptionPane.showMessageDialog(this, "Not enough stock.");
                    model.setValueAt(formatCartQtyDisplay(stock, "pc"), row, 2);
                    return;
                }

                double price = Double.parseDouble(
                    model.getValueAt(row, 4).toString().replace("₱", "").trim()
                );

                double total = qty * price;

                model.setValueAt(String.format("₱%.2f", total), row, 5);
                updateTotals();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity.");
                model.setValueAt("1", row, 2);
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

        pnlProducts = new irms.ui.components.RoundedPanel();
        lblCart2 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        searchbar = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblProducts = new javax.swing.JTable();
        pnlCart = new irms.ui.components.RoundedPanel();
        lblSubT = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        btnRemoveItem = new irms.ui.components.RoundedButtons();
        btnClearCart = new irms.ui.components.RoundedButtons();
        btnPay = new irms.ui.components.RoundedButtons();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblCart = new javax.swing.JTable();
        lblCart = new javax.swing.JLabel();
        lblChangeValue = new javax.swing.JLabel();
        lblTotalValue = new javax.swing.JLabel();
        lblBackground = new javax.swing.JLabel();

        setBackground(new java.awt.Color(200, 212, 222));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblCart2.setFont(new java.awt.Font("sansserif", 1, 24)); // NOI18N
        lblCart2.setForeground(new java.awt.Color(54, 67, 20));
        lblCart2.setText("Products");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Search:");

        tblProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Product", "Brand", "Stock", "Price", "Unit Type", "Unit", ""
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(tblProducts);

        javax.swing.GroupLayout pnlProductsLayout = new javax.swing.GroupLayout(pnlProducts);
        pnlProducts.setLayout(pnlProductsLayout);
        pnlProductsLayout.setHorizontalGroup(
            pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductsLayout.createSequentialGroup()
                .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlProductsLayout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(lblCart2, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlProductsLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlProductsLayout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(searchbar))
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        pnlProductsLayout.setVerticalGroup(
            pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlProductsLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(lblCart2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchbar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 481, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        add(pnlProducts, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, 530, 590));

        lblSubT.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblSubT.setText("   Change:");

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(126, 139, 74));
        lblTotal.setText("    Total:");

        btnRemoveItem.setBackground(new java.awt.Color(72, 92, 13));
        btnRemoveItem.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRemoveItem.setForeground(new java.awt.Color(255, 255, 255));
        btnRemoveItem.setText("Remove Item");
        btnRemoveItem.setFocusPainted(false);
        btnRemoveItem.addActionListener(this::btnRemoveItemActionPerformed);

        btnClearCart.setBackground(new java.awt.Color(154, 151, 33));
        btnClearCart.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnClearCart.setForeground(new java.awt.Color(255, 255, 255));
        btnClearCart.setText("Clear Cart");
        btnClearCart.setFocusPainted(false);
        btnClearCart.addActionListener(this::btnClearCartActionPerformed);

        btnPay.setBackground(new java.awt.Color(72, 92, 13));
        btnPay.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnPay.setForeground(new java.awt.Color(255, 255, 255));
        btnPay.setText("Pay");
        btnPay.setFocusPainted(false);
        btnPay.addActionListener(this::btnPayActionPerformed);

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

        lblCart.setFont(new java.awt.Font("sansserif", 1, 24)); // NOI18N
        lblCart.setForeground(new java.awt.Color(54, 67, 20));
        lblCart.setText("Cart");

        lblChangeValue.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblChangeValue.setText("---");

        lblTotalValue.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTotalValue.setForeground(new java.awt.Color(126, 139, 74));
        lblTotalValue.setText("---");

        javax.swing.GroupLayout pnlCartLayout = new javax.swing.GroupLayout(pnlCart);
        pnlCart.setLayout(pnlCartLayout);
        pnlCartLayout.setHorizontalGroup(
            pnlCartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCartLayout.createSequentialGroup()
                .addGroup(pnlCartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCartLayout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(lblCart, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlCartLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlCartLayout.createSequentialGroup()
                        .addGap(136, 136, 136)
                        .addGroup(pnlCartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlCartLayout.createSequentialGroup()
                                .addComponent(btnRemoveItem)
                                .addGap(11, 11, 11)
                                .addComponent(btnClearCart))
                            .addGroup(pnlCartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(lblTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblSubT, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(13, 13, 13)
                        .addGroup(pnlCartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblChangeValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnPay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTotalValue, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pnlCartLayout.setVerticalGroup(
            pnlCartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCartLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(lblCart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(pnlCartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSubT)
                    .addComponent(lblChangeValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotalValue, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlCartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnRemoveItem, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClearCart, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPay, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        add(pnlCart, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 160, 530, 590));

        lblBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/background/Sales List.png"))); // NOI18N
        add(lblBackground, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1070, 750));
    }// </editor-fold>//GEN-END:initComponents

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

            BigDecimal grossTotal = getCartGrandTotal();
            BigDecimal vatableSales = computeVatableSales(grossTotal);
            BigDecimal vatAmount = computeVatAmount(grossTotal);

            BigDecimal cashReceived = showCashDenominationDialog(grossTotal);

            if (cashReceived == null) {
                return;
            }

            BigDecimal changeAmount = cashReceived.subtract(grossTotal).setScale(2, RoundingMode.HALF_UP);
            lblChangeValue.setText(String.format("₱%.2f", changeAmount));
            
            String saleSql = "INSERT INTO sales (subtotal, total, vatable_sales, vat_amount, cash_received, change_amount, cashier_name, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            int saleId = 0;

            String firstName = getLoggedInCashierFirstName();

            try (PreparedStatement pstSale = conn.prepareStatement(saleSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                pstSale.setBigDecimal(1, grossTotal);
                pstSale.setBigDecimal(2, grossTotal);
                pstSale.setBigDecimal(3, vatableSales);
                pstSale.setBigDecimal(4, vatAmount);
                pstSale.setBigDecimal(5, cashReceived);
                pstSale.setBigDecimal(6, changeAmount);
                pstSale.setString(7, firstName);
                pstSale.setString(8, "VAT Sale");
                pstSale.executeUpdate();

                try (ResultSet rs = pstSale.getGeneratedKeys()) {
                    if (rs.next()) {
                        saleId = rs.getInt(1);
                    }
                }
            }

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                String productName = cartModel.getValueAt(i, 0).toString();
                double qty = parseCartQty(cartModel.getValueAt(i, 2).toString());
                double price = Double.parseDouble(cartModel.getValueAt(i, 4).toString().replace("₱", "").trim());
                double lineTotal = Double.parseDouble(cartModel.getValueAt(i, 5).toString().replace("₱", "").trim());

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
                updateProductAverageUsageAndReorderLevel(conn, productId);
            }

            conn.commit();

            String receipt = buildReceipt(
                saleId,
                vatableSales,
                vatAmount,
                grossTotal,
                cashReceived,
                changeAmount
            );

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
    private javax.swing.JButton btnClearCart;
    private javax.swing.JButton btnPay;
    private javax.swing.JButton btnRemoveItem;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblBackground;
    private javax.swing.JLabel lblCart;
    private javax.swing.JLabel lblCart2;
    private javax.swing.JLabel lblChangeValue;
    private javax.swing.JLabel lblSubT;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblTotalValue;
    private javax.swing.JPanel pnlCart;
    private javax.swing.JPanel pnlProducts;
    private javax.swing.JTextField searchbar;
    private javax.swing.JTable tblCart;
    private javax.swing.JTable tblProducts;
    // End of variables declaration//GEN-END:variables
}
