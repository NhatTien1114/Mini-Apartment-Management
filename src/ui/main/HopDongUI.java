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
import javax.swing.table.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import dao.GiaDetailDAO;
import dao.HopDongDAO;
import dao.HopDongKhachHangDAO;
import dao.QuanLyPhongDAO;
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

    private JPanel pnlRoot;
    private DefaultTableModel model;
    private JTable table;

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
        if (dateObj == null || dateObj.toString().isBlank()) {
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
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlToolbar.setOpaque(false);

        RoundedTextField txtSearch = new RoundedTextField(8) {
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
        txtSearch.setPreferredSize(new Dimension(320, 40));
        txtSearch.setBackground(Color.WHITE);
        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                txtSearch.repaint();
            }

            public void focusLost(FocusEvent e) {
                txtSearch.repaint();
            }
        });

        pnlToolbar.add(txtSearch);
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

        this.model = new DefaultTableModel(columnNames, 0);
        this.table = new JTable(this.model); // Sử dụng biến class

        loadDataToTable();

        table = new JTable(model);
        table.setRowHeight(56);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);
        table.setSelectionBackground(new Color(241, 245, 249));
        table.setBorder(null);

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
                table.clearSelection();
            }
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
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

        // --- Right-click context menu ---
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem miEdit = new JMenuItem("Xem/Sửa thông tin");
        JMenuItem miDelete = new JMenuItem("Xóa");
        miDelete.setForeground(new Color(239, 68, 68));
        contextMenu.add(miEdit);
        contextMenu.add(miDelete);

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

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(MAU_CARD);

        cardMargin.add(scrollPane, BorderLayout.CENTER);
        card.add(cardMargin, BorderLayout.CENTER);

        pnlMainContent.add(card, BorderLayout.CENTER);
        pnlRoot.add(pnlMainContent, BorderLayout.CENTER);

        return pnlRoot;
    }

    public void showAddContractForm() {
        showContractForm(false, -1);
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
        JFormattedTextField txtBatDau = createFocusableDateField();
        JFormattedTextField txtKetThuc = createFocusableDateField();
        RoundedTextField txtThue = createFocusableField();
        RoundedTextField txtCoc = createFocusableField();

        txtSoDienThoai.setToolTipText("Ví dụ: 0901234567");
        txtCccd.setToolTipText("Ví dụ: 079123456789");

        applyNumberFilter(txtSoDienThoai);
        applyNumberFilter(txtCccd);

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
                        if (err == null || err.isBlank()) {
                            showToast("Lỗi: Không thể lưu vào cơ sở dữ liệu!");
                        } else {
                            showToast("Lỗi lưu hợp đồng: " + err);
                        }
                    }
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

    private String extractRoomCode(String roomDisplay) {
        if (roomDisplay == null || roomDisplay.isBlank()) {
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
        dialog.setSize(500, 640);
        dialog.setLocationRelativeTo(pnlRoot);

        final boolean[] accepted = { false };

        RoundedPanel root = new RoundedPanel(12);
        root.setBackground(Color.WHITE);
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Xem trước hợp đồng");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        title.setForeground(MAU_TEXT);
        JButton btnClose = new JButton("x");
        btnClose.setFont(new Font("Inter", Font.PLAIN, 18));
        btnClose.setForeground(MAU_SUBTEXT);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());
        head.add(title, BorderLayout.WEST);
        head.add(btnClose, BorderLayout.EAST);
        root.add(head, BorderLayout.NORTH);

        JPanel doc = new JPanel();
        doc.setOpaque(true);
        doc.setBackground(Color.WHITE);
        doc.setLayout(new BoxLayout(doc, BoxLayout.Y_AXIS));
        doc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(16, 18, 16, 18)));

        addDocLine(doc, "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM", true, 20);
        addDocLine(doc, "Độc lập - Tự do - Hạnh phúc", false, 14);
        addDocLine(doc, "---o0o---", false, 13);
        addDocLine(doc, "HỢP ĐỒNG THUÊ PHÒNG", true, 30);

        addDocLine(doc, "Điều 1: Bên cho thuê (Bên A):", true, 10);
        addDocLine(doc, "Chủ chung cư MINI APARTMENT", false, 10);

        addDocLine(doc, "Điều 2: Bên thuê (Bên B):", true, 16);
        addDocLine(doc, "Họ tên: " + draft.hoTen, false, 10);
        addDocLine(doc, "CCCD/CMND: " + draft.cccd, false, 8);
        addDocLine(doc, "Số điện thoại: " + draft.soDienThoai, false, 8);
        addDocLine(doc, "Địa chỉ: " + draft.diaChi, false, 8);
        addDocLine(doc, "Ngày sinh: " + (draft.ngaySinh == null || draft.ngaySinh.isBlank() ? "" : draft.ngaySinh),
                false, 8);

        addDocLine(doc, "Điều 3: Nội dung hợp đồng:", true, 16);
        addDocLine(doc, "Phòng cho thuê: " + draft.phong, false, 10);
        addDocLine(doc, "Thời hạn: Từ " + draft.ngayBatDau + " đến " + draft.ngayKetThuc, false, 8);
        addDocLine(doc, "Giá thuê: " + formatCurrency(draft.giaThueRaw) + "/tháng", false, 8);
        addDocLine(doc, "Tiền cọc: " + formatCurrency(draft.tienCocRaw), false, 8);

        addDocLine(doc, "Điều 4: Quyền và nghĩa vụ:", true, 16);
        addDocLine(doc, "- Bên B thanh toán tiền thuê đúng hạn vào đầu mỗi tháng", false, 10);
        addDocLine(doc, "- Bên B chịu chi phí điện, nước, dịch vụ theo thực tế sử dụng", false, 8);
        addDocLine(doc, "- Bên A đảm bảo phòng trong tình trạng sử dụng tốt", false, 8);
        addDocLine(doc, "- Bên B không được tự ý sửa chữa, thay đổi kết cấu phòng", false, 8);

        addDocLine(doc, "Điều 5: Chấm dứt hợp đồng:", true, 16);
        addDocLine(doc, "- Hết thời hạn hợp đồng mà không gia hạn", false, 10);
        addDocLine(doc, "- Hai bên thỏa thuận chấm dứt", false, 8);
        addDocLine(doc, "- Bên vi phạm phải bồi thường theo quy định", false, 8);

        JScrollPane scroll = new JScrollPane(doc);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 2, 8, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));

        RoundedButton btnBack = ButtonStyles.createSecondary(
                "←   Quay lại sửa",
                new Font("Inter", Font.BOLD, 13),
                MAU_TEXT,
                Color.WHITE,
                new Color(241, 245, 249),
                8,
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        new EmptyBorder(10, 12, 10, 12)));
        btnBack.addActionListener(e -> dialog.dispose());

        JButton btnConfirm = primaryButton.makePrimaryButton("Xác nhận hợp đồng");
        btnConfirm.setBorder(new EmptyBorder(10, 12, 10, 12));
        btnConfirm.addActionListener(e -> {
            accepted[0] = true;
            dialog.dispose();
        });

        footer.add(btnBack);
        footer.add(btnConfirm);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
        return accepted[0];
    }

    private void addDocLine(JPanel doc, String text, boolean bold, int spaceTop) {
        if (spaceTop > 0) {
            doc.add(Box.createVerticalStrut(spaceTop));
        }
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(new Font("Inter", bold ? Font.BOLD : Font.PLAIN, bold ? 16 : 15));
        label.setForeground(MAU_TEXT);
        doc.add(label);
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
