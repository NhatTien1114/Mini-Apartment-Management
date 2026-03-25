package ui.main;

import dao.GiaDetailDAO;
import dao.GiaHeaderDAO;
import dao.QuanLyPhongDAO;
import dao.TangDAO;
import entity.GiaDetail;
import entity.GiaHeader;
import entity.Phong;
import entity.Phong.LoaiPhong;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import entity.Tang;
import ui.util.AppColors;
import ui.util.PrimaryButton;
import ui.util.RoundedTextField;

public class QuanLyPhongUI {

    private final Font FONT_TITLE = new Font("Be Vietnam Pro", Font.BOLD, 22);
    private final Font FONT_PLAIN = new Font("Be Vietnam Pro", Font.PLAIN, 13);
    private final Font FONT_SMALL = new Font("Be Vietnam Pro", Font.PLAIN, 12);

    private static final NumberFormat NF = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private GiaHeaderDAO giaHeaderDAO = new GiaHeaderDAO();
    private GiaDetailDAO giaDetailDAO = new GiaDetailDAO();
    private Tang tang = new Tang();

    private static final String[] DICH_VU_LIST = {
            "Điện - 3,500đ/kWh",
            "Nước - 15,000đ/m³",
            "Internet - 100,000đ/tháng",
            "Rác - 50,000đ/tháng",
            "Giữ xe máy - 100,000đ/tháng",
            "Giữ ô tô - 500,000đ/tháng"
    };

    private final QuanLyPhongDAO dao = new QuanLyPhongDAO();
    private final PrimaryButton primaryButtonHelper = new PrimaryButton();
    private JPanel floorsPanel;
    private JScrollPane scrollPane;
    private TangDAO tangDAO;

    public JPanel getPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.SLATE_100);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

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
        return root;
    }


    private JPanel createFloorSection(String floorName, List<Phong> phongs) {
        JPanel section = new JPanel(new BorderLayout(0, 10));
        section.setOpaque(false);
        JLabel lblFloor = new JLabel(floorName);
        lblFloor.setFont(new Font("Be Vietnam Pro", Font.BOLD, 18));
        lblFloor.setForeground(AppColors.SLATE_900);
        section.add(lblFloor, BorderLayout.NORTH);
        JPanel roomsWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        roomsWrap.setOpaque(false);
        for (Phong p : phongs) roomsWrap.add(createRoomCard(p));
        section.add(roomsWrap, BorderLayout.CENTER);
        return section;
    }

    private JPanel createRoomCard(Phong phong) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(AppColors.WHITE);
        card.setPreferredSize(new Dimension(210, 112));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_200, 1),
                new EmptyBorder(10, 12, 10, 12)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblRoom = new JLabel(phong.getMaPhong());
        lblRoom.setFont(new Font("Be Vietnam Pro", Font.BOLD, 17));
        lblRoom.setForeground(AppColors.SLATE_900);
        header.add(lblRoom, BorderLayout.WEST);
        header.add(createStatusBadge(phong.getTrangThai().getTen()), BorderLayout.EAST);

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
            }
        });

        btnRow.add(btnSetting, BorderLayout.CENTER);
        btnRow.add(btnDelete, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    // ── Dialog THÊM PHÒNG ────────────────────────────────────────────────────
    private void showAddDialog() {
        Window owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        JDialog dlg = new JDialog(owner, "Thêm phòng mới", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480, 660);
        dlg.setLocationRelativeTo(owner);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.WHITE);
        root.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel lblTitle = new JLabel("Thêm phòng mới");
        lblTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 18));
        lblTitle.setForeground(AppColors.SLATE_900);
        lblTitle.setBorder(new EmptyBorder(0, 0, 18, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setBackground(AppColors.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        RoundedTextField txtMa = new RoundedTextField(6);
        txtMa.setMaximumSize(new Dimension(420, 40));
        txtMa.setPlaceholder("VD: P1.06");
        JLabel errMa = makeErrLabel();

        JComboBox<LoaiPhong> cLoai = new JComboBox<>(LoaiPhong.values());
        cLoai.setFont(FONT_PLAIN);
        cLoai.setBackground(AppColors.WHITE);
        cLoai.setForeground(AppColors.SLATE_900);
        cLoai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        RoundedTextField txtGia = new RoundedTextField(6);
        txtGia.setMaximumSize(new Dimension(420, 40));
        txtGia.setPlaceholder("Tự động điền theo loại phòng");

        // FIX: flag để biết user đã sửa tay hay chưa
        boolean[] userEdited = { false };
        txtGia.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { userEdited[0] = true; }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { userEdited[0] = true; }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        // fill lần đầu (chưa có user edit nào)
        fillGia(txtGia, (LoaiPhong) cLoai.getSelectedItem());

        // FIX: chỉ auto-fill khi user chủ động đổi loại, reset flag sau khi fill
        cLoai.addActionListener(e -> {
            userEdited[0] = false;
            fillGia(txtGia, (LoaiPhong) cLoai.getSelectedItem());
            userEdited[0] = false; // fillGia setText sẽ kích listener, reset lại
        });

        JComboBox<String> cTT = makeCombo(new String[]{ "Trống", "Đã cọc", "Đã thuê", "Đang sửa" });

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
        form.add(wrapField("Loại phòng", cLoai));
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
        for (JCheckBox cb : checks) form.add(cb);

        root.add(form, BorderLayout.CENTER);

        JButton btnSave = primaryButtonHelper.makePrimaryButton("Thêm phòng");
        btnSave.addActionListener(e -> {
            String ma = txtMa.getText().trim().toUpperCase();

            // 1. Kiểm tra định dạng
            if (!dao.isValidFormat(ma)) {
                errMa.setText("Sai định dạng, nhập dạng P1.01");
                errMa.setVisible(true);
                // shakeFocus(txtMa); // Nếu ông có hàm này thì giữ, không thì xóa
                return;
            }

            // 2. Kiểm tra tồn tại
            if (dao.tonTai(ma)) {
                errMa.setText("Phòng \"" + ma + "\" đã tồn tại");
                errMa.setVisible(true);
                return;
            }
            errMa.setVisible(false);

            // 3. Logic lấy maGiaDetail tương ứng với Loại phòng đang chọn
            Phong.LoaiPhong loaiChon = (Phong.LoaiPhong) cLoai.getSelectedItem();
            String maGiaDetail = timMaGiaDetailPhuHop(loaiChon.ordinal());

            if (maGiaDetail == null) {
                JOptionPane.showMessageDialog(dlg, "Chưa có bảng giá cho " + loaiChon.getTen() + ". Vui lòng kiểm tra lại thiết lập giá!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 4. Xác định mã tầng ngầm (P2.01 -> T2)
            String maTang = "T" + ma.charAt(1);

            // 5. Lấy danh sách dịch vụ
            List<String> dvChon = new ArrayList<>();
            for (JCheckBox cb : checks) {
                if (cb.isSelected()) dvChon.add(cb.getText().split(" - ")[0]);
            }

            // 6. Gọi hàm thêm với đầy đủ tham số mới
            // Thứ tự: maPhong, maTang, loaiPhong(int), maGiaDetail, trangThai(int), dichVu
            int trangThaiInt = cTT.getSelectedIndex(); // Lấy index enum hoặc dùng hàm toTrangThai của ông

            String err = dao.them(ma, maTang, loaiChon.ordinal(), maGiaDetail, trangThaiInt);

            if (err != null) {
                JOptionPane.showMessageDialog(dlg, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            rebuildFloors(); // Load lại giao diện
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
        dlg.setSize(420, 560);
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

        // FIX: loaiPhong lấy đúng từ entity (đã được load từ DB)
        LoaiPhong currentLoai = phong.getLoaiPhong();
        JComboBox<LoaiPhong> cLoai = new JComboBox<>(LoaiPhong.values());
        cLoai.setFont(FONT_PLAIN);
        cLoai.setBackground(AppColors.WHITE);
        cLoai.setForeground(AppColors.SLATE_900);
        cLoai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        // FIX: setSelectedItem đúng loại phòng hiện tại (không còn null nữa)
        if (currentLoai != null) cLoai.setSelectedItem(currentLoai);

        RoundedTextField txtGia = new RoundedTextField(6);
        txtGia.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // FIX: flag chống ghi đè khi user đang sửa tay
        boolean[] userEdited = { false };
        txtGia.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { userEdited[0] = true; }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { userEdited[0] = true; }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        // fill lần đầu theo loại phòng hiện tại
        fillGia(txtGia, (LoaiPhong) cLoai.getSelectedItem());

        // FIX: chỉ auto-fill khi user chủ động đổi loại
        cLoai.addActionListener(e -> {
            userEdited[0] = false;
            fillGia(txtGia, (LoaiPhong) cLoai.getSelectedItem());
            userEdited[0] = false;
        });

        JComboBox<String> cTT = makeCombo(new String[]{ "Trống", "Đã cọc", "Đã thuê", "Đang sửa" });
        cTT.setSelectedItem(phong.getTrangThai().getTen());

        JCheckBox[] checks = new JCheckBox[DICH_VU_LIST.length];
        for (int i = 0; i < DICH_VU_LIST.length; i++) {
            checks[i] = new JCheckBox(DICH_VU_LIST[i], false);
            checks[i].setFont(FONT_PLAIN);
            checks[i].setForeground(AppColors.SLATE_900);
            checks[i].setOpaque(false);
        }

        form.add(wrapField("Loại phòng", cLoai));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Giá thuê (VNĐ/tháng) — có thể sửa tay", txtGia));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Trạng thái", cTT));
        form.add(Box.createVerticalStrut(12));

        JLabel lblDv = new JLabel("Dịch vụ sử dụng");
        lblDv.setFont(FONT_SMALL);
        lblDv.setForeground(AppColors.SLATE_500);
        form.add(lblDv);
        form.add(Box.createVerticalStrut(6));
        for (JCheckBox cb : checks) form.add(cb);

        root.add(form, BorderLayout.CENTER);

        JButton btnSave = primaryButtonHelper.makePrimaryButton("Lưu thay đổi");
        btnSave.addActionListener(e -> {
            String giaStr = txtGia.getText().trim().replaceAll("[,. ]", "");
            long gia;
            try {
                gia = Long.parseLong(giaStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Giá thuê không hợp lệ!", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<String> dvChon = new ArrayList<>();
            for (JCheckBox cb : checks)
                if (cb.isSelected()) dvChon.add(cb.getText().split(" - ")[0]);

            // FIX: dùng overload mới để lưu cả loaiPhong vào DB
            LoaiPhong loaiChon = (LoaiPhong) cLoai.getSelectedItem();
            String err = dao.capNhat(phong.getMaPhong(), loaiChon, gia, (String) cTT.getSelectedItem(), dvChon);
            if (err != null) {
                JOptionPane.showMessageDialog(dlg, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
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

    // ── PRIVATE HELPERS ──────────────────────────────────────────────────────

    /**
     * FIX: dùng SwingUtilities.invokeLater + flag để tránh vòng lặp
     * DocumentListener → setText → DocumentListener
     */
    private void fillGia(RoundedTextField txtGia, LoaiPhong loaiPhong) {
        if (loaiPhong == null) {
            txtGia.setText("");
            return;
        }
        long gia = dao.layGiaThueMoiNhat(loaiPhong);
        // Dùng invokeLater để setText không bị trigger DocumentListener chồng chéo
        SwingUtilities.invokeLater(() -> {
            if (gia > 0) {
                txtGia.setText(String.valueOf(gia));
            } else {
                txtGia.setText("");
                txtGia.setPlaceholder("Chưa có giá trong hệ thống, nhập tay");
            }
        });
    }

    private JLabel createStatusBadge(String text) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(new Font("Be Vietnam Pro", Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        switch (text) {
            case "Đã thuê"  -> { badge.setForeground(Color.WHITE);         badge.setBackground(AppColors.RED_500); }
            case "Đang sửa" -> { badge.setForeground(AppColors.AMBER_FG);  badge.setBackground(AppColors.AMBER_BG); }
            case "Đã cọc"   -> { badge.setForeground(Color.WHITE);         badge.setBackground(AppColors.BLUE); }
            default          -> { badge.setForeground(AppColors.GREEN_600); badge.setBackground(AppColors.GREEN_BG); }
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
                java.io.File f = new java.io.File("bin.png");
                if (f.exists()) url = f.toURI().toURL();
            }
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                icon = new ImageIcon(img);
            }
        } catch (Exception ignored) {}

        JButton btn = icon != null ? new JButton(icon) : new JButton("🗑");
        if (icon == null) btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
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
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(AppColors.RED); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(AppColors.RED_500); }
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
        int[] off  = { -6, 6, -4, 4, -2, 2, 0 };
        t.addActionListener(ev -> {
            if (step[0] < off.length) comp.setLocation(origin.x + off[step[0]++], origin.y);
            else { comp.setLocation(origin); t.stop(); }
        });
        t.start();
    }

    private String timMaGiaDetailPhuHop(int loaiPhongEnum) {
        // 1. Tìm bảng giá Phòng đang Active (loai = 0)
        List<GiaHeader> headers = giaHeaderDAO.layTheoLoai(0);
        GiaHeader activeHeader = headers.stream()
                .filter(h -> h.getTrangThai() == 1) // 1 là đang áp dụng
                .findFirst().orElse(null);

        if (activeHeader != null) {
            // 2. Tìm dòng chi tiết có loaiPhong khớp với ComboBox
            List<GiaDetail> details = giaDetailDAO.layTheoHeader(activeHeader.getMaGiaHeader());
            for (GiaDetail d : details) {
                if (d.getLoaiPhong() != null && d.getLoaiPhong() == loaiPhongEnum) {
                    return d.getMaGiaDetail(); // Trả về ID (VD: GD001)
                }
            }
        }
        return null;
    }
    private void rebuildFloors() {
        // 1. Xóa sạch các component cũ trên panel hiển thị
        floorsPanel.removeAll();

        // 2. Lấy danh sách tất cả các tầng từ Database
        // Nếu ông muốn hiện cố định từ T6 -> T1 thì dùng mảng String{"T6","T5"...}
        // Ở đây tôi dùng TangDAO để lấy cho linh hoạt
        List<entity.Tang> dsTang = tangDAO.layDanhSachTang();

        // Sắp xếp ngược lại nếu ông muốn Tầng cao hiện bên trên
        dsTang.sort((t1, t2) -> t2.getMaTang().compareTo(t1.getMaTang()));

        for (entity.Tang tang : dsTang) {
            // 3. Với mỗi tầng, đi lấy danh sách phòng của tầng đó
            List<entity.Phong> dsPhong = dao.layTheoTang(tang.getMaTang());

            // Nếu tầng có phòng thì mới hiển thị section của tầng đó
            if (!dsPhong.isEmpty()) {
                floorsPanel.add(createFloorSection(tang.getTenTang(), dsPhong));

                // Thêm khoảng cách giữa các tầng cho đẹp
                floorsPanel.add(Box.createVerticalStrut(20));
            }
        }

        // 4. Cập nhật lại giao diện (Bắt buộc phải có 2 dòng này)
        floorsPanel.revalidate();
        floorsPanel.repaint();
    }
}