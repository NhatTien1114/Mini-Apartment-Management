package ui.main;

import dao.ChiSoDienNuocDAO;
import dao.HoaDonDAO;
import dao.HopDongKhachHangDAO;
import dao.QuanLyPhongDAO;
import entity.ChiSoDienNuoc;
import entity.KhachHang;
import entity.Phong;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import ui.util.AppColors;
import ui.util.PrimaryButton;

public class ChiSoDienNuocUI {
    private final Font FONT_TITLE = new Font("Be Vietnam Pro", Font.BOLD, 22);
    private final Font FONT_BOLD = new Font("Be Vietnam Pro", Font.BOLD, 14);
    private final Font FONT_PLAIN = new Font("Be Vietnam Pro", Font.PLAIN, 14);
    private final Font FONT_SMALL = new Font("Be Vietnam Pro", Font.PLAIN, 13);

    private final ChiSoDienNuocDAO chiSoDAO = new ChiSoDienNuocDAO();
    private final QuanLyPhongDAO phongDAO = new QuanLyPhongDAO();
    private final HopDongKhachHangDAO hopDongKhachHangDAO = new HopDongKhachHangDAO();
    private final PrimaryButton primaryButton = new PrimaryButton();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboMonth;
    private JComboBox<String> cboYear;
    private JLabel lblStatus;
    private JPanel root;

    private final List<RoomMeterRow> currentRows = new ArrayList<>();

    private static class RoomMeterRow {
        String maPhong;
        String maHopDong;
        String tenKhach;
        int ngay;
        int dienCu;
        int dienMoi;
        int nuocCu;
        int nuocMoi;
        boolean saved;

        RoomMeterRow(String maPhong, String maHopDong, String tenKhach, int ngay,
                int dienCu, int dienMoi, int nuocCu, int nuocMoi, boolean saved) {
            this.maPhong = maPhong;
            this.maHopDong = maHopDong;
            this.tenKhach = tenKhach;
            this.ngay = ngay;
            this.dienCu = dienCu;
            this.dienMoi = dienMoi;
            this.nuocCu = nuocCu;
            this.nuocMoi = nuocMoi;
            this.saved = saved;
        }
    }

    public JPanel getPanel() {
        root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(AppColors.SLATE_100);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        root.add(createTopBar(), BorderLayout.NORTH);
        root.add(createMainCard(), BorderLayout.CENTER);

        loadData();
        return root;
    }

    public void refresh() {
        if (table != null && table.isEditing()) {
            table.getCellEditor().cancelCellEditing();
        }
        if (cboMonth != null && cboYear != null) {
            Calendar cal = Calendar.getInstance();
            int currentMonth = cal.get(Calendar.MONTH) + 1;
            int currentYear = cal.get(Calendar.YEAR);
            java.awt.event.ActionListener[] monthListeners = cboMonth.getActionListeners();
            java.awt.event.ActionListener[] yearListeners = cboYear.getActionListeners();
            for (java.awt.event.ActionListener l : monthListeners)
                cboMonth.removeActionListener(l);
            for (java.awt.event.ActionListener l : yearListeners)
                cboYear.removeActionListener(l);

            cboMonth.setSelectedItem(String.valueOf(currentMonth));
            cboYear.setSelectedItem(String.valueOf(currentYear));

            for (java.awt.event.ActionListener l : monthListeners)
                cboMonth.addActionListener(l);
            for (java.awt.event.ActionListener l : yearListeners)
                cboYear.addActionListener(l);
        }
        loadData();
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblTitle = new JLabel("Chỉ số điện nước");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(AppColors.SLATE_900);
        top.add(lblTitle, BorderLayout.WEST);

        return top;
    }

    private JPanel createMainCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(AppColors.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_200, 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(AppColors.WHITE);

        JPanel leftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBar.setBackground(AppColors.WHITE);

        JLabel lblMonth = new JLabel("Tháng:");
        lblMonth.setFont(FONT_PLAIN);
        lblMonth.setForeground(AppColors.SLATE_900);

        String[] months = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
        cboMonth = new JComboBox<>(months);
        cboMonth.setFont(FONT_PLAIN);
        cboMonth.setBackground(AppColors.WHITE);
        cboMonth.setPreferredSize(new Dimension(70, 36));
        cboMonth.setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH));

        JLabel lblYear = new JLabel("Năm:");
        lblYear.setFont(FONT_PLAIN);
        lblYear.setForeground(AppColors.SLATE_900);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[5];
        for (int i = 0; i < 5; i++) {
            years[i] = String.valueOf(currentYear - 2 + i);
        }
        cboYear = new JComboBox<>(years);
        cboYear.setFont(FONT_PLAIN);
        cboYear.setBackground(AppColors.WHITE);
        cboYear.setPreferredSize(new Dimension(90, 36));
        cboYear.setSelectedItem(String.valueOf(currentYear));

        Runnable onMonthYearChange = this::loadData;
        cboMonth.addActionListener(e -> onMonthYearChange.run());
        cboYear.addActionListener(e -> onMonthYearChange.run());

        leftBar.add(lblMonth);
        leftBar.add(cboMonth);
        leftBar.add(Box.createHorizontalStrut(8));
        leftBar.add(lblYear);
        leftBar.add(cboYear);

        lblStatus = new JLabel("");
        lblStatus.setFont(FONT_SMALL);
        lblStatus.setForeground(AppColors.GREEN_600);
        leftBar.add(Box.createHorizontalStrut(16));
        leftBar.add(lblStatus);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBar.setBackground(AppColors.WHITE);

        JButton btnSave = primaryButton.makePrimaryButton("Lưu tất cả");
        btnSave.addActionListener(e -> saveAll());
        rightBar.add(btnSave);

        toolbar.add(leftBar, BorderLayout.WEST);
        toolbar.add(rightBar, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);

        // Columns: 0=Phòng 1=Khách thuê 2=Ngày 3=Số điện cũ 4=Số điện mới
        // 5=Tiêu thụ(kWh) 6=Số nước cũ 7=Số nước mới 8=Tiêu thụ(m³) 9=Trạng thái
        String[] cols = { "Phòng", "Khách thuê", "Ngày", "Số điện cũ", "Số điện mới",
                "Tiêu thụ (kWh)", "Số nước cũ", "Số nước mới", "Tiêu thụ (m³)", "Trạng thái" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column != 2 && column != 4 && column != 7)
                    return false;
                if (row >= 0 && row < currentRows.size() && currentRows.get(row).saved)
                    return false;
                return true;
            }
        };

        table = new JTable(tableModel) {
            private int hoverRow = -1;

            {
                addMouseMotionListener(new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        int row = rowAtPoint(e.getPoint());
                        if (hoverRow != row) {
                            hoverRow = row;
                            repaint();
                        }
                    }
                });
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hoverRow = -1;
                        repaint();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (e.isPopupTrigger())
                            showTableContextMenu(e);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.isPopupTrigger())
                            showTableContextMenu(e);
                    }

                    private void showTableContextMenu(MouseEvent e) {
                        int row = rowAtPoint(e.getPoint());
                        if (row >= 0) {
                            setRowSelectionInterval(row, row);
                        }
                        JPopupMenu menu = new JPopupMenu();
                        JMenuItem itemAdd = new JMenuItem("Thêm chỉ số mới cho phòng này");
                        itemAdd.setFont(FONT_PLAIN);
                        itemAdd.addActionListener(ev -> addNewReadingForSelectedRoom());
                        menu.add(itemAdd);
                        menu.show(e.getComponent(), e.getX(), e.getY());
                    }
                });
            }

            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(hoverRow == row ? AppColors.SLATE_50 : AppColors.WHITE);
                }
                return c;
            }
        };

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 44));
        header.setBorder(new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setFont(FONT_SMALL);
                setForeground(AppColors.SLATE_600);
                setBackground(new Color(248, 250, 252));
                setBorder(new EmptyBorder(0, 10, 0, 8));
                setHorizontalAlignment(column >= 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return this;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        table.setRowHeight(50);
        table.setFont(FONT_PLAIN);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(AppColors.PRIMARY_TINT_HOVER);
        table.setSelectionForeground(AppColors.SLATE_900);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
                    int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200),
                        new EmptyBorder(0, 16, 0, 8)));
                setForeground(AppColors.SLATE_900);
                setFont(FONT_PLAIN);
                setHorizontalAlignment(col >= 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
                if (col == 0) {
                    setForeground(AppColors.PRIMARY);
                    setFont(FONT_BOLD);
                } else if (col == 1) {
                    setFont(FONT_BOLD);
                }
                return this;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        DefaultTableCellRenderer editableCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
                    int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(FONT_BOLD);
                boolean isSaved = row >= 0 && row < currentRows.size() && currentRows.get(row).saved;
                if (isSaved) {
                    setForeground(AppColors.SLATE_500);
                    if (!isSelected)
                        setBackground(AppColors.SLATE_50);
                    setBorder(BorderFactory.createCompoundBorder(
                            new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200),
                            new EmptyBorder(0, 10, 0, 8)));
                } else {
                    setForeground(AppColors.PRIMARY);
                    if (!isSelected)
                        setBackground(new Color(239, 246, 255));
                    setBorder(BorderFactory.createCompoundBorder(
                            new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200),
                            BorderFactory.createCompoundBorder(
                                    new EmptyBorder(4, 4, 4, 4),
                                    new LineBorder(AppColors.PRIMARY_TINT, 1, true))));
                }
                return this;
            }
        };
        table.getColumnModel().getColumn(2).setCellRenderer(editableCellRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(editableCellRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(editableCellRenderer);

        DefaultTableCellRenderer consumptionRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
                    int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(FONT_BOLD);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200),
                        new EmptyBorder(0, 10, 0, 8)));
                String text = value != null ? value.toString() : "0";
                try {
                    int val = Integer.parseInt(text);
                    setForeground(val > 0 ? AppColors.AMBER_FG : AppColors.SLATE_400);
                } catch (NumberFormatException e) {
                    setForeground(AppColors.SLATE_900);
                }
                return this;
            }
        };
        table.getColumnModel().getColumn(5).setCellRenderer(consumptionRenderer);
        table.getColumnModel().getColumn(8).setCellRenderer(consumptionRenderer);

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
                    int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Be Vietnam Pro", Font.BOLD, 12));
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200),
                        new EmptyBorder(0, 10, 0, 8)));
                String text = value != null ? value.toString() : "";
                if ("Đã nhập".equals(text)) {
                    setForeground(AppColors.GREEN_600);
                } else {
                    setForeground(AppColors.AMBER_FG);
                }
                return this;
            }
        };
        table.getColumnModel().getColumn(9).setCellRenderer(statusRenderer);

        int[] widths = { 80, 160, 60, 100, 110, 110, 100, 110, 110, 100 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        tableModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (row < 0 || row >= currentRows.size())
                return;
            if (col == 4 || col == 7) {
                SwingUtilities.invokeLater(() -> updateConsumption(row));
            } else if (col == 2) {
                SwingUtilities.invokeLater(() -> updateOldReading(row));
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(AppColors.WHITE);

        JPanel summaryBar = createSummaryBar();

        JPanel bodyPanel = new JPanel(new BorderLayout(0, 0));
        bodyPanel.setBackground(AppColors.WHITE);
        bodyPanel.add(sp, BorderLayout.CENTER);
        bodyPanel.add(summaryBar, BorderLayout.SOUTH);

        card.add(bodyPanel, BorderLayout.CENTER);
        return card;
    }

    private JLabel lblTotalRooms;
    private JLabel lblEnteredCount;
    private JLabel lblPendingCount;

    private JPanel createSummaryBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 10));
        bar.setBackground(new Color(248, 250, 252));
        bar.setBorder(new MatteBorder(1, 0, 0, 0, AppColors.SLATE_200));

        lblTotalRooms = makeSummaryLabel("Tổng phòng: 0");
        lblEnteredCount = makeSummaryLabel("Đã nhập: 0");
        lblPendingCount = makeSummaryLabel("Chưa nhập: 0");

        bar.add(lblTotalRooms);
        bar.add(lblEnteredCount);
        bar.add(lblPendingCount);
        return bar;
    }

    private JLabel makeSummaryLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(AppColors.SLATE_600);
        return lbl;
    }

    private void updateConsumption(int row) {
        if (row < 0 || row >= currentRows.size())
            return;
        RoomMeterRow data = currentRows.get(row);
        try {
            Object valDien = tableModel.getValueAt(row, 4);
            int dienMoi = valDien != null ? Integer.parseInt(valDien.toString().trim()) : data.dienCu;
            data.dienMoi = dienMoi;
            int tieuThuDien = Math.max(0, dienMoi - data.dienCu);
            tableModel.setValueAt(String.valueOf(tieuThuDien), row, 5);
        } catch (NumberFormatException ignored) {
        }
        try {
            Object valNuoc = tableModel.getValueAt(row, 7);
            int nuocMoi = valNuoc != null ? Integer.parseInt(valNuoc.toString().trim()) : data.nuocCu;
            data.nuocMoi = nuocMoi;
            int tieuThuNuoc = Math.max(0, nuocMoi - data.nuocCu);
            tableModel.setValueAt(String.valueOf(tieuThuNuoc), row, 8);
        } catch (NumberFormatException ignored) {
        }
    }

    private void updateOldReading(int row) {
        if (row < 0 || row >= currentRows.size())
            return;
        RoomMeterRow data = currentRows.get(row);
        if (data.saved)
            return;

        Object valNgay = tableModel.getValueAt(row, 2);
        int ngay;
        try {
            ngay = Integer.parseInt(valNgay.toString().trim());
            if (ngay < 1 || ngay > 31)
                return;
        } catch (NumberFormatException e) {
            return;
        }

        int thang = Integer.parseInt((String) cboMonth.getSelectedItem());
        int nam = Integer.parseInt((String) cboYear.getSelectedItem());

        data.ngay = ngay;
        int[] chiSoCu = chiSoDAO.layChiSoTruocNgay(data.maHopDong, LocalDate.of(nam, thang, ngay));
        data.dienCu = chiSoCu[0];
        data.nuocCu = chiSoCu[1];

        tableModel.setValueAt(String.valueOf(data.dienCu), row, 3);
        tableModel.setValueAt(String.valueOf(data.nuocCu), row, 6);
        updateConsumption(row);
    }

    private void autoImportPrevMonthIfMissing(int thang, int nam) {
        int thangTruoc = (thang == 1) ? 12 : thang - 1;
        int namTruoc = (thang == 1) ? nam - 1 : nam;

        ArrayList<ChiSoDienNuoc> existing = chiSoDAO.getAllChiSoThang(thangTruoc, namTruoc);
        java.util.Set<String> savedContracts = new java.util.HashSet<>();
        for (ChiSoDienNuoc cs : existing)
            savedContracts.add(cs.getMaHopDong());

        java.util.List<HoaDonUI.RoomMonthSummary> summaries = hoaDonDAO.getRoomSummariesTheoThang(thangTruoc, namTruoc);
        for (HoaDonUI.RoomMonthSummary s : summaries) {
            String maHopDong = hopDongKhachHangDAO.getMaHopDongHienTai(s.maPhong);
            if (maHopDong == null || savedContracts.contains(maHopDong))
                continue;
            LocalDate ngayGhi = LocalDate.of(namTruoc, thangTruoc, 1);
            int[] chiSoCu = chiSoDAO.layChiSoTruocNgay(maHopDong, ngayGhi);
            int soDienMoi = chiSoCu[0] + s.tieuThuDien;
            int soNuocMoi = chiSoCu[1] + s.tieuThuNuoc;
            chiSoDAO.luuHoacCapNhat(new ChiSoDienNuoc(maHopDong, ngayGhi, soDienMoi, soNuocMoi));
        }
    }

    private void loadData() {
        if (tableModel == null)
            return;

        int thang = Integer.parseInt((String) cboMonth.getSelectedItem());
        int nam = Integer.parseInt((String) cboYear.getSelectedItem());

        autoImportPrevMonthIfMissing(thang, nam);

        currentRows.clear();
        tableModel.setRowCount(0);

        // Pre-fetch tất cả ChiSo của tháng, gom theo maHopDong
        ArrayList<ChiSoDienNuoc> savedList = chiSoDAO.getAllChiSoThang(thang, nam);
        java.util.Map<String, java.util.List<ChiSoDienNuoc>> savedByHopDong = new java.util.LinkedHashMap<>();
        for (ChiSoDienNuoc cs : savedList) {
            savedByHopDong.computeIfAbsent(cs.getMaHopDong(), k -> new java.util.ArrayList<>()).add(cs);
        }

        java.util.List<HoaDonUI.RoomMonthSummary> invoiceSummaries = hoaDonDAO.getRoomSummariesTheoThang(thang, nam);
        java.util.Map<String, HoaDonUI.RoomMonthSummary> invoiceByRoom = new java.util.HashMap<>();
        for (HoaDonUI.RoomMonthSummary s : invoiceSummaries) {
            invoiceByRoom.put(s.maPhong, s);
        }

        ArrayList<Phong> dsPhong = phongDAO.getAllPhongDaThue();
        if (dsPhong == null)
            dsPhong = new ArrayList<>();

        int daNhap = 0;
        int chuaNhap = 0;

        Calendar cal = Calendar.getInstance();
        int todayDay = (thang == cal.get(Calendar.MONTH) + 1 && nam == cal.get(Calendar.YEAR))
                ? cal.get(Calendar.DAY_OF_MONTH)
                : 1;

        for (Phong phong : dsPhong) {
            String maPhong = phong.getMaPhong();
            String maHopDong = hopDongKhachHangDAO.getMaHopDongHienTai(maPhong);
            if (maHopDong == null)
                continue;

            String tenKhachHienTai = "";
            KhachHang khHT = hopDongKhachHangDAO.getNguoiDaiDienByMaPhong(maPhong);
            if (khHT != null)
                tenKhachHienTai = khHT.getHoTen();

            java.util.List<ChiSoDienNuoc> savedForRoom = savedByHopDong.getOrDefault(maHopDong,
                    java.util.Collections.emptyList());

            if (!savedForRoom.isEmpty()) {
                for (ChiSoDienNuoc cs : savedForRoom) {
                    int[] chiSoCu = chiSoDAO.layChiSoTruocNgay(maHopDong, cs.getNgayGhi());
                    int dienCu = chiSoCu[0];
                    int nuocCu = chiSoCu[1];
                    int dienMoi = cs.getSoDien();
                    int nuocMoi = cs.getSoNuoc();
                    int tieuThuDien = Math.max(0, dienMoi - dienCu);
                    int tieuThuNuoc = Math.max(0, nuocMoi - nuocCu);
                    int ngay = cs.getNgayGhi().getDayOfMonth();

                    KhachHang khTaiNgay = hopDongKhachHangDAO.getNguoiDaiDienByMaPhongTaiNgay(maPhong, cs.getNgayGhi());
                    String tenKhach = (khTaiNgay != null) ? khTaiNgay.getHoTen() : tenKhachHienTai;

                    currentRows.add(new RoomMeterRow(maPhong, maHopDong, tenKhach, ngay,
                            dienCu, dienMoi, nuocCu, nuocMoi, true));
                    tableModel.addRow(new Object[] {
                            maPhong, tenKhach,
                            String.valueOf(ngay),
                            String.valueOf(dienCu),
                            String.valueOf(dienMoi),
                            String.valueOf(tieuThuDien),
                            String.valueOf(nuocCu),
                            String.valueOf(nuocMoi),
                            String.valueOf(tieuThuNuoc),
                            "Đã nhập"
                    });
                    daNhap++;
                }
            } else {
                HoaDonUI.RoomMonthSummary invoice = invoiceByRoom.get(maPhong);
                if (invoice != null) {
                    LocalDate ngayGhi = LocalDate.of(nam, thang, 1);
                    int[] chiSoCu = chiSoDAO.layChiSoTruocNgay(maHopDong, ngayGhi);
                    int dienCu = chiSoCu[0];
                    int nuocCu = chiSoCu[1];
                    int dienMoi = dienCu + invoice.tieuThuDien;
                    int nuocMoi = nuocCu + invoice.tieuThuNuoc;
                    int tieuThuDien = invoice.tieuThuDien;
                    int tieuThuNuoc = invoice.tieuThuNuoc;

                    chiSoDAO.luuHoacCapNhat(new ChiSoDienNuoc(maHopDong, ngayGhi, dienMoi, nuocMoi));

                    KhachHang khTaiNgay = hopDongKhachHangDAO.getNguoiDaiDienByMaPhongTaiNgay(maPhong, ngayGhi);
                    String tenKhach = (khTaiNgay != null) ? khTaiNgay.getHoTen() : tenKhachHienTai;

                    currentRows.add(new RoomMeterRow(maPhong, maHopDong, tenKhach, 1,
                            dienCu, dienMoi, nuocCu, nuocMoi, true));
                    tableModel.addRow(new Object[] {
                            maPhong, tenKhach,
                            "1",
                            String.valueOf(dienCu),
                            String.valueOf(dienMoi),
                            String.valueOf(tieuThuDien),
                            String.valueOf(nuocCu),
                            String.valueOf(nuocMoi),
                            String.valueOf(tieuThuNuoc),
                            "Đã nhập"
                    });
                    daNhap++;
                } else {
                    int[] chiSoCu = chiSoDAO.layChiSoTruocNgay(maHopDong, LocalDate.of(nam, thang, todayDay));
                    int dienCu = chiSoCu[0];
                    int nuocCu = chiSoCu[1];

                    currentRows.add(new RoomMeterRow(maPhong, maHopDong, tenKhachHienTai, todayDay,
                            dienCu, dienCu, nuocCu, nuocCu, false));
                    tableModel.addRow(new Object[] {
                            maPhong, tenKhachHienTai,
                            String.valueOf(todayDay),
                            String.valueOf(dienCu),
                            String.valueOf(dienCu),
                            "0",
                            String.valueOf(nuocCu),
                            String.valueOf(nuocCu),
                            "0",
                            "Chưa nhập"
                    });
                    chuaNhap++;
                }
            }
        }

        int total = dsPhong.size();
        lblTotalRooms.setText("Tổng phòng: " + total);
        lblEnteredCount.setText("Đã nhập: " + daNhap);
        lblPendingCount.setText("Chưa nhập: " + chuaNhap);
        lblStatus.setText("");
    }

    private void addNewReadingForSelectedRoom() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentRows.size()) {
            JOptionPane.showMessageDialog(root,
                    "Vui lòng chọn một dòng của phòng cần thêm chỉ số.",
                    "Chưa chọn phòng", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        RoomMeterRow selected = currentRows.get(selectedRow);
        String maPhong = selected.maPhong;
        String maHopDong = selected.maHopDong;

        KhachHang khHienTai = hopDongKhachHangDAO.getNguoiDaiDienByMaPhong(maPhong);
        String tenKhach = (khHienTai != null) ? khHienTai.getHoTen() : selected.tenKhach;

        int thang = Integer.parseInt((String) cboMonth.getSelectedItem());
        int nam = Integer.parseInt((String) cboYear.getSelectedItem());

        Calendar cal = Calendar.getInstance();
        int defaultNgay = (thang == cal.get(Calendar.MONTH) + 1 && nam == cal.get(Calendar.YEAR))
                ? cal.get(Calendar.DAY_OF_MONTH)
                : 1;

        int[] chiSoCu = chiSoDAO.layChiSoTruocNgay(maHopDong, LocalDate.of(nam, thang, defaultNgay));
        int dienCu = chiSoCu[0];
        int nuocCu = chiSoCu[1];

        RoomMeterRow newRow = new RoomMeterRow(maPhong, maHopDong, tenKhach, defaultNgay,
                dienCu, dienCu, nuocCu, nuocCu, false);
        currentRows.add(newRow);

        tableModel.addRow(new Object[] {
                maPhong, tenKhach,
                String.valueOf(defaultNgay),
                String.valueOf(dienCu),
                String.valueOf(dienCu),
                "0",
                String.valueOf(nuocCu),
                String.valueOf(nuocCu),
                "0",
                "Chưa nhập"
        });

        int pending = (int) currentRows.stream().filter(r -> !r.saved).count();
        int entered = (int) currentRows.stream().filter(r -> r.saved).count();
        lblPendingCount.setText("Chưa nhập: " + pending);
        lblEnteredCount.setText("Đã nhập: " + entered);

        int newRowIdx = currentRows.size() - 1;
        table.scrollRectToVisible(table.getCellRect(newRowIdx, 0, true));
        table.changeSelection(newRowIdx, 2, false, false);
        lblStatus.setText("Đã thêm dòng mới cho phòng " + maPhong + " — nhập ngày và chỉ số mới.");
        lblStatus.setForeground(AppColors.PRIMARY);
    }

    private void saveAll() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        int thang = Integer.parseInt((String) cboMonth.getSelectedItem());
        int nam = Integer.parseInt((String) cboYear.getSelectedItem());

        List<String> errors = new ArrayList<>();
        int savedCount = 0;

        for (int i = 0; i < currentRows.size(); i++) {
            RoomMeterRow data = currentRows.get(i);

            if (data.saved)
                continue;

            int ngay;
            try {
                Object valNgay = tableModel.getValueAt(i, 2);
                ngay = Integer.parseInt(valNgay.toString().trim());
                if (ngay < 1 || ngay > 31)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                table.scrollRectToVisible(table.getCellRect(i, 2, true));
                table.changeSelection(i, 2, false, false);
                table.editCellAt(i, 2);
                JOptionPane.showMessageDialog(root,
                        "Phòng " + data.maPhong + ": Ngày không hợp lệ (1–31).",
                        "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int dienMoi;
            try {
                Object valDien = tableModel.getValueAt(i, 4);
                dienMoi = Integer.parseInt(valDien.toString().trim());
            } catch (NumberFormatException e) {
                errors.add(data.maPhong + ": Số điện mới không hợp lệ");
                table.scrollRectToVisible(table.getCellRect(i, 4, true));
                table.changeSelection(i, 4, false, false);
                table.editCellAt(i, 4);
                JOptionPane.showMessageDialog(root,
                        "Phòng " + data.maPhong + ": Số điện mới không hợp lệ, vui lòng nhập số nguyên.",
                        "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int nuocMoi;
            try {
                Object valNuoc = tableModel.getValueAt(i, 7);
                nuocMoi = Integer.parseInt(valNuoc.toString().trim());
            } catch (NumberFormatException e) {
                errors.add(data.maPhong + ": Số nước mới không hợp lệ");
                table.scrollRectToVisible(table.getCellRect(i, 7, true));
                table.changeSelection(i, 7, false, false);
                table.editCellAt(i, 7);
                JOptionPane.showMessageDialog(root,
                        "Phòng " + data.maPhong + ": Số nước mới không hợp lệ, vui lòng nhập số nguyên.",
                        "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (dienMoi < data.dienCu) {
                table.scrollRectToVisible(table.getCellRect(i, 4, true));
                table.changeSelection(i, 4, false, false);
                table.editCellAt(i, 4);
                JOptionPane.showMessageDialog(root,
                        "Phòng " + data.maPhong + ": Số điện mới (" + dienMoi + ") không được nhỏ hơn số cũ ("
                                + data.dienCu + ").",
                        "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (nuocMoi < data.nuocCu) {
                table.scrollRectToVisible(table.getCellRect(i, 7, true));
                table.changeSelection(i, 7, false, false);
                table.editCellAt(i, 7);
                JOptionPane.showMessageDialog(root,
                        "Phòng " + data.maPhong + ": Số nước mới (" + nuocMoi + ") không được nhỏ hơn số cũ ("
                                + data.nuocCu + ").",
                        "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (dienMoi <= data.dienCu) {
                table.scrollRectToVisible(table.getCellRect(i, 4, true));
                table.changeSelection(i, 4, false, false);
                table.editCellAt(i, 4);
                JOptionPane.showMessageDialog(root,
                        "Phòng " + data.maPhong + ": Tiêu thụ điện phải lớn hơn 0.\nSố điện cũ: " + data.dienCu
                                + " → Vui lòng nhập số điện mới lớn hơn " + data.dienCu + ".",
                        "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (nuocMoi <= data.nuocCu) {
                table.scrollRectToVisible(table.getCellRect(i, 7, true));
                table.changeSelection(i, 7, false, false);
                table.editCellAt(i, 7);
                JOptionPane.showMessageDialog(root,
                        "Phòng " + data.maPhong + ": Tiêu thụ nước phải lớn hơn 0.\nSố nước cũ: " + data.nuocCu
                                + " → Vui lòng nhập số nước mới lớn hơn " + data.nuocCu + ".",
                        "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ChiSoDienNuoc cs = new ChiSoDienNuoc(data.maHopDong, LocalDate.of(nam, thang, ngay), dienMoi, nuocMoi);
            String err = chiSoDAO.luuHoacCapNhat(cs);
            if (err != null) {
                errors.add(data.maPhong + ": " + err);
            } else {
                savedCount++;
            }
        }

        if (errors.isEmpty()) {
            lblStatus.setForeground(AppColors.GREEN_600);
            lblStatus.setText("Đã lưu " + savedCount + " phòng thành công");
        } else {
            lblStatus.setForeground(AppColors.RED_500);
            lblStatus.setText("Lỗi " + errors.size() + " phòng");
            JOptionPane.showMessageDialog(root,
                    "Lưu thành công: " + savedCount + " phòng\nLỗi:\n" + String.join("\n", errors),
                    "Kết quả", JOptionPane.WARNING_MESSAGE);
        }

        loadData();
    }
}
