package ui.main;

import entity.KhachHang;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import service.KhachHangService;
import ui.util.AppColors;
import ui.util.PrimaryButton;

public class KhachHangUI {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Font FONT_TITLE = new Font("Be Vietnam Pro", Font.BOLD, 24);
    private final Font FONT_BOLD = new Font("Be Vietnam Pro", Font.BOLD, 14);
    private final Font FONT_PLAIN = new Font("Be Vietnam Pro", Font.PLAIN, 14);
    private final Font FONT_SMALL = new Font("Be Vietnam Pro", Font.PLAIN, 13);

    private final ImageIcon ICON_PEN = loadActionIcon("pen.png", 16, 16);
    private final ImageIcon ICON_BIN = loadActionIcon("bin.png", 16, 16);

    private final KhachHangService khachHangService = new KhachHangService();
    private final PrimaryButton primaryButton = new PrimaryButton();

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtTimKiem;

    public JPanel getPanel() {
        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(AppColors.APP_BACKGROUND);

        root.add(createTopBar(), BorderLayout.NORTH);
        root.add(createTableCard(), BorderLayout.CENTER);

        loadKhachHangData();
        return root;
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(AppColors.APP_BACKGROUND);

        JLabel lblTitle = new JLabel("Quản lý khách hàng");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(AppColors.SLATE_900);

        JButton btnThem = primaryButton.makePrimaryButton("Thêm khách hàng");
        btnThem.addActionListener(e -> showKhachHangDialog(null));

        top.add(lblTitle, BorderLayout.WEST);
        top.add(btnThem, BorderLayout.EAST);
        return top;
    }

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(AppColors.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_200, 1, true),
                new EmptyBorder(12, 12, 12, 12)));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(AppColors.WHITE);

        txtTimKiem = new JTextField();
        txtTimKiem.setPreferredSize(new Dimension(300, 36));
        txtTimKiem.setFont(FONT_PLAIN);
        txtTimKiem.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_200, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        txtTimKiem.setToolTipText("Tìm theo mã, họ tên, số điện thoại, CCCD");

        bar.add(txtTimKiem, BorderLayout.WEST);
        card.add(bar, BorderLayout.NORTH);

        String[] cols = { "Mã KH", "Họ tên", "SĐT", "CCCD", "Ngày sinh", "Địa chỉ", "Thao tác" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        table = new JTable(tableModel) {
            private int hoverRow = -1;

            {
                addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(java.awt.event.MouseEvent e) {
                        int row = rowAtPoint(e.getPoint());
                        if (hoverRow != row) {
                            hoverRow = row;
                            repaint();
                        }
                    }
                });
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hoverRow = -1;
                        repaint();
                    }
                });
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(hoverRow == row ? AppColors.SLATE_50 : AppColors.WHITE);
                }
                return c;
            }
        };

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setFont(FONT_SMALL);
                setForeground(AppColors.SLATE_600);
                setBackground(AppColors.WHITE);
                setBorder(new EmptyBorder(0, 10, 0, 8));
                setHorizontalAlignment(SwingConstants.LEFT);
                return this;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        table.setRowHeight(52);
        table.setFont(FONT_PLAIN);
        table.setForeground(AppColors.SLATE_900);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setGridColor(AppColors.WHITE);
        table.setSelectionBackground(AppColors.PRIMARY_TINT_HOVER);
        table.setSelectionForeground(AppColors.SLATE_900);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(260);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setMaxWidth(100);

        DefaultTableCellRenderer paddedCell = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
                    int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200),
                        new EmptyBorder(0, 10, 0, 8)));
                setFont(col == 1 ? FONT_BOLD : FONT_PLAIN);
                setForeground(AppColors.SLATE_900);
                return this;
            }
        };

        for (int i = 0; i < 6; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(paddedCell);
        }

        table.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionEditor());

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(AppColors.WHITE);
        sp.setBackground(AppColors.WHITE);

        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private void applyFilter() {
        String keyword = txtTimKiem.getText().trim();
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(keyword), 0, 1, 2, 3));
    }

    private void loadKhachHangData() {
        tableModel.setRowCount(0);
        try {
            List<KhachHang> danhSach = khachHangService.layDanhSachKhachHang();
            for (KhachHang kh : danhSach) {
                tableModel.addRow(new Object[] {
                        kh.getMaKhachHang(),
                        kh.getHoTen(),
                        nullSafe(kh.getSoDienThoai()),
                        nullSafe(kh.getSoCCCD()),
                        kh.getNgaySinh() == null ? "" : DATE_FORMAT.format(kh.getNgaySinh()),
                        nullSafe(kh.getDiaChi()),
                        "ACT"
                });
            }
        } catch (RuntimeException ex) {
            showError("Không tải được danh sách khách hàng: " + ex.getMessage());
        }
    }

    private void showKhachHangDialog(KhachHang editKhachHang) {
        boolean isEdit = editKhachHang != null;
        String maKhachHangEdit = "";
        String hoTenValue = "";
        String sdtValue = "";
        String ngaySinhValue = "";
        String cccdValue = "";
        String diaChiValue = "";

        if (isEdit && editKhachHang != null) {
            maKhachHangEdit = nullSafe(editKhachHang.getMaKhachHang());
            hoTenValue = nullSafe(editKhachHang.getHoTen());
            sdtValue = nullSafe(editKhachHang.getSoDienThoai());
            cccdValue = nullSafe(editKhachHang.getSoCCCD());
            diaChiValue = nullSafe(editKhachHang.getDiaChi());
            if (editKhachHang.getNgaySinh() != null) {
                ngaySinhValue = DATE_FORMAT.format(editKhachHang.getNgaySinh());
            }
        }
        final String maKhachHangEditFinal = maKhachHangEdit;

        Window owner = SwingUtilities.getWindowAncestor(table);
        JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null,
                isEdit ? "Sửa khách hàng" : "Thêm khách hàng", true);
        dlg.setSize(560, 390);
        dlg.setLocationRelativeTo(owner);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.APP_BACKGROUND);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(AppColors.WHITE);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel(isEdit ? "Cập nhật thông tin khách hàng" : "Nhập thông tin khách hàng");
        title.setFont(new Font("Be Vietnam Pro", Font.BOLD, 18));
        title.setForeground(AppColors.SLATE_900);
        card.add(title, BorderLayout.NORTH);

        JTextField txtHoTen = makeField(hoTenValue);
        JTextField txtSdt = makeField(sdtValue);
        JTextField txtNgaySinh = makeField(ngaySinhValue);
        JTextField txtCccd = makeField(cccdValue);
        JTextField txtDiaChi = makeField(diaChiValue);

        JPanel grid = new JPanel(new GridLayout(3, 2, 12, 12));
        grid.setBackground(AppColors.WHITE);
        grid.add(wrapField("Họ tên *", txtHoTen));
        grid.add(wrapField("Số điện thoại", txtSdt));
        grid.add(wrapField("Ngày sinh (yyyy-MM-dd)", txtNgaySinh));
        grid.add(wrapField("CCCD", txtCccd));
        grid.add(wrapField("Địa chỉ", txtDiaChi));
        grid.add(new JPanel());

        card.add(grid, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(AppColors.WHITE);

        JButton btnHuy = makeOutlineButton("Hủy");
        btnHuy.addActionListener(e -> dlg.dispose());

        JButton btnLuu = primaryButton.makePrimaryButton(isEdit ? "Cập nhật" : "Thêm");
        btnLuu.addActionListener(e -> {
            try {
                String hoTen = txtHoTen.getText().trim();
                if (hoTen.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Họ tên không được để trống.", "Thiếu thông tin",
                            JOptionPane.WARNING_MESSAGE);
                    txtHoTen.requestFocus();
                    return;
                }

                LocalDate ngaySinh = null;
                String ngaySinhRaw = txtNgaySinh.getText().trim();
                if (!ngaySinhRaw.isEmpty()) {
                    try {
                        ngaySinh = LocalDate.parse(ngaySinhRaw, DATE_FORMAT);
                    } catch (DateTimeParseException ex) {
                        JOptionPane.showMessageDialog(dlg, "Ngày sinh không đúng định dạng yyyy-MM-dd.",
                                "Sai định dạng",
                                JOptionPane.WARNING_MESSAGE);
                        txtNgaySinh.requestFocus();
                        return;
                    }
                }

                KhachHang payload = new KhachHang();
                if (isEdit) {
                    payload.setMaKhachHang(maKhachHangEditFinal);
                }
                payload.setHoTen(hoTen);
                payload.setSoDienThoai(txtSdt.getText().trim());
                payload.setNgaySinh(ngaySinh);
                payload.setSoCCCD(txtCccd.getText().trim());
                payload.setDiaChi(txtDiaChi.getText().trim());

                if (isEdit) {
                    boolean ok = khachHangService.capNhatKhachHang(payload);
                    if (!ok) {
                        throw new RuntimeException("Không tìm thấy khách hàng để cập nhật.");
                    }
                } else {
                    khachHangService.themKhachHang(payload);
                }

                loadKhachHangData();
                dlg.dispose();
            } catch (RuntimeException ex) {
                showError("Lưu khách hàng thất bại: " + ex.getMessage());
            }
        });

        actions.add(btnHuy);
        actions.add(btnLuu);
        card.add(actions, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private JTextField makeField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(FONT_PLAIN);
        field.setForeground(AppColors.SLATE_900);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_200, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        field.setPreferredSize(new Dimension(0, 38));
        return field;
    }

    private JComponent wrapField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(AppColors.WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(AppColors.SLATE_900);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JButton makeOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_PLAIN);
        btn.setForeground(AppColors.SLATE_600);
        btn.setBackground(AppColors.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_200, 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        return btn;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(table, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private ImageIcon loadActionIcon(String fileName, int width, int height) {
        String[] paths = { "img/icon/" + fileName, "img/icons/" + fileName };
        for (String path : paths) {
            ImageIcon raw = new ImageIcon(path);
            if (raw.getIconWidth() > 0 && raw.getIconHeight() > 0) {
                Image scaled = raw.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        }
        return null;
    }

    private JLabel makeActionIconLabel(ImageIcon icon) {
        JLabel label = new JLabel();
        if (icon != null) {
            label.setIcon(icon);
        }
        return label;
    }

    class ActionRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
                int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 15));
            p.setBackground(isSelected ? AppColors.PRIMARY_TINT_HOVER : AppColors.WHITE);
            p.setBorder(new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200));

            JLabel lblSua = makeActionIconLabel(ICON_PEN);
            JLabel lblXoa = makeActionIconLabel(ICON_BIN);

            p.add(lblSua);
            p.add(lblXoa);
            return p;
        }
    }

    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private int modelRow;

        ActionEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 11));
            panel.setBackground(AppColors.WHITE);
            panel.setBorder(new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200));

            JButton btnSua = makeIconButton(ICON_PEN, "Sửa");
            JButton btnXoa = makeIconButton(ICON_BIN, "Xóa");

            btnSua.addActionListener(e -> {
                stopCellEditing();
                String maKhachHang = String.valueOf(tableModel.getValueAt(modelRow, 0));
                try {
                    KhachHang kh = khachHangService.timTheoMa(maKhachHang);
                    if (kh == null) {
                        showError("Không tìm thấy khách hàng để sửa.");
                        return;
                    }
                    showKhachHangDialog(kh);
                } catch (RuntimeException ex) {
                    showError("Không thể tải dữ liệu khách hàng: " + ex.getMessage());
                }
            });

            btnXoa.addActionListener(e -> {
                stopCellEditing();
                String maKhachHang = String.valueOf(tableModel.getValueAt(modelRow, 0));
                String hoTen = String.valueOf(tableModel.getValueAt(modelRow, 1));

                int confirm = JOptionPane.showConfirmDialog(table,
                        "Bạn có chắc muốn xóa khách hàng \"" + hoTen + "\" (" + maKhachHang + ")?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                try {
                    boolean ok = khachHangService.xoaKhachHang(maKhachHang);
                    if (!ok) {
                        showError("Xóa thất bại, khách hàng không tồn tại.");
                        return;
                    }
                    loadKhachHangData();
                } catch (RuntimeException ex) {
                    showError("Không thể xóa khách hàng: " + ex.getMessage());
                }
            });

            panel.add(btnSua);
            panel.add(btnXoa);
        }

        private JButton makeIconButton(ImageIcon icon, String tooltip) {
            JButton btn = new JButton();
            if (icon != null) {
                btn.setIcon(icon);
            } else {
                btn.setText(tooltip);
                btn.setForeground(AppColors.SLATE_600);
                btn.setFont(FONT_SMALL);
            }
            btn.setToolTipText(tooltip);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(new EmptyBorder(2, 4, 2, 4));
            return btn;
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int col) {
            modelRow = table.convertRowIndexToModel(row);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "ACT";
        }
    }
}
