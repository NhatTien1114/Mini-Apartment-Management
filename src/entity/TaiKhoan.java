package entity;

public class TaiKhoan {
    public enum Role {
        CHU(0),
        QUAN_LY(1);

        private final int value;
        Role(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    protected String maTaiKhoan;
    protected String email; // Đổi tênDangNhap thành email theo docs
    protected String matKhau;
    protected Role role;

    public TaiKhoan() {
    }

    public TaiKhoan(String maTaiKhoan, String email, String matKhau, Role role) {
        this.maTaiKhoan = maTaiKhoan;
        setEmail(email);
        setMatKhau(matKhau);
        setRole(role);
    }

    public String getMaTaiKhoan() { return maTaiKhoan; }
    
    public void setMaTaiKhoan(String maTaiKhoan) {
        if(maTaiKhoan == null || maTaiKhoan.trim().isEmpty()) throw new IllegalArgumentException("Mã tài khoản không rỗng");
        this.maTaiKhoan = maTaiKhoan;
    }

    public String getEmail() { return email; }
    
    public void setEmail(String email) {
        if(email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        this.email = email;
    }

    public String getMatKhau() { return matKhau; }
    
    public void setMatKhau(String matKhau) {
        if (matKhau == null || matKhau.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        this.matKhau = matKhau;
    }

    public Role getRole() { return role; }
    
    public void setRole(Role role) { this.role = role; }

    @Override
    public String toString() {
        return "TaiKhoan{" +
                "maTaiKhoan='" + maTaiKhoan + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
