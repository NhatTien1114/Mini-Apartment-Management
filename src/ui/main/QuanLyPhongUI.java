package ui.main;

import dao.ChiSoDienNuocDAO;
import dao.DichVuDAO;
import dao.GiaDetailDAO;
import dao.GiaHeaderDAO;
import dao.QuanLyPhongDAO;
import dao.TangDAO;
import entity.ChiSoDienNuoc;
import entity.DichVu;
import entity.GiaDetail;
import entity.GiaHeader;
import entity.Phong;
import entity.Phong.LoaiPhong;
import entity.Tang;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    private static final NumberFormat NF = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private GiaHeaderDAO giaHeaderDAO = new GiaHeaderDAO();
    private GiaDetailDAO giaDetailDAO = new GiaDetailDAO();
    private DichVuDAO dichVuDAO = new DichVuDAO();
    private ChiSoDienNuocDAO chiSoDAO = new ChiSoDienNuocDAO();
    private Tang tang = new Tang();

    private final QuanLyPhongDAO dao = new QuanLyPhongDAO();
    private final PrimaryButton primaryButtonHelper = new PrimaryButton();
    private JPanel floorsPanel;
    private JScrollPane scrollPane;
    private TangDAO tangDAO = new TangDAO();

    private Runnable onStatusChanged;

    public void setOnStatusChanged(Runnable callback) {
        this.onStatusChanged = callback;
    }

    public void refresh() {
        rebuildFloors();
    }

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
        rebuildFloors();
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
        for (Phong p : phongs)
            roomsWrap.add(createRoomCard(p));
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
                else
                    rebuildFloors();
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
        dlg.setSize(480, 700);
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

        boolean[] userEdited = { false };
        txtGia.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { userEdited[0] = true; }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { userEdited[0] = true; }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        fillGia(txtGia, (LoaiPhong) cLoai.getSelectedItem());

        cLoai.addActionListener(e -> {
            userEdited[0] = false;
            fillGia(txtGia, (LoaiPhong) cLoai.getSelectedItem());
            userEdited[0] = false;
        });

        JComboBox<String> cTT = makeCombo(new String[] { "Trống", "Đã cọc", "Đã thuê", "Đang sửa" });

        // ── Label tiêu đề dịch vụ ──
        JLabel lblDv = new JLabel("Dịch vụ sử dụng");
        lblDv.setFont(FONT_SMALL);
        lblDv.setForeground(AppColors.SLATE_500);

        // ── Điện + Nước: tick sẵn, disabled, giá từ DB ──
        JCheckBox cbDien = makeDichVuMacDinhCheckBox("điện");
        JCheckBox cbNuoc = makeDichVuMacDinhCheckBox("nước");

        // ── Dịch vụ tùy chọn (đã bỏ Điện/Nước) ──
        List<DichVu> dvTuyChon = layDichVuTuyChon();
        JCheckBox[] checks = new JCheckBox[dvTuyChon.size()];
        for (int i = 0; i < dvTuyChon.size(); i++) {
            DichVu dv = dvTuyChon.get(i);
            checks[i] = new JCheckBox(formatDichVuLabel(dv), false);
            checks[i].putClientProperty("maDichVu", dv.getMaDichVu());
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
        form.add(lblDv);
        form.add(Box.createVerticalStrut(6));
        form.add(cbDien);
        form.add(cbNuoc);
        for (JCheckBox cb : checks)
            form.add(cb);

        root.add(form, BorderLayout.CENTER);

        JButton btnSave = primaryButtonHelper.makePrimaryButton("Thêm phòng");
        btnSave.addActionListener(e -> {
            String ma = txtMa.getText().trim().toUpperCase();

            if (!dao.isValidFormat(ma)) {
                errMa.setText("Sai định dạng, nhập dạng P1.01");
                errMa.setVisible(true);
                return;
            }
            if (dao.tonTai(ma)) {
                errMa.setText("Phòng \"" + ma + "\" đã tồn tại");
                errMa.setVisible(true);
                return;
            }
            errMa.setVisible(false);

            Phong.LoaiPhong loaiChon = (Phong.LoaiPhong) cLoai.getSelectedItem();
            String maGiaDetail = timMaGiaDetailPhuHop(loaiChon.ordinal());
            if (maGiaDetail == null) {
                JOptionPane.showMessageDialog(dlg,
                        "Chưa có bảng giá cho " + loaiChon.getTen() + ". Vui lòng kiểm tra lại thiết lập giá!",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String maTang = "T" + ma.substring(1, ma.indexOf('.'));
            int trangThaiInt = cTT.getSelectedIndex();
            String err = dao.them(ma, maTang, loaiChon.ordinal(), maGiaDetail, trangThaiInt);
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
        dlg.setSize(440, 600);
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

        // ── Loại phòng ──
        LoaiPhong currentLoai = phong.getLoaiPhong();
        JComboBox<LoaiPhong> cLoai = new JComboBox<>(LoaiPhong.values());
        cLoai.setFont(FONT_PLAIN);
        cLoai.setBackground(AppColors.WHITE);
        cLoai.setForeground(AppColors.SLATE_900);
        cLoai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        if (currentLoai != null) cLoai.setSelectedItem(currentLoai);

        // ── Giá thuê ──
        RoundedTextField txtGia = new RoundedTextField(6);
        txtGia.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boolean[] userEdited = { false };
        txtGia.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { userEdited[0] = true; }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { userEdited[0] = true; }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        fillGia(txtGia, (LoaiPhong) cLoai.getSelectedItem());
        cLoai.addActionListener(e -> {
            userEdited[0] = false;
            fillGia(txtGia, (LoaiPhong) cLoai.getSelectedItem());
            userEdited[0] = false;
        });

        // ── Trạng thái ──
        JComboBox<String> cTT = makeCombo(new String[] { "Trống", "Đã cọc", "Đã thuê", "Đang sửa" });
        cTT.setSelectedItem(phong.getTrangThai().getTen());

        // ── Panel dịch vụ tùy chọn (ẩn/hiện theo trạng thái) ──
        JLabel lblDv = new JLabel("Dịch vụ sử dụng");
        lblDv.setFont(FONT_SMALL);
        lblDv.setForeground(AppColors.SLATE_500);
        lblDv.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Điện + Nước: tick sẵn, disabled
        JCheckBox cbDien = makeDichVuMacDinhCheckBox("điện");
        JCheckBox cbNuoc = makeDichVuMacDinhCheckBox("nước");

        List<DichVu> dvTuyChon = layDichVuTuyChon();
        JCheckBox[] checks = new JCheckBox[dvTuyChon.size()];
        for (int i = 0; i < dvTuyChon.size(); i++) {
            DichVu dv = dvTuyChon.get(i);
            checks[i] = new JCheckBox(formatDichVuLabel(dv), false);
            checks[i].putClientProperty("maDichVu", dv.getMaDichVu());
            checks[i].setFont(FONT_PLAIN);
            checks[i].setForeground(AppColors.SLATE_900);
            checks[i].setOpaque(false);
        }

        JPanel pnlDichVu = new JPanel();
        pnlDichVu.setBackground(AppColors.WHITE);
        pnlDichVu.setLayout(new BoxLayout(pnlDichVu, BoxLayout.Y_AXIS));
        pnlDichVu.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDichVu.add(lblDv);
        pnlDichVu.add(Box.createVerticalStrut(6));
        pnlDichVu.add(cbDien);
        pnlDichVu.add(cbNuoc);
        for (JCheckBox cb : checks) pnlDichVu.add(cb);

        // ── Panel chỉ số điện/nước (chỉ hiện khi "Đã thuê") ──
        LocalDate now = LocalDate.now();
        int[] chiSoCu = chiSoDAO.layChiSoThangTruoc(phong.getMaPhong(), now.getMonthValue(), now.getYear());

        JPanel pnlChiSo = new JPanel();
        pnlChiSo.setBackground(AppColors.WHITE);
        pnlChiSo.setLayout(new BoxLayout(pnlChiSo, BoxLayout.Y_AXIS));
        pnlChiSo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblChiSo = new JLabel("Chỉ số điện - nước tháng " + now.getMonthValue() + "/" + now.getYear());
        lblChiSo.setFont(FONT_SMALL);
        lblChiSo.setForeground(AppColors.SLATE_500);
        lblChiSo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel rowDien = new JPanel(new GridLayout(1, 2, 10, 0));
        rowDien.setBackground(AppColors.WHITE);
        rowDien.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowDien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        RoundedTextField txtDienCu = new RoundedTextField(6);
        txtDienCu.setText(String.valueOf(chiSoCu[0]));
        txtDienCu.setEditable(false);
        txtDienCu.setBackground(AppColors.SLATE_100);

        RoundedTextField txtDienMoi = new RoundedTextField(6);
        txtDienMoi.setPlaceholder("Nhập số mới");

        rowDien.add(wrapField("Số điện cũ (kWh)", txtDienCu));
        rowDien.add(wrapField("Số điện mới (kWh)", txtDienMoi));

        JPanel rowNuoc = new JPanel(new GridLayout(1, 2, 10, 0));
        rowNuoc.setBackground(AppColors.WHITE);
        rowNuoc.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowNuoc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        RoundedTextField txtNuocCu = new RoundedTextField(6);
        txtNuocCu.setText(String.valueOf(chiSoCu[1]));
        txtNuocCu.setEditable(false);
        txtNuocCu.setBackground(AppColors.SLATE_100);

        RoundedTextField txtNuocMoi = new RoundedTextField(6);
        txtNuocMoi.setPlaceholder("Nhập số mới");

        rowNuoc.add(wrapField("Số nước cũ (m³)", txtNuocCu));
        rowNuoc.add(wrapField("Số nước mới (m³)", txtNuocMoi));

        pnlChiSo.add(lblChiSo);
        pnlChiSo.add(Box.createVerticalStrut(8));
        pnlChiSo.add(rowDien);
        pnlChiSo.add(Box.createVerticalStrut(8));
        pnlChiSo.add(rowNuoc);

        // ── Panel dịch vụ đang sử dụng — hiện bên dưới chỉ số khi "Đã thuê" ──
        JPanel pnlDvDaChon = new JPanel();
        pnlDvDaChon.setBackground(AppColors.WHITE);
        pnlDvDaChon.setLayout(new BoxLayout(pnlDvDaChon, BoxLayout.Y_AXIS));
        pnlDvDaChon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDvDaChon = new JLabel("Dịch vụ đang sử dụng");
        lblDvDaChon.setFont(FONT_SMALL);
        lblDvDaChon.setForeground(AppColors.SLATE_500);
        lblDvDaChon.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlDvDaChon.add(lblDvDaChon);
        pnlDvDaChon.add(Box.createVerticalStrut(6));
        // Điện + Nước luôn hiện
        pnlDvDaChon.add(makeDvInfoLabel(layLabelDichVuMacDinh("điện")));
        pnlDvDaChon.add(makeDvInfoLabel(layLabelDichVuMacDinh("nước")));

        // Dịch vụ tùy chọn: tạo label tương ứng với từng checkbox trong checks,
        // visible = trạng thái checked của checkbox đó, sync real-time
        JLabel[] dvDaChonLabels = new JLabel[dvTuyChon.size()];
        for (int i = 0; i < dvTuyChon.size(); i++) {
            dvDaChonLabels[i] = makeDvInfoLabel(formatDichVuLabel(dvTuyChon.get(i)));
            dvDaChonLabels[i].setVisible(checks[i].isSelected());
            pnlDvDaChon.add(dvDaChonLabels[i]);
        }
        for (int i = 0; i < checks.length; i++) {
            final int idx = i;
            checks[i].addItemListener(ev ->
                    dvDaChonLabels[idx].setVisible(checks[idx].isSelected())
            );
        }

        // ── Thêm tất cả vào form ──
        form.add(wrapField("Loại phòng", cLoai));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Giá thuê (VNĐ/tháng)", txtGia));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Trạng thái", cTT));
        form.add(Box.createVerticalStrut(12));
        form.add(pnlDichVu);
        form.add(Box.createVerticalStrut(12));
        form.add(pnlChiSo);
        form.add(Box.createVerticalStrut(12));
        form.add(pnlDvDaChon);

        // ── Ẩn/hiện theo trạng thái hiện tại ──
        boolean isDaThue = "Đã thuê".equals(phong.getTrangThai().getTen());
        pnlDichVu.setVisible(!isDaThue);
        pnlChiSo.setVisible(isDaThue);
        pnlDvDaChon.setVisible(isDaThue);
        dlg.setSize(440, isDaThue ? 660 : 580);

        // ── Listener khi đổi trạng thái ──
        cTT.addActionListener(e -> {
            boolean isThue = "Đã thuê".equals(cTT.getSelectedItem());
            pnlDichVu.setVisible(!isThue);
            pnlChiSo.setVisible(isThue);
            pnlDvDaChon.setVisible(isThue);
            dlg.setSize(440, isThue ? 660 : 580);
            dlg.revalidate();
        });

        root.add(form, BorderLayout.CENTER);

        // ── Nút Lưu ──
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

            String trangThaiChon = (String) cTT.getSelectedItem();

            // Nếu đang là "Đã thuê" → lưu chỉ số điện/nước
            if ("Đã thuê".equals(trangThaiChon)) {
                int dienMoi, nuocMoi;
                try {
                    dienMoi = Integer.parseInt(txtDienMoi.getText().trim());
                    nuocMoi = Integer.parseInt(txtNuocMoi.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg,
                            "Số điện / số nước mới phải là số nguyên!", "Lỗi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (dienMoi < chiSoCu[0]) {
                    JOptionPane.showMessageDialog(dlg,
                            "Số điện mới không thể nhỏ hơn số điện cũ (" + chiSoCu[0] + ")!", "Lỗi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (nuocMoi < chiSoCu[1]) {
                    JOptionPane.showMessageDialog(dlg,
                            "Số nước mới không thể nhỏ hơn số nước cũ (" + chiSoCu[1] + ")!", "Lỗi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                ChiSoDienNuoc cs = new ChiSoDienNuoc(
                        phong.getMaPhong(),
                        now.getMonthValue(),
                        now.getYear(),
                        dienMoi,
                        nuocMoi
                );
                String errCs = chiSoDAO.luuHoacCapNhat(cs);
                if (errCs != null) {
                    JOptionPane.showMessageDialog(dlg, errCs, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Lấy danh sách dịch vụ tùy chọn (chỉ khi không phải "Đã thuê")
            List<String> dvChon = new ArrayList<>();
            if (!"Đã thuê".equals(trangThaiChon)) {
                for (JCheckBox cb : checks)
                    if (cb.isSelected())
                        dvChon.add(cb.getText().split(" - ")[0]);
            }

            LoaiPhong loaiChon = (LoaiPhong) cLoai.getSelectedItem();
            String err = dao.capNhat(phong.getMaPhong(), loaiChon, gia, trangThaiChon, dvChon);
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

    // ── PRIVATE HELPERS ──────────────────────────────────────────────────────

    /**
     * Tạo checkbox Điện hoặc Nước: tick sẵn, disabled, giá lấy từ bảng giá DB active.
     */
    private JCheckBox makeDichVuMacDinhCheckBox(String keyword) {
        JCheckBox cb = new JCheckBox(layLabelDichVuMacDinh(keyword), true);
        cb.setFont(FONT_PLAIN);
        cb.setForeground(AppColors.SLATE_900);
        cb.setOpaque(false);
        cb.setEnabled(false);
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        return cb;
    }

    /**
     * Lấy label "Điện - 1,500đ/kWh" hoặc "Nước - 3,000đ/m³" từ bảng giá active.
     */
    private String layLabelDichVuMacDinh(String keyword) {
        List<GiaHeader> headers = giaHeaderDAO.layTheoLoai(1);
        GiaHeader activeHeader = headers.stream()
                .filter(h -> h.getTrangThai() == 1)
                .findFirst().orElse(null);

        if (activeHeader != null) {
            List<GiaDetail> details = giaDetailDAO.layTheoHeader(activeHeader.getMaGiaHeader());
            List<DichVu> tatCa = dichVuDAO.layTatCa();
            for (GiaDetail d : details) {
                if (d.getMaDichVu() == null) continue;
                for (DichVu dv : tatCa) {
                    if (dv.getMaDichVu().equals(d.getMaDichVu())) {
                        String ten = dv.getTenDichVu() == null ? "" : dv.getTenDichVu().toLowerCase();
                        if (ten.contains(keyword)) {
                            dv.setDonGia(d.getDonGia());
                            return formatDichVuLabel(dv);
                        }
                    }
                }
            }
        }
        // Fallback nếu chưa có bảng giá
        return keyword.equals("điện") ? "Điện" : "Nước";
    }

    /** Label "• text" hiển thị dịch vụ đang dùng (read-only) */
    private JLabel makeDvInfoLabel(String text) {
        JLabel lbl = new JLabel("• " + text);
        lbl.setFont(FONT_PLAIN);
        lbl.setForeground(AppColors.SLATE_900);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void fillGia(RoundedTextField txtGia, LoaiPhong loaiPhong) {
        if (loaiPhong == null) {
            txtGia.setText("");
            return;
        }
        long gia = dao.layGiaThueMoiNhat(loaiPhong);
        SwingUtilities.invokeLater(() -> {
            if (gia > 0) {
                txtGia.setText(String.valueOf(gia));
            } else {
                txtGia.setText("");
                txtGia.setPlaceholder("Chưa có giá trong hệ thống ");
            }
        });
    }

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
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(AppColors.RED_500); }
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

    private String timMaGiaDetailPhuHop(int loaiPhongEnum) {
        List<GiaHeader> headers = giaHeaderDAO.layTheoLoai(0);
        GiaHeader activeHeader = headers.stream()
                .filter(h -> h.getTrangThai() == 1)
                .findFirst().orElse(null);
        if (activeHeader != null) {
            List<GiaDetail> details = giaDetailDAO.layTheoHeader(activeHeader.getMaGiaHeader());
            for (GiaDetail d : details) {
                if (d.getLoaiPhong() != null && d.getLoaiPhong() == loaiPhongEnum) {
                    return d.getMaGiaDetail();
                }
            }
        }
        return null;
    }

    /**
     * Load dịch vụ tùy chọn từ bảng giá dịch vụ đang active (loai=1, trangThai=1).
     * Loại bỏ Điện (DV02) và Nước (DV01) vì đó là mặc định bắt buộc.
     */
    private List<DichVu> layDichVuTuyChon() {
        List<DichVu> result = new ArrayList<>();
        List<GiaHeader> headers = giaHeaderDAO.layTheoLoai(1); // loai=1: DichVu
        GiaHeader activeHeader = headers.stream()
                .filter(h -> h.getTrangThai() == 1)
                .findFirst().orElse(null);
        if (activeHeader == null) return result;

        List<GiaDetail> details = giaDetailDAO.layTheoHeader(activeHeader.getMaGiaHeader());
        List<DichVu> tatCaDichVu = dichVuDAO.layTatCa();

        for (GiaDetail d : details) {
            if (d.getMaDichVu() == null) continue;
            for (DichVu dv : tatCaDichVu) {
                if (dv.getMaDichVu().equals(d.getMaDichVu())) {
                    String ten = dv.getTenDichVu() == null ? "" : dv.getTenDichVu().toLowerCase();
                    if (ten.contains("điện") || ten.contains("nước")) break;
                    dv.setDonGia(d.getDonGia());
                    result.add(dv);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Format label hiển thị cho checkbox dịch vụ:
     * VD: "Internet - 100,000đ/tháng"
     */
    private String formatDichVuLabel(DichVu dv) {
        String ten = dv.getTenDichVu() != null ? dv.getTenDichVu() : "";
        String donVi = dv.getDonVi() != null ? dv.getDonVi() : "tháng";
        if (dv.getDonGia() != null && dv.getDonGia() > 0) {
            long gia = dv.getDonGia().longValue();
            return ten + " - " + NF.format(gia) + "đ/" + donVi;
        }
        return ten;
    }

    private void rebuildFloors() {
        if (floorsPanel == null) return;
        floorsPanel.removeAll();
        List<Tang> dsTang = tangDAO.layDanhSachTang();
        dsTang.sort((t1, t2) -> t2.getMaTang().compareTo(t1.getMaTang()));
        for (Tang tang : dsTang) {
            List<entity.Phong> dsPhong = dao.layTheoTang(tang.getMaTang());
            if (!dsPhong.isEmpty()) {
                floorsPanel.add(createFloorSection(tang.getTenTang(), dsPhong));
                floorsPanel.add(Box.createVerticalStrut(20));
            }
        }
        floorsPanel.revalidate();
        floorsPanel.repaint();
    }
}