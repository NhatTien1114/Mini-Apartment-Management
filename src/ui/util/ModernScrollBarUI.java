package ui.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class ModernScrollBarUI extends BasicScrollBarUI {

    private static final Color THUMB_NORMAL = new Color(148, 163, 184);
    private static final Color THUMB_HOVER  = new Color(100, 116, 139);
    private static final Color TRACK_COLOR  = new Color(241, 245, 249);

    public static ComponentUI createUI(JComponent c) {
        return new ModernScrollBarUI();
    }

    @Override
    protected void configureScrollBarColors() {
        thumbColor = THUMB_NORMAL;
        trackColor = TRACK_COLOR;
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return zeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return zeroButton();
    }

    private JButton zeroButton() {
        JButton btn = new JButton();
        Dimension d = new Dimension(0, 0);
        btn.setPreferredSize(d);
        btn.setMinimumSize(d);
        btn.setMaximumSize(d);
        btn.setVisible(false);
        return btn;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(TRACK_COLOR);
        g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        g2.dispose();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean active = isDragging || isThumbRollover();
        g2.setColor(active ? THUMB_HOVER : THUMB_NORMAL);
        int pad = 2;
        g2.fillRoundRect(
            thumbBounds.x + pad,
            thumbBounds.y + pad,
            thumbBounds.width  - pad * 2,
            thumbBounds.height - pad * 2,
            8, 8);
        g2.dispose();
    }
}
