package ui.main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ui.util.RoundedButton;
import ui.util.RoundedPanel;
import ui.util.RoundedPasswordField;
import ui.util.RoundedTextField;
import ui.util.ValidationPopup;
import ui.util.AppColors;

public class SignUpUI extends JFrame {

    // HTML-mapped Colors
    private final Color MAU_NEN = AppColors.SLATE_50;
    private final Color MAU_CARD = AppColors.WHITE;
    private final Color MAU_TITLE = AppColors.SLATE_900;
    private final Color MAU_SUBTITLE = AppColors.SLATE_500;
    private final Color MAU_LABEL = AppColors.SLATE_900;
    private final Color MAU_BUTTON = AppColors.PRIMARY;
    private final Color MAU_LINK = AppColors.PRIMARY;

    public SignUpUI() {
        initUI();
    }

    private void initUI() {
        setTitle("Đăng ký");
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel pnlBackground = new JPanel(new GridBagLayout());
        pnlBackground.setBackground(MAU_NEN);

        pnlBackground.add(createSignUpCard());

        add(pnlBackground);
    }

    private JPanel createSignUpCard() {
        // max-w-md = 448px, p-8 = 32px padding, rounded-2xl = 16px radius
        RoundedPanel card = new RoundedPanel(16);
        card.setBackground(MAU_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 32, 32, 32));
        
        card.setPreferredSize(new Dimension(448, 620));
        card.setMaximumSize(new Dimension(448, 620));
        card.setMinimumSize(new Dimension(448, 620));

        // HEADER BLOCK
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setBackground(MAU_CARD);
        pnlHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlHeader.add(new ui.util.LogoIconPanel());

        pnlHeader.add(Box.createRigidArea(new Dimension(0, 12))); // mb-3

        // Titles
        JLabel lblTitle = new JLabel("MINI Apartment");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 24));
        lblTitle.setForeground(MAU_TITLE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblTitle);

        pnlHeader.add(Box.createRigidArea(new Dimension(0, 4))); // mt-1

        JLabel lblSubTitle = new JLabel("Hệ thống quản lý chung cư mini");
        lblSubTitle.setFont(new Font("Inter", Font.PLAIN, 14));
        lblSubTitle.setForeground(MAU_SUBTITLE);
        lblSubTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblSubTitle);

        card.add(pnlHeader);
        card.add(Box.createRigidArea(new Dimension(0, 32))); // mb-8

        // FORM BLOCK
        JPanel pnlForm = new JPanel();
        pnlForm.setLayout(new BoxLayout(pnlForm, BoxLayout.Y_AXIS));
        pnlForm.setBackground(MAU_CARD);
        pnlForm.setAlignmentX(Component.CENTER_ALIGNMENT);

        Dimension inputSize = new Dimension(384, 40);

        // Name field
        JLabel lblName = new JLabel("<html>Họ và tên <font color='#ef4444'>*</font></html>");
        lblName.setFont(new Font("Inter", Font.BOLD, 14));
        lblName.setForeground(MAU_LABEL);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblName);
        
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8))); // space-y-2

        RoundedTextField txtName = new RoundedTextField(6); // rounded-md
        txtName.setPlaceholder("Nhập họ và tên");
        txtName.setMaximumSize(inputSize);
        txtName.setPreferredSize(inputSize);
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(txtName);

        pnlForm.add(Box.createRigidArea(new Dimension(0, 16))); // space-y-4

        // Email field
        JLabel lblEmail = new JLabel("<html>Email <font color='#ef4444'>*</font></html>");
        lblEmail.setFont(new Font("Inter", Font.BOLD, 14));
        lblEmail.setForeground(MAU_LABEL);
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblEmail);
        
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8))); // space-y-2

        RoundedTextField txtEmail = new RoundedTextField(6);
        txtEmail.setPlaceholder("admin@example.com");
        txtEmail.setMaximumSize(inputSize);
        txtEmail.setPreferredSize(inputSize);
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(txtEmail);

        pnlForm.add(Box.createRigidArea(new Dimension(0, 16))); // space-y-4

        // Password field
        JLabel lblPass = new JLabel("<html>Mật khẩu <font color='#ef4444'>*</font></html>");
        lblPass.setFont(new Font("Inter", Font.BOLD, 14));
        lblPass.setForeground(MAU_LABEL);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblPass);

        pnlForm.add(Box.createRigidArea(new Dimension(0, 8))); // space-y-2

        RoundedPasswordField txtPass = new RoundedPasswordField(6);
        txtPass.setPlaceholder("••••••••");
        txtPass.setMaximumSize(inputSize);
        txtPass.setPreferredSize(inputSize);
        txtPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(txtPass);

        pnlForm.add(Box.createRigidArea(new Dimension(0, 8))); // space before progress bar

        // Thêm thanh quy mô mạnh yếu của mật khẩu
        JPanel pnlStrength = new JPanel(new GridLayout(1, 5, 4, 0)); 
        pnlStrength.setBackground(MAU_CARD);
        pnlStrength.setMaximumSize(new Dimension(384, 4));
        pnlStrength.setPreferredSize(new Dimension(384, 4));
        pnlStrength.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel[] blocks = new JPanel[5];
        for (int i=0; i<5; i++) {
            blocks[i] = new JPanel();
            blocks[i].setBackground(new Color(226, 232, 240)); // slate-200 default
            pnlStrength.add(blocks[i]);
        }
        pnlForm.add(pnlStrength);

        pnlForm.add(Box.createRigidArea(new Dimension(0, 6))); // space before text
        
        JLabel lblStrength = new JLabel("Độ mạnh mật khẩu: Chưa nhập");
        lblStrength.setFont(new Font("Inter", Font.PLAIN, 12));
        lblStrength.setForeground(MAU_SUBTITLE);
        lblStrength.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblStrength);

        txtPass.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateStrength(); }
            public void removeUpdate(DocumentEvent e) { updateStrength(); }
            public void changedUpdate(DocumentEvent e) { updateStrength(); }
            
            private void updateStrength() {
                String pass = new String(txtPass.getPassword());
                int score = 0;
                int level = 0;
                String text = "Chưa nhập";
                Color activeColor = new Color(226, 232, 240);
                
                if (pass.length() > 0) {
                    if (pass.length() >= 8) score++;
                    if (pass.length() >= 12) score++;
                    if (pass.matches(".*[a-z].*")) score++;
                    if (pass.matches(".*[A-Z].*")) score++;
                    if (pass.matches(".*[0-9].*")) score++;
                    if (pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;
                    
                    if (pass.length() < 8) {
                        level = 1; text = "Yếu (Cần ít nhất 8 ký tự)"; activeColor = new Color(239, 68, 68); // red-500
                    } else if (score <= 3) {
                        level = 2; text = "Trung bình"; activeColor = new Color(249, 115, 22); // orange-500
                    } else if (score == 4) {
                        level = 3; text = "Khá"; activeColor = new Color(234, 179, 8); // yellow-500
                    } else if (score == 5) {
                        level = 4; text = "Tốt"; activeColor = new Color(132, 204, 22); // lime-500
                    } else {
                        level = 5; text = "Rất tốt"; activeColor = new Color(34, 197, 94); // green-500
                    }
                }
                
                lblStrength.setText("Độ mạnh mật khẩu: " + text);
                lblStrength.setForeground(level > 0 ? activeColor : MAU_SUBTITLE);
                for (int i=0; i<5; i++) {
                    blocks[i].setBackground(i < level ? activeColor : new Color(226, 232, 240));
                }
            }
        });

        pnlForm.add(Box.createRigidArea(new Dimension(0, 24))); // gap to button

        // SignUp Button
        RoundedButton btnSignUp = new RoundedButton("Đăng ký", 6);
        btnSignUp.setBackground(MAU_BUTTON);
        btnSignUp.setForeground(Color.WHITE);
        btnSignUp.setFont(new Font("Inter", Font.BOLD, 14)); // text-sm
        btnSignUp.setMaximumSize(inputSize);
        btnSignUp.setPreferredSize(inputSize);
        btnSignUp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSignUp.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        btnSignUp.addActionListener(e -> {
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword());
            
            if (name.isEmpty()) {
                ValidationPopup.show(txtName, "Please fill out this field.");
                return;
            }
            if (email.isEmpty()) {
                ValidationPopup.show(txtEmail, "Please fill out this field.");
                return;
            }
            if (pass.isEmpty()) {
                ValidationPopup.show(txtPass, "Please fill out this field.");
                return;
            }
            
            if (!email.contains("@")) {
                ValidationPopup.show(txtEmail, "Please include an '@' in the email address. '" + email + "' is missing an '@'.");
                return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                ValidationPopup.show(txtEmail, "Please enter a part following '@'. '" + email + "' is incomplete.");
                return;
            }
            
            if (pass.length() < 8) {
                ValidationPopup.show(txtPass, "Please lengthen this text to 8 characters or more (you are currently using " + pass.length() + " characters).");
                return;
            }
            if (!pass.matches(".*[A-Z].*")) {
                ValidationPopup.show(txtPass, "Password must contain at least one uppercase letter.");
                return;
            }
            if (!pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
                ValidationPopup.show(txtPass, "Password must contain at least one special character.");
                return;
            }
            
            JOptionPane.showMessageDialog(this, "Đăng ký thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            new LoginUI().setVisible(true);
            this.dispose();
        });
        
        // Hỗ trợ Enter ở bất cứ đâu trên form
        this.getRootPane().setDefaultButton(btnSignUp);
        txtName.addActionListener(e -> btnSignUp.doClick());
        txtEmail.addActionListener(e -> btnSignUp.doClick());
        txtPass.addActionListener(e -> btnSignUp.doClick());
        
        pnlForm.add(btnSignUp);

        card.add(pnlForm);

        card.add(Box.createVerticalGlue()); // Push link to bottom

        // Link Log in
        JLabel lblLogin = new JLabel("Đã có tài khoản? Đăng nhập");
        lblLogin.setFont(new Font("Inter", Font.PLAIN, 14)); // text-sm
        lblLogin.setForeground(MAU_LINK);
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
        
        // mt-6 (24px) handled by glue and preferred layout spacing
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlFooter.setBackground(MAU_CARD);
        pnlFooter.setBorder(new EmptyBorder(24, 0, 0, 0)); // mt-6
        pnlFooter.add(lblLogin);
        
        card.add(pnlFooter);

        return card;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SignUpUI().setVisible(true));
    }
}
