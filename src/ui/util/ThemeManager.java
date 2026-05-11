package ui.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public final class ThemeManager {
    private static final ThemeManager INSTANCE = new ThemeManager();
    private static final String ORIGINAL_ICON_KEY = "originalIcon";

    private boolean darkMode = false;
    private final List<Runnable> listeners = new ArrayList<>();

    private final Map<Color, Color> lightToDarkBg = new HashMap<>();
    private final Map<Color, Color> darkToLightBg = new HashMap<>();
    private final Map<Color, Color> lightToDarkFg = new HashMap<>();
    private final Map<Color, Color> darkToLightFg = new HashMap<>();

    // IdentityHashMap dùng tham chiếu đối tượng làm key — tránh hash collision
    private final IdentityHashMap<ImageIcon, ImageIcon> iconCache = new IdentityHashMap<>();
    private final IdentityHashMap<ImageIcon, Boolean> tintDecisionCache = new IdentityHashMap<>();

    private ThemeManager() {
        addBgMapping(AppColors.APP_BACKGROUND, new Color(15, 23, 42));
        addBgMapping(Color.WHITE, new Color(30, 41, 59));
        addBgMapping(AppColors.MENU_BG, new Color(9, 14, 23));
        addBgMapping(AppColors.SLATE_50, new Color(24, 33, 50));
        addBgMapping(AppColors.SLATE_100, new Color(30, 41, 59));
        addBgMapping(AppColors.SLATE_200, new Color(51, 65, 85));
        addBgMapping(new Color(245, 245, 245), new Color(39, 50, 68));
        addBgMapping(new Color(248, 250, 252), new Color(24, 33, 50));
        addBgMapping(new Color(250, 250, 250), new Color(30, 41, 59));
        addBgMapping(new Color(239, 246, 255), new Color(30, 58, 95));
        addBgMapping(new Color(241, 245, 249), new Color(30, 41, 59));

        addFgMapping(AppColors.SLATE_900, new Color(241, 245, 249));
        addFgMapping(Color.BLACK, new Color(241, 245, 249));
        addFgMapping(new Color(15, 23, 42), new Color(241, 245, 249));
        addFgMapping(AppColors.SLATE_600, new Color(148, 163, 184));
        addFgMapping(AppColors.SLATE_500, new Color(148, 163, 184));
        addFgMapping(AppColors.SLATE_400, new Color(100, 116, 139));
        addFgMapping(new Color(100, 116, 139), new Color(148, 163, 184));
        addFgMapping(new Color(71, 85, 105), new Color(148, 163, 184));
        addFgMapping(new Color(100, 100, 100), new Color(160, 175, 195));
    }

    private void addBgMapping(Color light, Color dark) {
        lightToDarkBg.put(light, dark);
        darkToLightBg.put(dark, light);
    }

    private void addFgMapping(Color light, Color dark) {
        lightToDarkFg.put(light, dark);
        darkToLightFg.put(dark, light);
    }

    public static ThemeManager getInstance() { return INSTANCE; }
    public boolean isDarkMode() { return darkMode; }

    public void setDarkMode(boolean dark) {
        if (this.darkMode != dark) {
            this.darkMode = dark;
            iconCache.clear();
            tintDecisionCache.clear();
            for (Runnable l : listeners) l.run();
        }
    }

    public void toggle() { setDarkMode(!darkMode); }
    public void addListener(Runnable listener) { listeners.add(listener); }

    // ===== ICON TINTING =====

    public static Image tintImage(Image original, Color tint) {
        if (original == null) return null;
        int w = original.getWidth(null);
        int h = original.getHeight(null);
        if (w <= 0 || h <= 0) return original;

        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(original, 0, 0, null);
        g2.dispose();

        int tintRGB = (tint.getRed() << 16) | (tint.getGreen() << 8) | tint.getBlue();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int argb = bi.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha > 0) bi.setRGB(x, y, (alpha << 24) | tintRGB);
            }
        }
        return bi;
    }

    public ImageIcon getThemedIcon(ImageIcon original) {
        if (original == null) return null;
        if (!darkMode) return original;

        ImageIcon cached = iconCache.get(original);
        if (cached != null) return cached;

        Image tinted = tintImage(original.getImage(), new Color(220, 230, 245));
        ImageIcon result = new ImageIcon(tinted);
        iconCache.put(original, result);
        return result;
    }

    public ImageIcon loadThemedIcon(String path, int width, int height) {
        ImageIcon raw = new ImageIcon(path);
        if (raw.getIconWidth() <= 0 || raw.getIconHeight() <= 0) return null;
        Image scaled = raw.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        if (darkMode) {
            return new ImageIcon(tintImage(scaled, new Color(220, 230, 245)));
        }
        return new ImageIcon(scaled);
    }

    // ===== THEME APPLICATION =====

    public void applyTheme(Window window) {
        updateComponentTheme(window);
        window.repaint();
    }

    /** Overload cho phép áp dụng theme trên một component cụ thể (không cần toàn Window). */
    public void applyTheme(Component c) {
        updateComponentTheme(c);
        c.repaint();
    }

    public void updateComponentTheme(Component c) {
        if (c == null) return;

        if (c instanceof JButton) {
            JButton btn = (JButton) c;
            Color bg = btn.getBackground();
            if (bg == null || bg.equals(Color.WHITE) || bg.equals(AppColors.APP_BACKGROUND)) {
                if (c.isBackgroundSet()) c.setBackground(getMappedBg(c.getBackground()));
                if (c.isForegroundSet()) c.setForeground(getMappedFg(c.getForeground()));
            }
            if (btn.getIcon() instanceof ImageIcon) {
                applyIconTint(btn, (ImageIcon) btn.getIcon());
            }
        } else if (c instanceof JLabel) {
            if (c.isBackgroundSet()) c.setBackground(getMappedBg(c.getBackground()));
            if (c.isForegroundSet()) c.setForeground(getMappedFg(c.getForeground()));
            JLabel lbl = (JLabel) c;
            if (lbl.getIcon() instanceof ImageIcon) {
                applyIconTint(lbl, (ImageIcon) lbl.getIcon());
            }
        } else {
            if (c.isBackgroundSet()) c.setBackground(getMappedBg(c.getBackground()));
            if (c.isForegroundSet()) c.setForeground(getMappedFg(c.getForeground()));
        }

        if (c instanceof JTable) {
            JTable table = (JTable) c;
            if (table.getTableHeader() != null) updateComponentTheme(table.getTableHeader());
            table.setSelectionBackground(getMappedBg(table.getSelectionBackground()));
            table.setSelectionForeground(getMappedFg(table.getSelectionForeground()));
            table.setGridColor(getMappedBg(AppColors.SLATE_200));
            table.repaint();
        }

        if (c instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) c;
            if (scroll.getViewport() != null) {
                scroll.getViewport().setBackground(getMappedBg(scroll.getViewport().getBackground()));
            }
        }

        if (c instanceof JComboBox) {
            JComboBox<?> cb = (JComboBox<?>) c;
            for (int i = 0; i < cb.getComponentCount(); i++) updateComponentTheme(cb.getComponent(i));
        }

        if (c instanceof JPopupMenu) {
            for (Component child : ((JPopupMenu) c).getComponents()) updateComponentTheme(child);
        }

        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) updateComponentTheme(child);
        }
    }

    /** Áp dụng/hoàn tác tint icon cho JLabel hoặc JButton theo theme hiện tại. */
    private void applyIconTint(JComponent c, ImageIcon icon) {
        if (darkMode) {
            if (shouldTintIcon(icon)) {
                if (c.getClientProperty(ORIGINAL_ICON_KEY) == null) {
                    c.putClientProperty(ORIGINAL_ICON_KEY, icon);
                }
                ImageIcon themed = getThemedIcon(icon);
                if (c instanceof JLabel) ((JLabel) c).setIcon(themed);
                else if (c instanceof AbstractButton) ((AbstractButton) c).setIcon(themed);
            }
        } else {
            Object orig = c.getClientProperty(ORIGINAL_ICON_KEY);
            if (orig instanceof ImageIcon) {
                if (c instanceof JLabel) ((JLabel) c).setIcon((ImageIcon) orig);
                else if (c instanceof AbstractButton) ((AbstractButton) c).setIcon((ImageIcon) orig);
                c.putClientProperty(ORIGINAL_ICON_KEY, null);
            }
        }
    }

    private boolean shouldTintIcon(ImageIcon icon) {
        if (icon == null || icon.getIconWidth() <= 0) return false;
        if (!darkMode) return false;

        Boolean cached = tintDecisionCache.get(icon);
        if (cached != null) return cached;

        Image img = icon.getImage();
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (w <= 0 || h <= 0 || w > 64 || h > 64) {
            tintDecisionCache.put(icon, false);
            return false;
        }

        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();

        int darkPixels = 0, totalVisible = 0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int argb = bi.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha > 50) {
                    totalVisible++;
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    if ((0.299 * r + 0.587 * g + 0.114 * b) / 255.0 < 0.4) darkPixels++;
                }
            }
        }

        boolean result = totalVisible > 0 && (double) darkPixels / totalVisible > 0.5;
        tintDecisionCache.put(icon, result);
        return result;
    }

    private Color getMappedBg(Color c) {
        if (c == null) return null;
        return darkMode ? lightToDarkBg.getOrDefault(c, c) : darkToLightBg.getOrDefault(c, c);
    }

    private Color getMappedFg(Color c) {
        if (c == null) return null;
        if (darkMode) {
            if (lightToDarkFg.containsKey(c)) return lightToDarkFg.get(c);
            double lum = (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255;
            return lum < 0.4 ? new Color(241, 245, 249) : c;
        } else {
            if (darkToLightFg.containsKey(c)) return darkToLightFg.get(c);
            if (c.equals(new Color(241, 245, 249))) return AppColors.SLATE_900;
            double lum = (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255;
            return lum > 0.7 ? AppColors.SLATE_900 : c;
        }
    }

    // ===== THEME COLOR ACCESSORS =====
    public Color getAppBackground()  { return getMappedBg(AppColors.APP_BACKGROUND); }
    public Color getMenuBg()         { return getMappedBg(AppColors.MENU_BG); }
    public Color getMenuHover()      { return getMappedBg(AppColors.MENU_HOVER); }
    public Color getHeaderBg()       { return getMappedBg(Color.WHITE); }
    public Color getCardBg()         { return getMappedBg(Color.WHITE); }
    public Color getTextPrimary()    { return getMappedFg(AppColors.SLATE_900); }
    public Color getTextSecondary()  { return getMappedFg(AppColors.SLATE_500); }
    public Color getBorderColor()    { return getMappedBg(AppColors.SLATE_200); }
    public Color getTableBg()        { return getMappedBg(Color.WHITE); }
    public Color getTableHeaderBg()  { return getMappedBg(new Color(248, 250, 252)); }
    public Color getTableHoverBg()   { return getMappedBg(AppColors.SLATE_50); }
    public Color getInputBg()        { return getMappedBg(Color.WHITE); }
    public Color getSlate100()       { return getMappedBg(AppColors.SLATE_100); }
}
