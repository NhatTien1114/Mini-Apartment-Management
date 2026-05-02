package ui.util;

import java.awt.*;
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
