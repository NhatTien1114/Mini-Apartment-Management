package ui.main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import ui.util.PhongInfo;
import ui.util.RoundedButton;

public class TrangChu extends JFrame{

    // ===== MÀU HỆ THỐNG =====
    private final Color MAU_XANH = Color.decode("#28AF60");
    private final Color MAU_XANH_DUONG = Color.decode("#308CE8");
    private final Color MAU_DO = Color.decode("#DC2828");
    private final Color MAU_VANG = Color.decode("#E7B008");
    private final Color MAU_MENU = Color.decode("#121721");
    private final Color MAU_NEN = new Color(229, 231, 235);
    private final Color MAU_MENU_HOVER = Color.decode("#1F2937");
    
    // ===== LAYOUT COMPONENTS =====
    private CardLayout cardLayout;
    private JPanel pnlContent;
    private JButton[] menuButtons;
    private int selectedMenuIndex = 0;

    // ===== THÔNG TIN USER =====
    private final String tenTaiKhoan;
    private final String role;

    public TrangChu(String tenTaiKhoan, String role) {
        this.tenTaiKhoan = tenTaiKhoan;
        this.role = role;
        initUI();
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
        pnlMenu.setBackground(MAU_MENU);
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
        pnlLogo.setBackground(MAU_MENU);
        pnlLogo.add(lblLogo);

        pnlMenu.add(pnlLogo, BorderLayout.NORTH);

        JPanel pnlDanhSach = new JPanel();
        pnlDanhSach.setBackground(MAU_MENU);
        pnlDanhSach.setLayout(new GridLayout(10, 1, 0, 10));
        pnlDanhSach.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] danhSachMenu = {
                "Trang chủ", "Hợp đồng", "Quản lý", "Khách thuê",
                "Phương tiện", "Doanh thu", "Dịch vụ", "Hóa đơn"
        };

        String[] danhSachIcon = {
                "img/icons/home.png", "img/icons/google-docs.png",
                "img/icons/settings.png", "img/icons/user.png",
                "img/icons/bike.png", "img/icons/bar-chart.png",
                "img/icons/support.png", "img/icons/bill.png"
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
            btn.setBackground(MAU_MENU);
            btn.setBorderPainted(false);
            btn.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 16));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setIconTextGap(12);
            
            if (i == 0) btn.setBackground(MAU_MENU_HOVER);
            
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (selectedMenuIndex != index) btn.setBackground(MAU_MENU_HOVER);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    if (selectedMenuIndex != index) btn.setBackground(MAU_MENU);
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
        menuButtons[selectedMenuIndex].setBackground(MAU_MENU);
        selectedMenuIndex = index;
        menuButtons[selectedMenuIndex].setBackground(MAU_MENU_HOVER);
        cardLayout.show(pnlContent, String.valueOf(index));
    }

    // ================= PANEL CHÍNH =================
    private JPanel createMainPanel() {
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBackground(MAU_NEN);

        pnlMain.add(createHeaderPanel(), BorderLayout.NORTH);
        
        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(MAU_NEN);
        
        pnlContent.add(createTrangChuContent(), "0");
        pnlContent.add(new HopDongUI().getPanel(), "1");
        pnlContent.add(new QuanLyPhongUI().getPanel(), "2");
        pnlContent.add(new KhachThueUI().getPanel(), "3");
        pnlContent.add(new PhuongTienUI().getPanel(), "4");
        pnlContent.add(new DoanhThuUI().getPanel(), "5");
        pnlContent.add(new DichVuUI().getPanel(), "6");
        pnlContent.add(new HoaDonUI().getPanel(), "7");
        
        pnlMain.add(pnlContent, BorderLayout.CENTER);
        return pnlMain;
    }

    // ================= HEADER =================
    private JPanel createHeaderPanel() {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setPreferredSize(new Dimension(0, 70));
        pnlHeader.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlUser.setBackground(Color.WHITE);

        ImageIcon iconAvatar = new ImageIcon("img/icons/avatar.png");
        Image imgAvatar = iconAvatar.getImage();
        Image imgAvatarScale = imgAvatar.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon iconAvatarMoi = new ImageIcon(imgAvatarScale);
        JLabel lblAvatar = new JLabel(iconAvatarMoi);
        JLabel lblRole = new JLabel(role);
        lblRole.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 16));

        JLabel lblTen = new JLabel(tenTaiKhoan);
        lblTen.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));

        JPanel pnlThongTin = new JPanel();
        pnlThongTin.setLayout(new BoxLayout(pnlThongTin, BoxLayout.Y_AXIS));
        pnlThongTin.setBackground(Color.WHITE);
        pnlThongTin.add(lblRole);
        pnlThongTin.add(lblTen);

        pnlUser.add(lblAvatar);
        pnlUser.add(Box.createHorizontalStrut(10));
        pnlUser.add(pnlThongTin);

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
        pnlTrangChu.setBackground(MAU_NEN);

        pnlTrangChu.add(createThongKePanel(), BorderLayout.NORTH);
        pnlTrangChu.add(createSoDoPhongPanel(), BorderLayout.CENTER);

        return pnlTrangChu;
    }

    // ================= THỐNG KÊ =================
    private JPanel createThongKePanel() {
        JPanel pnlThongKe = new JPanel(new GridLayout(1, 4, 10, 0));
        pnlThongKe.setBackground(MAU_NEN);
        pnlThongKe.setPreferredSize(new Dimension(0, 70));

        pnlThongKe.add(createCard("15", "Trống", MAU_XANH));
        pnlThongKe.add(createCard("12", "Đã thuê", MAU_DO));
        pnlThongKe.add(createCard("2", "Đã cọc", MAU_XANH_DUONG));
        pnlThongKe.add(createCard("1", "Sữa chữa", MAU_VANG));

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

        pnlSoDo.add(createTang("TẦNG 6", new String[] { "T6.01", "T6.02", "T6.03", "T6.04" }, MAU_XANH));
        pnlSoDo.add(createTang("TẦNG 5", new String[] { "T5.01", "T5.02", "T5.03", "T5.04", "T5.05" }, MAU_XANH));
        pnlSoDo.add(createTang("TẦNG 4", new String[] { "T4.01", "T4.02", "T4.03", "T4.04", "T4.05" }, null));
        pnlSoDo.add(createTang("TẦNG 3", new String[] { "T3.01", "T3.02", "T3.03", "T3.04", "T3.05" }, null));
        pnlSoDo.add(createTang("TẦNG 2", new String[] { "T2.01", "T2.02", "T2.03", "T2.04", "T2.05", "T2.06" }, null));
        pnlSoDo.add(createTang("TẦNG 1", new String[] { "T1.01", "T1.02", "T1.03", "T1.04", "T1.05" }, null));

        return pnlSoDo;
    }

    private JPanel createTang(String tenTang, String[] dsPhong, Color macDinh) {
        JPanel pnlTang = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlTang.setBackground(Color.WHITE);

        JLabel lblTang = new JLabel(tenTang);
        lblTang.setFont(new Font("Be Vietnam Pro", Font.BOLD, 16));
        lblTang.setPreferredSize(new Dimension(80, 30));
        pnlTang.add(lblTang);

        for (String p : dsPhong) {
            RoundedButton btnPhong = new RoundedButton(p, 12);
            btnPhong.setForeground(Color.WHITE);
            btnPhong.setBackground(macDinh != null ? macDinh : MAU_XANH);
            btnPhong.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 16));
            btnPhong.setPreferredSize(new Dimension(106, 60));
            
            btnPhong.addActionListener(e -> new PhongInfo(p).showDialog());
            
            pnlTang.add(btnPhong);
        }

        return pnlTang;
    }

    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}