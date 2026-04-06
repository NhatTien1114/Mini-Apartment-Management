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
import ui.util.PhongInfo;

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

    private static class MenuItem {
        String name;
        String icon;
        int panelIndex;

        MenuItem(String name, String icon, int panelIndex) {
            this.name = name;
            this.icon = icon;
            this.panelIndex = panelIndex;
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
        pnlDanhSach.setLayout(new GridLayout(9, 1, 0, 10));
        pnlDanhSach.setBorder(new EmptyBorder(20, 20, 20, 20));

        List<MenuItem> allMenuItems = Arrays.asList(
                new MenuItem("Trang chủ", "img/icons/home.png", 0),
                new MenuItem("Hợp đồng", "img/icons/google-docs.png", 1),
                new MenuItem("Quản lý phòng", "img/icons/settings.png", 2),
                new MenuItem("Khách hàng", "img/icons/user.png", 3),
                new MenuItem("Phương tiện", "img/icons/bike.png", 4),
                new MenuItem("Doanh thu", "img/icons/bar-chart.png", 5),
                new MenuItem("Dịch vụ", "img/icons/support.png", 6),
                new MenuItem("Hóa đơn", "img/icons/bill.png", 7),
                new MenuItem("Bảng giá", "img/icons/menu.png", 8));

        if ("Quản lý".equals(role)) {
            menuItems = allMenuItems.stream()
                    .filter(item -> item.panelIndex != 5 && item.panelIndex != 8)
                    .collect(Collectors.toList());
        } else {
            menuItems = allMenuItems;
        }

        pnlDanhSach.setLayout(new GridLayout(menuItems.size(), 1, 0, 10));

        menuButtons = new JButton[menuItems.size()];

        for (int i = 0; i < menuItems.size(); i++) {
            int index = i;
            MenuItem item = menuItems.get(i);
            JButton btn = new JButton(item.name);
            menuButtons[i] = btn;

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

            btn.addActionListener(e -> selectMenuTab(item.panelIndex));

            pnlDanhSach.add(btn);
        }

        pnlMenu.add(pnlDanhSach, BorderLayout.CENTER);
        return pnlMenu;
    }

    // ================= MENU SELECTION HANDLER =================
    private void selectMenuTab(int panelIndex) {
        menuButtons[selectedMenuIndex].setBackground(AppColors.MENU_BG);
        selectedMenuIndex = -1;
        for (int i = 0; i < menuButtons.length; i++) {
            if (menuItems.get(i).panelIndex == panelIndex) {
                selectedMenuIndex = i;
                break;
            }
        }
        if (selectedMenuIndex != -1) {
            menuButtons[selectedMenuIndex].setBackground(AppColors.MENU_HOVER);
        }

        if (panelIndex == 0) {
            refreshTrangChuTab();
        }
        if (panelIndex == 2) {
            refreshQuanLyPhongTab();
        }
        if (panelIndex == 3) {
            refreshKhachHangTab();
        }

        cardLayout.show(pnlContent, String.valueOf(panelIndex));
    }

    // ================= PANEL CHÍNH =================
    private QuanLyPhongUI quanLyPhongUI;
    private HopDongUI hopDongUI;
    private KhachHangUI khachHangUI;

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
        pnlContent.add(new DoanhThuUI().getPanel(), "5");
        pnlContent.add(new DichVuUI().getPanel(), "6");
        pnlContent.add(new HoaDonUI().getPanel(), "7");
        pnlContent.add(new BangGiaUI(taiKhoan).getPanel(), "8");

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

        JPanel pnlRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRightButtons.setBackground(Color.WHITE);

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
        pnlThongKe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        pnlTop.add(pnlThongKe);
        pnlTop.add(Box.createVerticalStrut(12));
        pnlTop.add(createFilterPanel());

        return pnlTop;
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

        JPanel pnlCard = new JPanel(new BorderLayout(0, 12));
        pnlCard.setBackground(AppColors.WHITE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 0, AppColors.TRANSPARENT),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 0, AppColors.TRANSPARENT),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(borderColor, 2, true),
                                BorderFactory.createEmptyBorder(16, 16, 16, 16)))));

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

        JLabel lblStatus = new JLabel(phong.getTrangThai() != null ? phong.getTrangThai().getTen() : "Không xác định");
        lblStatus.setOpaque(true);
        lblStatus.setBackground(statusColor);
        lblStatus.setForeground(AppColors.WHITE);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
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

        JMenuItem menuItemInfo = new JMenuItem("Xem thông tin phòng");
        menuItemInfo.addActionListener(e -> {
            new PhongInfo(phong.getMaPhong()).showDialog();
            refreshTrangChuTab();
        });

        JMenuItem menuItemContract = new JMenuItem("Tạo hợp đồng");
        menuItemContract.addActionListener(e -> {
            // Switch to contract page and show add contract form
            selectMenuTab(1); // Switch to contract page (panel "1")
            if (hopDongUI != null) {
                hopDongUI.showAddContractForm();
            }
        });

        popupMenu.add(menuItemInfo);
        popupMenu.add(menuItemContract);

        popupMenu.show(card, x, y);
    }

    private Color mauTheoLoaiPhong(Phong.LoaiPhong loaiPhong) {
        if (loaiPhong == null) {
            return AppColors.SLATE_300;
        }
        switch (loaiPhong) {
            case DON:
                return AppColors.BLUE;
            case DOI:
                return AppColors.GREEN;
            case STUDIO:
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
            return List.of();
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

    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}