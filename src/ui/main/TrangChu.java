package ui.main;

import dao.QuanLyPhongDAO;
import entity.Phong;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import ui.util.AppColors;
import ui.util.PhongInfo;
import ui.util.RoundedButton;

public class TrangChu extends JFrame {
    // ===== LAYOUT COMPONENTS =====
    private CardLayout cardLayout;
    private JPanel pnlContent;
    private JPanel pnlTrangChuContent;
    private JButton[] menuButtons;
    private int selectedMenuIndex = 0;

    // ===== THÔNG TIN USER =====
    private final entity.TaiKhoan taiKhoan;
    private String tenTaiKhoan;
    private String role;
    private JLabel lblTen;
    private JLabel lblRole;
    private final QuanLyPhongDAO phongDAO = new QuanLyPhongDAO();

    public TrangChu(entity.TaiKhoan tk) {
        this.taiKhoan = tk;
        extractUserInfo();
        initUI();
    }

    private void extractUserInfo() {
        if (this.taiKhoan instanceof entity.Chu) {
            this.tenTaiKhoan = ((entity.Chu) this.taiKhoan).getHoTen();
            this.role = "Chủ";
        } else if (this.taiKhoan instanceof entity.QuanLy) {
            this.tenTaiKhoan = ((entity.QuanLy) this.taiKhoan).getHoTen();
            this.role = "Quản lý";
        } else {
            this.tenTaiKhoan = "Người dùng";
            this.role = "Khách";
        }
    }

    public void updateUserInfo() {
        extractUserInfo();
        if (lblTen != null)
            lblTen.setText(this.tenTaiKhoan);
        if (lblRole != null)
            lblRole.setText(this.role);
    }

    private void initUI() {
        setTitle("Mini Apartment - Trang Chủ");
        setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createMenuPanel(), BorderLayout.WEST);
        add(createMainPanel(), BorderLayout.CENTER);
        setVisible(true);
    }

    // ================= MENU BÊN TRÁI =================
    private JPanel createMenuPanel() {
        JPanel pnlMenu = new JPanel();
        pnlMenu.setBackground(AppColors.MENU_BG);
        pnlMenu.setPreferredSize(new Dimension(230, 0));
        pnlMenu.setLayout(new BorderLayout());

        String duongDanLogo = "img/logo/logo2.png";
        ImageIcon iconLogo = new ImageIcon(duongDanLogo);
        Image img = iconLogo.getImage();
        Image imgScale = img.getScaledInstance(240, 164, Image.SCALE_SMOOTH);
        ImageIcon iconMoi = new ImageIcon(imgScale);

        JLabel lblLogo = new JLabel(iconMoi);
        lblLogo.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel pnlLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlLogo.setBackground(AppColors.MENU_BG);
        pnlLogo.add(lblLogo);

        pnlMenu.add(pnlLogo, BorderLayout.NORTH);

        JPanel pnlDanhSach = new JPanel();
        pnlDanhSach.setBackground(AppColors.MENU_BG);
        pnlDanhSach.setLayout(new GridLayout(9, 1, 0, 10));
        pnlDanhSach.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] danhSachMenu = {
                "Trang chủ", "Hợp đồng", "Quản lý phòng", "Khách hàng",
                "Phương tiện", "Doanh thu", "Dịch vụ", "Hóa đơn", "Bảng giá"
        };

        String[] danhSachIcon = {
                "img/icons/home.png", "img/icons/google-docs.png",
                "img/icons/settings.png", "img/icons/user.png",
                "img/icons/bike.png", "img/icons/bar-chart.png",
                "img/icons/support.png", "img/icons/bill.png",
                "img/icons/menu.png"
        };

        menuButtons = new JButton[danhSachMenu.length];

        for (int i = 0; i < danhSachMenu.length; i++) {
            int index = i;
            JButton btn = new JButton(danhSachMenu[i]);
            menuButtons[i] = btn;

            ImageIcon iconGoc = new ImageIcon(danhSachIcon[i]);
            Image imgIcon = iconGoc.getImage();
            Image imgIconScale = imgIcon.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(imgIconScale));

            btn.setFocusPainted(false);
            btn.setForeground(Color.WHITE);
            btn.setBackground(AppColors.MENU_BG);
            btn.setBorderPainted(false);
            btn.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 16));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setIconTextGap(12);

            if (i == 0)
                btn.setBackground(AppColors.MENU_HOVER);

            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (selectedMenuIndex != index)
                        btn.setBackground(AppColors.MENU_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (selectedMenuIndex != index)
                        btn.setBackground(AppColors.MENU_BG);
                }
            });

            btn.addActionListener(e -> selectMenuTab(index));

            pnlDanhSach.add(btn);
        }

        pnlMenu.add(pnlDanhSach, BorderLayout.CENTER);
        return pnlMenu;
    }

    // ================= MENU SELECTION HANDLER =================
    private void selectMenuTab(int index) {
        menuButtons[selectedMenuIndex].setBackground(AppColors.MENU_BG);
        selectedMenuIndex = index;
        menuButtons[selectedMenuIndex].setBackground(AppColors.MENU_HOVER);

        if (index == 0) {
            refreshTrangChuTab();
        }

        cardLayout.show(pnlContent, String.valueOf(index));
    }

    // ================= PANEL CHÍNH =================
    private JPanel createMainPanel() {
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBackground(AppColors.APP_BACKGROUND);

        pnlMain.add(createHeaderPanel(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(AppColors.APP_BACKGROUND);

        pnlTrangChuContent = createTrangChuContent();
        pnlContent.add(pnlTrangChuContent, "0");
        pnlContent.add(new HopDongUI().getPanel(), "1");
        pnlContent.add(new QuanLyPhongUI().getPanel(), "2");
        pnlContent.add(new KhachHangUI().getPanel(), "3");
        pnlContent.add(new PhuongTienUI().getPanel(), "4");
        pnlContent.add(new DoanhThuUI().getPanel(), "5");
        pnlContent.add(new DichVuUI().getPanel(), "6");
        pnlContent.add(new HoaDonUI().getPanel(), "7");
        pnlContent.add(new BangGiaUI().getPanel(), "8");

        pnlMain.add(pnlContent, BorderLayout.CENTER);
        return pnlMain;
    }

    private void refreshTrangChuTab() {
        if (pnlContent == null) {
            return;
        }

        if (pnlTrangChuContent != null) {
            pnlContent.remove(pnlTrangChuContent);
        }

        pnlTrangChuContent = createTrangChuContent();
        pnlContent.add(pnlTrangChuContent, "0");
        pnlContent.revalidate();
        pnlContent.repaint();
    }

    // ================= HEADER =================
    private JPanel createHeaderPanel() {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setPreferredSize(new Dimension(0, 70));
        pnlHeader.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlUser.setBackground(Color.WHITE);
        pnlUser.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon iconAvatar = new ImageIcon("img/icons/avatar.png");
        Image imgAvatar = iconAvatar.getImage();
        Image imgAvatarScale = imgAvatar.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon iconAvatarMoi = new ImageIcon(imgAvatarScale);
        JLabel lblAvatar = new JLabel(iconAvatarMoi);
        lblRole = new JLabel(role);
        lblRole.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 16));

        lblTen = new JLabel(tenTaiKhoan);
        lblTen.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));

        JPanel pnlThongTin = new JPanel();
        pnlThongTin.setLayout(new BoxLayout(pnlThongTin, BoxLayout.Y_AXIS));
        pnlThongTin.setBackground(Color.WHITE);
        pnlThongTin.add(lblRole);
        pnlThongTin.add(lblTen);

        pnlUser.add(lblAvatar);
        pnlUser.add(Box.createHorizontalStrut(10));
        pnlUser.add(pnlThongTin);

        pnlUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new AccountInfoDialog(TrangChu.this, taiKhoan).setVisible(true);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                pnlUser.setBackground(new Color(245, 245, 245));
                pnlThongTin.setBackground(new Color(245, 245, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                pnlUser.setBackground(Color.WHITE);
                pnlThongTin.setBackground(Color.WHITE);
            }
        });

        pnlHeader.add(pnlUser, BorderLayout.WEST);

        JButton btnDangXuat = new JButton("Đăng xuất");
        btnDangXuat.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 16));
        btnDangXuat.setBackground(Color.WHITE);
        btnDangXuat.addActionListener(e -> {
            new LoginUI().setVisible(true);
            this.dispose();
        });
        pnlHeader.add(btnDangXuat, BorderLayout.EAST);

        return pnlHeader;
    }

    // ================= TRANG CHỦ CONTENT =================
    private JPanel createTrangChuContent() {
        JPanel pnlTrangChu = new JPanel();
        pnlTrangChu.setLayout(new BorderLayout(20, 20));
        pnlTrangChu.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnlTrangChu.setBackground(AppColors.APP_BACKGROUND);

        pnlTrangChu.add(createThongKePanel(), BorderLayout.NORTH);
        pnlTrangChu.add(createSoDoPhongPanel(), BorderLayout.CENTER);

        return pnlTrangChu;
    }

    // ================= THỐNG KÊ =================
    private JPanel createThongKePanel() {
        JPanel pnlThongKe = new JPanel(new GridLayout(1, 4, 10, 0));
        pnlThongKe.setBackground(AppColors.APP_BACKGROUND);
        pnlThongKe.setPreferredSize(new Dimension(0, 70));

        List<Phong> dsPhong = layDanhSachPhong();
        int soPhongTrong = demTheoTrangThai(dsPhong, "Trống");
        int soPhongDaThue = demTheoTrangThai(dsPhong, "Đã thuê");
        int soPhongDaCoc = demTheoTrangThai(dsPhong, "Đã cọc");
        int soPhongSua = demTheoTrangThai(dsPhong, "Đang sửa");

        pnlThongKe.add(createCard(String.valueOf(soPhongTrong), "Trống", AppColors.GREEN));
        pnlThongKe.add(createCard(String.valueOf(soPhongDaThue), "Đã thuê", AppColors.RED));
        pnlThongKe.add(createCard(String.valueOf(soPhongDaCoc), "Đã cọc", AppColors.BLUE));
        pnlThongKe.add(createCard(String.valueOf(soPhongSua), "Sửa chữa", AppColors.WARNING));

        return pnlThongKe;
    }

    private JPanel createCard(String so, String tieuDe, Color mau) {
        JPanel pnlCard = new JPanel();
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));

        JLabel lblSo = new JLabel(so);
        lblSo.setOpaque(true);
        lblSo.setBackground(mau);
        lblSo.setForeground(Color.WHITE);
        lblSo.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 16));
        lblSo.setHorizontalAlignment(SwingConstants.CENTER);
        lblSo.setPreferredSize(new Dimension(40, 30));

        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 16));

        pnlCard.add(lblSo);
        pnlCard.add(lblTieuDe);

        return pnlCard;
    }

    // ================= SƠ ĐỒ PHÒNG =================
    private JPanel createSoDoPhongPanel() {
        JPanel pnlSoDo = new JPanel();
        pnlSoDo.setLayout(new GridLayout(6, 1, 10, 10));
        pnlSoDo.setBackground(Color.WHITE);
        pnlSoDo.setBorder(new EmptyBorder(20, 20, 20, 20));

        pnlSoDo.add(createTang("TẦNG 6", phongDAO.layTheoTang("T6")));
        pnlSoDo.add(createTang("TẦNG 5", phongDAO.layTheoTang("T5")));
        pnlSoDo.add(createTang("TẦNG 4", phongDAO.layTheoTang("T4")));
        pnlSoDo.add(createTang("TẦNG 3", phongDAO.layTheoTang("T3")));
        pnlSoDo.add(createTang("TẦNG 2", phongDAO.layTheoTang("T2")));
        pnlSoDo.add(createTang("TẦNG 1", phongDAO.layTheoTang("T1")));

        return pnlSoDo;
    }

    private JPanel createTang(String tenTang, List<Phong> dsPhong) {
        JPanel pnlTang = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlTang.setBackground(Color.WHITE);

        JLabel lblTang = new JLabel(tenTang);
        lblTang.setFont(new Font("Be Vietnam Pro", Font.BOLD, 16));
        lblTang.setPreferredSize(new Dimension(80, 30));
        pnlTang.add(lblTang);

        if (dsPhong == null || dsPhong.isEmpty()) {
            JLabel lblTrong = new JLabel("Chưa có phòng");
            lblTrong.setForeground(Color.GRAY);
            lblTrong.setFont(new Font("Be Vietnam Pro", Font.ITALIC, 14));
            pnlTang.add(lblTrong);
            return pnlTang;
        }

        for (Phong p : dsPhong) {
            RoundedButton btnPhong = new RoundedButton(p.getMaPhong(), 12);
            btnPhong.setForeground(Color.WHITE);
            btnPhong.setBackground(mauTheoTrangThai(p.getTrangThai().getTen()));
            btnPhong.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 16));
            btnPhong.setPreferredSize(new Dimension(106, 60));

            btnPhong.addActionListener(e -> new PhongInfo(p.getMaPhong()).showDialog());

            pnlTang.add(btnPhong);
        }

        return pnlTang;
    }

    private List<Phong> layDanhSachPhong() {
        try {
            return phongDAO.layTatCa();
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private int demTheoTrangThai(List<Phong> dsPhong, String trangThai) {
        int dem = 0;
        for (Phong phong : dsPhong) {
            if (trangThai.equals(phong.getTrangThai().getTen())) {
                dem++;
            }
        }
        return dem;
    }

    private Color mauTheoTrangThai(String trangThai) {
        if ("Đã thuê".equals(trangThai)) {
            return AppColors.RED;
        }
        if ("Đã cọc".equals(trangThai)) {
            return AppColors.BLUE;
        }
        if ("Đang sửa".equals(trangThai)) {
            return AppColors.WARNING;
        }
        return AppColors.GREEN;
    }

    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}