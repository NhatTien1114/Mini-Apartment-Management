package UI.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PhongInfo {

    private final String tenPhong;
    private final Color MAU_XANH_DUONG = Color.decode("#308CE8");

    public PhongInfo(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public void showDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle(tenPhong);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);

        JPanel pnlMain = new JPanel(new BorderLayout(10, 10));
        pnlMain.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnlMain.setBackground(Color.WHITE);

        JPanel pnlTop = createTopPanel();
        pnlMain.add(pnlTop, BorderLayout.NORTH);

        JPanel pnlMiddle = createMiddlePanel();
        JScrollPane scrollPane = new JScrollPane(pnlMiddle);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        pnlMain.add(scrollPane, BorderLayout.CENTER);

        JPanel pnlBottom = createBottomPanel(dialog);
        pnlMain.add(pnlBottom, BorderLayout.SOUTH);

        dialog.add(pnlMain);
        dialog.setVisible(true);
    }

    // ================= TOP PANEL =================
    private JPanel createTopPanel() {
        JPanel pnlTop = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlTop.setBackground(Color.WHITE);

        JPanel pnlName = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlName.setBackground(Color.WHITE);
        JLabel lblNameTitle = new JLabel("Tên phòng");
        JLabel lblName = new JLabel(tenPhong);
        lblName.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));
        pnlName.add(lblNameTitle);
        pnlName.add(Box.createHorizontalStrut(30));
        pnlName.add(lblName);

        JPanel pnlArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlArea.setBackground(Color.WHITE);
        JLabel lblAreaTitle = new JLabel("Diện tích");
        JLabel lblArea = new JLabel("18 m²");
        lblArea.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));
        pnlArea.add(lblAreaTitle);
        pnlArea.add(Box.createHorizontalStrut(30));
        pnlArea.add(lblArea);

        JPanel pnlPrice = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlPrice.setBackground(Color.WHITE);
        JLabel lblPriceTitle = new JLabel("Giá thuê");
        JLabel lblPrice = new JLabel("3.000.000 đ");
        lblPrice.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));
        pnlPrice.add(lblPriceTitle);
        pnlPrice.add(Box.createHorizontalStrut(30));
        pnlPrice.add(lblPrice);

        pnlTop.add(pnlName);
        pnlTop.add(pnlArea);
        pnlTop.add(pnlPrice);

        return pnlTop;
    }

    // ================= MIDDLE PANEL =================
    private JPanel createMiddlePanel() {
        JPanel pnlContainer = new JPanel();
        pnlContainer.setLayout(new BoxLayout(pnlContainer, BoxLayout.Y_AXIS));
        pnlContainer.setBackground(Color.WHITE);
        pnlContainer.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel pnlRow1 = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlRow1.setBackground(Color.WHITE);
        
        JTextField txtTenKhachThue = new JTextField();
        JTextField txtPhone = new JTextField();
        JTextField txtCccd = new JTextField();

        pnlRow1.add(createFieldPanel(new JLabel("Tên khách thuê"), txtTenKhachThue));
        pnlRow1.add(createFieldPanel(new JLabel("SĐT"), txtPhone));
        pnlRow1.add(createFieldPanel(new JLabel("CCCD"), txtCccd));

        JPanel pnlRow2 = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlRow2.setBackground(Color.WHITE);
        
        JTextField txtStartDate = new JTextField();
        JTextField txtEndDate = new JTextField();
        JTextField txtDeposit = new JTextField();

        pnlRow2.add(createFieldPanel(new JLabel("Ngày bắt đầu"), txtStartDate));
        pnlRow2.add(createFieldPanel(new JLabel("Ngày kết thúc"), txtEndDate));
        pnlRow2.add(createFieldPanel(new JLabel("Tiền cọc"), txtDeposit));

        JPanel pnlRow3 = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlRow3.setBackground(Color.WHITE);
        
        JTextField txtMonthlyRent = new JTextField();
        JComboBox<String> cboCondition = new JComboBox<>(new String[] { "Đã cọc", "Đã thuê", "Trống" });

        pnlRow3.add(createFieldPanel(new JLabel("Tiền thuê/tháng"), txtMonthlyRent));
        pnlRow3.add(createFieldPanel(new JLabel("Trạng thái phòng sau khi tạo"), cboCondition));

        pnlContainer.add(pnlRow1);
        pnlContainer.add(Box.createVerticalStrut(10));
        pnlContainer.add(pnlRow2);
        pnlContainer.add(Box.createVerticalStrut(10));
        pnlContainer.add(pnlRow3);
        pnlContainer.add(Box.createVerticalGlue());

        return pnlContainer;
    }

    private JPanel createFieldPanel(JLabel label, JComponent field) {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBackground(Color.WHITE);
        
        label.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        if (field instanceof JTextField) {
            ((JTextField) field).setFont(new Font("Be Vietnam Pro", Font.PLAIN, 12));
        } else if (field instanceof JComboBox) {
            ((JComboBox<?>) field).setFont(new Font("Be Vietnam Pro", Font.PLAIN, 12));
        }
        
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        pnl.add(label);
        pnl.add(Box.createVerticalStrut(5));
        pnl.add(field);
        
        return pnl;
    }

    // ================= BOTTOM PANEL =================
    private JPanel createBottomPanel(JDialog dialog) {
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBottom.setBackground(Color.WHITE);

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnCreate = new JButton("Tạo hợp đồng");
        btnCreate.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setBackground(MAU_XANH_DUONG);
        btnCreate.setBorder(BorderFactory.createEmptyBorder());
        btnCreate.setPreferredSize(new Dimension(150, 40));
        btnCreate.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Tạo hợp đồng thành công!");
            dialog.dispose();
        });

        pnlBottom.add(btnCancel);
        pnlBottom.add(btnCreate);

        return pnlBottom;
    }
}
