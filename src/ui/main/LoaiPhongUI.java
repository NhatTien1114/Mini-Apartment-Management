package ui.main;

import dao.LoaiPhongDAO;
import entity.LoaiPhong;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import ui.util.AppColors;
import ui.util.MessageDialog;
import ui.util.PrimaryButton;
import ui.util.ValidationPopup;

public class LoaiPhongUI {
    private final Color MAU_NEN = AppColors.SLATE_100;
    private final Color MAU_CARD = AppColors.WHITE;
    private final Color MAU_BORDER = AppColors.SLATE_200;
    private final Color MAU_TEXT = AppColors.SLATE_900;
    private final Color MAU_MUTED = AppColors.SLATE_500;
    private final Color MAU_RED = AppColors.RED_500;
    private final Color MAU_HOVER_ROW = AppColors.SLATE_50;

    private final Font FONT_TITLE = new Font("Be Vietnam Pro", Font.BOLD, 22);
    private final Font FONT_BOLD = new Font("Be Vietnam Pro", Font.BOLD, 13);
    private final Font FONT_PLAIN = new Font("Be Vietnam Pro", Font.PLAIN, 13);
    private final Font FONT_SMALL = new Font("Be Vietnam Pro", Font.PLAIN, 12);

    private final PrimaryButton primaryButton = new PrimaryButton();

    private DefaultTableModel tableModel;
    private JTable table;
    private final LoaiPhongDAO dao = new LoaiPhongDAO();
    private List<LoaiPhong> dsLoaiPhong;

    public JPanel getPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 0));
        pnl.setBorder(new EmptyBorder(24, 24, 24, 24));
        pnl.setBackground(MAU_NEN);

        pnl.add(buildTopBar(), BorderLayout.NORTH);
        pnl.add(buildTableCard(), BorderLayout.CENTER);

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

        JLabel title = new JLabel("Quản lý loại phòng");
        title.setFont(FONT_TITLE);
        title.setForeground(MAU_TEXT);
        bar.add(title, BorderLayout.WEST);

        JButton btnAdd = primaryButton.makePrimaryButton("Thêm loại phòng");
        btnAdd.addActionListener(e -> showDialog(null));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(btnAdd);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(MAU_CARD);
        card.setBorder(new LineBorder(MAU_BORDER, 1, true));

        String[] cols = { "Mã loại phòng", "Tên loại phòng" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
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

        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);

        table.getColumnModel().getColumn(0).setCellRenderer(plainPaddedRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(boldPaddedRenderer());

        // --- Right-click context menu ---
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem miEdit = new JMenuItem("Xem/Sửa thông tin");
        JMenuItem miDelete = new JMenuItem("Xóa");
        miDelete.setForeground(new Color(239, 68, 68));
        contextMenu.add(miEdit);
        contextMenu.add(miDelete);

        miEdit.addActionListener(ev -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < dsLoaiPhong.size())
                showDialog(dsLoaiPhong.get(row));
        });
        miDelete.addActionListener(ev -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= dsLoaiPhong.size())
                return;
            LoaiPhong target = dsLoaiPhong.get(row);
            int confirm = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(table),
                    "Bạn có chắc chắn muốn xóa loại phòng: " + target.getTenLoaiPhong() + " ?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                String err = dao.xoa(target.getMaLoaiPhong());
                if (err == null) {
                    MessageDialog.show(null, "Đã xóa", "Đã xóa loại phòng thành công.",
                            MessageDialog.MessageType.SUCCESS);
                    loadTable();
                } else {
                    MessageDialog.show(null, "Báo lỗi", err,
                            MessageDialog.MessageType.ERROR);
                }
            }
        });

        table.setComponentPopupMenu(contextMenu);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(MAU_CARD);

        card.add(sp, BorderLayout.CENTER);
        loadTable();
        return card;
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        dsLoaiPhong = dao.layTatCa();
        for (LoaiPhong lp : dsLoaiPhong) {
            tableModel.addRow(new Object[] {
                    lp.getMaLoaiPhong(),
                    lp.getTenLoaiPhong()
            });
        }
        table.revalidate();
        table.repaint();
    }

    private void showDialog(LoaiPhong editLoaiPhong) {
        boolean editing = (editLoaiPhong != null);
        Window owner = SwingUtilities.getWindowAncestor(table);

        JDialog dlg = new JDialog(
                owner instanceof Frame ? (Frame) owner : null,
                editing ? "Sửa loại phòng" : "Thêm loại phòng", true);
        dlg.setUndecorated(true);
        dlg.setSize(420, 200);
        dlg.setLocationRelativeTo(owner);
        dlg.setBackground(new Color(0, 0, 0, 0));

        ui.util.RoundedPanel root = new ui.util.RoundedPanel(16);
        root.setLayout(new BorderLayout());
        root.setBackground(MAU_CARD);
        root.setBorder(new EmptyBorder(22, 28, 22, 28));

        // HEADER
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(MAU_CARD);
        headerRow.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel dlgTitle = new JLabel(editing ? "Sửa loại phòng" : "Thêm loại phòng");
        dlgTitle.setFont(new Font("Inter", Font.BOLD, 18));
        dlgTitle.setForeground(MAU_TEXT);

        JLabel btnClose = new JLabel("<html><span style='font-size:16px'>✕</span></html>");
        btnClose.setForeground(MAU_MUTED);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dlg.dispose();
            }

            public void mouseEntered(MouseEvent e) {
                btnClose.setForeground(MAU_RED);
            }

            public void mouseExited(MouseEvent e) {
                btnClose.setForeground(MAU_MUTED);
            }
        });

        headerRow.add(dlgTitle, BorderLayout.WEST);
        headerRow.add(btnClose, BorderLayout.EAST);

        // FIELD
        ui.util.RoundedTextField fTen = new ui.util.RoundedTextField(6);
        fTen.setPlaceholder("Nhập tên loại phòng, Vd: Phòng Đơn");
        fTen.setPreferredSize(new Dimension(0, 42));
        if (editing)
            fTen.setText(editLoaiPhong.getTenLoaiPhong());

        JPanel fieldPanel = wrapField("Tên loại phòng", fTen);

        // BUTTONS
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
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Inter", Font.BOLD, 13));
        btnSave.setPreferredSize(new Dimension(100, 38));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> {
            String ten = fTen.getText().trim();
            if (ten.isEmpty()) {
                ValidationPopup.show(fTen, "Tên loại phòng không được để trống!");
                return;
            }

            if (editing) {
                String err = dao.capNhat(editLoaiPhong.getMaLoaiPhong(), ten);
                if (err == null) {
                    MessageDialog.show(dlg, "Thành Công", "Đã cập nhật loại phòng.", MessageDialog.MessageType.SUCCESS);
                    dlg.dispose();
                    loadTable();
                } else {
                    MessageDialog.show(dlg, "Lỗi", err, MessageDialog.MessageType.ERROR);
                }
            } else {
                String err = dao.them(ten);
                if (err == null) {
                    MessageDialog.show(dlg, "Thêm Thành Công", "Đã tạo loại phòng mới.",
                            MessageDialog.MessageType.SUCCESS);
                    dlg.dispose();
                    loadTable();
                } else {
                    MessageDialog.show(dlg, "Lỗi", err, MessageDialog.MessageType.ERROR);
                }
            }
        });

        btnRow.add(btnCancel);
        btnRow.add(btnSave);

        root.add(headerRow, BorderLayout.NORTH);
        root.add(fieldPanel, BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);

        // ESC to close
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                "ESC_CLOSE");
        root.getActionMap().put("ESC_CLOSE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dlg.dispose();
            }
        });

        dlg.setContentPane(root);

        if (owner instanceof JFrame frame) {
            Component oldGlassPane = frame.getGlassPane();
            JPanel overlay = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(new Color(0, 0, 0, 110));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            overlay.setOpaque(false);
            overlay.addMouseListener(new MouseAdapter() {
            });
            frame.setGlassPane(overlay);
            overlay.setVisible(true);

            dlg.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    overlay.setVisible(false);
                    frame.setGlassPane(oldGlassPane);
                }
            });
        }

        dlg.setVisible(true);
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
