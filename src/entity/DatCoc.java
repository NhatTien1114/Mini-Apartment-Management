package entity;

import java.time.LocalDate;

public class DatCoc {
    private int maDatCoc;
    private String maPhong;
    private String hoTen;
    private String soCCCD;
    private double soTien;
    private LocalDate ngayDatCoc;
    private int soNgay; // Số ngày giữ chỗ (mặc định 3)

    public DatCoc() {}

    public DatCoc(int maDatCoc, String maPhong, String hoTen, String soCCCD, double soTien, LocalDate ngayDatCoc, int soNgay) {
        this.maDatCoc = maDatCoc;
        this.maPhong = maPhong;
        this.hoTen = hoTen;
        this.soCCCD = soCCCD;
        this.soTien = soTien;
        this.ngayDatCoc = ngayDatCoc;
        this.soNgay = soNgay;
    }

    public int getMaDatCoc() { return maDatCoc; }
    public void setMaDatCoc(int maDatCoc) { this.maDatCoc = maDatCoc; }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSoCCCD() { return soCCCD; }
    public void setSoCCCD(String soCCCD) { this.soCCCD = soCCCD; }

    public double getSoTien() { return soTien; }
    public void setSoTien(double soTien) { this.soTien = soTien; }

    public LocalDate getNgayDatCoc() { return ngayDatCoc; }
    public void setNgayDatCoc(LocalDate ngayDatCoc) { this.ngayDatCoc = ngayDatCoc; }

    public int getSoNgay() { return soNgay; }
    public void setSoNgay(int soNgay) { this.soNgay = soNgay; }

    /**
     * Kiểm tra xem đặt cọc đã hết hạn chưa.
     */
    public boolean isHetHan() {
        if (ngayDatCoc == null) return true;
        return LocalDate.now().isAfter(ngayDatCoc.plusDays(soNgay));
    }
}
