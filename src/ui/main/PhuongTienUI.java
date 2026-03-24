package ui.main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import ui.util.AppColors;
import ui.util.PrimaryButton;

public class PhuongTienUI {
    private final Color MAU_XANH_CHINH = AppColors.REVENUE;
    private final Color MAU_NEN = AppColors.APP_BACKGROUND;
    private DefaultTableModel model;
    private JTable table;
    private final PrimaryButton primaryButton = new PrimaryButton();

    public JPanel getPanel() {
        JPanel pnl = new JPanel(new BorderLayout(20, 20));
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnl.setBackground(MAU_NEN);

        // --- THANH TRÊN (Tiêu đề & Tìm kiếm & Nút Đăng ký) ---
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản Lý Phương Tiện");
        lblTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 24));

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setOpaque(false);

        JTextField txtSearch = new JTextField(20);
        txtSearch.setToolTipText("Tìm kiếm theo biển số hoặc tên...");

        JButton btnAdd = primaryButton.makePrimaryButton("Đăng ký xe");
        btnAdd.setBackground(MAU_XANH_CHINH);
        btnAdd.setForeground(AppColors.WHITE);
        btnAdd.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));
        btnAdd.setFocusPainted(false);

        pnlActions.add(new JLabel("Tìm kiếm: "));
        pnlActions.add(txtSearch);
        pnlActions.add(btnAdd);

        pnlTop.add(lblTitle, BorderLayout.WEST);
        pnlTop.add(pnlActions, BorderLayout.EAST);

        // --- BẢNG DỮ LIỆU ---
        String[] columns = { "Biển số xe", "Loại xe", "Chủ sở hữu", "Phòng", "Phí gửi/tháng", "Thao tác" };
        model = new DefaultTableModel(new Object[][] {
                { "29A-123.45", "Ô tô", "Nguyễn Văn An", "T1.102", "1.200.000", "✎  🗑" },
                { "51G-999.99", "Xe máy", "Trần Thị Bình", "T2.205", "150.000", "✎  🗑" }
        }, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("Be Vietnam Pro", Font.BOLD, 13));

        // Cấu hình Tìm kiếm (Search)
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtSearch.getText()));
            }
        });

        // Xử lý sự kiện click Sửa/Xóa
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.convertRowIndexToModel(table.getSelectedRow());
                int col = table.getSelectedColumn();
                if (col == 5) { // Cột thao tác
                    Point p = e.getPoint();
                    int columnWidth = table.getColumnModel().getColumn(5).getWidth();
                    // Click bên trái (✎) hoặc bên phải (🗑)
                    if (e.getX() % columnWidth < columnWidth / 2) {
                        showEditDialog(row);
                    } else {
                        handleDelete(row);
                    }
                }
            }
        });

        btnAdd.addActionListener(e -> showRegistrationDialog());

        pnl.add(pnlTop, BorderLayout.NORTH);
        pnl.add(new JScrollPane(table), BorderLayout.CENTER);
        return pnl;
    }

    // --- CHỨC NĂNG 1: ĐĂNG KÝ XE ---
    private void showRegistrationDialog() {
        JDialog dialog = new JDialog((Frame) null, "Đăng ký phương tiện mới", true);
        setupDialogForm(dialog, null);
    }

    // --- CHỨC NĂNG 2: SỬA PHƯƠNG TIỆN ---
    private void showEditDialog(int modelRow) {
        JDialog dialog = new JDialog((Frame) null, "Chỉnh sửa phương tiện", true);
        setupDialogForm(dialog, modelRow);
    }

    // Hàm dùng chung cho Form nhập liệu (Đăng ký/Sửa)
    private void setupDialogForm(JDialog dialog, Integer rowToEdit) {
        dialog.setLayout(new BorderLayout());
        JPanel pnlForm = new JPanel(new GridLayout(5, 2, 10, 15));
        pnlForm.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtBien = new JTextField();
        JComboBox<String> cbLoai = new JComboBox<>(new String[] { "Xe máy", "Ô tô", "Xe điện" });
        JTextField txtChu = new JTextField();
        JTextField txtPhong = new JTextField();
        JTextField txtPhi = new JTextField();

        if (rowToEdit != null) { // Nếu là Sửa, đổ dữ liệu cũ vào
            txtBien.setText(model.getValueAt(rowToEdit, 0).toString());
            txtChu.setText(model.getValueAt(rowToEdit, 2).toString());
            txtPhong.setText(model.getValueAt(rowToEdit, 3).toString());
            txtPhi.setText(model.getValueAt(rowToEdit, 4).toString());
        }

        pnlForm.add(new JLabel("Biển số:"));
        pnlForm.add(txtBien);
        pnlForm.add(new JLabel("Loại xe:"));
        pnlForm.add(cbLoai);
        pnlForm.add(new JLabel("Chủ xe:"));
        pnlForm.add(txtChu);
        pnlForm.add(new JLabel("Phòng:"));
        pnlForm.add(txtPhong);
        pnlForm.add(new JLabel("Phí gửi (vnđ):"));
        pnlForm.add(txtPhi);

        JButton btnConfirm = new JButton("Xác nhận");
        btnConfirm.addActionListener(e -> {
            Object[] rowData = { txtBien.getText(), cbLoai.getSelectedItem(), txtChu.getText(), txtPhong.getText(),
                    txtPhi.getText(), "✎  🗑" };
            if (rowToEdit == null)
                model.addRow(rowData);
            else {
                for (int i = 0; i < 5; i++)
                    model.setValueAt(rowData[i], rowToEdit, i);
            }
            dialog.dispose();
        });

        dialog.add(pnlForm, BorderLayout.CENTER);
        dialog.add(btnConfirm, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    // --- CHỨC NĂNG 3: XÓA ---
    private void handleDelete(int modelRow) {
        String bienSo = model.getValueAt(modelRow, 0).toString();
        int opt = JOptionPane.showConfirmDialog(null, "Xóa phương tiện " + bienSo + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            model.removeRow(modelRow);
        }
    }
}