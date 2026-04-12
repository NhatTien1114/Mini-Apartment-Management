package ui.util;

import dao.GiaDetailDAO;
import dao.HopDongDAO;
import dao.HopDongKhachHangDAO;
import dao.QuanLyPhongDAO;
import entity.HopDong;
import entity.KhachHang;
import entity.Phong;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PhongInfo {

    private final String tenPhong;
    private final Color MAU_XANH_DUONG = AppColors.PRIMARY_SOFT;

    QuanLyPhongDAO phongDAO = new QuanLyPhongDAO();
    GiaDetailDAO donGiaDAO = new GiaDetailDAO();
    HopDongKhachHangDAO HDKH_DAO = new HopDongKhachHangDAO();
    HopDongDAO hopDongDAO = new HopDongDAO();

    public PhongInfo(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public void showDialog() {
        Phong phong = phongDAO.layTheoMa(tenPhong);

        JDialog dialog = new JDialog();
        dialog.setTitle(tenPhong);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setModal(true);

        JPanel pnlMain = new JPanel(new BorderLayout(10, 10));
        pnlMain.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnlMain.setBackground(AppColors.WHITE);

        JPanel pnlContent;
        if (phong.getTrangThai() == Phong.TrangThai.THUE) {
            pnlContent = createContentForPhongDaThue(phong);
            dialog.setSize(700, 400);
        } else {
            pnlContent = createContentForPhongTrong(phong);
            dialog.setSize(700, 280);
        }

        JScrollPane scrollPane = new JScrollPane(pnlContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        pnlMain.add(scrollPane, BorderLayout.CENTER);

        JPanel pnlBottom = createBottomPanel(dialog);
        pnlMain.add(pnlBottom, BorderLayout.SOUTH);

        dialog.add(pnlMain);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    // ================= CONTENT PANELS =================
    private JPanel createContentForPhongTrong(Phong phong) {
        JPanel pnlContainer = new JPanel();
        pnlContainer.setLayout(new BoxLayout(pnlContainer, BoxLayout.Y_AXIS));
        pnlContainer.setBackground(Color.WHITE);
        pnlContainer.setBorder(new EmptyBorder(0, 0, 0, 0));

        // --- Thông tin phòng ---
        String giaThue = String.valueOf(donGiaDAO.getDonGiaByMa(phong.getMaGiaDetail()).getDonGia());
        String tang = phong.getMaTang() != null ? phong.getMaTang().getTenTang() : "";
        String toa = (phong.getMaTang() != null && phong.getMaTang().getToa() != null)
                ? phong.getMaTang().getToa().getTenToa()
                : "";

        JPanel pnlRoomRow1 = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlRoomRow1.setBackground(Color.WHITE);
        JTextField txtTenPhong = new JTextField(tenPhong);
        txtTenPhong.setEditable(false);
        JTextField txtLoaiPhong = new JTextField(phong.getLoaiPhong() != null ? phong.getLoaiPhong().getTen() : "");
        txtLoaiPhong.setEditable(false);
        JTextField txtTrangThai = new JTextField(phong.getTrangThai() != null ? phong.getTrangThai().getTen() : "");
        txtTrangThai.setEditable(false);
        pnlRoomRow1.add(createFieldPanel(new JLabel("Tên phòng"), txtTenPhong));
        pnlRoomRow1.add(createFieldPanel(new JLabel("Loại phòng"), txtLoaiPhong));
        pnlRoomRow1.add(createFieldPanel(new JLabel("Trạng thái"), txtTrangThai));

        JPanel pnlRoomRow2 = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlRoomRow2.setBackground(Color.WHITE);
        JTextField txtToa = new JTextField(toa);
        txtToa.setEditable(false);
        JTextField txtTang = new JTextField(tang);
        txtTang.setEditable(false);
        JTextField txtSoNguoi = new JTextField(String.valueOf(phong.getSoNguoiHienTai()));
        txtSoNguoi.setEditable(false);
        pnlRoomRow2.add(createFieldPanel(new JLabel("Tòa"), txtToa));
        pnlRoomRow2.add(createFieldPanel(new JLabel("Tầng"), txtTang));
        pnlRoomRow2.add(createFieldPanel(new JLabel("Số người hiện tại"), txtSoNguoi));

        JPanel pnlRoomRow3 = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlRoomRow3.setBackground(Color.WHITE);
        JTextField txtGiaThue = new JTextField(giaThue);
        txtGiaThue.setEditable(false);
        pnlRoomRow3.add(createFieldPanel(new JLabel("Giá thuê"), txtGiaThue));
        pnlRoomRow3.add(Box.createGlue());
        pnlRoomRow3.add(Box.createGlue());

        pnlContainer.add(pnlRoomRow1);
        pnlContainer.add(Box.createVerticalStrut(10));
        pnlContainer.add(pnlRoomRow2);
        pnlContainer.add(Box.createVerticalStrut(10));
        pnlContainer.add(pnlRoomRow3);
        pnlContainer.add(Box.createVerticalGlue());

        return pnlContainer;
    }

    private JPanel createContentForPhongDaThue(Phong phong) {
        JPanel pnlContainer = new JPanel();
        pnlContainer.setLayout(new BoxLayout(pnlContainer, BoxLayout.Y_AXIS));
        pnlContainer.setBackground(Color.WHITE);
        pnlContainer.setBorder(new EmptyBorder(0, 0, 0, 0));

        // --- Thông tin phòng ---
        String giaThue = String.valueOf(donGiaDAO.getDonGiaByMa(phong.getMaGiaDetail()).getDonGia());
        String tang = phong.getMaTang() != null ? phong.getMaTang().getTenTang() : "";
        String toa = (phong.getMaTang() != null && phong.getMaTang().getToa() != null)
                ? phong.getMaTang().getToa().getTenToa()
                : "";

        JPanel pnlRoomRow1 = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlRoomRow1.setBackground(Color.WHITE);
        JTextField txtTenPhong = new JTextField(tenPhong);
        txtTenPhong.setEditable(false);
        JTextField txtLoaiPhong = new JTextField(phong.getLoaiPhong() != null ? phong.getLoaiPhong().getTen() : "");
        txtLoaiPhong.setEditable(false);
        JTextField txtTrangThai = new JTextField(phong.getTrangThai() != null ? phong.getTrangThai().getTen() : "");
        txtTrangThai.setEditable(false);
        pnlRoomRow1.add(createFieldPanel(new JLabel("Tên phòng"), txtTenPhong));
        pnlRoomRow1.add(createFieldPanel(new JLabel("Loại phòng"), txtLoaiPhong));
        pnlRoomRow1.add(createFieldPanel(new JLabel("Trạng thái"), txtTrangThai));

        JPanel pnlRoomRow2 = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlRoomRow2.setBackground(Color.WHITE);
        JTextField txtToa = new JTextField(toa);
        txtToa.setEditable(false);
        JTextField txtTang = new JTextField(tang);
        txtTang.setEditable(false);
        JTextField txtSoNguoi = new JTextField(String.valueOf(phong.getSoNguoiHienTai()));
        txtSoNguoi.setEditable(false);
        pnlRoomRow2.add(createFieldPanel(new JLabel("Tòa"), txtToa));
        pnlRoomRow2.add(createFieldPanel(new JLabel("Tầng"), txtTang));
        pnlRoomRow2.add(createFieldPanel(new JLabel("Số người hiện tại"), txtSoNguoi));

        JPanel pnlRoomRow3 = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlRoomRow3.setBackground(Color.WHITE);
        JTextField txtGiaThue = new JTextField(giaThue);
        txtGiaThue.setEditable(false);
        pnlRoomRow3.add(createFieldPanel(new JLabel("Giá thuê"), txtGiaThue));
        pnlRoomRow3.add(Box.createGlue());
        pnlRoomRow3.add(Box.createGlue());

        pnlContainer.add(pnlRoomRow1);
        pnlContainer.add(Box.createVerticalStrut(10));
        pnlContainer.add(pnlRoomRow2);
        pnlContainer.add(Box.createVerticalStrut(10));
        pnlContainer.add(pnlRoomRow3);

        // --- Separator ---
        pnlContainer.add(Box.createVerticalStrut(15));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        pnlContainer.add(sep);
        pnlContainer.add(Box.createVerticalStrut(15));

        // --- Thông tin hợp đồng (read-only) ---
        KhachHang kh = HDKH_DAO.getNguoiDaiDienByMaPhong(phong.getMaPhong());
        HopDong hd = hopDongDAO.getHopDongByMaPhong(phong.getMaPhong());

        JPanel pnlRow1 = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlRow1.setBackground(Color.WHITE);

        JTextField txtTenKhachThueLocal = new JTextField(kh.getHoTen());
        JTextField txtPhoneLocal = new JTextField(kh.getSoDienThoai());
        JTextField txtCccdLocal = new JTextField(kh.getSoCCCD());
        txtTenKhachThueLocal.setEditable(false);
        txtPhoneLocal.setEditable(false);
        txtCccdLocal.setEditable(false);

        pnlRow1.add(createFieldPanel(new JLabel("Tên khách thuê"), txtTenKhachThueLocal));
        pnlRow1.add(createFieldPanel(new JLabel("SĐT"), txtPhoneLocal));
        pnlRow1.add(createFieldPanel(new JLabel("CCCD"), txtCccdLocal));

        JPanel pnlRow2 = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlRow2.setBackground(Color.WHITE);

        JTextField txtStartDateLocal = new JTextField(String.valueOf(hd.getNgayBatDau()));
        JTextField txtEndDateLocal = new JTextField(String.valueOf(hd.getNgayKetThuc()));
        JTextField txtDepositLocal = new JTextField(String.valueOf(hd.getTienCoc()));
        txtStartDateLocal.setEditable(false);
        txtEndDateLocal.setEditable(false);
        txtDepositLocal.setEditable(false);

        pnlRow2.add(createFieldPanel(new JLabel("Ngày bắt đầu"), txtStartDateLocal));
        pnlRow2.add(createFieldPanel(new JLabel("Ngày kết thúc"), txtEndDateLocal));
        pnlRow2.add(createFieldPanel(new JLabel("Tiền cọc"), txtDepositLocal));

        JPanel pnlRow3 = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlRow3.setBackground(Color.WHITE);

        JTextField txtMonthlyRentLocal = new JTextField(String.valueOf(hd.getTienThueThang()));
        txtMonthlyRentLocal.setEditable(false);
        JComboBox<String> cboConditionLocal = new JComboBox<>(new String[] { "Đã thuê" });

        pnlRow3.add(createFieldPanel(new JLabel("Tiền thuê/tháng"), txtMonthlyRentLocal));
        pnlRow3.add(createFieldPanel(new JLabel("Trạng thái"), cboConditionLocal));

        pnlContainer.add(pnlRow1);
        pnlContainer.add(Box.createVerticalStrut(10));
        pnlContainer.add(pnlRow2);
        pnlContainer.add(Box.createVerticalStrut(10));
        pnlContainer.add(pnlRow3);
        pnlContainer.add(Box.createVerticalGlue());

        return pnlContainer;
    }

    private JPanel createFieldPanel(JLabel label, JComponent field) {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBackground(Color.WHITE);

        label.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (field instanceof JTextField jTextField) {
            jTextField.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 12));
        } else if (field instanceof JComboBox) {
            ((JComboBox<?>) field).setFont(new Font("Be Vietnam Pro", Font.PLAIN, 12));
        }

        extracted(field);
        extracted2(field);

        pnl.add(label);
        pnl.add(Box.createVerticalStrut(5));
        pnl.add(field);

        return pnl;
    }

    private void extracted2(JComponent field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    }

    private void extracted(JComponent field) {
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    // ================= BOTTOM PANEL =================
    private JPanel createBottomPanel(JDialog dialog) {
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBottom.setBackground(Color.WHITE);

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));
        btnClose.setPreferredSize(new Dimension(100, 40));
        btnClose.addActionListener(e -> dialog.dispose());

        pnlBottom.add(btnClose);

        return pnlBottom;
    }
}
