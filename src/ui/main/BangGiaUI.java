package ui.main;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import ui.util.AppColors;
import ui.util.PrimaryButton;

public class BangGiaUI {
    private final Font FONT_TITLE = new Font("Be Vietnam Pro", Font.BOLD, 22);
    private final Font FONT_PLAIN = new Font("Be Vietnam Pro", Font.PLAIN, 13);

    private JTable tableGiaHeader;
    private JTable tableGiaDetail;
    private DefaultTableModel modelGiaHeader;
    private DefaultTableModel modelGiaDetail;
    private final PrimaryButton primaryButtonHelper = new PrimaryButton();
    private final AppColors appColors = new AppColors();

    public JPanel getPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(appColors.SLATE_100);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(appColors.SLATE_100);
        topBar.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Bảng giá");
        title.setFont(FONT_TITLE);
        title.setForeground(appColors.SLATE_900);
        topBar.add(title, BorderLayout.WEST);

        JButton btnAdd = primaryButtonHelper.makePrimaryButton("Thêm bảng giá");
        topBar.add(btnAdd, BorderLayout.EAST);

        root.add(topBar, BorderLayout.NORTH);

        // Tab panel
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setFont(FONT_PLAIN);
        tabPane.add("Phòng", createLoaiPanel(0));
        tabPane.add("Dịch vụ", createLoaiPanel(1));

        root.add(tabPane, BorderLayout.CENTER);

        return root;
    }

    private JPanel createLoaiPanel(int loai) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(appColors.SLATE_100);

        // GiaHeader section
        JPanel headerSection = new JPanel(new BorderLayout());
        headerSection.setBackground(appColors.WHITE);
        headerSection.setBorder(new LineBorder(appColors.SLATE_200, 1, true));

        modelGiaHeader = new DefaultTableModel(
                new String[] { "Code", "Bắt đầu", "Kết thúc", "Mô tả", "Trạng thái", "Ghi chú" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableGiaHeader = new JTable(modelGiaHeader);
        tableGiaHeader.setFont(FONT_PLAIN);
        tableGiaHeader.setRowHeight(25);

        JScrollPane scrollHeader = new JScrollPane(tableGiaHeader);
        scrollHeader.setBorder(null);

        JPanel headerButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        headerButtonPanel.setBackground(appColors.WHITE);

        JButton btnEditHeader = new JButton("Sửa");
        headerButtonPanel.add(btnEditHeader);

        JButton btnDelHeader = new JButton("Xóa");
        btnDelHeader.setForeground(Color.WHITE);
        btnDelHeader.setBackground(appColors.RED_500);
        headerButtonPanel.add(btnDelHeader);

        headerSection.add(scrollHeader, BorderLayout.CENTER);
        headerSection.add(headerButtonPanel, BorderLayout.SOUTH);
        panel.add(headerSection, BorderLayout.NORTH);

        // GiaDetail section
        JPanel detailSection = new JPanel(new BorderLayout());
        detailSection.setBackground(appColors.WHITE);
        detailSection.setBorder(new LineBorder(appColors.SLATE_200, 1, true));

        String[] detailColumns = loai == 0
                ? new String[] { "Mã", "Loại phòng", "Đơn giá", "" }
                : new String[] { "Mã", "Dịch vụ", "Đơn giá", "" };

        modelGiaDetail = new DefaultTableModel(detailColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableGiaDetail = new JTable(modelGiaDetail);
        tableGiaDetail.setFont(FONT_PLAIN);
        tableGiaDetail.setRowHeight(25);

        JScrollPane scrollDetail = new JScrollPane(tableGiaDetail);
        scrollDetail.setBorder(null);

        JPanel detailButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        detailButtonPanel.setBackground(appColors.WHITE);

        JButton btnAddDetail = new JButton("+ Thêm");
        detailButtonPanel.add(btnAddDetail);

        JButton btnEditDetail = new JButton("Sửa");
        detailButtonPanel.add(btnEditDetail);

        JButton btnDelDetail = new JButton("Xóa");
        btnDelDetail.setForeground(Color.WHITE);
        btnDelDetail.setBackground(appColors.RED_500);
        detailButtonPanel.add(btnDelDetail);

        detailSection.add(scrollDetail, BorderLayout.CENTER);
        detailSection.add(detailButtonPanel, BorderLayout.SOUTH);
        panel.add(detailSection, BorderLayout.CENTER);

        return panel;
    }
}