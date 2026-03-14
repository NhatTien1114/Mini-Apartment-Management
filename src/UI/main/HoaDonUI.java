package ui.main;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class HoaDonUI {
    private final Color MAU_NEN      = new Color(241, 245, 249);
    private final Color MAU_CARD     = Color.WHITE;
    private final Color MAU_BORDER   = new Color(226, 232, 240);
    private final Color MAU_TEXT     = new Color(15, 23, 42);
    private final Color MAU_MUTED    = new Color(100, 116, 139);
    private final Color MAU_PRIMARY  = new Color(37, 99, 235);
    private final Color MAU_PRIMARY_DISABLED = new Color(147, 197, 253);
    private final Color MAU_RED      = new Color(239, 68, 68);
    private final Color MAU_AMBER_BG = new Color(254, 249, 195);
    private final Color MAU_AMBER_FG = new Color(161, 98, 7);
    private final Color MAU_GREEN_BG = new Color(220, 252, 231);
    private final Color MAU_GREEN_FG = new Color(22, 163, 74);

    private final Font FONT_TITLE = new Font("Be Vietnam Pro", Font.BOLD, 22);
    private final Font FONT_BOLD  = new Font("Be Vietnam Pro", Font.BOLD, 13);
    private final Font FONT_PLAIN = new Font("Be Vietnam Pro", Font.PLAIN, 13);
    private final Font FONT_SMALL = new Font("Be Vietnam Pro", Font.PLAIN, 12);

    private final NumberFormat NF = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private final String[] ROOMS  = {
            "Chọn phòng",
            "T1.01","T1.02","T1.03","T1.04","T1.05",
            "T2.01","T2.02","T2.03","T2.04","T2.05","T2.06",
            "T3.01","T3.02","T3.03","T3.04","T3.05",
            "T4.01","T4.02","T4.03","T4.04","T4.05",
            "T5.01","T5.02","T5.03","T5.04","T5.05",
            "T6.01","T6.02","T6.03","T6.04"
    };
    private final String[] MONTHS = {"T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"};

    private final java.util.List<Object[]> invoiceRows = new ArrayList<>();
    private DefaultTableModel tableModel;
    private CardLayout bodyCard;
    private JPanel bodyPanel;

    // ════════════════════════════════════════════════════════════════════════
    public JPanel getPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 0));
        pnl.setBorder(new EmptyBorder(24, 24, 24, 24));
        pnl.setBackground(MAU_NEN);

        pnl.add(buildTopBar(), BorderLayout.NORTH);
        pnl.add(buildBody(),   BorderLayout.CENTER);

        return pnl;
    }

    // ── Top bar ──────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(MAU_NEN);
        bar.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Hóa đơn hàng tháng");
        title.setFont(FONT_TITLE);
        title.setForeground(MAU_TEXT);
        bar.add(title, BorderLayout.WEST);

        JButton btnAdd = makePrimaryButton("+ Tạo hóa đơn");
        btnAdd.addActionListener(e -> showCreateDialog());
        bar.add(btnAdd, BorderLayout.EAST);

        return bar;
    }

    // ── Body: CardLayout chuyển giữa empty state và bảng ────────────────────
    private JPanel buildBody() {
        bodyCard  = new CardLayout();
        bodyPanel = new JPanel(bodyCard);
        bodyPanel.setBackground(MAU_NEN);

        bodyPanel.add(buildEmptyState(), "EMPTY");
        bodyPanel.add(buildTableCard(),  "TABLE");

        bodyCard.show(bodyPanel, "EMPTY");
        return bodyPanel;
    }

    // ── Empty state ──────────────────────────────────────────────────────────
    private JPanel buildEmptyState() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(MAU_CARD);
        card.setBorder(new LineBorder(MAU_BORDER, 1, true));

        JPanel inner = new JPanel();
        inner.setBackground(MAU_CARD);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        // Document icon (vẽ bằng tay để giống ảnh)
        JLabel ico = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(148, 163, 184));
                // thân tài liệu
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(8, 2, 28, 36, 4, 4);
                // các dòng chữ
                g2.drawLine(14, 14, 30, 14);
                g2.drawLine(14, 20, 30, 20);
                g2.drawLine(14, 26, 24, 26);
                // gấp góc
                g2.drawLine(28, 2, 36, 10);
                g2.drawLine(28, 2, 28, 10);
                g2.drawLine(28, 10, 36, 10);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(44, 44); }
        };
        ico.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel txt = new JLabel("Chưa có hóa đơn nào. Nhấn \"Tạo hóa đơn\" để bắt đầu.");
        txt.setFont(FONT_PLAIN);
        txt.setForeground(MAU_MUTED);
        txt.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(ico);
        inner.add(Box.createVerticalStrut(12));
        inner.add(txt);

        card.add(inner);
        return card;
    }

    // ── Bảng hóa đơn ────────────────────────────────────────────────────────
    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(MAU_CARD);
        card.setBorder(new LineBorder(MAU_BORDER, 1, true));

        String[] cols = {"Phòng", "Tháng/Năm", "Điện (kWh)", "Nước (m³)", "Tổng tiền", "Trạng thái", ""};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(54);
        table.setFont(FONT_PLAIN);
        table.setBackground(MAU_CARD);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setGridColor(MAU_CARD);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);

        // Header — cùng màu trắng, chữ xám, căn trái
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SMALL);
        header.setForeground(MAU_MUTED);
        header.setBackground(MAU_CARD);
        header.setBorder(new MatteBorder(0, 0, 1, 0, MAU_BORDER));
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer hdrR = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(MAU_CARD); setForeground(MAU_MUTED); setFont(FONT_SMALL);
                setBorder(new EmptyBorder(0, 16, 0, 8));
                setHorizontalAlignment(SwingConstants.LEFT);
                return this;
            }
        };
        for (int i = 0; i < 7; i++) table.getColumnModel().getColumn(i).setHeaderRenderer(hdrR);

        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setMaxWidth(80);

        // Row renderer với bottom border
        DefaultTableCellRenderer rowR = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setFont(FONT_PLAIN); setForeground(MAU_TEXT);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, MAU_BORDER),
                        new EmptyBorder(0, 16, 0, 8)));
                return this;
            }
        };
        for (int i = 0; i < 5; i++) table.getColumnModel().getColumn(i).setCellRenderer(rowR);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionEditor(table));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(MAU_CARD);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Object[] r : invoiceRows) tableModel.addRow(r);
        bodyCard.show(bodyPanel, invoiceRows.isEmpty() ? "EMPTY" : "TABLE");
    }

    // ── Dialog Tạo hóa đơn ──────────────────────────────────────────────────
    private void showCreateDialog() {
        Window owner = null;
        if (bodyPanel.getTopLevelAncestor() instanceof Window window)
            owner = window;

        JDialog dlg = (owner instanceof Frame)
                ? new JDialog((Frame) owner, "Tạo hóa đơn tháng", true)
                : new JDialog((Dialog) owner, "Tạo hóa đơn tháng", true);
        dlg.setSize(520, 420);
        dlg.setLocationRelativeTo(owner);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(MAU_CARD);

        // ── Header ──
        JPanel dlgHead = new JPanel(new BorderLayout());
        dlgHead.setBackground(MAU_CARD);
        dlgHead.setBorder(new EmptyBorder(20, 22, 14, 22));

        JLabel dlgTitle = new JLabel("Tạo hóa đơn tháng");
        dlgTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 17));
        dlgTitle.setForeground(MAU_TEXT);
        dlgHead.add(dlgTitle, BorderLayout.WEST);

        JSeparator sep = new JSeparator();
        sep.setForeground(MAU_BORDER);

        JPanel headWrap = new JPanel(new BorderLayout());
        headWrap.setBackground(MAU_CARD);
        headWrap.add(dlgHead, BorderLayout.CENTER);
        headWrap.add(sep, BorderLayout.SOUTH);
        root.add(headWrap, BorderLayout.NORTH);

        // ── Form ──
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(MAU_CARD);
        form.setBorder(new EmptyBorder(16, 22, 16, 22));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTH;

        // Row 0: Phòng (rộng) + Tháng + Năm
        JComboBox<String> cRoom  = makeCombo(ROOMS);
        cRoom.setSelectedIndex(0);
        JComboBox<String> cMonth = makeCombo(MONTHS);
        int curMonth = Calendar.getInstance().get(Calendar.MONTH); // 0-based
        cMonth.setSelectedIndex(Math.max(0, curMonth));
        JTextField fYear = makeField(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));

        g.gridy = 0; g.weightx = 2.5; g.gridx = 0; g.insets = new Insets(0, 0, 0, 12);
        form.add(wrapField("Phòng", cRoom), g);
        g.weightx = 1.4; g.gridx = 1; g.insets = new Insets(0, 0, 0, 12);
        form.add(wrapField("Tháng", cMonth), g);
        g.weightx = 1.0; g.gridx = 2; g.insets = new Insets(0, 0, 0, 0);
        form.add(wrapField("Năm", fYear), g);

        // Row 1: Số điện cũ / mới
        JTextField fDienCu  = makeField("0");
        JTextField fDienMoi = makeField("0");

        g.gridy = 1; g.weightx = 1.5; g.gridx = 0; g.insets = new Insets(14, 0, 0, 12);
        form.add(wrapField("Số điện cũ (kWh)", fDienCu), g);
        g.gridx = 1; g.gridwidth = 2; g.insets = new Insets(14, 0, 0, 0);
        form.add(wrapField("Số điện mới (kWh)", fDienMoi), g);
        g.gridwidth = 1;

        // Row 2: Số nước cũ / mới
        JTextField fNuocCu  = makeField("0");
        JTextField fNuocMoi = makeField("0");

        g.gridy = 2; g.weightx = 1.5; g.gridx = 0; g.insets = new Insets(14, 0, 0, 12);
        form.add(wrapField("Số nước cũ (m³)", fNuocCu), g);
        g.gridx = 1; g.gridwidth = 2; g.insets = new Insets(14, 0, 0, 0);
        form.add(wrapField("Số nước mới (m³)", fNuocMoi), g);
        g.gridwidth = 1;

        root.add(form, BorderLayout.CENTER);

        // ── Nút Tính tổng tiền / Lưu ──
        JPanel botPanel = new JPanel(new BorderLayout());
        botPanel.setBackground(MAU_CARD);
        botPanel.setBorder(new EmptyBorder(0, 22, 20, 22));

        JButton calcBtn = new JButton("Tính tổng tiền") {
            @Override protected void paintComponent(Graphics g2) {
                Graphics2D g = (Graphics2D) g2.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean enabled = isEnabled();
                g.setColor(enabled
                        ? (getModel().isRollover() ? new Color(29, 78, 216) : MAU_PRIMARY)
                        : MAU_PRIMARY_DISABLED);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g.dispose();
                super.paintComponent(g2);
            }
        };
        calcBtn.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));
        calcBtn.setForeground(Color.WHITE);
        calcBtn.setContentAreaFilled(false); calcBtn.setBorderPainted(false); calcBtn.setFocusPainted(false);
        calcBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calcBtn.setBorder(new EmptyBorder(11, 18, 11, 18));
        calcBtn.setPreferredSize(new Dimension(0, 44));

        calcBtn.addActionListener(e -> {
            // Validate
            String room = (String) cRoom.getSelectedItem();
            if (room == null || room.equals("Chọn phòng")) { shake(cRoom); return; }
            double dCu, dMoi, nCu, nMoi;
            try {
                dCu  = Double.parseDouble(fDienCu.getText().trim());
                dMoi = Double.parseDouble(fDienMoi.getText().trim());
                nCu  = Double.parseDouble(fNuocCu.getText().trim());
                nMoi = Double.parseDouble(fNuocMoi.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Số liệu không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (dMoi < dCu || nMoi < nCu) {
                JOptionPane.showMessageDialog(dlg, "Số mới phải ≥ số cũ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Tính
            double tienDien   = (dMoi - dCu) * 3500;
            double tienNuoc   = (nMoi - nCu) * 15000;
            double tienInternet = 100_000;
            double tienRac      = 20_000;
            double total        = tienDien + tienNuoc + tienInternet + tienRac;
            String month = (String) cMonth.getSelectedItem();
            String year  = fYear.getText().trim();

            // Mở dialog xem trước
            boolean saved = showPreviewDialog(dlg, room, month, year,
                    dCu, dMoi, tienDien, nCu, nMoi, tienNuoc, tienInternet, tienRac, total);
            if (saved) dlg.dispose();
        });

        botPanel.add(calcBtn, BorderLayout.CENTER);
        root.add(botPanel, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    // RENDERERS
    // ════════════════════════════════════════════════════════════════════════

    class StatusBadgeRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 18));
            p.setBackground(sel ? new Color(239, 246, 255) : MAU_CARD);
            p.setBorder(new MatteBorder(0, 0, 1, 0, MAU_BORDER));
            String val = v == null ? "" : v.toString();
            JLabel badge = new JLabel(val);
            badge.setFont(FONT_SMALL); badge.setOpaque(true);
            badge.setBorder(new EmptyBorder(3, 10, 3, 10));
            if ("Đã thanh toán".equals(val)) {
                badge.setBackground(MAU_GREEN_BG); badge.setForeground(MAU_GREEN_FG);
            } else {
                badge.setBackground(MAU_AMBER_BG); badge.setForeground(MAU_AMBER_FG);
            }
            p.add(badge); return p;
        }
    }

    class ActionRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 18));
            p.setBackground(sel ? new Color(239, 246, 255) : MAU_CARD);
            p.setBorder(new MatteBorder(0, 0, 1, 0, MAU_BORDER));
            JLabel e = new JLabel("✏"); e.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14)); e.setForeground(MAU_MUTED);
            JLabel d = new JLabel("🗑"); d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14)); d.setForeground(MAU_RED);
            p.add(e); p.add(d); return p;
        }
    }

    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private int curRow;
        ActionEditor(JTable table) {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 18));
            panel.setBackground(MAU_CARD);
            panel.setBorder(new MatteBorder(0, 0, 1, 0, MAU_BORDER));
            JButton paid = iconBtn("✅", MAU_MUTED);
            JButton del  = iconBtn("🗑", MAU_RED);
            paid.addActionListener(e -> {
                stopCellEditing();
                if (curRow < invoiceRows.size()) {
                    invoiceRows.get(curRow)[5] = "Đã thanh toán";
                    refreshTable();
                }
            });
            del.addActionListener(e -> {
                stopCellEditing();
                int c = JOptionPane.showConfirmDialog(panel.getTopLevelAncestor(),
                        "Xóa hóa đơn này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) { invoiceRows.remove(curRow); refreshTable(); }
            });
            panel.add(paid); panel.add(del);
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int row, int col) {
            curRow = row; return panel;
        }
        @Override public Object getCellEditorValue() { return "ACT"; }
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS
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
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        return btn;
    }

    private JButton iconBtn(String icon, Color fg) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(2, 4, 2, 4));
        return btn;
    }

    private JTextField makeField(String val) {
        JTextField f = new JTextField(val);
        f.setFont(FONT_PLAIN);
        f.setForeground(MAU_TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(MAU_BORDER, 1, true),
                new EmptyBorder(7, 11, 7, 11)));
        f.setPreferredSize(new Dimension(0, 38));
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(MAU_PRIMARY, 2, true),
                        new EmptyBorder(6, 10, 6, 10)));
            }
            @Override
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
        cb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(MAU_BORDER, 1, true),
                new EmptyBorder(3, 8, 3, 8)));
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

    // ── Dialog Xem trước hóa đơn ────────────────────────────────────────────
    private boolean showPreviewDialog(JDialog parent,
                                      String room, String month, String year,
                                      double dCu, double dMoi, double tienDien,
                                      double nCu, double nMoi, double tienNuoc,
                                      double tienInternet, double tienRac, double total) {

        final boolean[] saved = {false};
        String today = String.format("%d/%d/%d",
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.YEAR));

        JDialog prev = new JDialog(parent, "", true);
        prev.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(MAU_NEN);

        // ── Header: title + × (chỉ 1 cái) ──
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(MAU_NEN);
        hdr.setBorder(new EmptyBorder(16, 22, 12, 22));

        JLabel hTitle = new JLabel("Xem trước hóa đơn");
        hTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 16));
        hTitle.setForeground(MAU_TEXT);
        hdr.add(hTitle, BorderLayout.WEST);
        root.add(hdr, BorderLayout.NORTH);

        // ── Nội dung hóa đơn: dùng BorderLayout thay BoxLayout để tránh lệch ──
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(MAU_BORDER, 1, true),
                new EmptyBorder(28, 32, 28, 32)));

        // Phần trên: tiêu đề + tháng + info (dùng panel riêng)
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(Color.WHITE);

        // Tiêu đề căn giữa — dùng JPanel + FlowLayout.CENTER
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titlePanel.setBackground(Color.WHITE);
        JLabel invTitle = new JLabel("HÓA ĐƠN TIỀN PHÒNG");
        invTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 19));
        invTitle.setForeground(MAU_TEXT);
        titlePanel.add(invTitle);
        topSection.add(titlePanel);
        topSection.add(Box.createVerticalStrut(8));

        JPanel subPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        subPanel.setBackground(Color.WHITE);
        JLabel invSub = new JLabel("Tháng " + month.replace("T","") + "/" + year);
        invSub.setFont(FONT_PLAIN);
        invSub.setForeground(MAU_MUTED);
        subPanel.add(invSub);
        topSection.add(subPanel);
        topSection.add(Box.createVerticalStrut(18));

        // Phòng + Ngày lập
        JPanel infoRow = new JPanel(new BorderLayout());
        infoRow.setBackground(Color.WHITE);

        JPanel leftInfo = new JPanel();
        leftInfo.setBackground(Color.WHITE);
        leftInfo.setLayout(new BoxLayout(leftInfo, BoxLayout.Y_AXIS));
        JLabel lblPhong = new JLabel("<html><b>Phòng:</b> " + room + "</html>");
        lblPhong.setFont(FONT_PLAIN); lblPhong.setForeground(MAU_TEXT);
        JLabel lblKhach = new JLabel("<html><b>Khách thuê:</b> —</html>");
        lblKhach.setFont(FONT_PLAIN); lblKhach.setForeground(MAU_TEXT);
        leftInfo.add(lblPhong);
        leftInfo.add(Box.createVerticalStrut(4));
        leftInfo.add(lblKhach);

        JLabel lblNgay = new JLabel("<html><b>Ngày lập:</b> " + today + "</html>");
        lblNgay.setFont(FONT_PLAIN); lblNgay.setForeground(MAU_TEXT);

        infoRow.add(leftInfo, BorderLayout.WEST);
        infoRow.add(lblNgay, BorderLayout.EAST);
        topSection.add(infoRow);
        topSection.add(Box.createVerticalStrut(16));

        card.add(topSection, BorderLayout.NORTH);

        // ── Bảng chi tiết ──
        String[] cols = {"Khoản mục", "Chi tiết", "Thành tiền"};
        Object[][] data = {
                {"Tiền phòng", "—", "—"},
                {"Điện",   String.format("%.0f kWh × %sđ", dMoi-dCu, NF.format(3500)),  NF.format((long)tienDien)+"đ"},
                {"Nước",   String.format("%.1f m³ × %sđ",  nMoi-nCu, NF.format(15000)), NF.format((long)tienNuoc)+"đ"},
                {"Internet", "—", NF.format((long)tienInternet)+"đ"},
                {"Rác",      "—", NF.format((long)tienRac)+"đ"},
        };

        JTable tbl = new JTable(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl.setFont(FONT_PLAIN);
        tbl.setForeground(MAU_TEXT);
        tbl.setBackground(Color.WHITE);
        tbl.setRowHeight(40);
        tbl.setShowVerticalLines(true);
        tbl.setShowHorizontalLines(true);
        tbl.setGridColor(MAU_BORDER);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setSelectionBackground(Color.WHITE);
        tbl.setFocusable(false);
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(160);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(240);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(120);

        // Header
        JTableHeader th = tbl.getTableHeader();
        th.setFont(FONT_BOLD);
        th.setBackground(new Color(249, 250, 251));
        th.setForeground(MAU_TEXT);
        th.setBorder(new LineBorder(MAU_BORDER, 1));
        th.setPreferredSize(new Dimension(0, 40));
        th.setReorderingAllowed(false);

        DefaultTableCellRenderer rLeft = new DefaultTableCellRenderer();
        rLeft.setBorder(new EmptyBorder(0, 14, 0, 12));
        DefaultTableCellRenderer rRight = new DefaultTableCellRenderer();
        rRight.setHorizontalAlignment(SwingConstants.RIGHT);
        rRight.setBorder(new EmptyBorder(0, 12, 0, 14));
        tbl.getColumnModel().getColumn(0).setCellRenderer(rLeft);
        tbl.getColumnModel().getColumn(1).setCellRenderer(rRight);
        tbl.getColumnModel().getColumn(2).setCellRenderer(rRight);

        DefaultTableCellRenderer hLeft = new DefaultTableCellRenderer();
        hLeft.setBackground(new Color(249, 250, 251)); hLeft.setForeground(MAU_TEXT);
        hLeft.setFont(FONT_BOLD); hLeft.setBorder(new EmptyBorder(0, 14, 0, 12));
        DefaultTableCellRenderer hRight = new DefaultTableCellRenderer();
        hRight.setBackground(new Color(249, 250, 251)); hRight.setForeground(MAU_TEXT);
        hRight.setFont(FONT_BOLD); hRight.setHorizontalAlignment(SwingConstants.RIGHT);
        hRight.setBorder(new EmptyBorder(0, 12, 0, 14));
        tbl.getColumnModel().getColumn(0).setHeaderRenderer(hLeft);
        tbl.getColumnModel().getColumn(1).setHeaderRenderer(hRight);
        tbl.getColumnModel().getColumn(2).setHeaderRenderer(hRight);

        // Table không wrap scroll, cố định chiều cao vừa khít
        int tableH = tbl.getRowHeight() * data.length + 42; // 42 = header height
        tbl.setPreferredScrollableViewportSize(new Dimension(580, tableH));
        JScrollPane tSp = new JScrollPane(tbl,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tSp.setBorder(new LineBorder(MAU_BORDER, 1));
        tSp.setPreferredSize(new Dimension(580, tableH + 2));
        tSp.setMaximumSize(new Dimension(Integer.MAX_VALUE, tableH + 2));
        tSp.setMinimumSize(new Dimension(100, tableH + 2));

        // TỔNG CỘNG ngay dưới bảng, liền kề
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setBackground(new Color(249, 250, 251));
        totalRow.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 1, 1, 1, MAU_BORDER),
                new EmptyBorder(13, 12, 13, 12)));

        JLabel lblTC = new JLabel("TỔNG CỘNG");
        lblTC.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));
        lblTC.setForeground(MAU_TEXT);
        JLabel lblAmt = new JLabel(NF.format((long) total) + "đ");
        lblAmt.setFont(new Font("Be Vietnam Pro", Font.BOLD, 15));
        lblAmt.setForeground(MAU_TEXT);
        totalRow.add(lblTC,  BorderLayout.WEST);
        totalRow.add(lblAmt, BorderLayout.EAST);

        // Gộp table + tổng cộng vào 1 panel
        JPanel tableSection = new JPanel(new BorderLayout());
        tableSection.setBackground(Color.WHITE);
        tableSection.add(tSp, BorderLayout.CENTER);
        tableSection.add(totalRow, BorderLayout.SOUTH);

        card.add(tableSection, BorderLayout.CENTER);
        // (bỏ cardBottom thừa)

        // Bọc card trong padding
        JPanel cardWrap = new JPanel(new BorderLayout());
        cardWrap.setBackground(MAU_NEN);
        cardWrap.setBorder(new EmptyBorder(0, 24, 12, 24));
        cardWrap.add(card, BorderLayout.CENTER);
        root.add(cardWrap, BorderLayout.CENTER);

        // ── Footer: 3 nút căn giữa ──
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 14));
        footer.setBackground(MAU_NEN);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, MAU_BORDER));

        JButton btnPrint = makeOutlineIconButton("🖨", "In hóa đơn");
        JButton btnPDF   = makeOutlineIconButton("⬇", "Tải PDF");
        JButton btnSave  = makePrimaryButton("🗒  Lưu hóa đơn");

        btnPrint.addActionListener(e ->
                JOptionPane.showMessageDialog(prev, "Tính năng in sẽ được cập nhật!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
        btnPDF.addActionListener(e ->
                JOptionPane.showMessageDialog(prev, "Tính năng xuất PDF sẽ được cập nhật!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
        btnSave.addActionListener(e -> {
            invoiceRows.add(new Object[]{
                    room, month + "/" + year,
                    String.format("%.0f kWh", dMoi - dCu),
                    String.format("%.1f m³",  nMoi - nCu),
                    NF.format((long) total) + "đ",
                    "Chưa thanh toán", "ACT"
            });
            refreshTable();
            saved[0] = true;
            prev.dispose();
        });

        footer.add(btnPrint);
        footer.add(btnPDF);
        footer.add(btnSave);
        root.add(footer, BorderLayout.SOUTH);

        prev.setContentPane(root);
        prev.pack();
        prev.setMinimumSize(new Dimension(640, prev.getHeight()));
        prev.setLocationRelativeTo(parent);
        prev.setVisible(true);
        return saved[0];
    }

    /** Nút outline có icon + text */
    private JButton makeOutlineIconButton(String icon, String text) {
        JButton btn = new JButton(icon + "  " + text);
        btn.setFont(FONT_PLAIN);
        btn.setForeground(MAU_TEXT);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(MAU_BORDER, 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Rung nhẹ component khi validate lỗi */
    private void shake(Component comp) {
        Point origin = comp.getLocation();
        javax.swing.Timer timer = new javax.swing.Timer(30, null);
        int[] step = {0};
        int[] offsets = {-6, 6, -4, 4, -2, 2, 0};
        timer.addActionListener(e -> {
            if (step[0] < offsets.length) {
                comp.setLocation(origin.x + offsets[step[0]++], origin.y);
            } else {
                comp.setLocation(origin);
                timer.stop();
            }
        });
        timer.start();
    }
}