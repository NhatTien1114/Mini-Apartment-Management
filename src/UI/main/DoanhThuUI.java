package UI.main;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DoanhThuUI {

    // ── Màu sắc ──────────────────────────────────────────────────────────────
    private static final Color MAU_NEN         = new Color(229, 231, 235);
    private static final Color MAU_TRANG       = Color.WHITE;
    private static final Color MAU_XANH_LA     = new Color(34, 197, 94);
    private static final Color MAU_XANH_DUONG  = new Color(59, 130, 246);
    private static final Color MAU_VANG        = new Color(234, 179, 8);
    private static final Color MAU_CHU_PHU     = new Color(107, 114, 128);
    private static final Color MAU_CHU_CHINH   = new Color(17, 24, 39);
    private static final Color MAU_COT         = new Color(156, 163, 175);
    private static final Color MAU_COT_HOVER   = new Color(99, 102, 241);
    private static final Color MAU_LUOI        = new Color(229, 231, 235);

    // ── Font ─────────────────────────────────────────────────────────────────
    private static final String FONT = "Be Vietnam Pro";

    // ── Tháng ────────────────────────────────────────────────────────────────
    private static final String[] THANG = {
            "T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"
    };

    // ── Mock data theo năm (index 0=2024, 1=2025, 2=2026, 3=2027) ───────────
    private static final long[][] DATA = {
            {5_000_000, 8_200_000, 6_100_000, 9_500_000, 11_000_000, 7_300_000,
                    12_400_000, 10_800_000, 9_200_000, 13_100_000, 14_500_000, 18_000_000},
            {3_500_000, 4_200_000, 6_800_000, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };
    private static final int[] YEARS = {2024, 2025, 2026, 2027};

    // ── State ────────────────────────────────────────────────────────────────
    private int selectedYearIndex = 2; // mặc định 2026

    // ── Components cần refresh ───────────────────────────────────────────────
    private JLabel lblTitleTong;
    private JLabel lblTongDoanhThu;
    private JLabel lblSoHoaDon;
    private JLabel lblTBThang;
    private BarChartPanel barChartPanel;

    // ─────────────────────────────────────────────────────────────────────────
    public JPanel getPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 16));
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnl.setBackground(MAU_NEN);

        pnl.add(buildHeader(), BorderLayout.NORTH);
        pnl.add(buildBody(),   BorderLayout.CENTER);

        return pnl;
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);

        JLabel lblTitle = new JLabel("Doanh thu");
        lblTitle.setFont(new Font(FONT, Font.BOLD, 22));
        lblTitle.setForeground(MAU_CHU_CHINH);
        pnl.add(lblTitle, BorderLayout.WEST);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlRight.setOpaque(false);

        JLabel lblNam = new JLabel("Năm:");
        lblNam.setFont(new Font(FONT, Font.PLAIN, 13));
        lblNam.setForeground(MAU_CHU_PHU);
        lblNam.setBorder(new EmptyBorder(0, 0, 0, 6));

        String[] yearStrs = new String[YEARS.length];
        for (int i = 0; i < YEARS.length; i++) yearStrs[i] = String.valueOf(YEARS[i]);

        JComboBox<String> cboNam = new JComboBox<>(yearStrs);
        cboNam.setSelectedIndex(selectedYearIndex);
        cboNam.setFont(new Font(FONT, Font.PLAIN, 13));
        cboNam.setFocusable(false);
        cboNam.setPreferredSize(new Dimension(90, 30));
        cboNam.addActionListener(e -> {
            selectedYearIndex = cboNam.getSelectedIndex();
            refreshData();
        });

        pnlRight.add(lblNam);
        pnlRight.add(cboNam);
        pnl.add(pnlRight, BorderLayout.EAST);

        return pnl;
    }

    // ── Body ─────────────────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel pnl = new JPanel(new BorderLayout(0, 16));
        pnl.setOpaque(false);
        pnl.add(buildCards(),        BorderLayout.NORTH);
        pnl.add(buildChartWrapper(), BorderLayout.CENTER);
        return pnl;
    }

    // ── 3 thẻ thống kê ────────────────────────────────────────────────────────
    private JPanel buildCards() {
        JPanel pnl = new JPanel(new GridLayout(1, 3, 12, 0));
        pnl.setOpaque(false);

        int year    = YEARS[selectedYearIndex];
        long[] data = DATA[selectedYearIndex];
        long tong   = tinhTong(data);
        long soHD   = tinhSoHoaDon(data);
        long tb     = tong / 12;

        lblTitleTong    = new JLabel("Tổng doanh thu " + year);
        lblTongDoanhThu = new JLabel(formatTien(tong));
        lblSoHoaDon     = new JLabel(String.valueOf(soHD));
        lblTBThang      = new JLabel(formatTien(tb));

        pnl.add(buildCard(MAU_XANH_LA,    "đ",  lblTitleTong,              lblTongDoanhThu));
        pnl.add(buildCard(MAU_XANH_DUONG, "\uD83D\uDCCA", new JLabel("Số hóa đơn"), lblSoHoaDon));
        pnl.add(buildCard(MAU_VANG,       "\uD83D\uDCC9", new JLabel("TB/tháng"),   lblTBThang));

        return pnl;
    }

    private JPanel buildCard(Color iconColor, String iconText, JLabel lblSub, JLabel lblVal) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MAU_TRANG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Icon box
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
            lblIcon.setForeground(MAU_TRANG);
        } else {
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        }
        pnlIcon.add(lblIcon);

        // Text
        JPanel pnlText = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlText.setOpaque(false);

        lblSub.setFont(new Font(FONT, Font.PLAIN, 11));
        lblSub.setForeground(MAU_CHU_PHU);

        lblVal.setFont(new Font(FONT, Font.BOLD, 20));
        lblVal.setForeground(MAU_CHU_CHINH);

        pnlText.add(lblSub);
        pnlText.add(lblVal);

        card.add(pnlIcon, BorderLayout.WEST);
        card.add(pnlText, BorderLayout.CENTER);

        return card;
    }

    // ── Wrapper biểu đồ ──────────────────────────────────────────────────────
    private JPanel buildChartWrapper() {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MAU_TRANG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblChartTitle = new JLabel("Biểu đồ doanh thu theo tháng");
        lblChartTitle.setFont(new Font(FONT, Font.BOLD, 14));
        lblChartTitle.setForeground(MAU_CHU_CHINH);
        lblChartTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        wrapper.add(lblChartTitle, BorderLayout.NORTH);

        barChartPanel = new BarChartPanel(DATA[selectedYearIndex]);
        wrapper.add(barChartPanel, BorderLayout.CENTER);

        return wrapper;
    }

    // ── Refresh khi đổi năm ───────────────────────────────────────────────────
    private void refreshData() {
        int year    = YEARS[selectedYearIndex];
        long[] data = DATA[selectedYearIndex];
        long tong   = tinhTong(data);
        long soHD   = tinhSoHoaDon(data);
        long tb     = tong / 12;

        lblTitleTong.setText("Tổng doanh thu " + year);
        lblTongDoanhThu.setText(formatTien(tong));
        lblSoHoaDon.setText(String.valueOf(soHD));
        lblTBThang.setText(formatTien(tb));

        barChartPanel.setData(data);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private long tinhTong(long[] data) {
        long s = 0; for (long v : data) s += v; return s;
    }
    private long tinhSoHoaDon(long[] data) {
        int c = 0; for (long v : data) if (v > 0) c++; return c;
    }
    private String formatTien(long value) {
        if (value == 0) return "0đ";
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + "đ";
    }

    // =========================================================================
    //  BarChartPanel — tự vẽ bằng Graphics2D, KHÔNG dùng thư viện ngoài
    // =========================================================================
    private class BarChartPanel extends JPanel {

        private long[] data;
        private int hoveredIndex = -1;
        private String tooltipText = null;
        private int tooltipX, tooltipY;

        BarChartPanel(long[] data) {
            this.data = data;
            setOpaque(false);
            setBackground(MAU_TRANG);

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
                    tooltipText  = null;
                    repaint();
                }
            });
        }

        void setData(long[] data) {
            this.data = data;
            hoveredIndex = -1;
            tooltipText  = null;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int marginLeft   = 55;
            int marginRight  = 16;
            int marginTop    = 16;
            int marginBottom = 30;

            int chartW = w - marginLeft - marginRight;
            int chartH = h - marginTop - marginBottom;

            // Max value
            long maxVal = 0;
            for (long v : data) if (v > maxVal) maxVal = v;
            if (maxVal == 0) maxVal = 1_000_000;

            // Làm tròn maxVal lên bội số đẹp
            maxVal = roundUpNice(maxVal);

            // ── Lưới ngang + nhãn trục Y ──────────────────────────────────
            int gridLines = 5;
            g2.setFont(new Font(FONT, Font.PLAIN, 10));

            for (int i = 0; i <= gridLines; i++) {
                int yPos = marginTop + chartH - (int)((double) i / gridLines * chartH);
                long labelVal = (long)((double) i / gridLines * maxVal);

                g2.setColor(MAU_LUOI);
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        1f, new float[]{4f}, 0f));
                g2.drawLine(marginLeft, yPos, marginLeft + chartW, yPos);

                g2.setColor(MAU_CHU_PHU);
                g2.setStroke(new BasicStroke(1f));
                String yLabel = formatTrieu(labelVal);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(yLabel, marginLeft - fm.stringWidth(yLabel) - 6,
                        yPos + fm.getAscent() / 2 - 1);
            }

            // ── Vẽ cột ────────────────────────────────────────────────────
            int n       = data.length;
            float slotW = (float) chartW / n;
            float barW  = slotW * 0.45f;

            for (int i = 0; i < n; i++) {
                int barH = (int)((double) data[i] / maxVal * chartH);
                int x    = marginLeft + (int)(i * slotW + (slotW - barW) / 2);
                int y    = marginTop + chartH - barH;

                if (barH > 0) {
                    g2.setColor(i == hoveredIndex ? MAU_COT_HOVER : MAU_COT);
                    g2.setStroke(new BasicStroke(1f));
                    int arc = Math.min(6, (int) barW / 2);
                    g2.fill(new RoundRectangle2D.Float(x, y, barW, barH, arc, arc));
                }

                // Label trục X
                g2.setColor(MAU_CHU_PHU);
                FontMetrics fm = g2.getFontMetrics();
                String lbl = THANG[i];
                int lx = x + (int)(barW / 2) - fm.stringWidth(lbl) / 2;
                int ly = marginTop + chartH + marginBottom - 8;
                g2.drawString(lbl, lx, ly);
            }

            // ── Tooltip ───────────────────────────────────────────────────
            if (tooltipText != null && hoveredIndex >= 0) {
                drawTooltip(g2, tooltipText, tooltipX, tooltipY);
            }

            g2.dispose();
        }

        private void updateHover(int mx, int my) {
            int w = getWidth(), h = getHeight();
            int marginLeft = 55, marginRight = 16, marginTop = 16, marginBottom = 30;
            int chartW = w - marginLeft - marginRight;
            int chartH = h - marginTop - marginBottom;

            int n = data.length;
            float slotW = (float) chartW / n;

            hoveredIndex = -1;
            tooltipText  = null;

            for (int i = 0; i < n; i++) {
                int slotX = marginLeft + (int)(i * slotW);
                if (mx >= slotX && mx < slotX + (int) slotW
                        && my >= marginTop && my <= marginTop + chartH) {
                    hoveredIndex = i;
                    tooltipText  = THANG[i] + "\nrevenue : " + formatTien(data[i]);
                    tooltipX     = mx;
                    tooltipY     = my - 10;
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

            int pad   = 8;
            int lineH = fm.getHeight();
            int bw    = 0;
            for (String l : lines) bw = Math.max(bw, fm.stringWidth(l));
            bw += pad * 2;
            int bh = lineH * lines.length + pad * 2;

            int tx = Math.min(x + 10, getWidth()  - bw - 4);
            int ty = Math.max(y - bh - 4, 4);

            // Shadow nhẹ
            g2.setColor(new Color(0, 0, 0, 20));
            g2.fillRoundRect(tx + 2, ty + 2, bw, bh, 6, 6);

            // Nền trắng + viền
            g2.setColor(MAU_TRANG);
            g2.fillRoundRect(tx, ty, bw, bh, 6, 6);
            g2.setColor(new Color(209, 213, 219));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(tx, ty, bw, bh, 6, 6);

            // Text
            g2.setColor(MAU_CHU_CHINH);
            for (int i = 0; i < lines.length; i++) {
                g2.drawString(lines[i], tx + pad, ty + pad + fm.getAscent() + i * lineH);
            }
        }

        // Làm tròn lên bội số đẹp để trục Y không bị lẻ
        private long roundUpNice(long val) {
            if (val <= 0) return 1_000_000;
            long magnitude = (long) Math.pow(10, (long) Math.log10(val));
            return ((val / magnitude) + 1) * magnitude;
        }

        private String formatTrieu(long value) {
            if (value == 0) return "0M";
            return (value / 1_000_000) + "M";
        }
    }
}