package entity;

public class HoaDonDetail {
    private String maChiTiet;
    private String maHoaDon;
    private String maDichVu;
    private int soLuong;
    private String tenKhoan;
    private double donGia;
    private double thanhTien; // Dùng để chứa dữ liệu khi SELECT từ SQL lên

    // 1. Constructor mặc định (Bắt buộc phải có)
    public HoaDonDetail() {
    }

    // 2. Constructor không có thanhTien
    // (Dùng khi bạn tạo mới chi tiết trong code để chuẩn bị Insert vào DB)
    public HoaDonDetail(String maChiTiet, String maHoaDon, String maDichVu, int soLuong, String tenKhoan, double donGia) {
        this.maChiTiet = maChiTiet;
        this.maHoaDon = maHoaDon;
        this.maDichVu = maDichVu;
        this.soLuong = soLuong;
        this.tenKhoan = tenKhoan;
        this.donGia = donGia;
    }

    // 3. Constructor đầy đủ
    // (Dùng khi bạn Select dữ liệu từ DB lên, lúc này DB đã tính xong thanhTien)
    public HoaDonDetail(String maChiTiet, String maHoaDon, String maDichVu, int soLuong, String tenKhoan, double donGia, double thanhTien) {
        this.maChiTiet = maChiTiet;
        this.maHoaDon = maHoaDon;
        this.maDichVu = maDichVu;
        this.soLuong = soLuong;
        this.tenKhoan = tenKhoan;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    // ==========================================
    // CÁC HÀM GETTER VÀ SETTER
    // ==========================================

    public String getMaChiTiet() {
        return maChiTiet;
    }

    public void setMaChiTiet(String maChiTiet) {
        this.maChiTiet = maChiTiet;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getTenKhoan() {
        return tenKhoan;
    }

    public void setTenKhoan(String tenKhoan) {
        this.tenKhoan = tenKhoan;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }
}
