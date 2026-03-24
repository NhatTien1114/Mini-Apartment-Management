package ui.main;

import dao.QuanLyPhongDAO;
import entity.Phong;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import ui.util.AppColors;
import ui.util.PrimaryButton;
import ui.util.RoundedTextField;

public class QuanLyPhongUI {

    private final Font FONT_TITLE = new Font("Be Vietnam Pro", Font.BOLD, 22);
    private final Font FONT_PLAIN = new Font("Be Vietnam Pro", Font.PLAIN, 13);
    private final Font FONT_SMALL = new Font("Be Vietnam Pro", Font.PLAIN, 12);

    private static final String[] DICH_VU_LIST = {
            "Điện - 3,500đ/kWh",
            "Nước - 15,000đ/m³",
            "Internet - 100,000đ/tháng",
            "Rác - 50,000đ/tháng",
            "Giữ xe máy - 100,000đ/tháng",
            "Giữ ô tô - 500,000đ/tháng"
    };

    // ── DAO & state ────
    private final QuanLyPhongDAO dao = new QuanLyPhongDAO();
    private final PrimaryButton primaryButtonHelper = new PrimaryButton();
    private JPanel floorsPanel;
    private JScrollPane scrollPane;

    // ════════════════════════════════════════════════════════════════════════
    public JPanel getPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.SLATE_100);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        // ── Top bar ──
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(AppColors.SLATE_100);
        topBar.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Quản lý phòng");
        title.setFont(FONT_TITLE);
        title.setForeground(AppColors.SLATE_900);
        topBar.add(title, BorderLayout.WEST);

        JButton btnAdd = primaryButtonHelper.makePrimaryButton("Thêm phòng");
        btnAdd.addActionListener(e -> showAddDialog());
        topBar.add(btnAdd, BorderLayout.EAST);

        root.add(topBar, BorderLayout.NORTH);

        // ── Content card ──
        JPanel contentCard = new JPanel(new BorderLayout());
        contentCard.setBackground(AppColors.WHITE);
        contentCard.setBorder(new LineBorder(AppColors.SLATE_200, 1, true));

        floorsPanel = new JPanel();
        floorsPanel.setBackground(AppColors.WHITE);
        floorsPanel.setLayout(new BoxLayout(floorsPanel, BoxLayout.Y_AXIS));
        floorsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        scrollPane = new JScrollPane(floorsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.getViewport().setBackground(AppColors.WHITE);

        contentCard.add(scrollPane, BorderLayout.CENTER);
        root.add(contentCard, BorderLayout.CENTER);

        rebuildFloors();
        return root;
    }

    // ── Xây lại toàn bộ danh sách tầng/phòng ────────────────────────────────
    private void rebuildFloors() {
        floorsPanel.removeAll();

        String[] tangs = { "T6", "T5", "T4", "T3", "T2", "T1" };
        String[] names = { "Tầng 6", "Tầng 5", "Tầng 4", "Tầng 3", "Tầng 2", "Tầng 1" };

        boolean first = true;
        for (int i = 0; i < tangs.length; i++) {
            List<Phong> phongs = dao.layTheoTang(tangs[i]);
            if (phongs.isEmpty())
                continue;
            if (!first)
                floorsPanel.add(Box.createVerticalStrut(18));
            floorsPanel.add(createFloorSection(names[i], phongs));
            first = false;
        }

        floorsPanel.revalidate();
        floorsPanel.repaint();
    }

    // ── Tầng ────────────────────────────────────────────────────────────────
    private JPanel createFloorSection(String floorName, List<Phong> phongs) {
        JPanel section = new JPanel(new BorderLayout(0, 10));
        section.setOpaque(false);

        JLabel lblFloor = new JLabel(floorName);
        lblFloor.setFont(new Font("Be Vietnam Pro", Font.BOLD, 18));
        lblFloor.setForeground(AppColors.SLATE_900);
        section.add(lblFloor, BorderLayout.NORTH);

        JPanel roomsWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        roomsWrap.setOpaque(false);
        for (Phong p : phongs)
            roomsWrap.add(createRoomCard(p));
        section.add(roomsWrap, BorderLayout.CENTER);
        return section;
    }

    // ── Card phòng ───────────────────────────────────────────────────────────
    private JPanel createRoomCard(Phong phong) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(AppColors.WHITE);
        card.setPreferredSize(new Dimension(210, 112));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_200, 1),
                new EmptyBorder(10, 12, 10, 12)));

        // Header: mã phòng + badge trạng thái
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblRoom = new JLabel(phong.getMaPhong());
        lblRoom.setFont(new Font("Be Vietnam Pro", Font.BOLD, 17));
        lblRoom.setForeground(AppColors.SLATE_900);
        header.add(lblRoom, BorderLayout.WEST);
        header.add(createStatusBadge(phong.getTrangThai().getTen()), BorderLayout.EAST);

        // Giá - sửa giá nha Quân
        // JLabel lblPrice = new JLabel("Giá: " + NF.format(phong.getGiaThue()) + "đ");
        // lblPrice.setFont(FONT_SMALL);
        // lblPrice.setForeground(AppColors.SLATE_500);

        // Buttons: Cài đặt + Xóa
        JPanel btnRow = new JPanel(new BorderLayout(6, 0));
        btnRow.setOpaque(false);

        JButton btnSetting = makeOutlineButton("⚙ Cài đặt");
        btnSetting.addActionListener(e -> showSettingDialog(phong));

        JButton btnDelete = makeDeleteButton();
        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(card),
                    "Xóa phòng \"" + phong.getMaPhong() + "\"?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                String err = dao.xoa(phong.getMaPhong());
                if (err != null)
                    JOptionPane.showMessageDialog(null, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
                else
                    rebuildFloors();
            }
        });

        btnRow.add(btnSetting, BorderLayout.CENTER);
        btnRow.add(btnDelete, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);
        return card;
    }

    // ── Dialog THÊM PHÒNG ────────────────────────────────────────────────────
    private void showAddDialog() {
        Window owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        JDialog dlg = new JDialog(owner, "Thêm phòng mới",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480, 620);
        dlg.setLocationRelativeTo(owner);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.WHITE);
        root.setBorder(new EmptyBorder(22, 24, 22, 24));

        // Title
        JLabel lblTitle = new JLabel("Thêm phòng mới");
        lblTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 18));
        lblTitle.setForeground(AppColors.SLATE_900);
        lblTitle.setBorder(new EmptyBorder(0, 0, 18, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel();
        form.setBackground(AppColors.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // Tên phòng
        RoundedTextField txtMa = new RoundedTextField(6);
        txtMa.setMaximumSize(new Dimension(420, 40));
        txtMa.setPlaceholder("VD: P1.06");
        JLabel errMa = makeErrLabel();

        // Giá thuê
        RoundedTextField txtGia = new RoundedTextField(6);
        txtGia.setMaximumSize(new Dimension(420, 40));
        txtGia.setPlaceholder("VD: 3000000");

        // Trạng thái
        JComboBox<String> cTT = makeCombo(new String[] { "Trống", "Đã cọc", "Đã thuê", "Đang sửa" });

        // Dịch vụ checkboxes
        JCheckBox[] checks = new JCheckBox[DICH_VU_LIST.length];
        for (int i = 0; i < DICH_VU_LIST.length; i++) {
            checks[i] = new JCheckBox(DICH_VU_LIST[i], false);
            checks[i].setFont(FONT_PLAIN);
            checks[i].setForeground(AppColors.SLATE_900);
            checks[i].setOpaque(false);
        }

        form.add(wrapField("Tên phòng", txtMa));
        form.add(errMa);
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Giá thuê (VNĐ/tháng)", txtGia));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Trạng thái", cTT));
        form.add(Box.createVerticalStrut(12));

        JLabel lblDv = new JLabel("Dịch vụ sử dụng");
        lblDv.setFont(FONT_SMALL);
        lblDv.setForeground(AppColors.SLATE_500);
        form.add(lblDv);
        form.add(Box.createVerticalStrut(6));
        for (JCheckBox cb : checks)
            form.add(cb);

        root.add(form, BorderLayout.CENTER);

        // Nút Thêm phòng
        JButton btnSave = primaryButtonHelper.makePrimaryButton("Thêm phòng");
        btnSave.addActionListener(e -> {
            String ma = txtMa.getText().trim();
            String giaStr = txtGia.getText().trim().replaceAll("[,.]", "");

            // Validate format
            if (!dao.isValidFormat(ma)) {
                errMa.setText("Sai định dạng, nhập dạng T1.01");
                errMa.setVisible(true);
                shakeFocus(txtMa);
                return;
            }
            // Kiểm tra trùng
            if (dao.tonTai(ma)) {
                errMa.setText("Phòng \"" + dao.normalise(ma) + "\" đã tồn tại");
                errMa.setVisible(true);
                shakeFocus(txtMa);
                return;
            }
            errMa.setVisible(false);

            long gia;
            try {
                gia = Long.parseLong(giaStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Giá thuê không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<String> dvChon = new ArrayList<>();
            for (JCheckBox cb : checks)
                if (cb.isSelected())
                    dvChon.add(cb.getText().split(" - ")[0]);

            String err = dao.them(ma, gia, (String) cTT.getSelectedItem(), dvChon);
            if (err != null) {
                JOptionPane.showMessageDialog(dlg, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            rebuildFloors();
            dlg.dispose();
        });

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppColors.WHITE);
        footer.setBorder(new EmptyBorder(16, 0, 0, 0));
        footer.add(btnSave, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ── Dialog CÀI ĐẶT PHÒNG ────────────────────────────────────────────────
    private void showSettingDialog(Phong phong) {
        Window owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        JDialog dlg = new JDialog(owner, "Cài đặt phòng " + phong.getMaPhong(),
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 520);
        dlg.setLocationRelativeTo(owner);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.WHITE);
        root.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel lblTitle = new JLabel("Cài đặt phòng " + phong.getMaPhong());
        lblTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 17));
        lblTitle.setForeground(AppColors.SLATE_900);
        lblTitle.setBorder(new EmptyBorder(0, 0, 18, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setBackground(AppColors.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // JTextField txtGia = makeField(String.valueOf(phong.getGiaThue()));
        JComboBox<String> cTT = makeCombo(new String[] { "Trống", "Đã cọc", "Đã thuê", "Đang sửa" });
        cTT.setSelectedItem(phong.getTrangThai().getTen());

        JCheckBox[] checks = new JCheckBox[DICH_VU_LIST.length];
        for (int i = 0; i < DICH_VU_LIST.length; i++) {
            // String tenDv = DICH_VU_LIST[i].split(" - ")[0];
            // boolean checked = phong.getDichVu() != null &&
            // phong.getDichVu().contains(tenDv);
            // checks[i] = new JCheckBox(DICH_VU_LIST[i], checked);
            checks[i].setFont(FONT_PLAIN);
            checks[i].setForeground(AppColors.SLATE_900);
            checks[i].setOpaque(false);
        }

        // form.add(wrapField("Giá thuê (VNĐ/tháng)", txtGia));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Trạng thái", cTT));
        form.add(Box.createVerticalStrut(12));

        JLabel lblDv = new JLabel("Dịch vụ sử dụng");
        lblDv.setFont(FONT_SMALL);
        lblDv.setForeground(AppColors.SLATE_500);
        form.add(lblDv);
        form.add(Box.createVerticalStrut(6));
        for (JCheckBox cb : checks)
            form.add(cb);

        root.add(form, BorderLayout.CENTER);

        JButton btnSave = primaryButtonHelper.makePrimaryButton("Lưu thay đổi");
        btnSave.addActionListener(e -> {

        });

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppColors.WHITE);
        footer.setBorder(new EmptyBorder(16, 0, 0, 0));
        footer.add(btnSave, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private JLabel createStatusBadge(String text) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(new Font("Be Vietnam Pro", Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        switch (text) {
            case "Đã thuê" -> {
                badge.setForeground(Color.WHITE);
                badge.setBackground(AppColors.RED_500);
            }
            case "Đang sửa" -> {
                badge.setForeground(AppColors.AMBER_FG);
                badge.setBackground(AppColors.AMBER_BG);
            }
            case "Đã cọc" -> {
                badge.setForeground(Color.WHITE);
                badge.setBackground(AppColors.BLUE);
            }
            default -> {
                badge.setForeground(AppColors.GREEN_600);
                badge.setBackground(AppColors.GREEN_BG);
            }
        }
        return badge;
    }

    private JButton makeOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_SMALL);
        btn.setForeground(AppColors.SLATE_900);
        btn.setBackground(AppColors.SLATE_100);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_200, 1),
                new EmptyBorder(5, 10, 5, 10)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeDeleteButton() {
        ImageIcon icon = null;
        try {
            java.net.URL url = getClass().getResource("/bin.png");
            if (url == null) {
                // fallback: load từ thư mục hiện tại
                java.io.File f = new java.io.File("bin.png");
                if (f.exists())
                    url = f.toURI().toURL();
            }
            if (url != null) {
                Image img = new ImageIcon(url).getImage()
                        .getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                icon = new ImageIcon(img);
            }
        } catch (Exception ignored) {
        }

        JButton btn = icon != null ? new JButton(icon) : new JButton("🗑");

        if (icon == null)
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));

        btn.setOpaque(true);
        btn.setBackground(AppColors.RED_500);
        btn.setForeground(AppColors.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.RED, 1, true),
                new EmptyBorder(5, 8, 5, 8)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(36, 30));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(AppColors.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(AppColors.RED_500);
            }
        });
        return btn;
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_PLAIN);
        cb.setBackground(AppColors.WHITE);
        cb.setForeground(AppColors.SLATE_900);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return cb;
    }

    private JPanel wrapField(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setBackground(AppColors.WHITE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(AppColors.SLATE_900);
        p.add(lbl);
        p.add(Box.createVerticalStrut(5));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(field);
        return p;
    }

    private JLabel makeErrLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(AppColors.RED);
        lbl.setVisible(false);
        return lbl;
    }

    private void shakeFocus(JComponent comp) {
        comp.requestFocusInWindow();
        Point origin = comp.getLocation();
        javax.swing.Timer t = new javax.swing.Timer(30, null);
        int[] step = { 0 };
        int[] off = { -6, 6, -4, 4, -2, 2, 0 };
        t.addActionListener(ev -> {
            if (step[0] < off.length)
                comp.setLocation(origin.x + off[step[0]++], origin.y);
            else {
                comp.setLocation(origin);
                t.stop();
            }
        });
        t.start();
    }
}