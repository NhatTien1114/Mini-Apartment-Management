package ui.main;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import ui.util.AppColors;

public class DoanhThuUI {
    private final Color MAU_NEN = AppColors.APP_BACKGROUND;
    private final Color MAU_DOANH_THU = AppColors.REVENUE;
    private final Color MAU_CHI_PHI = AppColors.ORANGE_500;
    private final Color MAU_LOI_NHUAN = AppColors.VIOLET_500;
    private final Color MAU_LAP_DAY = AppColors.PINK_500;
    
    public JPanel getPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BorderLayout(20, 20));
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnl.setBackground(MAU_NEN);

        JLabel lblTitle = new JLabel("Thống Kê Doanh Thu");
        lblTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 24));
        pnl.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlContent = new JPanel();
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));
        pnlContent.setOpaque(false);

        // --- 1. Thẻ tóm tắt ---
        JPanel pnlCards = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlCards.setOpaque(false);
        pnlCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        pnlCards.add(createCard("Tổng doanh thu", "1.520.000.000", MAU_DOANH_THU));
        pnlCards.add(createCard("Tổng chi phí", "670.000.000", MAU_CHI_PHI));
        pnlCards.add(createCard("Lợi nhuận", "850.000.000", MAU_LOI_NHUAN));
        pnlCards.add(createCard("Tỉ lệ lấp đầy", "85%", MAU_LAP_DAY));
        // --- 2. KHỐI BIỂU ĐỒ (12 THÁNG) ---
        JPanel pnlChartContainer = new JPanel(new BorderLayout(0, 10));
        pnlChartContainer.setBackground(Color.WHITE);
        pnlChartContainer.setBorder(new EmptyBorder(20, 20, 10, 20));
        // Cho biểu đồ cao hơn một chút để 12 tháng nhìn rõ
        pnlChartContainer.setPreferredSize(new Dimension(1000, 400));

        JLabel lblChartTitle = new JLabel("Biểu đồ doanh thu & chi phí năm 2026");
        lblChartTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 18));
        pnlChartContainer.add(lblChartTitle, BorderLayout.NORTH);

        // Panel chứa 12 cột
        JPanel pnlBars = new JPanel(new GridLayout(1, 12, 10, 0));
        pnlBars.setOpaque(false);

        // Dữ liệu giả lập cho 12 tháng (Doanh thu, Chi phí)
        int[][] monthlyData = {
                {80, 40}, {90, 50}, {110, 60}, {130, 70}, {120, 65}, {140, 80},
                {150, 85}, {130, 75}, {110, 60}, {120, 70}, {140, 90}, {160, 95}
        };

        for (int i = 0; i < 12; i++) {
            pnlBars.add(createBarGroup("T" + (i + 1), monthlyData[i][0], monthlyData[i][1]));
        }

        pnlChartContainer.add(pnlBars, BorderLayout.CENTER);

        // Chú thích
        JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlLegend.setOpaque(false);
        pnlLegend.add(createLegendItem("Doanh thu", MAU_DOANH_THU));
        pnlLegend.add(createLegendItem("Chi phí", MAU_CHI_PHI));
        pnlChartContainer.add(pnlLegend, BorderLayout.SOUTH);

        pnlContent.add(pnlCards);
        pnlContent.add(Box.createVerticalStrut(20));
        pnlContent.add(pnlChartContainer);

        pnl.add(pnlContent, BorderLayout.CENTER);
        return pnl;
    }
    private JPanel createBarGroup(String label, int revenue, int cost) {
        JPanel group = new JPanel(new BorderLayout(0, 5));
        group.setOpaque(false);

        // Panel vẽ cột
        JPanel barContainer = new JPanel(null) {
            @Override
            public Dimension getPreferredSize() { return new Dimension(50, 220); }
        };
        barContainer.setOpaque(false);

        // Tính toán chiều cao (giả sử tối đa là 200px)
        int h1 = revenue;
        int h2 = cost;

        // Cột Doanh Thu (Xanh)
        JPanel barRev = new JPanel();
        barRev.setBackground(MAU_DOANH_THU);
        barRev.setBounds(5, 200 - h1, 15, h1);
        barRev.setToolTipText("<html><b>" + label + "</b><br>Doanh thu: " + revenue + ".000.000đ</html>");

        // Cột Chi Phí (Cam)
        JPanel barCost = new JPanel();
        barCost.setBackground(MAU_CHI_PHI);
        barCost.setBounds(22, 200 - h2, 15, h2);
        barCost.setToolTipText("<html><b>" + label + "</b><br>Chi phí: " + cost + ".000.000đ</html>");

        // Thêm hiệu ứng hover để chắc chắn Tooltip hiện nhanh
        MouseAdapter hoverEffect = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ((JPanel)e.getSource()).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                ((JPanel)e.getSource()).setBorder(null);
            }
        };
        barRev.addMouseListener(hoverEffect);
        barCost.addMouseListener(hoverEffect);

        barContainer.add(barRev);
        barContainer.add(barCost);

        JLabel lblMonth = new JLabel(label, SwingConstants.CENTER);
        lblMonth.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 11));

        group.add(barContainer, BorderLayout.CENTER);
        group.add(lblMonth, BorderLayout.SOUTH);
        return group;
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(12, 12));
        box.setBackground(color);
        p.add(box);
        p.add(new JLabel(text));
        return p;
    }

    private JPanel createCard(String title, String value, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 219), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 13));
        lblTitle.setForeground(Color.GRAY);
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Be Vietnam Pro", Font.BOLD, 20));
        lblValue.setForeground(color);
        card.add(lblTitle);
        card.add(lblValue);
        return card;
    }
}
