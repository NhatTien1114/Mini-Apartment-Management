package ui.main;

import dao.DichVuDAO;
import entity.DichVu;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import ui.util.AppColors;
import ui.util.MessageDialog;
import ui.util.PrimaryButton;
import ui.util.ValidationPopup;

public class DichVuUI {
    private final Color MAU_NEN = AppColors.SLATE_100;
    private final Color MAU_CARD = AppColors.WHITE;
    private final Color MAU_BORDER = AppColors.SLATE_200;
    private final Color MAU_TEXT = AppColors.SLATE_900;
    private final Color MAU_MUTED = AppColors.SLATE_500;
    private final Color MAU_PRIMARY = AppColors.PRIMARY;
    private final Color MAU_RED = AppColors.RED_500;
    private final Color MAU_HOVER_ROW = AppColors.SLATE_50;
    private final Color MAU_WARN_BG = new Color(254, 243, 199); // Amber 100
    private final Color MAU_WARN_FG = new Color(180, 83, 9); // Amber 700
    private final Color MAU_GREEN_BG = new Color(220, 252, 231); // Green 100
    private final Color MAU_GREEN_FG = new Color(22, 163, 74); // Green 600

    private final Font FONT_TITLE = new Font("Be Vietnam Pro", Font.BOLD, 22);
    private final Font FONT_BOLD = new Font("Be Vietnam Pro", Font.BOLD, 13);
    private final Font FONT_PLAIN = new Font("Be Vietnam Pro", Font.PLAIN, 13);
    private final Font FONT_SMALL = new Font("Be Vietnam Pro", Font.PLAIN, 12);

    private final PrimaryButton primaryButton = new PrimaryButton();
    private final NumberFormat NF = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private DefaultTableModel tableModel;
    private JTable table;
    private final DichVuDAO dao = new DichVuDAO();
    private List<DichVu> dsDichVu;

    public JPanel getPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 0));
        pnl.setBorder(new EmptyBorder(24, 24, 24, 24));
        pnl.setBackground(MAU_NEN);

        pnl.add(buildTopBar(), BorderLayout.NORTH);
        pnl.add(buildTableCard(), BorderLayout.CENTER);

        // Tự động load lại dữ liệu bảng khi Panel (Tab) được focus vào màn hình
        pnl.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadTable();
            }
        });

        return pnl;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(MAU_NEN);
        bar.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Quản lý dịch vụ");
        title.setFont(FONT_TITLE);
        title.setForeground(MAU_TEXT);
        bar.add(title, BorderLayout.WEST);

        JButton btnAdd = primaryButton.makePrimaryButton("Thêm dịch vụ");
        btnAdd.addActionListener(e -> showDialog(null));
        bar.add(btnAdd, BorderLayout.EAST);

        return bar;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(MAU_CARD);
        card.setBorder(new LineBorder(MAU_BORDER, 1, true));

        String[] cols = { "Mã DV", "Tên dịch vụ", "Đơn vị tính", "Đơn giá", "Thao tác" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 4; // Cột thao tác
            }
        };

        table = new JTable(tableModel) {
            int hoverRow = -1;
            {
                addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        int r = rowAtPoint(e.getPoint());
                        if (r != hoverRow) {
                            hoverRow = r;
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
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row))
                    c.setBackground(hoverRow == row ? MAU_HOVER_ROW : MAU_CARD);
                return c;
            }
        };

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SMALL);
        header.setForeground(MAU_MUTED);
        header.setBackground(MAU_CARD);
        header.setBorder(new MatteBorder(0, 0, 1, 0, MAU_BORDER));
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer hdrRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(MAU_CARD);
                setForeground(MAU_MUTED);
                setFont(FONT_SMALL);
                setBorder(new EmptyBorder(0, 16, 0, 8));
                return this;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setHeaderRenderer(hdrRenderer);

        table.setRowHeight(56);
        table.setFont(FONT_PLAIN);
        table.setBackground(MAU_CARD);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(220);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);

        table.getColumnModel().getColumn(0).setCellRenderer(plainPaddedRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(boldPaddedRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(plainPaddedRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new PriceBadgeRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ActionEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(MAU_CARD);

        card.add(sp, BorderLayout.CENTER);
        loadTable();
        return card;
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        dsDichVu = dao.layTatCa();
        for (DichVu dv : dsDichVu) {
            tableModel.addRow(new Object[] {
                    dv.getMaDichVu(), 
                    dv.getTenDichVu(), 
                    dv.getDonVi(),
                    dv.getDonGia(),
                    "ACT"
            });
        }
    }

    private void showDialog(DichVu editDichVu) {
        boolean editing = (editDichVu != null);
        Window owner = SwingUtilities.getWindowAncestor(table);
        
        // 1) Define Custom Undecorated Dialog (giống hình vẽ)
        JDialog dlg = new JDialog(
                owner instanceof Frame ? (Frame) owner : null,
                editing ? "Sửa dịch vụ" : "Thêm dịch vụ", true);
        dlg.setUndecorated(true);
        dlg.setSize(480, 240); // Rộng rãi cho grid
        dlg.setLocationRelativeTo(owner);
        dlg.setBackground(new Color(0, 0, 0, 0)); // Transparent bg for rounded corners

        // ROOT PANEL (Rounded)
        ui.util.RoundedPanel root = new ui.util.RoundedPanel(16);
        root.setLayout(new BorderLayout());
        root.setBackground(MAU_CARD);
        root.setBorder(new EmptyBorder(22, 28, 22, 28));

        // HEADER
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(MAU_CARD);
        headerRow.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel dlgTitle = new JLabel(editing ? "Sửa dịch vụ" : "Thêm dịch vụ");
        dlgTitle.setFont(new Font("Inter", Font.BOLD, 18));
        dlgTitle.setForeground(MAU_TEXT);
        
        // Nút tắt nhỏ 'X'
        JLabel btnClose = new JLabel("<html><span style='font-size:16px'>✕</span></html>");
        btnClose.setForeground(MAU_MUTED);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dlg.dispose(); }
            public void mouseEntered(MouseEvent e) { btnClose.setForeground(MAU_RED); }
            public void mouseExited(MouseEvent e) { btnClose.setForeground(MAU_MUTED); }
        });

        headerRow.add(dlgTitle, BorderLayout.WEST);
        headerRow.add(btnClose, BorderLayout.EAST);

        // GRID FIELDS (1 dòng, 2 cột theo ảnh minh hoạ của Tab Dịch Vụ)
        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0)); 
        grid.setBackground(MAU_CARD);

        ui.util.RoundedTextField fTen = new ui.util.RoundedTextField(6);
        fTen.setPlaceholder("Nhập tên, Vd: Rác");
        fTen.setPreferredSize(new Dimension(0, 42)); // Ô nhập to bự
        if (editing) fTen.setText(editDichVu.getTenDichVu());
        
        ui.util.RoundedTextField fDonVi = new ui.util.RoundedTextField(6);
        fDonVi.setPlaceholder("Vd: Tháng");
        fDonVi.setPreferredSize(new Dimension(0, 42));
        if (editing) fDonVi.setText(editDichVu.getDonVi());

        grid.add(wrapField("Tên dịch vụ", fTen));
        grid.add(wrapField("Đơn vị", fDonVi));

        // BUTTONS BOTTOM
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(MAU_CARD);
        btnRow.setBorder(new EmptyBorder(24, 0, 0, 0));

        ui.util.RoundedButton btnCancel = new ui.util.RoundedButton("Hủy", 6);
        btnCancel.setBackground(MAU_CARD);
        btnCancel.setForeground(MAU_MUTED);
        btnCancel.setFont(new Font("Inter", Font.BOLD, 13));
        btnCancel.setBorder(new LineBorder(MAU_BORDER, 1, true));
        btnCancel.setPreferredSize(new Dimension(86, 38));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dlg.dispose());

        ui.util.RoundedButton btnSave = new ui.util.RoundedButton(editing ? "Lưu lại" : "Thêm", 6);
        btnSave.setBackground(new Color(37, 99, 235)); // Tailwind blue 600
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Inter", Font.BOLD, 13));
        btnSave.setPreferredSize(new Dimension(100, 38));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> {
            String ten = fTen.getText().trim();
            String donvi = fDonVi.getText().trim();
            
            if (ten.isEmpty()) {
                ValidationPopup.show(fTen, "Tên dịch vụ không được để trống!");
                return;
            }
            if (donvi.isEmpty()) {
                ValidationPopup.show(fDonVi, "Vui lòng nhập đơn vị tính!");
                return;
            }

            if (editing) {
                editDichVu.setTenDichVu(ten);
                editDichVu.setDonVi(donvi);
                if (dao.updateDichVu(editDichVu)) {
                    MessageDialog.show(dlg, "Thành Công", "Đã cập nhật dịch vụ.", MessageDialog.MessageType.SUCCESS);
                    loadTable();
                    dlg.dispose();
                } else {
                    MessageDialog.show(dlg, "Lỗi", "Lỗi CSDL.", MessageDialog.MessageType.ERROR);
                }
            } else {
                DichVu newDv = new DichVu();
                newDv.setTenDichVu(ten);
                newDv.setDonVi(donvi);
                if (dao.insertDichVu(newDv)) {
                    MessageDialog.show(dlg, "Thêm Thành Công", "Đã tạo dịch vụ mới.", MessageDialog.MessageType.SUCCESS);
                    loadTable();
                    dlg.dispose();
                } else {
                    MessageDialog.show(dlg, "Lỗi", "Lỗi CSDL.", MessageDialog.MessageType.ERROR);
                }
            }
        });

        btnRow.add(btnCancel);
        btnRow.add(btnSave);

        root.add(headerRow, BorderLayout.NORTH);
        root.add(grid, BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);

        // Map Keyboard ESC to close
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC_CLOSE");
        root.getActionMap().put("ESC_CLOSE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dlg.dispose();
            }
        });

        dlg.setContentPane(root);

        // 2) Dark Overlay Logic (Tối màu xung quanh)
        if (owner instanceof JFrame frame) {
            Component oldGlassPane = frame.getGlassPane();
            JPanel overlay = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(new Color(0, 0, 0, 110)); // Dark overlay (opacity 43%)
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            overlay.setOpaque(false);
            // Block all mouse clicks on overlay
            overlay.addMouseListener(new MouseAdapter() {});
            frame.setGlassPane(overlay);
            overlay.setVisible(true);

            // Hide overlay when dialog is disposed
            dlg.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    overlay.setVisible(false);
                    frame.setGlassPane(oldGlassPane);
                }
            });
        }

        // Show Dialog safely
        dlg.setVisible(true);
    }

    class PriceBadgeRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 18));
            p.setBackground(sel ? new Color(239, 246, 255) : MAU_CARD);
            p.setBorder(new MatteBorder(0, 0, 1, 0, MAU_BORDER));
            
            JLabel badge = new JLabel();
            badge.setFont(FONT_SMALL);
            badge.setOpaque(true);
            badge.setBorder(new EmptyBorder(4, 10, 4, 10));
            
            if (v == null) {
                badge.setText("Chưa cấu hình");
                badge.setBackground(MAU_WARN_BG);
                badge.setForeground(MAU_WARN_FG);
            } else {
                badge.setText(NF.format(v) + "đ");
                badge.setBackground(MAU_GREEN_BG);
                badge.setForeground(MAU_GREEN_FG);
            }
            p.add(badge);
            return p;
        }
    }

    class ActionRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 18));
            p.setBackground(sel ? new Color(239, 246, 255) : MAU_CARD);
            p.setBorder(new MatteBorder(0, 0, 1, 0, MAU_BORDER));
            p.add(makeIconLabel("✏", MAU_MUTED));
            p.add(makeIconLabel("🗑", MAU_RED));
            return p;
        }
    }

    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private int curRow;

        ActionEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 18));
            panel.setBackground(MAU_CARD);
            panel.setBorder(new MatteBorder(0, 0, 1, 0, MAU_BORDER));

            JButton btnEdit = makeIconButton("✏", MAU_MUTED);
            JButton btnDel = makeIconButton("🗑", MAU_RED);

            btnEdit.addActionListener(e -> {
                stopCellEditing();
                showDialog(dsDichVu.get(curRow));
            });
            btnDel.addActionListener(e -> {
                stopCellEditing();
                DichVu target = dsDichVu.get(curRow);
                
                // Show Custom MessageDialog
                int confirm = JOptionPane.showConfirmDialog(
                        panel.getTopLevelAncestor(),
                        "Bạn có chắc chắn muốn xóa dịch vụ: " + target.getTenDichVu() + " ?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (dao.deleteDichVu(target.getMaDichVu())) {
                        MessageDialog.show(null, "Đã xóa", "Đã xóa dịch vụ thành công.", MessageDialog.MessageType.SUCCESS);
                        loadTable();
                    } else {
                        MessageDialog.show(null, "Báo lỗi", "Không thể xóa do ràng buộc dữ liệu.", MessageDialog.MessageType.ERROR);
                    }
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

        @Override
        public Object getCellEditorValue() { return "ACT"; }
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

    private JPanel wrapField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(MAU_CARD);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(MAU_MUTED);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private TableCellRenderer boldPaddedRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setFont(FONT_BOLD);
                setForeground(MAU_TEXT);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, MAU_BORDER),
                        new EmptyBorder(0, 16, 0, 8)));
                return this;
            }
        };
    }

    private TableCellRenderer plainPaddedRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setFont(FONT_PLAIN);
                setForeground(MAU_MUTED);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, MAU_BORDER),
                        new EmptyBorder(0, 16, 0, 8)));
                return this;
            }
        };
    }
}