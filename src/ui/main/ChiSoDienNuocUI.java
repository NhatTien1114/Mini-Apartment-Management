package ui.main;

import dao.ChiSoDienNuocDAO;
import dao.HopDongKhachHangDAO;
import dao.QuanLyPhongDAO;
import entity.ChiSoDienNuoc;
import entity.KhachHang;
import entity.Phong;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboMonth;
    private JComboBox<String> cboYear;
    private JLabel lblStatus;
    private JPanel root;

    // Lưu trữ dữ liệu hiện tại
    private final List<RoomMeterRow> currentRows = new ArrayList<>();

    private static class RoomMeterRow {
        String maPhong;
        String tenKhach;
        int dienCu;
        int dienMoi;
        int nuocCu;
        int nuocMoi;
        boolean saved;

        RoomMeterRow(String maPhong, String tenKhach, int dienCu, int dienMoi, int nuocCu, int nuocMoi, boolean saved) {
            this.maPhong = maPhong;
            this.tenKhach = tenKhach;
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

        // ── Toolbar: chọn tháng/năm + nút lưu ──
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

        // Status label
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

        // ── Table ──
        String[] cols = { "Phòng", "Khách thuê", "Số điện cũ", "Số điện mới", "Tiêu thụ (kWh)",
                "Số nước cũ", "Số nước mới", "Tiêu thụ (m³)", "Trạng thái" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column != 3 && column != 6)
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

        // Header style
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

        // Cell style
        table.setRowHeight(50);
        table.setFont(FONT_PLAIN);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(AppColors.PRIMARY_TINT_HOVER);
        table.setSelectionForeground(AppColors.SLATE_900);

        // Cell renderer mặc định
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

        // Renderer cho cột số điện mới / số nước mới — editable: viền xanh, saved:
        // style khóa
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
        table.getColumnModel().getColumn(3).setCellRenderer(editableCellRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(editableCellRenderer);

        // Renderer cho cột tiêu thụ — hiển thị màu theo mức
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
        table.getColumnModel().getColumn(4).setCellRenderer(consumptionRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(consumptionRenderer);

        // Renderer cho cột trạng thái — badge style
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
        table.getColumnModel().getColumn(8).setCellRenderer(statusRenderer);

        // Column widths
        int[] widths = { 80, 180, 100, 110, 110, 100, 110, 110, 100 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Update tiêu thụ khi chỉnh sửa
        tableModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (row < 0 || row >= currentRows.size())
                return;
            if (col == 3 || col == 6) {
                SwingUtilities.invokeLater(() -> updateConsumption(row));
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(AppColors.WHITE);

        // ── Summary bar ──
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
            Object valDien = tableModel.getValueAt(row, 3);
            int dienMoi = valDien != null ? Integer.parseInt(valDien.toString().trim()) : data.dienCu;
            data.dienMoi = dienMoi;
            int tieuThuDien = Math.max(0, dienMoi - data.dienCu);
            tableModel.setValueAt(String.valueOf(tieuThuDien), row, 4);
        } catch (NumberFormatException ignored) {
        }
        try {
            Object valNuoc = tableModel.getValueAt(row, 6);
            int nuocMoi = valNuoc != null ? Integer.parseInt(valNuoc.toString().trim()) : data.nuocCu;
            data.nuocMoi = nuocMoi;
            int tieuThuNuoc = Math.max(0, nuocMoi - data.nuocCu);
            tableModel.setValueAt(String.valueOf(tieuThuNuoc), row, 7);
        } catch (NumberFormatException ignored) {
        }
    }

    private void loadData() {
        if (tableModel == null)
            return;

        int thang = Integer.parseInt((String) cboMonth.getSelectedItem());
        int nam = Integer.parseInt((String) cboYear.getSelectedItem());

        currentRows.clear();
        tableModel.setRowCount(0);

        ArrayList<Phong> dsPhong = phongDAO.getAllPhongDaThue();
        if (dsPhong == null)
            dsPhong = new ArrayList<>();

        int daNhap = 0;
        int chuaNhap = 0;

        for (Phong phong : dsPhong) {
            String maPhong = phong.getMaPhong();

            // Lấy tên khách đại diện
            String tenKhach = "";
            KhachHang kh = hopDongKhachHangDAO.getNguoiDaiDienByMaPhong(maPhong);
            if (kh != null) {
                tenKhach = kh.getHoTen();
            }

            // Chỉ số cũ (tháng trước)
            int[] chiSoCu = chiSoDAO.layChiSoThangTruoc(maPhong, thang, nam);

            // Chỉ số hiện tại (tháng đang chọn)
            int[] chiSoHienTai = chiSoDAO.layChiSoTheoThang(maPhong, thang, nam);

            int dienCu = chiSoCu[0];
            int nuocCu = chiSoCu[1];
            int dienMoi = chiSoHienTai != null ? chiSoHienTai[0] : dienCu;
            int nuocMoi = chiSoHienTai != null ? chiSoHienTai[1] : nuocCu;
            boolean saved = chiSoHienTai != null;

            if (saved)
                daNhap++;
            else
                chuaNhap++;

            RoomMeterRow rowData = new RoomMeterRow(maPhong, tenKhach, dienCu, dienMoi, nuocCu, nuocMoi, saved);
            currentRows.add(rowData);

            int tieuThuDien = Math.max(0, dienMoi - dienCu);
            int tieuThuNuoc = Math.max(0, nuocMoi - nuocCu);

            tableModel.addRow(new Object[] {
                    maPhong,
                    tenKhach,
                    String.valueOf(dienCu),
                    String.valueOf(dienMoi),
                    String.valueOf(tieuThuDien),
                    String.valueOf(nuocCu),
                    String.valueOf(nuocMoi),
                    String.valueOf(tieuThuNuoc),
                    saved ? "Đã nhập" : "Chưa nhập"
            });
        }

        // Update summary
        int total = dsPhong.size();
        lblTotalRooms.setText("Tổng phòng: " + total);
        lblEnteredCount.setText("Đã nhập: " + daNhap);
        lblPendingCount.setText("Chưa nhập: " + chuaNhap);
        lblStatus.setText("");
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

            // Bỏ qua phòng đã lưu rồi
            if (data.saved)
                continue;

            // Đọc giá trị mới nhất từ table
            int dienMoi, nuocMoi;
            try {
                Object valDien = tableModel.getValueAt(i, 3);
                dienMoi = Integer.parseInt(valDien.toString().trim());
            } catch (NumberFormatException e) {
                errors.add(data.maPhong + ": Số điện mới không hợp lệ");
                continue;
            }
            try {
                Object valNuoc = tableModel.getValueAt(i, 6);
                nuocMoi = Integer.parseInt(valNuoc.toString().trim());
            } catch (NumberFormatException e) {
                errors.add(data.maPhong + ": Số nước mới không hợp lệ");
                continue;
            }

            if (dienMoi < data.dienCu) {
                errors.add(data.maPhong + ": Số điện mới (" + dienMoi + ") < số cũ (" + data.dienCu + ")");
                continue;
            }
            if (nuocMoi < data.nuocCu) {
                errors.add(data.maPhong + ": Số nước mới (" + nuocMoi + ") < số cũ (" + data.nuocCu + ")");
                continue;
            }

            ChiSoDienNuoc cs = new ChiSoDienNuoc(data.maPhong, thang, nam, dienMoi, nuocMoi);
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

        // Reload data
        loadData();
    }
}
