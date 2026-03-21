package ui.main;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import ui.util.AppColors;

public class QuanLyPhongUI {
    private final Color MAU_NEN      = AppColors.SLATE_100;
    private final Color MAU_CARD     = AppColors.WHITE;
    private final Color MAU_BORDER   = AppColors.SLATE_200;
    private final Color MAU_TEXT     = AppColors.SLATE_900;
    private final Color MAU_MUTED    = AppColors.SLATE_500;
    private final Color MAU_PRIMARY  = AppColors.PRIMARY;
    private final Color MAU_PRIMARY_DISABLED = AppColors.PRIMARY_DISABLED;
    private final Color MAU_RED      = AppColors.RED_500;
    private final Color MAU_AMBER_BG = AppColors.AMBER_BG;
    private final Color MAU_AMBER_FG = AppColors.AMBER_FG;
    private final Color MAU_GREEN_BG = AppColors.GREEN_BG;
    private final Color MAU_GREEN_FG = AppColors.GREEN_600;

    private final Font FONT_TITLE = new Font("Be Vietnam Pro", Font.BOLD, 22);
    private final Font FONT_BOLD  = new Font("Be Vietnam Pro", Font.BOLD, 13);
    private final Font FONT_PLAIN = new Font("Be Vietnam Pro", Font.PLAIN, 13);
    private final Font FONT_SMALL = new Font("Be Vietnam Pro", Font.PLAIN, 12);

    public JPanel getPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(MAU_NEN);
        root.setBorder(new EmptyBorder(12, 16, 16, 16));

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(MAU_NEN);
        container.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel contentCard = new JPanel(new BorderLayout());
        contentCard.setBackground(MAU_CARD);
        contentCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MAU_BORDER),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JPanel floorsPanel = new JPanel();
        floorsPanel.setBackground(MAU_CARD);
        floorsPanel.setLayout(new BoxLayout(floorsPanel, BoxLayout.Y_AXIS));

        floorsPanel.add(createFloorSection("Tầng 6", new String[] {"T6.01", "T6.02", "T6.03", "T6.04"}));
        floorsPanel.add(Box.createVerticalStrut(14));
        floorsPanel.add(createFloorSection("Tầng 5", new String[] {"T5.01", "T5.02", "T5.03", "T5.04", "T5.05"}));
        floorsPanel.add(Box.createVerticalStrut(14));
        floorsPanel.add(createFloorSection("Tầng 4", new String[] {"T4.01", "T4.02", "T4.03", "T4.04", "T4.05"}));
        floorsPanel.add(Box.createVerticalStrut(14));
        floorsPanel.add(createFloorSection("Tầng 3", new String[] {"T3.01", "T3.02", "T3.03", "T3.04", "T3.05"}));
        floorsPanel.add(Box.createVerticalStrut(14));
        floorsPanel.add(createFloorSection("Tầng 2", new String[] {"T2.01", "T2.02", "T2.03", "T2.04", "T2.05", "T2.06"}));
        floorsPanel.add(Box.createVerticalStrut(14));
        floorsPanel.add(createFloorSection("Tầng 1", new String[] {"T1.01", "T1.02", "T1.03", "T1.04", "T1.05"}));

        JScrollPane scrollPane = new JScrollPane(floorsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.getViewport().setBackground(MAU_CARD);

        contentCard.add(scrollPane, BorderLayout.CENTER);
        container.add(contentCard, BorderLayout.CENTER);
        root.add(container, BorderLayout.CENTER);

        return root;
    }

    private JPanel createFloorSection(String floorName, String[] rooms) {
        JPanel section = new JPanel(new BorderLayout(0, 8));
        section.setOpaque(false);

        JLabel lblFloor = new JLabel(floorName);
        lblFloor.setFont(FONT_BOLD.deriveFont(20f));
        lblFloor.setForeground(MAU_TEXT);
        section.add(lblFloor, BorderLayout.NORTH);

        JPanel roomsWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        roomsWrap.setOpaque(false);

        for (String room : rooms) {
            roomsWrap.add(createRoomCard(room));
        }
        section.add(roomsWrap, BorderLayout.CENTER);

        return section;
    }

    private JPanel createRoomCard(String roomCode) {
        final String status = getRoomStatus(roomCode);

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(MAU_CARD);
        card.setPreferredSize(new Dimension(218, 116));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MAU_BORDER),
            new EmptyBorder(10, 10, 8, 10)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblRoom = new JLabel(roomCode);
        lblRoom.setFont(FONT_BOLD.deriveFont(19f));
        lblRoom.setForeground(MAU_TEXT);

        JLabel lblStatus = createStatusBadge(status);

        header.add(lblRoom, BorderLayout.WEST);
        header.add(lblStatus, BorderLayout.EAST);

        JLabel lblPrice = new JLabel("Giá: 3,000,000đ");
        lblPrice.setFont(FONT_SMALL);
        lblPrice.setForeground(MAU_MUTED);

        JButton btnSetting = createSettingButton();
        btnSetting.addActionListener(e -> showSettingDialog(roomCode, "Đã thuê".equals(status)));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(lblPrice);
        body.add(Box.createVerticalStrut(8));
        body.add(btnSetting);

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JLabel createStatusBadge(String text) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(FONT_BOLD.deriveFont(11f));
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(3, 9, 3, 9));

        switch (text) {
            case "Đã thuê" -> {
                badge.setForeground(Color.WHITE);
                badge.setBackground(MAU_RED);
            }
            case "Đang sửa" -> {
                badge.setForeground(MAU_AMBER_FG);
                badge.setBackground(MAU_AMBER_BG);
            }
            default -> {
                badge.setForeground(MAU_GREEN_FG);
                badge.setBackground(MAU_GREEN_BG);
            }
        }
        return badge;
    }

    private String getRoomStatus(String roomCode) {
        if ("T6.01".equals(roomCode)) {
            return "Đã thuê";
        }
        if ("T5.03".equals(roomCode)) {
            return "Đang sửa";
        }
        return "Trống";
    }

    private JButton createSettingButton() {
        JButton button = new JButton("Cài đặt");
        button.setFont(FONT_PLAIN);
        button.setForeground(MAU_TEXT);
        button.setBackground(MAU_NEN);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MAU_BORDER),
            new EmptyBorder(5, 10, 5, 10)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void showSettingDialog(String roomCode, boolean daThue) {
        Window parentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        JDialog dialog = new JDialog(parentWindow, "Cài đặt phòng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(420, 560);
        dialog.setLocationRelativeTo(parentWindow);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(MAU_CARD);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Cài đặt phòng " + roomCode);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(MAU_TEXT);

        JButton btnClose = new JButton("x");
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setForeground(MAU_MUTED);
        btnClose.setFont(FONT_BOLD.deriveFont(16f));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JTextField txtGiaThue = createTextField("3000000");
        JComboBox<String> cboTrangThai = createCombo(new String[] {"Trống", "Đã thuê", "Đang sửa"});
        cboTrangThai.setSelectedItem(daThue ? "Đã thuê" : "Trống");

        JTextField txtDienCu = createTextField("4");
        JTextField txtDienMoi = createTextField("4");
        JTextField txtNuocCu = createTextField("4");
        JTextField txtNuocMoi = createTextField("4");

        form.add(createField("Giá thuê (VNĐ/tháng)", txtGiaThue));
        form.add(Box.createVerticalStrut(8));
        form.add(createField("Trạng thái", cboTrangThai));
        form.add(Box.createVerticalStrut(10));

        JPanel gridSo = new JPanel(new GridLayout(2, 2, 10, 8));
        gridSo.setOpaque(false);
        gridSo.add(createField("Số điện cũ", txtDienCu));
        gridSo.add(createField("Số điện mới", txtDienMoi));
        gridSo.add(createField("Số nước cũ", txtNuocCu));
        gridSo.add(createField("Số nước mới", txtNuocMoi));
        form.add(gridSo);

        form.add(Box.createVerticalStrut(12));
        JLabel lblDv = new JLabel("Dịch vụ sử dụng");
        lblDv.setFont(FONT_PLAIN);
        lblDv.setForeground(MAU_MUTED);
        form.add(lblDv);
        form.add(Box.createVerticalStrut(6));

        form.add(createServiceCheck("Điện - 3,500đ/KWh", true));
        form.add(createServiceCheck("Nước - 15,000đ/m3", true));
        form.add(createServiceCheck("Internet - 100,000đ/tháng", true));
        form.add(createServiceCheck("Rác - 50,000đ/tháng", true));
        form.add(createServiceCheck("Giữ xe máy - 100,000đ/tháng", true));
        form.add(createServiceCheck("Giữ ô tô - 500,000đ/tháng", false));

        form.add(Box.createVerticalGlue());

        JButton btnSave = new JButton("Lưu");
        btnSave.setFont(FONT_BOLD);
        btnSave.setBackground(MAU_PRIMARY);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorder(new EmptyBorder(10, 16, 10, 16));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnSave.setBackground(MAU_PRIMARY_DISABLED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnSave.setBackground(MAU_PRIMARY);
            }
        });
        btnSave.addActionListener(e -> dialog.dispose());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12, 0, 0, 0));
        footer.add(btnSave, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private JPanel createField(String label, JComponent input) {
        JPanel field = new JPanel();
        field.setOpaque(false);
        field.setLayout(new BoxLayout(field, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_PLAIN);
        lbl.setForeground(MAU_MUTED);

        field.add(lbl);
        field.add(Box.createVerticalStrut(5));
        field.add(input);
        return field;
    }

    private JTextField createTextField(String value) {
        JTextField textField = new JTextField(value);
        textField.setFont(FONT_PLAIN);
        textField.setForeground(MAU_TEXT);
        textField.setBackground(MAU_CARD);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MAU_BORDER),
            new EmptyBorder(7, 10, 7, 10)
        ));
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(MAU_PRIMARY),
                    new EmptyBorder(7, 10, 7, 10)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(MAU_BORDER),
                    new EmptyBorder(7, 10, 7, 10)
                ));
            }
        });
        return textField;
    }

    private JComboBox<String> createCombo(String[] options) {
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setFont(FONT_PLAIN);
        combo.setForeground(MAU_TEXT);
        combo.setBackground(MAU_CARD);
        combo.setBorder(BorderFactory.createLineBorder(MAU_BORDER));
        return combo;
    }

    private JCheckBox createServiceCheck(String text, boolean selected) {
        JCheckBox checkBox = new JCheckBox(text, selected);
        checkBox.setOpaque(false);
        checkBox.setFont(FONT_PLAIN);
        checkBox.setForeground(MAU_TEXT);
        return checkBox;
    }
}
