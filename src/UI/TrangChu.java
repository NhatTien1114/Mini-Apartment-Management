package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TrangChu extends JFrame {

    // ===== MÀU HỆ THỐNG =====
    private final Color MAU_XANH = Color.decode("#28AF60");
    private final Color MAU_XANH_DUONG = Color.decode("#308CE8");
    private final Color MAU_DO = Color.decode("#DC2828");
    private final Color MAU_VANG = Color.decode("#E7B008");
    private final Color MAU_MENU = Color.decode("#121721");
    private final Color MAU_NEN = new Color(245, 247, 250);

    // ===== THÔNG TIN USER (truyền từ đăng nhập) =====
    private String tenTaiKhoan;
    private String role;

    public TrangChu(String tenTaiKhoan, String role) {
        this.tenTaiKhoan = tenTaiKhoan;
        this.role = role;

        initUI();
    }

    private void initUI() {
        setTitle("Mini Apartment - Trang Chu");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createMenuPanel(), BorderLayout.WEST);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    // ================= MENU BÊN TRÁI =================
    private JPanel createMenuPanel() {
        JPanel pnlMenu = new JPanel();
        pnlMenu.setBackground(MAU_MENU);
        pnlMenu.setPreferredSize(new Dimension(230, 0));
        pnlMenu.setLayout(new BorderLayout());

        // Logo
        JPanel pnlLogo = new JPanel();
        pnlLogo.setBackground(MAU_MENU);
        pnlLogo.setBorder(new EmptyBorder(20, 10, 20, 10));

        String duongDanLogo = "C:\\Users\\Admin\\Documents\\Phát triển ứng dụng\\Quản lý chung cư mini\\Quản lý chung cư mini\\img\\logo.png";
        ImageIcon icon = new ImageIcon(duongDanLogo);
        JLabel lblLogo = new JLabel(icon);
        pnlLogo.add(lblLogo);

        pnlMenu.add(pnlLogo, BorderLayout.NORTH);

        // Menu Items
        JPanel pnlDanhSach = new JPanel();
        pnlDanhSach.setBackground(MAU_MENU);
        pnlDanhSach.setLayout(new GridLayout(10, 1, 0, 10));
        pnlDanhSach.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] danhSachMenu = {
                "Trang chu",
                "Hop dong",
                "Quan ly",
                "Khach thue",
                "Phuong tien",
                "Doanh thu",
                "Dich vu",
                "Hoa don"
        };

        for (String item : danhSachMenu) {
            JButton btn = new JButton(item);
            btn.setFocusPainted(false);
            btn.setForeground(Color.WHITE);
            btn.setBackground(MAU_MENU);
            btn.setBorderPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            pnlDanhSach.add(btn);
        }

        pnlMenu.add(pnlDanhSach, BorderLayout.CENTER);

        return pnlMenu;
    }

    // ================= PANEL CHÍNH =================
    private JPanel createMainPanel() {
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBackground(MAU_NEN);

        pnlMain.add(createHeaderPanel(), BorderLayout.NORTH);
        pnlMain.add(createContentPanel(), BorderLayout.CENTER);

        return pnlMain;
    }

    // ================= HEADER =================
    private JPanel createHeaderPanel() {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setPreferredSize(new Dimension(0, 70));
        pnlHeader.setBorder(new EmptyBorder(10, 20, 10, 20));

        // User info
        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlUser.setBackground(Color.WHITE);

        JLabel lblAvatar = new JLabel("👤");
        JLabel lblRole = new JLabel(role);
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel lblTen = new JLabel(tenTaiKhoan);
        lblTen.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel pnlThongTin = new JPanel();
        pnlThongTin.setLayout(new BoxLayout(pnlThongTin, BoxLayout.Y_AXIS));
        pnlThongTin.setBackground(Color.WHITE);
        pnlThongTin.add(lblRole);
        pnlThongTin.add(lblTen);

        pnlUser.add(lblAvatar);
        pnlUser.add(Box.createHorizontalStrut(10));
        pnlUser.add(pnlThongTin);

        pnlHeader.add(pnlUser, BorderLayout.WEST);

        JButton btnDangXuat = new JButton("Dang xuat");
        pnlHeader.add(btnDangXuat, BorderLayout.EAST);

        return pnlHeader;
    }

    // ================= CONTENT =================
    private JPanel createContentPanel() {
        JPanel pnlContent = new JPanel();
        pnlContent.setLayout(new BorderLayout());
        pnlContent.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnlContent.setBackground(MAU_NEN);

        pnlContent.add(createThongKePanel(), BorderLayout.NORTH);
        pnlContent.add(createSoDoPhongPanel(), BorderLayout.CENTER);

        return pnlContent;
    }

    // ================= THỐNG KÊ =================
    private JPanel createThongKePanel() {
        JPanel pnlThongKe = new JPanel(new GridLayout(1, 4, 15, 0));
        pnlThongKe.setBackground(MAU_NEN);
        pnlThongKe.setPreferredSize(new Dimension(0, 90));

        pnlThongKe.add(createCard("15", "Trong", MAU_XANH));
        pnlThongKe.add(createCard("12", "Da thue", MAU_DO));
        pnlThongKe.add(createCard("2", "Da coc", MAU_XANH_DUONG));
        pnlThongKe.add(createCard("1", "Sua chua", MAU_VANG));

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
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSo.setHorizontalAlignment(SwingConstants.CENTER);
        lblSo.setPreferredSize(new Dimension(40, 30));

        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 14));

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

        pnlSoDo.add(createTang("TANG 6", new String[] { "T6.01", "T6.02", "T6.03", "T6.04" }, MAU_XANH));
        pnlSoDo.add(createTang("TANG 5", new String[] { "T5.01", "T5.02", "T5.03", "T5.04", "T5.05" }, MAU_XANH));
        pnlSoDo.add(createTang("TANG 4", new String[] { "T4.01", "T4.02", "T4.03", "T4.04", "T4.05" }, null));
        pnlSoDo.add(createTang("TANG 3", new String[] { "T3.01", "T3.02", "T3.03", "T3.04", "T3.05" }, null));
        pnlSoDo.add(createTang("TANG 2", new String[] { "T2.01", "T2.02", "T2.03", "T2.04", "T2.05", "T2.06" }, null));
        pnlSoDo.add(createTang("TANG 1", new String[] { "T1.01", "T1.02", "T1.03", "T1.04", "T1.05" }, null));

        return pnlSoDo;
    }

    private JPanel createTang(String tenTang, String[] dsPhong, Color macDinh) {
        JPanel pnlTang = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlTang.setBackground(Color.WHITE);

        JLabel lblTang = new JLabel(tenTang);
        lblTang.setPreferredSize(new Dimension(80, 30));
        pnlTang.add(lblTang);

        for (String p : dsPhong) {
            JButton btnPhong = new JButton(p);
            btnPhong.setForeground(Color.WHITE);
            btnPhong.setBackground(macDinh != null ? macDinh : MAU_XANH);
            btnPhong.setFocusPainted(false);
            pnlTang.add(btnPhong);
        }

        return pnlTang;
    }

    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TrangChu("TT", "Admin").setVisible(true);
        });
    }
}