package ui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public final class FormFieldStyles {
    private FormFieldStyles() {
    }

    public static JPanel createLabeledField(String label, JComponent component, Color labelColor, Font labelFont, int inputHeight) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(labelFont);
        lbl.setForeground(labelColor);

        component.setPreferredSize(new Dimension(0, inputHeight));

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    public static JComboBox<String> createRoomCombo(String[] roomOptions, Font font, Color textColor, Color borderColor) {
        JComboBox<String> combo = new JComboBox<>(roomOptions);
        combo.setFont(font);
        combo.setForeground(textColor);
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor),
            new EmptyBorder(4, 8, 4, 8)
        ));
        return combo;
    }
}
