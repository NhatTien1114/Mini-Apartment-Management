package ui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ValidationPopup {
    private static JWindow currentPopup;

    public static void show(JComponent target, String message) {
        if (currentPopup != null) {
            currentPopup.dispose();
        }
        
        Window parent = SwingUtilities.getWindowAncestor(target);
        if (parent == null) return;

        currentPopup = new JWindow(parent);
        
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        pnl.setBackground(AppColors.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.SLATE_300, 1),
            new EmptyBorder(2, 4, 2, 8)
        ));
        
        Icon warnIcon = new Icon() {
            @Override public int getIconWidth() { return 18; }
            @Override public int getIconHeight() { return 18; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.ORANGE_500);
                g2.fillRoundRect(x, y, 18, 18, 4, 4);
                g2.setColor(AppColors.WHITE);
                g2.setFont(new Font("Inter", Font.BOLD, 14));
                g2.drawString("!", x + 7, y + 14);
                g2.dispose();
            }
        };
        
        JLabel lbl = new JLabel(message);
        lbl.setFont(new Font("Inter", Font.PLAIN, 13));
        lbl.setForeground(AppColors.SLATE_900);
        lbl.setIcon(warnIcon);
        lbl.setIconTextGap(8);
        
        pnl.add(lbl);
        currentPopup.add(pnl);
        currentPopup.pack();
        
        Point loc = target.getLocationOnScreen();
        // Căn tooltip ngay bên dưới textfield (mô phỏng HTML5 Validation)
        currentPopup.setLocation(loc.x + 10, loc.y + target.getHeight() + 4);
        currentPopup.setVisible(true);

        // Hide when typing or clicking over the target
        target.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) { hideParams(); }
        });
        target.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { hideParams(); }
        });
        
        // Auto hide after 4 seconds
        Timer t = new Timer(4000, e -> hideParams());
        t.setRepeats(false);
        t.start();
        
        target.requestFocus();
    }
    
    private static void hideParams() {
        if (currentPopup != null) {
            currentPopup.dispose();
            currentPopup = null;
        }
    }
}
