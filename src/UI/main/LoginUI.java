package ui.main;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import ui.util.RoundedButton;
import ui.util.RoundedPanel;
import ui.util.RoundedPasswordField;
import ui.util.RoundedTextField;
import ui.util.ValidationPopup;
import ui.util.AppColors;

public class LoginUI extends JFrame {

    // HTML-mapped Colors
    private final Color MAU_NEN = AppColors.SLATE_50;
    private final Color MAU_CARD = AppColors.WHITE;
    private final Color MAU_TITLE = AppColors.SLATE_900;
    private final Color MAU_SUBTITLE = AppColors.SLATE_500;
    private final Color MAU_LABEL = AppColors.SLATE_900;
    private final Color MAU_BUTTON = AppColors.PRIMARY;
    private final Color MAU_LINK = AppColors.PRIMARY;

    public LoginUI() {
        initUI();
    }

    private void initUI() {
        setTitle("Đăng nhập");
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel pnlBackground = new JPanel(new GridBagLayout());
        pnlBackground.setBackground(MAU_NEN);

        pnlBackground.add(createLoginCard());

        add(pnlBackground);
    }

    private JPanel createLoginCard() {
        RoundedPanel card = new RoundedPanel(16); // rounded-2xl (16px)
        card.setBackground(MAU_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 32, 32, 32)); // p-8 (32px)
        
        // Cố định chiều rộng (max-w-md = 448px)
        card.setPreferredSize(new Dimension(448, 550));
        card.setMaximumSize(new Dimension(448, 550));
        card.setMinimumSize(new Dimension(448, 550));

        // HEADER BLOCK
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setBackground(MAU_CARD);
        pnlHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlHeader.add(new ui.util.LogoIconPanel());

        pnlHeader.add(Box.createRigidArea(new Dimension(0, 12))); // mb-3 (12px)

        // Title: text-2xl font-bold mt-1
        JLabel lblTitle = new JLabel("MINI Apartment");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 24));
        lblTitle.setForeground(MAU_TITLE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblTitle);

        pnlHeader.add(Box.createRigidArea(new Dimension(0, 4))); // mt-1 (4px)

        // Subtitle: text-sm text-muted-foreground mt-1
        JLabel lblSubTitle = new JLabel("Hệ thống quản lý chung cư mini");
        lblSubTitle.setFont(new Font("Inter", Font.PLAIN, 14));
        lblSubTitle.setForeground(MAU_SUBTITLE);
        lblSubTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblSubTitle);

        card.add(pnlHeader);
        card.add(Box.createRigidArea(new Dimension(0, 32))); // mb-8 (32px scale from title block)

        // FORM BLOCK
        JPanel pnlForm = new JPanel();
        pnlForm.setLayout(new BoxLayout(pnlForm, BoxLayout.Y_AXIS));
        pnlForm.setBackground(MAU_CARD);
        pnlForm.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input width = 448 - 64 = 384
        Dimension inputSize = new Dimension(384, 40);

        // Email field
        JLabel lblEmail = new JLabel("Email");
        lblEmail.setFont(new Font("Inter", Font.BOLD, 14)); // text-sm font-medium
        lblEmail.setForeground(MAU_LABEL);
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblEmail);
        
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8))); // space-y-2 (8px)

        RoundedTextField txtEmail = new RoundedTextField(6); // rounded-md (6px)
        txtEmail.setPlaceholder("admin@example.com");
        txtEmail.setMaximumSize(inputSize);
        txtEmail.setPreferredSize(inputSize);
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(txtEmail);

        pnlForm.add(Box.createRigidArea(new Dimension(0, 16))); // space-y-4 (16px)

        // Password field
        JLabel lblPass = new JLabel("Mật khẩu");
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

        pnlForm.add(Box.createRigidArea(new Dimension(0, 24))); // gap to button

        // Login Button
        RoundedButton btnLogin = new RoundedButton("Đăng nhập", 6); // h-10, rounded-md
        btnLogin.setBackground(MAU_BUTTON);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Inter", Font.BOLD, 14)); // text-sm font-medium
        btnLogin.setMaximumSize(inputSize);
        btnLogin.setPreferredSize(inputSize);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        btnLogin.addActionListener(e -> {
            String email = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword());
            
            if (email.isEmpty()) {
                ValidationPopup.show(txtEmail, "Please fill out this field.");
                return;
            }
            if (pass.isEmpty()) {
                ValidationPopup.show(txtPass, "Please fill out this field.");
                return;
            }
            
            // Validate email format
            if (!email.contains("@")) {
                ValidationPopup.show(txtEmail, "Please include an '@' in the email address. '" + email + "' is missing an '@'.");
                return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                ValidationPopup.show(txtEmail, "Please enter a part following '@'. '" + email + "' is incomplete.");
                return;
            }
            
            // Chuyển sang trạng thái Đang xử lý
            btnLogin.setText("Đang xử lý...");
            btnLogin.setBackground(new Color(147, 197, 253)); // Tailwind blue-300: Lighter blue to look grayed out
            btnLogin.setEnabled(false);
            txtEmail.setEnabled(false);
            txtPass.setEnabled(false);
            
            Timer loadingTimer = new Timer(1500, evt -> {
                // Mock authentication bao gồm demo account
                if ((email.equals("admin@example.com") && pass.equals("12345678")) ||
                    (email.equals("huyt3297@gmail.com") && pass.equals("123456"))) {
                    new TrangChu("Admin", "Admin");
                    this.dispose();
                } else {
                    // Trả lại form khi sai mật khẩu
                    btnLogin.setText("Đăng nhập");
                    btnLogin.setBackground(MAU_BUTTON);
                    btnLogin.setEnabled(true);
                    txtEmail.setEnabled(true);
                    txtPass.setEnabled(true);
                    ValidationPopup.show(txtEmail, "Tài khoản hoặc mật khẩu không chính xác!");
                }
            });
            loadingTimer.setRepeats(false);
            loadingTimer.start();
        });
        
        // Hỗ trợ Enter ở bất cứ đâu trên form
        this.getRootPane().setDefaultButton(btnLogin);
        txtEmail.addActionListener(e -> btnLogin.doClick());
        txtPass.addActionListener(e -> btnLogin.doClick());
        
        pnlForm.add(btnLogin);

        card.add(pnlForm);

        card.add(Box.createVerticalGlue()); // Push link to bottom

        // Link Sign up
        JLabel lblSignUp = new JLabel("Chưa có tài khoản? Đăng ký");
        lblSignUp.setFont(new Font("Inter", Font.PLAIN, 14)); // text-sm
        lblSignUp.setForeground(MAU_LINK);
        lblSignUp.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSignUp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblSignUp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new SignUpUI().setVisible(true);
                dispose();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                lblSignUp.setText("<html><u>Chưa có tài khoản? Đăng ký</u></html>");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                lblSignUp.setText("Chưa có tài khoản? Đăng ký");
            }
        });
        
        // mt-6 (24px) handled by glue and preferred layout spacing, but we can set explicit rigid
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlFooter.setBackground(MAU_CARD);
        pnlFooter.setBorder(new EmptyBorder(24, 0, 0, 0)); // mt-6
        pnlFooter.add(lblSignUp);
        
        card.add(pnlFooter);

        return card;
    }

    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    // }
}
