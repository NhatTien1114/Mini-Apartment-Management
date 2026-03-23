package ui.main;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import entity.Chu;
import entity.QuanLy;
import entity.TaiKhoan;
import dao.TaiKhoanDAO;
import ui.util.RoundedButton;
import ui.util.RoundedTextField;
import ui.util.ValidationPopup;

public class AccountInfoDialog extends JDialog {

    private final TaiKhoan taiKhoan;
    private RoundedTextField txtName;
    private RoundedTextField txtPhone;
    private RoundedTextField txtDob;
    private RoundedTextField txtAddress;
    private JLabel lblEmail;
    
    // Core design colors from SignUpUI
    private final Color MAU_LABEL = new Color(55, 65, 81); // text-gray-700
    private final Color MAU_BUTTON = new Color(37, 99, 235); // bg-blue-600
    private final Color MAU_BUTTON_HOVER = new Color(29, 78, 216); // bg-blue-700

    public AccountInfoDialog(JFrame parent, TaiKhoan tk) {
        super(parent, "Trang Cá Nhân - Chỉnh Sửa Thông Tin", true);
        this.taiKhoan = tk;
        initUI();
        loadData();
    }

    private void initUI() {
        setSize(480, 650);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- TITLE PANEL ---
        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlTitle.setBackground(Color.WHITE);
        pnlTitle.setBorder(new EmptyBorder(30, 0, 10, 0));
        JLabel lblTitle = new JLabel("Chỉnh Sửa Thông Tin", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 28));
        lblTitle.setForeground(new Color(17, 24, 39));
        pnlTitle.add(lblTitle);
        add(pnlTitle, BorderLayout.NORTH);

        // --- FORM PANEL ---
        JPanel pnlForm = new JPanel();
        pnlForm.setLayout(new BoxLayout(pnlForm, BoxLayout.Y_AXIS));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(10, 40, 30, 40));

        Dimension inputSize = new Dimension(400, 44);

        // Email (Readonly)
        pnlForm.add(createLabel("Email đăng nhập"));
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8)));
        lblEmail = new JLabel("");
        lblEmail.setFont(new Font("Inter", Font.BOLD, 15));
        lblEmail.setForeground(Color.GRAY);
        pnlForm.add(lblEmail);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 20)));

        // Name
        pnlForm.add(createLabel("<html>Họ và Tên <font color='#ef4444'>*</font></html>"));
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8)));
        txtName = new RoundedTextField(6);
        txtName.setPlaceholder("Nhập tên đăng nhập");
        txtName.setMaximumSize(inputSize);
        txtName.setPreferredSize(inputSize);
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(txtName);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 16)));

        // Phone
        pnlForm.add(createLabel("<html>Số Điện Thoại <font color='#ef4444'>*</font></html>"));
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8)));
        txtPhone = new RoundedTextField(6);
        txtPhone.setPlaceholder("Nhập số điện thoại");
        txtPhone.setMaximumSize(inputSize);
        txtPhone.setPreferredSize(inputSize);
        txtPhone.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(txtPhone);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 16)));

        // DOB
        pnlForm.add(createLabel("<html>Ngày sinh (dd/MM/yyyy) <font color='#ef4444'>*</font></html>"));
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8)));
        txtDob = new RoundedTextField(6);
        txtDob.setPlaceholder("dd/mm/yyyy");
        txtDob.setMaximumSize(inputSize);
        txtDob.setPreferredSize(inputSize);
        txtDob.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(txtDob);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 16)));

        // Address
        pnlForm.add(createLabel("<html>Địa chỉ <font color='#ef4444'>*</font></html>"));
        pnlForm.add(Box.createRigidArea(new Dimension(0, 8)));
        txtAddress = new RoundedTextField(6);
        txtAddress.setPlaceholder("Nhập địa chỉ của bạn");
        txtAddress.setMaximumSize(inputSize);
        txtAddress.setPreferredSize(inputSize);
        txtAddress.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(txtAddress);
        pnlForm.add(Box.createRigidArea(new Dimension(0, 30)));

        // Button Save
        RoundedButton btnSave = new RoundedButton("Lưu Thay Đổi", 8);
        btnSave.setBackground(MAU_BUTTON);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Inter", Font.BOLD, 16));
        btnSave.setMaximumSize(new Dimension(400, 48));
        btnSave.setPreferredSize(new Dimension(400, 48));
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveChanges());
        
        btnSave.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnSave.setBackground(MAU_BUTTON_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnSave.setBackground(MAU_BUTTON);
            }
        });
        
        pnlForm.add(btnSave);

        add(pnlForm, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Inter", Font.BOLD, 14));
        lbl.setForeground(MAU_LABEL);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void loadData() {
        if (taiKhoan == null) return;
        lblEmail.setText(taiKhoan.getEmail() + " (Mã: " + taiKhoan.getMaTaiKhoan() + ")");
        if (taiKhoan instanceof Chu) {
            Chu c = (Chu) taiKhoan;
            txtName.setText(c.getHoTen());
            txtPhone.setText(c.getSoDienThoai());
            txtAddress.setText(c.getDiaChi());
            if (c.getNgaySinh() != null) {
                txtDob.setText(c.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        } else if (taiKhoan instanceof QuanLy) {
            QuanLy q = (QuanLy) taiKhoan;
            txtName.setText(q.getHoTen());
            txtPhone.setText(q.getSoDienThoai());
            txtAddress.setText(q.getDiaChi());
            if (q.getNgaySinh() != null) {
                txtDob.setText(q.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }
    }

    private void saveChanges() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String dobStr = txtDob.getText().trim();
        String address = txtAddress.getText().trim();

        if (name.isEmpty()) { 
            ValidationPopup.show(txtName, "Họ và tên không được để trống!"); 
            return; 
        }
        if (phone.isEmpty() || !phone.matches("^0\\d{9}$")) { 
            ValidationPopup.show(txtPhone, "Số điện thoại bắt đầu bằng số 0 và gồm độ dài 10 số!"); 
            return; 
        }
        if (address.isEmpty()) {
            ValidationPopup.show(txtAddress, "Địa chỉ không được để trống!");
            return;
        }

        LocalDate dob = null;
        if (dobStr.isEmpty()) {
            ValidationPopup.show(txtDob, "Vui lòng nhập ngày sinh!");
            return;
        }
        
        try {
            dob = LocalDate.parse(dobStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException ex) {
            ValidationPopup.show(txtDob, "Ngày sinh không hợp lệ (Định dạng đúng: dd/MM/yyyy)!");
            return;
        }

        // Cập nhật thông tin vào đối tượng
        if (taiKhoan instanceof Chu) {
            Chu c = (Chu) taiKhoan;
            c.setHoTen(name);
            c.setSoDienThoai(phone);
            c.setNgaySinh(dob);
            c.setDiaChi(address);
        } else if (taiKhoan instanceof QuanLy) {
            QuanLy q = (QuanLy) taiKhoan;
            q.setHoTen(name);
            q.setSoDienThoai(phone);
            q.setNgaySinh(dob);
            q.setDiaChi(address);
        }

        // Cập nhật vào CSDL
        TaiKhoanDAO dao = new TaiKhoanDAO();
        if (dao.updateThongTinCaNhan(taiKhoan)) {
            ui.util.MessageDialog.show(this, "Thành Công!", "Cập nhật thông tin tài khoản thành công.", ui.util.MessageDialog.MessageType.SUCCESS);
            this.dispose();
            if (getParent() instanceof TrangChu) {
                ((TrangChu) getParent()).updateUserInfo(); // Không Load lại Panel, chỉ update Text
            }
        } else {
            ui.util.MessageDialog.show(this, "Lỗi Hệ Thống!", "Có lỗi xảy ra khi lưu vào CSDL. Vui lòng thử lại.", ui.util.MessageDialog.MessageType.ERROR);
        }
    }
}
