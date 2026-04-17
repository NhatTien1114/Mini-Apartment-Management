package ui.main;

import java.awt.*;
import java.awt.event.*;
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
            if (nguoiDaiDien != null) {
                tenNguoiDaiDien = nguoiDaiDien.getHoTen();
            }
            model.addRow(new Object[] {
                    row.getMaHopDong(),
                    row.getPhong().getMaPhong(),
                    tenNguoiDaiDien,
                    row.getNgayBatDau(),
                    row.getNgayKetThuc(),
                    formatMoneyDisplay(row.getTienCoc()),
                    formatMoneyDisplay(row.getTienThueThang()),
                    row.getTrangThai()

            });

        }
    }

    private String formatToDDMMYYYY(Object dateObj) {
        if (dateObj == null || dateObj.toString().trim().isEmpty()) {
            return "";
        }
        String dateStr = dateObj.toString().trim();

        // Nếu đã đúng chuẩn dd/MM/yyyy thì trả về luôn
        if (dateStr.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return dateStr;
        }

        // Nếu là chuẩn SQL yyyy-MM-dd thì convert lại
        try {
            // Cắt lấy phần ngày (nếu có kèm giờ phút giây yyyy-MM-dd HH:mm:ss)
            if (dateStr.length() > 10) {
                dateStr = dateStr.substring(0, 10);
            }
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return date.format(formatter);
        } catch (Exception e) {
            return dateStr; // Trả về chuỗi gốc nếu không thể parse
        }
    }

    public JPanel getPanel() {
        pnlRoot = new JPanel(new BorderLayout(0, 24));
        pnlRoot.setBorder(new EmptyBorder(32, 32, 32, 32));
        pnlRoot.setBackground(MAU_NEN);
        table = new JTable();

        // --- HEADER ---
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

        // --- MAIN CONTENT ---
        JPanel pnlMainContent = new JPanel(new BorderLayout(0, 20));
        pnlMainContent.setOpaque(false);

        // --- Search Bar ---
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
                int cx = 18;
                int cy = getHeight() / 2 - 2;
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
            public void focusGained(FocusEvent e) {
                txtSearch.repaint();
            }

            public void focusLost(FocusEvent e) {
                txtSearch.repaint();
            }
        });

        JButton btnTimKiem = primaryButton.makePrimaryButton("Tìm kiếm");
        btnTimKiem.setPreferredSize(new Dimension(110, 40));
        btnTimKiem.addActionListener(e -> applyContractFilter());

        cboFilterHanHopDong = new JComboBox<>(new String[] { "Tất cả", "Sắp hết hạn", "Đã hết hạn" });
        cboFilterHanHopDong.setPreferredSize(new Dimension(150, 40));
        cboFilterHanHopDong.setFont(new Font("Inter", Font.PLAIN, 14));
        cboFilterHanHopDong.setBackground(Color.WHITE);
        cboFilterHanHopDong.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.ITEM_STATE_CHANGED) {
                applyContractFilter();
            }
        });

        pnlToolbar.add(txtSearch);
        pnlToolbar.add(btnTimKiem);
        pnlToolbar.add(cboFilterHanHopDong);
        pnlMainContent.add(pnlToolbar, BorderLayout.NORTH);

        // --- Data Table Card ---
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(MAU_CARD);
        card.setLayout(new BorderLayout());

        JPanel cardMargin = new JPanel(new BorderLayout());
        cardMargin.setOpaque(false);
        cardMargin.setBorder(new EmptyBorder(4, 24, 24, 24));

        String[] columnNames = { "Mã HĐ", "Phòng", "Khách thuê", "Ngày bắt đầu", "Ngày kết thúc", "Tiền cọc",
                "Tiền thuê/tháng", "Trạng thái" };

        this.model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table = new JTable(this.model); // Sử dụng biến class

        loadDataToTable();

        table = new JTable(model);
        table.setRowHeight(56);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);
        table.setSelectionBackground(new Color(241, 245, 249));
        table.setBorder(null);

        // --- Right-click context menu (must be created before mouse listeners) ---
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem miEdit = new JMenuItem("Xem/Sửa thông tin");
        JMenuItem miMembers = new JMenuItem("Quản lý thành viên");
        JMenuItem miDelete = new JMenuItem("Xóa");
        miDelete.setForeground(new Color(239, 68, 68));
        contextMenu.add(miEdit);
        contextMenu.add(miMembers);
        contextMenu.add(miDelete);

        miMembers.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            int modelRow = table.convertRowIndexToModel(row);
            String maHopDong = String.valueOf(model.getValueAt(modelRow, 0));
            String maPhong = String.valueOf(model.getValueAt(modelRow, 1));
            showMemberManagementDialog(maHopDong, maPhong);
        });

        miEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0)
                showContractForm(true, table.convertRowIndexToModel(row));
        });
        miDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            int modelRow = table.convertRowIndexToModel(row);
            String maHopDong = String.valueOf(model.getValueAt(modelRow, 0));
            String tenKhachHang = String.valueOf(model.getValueAt(modelRow, 2));
            String maPhong = String.valueOf(model.getValueAt(modelRow, 1));
            int luaChon = JOptionPane.showConfirmDialog(
                    pnlRoot,
                    "Bạn có chắc chắn muốn xóa hợp đồng của khách hàng '" + tenKhachHang + "' (Phòng " + maPhong
                            + ") không?\n\nCảnh báo: Hành động này sẽ xóa vĩnh viễn dữ liệu và không thể khôi phục!",
                    "Xác nhận xóa hợp đồng",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (luaChon == JOptionPane.YES_OPTION) {
                boolean ok = HopDongDao.xoaHopDongVaKhachHangLienQuan(maHopDong);
                if (ok) {
                    loadDataToTable();
                    showToast("Đã xóa hợp đồng thành công");
                } else {
                    showToast("Xóa hợp đồng thất bại");
                }
            }
        });

        table.setComponentPopupMenu(contextMenu);

        table.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row > -1) {
                    table.setRowSelectionInterval(row, row);
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                if (!contextMenu.isVisible()) {
                    table.clearSelection();
                }
            }

            public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                }
            }
        });

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyContractFilter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyContractFilter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyContractFilter();
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r,
                    int c) {
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
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r,
                    int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setFont(new Font("Inter", Font.PLAIN, 13));
                l.setForeground(MAU_TEXT);
                l.setBackground(isSel ? t.getSelectionBackground() : MAU_CARD);
                l.setOpaque(true);

                if (c == 0) {
                    l.setFont(new Font("Inter", Font.BOLD, 13));
                } else if (c == 1) {
                    l.setForeground(new Color(37, 99, 235));
                    l.setFont(new Font("Inter", Font.BOLD, 13));
                }

                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                        new EmptyBorder(0, 16, 0, 8)));
                return l;
            }
        });

        table.getColumnModel().getColumn(7).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r,
                    int c) {
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

        if (!text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }

        if ("Sắp hết hạn".equals(hanFilter)) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    Object dateVal = entry.getValue(4); // Ngày kết thúc
                    if (dateVal == null)
                        return false;
                    try {
                        java.time.LocalDate ngayKetThuc = parseTableDate(dateVal);
                        if (ngayKetThuc == null)
                            return false;
                        java.time.LocalDate today = java.time.LocalDate.now();
                        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, ngayKetThuc);
                        return daysLeft >= 0 && daysLeft <= 15;
                    } catch (Exception e) {
                        return false;
                    }
                }
            });
        } else if ("Đã hết hạn".equals(hanFilter)) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    Object dateVal = entry.getValue(4); // Ngày kết thúc
                    if (dateVal == null)
                        return false;
                    try {
                        java.time.LocalDate ngayKetThuc = parseTableDate(dateVal);
                        if (ngayKetThuc == null)
                            return false;
                        return ngayKetThuc.isBefore(java.time.LocalDate.now());
                    } catch (Exception e) {
                        return false;
                    }
                }
            });
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private java.time.LocalDate parseTableDate(Object dateVal) {
        if (dateVal instanceof java.time.LocalDate) {
            return (java.time.LocalDate) dateVal;
        }
        String dateStr = dateVal.toString().trim();
        if (dateStr.isEmpty())
            return null;
        // Try yyyy-MM-dd
        try {
            if (dateStr.length() > 10)
                dateStr = dateStr.substring(0, 10);
            return java.time.LocalDate.parse(dateStr);
        } catch (Exception e) {
            // ignore
        }
        // Try dd/MM/yyyy
        try {
            return java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return null;
        }
    }

    public void showAddContractForm() {
        showContractForm(false, -1);
    }

    private TableCellRenderer boldPaddedRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setFont(FONT_BOLD);
                setForeground(AppColors.SLATE_900);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200),
                        new EmptyBorder(0, 16, 0, 8)));
                return this;
            }
        };
    }

    private void showContractForm(boolean isEdit, int row) {
        Window parent = SwingUtilities.getWindowAncestor(pnlRoot);

        JDialog overlay = new JDialog(parent);
        overlay.setUndecorated(true);
        try {
            overlay.setBackground(new Color(0, 0, 0, 100));
        } catch (Exception e) {
        }
        if (parent != null)
            overlay.setBounds(parent.getBounds());
        overlay.setFocusableWindowState(false);

        JDialog dialog = new JDialog(parent, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(620, 560);
        dialog.setLocationRelativeTo(pnlRoot);

        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC");
        dialog.getRootPane().getActionMap().put("ESC", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
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
        JComboBox<String> cboPhong = FormFieldStyles.createRoomCombo(
                roomOptions,
                new Font("Inter", Font.PLAIN, 14),
                MAU_TEXT,
                BORDER_COLOR);

        RoundedTextField txtPhongEdit = createFocusableField();
        txtPhongEdit.setEditable(false); // Không cho phép sửa mã phòng khi đang edit hợp đồng
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

        addValidationOnFocusLost(txtKhach, "^[\\p{L} .'-]{2,}$", "Họ tên phải có ít nhất 2 ký tự");
        addValidationOnFocusLost(txtSoDienThoai, "^0[0-9]{9}$", "Số điện thoại không hợp lệ (VD: 0901234567)");
        addValidationOnFocusLost(txtCccd, "^([0-9]{9}|[0-9]{12})$", "CCCD phải có 12 số, CMND phải có 9 số");
        addValidationOnFocusLost(txtDiaChi, "^.{3,}$", "Địa chỉ không được để trống");
        addDateValidationOnFocusLost(txtNgaySinh, "Ngày sinh");
        addDateValidationOnFocusLost(txtBatDau, "Ngày bắt đầu");
        addDateValidationOnFocusLost(txtKetThuc, "Ngày kết thúc");

        txtThue.setEditable(false);
        txtCoc.setEditable(false);

        ActionListener roomListener = e -> {
            String selected = (String) cboPhong.getSelectedItem();
            String roomCode = extractRoomCode(selected);
            long selectedPrice = roomPriceByCode.getOrDefault(roomCode, 0L);
            String rawPrice = String.valueOf(selectedPrice);
            txtThue.setText(formatCurrency(rawPrice));
            txtCoc.setText(formatCurrency(rawPrice));

        };
        cboPhong.addActionListener(roomListener);
        roomListener.actionPerformed(null);

        if (isEdit && row != -1) {
            String roomCode = model.getValueAt(row, 1).toString();
            txtPhongEdit.setText(roomCode);
            txtKhach.setText(model.getValueAt(row, 2).toString());
            KhachHang kh = HDKHdao.getNguoiDaiDienByMaPhong(roomCode);
            if (kh != null) {
                txtSoDienThoai.setText(kh.getSoDienThoai());
                txtCccd.setText(kh.getSoCCCD());
                txtDiaChi.setText(kh.getDiaChi());
                // Lấy và format ngày sinh (Giả sử hàm getNgaySinh() trả về String, Date hoặc
                // LocalDate)
                if (kh.getNgaySinh() != null) {
                    txtNgaySinh.setText(formatToDDMMYYYY(kh.getNgaySinh()));
                }
            }
            // Lấy và format ngày bắt đầu, ngày kết thúc từ Table Model
            Object objBatDau = model.getValueAt(row, 3);
            Object objKetThuc = model.getValueAt(row, 4);

            txtBatDau.setText(formatToDDMMYYYY(objBatDau));
            txtKetThuc.setText(formatToDDMMYYYY(objKetThuc));
            txtCoc.setText(formatCurrency(model.getValueAt(row, 5).toString()));
            txtThue.setText(formatCurrency(model.getValueAt(row, 6).toString()));
        }

        Font labelFont = new Font("Inter", Font.PLAIN, 13);
        if (isEdit) {
            JPanel pnlPhong = FormFieldStyles.createLabeledField("Phòng", txtPhongEdit, MAU_TEXT, labelFont, 52);
            pnlContent.add(pnlPhong, BorderLayout.NORTH);
            pnlGrid.add(FormFieldStyles.createLabeledField("Họ tên khách thuê", txtKhach, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Số điện thoại", txtSoDienThoai, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("CCCD/CMND", txtCccd, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Địa chỉ", txtDiaChi, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Ngày bắt đầu", txtBatDau, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Ngày kết thúc", txtKetThuc, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Tiền thuê/tháng", txtThue, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Tiền cọc", txtCoc, MAU_TEXT, labelFont, 52));

            pnlContent.add(pnlGrid, BorderLayout.CENTER);

        } else {
            pnlGrid.add(FormFieldStyles.createLabeledField("Phòng", cboPhong, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Họ tên khách thuê", txtKhach, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Số điện thoại", txtSoDienThoai, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("CCCD/CMND", txtCccd, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Địa chỉ", txtDiaChi, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Ngày sinh", txtNgaySinh, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Ngày bắt đầu", txtBatDau, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Ngày kết thúc", txtKetThuc, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Tiền thuê/tháng", txtThue, MAU_TEXT, labelFont, 52));
            pnlGrid.add(FormFieldStyles.createLabeledField("Tiền cọc", txtCoc, MAU_TEXT, labelFont, 52));

            pnlContent.add(pnlGrid, BorderLayout.CENTER);
        }

        pnlBg.add(pnlContent, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        pnlFooter.setOpaque(false);

        RoundedButton btnCancel = ButtonStyles.createSecondary(
                "Hủy",
                new Font("Inter", Font.BOLD, 13),
                MAU_TEXT,
                Color.WHITE,
                new Color(226, 232, 240),
                8,
                new EmptyBorder(10, 22, 10, 22));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = primaryButton.makePrimaryButton(isEdit ? "Cập Nhật" : "Tạo");

        btnSave.addActionListener(e -> {
            String ngaySinhRaw = "";
            if (!isEdit) {
                ngaySinhRaw = txtNgaySinh.getText().replace("_", "").trim();
            }
            String bDau = txtBatDau.getText().replace("_", "").trim();
            String kThuc = txtKetThuc.getText().replace("_", "").trim();
            String phongCode;
            if (isEdit) {
                phongCode = txtPhongEdit.getText().trim();
            } else {
                String phongDisplay = (String) cboPhong.getSelectedItem();
                phongCode = extractRoomCode(phongDisplay);
            }

            // ---------------------------

            if (phongCode.isEmpty() || txtKhach.getText().trim().isEmpty() || txtSoDienThoai.getText().trim().isEmpty()
                    ||
                    txtCccd.getText().trim().isEmpty() || txtDiaChi.getText().trim().isEmpty() ||
                    bDau.length() < 10 || kThuc.length() < 10) {
                showToast("Vui lòng nhập đầy đủ thông tin");
                return;
            }
            String cccdValue = txtCccd.getText().trim();
            String regexCMND = "^[0-9]{9}$";
            String regexCCCD = "^[0-9]{12}$";

            if (!cccdValue.matches(regexCMND) && !cccdValue.matches(regexCCCD)) {
                showToast("CCCD/CMND không hợp lệ! Vui lòng nhập lại 9 số đối với CMND và 12 số đối với CCCD.");
                txtCccd.requestFocus();
                return;
            }

            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(java.time.format.ResolverStyle.STRICT);
            try {
                if (!isEdit && !ngaySinhRaw.isEmpty()) {
                    java.time.LocalDate.parse(txtNgaySinh.getText(), fmt);
                }
                java.time.LocalDate start = java.time.LocalDate.parse(txtBatDau.getText(), fmt);
                java.time.LocalDate end = java.time.LocalDate.parse(txtKetThuc.getText(), fmt);
                if (start.isAfter(end)) {
                    showToast("Ngày bắt đầu không được lớn hơn ngày kết thúc");
                    return;
                }
            } catch (Exception ex) {
                showToast("Ngày sai định dạng dd/MM/yyyy");
                return;
            }

            String cCoc = formatCurrency(txtCoc.getText());
            String cThue = formatCurrency(txtThue.getText());

            if (isEdit) {
                model.setValueAt(phongCode, row, 1);
                model.setValueAt(txtKhach.getText(), row, 2);
                model.setValueAt(txtBatDau.getText(), row, 3);
                model.setValueAt(txtKetThuc.getText(), row, 4);
                model.setValueAt(cCoc, row, 5);
                model.setValueAt(cThue, row, 6);
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
                if (selectedPrice > 0) {
                    draft.tienCocRaw = String.valueOf(selectedPrice);
                    draft.giaThueRaw = String.valueOf(selectedPrice);
                } else {
                    draft.tienCocRaw = sanitizeMoneyRaw(txtCoc.getText().trim());
                    draft.giaThueRaw = sanitizeMoneyRaw(txtThue.getText().trim());
                }

                if (draft.tienCocRaw.isEmpty() || draft.giaThueRaw.isEmpty()) {
                    showToast("Tiền cọc / tiền thuê không hợp lệ");
                    return;
                }

                dialog.setVisible(false);
                boolean accepted = showContractPreviewDialog(draft);
                if (accepted) {
                    HopDongDAO dao = new HopDongDAO();
                    boolean success = dao.luuHopDongMoi(draft); // Gọi hàm lưu vào DB

                    if (success) {
                        loadDataToTable();
                        if (onContractCreated != null) {
                            onContractCreated.run();
                        }
                        showToast("Lưu vào cơ sở dữ liệu thành công!");
                        dialog.dispose();
                    } else {
                        String err = dao.getLastError();
                        if (err == null || err.trim().isEmpty()) {
                            showToast("Lỗi: Không thể lưu vào cơ sở dữ liệu!");
                        } else {
                            showToast("Lỗi lưu hợp đồng: " + err);
                        }
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

        if (parent != null)
            overlay.setVisible(true);
        dialog.setVisible(true);
        overlay.dispose();
    }

    private RoundedTextField createFocusableField() {
        RoundedTextField field = new RoundedTextField(6) {
            boolean focused = false;
            {
                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        focused = true;
                        repaint();
                    }

                    public void focusLost(FocusEvent e) {
                        focused = false;
                        repaint();
                    }
                });
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (focused) {
                    g2.setColor(new Color(37, 99, 235));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 6, 6);
                }
                g2.dispose();
            }
        };
        field.setFont(new Font("Inter", Font.PLAIN, 15));
        field.setBorder(new EmptyBorder(12, 12, 12, 12));
        return field;
    }

    private JFormattedTextField createFocusableDateField() {
        javax.swing.text.MaskFormatter mask = null;
        try {
            mask = new javax.swing.text.MaskFormatter("##/##/####");
            mask.setPlaceholderCharacter('_');
        } catch (Exception e) {
        }

        JFormattedTextField txt = new JFormattedTextField(mask) {
            boolean focused = false;
            {
                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        focused = true;
                        repaint();
                    }

                    public void focusLost(FocusEvent e) {
                        focused = false;
                        repaint();
                    }
                });
                setOpaque(false);
            }

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
                if (focused) {
                    g2.setColor(new Color(37, 99, 235));
                    g2.setStroke(new BasicStroke(2f));
                } else {
                    g2.setColor(new Color(226, 232, 240));
                    g2.setStroke(new BasicStroke(1f));
                }
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 6, 6);

                g2.setColor(new Color(15, 23, 42));
                int cx = getWidth() - 20;
                int cy = getHeight() / 2;
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
                txt.setCursor(e.getX() > txt.getWidth() - 36 ? new Cursor(Cursor.HAND_CURSOR)
                        : new Cursor(Cursor.TEXT_CURSOR));
            }
        });
        txt.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getX() > txt.getWidth() - 36) {
                    new DatePickerPopup(txt).show(txt, 0, txt.getHeight());
                    txt.requestFocus();
                } else {
                    if (txt.getText().equals("__/__/____")) {
                        txt.setCaretPosition(0);
                    }
                }
            }
        });

        txt.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                check();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                check();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                check();
            }

            private void check() {
                SwingUtilities.invokeLater(() -> {
                    String t = txt.getText();
                    if (t.length() < 10)
                        return;
                    String dd = t.substring(0, 2);
                    if (!dd.contains("_")) {
                        int d = Integer.parseInt(dd);
                        if (d < 1 || d > 31) {
                            ui.util.ValidationPopup.show(txt, "Ngày phải từ 01 tới 31");
                            txt.setText("__" + t.substring(2));
                            txt.setCaretPosition(0);
                            return;
                        }
                        String mm = t.substring(3, 5);
                        if (!mm.contains("_")) {
                            int m = Integer.parseInt(mm);
                            if (m < 1 || m > 12) {
                                ui.util.ValidationPopup.show(txt, "Tháng phải từ 01 tới 12");
                                txt.setText(t.substring(0, 3) + "__" + t.substring(5));
                                txt.setCaretPosition(3);
                                return;
                            }
                            int max = 31;
                            if (m == 4 || m == 6 || m == 9 || m == 11)
                                max = 30;
                            else if (m == 2) {
                                String yyyy = t.substring(6, 10);
                                if (!yyyy.contains("_")) {
                                    int y = Integer.parseInt(yyyy);
                                    max = (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) ? 29 : 28;
                                } else {
                                    max = 29;
                                }
                            }
                            if (d < 1 || d > max) {
                                ui.util.ValidationPopup.show(txt,
                                        "Ngày sai (Tháng " + m + " có tối đa " + max + " ngày)");
                                txt.setText(t.substring(0, 3) + "__" + t.substring(5));
                                txt.setCaretPosition(3);
                            }
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
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                autoFillEndDate();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                autoFillEndDate();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                autoFillEndDate();
            }

            private void autoFillEndDate() {
                SwingUtilities.invokeLater(() -> {
                    String t = txt.getText();
                    if (t == null || t.contains("_") || t.length() < 10)
                        return;
                    try {
                        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                                .ofPattern("dd/MM/yyyy");
                        java.time.LocalDate start = java.time.LocalDate.parse(t, fmt);
                        java.time.LocalDate end = start.plusYears(1);
                        txtEndDate.setText(end.format(fmt));
                    } catch (Exception ignored) {
                    }
                });
            }
        });
        return txt;
    }

    private void applyNumberFilter(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void insertString(FilterBypass fb, int o, String str, AttributeSet attr)
                    throws BadLocationException {
                if (str.matches("\\d+"))
                    super.insertString(fb, o, str, attr);
            }

            public void replace(FilterBypass fb, int o, int len, String text, AttributeSet attr)
                    throws BadLocationException {
                if (text == null || text.matches("\\d+"))
                    super.replace(fb, o, len, text, attr);
            }
        });
    }

    private void addValidationOnFocusLost(JTextField field, String regex, String errorMessage) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String val = field.getText().trim();
                if (!val.isEmpty() && !val.matches(regex)) {
                    ui.util.ValidationPopup.show(field, errorMessage);
                }
            }
        });
    }

    private void addDateValidationOnFocusLost(JFormattedTextField field, String label) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String val = field.getText().trim();
                if (val.isEmpty() || val.equals("__/__/____")) return;
                if (val.contains("_")) {
                    ui.util.ValidationPopup.show(field, label + " chưa nhập đầy đủ (dd/MM/yyyy)");
                    return;
                }
                try {
                    java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                            .ofPattern("dd/MM/uuuu")
                            .withResolverStyle(java.time.format.ResolverStyle.STRICT);
                    java.time.LocalDate.parse(val, fmt);
                } catch (Exception ex) {
                    ui.util.ValidationPopup.show(field, label + " không hợp lệ (dd/MM/yyyy)");
                }
            }
        });
    }

    private String extractRoomCode(String roomDisplay) {
        if (roomDisplay == null || roomDisplay.trim().isEmpty()) {
            return "";
        }
        int idx = roomDisplay.indexOf('-');
        if (idx <= 0) {
            return roomDisplay.trim();
        }
        return roomDisplay.substring(0, idx).trim();
    }

    private String extractRoomPriceRaw(String roomDisplay) {
        if (roomDisplay == null) {
            return "0";
        }
        int idx = roomDisplay.indexOf('-');
        String pricePart = idx >= 0 ? roomDisplay.substring(idx + 1) : roomDisplay;
        String digits = pricePart.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? "0" : digits;
    }

    private void selectRoomByCode(JComboBox<String> combo, String roomCode) {
        if (roomCode == null)
            return;

        for (int i = 0; i < combo.getItemCount(); i++) {
            Object itemObj = combo.getItemAt(i);
            if (itemObj != null) {
                String item = itemObj.toString();
                // Kiểm tra xem item có bắt đầu bằng mã phòng không
                if (item.startsWith(roomCode)) {
                    combo.setSelectedIndex(i);
                    return; // Tìm thấy thì thoát vòng lặp ngay
                }
            }
        }
    }

    public boolean showContractPreviewDialog(ContractDraft draft) {
        Window parent = SwingUtilities.getWindowAncestor(pnlRoot);
        JDialog dialog = new JDialog(parent, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(1200, 920);
        dialog.setLocationRelativeTo(pnlRoot);

        final boolean[] accepted = { false };

        RoundedPanel rootPanel = new RoundedPanel(12);
        rootPanel.setBackground(Color.WHITE);
        rootPanel.setLayout(new BorderLayout());
        rootPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        // --- Header ---
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

        // --- Load service data ---
        DichVuDAO dvAllDAO = new DichVuDAO();
        java.util.List<DichVu> allDV = dvAllDAO.layTatCa();
        PhongDichVuDAO phongDvDAO = new PhongDichVuDAO();
        java.util.List<DichVu> roomDV = phongDvDAO.layDichVuCuaPhong(draft.phong);

        String giaDien = "Theo quy định", giaNuoc = "Theo quy định";
        String phiGiuXe = "Miễn phí", phiDichVu = "Theo quy định";

        for (DichVu dv : allDV) {
            String ten = dv.getTenDichVu().toLowerCase();
            if (dv.getDonGia() == null || dv.getDonGia() <= 0)
                continue;
            String giaStr = formatCurrency(String.valueOf(Math.round(dv.getDonGia()))) + "đ/"
                    + (dv.getDonVi() != null ? dv.getDonVi().toLowerCase() : "");
            if (ten.contains("điện"))
                giaDien = giaStr;
            else if (ten.contains("nước"))
                giaNuoc = giaStr;
        }
        for (DichVu dv : roomDV) {
            String ten = dv.getTenDichVu().toLowerCase();
            if (dv.getDonGia() == null || dv.getDonGia() <= 0)
                continue;
            String giaStr = formatCurrency(String.valueOf(Math.round(dv.getDonGia()))) + "đ/"
                    + (dv.getDonVi() != null ? dv.getDonVi().toLowerCase() : "");
            if (ten.contains("xe") || ten.contains("giữ") || ten.contains("gửi"))
                phiGiuXe = giaStr;
            else if (ten.contains("wifi") || ten.contains("dịch vụ") || ten.contains("rác") || ten.contains("vệ sinh")
                    || ten.contains("phí"))
                phiDichVu = giaStr;
        }

        // --- Calculate contract duration ---
        String soThangThue = "";
        try {
            java.time.format.DateTimeFormatter fmtDate = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.LocalDate start = java.time.LocalDate.parse(draft.ngayBatDau, fmtDate);
            java.time.LocalDate end = java.time.LocalDate.parse(draft.ngayKetThuc, fmtDate);
            long months = java.time.temporal.ChronoUnit.MONTHS.between(start, end);
            soThangThue = months + " tháng";
        } catch (Exception ex) {
            soThangThue = "N/A";
        }

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
        addDocHtml(page1, "- Hôm nay, Ngày <b>" + today.getDayOfMonth() + "</b> Tháng <b>" + today.getMonthValue()
                + "</b> Năm <b>" + today.getYear() + "</b> chúng tôi gồm:", docFont, 0);
        page1.add(Box.createVerticalStrut(8));

        // --- I. BÊN A ---
        addDocLine(page1, "I. ĐẠI DIỆN BÊN A (Bên nhận cọc)", docBold, 0);
        addDocFill(page1, "Ông/Bà", "Tống Nguyễn Nhật Tiến", docFont);
        addDocHtml(page1,
                "Số CCCD/Hộ chiếu: <b>079205022180</b> &nbsp;&nbsp; Ngày cấp: <b>31/05/2021</b> &nbsp;&nbsp; Nơi cấp: <b>Cục CS QLHC về TTXH</b>",
                docFont, 0);
        addDocFill(page1, "Điện thoại", "0357.988.614", docFont);
        page1.add(Box.createVerticalStrut(8));

        // --- II. BÊN B ---
        addDocLine(page1, "II. ĐẠI DIỆN BÊN B (Bên đặt cọc)", docBold, 0);
        addDocFill(page1, "Ông/Bà", draft.hoTen, docFont);
        if (draft.ngaySinh != null && !draft.ngaySinh.trim().isEmpty()) {
            addDocFill(page1, "Ngày sinh", draft.ngaySinh, docFont);
        }
        addDocFill(page1, "Số CMND/Hộ chiếu", draft.cccd, docFont);
        addDocFill(page1, "Địa chỉ thường trú", draft.diaChi, docFont);
        addDocFill(page1, "Điện Thoại", draft.soDienThoai, docFont);
        page1.add(Box.createVerticalStrut(8));

        // --- Điều 1 ---
        addDocLine(page1, "Điều 1 \u2013 Nội dung thỏa thuận", docBold, 0);
        addDocHtml(page1,
                "<b>1.1.</b>&nbsp;&nbsp;Bằng thỏa thuận này, Bên B đồng ý đặt cọc cho bên A và bên A đồng ý nhận cọc "
                        + "của bên B nhằm mục đích đảm bảo việc giao kết thỏa thuận đặt cọc giữa bên B "
                        + "\nvà chủ đầu tư để thuê sản phẩm với thông tin như sau:",
                docFont, 0);
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
        addDocHtml(page1, "Phí giữ xe máy: <b>" + phiGiuXe + "</b>", docFont, 28);
        addDocHtml(page1, "Phí dịch vụ (Wifi, rác, vệ sinh): <b>" + phiDichVu + "</b>", docFont, 28);

        // ==================== PAGE 2 ====================
        JPanel page2 = createDocPage();

        // --- 1.2 ---
        addDocHtml(page2, "<b>1.2.</b>&nbsp;&nbsp;Bên A nhận số tiền đặt cọc giữ phòng là: <b>"
                + formatCurrency(draft.tienCocRaw) + " đ</b>", docFont, 0);
        addDocLine(page2, "Hình thức nhận cọc bằng chuyển khoản với thông tin cụ thể như sau:", docItalic, 16);
        addDocFillIndent(page2, "Ngân hàng", "MBBANK", docFont, 16);
        addDocFillIndent(page2, "Số tài khoản", "0357988614", docFont, 16);
        addDocFillIndent(page2, "Tên tài khoản", "Tống Nguyễn Nhật Tiến", docFont, 16);
        page2.add(Box.createVerticalStrut(8));

        // --- 1.3 ---
        addDocHtml(page2, "<b>1.3.</b>&nbsp;&nbsp;Hai bên cùng thỏa thuận và cam kết rằng:", docFont, 0);
        addDocHtml(page2,
                "<b>a.</b> Bên B cam kết đến kí hợp đồng thuê nhà và dọn vào ở không quá 2 ngày so với ngày dọn vào dự kiến;",
                docFont, 16);
        addDocHtml(page2, "<b>b.</b> Bên B cam kết đến kí hợp đồng theo thời gian đã nêu tại điều khoản 1.3.a, "
                + "đóng đủ tiền nhà và các phí dịch vụ của tòa nhà tháng đầu tiên;", docFont, 16);
        addDocHtml(page2, "<b>c.</b> Nếu quá thời hạn nêu trên mà bên B vẫn không Ký hợp đồng theo như "
                + "điều 1.3.a nêu trên thì bên B sẽ mất toàn bộ số tiền đặt cọc;", docFont, 16);
        addDocHtml(page2, "<b>d.</b> Trong trường hợp tòa nhà có quy định khách hàng được sang nhượng hợp đồng "
                + "thuê thì bên B có thể tự sang nhượng hoặc nhờ bên A tìm khách \nsang nhượng "
                + "trên tinh thần tự nguyện. Bên A không có trách nhiệm bắt buộc sang nhượng cho bên B;", docFont, 16);
        page2.add(Box.createVerticalStrut(10));

        // --- Điều 2 ---
        addDocLine(page2, "Điều 2 \u2013 Điều khoản chung:", docBold, 0);
        addDocHtml(page2, "<b>2.1.</b> Thỏa thuận này có hiệu lực từ thời điểm hai bên ký kết. "
                + "Trường hợp khách hàng đặt cọc bằng hình thức chuyển khoản, hợp đồng này có hiệu lực khi "
                + "Bên B đã đồng ý \nđặt cọc cho bên A (có xác thực bằng tin nhắn hoặc ghi âm) "
                + "và đã chuyển khoản tiền cọc cho bên A mà không cần phải có chữ ký của Bên B;", docFont, 0);
        addDocHtml(page2, "<b>2.2.</b> Thỏa thuận này sẽ chấm dứt hiệu lực trong các trường hợp sau: "
                + "Bên B không ký hợp đồng và dọn vào ở theo quy định tại điều 1.3.b;", docFont, 0);
        addDocHtml(page2, "<b>2.3.</b> Bên A không cho bất kì một bên nào khác bên B đặt cọc hoặc thuê phòng "
                + "là đối tượng được quy định trên hợp đồng này trong thời hạn thỏa thuận;", docFont, 0);
        addDocHtml(page2, "<b>2.4.</b> Ngay khi bên B ký hợp đồng thuê với chủ đầu tư, toàn bộ số tiền cọc sẽ "
                + "được chuyển thành tiền ký quỹ trong hợp đồng thuê của Bên B;", docFont, 0);
        page2.add(Box.createVerticalStrut(36));

        // --- Ký tên ---
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
        // --- Ảnh chữ ký Bên A ---
        try {
            java.awt.image.BufferedImage sigOrig = javax.imageio.ImageIO
                    .read(new java.io.File("img/signature/chuky.jpg"));
            int sigW = 120, sigH = (int) ((double) sigOrig.getHeight() / sigOrig.getWidth() * sigW);
            Image sigScaled = sigOrig.getScaledInstance(sigW, sigH, Image.SCALE_SMOOTH);
            JLabel lblSig = new JLabel(new ImageIcon(sigScaled));
            lblSig.setAlignmentX(Component.CENTER_ALIGNMENT);
            signA.add(lblSig);
        } catch (Exception ex) {
            signA.add(Box.createVerticalStrut(26));
        }
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

        // ==================== CARD LAYOUT ====================
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
                "\u2190  Quay lại sửa",
                new Font("Inter", Font.BOLD, 13),
                MAU_TEXT, Color.WHITE, new Color(241, 245, 249), 8,
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        new EmptyBorder(10, 12, 10, 12)));

        JButton btnNext = primaryButton.makePrimaryButton("Xem tiếp  \u2192");
        btnNext.setBorder(new EmptyBorder(10, 16, 10, 16));

        JButton btnConfirm = primaryButton.makePrimaryButton("Xác nhận hợp đồng");
        btnConfirm.setBorder(new EmptyBorder(10, 12, 10, 12));
        btnConfirm.setVisible(false);

        final int[] currentPage = { 1 };

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

        btnConfirm.addActionListener(e -> {
            accepted[0] = true;
            dialog.dispose();
        });

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setOpaque(false);
        rightBtns.add(btnNext);
        rightBtns.add(btnConfirm);

        footerBtns.add(btnBack, BorderLayout.WEST);
        footerBtns.add(rightBtns, BorderLayout.EAST);

        footer.add(lblPageNum, BorderLayout.NORTH);
        footer.add(footerBtns, BorderLayout.CENTER);
        rootPanel.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(rootPanel);
        dialog.setVisible(true);
        return accepted[0];
    }

    // ============ Contract document helper methods ============

    private JPanel createDocPage() {
        JPanel page = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                // Let width be determined by parent, height by content
                Dimension d = super.getPreferredSize();
                if (getParent() != null) {
                    d.width = getParent().getWidth();
                }
                return d;
            }
        };
        page.setBackground(Color.WHITE);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(8, 8, 8, 8),
                        BorderFactory.createLineBorder(new Color(210, 215, 220))),
                new EmptyBorder(28, 36, 28, 36)));
        return page;
    }

    private void addDocCenter(JPanel doc, String text, Font font) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(font);
        lbl.setForeground(MAU_TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, lbl.getPreferredSize().height));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        doc.add(lbl);
    }

    private void addDocLine(JPanel doc, String text, Font font, int indent) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(MAU_TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(2, indent, 2, 0));
        doc.add(lbl);
    }

    private void addDocHtml(JPanel doc, String html, Font font, int indent) {
        JLabel lbl = new JLabel("<html>" + html + "</html>");
        lbl.setFont(font);
        lbl.setForeground(MAU_TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(2, indent, 2, 0));
        doc.add(lbl);
    }

    private void addDocFill(JPanel doc, String label, String value, Font font) {
        addDocFillIndent(doc, label, value, font, 0);
    }

    private void addDocFillIndent(JPanel doc, String label, String value, Font font, int indent) {
        String safeVal = value != null ? value : "";
        // Build dotted line that stretches to fill
        String dots = " ................................................................................";
        int maxDots = Math.max(5, 50 - label.length() - safeVal.length());
        String filling = dots.substring(0, Math.min(maxDots, dots.length()));
        JLabel lbl = new JLabel(
                "<html>" + label + " :" + filling + "<b>" + safeVal + "</b>"
                        + filling.substring(0, Math.min(8, filling.length())) + "</html>");
        lbl.setFont(font);
        lbl.setForeground(MAU_TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        lbl.setBorder(new EmptyBorder(2, indent, 2, 0));
        doc.add(lbl);
    }

    private String formatCurrency(String num) {
        String digits = sanitizeMoneyRaw(num);
        if (digits.isEmpty())
            return "0";
        try {
            long v = Long.parseLong(digits);
            return VN_MONEY.format(v);
        } catch (NumberFormatException e) {
            return "0";
        }
    }

    private String formatMoneyDisplay(Object moneyObj) {
        if (moneyObj == null || moneyObj.toString().isEmpty())
            return "0 đ";
        try {
            double amount = Double.parseDouble(moneyObj.toString());
            long value = Math.round(amount);
            return VN_MONEY.format(value) + " đ";
        } catch (Exception e) {
            return "0 đ";
        }
    }

    private String sanitizeMoneyRaw(String text) {
        if (text == null)
            return "";
        return text.replaceAll("[^0-9]", "");
    }

    private String formatCurrencyValue(long amount) {
        return VN_MONEY.format(amount);
    }

    private void showMemberManagementDialog(String maHopDong, String maPhong) {
        Window parent = SwingUtilities.getWindowAncestor(pnlRoot);

        JDialog overlay = new JDialog(parent);
        overlay.setUndecorated(true);
        try {
            overlay.setBackground(new Color(0, 0, 0, 100));
        } catch (Exception e) {
        }
        if (parent != null)
            overlay.setBounds(parent.getBounds());
        overlay.setFocusableWindowState(false);

        JDialog dialog = new JDialog(parent, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(680, 520);
        dialog.setLocationRelativeTo(pnlRoot);

        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC");
        dialog.getRootPane().getActionMap().put("ESC", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        RoundedPanel pnlBg = new RoundedPanel(16);
        pnlBg.setBackground(Color.WHITE);
        pnlBg.setLayout(new BorderLayout());
        pnlBg.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Header
        JPanel pnlHead = new JPanel(new BorderLayout());
        pnlHead.setOpaque(false);
        JLabel lblTitle = new JLabel("Quản lý thành viên - Phòng " + maPhong);
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

        // Content - Member Table
        String[] cols = { "", "Họ tên", "SĐT", "CCCD", "Vai trò" };
        DefaultTableModel memberModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable memberTable = new JTable(memberModel);
        memberTable.setRowHeight(48);
        memberTable.setShowGrid(false);
        memberTable.setIntercellSpacing(new Dimension(0, 0));
        memberTable.setFocusable(false);
        memberTable.setSelectionBackground(new Color(241, 245, 249));
        memberTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        // Hide maHDKT column
        memberTable.getColumnModel().getColumn(0).setMinWidth(0);
        memberTable.getColumnModel().getColumn(0).setMaxWidth(0);
        memberTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        memberTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        memberTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        memberTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        memberTable.getColumnModel().getColumn(4).setPreferredWidth(120);

        JTableHeader mHeader = memberTable.getTableHeader();
        mHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r,
                    int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setFont(new Font("Inter", Font.BOLD, 13));
                l.setForeground(MAU_SUBTEXT);
                l.setBackground(MAU_CARD);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                        new EmptyBorder(12, 12, 12, 8)));
                return l;
            }
        });

        // Role column renderer with badge
        memberTable.getColumnModel().getColumn(4).setCellRenderer(new TableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r,
                    int c) {
                JPanel pnl = new JPanel(new GridBagLayout());
                pnl.setBackground(isSel ? t.getSelectionBackground() : MAU_CARD);
                pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));

                String role = v != null ? v.toString() : "";
                Color bg, fg;
                if (role.contains("Đại Diện")) {
                    bg = new Color(34, 88, 195);
                    fg = Color.WHITE;
                } else if (role.contains("rời")) {
                    bg = new Color(239, 68, 68);
                    fg = Color.WHITE;
                } else {
                    bg = new Color(226, 232, 240);
                    fg = new Color(71, 85, 105);
                }

                JLabel lbl = new JLabel(role) {
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
                gbc.insets = new Insets(0, 12, 0, 0);
                pnl.add(lbl, gbc);
                return pnl;
            }
        });

        // Default renderer for other columns
        memberTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r,
                    int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setFont(new Font("Inter", Font.PLAIN, 13));
                l.setForeground(MAU_TEXT);
                l.setBackground(isSel ? t.getSelectionBackground() : MAU_CARD);

                // Kiểm tra nếu người đã rời đi thì hiện chữ xám + gạch ngang
                int modelRow = t.convertRowIndexToModel(r);
                String vaiTro = String.valueOf(memberModel.getValueAt(modelRow, 4));
                if (vaiTro.contains("rời")) {
                    l.setForeground(new Color(148, 163, 184));
                }

                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                        new EmptyBorder(0, 12, 0, 8)));
                return l;
            }
        });

        // Load data
        Runnable loadMembers = () -> {
            memberModel.setRowCount(0);
            ArrayList<entity.HopDongKhachThue> dsTV = HDKHdao.getAllThanhVienByMaHopDong(maHopDong);
            for (entity.HopDongKhachThue hdkt : dsTV) {
                entity.KhachHang kh = hdkt.getKhachHang();
                memberModel.addRow(new Object[] {
                        hdkt.getMaHDKT(),
                        kh.getHoTen(),
                        kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "",
                        kh.getSoCCCD() != null ? kh.getSoCCCD() : "",
                        hdkt.getVaiTro().getTen()
                });
            }
        };
        loadMembers.run();

        JScrollPane scrollMembers = new JScrollPane(memberTable);
        scrollMembers.setBorder(BorderFactory.createEmptyBorder());
        scrollMembers.getViewport().setBackground(MAU_CARD);

        RoundedPanel cardMembers = new RoundedPanel(12);
        cardMembers.setBackground(MAU_CARD);
        cardMembers.setLayout(new BorderLayout());
        cardMembers.setBorder(new EmptyBorder(4, 0, 4, 0));
        cardMembers.add(scrollMembers, BorderLayout.CENTER);

        pnlBg.add(cardMembers, BorderLayout.CENTER);

        // Footer buttons
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        pnlFooter.setOpaque(false);

        JButton btnRoiDi = primaryButton.makePrimaryButton("Đánh dấu rời đi");
        btnRoiDi.setBackground(new Color(239, 68, 68));
        btnRoiDi.setBorder(new EmptyBorder(10, 18, 10, 18));
        btnRoiDi.addActionListener(e -> {
            int selRow = memberTable.getSelectedRow();
            if (selRow < 0) {
                showToast("Vui lòng chọn một thành viên");
                return;
            }
            int mRow = memberTable.convertRowIndexToModel(selRow);
            String maHDKT = String.valueOf(memberModel.getValueAt(mRow, 0));
            String tenKH = String.valueOf(memberModel.getValueAt(mRow, 1));
            String vaiTro = String.valueOf(memberModel.getValueAt(mRow, 4));

            if (vaiTro.contains("rời")) {
                showToast("Thành viên này đã rời đi rồi");
                return;
            }

            boolean laDaiDien = vaiTro.contains("Đại Diện");

            // Đếm số thành viên còn hoạt động (không phải đã rời đi)
            int soConLai = 0;
            ArrayList<String[]> danhSachConLai = new ArrayList<>();
            for (int i = 0; i < memberModel.getRowCount(); i++) {
                String vt = String.valueOf(memberModel.getValueAt(i, 4));
                String hdkt = String.valueOf(memberModel.getValueAt(i, 0));
                String ten = String.valueOf(memberModel.getValueAt(i, 1));
                if (!vt.contains("rời") && !hdkt.equals(maHDKT)) {
                    soConLai++;
                    danhSachConLai.add(new String[] { hdkt, ten });
                }
            }

            if (laDaiDien && soConLai == 0) {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "'" + tenKH + "' là người đại diện duy nhất.\n" +
                                "Đánh dấu rời đi sẽ khiến hợp đồng không còn người đại diện.\n\n" +
                                "Bạn có chắc chắn muốn tiếp tục?",
                        "Cảnh báo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION)
                    return;

                boolean ok = HDKHdao.thanhVienRoiDi(maHDKT, maPhong);
                if (ok) {
                    loadMembers.run();
                    loadDataToTable();
                    showToast("Đã đánh dấu '" + tenKH + "' rời đi");
                } else {
                    showToast("Thao tác thất bại");
                }
                return;
            }

            if (laDaiDien) {
                // Phải chọn người đại diện mới trước
                String[] options = new String[danhSachConLai.size()];
                for (int i = 0; i < danhSachConLai.size(); i++) {
                    options[i] = danhSachConLai.get(i)[1];
                }

                String chosen = (String) JOptionPane.showInputDialog(dialog,
                        "'" + tenKH + "' là người đại diện.\nVui lòng chọn người đại diện mới:",
                        "Chọn người đại diện mới",
                        JOptionPane.QUESTION_MESSAGE, null, options,
                        options.length > 0 ? options[0] : null);

                if (chosen == null)
                    return;

                // Tìm maHDKT của người được chọn
                String maHDKT_Moi = null;
                for (String[] item : danhSachConLai) {
                    if (item[1].equals(chosen)) {
                        maHDKT_Moi = item[0];
                        break;
                    }
                }

                boolean ok = HDKHdao.roiDiVaDoiDaiDien(maHDKT, maHopDong, maHDKT_Moi, maPhong);
                if (ok) {
                    loadMembers.run();
                    loadDataToTable();
                    showToast("Đã đánh dấu '" + tenKH + "' rời đi và '" + chosen + "' là người đại diện mới");
                } else {
                    showToast("Thao tác thất bại");
                }
            } else {
                // Thành viên thường rời đi
                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "Bạn có chắc chắn muốn đánh dấu '" + tenKH + "' đã rời đi?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION)
                    return;

                boolean ok = HDKHdao.thanhVienRoiDi(maHDKT, maPhong);
                if (ok) {
                    loadMembers.run();
                    loadDataToTable();
                    showToast("Đã đánh dấu '" + tenKH + "' rời đi");
                } else {
                    showToast("Thao tác thất bại");
                }
            }
        });

        JButton btnDoiDaiDien = primaryButton.makePrimaryButton("Đổi người đại diện");
        btnDoiDaiDien.setBorder(new EmptyBorder(10, 18, 10, 18));
        btnDoiDaiDien.addActionListener(e -> {
            // Tìm danh sách thành viên còn hoạt động (không phải đại diện, không phải đã
            // rời)
            ArrayList<String[]> danhSachTV = new ArrayList<>();
            for (int i = 0; i < memberModel.getRowCount(); i++) {
                String vt = String.valueOf(memberModel.getValueAt(i, 4));
                if (!vt.contains("Đại Diện") && !vt.contains("rời")) {
                    danhSachTV.add(new String[] {
                            String.valueOf(memberModel.getValueAt(i, 0)),
                            String.valueOf(memberModel.getValueAt(i, 1))
                    });
                }
            }

            if (danhSachTV.isEmpty()) {
                showToast("Không có thành viên nào khả dụng để đổi");
                return;
            }

            String[] options = new String[danhSachTV.size()];
            for (int i = 0; i < danhSachTV.size(); i++) {
                options[i] = danhSachTV.get(i)[1];
            }

            String chosen = (String) JOptionPane.showInputDialog(dialog,
                    "Chọn người đại diện mới:",
                    "Đổi người đại diện",
                    JOptionPane.QUESTION_MESSAGE, null, options,
                    options[0]);

            if (chosen == null)
                return;

            String maHDKT_Moi = null;
            for (String[] item : danhSachTV) {
                if (item[1].equals(chosen)) {
                    maHDKT_Moi = item[0];
                    break;
                }
            }

            boolean ok = HDKHdao.doiNguoiDaiDien(maHopDong, maHDKT_Moi);
            if (ok) {
                loadMembers.run();
                loadDataToTable();
                showToast("Đã đổi người đại diện thành '" + chosen + "'");
            } else {
                showToast("Thao tác thất bại");
            }
        });

        RoundedButton btnDong = ButtonStyles.createSecondary(
                "Đóng", new Font("Inter", Font.BOLD, 13),
                MAU_TEXT, Color.WHITE, new Color(226, 232, 240), 8,
                new EmptyBorder(10, 22, 10, 22));
        btnDong.addActionListener(e -> dialog.dispose());

        pnlFooter.add(btnDoiDaiDien);
        pnlFooter.add(btnRoiDi);
        pnlFooter.add(btnDong);
        pnlBg.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(pnlBg);
        if (parent != null)
            overlay.setVisible(true);
        dialog.setVisible(true);
        overlay.dispose();
    }

    private void showToast(String message) {
        Window parent = SwingUtilities.getWindowAncestor(pnlRoot);
        if (parent == null)
            return;
        JDialog toast = new JDialog(parent);
        toast.setUndecorated(true);
        try {
            toast.setBackground(new Color(0, 0, 0, 0));
        } catch (Exception e) {
        }
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
                g2.setColor(MAU_TEXT);
                g2.fillOval(0, 2, 16, 16);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(4, 10, 7, 13);
                g2.drawLine(7, 13, 11, 7);
                g2.dispose();
            }
        };
        lblIcon.setPreferredSize(new Dimension(16, 20));

        JLabel lblMsg = new JLabel(message);
        lblMsg.setFont(new Font("Inter", Font.BOLD, 13));
        lblMsg.setForeground(MAU_TEXT);

        pnl.add(lblIcon);
        pnl.add(lblMsg);
        toast.add(pnl);
        toast.pack();

        toast.setLocation(parent.getX() + parent.getWidth() - toast.getWidth() - 32,
                parent.getY() + parent.getHeight() - toast.getHeight() - 32);
        toast.setVisible(true);

        Timer t = new Timer(2500, e -> toast.dispose());
        t.setRepeats(false);
        t.start();
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
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR),
                    new EmptyBorder(12, 16, 12, 16)));

            JPanel pnlHead = new JPanel(new BorderLayout());
            pnlHead.setOpaque(false);
            lblMonthYear = new JLabel("", SwingConstants.LEFT);
            lblMonthYear.setFont(new Font("Inter", Font.BOLD, 14));

            JPanel pnlArrows = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pnlArrows.setOpaque(false);
            JButton btnPrev = createArrBtn("←");
            JButton btnNext = createArrBtn("→");
            btnPrev.addActionListener(e -> {
                calTracker.add(Calendar.MONTH, -1);
                updateCal();
            });
            btnNext.addActionListener(e -> {
                calTracker.add(Calendar.MONTH, 1);
                updateCal();
            });
            pnlArrows.add(btnPrev);
            pnlArrows.add(btnNext);

            pnlHead.add(lblMonthYear, BorderLayout.CENTER);
            pnlHead.add(pnlArrows, BorderLayout.EAST);
            add(pnlHead, BorderLayout.NORTH);

            JPanel pnlGrid = new JPanel(new BorderLayout(0, 8));
            pnlGrid.setOpaque(false);
            pnlGrid.setBorder(new EmptyBorder(12, 0, 12, 0));

            JPanel pnlDOW = new JPanel(new GridLayout(1, 7, 4, 4));
            pnlDOW.setOpaque(false);
            String[] dows = { "Su", "Mo", "Tu", "We", "Th", "Fr", "Sa" };
            for (String d : dows) {
                JLabel l = new JLabel(d, SwingConstants.CENTER);
                l.setFont(new Font("Inter", Font.PLAIN, 12));
                l.setForeground(MAU_SUBTEXT);
                pnlDOW.add(l);
            }
            pnlGrid.add(pnlDOW, BorderLayout.NORTH);

            daysPanel = new JPanel(new GridLayout(6, 7, 4, 4));
            daysPanel.setOpaque(false);
            pnlGrid.add(daysPanel, BorderLayout.CENTER);

            add(pnlGrid, BorderLayout.CENTER);

            JPanel pnlFooter = new JPanel(new BorderLayout());
            pnlFooter.setOpaque(false);
            JButton btnClear = createTxtBtn("Clear");
            JButton btnToday = createTxtBtn("Today");
            btnClear.addActionListener(e -> {
                targetField.setText("__/__/____");
                setVisible(false);
            });
            btnToday.addActionListener(e -> {
                targetField.setText(fmtStr(Calendar.getInstance()));
                setVisible(false);
            });
            pnlFooter.add(btnClear, BorderLayout.WEST);
            pnlFooter.add(btnToday, BorderLayout.EAST);
            add(pnlFooter, BorderLayout.SOUTH);

            updateCal();
        }

        private void updateCal() {
            daysPanel.removeAll();
            Calendar cal = (Calendar) calTracker.clone();
            String m = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.US);
            lblMonthYear.setText(m + " " + cal.get(Calendar.YEAR));

            cal.set(Calendar.DAY_OF_MONTH, 1);
            int sd = cal.get(Calendar.DAY_OF_WEEK);
            cal.add(Calendar.DAY_OF_MONTH, -(sd - 1));

            for (int i = 0; i < 42; i++) {
                int d = cal.get(Calendar.DAY_OF_MONTH);
                boolean cM = cal.get(Calendar.MONTH) == calTracker.get(Calendar.MONTH);
                JButton b = new JButton(String.valueOf(d));
                b.setFont(new Font("Inter", Font.PLAIN, 12));
                b.setForeground(cM ? MAU_TEXT : new Color(148, 163, 184));
                b.setBackground(Color.WHITE);
                b.setBorderPainted(false);
                b.setContentAreaFilled(false);
                b.setCursor(new Cursor(Cursor.HAND_CURSOR));
                b.setMargin(new Insets(2, 2, 2, 2));

                b.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        b.setContentAreaFilled(true);
                        b.setBackground(new Color(241, 245, 249));
                    }

                    public void mouseExited(MouseEvent e) {
                        b.setContentAreaFilled(false);
                    }
                });

                Calendar cap = (Calendar) cal.clone();
                b.addActionListener(e -> {
                    targetField.setText(fmtStr(cap));
                    setVisible(false);
                });
                daysPanel.add(b);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            daysPanel.revalidate();
            daysPanel.repaint();
        }

        private String fmtStr(Calendar c) {
            return String.format("%02d/%02d/%04d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1,
                    c.get(Calendar.YEAR));
        }

        private JButton createArrBtn(String t) {
            JButton b = new JButton(t);
            b.setFont(new Font("Inter", Font.BOLD, 14));
            b.setForeground(MAU_SUBTEXT);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }

        private JButton createTxtBtn(String t) {
            JButton b = new JButton(t);
            b.setFont(new Font("Inter", Font.PLAIN, 12));
            b.setForeground(new Color(37, 99, 235));
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }
    }

}
