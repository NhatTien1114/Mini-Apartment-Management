package entity;

import java.time.LocalDate;

public class GiaHeader {

    private String maGiaHeader;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private String moTa;
    private int trangThai;
    private String ghiChu;
    private int loai; // 0=Phong, 1=DichVu

    public GiaHeader() {
    }

    public GiaHeader(String maGiaHeader, LocalDate ngayBatDau, LocalDate ngayKetThuc,
            String moTa, int trangThai, String ghiChu, int loai) {
        this.maGiaHeader = maGiaHeader;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.moTa = moTa;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.loai = loai;
    }

    public String getMaGiaHeader() {
        return maGiaHeader;
    }

    public void setMaGiaHeader(String maGiaHeader) {
        this.maGiaHeader = maGiaHeader;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public int getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(int trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public int getLoai() {
        return loai;
    }

    public void setLoai(int loai) {
        this.loai = loai;
    }
}
