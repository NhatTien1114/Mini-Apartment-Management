package ui.main;

import dao.TaiKhoanDAO;
import entity.Chu;
import entity.QuanLy;
import entity.TaiKhoan;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import ui.util.AppColors;
import ui.util.RoundedButton;
import ui.util.RoundedPanel;
import ui.util.RoundedPasswordField;
import ui.util.RoundedTextField;
import ui.util.ValidationPopup;

public class SignUpUI extends JFrame {
    public SignUpUI() {
        initUI();
    }

    private void initUI() {
        setTitle("Đăng ký");
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel pnlBackground = new JPanel(new BorderLayout());
        pnlBackground.setBackground(AppColors.SLATE_50);

        JScrollPane scrollPane = new JScrollPane(createSignUpCard());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(AppColors.SLATE_50);
        scrollPane.getViewport().setBackground(AppColors.SLATE_50);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel pnlWrapper = new JPanel(new GridBagLayout());
        pnlWrapper.setBackground(AppColors.SLATE_50);
        pnlWrapper.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        pnlWrapper.add(createSignUpCard());

        JScrollPane outerScroll = new JScrollPane(pnlWrapper);
        outerScroll.setBorder(BorderFactory.createEmptyBorder());
        outerScroll.getVerticalScrollBar().setUnitIncrement(20);

        pnlBackground.add(outerScroll, BorderLayout.CENTER);

        add(pnlBackground);
    }

    private JPanel createSignUpCard() {
        // max-w-md = 448px, p-8 = 32px padding, rounded-2xl = 16px radius
        RoundedPanel card = new RoundedPanel(16);
        card.setBackground(AppColors.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 32, 32, 32));

        card.setPreferredSize(new Dimension(500, 1000));
        card.setMaximumSize(new Dimension(500, 1000));
        card.setMinimumSize(new Dimension(500, 1000));

        // HEADER BLOCK
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setBackground(AppColors.WHITE);
        pnlHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlHeader.add(new ui.util.LogoIconPanel());

        pnlHeader.add(Box.createRigidArea(new Dimension(0, 12))); // mb-3

        // Titles
        JLabel lblTitle = new JLabel("MINI Apartment");
        lblTitle.setFont(new Font("Be Vietnam Pro", Font.BOLD, 24));
        lblTitle.setForeground(AppColors.SLATE_900);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblTitle);

        pnlHeader.add(Box.createRigidArea(new Dimension(0, 4))); // mt-1

        JLabel lblSubTitle = new JLabel("Hệ thống quản lý");
        lblSubTitle.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));
        lblSubTitle.setForeground(AppColors.SLATE_500);
        lblSubTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblSubTitle);

        card.add(pnlHeader);
        card.add(Box.createRigidArea(new Dimension(0, 32))); // mb-8

        // FORM BLOCK
        JPanel pnlForm = new JPanel();
        pnlForm.setLayout(new BoxLayout(pnlForm, BoxLayout.Y_AXIS));
        pnlForm.setBackground(AppColors.WHITE);
        pnlForm.setAlignmentX(Component.CENTER_ALIGNMENT);

        Dimension inputSize = new Dimension(420, 40);

        // --- ROLE SELECTION ---
        JLabel lblRole = new JLabel("<html>Vai trò <font color='#ef4444'>*</font></html>");
        lblRole.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));
        lblRole.setForeground(AppColors.SLATE_900);
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblRole);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel pnlRole = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        pnlRole.setBackground(AppColors.WHITE);
        pnlRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlRole.setMaximumSize(new Dimension(420, 30));

        JRadioButton radChu = new JRadioButton("Chủ");
        radChu.setBackground(AppColors.WHITE);
        radChu.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));
        radChu.setSelected(true);

        JRadioButton radQuanLy = new JRadioButton("Quản lý");
        radQuanLy.setBackground(AppColors.WHITE);
        radQuanLy.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14));

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(radChu);
        roleGroup.add(radQuanLy);

        pnlRole.add(radChu);
        pnlRole.add(radQuanLy);
        pnlForm.add(pnlRole);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 16)));

        // --- NAME FIELD ---
        JLabel lblName = new JLabel("<html>Họ và tên <font color='#ef4444'>*</font></html>");
        lblName.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));
        lblName.setForeground(AppColors.SLATE_900);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblName);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8)));

        RoundedTextField txtName = new RoundedTextField(6); // rounded-md
        txtName.setPlaceholder("Nhập họ và tên");
        txtName.setMaximumSize(inputSize);
        txtName.setPreferredSize(inputSize);
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(txtName);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 16)));

        // --- PHONE FIELD ---
        JLabel lblPhone = new JLabel("<html>Số điện thoại <font color='#ef4444'>*</font></html>");
        lblPhone.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));
        lblPhone.setForeground(AppColors.SLATE_900);
        lblPhone.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblPhone);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8)));

        RoundedTextField txtPhone = new RoundedTextField(6);
        txtPhone.setPlaceholder("Nhập số điện thoại");
        txtPhone.setMaximumSize(inputSize);
        txtPhone.setPreferredSize(inputSize);
        txtPhone.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(txtPhone);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 16)));

        // --- DATE OF BIRTH FIELD ---
        JLabel lblDob = new JLabel("<html>Ngày sinh (dd/MM/yyyy) <font color='#ef4444'>*</font></html>");
        lblDob.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));
        lblDob.setForeground(AppColors.SLATE_900);
        lblDob.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblDob);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8)));

        RoundedTextField txtDob = new RoundedTextField(6);
        txtDob.setPlaceholder("01/01/2000");
        txtDob.setMaximumSize(inputSize);
        txtDob.setPreferredSize(inputSize);
        txtDob.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(txtDob);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 16)));

        // --- ADDRESS FIELD ---
        JLabel lblAddress = new JLabel("<html>Địa chỉ <font color='#ef4444'>*</font></html>");
        lblAddress.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));
        lblAddress.setForeground(AppColors.SLATE_900);
        lblAddress.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblAddress);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8)));

        RoundedTextField txtAddress = new RoundedTextField(6);
        txtAddress.setPlaceholder("Nhập địa chỉ của bạn");
        txtAddress.setMaximumSize(inputSize);
        txtAddress.setPreferredSize(inputSize);
        txtAddress.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(txtAddress);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 16)));

        // --- EMAIL / USERNAME FIELD ---
        JLabel lblEmail = new JLabel("<html>Email (Dùng làm Tên đăng nhập) <font color='#ef4444'>*</font></html>");
        lblEmail.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));
        lblEmail.setForeground(AppColors.SLATE_900);
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblEmail);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8)));

        RoundedTextField txtEmail = new RoundedTextField(6);
        txtEmail.setPlaceholder("admin@example.com");
        txtEmail.setMaximumSize(inputSize);
        txtEmail.setPreferredSize(inputSize);
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(txtEmail);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 16)));

        // --- PASSWORD FIELD ---
        JLabel lblPass = new JLabel("<html>Mật khẩu <font color='#ef4444'>*</font></html>");
        lblPass.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14));
        lblPass.setForeground(AppColors.SLATE_900);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblPass);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8)));

        RoundedPasswordField txtPass = new RoundedPasswordField(6);
        txtPass.setPlaceholder("••••••••");
        txtPass.setMaximumSize(inputSize);
        txtPass.setPreferredSize(inputSize);
        txtPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(txtPass);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8))); // space before progress bar

        // Thêm thanh quy mô mạnh yếu của mật khẩu
        JPanel pnlStrength = new JPanel(new GridLayout(1, 5, 4, 0));
        pnlStrength.setBackground(AppColors.WHITE);
        pnlStrength.setMaximumSize(new Dimension(420, 4));
        pnlStrength.setPreferredSize(new Dimension(420, 4));
        pnlStrength.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel[] blocks = new JPanel[5];
        for (int i = 0; i < 5; i++) {
            blocks[i] = new JPanel();
            blocks[i].setBackground(new Color(226, 232, 240)); // slate-200 default
            pnlStrength.add(blocks[i]);
        }
        pnlForm.add(pnlStrength);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 6))); // space before text

        JLabel lblStrength = new JLabel("Độ mạnh mật khẩu: Chưa nhập");
        lblStrength.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 12));
        lblStrength.setForeground(AppColors.SLATE_500);
        lblStrength.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblStrength);

        txtPass.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateStrength();
            }

            public void removeUpdate(DocumentEvent e) {
                updateStrength();
            }

            public void changedUpdate(DocumentEvent e) {
                updateStrength();
            }

            private void updateStrength() {
                String pass = new String(txtPass.getPassword());
                int score = 0;
                int level = 0;
                String text = "Chưa nhập";
                Color activeColor = new Color(226, 232, 240);

                if (pass.length() > 0) {
                    if (pass.length() >= 8)
                        score++;
                    if (pass.length() >= 12)
                        score++;
                    if (pass.matches(".*[a-z].*"))
                        score++;
                    if (pass.matches(".*[A-Z].*"))
                        score++;
                    if (pass.matches(".*[0-9].*"))
                        score++;
                    if (pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"))
                        score++;

                    if (pass.length() < 8) {
                        level = 1;
                        text = "Yếu (Cần ít nhất 8 ký tự)";
                        activeColor = new Color(239, 68, 68); // red-500
                    } else if (score <= 3) {
                        level = 2;
                        text = "Trung bình";
                        activeColor = new Color(249, 115, 22); // orange-500
                    } else if (score == 4) {
                        level = 3;
                        text = "Khá";
                        activeColor = new Color(234, 179, 8); // yellow-500
                    } else if (score == 5) {
                        level = 4;
                        text = "Tốt";
                        activeColor = new Color(132, 204, 22); // lime-500
                    } else {
                        level = 5;
                        text = "Rất tốt";
                        activeColor = new Color(34, 197, 94); // green-500
                    }
                }

                lblStrength.setText("Độ mạnh mật khẩu: " + text);
                lblStrength.setForeground(level > 0 ? activeColor : AppColors.SLATE_500);
                for (int i = 0; i < 5; i++) {
                    blocks[i].setBackground(i < level ? activeColor : new Color(226, 232, 240));
                }
            }
        });

        pnlForm.add(Box.createRigidArea(new Dimension(0, 24))); // gap to button

        // SignUp Button
        RoundedButton btnSignUp = new RoundedButton("Đăng ký", 6);
        btnSignUp.setBackground(AppColors.PRIMARY);
        btnSignUp.setForeground(Color.WHITE);
        btnSignUp.setFont(new Font("Be Vietnam Pro", Font.BOLD, 14)); // text-sm
        btnSignUp.setMaximumSize(inputSize);
        btnSignUp.setPreferredSize(inputSize);
        btnSignUp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSignUp.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSignUp.addActionListener(e -> {
            String name = txtName.getText().trim();
            String phone = txtPhone.getText().trim();
            String dobStr = txtDob.getText().trim();
            String address = txtAddress.getText().trim();
            String email = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword());
            boolean isChu = radChu.isSelected();

            if (name.isEmpty()) {
                ValidationPopup.show(txtName, "Vui lòng nhập họ và tên.");
                return;
            }
            if (phone.isEmpty()) {
                ValidationPopup.show(txtPhone, "Vui lòng nhập số điện thoại.");
                return;
            }
            if (!phone.matches("\\d{10,11}")) {
                ValidationPopup.show(txtPhone, "Số điện thoại không hợp lệ (10-11 số).");
                return;
            }
            if (dobStr.isEmpty()) {
                ValidationPopup.show(txtDob, "Vui lòng nhập ngày sinh.");
                return;
            }

            LocalDate dob = null;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dob = LocalDate.parse(dobStr, formatter);
            } catch (DateTimeParseException ex) {
                ValidationPopup.show(txtDob, "Ngày sinh không đúng định dạng dd/MM/yyyy.");
                return;
            }

            if (address.isEmpty()) {
                ValidationPopup.show(txtAddress, "Vui lòng nhập địa chỉ.");
                return;
            }
            if (email.isEmpty()) {
                ValidationPopup.show(txtEmail, "Vui lòng nhập email.");
                return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                ValidationPopup.show(txtEmail, "Email không hợp lệ.");
                return;
            }
            if (pass.isEmpty()) {
                ValidationPopup.show(txtPass, "Vui lòng nhập mật khẩu.");
                return;
            }

            if (pass.length() < 8) {
                ValidationPopup.show(txtPass, "Mật khẩu phải từ 8 ký tự trở lên.");
                return;
            }
            if (!pass.matches(".*[A-Z].*")) {
                ValidationPopup.show(txtPass, "Mật khẩu phải chứa ký tự viết hoa.");
                return;
            }
            if (!pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
                ValidationPopup.show(txtPass, "Mật khẩu phải chứa ký tự đặc biệt.");
                return;
            }

            // Database Transaction Logic
            TaiKhoanDAO dao = new TaiKhoanDAO();
            if (dao.kiemTraTonTaiTenDangNhap(email)) {
                JOptionPane.showMessageDialog(this, "Email/Tên đăng nhập đã tồn tại trên hệ thống!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String generatedId = dao.phatSinhMaTaiKhoan(email);
            TaiKhoan taiKhoan;

            if (isChu) {
                taiKhoan = new Chu(generatedId, email, pass, name, phone, dob, address);
            } else {
                taiKhoan = new QuanLy(generatedId, email, pass, name, phone, dob, address);
            }

            boolean success = dao.insertTaiKhoan(taiKhoan);

            if (success) {
                ui.util.MessageDialog.show(this, "Đăng ký thành công!", "Tài khoản của bạn đã được khởi tạo.",
                        ui.util.MessageDialog.MessageType.SUCCESS);
                new LoginUI().setVisible(true);
                this.dispose();
            } else {
                ui.util.MessageDialog.show(this, "Lỗi hệ thống", "Có lỗi xảy ra khi lưu vào CSDL. Vui lòng thử lại.",
                        ui.util.MessageDialog.MessageType.ERROR);
            }
        });

        // Hỗ trợ Enter ở bất cứ đâu trên form
        this.getRootPane().setDefaultButton(btnSignUp);
        txtName.addActionListener(e -> btnSignUp.doClick());
        txtPhone.addActionListener(e -> btnSignUp.doClick());
        txtDob.addActionListener(e -> btnSignUp.doClick());
        txtAddress.addActionListener(e -> btnSignUp.doClick());
        txtEmail.addActionListener(e -> btnSignUp.doClick());
        txtPass.addActionListener(e -> btnSignUp.doClick());

        pnlForm.add(btnSignUp);
        card.add(pnlForm);
        card.add(Box.createVerticalGlue()); // Push link to bottom

        // Link Log in
        JLabel lblLogin = new JLabel("Đã có tài khoản? Đăng nhập");
        lblLogin.setFont(new Font("Be Vietnam Pro", Font.PLAIN, 14)); // text-sm
        lblLogin.setForeground(AppColors.PRIMARY);
        lblLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LoginUI().setVisible(true);
                dispose();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lblLogin.setText("<html><u>Đã có tài khoản? Đăng nhập</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblLogin.setText("Đã có tài khoản? Đăng nhập");
            }
        });

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlFooter.setBackground(AppColors.WHITE);
        pnlFooter.setBorder(new EmptyBorder(24, 0, 0, 0)); // mt-6
        pnlFooter.add(lblLogin);

        card.add(pnlFooter);

        return card;
    }
}
