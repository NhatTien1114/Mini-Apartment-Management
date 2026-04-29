package ui.main;

import dao.TaiKhoanDAO;
import entity.Chu;
import entity.QuanLy;
import entity.TaiKhoan;
import ui.util.*;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

public class QuanLyTaiKhoanUI {

    // ── Colours ──────────────────────────────────────────────────────────────
    private static final Color C_BG = AppColors.SLATE_50;
    private static final Color C_CARD = AppColors.WHITE;
    private static final Color C_TEXT = AppColors.SLATE_900;
    private static final Color C_SUBTEXT = AppColors.SLATE_500;
    private static final Color C_BORDER = AppColors.SLATE_200;
    private static final Color C_ROW_SEL = new Color(241, 245, 249);

    // Role badge colours
    private static final Color CHU_BG = new Color(219, 234, 254); // blue-100
    private static final Color CHU_FG = new Color(29, 78, 216); // blue-700
    private static final Color QL_BG = new Color(220, 252, 231); // green-100
    private static final Color QL_FG = new Color(22, 163, 74); // green-600

    // Admin-badge colours
    private static final Color ADMIN_BG = new Color(254, 243, 199); // amber-100
    private static final Color ADMIN_FG = new Color(180, 83, 9); // amber-700

    // ── State ─────────────────────────────────────────────────────────────────
    private JPanel pnlRoot;
    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private RoundedTextField txtSearch;
    private JComboBox<String> cboVaiTro;

    private JLabel lblTongTK;
    private JLabel lblChuTK;
    private JLabel lblQLTK;

    private final TaiKhoanDAO dao = new TaiKhoanDAO();
    private final PrimaryButton btnFactory = new PrimaryButton();

    // The logged-in admin's email – accounts with this email cannot be edited
    private final String adminEmail;

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("vi", "VN"));

    // ─────────────────────────────────────────────────────────────────────────
    public QuanLyTaiKhoanUI(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    // ── Public API ────────────────────────────────────────────────────────────
    public JPanel getPanel() {
        pnlRoot = new JPanel(new BorderLayout(0, 24));
        pnlRoot.setBorder(new EmptyBorder(32, 32, 32, 32));
        pnlRoot.setBackground(C_BG);

        pnlRoot.add(buildHeader(), BorderLayout.NORTH);
        pnlRoot.add(buildContent(), BorderLayout.CENTER);

        loadData();
        return pnlRoot;
    }

    public void refresh() {
        if (txtSearch != null)
            txtSearch.setText("");
        if (cboVaiTro != null)
            cboVaiTro.setSelectedIndex(0);
        loadData();
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    private void loadData() {
        if (model == null)
            return;
        model.setRowCount(0);

        ArrayList<TaiKhoan> list = dao.getAllTaiKhoan();
        int chu = 0, ql = 0;

        for (TaiKhoan tk : list) {
            String vaiTro = (tk instanceof Chu) ? "Chủ" : "Quản lý";
            String hoTen = getHoTen(tk);
            String sdt = getSoDienThoai(tk);
            String ns = getNgaySinh(tk);
            String diaChi = getDiaChi(tk);

            if (tk instanceof Chu)
                chu++;
            else
                ql++;

            model.addRow(new Object[] {
                    tk.getMaTaiKhoan(), // 0 – hidden
                    vaiTro, // 1 – badge
                    hoTen, // 2
                    tk.getEmail(), // 3
                    sdt, // 4
                    ns, // 5
                    diaChi // 6
            });
        }

        int total = list.size();
        if (lblTongTK != null)
            lblTongTK.setText(String.valueOf(total));
        if (lblChuTK != null)
            lblChuTK.setText(String.valueOf(chu));
        if (lblQLTK != null)
            lblQLTK.setText(String.valueOf(ql));
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout(12, 0));
        pnl.setOpaque(false);

        // Left: title + subtitle
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý tài khoản");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 22));
        lblTitle.setForeground(C_TEXT);
        titleRow.add(lblTitle);

        // Admin-only badge
        JLabel badge = new JLabel("Admin Only") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ADMIN_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        badge.setFont(new Font("Inter", Font.BOLD, 11));
        badge.setForeground(ADMIN_FG);
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));
        titleRow.add(badge);

        JLabel lblSub = new JLabel("Xem và phân quyền tài khoản trong hệ thống");
        lblSub.setFont(new Font("Inter", Font.PLAIN, 13));
        lblSub.setForeground(C_SUBTEXT);
        lblSub.setBorder(new EmptyBorder(4, 2, 0, 0));

        left.add(titleRow);
        left.add(lblSub);

        // Right: create button
        JButton btnTao = btnFactory.makePrimaryButton("Tạo tài khoản");
        btnTao.addActionListener(e -> new SignUpUI().setVisible(true));

        pnl.add(left, BorderLayout.WEST);
        pnl.add(btnTao, BorderLayout.EAST);
        return pnl;
    }

    // ── Main Content ──────────────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel pnl = new JPanel(new BorderLayout(0, 20));
        pnl.setOpaque(false);

        pnl.add(buildStats(), BorderLayout.NORTH);
        pnl.add(buildCard(), BorderLayout.CENTER);
        return pnl;
    }

    // ── Stats Cards ───────────────────────────────────────────────────────────
    private JPanel buildStats() {
        JPanel pnl = new JPanel(new GridLayout(1, 3, 16, 0));
        pnl.setOpaque(false);
        pnl.setPreferredSize(new Dimension(0, 124));

        lblTongTK = new JLabel("–");
        lblChuTK = new JLabel("–");
        lblQLTK = new JLabel("–");

        pnl.add(buildStatCard(lblTongTK, "Tổng tài khoản",
                new Color(37, 99, 235), new Color(21, 66, 180), "img/icons/user.png"));
        pnl.add(buildStatCard(lblChuTK, "Tài khoản Chủ",
                new Color(99, 102, 241), new Color(67, 56, 202), "img/icons/user.png"));
        pnl.add(buildStatCard(lblQLTK, "Tài khoản Q.Lý",
                new Color(16, 185, 129), new Color(5, 150, 105), "img/icons/user.png"));
        return pnl;
    }

    private JPanel buildStatCard(JLabel lblValue, String label,
            Color colorStart, Color colorEnd, String iconPath) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, colorStart, getWidth(), getHeight(), colorEnd);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                // Decorative circles
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillOval(getWidth() - 88, -28, 108, 108);
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(getWidth() - 52, getHeight() - 38, 84, 84);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(22, 24, 22, 20));

        // Text column
        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        lblValue.setFont(new Font("Inter", Font.BOLD, 40));
        lblValue.setForeground(Color.WHITE);

        JLabel lblName = new JLabel(label);
        lblName.setFont(new Font("Inter", Font.PLAIN, 13));
        lblName.setForeground(new Color(255, 255, 255, 210));
        lblName.setBorder(new EmptyBorder(4, 0, 0, 0));

        textCol.add(lblValue);
        textCol.add(lblName);

        // Icon circle (white, semi-transparent)
        JPanel iconWrapper = new JPanel(new GridBagLayout());
        iconWrapper.setOpaque(false);
        JLabel iconCircle = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        try {
            ImageIcon raw = new ImageIcon(iconPath);
            Image scaled = raw.getImage().getScaledInstance(26, 26, Image.SCALE_SMOOTH);
            iconCircle.setIcon(new ImageIcon(scaled));
        } catch (Exception ignored) {
        }
        iconCircle.setPreferredSize(new Dimension(58, 58));
        iconCircle.setHorizontalAlignment(SwingConstants.CENTER);
        iconWrapper.add(iconCircle);

        card.add(textCol, BorderLayout.CENTER);
        card.add(iconWrapper, BorderLayout.EAST);
        return card;
    }

    // ── Table Card ────────────────────────────────────────────────────────────
    private JPanel buildCard() {
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(C_CARD);
        card.setLayout(new BorderLayout());

        JPanel inner = new JPanel(new BorderLayout(0, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(8, 24, 24, 24));

        inner.add(buildToolbar(), BorderLayout.NORTH);
        inner.add(buildTable(), BorderLayout.CENTER);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // ── Toolbar ───────────────────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        pnl.setOpaque(false);

        // Search
        txtSearch = new RoundedTextField(8) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(148, 163, 184));
                g2.setStroke(new BasicStroke(1.5f));
                int cx = 18, cy = getHeight() / 2 - 2;
                g2.drawOval(cx - 4, cy - 4, 8, 8);
                g2.drawLine(cx + 2, cy + 2, cx + 7, cy + 7);
                if (isFocusOwner()) {
                    g2.setColor(AppColors.PRIMARY);
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                }
                g2.dispose();
            }
        };
        txtSearch.setBorder(new EmptyBorder(8, 36, 8, 12));
        txtSearch.setPlaceholder("Tìm theo tên, email, SĐT...");
        txtSearch.setPreferredSize(new Dimension(280, 40));
        txtSearch.setBackground(Color.WHITE);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }
        });

        // Role filter
        cboVaiTro = new JComboBox<>(new String[] { "Tất cả", "Chủ", "Quản lý" });
        cboVaiTro.setPreferredSize(new Dimension(140, 40));
        cboVaiTro.setFont(new Font("Inter", Font.PLAIN, 13));
        cboVaiTro.setBackground(Color.WHITE);
        cboVaiTro.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED)
                applyFilter();
        });

        pnl.add(txtSearch);
        pnl.add(cboVaiTro);
        return pnl;
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] cols = { "Mã TK", "Vai trò", "Họ tên", "Email", "Số điện thoại", "Ngày sinh", "Địa chỉ" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(52);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);
        table.setSelectionBackground(C_ROW_SEL);
        table.setBorder(null);

        // Hide maTaiKhoan column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Column widths
        int[] widths = { 0, 110, 160, 200, 120, 110, 200 };
        for (int i = 1; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Header renderer
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                l.setFont(new Font("Inter", Font.BOLD, 12));
                l.setForeground(C_SUBTEXT);
                l.setBackground(C_CARD);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER),
                        new EmptyBorder(14, 16, 14, 8)));
                return l;
            }
        });

        // Default row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                l.setFont(new Font("Inter", Font.PLAIN, 13));
                l.setForeground(C_TEXT);
                l.setBackground(sel ? C_ROW_SEL : C_CARD);
                l.setOpaque(true);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                        new EmptyBorder(0, 16, 0, 8)));
                return l;
            }
        });

        // Role badge renderer for column 1
        table.getColumnModel().getColumn(1).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                JPanel cell = new JPanel(new GridBagLayout());
                cell.setBackground(sel ? C_ROW_SEL : C_CARD);
                cell.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));

                String role = v != null ? v.toString() : "";
                boolean isChu = "Chủ".equals(role);
                Color badgeBg = isChu ? CHU_BG : QL_BG;
                Color badgeFg = isChu ? CHU_FG : QL_FG;

                JLabel badge = new JLabel(role) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(badgeBg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                        super.paintComponent(g);
                        g2.dispose();
                    }
                };
                badge.setFont(new Font("Inter", Font.BOLD, 11));
                badge.setForeground(badgeFg);
                badge.setBorder(new EmptyBorder(3, 10, 3, 10));

                cell.add(badge);
                return cell;
            }
        });

        // Email renderer with dim colour
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                l.setFont(new Font("Inter", Font.PLAIN, 13));
                l.setForeground(new Color(37, 99, 235));
                l.setBackground(sel ? C_ROW_SEL : C_CARD);
                l.setOpaque(true);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                        new EmptyBorder(0, 16, 0, 8)));
                return l;
            }
        });

        // Sorter
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Context menu
        buildContextMenu();

        // Hover highlight
        table.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0)
                    table.setRowSelectionInterval(row, row);
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                table.clearSelection();
            }

            public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0)
                    table.setRowSelectionInterval(row, row);
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(C_CARD);
        return scroll;
    }

    // ── Context Menu ──────────────────────────────────────────────────────────
    private void buildContextMenu() {
        JPopupMenu menu = new JPopupMenu() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };

        JMenuItem miSua = new JMenuItem("Sửa thông tin");
        miSua.setFont(new Font("Inter", Font.PLAIN, 13));
        miSua.setForeground(C_TEXT);

        JMenuItem miDoiVaiTro = new JMenuItem("Đổi vai trò");
        miDoiVaiTro.setFont(new Font("Inter", Font.PLAIN, 13));
        miDoiVaiTro.setForeground(C_TEXT);

        JMenuItem miXoa = new JMenuItem("Xóa tài khoản");
        miXoa.setFont(new Font("Inter", Font.BOLD, 13));
        miXoa.setForeground(AppColors.RED_500);

        menu.add(miSua);
        menu.addSeparator();
        menu.add(miDoiVaiTro);
        menu.addSeparator();
        menu.add(miXoa);

        // Dynamically update menu before showing
        menu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                int viewRow = table.getSelectedRow();
                if (viewRow < 0)
                    return;
                int mRow = table.convertRowIndexToModel(viewRow);
                String email = model.getValueAt(mRow, 3).toString();
                String vaiTro = model.getValueAt(mRow, 1).toString();
                boolean isAdmin = adminEmail != null && adminEmail.equalsIgnoreCase(email);

                miSua.setEnabled(true);
                if (isAdmin) {
                    miDoiVaiTro.setEnabled(false);
                    miXoa.setEnabled(false);
                    miDoiVaiTro.setText("Tài khoản Admin");
                } else {
                    miDoiVaiTro.setEnabled(true);
                    miXoa.setEnabled(true);
                    miDoiVaiTro.setText("Chủ".equals(vaiTro)
                            ? "Đổi thành Quản lý"
                            : "Đổi thành Chủ");
                }
            }

            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
            }

            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
            }
        });

        // Edit action
        miSua.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0)
                return;
            showEditDialog(table.convertRowIndexToModel(viewRow));
        });

        // Change role action
        miDoiVaiTro.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0)
                return;
            int mRow = table.convertRowIndexToModel(viewRow);
            String maTK = model.getValueAt(mRow, 0).toString();
            String hoTen = model.getValueAt(mRow, 2).toString();
            String vaiTro = model.getValueAt(mRow, 1).toString();
            String target = "Chủ".equals(vaiTro) ? "Quản lý" : "Chủ";
            int newRole = "Quản lý".equals(target) ? 1 : 0;

            int choice = JOptionPane.showConfirmDialog(pnlRoot,
                    "Đổi vai trò của \"" + hoTen + "\"\ntừ " + vaiTro + " → " + target + "?",
                    "Xác nhận đổi vai trò", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice != JOptionPane.YES_OPTION)
                return;

            boolean ok = dao.capNhatVaiTro(maTK, newRole);
            showToast(ok ? "Đã đổi vai trò thành công" : "Đổi vai trò thất bại");
            if (ok)
                loadData();
        });

        // Delete action
        miXoa.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0)
                return;
            int mRow = table.convertRowIndexToModel(viewRow);
            String maTK = model.getValueAt(mRow, 0).toString();
            String hoTen = model.getValueAt(mRow, 2).toString();
            String email = model.getValueAt(mRow, 3).toString();

            if (adminEmail != null && adminEmail.equalsIgnoreCase(email)) {
                showToast("Không thể xóa tài khoản Admin");
                return;
            }

            int choice = JOptionPane.showConfirmDialog(pnlRoot,
                    "Xóa tài khoản của \"" + hoTen + "\"?\nHành động này không thể hoàn tác.",
                    "Xác nhận xóa tài khoản", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION)
                return;

            boolean ok = dao.xoaTaiKhoan(maTK);
            showToast(ok ? "Đã xóa tài khoản thành công" : "Xóa tài khoản thất bại");
            if (ok)
                loadData();
        });

        table.setComponentPopupMenu(menu);
    }

    // ── Edit Dialog ───────────────────────────────────────────────────────────
    private void showEditDialog(int modelRow) {
        String maTK = model.getValueAt(modelRow, 0).toString();
        String vaiTro = model.getValueAt(modelRow, 1).toString();
        String hoTenCu = model.getValueAt(modelRow, 2).toString();
        String sdtCu = model.getValueAt(modelRow, 4).toString();
        String nsCu = model.getValueAt(modelRow, 5).toString();
        String diaChiCu = model.getValueAt(modelRow, 6).toString();

        Window owner = SwingUtilities.getWindowAncestor(pnlRoot);
        JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null,
                "Sửa thông tin tài khoản", true);
        dlg.setSize(460, 470);
        dlg.setLocationRelativeTo(pnlRoot);
        dlg.setResizable(false);

        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(C_BG);
        main.setBorder(new EmptyBorder(28, 28, 24, 28));

        // Title
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(0, 0, 22, 0));
        JLabel lblDlgTitle = new JLabel("Sửa thông tin tài khoản");
        lblDlgTitle.setFont(new Font("Inter", Font.BOLD, 17));
        lblDlgTitle.setForeground(C_TEXT);
        JLabel lblVTBadge = new JLabel(vaiTro) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor("Chủ".equals(vaiTro) ? CHU_BG : QL_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        lblVTBadge.setFont(new Font("Inter", Font.BOLD, 11));
        lblVTBadge.setForeground("Chủ".equals(vaiTro) ? CHU_FG : QL_FG);
        lblVTBadge.setBorder(new EmptyBorder(3, 10, 3, 10));
        titleRow.add(lblDlgTitle);
        titleRow.add(Box.createHorizontalStrut(10));
        titleRow.add(lblVTBadge);

        // Form
        JPanel form = new JPanel(new GridLayout(4, 1, 0, 14));
        form.setOpaque(false);

        RoundedTextField txtHoTen = makeDialogField(hoTenCu);
        RoundedTextField txtSdt = makeDialogField(sdtCu);
        RoundedTextField txtNgaySinh = makeDialogField(nsCu);
        RoundedTextField txtDiaChi = makeDialogField(diaChiCu);

        form.add(makeDialogRow("Họ tên *", txtHoTen));
        form.add(makeDialogRow("Số điện thoại *", txtSdt));
        form.add(makeDialogRow("Ngày sinh (dd/MM/yyyy)", txtNgaySinh));
        form.add(makeDialogRow("Địa chỉ", txtDiaChi));

        // Buttons
        JButton btnLuu = btnFactory.makePrimaryButton("Lưu thay đổi");
        JButton btnHuy = new JButton("Hủy");
        btnHuy.setFont(new Font("Inter", Font.PLAIN, 13));
        btnHuy.setForeground(C_SUBTEXT);
        btnHuy.setBackground(C_CARD);
        btnHuy.setFocusPainted(false);
        btnHuy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHuy.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(9, 20, 9, 20)));
        btnHuy.addActionListener(ev -> dlg.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(20, 0, 0, 0));
        btnRow.add(btnHuy);
        btnRow.add(btnLuu);

        btnLuu.addActionListener(ev -> {
            String hoTen = txtHoTen.getText().trim();
            String sdt = txtSdt.getText().trim();
            String nsStr = txtNgaySinh.getText().trim();
            String diaChi = txtDiaChi.getText().trim();

            if (hoTen.isEmpty() || hoTen.length() < 2) {
                txtHoTen.requestFocus();
                showToast(hoTen.isEmpty() ? "Họ tên không được để trống" : "Họ tên phải có ít nhất 2 ký tự");
                return;
            }
            if (sdt.isEmpty()) {
                txtSdt.requestFocus();
                showToast("Số điện thoại không được để trống");
                return;
            }

            LocalDate ngaySinh = null;
            if (!nsStr.isEmpty()) {
                try {
                    ngaySinh = LocalDate.parse(nsStr,
                            DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("vi", "VN")));
                } catch (Exception ex) {
                    txtNgaySinh.requestFocus();
                    showToast("Ngày sinh không hợp lệ (dd/MM/yyyy)");
                    return;
                }
            }

            TaiKhoan tk;
            if ("Chủ".equals(vaiTro)) {
                Chu c = new Chu();
                c.setHoTen(hoTen);
                c.setSoDienThoai(sdt);
                c.setDiaChi(diaChi);
                if (ngaySinh != null) {
                    try {
                        c.setNgaySinh(ngaySinh);
                    } catch (IllegalArgumentException ex) {
                        showToast("Năm sinh phải > 1900 và < 2026");
                        return;
                    }
                }
                tk = c;
            } else {
                QuanLy ql = new QuanLy();
                ql.setHoTen(hoTen);
                ql.setSoDienThoai(sdt);
                ql.setDiaChi(diaChi);
                if (ngaySinh != null) {
                    try {
                        ql.setNgaySinh(ngaySinh);
                    } catch (IllegalArgumentException ex) {
                        showToast("Năm sinh phải > 1900 và < 2026");
                        return;
                    }
                }
                tk = ql;
            }
            tk.setMaTaiKhoan(maTK);

            boolean ok = dao.updateThongTinCaNhan(tk);
            if (ok) {
                dlg.dispose();
                loadData();
                showToast("Đã cập nhật thông tin thành công");
            } else {
                showToast("Cập nhật thất bại, vui lòng thử lại");
            }
        });

        main.add(titleRow, BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);
        main.add(btnRow, BorderLayout.SOUTH);
        dlg.setContentPane(main);
        dlg.setVisible(true);
    }

    private RoundedTextField makeDialogField(String value) {
        RoundedTextField f = new RoundedTextField(8);
        f.setText(value);
        f.setFont(new Font("Inter", Font.PLAIN, 13));
        f.setBackground(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        f.setPreferredSize(new Dimension(0, 44));
        return f;
    }

    private JPanel makeDialogRow(String labelText, RoundedTextField field) {
        JPanel row = new JPanel(new BorderLayout(0, 5));
        row.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Inter", Font.PLAIN, 12));
        lbl.setForeground(C_SUBTEXT);
        row.add(lbl, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // ── Filter ────────────────────────────────────────────────────────────────
    private void applyFilter() {
        if (sorter == null)
            return;
        String text = txtSearch.getText().trim();
        String filter = (String) cboVaiTro.getSelectedItem();

        java.util.List<RowFilter<Object, Object>> filters = new java.util.ArrayList<>();
        if (!text.isEmpty())
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 2, 3, 4));

        if ("Chủ".equals(filter))
            filters.add(RowFilter.regexFilter("^Chủ$", 1));
        else if ("Quản lý".equals(filter))
            filters.add(RowFilter.regexFilter("^Quản lý$", 1));

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    // ── Toast ─────────────────────────────────────────────────────────────────
    private void showToast(String message) {
        JWindow toast = new JWindow(SwingUtilities.getWindowAncestor(pnlRoot));
        JLabel lbl = new JLabel(message) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 41, 59));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        lbl.setFont(new Font("Inter", Font.PLAIN, 13));
        lbl.setForeground(Color.WHITE);
        lbl.setBorder(new EmptyBorder(10, 20, 10, 20));
        lbl.setOpaque(false);
        toast.add(lbl);
        toast.pack();

        Window owner = SwingUtilities.getWindowAncestor(pnlRoot);
        if (owner != null) {
            Point loc = owner.getLocation();
            Dimension sz = owner.getSize();
            toast.setLocation(loc.x + sz.width / 2 - toast.getWidth() / 2,
                    loc.y + sz.height - 80);
        }
        toast.setVisible(true);
        new javax.swing.Timer(2200, ev -> toast.dispose()).start();
    }

    // ── Helper extractors ─────────────────────────────────────────────────────
    private static String getHoTen(TaiKhoan tk) {
        if (tk instanceof Chu)
            return ((Chu) tk).getHoTen();
        if (tk instanceof QuanLy)
            return ((QuanLy) tk).getHoTen();
        return "";
    }

    private static String getSoDienThoai(TaiKhoan tk) {
        if (tk instanceof Chu)
            return nvl(((Chu) tk).getSoDienThoai());
        if (tk instanceof QuanLy)
            return nvl(((QuanLy) tk).getSoDienThoai());
        return "";
    }

    private static String getDiaChi(TaiKhoan tk) {
        if (tk instanceof Chu)
            return nvl(((Chu) tk).getDiaChi());
        if (tk instanceof QuanLy)
            return nvl(((QuanLy) tk).getDiaChi());
        return "";
    }

    private static String getNgaySinh(TaiKhoan tk) {
        LocalDate ns = null;
        if (tk instanceof Chu)
            ns = ((Chu) tk).getNgaySinh();
        if (tk instanceof QuanLy)
            ns = ((QuanLy) tk).getNgaySinh();
        return ns != null ? ns.format(FMT_DATE) : "";
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
