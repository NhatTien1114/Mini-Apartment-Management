package ui.main;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import UI.util.PhongInfo;
import UI.util.RoundedButton;

public class QuanLyUI {
    private final Color MAU_NEN = new Color(229, 231, 235);
    
    public JPanel getPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BorderLayout(20, 20));
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnl.setBackground(MAU_NEN);
        
        JPanel pnlTop = new JPanel();
        pnlTop.setLayout(new BorderLayout());
        JLabel lblTitle = new JLabel("Quản Lý Chung Cư");
        RoundedButton btnThemPhong = new RoundedButton("+ Thêm phòng",17);
        lblTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 24));
        pnlTop.add(lblTitle, BorderLayout.WEST);
        pnlTop.add(btnThemPhong, BorderLayout.EAST);
        btnThemPhong.setFont(new Font("Be Vietnam Pro", Font.BOLD, 17));
        btnThemPhong.setBackground(new Color(65, 113, 201));
        btnThemPhong.setForeground(Color.WHITE);
        pnl.add(pnlTop, BorderLayout.NORTH);
        pnlTop.setBackground(MAU_NEN);
        
        JPanel pnlContent = new JPanel();
        JPanel pnlContentTop = new JPanel();
        pnlContentTop.setLayout(new BorderLayout());
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setLayout(new BorderLayout());
        JTextField txtTimPhong = new JTextField(20);
        txtTimPhong.setPreferredSize(new Dimension(200, 30));
        pnlContentTop.add(txtTimPhong, BorderLayout.WEST);
        pnlContentTop.setBackground(MAU_NEN);
        pnlContent.add(pnlContentTop, BorderLayout.NORTH);
        
        String[] columnNames = {"Phòng", "Tầng", "Phòng đơn", "Diện tích", "Giá thuê", "Trạng thái"};
        Object[][] data = {
            {"T1.01", "Tầng 1", "Phòng đơn", "20m2", 500000, "Đã Cọc"},
            {"T1.02", "Tầng 1", "Phòng đơn", "20m2", 500000, "Đã Cọc"},
            {"T1.03", "Tầng 1", "Phòng đơn", "20m2", 500000, "Đã Cọc"}
        };
        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        table.getTableHeader().setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));
        
        
        pnl.add(scrollPane, BorderLayout.CENTER);
        return pnl;
    }
}