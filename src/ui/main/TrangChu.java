package ui.main;

import dao.QuanLyPhongDAO;
import entity.Phong;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import ui.util.AppColors;
import ui.util.ThemeManager;
import ui.util.PhongInfo;
import ui.util.RoundedPanel;
import dao.HoaDonDAO;
import dao.HopDongDAO;
import entity.HopDong;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import ui.util.NotificationManager;

public class TrangChu extends JFrame {
    // ===== LAYOUT COMPONENTS =====
    private CardLayout cardLayout;
    private JPanel pnlContent;
    private JPanel pnlTrangChuContent;
    private JButton[] menuButtons;
    private int selectedMenuIndex = 0;

    private List<MenuItem> menuItems;
    private List<Phong> allPhongs = List.of();
    private JComboBox<String> cboLoaiPhongFilter;
    private JComboBox<String> cboTangFilter;
    private String currentLoaiPhongFilter = "Tất cả";
    private String currentTangFilter = "Tất cả";

    // ===== THÔNG TIN USER =====
    private final entity.TaiKhoan taiKhoan;
    private String tenTaiKhoan;
    private String role;
    private JLabel lblTen;
    private JLabel lblRole;
    private final QuanLyPhongDAO phongDAO = new QuanLyPhongDAO();

    private final NotificationManager notifManager = NotificationManager.getInstance();

    private static class MenuItem {
        String name;
        String icon;
        int panelIndex;
        boolean isSectionHeader;

        MenuItem(String name, String icon, int panelIndex) {
            this.name = name;
            this.icon = icon;
            this.panelIndex = panelIndex;
            this.isSectionHeader = false;
        }

        static MenuItem sectionHeader(String name) {
            MenuItem m = new MenuItem(name, null, -1);
            m.isSectionHeader = true;
            return m;
        }
    }

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

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                checkAndUpdateOverdueInvoices();
            }
        });

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
        pnlDanhSach.setLayout(new BoxLayout(pnlDanhSach, BoxLayout.Y_AXIS));
        pnlDanhSach.setBorder(new EmptyBorder(20, 20, 20, 20));

        List<MenuItem> allMenuItems = new java.util.ArrayList<>(Arrays.asList(
                new MenuItem("Trang chủ", "img/icons/home.png", 0),
                new MenuItem("Hợp đồng", "img/icons/google-docs.png", 1),
                new MenuItem("Quản lý phòng", "img/icons/settings.png", 2),
                new MenuItem("Khách hàng", "img/icons/user.png", 3),
                new MenuItem("Phương tiện", "img/icons/bike.png", 4),
                new MenuItem("Điện nước", "img/icons/electric.png", 10),
                new MenuItem("Dịch vụ", "img/icons/support.png", 6),
                new MenuItem("Hóa đơn", "img/icons/bill.png", 7)));

        if (!"Quản lý".equals(role)) {
            allMenuItems.add(MenuItem.sectionHeader("Chủ chung cư"));
            allMenuItems.add(new MenuItem("Doanh thu", "img/icons/bar-chart.png", 5));
            allMenuItems.add(new MenuItem("Bảng giá", "img/icons/menu.png", 8));
            allMenuItems.add(new MenuItem("Loại phòng", "img/icons/settings.png", 9));
        }

        if ("admin@gmail.com".equalsIgnoreCase(taiKhoan.getEmail())) {
            allMenuItems.add(MenuItem.sectionHeader("Quản trị"));
            allMenuItems.add(new MenuItem("Tài khoản", "img/icons/user.png", 11));
        }

        menuItems = allMenuItems;

        // Count clickable items for buttons array
        int clickableCount = 0;
        for (MenuItem item : menuItems) {
            if (!item.isSectionHeader)
                clickableCount++;
        }
        menuButtons = new JButton[clickableCount];

        int btnIndex = 0;
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem item = menuItems.get(i);

            if (item.isSectionHeader) {
                pnlDanhSach.add(Box.createVerticalStrut(16));
                JLabel lblSection = new JLabel(item.name.toUpperCase());
                lblSection.setFont(new Font("Be Vietnam Pro", Font.BOLD, 11));
                lblSection.setForeground(new Color(255, 255, 255, 120));
                lblSection.setBorder(new EmptyBorder(0, 4, 6, 0));
                lblSection.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
                pnlDanhSach.add(lblSection);
                continue;
            }

            int currentBtnIndex = btnIndex;
            JButton btn = new JButton(item.name);
            menuButtons[btnIndex] = btn;

            ImageIcon iconGoc = new ImageIcon(item.icon);
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
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);

            if (btnIndex == 0)
                btn.setBackground(AppColors.MENU_HOVER);

            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (selectedMenuIndex != currentBtnIndex)
                        btn.setBackground(AppColors.MENU_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (selectedMenuIndex != currentBtnIndex)
                        btn.setBackground(AppColors.MENU_BG);
                }
            });

            btn.addActionListener(e -> selectMenuTab(item.panelIndex));

            pnlDanhSach.add(btn);
            pnlDanhSach.add(Box.createVerticalStrut(10));
            btnIndex++;
        }

        pnlMenu.add(pnlDanhSach, BorderLayout.CENTER);
        return pnlMenu;
    }

    // ================= MENU SELECTION HANDLER =================
    private void selectMenuTab(int panelIndex) {
        menuButtons[selectedMenuIndex].setBackground(AppColors.MENU_BG);
        selectedMenuIndex = -1;
        int btnIdx = 0;
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem item = menuItems.get(i);
            if (item.isSectionHeader)
                continue;
            if (item.panelIndex == panelIndex) {
                selectedMenuIndex = btnIdx;
                break;
            }
            btnIdx++;
        }
        if (selectedMenuIndex != -1) {
            menuButtons[selectedMenuIndex].setBackground(AppColors.MENU_HOVER);
        }

        if (panelIndex == 0) {
            refreshTrangChuTab();
            refreshNotifications();
        }
        if (panelIndex == 2) {
            refreshQuanLyPhongTab();
        }
        if (panelIndex == 3) {
            refreshKhachHangTab();
        }
        if (panelIndex == 5) {
            refreshDoanhThuTab();
        }
        if (panelIndex == 10) {
            refreshChiSoDienNuocTab();
        }
        if (panelIndex == 7) {
            hoaDonUI.refresh();
        }
        if (panelIndex == 11 && quanLyTaiKhoanUI != null) {
            quanLyTaiKhoanUI.refresh();
        }

        cardLayout.show(pnlContent, String.valueOf(panelIndex));
    }

    // ================= PANEL CHÍNH =================
    private QuanLyPhongUI quanLyPhongUI;
    private HopDongUI hopDongUI;
    private KhachHangUI khachHangUI;
    private DoanhThuUI doanhThuUI;
    private HoaDonUI hoaDonUI;
    private ChiSoDienNuocUI chiSoDienNuocUI;
    private QuanLyTaiKhoanUI quanLyTaiKhoanUI;

    private JPanel createMainPanel() {
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBackground(AppColors.APP_BACKGROUND);

        pnlMain.add(createHeaderPanel(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(AppColors.APP_BACKGROUND);

        pnlTrangChuContent = createTrangChuContent();
        pnlContent.add(pnlTrangChuContent, "0");
        hopDongUI = new HopDongUI();
        pnlContent.add(hopDongUI.getPanel(), "1");
        quanLyPhongUI = new QuanLyPhongUI();
        pnlContent.add(quanLyPhongUI.getPanel(), "2");
        khachHangUI = new KhachHangUI();
        pnlContent.add(khachHangUI.getPanel(), "3");
        pnlContent.add(new PhuongTienUI().getPanel(), "4");
        doanhThuUI = new DoanhThuUI();
        pnlContent.add(doanhThuUI.getPanel(), "5");
        pnlContent.add(new DichVuUI().getPanel(), "6");
        hoaDonUI = new HoaDonUI();
        hoaDonUI.setOnInvoiceSaved(() -> refreshDoanhThuTab());
        hoaDonUI.setOnNavigateToChiSo(() -> {
            cardLayout.show(pnlContent, "10");
            refreshChiSoDienNuocTab();
        });
        pnlContent.add(hoaDonUI.getPanel(), "7");
        pnlContent.add(new BangGiaUI(taiKhoan).getPanel(), "8");
        pnlContent.add(new LoaiPhongUI().getPanel(), "9");
        chiSoDienNuocUI = new ChiSoDienNuocUI();
        pnlContent.add(chiSoDienNuocUI.getPanel(), "10");

        if ("admin@gmail.com".equalsIgnoreCase(taiKhoan.getEmail())) {
            quanLyTaiKhoanUI = new QuanLyTaiKhoanUI(taiKhoan.getEmail());
            pnlContent.add(quanLyTaiKhoanUI.getPanel(), "11");
        }

        if (hopDongUI != null) {
            hopDongUI.setOnContractCreated(() -> {
                refreshQuanLyPhongTab();
                refreshKhachHangTab();
            });
        }

        // Show the initial panel (Trang Chu)
        cardLayout.show(pnlContent, "0");

        pnlMain.add(pnlContent, BorderLayout.CENTER);
        return pnlMain;
    }

    private void refreshTrangChuTab() {
        if (pnlContent == null) {
            return;
        }

        // Reset bộ lọc về mặc định khi quay lại trang chủ
        currentLoaiPhongFilter = "Tất cả";
        currentTangFilter = "Tất cả";

        if (pnlTrangChuContent != null) {
            pnlContent.remove(pnlTrangChuContent);
        }

        pnlTrangChuContent = createTrangChuContent();
        pnlContent.add(pnlTrangChuContent, "0");
        cardLayout.show(pnlContent, "0");
        pnlContent.revalidate();
        pnlContent.repaint();
    }

    private void refreshQuanLyPhongTab() {
        if (quanLyPhongUI != null) {
            quanLyPhongUI.refresh();
        }
    }

    private void refreshKhachHangTab() {
        if (khachHangUI != null) {
            khachHangUI.refresh();
        }
    }

    private void refreshDoanhThuTab() {
        if (doanhThuUI != null) {
            doanhThuUI.refresh();
        }
    }

    private void refreshChiSoDienNuocTab() {
        if (chiSoDienNuocUI != null) {
            chiSoDienNuocUI.refresh();
        }
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

        JPanel pnlRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        pnlRightButtons.setBackground(Color.WHITE);

        // Load contract expiry notifications into NotificationManager
        refreshNotifications();

        JLabel lblBell = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int unviewedCount = notifManager.getUnviewedCount();
                if (unviewedCount > 0) {
                    // Hiển thị SỐ thông báo chưa xem
                    g2.setColor(new Color(239, 68, 68));
                    int d = 18;
                    int x = getWidth() - d;
                    int y = 0;
                    g2.fillOval(x, y, d, d);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.BOLD, 10));
                    String txt = unviewedCount > 99 ? "99+" : String.valueOf(unviewedCount);
                    FontMetrics fm = g2.getFontMetrics();
                    int tw = fm.stringWidth(txt);
                    int th = fm.getAscent();
                    g2.drawString(txt, x + (d - tw) / 2, y + (d + th) / 2 - 2);
                } else if (notifManager.hasUnresolved()) {
                    // Đã xem nhưng vẫn còn thông báo chưa xử lý → chấm đỏ nhỏ
                    g2.setColor(new Color(239, 68, 68));
                    int d = 10;
                    int x = getWidth() - d;
                    int y = 2;
                    g2.fillOval(x, y, d, d);
                }
                g2.dispose();
            }
        };
        ImageIcon iconBell = new ImageIcon("img/icons/bell.png");
        Image imgBell = iconBell.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        lblBell.setIcon(new ImageIcon(imgBell));
        lblBell.setPreferredSize(new Dimension(34, 34));
        lblBell.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Listen for new notifications and repaint the bell badge
        notifManager.addListener(n -> {
            SwingUtilities.invokeLater(() -> lblBell.repaint());
        });
        notifManager.addRemovedListener(() -> {
            SwingUtilities.invokeLater(() -> lblBell.repaint());
        });

        final String currentRole = this.role;

        lblBell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                notifManager.markAsViewed();
                lblBell.repaint();

                JPopupMenu popupNotifications = new JPopupMenu();
                popupNotifications.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

                JPanel pnlNotifs = new JPanel();
                pnlNotifs.setLayout(new BoxLayout(pnlNotifs, BoxLayout.Y_AXIS));
                pnlNotifs.setBackground(Color.WHITE);

                // Header of popup
                JPanel headerPanel = new JPanel(new BorderLayout());
                headerPanel.setBackground(new Color(248, 250, 252));
                headerPanel.setBorder(new EmptyBorder(10, 14, 10, 14));
                headerPanel.setMaximumSize(new Dimension(340, 40));
                JLabel lblHeader = new JLabel("Thông báo");
                lblHeader.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));
                lblHeader.setForeground(new Color(15, 23, 42));
                headerPanel.add(lblHeader, BorderLayout.WEST);
                pnlNotifs.add(headerPanel);

                // Filter notifications based on role
                List<NotificationManager.Notification> allNotifs = notifManager.getNotifications();
                List<NotificationManager.Notification> filteredNotifs = new java.util.ArrayList<>();
                for (NotificationManager.Notification notif : allNotifs) {
                    // service_price notifications only for Chủ
                    if ("service_price".equals(notif.type) && !"Chủ".equals(currentRole)) {
                        continue;
                    }
                    filteredNotifs.add(notif);
                }

                if (filteredNotifs.isEmpty()) {
                    JPanel emptyPanel = new JPanel(new BorderLayout());
                    emptyPanel.setBackground(Color.WHITE);
                    emptyPanel.setBorder(new EmptyBorder(24, 14, 24, 14));
                    emptyPanel.setMaximumSize(new Dimension(340, 70));
                    JLabel lblEmpty = new JLabel("Không có thông báo nào", SwingConstants.CENTER);
                    lblEmpty.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 13));
                    lblEmpty.setForeground(new Color(148, 163, 184));
                    emptyPanel.add(lblEmpty, BorderLayout.CENTER);
                    pnlNotifs.add(emptyPanel);
                } else {
                    for (int idx = 0; idx < filteredNotifs.size(); idx++) {
                        NotificationManager.Notification notif = filteredNotifs.get(idx);
                        final int notifIdx = idx;

                        JPanel itemPanel = new JPanel(new BorderLayout(8, 0));
                        itemPanel.setBackground(Color.WHITE);
                        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                                new EmptyBorder(10, 14, 10, 10)));
                        itemPanel.setMaximumSize(new Dimension(340, 80));

                        // Left color bar based on type
                        Color barColor;
                        String icon;
                        if ("contract_expiry".equals(notif.type)) {
                            barColor = new Color(245, 158, 11);
                            icon = "⚠";
                        } else if ("service_price".equals(notif.type)) {
                            barColor = new Color(59, 130, 246);
                            icon = "📋";
                        } else if ("price_update".equals(notif.type)) {
                            barColor = new Color(34, 197, 94);
                            icon = "✅";
                        } else if ("overdue_grace".equals(notif.type)) {
                            barColor = new Color(234, 179, 8);
                            icon = "⏰";
                        } else if ("overdue_payment".equals(notif.type)) {
                            barColor = new Color(239, 68, 68);
                            icon = "⚠";
                        } else if ("overdue_urgent".equals(notif.type)) {
                            barColor = new Color(220, 38, 38);
                            icon = "🚨";
                        } else {
                            barColor = new Color(148, 163, 184);
                            icon = "🔔";
                        }

                        JPanel colorBar = new JPanel();
                        colorBar.setBackground(barColor);
                        colorBar.setPreferredSize(new Dimension(4, 0));
                        itemPanel.add(colorBar, BorderLayout.WEST);

                        // Content
                        JPanel contentPanel = new JPanel();
                        contentPanel.setOpaque(false);
                        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

                        JLabel lblTitle = new JLabel(notif.title);
                        lblTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 12));
                        lblTitle.setForeground(new Color(15, 23, 42));
                        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

                        JLabel lblMsg = new JLabel("<html><div style='width:220px'>" + notif.message + "</div></html>");
                        lblMsg.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 11));
                        lblMsg.setForeground(new Color(100, 116, 139));
                        lblMsg.setAlignmentX(Component.LEFT_ALIGNMENT);

                        // Timestamp
                        long elapsed = System.currentTimeMillis() - notif.timestamp;
                        String timeAgo;
                        if (elapsed < 60_000) timeAgo = "Vừa xong";
                        else if (elapsed < 3_600_000) timeAgo = (elapsed / 60_000) + " phút trước";
                        else if (elapsed < 86_400_000) timeAgo = (elapsed / 3_600_000) + " giờ trước";
                        else timeAgo = (elapsed / 86_400_000) + " ngày trước";

                        JLabel lblTime = new JLabel(timeAgo);
                        lblTime.setFont(new Font("Be Vietnam Pro", Font.ITALIC, 10));
                        lblTime.setForeground(new Color(148, 163, 184));
                        lblTime.setAlignmentX(Component.LEFT_ALIGNMENT);

                        contentPanel.add(lblTitle);
                        contentPanel.add(Box.createVerticalStrut(2));
                        contentPanel.add(lblMsg);
                        contentPanel.add(Box.createVerticalStrut(2));
                        contentPanel.add(lblTime);
                        itemPanel.add(contentPanel, BorderLayout.CENTER);

                        // Dismiss button
                        JLabel btnDismiss = new JLabel("✕");
                        btnDismiss.setFont(new Font("Inter", Font.BOLD, 12));
                        btnDismiss.setForeground(new Color(203, 213, 225));
                        btnDismiss.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        btnDismiss.setBorder(new EmptyBorder(0, 4, 0, 4));
                        btnDismiss.setVerticalAlignment(SwingConstants.TOP);
                        btnDismiss.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent ev) {
                                notifManager.removeNotification(notif);
                                popupNotifications.setVisible(false);
                                lblBell.repaint();
                            }

                            @Override
                            public void mouseEntered(MouseEvent ev) {
                                btnDismiss.setForeground(new Color(239, 68, 68));
                            }

                            @Override
                            public void mouseExited(MouseEvent ev) {
                                btnDismiss.setForeground(new Color(203, 213, 225));
                            }
                        });
                        itemPanel.add(btnDismiss, BorderLayout.EAST);

                        // Hover effect for item
                        itemPanel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent ev) {
                                itemPanel.setBackground(new Color(248, 250, 252));
                                contentPanel.setBackground(new Color(248, 250, 252));
                            }

                            @Override
                            public void mouseExited(MouseEvent ev) {
                                itemPanel.setBackground(Color.WHITE);
                                contentPanel.setBackground(Color.WHITE);
                            }
                        });

                        pnlNotifs.add(itemPanel);
                    }
                }

                JScrollPane scrollNotifs = new JScrollPane(pnlNotifs);
                scrollNotifs.setBorder(null);
                int notifH = filteredNotifs.isEmpty() ? 120 : Math.min(400, filteredNotifs.size() * 85 + 45);
                scrollNotifs.setPreferredSize(new Dimension(340, notifH));
                scrollNotifs.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                popupNotifications.add(scrollNotifs);
                popupNotifications.show(lblBell, -340 + lblBell.getWidth(), lblBell.getHeight() + 5);
            }
        });

        pnlRightButtons.add(lblBell);

        // ===== DARK MODE TOGGLE =====
        pnlRightButtons.add(createDarkModeToggle());

        if ("admin@gmail.com".equalsIgnoreCase(taiKhoan.getEmail())) {
            JButton btnTaoTaiKhoan = new ui.util.PrimaryButton().makePrimaryButton("Tạo Tài Khoản");
            btnTaoTaiKhoan.addActionListener(e -> {
                new SignUpUI().setVisible(true);
            });
            pnlRightButtons.add(btnTaoTaiKhoan);
        }

        JButton btnDangXuat = new ui.util.PrimaryButton().makeErrorButton("Đăng xuất");
        btnDangXuat.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 16));
        btnDangXuat.setBackground(Color.WHITE);
        btnDangXuat.addActionListener(e -> {
            new LoginUI().setVisible(true);
            this.dispose();
        });

        pnlRightButtons.add(btnDangXuat);
        pnlHeader.add(pnlRightButtons, BorderLayout.EAST);

        return pnlHeader;
    }

    // ================= DARK MODE TOGGLE =================
    private JPanel createDarkModeToggle() {
        final ThemeManager theme = ThemeManager.getInstance();
        final int TRACK_W = 70, TRACK_H = 34, KNOB_D = 28, ARC = TRACK_H;
        final int PADDING = (TRACK_H - KNOB_D) / 2;
        final int LEFT_X = PADDING, RIGHT_X = TRACK_W - KNOB_D - PADDING;

        // Load sun and moon icons
        ImageIcon sunRaw = new ImageIcon("img/icons/sun.png");
        Image sunImg = sunRaw.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
        ImageIcon sunIcon = new ImageIcon(sunImg);

        ImageIcon moonRaw = new ImageIcon("img/icons/moon.png");
        Image moonImg = moonRaw.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        ImageIcon moonIcon = new ImageIcon(moonImg);

        final float[] knobPos = { theme.isDarkMode() ? RIGHT_X : LEFT_X };

        JPanel toggle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Track background
                boolean dark = theme.isDarkMode();
                Color trackBg = dark ? new Color(51, 65, 85) : new Color(203, 213, 225);
                g2.setColor(trackBg);
                g2.fillRoundRect(0, 0, TRACK_W, TRACK_H, ARC, ARC);

                // Moon icon (left side of track)
                int moonX = PADDING + 2;
                int moonY = (TRACK_H - 16) / 2;
                moonIcon.paintIcon(this, g2, moonX, moonY);

                // Sun icon (right side of track)
                int sunX = TRACK_W - 18 - PADDING - 2;
                int sunY = (TRACK_H - 18) / 2;
                sunIcon.paintIcon(this, g2, sunX, sunY);

                // Knob (sliding circle)
                int kx = Math.round(knobPos[0]);
                int ky = PADDING;
                g2.setColor(Color.WHITE);
                g2.fillOval(kx, ky, KNOB_D, KNOB_D);

                // Shadow on knob
                g2.setColor(new Color(0, 0, 0, 20));
                g2.drawOval(kx, ky, KNOB_D, KNOB_D);

                g2.dispose();
            }
        };
        toggle.setOpaque(false);
        toggle.setPreferredSize(new Dimension(TRACK_W, TRACK_H));
        toggle.setMinimumSize(new Dimension(TRACK_W, TRACK_H));
        toggle.setMaximumSize(new Dimension(TRACK_W, TRACK_H));
        toggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggle.setToolTipText(theme.isDarkMode() ? "Chuyển sang sáng" : "Chuyển sang tối");

        // Animation timer
        final javax.swing.Timer[] animTimer = { null };

        toggle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                theme.toggle();
                float target = theme.isDarkMode() ? RIGHT_X : LEFT_X;
                toggle.setToolTipText(theme.isDarkMode() ? "Chuyển sang sáng" : "Chuyển sang tối");
                
                // Cập nhật UI ngay lập tức
                Window w = SwingUtilities.getWindowAncestor(toggle);
                if (w != null) {
                    theme.applyTheme(w);
                }

                if (animTimer[0] != null && animTimer[0].isRunning()) {
                    animTimer[0].stop();
                }

                animTimer[0] = new javax.swing.Timer(10, null);
                animTimer[0].addActionListener(evt -> {
                    float diff = target - knobPos[0];
                    if (Math.abs(diff) < 1f) {
                        knobPos[0] = target;
                        animTimer[0].stop();
                    } else {
                        knobPos[0] += diff * 0.3f;
                    }
                    toggle.repaint();
                });
                animTimer[0].start();
            }
        });

        return toggle;
    }

    // ================= TRANG CHỦ CONTENT =================
    private JPanel createTrangChuContent() {
        JPanel pnlTrangChu = new JPanel(new BorderLayout(20, 20));
        pnlTrangChu.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnlTrangChu.setBackground(AppColors.APP_BACKGROUND);

        pnlTrangChu.add(createTopPanel(), BorderLayout.NORTH);
        pnlTrangChu.add(createSoDoPhongPanel(), BorderLayout.CENTER);

        return pnlTrangChu;
    }

    private JPanel createTopPanel() {
        JPanel pnlTop = new JPanel();
        pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
        pnlTop.setBackground(AppColors.APP_BACKGROUND);

        JPanel pnlThongKe = createThongKePanel();
        pnlThongKe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        pnlTop.add(pnlThongKe);
        pnlTop.add(Box.createVerticalStrut(12));
        pnlTop.add(createFilterPanel());

        return pnlTop;
    }

    // ================= THỐNG KÊ =================
    private JPanel createThongKePanel() {
        JPanel pnlThongKe = new JPanel(new GridLayout(1, 5, 14, 0));
        pnlThongKe.setBackground(AppColors.APP_BACKGROUND);
        pnlThongKe.setPreferredSize(new Dimension(0, 100));

        List<Phong> dsPhong = layDanhSachPhong();
        int tongSoPhong = dsPhong.size();
        int soPhongTrong = demTheoTrangThai(dsPhong, "Trống");
        int soPhongDaThue = demTheoTrangThai(dsPhong, "Đã thuê");
        int soPhongDaCoc = demTheoTrangThai(dsPhong, "Đã cọc");
        int soPhongSua = demTheoTrangThai(dsPhong, "Đang sửa");

        pnlThongKe.add(
                createStatCard(String.valueOf(tongSoPhong), "Tổng phòng", AppColors.PRIMARY, "img/icons/home.png"));
        pnlThongKe.add(
                createStatCard(String.valueOf(soPhongTrong), "Phòng trống", AppColors.GREEN_500, "img/icons/home.png"));
        pnlThongKe
                .add(createStatCard(String.valueOf(soPhongDaThue), "Đã thuê", AppColors.RED_500, "img/icons/user.png"));
        pnlThongKe.add(
                createStatCard(String.valueOf(soPhongDaCoc), "Đã cọc", AppColors.BLUE, "img/icons/google-docs.png"));
        pnlThongKe.add(
                createStatCard(String.valueOf(soPhongSua), "Đang sửa", AppColors.WARNING, "img/icons/settings.png"));

        return pnlThongKe;
    }

    private JPanel createFilterPanel() {
        allPhongs = layDanhSachPhong();
        Set<String> loaiPhongSet = allPhongs.stream()
                .map(p -> p.getLoaiPhong() != null ? p.getLoaiPhong().getTen() : "Không xác định")
                .collect(Collectors.toSet());
        Set<String> tangSet = allPhongs.stream()
                .map(p -> p.getMaTang() != null ? p.getMaTang().getTenTang() : "Chưa xác định")
                .collect(Collectors.toSet());

        String[] loaiOptions = Stream.concat(Stream.of("Tất cả"), loaiPhongSet.stream().sorted())
                .toArray(String[]::new);
        String[] tangOptions = Stream.concat(Stream.of("Tất cả"), tangSet.stream()
                .sorted((t1, t2) -> {
                    if ("Chưa xác định".equals(t1))
                        return 1;
                    if ("Chưa xác định".equals(t2))
                        return -1;
                    // Extract floor number from "Tầng X" format
                    try {
                        int num1 = Integer.parseInt(t1.replaceAll("\\D+", ""));
                        int num2 = Integer.parseInt(t2.replaceAll("\\D+", ""));
                        return Integer.compare(num1, num2);
                    } catch (NumberFormatException e) {
                        return t1.compareTo(t2);
                    }
                }))
                .toArray(String[]::new);

        cboLoaiPhongFilter = new JComboBox<>(loaiOptions);
        cboTangFilter = new JComboBox<>(tangOptions);
        cboLoaiPhongFilter.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));
        cboTangFilter.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));

        // Set current selections
        cboLoaiPhongFilter.setSelectedItem(currentLoaiPhongFilter);
        cboTangFilter.setSelectedItem(currentTangFilter);

        cboLoaiPhongFilter.addActionListener(e -> {
            currentLoaiPhongFilter = String.valueOf(cboLoaiPhongFilter.getSelectedItem());
            updateSoDoPhongPanel();
        });
        cboTangFilter.addActionListener(e -> {
            currentTangFilter = String.valueOf(cboTangFilter.getSelectedItem());
            updateSoDoPhongPanel();
        });

        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        pnlFilter.setBackground(AppColors.APP_BACKGROUND);
        pnlFilter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, AppColors.SLATE_200),
                new EmptyBorder(10, 10, 10, 10)));

        pnlFilter.add(createFilterLabel("Lọc theo loại phòng:"));
        pnlFilter.add(cboLoaiPhongFilter);
        pnlFilter.add(createFilterLabel("Lọc theo tầng:"));
        pnlFilter.add(cboTangFilter);

        return pnlFilter;
    }

    private JLabel createFilterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Be Vietnam Pro", Font.BOLD, 13));
        lbl.setForeground(AppColors.SLATE_500);
        return lbl;
    }

    private JPanel createStatCard(String so, String tieuDe, Color mau, String iconPath) {
        RoundedPanel pnlCard = new RoundedPanel(14);
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setLayout(new BorderLayout());

        // Colored left accent bar
        JPanel accent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mau);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(Color.WHITE);
                g2.fillRect(getWidth() / 2, 0, getWidth() / 2, getHeight());
            }
        };
        accent.setPreferredSize(new Dimension(6, 0));
        accent.setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Icon + title row
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        try {
            ImageIcon rawIcon = new ImageIcon(iconPath);
            Image scaledImg = rawIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            JLabel lblIcon = new JLabel(new ImageIcon(scaledImg));
            titleRow.add(lblIcon);
        } catch (Exception ignored) {
        }
        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 12));
        lblTieuDe.setForeground(AppColors.SLATE_500);
        titleRow.add(lblTieuDe);

        // Large number
        JLabel lblSo = new JLabel(so);
        lblSo.setFont(new Font("Be Vietnam Pro", Font.BOLD, 28));
        lblSo.setForeground(mau);
        lblSo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSo.setBorder(new EmptyBorder(4, 2, 0, 0));

        content.add(titleRow);
        content.add(lblSo);

        pnlCard.add(accent, BorderLayout.WEST);
        pnlCard.add(content, BorderLayout.CENTER);

        return pnlCard;
    }

    // ================= SƠ ĐỒ PHÒNG =================
    private JPanel createSoDoPhongPanel() {
        JPanel pnlSoDo = new JPanel(new BorderLayout(0, 12));
        pnlSoDo.setBackground(AppColors.APP_BACKGROUND);

        JLabel lblTitle = new JLabel("Thông tin phòng theo tầng");
        lblTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 18));
        lblTitle.setForeground(AppColors.SLATE_900);
        pnlSoDo.add(lblTitle, BorderLayout.NORTH);

        List<Phong> filteredRooms = getFilteredRooms();
        if (filteredRooms.isEmpty()) {
            JLabel lblEmpty = new JLabel("Không có phòng phù hợp.");
            lblEmpty.setFont(new Font("Be Vietnam Pro", Font.ITALIC, 14));
            lblEmpty.setForeground(AppColors.SLATE_500);
            pnlSoDo.add(lblEmpty, BorderLayout.CENTER);
            return pnlSoDo;
        }

        JPanel pnlFloors = new JPanel();
        pnlFloors.setBackground(AppColors.APP_BACKGROUND);
        pnlFloors.setLayout(new BoxLayout(pnlFloors, BoxLayout.Y_AXIS));

        List<String> tangOrdered = filteredRooms.stream()
                .map(p -> p.getMaTang() != null ? p.getMaTang().getTenTang() : "Chưa xác định")
                .distinct()
                .sorted((t1, t2) -> {
                    if ("Chưa xác định".equals(t1))
                        return 1;
                    if ("Chưa xác định".equals(t2))
                        return -1;
                    // Extract floor number from "Tầng X" format
                    try {
                        int num1 = Integer.parseInt(t1.replaceAll("\\D+", ""));
                        int num2 = Integer.parseInt(t2.replaceAll("\\D+", ""));
                        return Integer.compare(num1, num2);
                    } catch (NumberFormatException e) {
                        return t1.compareTo(t2);
                    }
                })
                .collect(Collectors.toList());

        for (String tangName : tangOrdered) {
            List<Phong> roomsOnFloor = filteredRooms.stream()
                    .filter(p -> {
                        String current = p.getMaTang() != null ? p.getMaTang().getTenTang() : "Chưa xác định";
                        return tangName.equals(current);
                    })
                    .collect(Collectors.toList());
            pnlFloors.add(createFloorPanel(tangName, roomsOnFloor));
            pnlFloors.add(Box.createVerticalStrut(12));
        }

        JScrollPane scroll = new JScrollPane(pnlFloors);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(AppColors.APP_BACKGROUND);
        scroll.setBackground(AppColors.APP_BACKGROUND);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        pnlSoDo.add(scroll, BorderLayout.CENTER);
        return pnlSoDo;
    }

    private JPanel createFloorPanel(String tangName, List<Phong> rooms) {
        JPanel pnlFloor = new JPanel(new BorderLayout(0, 10));
        pnlFloor.setBackground(AppColors.APP_BACKGROUND);

        JLabel lblFloorTitle = new JLabel(tangName);
        lblFloorTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 16));
        lblFloorTitle.setForeground(AppColors.SLATE_900);
        pnlFloor.add(lblFloorTitle, BorderLayout.NORTH);

        // Sort rooms by room number
        rooms.sort((p1, p2) -> {
            try {
                // Extract room number from format like "P1.01", "P1.02", etc.
                String[] parts1 = p1.getMaPhong().split("\\.");
                String[] parts2 = p2.getMaPhong().split("\\.");
                if (parts1.length >= 2 && parts2.length >= 2) {
                    int floor1 = Integer.parseInt(parts1[0].substring(1));
                    int floor2 = Integer.parseInt(parts2[0].substring(1));
                    int room1 = Integer.parseInt(parts1[1]);
                    int room2 = Integer.parseInt(parts2[1]);
                    int compare = Integer.compare(floor1, floor2);
                    if (compare != 0)
                        return compare;
                    return Integer.compare(room1, room2);
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                // Fall back to string comparison
            }
            return p1.getMaPhong().compareTo(p2.getMaPhong());
        });

        JPanel pnlCards = new JPanel(new GridLayout(0, 5, 12, 12));
        pnlCards.setBackground(AppColors.APP_BACKGROUND);
        pnlCards.setBorder(new EmptyBorder(8, 0, 0, 0));

        for (Phong phong : rooms) {
            pnlCards.add(createRoomCard(phong));
        }

        pnlFloor.add(pnlCards, BorderLayout.CENTER);
        return pnlFloor;
    }

    private JPanel createRoomCard(Phong phong) {
        Color borderColor = mauTheoLoaiPhong(phong.getLoaiPhong());
        Color statusColor = mauNenTrangThai(phong.getTrangThai() != null ? phong.getTrangThai().getTen() : "");

        final int ARC = 16;
        final Color[] hoverBorder = { borderColor };
        JPanel pnlCard = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
                g2.setColor(hoverBorder[0]);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, ARC, ARC);
                g2.dispose();
            }
        };
        pnlCard.setOpaque(false);
        pnlCard.setBackground(AppColors.WHITE);
        pnlCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        pnlCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pnlCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hoverBorder[0] = borderColor.darker();
                pnlCard.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverBorder[0] = borderColor;
                pnlCard.repaint();
            }
        });

        JLabel lblRoom = new JLabel(phong.getMaPhong());
        lblRoom.setFont(new Font("Be Vietnam Pro", Font.BOLD, 20));
        lblRoom.setForeground(AppColors.SLATE_900);

        JLabel lblType = new JLabel(
                phong.getLoaiPhong() != null ? phong.getLoaiPhong().getTen() : "Loại phòng chưa xác định");
        lblType.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 13));
        lblType.setForeground(borderColor.darker());

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 4));
        pnlHeader.setOpaque(false);
        pnlHeader.add(lblRoom, BorderLayout.NORTH);
        pnlHeader.add(lblType, BorderLayout.SOUTH);

        JLabel lblStatus = new JLabel(phong.getTrangThai() != null ? phong.getTrangThai().getTen() : "Không xác định") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblStatus.setOpaque(false);
        lblStatus.setBackground(statusColor);
        lblStatus.setForeground(AppColors.WHITE);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        lblStatus.setFont(new Font("Be Vietnam Pro", Font.BOLD, 12));

        JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlStatus.setOpaque(false);
        pnlStatus.add(lblStatus);

        JLabel lblFloor = new JLabel(phong.getMaTang() != null ? phong.getMaTang().getTenTang() : "Tầng chưa xác định");
        lblFloor.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 12));
        lblFloor.setForeground(AppColors.SLATE_500);

        JLabel lblOccupants = new JLabel("Số người: " + phong.getSoNguoiHienTai());
        lblOccupants.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 12));
        lblOccupants.setForeground(AppColors.SLATE_500);

        JPanel pnlInfo = new JPanel(new GridLayout(2, 1, 0, 4));
        pnlInfo.setOpaque(false);
        pnlInfo.add(lblFloor);
        pnlInfo.add(lblOccupants);

        pnlCard.add(pnlHeader, BorderLayout.NORTH);
        pnlCard.add(pnlInfo, BorderLayout.CENTER);
        pnlCard.add(pnlStatus, BorderLayout.SOUTH);
        pnlCard.setPreferredSize(new Dimension(160, 120));
        pnlCard.setMaximumSize(new Dimension(160, 120));

        pnlCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    // Show context menu
                    showRoomContextMenu(pnlCard, phong, e.getX(), e.getY());
                } else if (e.getClickCount() == 1) {
                    // Left click - show room info
                    new PhongInfo(phong.getMaPhong()).showDialog();
                    refreshTrangChuTab();
                }
            }
        });

        return pnlCard;
    }

    private void showRoomContextMenu(JPanel card, Phong phong, int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();
        String trangThai = phong.getTrangThai() != null ? phong.getTrangThai().getTen() : "";

        JMenuItem menuItemInfo = new JMenuItem("Xem thông tin phòng");
        menuItemInfo.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 13));
        menuItemInfo.addActionListener(e -> {
            new PhongInfo(phong.getMaPhong()).showDialog();
            refreshTrangChuTab();
        });
        popupMenu.add(menuItemInfo);

        if ("Đang sửa".equals(trangThai)) {
            popupMenu.addSeparator();
            JMenuItem menuItemTrong = new JMenuItem("Chuyển sang Trống");
            menuItemTrong.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 13));
            menuItemTrong.setForeground(new Color(22, 163, 74));
            menuItemTrong.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Chuyển phòng " + phong.getMaPhong() + " sang trạng thái Trống?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (phongDAO.updateTrangThaiPhong(phong.getMaPhong(), "Trống")) {
                        ui.util.MessageDialog.show(this, "Thành công",
                                "Phòng " + phong.getMaPhong() + " đã chuyển sang trạng thái Trống.",
                                ui.util.MessageDialog.MessageType.SUCCESS);
                        refreshTrangChuTab();
                    } else {
                        ui.util.MessageDialog.show(this, "Lỗi",
                                "Không thể cập nhật trạng thái phòng.",
                                ui.util.MessageDialog.MessageType.ERROR);
                    }
                }
            });
            popupMenu.add(menuItemTrong);
        }

        if ("Trống".equals(trangThai)) {
            popupMenu.addSeparator();
            JMenuItem menuItemCoc = new JMenuItem("Đặt cọc phòng");
            menuItemCoc.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 13));
            menuItemCoc.setForeground(new Color(37, 99, 235));
            menuItemCoc.addActionListener(e -> showDatCocDialog(phong));
            popupMenu.add(menuItemCoc);

            JMenuItem menuItemContract = new JMenuItem("Tạo hợp đồng");
            menuItemContract.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 13));
            menuItemContract.addActionListener(ev -> {
                selectMenuTab(1);
                if (hopDongUI != null) {
                    hopDongUI.showAddContractForm(phong.getMaPhong());
                }
            });
            popupMenu.add(menuItemContract);
        }

        if ("Đã cọc".equals(trangThai)) {
            popupMenu.addSeparator();
            JMenuItem menuItemContract = new JMenuItem("Lập hợp đồng");
            menuItemContract.setFont(new Font("Be Vietnam Pro", Font.BOLD, 13));
            menuItemContract.setForeground(new Color(37, 99, 235));
            menuItemContract.addActionListener(ev -> {
                dao.DatCocDAO datCocDAO = new dao.DatCocDAO();
                entity.DatCoc dc = datCocDAO.layTheoPhong(phong.getMaPhong());
                selectMenuTab(1);
                if (hopDongUI != null) {
                    if (dc != null) {
                        hopDongUI.showAddContractForm(phong.getMaPhong(), dc.getHoTen(), dc.getSoCCCD());
                    } else {
                        hopDongUI.showAddContractForm(phong.getMaPhong());
                    }
                }
            });
            popupMenu.add(menuItemContract);
        }

        popupMenu.show(card, x, y);
    }

    private void showDatCocDialog(Phong phong) {
        JDialog dlg = new JDialog(this, "Đặt cọc phòng " + phong.getMaPhong(), true);
        dlg.setUndecorated(true);
        dlg.setSize(440, 340);
        dlg.setLocationRelativeTo(this);
        dlg.setBackground(new Color(0, 0, 0, 0));

        ui.util.RoundedPanel root = new ui.util.RoundedPanel(16);
        root.setLayout(new BorderLayout(0, 12));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel dlgTitle = new JLabel("Đặt cọc phòng " + phong.getMaPhong());
        dlgTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 17));
        dlgTitle.setForeground(new Color(15, 23, 42));

        JLabel btnClose = new JLabel("✕");
        btnClose.setFont(new Font("Inter", Font.BOLD, 14));
        btnClose.setForeground(new Color(148, 163, 184));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dlg.dispose(); }
            public void mouseEntered(MouseEvent e) { btnClose.setForeground(new Color(239, 68, 68)); }
            public void mouseExited(MouseEvent e) { btnClose.setForeground(new Color(148, 163, 184)); }
        });
        headerRow.add(dlgTitle, BorderLayout.WEST);
        headerRow.add(btnClose, BorderLayout.EAST);
        root.add(headerRow, BorderLayout.NORTH);

        // Form fields
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 12, 10));
        formPanel.setOpaque(false);

        JTextField txtHoTen = new JTextField();
        txtHoTen.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 13));
        txtHoTen.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(8, 10, 8, 10)));

        JTextField txtSoTien = new JTextField();
        txtSoTien.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 13));
        txtSoTien.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(8, 10, 8, 10)));

        JTextField txtCCCD = new JTextField();
        txtCCCD.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 13));
        txtCCCD.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(8, 10, 8, 10)));

        // Số ngày giữ chỗ (hiển thị cố định 3 ngày)
        JPanel soNgayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        soNgayPanel.setOpaque(false);
        JLabel lblSoNgay = new JLabel("3 ngày");
        lblSoNgay.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));
        lblSoNgay.setForeground(new Color(245, 158, 11));
        JLabel lblNote = new JLabel("(sau 3 ngày sẽ chuyển về Trống)");
        lblNote.setFont(new Font("Be Vietnam Pro", Font.ITALIC, 11));
        lblNote.setForeground(new Color(148, 163, 184));
        soNgayPanel.add(lblSoNgay);
        soNgayPanel.add(lblNote);

        formPanel.add(makeFormLabel("Họ tên *"));
        formPanel.add(txtHoTen);
        formPanel.add(makeFormLabel("Số tiền cọc *"));
        formPanel.add(txtSoTien);
        formPanel.add(makeFormLabel("Số CCCD/CMND *"));
        formPanel.add(txtCCCD);
        formPanel.add(makeFormLabel("Thời gian giữ chỗ"));
        formPanel.add(soNgayPanel);

        root.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 13));
        btnCancel.setPreferredSize(new Dimension(80, 36));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = new ui.util.PrimaryButton().makePrimaryButton("Đặt cọc");
        btnSave.addActionListener(e -> {
            String hoTen = txtHoTen.getText().trim();
            String soTien = txtSoTien.getText().trim();
            String cccd = txtCCCD.getText().trim();

            if (hoTen.isEmpty()) {
                ui.util.ValidationPopup.show(txtHoTen, "Họ tên không được để trống!");
                return;
            }
            if (soTien.isEmpty()) {
                ui.util.ValidationPopup.show(txtSoTien, "Số tiền cọc không được để trống!");
                return;
            }
            if (cccd.isEmpty() || (!cccd.matches("\\d{9}") && !cccd.matches("\\d{12}"))) {
                ui.util.ValidationPopup.show(txtCCCD, "CCCD phải có 12 số hoặc CMND 9 số!");
                return;
            }

            double tien;
            try {
                tien = Double.parseDouble(soTien.replace(",", "").replace(".", ""));
            } catch (NumberFormatException ex) {
                ui.util.ValidationPopup.show(txtSoTien, "Số tiền không hợp lệ!");
                return;
            }

            entity.DatCoc dc = new entity.DatCoc();
            dc.setMaPhong(phong.getMaPhong());
            dc.setHoTen(hoTen);
            dc.setSoCCCD(cccd);
            dc.setSoTien(tien);
            dc.setNgayDatCoc(LocalDate.now());
            dc.setSoNgay(3);

            dao.DatCocDAO datCocDAO = new dao.DatCocDAO();
            if (datCocDAO.them(dc)) {
                phongDAO.updateTrangThaiPhong(phong.getMaPhong(), "Đã cọc");
                ui.util.MessageDialog.show(dlg, "Thành công",
                        "Đã đặt cọc phòng " + phong.getMaPhong() + " thành công.\n"
                        + "Giữ chỗ trong 3 ngày.",
                        ui.util.MessageDialog.MessageType.SUCCESS);
                dlg.dispose();
                refreshTrangChuTab();
            } else {
                ui.util.MessageDialog.show(dlg, "Lỗi",
                        "Không thể đặt cọc phòng.",
                        ui.util.MessageDialog.MessageType.ERROR);
            }
        });

        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        root.add(btnRow, BorderLayout.SOUTH);

        // Glass pane overlay
        Component oldGlassPane = this.getGlassPane();
        JPanel overlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 110));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlay.setOpaque(false);
        overlay.addMouseListener(new MouseAdapter() {});
        this.setGlassPane(overlay);
        overlay.setVisible(true);

        dlg.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                overlay.setVisible(false);
                TrangChu.this.setGlassPane(oldGlassPane);
            }
        });

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private JLabel makeFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 13));
        lbl.setForeground(new Color(71, 85, 105));
        return lbl;
    }

    private Color mauTheoLoaiPhong(entity.LoaiPhong loaiPhong) {
        if (loaiPhong == null) {
            return AppColors.SLATE_300;
        }
        switch (loaiPhong.ordinal()) {
            case 0:
                return AppColors.BLUE;
            case 1:
                return AppColors.GREEN;
            case 2:
                return AppColors.VIOLET_500;
            default:
                return AppColors.SLATE_300;
        }
    }

    private Color mauNenTrangThai(String trangThai) {
        if ("Đã thuê".equals(trangThai)) {
            return AppColors.RED_500;
        }
        if ("Đã cọc".equals(trangThai)) {
            return AppColors.BLUE;
        }
        if ("Đang sửa".equals(trangThai)) {
            return AppColors.WARNING;
        }
        return AppColors.GREEN_500;
    }

    private void updateSoDoPhongPanel() {
        refreshTrangChuTab();
    }

    private List<Phong> getFilteredRooms() {
        if (allPhongs == null || allPhongs.isEmpty()) {
            allPhongs = layDanhSachPhong();
        }
        String selectedLoai = currentLoaiPhongFilter;
        String selectedTang = currentTangFilter;

        return allPhongs.stream()
                .filter(p -> {
                    if (!"Tất cả".equals(selectedLoai) && !selectedLoai.equals("Không xác định")) {
                        return p.getLoaiPhong() != null && selectedLoai.equals(p.getLoaiPhong().getTen());
                    }
                    if (!"Tất cả".equals(selectedLoai) && "Không xác định".equals(selectedLoai)) {
                        return p.getLoaiPhong() == null;
                    }
                    return true;
                })
                .filter(p -> {
                    if (!"Tất cả".equals(selectedTang)) {
                        String current = p.getMaTang() != null ? p.getMaTang().getTenTang() : "Chưa xác định";
                        return selectedTang.equals(current);
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private List<Phong> layDanhSachPhong() {
        try {
            return phongDAO.layTatCa();
        } catch (RuntimeException e) {
            return java.util.Collections.emptyList();
        }
    }

    private int demTheoTrangThai(List<Phong> dsPhong, String trangThai) {
        int dem = 0;
        for (Phong phong : dsPhong) {
            if (phong.getTrangThai() != null && trangThai.equals(phong.getTrangThai().getTen())) {
                dem++;
            }
        }
        return dem;
    }

    // ================= KIỂM TRA HOÁ ĐƠN QUÁ HẠN =================
    /**
     * Làm mới toàn bộ thông báo: xóa thông báo cũ đã hết hiệu lực, thêm lại các thông báo mới.
     * Gọi khi quay lại Trang Chủ hoặc sau khi thực hiện thao tác liên quan.
     */
    private void refreshNotifications() {
        new Thread(() -> {
            // 1. Xóa thông báo hợp đồng hết hạn cũ
            notifManager.clearByType("contract_expiry");

            // 2. Kiểm tra lại hợp đồng sắp hết hạn từ DB
            List<HopDong> expiringContracts = new HopDongDAO().getAllHopDongDangHieuLuc().stream()
                    .filter(hd -> {
                        long days = ChronoUnit.DAYS.between(LocalDate.now(), hd.getNgayKetThuc());
                        return days >= 0 && days <= 30;
                    })
                    .collect(Collectors.toList());

            // 3. Xóa thông báo quá hạn cũ
            notifManager.clearByType("overdue_payment");
            notifManager.clearByType("overdue_grace");
            notifManager.clearByType("overdue_urgent");

            SwingUtilities.invokeLater(() -> {
                for (HopDong hd : expiringContracts) {
                    long days = ChronoUnit.DAYS.between(LocalDate.now(), hd.getNgayKetThuc());
                    notifManager.addNotification(
                            "⚠ Hợp đồng sắp hết hạn",
                            "Phòng " + hd.getPhong().getMaPhong() + " sẽ hết hạn sau " + days + " ngày.",
                            "contract_expiry",
                            hd.getMaHopDong());
                }
            });
        }, "refresh-notifications").start();
    }

    private void checkAndUpdateOverdueInvoices() {
        new Thread(() -> {
            HoaDonDAO dao = new HoaDonDAO();
            HoaDonDAO.CauHinhPhat cfg = dao.getCauHinhPhat();
            List<HoaDonDAO.QuaHanInfo> list = dao.getHoaDonChuaTTQuaHan(cfg.ngayHanThanhToan);
            if (list.isEmpty()) return;

            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

            for (HoaDonDAO.QuaHanInfo info : list) {
                long ngay = info.soNgayQuaHan;
                String phong = info.maPhong;

                if (ngay <= cfg.soNgayAnHan) {
                    String msg = String.format(
                            "Phòng %s đã quá hạn thanh toán %d ngày (trong thời gian ân hạn).", phong, ngay);
                    SwingUtilities.invokeLater(() ->
                            notifManager.addNotification("⏰ Nhắc thanh toán", msg, "overdue_grace", info.maHoaDon));
                } else {
                    long ngayTinhPhat = ngay - cfg.soNgayAnHan;
                    double tienPhat = info.tongTien * cfg.mucPhatNgay * ngayTinhPhat;
                    dao.capNhatTrangThaiQuaHan(info.maHoaDon, tienPhat);

                    String title;
                    String msg;
                    String type;
                    if (ngay <= 7) {
                        title = "⚠ Quá hạn thanh toán";
                        msg = String.format(
                                "Phòng %s đã quá hạn %d ngày. Tiền phạt: %s VNĐ.",
                                phong, ngay, nf.format(Math.round(tienPhat)));
                        type = "overdue_payment";
                    } else {
                        title = "🚨 Cần xử lý khẩn";
                        msg = String.format(
                                "Phòng %s đã quá hạn thanh toán %d ngày - Cần được xử lý hoặc ngưng cung cấp dịch vụ.",
                                phong, ngay);
                        type = "overdue_urgent";
                    }
                    SwingUtilities.invokeLater(() ->
                            notifManager.addNotification(title, msg, type, info.maHoaDon));
                }
            }
        }, "overdue-check").start();
    }

    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}