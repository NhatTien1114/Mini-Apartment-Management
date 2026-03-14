package UI.main;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class DichVuUI {
    private final Color MAU_NEN       = new Color(241, 245, 249);
    private final Color MAU_CARD      = Color.WHITE;
    private final Color MAU_BORDER    = new Color(226, 232, 240);
    private final Color MAU_TEXT      = new Color(15, 23, 42);
    private final Color MAU_MUTED     = new Color(100, 116, 139);
    private final Color MAU_PRIMARY   = new Color(37, 99, 235);
    private final Color MAU_RED       = new Color(239, 68, 68);
    private final Color MAU_BLUE_BG   = new Color(219, 234, 254);
    private final Color MAU_BLUE_FG   = new Color(29, 78, 216);
    private final Color MAU_GRAY_BG   = new Color(241, 245, 249);
    private final Color MAU_GRAY_FG   = new Color(71, 85, 105);
    private final Color MAU_HOVER_ROW = new Color(248, 250, 252);

    private final Font FONT_TITLE  = new Font("Be Vietnam Pro", Font.BOLD, 22);
    private final Font FONT_BOLD   = new Font("Be Vietnam Pro", Font.BOLD, 13);
    private final Font FONT_PLAIN  = new Font("Be Vietnam Pro", Font.PLAIN, 13);
    private final Font FONT_SMALL  = new Font("Be Vietnam Pro", Font.PLAIN, 12);

    private final NumberFormat NF = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // ── Dữ liệu mẫu ─────────────────────────────────────────────────────────
    private final java.util.List<Object[]> rows = new ArrayList<>(Arrays.asList(
            new Object[]{"Điện",            "kWh",       3_500,     "Biến đổi"},
            new Object[]{"Nước",            "m³",       15_000,     "Biến đổi"},
            new Object[]{"Internet",        "tháng",   100_000,     "Cố định"},
            new Object[]{"Rác",             "tháng",    20_000,     "Cố định"},
            new Object[]{"Giữ xe máy",      "xe/tháng",100_000,     "Cố định"},
            new Object[]{"Giữ xe đạp điện", "xe/tháng", 50_000,     "Cố định"},
            new Object[]{"Giữ ô tô",        "xe/tháng",1_500_000,   "Cố định"}
    ));

    private DefaultTableModel tableModel;
    private JTable table;

    // ════════════════════════════════════════════════════════════════════════
    public JPanel getPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 0));
        pnl.setBorder(new EmptyBorder(24, 24, 24, 24));
        pnl.setBackground(MAU_NEN);

        pnl.add(buildTopBar(), BorderLayout.NORTH);
        pnl.add(buildTableCard(), BorderLayout.CENTER);

        return pnl;
    }

    // ── Top bar: tiêu đề + nút thêm ─────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(MAU_NEN);
        bar.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Quản lý dịch vụ");
        title.setFont(FONT_TITLE);
        title.setForeground(MAU_TEXT);
        bar.add(title, BorderLayout.WEST);

        JButton btnAdd = makePrimaryButton("+ Thêm dịch vụ");
        btnAdd.addActionListener(e -> showDialog(null));
        bar.add(btnAdd, BorderLayout.EAST);

        return bar;
    }

    // ── Bảng ─────────────────────────────────────────────────────────────────
    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(MAU_CARD);
        card.setBorder(new LineBorder(MAU_BORDER, 1, true));

        String[] cols = {"Tên dịch vụ", "Đơn vị", "Đơn giá", "Loại", ""};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
            @Override public Class<?> getColumnClass(int c) { return Object.class; }
        };
        loadTable();

        table = new JTable(tableModel) {
            // Hover effect per row
            int hoverRow = -1;
            { addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    int r = rowAtPoint(e.getPoint());
                    if (r != hoverRow) { hoverRow = r; repaint(); }
                }
            });
                addMouseListener(new MouseAdapter() {
                    public void mouseExited(MouseEvent e) { hoverRow = -1; repaint(); }
                });
            }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row))
                    c.setBackground(hoverRow == row ? MAU_HOVER_ROW : MAU_CARD);
                return c;
            }
        };

        // Style header — cùng màu nền trắng với nội dung, chữ xám nhạt
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SMALL);
        header.setForeground(MAU_MUTED);
        header.setBackground(MAU_CARD);           // ← trắng, không phải xám
        header.setBorder(new MatteBorder(0, 0, 1, 0, MAU_BORDER));
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);

        // Custom header renderer: trắng + padding trái giống row
        DefaultTableCellRenderer hdrRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(MAU_CARD);
                setForeground(MAU_MUTED);
                setFont(FONT_SMALL);
                setBorder(new EmptyBorder(0, 16, 0, 8));
                setHorizontalAlignment(SwingConstants.LEFT);
                return this;
            }
        };
        for (int i = 0; i < 5; i++)
            table.getColumnModel().getColumn(i).setHeaderRenderer(hdrRenderer);

        // Style table — tắt grid, dùng border dưới mỗi row qua renderer
        table.setRowHeight(56);
        table.setFont(FONT_PLAIN);
        table.setBackground(MAU_CARD);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);      // ← tắt line mặc định
        table.setGridColor(MAU_CARD);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(340);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setMaxWidth(80);

        // Renderers
        table.getColumnModel().getColumn(0).setCellRenderer(boldPaddedRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(plainPaddedRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(plainPaddedRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new BadgeRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ActionEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(MAU_CARD);
        sp.setBackground(MAU_CARD);

        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        for (Object[] r : rows) {
            tableModel.addRow(new Object[]{
                    r[0], r[1],
                    NF.format((long)(int) r[2]) + "đ",
                    r[3],
                    "ACT"
            });
        }
    }

    // ── Dialog Thêm / Sửa ────────────────────────────────────────────────────
    private void showDialog(int[] editIdx) {
        boolean editing = editIdx != null;
        Object[] src = editing ? rows.get(editIdx[0]) : null;

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(table) instanceof Frame
                ? (Frame) SwingUtilities.getWindowAncestor(table) : null,
                editing ? "Sửa dịch vụ" : "Thêm dịch vụ", true);
        dlg.setSize(480, 300);
        dlg.setLocationRelativeTo(null);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(MAU_NEN);

        // ── Dialog card ──
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(MAU_CARD);
        card.setBorder(new EmptyBorder(22, 24, 22, 24));

        // Title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(MAU_CARD);
        titleRow.setBorder(new EmptyBorder(0, 0, 18, 0));
        JLabel dlgTitle = new JLabel(editing ? "Sửa dịch vụ" : "Thêm dịch vụ");
        dlgTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 17));
        dlgTitle.setForeground(MAU_TEXT);
        titleRow.add(dlgTitle, BorderLayout.WEST);

        // Fields grid
        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setBackground(MAU_CARD);

        JTextField fTen   = makeField(editing ? (String) src[0] : "");
        JTextField fDonVi = makeField(editing ? (String) src[1] : "");
        JTextField fGia   = makeField(editing ? String.valueOf((int) src[2]) : "0");
        JComboBox<String> cLoai = makeCombo(new String[]{"Cố định", "Biến đổi"});
        if (editing) cLoai.setSelectedItem(src[3]);

        grid.add(wrapField("Tên dịch vụ", fTen));
        grid.add(wrapField("Đơn vị", fDonVi));
        grid.add(wrapField("Đơn giá", fGia));
        grid.add(wrapField("Loại", cLoai));

        // Button row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(MAU_CARD);
        btnRow.setBorder(new EmptyBorder(18, 0, 0, 0));

        JButton btnCancel = makeOutlineButton("Hủy");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = makePrimaryButton(editing ? "Cập nhật" : "Thêm");
        btnSave.addActionListener(e -> {
            String ten = fTen.getText().trim();
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập tên dịch vụ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int gia;
            try { gia = Integer.parseInt(fGia.getText().trim().replace(",", "").replace(".", "")); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Đơn giá không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object[] row = {ten, fDonVi.getText().trim(), gia, cLoai.getSelectedItem()};
            if (editing) rows.set(editIdx[0], row);
            else rows.add(row);
            loadTable();
            dlg.dispose();
        });

        btnRow.add(btnCancel);
        btnRow.add(btnSave);

        card.add(titleRow, BorderLayout.NORTH);
        card.add(grid,    BorderLayout.CENTER);
        card.add(btnRow,  BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    // RENDERERS
    // ════════════════════════════════════════════════════════════════════════

    /** Badge "Biến đổi" / "Cố định" */
    class BadgeRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                                                       boolean sel, boolean foc, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 18));
            p.setBackground(sel ? new Color(239,246,255) : MAU_CARD);
            p.setBorder(new MatteBorder(0, 0, 1, 0, MAU_BORDER));
            String val = v == null ? "" : v.toString();
            JLabel badge = new JLabel(val);
            badge.setFont(FONT_SMALL);
            badge.setOpaque(true);
            badge.setBorder(new EmptyBorder(3, 10, 3, 10));
            if ("Biến đổi".equals(val)) {
                badge.setBackground(MAU_BLUE_BG);
                badge.setForeground(MAU_BLUE_FG);
            } else {
                badge.setBackground(MAU_GRAY_BG);
                badge.setForeground(MAU_GRAY_FG);
            }
            p.add(badge);
            return p;
        }
    }

    /** Nút ✏ 🗑 (chỉ render) */
    class ActionRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                                                       boolean sel, boolean foc, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 18));
            p.setBackground(sel ? new Color(239,246,255) : MAU_CARD);
            p.setBorder(new MatteBorder(0, 0, 1, 0, MAU_BORDER));
            p.add(makeIconLabel("✏", MAU_MUTED));
            p.add(makeIconLabel("🗑", MAU_RED));
            return p;
        }
    }

    /** Nút ✏ 🗑 (click được) */
    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private int curRow;

        ActionEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 18));
            panel.setBackground(MAU_CARD);
            panel.setBorder(new MatteBorder(0, 0, 1, 0, MAU_BORDER));

            JButton btnEdit = makeIconButton("✏", MAU_MUTED);
            JButton btnDel  = makeIconButton("🗑", MAU_RED);

            btnEdit.addActionListener(e -> { stopCellEditing(); showDialog(new int[]{curRow}); });
            btnDel.addActionListener(e -> {
                stopCellEditing();
                int confirm = JOptionPane.showConfirmDialog(
                        panel.getTopLevelAncestor(),
                        "Xóa dịch vụ \"" + rows.get(curRow)[0] + "\"?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    rows.remove(curRow);
                    loadTable();
                }
            });

            panel.add(btnEdit);
            panel.add(btnDel);
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int row, int col) {
            curRow = row;
            return panel;
        }

        @Override public Object getCellEditorValue() { return "ACT"; }
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPER FACTORIES
    // ════════════════════════════════════════════════════════════════════════

    private JButton makePrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(29, 78, 216) : MAU_PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Be Vietnam Pro", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        return btn;
    }

    private JButton makeOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_PLAIN);
        btn.setForeground(MAU_MUTED);
        btn.setBackground(MAU_CARD);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(MAU_BORDER, 1, true),
                new EmptyBorder(7, 16, 7, 16)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeIconButton(String icon, Color fg) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(2, 5, 2, 5));
        return btn;
    }

    private JLabel makeIconLabel(String icon, Color fg) {
        JLabel l = new JLabel(icon);
        l.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        l.setForeground(fg);
        return l;
    }

    private JTextField makeField(String val) {
        JTextField f = new JTextField(val);
        f.setFont(FONT_PLAIN);
        f.setForeground(MAU_TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(MAU_BORDER, 1, true),
                new EmptyBorder(7, 11, 7, 11)));
        f.setPreferredSize(new Dimension(0, 38));
        // Blue focus border
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(MAU_PRIMARY, 2, true),
                        new EmptyBorder(6, 10, 6, 10)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(MAU_BORDER, 1, true),
                        new EmptyBorder(7, 11, 7, 11)));
            }
        });
        return f;
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_PLAIN);
        cb.setBackground(MAU_CARD);
        cb.setForeground(MAU_TEXT);
        cb.setBorder(new LineBorder(MAU_BORDER, 1, true));
        cb.setPreferredSize(new Dimension(0, 38));
        return cb;
    }

    private JPanel wrapField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(MAU_CARD);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(MAU_TEXT);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private TableCellRenderer boldPaddedRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setFont(FONT_BOLD);
                setForeground(MAU_TEXT);
                // Đường kẻ dưới row mỏng, không có vertical line
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, MAU_BORDER),
                        new EmptyBorder(0, 16, 0, 8)));
                return this;
            }
        };
    }

    private TableCellRenderer plainPaddedRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setFont(FONT_PLAIN);
                setForeground(MAU_TEXT);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, MAU_BORDER),
                        new EmptyBorder(0, 16, 0, 8)));
                return this;
            }
        };
    }
}