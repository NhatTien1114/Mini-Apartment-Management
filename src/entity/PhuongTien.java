package entity;

public class PhuongTien {
    private String bienSo;
    private String loaiXe;
    private String maKhachHang;
    private String tenKhachHang; // for display
    private String maPhong;
    private double mucPhi;

    public PhuongTien() {}

    public PhuongTien(String bienSo, String loaiXe, String maKhachHang, String tenKhachHang, String maPhong, double mucPhi) {
        this.bienSo = bienSo;
        this.loaiXe = loaiXe;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.maPhong = maPhong;
        this.mucPhi = mucPhi;
    }

    public String getBienSo() { return bienSo; }
    public void setBienSo(String bienSo) { this.bienSo = bienSo; }
    public String getLoaiXe() { return loaiXe; }
    public void setLoaiXe(String loaiXe) { this.loaiXe = loaiXe; }
    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }
    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }
    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }
    public double getMucPhi() { return mucPhi; }
    public void setMucPhi(double mucPhi) { this.mucPhi = mucPhi; }
}
