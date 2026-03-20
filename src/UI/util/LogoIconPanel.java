package ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class LogoIconPanel extends JPanel {
    private final int boxSize = 80;
    private final Color bgColor = AppColors.PRIMARY_OVERLAY;
    private final Color iconColor = AppColors.PRIMARY;

    public LogoIconPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(boxSize, boxSize));
        setMaximumSize(new Dimension(boxSize, boxSize));
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // 1. Vẽ nền bo góc (w-20 h-20 rounded-2xl)
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, boxSize, boxSize, 24, 24); // rounded-2xl tương đương border-radius 16px (hoặc 24px để ôm tròn đẹp hơn trong Swing)

        // 2. Tính toán ma trận biến đổi để scale từ ViewBox 24x24 lên 40x40
        double scale = 40.0 / 24.0;
        double offset = (boxSize - 40.0) / 2.0;

        g2.translate(offset, offset);
        g2.scale(scale, scale);

        // 3. Thiết lập nét vẽ cho icon
        g2.setColor(iconColor);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // -- BẮT ĐẦU VẼ CÁC ĐƯỜNG PATH CỦA LUCIDE BUILDING-2 --
        
        // Path 1: Tòa nhà chính giữa
        Path2D p1 = new Path2D.Double();
        p1.moveTo(6, 22);
        p1.lineTo(6, 4);
        p1.append(new Arc2D.Double(6, 2, 4, 4, 180, -90, Arc2D.OPEN), true); // góc bo trái trên
        p1.lineTo(14, 2);
        p1.append(new Arc2D.Double(14, 2, 4, 4, 90, -90, Arc2D.OPEN), true); // góc bo phải trên
        p1.lineTo(18, 22);
        p1.closePath();
        g2.draw(p1);

        // Path 2: Tòa nhà phụ bên trái
        Path2D p2 = new Path2D.Double();
        p2.moveTo(6, 12);
        p2.lineTo(4, 12);
        p2.append(new Arc2D.Double(2, 12, 4, 4, 90, 90, Arc2D.OPEN), true); // góc bo trái trên
        p2.lineTo(2, 20);
        p2.append(new Arc2D.Double(2, 18, 4, 4, 180, 90, Arc2D.OPEN), true); // góc bo trái dưới
        p2.lineTo(6, 22);
        g2.draw(p2);

        // Path 3: Tòa nhà phụ bên phải
        Path2D p3 = new Path2D.Double();
        p3.moveTo(18, 9);
        p3.lineTo(20, 9);
        p3.append(new Arc2D.Double(20, 9, 4, 4, 90, -90, Arc2D.OPEN), true); // góc bo phải trên
        p3.lineTo(24, 20);
        p3.append(new Arc2D.Double(20, 18, 4, 4, 0, -90, Arc2D.OPEN), true); // góc bo phải dưới
        p3.lineTo(18, 22);
        g2.draw(p3);

        // Windows (4 dòng kẻ ngang)
        g2.draw(new Line2D.Double(10, 6, 14, 6));
        g2.draw(new Line2D.Double(10, 10, 14, 10));
        g2.draw(new Line2D.Double(10, 14, 14, 14));
        g2.draw(new Line2D.Double(10, 18, 14, 18));

        g2.dispose();
    }
}
