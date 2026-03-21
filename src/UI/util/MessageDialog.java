package ui.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MessageDialog extends JDialog {

    public enum MessageType {
        SUCCESS, ERROR, INFO
    }

    public static void show(Component parent, String title, String message, MessageType type) {
        new MessageDialog(parent, title, message, type, "OK").setVisible(true);
    }

    public static void show(Component parent, String title, String message, MessageType type, String buttonText) {
        new MessageDialog(parent, title, message, type, buttonText).setVisible(true);
    }

    private MessageDialog(Component parent, String title, String message, MessageType type, String buttonText) {
        super(SwingUtilities.getWindowAncestor(parent), title, ModalityType.APPLICATION_MODAL);
        initUI(title, message, type, buttonText);
    }

    private void initUI(String title, String message, MessageType type, String buttonText) {
        setUndecorated(true);
        setSize(360, 200);
        setLocationRelativeTo(getParent());

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBackground(Color.WHITE);
        pnlMain.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 2),
            new EmptyBorder(25, 25, 25, 25)
        ));

        // Icon based on type
        JLabel lblIcon = new JLabel();
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        Color accentColor;
        
        switch (type) {
            case SUCCESS:
                lblIcon.setText("<html><span style='font-size: 40px; color: #22c55e;'>✔</span></html>");
                accentColor = new Color(34, 197, 94); // Green 500
                break;
            case ERROR:
                lblIcon.setText("<html><span style='font-size: 40px; color: #ef4444;'>✘</span></html>");
                accentColor = new Color(239, 68, 68); // Red 500
                break;
            default:
                lblIcon.setText("<html><span style='font-size: 40px; color: #3b82f6;'>ℹ</span></html>");
                accentColor = new Color(59, 130, 246); // Blue 500
                break;
        }
        
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.add(lblIcon, BorderLayout.CENTER);
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 18));
        lblTitle.setForeground(new Color(17, 24, 39));
        lblTitle.setBorder(new EmptyBorder(10, 0, 5, 0));
        pnlHeader.add(lblTitle, BorderLayout.SOUTH);
        
        pnlMain.add(pnlHeader, BorderLayout.NORTH);

        // Message body
        JLabel lblMessage = new JLabel("<html><div style='text-align: center; color: #6b7280; font-family: Inter;'>" + message + "</div></html>");
        lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
        pnlMain.add(lblMessage, BorderLayout.CENTER);

        // Button
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        RoundedButton btnOk = new RoundedButton(buttonText, 6);
        btnOk.setPreferredSize(new Dimension(140, 40));
        btnOk.setBackground(accentColor);
        btnOk.setForeground(Color.WHITE);
        btnOk.setFont(new Font("Inter", Font.BOLD, 14));
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOk.addActionListener(e -> dispose());
        btnOk.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnOk.setBackground(accentColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnOk.setBackground(accentColor);
            }
        });
        
        pnlBottom.add(btnOk);
        pnlMain.add(pnlBottom, BorderLayout.SOUTH);

        add(pnlMain);
    }
}
