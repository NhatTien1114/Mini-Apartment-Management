package ui.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

public final class ButtonStyles {
    private ButtonStyles() {
    }

    public static RoundedButton createSecondary(String text, Font font, Color textColor, Color normal, Color hover,
            int radius, Border border) {
        RoundedButton button = new RoundedButton(text, radius);
        button.setFont(font);
        button.setBackground(normal);
        button.setForeground(textColor);
        button.setBorder(border == null ? BorderFactory.createEmptyBorder(10, 20, 10, 20) : border);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        applyHover(button, normal, hover);
        bindEnterToClick(button);
        return button;
    }

    public static void applyHover(RoundedButton button, Color normal, Color hover) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normal);
            }
        });
    }

    public static void bindEnterToClick(AbstractButton button) {
        button.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    button.doClick();
                }
            }
        });
    }
}
