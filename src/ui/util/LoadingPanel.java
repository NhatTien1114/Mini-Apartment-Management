package ui.util;

import java.awt.*;
import javax.swing.*;

public class LoadingPanel extends JPanel {
    private float angle = 0f;
    private final Timer timer;
    private final JPanel spinner;

    public LoadingPanel(Color background) {
        setBackground(background);
        setLayout(new GridBagLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        spinner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 36;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(37, 99, 235, 40));
                g2.drawOval(x, y, size, size);
                g2.setColor(AppColors.PRIMARY);
                g2.drawArc(x, y, size, size, (int) angle, 100);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(52, 52);
            }
        };
        spinner.setOpaque(false);
        spinner.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = new JLabel("Đang tải dữ liệu...");
        lbl.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 13));
        lbl.setForeground(AppColors.SLATE_400);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(spinner);
        inner.add(Box.createVerticalStrut(10));
        inner.add(lbl);
        add(inner);

        timer = new Timer(16, e -> {
            angle = (angle + 9) % 360;
            spinner.repaint();
        });
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) timer.start();
        else timer.stop();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (isVisible()) timer.start();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }
}
