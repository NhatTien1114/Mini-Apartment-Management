package ui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class RoundedTextField extends JTextField {
    private int cornerRadius = 6;
    private String placeholder = "";
    private boolean isFocused = false;
    
    private final Color RING_COLOR = AppColors.PRIMARY_RING;
    // Màu nền xám nhạt vĩnh viễn (hsl(var(--background)) -> gần slate-50)
    private final Color BG_COLOR = AppColors.SLATE_50;

    public RoundedTextField(int radius) {
        super();
        this.cornerRadius = radius;
        setOpaque(false);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setFont(new Font("Inter", Font.PLAIN, 15));
        setBackground(BG_COLOR);
        setForeground(AppColors.SLATE_900);
        
        // Hiệu ứng Focus Ring
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(BG_COLOR);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        g2.dispose();
        
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (isFocused) {
            g2.setColor(RING_COLOR);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, cornerRadius, cornerRadius);
        } else {
            g2.setColor(new Color(226, 232, 240)); 
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }
        g2.dispose();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (getText().length() == 0 && placeholder != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(100, 116, 139)); 
            g2.setFont(getFont());
            FontMetrics metrics = g2.getFontMetrics(getFont());
            int x = getInsets().left;
            int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(placeholder, x, y);
            g2.dispose();
        }
    }
}
