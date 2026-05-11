package main;

import java.awt.Color;
import javax.swing.SwingUtilities;
import ui.main.LoginUI;

/**
 * Điểm khởi động duy nhất của ứng dụng.
 * Cấu hình UIManager toàn cục và mở màn hình đăng nhập.
 */
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        configureUIManager();
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }

    private static void configureUIManager() {
        // Java 9+ module fix: đảm bảo UIManager tìm được các UI class tùy chỉnh
        javax.swing.UIManager.put("ClassLoader", Thread.currentThread().getContextClassLoader());

        // Scrollbar hiện đại toàn cục
        javax.swing.UIManager.put("ScrollBarUI",         "ui.util.ModernScrollBarUI");
        javax.swing.UIManager.put("ScrollBar.width",     8);
        javax.swing.UIManager.put("ScrollBar.track",     new Color(241, 245, 249));
        javax.swing.UIManager.put("ScrollBar.thumb",     new Color(148, 163, 184));
        javax.swing.UIManager.put("ScrollBar.thumbShadow",     new Color(0, 0, 0, 0));
        javax.swing.UIManager.put("ScrollBar.thumbHighlight",  new Color(0, 0, 0, 0));
        javax.swing.UIManager.put("ScrollBar.thumbDarkShadow", new Color(0, 0, 0, 0));

        // ComboBox hiện đại toàn cục
        javax.swing.UIManager.put("ComboBoxUI",                   "ui.util.ModernComboBoxUI");
        javax.swing.UIManager.put("ComboBox.background",          Color.WHITE);
        javax.swing.UIManager.put("ComboBox.foreground",          new Color(15, 23, 42));
        javax.swing.UIManager.put("ComboBox.selectionBackground", new Color(239, 246, 255));
        javax.swing.UIManager.put("ComboBox.selectionForeground", new Color(37, 99, 235));
        javax.swing.UIManager.put("ComboBox.disabledBackground",  new Color(248, 250, 252));

        // Tắt focus ring cho Button và ComboBox
        javax.swing.UIManager.put("Button.focus",   new Color(0, 0, 0, 0));
        javax.swing.UIManager.put("ComboBox.focus", new Color(0, 0, 0, 0));
    }
}
