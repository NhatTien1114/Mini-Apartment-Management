package ui.main;

import dao.ChiSoDienNuocDAO;
import dao.DichVuDAO;
import dao.GiaDetailDAO;
import dao.HoaDonDAO;
import dao.QuanLyPhongDAO;
import entity.ChiSoDienNuoc;
import entity.DichVu;
import entity.GiaDetail;
import entity.Phong;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
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

    private final ChiSoDienNuocDAO dienNuocDAO = new ChiSoDienNuocDAO();
    private final QuanLyPhongDAO phongDAO = new QuanLyPhongDAO();
    private final GiaDetailDAO giaDetailDAO = new GiaDetailDAO();
    private final DichVuDAO dvDAO = new DichVuDAO();
    private final HoaDonDAO hdDAO = new HoaDonDAO();

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
        int soDienCu;
        int soDienMoi;
        int soNuocCu;
        int soNuocMoi;
        double donGiaDien;
        double donGiaNuoc;
        double tienPhong;
        boolean daThanhToan;
        List<ServiceOption> options = new ArrayList<>();

        int tieuThuDien() {
            return Math.max(0, soDienMoi - soDienCu);
        }

        int tieuThuNuoc() {
            return Math.max(0, soNuocMoi - soNuocCu);
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
            return tienPhong + tieuThuDien() * donGiaDien + tieuThuNuoc() * donGiaNuoc + tienDichVuKhac();
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

    public JPanel getPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(MAU_NEN);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);

        loadHistory();
        refreshSummaryCard();
        return root;
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Hóa đơn hàng tháng");
        title.setFont(FONT_TITLE);
        title.setForeground(MAU_TEXT);
        top.add(title, BorderLayout.WEST);

        JButton btnCalc = makeOutlineButton("Tính hóa đơn");
        btnCalc.addActionListener(e -> showCreateDialog());
        top.add(btnCalc, BorderLayout.EAST);

        return top;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        summaryCard = buildSummaryCard();
        JPanel historyCard = buildHistoryCard();

        body.add(summaryCard);
        body.add(Box.createVerticalStrut(16));
        body.add(historyCard);
        return body;
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

        String[] cols = { "Phòng", "Tiền phòng", "Điện", "Nước", "Dịch vụ", "Tổng", "Đã TT", "Chi tiết" };
        summaryModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6 || column == 7;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 6) {
                    return Boolean.class;
                }
                return Object.class;
            }
        };
        summaryModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (row >= 0 && row < currentDrafts.size() && col == 6) {
                Object val = summaryModel.getValueAt(row, 6);
                currentDrafts.get(row).daThanhToan = Boolean.TRUE.equals(val);
            }
        });

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

        tblSummary.getColumnModel().getColumn(7).setCellRenderer(new EyeCellRenderer());
        tblSummary.getColumnModel().getColumn(7).setCellEditor(new SummaryDetailEditor());

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

        String[] cols = { "Tháng/Năm", "Số phòng", "Tổng doanh thu", "Ngày tạo", "Xem" };
        historyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
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

        tblHistory.getColumnModel().getColumn(4).setCellRenderer(new EyeCellRenderer());
        tblHistory.getColumnModel().getColumn(4).setCellEditor(new HistoryViewEditor());

        JScrollPane sp = new JScrollPane(tblHistory);
        sp.setBorder(new EmptyBorder(0, 0, 8, 0));
        sp.getViewport().setBackground(MAU_CARD);

        card.add(title, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
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
            calculateDraft(month, year);
            refreshSummaryCard();
            dlg.dispose();
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

        for (Phong room : rooms) {
            MonthlyRoomDraft d = new MonthlyRoomDraft();
            d.maPhong = room.getMaPhong();
            d.donGiaDien = donGiaDien;
            d.donGiaNuoc = donGiaNuoc;

            int[] old = dienNuocDAO.layChiSoThangTruoc(room.getMaPhong(), m, y);
            int[] cur = dienNuocDAO.layChiSoTheoThang(room.getMaPhong(), m, y);
            d.soDienCu = old[0];
            d.soNuocCu = old[1];
            d.soDienMoi = cur == null ? old[0] : cur[0];
            d.soNuocMoi = cur == null ? old[1] : cur[1];

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

            currentDrafts.add(d);
        }
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
            summaryModel.addRow(new Object[] {
                    d.maPhong,
                    formatMoney(d.tienPhong),
                    formatMoney(d.tieuThuDien() * d.donGiaDien) + " (" + d.tieuThuDien() + " kWh)",
                    formatMoney(d.tieuThuNuoc() * d.donGiaNuoc) + " (" + d.tieuThuNuoc() + " m³)",
                    formatMoney(d.tienDichVuKhac()),
                    formatMoney(d.tongTien()),
                    d.daThanhToan,
                    ""
            });
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
                    item.ngayTao == null ? "" : DF.format(item.ngayTao),
                    ""
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

    private void exportCurrentMonth() {
        if (currentDrafts.isEmpty()) {
            JOptionPane.showMessageDialog(summaryCard, "Chưa có dữ liệu để xuất.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<String> phongChuaThanhToan = new ArrayList<>();
        for (MonthlyRoomDraft d : currentDrafts) {
            if (!d.daThanhToan) {
                phongChuaThanhToan.add(d.maPhong);
            }
        }
        if (!phongChuaThanhToan.isEmpty()) {
            JOptionPane.showMessageDialog(
                    summaryCard,
                    "Không thể xuất hóa đơn vì còn phòng chưa tick 'Đã TT':\n"
                            + String.join(", ", phongChuaThanhToan)
                            + "\n\nVui lòng tick 'Đã TT' cho tất cả phòng trước khi xuất.",
                    "Thiếu xác nhận thanh toán",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int thang = Integer.parseInt(currentMonth);
        int nam = Integer.parseInt(currentYear);
        List<String> loiChiSo = new ArrayList<>();
        for (MonthlyRoomDraft d : currentDrafts) {
            ChiSoDienNuoc chiSo = new ChiSoDienNuoc(d.maPhong, thang, nam, d.soDienMoi, d.soNuocMoi);
            String err = dienNuocDAO.luuHoacCapNhat(chiSo);
            if (err != null) {
                loiChiSo.add(d.maPhong + ": " + err);
            }
        }
        if (!loiChiSo.isEmpty()) {
            JOptionPane.showMessageDialog(
                    summaryCard,
                    "Không thể xuất hóa đơn vì lưu chỉ số điện/nước thất bại:\n"
                            + String.join("\n", loiChiSo),
                    "Lỗi lưu chỉ số",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        ArrayList<Bill> bills = new ArrayList<>();
        for (MonthlyRoomDraft d : currentDrafts) {
            Bill b = new Bill();
            b.phong = d.maPhong;
            b.month = currentMonth;
            b.year = currentYear;
            b.daThanhToan = d.daThanhToan;
            b.tongTieuThuD = d.tieuThuDien();
            b.tongTieuThuN = d.tieuThuNuoc();
            b.donGiaDien = d.donGiaDien;
            b.donGiaNuoc = d.donGiaNuoc;
            b.tienDien = b.tongTieuThuD * b.donGiaDien;
            b.tienNuoc = b.tongTieuThuN * b.donGiaNuoc;
            b.tienPhong = d.tienPhong;

            DichVu dvDien = dvDAO.getDichVuByTen("Điện");
            DichVu dvNuoc = dvDAO.getDichVuByTen("Nước");
            b.maDichVuDien = dvDien == null ? null : dvDien.getMaDichVu();
            b.maDichVuNuoc = dvNuoc == null ? null : dvNuoc.getMaDichVu();

            for (ServiceOption op : d.options) {
                if (!op.selected) {
                    continue;
                }
                BillServiceItem si = new BillServiceItem();
                si.maDichVu = op.maDichVu;
                si.tenKhoan = op.tenDichVu;
                si.soLuong = 1;
                si.donGia = op.donGia;
                b.dichVuKhac.add(si);
            }

            bills.add(b);
        }

        if (hdDAO.luuNhieuHoaDonMoi(bills, "AD01")) {
            JOptionPane.showMessageDialog(summaryCard, "Xuất hóa đơn thành công!");
            currentDrafts.clear();
            refreshSummaryCard();
            loadHistory();
        } else {
            JOptionPane.showMessageDialog(summaryCard, "Không thể xuất hóa đơn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRoomDetailDialog(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= currentDrafts.size()) {
            return;
        }

        MonthlyRoomDraft d = currentDrafts.get(rowIndex);
        Window owner = SwingUtilities.getWindowAncestor(summaryCard);
        JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null,
                "Chi tiết phòng " + d.maPhong, true);
        dlg.setSize(430, 430);
        dlg.setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(MAU_CARD);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Chi tiết phòng " + d.maPhong);
        title.setFont(FONT_BOLD);
        title.setForeground(MAU_TEXT);
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.setOpaque(false);
        JSpinner spDienCu = new JSpinner(new SpinnerNumberModel(d.soDienCu, 0, Integer.MAX_VALUE, 1));
        JSpinner spDienMoi = new JSpinner(new SpinnerNumberModel(d.soDienMoi, 0, Integer.MAX_VALUE, 1));
        JSpinner spNuocCu = new JSpinner(new SpinnerNumberModel(d.soNuocCu, 0, Integer.MAX_VALUE, 1));
        JSpinner spNuocMoi = new JSpinner(new SpinnerNumberModel(d.soNuocMoi, 0, Integer.MAX_VALUE, 1));

        form.add(wrapField("Số điện cũ", spDienCu));
        form.add(wrapField("Số điện mới", spDienMoi));
        form.add(wrapField("Số nước cũ", spNuocCu));
        form.add(wrapField("Số nước mới", spNuocMoi));

        JPanel servicePanel = new JPanel();
        servicePanel.setOpaque(false);
        servicePanel.setLayout(new BoxLayout(servicePanel, BoxLayout.Y_AXIS));
        servicePanel.add(new JLabel("Dịch vụ sử dụng"));
        servicePanel.add(Box.createVerticalStrut(6));

        List<JCheckBox> cbs = new ArrayList<>();
        for (ServiceOption op : d.options) {
            JCheckBox cb = new JCheckBox(op.tenDichVu + " - " + formatMoney(op.donGia) + "/tháng");
            cb.setOpaque(false);
            cb.setSelected(op.selected);
            cb.putClientProperty("service", op);
            cbs.add(cb);
            servicePanel.add(cb);
        }

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(servicePanel, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JButton btnUpdate = primaryButton.makePrimaryButton("Cập nhật & Tính lại");
        btnUpdate.addActionListener(e -> {
            int dienCu = (Integer) spDienCu.getValue();
            int dienMoi = (Integer) spDienMoi.getValue();
            int nuocCu = (Integer) spNuocCu.getValue();
            int nuocMoi = (Integer) spNuocMoi.getValue();

            if (dienMoi < dienCu || nuocMoi < nuocCu) {
                JOptionPane.showMessageDialog(dlg, "Số mới phải lớn hơn hoặc bằng số cũ.", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            d.soDienCu = dienCu;
            d.soDienMoi = dienMoi;
            d.soNuocCu = nuocCu;
            d.soNuocMoi = nuocMoi;
            for (JCheckBox cb : cbs) {
                ServiceOption op = (ServiceOption) cb.getClientProperty("service");
                if (op != null) {
                    op.selected = cb.isSelected();
                }
            }

            refreshSummaryCard();
            dlg.dispose();
        });
        root.add(btnUpdate, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private void showHistoryMonthDialog(String monthYear) {
        String[] parts = monthYear.split("/");
        if (parts.length != 2) {
            return;
        }
        int m = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);

        List<MonthDetailRow> rows = hdDAO.getChiTietHoaDonTheoThang(m, y);
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(summaryCard, "Không có dữ liệu chi tiết.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(summaryCard);
        JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null,
                "Hóa đơn tháng " + monthYear, true);
        dlg.setSize(560, 330);
        dlg.setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(MAU_CARD);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Hóa đơn tháng " + monthYear);
        title.setFont(FONT_BOLD);
        title.setForeground(MAU_TEXT);
        root.add(title, BorderLayout.NORTH);

        String[] cols = { "Phòng", "Tiền phòng", "Điện", "Nước", "Dịch vụ", "Tổng" };
        DefaultTableModel mDetail = new DefaultTableModel(cols, 0);
        double grand = 0;
        for (MonthDetailRow r : rows) {
            grand += r.tong;
            mDetail.addRow(new Object[] {
                    r.maPhong,
                    formatMoney(r.tienPhong),
                    formatMoney(r.tienDien),
                    formatMoney(r.tienNuoc),
                    formatMoney(r.tienDichVu),
                    formatMoney(r.tong)
            });
        }

        JTable table = new JTable(mDetail);
        table.setRowHeight(30);
        table.setFont(FONT_PLAIN);
        table.setEnabled(false);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel total = new JLabel("Tổng cộng: " + formatMoney(grand));
        total.setFont(FONT_BOLD);
        total.setHorizontalAlignment(SwingConstants.RIGHT);
        root.add(total, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
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
                    showHistoryMonthDialog(historyItems.get(row).monthYear);
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