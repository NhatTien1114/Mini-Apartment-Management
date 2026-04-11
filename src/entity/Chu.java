package entity;

import java.time.LocalDate;

public class Chu extends TaiKhoan {
    private String hoTen;
    private String soDienThoai;
    private LocalDate ngaySinh;
    private String diaChi;

    public Chu() {
        super();
        this.role = Role.CHU;
    }

    public Chu(String maChu, String email, String matKhau, String hoTen, String soDienThoai, LocalDate ngaySinh, String diaChi) {
        super(maChu, email, matKhau, Role.CHU);
        setHoTen(hoTen);
        setSoDienThoai(soDienThoai);
        setNgaySinh(ngaySinh);
        setDiaChi(diaChi);
    }

    public String getMaChu() { return getMaTaiKhoan(); }

    public void setMaChu(String maChu) { setMaTaiKhoan(maChu); }

    public String getHoTen() { return hoTen; }

    public void setHoTen(String hoTen) {
        this.hoTen = (hoTen == null) ? "" : hoTen;
    }

    public String getSoDienThoai() { return soDienThoai; }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = (soDienThoai == null) ? "" : soDienThoai;
    }

    public LocalDate getNgaySinh() { return ngaySinh; }

    public void setNgaySinh(LocalDate ngaySinh) {
        if(ngaySinh == null) throw new IllegalArgumentException("Ngày sinh không hợp lệ");
        int year = ngaySinh.getYear();
        if(year <= 1900 || year >= 2026) throw new IllegalArgumentException("Năm sinh phải > 1900 và < 2026");
        this.ngaySinh = ngaySinh;
    }

    public String getDiaChi() { return diaChi; }

    public void setDiaChi(String diaChi) {
        this.diaChi = (diaChi == null) ? "" : diaChi;
    }

    public boolean suaThongTinCaNhan() {
        return true; 
    }

    @Override
    public String toString() {
        return "Chu{" +
                "maChu='" + getMaChu() + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", soDienThoai='" + soDienThoai + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", diaChi='" + diaChi + '\'' +
                ", email='" + getEmail() + '\'' + // Assuming getTenDangNhap() returns the email from TaiKhoan
                '}';
    }
}
