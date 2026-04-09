package ui.main;

import dao.ChiSoDienNuocDAO;
import dao.DichVuDAO;
import dao.GiaDetailDAO;
import dao.GiaHeaderDAO;
import dao.HopDongKhachHangDAO;
import dao.LoaiPhongDAO;
import dao.QuanLyPhongDAO;
import dao.TangDAO;
import entity.ChiSoDienNuoc;
import entity.DichVu;
import entity.GiaDetail;
import entity.GiaHeader;
import entity.KhachHang;
import entity.LoaiPhong;
import entity.Phong;
import entity.Tang;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    // Format giá kiểu 3.500.000
    private static final NumberFormat NF = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private GiaHeaderDAO giaHeaderDAO = new GiaHeaderDAO();
    private GiaDetailDAO giaDetailDAO = new GiaDetailDAO();
    private DichVuDAO dichVuDAO = new DichVuDAO();
    private ChiSoDienNuocDAO chiSoDAO = new ChiSoDienNuocDAO();
    private HopDongKhachHangDAO hopDongKhachHangDAO = new HopDongKhachHangDAO();

    private final QuanLyPhongDAO dao = new QuanLyPhongDAO();
    private final PrimaryButton primaryButtonHelper = new PrimaryButton();
    private JPanel floorsPanel;
    private JScrollPane scrollPane;
    private TangDAO tangDAO = new TangDAO();
    private RoundedTextField txtSearch;

    // ── Filters ──
    private String currentKeyword = "";
    private String selectedStatusFilter = null;
    private String selectedTangFilter = "Tất cả";
    private String selectedLoaiPhongFilter = "Tất cả";

    private final Map<String, JButton> statusFilterButtons = new LinkedHashMap<>();
    private final Map<String, Color> statusFilterColors = new LinkedHashMap<>();
    private final Map<String, Color> statusFilterActiveText = new LinkedHashMap<>();

    // Combo lọc tầng + loại phòng (giống TrangChu)
    private JComboBox<String> cboTangFilter;
    private JComboBox<String> cboLoaiPhongFilter;

    private Runnable onStatusChanged;

    public void setOnStatusChanged(Runnable callback) {
        this.onStatusChanged = callback;
    }

    public void refresh() {
        rebuildFloors();
    }

    // ═══════════════════════════════════════════════════════════
    //  MAIN PANEL
    // ═══════════════════════════════════════════════════════════
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

        JPanel northWrap = new JPanel(new BorderLayout(0, 8));
        northWrap.setOpaque(false);
        northWrap.add(topBar, BorderLayout.NORTH);
        northWrap.add(createSearchAndFilterBar(), BorderLayout.SOUTH);

        root.add(northWrap, BorderLayout.NORTH);

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

    // ═══════════════════════════════════════════════════════════
    //  SEARCH + FILTER BAR
    // ═══════════════════════════════════════════════════════════
    private JPanel createSearchAndFilterBar() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setOpaque(false);

        // Row 1: search + status chips
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        row1.setOpaque(false);

        txtSearch = new RoundedTextField(6);
        txtSearch.setFont(FONT_PLAIN);
        txtSearch.setPlaceholder("Tìm theo mã phòng...");
        txtSearch.setPreferredSize(new Dimension(220, 36));

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setFont(FONT_PLAIN);
        btnSearch.setFocusPainted(false);
        btnSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSearch.setOpaque(true);
        btnSearch.setBackground(AppColors.SLATE_900);
        btnSearch.setForeground(AppColors.WHITE);
        btnSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_900, 1, true), new EmptyBorder(7, 14, 7, 14)));
        btnSearch.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnSearch.setBackground(AppColors.SLATE_600); }
            @Override public void mouseExited(MouseEvent e) { btnSearch.setBackground(AppColors.SLATE_900); }
        });

        Runnable applySearch = () -> {
            currentKeyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
            rebuildFloors();
        };
        btnSearch.addActionListener(e -> applySearch.run());
        txtSearch.addActionListener(e -> applySearch.run());

        row1.add(txtSearch);
        row1.add(btnSearch);
        row1.add(createStatusFilterButton("Trống", AppColors.GREEN_600, AppColors.WHITE));
        row1.add(createStatusFilterButton("Đã cọc", AppColors.BLUE, AppColors.WHITE));
        row1.add(createStatusFilterButton("Đã thuê", AppColors.RED_500, AppColors.WHITE));
        row1.add(createStatusFilterButton("Đang sửa", AppColors.AMBER_FG, AppColors.WHITE));

        // Row 2: lọc tầng + loại phòng (giống TrangChu)
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 2));
        row2.setOpaque(false);

        // Build combo tầng
        List<Tang> dsTang = tangDAO.layDanhSachTang();
        String[] tangOptions = Stream.concat(
                Stream.of("Tất cả"),
                dsTang.stream()
                        .map(Tang::getTenTang)
                        .sorted((t1, t2) -> {
                            try {
                                return Integer.compare(
                                        Integer.parseInt(t1.replaceAll("\\D+", "")),
                                        Integer.parseInt(t2.replaceAll("\\D+", "")));
                            } catch (NumberFormatException ex) { return t1.compareTo(t2); }
                        })
        ).toArray(String[]::new);
        cboTangFilter = new JComboBox<>(tangOptions);
        cboTangFilter.setFont(FONT_PLAIN);
        cboTangFilter.setSelectedItem(selectedTangFilter);
        cboTangFilter.addActionListener(e -> {
            selectedTangFilter = (String) cboTangFilter.getSelectedItem();
            rebuildFloors();
        });

        // Build combo loại phòng
        List<LoaiPhong> dsLoai = new LoaiPhongDAO().layTatCa();
        String[] loaiOptions = Stream.concat(
                Stream.of("Tất cả"),
                dsLoai.stream().map(LoaiPhong::getTen).sorted()
        ).toArray(String[]::new);
        cboLoaiPhongFilter = new JComboBox<>(loaiOptions);
        cboLoaiPhongFilter.setFont(FONT_PLAIN);
        cboLoaiPhongFilter.setSelectedItem(selectedLoaiPhongFilter);
        cboLoaiPhongFilter.addActionListener(e -> {
            selectedLoaiPhongFilter = (String) cboLoaiPhongFilter.getSelectedItem();
            rebuildFloors();
        });

        row2.add(makeFilterLabel("Lọc theo tầng:"));
        row2.add(cboTangFilter);
        row2.add(makeFilterLabel("Lọc theo loại phòng:"));
        row2.add(cboLoaiPhongFilter);

        wrapper.add(row1, BorderLayout.NORTH);
        wrapper.add(row2, BorderLayout.SOUTH);
        return wrapper;
    }

    private JLabel makeFilterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Be Vietnam Pro", Font.BOLD, 13));
        lbl.setForeground(AppColors.SLATE_500);
        return lbl;
    }

    private JButton createStatusFilterButton(String status, Color chipColor, Color activeTextColor) {
        statusFilterColors.put(status, chipColor);
        statusFilterActiveText.put(status, activeTextColor);

        JButton btn = new JButton(status);
        btn.setFont(FONT_SMALL);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.addActionListener(e -> {
            selectedStatusFilter = status.equals(selectedStatusFilter) ? null : status;
            updateFilterButtonStyles();
            rebuildFloors();
        });
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!status.equals(selectedStatusFilter)) btn.setBackground(new Color(248, 250, 252));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!status.equals(selectedStatusFilter)) btn.setBackground(AppColors.WHITE);
            }
        });

        statusFilterButtons.put(status, btn);
        updateFilterButtonStyles();
        return btn;
    }

    private void updateFilterButtonStyles() {
        for (Map.Entry<String, JButton> entry : statusFilterButtons.entrySet()) {
            String status = entry.getKey();
            JButton btn = entry.getValue();
            Color chipColor = statusFilterColors.get(status);
            Color activeText = statusFilterActiveText.get(status);
            if (status.equals(selectedStatusFilter)) {
                btn.setBackground(chipColor);
                btn.setForeground(activeText);
            } else {
                btn.setBackground(AppColors.WHITE);
                btn.setForeground(chipColor);
            }
            btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(chipColor, 1, true), new EmptyBorder(7, 12, 7, 12)));
        }
    }

    private boolean matchesCurrentFilter(Phong phong) {
        if (phong == null) return false;

        // Filter trạng thái
        if (selectedStatusFilter != null) {
            String status = phong.getTrangThai() == null ? "" : phong.getTrangThai().getTen();
            if (!selectedStatusFilter.equals(status)) return false;
        }

        // Filter loại phòng
        if (!"Tất cả".equals(selectedLoaiPhongFilter)) {
            String tenLoai = phong.getLoaiPhong() != null ? phong.getLoaiPhong().getTen() : "";
            if (!selectedLoaiPhongFilter.equals(tenLoai)) return false;
        }

        // Filter tầng
        if (!"Tất cả".equals(selectedTangFilter)) {
            String tenTang = phong.getMaTang() != null ? phong.getMaTang().getTenTang() : "";
            if (!selectedTangFilter.equals(tenTang)) return false;
        }

        // Search keyword
        if (currentKeyword != null && !currentKeyword.isEmpty()) {
            String maPhong = phong.getMaPhong() == null ? "" : phong.getMaPhong().toLowerCase();
            return maPhong.contains(currentKeyword);
        }
        return true;
    }

    // ═══════════════════════════════════════════════════════════
    //  FLOOR SECTION + ROOM CARD
    // ═══════════════════════════════════════════════════════════
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
        boolean isDaThue = phong.getTrangThai() == Phong.TrangThai.THUE;

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
        card.add(header, BorderLayout.NORTH);

        // Nếu đã thuê: hiển thị danh sách dịch vụ ngắn gọn
        if (isDaThue) {
            JPanel pnlDv = new JPanel();
            pnlDv.setOpaque(false);
            pnlDv.setLayout(new BoxLayout(pnlDv, BoxLayout.Y_AXIS));

            // Điện + Nước luôn có
            pnlDv.add(makeDvInfoLabel(layLabelDichVuMacDinh("điện")));
            pnlDv.add(makeDvInfoLabel(layLabelDichVuMacDinh("nước")));

            // Dịch vụ tùy chọn đã đăng ký
            Set<String> maDvDaChon = dichVuDAO.layMaDichVuTheoPhong(phong.getMaPhong());
            List<DichVu> dvTuyChon = layDichVuTuyChon();
            for (DichVu dv : dvTuyChon) {
                if (maDvDaChon.contains(dv.getMaDichVu())) {
                    pnlDv.add(makeDvInfoLabel(formatDichVuLabel(dv)));
                }
            }
            card.add(pnlDv, BorderLayout.CENTER);
        }

        // ── Right-click context menu ──
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem miEdit = new JMenuItem("⚙ Cài đặt");
        miEdit.addActionListener(ev -> showSettingDialog(phong));
        contextMenu.add(miEdit);

        if (isDaThue) {
            JMenuItem miKhach = new JMenuItem("👤 Thông tin khách thuê");
            miKhach.addActionListener(ev -> showKhachThueDialog(phong.getMaPhong()));
            contextMenu.add(miKhach);
        }

        contextMenu.addSeparator();
        JMenuItem miDelete = new JMenuItem("🗑 Xóa phòng");
        miDelete.setForeground(new Color(239, 68, 68));
        miDelete.addActionListener(ev -> {
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
        contextMenu.add(miDelete);

        card.setComponentPopupMenu(contextMenu);
        return card;
    }

    // ═══════════════════════════════════════════════════════════
    //  DIALOG KHÁCH THUÊ
    // ═══════════════════════════════════════════════════════════
    private void showKhachThueDialog(String maPhong) {
        Window owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        JDialog dlg = new JDialog(owner, "Khách thuê phòng " + maPhong, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(320, 220);
        dlg.setLocationRelativeTo(owner);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel lblTitle = new JLabel("Khách thuê phòng " + maPhong);
        lblTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 15));
        lblTitle.setForeground(AppColors.SLATE_900);
        lblTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        // Dùng HopDongKhachHangDAO để lấy người đại diện (vaiTro=0)
        KhachHang kh = hopDongKhachHangDAO.getNguoiDaiDienByMaPhong(maPhong);

        JPanel pnlList = new JPanel();
        pnlList.setBackground(AppColors.WHITE);
        pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.Y_AXIS));

        if (kh == null) {
            JLabel lbl = new JLabel("Không tìm thấy thông tin khách thuê.");
            lbl.setFont(FONT_PLAIN);
            lbl.setForeground(AppColors.SLATE_500);
            pnlList.add(lbl);
        } else {
            JLabel lblTen = new JLabel("• " + kh.getHoTen());
            lblTen.setFont(FONT_PLAIN);
            lblTen.setForeground(AppColors.SLATE_900);
            pnlList.add(lblTen);
        }

        root.add(pnlList, BorderLayout.CENTER);

        JButton btnClose = primaryButtonHelper.makePrimaryButton("Đóng");
        btnClose.addActionListener(e -> dlg.dispose());
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppColors.WHITE);
        footer.setBorder(new EmptyBorder(12, 0, 0, 0));
        footer.add(btnClose, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════
    //  DIALOG THÊM PHÒNG
    // ═══════════════════════════════════════════════════════════
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

        List<LoaiPhong> dsLoai = new LoaiPhongDAO().layTatCa();
        JComboBox<LoaiPhong> cLoai = new JComboBox<>(dsLoai.toArray(new LoaiPhong[0]));
        cLoai.setFont(FONT_PLAIN);
        cLoai.setBackground(AppColors.WHITE);
        cLoai.setForeground(AppColors.SLATE_900);
        cLoai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        RoundedTextField txtGia = new RoundedTextField(6);
        txtGia.setMaximumSize(new Dimension(420, 40));
        txtGia.setPlaceholder("Tự động điền theo loại phòng");

        boolean[] userEdited = {false};
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

        // Trạng thái: không có "Đã thuê" vì phải tạo qua hợp đồng
        JComboBox<String> cTT = makeCombo(new String[]{"Trống", "Đã cọc", "Đang sửa"});

        form.add(wrapField("Tên phòng", txtMa));
        form.add(errMa);
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Loại phòng", cLoai));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Giá thuê (VNĐ/tháng)", txtGia));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Trạng thái", cTT));

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

            LoaiPhong loaiChon = (LoaiPhong) cLoai.getSelectedItem();
            String maGiaDetail = timMaGiaDetailPhuHop(loaiChon.ordinal());
            if (maGiaDetail == null) {
                JOptionPane.showMessageDialog(dlg,
                        "Chưa có bảng giá cho " + loaiChon.getTen() + ". Vui lòng kiểm tra lại thiết lập giá!",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String maTang = "T" + ma.substring(1, ma.indexOf('.'));
            // Map combo index sang trangThaiInt: 0=Trống, 1=Đã cọc, 2=Đang sửa
            int[] ttMap = {0, 3, 2};
            int trangThaiInt = ttMap[cTT.getSelectedIndex()];
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

    // ═══════════════════════════════════════════════════════════
    //  DIALOG CÀI ĐẶT PHÒNG
    // ═══════════════════════════════════════════════════════════
    private void showSettingDialog(Phong phong) {
        boolean isDaThue = phong.getTrangThai() == Phong.TrangThai.THUE;

        Window owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        JDialog dlg = new JDialog(owner, "Cài đặt phòng " + phong.getMaPhong(),
                Dialog.ModalityType.APPLICATION_MODAL);
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
        List<LoaiPhong> dsLoai = new LoaiPhongDAO().layTatCa();
        JComboBox<LoaiPhong> cLoai = new JComboBox<>(dsLoai.toArray(new LoaiPhong[0]));
        cLoai.setFont(FONT_PLAIN);
        cLoai.setBackground(AppColors.WHITE);
        cLoai.setForeground(AppColors.SLATE_900);
        cLoai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        if (phong.getLoaiPhong() != null) cLoai.setSelectedItem(phong.getLoaiPhong());
        // Nếu đã thuê → không cho đổi loại
        cLoai.setEnabled(!isDaThue);

        // ── Giá thuê ──
        RoundedTextField txtGia = new RoundedTextField(6);
        txtGia.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boolean[] userEdited = {false};
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

        // ── Trạng thái: không cho chọn "Đã thuê" (phải qua hợp đồng) ──
        JComboBox<String> cTT = makeCombo(new String[]{"Trống", "Đã cọc", "Đang sửa"});
        if (!isDaThue) {
            // Chọn đúng trạng thái hiện tại
            String tenTT = phong.getTrangThai().getTen();
            if ("Đã cọc".equals(tenTT)) cTT.setSelectedItem("Đã cọc");
            else if ("Đang sửa".equals(tenTT)) cTT.setSelectedItem("Đang sửa");
            else cTT.setSelectedItem("Trống");
        }

        // ── Panel dịch vụ tùy chọn (chỉ hiện khi KHÔNG phải "Đã thuê") ──
        JCheckBox cbDien = makeDichVuMacDinhCheckBox("điện");
        JCheckBox cbNuoc = makeDichVuMacDinhCheckBox("nước");

        List<DichVu> dvTuyChon = layDichVuTuyChon();
        Set<String> maDvDaChon = dichVuDAO.layMaDichVuTheoPhong(phong.getMaPhong());

        JCheckBox[] checks = new JCheckBox[dvTuyChon.size()];
        for (int i = 0; i < dvTuyChon.size(); i++) {
            DichVu dv = dvTuyChon.get(i);
            checks[i] = new JCheckBox(formatDichVuLabel(dv), maDvDaChon.contains(dv.getMaDichVu()));
            checks[i].putClientProperty("maDichVu", dv.getMaDichVu());
            checks[i].setFont(FONT_PLAIN);
            checks[i].setForeground(AppColors.SLATE_900);
            checks[i].setOpaque(false);
        }

        JPanel pnlDichVu = new JPanel();
        pnlDichVu.setBackground(AppColors.WHITE);
        pnlDichVu.setLayout(new BoxLayout(pnlDichVu, BoxLayout.Y_AXIS));
        pnlDichVu.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDv = new JLabel("Dịch vụ sử dụng");
        lblDv.setFont(FONT_SMALL);
        lblDv.setForeground(AppColors.SLATE_500);
        lblDv.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDichVu.add(lblDv);
        pnlDichVu.add(Box.createVerticalStrut(6));
        pnlDichVu.add(cbDien);
        pnlDichVu.add(cbNuoc);
        for (JCheckBox cb : checks) pnlDichVu.add(cb);

        // ── Panel chỉ số điện/nước (chỉ hiện khi "Đã thuê") ──
        LocalDate now = LocalDate.now();
        int[] chiSoCu = chiSoDAO.layChiSoGanNhat(phong.getMaPhong());
        int[] chiSoThangNay = chiSoDAO.layChiSoTheoThang(phong.getMaPhong(), now.getMonthValue(), now.getYear());

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
        if (chiSoThangNay != null) txtDienMoi.setText(String.valueOf(chiSoThangNay[0]));
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
        if (chiSoThangNay != null) txtNuocMoi.setText(String.valueOf(chiSoThangNay[1]));
        rowNuoc.add(wrapField("Số nước cũ (m³)", txtNuocCu));
        rowNuoc.add(wrapField("Số nước mới (m³)", txtNuocMoi));

        pnlChiSo.add(lblChiSo);
        pnlChiSo.add(Box.createVerticalStrut(8));
        pnlChiSo.add(rowDien);
        pnlChiSo.add(Box.createVerticalStrut(8));
        pnlChiSo.add(rowNuoc);

        // ── Panel dịch vụ đang sử dụng (bên dưới chỉ số, chỉ khi "Đã thuê") ──
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
        pnlDvDaChon.add(makeDvInfoLabel(layLabelDichVuMacDinh("điện")));
        pnlDvDaChon.add(makeDvInfoLabel(layLabelDichVuMacDinh("nước")));
        for (DichVu dv : dvTuyChon) {
            if (maDvDaChon.contains(dv.getMaDichVu())) {
                pnlDvDaChon.add(makeDvInfoLabel(formatDichVuLabel(dv)));
            }
        }

        // ── Thêm vào form ──
        form.add(wrapField("Loại phòng", cLoai));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Giá thuê (VNĐ/tháng)", txtGia));
        form.add(Box.createVerticalStrut(10));

        if (!isDaThue) {
            form.add(wrapField("Trạng thái", cTT));
            form.add(Box.createVerticalStrut(12));
            form.add(pnlDichVu);
        } else {
            // Khi đã thuê: hiển thị trạng thái dạng label (không cho đổi)
            JLabel lblTT = new JLabel("Trạng thái");
            lblTT.setFont(FONT_SMALL);
            lblTT.setForeground(AppColors.SLATE_900);
            JLabel lblTTVal = new JLabel("Đã thuê");
            lblTTVal.setFont(FONT_SMALL);
            lblTTVal.setForeground(AppColors.SLATE_500);
            JPanel pTT = new JPanel();
            pTT.setBackground(AppColors.WHITE);
            pTT.setLayout(new BoxLayout(pTT, BoxLayout.Y_AXIS));
            pTT.setAlignmentX(Component.LEFT_ALIGNMENT);
            pTT.add(lblTT);
            pTT.add(Box.createVerticalStrut(4));
            pTT.add(lblTTVal);
            form.add(pTT);
            form.add(Box.createVerticalStrut(12));
            form.add(pnlChiSo);
            form.add(Box.createVerticalStrut(12));
            form.add(pnlDvDaChon);
        }

        root.add(form, BorderLayout.CENTER);

        dlg.setSize(440, isDaThue ? 660 : 560);
        dlg.setLocationRelativeTo(owner);

        // ── Nút Lưu ──
        JButton btnSave = primaryButtonHelper.makePrimaryButton("Lưu thay đổi");
        btnSave.addActionListener(e -> {
            String giaStr = txtGia.getText().trim().replaceAll("[,. ]", "");
            long gia;
            try {
                gia = Long.parseLong(giaStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Giá thuê không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isDaThue) {
                // Chỉ lưu chỉ số điện nước
                int dienMoi, nuocMoi;
                try {
                    dienMoi = Integer.parseInt(txtDienMoi.getText().trim());
                    nuocMoi = Integer.parseInt(txtNuocMoi.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg, "Số điện / số nước mới phải là số nguyên!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (dienMoi < chiSoCu[0]) {
                    JOptionPane.showMessageDialog(dlg, "Số điện mới không thể nhỏ hơn số điện cũ (" + chiSoCu[0] + ")!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (nuocMoi < chiSoCu[1]) {
                    JOptionPane.showMessageDialog(dlg, "Số nước mới không thể nhỏ hơn số nước cũ (" + chiSoCu[1] + ")!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                ChiSoDienNuoc cs = new ChiSoDienNuoc(phong.getMaPhong(), now.getMonthValue(), now.getYear(), dienMoi, nuocMoi);
                String errCs = chiSoDAO.luuHoacCapNhat(cs);
                if (errCs != null) {
                    JOptionPane.showMessageDialog(dlg, errCs, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Giá thuê vẫn cập nhật được kể cả đang thuê
                LoaiPhong loaiChon = (LoaiPhong) cLoai.getSelectedItem();
                dao.capNhat(phong.getMaPhong(), loaiChon, gia, "Đã thuê", new ArrayList<>());
            } else {
                // Lưu dịch vụ tùy chọn
                Set<String> maDvChon = new HashSet<>();
                for (JCheckBox cb : checks)
                    if (cb.isSelected()) maDvChon.add((String) cb.getClientProperty("maDichVu"));
                dichVuDAO.capNhatDichVuPhong(phong.getMaPhong(), maDvChon);

                // Map combo "Trống/Đã cọc/Đang sửa" sang tên trangThai
                String[] ttNames = {"Trống", "Đã cọc", "Đang sửa"};
                String trangThaiChon = ttNames[cTT.getSelectedIndex()];

                LoaiPhong loaiChon = (LoaiPhong) cLoai.getSelectedItem();
                String err = dao.capNhat(phong.getMaPhong(), loaiChon, gia, trangThaiChon, new ArrayList<>());
                if (err != null) {
                    JOptionPane.showMessageDialog(dlg, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
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

    // ═══════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════

    private JCheckBox makeDichVuMacDinhCheckBox(String keyword) {
        JCheckBox cb = new JCheckBox(layLabelDichVuMacDinh(keyword), true);
        cb.setFont(FONT_PLAIN);
        cb.setForeground(AppColors.SLATE_900);
        cb.setOpaque(false);
        cb.setEnabled(false);
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        return cb;
    }

    private String layLabelDichVuMacDinh(String keyword) {
        List<GiaHeader> headers = giaHeaderDAO.layTheoLoai(1);
        GiaHeader activeHeader = headers.stream()
                .filter(h -> h.getTrangThai() == 1).findFirst().orElse(null);
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
        return keyword.equals("điện") ? "Điện" : "Nước";
    }

    private JLabel makeDvInfoLabel(String text) {
        JLabel lbl = new JLabel("• " + text);
        lbl.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 11));
        lbl.setForeground(AppColors.SLATE_900);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void fillGia(RoundedTextField txtGia, LoaiPhong loaiPhong) {
        if (loaiPhong == null) { txtGia.setText(""); return; }
        long gia = dao.layGiaThueMoiNhat(loaiPhong);
        SwingUtilities.invokeLater(() -> {
            if (gia > 0) txtGia.setText(NF.format(gia));
            else { txtGia.setText(""); txtGia.setPlaceholder("Chưa có giá trong hệ thống"); }
        });
    }

    private JLabel createStatusBadge(String text) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(new Font("Be Vietnam Pro", Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        switch (text) {
            case "Đã thuê": badge.setForeground(Color.WHITE); badge.setBackground(AppColors.RED_500); break;
            case "Đang sửa": badge.setForeground(AppColors.AMBER_FG); badge.setBackground(AppColors.AMBER_BG); break;
            case "Đã cọc": badge.setForeground(Color.WHITE); badge.setBackground(AppColors.BLUE); break;
            default: badge.setForeground(AppColors.GREEN_600); badge.setBackground(AppColors.GREEN_BG); break;
        }
        return badge;
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

    private String timMaGiaDetailPhuHop(int loaiPhongOrdinal) {
        List<GiaHeader> headers = giaHeaderDAO.layTheoLoai(0);
        GiaHeader activeHeader = headers.stream()
                .filter(h -> h.getTrangThai() == 1).findFirst().orElse(null);
        if (activeHeader != null) {
            List<GiaDetail> details = giaDetailDAO.layTheoHeader(activeHeader.getMaGiaHeader());
            for (GiaDetail d : details)
                if (d.getLoaiPhong() != null && d.getLoaiPhong() == loaiPhongOrdinal)
                    return d.getMaGiaDetail();
        }
        return null;
    }

    private List<DichVu> layDichVuTuyChon() {
        List<DichVu> result = new ArrayList<>();
        List<GiaHeader> headers = giaHeaderDAO.layTheoLoai(1);
        GiaHeader activeHeader = headers.stream()
                .filter(h -> h.getTrangThai() == 1).findFirst().orElse(null);
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

    private String formatDichVuLabel(DichVu dv) {
        String ten = dv.getTenDichVu() != null ? dv.getTenDichVu() : "";
        String donVi = dv.getDonVi() != null ? dv.getDonVi() : "tháng";
        if (dv.getDonGia() != null && dv.getDonGia() > 0) {
            long gia = dv.getDonGia().longValue();
            return ten + " - " + NF.format(gia) + "đ/" + donVi;
        }
        return ten;
    }

    // ═══════════════════════════════════════════════════════════
    //  REBUILD
    // ═══════════════════════════════════════════════════════════
    private void rebuildFloors() {
        if (floorsPanel == null) return;
        floorsPanel.removeAll();
        List<Tang> dsTang = tangDAO.layDanhSachTang();
        dsTang.sort((t1, t2) -> {
            try {
                return Integer.compare(
                        Integer.parseInt(t1.getMaTang().replaceAll("\\D+", "")),
                        Integer.parseInt(t2.getMaTang().replaceAll("\\D+", "")));
            } catch (NumberFormatException e) { return t2.getMaTang().compareTo(t1.getMaTang()); }
        });
        // Đảo ngược: tầng cao hiển thị trước
        java.util.Collections.reverse(dsTang);

        boolean hasAnyRoom = false;
        for (Tang tang : dsTang) {
            List<Phong> dsPhong = dao.layTheoTang(tang.getMaTang());
            List<Phong> dsLoc = dsPhong.stream().filter(this::matchesCurrentFilter).collect(Collectors.toList());
            if (!dsLoc.isEmpty()) {
                hasAnyRoom = true;
                floorsPanel.add(createFloorSection(tang.getTenTang(), dsLoc));
                floorsPanel.add(Box.createVerticalStrut(20));
            }
        }

        if (!hasAnyRoom) {
            JLabel emptyLabel = new JLabel("Không có phòng phù hợp với bộ lọc hiện tại", SwingConstants.CENTER);
            emptyLabel.setFont(FONT_PLAIN);
            emptyLabel.setForeground(AppColors.SLATE_500);
            floorsPanel.add(emptyLabel);
        }

        floorsPanel.revalidate();
        floorsPanel.repaint();
    }
}