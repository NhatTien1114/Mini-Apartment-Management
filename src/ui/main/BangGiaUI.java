package ui.main;

import entity.GiaDetail;
import entity.GiaHeader;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import service.BangGiaService;

public class BangGiaUI {

    // Colors
    private final Color MAU_NEN = new Color(241, 245, 249);
    private final Color MAU_CARD = Color.WHITE;
    private final Color MAU_BORDER = new Color(226, 232, 240);
    private final Color MAU_TEXT = new Color(15, 23, 42);
    private final Color MAU_PRIMARY = new Color(30, 41, 80);
    private final Color MAU_RED = new Color(239, 68, 68);

    private final Font FONT_TITLE = new Font("Be Vietnam Pro", Font.BOLD, 22);
    private final Font FONT_PLAIN = new Font("Be Vietnam Pro", Font.PLAIN, 13);

    private final BangGiaService service = new BangGiaService();
    private int currentLoai = 0; // 0=Phòng, 1=DichVu
    private String selectedGiaHeaderId = null;

    private JTable tableGiaHeader;
    private JTable tableGiaDetail;
    private DefaultTableModel modelGiaHeader;
    private DefaultTableModel modelGiaDetail;

    public JPanel getPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(MAU_NEN);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(MAU_NEN);
        topBar.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Bảng giá");
        title.setFont(FONT_TITLE);
        title.setForeground(MAU_TEXT);
        topBar.add(title, BorderLayout.WEST);

        JButton btnAdd = makePrimaryButton("+ Thêm bảng giá");
        btnAdd.addActionListener(e -> showAddGiaHeaderDialog());
        topBar.add(btnAdd, BorderLayout.EAST);

        root.add(topBar, BorderLayout.NORTH);

        // Tab panel
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setFont(FONT_PLAIN);
        tabPane.add("Phòng", createLoaiPanel(0));
        tabPane.add("Dịch vụ", createLoaiPanel(1));
        tabPane.addChangeListener(e -> {
            currentLoai = tabPane.getSelectedIndex();
            refreshGiaHeaderTable();
        });

        root.add(tabPane, BorderLayout.CENTER);

        refreshGiaHeaderTable();
        return root;
    }

    private JPanel createLoaiPanel(int loai) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(MAU_NEN);

        // GiaHeader section
        JPanel headerSection = new JPanel(new BorderLayout());
        headerSection.setBackground(MAU_CARD);
        headerSection.setBorder(new LineBorder(MAU_BORDER, 1, true));

        modelGiaHeader = new DefaultTableModel(
                new String[]{"Code", "Bắt đầu", "Kết thúc", "Mô tả", "Trạng thái", "Ghi chú"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableGiaHeader = new JTable(modelGiaHeader);
        tableGiaHeader.setFont(FONT_PLAIN);
        tableGiaHeader.setRowHeight(25);
        tableGiaHeader.getSelectionModel().addListSelectionListener(e -> refreshGiaDetailTable());

        JScrollPane scrollHeader = new JScrollPane(tableGiaHeader);
        scrollHeader.setBorder(null);

        JPanel headerButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        headerButtonPanel.setBackground(MAU_CARD);

        JButton btnEditHeader = new JButton("Sửa");
        btnEditHeader.addActionListener(e -> editSelectedGiaHeader());
        headerButtonPanel.add(btnEditHeader);

        JButton btnDelHeader = new JButton("Xóa");
        btnDelHeader.setForeground(Color.WHITE);
        btnDelHeader.setBackground(MAU_RED);
        btnDelHeader.addActionListener(e -> deleteSelectedGiaHeader());
        headerButtonPanel.add(btnDelHeader);

        headerSection.add(scrollHeader, BorderLayout.CENTER);
        headerSection.add(headerButtonPanel, BorderLayout.SOUTH);
        panel.add(headerSection, BorderLayout.NORTH);

        // GiaDetail section
        JPanel detailSection = new JPanel(new BorderLayout());
        detailSection.setBackground(MAU_CARD);
        detailSection.setBorder(new LineBorder(MAU_BORDER, 1, true));

        String[] detailColumns = loai == 0 
            ? new String[]{"Mã", "Loại phòng", "Đơn giá", ""}
            : new String[]{"Mã", "Dịch vụ", "Đơn giá", ""};

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
        detailButtonPanel.setBackground(MAU_CARD);

        JButton btnAddDetail = new JButton("+ Thêm");
        btnAddDetail.addActionListener(e -> showAddGiaDetailDialog(loai));
        detailButtonPanel.add(btnAddDetail);

        JButton btnEditDetail = new JButton("Sửa");
        btnEditDetail.addActionListener(e -> editSelectedGiaDetail(loai));
        detailButtonPanel.add(btnEditDetail);

        JButton btnDelDetail = new JButton("Xóa");
        btnDelDetail.setForeground(Color.WHITE);
        btnDelDetail.setBackground(MAU_RED);
        btnDelDetail.addActionListener(e -> deleteSelectedGiaDetail());
        detailButtonPanel.add(btnDelDetail);

        detailSection.add(scrollDetail, BorderLayout.CENTER);
        detailSection.add(detailButtonPanel, BorderLayout.SOUTH);
        panel.add(detailSection, BorderLayout.CENTER);

        return panel;
    }

    private void refreshGiaHeaderTable() {
        modelGiaHeader.setRowCount(0);
        List<GiaHeader> list = service.layGiaHeaderTheoLoai(currentLoai);
        for (GiaHeader gh : list) {
            modelGiaHeader.addRow(new Object[]{
                gh.getMaGiaHeader(),
                gh.getNgayBatDau(),
                gh.getNgayKetThuc(),
                gh.getMoTa(),
                gh.getTrangThaiTen(),
                gh.getGhiChu()
            });
        }
    }

    private void refreshGiaDetailTable() {
        modelGiaDetail.setRowCount(0);
        int selectedRow = tableGiaHeader.getSelectedRow();
        if (selectedRow < 0) return;

        String ma = (String) modelGiaHeader.getValueAt(selectedRow, 0);
        selectedGiaHeaderId = ma;

        List<GiaDetail> list = service.layGiaDetailTheoGiaHeader(ma);
        for (GiaDetail gd : list) {
            String loaiOrDichVu = gd.isPhongType() ? getLoaiPhongName(gd.getLoaiPhong()) : gd.getMaDichVu();
            modelGiaDetail.addRow(new Object[]{
                gd.getMaGiaDetail(),
                loaiOrDichVu,
                String.format("%,.0f", gd.getDonGia()),
                ""
            });
        }
    }

    private void showAddGiaHeaderDialog() {
        JDialog dlg = new JDialog();
        dlg.setTitle("Thêm bảng giá");
        dlg.setSize(500, 320);
        dlg.setLocationRelativeTo(null);
        dlg.setModal(true);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTextField txtMa = new JTextField();
        JTextField dateStart = new JTextField("yyyy-MM-dd");
        JTextField dateEnd = new JTextField("yyyy-MM-dd (tùy chọn)");
        JTextField txtMoTa = new JTextField();
        JCheckBox chkActive = new JCheckBox("Kích hoạt");
        chkActive.setSelected(true);

        form.add(new JLabel("Mã bảng giá:"));
        form.add(txtMa);
        form.add(new JLabel("Ngày bắt đầu (yyyy-MM-dd):"));
        form.add(dateStart);
        form.add(new JLabel("Ngày kết thúc (tùy chọn):"));
        form.add(dateEnd);
        form.add(new JLabel("Mô tả:"));
        form.add(txtMoTa);
        form.add(chkActive);

        JButton btnSave = new JButton("Thêm");
        btnSave.addActionListener(e -> {
            String ma = txtMa.getText().trim();
            if (ma.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Mã bảng giá không được để trống");
                return;
            }
            try {
                GiaHeader gh = new GiaHeader();
                gh.setMaGiaHeader(ma);
                gh.setNgayBatDau(LocalDate.parse(dateStart.getText().trim()));
                
                String dateEndStr = dateEnd.getText().trim();
                if (!dateEndStr.isEmpty() && !dateEndStr.contains("tùy chọn")) {
                    gh.setNgayKetThuc(LocalDate.parse(dateEndStr));
                }
                
                gh.setMoTa(txtMoTa.getText());
                gh.setTrangThai(chkActive.isSelected() ? 1 : 0);
                gh.setLoai(currentLoai);

                String err = service.themGiaHeader(gh);
                if (err != null) {
                    JOptionPane.showMessageDialog(dlg, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                refreshGiaHeaderTable();
                dlg.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Lỗi định dạng ngày: " + ex.getMessage());
            }
        });

        form.add(btnSave);
        dlg.add(form);
        dlg.setVisible(true);
    }

    private void showAddGiaDetailDialog(int loai) {
        if (selectedGiaHeaderId == null) {
            JOptionPane.showMessageDialog(null, "Chọn bảng giá trước");
            return;
        }

        JDialog dlg = new JDialog();
        dlg.setTitle("Thêm chi tiết giá");
        dlg.setSize(400, 250);
        dlg.setLocationRelativeTo(null);
        dlg.setModal(true);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTextField txtMa = new JTextField();
        JComboBox<String> comboLoaiOrDichVu = new JComboBox<>();
        if (loai == 0) {
            comboLoaiOrDichVu.addItem("0 - Phòng đơn");
            comboLoaiOrDichVu.addItem("1 - Phòng đôi");
            comboLoaiOrDichVu.addItem("2 - Phòng studio");
        } else {
            // TODO: load từ DichVu table
        }
        JTextField txtDonGia = new JTextField();

        form.add(new JLabel("Mã chi tiết:"));
        form.add(txtMa);
        form.add(new JLabel(loai == 0 ? "Loại phòng:" : "Dịch vụ:"));
        form.add(comboLoaiOrDichVu);
        form.add(new JLabel("Đơn giá:"));
        form.add(txtDonGia);

        JButton btnSave = new JButton("Thêm");
        btnSave.addActionListener(e -> {
            if (txtMa.getText().trim().isEmpty() || txtDonGia.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Điền đầy đủ thông tin");
                return;
            }
            try {
                GiaDetail gd = new GiaDetail();
                gd.setMaGiaDetail(txtMa.getText());
                gd.setMaGiaHeader(selectedGiaHeaderId);
                if (loai == 0) {
                    gd.setLoaiPhong(Integer.parseInt(comboLoaiOrDichVu.getSelectedItem().toString().split(" ")[0]));
                } else {
                    gd.setMaDichVu((String) comboLoaiOrDichVu.getSelectedItem());
                }
                gd.setDonGia(Double.parseDouble(txtDonGia.getText()));

                String err = service.themGiaDetail(gd);
                if (err != null) {
                    JOptionPane.showMessageDialog(dlg, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                refreshGiaDetailTable();
                dlg.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Đơn giá phải là số");
            }
        });

        form.add(btnSave);
        dlg.add(form);
        dlg.setVisible(true);
    }

    private void editSelectedGiaHeader() {
        int selectedRow = tableGiaHeader.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(null, "Chọn một bảng giá để sửa");
            return;
        }
        String ma = (String) modelGiaHeader.getValueAt(selectedRow, 0);
        // TODO: show edit dialog
    }

    private void deleteSelectedGiaHeader() {
        int selectedRow = tableGiaHeader.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(null, "Chọn một bảng giá để xóa");
            return;
        }
        String ma = (String) modelGiaHeader.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(null, "Xóa bảng giá \"" + ma + "\"?");
        if (confirm == JOptionPane.YES_OPTION) {
            String err = service.xoaGiaHeader(ma);
            if (err != null) {
                JOptionPane.showMessageDialog(null, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            refreshGiaHeaderTable();
        }
    }

    private void editSelectedGiaDetail(int loai) {
        // TODO
    }

    private void deleteSelectedGiaDetail() {
        int selectedRow = tableGiaDetail.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(null, "Chọn một chi tiết để xóa");
            return;
        }
        String ma = (String) modelGiaDetail.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(null, "Xóa chi tiết \"" + ma + "\"?");
        if (confirm == JOptionPane.YES_OPTION) {
            String err = service.xoaGiaDetail(ma);
            if (err != null) {
                JOptionPane.showMessageDialog(null, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            refreshGiaDetailTable();
        }
    }

    private String getLoaiPhongName(int loai) {
        return switch (loai) {
            case 0 -> "Phòng đơn";
            case 1 -> "Phòng đôi";
            case 2 -> "Phòng studio";
            default -> "Loại " + loai;
        };
    }

    private JButton makePrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Be Vietnam Pro", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(MAU_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}