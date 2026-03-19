package ui.main;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.Calendar;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import ui.util.RoundedButton;
import ui.util.RoundedPanel;
import ui.util.RoundedTextField;

public class HopDongUI {
    private final Color MAU_NEN = new Color(248, 250, 252); 
    private final Color MAU_CARD = Color.WHITE;
    private final Color MAU_TEXT = new Color(15, 23, 42); 
    private final Color MAU_SUBTEXT = new Color(100, 116, 139);
    private final Color BORDER_COLOR = new Color(226, 232, 240);
    
    private JPanel pnlRoot;
    private DefaultTableModel model;
    private JTable table;

    public JPanel getPanel() {
        pnlRoot = new JPanel(new BorderLayout(0, 24));
        pnlRoot.setBorder(new EmptyBorder(32, 32, 32, 32));
        pnlRoot.setBackground(MAU_NEN);
        
        // --- HEADER ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Quản lý hợp đồng");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 20));
        lblTitle.setForeground(MAU_TEXT);
        
        RoundedButton btnAdd = new RoundedButton(" + Tạo hợp đồng ", 8);
        btnAdd.setFont(new Font("Inter", Font.BOLD, 14));
        btnAdd.setBackground(new Color(34, 88, 195)); 
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setBorder(new EmptyBorder(8, 16, 8, 16)); 
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        applyButtonHover(btnAdd, new Color(34, 88, 195), new Color(23, 62, 138));
        btnAdd.addActionListener(e -> showContractForm(false, -1));
        
        JPanel pnlHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlHeaderRight.setOpaque(false);
        pnlHeaderRight.add(btnAdd);

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(pnlHeaderRight, BorderLayout.EAST);
        
        pnlRoot.add(pnlHeader, BorderLayout.NORTH);
        
        // --- MAIN CONTENT ---
        JPanel pnlMainContent = new JPanel(new BorderLayout(0, 20)); 
        pnlMainContent.setOpaque(false);
        
        // --- Search Bar ---
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlToolbar.setOpaque(false);
        
        RoundedTextField txtSearch = new RoundedTextField(8) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(148, 163, 184)); 
                g2.setStroke(new BasicStroke(1.5f));
                int cx = 18; int cy = getHeight()/2 - 2;
                g2.drawOval(cx-4, cy-4, 8, 8); 
                g2.drawLine(cx+2, cy+2, cx+7, cy+7); 
                
                if(isFocusOwner()) {
                    g2.setColor(new Color(37,99,235));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                }
                g2.dispose();
            }
        };
        txtSearch.setBorder(new EmptyBorder(8, 36, 8, 12));
        txtSearch.setPlaceholder("Tìm theo tên hoặc phòng...");
        txtSearch.setPreferredSize(new Dimension(320, 40));
        txtSearch.setBackground(Color.WHITE);
        txtSearch.addFocusListener(new FocusAdapter(){
            public void focusGained(FocusEvent e) { txtSearch.repaint(); }
            public void focusLost(FocusEvent e) { txtSearch.repaint(); }
        });
        
        pnlToolbar.add(txtSearch);
        pnlMainContent.add(pnlToolbar, BorderLayout.NORTH);
        
        // --- Data Table Card ---
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(MAU_CARD);
        card.setLayout(new BorderLayout());
        
        JPanel cardMargin = new JPanel(new BorderLayout());
        cardMargin.setOpaque(false);
        cardMargin.setBorder(new EmptyBorder(4, 24, 24, 24));
        
        String[] columnNames = {"Mã HĐ", "Phòng", "Khách thuê", "Ngày bắt đầu", "Ngày kết thúc", "Tiền cọc", "Tiền thuê/tháng", "Trạng thái", ""};
        Object[][] data = {
            {"HD001", "T1.02", "Nguyễn Văn An", "01/15/2025", "01/15/2026", "7.000.000đ", "3.500.000đ", "Đang hiệu lực", ""},
            {"HD002", "T1.03", "Trần Thị Bình", "03/01/2025", "03/01/2026", "8.000.000đ", "4.000.000đ", "Đang hiệu lực", ""},
            {"HD003", "T2.01", "Lê Hoàng Cường", "02/10/2025", "02/10/2026", "7.000.000đ", "3.500.000đ", "Đang hiệu lực", ""},
            {"HD004", "T2.02", "Phạm Minh Dũng", "04/01/2025", "04/01/2026", "7.000.000đ", "3.500.000đ", "Đang hiệu lực", ""},
            {"HD005", "T2.03", "Hoàng Thị Em", "05/15/2025", "05/15/2026", "8.000.000đ", "4.000.000đ", "Đang hiệu lực", ""},
            {"HD006", "T3.01", "Đặng Thị Giang", "01/01/2025", "01/01/2026", "7.000.000đ", "3.500.000đ", "Sắp hết hạn", ""}
        };
        
        model = new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) { return column == 8; }
        };
        
        table = new JTable(model);
        table.setRowHeight(56);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);
        table.setSelectionBackground(new Color(241, 245, 249)); 
        table.setBorder(null);
        
        table.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if(row > -1) { table.setRowSelectionInterval(row, row); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) { table.clearSelection(); }
        });
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
        
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t,v,isSel,hasFocus,r,c);
                l.setFont(new Font("Inter", Font.BOLD, 13));
                l.setForeground(MAU_SUBTEXT);
                l.setBackground(MAU_CARD);
                l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                    new EmptyBorder(16, 16, 16, 8)
                ));
                return l;
            }
        });
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t,v,isSel,hasFocus,r,c);
                l.setFont(new Font("Inter", Font.PLAIN, 13)); 
                l.setForeground(MAU_TEXT);
                l.setBackground(isSel ? t.getSelectionBackground() : MAU_CARD);
                l.setOpaque(true);
                
                if (c == 0) { 
                    l.setFont(new Font("Inter", Font.BOLD, 13));
                } else if (c == 1) { 
                    l.setForeground(new Color(37, 99, 235)); 
                    l.setFont(new Font("Inter", Font.BOLD, 13));
                }
                
                l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)), 
                    new EmptyBorder(0, 16, 0, 8)
                ));
                return l;
            }
        });
        
        table.getColumnModel().getColumn(7).setCellRenderer(new TableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                JPanel pnl = new JPanel(new GridBagLayout());
                pnl.setBackground(isSel ? t.getSelectionBackground() : MAU_CARD);
                pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));
                
                String status = v != null ? v.toString() : "";
                boolean active = status.equals("Đang hiệu lực");
                Color bg = active ? new Color(34, 88, 195) : new Color(226, 232, 240); 
                Color fg = active ? Color.WHITE : new Color(71, 85, 105);
                
                JLabel lbl = new JLabel(status) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D)g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(bg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14); 
                        super.paintComponent(g);
                        g2.dispose();
                    }
                };
                lbl.setForeground(fg);
                lbl.setFont(new Font("Inter", Font.BOLD, 11)); 
                lbl.setBorder(new EmptyBorder(4, 10, 4, 10)); 
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0;
                gbc.insets = new Insets(0, 16, 0, 0); 
                pnl.add(lbl, gbc);
                return pnl;
            }
        });
        
        table.getColumnModel().getColumn(8).setCellRenderer(new TableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                ActionPanel pnl = new ActionPanel(true, r);
                pnl.setBackground(isSel ? t.getSelectionBackground() : MAU_CARD);
                pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));
                return pnl;
            }
        });
        table.getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private ActionPanel currentPanel;
            @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean isSel, int r, int c) {
                currentPanel = new ActionPanel(false, r);
                currentPanel.setBackground(isSel ? t.getSelectionBackground() : MAU_CARD);
                return currentPanel;
            }
            @Override public Object getCellEditorValue() { return ""; }
        });
        
        table.getColumnModel().getColumn(0).setPreferredWidth(70);  
        table.getColumnModel().getColumn(1).setPreferredWidth(60);  
        table.getColumnModel().getColumn(2).setPreferredWidth(140); 
        table.getColumnModel().getColumn(3).setPreferredWidth(110); 
        table.getColumnModel().getColumn(4).setPreferredWidth(110); 
        table.getColumnModel().getColumn(5).setPreferredWidth(100); 
        table.getColumnModel().getColumn(6).setPreferredWidth(110); 
        table.getColumnModel().getColumn(7).setPreferredWidth(125); 
        table.getColumnModel().getColumn(8).setPreferredWidth(90);  

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(MAU_CARD);

        cardMargin.add(scrollPane, BorderLayout.CENTER);
        card.add(cardMargin, BorderLayout.CENTER);
        
        pnlMainContent.add(card, BorderLayout.CENTER);
        pnlRoot.add(pnlMainContent, BorderLayout.CENTER);
        
        return pnlRoot;
    }
    
    private void applyButtonHover(RoundedButton btn, Color normal, Color hover) {
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e) { btn.setBackground(normal); }
        });
    }

    private void showContractForm(boolean isEdit, int row) {
        Window parent = SwingUtilities.getWindowAncestor(pnlRoot);
        
        JDialog overlay = new JDialog(parent);
        overlay.setUndecorated(true);
        try { overlay.setBackground(new Color(0, 0, 0, 100)); } catch(Exception e){}
        if(parent != null) overlay.setBounds(parent.getBounds());
        overlay.setFocusableWindowState(false);
        
        JDialog dialog = new JDialog(parent, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(pnlRoot);
        
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC");
        dialog.getRootPane().getActionMap().put("ESC", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { dialog.dispose(); }
        });
        
        RoundedPanel pnlBg = new RoundedPanel(16);
        pnlBg.setBackground(Color.WHITE);
        pnlBg.setLayout(new BorderLayout());
        pnlBg.setBorder(new EmptyBorder(16, 20, 20, 20));
        
        JPanel pnlHead = new JPanel(new BorderLayout());
        pnlHead.setOpaque(false);
        JLabel lblTitle = new JLabel(isEdit ? "Sửa hợp đồng" : "Tạo hợp đồng");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 18));
        lblTitle.setForeground(MAU_TEXT);
        
        JButton btnClose = new JButton("X") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? MAU_TEXT : MAU_SUBTEXT);
                g2.setStroke(new BasicStroke(1.5f));
                int cx=getWidth()/2, cy=getHeight()/2;
                g2.drawLine(cx-4,cy-4, cx+4,cy+4);
                g2.drawLine(cx-4,cy+4, cx+4,cy-4);
                g2.dispose();
            }
        };
        btnClose.setPreferredSize(new Dimension(24, 24));
        btnClose.setBorderPainted(false); btnClose.setContentAreaFilled(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());
        
        pnlHead.add(lblTitle, BorderLayout.WEST);
        pnlHead.add(btnClose, BorderLayout.EAST);
        pnlHead.setBorder(new EmptyBorder(0, 0, 16, 0));
        pnlBg.add(pnlHead, BorderLayout.NORTH);
        
        JPanel pnlContent = new JPanel(new GridLayout(3, 2, 16, 16));
        pnlContent.setOpaque(false);
        
        RoundedTextField txtPhong = createFocusableField();
        RoundedTextField txtKhach = createFocusableField();
        JFormattedTextField txtBatDau = createFocusableDateField();
        JFormattedTextField txtKetThuc = createFocusableDateField();
        RoundedTextField txtCoc = createFocusableField(); 
        RoundedTextField txtThue = createFocusableField();
        
        applyNumberFilter(txtCoc);
        applyNumberFilter(txtThue);
        
        if (isEdit && row != -1) {
            txtPhong.setText(model.getValueAt(row, 1).toString());
            txtKhach.setText(model.getValueAt(row, 2).toString());
            txtBatDau.setText(model.getValueAt(row, 3).toString());
            txtKetThuc.setText(model.getValueAt(row, 4).toString());
            txtCoc.setText(model.getValueAt(row, 5).toString().replaceAll("[^0-9]", ""));
            txtThue.setText(model.getValueAt(row, 6).toString().replaceAll("[^0-9]", ""));
        } else {
            // Không gán gì cả vì Mask đã có Placeholder "__/__/____" 
            txtCoc.setText("0");
            txtThue.setText("0");
        }
        
        pnlContent.add(createLabeledField("Phòng", txtPhong));
        pnlContent.add(createLabeledField("Khách thuê", txtKhach));
        pnlContent.add(createLabeledField("Ngày bắt đầu", txtBatDau));
        pnlContent.add(createLabeledField("Ngày kết thúc", txtKetThuc));
        pnlContent.add(createLabeledField("Tiền cọc", txtCoc));
        pnlContent.add(createLabeledField("Tiền thuê/tháng", txtThue));
        
        pnlBg.add(pnlContent, BorderLayout.CENTER);
        
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        pnlFooter.setOpaque(false);
        
        RoundedButton btnCancel = new RoundedButton("Hủy", 8);
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(MAU_TEXT);
        btnCancel.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        applyButtonHover(btnCancel, Color.WHITE, new Color(226, 232, 240));
        btnCancel.addActionListener(e -> dialog.dispose());
        btnCancel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if(e.getKeyCode() == KeyEvent.VK_ENTER) btnCancel.doClick(); }
        });
        
        RoundedButton btnSave = new RoundedButton(isEdit ? "Cập nhật" : "Tạo", 8);
        btnSave.setBackground(new Color(34, 88, 195));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        applyButtonHover(btnSave, new Color(34, 88, 195), new Color(23, 62, 138));
        btnSave.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if(e.getKeyCode() == KeyEvent.VK_ENTER) btnSave.doClick(); }
        });
        
        btnSave.addActionListener(e -> {
            String bDau = txtBatDau.getText().replace("_", "").trim();
            String kThuc = txtKetThuc.getText().replace("_", "").trim();
            if(txtPhong.getText().trim().isEmpty() || txtKhach.getText().trim().isEmpty() || 
               bDau.length() < 10 || kThuc.length() < 10) {
                showToast("Vui lòng nhập đầy đủ thông tin");
                return;
            }
            
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MM/dd/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT);
            try {
                java.time.LocalDate start = java.time.LocalDate.parse(txtBatDau.getText(), fmt);
                java.time.LocalDate end = java.time.LocalDate.parse(txtKetThuc.getText(), fmt);
                if(start.isAfter(end)) {
                    showToast("Ngày bắt đầu không được lớn hơn ngày kết thúc");
                    return;
                }
            } catch(Exception ex) {
                showToast("Ngày sai (Chỉ nhập MM(01-12) / DD(01-31))");
                return;
            }
            
            String cCoc = formatCurrency(txtCoc.getText());
            String cThue = formatCurrency(txtThue.getText());
            if (isEdit) {
                model.setValueAt(txtPhong.getText(), row, 1);
                model.setValueAt(txtKhach.getText(), row, 2);
                model.setValueAt(txtBatDau.getText(), row, 3);
                model.setValueAt(txtKetThuc.getText(), row, 4);
                model.setValueAt(cCoc, row, 5);
                model.setValueAt(cThue, row, 6);
                showToast("Cập nhật hợp đồng thành công");
            } else {
                String genId = "HD" + System.currentTimeMillis();
                Vector<Object> r = new Vector<>();
                r.add(genId); r.add(txtPhong.getText()); r.add(txtKhach.getText());
                r.add(txtBatDau.getText()); r.add(txtKetThuc.getText());
                r.add(cCoc); r.add(cThue); r.add("Đang hiệu lực"); r.add("");
                model.addRow(r);
                showToast("Tạo hợp đồng thành công");
            }
            dialog.dispose();
        });
        
        pnlFooter.add(btnCancel);
        pnlFooter.add(btnSave);
        pnlBg.add(pnlFooter, BorderLayout.SOUTH);
        
        dialog.add(pnlBg);
        
        if (parent != null) overlay.setVisible(true);
        dialog.setVisible(true); 
        overlay.dispose();
    }
    
    private RoundedTextField createFocusableField() {
        return new RoundedTextField(6) {
            boolean focused = false;
            {
                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) { focused = true; repaint(); }
                    public void focusLost(FocusEvent e) { focused = false; repaint(); }
                });
            }
            @Override public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if(focused) {
                    g2.setColor(new Color(37,99,235)); 
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 6, 6);
                }
                g2.dispose();
            }
        };
    }
    
    private JFormattedTextField createFocusableDateField() {
        javax.swing.text.MaskFormatter mask = null;
        try { 
            mask = new javax.swing.text.MaskFormatter("##/##/####"); 
            mask.setPlaceholderCharacter('_');
        } catch(Exception e){}
        
        JFormattedTextField txt = new JFormattedTextField(mask) {
            boolean focused = false;
            {
                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) { focused = true; repaint(); }
                    public void focusLost(FocusEvent e) { focused = false; repaint(); }
                });
                setOpaque(false);
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                super.paintComponent(g);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if(focused) {
                    g2.setColor(new Color(37,99,235)); 
                    g2.setStroke(new BasicStroke(2f));
                } else {
                    g2.setColor(new Color(226, 232, 240)); 
                    g2.setStroke(new BasicStroke(1f));
                }
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 6, 6);
                
                g2.setColor(new Color(15, 23, 42));
                int cx = getWidth() - 20; 
                int cy = getHeight()/2;
                g2.drawRect(cx-5, cy-4, 10, 8); 
                g2.drawLine(cx-3, cy-6, cx-3, cy-4); 
                g2.drawLine(cx+3, cy-6, cx+3, cy-4); 
                g2.drawLine(cx-5, cy-1, cx+5, cy-1); 
                g2.drawRect(cx-2, cy+2, 1, 1); 
                g2.dispose();
            }
        };
        txt.setBorder(new EmptyBorder(8, 12, 8, 36)); 
        txt.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        txt.setFont(new Font("Inter", Font.PLAIN, 14));
        txt.setForeground(MAU_TEXT);
        
        txt.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) { txt.setCursor(e.getX() > txt.getWidth() - 36 ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.TEXT_CURSOR)); }
        });
        txt.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if(e.getX() > txt.getWidth() - 36) {
                    new DatePickerPopup(txt).show(txt, 0, txt.getHeight());
                    txt.requestFocus();
                } else {
                    if (txt.getText().equals("__/__/____")) {
                        txt.setCaretPosition(0);
                    }
                }
            }
        });
        
        txt.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { check(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { check(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { check(); }
            private void check() {
                SwingUtilities.invokeLater(() -> {
                    String t = txt.getText();
                    if(t.length() < 10) return;
                    String mm = t.substring(0, 2);
                    if(!mm.contains("_")) {
                        int m = Integer.parseInt(mm);
                        if(m < 1 || m > 12) {
                            ui.util.ValidationPopup.show(txt, "Tháng phải từ 01 tới 12");
                            txt.setText("__" + t.substring(2));
                            txt.setCaretPosition(0);
                            return;
                        }
                        String dd = t.substring(3, 5);
                        if(!dd.contains("_")) {
                            int d = Integer.parseInt(dd);
                            int max = 31;
                            if(m == 4 || m == 6 || m == 9 || m == 11) max = 30;
                            else if(m == 2) {
                                String yyyy = t.substring(6, 10);
                                if(!yyyy.contains("_")) {
                                    int y = Integer.parseInt(yyyy);
                                    max = (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) ? 29 : 28;
                                } else {
                                    max = 29; 
                                }
                            }
                            if(d < 1 || d > max) {
                                ui.util.ValidationPopup.show(txt, "Ngày sai (Tháng " + m + " có tối đa " + max + " ngày)");
                                txt.setText(t.substring(0,3) + "__" + t.substring(5));
                                txt.setCaretPosition(3);
                            }
                        }
                    }
                });
            }
        });
        
        return txt;
    }
    
    private void applyNumberFilter(JTextField field) {
        ((AbstractDocument)field.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void insertString(FilterBypass fb, int o, String str, AttributeSet attr) throws BadLocationException {
                if(str.matches("\\d+")) super.insertString(fb, o, str, attr);
            }
            public void replace(FilterBypass fb, int o, int len, String text, AttributeSet attr) throws BadLocationException {
                if(text == null || text.matches("\\d+")) super.replace(fb, o, len, text, attr);
            }
        });
    }
    
    private JPanel createLabeledField(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Inter", Font.PLAIN, 13));
        l.setForeground(MAU_TEXT);
        field.setPreferredSize(new Dimension(0, 42));
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
    
    private String formatCurrency(String num) {
        if(num.isEmpty()) return "0đ";
        long v = Long.parseLong(num);
        return String.format("%,d", v).replace(",", ".") + "đ";
    }

    private void showToast(String message) {
        Window parent = SwingUtilities.getWindowAncestor(pnlRoot);
        if(parent == null) return;
        JDialog toast = new JDialog(parent);
        toast.setUndecorated(true);
        try { toast.setBackground(new Color(0,0,0,0)); } catch(Exception e){}
        toast.setFocusableWindowState(false); 
        toast.setAlwaysOnTop(true);
        
        RoundedPanel pnl = new RoundedPanel(8);
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 16, 10, 16));
        pnl.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        JLabel lblIcon = new JLabel() {
            @Override public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MAU_TEXT);
                g2.fillOval(0, 2, 16, 16);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(4, 10, 7, 13);
                g2.drawLine(7, 13, 11, 7);
                g2.dispose();
            }
        };
        lblIcon.setPreferredSize(new Dimension(16, 20));
        
        JLabel lblMsg = new JLabel(message);
        lblMsg.setFont(new Font("Inter", Font.BOLD, 13)); 
        lblMsg.setForeground(MAU_TEXT);
        
        pnl.add(lblIcon);
        pnl.add(lblMsg);
        toast.add(pnl);
        toast.pack();
        
        toast.setLocation(parent.getX() + parent.getWidth() - toast.getWidth() - 32, 
                          parent.getY() + parent.getHeight() - toast.getHeight() - 32);
        toast.setVisible(true);
        
        Timer t = new Timer(2500, e -> toast.dispose());
        t.setRepeats(false);
        t.start();
    }
    
    class DatePickerPopup extends JPopupMenu {
        private Calendar calTracker = Calendar.getInstance(); 
        private JPanel daysPanel;
        private JLabel lblMonthYear;
        private JTextField targetField;

        public DatePickerPopup(JTextField targetField) {
            this.targetField = targetField;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(12, 16, 12, 16)
            ));
            
            JPanel pnlHead = new JPanel(new BorderLayout());
            pnlHead.setOpaque(false);
            lblMonthYear = new JLabel("", SwingConstants.LEFT);
            lblMonthYear.setFont(new Font("Inter", Font.BOLD, 14));
            
            JPanel pnlArrows = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0,0));
            pnlArrows.setOpaque(false);
            JButton btnPrev = createArrBtn("←");
            JButton btnNext = createArrBtn("→");
            btnPrev.addActionListener(e -> { calTracker.add(Calendar.MONTH, -1); updateCal(); });
            btnNext.addActionListener(e -> { calTracker.add(Calendar.MONTH, 1); updateCal(); });
            pnlArrows.add(btnPrev); pnlArrows.add(btnNext);
            
            pnlHead.add(lblMonthYear, BorderLayout.CENTER);
            pnlHead.add(pnlArrows, BorderLayout.EAST);
            add(pnlHead, BorderLayout.NORTH);
            
            JPanel pnlGrid = new JPanel(new BorderLayout(0,8));
            pnlGrid.setOpaque(false);
            pnlGrid.setBorder(new EmptyBorder(12,0,12,0));
            
            JPanel pnlDOW = new JPanel(new GridLayout(1, 7, 4, 4));
            pnlDOW.setOpaque(false);
            String[] dows = {"Su","Mo","Tu","We","Th","Fr","Sa"};
            for(String d : dows) {
                JLabel l = new JLabel(d, SwingConstants.CENTER);
                l.setFont(new Font("Inter", Font.PLAIN, 12));
                l.setForeground(MAU_SUBTEXT);
                pnlDOW.add(l);
            }
            pnlGrid.add(pnlDOW, BorderLayout.NORTH);
            
            daysPanel = new JPanel(new GridLayout(6, 7, 4, 4));
            daysPanel.setOpaque(false);
            pnlGrid.add(daysPanel, BorderLayout.CENTER);
            
            add(pnlGrid, BorderLayout.CENTER);
            
            JPanel pnlFooter = new JPanel(new BorderLayout());
            pnlFooter.setOpaque(false);
            JButton btnClear = createTxtBtn("Clear");
            JButton btnToday = createTxtBtn("Today");
            btnClear.addActionListener(e -> { targetField.setText("__/__/____"); setVisible(false); });
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
            String m = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.US);
            lblMonthYear.setText(m + " " + cal.get(Calendar.YEAR));
            
            cal.set(Calendar.DAY_OF_MONTH, 1);
            int sd = cal.get(Calendar.DAY_OF_WEEK); 
            cal.add(Calendar.DAY_OF_MONTH, -(sd - 1));
            
            for(int i=0; i<42; i++) {
                int d = cal.get(Calendar.DAY_OF_MONTH);
                boolean cM = cal.get(Calendar.MONTH) == calTracker.get(Calendar.MONTH);
                JButton b = new JButton(String.valueOf(d));
                b.setFont(new Font("Inter", Font.PLAIN, 12));
                b.setForeground(cM ? MAU_TEXT : new Color(148,163,184));
                b.setBackground(Color.WHITE);
                b.setBorderPainted(false); b.setContentAreaFilled(false);
                b.setCursor(new Cursor(Cursor.HAND_CURSOR));
                b.setMargin(new Insets(2,2,2,2));
                
                b.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { b.setContentAreaFilled(true); b.setBackground(new Color(241,245,249)); }
                    public void mouseExited(MouseEvent e) { b.setContentAreaFilled(false); }
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
            return String.format("%02d/%02d/%04d", c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.YEAR));
        }
        private JButton createArrBtn(String t) {
            JButton b = new JButton(t);
            b.setFont(new Font("Inter", Font.BOLD, 14));
            b.setForeground(MAU_SUBTEXT);
            b.setBorderPainted(false); b.setContentAreaFilled(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }
        private JButton createTxtBtn(String t) {
            JButton b = new JButton(t);
            b.setFont(new Font("Inter", Font.PLAIN, 12));
            b.setForeground(new Color(37,99,235));
            b.setBorderPainted(false); b.setContentAreaFilled(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }
    }
    
    class ActionPanel extends JPanel {
        private int rowTarget;
        public ActionPanel(boolean isRenderer, int row) {
            this.rowTarget = row;
            setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 12)); 
            setOpaque(true);
            
            JButton btnEdit = createIconButton(false);
            JButton btnDelete = createIconButton(true);
            add(btnEdit);
            add(btnDelete);
            
            if(!isRenderer) {
                btnEdit.addActionListener(e -> {
                    if (table.isEditing()) table.getCellEditor().stopCellEditing();
                    showContractForm(true, table.convertRowIndexToModel(rowTarget));
                });
                btnDelete.addActionListener(e -> {
                    if (table.isEditing()) table.getCellEditor().stopCellEditing();
                    model.removeRow(table.convertRowIndexToModel(rowTarget));
                    showToast("Đã xóa hợp đồng");
                });
            }
        }
        
        private JButton createIconButton(boolean isDelete) {
            JButton btn = new JButton() {
                boolean isHovered = false;
                {
                    addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                        public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                    });
                }
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isHovered) {
                        g2.setColor(new Color(241, 245, 249)); 
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    }
                    g2.setColor(isDelete ? new Color(239, 68, 68) : new Color(71, 85, 105)); 
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    
                    int cx = getWidth()/2; int cy = getHeight()/2;
                    if(isDelete) { 
                        g2.drawLine(cx-7, cy-4, cx+7, cy-4);
                        Path2D b = new Path2D.Double();
                        b.moveTo(cx+5, cy-4); b.lineTo(cx+5, cy+5); 
                        b.quadTo(cx+5, cy+7, cx+3, cy+7); 
                        b.lineTo(cx-3, cy+7); 
                        b.quadTo(cx-5, cy+7, cx-5, cy+5); 
                        b.lineTo(cx-5, cy-4);
                        g2.draw(b);
                        Path2D top = new Path2D.Double();
                        top.moveTo(cx-3, cy-4); top.lineTo(cx-3, cy-6);
                        top.quadTo(cx-3, cy-7, cx-2, cy-7);
                        top.lineTo(cx+2, cy-7);
                        top.quadTo(cx+3, cy-7, cx+3, cy-6);
                        top.lineTo(cx+3, cy-4);
                        g2.draw(top);
                        g2.drawLine(cx-2, cy-1, cx-2, cy+4);
                        g2.drawLine(cx+2, cy-1, cx+2, cy+4);
                    } else { 
                        Path2D p = new Path2D.Double();
                        p.moveTo(cx-5, cy+5); 
                        p.lineTo(cx-5, cy+1); 
                        p.lineTo(cx+3, cy-7); 
                        p.quadTo(cx+5, cy-9, cx+7, cy-7);
                        p.quadTo(cx+9, cy-5, cx+7, cy-3);
                        p.lineTo(cx-1, cy+5); 
                        p.closePath();
                        g2.draw(p); 
                        g2.drawLine(cx+1, cy-5, cx+5, cy-1); 
                    }
                    g2.dispose();
                }
            };
            btn.setPreferredSize(new Dimension(26, 26)); 
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }
    }
}
