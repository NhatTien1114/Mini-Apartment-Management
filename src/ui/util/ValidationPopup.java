package ui.util;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ValidationPopup {
    private static JWindow currentPopup;

    private static KeyAdapter currentKeyListener;
    private static MouseAdapter currentMouseListener;
    private static Timer currentTimer;

    public static void show(JComponent target, String message) {
        hide();

        Window parent = SwingUtilities.getWindowAncestor(target);
        if (parent == null)
            return;

        currentPopup = new JWindow(parent);

        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        pnl.setBackground(AppColors.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.SLATE_300, 1),
                new EmptyBorder(2, 4, 2, 8)));

        Icon warnIcon = new Icon() {
            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
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
        currentPopup.setLocation(loc.x + 10, loc.y + target.getHeight() + 4);
        currentPopup.setVisible(true);

        // Hide when typing or clicking — track listeners to remove later
        currentKeyListener = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                hide();
            }
        };
        currentMouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                hide();
            }
        };
        target.addKeyListener(currentKeyListener);
        target.addMouseListener(currentMouseListener);
        currentTarget = target;

        // Auto hide after 4 seconds
        currentTimer = new Timer(4000, e -> hide());
        currentTimer.setRepeats(false);
        currentTimer.start();
    }

    private static JComponent currentTarget;

    public static void hide() {
        if (currentTimer != null) {
            currentTimer.stop();
            currentTimer = null;
        }
        if (currentTarget != null) {
            if (currentKeyListener != null)
                currentTarget.removeKeyListener(currentKeyListener);
            if (currentMouseListener != null)
                currentTarget.removeMouseListener(currentMouseListener);
            currentKeyListener = null;
            currentMouseListener = null;
            currentTarget = null;
        }
        if (currentPopup != null) {
            currentPopup.dispose();
            currentPopup = null;
        }
    }
}
