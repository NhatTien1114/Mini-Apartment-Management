package ui.main;

import dao.HoaDonDAO;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DoanhThuUI {

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color BG = new Color(241, 245, 249);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color ACCENT = new Color(37, 99, 235);
    private static final Color ACCENT_LIGHT = new Color(219, 234, 254);

    private static final Color COLOR_PHONG = new Color(30, 58, 138);
    private static final Color COLOR_PHONG_HOVER = new Color(37, 99, 235);
    private static final Color COLOR_DICHVU = new Color(34, 197, 94);
    private static final Color COLOR_DICHVU_HOVER = new Color(22, 163, 74);

    private static final Color CARD_GREEN = new Color(34, 197, 94);
    private static final Color CARD_BLUE = new Color(59, 130, 246);
    private static final Color CARD_ORANGE = new Color(249, 115, 22);
    private static final Color CARD_PURPLE = new Color(168, 85, 247);

    // ── Font ─────────────────────────────────────────────────────────────────
    private static final String FONT = "Be Vietnam Pro";

    // ── Filter options ───────────────────────────────────────────────────────
    private static final String[] FILTER_PERIOD = { "Tháng", "Quý", "Năm" };
    private static final String[] FILTER_TYPE = { "Tất cả", "Phòng", "Dịch vụ" };

    // ── State ────────────────────────────────────────────────────────────────
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private List<Integer> years = new ArrayList<>();

    private String selectedPeriod = "Tháng";
    private String selectedType = "Tất cả";
    private LocalDate dateFrom;
    private LocalDate dateTo;

    private long doanhThuPhong = 0;
    private long doanhThuDichVu = 0;
    private long tongDoanhThu = 0;
    private int soHoaDon = 0;
    private List<Object[]> chartData = new ArrayList<>();

    // ── Components ───────────────────────────────────────────────────────────
    private JLabel lblTongDoanhThu, lblDoanhThuPhong, lblDoanhThuDichVu, lblSoHoaDon;
    private JLabel lblCardTitleTong;
    private BarChartPanel barChartPanel;
    private PieChartPanel pieChartPanel;
    private JTextField txtFromDate, txtToDate;
    private JPanel pnlPeriodButtons;
    private JPanel pnlTypeButtons;

    public DoanhThuUI() {
        years.addAll(hoaDonDAO.getDanhSachNamHoaDon());
        int currentYear = LocalDate.now().getYear();
        if (years.isEmpty())
            years.add(currentYear);
        dateFrom = LocalDate.of(currentYear, 1, 1);
        dateTo = LocalDate.of(currentYear, 12, 31);
        loadData();
    }

    public void refresh() {
        years.clear();
        years.addAll(hoaDonDAO.getDanhSachNamHoaDon());
        int currentYear = LocalDate.now().getYear();
        if (years.isEmpty())
            years.add(currentYear);
        loadData();
        refreshAll();
    }

    public JPanel getPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(buildHeader());
        content.add(Box.createVerticalStrut(16));
        content.add(buildFilterBar());
        content.add(Box.createVerticalStrut(16));
        content.add(buildCards());
        content.add(Box.createVerticalStrut(16));
        content.add(buildChartsRow());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        root.add(scroll, BorderLayout.CENTER);

        return root;
    }

    // ── DATA ─────────────────────────────────────────────────────────────────
    private void loadData() {
        Object[] summary = hoaDonDAO.getThongKeDoanhThuTheoKhoangNgay(dateFrom, dateTo);
        doanhThuPhong = (long) summary[0];
        doanhThuDichVu = (long) summary[1];
        tongDoanhThu = (long) summary[2];
        soHoaDon = (int) summary[3];

        switch (selectedPeriod) {
            case "Tháng":
                chartData = hoaDonDAO.getDoanhThuTheoThangTrongKhoang(dateFrom, dateTo);
                break;
            case "Quý":
                chartData = hoaDonDAO.getDoanhThuTheoQuyTrongKhoang(dateFrom, dateTo);
                break;
            case "Năm":
                chartData = hoaDonDAO.getDoanhThuTheoNamTrongKhoang(dateFrom, dateTo);
                break;
        }
    }

    private void refreshAll() {
        loadData();
        lblCardTitleTong.setText("Tổng doanh thu");
        lblTongDoanhThu.setText(formatTien(tongDoanhThu));
        lblDoanhThuPhong.setText(formatTien(doanhThuPhong));
        lblDoanhThuDichVu.setText(formatTien(doanhThuDichVu));
        lblSoHoaDon.setText(String.valueOf(soHoaDon));
        barChartPanel.setData(chartData, selectedType);
        pieChartPanel.setData(doanhThuPhong, doanhThuDichVu);
    }

    // ── HEADER ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lbl = new JLabel("Doanh thu");
        lbl.setFont(new Font(FONT, Font.BOLD, 22));
        lbl.setForeground(TEXT_PRIMARY);
        pnl.add(lbl, BorderLayout.WEST);
        return pnl;
    }

    // ── FILTER BAR ───────────────────────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel wrapper = createRoundedPanel();
        wrapper.setLayout(new BorderLayout(0, 12));
        wrapper.setBorder(new EmptyBorder(16, 20, 16, 20));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topRow.setOpaque(false);

        JLabel lblPeriod = new JLabel("Kỳ:");
        lblPeriod.setFont(new Font(FONT, Font.BOLD, 12));
        lblPeriod.setForeground(TEXT_SECONDARY);
        topRow.add(lblPeriod);

        pnlPeriodButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlPeriodButtons.setOpaque(false);
        for (String period : FILTER_PERIOD) {
            JButton btn = createFilterChip(period, period.equals(selectedPeriod));
            btn.addActionListener(e -> {
                selectedPeriod = period;
                updateFilterChips(pnlPeriodButtons, selectedPeriod);
                refreshAll();
            });
            pnlPeriodButtons.add(btn);
        }
        topRow.add(pnlPeriodButtons);

        topRow.add(Box.createHorizontalStrut(20));

        JLabel lblType = new JLabel("Loại:");
        lblType.setFont(new Font(FONT, Font.BOLD, 12));
        lblType.setForeground(TEXT_SECONDARY);
        topRow.add(lblType);

        pnlTypeButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlTypeButtons.setOpaque(false);
        for (String type : FILTER_TYPE) {
            JButton btn = createFilterChip(type, type.equals(selectedType));
            btn.addActionListener(e -> {
                selectedType = type;
                updateFilterChips(pnlTypeButtons, selectedType);
                refreshAll();
            });
            pnlTypeButtons.add(btn);
        }
        topRow.add(pnlTypeButtons);

        wrapper.add(topRow, BorderLayout.NORTH);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bottomRow.setOpaque(false);

        JLabel lblFrom = new JLabel("Từ ngày:");
        lblFrom.setFont(new Font(FONT, Font.PLAIN, 12));
        lblFrom.setForeground(TEXT_SECONDARY);
        bottomRow.add(lblFrom);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        txtFromDate = new JTextField(dateFrom.format(dtf), 10);
        txtFromDate.setFont(new Font(FONT, Font.PLAIN, 12));
        txtFromDate.setPreferredSize(new Dimension(110, 30));
        bottomRow.add(txtFromDate);

        bottomRow.add(Box.createHorizontalStrut(8));
        JLabel lblTo = new JLabel("Đến ngày:");
        lblTo.setFont(new Font(FONT, Font.PLAIN, 12));
        lblTo.setForeground(TEXT_SECONDARY);
        bottomRow.add(lblTo);

        txtToDate = new JTextField(dateTo.format(dtf), 10);
        txtToDate.setFont(new Font(FONT, Font.PLAIN, 12));
        txtToDate.setPreferredSize(new Dimension(110, 30));
        bottomRow.add(txtToDate);

        bottomRow.add(Box.createHorizontalStrut(8));

        JButton btnApply = new JButton("Áp dụng");
        btnApply.setFont(new Font(FONT, Font.BOLD, 12));
        btnApply.setForeground(WHITE);
        btnApply.setBackground(ACCENT);
        btnApply.setBorderPainted(false);
        btnApply.setFocusPainted(false);
        btnApply.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnApply.setPreferredSize(new Dimension(90, 30));
        btnApply.addActionListener(e -> applyDateFilter());
        bottomRow.add(btnApply);

        bottomRow.add(Box.createHorizontalStrut(14));
        String[][] quickFilters = {
                { "Tháng này", "month" }, { "Quý này", "quarter" }, { "Năm nay", "year" }
        };
        for (String[] qf : quickFilters) {
            JButton btnQ = new JButton(qf[0]);
            btnQ.setFont(new Font(FONT, Font.PLAIN, 11));
            btnQ.setForeground(ACCENT);
            btnQ.setBackground(ACCENT_LIGHT);
            btnQ.setBorderPainted(false);
            btnQ.setFocusPainted(false);
            btnQ.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnQ.setMargin(new Insets(4, 12, 4, 12));
            String type = qf[1];
            btnQ.addActionListener(e -> applyQuickFilter(type));
            bottomRow.add(btnQ);
        }

        wrapper.add(bottomRow, BorderLayout.SOUTH);
        return wrapper;
    }

    private JButton createFilterChip(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT, Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(4, 14, 4, 14));
        btn.setBorderPainted(false);
        styleFilterChip(btn, active);
        return btn;
    }

    private void styleFilterChip(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(ACCENT);
            btn.setForeground(WHITE);
        } else {
            btn.setBackground(new Color(241, 245, 249));
            btn.setForeground(TEXT_SECONDARY);
        }
    }

    private void updateFilterChips(JPanel panel, String selected) {
        for (Component c : panel.getComponents()) {
            if (c instanceof JButton btn) {
                styleFilterChip(btn, btn.getText().equals(selected));
            }
        }
    }

    private void applyDateFilter() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            dateFrom = LocalDate.parse(txtFromDate.getText().trim(), dtf);
            dateTo = LocalDate.parse(txtToDate.getText().trim(), dtf);
            if (dateFrom.isAfter(dateTo)) {
                LocalDate tmp = dateFrom;
                dateFrom = dateTo;
                dateTo = tmp;
                txtFromDate.setText(dateFrom.format(dtf));
                txtToDate.setText(dateTo.format(dtf));
            }
            refreshAll();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(null, "Định dạng ngày không hợp lệ (dd/MM/yyyy)");
        }
    }

    private void applyQuickFilter(String type) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate now = LocalDate.now();
        switch (type) {
            case "month":
                dateFrom = now.withDayOfMonth(1);
                dateTo = now.withDayOfMonth(now.lengthOfMonth());
                break;
            case "quarter":
                int q = (now.getMonthValue() - 1) / 3;
                dateFrom = LocalDate.of(now.getYear(), q * 3 + 1, 1);
                dateTo = dateFrom.plusMonths(2).withDayOfMonth(dateFrom.plusMonths(2).lengthOfMonth());
                break;
            case "year":
                dateFrom = LocalDate.of(now.getYear(), 1, 1);
                dateTo = LocalDate.of(now.getYear(), 12, 31);
                break;
        }
        txtFromDate.setText(dateFrom.format(dtf));
        txtToDate.setText(dateTo.format(dtf));
        refreshAll();
    }

    // ── CARDS ────────────────────────────────────────────────────────────────
    private JPanel buildCards() {
        JPanel pnl = new JPanel(new GridLayout(1, 4, 12, 0));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        lblCardTitleTong = new JLabel("Tổng doanh thu");
        lblTongDoanhThu = new JLabel(formatTien(tongDoanhThu));
        lblDoanhThuPhong = new JLabel(formatTien(doanhThuPhong));
        lblDoanhThuDichVu = new JLabel(formatTien(doanhThuDichVu));
        lblSoHoaDon = new JLabel(String.valueOf(soHoaDon));

        pnl.add(buildCard(CARD_GREEN, "đ", lblCardTitleTong, lblTongDoanhThu));
        pnl.add(buildCard(CARD_BLUE, "\uD83C\uDFE0", new JLabel("Doanh thu phòng"), lblDoanhThuPhong));
        pnl.add(buildCard(CARD_ORANGE, "\u2699", new JLabel("Doanh thu dịch vụ"), lblDoanhThuDichVu));
        pnl.add(buildCard(CARD_PURPLE, "\uD83D\uDCCA", new JLabel("Số hóa đơn"), lblSoHoaDon));

        return pnl;
    }

    private JPanel buildCard(Color iconColor, String iconText, JLabel lblSub, JLabel lblVal) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel pnlIcon = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
            }
        };
        pnlIcon.setOpaque(false);
        pnlIcon.setPreferredSize(new Dimension(44, 44));

        JLabel lblIcon = new JLabel(iconText);
        if (iconText.equals("đ")) {
            lblIcon.setFont(new Font(FONT, Font.BOLD, 20));
            lblIcon.setForeground(WHITE);
        } else {
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        }
        pnlIcon.add(lblIcon);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);
        lblSub.setFont(new Font(FONT, Font.PLAIN, 11));
        lblSub.setForeground(TEXT_SECONDARY);
        lblVal.setFont(new Font(FONT, Font.BOLD, 18));
        lblVal.setForeground(TEXT_PRIMARY);
        textPanel.add(lblSub);
        textPanel.add(lblVal);

        card.add(pnlIcon, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    // ── CHARTS ROW ───────────────────────────────────────────────────────────
    private JPanel buildChartsRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 0, 0, 8);
        row.add(buildBarChartWrapper(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 8, 0, 0);
        row.add(buildPieChartWrapper(), gbc);

        return row;
    }

    private JPanel buildBarChartWrapper() {
        JPanel wrapper = createRoundedPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel("Biểu đồ doanh thu");
        lbl.setFont(new Font(FONT, Font.BOLD, 14));
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        legend.setOpaque(false);
        legend.add(createLegendItem(COLOR_PHONG, "Phòng"));
        legend.add(createLegendItem(COLOR_DICHVU, "Dịch vụ"));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lbl, BorderLayout.WEST);
        top.add(legend, BorderLayout.EAST);
        wrapper.add(top, BorderLayout.NORTH);

        barChartPanel = new BarChartPanel(chartData, selectedType);
        barChartPanel.setPreferredSize(new Dimension(0, 300));
        wrapper.add(barChartPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel buildPieChartWrapper() {
        JPanel wrapper = createRoundedPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel("Cơ cấu doanh thu");
        lbl.setFont(new Font(FONT, Font.BOLD, 14));
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        wrapper.add(lbl, BorderLayout.NORTH);

        pieChartPanel = new PieChartPanel(doanhThuPhong, doanhThuDichVu);
        pieChartPanel.setPreferredSize(new Dimension(0, 300));
        wrapper.add(pieChartPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createLegendItem(Color color, String text) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnl.setOpaque(false);

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, 10, 10);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(10, 10));
        pnl.add(dot);

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(FONT, Font.PLAIN, 11));
        lbl.setForeground(TEXT_SECONDARY);
        pnl.add(lbl);
        return pnl;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JPanel createRoundedPanel() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    private String formatTien(long value) {
        if (value == 0)
            return "0đ";
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + "đ";
    }

    // =========================================================================
    // BarChartPanel — grouped bar chart phòng + dịch vụ
    // =========================================================================
    private class BarChartPanel extends JPanel {
        private List<Object[]> data;
        private String filterType;
        private int hoveredIndex = -1;
        private String tooltipText = null;
        private int tooltipX, tooltipY;

        BarChartPanel(List<Object[]> data, String filterType) {
            this.data = data;
            this.filterType = filterType;
            setOpaque(false);
            setBackground(WHITE);
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    updateHover(e.getX(), e.getY());
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoveredIndex = -1;
                    tooltipText = null;
                    repaint();
                }
            });
        }

        void setData(List<Object[]> data, String filterType) {
            this.data = data;
            this.filterType = filterType;
            hoveredIndex = -1;
            tooltipText = null;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) {
                g.setFont(new Font(FONT, Font.PLAIN, 13));
                g.setColor(TEXT_SECONDARY);
                g.drawString("Không có dữ liệu", getWidth() / 2 - 50, getHeight() / 2);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int mL = 60, mR = 16, mT = 16, mB = 35;
            int cW = w - mL - mR, cH = h - mT - mB;
            int n = data.size();

            long maxVal = 0;
            for (Object[] row : data) {
                long p = (long) row[1], d = (long) row[2];
                long total = p + d;
                if (filterType.equals("Phòng"))
                    total = p;
                else if (filterType.equals("Dịch vụ"))
                    total = d;
                if (total > maxVal)
                    maxVal = total;
            }
            if (maxVal == 0)
                maxVal = 1_000_000;
            maxVal = roundUpNice(maxVal);

            int gridLines = 5;
            g2.setFont(new Font(FONT, Font.PLAIN, 10));
            for (int i = 0; i <= gridLines; i++) {
                int yPos = mT + cH - (int) ((double) i / gridLines * cH);
                long labelVal = (long) ((double) i / gridLines * maxVal);
                g2.setColor(new Color(229, 231, 235));
                g2.setStroke(
                        new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 4f }, 0f));
                g2.drawLine(mL, yPos, mL + cW, yPos);

                g2.setColor(TEXT_SECONDARY);
                g2.setStroke(new BasicStroke(1f));
                String yLabel = formatTrieu(labelVal);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(yLabel, mL - fm.stringWidth(yLabel) - 6, yPos + fm.getAscent() / 2 - 1);
            }

            float slotW = (float) cW / n;
            boolean showBoth = filterType.equals("Tất cả");

            for (int i = 0; i < n; i++) {
                Object[] row = data.get(i);
                String label = (String) row[0];
                long dtPhong = (long) row[1];
                long dtDichVu = (long) row[2];

                float groupW = slotW * 0.7f;
                float x0 = mL + i * slotW + (slotW - groupW) / 2;

                if (showBoth) {
                    float barW = groupW / 2 - 2;
                    int barH1 = (int) ((double) dtPhong / maxVal * cH);
                    if (barH1 > 0) {
                        g2.setColor(i == hoveredIndex ? COLOR_PHONG_HOVER : COLOR_PHONG);
                        int arc = Math.min(4, (int) barW / 2);
                        g2.fill(new RoundRectangle2D.Float(x0, mT + cH - barH1, barW, barH1, arc, arc));
                    }
                    int barH2 = (int) ((double) dtDichVu / maxVal * cH);
                    if (barH2 > 0) {
                        g2.setColor(i == hoveredIndex ? COLOR_DICHVU_HOVER : COLOR_DICHVU);
                        int arc = Math.min(4, (int) barW / 2);
                        g2.fill(new RoundRectangle2D.Float(x0 + barW + 4, mT + cH - barH2, barW, barH2, arc, arc));
                    }
                } else {
                    long val = filterType.equals("Phòng") ? dtPhong : dtDichVu;
                    Color col = filterType.equals("Phòng") ? COLOR_PHONG : COLOR_DICHVU;
                    Color colHov = filterType.equals("Phòng") ? COLOR_PHONG_HOVER : COLOR_DICHVU_HOVER;
                    int barH = (int) ((double) val / maxVal * cH);
                    if (barH > 0) {
                        g2.setColor(i == hoveredIndex ? colHov : col);
                        int arc = Math.min(6, (int) groupW / 2);
                        g2.fill(new RoundRectangle2D.Float(x0, mT + cH - barH, groupW, barH, arc, arc));
                    }
                }

                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font(FONT, Font.PLAIN, 10));
                FontMetrics fm = g2.getFontMetrics();
                int lx = (int) (x0 + groupW / 2) - fm.stringWidth(label) / 2;
                g2.drawString(label, lx, mT + cH + mB - 8);
            }

            if (tooltipText != null && hoveredIndex >= 0) {
                drawTooltip(g2, tooltipText, tooltipX, tooltipY);
            }
            g2.dispose();
        }

        private void updateHover(int mx, int my) {
            int w = getWidth(), h = getHeight();
            int mL = 60, mR = 16, mT = 16, mB = 35;
            int cW = w - mL - mR, cH = h - mT - mB;
            int n = data.size();
            if (n == 0)
                return;
            float slotW = (float) cW / n;

            hoveredIndex = -1;
            tooltipText = null;

            for (int i = 0; i < n; i++) {
                int slotX = mL + (int) (i * slotW);
                if (mx >= slotX && mx < slotX + (int) slotW && my >= mT && my <= mT + cH) {
                    hoveredIndex = i;
                    Object[] row = data.get(i);
                    long p = (long) row[1], d = (long) row[2];
                    tooltipText = row[0] + "\nPhòng: " + formatTien(p) + "\nDịch vụ: " + formatTien(d)
                            + "\nTổng: " + formatTien(p + d);
                    tooltipX = mx;
                    tooltipY = my - 10;
                    break;
                }
            }
            repaint();
        }

        private void drawTooltip(Graphics2D g2, String text, int x, int y) {
            String[] lines = text.split("\n");
            Font f = new Font(FONT, Font.PLAIN, 11);
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            int pad = 8, lineH = fm.getHeight();
            int bw = 0;
            for (String l : lines)
                bw = Math.max(bw, fm.stringWidth(l));
            bw += pad * 2;
            int bh = lineH * lines.length + pad * 2;
            int tx = Math.min(x + 10, getWidth() - bw - 4);
            int ty = Math.max(y - bh - 4, 4);

            g2.setColor(new Color(0, 0, 0, 20));
            g2.fillRoundRect(tx + 2, ty + 2, bw, bh, 8, 8);
            g2.setColor(WHITE);
            g2.fillRoundRect(tx, ty, bw, bh, 8, 8);
            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(tx, ty, bw, bh, 8, 8);
            g2.setColor(TEXT_PRIMARY);
            for (int i = 0; i < lines.length; i++) {
                g2.drawString(lines[i], tx + pad, ty + pad + fm.getAscent() + i * lineH);
            }
        }

        private long roundUpNice(long val) {
            if (val <= 0)
                return 1_000_000;
            long magnitude = (long) Math.pow(10, (long) Math.log10(val));
            return ((val / magnitude) + 1) * magnitude;
        }

        private String formatTrieu(long value) {
            if (value == 0)
                return "0";
            if (value >= 1_000_000)
                return (value / 1_000_000) + "M";
            if (value >= 1_000)
                return (value / 1_000) + "K";
            return String.valueOf(value);
        }
    }

    // =========================================================================
    // PieChartPanel — donut chart phòng vs dịch vụ
    // =========================================================================
    private class PieChartPanel extends JPanel {
        private long valPhong, valDichVu;
        private int hoveredSlice = -1;

        PieChartPanel(long valPhong, long valDichVu) {
            this.valPhong = valPhong;
            this.valDichVu = valDichVu;
            setOpaque(false);
            setBackground(WHITE);

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    updatePieHover(e.getX(), e.getY());
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoveredSlice = -1;
                    repaint();
                }
            });
        }

        void setData(long valPhong, long valDichVu) {
            this.valPhong = valPhong;
            this.valDichVu = valDichVu;
            hoveredSlice = -1;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            long total = valPhong + valDichVu;

            if (total == 0) {
                g2.setFont(new Font(FONT, Font.PLAIN, 13));
                g2.setColor(TEXT_SECONDARY);
                g2.drawString("Không có dữ liệu", w / 2 - 50, h / 2);
                g2.dispose();
                return;
            }

            int size = Math.min(w, h) - 80;
            if (size < 80)
                size = 80;
            int cx = w / 2, cy = h / 2 - 20;
            int x0 = cx - size / 2, y0 = cy - size / 2;
            int innerSize = (int) (size * 0.55);
            int ix = cx - innerSize / 2, iy = cy - innerSize / 2;

            double anglePhong = 360.0 * valPhong / total;
            double angleDichVu = 360.0 - anglePhong;

            g2.setColor(hoveredSlice == 0 ? COLOR_PHONG_HOVER : COLOR_PHONG);
            g2.fill(new Arc2D.Double(x0, y0, size, size, 90, -anglePhong, Arc2D.PIE));

            g2.setColor(hoveredSlice == 1 ? COLOR_DICHVU_HOVER : COLOR_DICHVU);
            g2.fill(new Arc2D.Double(x0, y0, size, size, 90 - anglePhong, -angleDichVu, Arc2D.PIE));

            g2.setColor(WHITE);
            g2.fillOval(ix, iy, innerSize, innerSize);

            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font(FONT, Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            String centerText = formatTien(total);
            g2.drawString(centerText, cx - fm.stringWidth(centerText) / 2, cy + fm.getAscent() / 2 - 2);

            int ly = cy + size / 2 + 20;
            g2.setFont(new Font(FONT, Font.PLAIN, 11));

            double pctPhong = 100.0 * valPhong / total;
            double pctDichVu = 100.0 * valDichVu / total;

            g2.setColor(COLOR_PHONG);
            g2.fillOval(cx - 100, ly - 4, 10, 10);
            g2.setColor(TEXT_PRIMARY);
            g2.drawString(String.format("Phòng: %.1f%%", pctPhong), cx - 85, ly + 5);

            g2.setColor(COLOR_DICHVU);
            g2.fillOval(cx + 10, ly - 4, 10, 10);
            g2.setColor(TEXT_PRIMARY);
            g2.drawString(String.format("Dịch vụ: %.1f%%", pctDichVu), cx + 25, ly + 5);

            g2.dispose();
        }

        private void updatePieHover(int mx, int my) {
            int w = getWidth(), h = getHeight();
            int cx = w / 2, cy = h / 2 - 20;
            int size = Math.min(w, h) - 80;
            if (size < 80)
                size = 80;

            double dx = mx - cx, dy = my - cy;
            double dist = Math.sqrt(dx * dx + dy * dy);
            int innerSize = (int) (size * 0.55);

            if (dist > size / 2.0 || dist < innerSize / 2.0) {
                hoveredSlice = -1;
                repaint();
                return;
            }

            long total = valPhong + valDichVu;
            if (total == 0) {
                hoveredSlice = -1;
                repaint();
                return;
            }

            double angle = Math.toDegrees(Math.atan2(-dy, dx));
            if (angle < 0)
                angle += 360;
            double phongAngle = 360.0 * valPhong / total;
            double rel = 90 - angle;
            if (rel < 0)
                rel += 360;

            hoveredSlice = (rel <= phongAngle) ? 0 : 1;
            repaint();
        }
    }
}