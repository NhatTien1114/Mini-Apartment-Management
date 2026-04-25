package ui.main;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import dao.DichVuDAO;
import dao.GiaDetailDAO;
import dao.HopDongDAO;
import dao.HopDongKhachHangDAO;
import dao.PhongDichVuDAO;
import dao.QuanLyPhongDAO;
import entity.DichVu;
import entity.GiaDetail;
import entity.HopDong;
import entity.KhachHang;
import entity.Phong;
import ui.util.*;

public class HopDongUI {
    private static final java.text.NumberFormat VN_MONEY = java.text.NumberFormat
            .getNumberInstance(new Locale("vi", "VN"));

    private final Color MAU_NEN = AppColors.SLATE_50;
    private final Color MAU_CARD = AppColors.WHITE;
    private final Color MAU_TEXT = AppColors.SLATE_900;
    private final Color MAU_SUBTEXT = AppColors.SLATE_500;
    private final Color BORDER_COLOR = AppColors.SLATE_200;

    private final Font FONT_BOLD = new Font("Be Vietnam Pro", Font.BOLD, 14);

    private JPanel pnlRoot;
    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private RoundedTextField txtSearch;
    private JComboBox<String> cboFilterHanHopDong;

    HopDongDAO HopDongDao = new HopDongDAO();
    HopDongKhachHangDAO HDKHdao = new HopDongKhachHangDAO();
    QuanLyPhongDAO PhongDAO = new QuanLyPhongDAO();
    GiaDetailDAO giaDetailDAO = new GiaDetailDAO();

    private PrimaryButton primaryButton = new PrimaryButton();
    private Runnable onContractCreated;

    public void setOnContractCreated(Runnable callback) {
        this.onContractCreated = callback;
    }

    public static class ContractDraft {
        public String phong;
        public String hoTen;
        public String soDienThoai;
        public String cccd;
        public String diaChi;
        public String ngaySinh;
        public String ngayBatDau;
        public String ngayKetThuc;
        public String tienCocRaw;
        public String giaThueRaw;
    }

    public void loadDataToTable() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        ArrayList<HopDong> listHD = HopDongDao.getAllHopDongDangHieuLuc();
        for (HopDong row : listHD) {
            String tenNguoiDaiDien = "";
            entity.KhachHang nguoiDaiDien = HDKHdao
                    .getNguoiDaiDienByMaPhong(String.valueOf(row.getPhong().getMaPhong()));
            if (nguoiDaiDien != null) tenNguoiDaiDien = nguoiDaiDien.getHoTen();
            String trangThaiHienThi = (row.getNgayKetThuc() != null && java.time.LocalDate.now().isAfter(row.getNgayKetThuc()))
                    ? entity.HopDong.TrangThai.DA_KET_THUC.getTen()
                    : (row.getTrangThai() != null ? row.getTrangThai().toString() : "");
            model.addRow(new Object[]{
                    row.getMaHopDong(), row.getPhong().getMaPhong(), tenNguoiDaiDien,
                    row.getNgayBatDau(), row.getNgayKetThuc(),
                    formatMoneyDisplay(row.getTienCoc()), formatMoneyDisplay(row.getTienThueThang()),
                    trangThaiHienThi
            });
        }
    }

    private String formatToDDMMYYYY(Object dateObj) {
        if (dateObj == null || dateObj.toString().trim().isEmpty()) return "";
        String dateStr = dateObj.toString().trim();
        if (dateStr.matches("\\d{2}/\\d{2}/\\d{4}")) return dateStr;
        try {
            if (dateStr.length() > 10) dateStr = dateStr.substring(0, 10);
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return dateStr;
        }
    }

    public JPanel getPanel() {
        pnlRoot = new JPanel(new BorderLayout(0, 24));
        pnlRoot.setBorder(new EmptyBorder(32, 32, 32, 32));
        pnlRoot.setBackground(MAU_NEN);
        table = new JTable();

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Quản lý hợp đồng");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 20));
        lblTitle.setForeground(MAU_TEXT);
        JButton btnAdd = primaryButton.makePrimaryButton("Tạo Hợp Đồng");
        btnAdd.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnAdd.addActionListener(e -> showContractForm(false, -1));
        JPanel pnlHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlHeaderRight.setOpaque(false);
        pnlHeaderRight.add(btnAdd);
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(pnlHeaderRight, BorderLayout.EAST);
        pnlRoot.add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlMainContent = new JPanel(new BorderLayout(0, 20));
        pnlMainContent.setOpaque(false);

        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlToolbar.setOpaque(false);
        txtSearch = new RoundedTextField(8) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(148, 163, 184));
                g2.setStroke(new BasicStroke(1.5f));
                int cx = 18, cy = getHeight() / 2 - 2;
                g2.drawOval(cx - 4, cy - 4, 8, 8);
                g2.drawLine(cx + 2, cy + 2, cx + 7, cy + 7);
                if (isFocusOwner()) {
                    g2.setColor(new Color(37, 99, 235));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                }
                g2.dispose();
            }
        };
        txtSearch.setBorder(new EmptyBorder(8, 36, 8, 12));
        txtSearch.setPlaceholder("Tìm theo tên hoặc phòng...");
        txtSearch.setPreferredSize(new Dimension(280, 40));
        txtSearch.setBackground(Color.WHITE);
        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { txtSearch.repaint(); }
            public void focusLost(FocusEvent e) { txtSearch.repaint(); }
        });

        JButton btnTimKiem = primaryButton.makePrimaryButton("Tìm kiếm");
        btnTimKiem.setPreferredSize(new Dimension(110, 40));
        btnTimKiem.addActionListener(e -> applyContractFilter());

        cboFilterHanHopDong = new JComboBox<>(new String[]{"Tất cả", "Sắp hết hạn", "Đã hết hạn"});
        cboFilterHanHopDong.setPreferredSize(new Dimension(150, 40));
        cboFilterHanHopDong.setFont(new Font("Inter", Font.PLAIN, 14));
        cboFilterHanHopDong.setBackground(Color.WHITE);
        cboFilterHanHopDong.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.ITEM_STATE_CHANGED) applyContractFilter();
        });

        pnlToolbar.add(txtSearch);
        pnlToolbar.add(btnTimKiem);
        pnlToolbar.add(cboFilterHanHopDong);
        pnlMainContent.add(pnlToolbar, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(MAU_CARD);
        card.setLayout(new BorderLayout());
        JPanel cardMargin = new JPanel(new BorderLayout());
        cardMargin.setOpaque(false);
        cardMargin.setBorder(new EmptyBorder(4, 24, 24, 24));

        String[] columnNames = {"Mã HĐ", "Phòng", "Khách thuê", "Ngày bắt đầu", "Ngày kết thúc",
                "Tiền cọc", "Tiền thuê/tháng", "Trạng thái"};
        this.model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        this.table = new JTable(this.model);
        loadDataToTable();
        table = new JTable(model);
        table.setRowHeight(56);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);
        table.setSelectionBackground(new Color(241, 245, 249));
        table.setBorder(null);

        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem miEdit = new JMenuItem("Xem/Sửa thông tin");
        JMenuItem miMembers = new JMenuItem("Quản lý thành viên");
        JMenuItem miThanhToan = new JMenuItem("Thanh toán hợp đồng");
        JMenuItem miDelete = new JMenuItem("Xóa");
        miDelete.setForeground(new Color(239, 68, 68));
        contextMenu.add(miEdit);
        contextMenu.add(miMembers);
        contextMenu.add(miThanhToan);
        contextMenu.add(miDelete);

        miMembers.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int modelRow = table.convertRowIndexToModel(row);
            showMemberManagementDialog(String.valueOf(model.getValueAt(modelRow, 0)),
                    String.valueOf(model.getValueAt(modelRow, 1)));
        });
        miEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) showContractForm(true, table.convertRowIndexToModel(row));
        });
        miThanhToan.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int modelRow = table.convertRowIndexToModel(row);
            showThanhToanHopDongDialog(String.valueOf(model.getValueAt(modelRow, 0)),
                    String.valueOf(model.getValueAt(modelRow, 1)));
        });
        miDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int modelRow = table.convertRowIndexToModel(row);
            String maHopDong = String.valueOf(model.getValueAt(modelRow, 0));
            String tenKhachHang = String.valueOf(model.getValueAt(modelRow, 2));
            String maPhong = String.valueOf(model.getValueAt(modelRow, 1));
            int luaChon = JOptionPane.showConfirmDialog(pnlRoot,
                    "Bạn có chắc chắn muốn xóa hợp đồng của khách hàng '" + tenKhachHang + "' (Phòng " + maPhong + ") không?\n\nCảnh báo: Hành động này sẽ xóa vĩnh viễn dữ liệu và không thể khôi phục!",
                    "Xác nhận xóa hợp đồng", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (luaChon == JOptionPane.YES_OPTION) {
                boolean ok = HopDongDao.xoaHopDongVaKhachHangLienQuan(maHopDong);
                showToast(ok ? "Đã xóa hợp đồng thành công" : "Xóa hợp đồng thất bại");
                if (ok) loadDataToTable();
            }
        });

        table.setComponentPopupMenu(contextMenu);
        table.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row > -1) table.setRowSelectionInterval(row, row);
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) { if (!contextMenu.isVisible()) table.clearSelection(); }
            public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) table.setRowSelectionInterval(row, row);
            }
        });

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyContractFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyContractFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyContractFilter(); }
        });

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setFont(new Font("Inter", Font.BOLD, 13));
                l.setForeground(MAU_SUBTEXT);
                l.setBackground(MAU_CARD);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                        new EmptyBorder(16, 16, 16, 8)));
                return l;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setFont(new Font("Inter", Font.PLAIN, 13));
                l.setForeground(MAU_TEXT);
                l.setBackground(isSel ? t.getSelectionBackground() : MAU_CARD);
                l.setOpaque(true);
                if (c == 0) l.setFont(new Font("Inter", Font.BOLD, 13));
                else if (c == 1) { l.setForeground(new Color(37, 99, 235)); l.setFont(new Font("Inter", Font.BOLD, 13)); }
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                        new EmptyBorder(0, 16, 0, 8)));
                return l;
            }
        });

        table.getColumnModel().getColumn(7).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                JPanel pnl = new JPanel(new GridBagLayout());
                pnl.setBackground(isSel ? t.getSelectionBackground() : MAU_CARD);
                pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));
                String status = v != null ? v.toString() : "";
                boolean active = status.equals("Đang hiệu lực");
                Color bg = active ? new Color(34, 88, 195) : new Color(226, 232, 240);
                Color fg = active ? Color.WHITE : new Color(71, 85, 105);
                JLabel lbl = new JLabel(status) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(bg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                        super.paintComponent(g);
                        g2.dispose();
                    }
                };
                lbl.setForeground(fg);
                lbl.setFont(new Font("Inter", Font.BOLD, 11));
                lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0;
                gbc.insets = new Insets(0, 16, 0, 0);
                pnl.add(lbl, gbc);
                return pnl;
            }
        });

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(125);
        table.getColumnModel().getColumn(2).setCellRenderer(boldPaddedRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(MAU_CARD);
        cardMargin.add(scrollPane, BorderLayout.CENTER);
        card.add(cardMargin, BorderLayout.CENTER);
        pnlMainContent.add(card, BorderLayout.CENTER);
        pnlRoot.add(pnlMainContent, BorderLayout.CENTER);
        return pnlRoot;
    }

    private void applyContractFilter() {
        String text = txtSearch.getText().trim();
        String hanFilter = (String) cboFilterHanHopDong.getSelectedItem();
        java.util.List<RowFilter<Object, Object>> filters = new java.util.ArrayList<>();
        if (!text.isEmpty()) filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        if ("Sắp hết hạn".equals(hanFilter)) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    Object dateVal = entry.getValue(4);
                    if (dateVal == null) return false;
                    try {
                        java.time.LocalDate ngayKetThuc = parseTableDate(dateVal);
                        if (ngayKetThuc == null) return false;
                        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), ngayKetThuc);
                        return daysLeft >= 0 && daysLeft <= 15;
                    } catch (Exception e) { return false; }
                }
            });
        } else if ("Đã hết hạn".equals(hanFilter)) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    Object dateVal = entry.getValue(4);
                    if (dateVal == null) return false;
                    try {
                        java.time.LocalDate ngayKetThuc = parseTableDate(dateVal);
                        return ngayKetThuc != null && ngayKetThuc.isBefore(java.time.LocalDate.now());
                    } catch (Exception e) { return false; }
                }
            });
        }
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private java.time.LocalDate parseTableDate(Object dateVal) {
        if (dateVal instanceof java.time.LocalDate) return (java.time.LocalDate) dateVal;
        String dateStr = dateVal.toString().trim();
        if (dateStr.isEmpty()) return null;
        try {
            if (dateStr.length() > 10) dateStr = dateStr.substring(0, 10);
            return java.time.LocalDate.parse(dateStr);
        } catch (Exception e) {}
        try {
            return java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) { return null; }
    }

    public void showAddContractForm() { showContractForm(false, -1, null); }
    public void showAddContractForm(String preselectedMaPhong) { showContractForm(false, -1, preselectedMaPhong); }

    private TableCellRenderer boldPaddedRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setFont(FONT_BOLD);
                setForeground(AppColors.SLATE_900);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200), new EmptyBorder(0, 16, 0, 8)));
                return this;
            }
        };
    }

    private void showContractForm(boolean isEdit, int row) { showContractForm(isEdit, row, null); }

    private void showContractForm(boolean isEdit, int row, String preselectedMaPhong) {
        Window parent = SwingUtilities.getWindowAncestor(pnlRoot);
        JDialog overlay = new JDialog(parent);
        overlay.setUndecorated(true);
        try { overlay.setBackground(new Color(0, 0, 0, 100)); } catch (Exception e) {}
        if (parent != null) overlay.setBounds(parent.getBounds());
        overlay.setFocusableWindowState(false);

        JDialog dialog = new JDialog(parent, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(620, 560);
        dialog.setLocationRelativeTo(pnlRoot);
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC");
        dialog.getRootPane().getActionMap().put("ESC", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { dialog.dispose(); }
        });

        RoundedPanel pnlBg = new RoundedPanel(16);
        pnlBg.setBackground(Color.WHITE);
        pnlBg.setLayout(new BorderLayout());
        pnlBg.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel pnlHead = new JPanel(new BorderLayout());
        pnlHead.setOpaque(false);
        JLabel lblTitle = new JLabel(isEdit ? "Sửa hợp đồng" : "Tạo hợp đồng mới");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 18));
        lblTitle.setForeground(MAU_TEXT);
        JButton btnClose = new JButton("X") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? MAU_TEXT : MAU_SUBTEXT);
                g2.setStroke(new BasicStroke(1.5f));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.drawLine(cx - 4, cy - 4, cx + 4, cy + 4);
                g2.drawLine(cx - 4, cy + 4, cx + 4, cy - 4);
                g2.dispose();
            }
        };
        btnClose.setPreferredSize(new Dimension(24, 24));
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());
        pnlHead.add(lblTitle, BorderLayout.WEST);
        pnlHead.add(btnClose, BorderLayout.EAST);
        pnlHead.setBorder(new EmptyBorder(0, 0, 16, 0));
        pnlBg.add(pnlHead, BorderLayout.NORTH);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 12));
        pnlContent.setOpaque(false);
        JPanel pnlGrid = new JPanel(new GridLayout(0, 2, 14, 12));
        pnlGrid.setOpaque(false);

        ArrayList<Phong> dsPhongTrong = PhongDAO.getAllPhongTrong();
        String[] roomOptions = new String[dsPhongTrong.size()];
        Map<String, Long> roomPriceByCode = new HashMap<>();
        for (int i = 0; i < dsPhongTrong.size(); i++) {
            Phong room = dsPhongTrong.get(i);
            GiaDetail gd = giaDetailDAO.getDonGiaByMa(room.getMaGiaDetail());
            double donGia = (gd != null) ? gd.getDonGia() : 0.0;
            long giaLamTron = Math.max(0L, Math.round(donGia));
            roomPriceByCode.put(room.getMaPhong(), giaLamTron);
            roomOptions[i] = room.getMaPhong() + " - " + formatCurrencyValue(giaLamTron) + " đ";
        }
        JComboBox<String> cboPhong = FormFieldStyles.createRoomCombo(roomOptions, new Font("Inter", Font.PLAIN, 14), MAU_TEXT, BORDER_COLOR);

        RoundedTextField txtPhongEdit = createFocusableField();
        txtPhongEdit.setEditable(false);
        txtPhongEdit.setBackground(new Color(241, 245, 249));

        RoundedTextField txtKhach = createFocusableField();
        RoundedTextField txtSoDienThoai = createFocusableField();
        RoundedTextField txtCccd = createFocusableField();
        RoundedTextField txtDiaChi = createFocusableField();
        JFormattedTextField txtNgaySinh = createFocusableDateField();
        JFormattedTextField txtKetThuc = createFocusableDateField();
        JFormattedTextField txtBatDau = createFocusableDateFieldWithAutoEndDate(txtKetThuc);
        RoundedTextField txtThue = createFocusableField();
        RoundedTextField txtCoc = createFocusableField();

        txtSoDienThoai.setToolTipText("Ví dụ: 0901234567");
        txtCccd.setToolTipText("Ví dụ: 079123456789");
        applyNumberFilter(txtSoDienThoai);
        applyNumberFilter(txtCccd);

        addValidationOnFocusLost(txtKhach, "^[\\p{L} .'-]{2,}$", "Họ tên không được để trống", "Họ tên phải có ít nhất 2 ký tự");
        addValidationOnFocusLost(txtSoDienThoai, "^0[0-9]{9}$", "Số điện thoại không được để trống", "SĐT phải gồm 10 số và bắt đầu bằng số 0");
        addValidationOnFocusLost(txtCccd, "^([0-9]{9}|[0-9]{12})$", "CCCD/CMND không được để trống", "CCCD phải có 12 số, CMND phải có 9 số");
        addValidationOnFocusLost(txtDiaChi, "^.{3,}$", "Địa chỉ không được để trống", "Địa chỉ không được để trống");
        addDateValidationOnFocusLost(txtNgaySinh, "Ngày sinh");
        addDateValidationOnFocusLost(txtBatDau, "Ngày bắt đầu");
        addDateValidationOnFocusLost(txtKetThuc, "Ngày kết thúc");
        txtThue.setEditable(false);
        txtCoc.setEditable(false);

        ActionListener roomListener = e -> {
            String selected = (String) cboPhong.getSelectedItem();
            String roomCode = extractRoomCode(selected);
            long selectedPrice = roomPriceByCode.getOrDefault(roomCode, 0L);
            txtThue.setText(formatCurrency(String.valueOf(selectedPrice)));
            txtCoc.setText(formatCurrency(String.valueOf(selectedPrice)));
        };
        cboPhong.addActionListener(roomListener);

        if (!isEdit && preselectedMaPhong != null && !preselectedMaPhong.isEmpty()) {
            for (int i = 0; i < cboPhong.getItemCount(); i++) {
                String item = cboPhong.getItemAt(i);
                if (item != null && item.startsWith(preselectedMaPhong + " ")) { cboPhong.setSelectedIndex(i); break; }
            }
        }
        roomListener.actionPerformed(null);

        String[] maKhachHangRef = {null};
        if (isEdit && row != -1) {
            String roomCode = model.getValueAt(row, 1).toString();
            txtPhongEdit.setText(roomCode);
            txtKhach.setText(model.getValueAt(row, 2).toString());
            KhachHang kh = HDKHdao.getNguoiDaiDienByMaPhong(roomCode);
            if (kh != null) {
                maKhachHangRef[0] = kh.getMaKhachHang();
                txtSoDienThoai.setText(kh.getSoDienThoai());
                txtCccd.setText(kh.getSoCCCD());
                txtDiaChi.setText(kh.getDiaChi());
                if (kh.getNgaySinh() != null) txtNgaySinh.setText(formatToDDMMYYYY(kh.getNgaySinh()));
            }
            txtBatDau.setText(formatToDDMMYYYY(model.getValueAt(row, 3)));
            txtKetThuc.setText(formatToDDMMYYYY(model.getValueAt(row, 4)));
            txtCoc.setText(formatCurrency(model.getValueAt(row, 5).toString()));
            txtThue.setText(formatCurrency(model.getValueAt(row, 6).toString()));
        }

        Font labelFont = new Font("Inter", Font.PLAIN, 13);
        if (isEdit) {
            pnlContent.add(FormFieldStyles.createLabeledField("Phòng", txtPhongEdit, MAU_TEXT, labelFont, 52), BorderLayout.NORTH);
            pnlGrid.add(FormFieldStyles.createLabeledField("Họ tên khách thuê *", txtKhach, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Số điện thoại *", txtSoDienThoai, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("CCCD/CMND *", txtCccd, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Địa chỉ *", txtDiaChi, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Ngày bắt đầu *", txtBatDau, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Ngày kết thúc *", txtKetThuc, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Tiền thuê/tháng", txtThue, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Tiền cọc", txtCoc, MAU_TEXT, labelFont, 52));
            pnlContent.add(pnlGrid, BorderLayout.CENTER);
        } else {
            pnlGrid.add(FormFieldStyles.createLabeledField("Phòng *", cboPhong, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Họ tên khách thuê *", txtKhach, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Số điện thoại *", txtSoDienThoai, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("CCCD/CMND *", txtCccd, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Địa chỉ *", txtDiaChi, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Ngày sinh *", txtNgaySinh, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Ngày bắt đầu *", txtBatDau, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Ngày kết thúc *", txtKetThuc, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Tiền thuê/tháng", txtThue, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Tiền cọc", txtCoc, MAU_TEXT, labelFont, 52));
            pnlContent.add(pnlGrid, BorderLayout.CENTER);
        }
        pnlBg.add(pnlContent, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        pnlFooter.setOpaque(false);
        RoundedButton btnCancel = ButtonStyles.createSecondary("Hủy", new Font("Inter", Font.BOLD, 13),
                MAU_TEXT, Color.WHITE, new Color(226, 232, 240), 8, new EmptyBorder(10, 22, 10, 22));
        btnCancel.addActionListener(e -> dialog.dispose());
        JButton btnSave = primaryButton.makePrimaryButton(isEdit ? "Cập Nhật" : "Tạo");
        btnSave.addActionListener(e -> {
            String bDau = txtBatDau.getText().replace("_", "").trim();
            String kThuc = txtKetThuc.getText().replace("_", "").trim();
            String phongCode = isEdit ? txtPhongEdit.getText().trim() : extractRoomCode((String) cboPhong.getSelectedItem());

            if (txtKhach.getText().trim().isEmpty()) { ui.util.ValidationPopup.show(txtKhach, "Họ tên không được để trống"); txtKhach.requestFocus(); return; }
            if (!txtKhach.getText().trim().matches("^[\\p{L} .'-]{2,}$")) { ui.util.ValidationPopup.show(txtKhach, "Họ tên phải có ít nhất 2 ký tự"); txtKhach.requestFocus(); return; }
            String sdtVal = txtSoDienThoai.getText().trim();
            if (sdtVal.isEmpty()) { ui.util.ValidationPopup.show(txtSoDienThoai, "Số điện thoại không được để trống"); txtSoDienThoai.requestFocus(); return; }
            if (!sdtVal.matches("^0[0-9]{9}$")) { ui.util.ValidationPopup.show(txtSoDienThoai, "SĐT phải gồm 10 số và bắt đầu bằng số 0"); txtSoDienThoai.requestFocus(); return; }
            String cccdValue = txtCccd.getText().trim();
            if (cccdValue.isEmpty()) { ui.util.ValidationPopup.show(txtCccd, "CCCD/CMND không được để trống"); txtCccd.requestFocus(); return; }
            if (!cccdValue.matches("^([0-9]{9}|[0-9]{12})$")) { ui.util.ValidationPopup.show(txtCccd, "CCCD phải có 12 số, CMND phải có 9 số"); txtCccd.requestFocus(); return; }
            if (new dao.KhachHangDAO().kiemTraCCCDTonTai(cccdValue, maKhachHangRef[0])) { ui.util.ValidationPopup.show(txtCccd, "CCCD/CMND này đã tồn tại trong hệ thống"); txtCccd.requestFocus(); return; }
            if (txtDiaChi.getText().trim().isEmpty()) { ui.util.ValidationPopup.show(txtDiaChi, "Địa chỉ không được để trống"); txtDiaChi.requestFocus(); return; }

            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(java.time.format.ResolverStyle.STRICT);
            if (!isEdit) {
                String ngaySinhCheck = txtNgaySinh.getText().replace("_", "").replace("/", "").trim();
                if (ngaySinhCheck.isEmpty()) { ui.util.ValidationPopup.show(txtNgaySinh, "Ngày sinh không được để trống"); txtNgaySinh.requestFocus(); return; }
                try { java.time.LocalDate.parse(txtNgaySinh.getText().trim(), fmt); }
                catch (Exception ex) { ui.util.ValidationPopup.show(txtNgaySinh, "Ngày sinh không hợp lệ (dd/MM/yyyy)"); txtNgaySinh.requestFocus(); return; }
            }
            if (bDau.length() < 10 || bDau.replace("/", "").trim().isEmpty()) { ui.util.ValidationPopup.show(txtBatDau, "Ngày bắt đầu không được để trống"); txtBatDau.requestFocus(); return; }
            if (kThuc.length() < 10 || kThuc.replace("/", "").trim().isEmpty()) { ui.util.ValidationPopup.show(txtKetThuc, "Ngày kết thúc không được để trống"); txtKetThuc.requestFocus(); return; }
            try {
                java.time.LocalDate start = java.time.LocalDate.parse(txtBatDau.getText().trim(), fmt);
                java.time.LocalDate end = java.time.LocalDate.parse(txtKetThuc.getText().trim(), fmt);
                if (start.isAfter(end)) { ui.util.ValidationPopup.show(txtKetThuc, "Ngày kết thúc phải sau ngày bắt đầu"); txtKetThuc.requestFocus(); return; }
            } catch (Exception ex) { ui.util.ValidationPopup.show(txtBatDau, "Ngày sai định dạng dd/MM/yyyy"); txtBatDau.requestFocus(); return; }

            if (isEdit) {
                model.setValueAt(phongCode, row, 1);
                model.setValueAt(txtKhach.getText(), row, 2);
                model.setValueAt(txtBatDau.getText(), row, 3);
                model.setValueAt(txtKetThuc.getText(), row, 4);
                model.setValueAt(formatCurrency(txtCoc.getText()), row, 5);
                model.setValueAt(formatCurrency(txtThue.getText()), row, 6);
                showToast("Cập nhật hợp đồng thành công");
                dialog.dispose();
            } else {
                ContractDraft draft = new ContractDraft();
                draft.phong = phongCode;
                draft.hoTen = txtKhach.getText().trim();
                draft.soDienThoai = txtSoDienThoai.getText().trim();
                draft.cccd = txtCccd.getText().trim();
                draft.diaChi = txtDiaChi.getText().trim();
                draft.ngaySinh = txtNgaySinh.getText().replace("_", "").trim();
                draft.ngayBatDau = txtBatDau.getText().trim();
                draft.ngayKetThuc = txtKetThuc.getText().trim();
                long selectedPrice = roomPriceByCode.getOrDefault(phongCode, 0L);
                if (selectedPrice > 0) { draft.tienCocRaw = String.valueOf(selectedPrice); draft.giaThueRaw = String.valueOf(selectedPrice); }
                else { draft.tienCocRaw = sanitizeMoneyRaw(txtCoc.getText().trim()); draft.giaThueRaw = sanitizeMoneyRaw(txtThue.getText().trim()); }
                if (draft.tienCocRaw.isEmpty() || draft.giaThueRaw.isEmpty()) { showToast("Tiền cọc / tiền thuê không hợp lệ"); return; }

                dialog.setVisible(false);
                boolean accepted = showContractPreviewDialog(draft);
                if (accepted) {
                    HopDongDAO dao = new HopDongDAO();
                    boolean success = dao.luuHopDongMoi(draft);
                    if (success) {
                        loadDataToTable();
                        if (onContractCreated != null) onContractCreated.run();
                        showToast("Lưu vào cơ sở dữ liệu thành công!");
                        dialog.dispose();
                    } else {
                        String err = dao.getLastError();
                        showToast((err == null || err.trim().isEmpty()) ? "Lỗi: Không thể lưu vào cơ sở dữ liệu!" : "Lỗi lưu hợp đồng: " + err);
                    }
                } else {
                    dialog.setVisible(true);
                }
            }
        });

        pnlFooter.add(btnCancel);
        pnlFooter.add(btnSave);
        pnlBg.add(pnlFooter, BorderLayout.SOUTH);
        dialog.add(pnlBg);
        if (parent != null) overlay.setVisible(true);
        dialog.setVisible(true);
        overlay.dispose();
    }

    private RoundedTextField createFocusableField() {
        RoundedTextField field = new RoundedTextField(6) {
            boolean focused = false;
            { addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { focused = true; repaint(); }
                public void focusLost(FocusEvent e) { focused = false; repaint(); }
            }); }
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (focused) { g2.setColor(new Color(37, 99, 235)); g2.setStroke(new BasicStroke(2f)); g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 6, 6); }
                g2.dispose();
            }
        };
        field.setFont(new Font("Inter", Font.PLAIN, 15));
        field.setBorder(new EmptyBorder(12, 12, 12, 12));
        return field;
    }

    private JFormattedTextField createFocusableDateField() {
        javax.swing.text.MaskFormatter mask = null;
        try { mask = new javax.swing.text.MaskFormatter("##/##/####"); mask.setPlaceholderCharacter('_'); } catch (Exception e) {}
        JFormattedTextField txt = new JFormattedTextField(mask) {
            boolean focused = false;
            { addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { focused = true; repaint(); }
                public void focusLost(FocusEvent e) { focused = false; repaint(); }
            }); setOpaque(false); }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                super.paintComponent(g);
                g2.dispose();
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (focused) { g2.setColor(new Color(37, 99, 235)); g2.setStroke(new BasicStroke(2f)); }
                else { g2.setColor(new Color(226, 232, 240)); g2.setStroke(new BasicStroke(1f)); }
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 6, 6);
                g2.setColor(new Color(15, 23, 42));
                int cx = getWidth() - 20, cy = getHeight() / 2;
                g2.drawRect(cx - 5, cy - 4, 10, 8);
                g2.drawLine(cx - 3, cy - 6, cx - 3, cy - 4);
                g2.drawLine(cx + 3, cy - 6, cx + 3, cy - 4);
                g2.drawLine(cx - 5, cy - 1, cx + 5, cy - 1);
                g2.drawRect(cx - 2, cy + 2, 1, 1);
                g2.dispose();
            }
        };
        txt.setBorder(new EmptyBorder(12, 12, 12, 36));
        txt.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        txt.setFont(new Font("Inter", Font.PLAIN, 15));
        txt.setForeground(MAU_TEXT);
        txt.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                txt.setCursor(e.getX() > txt.getWidth() - 36 ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.TEXT_CURSOR));
            }
        });
        txt.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getX() > txt.getWidth() - 36) { new DatePickerPopup(txt).show(txt, 0, txt.getHeight()); txt.requestFocus(); }
                else if (txt.getText().equals("__/__/____")) txt.setCaretPosition(0);
            }
        });
        txt.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { check(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { check(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { check(); }
            private void check() {
                SwingUtilities.invokeLater(() -> {
                    String t = txt.getText();
                    if (t.length() < 10) return;
                    String dd = t.substring(0, 2);
                    if (!dd.contains("_")) {
                        int d = Integer.parseInt(dd);
                        if (d < 1 || d > 31) { ui.util.ValidationPopup.show(txt, "Ngày phải từ 01 tới 31"); txt.setText("__" + t.substring(2)); txt.setCaretPosition(0); return; }
                        String mm = t.substring(3, 5);
                        if (!mm.contains("_")) {
                            int m = Integer.parseInt(mm);
                            if (m < 1 || m > 12) { ui.util.ValidationPopup.show(txt, "Tháng phải từ 01 tới 12"); txt.setText(t.substring(0, 3) + "__" + t.substring(5)); txt.setCaretPosition(3); return; }
                            int max = 31;
                            if (m == 4 || m == 6 || m == 9 || m == 11) max = 30;
                            else if (m == 2) {
                                String yyyy = t.substring(6, 10);
                                if (!yyyy.contains("_")) { int y = Integer.parseInt(yyyy); max = (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) ? 29 : 28; }
                                else max = 29;
                            }
                            if (d < 1 || d > max) { ui.util.ValidationPopup.show(txt, "Ngày sai (Tháng " + m + " có tối đa " + max + " ngày)"); txt.setText(t.substring(0, 3) + "__" + t.substring(5)); txt.setCaretPosition(3); }
                        }
                    }
                });
            }
        });
        return txt;
    }

    private JFormattedTextField createFocusableDateFieldWithAutoEndDate(JFormattedTextField txtEndDate) {
        JFormattedTextField txt = createFocusableDateField();
        txt.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { autoFillEndDate(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { autoFillEndDate(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { autoFillEndDate(); }
            private void autoFillEndDate() {
                SwingUtilities.invokeLater(() -> {
                    String t = txt.getText();
                    if (t == null || t.contains("_") || t.length() < 10) return;
                    try {
                        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        java.time.LocalDate start = java.time.LocalDate.parse(t, fmt);
                        txtEndDate.setText(start.plusYears(1).format(fmt));
                    } catch (Exception ignored) {}
                });
            }
        });
        return txt;
    }

    private void applyNumberFilter(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void insertString(FilterBypass fb, int o, String str, AttributeSet attr) throws BadLocationException {
                if (str.matches("\\d+")) super.insertString(fb, o, str, attr);
            }
            public void replace(FilterBypass fb, int o, int len, String text, AttributeSet attr) throws BadLocationException {
                if (text == null || text.matches("\\d+")) super.replace(fb, o, len, text, attr);
            }
        });
    }

    private void addValidationOnFocusLost(JTextField field, String regex, String emptyMessage, String errorMessage) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.isTemporary()) return;
                SwingUtilities.invokeLater(() -> {
                    String val = field.getText().trim();
                    if (val.isEmpty()) ui.util.ValidationPopup.show(field, emptyMessage);
                    else if (!val.matches(regex)) ui.util.ValidationPopup.show(field, errorMessage);
                });
            }
        });
    }

    private void addDateValidationOnFocusLost(JFormattedTextField field, String label) {
        field.setFocusLostBehavior(JFormattedTextField.PERSIST);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.isTemporary()) return;
                SwingUtilities.invokeLater(() -> {
                    String val = field.getText().trim();
                    String raw = val.replace("_", "").replace("/", "").trim();
                    if (raw.isEmpty() || val.equals("__/__/____")) { ui.util.ValidationPopup.show(field, label + " không được để trống"); return; }
                    if (val.contains("_")) { ui.util.ValidationPopup.show(field, label + " chưa nhập đầy đủ (dd/MM/yyyy)"); return; }
                    try { java.time.LocalDate.parse(val, java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT)); }
                    catch (Exception ex) { ui.util.ValidationPopup.show(field, label + " không hợp lệ (dd/MM/yyyy)"); }
                });
            }
        });
    }

    private String extractRoomCode(String roomDisplay) {
        if (roomDisplay == null || roomDisplay.trim().isEmpty()) return "";
        int idx = roomDisplay.indexOf('-');
        return idx <= 0 ? roomDisplay.trim() : roomDisplay.substring(0, idx).trim();
    }

    private String extractRoomPriceRaw(String roomDisplay) {
        if (roomDisplay == null) return "0";
        int idx = roomDisplay.indexOf('-');
        String pricePart = idx >= 0 ? roomDisplay.substring(idx + 1) : roomDisplay;
        String digits = pricePart.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? "0" : digits;
    }

    private void selectRoomByCode(JComboBox<String> combo, String roomCode) {
        if (roomCode == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object itemObj = combo.getItemAt(i);
            if (itemObj != null && itemObj.toString().startsWith(roomCode)) { combo.setSelectedIndex(i); return; }
        }
    }

    public boolean showContractPreviewDialog(ContractDraft draft) {
        Window parent = SwingUtilities.getWindowAncestor(pnlRoot);
        JDialog dialog = new JDialog(parent, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
        int maxW = screenSize.width - screenInsets.left - screenInsets.right - 40;
        int maxH = screenSize.height - screenInsets.top - screenInsets.bottom - 40;
        dialog.setSize(Math.min(1200, maxW), Math.min(920, maxH));
        dialog.setLocationRelativeTo(null);

        final boolean[] accepted = {false};

        RoundedPanel rootPanel = new RoundedPanel(12);
        rootPanel.setBackground(Color.WHITE);
        rootPanel.setLayout(new BorderLayout());
        rootPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel titleLbl = new JLabel("Xem trước hợp đồng", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Inter", Font.BOLD, 20));
        titleLbl.setForeground(MAU_TEXT);
        JButton btnClose = new JButton("X") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? MAU_TEXT : MAU_SUBTEXT);
                g2.setStroke(new BasicStroke(1.5f));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.drawLine(cx - 4, cy - 4, cx + 4, cy + 4);
                g2.drawLine(cx - 4, cy + 4, cx + 4, cy - 4);
                g2.dispose();
            }
        };
        btnClose.setPreferredSize(new Dimension(24, 24));
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());
        head.add(titleLbl, BorderLayout.CENTER);
        head.add(btnClose, BorderLayout.EAST);
        head.setBorder(new EmptyBorder(0, 0, 10, 0));
        rootPanel.add(head, BorderLayout.NORTH);

        DichVuDAO dvAllDAO = new DichVuDAO();
        java.util.List<DichVu> allDV = dvAllDAO.layTatCa();
        PhongDichVuDAO phongDvDAO = new PhongDichVuDAO();
        java.util.List<DichVu> roomDV = phongDvDAO.layDichVuCuaPhong(draft.phong);

        String giaDien = "Theo quy định", giaNuoc = "Theo quy định", phiDichVu = "Theo quy định";
        for (DichVu dv : allDV) {
            String ten = dv.getTenDichVu().toLowerCase();
            if (dv.getDonGia() == null || dv.getDonGia() <= 0) continue;
            String giaStr = formatCurrency(String.valueOf(Math.round(dv.getDonGia()))) + "đ/" + (dv.getDonVi() != null ? dv.getDonVi().toLowerCase() : "");
            if (ten.contains("điện")) giaDien = giaStr;
            else if (ten.contains("nước")) giaNuoc = giaStr;
        }
        for (DichVu dv : roomDV) {
            String ten = dv.getTenDichVu().toLowerCase();
            if (dv.getDonGia() == null || dv.getDonGia() <= 0) continue;
            String giaStr = formatCurrency(String.valueOf(Math.round(dv.getDonGia()))) + "đ/" + (dv.getDonVi() != null ? dv.getDonVi().toLowerCase() : "");
            if (ten.contains("wifi") || ten.contains("dịch vụ") || ten.contains("rác") || ten.contains("vệ sinh") || ten.contains("phí"))
                phiDichVu = giaStr;
        }

        String soThangThue = "";
        try {
            java.time.format.DateTimeFormatter fmtDate = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.LocalDate start = java.time.LocalDate.parse(draft.ngayBatDau, fmtDate);
            java.time.LocalDate end = java.time.LocalDate.parse(draft.ngayKetThuc, fmtDate);
            soThangThue = java.time.temporal.ChronoUnit.MONTHS.between(start, end) + " tháng";
        } catch (Exception ex) { soThangThue = "N/A"; }

        java.time.LocalDate today = java.time.LocalDate.now();
        Font docFont = new Font("Be Vietnam Pro", Font.PLAIN, 13);
        Font docBold = new Font("Be Vietnam Pro", Font.BOLD, 13);
        Font docItalic = new Font("Be Vietnam Pro", Font.ITALIC, 13);

        // ==================== PAGE 1 ====================
        JPanel page1 = createDocPage();
        addDocCenter(page1, "Cộng Hòa Xã Hội Chủ Nghĩa Việt Nam", new Font("Be Vietnam Pro", Font.BOLD, 14));
        addDocCenter(page1, "Độc Lập \u2013 Tự Do \u2013 Hạnh Phúc", docFont);
        addDocCenter(page1, "\u2500oOo\u2500", new Font("Be Vietnam Pro", Font.PLAIN, 12));
        page1.add(Box.createVerticalStrut(14));
        addDocCenter(page1, "HỢP ĐỒNG ĐẶT CỌC", new Font("Be Vietnam Pro", Font.BOLD, 20));
        page1.add(Box.createVerticalStrut(10));
        addDocLine(page1, "- Căn cứ vào khả năng và nhu cầu của hai bên;", docFont, 0);
        addDocHtml(page1, "- Hôm nay, Ngày <b>" + today.getDayOfMonth() + "</b> Tháng <b>" + today.getMonthValue() + "</b> Năm <b>" + today.getYear() + "</b> chúng tôi gồm:", docFont, 0);
        page1.add(Box.createVerticalStrut(8));
        addDocLine(page1, "I. ĐẠI DIỆN BÊN A (Bên nhận cọc)", docBold, 0);
        addDocFill(page1, "Ông/Bà", "Tống Nguyễn Nhật Tiến", docFont);
        addDocHtml(page1, "Số CCCD/Hộ chiếu: <b>079205022180</b> &nbsp;&nbsp; Ngày cấp: <b>31/05/2021</b> &nbsp;&nbsp; Nơi cấp: <b>Cục CS QLHC về TTXH</b>", docFont, 0);
        addDocFill(page1, "Điện thoại", "0357.988.614", docFont);
        page1.add(Box.createVerticalStrut(8));
        addDocLine(page1, "II. ĐẠI DIỆN BÊN B (Bên đặt cọc)", docBold, 0);
        addDocFill(page1, "Ông/Bà", draft.hoTen, docFont);
        if (draft.ngaySinh != null && !draft.ngaySinh.trim().isEmpty()) addDocFill(page1, "Ngày sinh", draft.ngaySinh, docFont);
        addDocFill(page1, "Số CMND/Hộ chiếu", draft.cccd, docFont);
        addDocFill(page1, "Địa chỉ thường trú", draft.diaChi, docFont);
        addDocFill(page1, "Điện Thoại", draft.soDienThoai, docFont);
        page1.add(Box.createVerticalStrut(8));
        addDocLine(page1, "Điều 1 \u2013 Nội dung thỏa thuận", docBold, 0);
        addDocHtml(page1, "<b>1.1.</b>&nbsp;&nbsp;Bằng thỏa thuận này, Bên B đồng ý đặt cọc cho bên A và bên A đồng ý nhận cọc của bên B nhằm mục đích đảm bảo việc giao kết thỏa thuận đặt cọc giữa bên B \nvà chủ đầu tư để thuê sản phẩm với thông tin như sau:", docFont, 0);
        page1.add(Box.createVerticalStrut(4));
        addDocLine(page1, "a. Thông tin phòng:", docBold, 16);
        addDocHtml(page1, "Mã phòng: <b>" + draft.phong + "</b>", docFont, 28);
        addDocHtml(page1, "Giá phòng: <b>" + formatCurrency(draft.giaThueRaw) + " đ</b>", docFont, 28);
        addDocHtml(page1, "Thời hạn thuê: <b>" + soThangThue + "</b>", docFont, 28);
        addDocHtml(page1, "Ngày dự kiến dọn vào: <b>" + draft.ngayBatDau + "</b>", docFont, 28);
        addDocHtml(page1, "Tại địa chỉ: <b>36 Thích Bửu Đăng, P.1, Q. Gò Vấp, TP.HCM</b>", docFont, 28);
        page1.add(Box.createVerticalStrut(4));
        addDocLine(page1, "b. Thông tin phí dịch vụ của tòa nhà:", docBold, 16);
        addDocHtml(page1, "Đơn giá điện: <b>" + giaDien + "</b>", docFont, 28);
        addDocHtml(page1, "Giá nước: <b>" + giaNuoc + "</b>", docFont, 28);
        addDocHtml(page1, "Phí giữ xe máy: <b>100.000đ</b>", docFont, 28);
        addDocHtml(page1, "Phí giữ xe đạp điện: <b>50.000đ</b>", docFont, 28);
        addDocHtml(page1, "Phí dịch vụ (Wifi, rác, vệ sinh): <b>" + phiDichVu + "</b>", docFont, 28);

        // ==================== PAGE 2 ====================
        JPanel page2 = createDocPage();
        addDocHtml(page2, "<b>1.2.</b>&nbsp;&nbsp;Bên A nhận số tiền đặt cọc giữ phòng là: <b>" + formatCurrency(draft.tienCocRaw) + " đ</b>", docFont, 0);
        addDocLine(page2, "Hình thức nhận cọc bằng chuyển khoản với thông tin cụ thể như sau:", docItalic, 16);
        addDocFillIndent(page2, "Ngân hàng", "MBBANK", docFont, 16);
        addDocFillIndent(page2, "Số tài khoản", "0357988614", docFont, 16);
        addDocFillIndent(page2, "Tên tài khoản", "Tống Nguyễn Nhật Tiến", docFont, 16);
        page2.add(Box.createVerticalStrut(8));
        addDocHtml(page2, "<b>1.3.</b>&nbsp;&nbsp;Hai bên cùng thỏa thuận và cam kết rằng:", docFont, 0);
        addDocHtml(page2, "<b>a.</b> Bên B cam kết đến kí hợp đồng thuê nhà và dọn vào ở không quá 2 ngày so với ngày dọn vào dự kiến;", docFont, 16);
        addDocHtml(page2, "<b>b.</b> Bên B cam kết đến kí hợp đồng theo thời gian đã nêu tại điều khoản 1.3.a, đóng đủ tiền nhà và các phí dịch vụ của tòa nhà tháng đầu tiên;", docFont, 16);
        addDocHtml(page2, "<b>c.</b> Nếu quá thời hạn nêu trên mà bên B vẫn không Ký hợp đồng theo như điều 1.3.a nêu trên thì bên B sẽ mất toàn bộ số tiền đặt cọc;", docFont, 16);
        addDocHtml(page2, "<b>d.</b> Trong trường hợp tòa nhà có quy định khách hàng được sang nhượng hợp đồng thuê thì bên B có thể tự sang nhượng hoặc nhờ bên A tìm khách \nsang nhượng trên tinh thần tự nguyện. Bên A không có trách nhiệm bắt buộc sang nhượng cho bên B;", docFont, 16);
        page2.add(Box.createVerticalStrut(10));
        addDocLine(page2, "Điều 2 \u2013 Điều khoản chung:", docBold, 0);
        addDocHtml(page2, "<b>2.1.</b> Thỏa thuận này có hiệu lực từ thời điểm hai bên ký kết. Trường hợp khách hàng đặt cọc bằng hình thức chuyển khoản, hợp đồng này có hiệu lực khi Bên B đã đồng ý \nđặt cọc cho bên A (có xác thực bằng tin nhắn hoặc ghi âm) và đã chuyển khoản tiền cọc cho bên A mà không cần phải có chữ ký của Bên B;", docFont, 0);
        addDocHtml(page2, "<b>2.2.</b> Thỏa thuận này sẽ chấm dứt hiệu lực trong các trường hợp sau: Bên B không ký hợp đồng và dọn vào ở theo quy định tại điều 1.3.b;", docFont, 0);
        addDocHtml(page2, "<b>2.3.</b> Bên A không cho bất kì một bên nào khác bên B đặt cọc hoặc thuê phòng là đối tượng được quy định trên hợp đồng này trong thời hạn thỏa thuận;", docFont, 0);
        addDocHtml(page2, "<b>2.4.</b> Ngay khi bên B ký hợp đồng thuê với chủ đầu tư, toàn bộ số tiền cọc sẽ được chuyển thành tiền ký quỹ trong hợp đồng thuê của Bên B;", docFont, 0);
        page2.add(Box.createVerticalStrut(36));

        JPanel signPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        signPanel.setOpaque(false);
        signPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        signPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        JPanel signA = new JPanel();
        signA.setOpaque(false);
        signA.setLayout(new BoxLayout(signA, BoxLayout.Y_AXIS));
        JLabel lblBenA = new JLabel("ĐẠI DIỆN BÊN A");
        lblBenA.setFont(new Font("Be Vietnam Pro", Font.BOLD, 13));
        lblBenA.setAlignmentX(Component.CENTER_ALIGNMENT);
        signA.add(lblBenA);
        signA.add(Box.createVerticalStrut(6));
        try {
            java.awt.image.BufferedImage sigOrig = javax.imageio.ImageIO.read(new java.io.File("img/signature/chuky.jpg"));
            int sigW = 120, sigH = (int) ((double) sigOrig.getHeight() / sigOrig.getWidth() * sigW);
            JLabel lblSig = new JLabel(new ImageIcon(sigOrig.getScaledInstance(sigW, sigH, Image.SCALE_SMOOTH)));
            lblSig.setAlignmentX(Component.CENTER_ALIGNMENT);
            signA.add(lblSig);
        } catch (Exception ex) { signA.add(Box.createVerticalStrut(26)); }
        signA.add(Box.createVerticalStrut(4));
        JLabel lblNameA = new JLabel("Tống Nguyễn Nhật Tiến");
        lblNameA.setFont(new Font("Be Vietnam Pro", Font.BOLD, 13));
        lblNameA.setAlignmentX(Component.CENTER_ALIGNMENT);
        signA.add(lblNameA);
        JPanel signB = new JPanel();
        signB.setOpaque(false);
        signB.setLayout(new BoxLayout(signB, BoxLayout.Y_AXIS));
        JLabel lblBenB = new JLabel("ĐẠI DIỆN BÊN B");
        lblBenB.setFont(new Font("Be Vietnam Pro", Font.BOLD, 13));
        lblBenB.setAlignmentX(Component.CENTER_ALIGNMENT);
        signB.add(lblBenB);
        signB.add(Box.createVerticalStrut(6));
        JLabel lblSignHint = new JLabel("(Ký, ghi rõ họ tên)");
        lblSignHint.setFont(new Font("Be Vietnam Pro", Font.ITALIC, 11));
        lblSignHint.setForeground(MAU_SUBTEXT);
        lblSignHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        signB.add(lblSignHint);
        signPanel.add(signA);
        signPanel.add(signB);
        page2.add(signPanel);

        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);
        JScrollPane scroll1 = new JScrollPane(page1);
        scroll1.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        scroll1.getVerticalScrollBar().setUnitIncrement(16);
        scroll1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll1.getViewport().setBackground(new Color(245, 245, 245));
        JScrollPane scroll2 = new JScrollPane(page2);
        scroll2.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        scroll2.getVerticalScrollBar().setUnitIncrement(16);
        scroll2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll2.getViewport().setBackground(new Color(245, 245, 245));
        cardPanel.add(scroll1, "1");
        cardPanel.add(scroll2, "2");
        rootPanel.add(cardPanel, BorderLayout.CENTER);

        // ==================== FOOTER ====================
        JPanel footer = new JPanel(new BorderLayout(0, 6));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel lblPageNum = new JLabel("Trang 1 / 2", SwingConstants.CENTER);
        lblPageNum.setFont(new Font("Inter", Font.BOLD, 13));
        lblPageNum.setForeground(MAU_SUBTEXT);
        lblPageNum.setBorder(new EmptyBorder(4, 0, 4, 0));

        JPanel footerBtns = new JPanel(new BorderLayout(8, 0));
        footerBtns.setOpaque(false);

        RoundedButton btnBack = ButtonStyles.createSecondary(
                "\u2190  Quay lại sửa", new Font("Inter", Font.BOLD, 13),
                MAU_TEXT, Color.WHITE, new Color(241, 245, 249), 8,
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(10, 12, 10, 12)));

        JButton btnNext = primaryButton.makePrimaryButton("Xem tiếp  \u2192");
        btnNext.setBorder(new EmptyBorder(10, 16, 10, 16));

        JButton btnConfirm = primaryButton.makePrimaryButton("Xác nhận hợp đồng");
        btnConfirm.setBorder(new EmptyBorder(10, 12, 10, 12));
        btnConfirm.setVisible(false);

        // ── Nút in hợp đồng ──
        JButton btnPrint = new JButton("🖨 In hợp đồng");
        btnPrint.setFont(new Font("Inter", Font.BOLD, 13));
        btnPrint.setFocusPainted(false);
        btnPrint.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrint.setOpaque(true);
        btnPrint.setBackground(Color.WHITE);
        btnPrint.setForeground(new Color(37, 99, 235));
        btnPrint.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(37, 99, 235), 1, true),
                new EmptyBorder(10, 14, 10, 14)));
        btnPrint.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnPrint.setBackground(new Color(239, 246, 255)); }
            @Override public void mouseExited(MouseEvent e) { btnPrint.setBackground(Color.WHITE); }
        });
        btnPrint.addActionListener(e -> inHopDong(page1, page2, dialog));
        // ── Hết phần nút in ──

        final int[] currentPage = {1};

        btnBack.addActionListener(e -> {
            if (currentPage[0] == 2) {
                cardLayout.show(cardPanel, "1");
                currentPage[0] = 1;
                lblPageNum.setText("Trang 1 / 2");
                btnNext.setVisible(true);
                btnConfirm.setVisible(false);
                btnBack.setText("\u2190  Quay lại sửa");
            } else {
                accepted[0] = false;
                dialog.dispose();
            }
        });
        btnNext.addActionListener(e -> {
            cardLayout.show(cardPanel, "2");
            currentPage[0] = 2;
            lblPageNum.setText("Trang 2 / 2");
            btnNext.setVisible(false);
            btnConfirm.setVisible(true);
            btnBack.setText("\u2190  Trang trước");
        });
        btnConfirm.addActionListener(e -> { accepted[0] = true; dialog.dispose(); });

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setOpaque(false);
        rightBtns.add(btnNext);
        rightBtns.add(btnConfirm);

        footerBtns.add(btnBack, BorderLayout.WEST);
        footerBtns.add(btnPrint, BorderLayout.CENTER);
        footerBtns.add(rightBtns, BorderLayout.EAST);

        footer.add(lblPageNum, BorderLayout.NORTH);
        footer.add(footerBtns, BorderLayout.CENTER);
        rootPanel.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(rootPanel);
        dialog.setVisible(true);
        return accepted[0];
    }

    // ── Phần in hợp đồng ──────────────────────────────────────────────────────

    /**
     * In cả 2 trang hợp đồng. OS print dialog cho phép chọn máy in hoặc Save as PDF.
     */
    private void inHopDong(JPanel page1, JPanel page2, JDialog parentDialog) {
        java.util.List<JPanel> pages = java.util.Arrays.asList(page1, page2);

        Printable printable = (graphics, pageFormat, pageIndex) -> {
            if (pageIndex >= pages.size()) return Printable.NO_SUCH_PAGE;
            JPanel page = pages.get(pageIndex);
            page.setSize(new Dimension(794, 1123));
            forceLayout(page);

            double pageW = pageFormat.getImageableWidth();
            double pageH = pageFormat.getImageableHeight();
            double panelW = Math.max(page.getWidth(), 794);
            double panelH = Math.max(page.getHeight(), 1);
            double scale = Math.min(pageW / panelW, pageH / panelH);

            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2.scale(scale, scale);

            boolean wasBuffered = disableDoubleBuffering(page);
            page.print(g2);
            restoreDoubleBuffering(page, wasBuffered);
            return Printable.PAGE_EXISTS;
        };

        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.defaultPage();
        Paper paper = new Paper();
        double a4W = 595.0, a4H = 842.0, margin = 36.0;
        paper.setSize(a4W, a4H);
        paper.setImageableArea(margin, margin, a4W - 2 * margin, a4H - 2 * margin);
        pf.setPaper(paper);
        pf.setOrientation(PageFormat.PORTRAIT);

        Book book = new Book();
        book.append(printable, pf);
        book.append(printable, pf);
        job.setPageable(book);
        job.setJobName("HopDong_" + java.time.LocalDate.now());

        if (job.printDialog()) {
            try {
                job.print();
                showToast("In hợp đồng thành công!");
            } catch (PrinterException ex) {
                ex.printStackTrace();
                showToast("Lỗi khi in: " + ex.getMessage());
            }
        }
    }

    private void forceLayout(java.awt.Container container) {
        container.validate();
        for (java.awt.Component c : container.getComponents()) {
            if (c instanceof java.awt.Container) forceLayout((java.awt.Container) c);
        }
    }

    private boolean disableDoubleBuffering(java.awt.Component c) {
        javax.swing.RepaintManager mgr = javax.swing.RepaintManager.currentManager(c);
        boolean was = mgr.isDoubleBufferingEnabled();
        mgr.setDoubleBufferingEnabled(false);
        return was;
    }

    private void restoreDoubleBuffering(java.awt.Component c, boolean was) {
        javax.swing.RepaintManager.currentManager(c).setDoubleBufferingEnabled(was);
    }

    // ── Hết phần in hợp đồng ──────────────────────────────────────────────────

    private JPanel createDocPage() {
        JPanel page = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                if (getParent() != null) d.width = getParent().getWidth();
                return d;
            }
        };
        page.setBackground(Color.WHITE);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(new EmptyBorder(8, 8, 8, 8), BorderFactory.createLineBorder(new Color(210, 215, 220))),
                new EmptyBorder(28, 36, 28, 36)));
        return page;
    }

    private void addDocCenter(JPanel doc, String text, Font font) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(font); lbl.setForeground(MAU_TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, lbl.getPreferredSize().height));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        doc.add(lbl);
    }

    private void addDocLine(JPanel doc, String text, Font font, int indent) {
        JLabel lbl = new JLabel("<html>" + text + "</html>");
        lbl.setFont(font); lbl.setForeground(MAU_TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        lbl.setBorder(new EmptyBorder(2, indent, 2, 0));
        doc.add(lbl);
    }

    private void addDocHtml(JPanel doc, String html, Font font, int indent) {
        JLabel lbl = new JLabel("<html>" + html + "</html>");
        lbl.setFont(font); lbl.setForeground(MAU_TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        lbl.setBorder(new EmptyBorder(2, indent, 2, 0));
        doc.add(lbl);
    }

    private void addDocFill(JPanel doc, String label, String value, Font font) { addDocFillIndent(doc, label, value, font, 0); }

    private void addDocFillIndent(JPanel doc, String label, String value, Font font, int indent) {
        String safeVal = value != null ? value : "";
        String dots = " ................................................................................";
        int maxDots = Math.max(5, 50 - label.length() - safeVal.length());
        String filling = dots.substring(0, Math.min(maxDots, dots.length()));
        JLabel lbl = new JLabel("<html>" + label + " :" + filling + "<b>" + safeVal + "</b>" + filling.substring(0, Math.min(8, filling.length())) + "</html>");
        lbl.setFont(font); lbl.setForeground(MAU_TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        lbl.setBorder(new EmptyBorder(2, indent, 2, 0));
        doc.add(lbl);
    }

    private String formatCurrency(String num) {
        String digits = sanitizeMoneyRaw(num);
        if (digits.isEmpty()) return "0";
        try { return VN_MONEY.format(Long.parseLong(digits)); } catch (NumberFormatException e) { return "0"; }
    }

    private String formatMoneyDisplay(Object moneyObj) {
        if (moneyObj == null || moneyObj.toString().isEmpty()) return "0 đ";
        try { return VN_MONEY.format(Math.round(Double.parseDouble(moneyObj.toString()))) + " đ"; }
        catch (Exception e) { return "0 đ"; }
    }

    private String sanitizeMoneyRaw(String text) {
        if (text == null) return "";
        return text.replaceAll("[^0-9]", "");
    }

    private String formatCurrencyValue(long amount) { return VN_MONEY.format(amount); }

    private void showThanhToanHopDongDialog(String maHopDong, String maPhong) {
        dao.HopDongDAO hopDongDAO = new dao.HopDongDAO();
        entity.HopDong hd = hopDongDAO.getHopDongByMaHopDong(maHopDong);
        if (hd == null) { showToast("Không tìm thấy hợp đồng."); return; }

        dao.ChiSoDienNuocDAO chiSoDAO = new dao.ChiSoDienNuocDAO();
        dao.DichVuDAO dvDAO2 = new dao.DichVuDAO();
        dao.GiaDetailDAO gdDAO = new dao.GiaDetailDAO();
        dao.PhongDichVuDAO phongDvDAO = new dao.PhongDichVuDAO();
        dao.PhuongTienDAO ptDAO = new dao.PhuongTienDAO();
        dao.HoaDonDAO hoaDonDAO = new dao.HoaDonDAO();

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate ngayBD = hd.getNgayBatDau();
        java.time.LocalDate ngayDauThang = today.withDayOfMonth(1);
        java.time.LocalDate ngayBDHieuQua = ngayBD.isAfter(ngayDauThang) ? ngayBD : ngayDauThang;
        long soNgay = java.time.temporal.ChronoUnit.DAYS.between(ngayBDHieuQua, today) + 1;
        long soNgayTrongThang = today.lengthOfMonth();
        double tienThueThang = hd.getTienThueThang();
        double tienPhongProRated = Math.round(tienThueThang * soNgay / (double) soNgayTrongThang);

        entity.DichVu dvDien = dvDAO2.getDichVuByTen("Điện");
        entity.DichVu dvNuoc = dvDAO2.getDichVuByTen("Nước");
        String maDichVuDien = dvDien != null ? dvDien.getMaDichVu() : null;
        String maDichVuNuoc = dvNuoc != null ? dvNuoc.getMaDichVu() : null;
        final double donGiaDien, donGiaNuoc;
        {
            double tmp = 0;
            if (dvDien != null && dvDien.getMaGiaDetail() != null) { entity.GiaDetail gd = gdDAO.getDonGiaByMa(dvDien.getMaGiaDetail()); if (gd != null) tmp = gd.getDonGia(); }
            donGiaDien = tmp; tmp = 0;
            if (dvNuoc != null && dvNuoc.getMaGiaDetail() != null) { entity.GiaDetail gd = gdDAO.getDonGiaByMa(dvNuoc.getMaGiaDetail()); if (gd != null) tmp = gd.getDonGia(); }
            donGiaNuoc = tmp;
        }

        int[] chiSoCu = chiSoDAO.layChiSoThangTruoc(maPhong, today.getMonthValue(), today.getYear());
        int[] chiSoMoi = chiSoDAO.layChiSoTheoThang(maPhong, today.getMonthValue(), today.getYear());
        int prefillDien = chiSoMoi != null ? chiSoMoi[0] : chiSoCu[0];
        int prefillNuoc = chiSoMoi != null ? chiSoMoi[1] : chiSoCu[1];

        java.util.List<HoaDonUI.BillServiceItem> dichVuKhac = new java.util.ArrayList<>();
        for (entity.DichVu dv : phongDvDAO.layDichVuCuaPhong(maPhong)) {
            if (dv.getTenDichVu() == null) continue;
            String ten = dv.getTenDichVu().toLowerCase();
            if (ten.contains("điện") || ten.contains("nước")) continue;
            HoaDonUI.BillServiceItem si = new HoaDonUI.BillServiceItem();
            si.maDichVu = dv.getMaDichVu(); si.tenKhoan = dv.getTenDichVu(); si.soLuong = 1;
            si.donGia = dv.getDonGia() != null ? Math.round(dv.getDonGia() * soNgay / (double) soNgayTrongThang) : 0;
            dichVuKhac.add(si);
        }
        for (entity.PhuongTien pt : ptDAO.getAllPhuongTien()) {
            if (!maPhong.equals(pt.getMaPhong())) continue;
            HoaDonUI.BillServiceItem si = new HoaDonUI.BillServiceItem();
            si.maDichVu = "DVXE"; si.tenKhoan = "Gửi xe: " + pt.getLoaiXe() + " (" + pt.getBienSo() + ")"; si.soLuong = 1;
            si.donGia = Math.round(pt.getMucPhi() * soNgay / (double) soNgayTrongThang);
            dichVuKhac.add(si);
        }

        final int[] tieuThuD = {0}, tieuThuN = {0};
        final double[] tienDien = {0}, tienNuoc = {0};

        Window parent2 = SwingUtilities.getWindowAncestor(pnlRoot);
        JDialog dialog = new JDialog(parent2, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(520, 540);
        dialog.setLocationRelativeTo(pnlRoot);
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC");
        dialog.getRootPane().getActionMap().put("ESC", new AbstractAction() { public void actionPerformed(ActionEvent ev) { dialog.dispose(); } });

        RoundedPanel pnlBg = new RoundedPanel(16);
        pnlBg.setBackground(Color.WHITE);
        pnlBg.setLayout(new BorderLayout(0, 10));
        pnlBg.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel pnlHead = new JPanel(new BorderLayout());
        pnlHead.setOpaque(false);
        JLabel lblTitle2 = new JLabel("Thanh toán & Kết thúc – Phòng " + maPhong);
        lblTitle2.setFont(new Font("Inter", Font.BOLD, 16));
        lblTitle2.setForeground(MAU_TEXT);
        JButton btnX = new JButton("✕");
        btnX.setFont(new Font("Inter", Font.PLAIN, 14));
        btnX.setBorderPainted(false); btnX.setContentAreaFilled(false);
        btnX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnX.addActionListener(ev -> dialog.dispose());
        pnlHead.add(lblTitle2, BorderLayout.WEST);
        pnlHead.add(btnX, BorderLayout.EAST);

        JPanel pnlBody = new JPanel();
        pnlBody.setOpaque(false);
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        Font fLbl = new Font("Inter", Font.BOLD, 12);
        Font fVal = new Font("Inter", Font.PLAIN, 13);

        java.util.function.BiConsumer<String, String> addRow = (lbl, val) -> {
            JPanel r = new JPanel(new BorderLayout(8, 0));
            r.setOpaque(false); r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            JLabel jl = new JLabel(lbl); jl.setFont(fLbl); jl.setForeground(MAU_SUBTEXT);
            JLabel jv = new JLabel(val); jv.setFont(fVal); jv.setForeground(MAU_TEXT); jv.setHorizontalAlignment(SwingConstants.RIGHT);
            r.add(jl, BorderLayout.WEST); r.add(jv, BorderLayout.EAST);
            pnlBody.add(r); pnlBody.add(Box.createVerticalStrut(5));
        };

        addRow.accept("Kỳ tính:", String.format("%d/%d – %d/%d/%d  (%d/%d ngày)",
                ngayBDHieuQua.getDayOfMonth(), ngayBDHieuQua.getMonthValue(),
                today.getDayOfMonth(), today.getMonthValue(), today.getYear(), soNgay, soNgayTrongThang));

        JLabel lblSec1 = new JLabel("Tiền phòng & Dịch vụ (theo ngày)");
        lblSec1.setFont(new Font("Inter", Font.BOLD, 12)); lblSec1.setForeground(new Color(37, 99, 235));
        lblSec1.setBorder(new EmptyBorder(6, 0, 2, 0)); pnlBody.add(lblSec1);
        addRow.accept("Tiền phòng:", formatMoneyDisplay(tienPhongProRated));
        for (HoaDonUI.BillServiceItem si : dichVuKhac) addRow.accept(si.tenKhoan + ":", formatMoneyDisplay(si.donGia));

        JLabel lblSec2 = new JLabel("Điện / Nước");
        lblSec2.setFont(new Font("Inter", Font.BOLD, 12)); lblSec2.setForeground(new Color(37, 99, 235));
        lblSec2.setBorder(new EmptyBorder(6, 0, 2, 0)); pnlBody.add(lblSec2);
        JLabel lblNote = new JLabel("Nhập chỉ số điện/nước tại thời điểm thanh lý:");
        lblNote.setFont(new Font("Inter", Font.ITALIC, 12)); lblNote.setForeground(new Color(37, 99, 235));
        pnlBody.add(lblNote); pnlBody.add(Box.createVerticalStrut(4));

        JPanel grid = new JPanel(new java.awt.GridLayout(2, 2, 8, 6));
        grid.setOpaque(false); grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        JLabel lD = new JLabel("Điện mới (cũ: " + chiSoCu[0] + " kWh):"); lD.setFont(fLbl);
        JLabel lN = new JLabel("Nước mới (cũ: " + chiSoCu[1] + " m³):"); lN.setFont(fLbl);
        JTextField txtDienMoi = new JTextField(String.valueOf(prefillDien));
        JTextField txtNuocMoi = new JTextField(String.valueOf(prefillNuoc));
        txtDienMoi.setFont(fVal); txtNuocMoi.setFont(fVal);
        grid.add(lD); grid.add(txtDienMoi); grid.add(lN); grid.add(txtNuocMoi);
        pnlBody.add(grid); pnlBody.add(Box.createVerticalStrut(6));

        pnlBody.add(Box.createVerticalStrut(6));
        JSeparator sep2 = new JSeparator(); sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        pnlBody.add(sep2); pnlBody.add(Box.createVerticalStrut(6));

        double tongTamTinh = tienPhongProRated + dichVuKhac.stream().mapToDouble(s -> s.donGia).sum() + tienDien[0] + tienNuoc[0];
        JPanel rowTong = new JPanel(new BorderLayout()); rowTong.setOpaque(false); rowTong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel lTL = new JLabel("TỔNG CỘNG:"); lTL.setFont(new Font("Inter", Font.BOLD, 14));
        JLabel lTV = new JLabel(formatMoneyDisplay(tongTamTinh)); lTV.setFont(new Font("Inter", Font.BOLD, 14));
        lTV.setForeground(new Color(22, 163, 74)); lTV.setHorizontalAlignment(SwingConstants.RIGHT);
        rowTong.add(lTL, BorderLayout.WEST); rowTong.add(lTV, BorderLayout.EAST); pnlBody.add(rowTong);

        JPanel pnlFoot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlFoot.setOpaque(false);
        JButton btnHuy = new JButton("Hủy");
        btnHuy.setFont(new Font("Inter", Font.BOLD, 13)); btnHuy.setBorder(new EmptyBorder(9, 20, 9, 20));
        btnHuy.addActionListener(ev -> dialog.dispose());
        JButton btnXacNhan = primaryButton.makePrimaryButton("Xác nhận thanh toán");
        btnXacNhan.setBorder(new EmptyBorder(9, 16, 9, 16));

        final JTextField fDien = txtDienMoi, fNuoc = txtNuocMoi;
        btnXacNhan.addActionListener(ev -> {
            try {
                int dm = Integer.parseInt(fDien.getText().trim());
                int nm = Integer.parseInt(fNuoc.getText().trim());
                if (dm < chiSoCu[0] || nm < chiSoCu[1]) { showToast("Chỉ số mới không được nhỏ hơn chỉ số cũ."); return; }
                tieuThuD[0] = dm - chiSoCu[0]; tieuThuN[0] = nm - chiSoCu[1];
                tienDien[0] = tieuThuD[0] * donGiaDien; tienNuoc[0] = tieuThuN[0] * donGiaNuoc;
                entity.ChiSoDienNuoc cs = new entity.ChiSoDienNuoc(maPhong, today.getMonthValue(), today.getYear(), today.getDayOfMonth(), dm, nm);
                chiSoDAO.luuHoacCapNhat(cs);
            } catch (NumberFormatException ex) { showToast("Chỉ số điện/nước phải là số nguyên."); return; }

            HoaDonUI.Bill bill = new HoaDonUI.Bill();
            bill.phong = maPhong; bill.daThanhToan = true; bill.tienPhong = tienPhongProRated;
            bill.tongTieuThuD = tieuThuD[0]; bill.donGiaDien = donGiaDien; bill.tienDien = tienDien[0]; bill.maDichVuDien = maDichVuDien;
            bill.tongTieuThuN = tieuThuN[0]; bill.donGiaNuoc = donGiaNuoc; bill.tienNuoc = tienNuoc[0]; bill.maDichVuNuoc = maDichVuNuoc;
            bill.month = String.valueOf(today.getMonthValue()); bill.year = String.valueOf(today.getYear()); bill.dichVuKhac = dichVuKhac;

            boolean savedBill = hoaDonDAO.luuHoaDonKetThucHopDong(bill, ngayBDHieuQua, today, maHopDong);
            boolean ended = hopDongDAO.ketThucHopDong(maHopDong, maPhong);
            dialog.dispose();
            if (savedBill && ended) {
                loadDataToTable();
                if (onContractCreated != null) onContractCreated.run();
                showToast("Thanh toán thành công – Phòng " + maPhong + " đã chuyển về Trống.");
            } else { showToast("Có lỗi xảy ra. Vui lòng kiểm tra lại."); }
        });

        pnlFoot.add(btnHuy); pnlFoot.add(btnXacNhan);
        JScrollPane sp = new JScrollPane(pnlBody);
        sp.setBorder(BorderFactory.createEmptyBorder()); sp.getViewport().setOpaque(false); sp.setOpaque(false);
        pnlBg.add(pnlHead, BorderLayout.NORTH); pnlBg.add(sp, BorderLayout.CENTER); pnlBg.add(pnlFoot, BorderLayout.SOUTH);
        dialog.add(pnlBg);
        dialog.setVisible(true);
    }

    private void showMemberManagementDialog(String maHopDong, String maPhong) {
        Window parent = SwingUtilities.getWindowAncestor(pnlRoot);
        JDialog overlay = new JDialog(parent);
        overlay.setUndecorated(true);
        try { overlay.setBackground(new Color(0, 0, 0, 100)); } catch (Exception e) {}
        if (parent != null) overlay.setBounds(parent.getBounds());
        overlay.setFocusableWindowState(false);

        JDialog dialog = new JDialog(parent, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(680, 520);
        dialog.setLocationRelativeTo(pnlRoot);
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC");
        dialog.getRootPane().getActionMap().put("ESC", new AbstractAction() { public void actionPerformed(ActionEvent e) { dialog.dispose(); } });

        RoundedPanel pnlBg = new RoundedPanel(16);
        pnlBg.setBackground(Color.WHITE);
        pnlBg.setLayout(new BorderLayout());
        pnlBg.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel pnlHead = new JPanel(new BorderLayout());
        pnlHead.setOpaque(false);
        JLabel lblTitle = new JLabel("Quản lý thành viên - Phòng " + maPhong);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 18)); lblTitle.setForeground(MAU_TEXT);
        JButton btnClose = new JButton("X") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? MAU_TEXT : MAU_SUBTEXT);
                g2.setStroke(new BasicStroke(1.5f));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.drawLine(cx - 4, cy - 4, cx + 4, cy + 4);
                g2.drawLine(cx - 4, cy + 4, cx + 4, cy - 4);
                g2.dispose();
            }
        };
        btnClose.setPreferredSize(new Dimension(24, 24));
        btnClose.setBorderPainted(false); btnClose.setContentAreaFilled(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());
        pnlHead.add(lblTitle, BorderLayout.WEST); pnlHead.add(btnClose, BorderLayout.EAST);
        pnlHead.setBorder(new EmptyBorder(0, 0, 16, 0));
        pnlBg.add(pnlHead, BorderLayout.NORTH);

        String[] cols = {"", "Họ tên", "SĐT", "CCCD", "Vai trò"};
        DefaultTableModel memberModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable memberTable = new JTable(memberModel);
        memberTable.setRowHeight(48); memberTable.setShowGrid(false);
        memberTable.setIntercellSpacing(new Dimension(0, 0)); memberTable.setFocusable(false);
        memberTable.setSelectionBackground(new Color(241, 245, 249));
        memberTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        memberTable.getColumnModel().getColumn(0).setMinWidth(0); memberTable.getColumnModel().getColumn(0).setMaxWidth(0); memberTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        memberTable.getColumnModel().getColumn(1).setPreferredWidth(160); memberTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        memberTable.getColumnModel().getColumn(3).setPreferredWidth(130); memberTable.getColumnModel().getColumn(4).setPreferredWidth(120);

        memberTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setFont(new Font("Inter", Font.BOLD, 13)); l.setForeground(MAU_SUBTEXT); l.setBackground(MAU_CARD);
                l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR), new EmptyBorder(12, 12, 12, 8)));
                return l;
            }
        });

        memberTable.getColumnModel().getColumn(4).setCellRenderer(new TableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                JPanel pnl = new JPanel(new GridBagLayout());
                pnl.setBackground(isSel ? t.getSelectionBackground() : MAU_CARD);
                pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));
                String role = v != null ? v.toString() : "";
                Color bg, fg;
                if (role.contains("Đại Diện")) { bg = new Color(34, 88, 195); fg = Color.WHITE; }
                else if (role.contains("rời")) { bg = new Color(239, 68, 68); fg = Color.WHITE; }
                else { bg = new Color(226, 232, 240); fg = new Color(71, 85, 105); }
                JLabel lbl = new JLabel(role) {
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                        super.paintComponent(g); g2.dispose();
                    }
                };
                lbl.setForeground(fg); lbl.setFont(new Font("Inter", Font.BOLD, 11)); lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
                GridBagConstraints gbc = new GridBagConstraints(); gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0; gbc.insets = new Insets(0, 12, 0, 0);
                pnl.add(lbl, gbc); return pnl;
            }
        });

        memberTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setFont(new Font("Inter", Font.PLAIN, 13)); l.setForeground(MAU_TEXT);
                l.setBackground(isSel ? t.getSelectionBackground() : MAU_CARD);
                int modelRow = t.convertRowIndexToModel(r);
                if (String.valueOf(memberModel.getValueAt(modelRow, 4)).contains("rời")) l.setForeground(new Color(148, 163, 184));
                l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)), new EmptyBorder(0, 12, 0, 8)));
                return l;
            }
        });

        Runnable loadMembers = () -> {
            memberModel.setRowCount(0);
            ArrayList<entity.HopDongKhachThue> dsTV = HDKHdao.getAllThanhVienByMaHopDong(maHopDong);
            for (entity.HopDongKhachThue hdkt : dsTV) {
                entity.KhachHang kh = hdkt.getKhachHang();
                memberModel.addRow(new Object[]{hdkt.getMaHDKT(), kh.getHoTen(),
                        kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "",
                        kh.getSoCCCD() != null ? kh.getSoCCCD() : "", hdkt.getVaiTro().getTen()});
            }
        };
        loadMembers.run();

        JScrollPane scrollMembers = new JScrollPane(memberTable);
        scrollMembers.setBorder(BorderFactory.createEmptyBorder());
        scrollMembers.getViewport().setBackground(MAU_CARD);
        RoundedPanel cardMembers = new RoundedPanel(12);
        cardMembers.setBackground(MAU_CARD); cardMembers.setLayout(new BorderLayout());
        cardMembers.setBorder(new EmptyBorder(4, 0, 4, 0)); cardMembers.add(scrollMembers, BorderLayout.CENTER);
        pnlBg.add(cardMembers, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        pnlFooter.setOpaque(false);

        JButton btnRoiDi = primaryButton.makePrimaryButton("Đánh dấu rời đi");
        btnRoiDi.setBackground(new Color(239, 68, 68)); btnRoiDi.setBorder(new EmptyBorder(10, 18, 10, 18));
        btnRoiDi.addActionListener(e -> {
            int selRow = memberTable.getSelectedRow();
            if (selRow < 0) { showToast("Vui lòng chọn một thành viên"); return; }
            int mRow = memberTable.convertRowIndexToModel(selRow);
            String maHDKT = String.valueOf(memberModel.getValueAt(mRow, 0));
            String tenKH = String.valueOf(memberModel.getValueAt(mRow, 1));
            String vaiTro = String.valueOf(memberModel.getValueAt(mRow, 4));
            if (vaiTro.contains("rời")) { showToast("Thành viên này đã rời đi rồi"); return; }
            boolean laDaiDien = vaiTro.contains("Đại Diện");
            int soConLai = 0;
            ArrayList<String[]> danhSachConLai = new ArrayList<>();
            for (int i = 0; i < memberModel.getRowCount(); i++) {
                String vt = String.valueOf(memberModel.getValueAt(i, 4));
                String hdkt = String.valueOf(memberModel.getValueAt(i, 0));
                String ten = String.valueOf(memberModel.getValueAt(i, 1));
                if (!vt.contains("rời") && !hdkt.equals(maHDKT)) { soConLai++; danhSachConLai.add(new String[]{hdkt, ten}); }
            }
            if (laDaiDien && soConLai == 0) {
                int confirm = JOptionPane.showConfirmDialog(dialog, "'" + tenKH + "' là người đại diện duy nhất.\nĐánh dấu rời đi sẽ khiến hợp đồng không còn người đại diện.\n\nBạn có chắc chắn muốn tiếp tục?", "Cảnh báo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
                boolean ok = HDKHdao.thanhVienRoiDi(maHDKT, maPhong);
                if (ok) { loadMembers.run(); loadDataToTable(); showToast("Đã đánh dấu '" + tenKH + "' rời đi"); } else showToast("Thao tác thất bại");
                return;
            }
            if (laDaiDien) {
                String[] options = danhSachConLai.stream().map(a -> a[1]).toArray(String[]::new);
                String chosen = (String) JOptionPane.showInputDialog(dialog, "'" + tenKH + "' là người đại diện.\nVui lòng chọn người đại diện mới:", "Chọn người đại diện mới", JOptionPane.QUESTION_MESSAGE, null, options, options.length > 0 ? options[0] : null);
                if (chosen == null) return;
                String maHDKT_Moi = danhSachConLai.stream().filter(a -> a[1].equals(chosen)).map(a -> a[0]).findFirst().orElse(null);
                boolean ok = HDKHdao.roiDiVaDoiDaiDien(maHDKT, maHopDong, maHDKT_Moi, maPhong);
                if (ok) { loadMembers.run(); loadDataToTable(); showToast("Đã đánh dấu '" + tenKH + "' rời đi và '" + chosen + "' là người đại diện mới"); } else showToast("Thao tác thất bại");
            } else {
                int confirm = JOptionPane.showConfirmDialog(dialog, "Bạn có chắc chắn muốn đánh dấu '" + tenKH + "' đã rời đi?", "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
                boolean ok = HDKHdao.thanhVienRoiDi(maHDKT, maPhong);
                if (ok) { loadMembers.run(); loadDataToTable(); showToast("Đã đánh dấu '" + tenKH + "' rời đi"); } else showToast("Thao tác thất bại");
            }
        });

        JButton btnDoiDaiDien = primaryButton.makePrimaryButton("Đổi người đại diện");
        btnDoiDaiDien.setBorder(new EmptyBorder(10, 18, 10, 18));
        btnDoiDaiDien.addActionListener(e -> {
            ArrayList<String[]> danhSachTV = new ArrayList<>();
            for (int i = 0; i < memberModel.getRowCount(); i++) {
                String vt = String.valueOf(memberModel.getValueAt(i, 4));
                if (!vt.contains("Đại Diện") && !vt.contains("rời"))
                    danhSachTV.add(new String[]{String.valueOf(memberModel.getValueAt(i, 0)), String.valueOf(memberModel.getValueAt(i, 1))});
            }
            if (danhSachTV.isEmpty()) { showToast("Không có thành viên nào khả dụng để đổi"); return; }
            String[] options = danhSachTV.stream().map(a -> a[1]).toArray(String[]::new);
            String chosen = (String) JOptionPane.showInputDialog(dialog, "Chọn người đại diện mới:", "Đổi người đại diện", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (chosen == null) return;
            String maHDKT_Moi = danhSachTV.stream().filter(a -> a[1].equals(chosen)).map(a -> a[0]).findFirst().orElse(null);
            boolean ok = HDKHdao.doiNguoiDaiDien(maHopDong, maHDKT_Moi);
            if (ok) { loadMembers.run(); loadDataToTable(); showToast("Đã đổi người đại diện thành '" + chosen + "'"); } else showToast("Thao tác thất bại");
        });

        RoundedButton btnDong = ButtonStyles.createSecondary("Đóng", new Font("Inter", Font.BOLD, 13),
                MAU_TEXT, Color.WHITE, new Color(226, 232, 240), 8, new EmptyBorder(10, 22, 10, 22));
        btnDong.addActionListener(e -> dialog.dispose());
        pnlFooter.add(btnDoiDaiDien); pnlFooter.add(btnRoiDi); pnlFooter.add(btnDong);
        pnlBg.add(pnlFooter, BorderLayout.SOUTH);
        dialog.add(pnlBg);
        if (parent != null) overlay.setVisible(true);
        dialog.setVisible(true);
        overlay.dispose();
    }

    private void showToast(String message) {
        Window parent = SwingUtilities.getWindowAncestor(pnlRoot);
        if (parent == null) return;
        JDialog toast = new JDialog(parent);
        toast.setUndecorated(true);
        try { toast.setBackground(new Color(0, 0, 0, 0)); } catch (Exception e) {}
        toast.setFocusableWindowState(false);
        toast.setAlwaysOnTop(true);
        RoundedPanel pnl = new RoundedPanel(8);
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 16, 10, 16));
        pnl.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JLabel lblIcon = new JLabel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MAU_TEXT); g2.fillOval(0, 2, 16, 16);
                g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(4, 10, 7, 13); g2.drawLine(7, 13, 11, 7); g2.dispose();
            }
        };
        lblIcon.setPreferredSize(new Dimension(16, 20));
        JLabel lblMsg = new JLabel(message);
        lblMsg.setFont(new Font("Inter", Font.BOLD, 13)); lblMsg.setForeground(MAU_TEXT);
        pnl.add(lblIcon); pnl.add(lblMsg);
        toast.add(pnl); toast.pack();
        toast.setLocation(parent.getX() + parent.getWidth() - toast.getWidth() - 32,
                parent.getY() + parent.getHeight() - toast.getHeight() - 32);
        toast.setVisible(true);
        Timer t = new Timer(2500, e -> toast.dispose()); t.setRepeats(false); t.start();
    }

    class DatePickerPopup extends JPopupMenu {
        private Calendar calTracker = Calendar.getInstance();
        private JPanel daysPanel;
        private JLabel lblMonthYear;
        private JTextField targetField;

        public DatePickerPopup(JTextField targetField) {
            this.targetField = targetField;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(12, 16, 12, 16)));
            JPanel pnlHead = new JPanel(new BorderLayout()); pnlHead.setOpaque(false);
            lblMonthYear = new JLabel("", SwingConstants.LEFT); lblMonthYear.setFont(new Font("Inter", Font.BOLD, 14));
            JPanel pnlArrows = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); pnlArrows.setOpaque(false);
            JButton btnPrev = createArrBtn("←"); JButton btnNext = createArrBtn("→");
            btnPrev.addActionListener(e -> { calTracker.add(Calendar.MONTH, -1); updateCal(); });
            btnNext.addActionListener(e -> { calTracker.add(Calendar.MONTH, 1); updateCal(); });
            pnlArrows.add(btnPrev); pnlArrows.add(btnNext);
            pnlHead.add(lblMonthYear, BorderLayout.CENTER); pnlHead.add(pnlArrows, BorderLayout.EAST);
            add(pnlHead, BorderLayout.NORTH);
            JPanel pnlGrid = new JPanel(new BorderLayout(0, 8)); pnlGrid.setOpaque(false); pnlGrid.setBorder(new EmptyBorder(12, 0, 12, 0));
            JPanel pnlDOW = new JPanel(new GridLayout(1, 7, 4, 4)); pnlDOW.setOpaque(false);
            for (String d : new String[]{"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"}) {
                JLabel l = new JLabel(d, SwingConstants.CENTER); l.setFont(new Font("Inter", Font.PLAIN, 12)); l.setForeground(MAU_SUBTEXT); pnlDOW.add(l);
            }
            pnlGrid.add(pnlDOW, BorderLayout.NORTH);
            daysPanel = new JPanel(new GridLayout(6, 7, 4, 4)); daysPanel.setOpaque(false);
            pnlGrid.add(daysPanel, BorderLayout.CENTER); add(pnlGrid, BorderLayout.CENTER);
            JPanel pnlFooter = new JPanel(new BorderLayout()); pnlFooter.setOpaque(false);
            JButton btnClear = createTxtBtn("Clear"); JButton btnToday = createTxtBtn("Today");
            btnClear.addActionListener(e -> { targetField.setText("__/__/____"); setVisible(false); });
            btnToday.addActionListener(e -> { targetField.setText(fmtStr(Calendar.getInstance())); setVisible(false); });
            pnlFooter.add(btnClear, BorderLayout.WEST); pnlFooter.add(btnToday, BorderLayout.EAST);
            add(pnlFooter, BorderLayout.SOUTH);
            updateCal();
        }

        private void updateCal() {
            daysPanel.removeAll();
            Calendar cal = (Calendar) calTracker.clone();
            lblMonthYear.setText(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.US) + " " + cal.get(Calendar.YEAR));
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, -(cal.get(Calendar.DAY_OF_WEEK) - 1));
            for (int i = 0; i < 42; i++) {
                int d = cal.get(Calendar.DAY_OF_MONTH);
                boolean cM = cal.get(Calendar.MONTH) == calTracker.get(Calendar.MONTH);
                JButton b = new JButton(String.valueOf(d));
                b.setFont(new Font("Inter", Font.PLAIN, 12)); b.setForeground(cM ? MAU_TEXT : new Color(148, 163, 184));
                b.setBackground(Color.WHITE); b.setBorderPainted(false); b.setContentAreaFilled(false);
                b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setMargin(new Insets(2, 2, 2, 2));
                b.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { b.setContentAreaFilled(true); b.setBackground(new Color(241, 245, 249)); }
                    public void mouseExited(MouseEvent e) { b.setContentAreaFilled(false); }
                });
                Calendar cap = (Calendar) cal.clone();
                b.addActionListener(e -> { targetField.setText(fmtStr(cap)); setVisible(false); });
                daysPanel.add(b);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            daysPanel.revalidate(); daysPanel.repaint();
        }

        private String fmtStr(Calendar c) {
            return String.format("%02d/%02d/%04d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
        }
        private JButton createArrBtn(String t) {
            JButton b = new JButton(t); b.setFont(new Font("Inter", Font.BOLD, 14)); b.setForeground(MAU_SUBTEXT);
            b.setBorderPainted(false); b.setContentAreaFilled(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
        }
        private JButton createTxtBtn(String t) {
            JButton b = new JButton(t); b.setFont(new Font("Inter", Font.PLAIN, 12)); b.setForeground(new Color(37, 99, 235));
            b.setBorderPainted(false); b.setContentAreaFilled(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
        }
    }
}