package ui.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

/**
 * Quản lý Dark Mode / Light Mode cho toàn ứng dụng.
 * Có chức năng tự động cập nhật màu sắc các component đệ quy.
 */
public final class ThemeManager {
    private static final ThemeManager INSTANCE = new ThemeManager();
    private boolean darkMode = false;
    private final List<Runnable> listeners = new ArrayList<>();

    private final Map<Color, Color> lightToDarkBg = new HashMap<>();
    private final Map<Color, Color> darkToLightBg = new HashMap<>();

    private final Map<Color, Color> lightToDarkFg = new HashMap<>();
    private final Map<Color, Color> darkToLightFg = new HashMap<>();

    // Cache for tinted icons
    private final Map<String, ImageIcon> iconCache = new HashMap<>();

    private ThemeManager() {
        // --- BACKGROUND MAPPINGS ---
        // Nền app chính
        addBgMapping(AppColors.APP_BACKGROUND, new Color(15, 23, 42)); // Slate 900

        // Nền Card/Panel chính (Trắng -> Đen nhạt)
        addBgMapping(Color.WHITE, new Color(30, 41, 59)); // Slate 800

        // Nền Menu (Xanh đậm -> Đen đậm hơn)
        addBgMapping(AppColors.MENU_BG, new Color(9, 14, 23));

        // Các sắc độ Slate sáng (dùng cho hover, header bảng)
        addBgMapping(AppColors.SLATE_50, new Color(24, 33, 50));
        addBgMapping(AppColors.SLATE_100, new Color(30, 41, 59));
        addBgMapping(AppColors.SLATE_200, new Color(51, 65, 85));
        addBgMapping(new Color(245, 245, 245), new Color(39, 50, 68)); // User hover
        addBgMapping(new Color(248, 250, 252), new Color(24, 33, 50)); // Table header
        addBgMapping(new Color(250, 250, 250), new Color(30, 41, 59));

        // Thêm mapping cho các màu nền thường gặp
        addBgMapping(new Color(239, 246, 255), new Color(30, 58, 95)); // Selection blue light -> dark blue
        addBgMapping(new Color(241, 245, 249), new Color(30, 41, 59)); // Slate 100 exact

        // --- FOREGROUND MAPPINGS ---
        // Text chính
        addFgMapping(AppColors.SLATE_900, new Color(241, 245, 249)); // Slate 100
        addFgMapping(Color.BLACK, new Color(241, 245, 249));
        addFgMapping(new Color(15, 23, 42), new Color(241, 245, 249));

        // Text phụ
        addFgMapping(AppColors.SLATE_600, new Color(148, 163, 184)); // Slate 400
        addFgMapping(AppColors.SLATE_500, new Color(148, 163, 184));
        addFgMapping(AppColors.SLATE_400, new Color(100, 116, 139));
        addFgMapping(new Color(100, 116, 139), new Color(148, 163, 184));
        addFgMapping(new Color(71, 85, 105), new Color(148, 163, 184));

        // Text xám nhạt (cho labels phụ)
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

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean dark) {
        if (this.darkMode != dark) {
            this.darkMode = dark;
            iconCache.clear(); // Clear icon cache on theme change
            for (Runnable l : listeners) {
                l.run();
            }
        }
    }

    public void toggle() {
        setDarkMode(!darkMode);
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    // ===== ICON TINTING =====

    /**
     * Tint một Image sang màu chỉ định, giữ nguyên alpha channel.
     */
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
                if (alpha > 0) {
                    bi.setRGB(x, y, (alpha << 24) | tintRGB);
                }
            }
        }
        return bi;
    }

    /**
     * Trả về icon đã tint trắng nếu đang dark mode, ngược lại trả về icon gốc.
     * Sử dụng cache để tránh tint lại nhiều lần.
     */
    public ImageIcon getThemedIcon(ImageIcon original) {
        if (original == null) return null;
        if (!darkMode) return original;

        String key = String.valueOf(System.identityHashCode(original));
        if (iconCache.containsKey(key)) {
            return iconCache.get(key);
        }

        Image tinted = tintImage(original.getImage(), new Color(220, 230, 245));
        ImageIcon result = new ImageIcon(tinted);
        iconCache.put(key, result);
        return result;
    }

    /**
     * Tint icon từ file path, trả về phiên bản phù hợp với theme hiện tại.
     */
    public ImageIcon loadThemedIcon(String path, int width, int height) {
        ImageIcon raw = new ImageIcon(path);
        if (raw.getIconWidth() <= 0 || raw.getIconHeight() <= 0) return null;
        Image scaled = raw.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaled);
        if (darkMode) {
            Image tinted = tintImage(scaled, new Color(220, 230, 245));
            return new ImageIcon(tinted);
        }
        return scaledIcon;
    }

    // ===== UI UPDATER TỰ ĐỘNG =====

    /**
     * Tự động quét toàn bộ cây component và đổi màu nền, màu chữ theo map
     */
    public void applyTheme(Window window) {
        updateComponentTheme(window);
        window.repaint();
    }

    public void updateComponentTheme(Component c) {
        if (c == null)
            return;

        // Bỏ qua nếu là component đặc biệt mà ta không muốn đổi màu nền
        // Ví dụ: PrimaryButton (màu xanh dương vẫn để nguyên)
        if (c instanceof JButton) {
            JButton btn = (JButton) c;
            Color bg = btn.getBackground();
            // Nếu nút màu xanh dương hoặc màu đặc biệt (không phải trắng/xám), giữ nguyên.
            if (bg != null && !bg.equals(Color.WHITE) && !bg.equals(AppColors.APP_BACKGROUND)) {
                // Chỉ đổi màu text nếu cần
            } else {
                if (c.isBackgroundSet())
                    c.setBackground(getMappedBg(c.getBackground()));
                if (c.isForegroundSet())
                    c.setForeground(getMappedFg(c.getForeground()));
            }
            // Tint icon trên JButton
            if (btn.getIcon() instanceof ImageIcon) {
                ImageIcon icon = (ImageIcon) btn.getIcon();
                if (shouldTintIcon(icon)) {
                    btn.setIcon(getThemedIcon(icon));
                }
            }
        } else if (c instanceof JLabel) {
            // Components thông thường
            if (c.isBackgroundSet())
                c.setBackground(getMappedBg(c.getBackground()));
            if (c.isForegroundSet())
                c.setForeground(getMappedFg(c.getForeground()));
            // Tint icon trên JLabel
            JLabel lbl = (JLabel) c;
            if (lbl.getIcon() instanceof ImageIcon) {
                ImageIcon icon = (ImageIcon) lbl.getIcon();
                if (shouldTintIcon(icon)) {
                    lbl.setIcon(getThemedIcon(icon));
                }
            }
        } else {
            // Components thông thường
            if (c.isBackgroundSet())
                c.setBackground(getMappedBg(c.getBackground()));
            if (c.isForegroundSet())
                c.setForeground(getMappedFg(c.getForeground()));
        }

        // Xử lý JTable
        if (c instanceof JTable) {
            JTable table = (JTable) c;
            if (table.getTableHeader() != null) {
                updateComponentTheme(table.getTableHeader());
            }
            table.setSelectionBackground(getMappedBg(table.getSelectionBackground()));
            table.setSelectionForeground(getMappedFg(table.getSelectionForeground()));
            table.setGridColor(getMappedBg(AppColors.SLATE_200)); // Đổi màu viền bảng
            table.repaint();
        }

        // Xử lý JScrollPane
        if (c instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) c;
            if (scroll.getViewport() != null) {
                scroll.getViewport().setBackground(getMappedBg(scroll.getViewport().getBackground()));
            }
        }

        // Xử lý JComboBox
        if (c instanceof JComboBox) {
            JComboBox<?> cb = (JComboBox<?>) c;
            for (int i = 0; i < cb.getComponentCount(); i++) {
                updateComponentTheme(cb.getComponent(i));
            }
        }

        // Xử lý JPopupMenu
        if (c instanceof JPopupMenu) {
            JPopupMenu menu = (JPopupMenu) c;
            for (Component child : menu.getComponents()) {
                updateComponentTheme(child);
            }
        }

        // Đệ quy
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                updateComponentTheme(child);
            }
        }
    }

    /**
     * Kiểm tra xem icon có cần tint không (icon tối trên nền tối sẽ bị che).
     * Chỉ tint nếu icon có pixel tối chiếm đa số.
     */
    private boolean shouldTintIcon(ImageIcon icon) {
        if (icon == null || icon.getIconWidth() <= 0) return false;
        // Trong dark mode, luôn tint các icon nhỏ (thường là icon UI đen)
        if (darkMode) {
            Image img = icon.getImage();
            int w = img.getWidth(null);
            int h = img.getHeight(null);
            if (w <= 0 || h <= 0 || w > 64 || h > 64) return false; // Chỉ tint icon nhỏ

            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bi.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();

            int darkPixels = 0;
            int totalVisible = 0;
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int argb = bi.getRGB(x, y);
                    int alpha = (argb >> 24) & 0xFF;
                    if (alpha > 50) {
                        totalVisible++;
                        int r = (argb >> 16) & 0xFF;
                        int g = (argb >> 8) & 0xFF;
                        int b = argb & 0xFF;
                        double lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
                        if (lum < 0.4) darkPixels++;
                    }
                }
            }
            // Tint nếu phần lớn pixel là tối
            return totalVisible > 0 && (double) darkPixels / totalVisible > 0.5;
        }
        return false;
    }

    private Color getMappedBg(Color c) {
        if (c == null)
            return null;
        if (darkMode) {
            return lightToDarkBg.getOrDefault(c, c);
        } else {
            return darkToLightBg.getOrDefault(c, c);
        }
    }

    private Color getMappedFg(Color c) {
        if (c == null)
            return null;
        if (darkMode) {
            if (lightToDarkFg.containsKey(c)) {
                return lightToDarkFg.get(c);
            }
            // Heuristic: Nếu màu chữ quá tối trên nền dark mode -> chuyển sang sáng
            double luminance = (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255;
            if (luminance < 0.4) {
                return new Color(241, 245, 249); // Màu trắng sáng
            }
            return c;
        } else {
            if (darkToLightFg.containsKey(c)) {
                return darkToLightFg.get(c);
            }
            // Heuristic: Nếu màu chữ sáng (do dark mode chuyển lại) -> chuyển về tối
            if (c.equals(new Color(241, 245, 249))) {
                return AppColors.SLATE_900;
            }
            // Chuyển lại các màu sáng khác
            double luminance = (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255;
            if (luminance > 0.7) {
                return AppColors.SLATE_900;
            }
            return c;
        }
    }

    // ===== THEME COLORS CŨ (CHO TƯƠNG THÍCH CODE CŨ) =====
    public Color getAppBackground() {
        return getMappedBg(AppColors.APP_BACKGROUND);
    }

    public Color getMenuBg() {
        return getMappedBg(AppColors.MENU_BG);
    }

    public Color getMenuHover() {
        return getMappedBg(AppColors.MENU_HOVER);
    }

    public Color getHeaderBg() {
        return getMappedBg(Color.WHITE);
    }

    public Color getCardBg() {
        return getMappedBg(Color.WHITE);
    }

    public Color getTextPrimary() {
        return getMappedFg(AppColors.SLATE_900);
    }

    public Color getTextSecondary() {
        return getMappedFg(AppColors.SLATE_500);
    }

    public Color getBorderColor() {
        return getMappedBg(AppColors.SLATE_200);
    }

    public Color getTableBg() {
        return getMappedBg(Color.WHITE);
    }

    public Color getTableHeaderBg() {
        return getMappedBg(new Color(248, 250, 252));
    }

    public Color getTableHoverBg() {
        return getMappedBg(AppColors.SLATE_50);
    }

    public Color getInputBg() {
        return getMappedBg(Color.WHITE);
    }

    public Color getSlate100() {
        return getMappedBg(AppColors.SLATE_100);
    }
}
