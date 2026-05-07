package ui.util;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public class ModernComboBoxUI extends BasicComboBoxUI {

    private static final Color BG            = Color.WHITE;
    private static final Color DISABLED_BG   = new Color(248, 250, 252);
    private static final Color BORDER_NORMAL = new Color(203, 213, 225);
    private static final Color BORDER_FOCUS  = new Color(59, 130, 246);
    private static final Color FG            = new Color(15, 23, 42);
    private static final Color FG_DISABLED   = new Color(148, 163, 184);
    private static final Color ARROW_BG      = new Color(59, 130, 246);
    private static final Color ARROW_BG_DIS  = new Color(148, 163, 184);
    private static final Color CLEAR_FG      = new Color(100, 116, 139);
    private static final Color CLEAR_HOVER   = new Color(226, 232, 240);
    private static final Color LIST_BG       = Color.WHITE;
    private static final Color SEL_BG        = new Color(239, 246, 255);
    private static final Color SEL_FG        = new Color(37, 99, 235);
    private static final int   ARROW_W       = 32;
    private static final int   CLEAR_SIZE    = 18;
    private static final int   CLEAR_GAP     = 4; // gap between clear button and arrow

    private JButton clearButton;

    public static ComponentUI createUI(JComponent c) {
        return new ModernComboBoxUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        comboBox.setBackground(BG);
        comboBox.setForeground(FG);
        comboBox.setBorder(new EmptyBorder(0, 0, 0, 0));
        comboBox.setOpaque(false);
        comboBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void installComponents() {
        super.installComponents();

        clearButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(CLEAR_HOVER);
                    g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                }
                g2.setColor(CLEAR_FG);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int p = 5;
                g2.drawLine(p, p, getWidth() - p - 1, getHeight() - p - 1);
                g2.drawLine(getWidth() - p - 1, p, p, getHeight() - p - 1);
                g2.dispose();
            }
        };
        clearButton.setOpaque(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setBorderPainted(false);
        clearButton.setFocusPainted(false);
        clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearButton.setVisible(false);
        // X resets to first item (index 0), not to -1
        clearButton.addActionListener(e -> {
            if (comboBox.getItemCount() > 0)
                comboBox.setSelectedIndex(0);
        });
        comboBox.add(clearButton);

        comboBox.addItemListener(e -> updateClearButton());
        updateClearButton();
    }

    @Override
    protected void uninstallComponents() {
        if (clearButton != null) {
            comboBox.remove(clearButton);
            clearButton = null;
        }
        super.uninstallComponents();
    }

    /**
     * X hiện chỉ khi user chọn item khác item đầu tiên (index > 0).
     * Với combobox filter "Tất cả" ở index 0, X sẽ không hiện khi đang chọn mặc định.
     */
    private void updateClearButton() {
        if (clearButton == null) return;
        boolean show = comboBox.getSelectedIndex() > 0;
        boolean wasVisible = clearButton.isVisible();
        clearButton.setVisible(show);
        if (wasVisible != show) {
            comboBox.revalidate();
            comboBox.repaint();
        }
    }

    @Override
    protected JButton createArrowButton() {
        // Transparent click-target; visuals drawn in paint()
        JButton btn = new JButton();
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        return btn;
    }

    @Override
    public LayoutManager createLayoutManager() {
        return new LayoutManager() {
            @Override public void addLayoutComponent(String name, Component comp) {}
            @Override public void removeLayoutComponent(Component comp) {}
            @Override public Dimension preferredLayoutSize(Container parent) { return parent.getPreferredSize(); }
            @Override public Dimension minimumLayoutSize(Container parent) { return parent.getMinimumSize(); }

            @Override
            public void layoutContainer(Container parent) {
                int w = parent.getWidth(), h = parent.getHeight();
                boolean showClear = clearButton != null && clearButton.isVisible();

                // Arrow button covers the right ARROW_W pixels (transparent click target)
                if (arrowButton != null)
                    arrowButton.setBounds(w - ARROW_W, 0, ARROW_W, h);

                // Clear button positioned to the left of arrow area
                if (clearButton != null) {
                    if (showClear) {
                        int x = w - ARROW_W - CLEAR_GAP - CLEAR_SIZE;
                        int y = (h - CLEAR_SIZE) / 2;
                        clearButton.setBounds(x, y, CLEAR_SIZE, CLEAR_SIZE);
                    } else {
                        clearButton.setBounds(0, 0, 0, 0);
                    }
                }

                // Editor (editable combos only)
                if (editor != null) {
                    int textRight = w - ARROW_W - (showClear ? CLEAR_GAP + CLEAR_SIZE + 4 : 0) - 4;
                    editor.setBounds(10, 2, textRight - 10, h - 4);
                }
            }
        };
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        hasFocus = comboBox.hasFocus();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = c.getWidth(), h = c.getHeight();
        boolean showClear = clearButton != null && clearButton.isVisible();

        // ── Background ──
        g2.setColor(comboBox.isEnabled() ? BG : DISABLED_BG);
        g2.fillRoundRect(1, 1, w - 2, h - 2, 8, 8);

        // ── Blue arrow area, clipped to rounded shape ──
        Shape roundClip = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 8, 8);
        g2.setClip(roundClip);
        g2.setColor(comboBox.isEnabled() ? ARROW_BG : ARROW_BG_DIS);
        g2.fillRect(w - ARROW_W, 0, ARROW_W, h);
        g2.setClip(null);

        // ── Border ──
        g2.setColor(hasFocus ? BORDER_FOCUS : BORDER_NORMAL);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

        // ── Chevron ──
        int cx = w - ARROW_W / 2, cy = h / 2;
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
        g2.drawLine(cx, cy + 2, cx + 4, cy - 2);

        // ── Selected item text (drawn manually to bypass L&F renderer) ──
        if (!comboBox.isEditable()) {
            Object sel = comboBox.getSelectedItem();
            String text = (sel != null) ? sel.toString() : "";
            if (!text.isEmpty()) {
                Font f = comboBox.getFont();
                if (f == null) f = new Font("Dialog", Font.PLAIN, 13);
                g2.setFont(f);
                g2.setColor(comboBox.isEnabled() ? FG : FG_DISABLED);
                FontMetrics fm = g2.getFontMetrics();

                int textLeft = 12;
                int textRight = w - ARROW_W - 4
                        - (showClear ? CLEAR_GAP + CLEAR_SIZE + 4 : 0);
                int maxWidth = textRight - textLeft;

                // Truncate with ellipsis if too wide
                if (fm.stringWidth(text) > maxWidth && maxWidth > 0) {
                    while (text.length() > 0 && fm.stringWidth(text + "…") > maxWidth)
                        text = text.substring(0, text.length() - 1);
                    text = text + "…";
                }

                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, textLeft, ty);
            }
        }

        g2.dispose();
    }

    // Not needed — we draw text in paint() directly
    @Override
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {}

    @Override
    public Rectangle rectangleForCurrentValue() {
        // Not used (text drawn in paint()), but required by base class
        return new Rectangle(0, 0, 0, 0);
    }

    @Override
    protected ComboPopup createPopup() {
        return new BasicComboPopup(comboBox) {

            @Override
            protected JScrollPane createScroller() {
                JScrollPane sp = super.createScroller();
                sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
                sp.getVerticalScrollBar().setPreferredSize(new Dimension(8, Integer.MAX_VALUE));
                sp.setBorder(null);
                return sp;
            }

            @SuppressWarnings({"rawtypes", "unchecked"})
            @Override
            protected void configureList() {
                super.configureList();
                list.setBackground(LIST_BG);
                list.setSelectionBackground(SEL_BG);
                list.setSelectionForeground(SEL_FG);
                list.setFixedCellHeight(36);
                list.setBorder(new EmptyBorder(4, 0, 4, 0));
                list.setCellRenderer(new ListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList lst, Object value,
                            int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel lbl = new JLabel(value != null ? value.toString() : "");
                        lbl.setFont(comboBox.getFont() != null
                                ? comboBox.getFont()
                                : new Font("Dialog", Font.PLAIN, 13));
                        lbl.setBackground(isSelected ? SEL_BG : LIST_BG);
                        lbl.setForeground(isSelected ? SEL_FG : FG);
                        lbl.setBorder(new EmptyBorder(0, 14, 0, 14));
                        lbl.setOpaque(true);
                        return lbl;
                    }
                });
            }

            @Override
            protected void configurePopup() {
                super.configurePopup();
                setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
                setBackground(LIST_BG);
            }
        };
    }
}
