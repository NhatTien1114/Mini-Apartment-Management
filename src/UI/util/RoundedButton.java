package ui.util;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

public class RoundedButton extends JButton {
    private int cornerRadius = 12;

    public RoundedButton(String text, int radius) {
        super(text);
        this.cornerRadius = radius;
        setOpaque(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        // Draw border
        g2.setColor(getBackground().darker());
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        // Draw text
        super.paintComponent(g);
    }

    @Override
    public void paintBorder(Graphics g) {
    }
}

