package ui.main;

import entity.Chu;
import entity.DichVu;
import entity.GiaDetail;
import entity.GiaHeader;
import entity.QuanLy;
import entity.TaiKhoan;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import service.BangGiaService;
import ui.util.AppColors;
import ui.util.PrimaryButton;

public class BangGiaUI {

    private static class ComboItem {
        private final String value;
        private final String label;

        ComboItem(String value, String label) {
            this.value = value;
            this.label = label;
        }

        String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class AddContext {
        final int loai;
        final JTextField txtNgayBatDau = new JTextField();
        final JTextField txtNgayKetThuc = new JTextField();
        final JTextField txtMoTa = new JTextField();
        final JComboBox<String> cbTrangThai = new JComboBox<>(new String[] { "Đang áp dụng", "Ngừng áp dụng" });
        final DefaultTableModel modelDetail;
        final JTable tableDetail;
        List<ComboItem> comboItems = new ArrayList<>();

        AddContext(int loai) {
            this.loai = loai;
            String[] columns = loai == 0
                    ? new String[] { "Loại phòng", "Đơn giá" }
                    : new String[] { "Dịch vụ", "Đơn giá" };
            this.modelDetail = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }
            };
            this.tableDetail = new JTable(modelDetail);
            this.tableDetail.setRowHeight(26);
        }
    }

    private final java.awt.Font FONT_TITLE = new java.awt.Font("Be Vietnam Pro", java.awt.Font.BOLD, 22);
    private final java.awt.Font FONT_PLAIN = new java.awt.Font("Be Vietnam Pro", java.awt.Font.PLAIN, 13);
    private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PrimaryButton primaryButtonHelper = new PrimaryButton();
    private final BangGiaService bangGiaService = new BangGiaService();
    private final TaiKhoan taiKhoan;
    private final String tenNguoiCapNhat;
    private final ImageIcon penIcon = loadActionIcon("img/icons/pen.png");
    private final ImageIcon binIcon = loadActionIcon("img/icons/bin.png");

    private JTable tableTongHop;
    private DefaultTableModel modelTongHop;
    private List<GiaHeader> headers = new ArrayList<>();

    public BangGiaUI() {
        this(null);
    }

    public BangGiaUI(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        this.tenNguoiCapNhat = resolveTenNguoiCapNhat();
    }

    public JPanel getPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.SLATE_100);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(AppColors.SLATE_100);
        topBar.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Bảng giá");
        title.setFont(FONT_TITLE);
        title.setForeground(AppColors.SLATE_900);
        topBar.add(title, BorderLayout.WEST);

        JButton btnAdd = primaryButtonHelper.makePrimaryButton("Thêm giá");
        btnAdd.addActionListener(e -> showAddPriceDialog());
        topBar.add(btnAdd, BorderLayout.EAST);

        root.add(topBar, BorderLayout.NORTH);
        root.add(createTongHopSection(), BorderLayout.CENTER);

        loadTongHop();

        // Reload data when tab is shown
        root.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadTongHop();
            }
        });

        return root;
    }

    private JPanel createTongHopSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(AppColors.WHITE);
        section.setBorder(new LineBorder(AppColors.SLATE_200, 1, true));

        modelTongHop = new DefaultTableModel(
                new String[] { "Mã bảng giá", "Người tạo", "Ngày bắt đầu", "Ngày kết thúc" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableTongHop = new JTable(modelTongHop);
        tableTongHop.setFont(FONT_PLAIN);
        tableTongHop.setRowHeight(28);
        tableTongHop.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- Right-click context menu ---
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem miEdit = new JMenuItem("Xem/Sửa thông tin");
        JMenuItem miDelete = new JMenuItem("Xóa");
        miDelete.setForeground(new java.awt.Color(239, 68, 68));
        contextMenu.add(miEdit);
        contextMenu.add(miDelete);

        miEdit.addActionListener(ev -> {
            int row = tableTongHop.getSelectedRow();
            if (row >= 0 && row < headers.size())
                showDetailDialog(headers.get(row));
        });
        miDelete.addActionListener(ev -> {
            int row = tableTongHop.getSelectedRow();
            if (row < 0 || row >= headers.size())
                return;
            GiaHeader h = headers.get(row);
            int confirm = JOptionPane.showConfirmDialog(null,
                    "Xóa bảng giá " + h.getMaGiaHeader() + "?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                String err = bangGiaService.xoaHeader(h.getMaGiaHeader());
                if (err != null) {
                    JOptionPane.showMessageDialog(null, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                loadTongHop();
            }
        });

        tableTongHop.setComponentPopupMenu(contextMenu);

        JScrollPane scroll = new JScrollPane(tableTongHop);
        scroll.setBorder(null);
        section.add(scroll, BorderLayout.CENTER);
        return section;
    }

    private void loadTongHop() {
        headers = bangGiaService.layTatCaHeader();
        modelTongHop.setRowCount(0);

        for (GiaHeader h : headers) {
            modelTongHop.addRow(new Object[] {
                    h.getMaGiaHeader(),
                    h.getGhiChu(),
                    formatDate(h.getNgayBatDau()),
                    formatDate(h.getNgayKetThuc())
            });
        }
    }

    private ImageIcon loadActionIcon(String path) {
        ImageIcon raw = new ImageIcon(path);
        Image scaled = raw.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void showDetailDialog(GiaHeader header) {
        Window owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        JDialog dlg = new JDialog(owner, "Chi tiết " + header.getMaGiaHeader(), Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(620, 430);
        dlg.setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(AppColors.WHITE);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblTitle = new JLabel("Chi tiết bảng giá " + header.getMaGiaHeader());
        lblTitle.setFont(new java.awt.Font("Be Vietnam Pro", java.awt.Font.BOLD, 17));
        root.add(lblTitle, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new String[] { "Loại", "Đơn giá" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(FONT_PLAIN);

        for (GiaDetail d : bangGiaService.layDetailTheoHeader(header.getMaGiaHeader())) {
            model.addRow(new Object[] { resolveTenLoai(header.getLoai(), d), d.getDonGia() });
        }

        root.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(8, 0));
        south.setBackground(AppColors.WHITE);

        JTextField txtNgayKetThuc = new JTextField(formatDate(header.getNgayKetThuc()));
        south.add(makeField("Ngày kết thúc (yyyy-MM-dd, để trống nếu không có)", txtNgayKetThuc), BorderLayout.CENTER);

        JButton btnSave = primaryButtonHelper.makePrimaryButton("Lưu ngày kết thúc");
        btnSave.addActionListener(e -> {
            LocalDate ketThuc;
            try {
                ketThuc = parseDate(txtNgayKetThuc.getText().trim(), true);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Sai định dạng", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String err = bangGiaService.capNhatNgayKetThuc(header.getMaGiaHeader(), ketThuc, tenNguoiCapNhat);
            if (err != null) {
                JOptionPane.showMessageDialog(dlg, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            loadTongHop();
            dlg.dispose();
        });

        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.setBackground(AppColors.WHITE);
        btnWrap.add(btnSave, BorderLayout.EAST);
        south.add(btnWrap, BorderLayout.SOUTH);

        root.add(south, BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private void showAddPriceDialog() {
        Window owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        JDialog dlg = new JDialog(owner, "Thêm giá", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(760, 560);
        dlg.setLocationRelativeTo(owner);

        AddContext phongCtx = new AddContext(0);
        AddContext dichVuCtx = new AddContext(1);
        loadComboOptions(phongCtx);
        loadComboOptions(dichVuCtx);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FONT_PLAIN);
        tabs.add("Phòng", buildAddPanel(phongCtx, dlg));
        tabs.add("Dịch vụ", buildAddPanel(dichVuCtx, dlg));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.SLATE_100);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.add(tabs, BorderLayout.CENTER);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private JPanel buildAddPanel(AddContext ctx, JDialog parentDlg) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(AppColors.SLATE_100);

        JPanel headerForm = new JPanel();
        headerForm.setLayout(new BoxLayout(headerForm, BoxLayout.Y_AXIS));
        headerForm.setBackground(AppColors.WHITE);
        headerForm.setBorder(new LineBorder(AppColors.SLATE_200, 1, true));

        ctx.txtNgayBatDau.setText(LocalDate.now().format(DATE_FMT));

        String lblLoai = ctx.loai == 0 ? "Phòng" : "Dịch vụ";
        headerForm.add(makeField("Loại", new JLabel(lblLoai)));
        headerForm.add(makeField("Ngày bắt đầu (yyyy-MM-dd)", ctx.txtNgayBatDau));
        headerForm.add(makeField("Ngày kết thúc (yyyy-MM-dd, có thể để trống)", ctx.txtNgayKetThuc));
        headerForm.add(makeField("Mô tả", ctx.txtMoTa));
        headerForm.add(makeField("Trạng thái", ctx.cbTrangThai));

        panel.add(headerForm, BorderLayout.NORTH);

        JPanel detailSection = new JPanel(new BorderLayout());
        detailSection.setBackground(AppColors.WHITE);
        detailSection.setBorder(new LineBorder(AppColors.SLATE_200, 1, true));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actionRow.setBackground(AppColors.WHITE);

        JButton btnAddRow = new JButton("+ Thêm dòng");
        btnAddRow.addActionListener(e -> {
            if (ctx.comboItems.isEmpty()) {
                JOptionPane.showMessageDialog(parentDlg, "Không có dữ liệu để chọn", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            ctx.modelDetail.addRow(new Object[] { ctx.comboItems.get(0), 0d });
        });

        JButton btnDeleteRow = new JButton("Xóa dòng");
        btnDeleteRow.addActionListener(e -> {
            int row = ctx.tableDetail.getSelectedRow();
            if (row >= 0) {
                ctx.modelDetail.removeRow(row);
            }
        });

        JButton btnUpdatePrice = primaryButtonHelper.makePrimaryButton("Cập nhật giá");
        btnUpdatePrice.addActionListener(e -> saveNewPrice(ctx, parentDlg));

        actionRow.add(btnAddRow);
        actionRow.add(btnDeleteRow);
        actionRow.add(btnUpdatePrice);

        detailSection.add(actionRow, BorderLayout.NORTH);
        detailSection.add(new JScrollPane(ctx.tableDetail), BorderLayout.CENTER);

        panel.add(detailSection, BorderLayout.CENTER);
        return panel;
    }

    private void loadComboOptions(AddContext ctx) {
        List<ComboItem> items = new ArrayList<>();
        if (ctx.loai == 0) {
            for (BangGiaService.LoaiPhongItem p : bangGiaService.layDanhSachLoaiPhong()) {
                items.add(new ComboItem(String.valueOf(p.getId()), p.getTen()));
            }
        } else {
            for (DichVu d : bangGiaService.layDanhSachDichVu()) {
                items.add(new ComboItem(d.getMaDichVu(), d.toString()));
            }
        }

        ctx.comboItems = items;
        ctx.tableDetail.getColumnModel().getColumn(0).setCellEditor(
                new DefaultCellEditor(new JComboBox<>(items.toArray(ComboItem[]::new))));
        ctx.tableDetail.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof ComboItem item) {
                    super.setValue(item.toString());
                    return;
                }
                super.setValue(value);
            }
        });
    }

    private void saveNewPrice(AddContext ctx, JDialog parentDlg) {
        if (ctx.tableDetail.isEditing()) {
            ctx.tableDetail.getCellEditor().stopCellEditing();
        }

        LocalDate batDau;
        LocalDate ketThuc;
        try {
            batDau = parseDate(ctx.txtNgayBatDau.getText().trim(), false);
            ketThuc = parseDate(ctx.txtNgayKetThuc.getText().trim(), true);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(parentDlg, ex.getMessage(), "Sai định dạng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<BangGiaService.LuuChiTietItem> items = new ArrayList<>();
        for (int i = 0; i < ctx.modelDetail.getRowCount(); i++) {
            ComboItem combo = toComboItem(ctx, ctx.modelDetail.getValueAt(i, 0));
            if (combo == null) {
                JOptionPane.showMessageDialog(parentDlg,
                        "Dòng " + (i + 1) + " chưa chọn loại",
                        "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double donGia;
            try {
                donGia = parseDonGia(ctx.modelDetail.getValueAt(i, 1));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parentDlg,
                        "Đơn giá dòng " + (i + 1) + " không hợp lệ",
                        "Sai dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            items.add(new BangGiaService.LuuChiTietItem(combo.getValue(), donGia));
        }

        int trangThai = ctx.cbTrangThai.getSelectedIndex() == 0 ? 1 : 0;
        String err = bangGiaService.taoBangGiaDayDu(
                ctx.loai,
                batDau,
                ketThuc,
                ctx.txtMoTa.getText().trim(),
                trangThai,
                items,
                tenNguoiCapNhat);

        if (err != null) {
            JOptionPane.showMessageDialog(parentDlg, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        loadTongHop();
        JOptionPane.showMessageDialog(parentDlg, "Đã lưu bảng giá", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        parentDlg.dispose();
    }

    private ComboItem toComboItem(AddContext ctx, Object value) {
        if (value instanceof ComboItem item) {
            return item;
        }
        if (value == null) {
            return null;
        }
        String text = value.toString();
        for (ComboItem item : ctx.comboItems) {
            if (item.getValue().equals(text) || item.toString().equals(text)) {
                return item;
            }
        }
        return null;
    }

    private String resolveTenLoai(int loai, GiaDetail detail) {
        if (loai == 0) {
            Integer lp = detail.getLoaiPhong();
            if (lp == null) {
                return "";
            }
            List<BangGiaService.LoaiPhongItem> ds = bangGiaService.layDanhSachLoaiPhong();
            if (lp >= 0 && lp < ds.size()) {
                return ds.get(lp).getTen();
            }
            return String.valueOf(lp);
        }

        String maDichVu = detail.getMaDichVu();
        if (maDichVu == null) {
            return "";
        }
        for (DichVu d : bangGiaService.layDanhSachDichVu()) {
            if (maDichVu.equals(d.getMaDichVu())) {
                return d.toString();
            }
        }
        return maDichVu;
    }

    private JPanel makeField(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(AppColors.WHITE);
        p.setBorder(new EmptyBorder(0, 10, 10, 10));
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_PLAIN);
        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private LocalDate parseDate(String text, boolean allowBlank) {
        if (text == null || text.isBlank()) {
            if (allowBlank) {
                return null;
            }
            throw new IllegalArgumentException("Ngày bắt đầu không được để trống");
        }
        try {
            return LocalDate.parse(text, DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày phải theo định dạng yyyy-MM-dd");
        }
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FMT);
    }

    private double parseDonGia(Object value) {
        if (value == null) {
            throw new NumberFormatException("Empty value");
        }
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return Double.parseDouble(value.toString().trim().replace(",", ""));
    }

    private String resolveTenNguoiCapNhat() {
        if (taiKhoan == null) {
            return "Không xác định";
        }
        if (taiKhoan instanceof Chu c) {
            return c.getHoTen() + " (" + c.getMaTaiKhoan() + ")";
        }
        if (taiKhoan instanceof QuanLy ql) {
            return ql.getHoTen() + " (" + ql.getMaTaiKhoan() + ")";
        }
        return taiKhoan.getEmail() + " (" + taiKhoan.getMaTaiKhoan() + ")";
    }
}
