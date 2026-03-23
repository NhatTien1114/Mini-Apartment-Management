package entity;

import java.time.LocalDate;

public class QuanLy extends TaiKhoan {
    private String hoTen;
    private String soDienThoai;
    private LocalDate ngaySinh;
    private String diaChi;

    public QuanLy() {
        super();
        this.role = Role.QUAN_LY;
    }

    public QuanLy(String maQuanLy, String email, String matKhau, String hoTen, String soDienThoai, LocalDate ngaySinh, String diaChi) {
        super(maQuanLy, email, matKhau, Role.QUAN_LY);
        setHoTen(hoTen);
        setSoDienThoai(soDienThoai);
        setNgaySinh(ngaySinh);
        setDiaChi(diaChi);
    }

    public String getMaQuanLy() { return getMaTaiKhoan(); }

    public void setMaQuanLy(String maQuanLy) { setMaTaiKhoan(maQuanLy); }

    public String getHoTen() { return hoTen; }

    public void setHoTen(String hoTen) {
        if(hoTen == null || hoTen.trim().isEmpty()) throw new IllegalArgumentException("Họ tên không được rỗng");
        this.hoTen = hoTen;
    }

    public String getSoDienThoai() { return soDienThoai; }

    public void setSoDienThoai(String soDienThoai) {
        if(soDienThoai == null || !soDienThoai.matches("^0\\d{9}$")) {
            throw new IllegalArgumentException("Dãy số gồm có 10 số và bắt đầu là số 0");
        }
        this.soDienThoai = soDienThoai;
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
        if(diaChi == null || diaChi.trim().isEmpty()) throw new IllegalArgumentException("Địa chỉ không rỗng");
        this.diaChi = diaChi;
    }

    public boolean suaThongTinCaNhan() {
        return true; 
    }

    @Override
    public String toString() {
        return "QuanLy{" +
                "maQuanLy='" + getMaQuanLy() + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", soDienThoai='" + soDienThoai + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", diaChi='" + diaChi + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
