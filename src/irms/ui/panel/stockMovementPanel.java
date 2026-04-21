/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package irms.ui.panel;

import irms.ui.frame.MainFrame;
import irms.db.MySQLConnect;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author USER
 */
public class stockMovementPanel extends javax.swing.JPanel {

    /**
     * Creates new form stockMovementPanel
     */
    public stockMovementPanel() {
        initComponents();
        loadHistory();
        autoSearchHistory();
        styleTable();
    }
    
    private String formatMovementQty(BigDecimal qty, String unitLabel) {
        if (qty == null) {
            return "0";
        }

        if (unitLabel != null && unitLabel.equalsIgnoreCase("kg")) {
            return String.format("%.2f", qty);
        }

        return String.valueOf(qty.intValue());
    }

    private void loadHistory() {
        DefaultTableModel model = (DefaultTableModel) tblStocks.getModel();
        model.setRowCount(0);

        String sql = "SELECT sm.movement_id, p.product_name, sm.movement_type, sm.quantity, sm.unit_label, " +
                     "sm.previous_stock, sm.new_stock, sm.remarks, u.username, sm.movement_date, sm.movement_time " +
                     "FROM stock_movements sm " +
                     "INNER JOIN products p ON sm.product_id = p.product_id " +
                     "LEFT JOIN users u ON sm.moved_by = u.user_id " +
                     "ORDER BY sm.movement_id DESC";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String unitLabel = rs.getString("unit_label");

                model.addRow(new Object[]{
                    rs.getInt("movement_id"),
                    rs.getString("product_name"),
                    rs.getString("movement_type"),
                    formatMovementQty(rs.getBigDecimal("quantity"), unitLabel),
                    unitLabel,
                    formatMovementQty(rs.getBigDecimal("previous_stock"), unitLabel),
                    formatMovementQty(rs.getBigDecimal("new_stock"), unitLabel),
                    rs.getString("remarks"),
                    rs.getString("username"),
                    rs.getDate("movement_date"),
                    rs.getTime("movement_time")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Load history error: " + e.getMessage());
        }
    }

    private void searchHistory(String keyword) {
        DefaultTableModel model = (DefaultTableModel) tblStocks.getModel();
        model.setRowCount(0);

        String sql = "SELECT sm.movement_id, p.product_name, sm.movement_type, sm.quantity, sm.unit_label, " +
                     "sm.previous_stock, sm.new_stock, sm.remarks, u.username, sm.movement_date, sm.movement_time " +
                     "FROM stock_movements sm " +
                     "INNER JOIN products p ON sm.product_id = p.product_id " +
                     "LEFT JOIN users u ON sm.moved_by = u.user_id " +
                     "WHERE CAST(sm.movement_id AS CHAR) LIKE ? " +
                     "   OR p.product_name LIKE ? " +
                     "   OR sm.movement_type LIKE ? " +
                     "   OR sm.remarks LIKE ? " +
                     "   OR COALESCE(u.username, '') LIKE ? " +
                     "ORDER BY sm.movement_id DESC";

        try (Connection conn = MySQLConnect.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            pst.setString(1, like);
            pst.setString(2, like);
            pst.setString(3, like);
            pst.setString(4, like);
            pst.setString(5, like);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String unitLabel = rs.getString("unit_label");

                    model.addRow(new Object[]{
                        rs.getInt("movement_id"),
                        rs.getString("product_name"),
                        rs.getString("movement_type"),
                        formatMovementQty(rs.getBigDecimal("quantity"), unitLabel),
                        unitLabel,
                        formatMovementQty(rs.getBigDecimal("previous_stock"), unitLabel),
                        formatMovementQty(rs.getBigDecimal("new_stock"), unitLabel),
                        rs.getString("remarks"),
                        rs.getString("username"),
                        rs.getDate("movement_date"),
                        rs.getTime("movement_time")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Search history error: " + e.getMessage());
        }
    }

    private void autoSearchHistory() {
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
                    loadHistory();
                } else {
                    searchHistory(text);
                }
            }
        });
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
    
    private void openPanel(javax.swing.JPanel panel) {
        java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);

        if (window instanceof MainFrame) {
            ((MainFrame) window).showPanel(panel);
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

        btnClose = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblStocks = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        searchbar = new javax.swing.JTextField();
        lblBackground = new javax.swing.JLabel();

        setBackground(new java.awt.Color(200, 212, 222));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnClose.setText("Close");
        btnClose.addActionListener(this::btnCloseActionPerformed);
        add(btnClose, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 700, 100, 30));

        tblStocks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Movement ID", "Product Name", "Movement Type", "Quantity", "Unit Label", "Previous Stock", "New Stock", "Remarks", "Moved By", "Date", "Time"
            }
        ));
        tblStocks.setEnabled(false);
        jScrollPane1.setViewportView(tblStocks);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, 1060, 440));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Search:");
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 50, 30));

        searchbar.addActionListener(this::searchbarActionPerformed);
        add(searchbar, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 160, 270, 30));

        lblBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/irms/design/Stock List.png"))); // NOI18N
        add(lblBackground, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        // TODO add your handling code here:
        openPanel(new stockPanel());

    }//GEN-LAST:event_btnCloseActionPerformed

    private void searchbarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchbarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchbarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBackground;
    private javax.swing.JTextField searchbar;
    private javax.swing.JTable tblStocks;
    // End of variables declaration//GEN-END:variables
}
