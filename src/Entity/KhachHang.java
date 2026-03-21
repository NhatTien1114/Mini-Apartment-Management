package entity;

import java.time.LocalDate;

public class KhachHang {
    private String maKhachHang;
    private String hoTen;
    private String soDienThoai;
    private LocalDate ngaySinh;
    private String soCCCD;
    private String diaChi;
    private Phong phong;

    public KhachHang(String maKhachHang, String hoTen, String soDienThoai, LocalDate ngaySinh, String soCCCD, String diaChi, Phong phong) {
        this.maKhachHang = maKhachHang;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.ngaySinh = ngaySinh;
        this.soCCCD = soCCCD;
        this.diaChi = diaChi;
        this.phong = phong;
    }

    public KhachHang(){}

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public String getSoCCCD() {
        return soCCCD;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public Phong getPhong() {
        return phong;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public void setSoCCCD(String soCCCD) {
        this.soCCCD = soCCCD;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public void setPhong(Phong phong) {
        this.phong = phong;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("KhachHang{");
        sb.append("maKhachHang=").append(maKhachHang);
        sb.append(", hoTen=").append(hoTen);
        sb.append(", soDienThoai=").append(soDienThoai);
        sb.append(", ngaySinh=").append(ngaySinh);
        sb.append(", soCCCD=").append(soCCCD);
        sb.append(", diaChi=").append(diaChi);
        sb.append(", phong=").append(phong);
        sb.append('}');
        return sb.toString();
    }

}
