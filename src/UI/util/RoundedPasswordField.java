package ui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

public class RoundedPasswordField extends JPasswordField {
    private int cornerRadius = 6; 
    private String placeholder = "";
    private boolean isFocused = false;
    
    private final Color RING_COLOR = new Color(59, 130, 246); // Tailwind blue-500
    private final Color BG_COLOR = new Color(248, 250, 252); // slate-50

    private char echoCharConfig = '•';

    public RoundedPasswordField(int radius) {
        super();
        this.cornerRadius = radius;
        setOpaque(false);
        // padding chừa sát góc phải đúng bề ngang con mắt (34px)
        setBorder(new EmptyBorder(8, 12, 8, 34)); 
        setFont(new Font("Inter", Font.PLAIN, 15));
        setBackground(BG_COLOR);
        setForeground(new Color(15, 23, 42));
        setEchoChar(echoCharConfig);
        
        setLayout(null); // Sử dụng null layout để ôm sát góc
        
        JButton btnEye = new JButton() {
            boolean isEyeHovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { isEyeHovered = true; repaint(); }
                    public void mouseExited(MouseEvent e) { isEyeHovered = false; repaint(); }
                });
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                
                g2.setColor(isEyeHovered ? new Color(15, 23, 42) : new Color(100, 116, 139));
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                
                Path2D eye = new Path2D.Double();
                eye.moveTo(cx - 7, cy);
                eye.quadTo(cx, cy - 6, cx + 7, cy);
                eye.quadTo(cx, cy + 6, cx - 7, cy);
                g2.draw(eye);
                g2.drawOval(cx - 2, cy - 2, 4, 4); 
                
                if (getEchoChar() != (char) 0) {
                    g2.drawLine(cx - 7, cy - 6, cx + 7, cy + 6);
                }
                
                g2.dispose();
            }
        };
        btnEye.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEye.setContentAreaFilled(false);
        btnEye.setBorderPainted(false);
        btnEye.setFocusPainted(false);
        
        btnEye.addActionListener(e -> {
            if (getEchoChar() == '•') {
                setEchoChar((char) 0); 
            } else {
                setEchoChar('•'); 
            }
        });

        add(btnEye);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Ép sát góc phải
                btnEye.setBounds(getWidth() - 34, 0, 34, getHeight());
            }
        });
        
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
        if (getPassword().length == 0 && placeholder != null) {
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
