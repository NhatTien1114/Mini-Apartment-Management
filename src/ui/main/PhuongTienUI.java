package ui.main;

import dao.DichVuDAO;
import dao.KhachHangDAO;
import dao.PhuongTienDAO;
import entity.DichVu;
import entity.KhachHang;
import entity.PhuongTien;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import ui.util.AppColors;
import ui.util.PrimaryButton;

public class PhuongTienUI {
    private final Font FONT_TITLE = new Font("Be Vietnam Pro", Font.BOLD, 24);
    private final Font FONT_BOLD = new Font("Be Vietnam Pro", Font.BOLD, 14);
    private final Font FONT_PLAIN = new Font("Be Vietnam Pro", Font.PLAIN, 14);
    private final Font FONT_SMALL = new Font("Be Vietnam Pro", Font.PLAIN, 13);
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    private final PhuongTienDAO phuongTienDAO = new PhuongTienDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final DichVuDAO dichVuDAO = new DichVuDAO();
    private final PrimaryButton primaryButton = new PrimaryButton();

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtTimKiem;
    private JComboBox<String> cboFilterType;

    public JPanel getPanel() {
        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(AppColors.APP_BACKGROUND);

        root.add(createTopBar(), BorderLayout.NORTH);
        root.add(createTableCard(), BorderLayout.CENTER);

        loadData();
        return root;
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(AppColors.APP_BACKGROUND);

        JLabel lblTitle = new JLabel("Quản lý phương tiện");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(AppColors.SLATE_900);

        JButton btnThem = primaryButton.makePrimaryButton("Đăng ký xe");
        btnThem.addActionListener(e -> showRegistrationDialog(null));

        top.add(lblTitle, BorderLayout.WEST);
        top.add(btnThem, BorderLayout.EAST);
        return top;
    }

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(AppColors.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_200, 1, true),
                new EmptyBorder(12, 12, 12, 12)));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        bar.setBackground(AppColors.WHITE);

        txtTimKiem = new JTextField();
        txtTimKiem.setPreferredSize(new Dimension(300, 36));
        txtTimKiem.setFont(FONT_PLAIN);
        txtTimKiem.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_200, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        txtTimKiem.setToolTipText("Tìm kiếm theo biển số, khách hàng...");

        cboFilterType = new JComboBox<>(new String[]{"Tất cả loại xe", "Xe máy", "Ô tô", "Xe điện", "Khác"});
        cboFilterType.setPreferredSize(new Dimension(150, 36));
        cboFilterType.setFont(FONT_PLAIN);
        cboFilterType.setBackground(AppColors.WHITE);

        bar.add(txtTimKiem);
        bar.add(cboFilterType);
        card.add(bar, BorderLayout.NORTH);

        String[] cols = { "Biển số xe", "Loại xe", "Mã KH", "Chủ sở hữu", "Phòng", "Phí gửi (vnđ)" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel) {
            private int hoverRow = -1;

            {
                addMouseMotionListener(new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        int row = rowAtPoint(e.getPoint());
                        if (hoverRow != row) {
                            hoverRow = row;
                            repaint();
                        }
                    }
                });
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hoverRow = -1;
                        repaint();
                    }
                });
            }

            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(hoverRow == row ? AppColors.SLATE_50 : AppColors.WHITE);
                }
                return c;
            }
        };

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setFont(FONT_SMALL);
                setForeground(AppColors.SLATE_600);
                setBackground(AppColors.WHITE);
                setBorder(new EmptyBorder(0, 10, 0, 8));
                return this;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        table.setRowHeight(52);
        table.setFont(FONT_PLAIN);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(AppColors.PRIMARY_TINT_HOVER);
        table.setSelectionForeground(AppColors.SLATE_900);

        DefaultTableCellRenderer paddedCell = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200),
                        new EmptyBorder(0, 10, 0, 8)));
                setFont(col == 0 ? FONT_BOLD : FONT_PLAIN);
                return this;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(paddedCell);
        }

        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem miEdit = new JMenuItem("Xem/Sửa thông tin");
        JMenuItem miDelete = new JMenuItem("Xóa");
        miDelete.setForeground(new Color(239, 68, 68));
        contextMenu.add(miEdit);
        contextMenu.add(miDelete);

        miEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                String bienSo = (String) tableModel.getValueAt(modelRow, 0);
                String loaiXe = (String) tableModel.getValueAt(modelRow, 1);
                String maKH = (String) tableModel.getValueAt(modelRow, 2);
                String chuSoHuu = (String) tableModel.getValueAt(modelRow, 3);
                String phong = (String) tableModel.getValueAt(modelRow, 4);
                String phiStr = (String) tableModel.getValueAt(modelRow, 5);
                double phi = Double.parseDouble(phiStr.replace(".", "").replace(",", ""));

                PhuongTien pt = new PhuongTien(bienSo, loaiXe, maKH, chuSoHuu, phong, phi);
                showRegistrationDialog(pt);
            }
        });

        miDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                String bienSo = (String) tableModel.getValueAt(modelRow, 0);
                int opt = JOptionPane.showConfirmDialog(table, "Bạn có chắc muốn xóa phương tiện \nBiển số: " + bienSo + "?", "Xóa", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    if (phuongTienDAO.delete(bienSo)) {
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(table, "Lỗi khi xóa phương tiện!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        table.setComponentPopupMenu(contextMenu);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        cboFilterType.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                applyFilter();
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(AppColors.WHITE);
        card.add(sp, BorderLayout.CENTER);

        return card;
    }

    private void applyFilter() {
        String keyword = txtTimKiem.getText().trim();
        String typeFilter = (String) cboFilterType.getSelectedItem();
        
        List<RowFilter<Object,Object>> filters = new java.util.ArrayList<>();
        
        if (!keyword.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(keyword), 0, 2, 3, 4));
        }
        
        if (!"Tất cả loại xe".equals(typeFilter)) {
            filters.add(RowFilter.regexFilter("(?i)^" + java.util.regex.Pattern.quote(typeFilter) + "$", 1));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<PhuongTien> list = phuongTienDAO.getAllPhuongTien();
        for (PhuongTien pt : list) {
            tableModel.addRow(new Object[]{
                pt.getBienSo(),
                pt.getLoaiXe(),
                pt.getMaKhachHang(),
                pt.getTenKhachHang() != null ? pt.getTenKhachHang() : "",
                pt.getMaPhong() != null ? pt.getMaPhong() : "",
                formatter.format(pt.getMucPhi())
            });
        }
    }

    private void showRegistrationDialog(PhuongTien editPt) {
        boolean isEdit = editPt != null;
        Window owner = SwingUtilities.getWindowAncestor(table);
        JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null, isEdit ? "Sửa phương tiện" : "Đăng ký phương tiện", true);
        dlg.setSize(550, 600);
        dlg.setLocationRelativeTo(owner);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.APP_BACKGROUND);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(AppColors.WHITE);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel(isEdit ? "Cập nhật thông tin phương tiện" : "Nhập thông tin phương tiện");
        title.setFont(new Font("Be Vietnam Pro", Font.BOLD, 18));
        title.setForeground(AppColors.SLATE_900);
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setBackground(AppColors.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JTextField txtBienSo = makeField(isEdit ? editPt.getBienSo() : "");
        if (isEdit) txtBienSo.setEnabled(false); // Primary key locked on edit

        JComboBox<String> cboLoaiXe = new JComboBox<>(new String[]{"Xe máy", "Ô tô", "Xe điện", "Khác"});
        cboLoaiXe.setFont(FONT_PLAIN);
        cboLoaiXe.setBackground(AppColors.WHITE);
        cboLoaiXe.setPreferredSize(new Dimension(0, 38));
        cboLoaiXe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        if(isEdit) cboLoaiXe.setSelectedItem(editPt.getLoaiXe());

        JComboBox<KhachHangItem> cboKhachHang = new JComboBox<>();
        cboKhachHang.setFont(FONT_PLAIN);
        cboKhachHang.setBackground(AppColors.WHITE);
        cboKhachHang.setPreferredSize(new Dimension(0, 38));
        cboKhachHang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        
        List<KhachHang> dskh = khachHangDAO.layDanhSachKhachHang();
        KhachHangItem selectedKH = null;
        cboKhachHang.addItem(new KhachHangItem("", "-- Chọn Khách Hàng --"));
        for (KhachHang kh : dskh) {
            KhachHangItem item = new KhachHangItem(kh.getMaKhachHang(), kh.getMaKhachHang() + " - " + kh.getHoTen());
            cboKhachHang.addItem(item);
            if (isEdit && kh.getMaKhachHang().equals(editPt.getMaKhachHang())) {
                selectedKH = item;
            }
        }
        if (selectedKH != null) cboKhachHang.setSelectedItem(selectedKH);

        JTextField txtPhong = makeField(isEdit && editPt.getMaPhong() != null ? editPt.getMaPhong() : "");
        txtPhong.setEnabled(false);

        JTextField txtPhi = makeField(isEdit ? String.valueOf((long)editPt.getMucPhi()) : "0");

        // Dynamic price matching
        Runnable updatePrice = () -> {
            if ("Khác".equals(cboLoaiXe.getSelectedItem())) {
                txtPhi.setText("0");
                return;
            }
            String type = ((String)cboLoaiXe.getSelectedItem()).toLowerCase();
            List<DichVu> services = dichVuDAO.layTatCa();
            boolean found = false;
            for(DichVu dv : services) {
                if(dv.getTenDichVu() != null && dv.getTenDichVu().toLowerCase().contains(type)) {
                    if(dv.getDonGia() != null) {
                        txtPhi.setText(String.valueOf(dv.getDonGia().longValue()));
                        found = true;
                    }
                    break;
                }
            }
            if (!found) {
                txtPhi.setText("0");
            }
        };

        cboLoaiXe.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && !isEdit) {
                updatePrice.run();
            }
        });

        // Initialize price for the default selected item on open
        if (!isEdit) {
            updatePrice.run();
        }

        cboKhachHang.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                KhachHangItem it = (KhachHangItem) cboKhachHang.getSelectedItem();
                if (it != null && !it.maKH.isEmpty()) {
                    String p = khachHangDAO.layMaPhongHienTaiTheoKhach(it.maKH);
                    txtPhong.setText(p != null ? p : "");
                } else {
                    txtPhong.setText("");
                }
            }
        });

        form.add(wrapField("Biển số (BSX)", txtBienSo));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Họ Tên Chủ", cboKhachHang));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Loại phương tiện", cboLoaiXe));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Thuộc phòng", txtPhong));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Phí gửi (vnđ/tháng)", txtPhi));

        card.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(AppColors.WHITE);

        JButton btnHuy = makeOutlineButton("Hủy");
        btnHuy.addActionListener(e -> dlg.dispose());

        JButton btnLuu = primaryButton.makePrimaryButton(isEdit ? "Cập nhật" : "Lưu dữ liệu");
        btnLuu.addActionListener(e -> {
            String bsx = txtBienSo.getText().trim();
            if (bsx.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập biển số xe.");
                return;
            }
            KhachHangItem khItem = (KhachHangItem) cboKhachHang.getSelectedItem();
            String maKH = khItem != null && !khItem.maKH.isEmpty() ? khItem.maKH : null;
            String loai = (String) cboLoaiXe.getSelectedItem();
            String phong = txtPhong.getText().trim();
            if(phong.isEmpty()) phong = null;
            double phi = 0;
            try {
                phi = Double.parseDouble(txtPhi.getText().trim());
            } catch (Exception exx) {
                JOptionPane.showMessageDialog(dlg, "Phí đóng phải là số hợp lệ.");
                return;
            }

            PhuongTien pt = new PhuongTien(bsx, loai, maKH, "", phong, phi);
            boolean success = false;
            
            if (isEdit) {
                success = phuongTienDAO.update(pt);
            } else {
                success = phuongTienDAO.insert(pt);
            }

            if (success) {
                loadData();
                dlg.dispose();
            } else {
                JOptionPane.showMessageDialog(dlg, "Lưu thất bại! Hãy kiểm tra lại Biển Số (không được trùng).");
            }
        });

        actions.add(btnHuy);
        actions.add(btnLuu);
        card.add(actions, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private JTextField makeField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(FONT_PLAIN);
        field.setForeground(AppColors.SLATE_900);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_200, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        field.setPreferredSize(new Dimension(0, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return field;
    }

    private JButton makeOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_PLAIN);
        btn.setForeground(AppColors.SLATE_600);
        btn.setBackground(AppColors.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_200, 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        return btn;
    }

    private JComponent wrapField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(AppColors.WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(AppColors.SLATE_900);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    static class KhachHangItem {
        String maKH;
        String display;
        KhachHangItem(String maKH, String display) {
            this.maKH = maKH;
            this.display = display;
        }
        @Override
        public String toString() {
            return display;
        }
    }
}