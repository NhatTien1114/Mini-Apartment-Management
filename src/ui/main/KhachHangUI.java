package ui.main;

import dao.QuanLyPhongDAO;
import entity.KhachHang;
import entity.Phong;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.MaskFormatter;
import service.KhachHangService;
import ui.util.AppColors;
import ui.util.PrimaryButton;
import ui.util.RoundedTextField;
import ui.util.ValidationPopup;

public class KhachHangUI {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Font FONT_TITLE = new Font("Be Vietnam Pro", Font.BOLD, 24);
    private final Font FONT_BOLD = new Font("Be Vietnam Pro", Font.BOLD, 14);
    private final Font FONT_PLAIN = new Font("Be Vietnam Pro", Font.PLAIN, 14);
    private final Font FONT_SMALL = new Font("Be Vietnam Pro", Font.PLAIN, 13);

    private final ImageIcon ICON_PEN = loadActionIcon("pen.png", 16, 16);
    private final ImageIcon ICON_BIN = loadActionIcon("bin.png", 16, 16);

    private final KhachHangService khachHangService = new KhachHangService();
    private final QuanLyPhongDAO phongDAO = new QuanLyPhongDAO();
    private final PrimaryButton primaryButton = new PrimaryButton();

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private RoundedTextField txtTimKiem;
    private JComboBox<String> cboFilterPhong;

    public JPanel getPanel() {
        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(AppColors.APP_BACKGROUND);

        root.add(createTopBar(), BorderLayout.NORTH);
        root.add(createTableCard(), BorderLayout.CENTER);

        loadKhachHangData();
        return root;
    }

    public void refresh() {
        loadKhachHangData();
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(AppColors.APP_BACKGROUND);

        JLabel lblTitle = new JLabel("Quản lý khách hàng");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(AppColors.SLATE_900);

        JButton btnThem = primaryButton.makePrimaryButton("Thêm khách hàng");
        btnThem.addActionListener(e -> showKhachHangDialog(null));

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

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(AppColors.WHITE);

        txtTimKiem = new RoundedTextField(8) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(148, 163, 184));
                g2.setStroke(new BasicStroke(1.5f));
                int cx = 18;
                int cy = getHeight() / 2 - 2;
                g2.drawOval(cx - 4, cy - 4, 8, 8);
                g2.drawLine(cx + 2, cy + 2, cx + 7, cy + 7);

                if (isFocusOwner()) {
                    g2.setColor(new Color(37, 99, 235));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                }
                g2.dispose();
            }
        };

        txtTimKiem.setBorder(new EmptyBorder(8, 36, 8, 12));
        txtTimKiem.setPlaceholder("Tìm kiếm khách hàng...");
        txtTimKiem.setPreferredSize(new Dimension(280, 40));
        txtTimKiem.setFont(FONT_PLAIN);
        txtTimKiem.setToolTipText("Tìm theo mã, họ tên, số điện thoại, CCCD");

        JButton btnTimKiem = primaryButton.makePrimaryButton("Tìm kiếm");
        btnTimKiem.setPreferredSize(new Dimension(110, 36));
        btnTimKiem.addActionListener(e -> applyFilter());

        List<String> dsPhong = new ArrayList<>();
        dsPhong.add("Tất cả phòng");
        for (Phong phong : phongDAO.getAllPhongDaThue()) {
            dsPhong.add(phong.getMaPhong());
        }
        cboFilterPhong = new JComboBox<>(dsPhong.toArray(new String[0]));
        cboFilterPhong.setPreferredSize(new Dimension(150, 36));
        cboFilterPhong.setFont(FONT_PLAIN);
        cboFilterPhong.setBackground(AppColors.WHITE);

        bar.add(txtTimKiem);
        bar.add(btnTimKiem);
        bar.add(cboFilterPhong);
        card.add(bar, BorderLayout.NORTH);

        String[] cols = { "Mã KH", "Họ tên", "SĐT", "CCCD", "Ngày sinh", "Địa chỉ", "Phòng", "Trạng thái" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel) {
            private int hoverRow = -1;

            {
                addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(java.awt.event.MouseEvent e) {
                        int row = rowAtPoint(e.getPoint());
                        if (hoverRow != row) {
                            hoverRow = row;
                            repaint();
                        }
                    }
                });
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hoverRow = -1;
                        repaint();
                    }
                });
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(hoverRow == row ? AppColors.SLATE_50 : AppColors.WHITE);
                }
                return c;
            }
        };

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r,
                    int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 13));
                l.setBackground(isSel ? t.getSelectionBackground() : AppColors.WHITE);
                l.setOpaque(true);

                // Check if this row is "Đã rời đi"
                int modelRow = t.convertRowIndexToModel(r);
                Object statusVal = t.getModel().getValueAt(modelRow, 7);
                boolean daRoiDi = "Đã rời đi".equals(statusVal);

                if (daRoiDi) {
                    l.setForeground(new Color(156, 163, 175)); // gray text
                } else {
                    l.setForeground(AppColors.SLATE_900);
                }

                if (c == 0) {
                    l.setForeground(daRoiDi ? new Color(156, 163, 175) : new Color(37, 99, 235));
                    l.setFont(new Font("Be Vietnam Pro", Font.BOLD, 13));
                }

                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                        new EmptyBorder(0, 16, 0, 8)));
                return l;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setFont(FONT_SMALL);
                setForeground(AppColors.SLATE_600);
                setBackground(AppColors.WHITE);
                setBorder(new EmptyBorder(0, 10, 0, 8));
                setHorizontalAlignment(SwingConstants.LEFT);
                return this;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        table.setRowHeight(52);
        table.setFont(FONT_PLAIN);
        table.setForeground(AppColors.SLATE_900);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setGridColor(AppColors.WHITE);
        table.setSelectionBackground(AppColors.PRIMARY_TINT_HOVER);
        table.setSelectionForeground(AppColors.SLATE_900);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(180);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(110);

        DefaultTableCellRenderer paddedCell = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
                    int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200),
                        new EmptyBorder(0, 10, 0, 8)));
                setFont(col == 1 ? FONT_BOLD : FONT_PLAIN);
                int modelRow = t.convertRowIndexToModel(row);
                Object statusVal = t.getModel().getValueAt(modelRow, 7);
                boolean daRoiDi = "Đã rời đi".equals(statusVal);
                setForeground(daRoiDi ? new Color(156, 163, 175) : AppColors.SLATE_900);
                return this;
            }
        };

        for (int i = 2; i < 7; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(paddedCell);
        }

        table.getColumnModel().getColumn(1).setCellRenderer(boldPaddedRenderer());

        // Status column renderer (badge style)
        table.getColumnModel().getColumn(7).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSel, boolean hasFocus,
                    int row, int col) {
                String status = value != null ? value.toString() : "";
                boolean daRoiDi = "Đã rời đi".equals(status);

                JLabel badge = new JLabel(status, SwingConstants.CENTER);
                badge.setOpaque(true);
                badge.setFont(new Font("Be Vietnam Pro", Font.BOLD, 11));

                if (daRoiDi) {
                    badge.setForeground(new Color(239, 68, 68));
                    badge.setBackground(new Color(254, 226, 226));
                } else {
                    badge.setForeground(new Color(22, 163, 74));
                    badge.setBackground(new Color(220, 252, 231));
                }

                badge.setBorder(new EmptyBorder(4, 8, 4, 8));

                JPanel cell = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
                cell.setBackground(isSel ? t.getSelectionBackground() : AppColors.WHITE);
                cell.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));
                cell.add(badge);
                return cell;
            }
        });

        // --- Right-click context menu ---
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem miEdit = new JMenuItem("Xem/Sửa thông tin");
        JMenuItem miDelete = new JMenuItem("Xóa");
        miDelete.setForeground(new Color(239, 68, 68));
        contextMenu.add(miEdit);
        contextMenu.add(miDelete);

        miEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            int modelRow = table.convertRowIndexToModel(row);
            String maKhachHang = String.valueOf(tableModel.getValueAt(modelRow, 0));
            try {
                KhachHang kh = khachHangService.timTheoMa(maKhachHang);
                if (kh == null) {
                    showError("Không tìm thấy khách hàng để sửa.");
                    return;
                }
                showKhachHangDialog(kh);
            } catch (RuntimeException ex) {
                showError("Không thể tải dữ liệu khách hàng: " + ex.getMessage());
            }
        });
        miDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            int modelRow = table.convertRowIndexToModel(row);
            String maKhachHang = String.valueOf(tableModel.getValueAt(modelRow, 0));
            String hoTen = String.valueOf(tableModel.getValueAt(modelRow, 1));
            int confirm = JOptionPane.showConfirmDialog(table,
                    "Bạn có chắc muốn xóa khách hàng \"" + hoTen + "\" (" + maKhachHang + ")?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION)
                return;
            try {
                boolean ok = khachHangService.xoaKhachHang(maKhachHang);
                if (!ok) {
                    showError("Xóa thất bại, khách hàng không tồn tại.");
                    return;
                }
                loadKhachHangData();
            } catch (RuntimeException ex) {
                showError("Không thể xóa khách hàng: " + ex.getMessage());
            }
        });

        table.setComponentPopupMenu(contextMenu);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        cboFilterPhong.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                applyFilter();
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(AppColors.WHITE);
        sp.setBackground(AppColors.WHITE);

        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private TableCellRenderer boldPaddedRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setFont(FONT_BOLD);
                int modelRow = t.convertRowIndexToModel(r);
                Object statusVal = t.getModel().getValueAt(modelRow, 7);
                boolean daRoiDi = "Đã rời đi".equals(statusVal);
                setForeground(daRoiDi ? new Color(156, 163, 175) : AppColors.SLATE_900);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, AppColors.SLATE_200),
                        new EmptyBorder(0, 16, 0, 8)));
                return this;
            }
        };
    }

    private void applyFilter() {
        String keyword = txtTimKiem.getText().trim();
        String phongFilter = (String) cboFilterPhong.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        if (!keyword.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(keyword), 0, 1, 2, 3, 5, 6));
        }

        if (phongFilter != null && !"Tất cả phòng".equals(phongFilter)) {
            filters.add(RowFilter.regexFilter("(?i)^" + java.util.regex.Pattern.quote(phongFilter) + "$", 6));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void loadKhachHangData() {
        tableModel.setRowCount(0);
        try {
            List<KhachHang> danhSach = khachHangService.layDanhSachKhachHang();
            for (KhachHang kh : danhSach) {
                boolean daRoiDi = khachHangService.kiemTraDaRoiDi(kh.getMaKhachHang());
                String maPhong = daRoiDi
                        ? khachHangService.layMaPhongCuoiCungTheoKhach(kh.getMaKhachHang())
                        : khachHangService.layMaPhongHienTaiTheoKhach(kh.getMaKhachHang());
                tableModel.addRow(new Object[] {
                        kh.getMaKhachHang(),
                        kh.getHoTen(),
                        nullSafe(kh.getSoDienThoai()),
                        nullSafe(kh.getSoCCCD()),
                        kh.getNgaySinh() == null ? "" : DATE_FORMAT.format(kh.getNgaySinh()),
                        nullSafe(kh.getDiaChi()),
                        nullSafe(maPhong),
                        daRoiDi ? "Đã rời đi" : "Đang ở"
                });
            }
        } catch (RuntimeException ex) {
            showError("Không tải được danh sách khách hàng: " + ex.getMessage());
        }
    }

    private void showKhachHangDialog(KhachHang editKhachHang) {
        boolean isEdit = editKhachHang != null;
        String maKhachHangEdit = "";
        String hoTenValue = "";
        String sdtValue = "";
        String ngaySinhValue = "";
        String cccdValue = "";
        String diaChiValue = "";

        if (isEdit && editKhachHang != null) {
            maKhachHangEdit = nullSafe(editKhachHang.getMaKhachHang());
            hoTenValue = nullSafe(editKhachHang.getHoTen());
            sdtValue = nullSafe(editKhachHang.getSoDienThoai());
            cccdValue = nullSafe(editKhachHang.getSoCCCD());
            diaChiValue = nullSafe(editKhachHang.getDiaChi());
            if (editKhachHang.getNgaySinh() != null) {
                ngaySinhValue = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(editKhachHang.getNgaySinh());
            }
        }
        final String maKhachHangEditFinal = maKhachHangEdit;

        Window owner = SwingUtilities.getWindowAncestor(table);
        JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null,
                isEdit ? "Sửa khách hàng" : "Thêm khách hàng", true);
        dlg.setSize(680, 640);
        dlg.setLocationRelativeTo(owner);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.APP_BACKGROUND);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(AppColors.WHITE);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel(isEdit ? "Cập nhật thông tin khách hàng" : "Nhập thông tin khách hàng");
        title.setFont(new Font("Be Vietnam Pro", Font.BOLD, 18));
        title.setForeground(AppColors.SLATE_900);
        card.add(title, BorderLayout.NORTH);

        JTextField txtHoTen = makeField(hoTenValue);
        JTextField txtSdt = makeField(sdtValue);
        JFormattedTextField txtNgaySinh = createDatePickerField(ngaySinhValue);
        JTextField txtCccd = makeField(cccdValue);
        JTextArea txtDiaChi = makeTextArea(diaChiValue);

        // --- Number-only filter for SĐT and CCCD ---
        applyNumberFilter(txtSdt);
        applyNumberFilter(txtCccd);

        // --- Focus-lost validation ---
        txtHoTen.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.isTemporary())
                    return;
                SwingUtilities.invokeLater(() -> {
                    String val = txtHoTen.getText().trim();
                    if (val.isEmpty()) {
                        ValidationPopup.show(txtHoTen, "Họ tên không được để trống");
                    } else if (val.split("\\s+").length < 2) {
                        ValidationPopup.show(txtHoTen, "Họ tên phải có ít nhất 2 từ");
                    }
                });
            }
        });
        txtSdt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.isTemporary())
                    return;
                SwingUtilities.invokeLater(() -> {
                    String val = txtSdt.getText().trim();
                    if (val.isEmpty()) {
                        ValidationPopup.show(txtSdt, "Số điện thoại không được để trống");
                    } else if (!val.matches("^0[0-9]{9}$")) {
                        ValidationPopup.show(txtSdt, "SĐT phải gồm 10 số và bắt đầu bằng số 0");
                    }
                });
            }
        });
        txtCccd.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.isTemporary())
                    return;
                SwingUtilities.invokeLater(() -> {
                    String val = txtCccd.getText().trim();
                    if (val.isEmpty()) {
                        ValidationPopup.show(txtCccd, "Số CCCD không được để trống");
                    } else if (!val.matches("^[0-9]{12}$")) {
                        ValidationPopup.show(txtCccd, "Số CCCD phải đủ 12 số");
                    } else if (khachHangService.kiemTraCCCDTonTai(val, isEdit ? maKhachHangEditFinal : null)) {
                        ValidationPopup.show(txtCccd, "Số CCCD này đã tồn tại trong hệ thống");
                    }
                });
            }
        });
        txtNgaySinh.setFocusLostBehavior(JFormattedTextField.PERSIST);
        txtNgaySinh.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.isTemporary())
                    return;
                SwingUtilities.invokeLater(() -> {
                    String raw = txtNgaySinh.getText().replace("_", "").replace("/", "").trim();
                    if (raw.isEmpty()) {
                        ValidationPopup.show(txtNgaySinh, "Ngày sinh không được để trống");
                    } else {
                        String fullText = txtNgaySinh.getText().replace("_", "").trim();
                        if (fullText.length() >= 10) {
                            try {
                                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                                        .withResolverStyle(java.time.format.ResolverStyle.STRICT);
                                LocalDate.parse(fullText, fmt);
                            } catch (Exception ex) {
                                ValidationPopup.show(txtNgaySinh, "Ngày sinh không hợp lệ");
                            }
                        }
                    }
                });
            }
        });
        txtDiaChi.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.isTemporary())
                    return;
                SwingUtilities.invokeLater(() -> {
                    if (txtDiaChi.getText().trim().isEmpty()) {
                        ValidationPopup.show(txtDiaChi, "Địa chỉ không được để trống");
                    }
                });
            }
        });

        List<String> dsPhongDisplay = new ArrayList<>();
        for (Phong phong : phongDAO.getAllPhongDaThue()) {
            dsPhongDisplay.add(phong.getMaPhong());
        }
        if (dsPhongDisplay.isEmpty()) {
            dsPhongDisplay.add("(Không có phòng đã thuê)");
        }
        JComboBox<String> cboPhongDaThue = new JComboBox<>(dsPhongDisplay.toArray(new String[0]));
        cboPhongDaThue.setFont(FONT_PLAIN);
        cboPhongDaThue.setBackground(AppColors.WHITE);
        cboPhongDaThue.setForeground(AppColors.SLATE_900);
        cboPhongDaThue.setEnabled(!isEdit);
        cboPhongDaThue.setPreferredSize(new Dimension(0, 42));
        cboPhongDaThue.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        txtHoTen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtSdt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtNgaySinh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtCccd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtDiaChi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel form = new JPanel();
        form.setBackground(AppColors.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(wrapField("Phòng (đã thuê) *", cboPhongDaThue));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Họ tên *", txtHoTen));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Số điện thoại *", txtSdt));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Ngày sinh (dd/MM/yyyy) *", txtNgaySinh));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("CCCD *", txtCccd));
        form.add(Box.createVerticalStrut(10));
        form.add(wrapField("Địa chỉ *", new JScrollPane(txtDiaChi)));

        card.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(AppColors.WHITE);

        JButton btnHuy = makeOutlineButton("Hủy");
        btnHuy.addActionListener(e -> dlg.dispose());

        JButton btnLuu = primaryButton.makePrimaryButton(isEdit ? "Cập nhật" : "Thêm");
        btnLuu.addActionListener(e -> {
            try {
                // --- Validate all fields ---
                String hoTen = txtHoTen.getText().trim();
                if (hoTen.isEmpty()) {
                    ValidationPopup.show(txtHoTen, "Họ tên không được để trống");
                    txtHoTen.requestFocus();
                    return;
                }
                if (hoTen.split("\\s+").length < 2) {
                    ValidationPopup.show(txtHoTen, "Họ tên phải có ít nhất 2 từ");
                    txtHoTen.requestFocus();
                    return;
                }

                String sdt = txtSdt.getText().trim();
                if (sdt.isEmpty()) {
                    ValidationPopup.show(txtSdt, "Số điện thoại không được để trống");
                    txtSdt.requestFocus();
                    return;
                }
                if (!sdt.matches("^0[0-9]{9}$")) {
                    ValidationPopup.show(txtSdt, "SĐT phải gồm 10 số và bắt đầu bằng số 0");
                    txtSdt.requestFocus();
                    return;
                }

                String ngaySinhRaw = txtNgaySinh.getText().replace("_", "").trim();
                if (ngaySinhRaw.isEmpty() || ngaySinhRaw.replace("/", "").trim().isEmpty()) {
                    ValidationPopup.show(txtNgaySinh, "Ngày sinh không được để trống");
                    txtNgaySinh.requestFocus();
                    return;
                }
                LocalDate ngaySinh = null;
                if (ngaySinhRaw.length() >= 10) {
                    try {
                        DateTimeFormatter ddMMyyyyFmt = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                                .withResolverStyle(java.time.format.ResolverStyle.STRICT);
                        ngaySinh = LocalDate.parse(ngaySinhRaw, ddMMyyyyFmt);
                    } catch (DateTimeParseException ex) {
                        ValidationPopup.show(txtNgaySinh, "Ngày sinh không hợp lệ");
                        txtNgaySinh.requestFocus();
                        return;
                    }
                } else {
                    ValidationPopup.show(txtNgaySinh, "Ngày sinh không đúng định dạng dd/MM/yyyy");
                    txtNgaySinh.requestFocus();
                    return;
                }

                String cccd = txtCccd.getText().trim();
                if (cccd.isEmpty()) {
                    ValidationPopup.show(txtCccd, "Số CCCD không được để trống");
                    txtCccd.requestFocus();
                    return;
                }
                if (!cccd.matches("^[0-9]{12}$")) {
                    ValidationPopup.show(txtCccd, "Số CCCD phải đủ 12 số");
                    txtCccd.requestFocus();
                    return;
                }
                if (khachHangService.kiemTraCCCDTonTai(cccd, isEdit ? maKhachHangEditFinal : null)) {
                    ValidationPopup.show(txtCccd, "Số CCCD này đã tồn tại trong hệ thống");
                    txtCccd.requestFocus();
                    return;
                }

                String diaChi = txtDiaChi.getText().trim();
                if (diaChi.isEmpty()) {
                    ValidationPopup.show(txtDiaChi, "Địa chỉ không được để trống");
                    txtDiaChi.requestFocus();
                    return;
                }

                KhachHang payload = new KhachHang();
                if (isEdit) {
                    payload.setMaKhachHang(maKhachHangEditFinal);
                }
                payload.setHoTen(hoTen);
                payload.setSoDienThoai(sdt);
                payload.setNgaySinh(ngaySinh);
                payload.setSoCCCD(cccd);
                payload.setDiaChi(diaChi);

                if (isEdit) {
                    boolean ok = khachHangService.capNhatKhachHang(payload);
                    if (!ok) {
                        throw new RuntimeException("Không tìm thấy khách hàng để cập nhật.");
                    }
                } else {
                    String maPhongChon = extractRoomCode((String) cboPhongDaThue.getSelectedItem());
                    if (maPhongChon.isEmpty()) {
                        JOptionPane.showMessageDialog(dlg, "Vui lòng chọn phòng đã thuê.", "Thiếu thông tin",
                                JOptionPane.WARNING_MESSAGE);
                        cboPhongDaThue.requestFocus();
                        return;
                    }
                    khachHangService.themKhachHangVaoPhongDaThue(payload, maPhongChon);
                }

                loadKhachHangData();
                dlg.dispose();
            } catch (RuntimeException ex) {
                showError("Lưu khách hàng thất bại: " + ex.getMessage());
            }
        });

        actions.add(btnHuy);
        actions.add(btnLuu);
        card.add(actions, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String extractRoomCode(String roomDisplay) {
        if (roomDisplay == null) {
            return "";
        }
        String value = roomDisplay.trim();
        if (value.startsWith("(")) {
            return "";
        }
        int idx = value.indexOf('-');
        return idx > 0 ? value.substring(0, idx).trim() : value;
    }

    private JTextField makeField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(FONT_PLAIN);
        field.setForeground(AppColors.SLATE_900);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_200, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        field.setPreferredSize(new Dimension(0, 38));
        return field;
    }

    private JTextArea makeTextArea(String value) {
        JTextArea area = new JTextArea(value);
        area.setFont(FONT_PLAIN);
        area.setForeground(AppColors.SLATE_900);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SLATE_200, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        area.setPreferredSize(new Dimension(0, 90));
        return area;
    }

    private void applyNumberFilter(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string != null && string.matches("[0-9]*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text != null && text.matches("[0-9]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
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

    private void showError(String message) {
        JOptionPane.showMessageDialog(table, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private ImageIcon loadActionIcon(String fileName, int width, int height) {
        String[] paths = { "img/icon/" + fileName, "img/icons/" + fileName };
        for (String path : paths) {
            ImageIcon raw = new ImageIcon(path);
            if (raw.getIconWidth() > 0 && raw.getIconHeight() > 0) {
                Image scaled = raw.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        }
        return null;
    }

    private JLabel makeActionIconLabel(ImageIcon icon) {
        JLabel label = new JLabel();
        if (icon != null) {
            label.setIcon(icon);
        }
        return label;
    }

    private JFormattedTextField createDatePickerField(String initialValue) {
        MaskFormatter mask = null;
        try {
            mask = new MaskFormatter("##/##/####");
            mask.setPlaceholderCharacter('_');
        } catch (java.text.ParseException e) {
            // ignore
        }

        JFormattedTextField txt = new JFormattedTextField(mask) {
            boolean focused = false;
            {
                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        focused = true;
                        repaint();
                    }

                    public void focusLost(FocusEvent e) {
                        focused = false;
                        repaint();
                    }
                });
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (focused) {
                    g2.setColor(AppColors.PRIMARY);
                    g2.setStroke(new BasicStroke(2f));
                } else {
                    g2.setColor(AppColors.SLATE_200);
                    g2.setStroke(new BasicStroke(1f));
                }
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 6, 6);

                // calendar icon
                g2.setColor(AppColors.SLATE_900);
                int cx = getWidth() - 20;
                int cy = getHeight() / 2;
                g2.drawRect(cx - 5, cy - 4, 10, 8);
                g2.drawLine(cx - 3, cy - 6, cx - 3, cy - 4);
                g2.drawLine(cx + 3, cy - 6, cx + 3, cy - 4);
                g2.drawLine(cx - 5, cy - 1, cx + 5, cy - 1);
                g2.drawRect(cx - 2, cy + 2, 1, 1);
                g2.dispose();
            }
        };
        txt.setBorder(new EmptyBorder(7, 10, 7, 36));
        txt.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        txt.setFont(FONT_PLAIN);
        txt.setForeground(AppColors.SLATE_900);
        txt.setPreferredSize(new Dimension(0, 42));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        if (initialValue != null && !initialValue.isEmpty()) {
            txt.setText(initialValue);
        }

        txt.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                txt.setCursor(e.getX() > txt.getWidth() - 36
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            }
        });
        txt.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getX() > txt.getWidth() - 36) {
                    new KhDatePickerPopup(txt).show(txt, 0, txt.getHeight());
                    txt.requestFocus();
                } else {
                    if (txt.getText().equals("__/__/____")) {
                        txt.setCaretPosition(0);
                    }
                }
            }
        });

        return txt;
    }

    class KhDatePickerPopup extends JPopupMenu {
        private Calendar calTracker = Calendar.getInstance();
        private JPanel daysPanel;
        private JLabel lblMonthYear;
        private JTextField targetField;

        public KhDatePickerPopup(JTextField targetField) {
            this.targetField = targetField;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppColors.SLATE_200),
                    new EmptyBorder(12, 16, 12, 16)));

            JPanel pnlHead = new JPanel(new BorderLayout());
            pnlHead.setOpaque(false);
            lblMonthYear = new JLabel("", SwingConstants.LEFT);
            lblMonthYear.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));

            JPanel pnlArrows = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pnlArrows.setOpaque(false);
            JButton btnPrev = createArrBtn("\u2190");
            JButton btnNext = createArrBtn("\u2192");
            btnPrev.addActionListener(e -> {
                calTracker.add(Calendar.MONTH, -1);
                updateCal();
            });
            btnNext.addActionListener(e -> {
                calTracker.add(Calendar.MONTH, 1);
                updateCal();
            });
            pnlArrows.add(btnPrev);
            pnlArrows.add(btnNext);

            pnlHead.add(lblMonthYear, BorderLayout.CENTER);
            pnlHead.add(pnlArrows, BorderLayout.EAST);
            add(pnlHead, BorderLayout.NORTH);

            JPanel pnlGrid = new JPanel(new BorderLayout(0, 8));
            pnlGrid.setOpaque(false);
            pnlGrid.setBorder(new EmptyBorder(12, 0, 12, 0));

            JPanel pnlDOW = new JPanel(new GridLayout(1, 7, 4, 4));
            pnlDOW.setOpaque(false);
            String[] dows = { "CN", "T2", "T3", "T4", "T5", "T6", "T7" };
            for (String d : dows) {
                JLabel l = new JLabel(d, SwingConstants.CENTER);
                l.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 12));
                l.setForeground(AppColors.SLATE_500);
                pnlDOW.add(l);
            }
            pnlGrid.add(pnlDOW, BorderLayout.NORTH);

            daysPanel = new JPanel(new GridLayout(6, 7, 4, 4));
            daysPanel.setOpaque(false);
            pnlGrid.add(daysPanel, BorderLayout.CENTER);
            add(pnlGrid, BorderLayout.CENTER);

            JPanel pnlFooter = new JPanel(new BorderLayout());
            pnlFooter.setOpaque(false);
            JButton btnClear = createTxtBtn("X\u00f3a");
            JButton btnToday = createTxtBtn("H\u00f4m nay");
            btnClear.addActionListener(e -> {
                targetField.setText("__/__/____");
                setVisible(false);
            });
            btnToday.addActionListener(e -> {
                targetField.setText(fmtStr(Calendar.getInstance()));
                setVisible(false);
            });
            pnlFooter.add(btnClear, BorderLayout.WEST);
            pnlFooter.add(btnToday, BorderLayout.EAST);
            add(pnlFooter, BorderLayout.SOUTH);

            updateCal();
        }

        private void updateCal() {
            daysPanel.removeAll();
            Calendar cal = (Calendar) calTracker.clone();
            String m = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault());
            lblMonthYear.setText(m + " " + cal.get(Calendar.YEAR));

            cal.set(Calendar.DAY_OF_MONTH, 1);
            int sd = cal.get(Calendar.DAY_OF_WEEK);
            cal.add(Calendar.DAY_OF_MONTH, -(sd - 1));

            for (int i = 0; i < 42; i++) {
                int d = cal.get(Calendar.DAY_OF_MONTH);
                boolean cM = cal.get(Calendar.MONTH) == calTracker.get(Calendar.MONTH);
                JButton b = new JButton(String.valueOf(d));
                b.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 12));
                b.setForeground(cM ? AppColors.SLATE_900 : AppColors.SLATE_400);
                b.setBackground(Color.WHITE);
                b.setBorderPainted(false);
                b.setContentAreaFilled(false);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                b.setMargin(new Insets(2, 2, 2, 2));

                b.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        b.setContentAreaFilled(true);
                        b.setBackground(AppColors.SLATE_100);
                    }

                    public void mouseExited(MouseEvent e) {
                        b.setContentAreaFilled(false);
                    }
                });

                Calendar cap = (Calendar) cal.clone();
                b.addActionListener(e -> {
                    targetField.setText(fmtStr(cap));
                    setVisible(false);
                });
                daysPanel.add(b);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            daysPanel.revalidate();
            daysPanel.repaint();
        }

        private String fmtStr(Calendar c) {
            return String.format("%02d/%02d/%04d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1,
                    c.get(Calendar.YEAR));
        }

        private JButton createArrBtn(String t) {
            JButton b = new JButton(t);
            b.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));
            b.setForeground(AppColors.SLATE_500);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }

        private JButton createTxtBtn(String t) {
            JButton b = new JButton(t);
            b.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 12));
            b.setForeground(AppColors.PRIMARY);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }
    }

}
