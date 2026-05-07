/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */

package irms.ui.panel;

import irms.db.MySQLConnect;
import irms.ui.frame.MainFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
/**
 *
 * @author USER
 */
public class restockRecords extends javax.swing.JPanel {

    /** Creates new form restockRecords */
    public restockRecords() {
        initComponents();

        java.util.Calendar cal = java.util.Calendar.getInstance();

        jDateChooser2.setDate(cal.getTime());

        cal.add(java.util.Calendar.DAY_OF_MONTH, -30);
        jDateChooser1.setDate(cal.getTime());

        setupTables();
        styleTables();
        addSearchListener();
        addTableClickListener();

        loadRestockRecords();
    }
    
    private void openPanel(javax.swing.JPanel panel) {
        java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);

        if (window instanceof MainFrame) {
            ((MainFrame) window).showPanel(panel);
        }
    }
    
    private void setupTables() {
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        jTable1.setModel(new DefaultTableModel(
            new Object[][]{},
            new String[]{"Restock Date", "Total Items", "Total Quantity", "Total Cost"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        jTable2.setModel(new DefaultTableModel(
            new Object[][]{},
            new String[]{"Product Name", "Brand", "Category", "Quantity", "Unit Type", "Unit Cost", "Subtotal", "Time"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    private void styleTables() {
        jTable1.setRowHeight(28);
        jTable2.setRowHeight(28);

        jTable1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jTable2.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTableHeader header1 = jTable1.getTableHeader();
        header1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header1.setBackground(new Color(220, 229, 236));
        header1.setForeground(new Color(54, 67, 20));
        header1.setReorderingAllowed(false);

        JTableHeader header2 = jTable2.getTableHeader();
        header2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header2.setBackground(new Color(220, 229, 236));
        header2.setForeground(new Color(54, 67, 20));
        header2.setReorderingAllowed(false);

        jTable2.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

        jTable2.getColumnModel().getColumn(0).setPreferredWidth(160); // Product Name
        jTable2.getColumnModel().getColumn(1).setPreferredWidth(110); // Brand
        jTable2.getColumnModel().getColumn(2).setPreferredWidth(130); // Category
        jTable2.getColumnModel().getColumn(3).setPreferredWidth(80);  // Quantity
        jTable2.getColumnModel().getColumn(4).setPreferredWidth(90);  // Unit Type
        jTable2.getColumnModel().getColumn(5).setPreferredWidth(90);  // Unit Cost
        jTable2.getColumnModel().getColumn(6).setPreferredWidth(100); // Subtotal
        jTable2.getColumnModel().getColumn(7).setPreferredWidth(90);  // Time
    }

    private void addSearchListener() {
        jTextField1.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                loadRestockRecords();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                loadRestockRecords();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                loadRestockRecords();
            }
        });
    }

    private void addTableClickListener() {
        jTable1.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = jTable1.getSelectedRow();

                if (row != -1) {
                    String selectedDate = jTable1.getValueAt(row, 0).toString();
                    loadItemsByDate(selectedDate);
                }
            }
        });
    }

    private void loadRestockRecords() {
        loadSummaryCards();
        loadRecordedRestockDates();

        if (jTable1.getRowCount() > 0) {
            jTable1.setRowSelectionInterval(0, 0);
            String selectedDate = jTable1.getValueAt(0, 0).toString();
            loadItemsByDate(selectedDate);
        } else {
            clearItemTable();
        }
    }

    private void loadSummaryCards() {
        java.util.Date fromDate = jDateChooser1.getDate();
        java.util.Date toDate = jDateChooser2.getDate();

        if (fromDate == null || toDate == null) {
            return;
        }

        String keyword = jTextField1.getText().trim();
        String like = "%" + keyword + "%";

        String sql =
            "SELECT " +
            "COUNT(DISTINCT sm.movement_date) AS total_records, " +
            "COALESCE(SUM(sm.quantity), 0) AS total_quantity, " +
            "COALESCE(SUM(sm.quantity * p.cost_price), 0) AS total_cost " +
            "FROM stock_movements sm " +
            "INNER JOIN products p ON sm.product_id = p.product_id " +
            "LEFT JOIN brands b ON p.brand_id = b.brand_id " +
            "LEFT JOIN categories c ON p.category_id = c.category_id " +
            "WHERE sm.movement_type = 'IN' " +
            "AND sm.movement_date BETWEEN ? AND ? " +
            "AND (p.product_name LIKE ? OR COALESCE(b.brand_name, '') LIKE ? OR COALESCE(c.category_name, '') LIKE ?)";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setDate(1, new Date(fromDate.getTime()));
            pst.setDate(2, new Date(toDate.getTime()));
            pst.setString(3, like);
            pst.setString(4, like);
            pst.setString(5, like);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    lblTotalProduct.setText(String.valueOf(rs.getInt("total_records")));
                    lblTotalProduct1.setText(formatNumber(rs.getBigDecimal("total_quantity")));
                    lblTotalProduct2.setText("₱" + formatMoney(rs.getBigDecimal("total_cost")));
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Summary load error: " + e.getMessage());
        }
    }

    private void loadRecordedRestockDates() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        java.util.Date fromDate = jDateChooser1.getDate();
        java.util.Date toDate = jDateChooser2.getDate();

        if (fromDate == null || toDate == null) {
            return;
        }

        String keyword = jTextField1.getText().trim();
        String like = "%" + keyword + "%";

        String sql =
            "SELECT " +
            "sm.movement_date AS restock_date, " +
            "COUNT(*) AS total_items, " +
            "COALESCE(SUM(sm.quantity), 0) AS total_quantity, " +
            "COALESCE(SUM(sm.quantity * p.cost_price), 0) AS total_cost " +
            "FROM stock_movements sm " +
            "INNER JOIN products p ON sm.product_id = p.product_id " +
            "LEFT JOIN brands b ON p.brand_id = b.brand_id " +
            "LEFT JOIN categories c ON p.category_id = c.category_id " +
            "WHERE sm.movement_type = 'IN' " +
            "AND sm.movement_date BETWEEN ? AND ? " +
            "AND (p.product_name LIKE ? OR COALESCE(b.brand_name, '') LIKE ? OR COALESCE(c.category_name, '') LIKE ?) " +
            "GROUP BY sm.movement_date " +
            "ORDER BY sm.movement_date DESC";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setDate(1, new Date(fromDate.getTime()));
            pst.setDate(2, new Date(toDate.getTime()));
            pst.setString(3, like);
            pst.setString(4, like);
            pst.setString(5, like);

            try (ResultSet rs = pst.executeQuery()) {
                int count = 0;

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getDate("restock_date"),
                        rs.getInt("total_items"),
                        formatNumber(rs.getBigDecimal("total_quantity")),
                        "₱" + formatMoney(rs.getBigDecimal("total_cost"))
                    });

                    count++;
                }

                jLabel6.setText(String.valueOf(count));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Restock records load error: " + e.getMessage());
        }
    }

    private void loadItemsByDate(String selectedDate) {
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);

        String sql =
            "SELECT " +
            "p.product_name, " +
            "COALESCE(b.brand_name, 'No Brand') AS brand_name, " +
            "COALESCE(c.category_name, 'No Category') AS category_name, " +
            "sm.quantity, " +
            "sm.unit_label, " +
            "p.cost_price, " +
            "(sm.quantity * p.cost_price) AS subtotal, " +
            "sm.movement_time " +
            "FROM stock_movements sm " +
            "INNER JOIN products p ON sm.product_id = p.product_id " +
            "LEFT JOIN brands b ON p.brand_id = b.brand_id " +
            "LEFT JOIN categories c ON p.category_id = c.category_id " +
            "WHERE sm.movement_type = 'IN' " +
            "AND sm.movement_date = ? " +
            "ORDER BY sm.movement_time ASC";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setDate(1, Date.valueOf(selectedDate));

            try (ResultSet rs = pst.executeQuery()) {
                int itemCount = 0;
                BigDecimal totalQuantity = BigDecimal.ZERO;
                BigDecimal totalCost = BigDecimal.ZERO;

                while (rs.next()) {
                    BigDecimal quantity = rs.getBigDecimal("quantity");
                    BigDecimal costPrice = rs.getBigDecimal("cost_price");
                    BigDecimal subtotal = rs.getBigDecimal("subtotal");

                    if (quantity == null) quantity = BigDecimal.ZERO;
                    if (costPrice == null) costPrice = BigDecimal.ZERO;
                    if (subtotal == null) subtotal = BigDecimal.ZERO;

                    model.addRow(new Object[]{
                        rs.getString("product_name"),
                        rs.getString("brand_name"),
                        rs.getString("category_name"),
                        formatNumber(quantity),
                        rs.getString("unit_label"),
                        "₱" + formatMoney(costPrice),
                        "₱" + formatMoney(subtotal),
                        rs.getTime("movement_time")
                    });

                    itemCount++;
                    totalQuantity = totalQuantity.add(quantity);
                    totalCost = totalCost.add(subtotal);
                }

                jLabel3.setText(selectedDate);
                jLabel9.setText(String.valueOf(itemCount));
                jLabel8.setText(formatNumber(totalQuantity));
                jLabel7.setText("₱" + formatMoney(totalCost));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Items load error: " + e.getMessage());
        }
    }

    private void clearItemTable() {
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);

        jLabel3.setText("---");
        jLabel9.setText("0");
        jLabel8.setText("0");
        jLabel7.setText("₱0.00");
    }

    private void refreshReport() {
        jTextField1.setText("");

        java.util.Calendar cal = java.util.Calendar.getInstance();

        jDateChooser2.setDate(cal.getTime());

        cal.add(java.util.Calendar.DAY_OF_MONTH, -30);
        jDateChooser1.setDate(cal.getTime());

        loadRestockRecords();
    }

    private void loadProductCombo(JComboBox<String> cmbProduct) {
        cmbProduct.removeAllItems();
        cmbProduct.addItem("All Products");

        String sql = "SELECT product_name FROM products ORDER BY product_name";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                cmbProduct.addItem(rs.getString("product_name"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Product combo load error: " + e.getMessage());
        }
    }

    private void loadAllRestockRecords(
        DefaultTableModel model,
        JLabel lblRows,
        JLabel lblTotalQty,
        JLabel lblTotalCost,
        String keyword,
        String selectedProduct,
        java.util.Date fromDate,
        java.util.Date toDate
    ) {
        model.setRowCount(0);

        if (fromDate == null || toDate == null) {
            return;
        }

        String like = "%" + keyword + "%";

        String sql =
            "SELECT " +
            "sm.movement_date, " +
            "p.product_name, " +
            "COALESCE(b.brand_name, 'No Brand') AS brand_name, " +
            "sm.quantity, " +
            "sm.unit_label, " +
            "p.cost_price, " +
            "(sm.quantity * p.cost_price) AS subtotal, " +
            "sm.movement_time " +
            "FROM stock_movements sm " +
            "INNER JOIN products p ON sm.product_id = p.product_id " +
            "LEFT JOIN brands b ON p.brand_id = b.brand_id " +
            "LEFT JOIN categories c ON p.category_id = c.category_id " +
            "WHERE sm.movement_type = 'IN' " +
            "AND sm.movement_date BETWEEN ? AND ? " +
            "AND (? = 'All Products' OR p.product_name = ?) " +
            "AND (p.product_name LIKE ? OR COALESCE(b.brand_name, '') LIKE ? OR COALESCE(c.category_name, '') LIKE ?) " +
            "ORDER BY sm.movement_date DESC, sm.movement_time ASC";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setDate(1, new Date(fromDate.getTime()));
            pst.setDate(2, new Date(toDate.getTime()));
            pst.setString(3, selectedProduct);
            pst.setString(4, selectedProduct);
            pst.setString(5, like);
            pst.setString(6, like);
            pst.setString(7, like);

            try (ResultSet rs = pst.executeQuery()) {
                int rows = 0;
                BigDecimal totalQuantity = BigDecimal.ZERO;
                BigDecimal totalCost = BigDecimal.ZERO;

                while (rs.next()) {
                    BigDecimal quantity = rs.getBigDecimal("quantity");
                    BigDecimal costPrice = rs.getBigDecimal("cost_price");
                    BigDecimal subtotal = rs.getBigDecimal("subtotal");

                    if (quantity == null) quantity = BigDecimal.ZERO;
                    if (costPrice == null) costPrice = BigDecimal.ZERO;
                    if (subtotal == null) subtotal = BigDecimal.ZERO;

                    model.addRow(new Object[]{
                        rs.getDate("movement_date"),
                        rs.getString("product_name"),
                        rs.getString("brand_name"),
                        formatNumber(quantity),
                        rs.getString("unit_label"),
                        "₱" + formatMoney(costPrice),
                        "₱" + formatMoney(subtotal),
                        rs.getTime("movement_time")
                    });

                    rows++;
                    totalQuantity = totalQuantity.add(quantity);
                    totalCost = totalCost.add(subtotal);
                }

                lblRows.setText("Rows: " + rows);
                lblTotalQty.setText("Total Quantity: " + formatNumber(totalQuantity));
                lblTotalCost.setText("Total Cost: ₱" + formatMoney(totalCost));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "View all load error: " + e.getMessage());
        }
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }

        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(value);
    }

    private String formatNumber(BigDecimal value) {
        if (value == null) {
            return "0";
        }

        BigDecimal cleaned = value.stripTrailingZeros();

        if (cleaned.scale() <= 0) {
            return cleaned.toPlainString();
        }

        return cleaned.toPlainString();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblReportType = new javax.swing.JLabel();
        lblSearch = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        lblDate = new javax.swing.JLabel();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        btnViewAll = new irms.ui.components.RoundedButtons();
        btnRefresh = new irms.ui.components.RoundedButtons();
        jTextField1 = new javax.swing.JTextField();
        pnlTotalStockRecords = new irms.ui.components.RoundedPanel();
        lblTotalProduct = new javax.swing.JLabel();
        lblProductsText = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        pnlTotalItemsBought = new irms.ui.components.RoundedPanel();
        lblTotalProduct1 = new javax.swing.JLabel();
        lblProductsText1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        pnlTotalRestockCost = new irms.ui.components.RoundedPanel();
        lblTotalProduct2 = new javax.swing.JLabel();
        lblProductsText2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        pnlRecordedStockDates = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        lblTotalRecords = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        pnlItemRestocked = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        lblSelectedDate = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblTotalRecords1 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblTotalRecords2 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        lblTotalRecords3 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblReportType.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblReportType.setText("Date from:");
        add(lblReportType, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 160, -1, 40));

        lblSearch.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSearch.setText("Search:");
        add(lblSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, -1, 40));
        add(jDateChooser1, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 160, 210, 40));

        lblDate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblDate.setText("Date to:");
        add(lblDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 160, -1, 40));
        add(jDateChooser2, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 160, 210, 40));

        btnViewAll.setBackground(new java.awt.Color(72, 92, 13));
        btnViewAll.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnViewAll.setForeground(new java.awt.Color(255, 255, 255));
        btnViewAll.setText("Refresh");
        btnViewAll.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnViewAll.addActionListener(this::btnViewAllActionPerformed);
        add(btnViewAll, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 160, 110, 40));

        btnRefresh.setBackground(new java.awt.Color(154, 151, 33));
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRefresh.setForeground(new java.awt.Color(255, 255, 255));
        btnRefresh.setText("Back");
        btnRefresh.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);
        add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 160, 110, 40));
        add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 160, 210, 40));

        pnlTotalStockRecords.setBackground(new java.awt.Color(255, 255, 255));
        pnlTotalStockRecords.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalProduct.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTotalProduct.setText("---");
        pnlTotalStockRecords.add(lblTotalProduct, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 20, -1, -1));

        lblProductsText.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblProductsText.setForeground(new java.awt.Color(45, 45, 45));
        lblProductsText.setText("Total Restock Records");
        pnlTotalStockRecords.add(lblProductsText, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 50, -1, -1));

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/icon/Item Sold_1.png"))); // NOI18N
        pnlTotalStockRecords.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, -1, 60));

        add(pnlTotalStockRecords, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, 340, 100));

        pnlTotalItemsBought.setBackground(new java.awt.Color(255, 255, 255));
        pnlTotalItemsBought.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalProduct1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTotalProduct1.setText("---");
        pnlTotalItemsBought.add(lblTotalProduct1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 20, -1, -1));

        lblProductsText1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblProductsText1.setForeground(new java.awt.Color(45, 45, 45));
        lblProductsText1.setText("Total Item Bought");
        pnlTotalItemsBought.add(lblProductsText1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 50, -1, -1));

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/icon/Item Bought.png"))); // NOI18N
        pnlTotalItemsBought.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, -1, 60));

        add(pnlTotalItemsBought, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 220, 350, 100));

        pnlTotalRestockCost.setBackground(new java.awt.Color(255, 255, 255));
        pnlTotalRestockCost.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalProduct2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTotalProduct2.setText("---");
        pnlTotalRestockCost.add(lblTotalProduct2, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 20, -1, -1));

        lblProductsText2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblProductsText2.setForeground(new java.awt.Color(45, 45, 45));
        lblProductsText2.setText("Total Restock Cost");
        pnlTotalRestockCost.add(lblProductsText2, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 50, -1, -1));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/icon/Item Cost.png"))); // NOI18N
        pnlTotalRestockCost.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, -1, -1));

        add(pnlTotalRestockCost, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 220, 340, 100));

        pnlRecordedStockDates.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Restock Date", "Total Items", "Total Quantity", "Total Cost"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
            jTable1.getColumnModel().getColumn(1).setResizable(false);
            jTable1.getColumnModel().getColumn(2).setResizable(false);
            jTable1.getColumnModel().getColumn(3).setResizable(false);
        }

        pnlRecordedStockDates.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 370, 330));

        jPanel1.setBackground(new java.awt.Color(126, 139, 74));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Recorded Restock Dates");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel2)
                .addContainerGap(189, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlRecordedStockDates.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 370, 40));

        lblTotalRecords.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTotalRecords.setText("Total Records:");
        pnlRecordedStockDates.add(lblTotalRecords, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 380, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("...");
        pnlRecordedStockDates.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 380, -1, -1));

        add(pnlRecordedStockDates, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 340, 370, 410));

        pnlItemRestocked.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Product Name", "Brand", "Category", "Quantity", "Unit Type", "Unit Cost", "Subtotal", "Time"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        pnlItemRestocked.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 670, 330));

        jPanel2.setBackground(new java.awt.Color(126, 139, 74));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Item Restocked on Selected Date");

        lblSelectedDate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSelectedDate.setForeground(new java.awt.Color(255, 255, 255));
        lblSelectedDate.setText("Selected Date:");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("2026-05-06");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 242, Short.MAX_VALUE)
                .addComponent(lblSelectedDate)
                .addGap(10, 10, 10)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
            .addComponent(lblSelectedDate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pnlItemRestocked.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 670, 40));

        lblTotalRecords1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTotalRecords1.setText("Total Cost:");
        pnlItemRestocked.add(lblTotalRecords1, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 380, -1, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("...");
        pnlItemRestocked.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 380, -1, -1));

        lblTotalRecords2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTotalRecords2.setText("Total Quantity:");
        pnlItemRestocked.add(lblTotalRecords2, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 380, -1, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("...");
        pnlItemRestocked.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 380, -1, -1));

        lblTotalRecords3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTotalRecords3.setText("Items:");
        pnlItemRestocked.add(lblTotalRecords3, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 380, -1, -1));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("...");
        pnlItemRestocked.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 380, -1, -1));

        add(pnlItemRestocked, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 340, 670, 410));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/resources/background/Stock List.png"))); // NOI18N
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        openPanel(new stockPanel());
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnViewAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewAllActionPerformed
        // TODO add your handling code here:
         loadRestockRecords();
    }//GEN-LAST:event_btnViewAllActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnViewAll;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblProductsText;
    private javax.swing.JLabel lblProductsText1;
    private javax.swing.JLabel lblProductsText2;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblSelectedDate;
    private javax.swing.JLabel lblTotalProduct;
    private javax.swing.JLabel lblTotalProduct1;
    private javax.swing.JLabel lblTotalProduct2;
    private javax.swing.JLabel lblTotalRecords;
    private javax.swing.JLabel lblTotalRecords1;
    private javax.swing.JLabel lblTotalRecords2;
    private javax.swing.JLabel lblTotalRecords3;
    private javax.swing.JPanel pnlItemRestocked;
    private javax.swing.JPanel pnlRecordedStockDates;
    private javax.swing.JPanel pnlTotalItemsBought;
    private javax.swing.JPanel pnlTotalRestockCost;
    private javax.swing.JPanel pnlTotalStockRecords;
    // End of variables declaration//GEN-END:variables

}
