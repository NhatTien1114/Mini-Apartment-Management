package ui.main;

import dao.ChiSoDienNuocDAO;
import dao.DichVuDAO;
import dao.GiaDetailDAO;
import dao.HoaDonDAO;
import dao.HopDongKhachHangDAO;
import dao.PhuongTienDAO;
import dao.QuanLyPhongDAO;
import entity.ChiSoDienNuoc;
import entity.DichVu;
import entity.GiaDetail;
import entity.KhachHang;
import entity.Phong;
import entity.PhuongTien;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import ui.util.AppColors;
import ui.util.PrimaryButton;

public class HoaDonUI {
    private final Color MAU_NEN = AppColors.SLATE_100;
    private final Color MAU_CARD = AppColors.WHITE;
    private final Color MAU_BORDER = AppColors.SLATE_200;
    private final Color MAU_TEXT = AppColors.SLATE_900;

    private final Font FONT_TITLE = new Font("Be Vietnam Pro", Font.BOLD, 22);
    private final Font FONT_BOLD = new Font("Be Vietnam Pro", Font.BOLD, 14);
    private final Font FONT_PLAIN = new Font("Be Vietnam Pro", Font.PLAIN, 13);
    private final Font FONT_SMALL = new Font("Be Vietnam Pro", Font.PLAIN, 12);

    private final PrimaryButton primaryButton = new PrimaryButton();
    private final NumberFormat NF = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter DF = DateTimeFormatter.ofPattern("d/M/yyyy");
    private final String[] MONTHS = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
    private final ImageIcon eyeIcon = loadEyeIcon();
    private final ImageIcon gearIcon = loadGearIcon();

    private final ChiSoDienNuocDAO dienNuocDAO = new ChiSoDienNuocDAO();
    private final QuanLyPhongDAO phongDAO = new QuanLyPhongDAO();
    private final HopDongKhachHangDAO hdkhDAO = new HopDongKhachHangDAO();
    private final GiaDetailDAO giaDetailDAO = new GiaDetailDAO();
    private final DichVuDAO dvDAO = new DichVuDAO();
    private final HoaDonDAO hdDAO = new HoaDonDAO();
    private final PhuongTienDAO ptDAO = new PhuongTienDAO();
    private final ChiSoDienNuocDAO chiSoDAO = new ChiSoDienNuocDAO();

    private final PrimaryButton primaryButtonHelper = new PrimaryButton();

    private QuanLyPhongUI QLUI = new QuanLyPhongUI();

    private final List<MonthlyRoomDraft> currentDrafts = new ArrayList<>();
    private final List<HistoryItem> historyItems = new ArrayList<>();

    private JPanel summaryCard;
    private JLabel lblSummaryTitle;
    private JTable tblSummary;
    private DefaultTableModel summaryModel;

    private JTable tblHistory;
    private DefaultTableModel historyModel;

    private String currentMonth = "";
    private String currentYear = "";

    private Runnable onInvoiceSaved;
    private Runnable onNavigateToChiSo;

    public void setOnInvoiceSaved(Runnable callback) {
        this.onInvoiceSaved = callback;
    }

    public void setOnNavigateToChiSo(Runnable callback) {
        this.onNavigateToChiSo = callback;
    }

    public static class BillServiceItem {
        public String maDichVu;
        public String tenKhoan;
        public int soLuong;
        public double donGia;
    }

    public static class Bill {
        public String phong;
        public String maHopDong;
        public boolean daThanhToan;
        public int tongTieuThuD;
        public double donGiaDien;
        public double tienDien;
        public String maDichVuDien;
        public int tongTieuThuN;
        public double donGiaNuoc;
        public double tienNuoc;
        public String maDichVuNuoc;
        public double tienPhong;
        public String month;
        public String year;
        public List<BillServiceItem> dichVuKhac = new ArrayList<>();
    }

    private static class ServiceOption {
        String maDichVu;
        String tenDichVu;
        double donGia;
        boolean selected;
    }

    private static class MonthlyRoomDraft {
        String maPhong;
        String maHopDong = "";
        String tenKhach = "";
        int soDienCu;
        int soDienMoi;
        int soNuocCu;
        int soNuocMoi;
        double donGiaDien;
        double donGiaNuoc;
        double tienPhong;
        boolean daThanhToan;
        boolean isExisting = false; // true = loaded from DB (read-only, not re-saved)
        boolean hasChiSoMoi = false; // true = ChiSoDienNuoc for month already in DB
        double tienDienOverride = -1;
        double tienNuocOverride = -1;
        List<ServiceOption> options = new ArrayList<>();

        int tieuThuDien() {
            return Math.max(0, soDienMoi - soDienCu);
        }

        int tieuThuNuoc() {
            return Math.max(0, soNuocMoi - soNuocCu);
        }

        double getTienDien() {
            return tienDienOverride >= 0 ? tienDienOverride : tieuThuDien() * donGiaDien;
        }

        double getTienNuoc() {
            return tienNuocOverride >= 0 ? tienNuocOverride : tieuThuNuoc() * donGiaNuoc;
        }

        double tienDichVuKhac() {
            double sum = 0;
            for (ServiceOption op : options) {
                if (op.selected) {
                    sum += op.donGia;
                }
            }
            return sum;
        }

        double tongTien() {
            return tienPhong + getTienDien() + getTienNuoc() + tienDichVuKhac();
        }
    }

    private static class HistoryItem {
        String monthYear;
        int soPhong;
        double tongDoanhThu;
        LocalDate ngayTao;
    }

    public static class MonthDetailRow {
        public String maPhong;
        public double tienPhong;
        public double tienDien;
        public double tienNuoc;
        public double tienDichVu;
        public double tong;
    }

    public static class RoomMonthSummary {
        public String maHoaDon = "";
        public String maPhong;
        public double tienPhong;
        public int tieuThuDien;
        public double donGiaDien;
        public int tieuThuNuoc;
        public double donGiaNuoc;
        public boolean daThanhToan;
        public List<BillServiceItem> services = new ArrayList<>();
    }

    private JPanel pnlRoot;
    private JPanel pnlHistoryView;
    private JPanel pnlCalcView;
    private CardLayout mainCardLayout;

    public void refresh() {
        loadHistory();
        rebuildSummaryModel();
    }

    public JPanel getPanel() {
        pnlRoot = new JPanel(new BorderLayout());
        pnlRoot.setBackground(MAU_NEN);

        mainCardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(mainCardLayout);
        cardPanel.setBackground(MAU_NEN);

        pnlHistoryView = buildHistoryView();
        pnlCalcView = buildCalcView();

        cardPanel.add(pnlHistoryView, "history");
        cardPanel.add(pnlCalcView, "calc");

        pnlRoot.add(cardPanel, BorderLayout.CENTER);

        mainCardLayout.show(cardPanel, "history");

        loadHistory();
        return pnlRoot;
    }

    private JPanel buildHistoryView() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(MAU_NEN);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Hóa đơn hàng tháng");
        title.setFont(FONT_TITLE);
        title.setForeground(MAU_TEXT);
        top.add(title, BorderLayout.WEST);

        JButton btnCalc = primaryButton.makePrimaryButton("Tính hóa đơn");
        btnCalc.addActionListener(e -> showCreateDialog());
        top.add(btnCalc, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);
        root.add(buildHistoryCard(), BorderLayout.CENTER);

        return root;
    }

    private JPanel buildCalcView() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(MAU_NEN);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Hóa đơn hàng tháng");
        title.setFont(FONT_TITLE);
        title.setForeground(MAU_TEXT);
        top.add(title, BorderLayout.WEST);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setOpaque(false);

        JButton btnBack = makeOutlineButton("← Quay lại");
        btnBack.addActionListener(e -> {
            mainCardLayout.show(
                    pnlRoot.getComponent(0) instanceof JPanel ? (java.awt.Container) pnlRoot.getComponent(0) : pnlRoot,
                    "history");
            loadHistory();
        });
        rightBtns.add(btnBack);
        top.add(rightBtns, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        summaryCard = buildSummaryCard();
        body.add(summaryCard);

        root.add(body, BorderLayout.CENTER);

        return root;
    }

    private JPanel buildSummaryCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(MAU_CARD);
        card.setBorder(new LineBorder(MAU_BORDER, 1, true));

        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(MAU_CARD);
        head.setBorder(new EmptyBorder(14, 20, 4, 20));

        lblSummaryTitle = new JLabel("Hóa đơn tháng");
        lblSummaryTitle.setFont(FONT_BOLD);
        lblSummaryTitle.setForeground(MAU_TEXT);

        JButton btnExport = primaryButton.makePrimaryButton("Xuất hóa đơn");
        btnExport.addActionListener(e -> exportCurrentMonth());

        head.add(lblSummaryTitle, BorderLayout.WEST);
        head.add(btnExport, BorderLayout.EAST);

        summaryModel = new DefaultTableModel(new String[] {}, 0);

        tblSummary = new JTable(summaryModel);
        tblSummary.setRowHeight(46);
        tblSummary.setFont(FONT_PLAIN);
        tblSummary.setShowVerticalLines(false);
        tblSummary.setShowHorizontalLines(true);
        tblSummary.setGridColor(MAU_BORDER);
        tblSummary.setSelectionBackground(new Color(239, 246, 255));

        tblSummary.getTableHeader().setFont(FONT_SMALL);
        tblSummary.getTableHeader().setBackground(new Color(248, 250, 252));
        tblSummary.getTableHeader().setReorderingAllowed(false);

        JScrollPane sp = new JScrollPane(tblSummary);
        sp.setBorder(new EmptyBorder(0, 20, 16, 20));
        sp.getViewport().setBackground(MAU_CARD);

        card.add(head, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildHistoryCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(MAU_CARD);
        card.setBorder(new LineBorder(MAU_BORDER, 1, true));

        JLabel title = new JLabel("Lịch sử hóa đơn");
        title.setFont(FONT_BOLD);
        title.setForeground(MAU_TEXT);
        title.setBorder(new EmptyBorder(14, 16, 0, 16));

        String[] cols = { "Tháng/Năm", "Số phòng", "Tổng doanh thu", "Ngày tạo" };
        historyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblHistory = new JTable(historyModel);
        tblHistory.setRowHeight(48);
        tblHistory.setFont(FONT_PLAIN);
        tblHistory.setShowVerticalLines(false);
        tblHistory.setGridColor(MAU_BORDER);
        tblHistory.getTableHeader().setFont(FONT_SMALL);
        tblHistory.getTableHeader().setBackground(new Color(248, 250, 252));
        tblHistory.getTableHeader().setReorderingAllowed(false);

        tblHistory.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    int row = tblHistory.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < historyItems.size()) {
                        loadMonthFromHistory(historyItems.get(row).monthYear);
                    }
                }
            }
        });
        tblHistory.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        tblHistory.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r,
                    int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setFont(FONT_PLAIN);
                l.setForeground(AppColors.SLATE_900);
                l.setBackground(isSel ? t.getSelectionBackground() : AppColors.WHITE);
                l.setOpaque(true);

                if (c == 2) {
                    l.setForeground(new Color(22, 163, 74));
                    l.setFont(FONT_BOLD);
                }

                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                        new EmptyBorder(0, 16, 0, 8)));
                return l;
            }
        });

        JScrollPane sp = new JScrollPane(tblHistory);
        sp.setBorder(new EmptyBorder(0, 0, 8, 0));
        sp.getViewport().setBackground(MAU_CARD);

        card.add(title, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private void applySummaryRenderers() {
        tblSummary.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r,
                    int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setFont(FONT_PLAIN);
                l.setForeground(AppColors.SLATE_900);
                l.setBackground(isSel ? t.getSelectionBackground() : AppColors.WHITE);
                l.setOpaque(true);
                l.setHorizontalAlignment(SwingConstants.LEFT);

                if (c == 0) {
                    l.setForeground(new Color(37, 99, 235));
                    l.setFont(FONT_BOLD);
                } else if (c == 5) {
                    l.setFont(FONT_BOLD);
                    l.setForeground(new Color(22, 163, 74));
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (c >= 1 && c <= 4) {
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                }

                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.SLATE_200),
                        new EmptyBorder(0, 16, 0, 8)));
                return l;
            }
        });
    }

    private void showCreateDialog() {
        Window owner = SwingUtilities.getWindowAncestor(summaryCard);
        JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Tính hóa đơn", true);
        dlg.setSize(420, 230);
        dlg.setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(MAU_CARD);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new GridLayout(1, 2, 10, 0));
        form.setOpaque(false);
        JComboBox<String> cMonth = makeCombo(MONTHS);
        cMonth.setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH));
        JTextField fYear = makeField(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));

        form.add(wrapField("Tháng", cMonth));
        form.add(wrapField("Năm", fYear));

        JButton btnCalc = primaryButton.makePrimaryButton("Tính tổng tiền");
        btnCalc.addActionListener(e -> {
            String month = String.valueOf(cMonth.getSelectedItem());
            String year = fYear.getText().trim();
            if (!year.matches("\\d{4}")) {
                JOptionPane.showMessageDialog(dlg, "Năm không hợp lệ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int m = Integer.parseInt(month);
            int y = Integer.parseInt(year);
            if (hdDAO.daCoHoaDonThang(m, y)) {
                JOptionPane.showMessageDialog(dlg,
                        "Tháng " + month + "/" + year + " đã có hóa đơn trong hệ thống.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Kiểm tra chỉ số điện/nước
            java.util.List<String> phongChuaNhap = new java.util.ArrayList<>();
            for (Phong p : phongDAO.getAllPhongDaThue()) {
                if (dienNuocDAO.layChiSoTheoThangVoiNgay(p.getMaPhong(), m, y) == null) {
                    phongChuaNhap.add(p.getMaPhong());
                }
            }
            if (!phongChuaNhap.isEmpty()) {
                JOptionPane.showMessageDialog(dlg,
                        "Các phòng sau chưa nhập chỉ số điện/nước tháng " + month + "/" + year + ":\n"
                                + String.join(", ", phongChuaNhap)
                                + "\nVui lòng nhập chỉ số trước khi tính hóa đơn.",
                        "Chưa nhập chỉ số điện/nước", JOptionPane.WARNING_MESSAGE);
                dlg.dispose();
                if (onNavigateToChiSo != null)
                    onNavigateToChiSo.run();
                return;
            }
            calculateDraft(month, year);
            dlg.dispose();

            // Lưu vào database
            saveToDatabase();
            loadHistory();
            if (onInvoiceSaved != null) {
                onInvoiceSaved.run();
            }

            // Hiển thị bảng hóa đơn
            rebuildSummaryModel();
            refreshSummaryCard();
            mainCardLayout.show((java.awt.Container) pnlRoot.getComponent(0), "calc");
        });

        root.add(form, BorderLayout.CENTER);
        root.add(btnCalc, BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private void calculateDraft(String month, String year) {
        currentDrafts.clear();
        currentMonth = month;
        currentYear = year;

        List<Phong> rooms = phongDAO.getAllPhongDaThue();
        if (rooms == null) {
            return;
        }

        Map<String, DichVu> dvById = new HashMap<>();
        for (DichVu dv : dvDAO.layTatCa()) {
            dvById.put(dv.getMaDichVu(), dv);
        }

        DichVu dvDien = dvDAO.getDichVuByTen("Điện");
        DichVu dvNuoc = dvDAO.getDichVuByTen("Nước");
        double donGiaDien = 0;
        double donGiaNuoc = 0;
        if (dvDien != null && dvDien.getMaGiaDetail() != null) {
            GiaDetail gd = giaDetailDAO.getDonGiaByMa(dvDien.getMaGiaDetail());
            donGiaDien = gd == null ? 0 : gd.getDonGia();
        }
        if (dvNuoc != null && dvNuoc.getMaGiaDetail() != null) {
            GiaDetail gd = giaDetailDAO.getDonGiaByMa(dvNuoc.getMaGiaDetail());
            donGiaNuoc = gd == null ? 0 : gd.getDonGia();
        }

        int m = Integer.parseInt(month);
        int y = Integer.parseInt(year);

        // 1. Tải hoá đơn đã có trong tháng (vd: thanh lý hợp đồng giữa tháng) làm draft
        // read-only
        for (RoomMonthSummary existS : hdDAO.getRoomSummariesTheoThang(m, y)) {
            MonthlyRoomDraft ed = new MonthlyRoomDraft();
            ed.maPhong = existS.maPhong;
            ed.donGiaDien = existS.donGiaDien;
            ed.donGiaNuoc = existS.donGiaNuoc;
            ed.tienPhong = existS.tienPhong;
            ed.daThanhToan = existS.daThanhToan;
            ed.isExisting = true;
            // Lấy chỉ số thực tế từ ChiSoDienNuoc
            String maHopDongEx = hdkhDAO.getMaHopDongHienTai(existS.maPhong);
            ed.maHopDong = maHopDongEx != null ? maHopDongEx : "";
            ChiSoDienNuoc curFullEx = (maHopDongEx != null)
                    ? dienNuocDAO.layChiSoTheoThangVoiNgay(maHopDongEx, m, y) : null;
            if (curFullEx != null) {
                int[] old = dienNuocDAO.layChiSoTruocNgay(maHopDongEx, curFullEx.getNgayGhi());
                ed.soDienCu = old[0];
                ed.soNuocCu = old[1];
                ed.soDienMoi = curFullEx.getSoDien();
                ed.soNuocMoi = curFullEx.getSoNuoc();
            } else {
                ed.soDienCu = 0;
                ed.soDienMoi = existS.tieuThuDien;
                ed.soNuocCu = 0;
                ed.soNuocMoi = existS.tieuThuNuoc;
            }
            ed.tienDienOverride = existS.tieuThuDien * existS.donGiaDien;
            ed.tienNuocOverride = existS.tieuThuNuoc * existS.donGiaNuoc;
            // Tên người đại diện
            KhachHang khEx = hdkhDAO.getNguoiDaiDienByMaHopDong(existS.maHoaDon);
            ed.tenKhach = (khEx != null) ? khEx.getHoTen() : "";
            for (BillServiceItem si : existS.services) {
                ServiceOption op = new ServiceOption();
                op.maDichVu = si.maDichVu;
                op.tenDichVu = si.tenKhoan;
                op.donGia = si.donGia * si.soLuong; // tổng tiền để hiển thị đúng
                op.selected = true;
                ed.options.add(op);
            }
            currentDrafts.add(ed);
        }

        // 2. Tạo draft mới cho tất cả phòng đang thuê
        List<PhuongTien> allVehicles = ptDAO.getAllPhuongTien();

        for (Phong room : rooms) {
            // Đảm bảo tất cả dịch vụ được gán cho phòng
            dvDAO.ganTatCaDichVuChoPhongNeuChuaCo(room.getMaPhong());

            MonthlyRoomDraft d = new MonthlyRoomDraft();
            d.maPhong = room.getMaPhong();
            d.donGiaDien = donGiaDien;
            d.donGiaNuoc = donGiaNuoc;
            KhachHang khNew = hdkhDAO.getNguoiDaiDienByMaPhong(room.getMaPhong());
            d.tenKhach = (khNew != null) ? khNew.getHoTen() : "";

            String maHopDongNew = hdkhDAO.getMaHopDongHienTai(room.getMaPhong());
            d.maHopDong = maHopDongNew != null ? maHopDongNew : "";
            ChiSoDienNuoc curFull = (maHopDongNew != null)
                    ? dienNuocDAO.layChiSoTheoThangVoiNgay(maHopDongNew, m, y) : null;
            d.hasChiSoMoi = curFull != null;
            int[] old = curFull != null
                    ? dienNuocDAO.layChiSoTruocNgay(maHopDongNew, curFull.getNgayGhi())
                    : (maHopDongNew != null
                            ? dienNuocDAO.layChiSoThangTruoc(maHopDongNew, m, y)
                            : new int[]{0, 0});
            d.soDienCu = old[0];
            d.soNuocCu = old[1];
            d.soDienMoi = curFull == null ? old[0] : curFull.getSoDien();
            d.soNuocMoi = curFull == null ? old[1] : curFull.getSoNuoc();

            if (room.getMaGiaDetail() != null) {
                GiaDetail gdPhong = giaDetailDAO.getDonGiaByMa(room.getMaGiaDetail());
                d.tienPhong = gdPhong == null ? 0 : gdPhong.getDonGia();
            }

            Set<String> selectedIds = dvDAO.layMaDichVuTheoPhong(room.getMaPhong());
            for (String maDv : selectedIds) {
                DichVu dv = dvById.get(maDv);
                if (dv == null || dv.getTenDichVu() == null) {
                    continue;
                }
                String ten = dv.getTenDichVu().toLowerCase();
                if (ten.contains("điện") || ten.contains("nuoc") || ten.contains("nước")) {
                    continue;
                }
                ServiceOption op = new ServiceOption();
                op.maDichVu = dv.getMaDichVu();
                op.tenDichVu = dv.getTenDichVu();
                Double donGia = dv.getDonGia();
                op.donGia = donGia == null ? 0 : donGia;
                op.selected = true;
                d.options.add(op);
            }

            // Thêm phí phương tiện (theo từng xe đăng ký trong phòng)
            for (PhuongTien pt : allVehicles) {
                if (room.getMaPhong().equals(pt.getMaPhong())) {
                    ServiceOption vop = new ServiceOption();
                    vop.maDichVu = "DVXE";
                    vop.tenDichVu = "Gửi xe: " + pt.getLoaiXe() + " (" + pt.getBienSo() + ")";
                    vop.donGia = pt.getMucPhi();
                    vop.selected = true;
                    d.options.add(vop);
                }
            }

            currentDrafts.add(d);
        }
    }

    private void saveToDatabase() {
        if (currentDrafts.isEmpty()) {
            return;
        }

        int thang = Integer.parseInt(currentMonth);
        int nam = Integer.parseInt(currentYear);

        List<String> loiChiSo = new ArrayList<>();
        for (MonthlyRoomDraft d : currentDrafts) {
            // Bỏ qua draft đã có trong DB hoặc đã có chỉ số trong tháng
            if (d.isExisting || d.hasChiSoMoi)
                continue;
            int ngayHD = LocalDate.of(nam, thang, 1).lengthOfMonth();
            ChiSoDienNuoc chiSo = new ChiSoDienNuoc(d.maHopDong, LocalDate.of(nam, thang, ngayHD), d.soDienMoi, d.soNuocMoi);
            String err = dienNuocDAO.luuHoacCapNhat(chiSo);
            if (err != null) {
                loiChiSo.add(d.maPhong + ": " + err);
            }
        }
        if (!loiChiSo.isEmpty()) {
            JOptionPane.showMessageDialog(summaryCard,
                    "Lưu chỉ số điện/nước thất bại:\n" + String.join("\n", loiChiSo),
                    "Lỗi lưu chỉ số", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DichVu dvDien = dvDAO.getDichVuByTen("Điện");
        DichVu dvNuoc = dvDAO.getDichVuByTen("Nước");
        ArrayList<Bill> bills = new ArrayList<>();
        for (MonthlyRoomDraft d : currentDrafts) {
            if (d.isExisting)
                continue; // Bỏ qua draft đã tồn tại trong DB
            Bill b = new Bill();
            b.phong = d.maPhong;
            b.month = currentMonth;
            b.year = currentYear;
            b.daThanhToan = false;
            b.tongTieuThuD = d.tieuThuDien();
            b.tongTieuThuN = d.tieuThuNuoc();
            b.tienDien = d.getTienDien();
            b.tienNuoc = d.getTienNuoc();
            b.donGiaDien = b.tongTieuThuD > 0 ? b.tienDien / b.tongTieuThuD : d.donGiaDien;
            b.donGiaNuoc = b.tongTieuThuN > 0 ? b.tienNuoc / b.tongTieuThuN : d.donGiaNuoc;
            b.tienPhong = d.tienPhong;
            b.maDichVuDien = dvDien == null ? null : dvDien.getMaDichVu();
            b.maDichVuNuoc = dvNuoc == null ? null : dvNuoc.getMaDichVu();
            for (ServiceOption op : d.options) {
                if (!op.selected)
                    continue;
                BillServiceItem si = new BillServiceItem();
                si.maDichVu = op.maDichVu;
                si.tenKhoan = op.tenDichVu;
                si.soLuong = 1;
                si.donGia = op.donGia;
                b.dichVuKhac.add(si);
            }
            bills.add(b);
        }

        if (!hdDAO.luuNhieuHoaDonMoi(bills, "AD01")) {
            JOptionPane.showMessageDialog(summaryCard, "Không thể lưu hóa đơn vào database.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rebuildSummaryModel() {
        summaryModel = new DefaultTableModel(
                new String[] { "Phòng", "Tiền phòng", "Điện", "Nước", "Dịch vụ", "Tổng", "Đã TT" },
                0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // Khóa checkbox "Dã TT" và cài đặt cho hóa đơn đã tồn tại (read-only)
                if (row >= 0 && row < currentDrafts.size() && currentDrafts.get(row).isExisting) {
                    return col == 7; // chỉ cho xem chi tiết
                }
                return col == 6 || col == 7 || col == 8;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                return col == 6 ? Boolean.class : Object.class;
            }
        };
        final boolean[] updating = { false };
        summaryModel.addTableModelListener(e -> {
            int row = e.getFirstRow(), col = e.getColumn();
            if (row >= 0 && row < currentDrafts.size() && col == 6) {
                if (updating[0])
                    return;
                boolean paid = Boolean.TRUE.equals(summaryModel.getValueAt(row, 6));
                MonthlyRoomDraft d = currentDrafts.get(row);
                String action = paid ? "xác nhận đã thanh toán" : "hủy thanh toán";
                int confirm = JOptionPane.showConfirmDialog(
                        tblSummary,
                        "Bạn có chắc muốn " + action + " cho phòng " + d.maPhong + "?",
                        "Xác nhận thay đổi trạng thái",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) {
                    updating[0] = true;
                    summaryModel.setValueAt(!paid, row, 6);
                    updating[0] = false;
                    return;
                }
                d.daThanhToan = paid;
                if (!currentMonth.isEmpty() && !currentYear.isEmpty()) {
                    hdDAO.updateTrangThaiThanhToan(d.maPhong,
                            Integer.parseInt(currentMonth), Integer.parseInt(currentYear), paid);
                }
            }
        });
        tblSummary.setModel(summaryModel);
        tblSummary.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Right-click context menu
        JPopupMenu summaryMenu = new JPopupMenu();
        JMenuItem miXemHoaDon = new JMenuItem("Xem hóa đơn");
        JMenuItem miCaiDat = new JMenuItem("Chỉnh sửa");
        summaryMenu.add(miXemHoaDon);
        summaryMenu.add(miCaiDat);

        tblSummary.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    showSummaryMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    showSummaryMenu(e);
            }

            private void showSummaryMenu(MouseEvent e) {
                int row = tblSummary.rowAtPoint(e.getPoint());
                if (row < 0)
                    return;
                tblSummary.setRowSelectionInterval(row, row);
                boolean isExisting = row < currentDrafts.size() && currentDrafts.get(row).isExisting;
                miCaiDat.setEnabled(!isExisting);
                summaryMenu.show(tblSummary, e.getX(), e.getY());
            }
        });

        miXemHoaDon.addActionListener(e -> {
            int row = tblSummary.getSelectedRow();
            if (row >= 0)
                showInvoicePreviewDialog(row);
        });
        miCaiDat.addActionListener(e -> {
            int row = tblSummary.getSelectedRow();
            if (row >= 0)
                showRoomDetailDialog(row);
        });

        applySummaryRenderers();
    }

    private void refreshSummaryCard() {
        summaryModel.setRowCount(0);
        if (currentDrafts.isEmpty()) {
            summaryCard.setVisible(false);
            return;
        }

        summaryCard.setVisible(true);
        lblSummaryTitle.setText("Hóa đơn tháng " + currentMonth + "/" + currentYear);
        for (MonthlyRoomDraft d : currentDrafts) {
            String tenPhong = d.isExisting ? "(cũ) " + d.maPhong : d.maPhong;
            summaryModel.addRow(new Object[] {
                    tenPhong,
                    formatMoney(d.tienPhong),
                    formatMoney(d.getTienDien()) + " (" + d.tieuThuDien() + " kWh)",
                    formatMoney(d.getTienNuoc()) + " (" + d.tieuThuNuoc() + " m³)",
                    formatMoney(d.tienDichVuKhac()),
                    formatMoney(d.tongTien()),
                    d.daThanhToan
            });
        }
    }

    private void loadCurrentMonthDrafts() {
        Calendar cal = Calendar.getInstance();
        int m = cal.get(Calendar.MONTH) + 1;
        int y = cal.get(Calendar.YEAR);
        if (!hdDAO.daCoHoaDonThang(m, y))
            return;
        loadDraftsFromDB(m, y);
    }

    private void loadMonthFromHistory(String monthYear) {
        String[] parts = monthYear.split("/");
        if (parts.length != 2)
            return;
        loadDraftsFromDB(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        rebuildSummaryModel();
        refreshSummaryCard();
        // Switch to calc view
        mainCardLayout.show((java.awt.Container) pnlRoot.getComponent(0), "calc");
    }

    private void loadDraftsFromDB(int m, int y) {
        currentMonth = String.valueOf(m);
        currentYear = String.valueOf(y);
        currentDrafts.clear();

        List<RoomMonthSummary> summaries = hdDAO.getRoomSummariesTheoThang(m, y);
        for (RoomMonthSummary s : summaries) {
            MonthlyRoomDraft d = new MonthlyRoomDraft();
            d.maPhong = s.maPhong;
            d.donGiaDien = s.donGiaDien;
            d.donGiaNuoc = s.donGiaNuoc;
            d.tienPhong = s.tienPhong;
            d.daThanhToan = s.daThanhToan;
            // Lấy chỉ số thực tế từ ChiSoDienNuoc
            String maHopDongDB = hdkhDAO.getMaHopDongHienTai(s.maPhong);
            d.maHopDong = maHopDongDB != null ? maHopDongDB : "";
            ChiSoDienNuoc curFullDB = (maHopDongDB != null)
                    ? dienNuocDAO.layChiSoTheoThangVoiNgay(maHopDongDB, m, y) : null;
            if (curFullDB != null) {
                int[] old = dienNuocDAO.layChiSoTruocNgay(maHopDongDB, curFullDB.getNgayGhi());
                d.soDienCu = old[0];
                d.soNuocCu = old[1];
                d.soDienMoi = curFullDB.getSoDien();
                d.soNuocMoi = curFullDB.getSoNuoc();
            } else {
                d.soDienCu = 0;
                d.soDienMoi = s.tieuThuDien;
                d.soNuocCu = 0;
                d.soNuocMoi = s.tieuThuNuoc;
            }
            d.tienDienOverride = s.tieuThuDien * s.donGiaDien;
            d.tienNuocOverride = s.tieuThuNuoc * s.donGiaNuoc;
            // Tên người đại diện
            KhachHang khDB = hdkhDAO.getNguoiDaiDienByMaHopDong(s.maHoaDon);
            d.tenKhach = (khDB != null) ? khDB.getHoTen() : "";

            for (BillServiceItem si : s.services) {
                ServiceOption op = new ServiceOption();
                op.maDichVu = si.maDichVu;
                op.tenDichVu = si.tenKhoan;
                op.donGia = si.donGia;
                op.selected = true;
                d.options.add(op);
            }
            currentDrafts.add(d);
        }
    }

    private void loadHistory() {
        historyItems.clear();
        historyModel.setRowCount(0);
        for (Object[] row : hdDAO.getLichSuHoaDonTongHop()) {
            HistoryItem item = new HistoryItem();
            item.monthYear = String.valueOf(row[0]);
            item.soPhong = ((Number) row[1]).intValue();
            item.tongDoanhThu = ((Number) row[2]).doubleValue();
            item.ngayTao = (LocalDate) row[3];
            historyItems.add(item);

            historyModel.addRow(new Object[] {
                    item.monthYear,
                    item.soPhong + " phòng",
                    formatMoney(item.tongDoanhThu),
                    item.ngayTao == null ? "" : DF.format(item.ngayTao)
            });
        }
    }

    private ImageIcon loadEyeIcon() {
        ImageIcon raw = new ImageIcon("img/icons/eye.png");
        if (raw.getIconWidth() <= 0 || raw.getIconHeight() <= 0) {
            return null;
        }
        Image scaled = raw.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private ImageIcon loadGearIcon() {
        ImageIcon raw = new ImageIcon("img/icons/settings.png");
        if (raw.getIconWidth() <= 0 || raw.getIconHeight() <= 0) {
            return null;
        }
        Image scaled = raw.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void exportCurrentMonth() {
        if (currentDrafts.isEmpty()) {
            JOptionPane.showMessageDialog(summaryCard, "Chưa có dữ liệu để xuất.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Chọn thư mục lưu
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn thư mục lưu hóa đơn");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        if (fc.showSaveDialog(summaryCard) != JFileChooser.APPROVE_OPTION)
            return;

        // Xuất ảnh hàng loạt
        File dir = fc.getSelectedFile();
        List<String> errors = new ArrayList<>();
        int count = 0;

        for (MonthlyRoomDraft d : currentDrafts) {
            try {
                JPanel panel = buildInvoicePanel(d);
                BufferedImage img = renderPanelToImage(panel);
                String fileName = "HoaDon_" + d.maPhong + "_T" + currentMonth + "_" + currentYear + ".png";
                ImageIO.write(img, "png", new File(dir, fileName));
                count++;
            } catch (Exception ex) {
                errors.add(d.maPhong + ": " + ex.getMessage());
            }
        }

        if (errors.isEmpty()) {
            JOptionPane.showMessageDialog(summaryCard,
                    "Đã lưu và xuất " + count + " hóa đơn vào:\n" + dir.getAbsolutePath(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(summaryCard,
                    "Xuất " + count + "/" + currentDrafts.size() + " hóa đơn.\nLỗi:\n" + String.join("\n", errors),
                    "Có lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private BufferedImage renderPanelToImage(JPanel panel) {
        panel.setSize(panel.getPreferredSize());
        doLayoutRecursive(panel);
        Dimension sz = panel.getSize();
        BufferedImage img = new BufferedImage(
                Math.max(1, sz.width), Math.max(1, sz.height), BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        panel.printAll(g);
        g.dispose();
        return img;
    }

    private void doLayoutRecursive(java.awt.Container c) {
        c.doLayout();
        for (Component child : c.getComponents()) {
            if (child instanceof java.awt.Container) {
                doLayoutRecursive((java.awt.Container) child);
            }
        }
    }

    private String formatMoney(double value) {
        return NF.format(Math.round(value)) + "đ";
    }

    private JButton makeOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_PLAIN);
        btn.setForeground(MAU_TEXT);
        btn.setBackground(MAU_CARD);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(MAU_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTextField makeField(String val) {
        JTextField f = new JTextField(val);
        f.setFont(FONT_PLAIN);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(MAU_BORDER, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        return f;
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_PLAIN);
        cb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(MAU_BORDER, 1, true),
                new EmptyBorder(3, 8, 3, 8)));
        return cb;
    }

    private JPanel wrapField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(MAU_TEXT);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private class EyeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setBackground(isSelected ? table.getSelectionBackground() : AppColors.WHITE);
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.SLATE_200),
                    new EmptyBorder(0, 4, 0, 4)));
            lbl.setIcon(eyeIcon);
            lbl.setText(eyeIcon == null ? "👁" : "");
            return lbl;
        }
    }

    private class SummaryDetailEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn = new JButton();
        private int row;

        SummaryDetailEditor() {
            btn.setIcon(eyeIcon);
            btn.setText(eyeIcon == null ? "👁" : "");
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                stopCellEditing();
                showInvoicePreviewDialog(row);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            this.row = row;
            return btn;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    private void refreshRowInTable(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= currentDrafts.size())
            return;
        MonthlyRoomDraft d = currentDrafts.get(rowIndex);
        summaryModel.setValueAt(formatMoney(d.tienPhong), rowIndex, 1);
        summaryModel.setValueAt(formatMoney(d.getTienDien()), rowIndex, 2);
        summaryModel.setValueAt(formatMoney(d.getTienNuoc()), rowIndex, 3);
        summaryModel.setValueAt(formatMoney(d.tienDichVuKhac()), rowIndex, 4);
        summaryModel.setValueAt(formatMoney(d.tongTien()), rowIndex, 5);
    }

    private void showRoomDetailDialog(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= currentDrafts.size())
            return;
        MonthlyRoomDraft d = currentDrafts.get(rowIndex);

        Window owner = SwingUtilities.getWindowAncestor(summaryCard);
        JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null,
                "Chi tiết hóa đơn phòng " + d.maPhong, true);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(MAU_CARD);
        root.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel lblTitle = new JLabel("Phòng " + d.maPhong + "  —  Tháng " + currentMonth + "/" + currentYear);
        lblTitle.setFont(FONT_BOLD);
        lblTitle.setForeground(MAU_TEXT);
        lblTitle.setBorder(new EmptyBorder(0, 0, 18, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setBackground(MAU_CARD);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // ── Tiền phòng ──
        JTextField txtTienPhong = makeField(NF.format((long) d.tienPhong));
        txtTienPhong.setEditable(false);
        txtTienPhong.setMaximumSize(new Dimension(5, 38));

        // ── Điện ──
        JTextField txtDienCu = makeField(String.valueOf(d.soDienCu));
        txtDienCu.setEditable(false);
        txtDienCu.setBackground(MAU_NEN);
        txtDienCu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JTextField txtDienMoi = makeField(String.valueOf(d.soDienMoi));
        txtDienMoi.setEditable(true);
        txtDienMoi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel lblKwh = new JLabel("Tiêu thụ: " + d.tieuThuDien() + " kWh  →  " + formatMoney(d.getTienDien()));
        lblKwh.setFont(FONT_SMALL);
        lblKwh.setForeground(new Color(100, 100, 100));
        lblKwh.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Nước ──
        JTextField txtNuocCu = makeField(String.valueOf(d.soNuocCu));
        txtNuocCu.setEditable(false);
        txtNuocCu.setBackground(MAU_NEN);
        txtNuocCu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JTextField txtNuocMoi = makeField(String.valueOf(d.soNuocMoi));
        txtNuocMoi.setEditable(true);
        txtNuocMoi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel lblM3 = new JLabel("Tiêu thụ: " + d.tieuThuNuoc() + " m³  →  " + formatMoney(d.getTienNuoc()));
        lblM3.setFont(FONT_SMALL);
        lblM3.setForeground(new Color(100, 100, 100));
        lblM3.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Dịch vụ ──
        JCheckBox[] checks = new JCheckBox[d.options.size()];
        for (int i = 0; i < d.options.size(); i++) {
            ServiceOption op = d.options.get(i);
            checks[i] = new JCheckBox(op.tenDichVu + "  —  " + formatMoney(op.donGia), op.selected);
            checks[i].setFont(FONT_PLAIN);
            checks[i].setForeground(MAU_TEXT);
            checks[i].setOpaque(false);
            checks[i].setEnabled(true);
            checks[i].putClientProperty("idx", i);
            checks[i].setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        // ── Build form ──
        JPanel pTienPhong = wrapField("Tiền phòng (VNĐ)", txtTienPhong);
        pTienPhong.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(pTienPhong);
        form.add(Box.createVerticalStrut(10));

        JPanel rowDien = new JPanel(new GridLayout(1, 2, 10, 0));
        rowDien.setBackground(MAU_CARD);
        rowDien.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowDien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        rowDien.add(wrapField("Số điện cũ (kWh)", txtDienCu));
        rowDien.add(wrapField("Số điện mới (kWh)", txtDienMoi));
        form.add(rowDien);
        form.add(Box.createVerticalStrut(4));
        form.add(lblKwh);
        form.add(Box.createVerticalStrut(10));

        JPanel rowNuoc = new JPanel(new GridLayout(1, 2, 10, 0));
        rowNuoc.setBackground(MAU_CARD);
        rowNuoc.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowNuoc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        rowNuoc.add(wrapField("Số nước cũ (m³)", txtNuocCu));
        rowNuoc.add(wrapField("Số nước mới (m³)", txtNuocMoi));
        form.add(rowNuoc);
        form.add(Box.createVerticalStrut(4));
        form.add(lblM3);
        form.add(Box.createVerticalStrut(10));

        if (checks.length > 0) {
            JLabel lblDv = new JLabel("Dịch vụ sử dụng");
            lblDv.setFont(FONT_SMALL);
            lblDv.setForeground(new Color(100, 100, 100));
            lblDv.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(lblDv);
            form.add(Box.createVerticalStrut(4));
            for (JCheckBox cb : checks)
                form.add(cb);
            form.add(Box.createVerticalStrut(10));
        }

        JLabel lblTongLabel = new JLabel("Tổng cộng");
        lblTongLabel.setFont(FONT_SMALL);
        lblTongLabel.setForeground(new Color(100, 100, 100));
        lblTongLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblTong = new JLabel(formatMoney(d.tongTien()));
        lblTong.setFont(new Font("Be Vietnam Pro", Font.BOLD, 15));
        lblTong.setForeground(MAU_TEXT);
        lblTong.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblTongLabel);
        form.add(Box.createVerticalStrut(2));
        form.add(lblTong);

        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null);
        sp.getViewport().setBackground(MAU_CARD);
        root.add(sp, BorderLayout.CENTER);

        // ── Buttons ──
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(MAU_CARD);
        btnPanel.setBorder(new EmptyBorder(14, 0, 0, 0));

        JButton btnClose = makeOutlineButton("Đóng");
        btnClose.addActionListener(e -> dlg.dispose());

        JButton btnImg = makeOutlineButton("Tải ảnh (PNG)");
        btnImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Lưu hóa đơn");
            fc.setSelectedFile(new File("HoaDon_" + d.maPhong + "_T" + currentMonth + "_" + currentYear + ".png"));
            fc.setFileFilter(new FileNameExtensionFilter("Hình ảnh PNG (*.png)", "png"));
            if (fc.showSaveDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (!f.getName().toLowerCase().endsWith(".png"))
                    f = new File(f.getAbsolutePath() + ".png");
                try {
                    ImageIO.write(renderPanelToImage(buildInvoicePanel(d)), "png", f);
                    JOptionPane.showMessageDialog(dlg, "Đã lưu:\n" + f.getAbsolutePath(),
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnApply = primaryButton.makePrimaryButton("Áp dụng");
        btnApply.addActionListener(e -> {
            // Tiền phòng
            String tienStr = txtTienPhong.getText().trim().replaceAll("[^0-9]", "");
            if (!tienStr.isEmpty()) {
                try {
                    d.tienPhong = Long.parseLong(tienStr);
                } catch (NumberFormatException ignored) {
                }
            }
            // Số điện mới
            try {
                int dienMoi = Integer.parseInt(txtDienMoi.getText().trim());
                if (dienMoi < d.soDienCu) {
                    JOptionPane.showMessageDialog(dlg,
                            "Số điện mới không thể nhỏ hơn số điện cũ (" + d.soDienCu + ")!",
                            "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                d.soDienMoi = dienMoi;
                d.tienDienOverride = -1;
            } catch (NumberFormatException ignored) {
            }
            // Số nước mới
            try {
                int nuocMoi = Integer.parseInt(txtNuocMoi.getText().trim());
                if (nuocMoi < d.soNuocCu) {
                    JOptionPane.showMessageDialog(dlg,
                            "Số nước mới không thể nhỏ hơn số nước cũ (" + d.soNuocCu + ")!",
                            "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                d.soNuocMoi = nuocMoi;
                d.tienNuocOverride = -1;
            } catch (NumberFormatException ignored) {
            }
            // Dịch vụ
            for (JCheckBox cb : checks) {
                int idx = (int) cb.getClientProperty("idx");
                d.options.get(idx).selected = cb.isSelected();
            }
            refreshRowInTable(rowIndex);
            dlg.dispose();
        });
        btnPanel.add(btnImg);
        btnPanel.add(btnClose);
        btnPanel.add(btnApply);
        root.add(btnPanel, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setSize(440, 600);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    private void showInvoicePreviewDialog(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= currentDrafts.size())
            return;
        MonthlyRoomDraft d = currentDrafts.get(rowIndex);

        Window owner = SwingUtilities.getWindowAncestor(summaryCard);
        JDialog prev = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Xem trước hóa đơn", true);
        prev.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(MAU_NEN);

        // Dialog header bar
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(MAU_NEN);
        hdr.setBorder(new EmptyBorder(14, 20, 10, 20));
        JLabel hTitle = new JLabel("Xem trước hóa đơn");
        hTitle.setFont(FONT_BOLD);
        hTitle.setForeground(MAU_TEXT);
        hdr.add(hTitle, BorderLayout.WEST);
        root.add(hdr, BorderLayout.NORTH);

        // Invoice panel (sẽ được render ra ảnh)
        JPanel invoicePanel = buildInvoicePanel(d);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(MAU_NEN);
        wrap.setBorder(new EmptyBorder(0, 24, 8, 24));
        wrap.add(invoicePanel, BorderLayout.CENTER);
        root.add(wrap, BorderLayout.CENTER);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        footer.setBackground(MAU_NEN);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, MAU_BORDER));

        JButton btnClose = makeOutlineButton("Đóng");
        JButton btnDownload = primaryButton.makePrimaryButton("Tải về (PNG)");

        btnClose.addActionListener(e -> prev.dispose());
        btnDownload.addActionListener(e -> {
            invoicePanel.revalidate();
            invoicePanel.repaint();
            Dimension sz = invoicePanel.getSize();
            BufferedImage img = new BufferedImage(sz.width, sz.height, BufferedImage.TYPE_INT_RGB);
            Graphics g = img.getGraphics();
            invoicePanel.printAll(g);
            g.dispose();

            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Lưu hóa đơn");
            fc.setSelectedFile(new File("HoaDon_" + d.maPhong + "_T" + currentMonth + "_" + currentYear + ".png"));
            fc.setFileFilter(new FileNameExtensionFilter("Hình ảnh PNG (*.png)", "png"));
            if (fc.showSaveDialog(prev) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (!f.getName().toLowerCase().endsWith(".png"))
                    f = new File(f.getAbsolutePath() + ".png");
                try {
                    ImageIO.write(img, "png", f);
                    JOptionPane.showMessageDialog(prev, "Đã lưu:\n" + f.getAbsolutePath(),
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(prev, "Lỗi lưu file: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        footer.add(btnClose);
        footer.add(btnDownload);
        root.add(footer, BorderLayout.SOUTH);

        prev.setContentPane(root);
        prev.pack();
        prev.setMinimumSize(new Dimension(580, prev.getHeight()));
        prev.setLocationRelativeTo(owner);
        prev.setVisible(true);
    }

    // Row types cho invoice table
    private static final int ROW_NORMAL = 0;
    private static final int ROW_SUBITEM = 1;
    private static final int ROW_SUBITEM_BOLD = 2;
    private static final int ROW_TOTAL = 3;
    private static final int ROW_EMPTY = 4;

    private JPanel buildInvoicePanel(MonthlyRoomDraft d) {
        Color borderC = new Color(160, 160, 160);
        Color totalBg = new Color(146, 208, 80); // xanh lá như ảnh mẫu
        Font fNormal = new Font("Arial", Font.PLAIN, 13);
        Font fBold = new Font("Arial", Font.BOLD, 13);

        // ── Xây dựng dữ liệu các dòng ──
        // Mỗi phần tử: [stt, dichVu, chiSo, soTien, rowType]
        List<Object[]> rows = new ArrayList<>();
        int stt = 1;

        rows.add(new Object[] { String.valueOf(stt++), "Tiền phòng", "1", formatMoney(d.tienPhong), ROW_NORMAL });
        rows.add(new Object[] { "", "Chỉ số mới", String.valueOf(d.soDienMoi), "", ROW_SUBITEM });
        rows.add(new Object[] { "", "Chỉ số cũ", String.valueOf(d.soDienCu), "", ROW_SUBITEM });
        rows.add(new Object[] { "", "Số kWh điện tiêu thụ", String.valueOf(d.tieuThuDien()), "", ROW_SUBITEM_BOLD });
        rows.add(new Object[] {
                String.valueOf(stt++),
                "Thành tiền (×" + NF.format((long) d.donGiaDien) + " VND)",
                "",
                formatMoney((double) d.tieuThuDien() * d.donGiaDien),
                ROW_NORMAL
        });
        rows.add(new Object[] {
                String.valueOf(stt++), "Tiền nước",
                d.tieuThuNuoc() + " m³",
                formatMoney((double) d.tieuThuNuoc() * d.donGiaNuoc),
                ROW_NORMAL
        });
        for (ServiceOption op : d.options) {
            if (op.selected) {
                rows.add(new Object[] { String.valueOf(stt++), op.tenDichVu, "1", formatMoney(op.donGia), ROW_NORMAL });
            }
        }
        rows.add(new Object[] { "", "", "", "", ROW_EMPTY });
        rows.add(new Object[] { "", "Tổng cộng", "", formatMoney(d.tongTien()), ROW_TOTAL });

        // Tách dữ liệu bảng và mảng loại dòng
        String[] cols = { "STT", "DỊCH VỤ", "CHỈ SỐ", "SỐ TIỀN" };
        Object[][] tableData = new Object[rows.size()][4];
        int[] rowTypes = new int[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            Object[] r = rows.get(i);
            tableData[i] = new Object[] { r[0], r[1], r[2], r[3] };
            rowTypes[i] = (int) r[4];
        }

        DefaultTableModel model = new DefaultTableModel(tableData, cols) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(fNormal);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(borderC);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);
        table.setSelectionBackground(Color.WHITE);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // Cột: STT=45, DỊCH VỤ=240, CHỈ SỐ=110, SỐ TIỀN=130
        int[] colWidths = { 45, 240, 110, 130 };
        for (int c = 0; c < 4; c++)
            table.getColumnModel().getColumn(c).setPreferredWidth(colWidths[c]);

        // Header styling
        JTableHeader th = table.getTableHeader();
        th.setFont(fBold);
        th.setBackground(Color.WHITE);
        th.setForeground(Color.BLACK);
        th.setPreferredSize(new Dimension(0, 30));
        th.setReorderingAllowed(false);

        // Header renderers
        int[] hAlign = { SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER };
        for (int c = 0; c < 4; c++) {
            final int col = c;
            table.getColumnModel().getColumn(c).setHeaderRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r,
                        int co) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, co);
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(Color.BLACK);
                    lbl.setFont(fBold);
                    lbl.setHorizontalAlignment(hAlign[col]);
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(borderC, 1), new EmptyBorder(4, 6, 4, 6)));
                    return lbl;
                }
            });
        }

        // Cell renderers
        for (int c = 0; c < 4; c++) {
            final int col = c;
            table.getColumnModel().getColumn(c).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row,
                        int co) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, co);
                    int type = rowTypes[row];

                    // Màu nền
                    lbl.setBackground(type == ROW_TOTAL ? totalBg : Color.WHITE);
                    lbl.setForeground(Color.BLACK);

                    // Font
                    boolean bold = (type == ROW_TOTAL)
                            || (type == ROW_NORMAL && col == 0)
                            || (type == ROW_SUBITEM_BOLD && col == 2);
                    lbl.setFont(bold ? fBold : fNormal);

                    // Căn lề
                    if (col == 0 || col == 2) {
                        lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    } else if (col == 3) {
                        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    } else {
                        lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    }

                    // Indent cho sub-item cột DỊCH VỤ
                    int leftPad = (col == 1 && (type == ROW_SUBITEM || type == ROW_SUBITEM_BOLD)) ? 22 : 6;
                    lbl.setBorder(new EmptyBorder(3, leftPad, 3, 6));
                    lbl.setOpaque(true);
                    return lbl;
                }
            });
        }

        int tblH = table.getRowHeight() * rows.size() + 30;
        table.setPreferredScrollableViewportSize(new Dimension(525, tblH));
        JScrollPane sp = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(new LineBorder(borderC, 1));
        sp.setPreferredSize(new Dimension(525, tblH + 2));

        // ── Header hóa đơn (MÃ PHÒNG / Tháng) ──
        JPanel hdRow = new JPanel(new BorderLayout(0, 0));
        hdRow.setBackground(Color.WHITE);
        hdRow.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderC, 1), new EmptyBorder(7, 10, 7, 10)));
        String tenKhachDisplay = (d.tenKhach != null && !d.tenKhach.isEmpty()) ? "   —   " + d.tenKhach : "";
        JLabel lblMaPhong = new JLabel("MÃ PHÒNG:   " + d.maPhong + tenKhachDisplay);
        lblMaPhong.setFont(fBold);
        JLabel lblThang = new JLabel("Tháng " + currentMonth + "/" + currentYear);
        lblThang.setFont(fNormal);
        lblThang.setHorizontalAlignment(SwingConstants.RIGHT);
        hdRow.add(lblMaPhong, BorderLayout.WEST);
        hdRow.add(lblThang, BorderLayout.EAST);

        // ── Ghi chú cuối ──
        JLabel footerNote = new JLabel("Lưu ý: vui lòng thanh toán trước ngày 5");
        footerNote.setFont(new Font("Arial", Font.ITALIC, 12));
        footerNote.setForeground(new Color(100, 100, 100));
        footerNote.setBorder(new EmptyBorder(7, 4, 4, 4));

        // Ghép lại
        JPanel invoice = new JPanel(new BorderLayout(0, 0));
        invoice.setBackground(Color.WHITE);
        invoice.setBorder(new LineBorder(borderC, 1));
        invoice.add(hdRow, BorderLayout.NORTH);
        invoice.add(sp, BorderLayout.CENTER);
        invoice.add(footerNote, BorderLayout.SOUTH);

        return invoice;
    }

    private class GearCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setBackground(isSelected ? table.getSelectionBackground() : AppColors.WHITE);
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.SLATE_200),
                    new EmptyBorder(0, 4, 0, 4)));
            lbl.setIcon(gearIcon);
            lbl.setText(gearIcon == null ? "⚙" : "");
            return lbl;
        }
    }

    private class GearDetailEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn = new JButton();
        private int row;

        GearDetailEditor() {
            btn.setIcon(gearIcon);
            btn.setText(gearIcon == null ? "⚙" : "");
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                stopCellEditing();
                showRoomDetailDialog(row);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            this.row = row;
            return btn;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    private class HistoryViewEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn = new JButton();
        private int row;

        HistoryViewEditor() {
            btn.setIcon(eyeIcon);
            btn.setText(eyeIcon == null ? "👁" : "");
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                stopCellEditing();
                if (row >= 0 && row < historyItems.size()) {
                    loadMonthFromHistory(historyItems.get(row).monthYear);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            this.row = row;
            return btn;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}