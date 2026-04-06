package ui.main;

import dao.DichVuDAO;
import dao.QuanLyPhongDAO;
import entity.DichVu;
import entity.Phong;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import ui.util.AppColors;
import ui.util.MessageDialog;
import ui.util.PrimaryButton;
import ui.util.ValidationPopup;

public class DichVuUI {
    private final Color MAU_NEN = AppColors.SLATE_100;
    private final Color MAU_CARD = AppColors.WHITE;
    private final Color MAU_BORDER = AppColors.SLATE_200;
    private final Color MAU_TEXT = AppColors.SLATE_900;
    private final Color MAU_MUTED = AppColors.SLATE_500;
    private final Color MAU_PRIMARY = AppColors.PRIMARY;
    private final Color MAU_RED = AppColors.RED_500;
    private final Color MAU_HOVER_ROW = AppColors.SLATE_50;
    private final Color MAU_WARN_BG = new Color(254, 243, 199);
    private final Color MAU_WARN_FG = new Color(180, 83, 9);
    private final Color MAU_GREEN_BG = new Color(220, 252, 231);
    private final Color MAU_GREEN_FG = new Color(22, 163, 74);

    private final Font FONT_TITLE = new Font("Be Vietnam Pro", Font.BOLD, 22);
    private final Font FONT_BOLD = new Font("Be Vietnam Pro", Font.BOLD, 13);
    private final Font FONT_PLAIN = new Font("Be Vietnam Pro", Font.PLAIN, 13);
    private final Font FONT_SMALL = new Font("Be Vietnam Pro", Font.PLAIN, 12);

    private final PrimaryButton primaryButton = new PrimaryButton();
    private final NumberFormat NF = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private DefaultTableModel tableModel;
    private JTable table;
    private final DichVuDAO dao = new DichVuDAO();
    private final QuanLyPhongDAO phongDAO = new QuanLyPhongDAO();
    private List<DichVu> dsDichVu;

    public JPanel getPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 0));
        pnl.setBorder(new EmptyBorder(24, 24, 24, 24));
        pnl.setBackground(MAU_NEN);

        pnl.add(buildTopBar(), BorderLayout.NORTH);
        pnl.add(buildTableCard(), BorderLayout.CENTER);

        // Tự động load lại dữ liệu bảng khi Panel (Tab) được focus vào màn hình
        pnl.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadTable();
            }
        });

        return pnl;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(MAU_NEN);
        bar.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Quản lý dịch vụ");
        title.setFont(FONT_TITLE);
        title.setForeground(MAU_TEXT);
        bar.add(title, BorderLayout.WEST);

        JButton btnAdd = primaryButton.makePrimaryButton("Thêm dịch vụ");
        btnAdd.addActionListener(e -> showDialog(null));

        JButton btnConfigRoom = makeOutlineButton("Cấu hình phòng thuê");
        btnConfigRoom.addActionListener(e -> showRoomServiceDialog());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(btnConfigRoom);
        right.add(btnAdd);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JButton makeOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_PLAIN);
        btn.setForeground(MAU_TEXT);
        btn.setBackground(MAU_CARD);
        btn.setBorder(new CompoundBorder(
                new LineBorder(MAU_BORDER, 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showRoomServiceDialog() {
        List<Phong> dsPhongThue = phongDAO.getAllPhongDaThue();
        if (dsPhongThue == null || dsPhongThue.isEmpty()) {
            MessageDialog.show(null, "Thông báo", "Không có phòng đang thuê để cấu hình.",
                    MessageDialog.MessageType.ERROR);
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(table);
        JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null,
                "Cấu hình dịch vụ phòng thuê", true);
        dlg.setSize(560, 560);
        dlg.setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(MAU_CARD);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JComboBox<String> cboPhong = new JComboBox<>(
                dsPhongThue.stream().map(Phong::getMaPhong).toArray(String[]::new));
        cboPhong.setFont(FONT_PLAIN);
        cboPhong.setPreferredSize(new Dimension(200, 36));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(new JLabel("Phòng đang thuê"), BorderLayout.NORTH);
        top.add(cboPhong, BorderLayout.CENTER);
        root.add(top, BorderLayout.NORTH);

        JPanel pnlChecks = new JPanel();
        pnlChecks.setBackground(MAU_CARD);
        pnlChecks.setLayout(new BoxLayout(pnlChecks, BoxLayout.Y_AXIS));

        JScrollPane sp = new JScrollPane(pnlChecks);
        sp.setBorder(new LineBorder(MAU_BORDER, 1, true));
        sp.getViewport().setBackground(MAU_CARD);
        root.add(sp, BorderLayout.CENTER);

        List<JCheckBox> checkBoxes = new ArrayList<>();
        Runnable reloadChecks = () -> {
            pnlChecks.removeAll();
            checkBoxes.clear();

            String maPhong = (String) cboPhong.getSelectedItem();
            Set<String> selected = maPhong == null ? new HashSet<>() : dao.layMaDichVuTheoPhong(maPhong);
            boolean noConfig = selected.isEmpty();

            List<DichVu> allServices = dao.layTatCa();
            for (DichVu dv : allServices) {
                String label = dv.getTenDichVu() + " - " + (dv.getDonGia() == null ? "0" : NF.format(dv.getDonGia()))
                        + "đ/" + (dv.getDonVi() == null ? "tháng" : dv.getDonVi());
                JCheckBox cb = new JCheckBox(label);
                cb.putClientProperty("maDichVu", dv.getMaDichVu());
                cb.setFont(FONT_PLAIN);
                cb.setOpaque(false);
                cb.setSelected(noConfig || selected.contains(dv.getMaDichVu()));
                checkBoxes.add(cb);
                pnlChecks.add(cb);
                pnlChecks.add(Box.createVerticalStrut(6));
            }

            pnlChecks.revalidate();
            pnlChecks.repaint();
        };

        cboPhong.addActionListener(e -> reloadChecks.run());
        reloadChecks.run();

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);
        JButton btnCancel = makeOutlineButton("Hủy");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = primaryButton.makePrimaryButton("Lưu cấu hình");
        btnSave.addActionListener(e -> {
            String maPhong = (String) cboPhong.getSelectedItem();
            if (maPhong == null || maPhong.isBlank()) {
                MessageDialog.show(dlg, "Lỗi", "Chưa chọn phòng.", MessageDialog.MessageType.ERROR);
                return;
            }

            Set<String> selectedIds = new HashSet<>();
            for (JCheckBox cb : checkBoxes) {
                if (cb.isSelected()) {
                    Object maDv = cb.getClientProperty("maDichVu");
                    if (maDv != null) {
                        selectedIds.add(maDv.toString());
                    }
                }
            }

            if (dao.capNhatDichVuPhong(maPhong, selectedIds)) {
                MessageDialog.show(dlg, "Thành công", "Đã lưu dịch vụ cho phòng " + maPhong,
                        MessageDialog.MessageType.SUCCESS);
                dlg.dispose();
            } else {
                MessageDialog.show(dlg, "Lỗi", "Không thể lưu cấu hình dịch vụ.",
                        MessageDialog.MessageType.ERROR);
            }
        });

        bottom.add(btnCancel);
        bottom.add(btnSave);
        root.add(bottom, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(MAU_CARD);
        card.setBorder(new LineBorder(MAU_BORDER, 1, true));

        String[] cols = { "Mã DV", "Tên dịch vụ", "Đơn vị tính", "Đơn giá" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel) {
            int hoverRow = -1;
            {
                addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        int r = rowAtPoint(e.getPoint());
                        if (r != hoverRow) {
                            hoverRow = r;
                            repaint();
                        }
                    }
                });
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hoverRow = -1;
                        repaint();
                    }
                });
            }

            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row))
                    c.setBackground(hoverRow == row ? MAU_HOVER_ROW : MAU_CARD);
                return c;
            }
        };

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SMALL);
        header.setForeground(MAU_MUTED);
        header.setBackground(MAU_CARD);
        header.setBorder(new MatteBorder(0, 0, 1, 0, MAU_BORDER));
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer hdrRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(MAU_CARD);
                setForeground(MAU_MUTED);
                setFont(FONT_SMALL);
                setBorder(new EmptyBorder(0, 16, 0, 8));
                return this;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setHeaderRenderer(hdrRenderer);

        table.setRowHeight(56);
        table.setFont(FONT_PLAIN);
        table.setBackground(MAU_CARD);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(220);

        table.getColumnModel().getColumn(0).setCellRenderer(plainPaddedRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(boldPaddedRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(plainPaddedRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new PriceBadgeRenderer());

        // --- Right-click context menu ---
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem miEdit = new JMenuItem("Xem/Sửa thông tin");
        JMenuItem miDelete = new JMenuItem("Xóa");
        miDelete.setForeground(new Color(239, 68, 68));
        contextMenu.add(miEdit);
        contextMenu.add(miDelete);

        miEdit.addActionListener(ev -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < dsDichVu.size())
                showDialog(dsDichVu.get(row));
        });
        miDelete.addActionListener(ev -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= dsDichVu.size())
                return;
            DichVu target = dsDichVu.get(row);
            int confirm = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(table),
                    "Bạn có chắc chắn muốn xóa dịch vụ: " + target.getTenDichVu() + " ?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                if (dao.deleteDichVu(target.getMaDichVu())) {
                    MessageDialog.show(null, "Đã xóa", "Đã xóa dịch vụ thành công.",
                            MessageDialog.MessageType.SUCCESS);
                    loadTable();
                } else {
                    MessageDialog.show(null, "Báo lỗi", "Không thể xóa do ràng buộc dữ liệu.",
                            MessageDialog.MessageType.ERROR);
                }
            }
        });

        table.setComponentPopupMenu(contextMenu);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(MAU_CARD);

        card.add(sp, BorderLayout.CENTER);
        loadTable();
        return card;
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        dsDichVu = dao.layTatCa();
        for (DichVu dv : dsDichVu) {
            tableModel.addRow(new Object[] {
                    dv.getMaDichVu(),
                    dv.getTenDichVu(),
                    dv.getDonVi(),
                    dv.getDonGia(),
                    "ACT"
            });
        }
        table.revalidate();
        table.repaint();
    }

    private void showDialog(DichVu editDichVu) {
        boolean editing = (editDichVu != null);
        Window owner = SwingUtilities.getWindowAncestor(table);

        JDialog dlg = new JDialog(
                owner instanceof Frame ? (Frame) owner : null,
                editing ? "Sửa dịch vụ" : "Thêm dịch vụ", true);
        dlg.setUndecorated(true);
        dlg.setSize(480, 240);
        dlg.setLocationRelativeTo(owner);
        dlg.setBackground(new Color(0, 0, 0, 0));

        // ROOT PANEL (Rounded)
        ui.util.RoundedPanel root = new ui.util.RoundedPanel(16);
        root.setLayout(new BorderLayout());
        root.setBackground(MAU_CARD);
        root.setBorder(new EmptyBorder(22, 28, 22, 28));

        // HEADER
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(MAU_CARD);
        headerRow.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel dlgTitle = new JLabel(editing ? "Sửa dịch vụ" : "Thêm dịch vụ");
        dlgTitle.setFont(new Font("Inter", Font.BOLD, 18));
        dlgTitle.setForeground(MAU_TEXT);

        // Nút tắt nhỏ 'X'
        JLabel btnClose = new JLabel("<html><span style='font-size:16px'>✕</span></html>");
        btnClose.setForeground(MAU_MUTED);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dlg.dispose();
            }

            public void mouseEntered(MouseEvent e) {
                btnClose.setForeground(MAU_RED);
            }

            public void mouseExited(MouseEvent e) {
                btnClose.setForeground(MAU_MUTED);
            }
        });

        headerRow.add(dlgTitle, BorderLayout.WEST);
        headerRow.add(btnClose, BorderLayout.EAST);

        // GRID FIELDS (1 dòng, 2 cột theo ảnh minh hoạ của Tab Dịch Vụ)
        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setBackground(MAU_CARD);

        ui.util.RoundedTextField fTen = new ui.util.RoundedTextField(6);
        fTen.setPlaceholder("Nhập tên, Vd: Rác");
        fTen.setPreferredSize(new Dimension(0, 42)); // Ô nhập to bự
        if (editing)
            fTen.setText(editDichVu.getTenDichVu());

        JComboBox<String> cmbDonVi = new JComboBox<>(new String[] { "Tháng", "Số" });
        cmbDonVi.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));
        cmbDonVi.setBackground(Color.WHITE);
        cmbDonVi.setPreferredSize(new Dimension(0, 42));
        if (editing) {
            String dv = editDichVu.getDonVi();
            if (dv != null)
                cmbDonVi.setSelectedItem(dv);
        }

        grid.add(wrapField("Tên dịch vụ", fTen));
        grid.add(wrapField("Đơn vị", cmbDonVi));

        // BUTTONS BOTTOM
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(MAU_CARD);
        btnRow.setBorder(new EmptyBorder(24, 0, 0, 0));

        ui.util.RoundedButton btnCancel = new ui.util.RoundedButton("Hủy", 6);
        btnCancel.setBackground(MAU_CARD);
        btnCancel.setForeground(MAU_MUTED);
        btnCancel.setFont(new Font("Inter", Font.BOLD, 13));
        btnCancel.setBorder(new LineBorder(MAU_BORDER, 1, true));
        btnCancel.setPreferredSize(new Dimension(86, 38));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dlg.dispose());

        ui.util.RoundedButton btnSave = new ui.util.RoundedButton(editing ? "Lưu lại" : "Thêm", 6);
        btnSave.setBackground(new Color(37, 99, 235)); // Tailwind blue 600
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Inter", Font.BOLD, 13));
        btnSave.setPreferredSize(new Dimension(100, 38));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> {
            String ten = fTen.getText().trim();
            String donvi = cmbDonVi.getSelectedItem() != null ? cmbDonVi.getSelectedItem().toString().trim() : "";

            if (ten.isEmpty()) {
                ValidationPopup.show(fTen, "Tên dịch vụ không được để trống!");
                return;
            }

            if (editing) {
                editDichVu.setTenDichVu(ten);
                editDichVu.setDonVi(donvi);
                if (dao.updateDichVu(editDichVu)) {
                    MessageDialog.show(dlg, "Thành Công", "Đã cập nhật dịch vụ.", MessageDialog.MessageType.SUCCESS);
                    dlg.dispose();
                    loadTable();
                } else {
                    MessageDialog.show(dlg, "Lỗi", "Lỗi CSDL.", MessageDialog.MessageType.ERROR);
                }
            } else {
                DichVu newDv = new DichVu();
                newDv.setTenDichVu(ten);
                newDv.setDonVi(donvi);
                if (dao.insertDichVu(newDv)) {
                    MessageDialog.show(dlg, "Thêm Thành Công", "Đã tạo dịch vụ mới.",
                            MessageDialog.MessageType.SUCCESS);
                    dlg.dispose();
                    loadTable();
                } else {
                    MessageDialog.show(dlg, "Lỗi", "Lỗi CSDL.", MessageDialog.MessageType.ERROR);
                }
            }
        });

        btnRow.add(btnCancel);
        btnRow.add(btnSave);

        root.add(headerRow, BorderLayout.NORTH);
        root.add(grid, BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);

        // Map Keyboard ESC to close
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                "ESC_CLOSE");
        root.getActionMap().put("ESC_CLOSE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dlg.dispose();
            }
        });

        dlg.setContentPane(root);

        if (owner instanceof JFrame frame) {
            Component oldGlassPane = frame.getGlassPane();
            JPanel overlay = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(new Color(0, 0, 0, 110));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            overlay.setOpaque(false);
            // Block all mouse clicks on overlay
            overlay.addMouseListener(new MouseAdapter() {
            });
            frame.setGlassPane(overlay);
            overlay.setVisible(true);

            // Hide overlay when dialog is disposed
            dlg.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    overlay.setVisible(false);
                    frame.setGlassPane(oldGlassPane);
                }
            });
        }

        // Show Dialog safely
        dlg.setVisible(true);
    }

    class PriceBadgeRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 18));
            p.setBackground(sel ? new Color(239, 246, 255) : MAU_CARD);
            p.setBorder(new MatteBorder(0, 0, 1, 0, MAU_BORDER));

            JLabel badge = new JLabel();
            badge.setFont(FONT_SMALL);
            badge.setOpaque(true);
            badge.setBorder(new EmptyBorder(4, 10, 4, 10));

            if (v == null) {
                badge.setText("Chưa cấu hình");
                badge.setBackground(MAU_WARN_BG);
                badge.setForeground(MAU_WARN_FG);
            } else {
                badge.setText(NF.format(v) + "đ");
                badge.setBackground(MAU_GREEN_BG);
                badge.setForeground(MAU_GREEN_FG);
            }
            p.add(badge);
            return p;
        }
    }

    private JButton makeIconButton(String icon, Color fg) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(2, 5, 2, 5));
        return btn;
    }

    private JLabel makeIconLabel(String icon, Color fg) {
        JLabel l = new JLabel(icon);
        l.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        l.setForeground(fg);
        return l;
    }

    private JPanel wrapField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(MAU_CARD);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(MAU_MUTED);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private TableCellRenderer boldPaddedRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setFont(FONT_BOLD);
                setForeground(MAU_TEXT);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, MAU_BORDER),
                        new EmptyBorder(0, 16, 0, 8)));
                return this;
            }
        };
    }

    private TableCellRenderer plainPaddedRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setFont(FONT_PLAIN);
                setForeground(MAU_MUTED);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, MAU_BORDER),
                        new EmptyBorder(0, 16, 0, 8)));
                return this;
            }
        };
    }
}