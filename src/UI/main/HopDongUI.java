package UI.main;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class HopDongUI {
    private final Color MAU_NEN = new Color(229, 231, 235);
    
    public JPanel getPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BorderLayout(20, 20));
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnl.setBackground(MAU_NEN);
        
        JLabel lblTitle = new JLabel("Quản Lý Hợp Đồng");
        lblTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 24));
        pnl.add(lblTitle, BorderLayout.NORTH);
        
        JPanel pnlContent = new JPanel();
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setLayout(new BorderLayout());
        JLabel lblContent = new JLabel("Nội dung hợp đồng sẽ được cập nhật");
        lblContent.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));
        lblContent.setHorizontalAlignment(SwingConstants.CENTER);
        pnlContent.add(lblContent, BorderLayout.CENTER);
        
        pnl.add(pnlContent, BorderLayout.CENTER);
        return pnl;
    }
}
